/*
 * Copyright (C) 2017 CMCC, Inc. and others. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.usecaseui.server.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.usecaseui.server.bean.AlarmsHeader;
import org.onap.usecaseui.server.bean.PerformanceHeader;
import org.onap.usecaseui.server.bean.PerformanceInformation;
import org.onap.usecaseui.server.bo.PerformanceBo;
import org.onap.usecaseui.server.constant.Constant;
import org.onap.usecaseui.server.service.PerformanceHeaderService;
import org.onap.usecaseui.server.service.PerformanceInformationService;
import org.onap.usecaseui.server.util.CSVUtils;
import org.onap.usecaseui.server.util.DateUtils;
import org.onap.usecaseui.server.util.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.Data;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@RestController
@Configuration
@EnableAspectJAutoProxy
public class PerformanceController {

    @Resource(name = "PerformanceHeaderService")
    private PerformanceHeaderService performanceHeaderService;

    @Resource(name = "PerformanceInformationService")
    private PerformanceInformationService performanceInformationService;

    private Logger logger = LoggerFactory.getLogger(PerformanceController.class);

    @RequestMapping(value = {"/performance/{currentPage}/{pageSize}","/performance/{currentPage}/{pageSize}/{eventId}/{eventName}/{name}/{value}/{createTime}"},method = RequestMethod.GET, produces = "application/json")
    public String getPerformanceData(HttpServletResponse response,@PathVariable int currentPage,
                                     @PathVariable int pageSize,@PathVariable(required = false) String eventId,
                                     @PathVariable(required = false) String eventName,@PathVariable(required = false) String name,
                                     @PathVariable(required = false) String value,@PathVariable(required = false) String createTime)  {
        List<Object> list = new ArrayList<>();
        if (null != eventId || null != eventName || null != name || null != value || null != createTime){
            PerformanceHeader performanceHeader = new PerformanceHeader();
            performanceHeader.setEventId(!"null".equals(eventId)?eventId:null);
            performanceHeader.setEventName(!"null".equals(eventName)?eventName:null);
            try {
                performanceHeader.setCreateTime(!"null".equals(createTime)?DateUtils.stringToDate(createTime):null);
            } catch (ParseException e) {
                 if (null != response)
                    response.setStatus(400);
                logger.error("ParseException["+createTime+"]:"+e.getMessage());
                return "{'result':'error'}";
            }
            List<PerformanceHeader> performanceHeaders = performanceHeaderService.queryPerformanceHeader(performanceHeader,currentPage,pageSize).getList();
            performanceHeaders.forEach( per ->{
                PerformanceBo pbo = new PerformanceBo();
                PerformanceInformation pe = new PerformanceInformation();
                pe.setEventId(per.getEventId());
                pe.setName(!"null".equals(name)?name:null);
                pe.setValue(!"null".equals(value)?value:null);
                List<PerformanceInformation> performanceInformations = performanceInformationService.queryPerformanceInformation(pe,1,100).getList();
                pbo.setPerformanceHeader(per);
                pbo.setPerformanceInformation(performanceInformations);
                list.add(pbo);
            });
        }else{
            Page pa = performanceHeaderService.queryPerformanceHeader(null,currentPage,pageSize);
            List<PerformanceHeader> p = pa!=null?pa.getList():null;
            if (null != p && p.size() > 0)
                p.forEach( per ->{
                    PerformanceBo pbo = new PerformanceBo();
                    pbo.setPerformanceHeader(per);
                    pbo.setPerformanceInformation(performanceInformationService.queryPerformanceInformation(new PerformanceInformation(per.getEventId()),1,100).getList());
                    list.add(pbo);
                });
        }
        try {
            ObjectMapper ojm = new ObjectMapper();
            ojm.setDateFormat(new SimpleDateFormat(Constant.DATE_FROMAT));
            return ojm.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            logger.error("JsonProcessingException"+e.getMessage());
            return "{'result':'error'}";
        }
    }

    @RequestMapping(value = {"/performance/genCsv/{eventId}"}, method = RequestMethod.GET, produces = "application/json")
    public String generateCsvFile(HttpServletResponse response, @PathVariable String[] eventId) throws JsonProcessingException {
        String csvFile = "csvFiles/vnf_performance_"+new SimpleDateFormat("yy-MM-ddHH:mm:ss").format(new Date())+".csv";
        String[] headers = new String[]{"version",
                "eventName","domain","eventId","eventType","nfcNamingCode",
                "nfNamingCode","sourceId","sourceName","reportingEntityId",
                "reportingEntityName","priority","startEpochMicrosec","lastEpochMicroSec",
                "sequence","measurementsForVfScalingVersion","measurementInterval",
                "createTime","updateTime","value","name"};
        List<PerformanceHeader> performanceHeaders = performanceHeaderService.queryId(eventId);
        if (null == performanceHeaders || performanceHeaders.size() < 0)
            return new ObjectMapper().writeValueAsString("selected eventId don't exist");
        List<String[]> csvData = new ArrayList<>();
        performanceHeaders.forEach(s ->{
            List<PerformanceInformation> information = performanceInformationService.queryPerformanceInformation(new PerformanceInformation(s.getEventId()),1,100).getList();
            String names = new String();
            String values = new String();
            if (0 < information.size() && null != information){
                for (PerformanceInformation a : information){
                    names += a.getName()+",";
                    values += a.getValue()+",";
                }
                names = names.substring(0,names.lastIndexOf(','));
                values = values.substring(0,values.lastIndexOf(','));
            }
            csvData.add(new String[]{
                s.getVersion(),s.getEventName(),s.getDomain(),s.getEventId(),s.getEventType(),s.getNfcNamingCode(),s.getNfNamingCode(),
                    s.getSourceId(),s.getSourceName(),s.getReportingEntityId(),s.getReportingEntityName(),s.getPriority(),
                    s.getStartEpochMicrosec(),s.getLastEpochMicroSec(),s.getSequence(),s.getMeasurementsForVfScalingVersion(),
                    s.getMeasurementInterval(),DateUtils.dateToString(s.getCreateTime()),DateUtils.dateToString(s.getUpdateTime()),
                    names,values
            });
        });
        CSVUtils.writeCsv(headers,csvData,csvFile);
        if (null != response){
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/csv");
            response.setHeader("Content-Disposition","attachment;filename="+csvFile);
            try(InputStream is = new FileInputStream(csvFile);
                OutputStream os = response.getOutputStream()){
                byte[] b = new byte[2048];
                int length;
                while ((length = is.read(b)) > 0) {
                    os.write(b, 0, length);
                }
                return "{'result':'success'}";
            }catch (IOException e){
                logger.error("download csv File error :"+e.getMessage());
                return "{'result':'failed'}";
            }
        }else
            return "csvFile generate success,response is null,don't download to local";

    }

    @RequestMapping(value = {"/performance/genDiaCsv"}, method = RequestMethod.POST, produces = "application/json")
    public String generateDiaCsvFile(HttpServletResponse response,@RequestBody Map<String,String> p) {
        String csvFileName = "csvFiles/"+p.get("name")+"_"+new SimpleDateFormat("yy-MM-ddHH:mm:ss").format(new Date())+".csv";
        try{
            String[] headers = new String[]{"eventId","name","value","createTime","updateTime"};
            List<String[]> csvDatas = new ArrayList<>();
            if (null != p){
                StringBuffer fileData = new StringBuffer();
                p.forEach((k,v)->{
                    fileData.append(v+",");
                });
                csvDatas.add(fileData.toString().split(","));
            }
            CSVUtils.writeCsv(headers,csvDatas,csvFileName);
        }catch (Exception pe){
            logger.error(pe.getMessage());
        }
        if (null != response){
            response.setContentType("application/csv");
            response.setHeader("Content-Disposition","attachment;filename="+csvFileName+"");
            try(InputStream is = new FileInputStream(csvFileName);
                OutputStream os = response.getOutputStream()){
                byte[] b = new byte[2048];
                int length;
                while ((length = is.read(b)) > 0) {
                    os.write(b, 0, length);
                }
                return "{'result':'success'}";
            }catch (IOException e){
                logger.error("download csv File error :"+e.getMessage());
                return "{'result':'failed'}";
            }
        }else
            return "csvFile generate success,response is null,don't download to local";
    }

    @ResponseBody
    @RequestMapping(value = {"/performance/diagram/{unit}/{eventId}"}, method = RequestMethod.GET, produces = "application/json")
    public String generateDiagram(@PathVariable String unit,@PathVariable String eventId) throws ParseException, JsonProcessingException {
        String revDate = "";
        switch (unit){
            case "hour":
                revDate = DateUtils.initDate(new Date(),1,1,1,-1,0,0);
                break;
            case "day":
                revDate = DateUtils.initDate(new Date(),1,1,-1,0,0,0);
                break;
            case "month":
                revDate = DateUtils.initDate(new Date(),1,-1,0,0,0,0);
                break;
            case "year":
                revDate = DateUtils.initDate(new Date(),-1,0,0,0,0,0);
                break;
        }
        PerformanceInformation performanceInformation = new PerformanceInformation();
        performanceInformation.setEventId(eventId);
        performanceInformation.setCreateTime(DateUtils.stringToDate(revDate));
        List<PerformanceInformation> informations = performanceInformationService.queryPerformanceInformation(performanceInformation,1,100).getList();
        ObjectMapper ojm = new ObjectMapper();
        ojm.setDateFormat(new SimpleDateFormat(Constant.DATE_FROMAT));
        return ojm.writeValueAsString(informations);
    }

}