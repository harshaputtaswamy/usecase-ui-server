/*
 * Copyright (C) 2017 CTC, Inc. and others. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.onap.usecaseui.server.bean.HttpResponseResult;
import org.onap.usecaseui.server.bean.csmf.SlicingOrder;
import org.onap.usecaseui.server.bean.csmf.SlicingOrderDetail;
import org.onap.usecaseui.server.bean.intent.IntentInstance;
import org.onap.usecaseui.server.bean.intent.IntentModel;
import org.onap.usecaseui.server.bean.intent.IntentResponseBody;
import org.onap.usecaseui.server.bean.nsmf.common.ServiceResult;
import org.onap.usecaseui.server.controller.csmf.SlicingController;
import org.onap.usecaseui.server.service.csmf.SlicingService;
import org.onap.usecaseui.server.service.intent.IntentApiService;
import org.onap.usecaseui.server.service.intent.IntentInstanceService;
import org.onap.usecaseui.server.service.intent.IntentService;
import org.onap.usecaseui.server.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import retrofit2.Call;
import retrofit2.Response;

@RestController
@org.springframework.context.annotation.Configuration
@EnableAspectJAutoProxy
@CrossOrigin(origins = "*")
@RequestMapping("/intent")
public class IntentController {
    private final Logger logger = LoggerFactory.getLogger(IntentController.class);
    private final static String UPLOADPATH = "/home/uui/upload/";
    private final static String NLPLOADPATH = "/home/run/bert-master/upload/";

    @Resource(name = "IntentService")
    private IntentService intentService;

    @Resource(name = "IntentInstanceService")
    private IntentInstanceService intentInstanceService;

    private IntentApiService intentApiService;

    private ObjectMapper omAlarm = new ObjectMapper();

    @Resource(name = "SlicingService")
    private SlicingService slicingService;

    public IntentController() {
        this(RestfulServices.create(IntentApiService.class));
    }
    public IntentController(IntentApiService intentApiService) {
        this.intentApiService = intentApiService;
    }

    @GetMapping(value="/listModel",produces = "application/json;charset=utf8")
    public String getModels() throws JsonProcessingException {
        List<IntentModel> listModels = intentService.listModels();
        return omAlarm.writeValueAsString(listModels);
    }

    @RequestMapping("/uploadModel")
    @ResponseBody
    public String uploadModel (@RequestParam("file") MultipartFile file) {
        String fileName = file.getOriginalFilename();

        String filePath = UPLOADPATH + fileName ;

        File dest = new File(filePath);

        if(!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
            logger.info("create dir, name=" + dest.getParentFile().getName());
        }
        try {

            file.transferTo(dest);
            logger.info("upload file, name = " + dest.getName());
            IntentModel model = new IntentModel();
            model.setModelName(fileName);
            model.setFilePath(filePath);
            model.setCreateTime(DateUtils.dateToString(new Date()));
            float size = dest.length();
            float sizeM = size/1024;
            model.setSize(sizeM);
            model.setActive(0);
            intentService.addModel(model);

            logger.info("save model, " + model.toString());
            return "1";
        } catch (Exception e) {
            logger.error("Details:" + e.getMessage());
            return "0";
        }
    }

    private String deleteModelFile(String modelId){
        String result = "0";
        try{
            IntentModel model = intentService.getModel(modelId);
            if( model==null){
                return result;
            }

            String fileName = model.getModelName();
            String filePath = UPLOADPATH + fileName;
            logger.info("delete model file: " + filePath);
            File dest = new File(filePath);
            if(dest.exists()){
                dest.delete();
                logger.info("delete file OK: " + filePath);
                if (filePath.endsWith(".zip")) {
                    String unzipPath = filePath.substring(0, filePath.length() - 1 - 4);
                    File unZipFile = new File(unzipPath);
                    if (unZipFile.exists()) {
                        unZipFile.delete();
                    }
                }
            }{
                logger.info("file not found: " + filePath);
            }
            result = "1";
        }catch (Exception e){
            logger.error("Details:" + e.getMessage());
            return "0";
        }
        return result;
    }

    @GetMapping(value = {"/activeModel"}, produces = "application/json")
    public String activeModel(@RequestParam String modelId){
        String result = "0";
        try{
            logger.info("update model record status: id=" + modelId);
            IntentModel model = intentService.activeModel(modelId);

            logger.info("active NLP model, model=" + model.getFilePath());
            String dirPath = intentService.activeModelFile(model);
            if (dirPath != null) {
                dirPath = dirPath.replace(UPLOADPATH, NLPLOADPATH);
                load(dirPath);
            }


            result = "1";
        }catch (Exception e) {
            logger.error("Details:" + e.getMessage());
            return "0";
        }

        return result;
    }

    private String load(String dirPath) {

        String url = "http://uui-nlp.onap:33011/api/online/load";
        HashMap<String, String> headers = new HashMap<>();
        String bodyStr = "{" + "\"path\": \""+dirPath+"\"" + "}";
        logger.info("request body: " + bodyStr);

        HttpResponseResult result = HttpUtil.sendPostRequestByJson(url, headers, bodyStr);
        String respContent = result.getResultContent();

        logger.info("NLP api respond: " + String.valueOf(result.getResultCode()));
        logger.info(respContent);

        JSONObject map = JSON.parseObject(respContent);

        String status = map.getString("Status");
        logger.info("load result: " + status);

        return status;
    }

    @DeleteMapping(value = {"/deleteModel"}, produces = "application/json")
    public String deleteModel(@RequestParam String modelId){
        String result = "0";
        try{
            result = deleteModelFile(modelId);

            logger.info("delete model record: id=" + modelId);
            result = intentService.deleteModel(modelId);
        }catch (Exception e) {
            logger.error("Details:" + e.getMessage());
            return "0";
        }

        return result;
    }

    @ResponseBody
    @PostMapping(value = {"/predict"}, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public String predict(@RequestBody Object body) throws ParseException {
        String text = (String)((Map)body).get("text");
        //System.out.println(text);

        String url = "http://uui-nlp.onap:33011/api/online/predict";
        HashMap<String, String> headers = new HashMap<>();
        String bodyStr = "{\"title\": \"predict\", \"text\": \"" + text
                +  "\"}";
        logger.info("request body: " + bodyStr);

        HttpResponseResult result = HttpUtil.sendPostRequestByJson(url, headers, bodyStr);
        String respContent = result.getResultContent();

        logger.info("NLP api respond: " + String.valueOf(result.getResultCode()));
        logger.info(respContent);

        JSONObject map = JSON.parseObject(respContent);

        JSONObject map2 = new JSONObject();

        for (Map.Entry<String, Object> entry:map.entrySet()) {
            logger.debug(entry.getKey()+","+entry.getValue());
            String key = tranlateFieldName(entry.getKey());
            String valueStr = (String) entry.getValue();
            String value = intentService.calcFieldValue(key, valueStr);
            map2.put(key, value);
        }

        logger.info("translate result: " + map2.toJSONString());

        return map2.toJSONString();
    }



    private static String tranlateFieldName(String key){
        String ret = "";
        if(key==null || key.trim().equals(""))
            return ret;

        HashMap<String, String> map = new HashMap<>();
        map.put("Communication service","name");
        map.put("Maximum user devices","maxNumberofUEs");
        map.put("Downlink data rate","expDataRateDL");
        map.put("Time delay","latency");
        map.put("Uplink data rate","expDataRateUL");
        map.put("Resource","resourceSharingLevel");
        map.put("Mobility","uEMobilityLevel");
        map.put("Region","coverageArea");

        ret = map.get(key.trim());
        return ret;
    }

    @IntentResponseBody
    @ResponseBody
    @GetMapping(value = {"/getInstanceId"},
            produces = "application/json")
    public JSONObject getInstanceId() {
        int first = new Random(10).nextInt(8) + 1;
        System.out.println(first);
        int hashCodeV = UUID.randomUUID().toString().hashCode();
        if (hashCodeV < 0) {//有可能是负数
            hashCodeV = -hashCodeV;
        }
        String instanceId = first + String.format("%015d", hashCodeV);
        JSONObject result = new JSONObject();
        result.put("instanceId", instanceId);
        return result;
    }
    @IntentResponseBody
    @GetMapping(value = {"/getInstanceList/{currentPage}/{pageSize}"},
            produces = "application/json")
    public Object getInstanceList(@PathVariable String currentPage, @PathVariable String pageSize) {
        return intentInstanceService.queryIntentInstance(null, Integer.parseInt(currentPage), Integer.parseInt(pageSize));
    }
    @IntentResponseBody
    @ResponseBody
    @PostMapping(value = {"/createIntentInstance"}, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public Object createIntentInstance(@RequestBody Object body) throws IOException {
        String intentInstanceId = (String) ((Map)body).get("instanceId");
        String name = (String) ((Map)body).get("name");
        String lineNum = (String) ((Map)body).get("lineNum");
        String cloudPointName = (String) ((Map)body).get("cloudPointName");
        Map<String, Object> accessPointOne = (Map) ((Map)body).get("accessPointOne");
        String accessPointOneName = MapUtils.getString(accessPointOne, "name");
        int accessPointOneBandWidth = MapUtils.getIntValue(accessPointOne, "bandwidth");

        IntentInstance intentInstance = new IntentInstance();
        intentInstance.setInstanceId(intentInstanceId);
        intentInstance.setName(name);
        intentInstance.setLineNum(lineNum);
        intentInstance.setCloudPointName(cloudPointName);
        intentInstance.setAccessPointOneName(accessPointOneName);
        intentInstance.setAccessPointOneBandWidth(accessPointOneBandWidth);

        int flag = intentInstanceService.createIntentInstance(intentInstance);

        if(flag == 1) {
            return "OK";
        }
        else {
            throw new RuntimeException("create Instance error");
        }
    }

    @IntentResponseBody
    @GetMapping(value = {"/getFinishedInstanceInfo"},
            produces = "application/json")
    public Object getFinishedInstanceInfo() {
        List<IntentInstance> instanceList = intentInstanceService.getFinishedInstanceInfo();
        List<Map<String, Object>> result = new ArrayList<>();
        for (IntentInstance instance : instanceList) {
            Map<String, Object> instanceInfo = new HashMap<>();
            instanceInfo.put("instanceId", instance.getInstanceId());
            instanceInfo.put("name", instance.getName());
            result.add(instanceInfo);
        }
        return result;
    }

    @IntentResponseBody
    @DeleteMapping(value = {"/deleteIntentInstance"},
            produces = "application/json")
    public Object deleteIntentInstance(@RequestParam(value = "instanceId") String instanceId) {
        intentInstanceService.deleteIntentInstance(instanceId);
        return "ok";
    }
    @IntentResponseBody
    @PutMapping(value = {"/activeIntentInstance"},
            produces = "application/json")
    public Object activeIntentInstance(@RequestParam(value = "instanceId") String instanceId) {
        intentInstanceService.activeIntentInstance(instanceId);
        return "ok";
    }
    @IntentResponseBody
    @PutMapping(value = {"/invalidIntentInstance"},
            produces = "application/json")
    public Object invalidIntentInstance(@RequestParam(value = "instanceId") String instanceId) {
        intentInstanceService.invalidIntentInstance(instanceId);
        return "ok";
    }

    @IntentResponseBody
    @PutMapping(value = {"/queryInstancePerformanceData"},
            produces = "application/json")
    public Object queryInstancePerformanceData(@RequestParam(value = "instanceId") String instanceId) {
        return intentInstanceService.queryInstancePerformanceData(instanceId);
    }

    @IntentResponseBody
    @GetMapping(value = {"/queryAccessNodeInfo"},
            produces = "application/json")
    public Object queryAccessNodeInfo() throws IOException{
        return intentInstanceService.queryAccessNodeInfo();
    }
}
