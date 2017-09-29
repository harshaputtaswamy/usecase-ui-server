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
import org.onap.usecaseui.server.service.lcm.ServiceLcmService;
import org.onap.usecaseui.server.service.lcm.domain.so.SOService;
import org.onap.usecaseui.server.service.lcm.domain.so.bean.OperationProgressInformation;
import org.onap.usecaseui.server.service.lcm.domain.so.bean.ServiceInstantiationRequest;
import org.onap.usecaseui.server.service.lcm.domain.so.bean.ServiceOperation;
import org.onap.usecaseui.server.service.lcm.domain.so.exceptions.SOException;

import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.onap.usecaseui.server.util.CallStub.failedCall;
import static org.onap.usecaseui.server.util.CallStub.successfulCall;

public class DefaultServiceLcmServiceTest {

    @Test
    public void itCanInstantiateService() {
        SOService soService = mock(SOService.class);
        ServiceInstantiationRequest request = new ServiceInstantiationRequest(
                "name",
                "description",
                "123",
                "123",
                new HashMap<>()
        );
        ServiceOperation operation = new ServiceOperation("1", "1");
        when(soService.instantiateService(request)).thenReturn(successfulCall(operation));

        ServiceLcmService service = new DefaultServiceLcmService(soService);

        Assert.assertSame(operation, service.instantiateService(request));
    }

    @Test(expected = SOException.class)
    public void instantiateServiceWillThrowExceptionWhenSOIsNotAvailable() {
        SOService soService = mock(SOService.class);
        ServiceInstantiationRequest request = new ServiceInstantiationRequest(
                "name",
                "description",
                "123",
                "123",
                new HashMap<>()
        );
        when(soService.instantiateService(request)).thenReturn(failedCall("SO is not available!"));

        ServiceLcmService service = new DefaultServiceLcmService(soService);

        service.instantiateService(request);
    }

    @Test
    public void itCanTerminateService() {
        SOService soService = mock(SOService.class);
        String serviceId = "1";
        ServiceOperation operation = new ServiceOperation("1", "1");
        when(soService.terminateService(serviceId)).thenReturn(successfulCall(operation));

        ServiceLcmService service = new DefaultServiceLcmService(soService);

        Assert.assertSame(operation, service.terminateService(serviceId));
    }

    @Test(expected = SOException.class)
    public void terminateServiceWillThrowExceptionWhenSOIsNotAvailable() {
        SOService soService = mock(SOService.class);
        String serviceId = "1";
        when(soService.terminateService(serviceId)).thenReturn(failedCall("SO is not available!"));

        ServiceLcmService service = new DefaultServiceLcmService(soService);

        service.terminateService(serviceId);
    }

    @Test
    public void itCanQueryOperationProgress() {
        SOService soService = mock(SOService.class);
        String serviceId = "1";
        String operationId = "1";
        OperationProgressInformation progress = new OperationProgressInformation();
        when(soService.queryOperationProgress(serviceId, operationId)).thenReturn(successfulCall(progress));

        ServiceLcmService service = new DefaultServiceLcmService(soService);

        Assert.assertSame(progress, service.queryOperationProgress(serviceId, operationId));
    }

    @Test(expected = SOException.class)
    public void queryOperationProgressWillThrowExceptionWhenSOIsNotAvailable() {
        SOService soService = mock(SOService.class);
        String serviceId = "1";
        String operationId = "1";
        when(soService.queryOperationProgress(serviceId, operationId)).thenReturn(failedCall("SO is not available!"));

        ServiceLcmService service = new DefaultServiceLcmService(soService);

        service.queryOperationProgress(serviceId, operationId);
    }
}