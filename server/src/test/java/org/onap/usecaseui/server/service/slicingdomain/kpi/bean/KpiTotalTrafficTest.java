/*
 * Copyright (C) 2020 CMCC, Inc. and others. All rights reserved.
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
package org.onap.usecaseui.server.service.slicingdomain.kpi.bean;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.usecaseui.server.bean.nsmf.monitor.TrafficReqInfo;

public class KpiTotalTrafficTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testSetAndGetKpiTotalTraffic() throws Exception {
        KpiTotalTraffic kpiTotalTraffic = new KpiTotalTraffic();

        List<TotalTraffic> totalTrafficList = new ArrayList<>();
        kpiTotalTraffic.setResult(totalTrafficList);

        TrafficReqInfo trafficReqInfo = new TrafficReqInfo();
        kpiTotalTraffic.setRequest(trafficReqInfo);

        kpiTotalTraffic.setResult_count(3);

        kpiTotalTraffic.getRequest();
        kpiTotalTraffic.getResult();
        kpiTotalTraffic.getResult_count();
    }

}
