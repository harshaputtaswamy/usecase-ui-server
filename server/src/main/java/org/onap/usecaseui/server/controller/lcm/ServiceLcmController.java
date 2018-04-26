/**
 * Copyright 2016-2017 ZTE Corporation.
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
package org.onap.usecaseui.server.controller.lcm;

import org.onap.usecaseui.server.service.lcm.ServiceLcmService;
import org.onap.usecaseui.server.service.lcm.domain.so.bean.DeleteOperationRsp;
import org.onap.usecaseui.server.service.lcm.domain.so.bean.OperationProgressInformation;
import org.onap.usecaseui.server.service.lcm.domain.so.bean.ServiceOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@org.springframework.context.annotation.Configuration
@EnableAspectJAutoProxy
public class ServiceLcmController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLcmController.class);

    @Resource(name="ServiceLcmService")
    private ServiceLcmService serviceLcmService;

    public void setServiceLcmService(ServiceLcmService serviceLcmService) {
        this.serviceLcmService = serviceLcmService;
    }

    @ResponseBody
    @RequestMapping(value = {"/uui-lcm/services"}, method = RequestMethod.POST , produces = "application/json")
    public ServiceOperation instantiateService(HttpServletRequest request){
        return serviceLcmService.instantiateService(request);
    }

    @ResponseBody
    @RequestMapping(value = {"/uui-lcm/services/{serviceId}/operations/{operationId}"}, method = RequestMethod.GET , produces = "application/json")
    public OperationProgressInformation queryOperationProgress(@PathVariable(value="serviceId") String serviceId, @PathVariable(value="operationId") String operationId){
        return serviceLcmService.queryOperationProgress(serviceId, operationId);
    }

    @ResponseBody
    @RequestMapping(value = {"/uui-lcm/services/{serviceId}"}, method = RequestMethod.DELETE , produces = "application/json")
    public DeleteOperationRsp terminateService(@PathVariable(value = "serviceId") String serviceId, HttpServletRequest request){
        return serviceLcmService.terminateService(serviceId, request);
    }
    
    /**
     * 
      * scaleServices
      * 
      * @Auther YYY
      * @Date   2018/04/19 16:12:45
      * @Title: scaleServices
      * @Description: Scaling out/in
      * @param serviceId
      * @param request
      * @return
     */
    @ResponseBody
    @RequestMapping(value = {"/uui-lcm/services/scaleServices/{serviceId}"}, method = RequestMethod.POST , produces = "application/json")
    public DeleteOperationRsp scaleServices(@PathVariable(value = "serviceId") String serviceId, HttpServletRequest request){
        return serviceLcmService.scaleService(serviceId, request);
    }
    
    /**
     * 
      * updateServices
      * 
      * @Auther YYY
      * @Date   2018/04/19 16:13:59
      * @Title: updateServices
      * @Description: Upgrade
      * @param serviceId
      * @param request
      * @return
     */
    @ResponseBody
    @RequestMapping(value = {"/uui-lcm/services/updateService/{serviceId}"}, method = RequestMethod.PUT , produces = "application/json")
    public DeleteOperationRsp updateServices(@PathVariable(value = "serviceId") String serviceId, HttpServletRequest request){
        return serviceLcmService.scaleService(serviceId, request);
    }
}
