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
package org.onap.usecaseui.server.listener;

import org.onap.usecaseui.server.service.InitializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;


public class UsecaseServerEventListener implements ApplicationListener<ApplicationReadyEvent>
{
    private static final Logger logger = LoggerFactory.getLogger(UsecaseServerEventListener.class);
    
    @Autowired
    InitializationService initializationService;
    
    public void onApplicationEvent(ApplicationReadyEvent arg0)
    {
        initializationService.initialize();
    }
    
}