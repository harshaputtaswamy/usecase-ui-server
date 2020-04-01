/**
 * Copyright 2019  Verizon. All Rights Reserved.
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

import org.onap.usecaseui.server.service.lcm.OrchestratorService;
import org.onap.usecaseui.server.service.lcm.domain.aai.bean.AAIEsrNfvo;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Controller
@org.springframework.context.annotation.Configuration
@EnableAspectJAutoProxy
public class OrchestratorController {

    @Resource(name="OrchestratorService")
    private OrchestratorService  orchestratorService;

    public void setOrchestratorService(OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @ResponseBody
    @RequestMapping(value = {"/uui-lcm/orchestrators"}, method = RequestMethod.GET , produces = "application/json")
    public List<AAIEsrNfvo> getOrchestrators(){
        return orchestratorService.listOrchestrator();
    }

}
