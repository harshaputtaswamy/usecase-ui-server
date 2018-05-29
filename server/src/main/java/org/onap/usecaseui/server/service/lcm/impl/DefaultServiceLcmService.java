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

import static org.onap.usecaseui.server.util.RestfulServices.create;
import static org.onap.usecaseui.server.util.RestfulServices.extractBody;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.onap.usecaseui.server.service.lcm.ServiceLcmService;
import org.onap.usecaseui.server.service.lcm.domain.so.SOService;
import org.onap.usecaseui.server.service.lcm.domain.so.bean.DeleteOperationRsp;
import org.onap.usecaseui.server.service.lcm.domain.so.bean.OperationProgressInformation;
import org.onap.usecaseui.server.service.lcm.domain.so.bean.SaveOrUpdateOperationRsp;
import org.onap.usecaseui.server.service.lcm.domain.so.bean.ServiceOperation;
import org.onap.usecaseui.server.service.lcm.domain.so.exceptions.SOException;
import org.onap.usecaseui.server.util.UuiCommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;

import okhttp3.RequestBody;
import retrofit2.Response;

@Service("ServiceLcmService")
@org.springframework.context.annotation.Configuration
@EnableAspectJAutoProxy
public class DefaultServiceLcmService implements ServiceLcmService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceLcmService.class);

    private SOService soService;

    public DefaultServiceLcmService() {
        this(create(SOService.class));
    }

    public DefaultServiceLcmService(SOService soService) {
        this.soService = soService;
    }

    @Override
    public ServiceOperation instantiateService(HttpServletRequest request) {
        try {
        	logger.info("so instantiate is starting");
        	RequestBody requestBody = extractBody(request);
            Response<ServiceOperation> response = soService.instantiateService(requestBody).execute();
			logger.info("so instantiate has finished");
            if (response.isSuccessful()) {
                return response.body();
            } else {
                logger.error(String.format("Can not instantiate service[code=%s, message=%s]", response.code(), response.message()));
                throw new SOException("SO instantiate service failed!");
            }
        } catch (Exception e) {
            throw new SOException("SO Service is not available!", e);
        }
    }

    @Override
    public OperationProgressInformation queryOperationProgress(String serviceId, String operationId) {
        try {
            Response<OperationProgressInformation> response = soService.queryOperationProgress(serviceId, operationId).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                logger.error(String.format("Can not query operation process[code=%s, message=%s]", response.code(), response.message()));
                throw new SOException("SO query operation process failed!");
            }
        } catch (IOException e) {
            throw new SOException("SO Service is not available!", e);
        }
    }

    @Override
    public DeleteOperationRsp terminateService(String serviceId, HttpServletRequest request) {
        try {
        	logger.info("so terminate is starting");
            RequestBody requestBody = extractBody(request);
            Response<DeleteOperationRsp> response = soService.terminateService(serviceId, requestBody).execute();
			logger.info("so terminate has finished");
            if (response.isSuccessful()) {
                return response.body();
            } else {
                logger.error(String.format("Can not terminate service[code=%s, message=%s]", response.code(), response.message()));
                throw new SOException("SO terminate service failed!");
            }
        } catch (IOException e) {
            throw new SOException("SO Service is not available!", e);
        }
    }

	@Override
	public SaveOrUpdateOperationRsp scaleService(String serviceId, HttpServletRequest request) {
		try {
			logger.info("so scale is finished");
			RequestBody requestBody = extractBody(request);
			Response<SaveOrUpdateOperationRsp> response = soService.scaleService(serviceId,requestBody).execute();
			logger.info("so scale has finished");
			if(response.isSuccessful()){
				logger.info("scaleService response content is :"+response.body().toString());
				return response.body();
			}else{
                logger.error(String.format("Can not scaleService service[code=%s, message=%s]", response.code(), response.message()));
                throw new SOException("SO terminate service failed!");
			}
		} catch (IOException e) {
			 throw new SOException("SO Service is not available!", e);
		}
	}

	@Override
	public SaveOrUpdateOperationRsp updateService(String serviceId, HttpServletRequest request) {
		try {
			logger.info("so update is starting");
			RequestBody requestBody = extractBody(request);
			Response<SaveOrUpdateOperationRsp> response = soService.updateService(serviceId,requestBody).execute();
			logger.info("so update has finished");
			if(response.isSuccessful()){
				return response.body();
			}else{
                logger.error(String.format("Can not updateService service[code=%s, message=%s]", response.code(), response.message()));
                throw new SOException("SO terminate service failed!");
			}
		} catch (IOException e) {
			 throw new SOException("SO Service is not available!", e);
		}
	}
}
