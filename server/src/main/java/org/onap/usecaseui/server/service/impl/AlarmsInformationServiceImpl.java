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
package org.onap.usecaseui.server.service.impl;


import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.onap.usecaseui.server.bean.AlarmsInformation;
import org.onap.usecaseui.server.bean.maxAndMinTimeBean;
import org.onap.usecaseui.server.service.AlarmsInformationService;
import org.onap.usecaseui.server.util.Page;
import org.onap.usecaseui.server.util.UuiCommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;


@Service("AlarmsInformationService")
@Transactional
@org.springframework.context.annotation.Configuration
@EnableAspectJAutoProxy
public class AlarmsInformationServiceImpl implements AlarmsInformationService {
	private static final Logger logger = LoggerFactory.getLogger(AlarmsInformationServiceImpl.class);

	@Autowired
	private SessionFactory sessionFactory;

	private Session getSession() {
		return sessionFactory.openSession();
	}

	@Override
	public String saveAlarmsInformation(AlarmsInformation alarmsInformation) {
		 try(Session session = getSession()){
				if (null == alarmsInformation) {
					logger.error("alarmsInformation saveAlarmsInformation alarmsInformation is null!");
				}
				Transaction tx = session.beginTransaction();
				session.save(alarmsInformation);
				tx.commit();
				session.flush();
				return "1";
			} catch (Exception e) {
				logger.error("exception occurred while performing AlarmsInformationServiceImpl saveAlarmsInformation. Details:" + e.getMessage());
				return "0";
			}
			
	}

	@Override
	public String updateAlarmsInformation(AlarmsInformation alarmsInformation) {
		try(Session session = getSession()){
			if (null == alarmsInformation) {
				logger.error("alarmsInformation updateAlarmsInformation alarmsInformation is null!");
			}
			Transaction tx = session.beginTransaction();
			session.update(alarmsInformation);
			tx.commit();
			session.flush();
			return "1";
		} catch (Exception e) {
			logger.error("exception occurred while performing AlarmsInformationServiceImpl updateAlarmsInformation. Details:" + e.getMessage());
			return "0";
		}
	}
	

