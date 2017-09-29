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
package org.onap.usecaseui.server.service;

import java.util.List;

import org.onap.usecaseui.server.bean.AlarmsHeader;
import org.onap.usecaseui.server.bean.PerformanceHeader;
import org.onap.usecaseui.server.util.Page;


public interface PerformanceHeaderService {
    
    String savePerformanceHeader(PerformanceHeader performanceHeder);
    
    String updatePerformanceHeader(PerformanceHeader performanceHeder);
    
    int getAllCount();
    
    Page<PerformanceHeader> queryPerformanceHeader(PerformanceHeader performanceHeder, int currentPage, int pageSize);
    
    List<PerformanceHeader> queryId(String[] id);
}