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
package org.onap.usecaseui.server.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.client.ClientConfig;
import org.onap.usecaseui.server.bean.AlarmsHeader;
import org.onap.usecaseui.server.bean.AlarmsInformation;
import org.onap.usecaseui.server.bean.PerformanceHeader;
import org.onap.usecaseui.server.bean.PerformanceInformation;
import org.onap.usecaseui.server.constant.Constant;
import org.onap.usecaseui.server.service.AlarmsHeaderService;
import org.onap.usecaseui.server.service.AlarmsInformationService;
import org.onap.usecaseui.server.service.PerformanceHeaderService;
import org.onap.usecaseui.server.service.PerformanceInformationService;
import org.onap.usecaseui.server.service.impl.AlarmsHeaderServiceImpl;
import org.onap.usecaseui.server.service.impl.AlarmsInformationServiceImpl;
import org.onap.usecaseui.server.service.impl.PerformanceHeaderServiceImpl;
import org.onap.usecaseui.server.service.impl.PerformanceInformationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class DmaapSubscriber implements Runnable {

    private Logger logger = LoggerFactory.getLogger(DmaapSubscriber.class);

    private String url;
    private String alarmTopic;
    private String performanceTopic;
    private String consumerGroup;
    private String consumer;
    private int timeout;

    private boolean isActive = true;

    @Resource(name = "AlarmsHeaderService")
    private AlarmsHeaderService alarmsHeaderService;

    @Resource(name = "AlarmsInformationService")
    private AlarmsInformationService alarmsInformationService;

    @Resource(name = "PerformanceHeaderService")
    private PerformanceHeaderService performanceHeaderService;

    @Resource(name = "PerformanceInformationService")
    private PerformanceInformationService performanceInformationService;

    public void subscribe(String topic) {
        try {
            List<String> respList = getDMaaPData(topic);
            if (respList.size() <= 0 || respList == null) {
                logger.info("response is null");
                return;
            }
            ObjectMapper objMapper = new ObjectMapper();
            objMapper.setDateFormat(new SimpleDateFormat(Constant.DATE_FORMAT));
            respList.forEach(rl -> {
                logger.info(rl);
                try {
                    Map<String, Object> eventMaps = (Map<String, Object>) objMapper.readValue(rl, Map.class).get("event");
                    if (eventMaps.containsKey("measurementsForVfScalingFields")) {
                        performanceProcess(eventMaps);
                    } else if (eventMaps.containsKey("faultFields")) {
                        alarmProcess(eventMaps);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getDMaaP Information failed :" + e.getMessage());
        }
    }

    private List<String> getDMaaPData(String topic) {
        Client client = ClientBuilder.newClient(new ClientConfig());
        WebTarget webTarget = client.target(url + "/" + topic + "/" + consumerGroup + "/" + consumer);
        logger.info("target:" + url + "/" + topic + "/" + consumerGroup + "/" + consumer);
        Response response = webTarget.queryParam("timeout", timeout).request().get();
        return response.readEntity(List.class);
    }

    private void initConfig() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("dmaap.properties");
        Properties p = new Properties();
        try {
            p.load(inputStream);
            this.url = p.getProperty("dmaap.url") + System.getenv("MR_ADDR");
            this.alarmTopic = p.getProperty("dmaap.alarmTopic");
            this.performanceTopic = p.getProperty("dmaap.performanceTopic");
            this.consumerGroup = p.getProperty("dmaap.consumerGroup");
            this.consumer = p.getProperty("dmaap.consumer");
            this.timeout = Integer.parseInt(p.getProperty("dmaap.timeout"));
        } catch (IOException e1) {
            logger.error("get configuration file arise error :" + e1.getMessage());
        }
    }

    public void run() {
        try {
            initConfig();
            while (isActive) {
                logger.info("alarm data subscription is starting......");
                subscribe(alarmTopic);
                logger.info("alarm data subscription has finished.");
                logger.info("performance data subscription is starting......");
                subscribe(performanceTopic);
                logger.info("performance data subscription has finished.");
            }
        } catch (Exception e) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            logger.error("subscribe raise error :" + e.getCause());
            run();
        }
    }

    private void alarmProcess(Map<String, Object> eventMap) {
        AlarmsHeader alarm_header = new AlarmsHeader();
        List<AlarmsInformation> alarm_informations = new ArrayList<>();
        eventMap.forEach((ek1, ev1) -> {
            if (ek1.equals("commonEventHeader")) {
                ((Map<String, Object>) ev1).forEach((k2, v2) -> {
                    if (k2.equals("version"))
                        alarm_header.setVersion(v2.toString());
                    if (k2.equals("eventName"))
                        alarm_header.setEventName(v2.toString());
                    if (k2.equals("domain"))
                        alarm_header.setDomain(v2.toString());
                    if (k2.equals("eventId"))
                        alarm_header.setEventId(v2.toString());
                    if (k2.equals("eventType"))
                        alarm_header.setEventType(v2.toString());
                    if (k2.equals("nfcNamingCode"))
                        alarm_header.setNfcNamingCode(v2.toString());
                    if (k2.equals("nfNamingCode"))
                        alarm_header.setNfNamingCode(v2.toString());
                    if (k2.equals("sourceId"))
                        alarm_header.setSourceId(v2.toString());
                    if (k2.equals("sourceName"))
                        alarm_header.setSourceName(v2.toString());
                    if (k2.equals("reportingEntityId"))
                        alarm_header.setReportingEntityId(v2.toString());
                    if (k2.equals("reportingEntityName"))
                        alarm_header.setReportingEntityName(v2.toString());
                    if (k2.equals("priority"))
                        alarm_header.setPriority(v2.toString());
                    if (k2.equals("startEpochMicrosec"))
                        alarm_header.setStartEpochMicrosec(v2.toString());
                    if (k2.equals("lastEpochMicrosec"))
                        alarm_header.setLastEpochMicroSec(v2.toString());
                    if (k2.equals("sequence"))
                        alarm_header.setSequence(v2.toString());
                });
            } else if (ek1.equals("faultFields")) {
                ((Map<String, Object>) ev1).forEach((k3, v3) -> {
                    if (k3.equals("faultFieldsVersion"))
                        alarm_header.setFaultFieldsVersion(v3.toString());
                    if (k3.equals("eventSeverity"))
                        alarm_header.setEventServrity(v3.toString());
                    if (k3.equals("eventSourceType"))
                        alarm_header.setEventSourceType(v3.toString());
                    if (k3.equals("eventCategory"))
                        alarm_header.setEventCategory(v3.toString());
                    if (k3.equals("alarmCondition"))
                        alarm_header.setAlarmCondition(v3.toString());
                    if (k3.equals("specificProblem"))
                        alarm_header.setSpecificProblem(v3.toString());
                    if (k3.equals("vfStatus"))
                        alarm_header.setVfStatus(v3.toString());
                    if (k3.equals("alarmInterfaceA"))
                        alarm_header.setAlarmInterfaceA(v3.toString());
                    if (k3.equals("alarmAdditionalInformation")) {
                        try {
                            List<Map<String, Object>> m = (List<Map<String, Object>>) v3;
                            m.forEach(i -> {
                                if (i.get("name").toString().equals("eventTime"))
                                    try {
                                        alarm_header.setCreateTime(DateUtils.stringToDate(i.get("value").toString()));
                                        alarm_header.setUpdateTime(DateUtils.now());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                else {
                                    alarm_informations.add(new AlarmsInformation(i.get("name").toString(), i.get("value").toString(), alarm_header.getSourceId(), null, new Date()));
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("convert alarmAdditionalInformation error：" + e.getMessage());
                        }
                    }
                });
                alarm_informations.forEach(ai -> {
                    ai.setCreateTime(alarm_header.getCreateTime());
                    ai.setUpdateTime(new Date());
                });
                if (alarm_header.getEventName().contains("Cleared")) {
                    alarm_header.setStatus("3");
                    logger.info("alarmCleared data header insert is starting......");
                    alarmsHeaderService.saveAlarmsHeader(alarm_header);
                    logger.info("alarmCleared data header insert has finished.");
                    logger.info("alarmCleared data detail insert is starting......");
                    alarm_informations.forEach(information ->
                            alarmsInformationService.saveAlarmsInformation(information));
                    logger.info("alarmCleared data detail insert has finished. " + alarm_informations.size() + " records have been inserted.");
                    AlarmsHeader header1 = new AlarmsHeader();
                    header1.setEventName(alarm_header.getEventName().substring(0, alarm_header.getEventName().indexOf("Cleared")));
                    List<AlarmsHeader> alarmsHeaders = alarmsHeaderService.queryAlarmsHeader(header1, 1, 10).getList();
                    alarmsHeaders.forEach(alarms -> {
                        alarms.setStatus("2");
                        alarms.setUpdateTime(new Date());
                        alarmsHeaderService.updateAlarmsHeader(alarms);
                    });
                } else {
                    alarm_header.setUpdateTime(new Date());
                    alarm_header.setStatus("1");
                    logger.info("alarm data header insert is starting......");
                    alarmsHeaderService.saveAlarmsHeader(alarm_header);
                    logger.info("alarm data header insert has finished.");
                    logger.info("alarm data detail insert is starting......");
                    alarm_informations.forEach(information ->
                            alarmsInformationService.saveAlarmsInformation(information));
                    logger.info("alarm data detail insert has finished. " + alarm_informations.size() + " records have been inserted.");
                }
            }
        });
    }

    private void performanceProcess(Map<String, Object> eventMap) {
        PerformanceHeader performance_header = new PerformanceHeader();
        List<PerformanceInformation> performance_informations = new ArrayList<>();
        eventMap.forEach((ek1, ev1) -> {
            if (ek1.equals("commonEventHeader")) {
                ((Map<String, Object>) ev1).forEach((k2, v2) -> {
                    if (k2.equals("version"))
                        performance_header.setVersion(v2.toString());
                    if (k2.equals("eventName"))
                        performance_header.setEventName(v2.toString());
                    if (k2.equals("domain"))
                        performance_header.setDomain(v2.toString());
                    if (k2.equals("eventId"))
                        performance_header.setEventId(v2.toString());
                    if (k2.equals("eventType"))
                        performance_header.setEventType(v2.toString());
                    if (k2.equals("nfcNamingCode"))
                        performance_header.setNfcNamingCode(v2.toString());
                    if (k2.equals("nfNamingCode"))
                        performance_header.setNfNamingCode(v2.toString());
                    if (k2.equals("sourceId"))
                        performance_header.setSourceId(v2.toString());
                    if (k2.equals("sourceName"))
                        performance_header.setSourceName(v2.toString());
                    if (k2.equals("reportingEntityId"))
                        performance_header.setReportingEntityId(v2.toString());
                    if (k2.equals("reportingEntityName"))
                        performance_header.setReportingEntityName(v2.toString());
                    if (k2.equals("priority"))
                        performance_header.setPriority(v2.toString());
                    if (k2.equals("startEpochMicrosec"))
                        performance_header.setStartEpochMicrosec(v2.toString());
                    if (k2.equals("lastEpochMicrosec"))
                        performance_header.setLastEpochMicroSec(v2.toString());
                    if (k2.equals("sequence"))
                        performance_header.setSequence(v2.toString());
                });
            } else if (ek1.equals("measurementsForVfScalingFields")) {
                ((Map<String, Object>) ev1).forEach((k3, v3) -> {
                    if (k3.equals("measurementsForVfScalingVersion"))
                        performance_header.setMeasurementsForVfScalingVersion(v3.toString());
                    if (k3.equals("measurementInterval"))
                        performance_header.setMeasurementInterval(v3.toString());
                    if (k3.equals("additionalMeasurements")) {
                        try {
                            List<Map<String, Object>> m = (List<Map<String, Object>>) v3;
                            m.forEach(i -> {
                                if (i.containsKey("arrayOfFields")){
                                    List<Map<String,String>> arrayOfFields = (List<Map<String, String>>) i.get("arrayOfFields");
                                    arrayOfFields.forEach( fields -> {
                                        if (fields.get("name").equals("StartTime")){
                                            try {
                                                performance_informations.add(new PerformanceInformation(fields.get("name"),fields.get("value"),performance_header.getSourceId(),null,DateUtils.now()));
                                                performance_header.setCreateTime(DateUtils.stringToDate(fields.get("value").replaceAll(Constant.RegEX_DATE_FORMAT," ")));
                                                performance_header.setUpdateTime(DateUtils.now());
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }else{
                                            try {
                                                performance_informations.add(new PerformanceInformation(fields.get("name"),fields.get("value"),performance_header.getSourceId(),null,DateUtils.now()));
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } );
                                }

                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("convert performanceAdditionalInformation error：" + e.getMessage());
                        }
                    }
                });

                logger.info("performance data header insert is starting......");
                performanceHeaderService.savePerformanceHeader(performance_header);
                logger.info("performance data header insert has finished.");
                logger.info("performance data detail insert is starting......");
                performance_informations.forEach(ai -> {
                    ai.setCreateTime(performance_header.getCreateTime());
                    performanceInformationService.savePerformanceInformation(ai);
                });
                logger.info("performance data detail insert has finished. " + performance_informations.size() + " records have been inserted.");
            }
        });


    }
}