	public int getAllCount(AlarmsInformation alarmsInformation, int currentPage, int pageSize) {
		try(Session session = getSession()){
			StringBuffer hql = new StringBuffer("select count(*) from AlarmsInformation a where 1=1");
			if (null == alarmsInformation) {
			}else {
				if(null!=alarmsInformation.getName()) {
					String ver=alarmsInformation.getName();
					hql.append(" and a.name like '%"+ver+"%'");
				}
				if(null!=alarmsInformation.getValue()) {
					String ver=alarmsInformation.getValue();
					hql.append(" and a.value like '%"+ver+"%'");
				}
				if(null!=alarmsInformation.getSourceId()) {
					String ver=alarmsInformation.getSourceId();
					hql.append(" and a.sourceId = '"+ver+"'");
				}
				if(null!=alarmsInformation.getStartEpochMicroSec()) {
					String  ver =alarmsInformation.getStartEpochMicroSec();
					hql.append(" and a.createTime > '%"+ver+"%'");
				}
				if(null!=alarmsInformation.getLastEpochMicroSec()) {
					String ver =alarmsInformation.getLastEpochMicroSec();
					hql.append(" and a.updateTime like '%"+ver+"%'");
				}
			} 
			long q=(long)session.createQuery(hql.toString()).uniqueResult();
			session.flush();
			return (int)q;
		} catch (Exception e) {
			logger.error("exception occurred while performing AlarmsInformationServiceImpl getAllCount. Details:" + e.getMessage());
			return 0;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Page<AlarmsInformation> queryAlarmsInformation(AlarmsInformation alarmsInformation, int currentPage,
			int pageSize) {
		Page<AlarmsInformation> page = new Page<AlarmsInformation>();
		int allRow =this.getAllCount(alarmsInformation,currentPage,pageSize);
		int offset = page.countOffset(currentPage, pageSize);
		
		try(Session session = getSession()){
			StringBuffer hql =new StringBuffer("from AlarmsInformation a where 1=1");
			if (null == alarmsInformation) {
				//logger.error("AlarmsInformationServiceImpl queryAlarmsInformation alarmsInformation is null!");
			}else {
				if(null!=alarmsInformation.getName()) {
					String ver=alarmsInformation.getName();
					hql.append(" and a.name like '%"+ver+"%'");
				}
				if(null!=alarmsInformation.getValue()) {
					String ver=alarmsInformation.getValue();
					hql.append(" and a.value like '%"+ver+"%'");
				}
				if(null!=alarmsInformation.getSourceId()) {
					String ver=alarmsInformation.getSourceId();
					hql.append(" and a.sourceId = '"+ver+"'");
				}
				if(null!=alarmsInformation.getStartEpochMicroSec() || alarmsInformation.getLastEpochMicroSec()!= null) {
					hql.append(" and a.startEpochMicrosec between :startTime and :endTime");
				}
			}
			Query query = session.createQuery(hql.toString());
			if(null!=alarmsInformation.getStartEpochMicroSec() || alarmsInformation.getLastEpochMicroSec()!= null) {
				query.setString("startTime",alarmsInformation.getStartEpochMicroSec());
				query.setString("endTime",alarmsInformation.getLastEpochMicroSec());
			}
			query.setFirstResult(offset);
			query.setMaxResults(pageSize);
			List<AlarmsInformation> list= query.list();
			page.setPageNo(currentPage);
			page.setPageSize(pageSize);
			page.setTotalRecords(allRow);
			page.setList(list);
			session.flush();
			return page;
		} catch (Exception e) {
			logger.error("exception occurred while performing AlarmsInformationServiceImpl queryAlarmsInformation. Details:" + e.getMessage());
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AlarmsInformation> queryId(String[] id) {
		try {
			if(id.length==0) {
			}
			List<AlarmsInformation> list = new ArrayList<AlarmsInformation>();
			Session session = getSession();
			Query query = session.createQuery("from AlarmsInformation a where a.sourceId IN (:alist)");
			list = query.setParameterList("alist", id).list();
			session.close();
			return list;
		} catch (Exception e) {
			logger.error("exception occurred while performing AlarmsInformationServiceImpl queryId. Details:" + e.getMessage());
			return null;
		}
	}


	@Override
	public int queryDateBetween(String sourceName, String startTime, String endTime,String status) {
		if("1526554800000".equals(startTime)){
			System.out.print(startTime);
		}
		try(Session session = getSession()) {
			String hql = "select count(*) from AlarmsHeader a where 1=1 ";
			if (sourceName != null && !"".equals(sourceName)){
				hql += " and a.sourceName = :sourceName";
			}
			if (UuiCommonUtil.isNotNullOrEmpty(status)){
				hql += " and a.status = :status";
			}
			if (startTime != null && !"".equals(startTime) && endTime != null && !"".equals(endTime)){
				hql += " and (CASE WHEN a.startEpochMicrosec=0 THEN a.lastEpochMicroSec ELSE a.startEpochMicrosec END) between :startTime and :endTime ";
			}
			Query query = session.createQuery(hql);
			if (sourceName != null && !"".equals(sourceName)){
				query.setString("sourceName",sourceName);
			}
			if (UuiCommonUtil.isNotNullOrEmpty(status)){
				query.setString("status",status);
			}
			if (startTime != null && !"".equals(startTime) && endTime != null && !"".equals(endTime)){
				query.setString("startTime", startTime).setString("endTime", endTime);
			}
			long num=(long) query.uniqueResult();
			return (int)num;
		} catch (Exception e) {
			logger.error("exception occurred while performing PerformanceInformationServiceImpl queryDateBetween. Details:" + e.getMessage());
			return 0;
		}
	}
	
	@Override
	public List<maxAndMinTimeBean> queryMaxAndMinTime(){
		List<maxAndMinTimeBean> list = new ArrayList<>();
		try (Session session = getSession()){
			String sql = "select MAX(startEpochMicrosec),MIN(startEpochMicrosec) FROM alarms_commoneventheader";
			Query query = session.createSQLQuery(sql);
			list = query.list();
			session.flush();
		}catch (Exception e){
			logger.error("exception occurred while performing PerformanceInformationServiceImpl queryDateBetween. LIST:" + e.getMessage());

			 list = new ArrayList<>();
		}
	
		return list;
	}

	@Override
	public List<AlarmsInformation> getAllAlarmsInformationByHeaderId(String headerId) {
		try (Session session = getSession()){
			String string = "from AlarmsInformation a where 1=1 and a.headerId=:headerId";
			Query query = session.createQuery(string);
			query.setString("headerId",headerId);
			List<AlarmsInformation> list = query.list();
			session.flush();
			return list;
		}catch (Exception e){
			logger.error("exception occurred while performing PerformanceInformationServiceImpl queryDateBetween. LIST:" + e.getMessage());

			return null;
		}
	}
}
