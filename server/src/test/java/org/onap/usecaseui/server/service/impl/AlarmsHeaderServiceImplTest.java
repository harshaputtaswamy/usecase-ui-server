/**
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
package org.onap.usecaseui.server.service.impl; 

import org.junit.Test; 
import org.junit.Before; 
import org.junit.After;
import org.junit.runner.RunWith;
import org.onap.usecaseui.server.UsecaseuiServerApplication;
import org.onap.usecaseui.server.bean.AlarmsHeader;
import org.onap.usecaseui.server.service.impl.AlarmsHeaderServiceImpl;
import org.onap.usecaseui.server.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import mockit.Mock;
import mockit.MockUp;
/** 
* AlarmsHeaderServiceImpl Tester. 
* 
* @author <Authors name> 
* @since <pre>8, 2018</pre>
* @version 1.0 
*/
public class AlarmsHeaderServiceImplTest {
	@Before
		public void before() throws Exception { 
	}

	@After
	public void after() throws Exception { 
	}

	private Session session;
	private Transaction transaction;
	private Query query;

	/** 
	* 
	* Method: saveAlarmsHeader(AlarmsHeader alarmsHeader) 
	* 
	*/ 
	@Test
	public void testSaveAlarmsHeader() throws Exception { 
	//TODO: Test goes here...
		AlarmsHeader a = new AlarmsHeader();
		a.setEventName("a");
		a.setStatus("1");
		a.setVfStatus("1");
		a.setEventId("1119");
		a.setDomain("asb");
		a.setEventCategory("s");
		a.setAlarmCondition("ea");
		a.setAlarmInterfaceA("cs");
		a.setCreateTime(DateUtils.now());
		a.setEventServrity("s");
		a.setEventSourceType("q");
		a.setEventType("q");
		a.setFaultFieldsVersion("v1");
		a.setLastEpochMicroSec("csa");
		a.setNfcNamingCode("std");
		a.setNfNamingCode("cout");
		a.setPriority("cs");
		a.setReportingEntityId("112");
		a.setReportingEntityName("asfs");
		a.setSequence("cgg");
		a.setSourceId("123");
		a.setSourceName("eggs");
		a.setSpecificProblem("especially");
		a.setStartEpochMicrosec("wallet");
		a.setUpdateTime(DateUtils.now());
		a.setVersion("va2");

		
		
		
        MockUp<Query> mockUpQuery = new MockUp<Query>() {
        };
		MockUp<Session> mockedSession = new MockUp<Session>() {
            @Mock
            public Query createQuery(String sql) {
                return mockUpQuery.getMockInstance();
            }
			@Mock
			public Transaction beginTransaction() {
				return transaction;
			}
		};
		new MockUp<SessionFactory>() {
			@Mock
			public Session openSession() {
				return mockedSession.getMockInstance();
			}
		};
		new MockUp<Transaction>() {
			@Mock
			public void commit() {
			}
		};
        new MockUp<AlarmsHeaderServiceImpl>() {
            @Mock
            private Session getSession() {
                return mockedSession.getMockInstance();
            }
        };

		AlarmsHeaderServiceImpl alarmsHeaderServiceImpl = new AlarmsHeaderServiceImpl();
		alarmsHeaderServiceImpl.saveAlarmsHeader(a);
	} 






} 