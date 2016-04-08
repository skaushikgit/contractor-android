
/** 
 * Copyright 2014 Accela, Inc. 
 * 
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to 
 * use, copy, modify, and distribute this software in source code or binary 
 * form for use in connection with the web services and APIs provided by 
 * Accela. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE. 
 * 
 * 
 * 
 */

/*
 * 
 * 
 *   Created by eyang on 6/16/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */
package com.accela.contractor.service.test;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import junit.framework.Assert;

import org.json.JSONException;

import android.test.InstrumentationTestCase;

import com.accela.contractor.AppConstants;
import com.accela.contractor.R;
import com.accela.contractor.activity.BaseActivity;
import com.accela.contractor.model.ProjectModel;
import com.accela.contractor.service.AppInstance;
import com.accela.contractor.service.InspectionLoader;
import com.accela.contractor.service.InspectionLoader.RecordInspectionItems;
import com.accela.contractor.service.InstantService;
import com.accela.contractor.service.ProjectsLoader;
import com.accela.contractor.service.AppInstance.AppServiceDelegate;
import com.accela.contractor.test.Utils;
import com.accela.contractor.utils.ActivityUtils;
import com.accela.framework.model.InspectionModel;
import com.accela.mobile.AMLogger;
import com.accela.mobile.util.UIUtils;
import com.accela.record.model.RecordInspectionModel;
import com.accela.sqlite.framework.util.Log;

public class InstantServiceTest extends InstrumentationTestCase implements Observer{
	private ProjectsLoader projectsLoader;
	private boolean projectNotifyTag = false;
	private boolean inspectionNotifyTag = false;
	private String TAG = "InstantServiceTest";

	
	public InstantServiceTest() throws JSONException {
//		AppInstance.clearAll();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();		
		Utils.login();
		projectsLoader = AppInstance.getProjectsLoader();
        assertNotNull("projectsLoader is null", projectsLoader);
		projectNotifyTag = false;
		inspectionNotifyTag = false;
	}
	

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	@Override
	public void update(Observable observable, Object data) {
		Log.e("PROJECT_LIST_LOAD_PROGRESS");
		if(observable instanceof ProjectsLoader && data instanceof Integer) {
			List<ProjectModel> projects = projectsLoader.getProjects();
			int flag = (Integer) data;
			switch (flag) {
				case AppConstants.PROJECT_LIST_LOAD_PROGRESS:
					Assert.assertNotNull("Project list is empty!", projects);
					Assert.assertTrue("Project list number is 0 !", projects.size()>0);
					break;
				case AppConstants.PROJECT_LIST_CHANGE:
					Assert.assertNotNull("Project list is empty!", projects);
					Assert.assertTrue("Project list number is 0 !", projects.size()>0);
					this.projectNotifyTag = true;
					break;
				case AppConstants.PROJECT_LIST_RECENT_INSPECTION_CHANGE:
					Assert.assertNotNull(projectsLoader.getRecentInpsections());
					this.inspectionNotifyTag = true;
					break;
			}
		}
	}
	
	AppServiceDelegate<InspectionModel> delegate = new AppServiceDelegate<InspectionModel>() {
		@Override
		public void onSuccess(List<InspectionModel> response) {
			// TODO Auto-generated method stub
			Log.e(TAG, "onSuccess");
			Assert.assertNotNull("list is empty!", response);
			Assert.assertTrue("list number is 0 !", response.size()>0);
			inspectionNotifyTag = true;
		}

		@Override
		public void onFailure(Throwable error) {
			// TODO Auto-generated method stub
			Log.e(TAG, "onFailure" + error.getMessage());
			fail(error.getMessage());
			inspectionNotifyTag = true;
		}
	};
	

	
	public void testRescheduleInspection() throws InterruptedException{
		//RecordInspectionItems item = inspectionLoader.getInspectionByRecord("15CAP-00000-0001G");
		List<RecordInspectionModel> list = projectsLoader.getScheduleInpsections();
		boolean tag = false;
		AMLogger.logInfo("the schedule inspection size:" + list.size());

		if(list!=null && list.size()>0){
			for(final RecordInspectionModel recordInspection : list){
				if(recordInspection.getAddress().getStreetAddress()!=null){
					AMLogger.logInfo("the schedule inspection address" + recordInspection.getAddress().getStreetAddress());

				}else{
					AMLogger.logInfo("the schedule record id" + recordInspection.getRecordId_customId());
					AMLogger.logInfo("the address is empty");
				}
				if(recordInspection.getRecordId_customId()!=null && recordInspection.getRecordId_customId().contains("BLD15-00004")){
					Thread thread = new Thread(new Runnable(){
						@Override
						public void run() {
							// TODO Auto-generated method stub
							InstantService.rescheduleInspection(delegate, recordInspection, recordInspection.getRecordId_id());
						}
					});
					thread.start();
					tag = true;
					waitForInspectionResponse();
					break;
				}
			}
			if(!tag){
				fail("inspection not exist!");
			}
		}
	}
	
	public void testScheduleInspection() throws InterruptedException{
		//RecordInspectionItems item = inspectionLoader.getInspectionByRecord("15CAP-00000-0001G");
		List<RecordInspectionModel> list = projectsLoader.getScheduleInpsections();
		boolean tag = false;
		AMLogger.logInfo("the schedule inspection size:" + list.size());

		if(list!=null && list.size()>0){
			for(final RecordInspectionModel recordInspection : list){
				if(recordInspection.getAddress().getStreetAddress()!=null){
					AMLogger.logInfo("the schedule inspection address" + recordInspection.getAddress().getStreetAddress());

				}else{
					AMLogger.logInfo("the schedule record id" + recordInspection.getRecordId_customId());
					AMLogger.logInfo("the address is empty");
				}
				if(recordInspection.getRecordId_customId()!=null && recordInspection.getRecordId_customId().contains("BLD15-00004")){
					Thread thread = new Thread(new Runnable(){
						@Override
						public void run() {
							// TODO Auto-generated method stub
							InstantService.scheduleNewInspection(delegate, recordInspection.getRecordId_id(), recordInspection.getAddress(), recordInspection.getType().getId(), recordInspection.getScheduleDate(), null, null, null, null, "form unit test", null, null, null, null, null, null, null, null);
						}
					});
					thread.start();
					tag = true;
					waitForInspectionResponse();
					break;
				}
			}
			if(!tag){
				fail("inspection not exist!");
			}
		}
	}
	


	
	private void waitForInspectionResponse() throws InterruptedException {
		// TODO Auto-generated method stub
		while(true){
			if(this.inspectionNotifyTag){
				Thread.sleep(5000); //give some time to get other inspections
				break;
			}
			Thread.sleep(2000);
		}
	}
	
}
