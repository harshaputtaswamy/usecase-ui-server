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
package org.onap.usecaseui.server.service.lcm.impl;

import org.junit.Assert;
import org.junit.Test;
import org.onap.usecaseui.server.service.lcm.ServiceInstanceService;
import org.onap.usecaseui.server.service.lcm.domain.aai.AAIService;
import org.onap.usecaseui.server.service.lcm.domain.aai.bean.ServiceInstance;
import org.onap.usecaseui.server.service.lcm.domain.aai.exceptions.AAIException;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.onap.usecaseui.server.util.CallStub.failedCall;
import static org.onap.usecaseui.server.util.CallStub.successfulCall;

public class DefaultServiceInstanceServiceTest {

    @Test
    public void itCanRetrieveServiceInstanceFromAAI() {
        AAIService aaiService = mock(AAIService.class);
        String customerId = "1";
        String serviceType = "service";
        List<ServiceInstance> instances = Collections.singletonList(new ServiceInstance("1","service","1","VoLTE","e2eservice","abc","vim1"));
        when(aaiService.listServiceInstances(customerId, serviceType)).thenReturn(successfulCall(instances));

        ServiceInstanceService service = new DefaultServiceInstanceService(aaiService);

        Assert.assertSame(instances, service.listServiceInstances(customerId, serviceType));
    }

    @Test(expected = AAIException.class)
    public void retrieveServiceInstancesWillThrowExceptionWhenAAIIsNotAvailable() {
        AAIService aaiService = mock(AAIService.class);
        String customerId = "1";
        String serviceType = "service";
        when(aaiService.listServiceInstances(customerId, serviceType)).thenReturn(failedCall("AAI is not available!"));

        ServiceInstanceService service = new DefaultServiceInstanceService(aaiService);
        service.listServiceInstances(customerId, serviceType);
    }
}