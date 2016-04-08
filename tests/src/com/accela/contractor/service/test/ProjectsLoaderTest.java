
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONException;

import junit.framework.Assert;

import com.accela.contractor.AppConstants;
import com.accela.contractor.mock.MockData;
import com.accela.contractor.model.ProjectModel;
import com.accela.contractor.service.AppInstance;
import com.accela.contractor.service.ProjectsLoader;
import com.accela.contractor.test.Utils;
import com.accela.record.model.RecordInspectionModel;
import com.accela.sqlite.framework.util.Log;


import android.test.InstrumentationTestCase;



public class ProjectsLoaderTest extends InstrumentationTestCase implements Observer{

	private ProjectsLoader projectsLoader;
	private boolean projectNotifyTag = false;
	private boolean inspectionNotifyTag = false;
	


	
	public ProjectsLoaderTest() throws JSONException {
		AppInstance.clearAll();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Utils.login();
		projectsLoader = AppInstance.getProjectsLoader();
		projectsLoader.addObserver(this);
        assertNotNull("projectsLoader is null", projectsLoader);
	}
	

	protected void tearDown() throws Exception {
		super.tearDown();
		projectsLoader.deleteObserver(this);
	}
	

	@Override
	public void update(Observable observable, Object data) {
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
	
	public void testALoadProjects() throws InterruptedException {
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				projectsLoader.loadAllProjects(false);
			}
		});
		thread.start();
		waitForProjectResponse();
	}
	
	public void testBLoadInspections() throws InterruptedException {
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				projectsLoader.loadAllInspections();
			}
		});
		thread.start();
		waitForInspectionResponse();
	}
	
	public void testGetProjects() {
		List<ProjectModel> projects = projectsLoader.getProjects();
		Assert.assertNotNull("Project list is empty!", projects);
		Assert.assertTrue("Project list number is 0 !", projects.size()>0);
	}
	
	
	public void testGetRecentInpsections() {
		List<RecordInspectionModel> inspections = projectsLoader.getRecentInpsections();
		Assert.assertNotNull("inspections list is empty!", inspections);
		Assert.assertTrue("inspections list number is 0 !", inspections.size()>0);
	}  
	
	
	
	public void testGetNearByProject() {
		List<ProjectModel> nearByProject = projectsLoader.getNearByProject();
		Assert.assertNotNull("Project list is empty!", nearByProject);
	}
	
	
	public void getScheduleInpsections() {
		List<RecordInspectionModel> scheduledInspection = projectsLoader.getScheduleInpsections();
		Assert.assertNotNull("scheduledInspection list is empty!", scheduledInspection);
		Assert.assertTrue("scheduledInspection list number is 0 !", scheduledInspection.size()>0);
	} 
	
	
	public void testGetNextInspection(){
		RecordInspectionModel inspection = projectsLoader.getNextInspection();
		Assert.assertNotNull("nextInspection should not be null", inspection);
		Date currentDate = new Date();
		if(inspection.getScheduleDate()!=null){
			Assert.assertFalse("Next inspection date is before the current date!", inspection.getScheduleDate().before(currentDate));
		}
	}
	

	

	private void waitForProjectResponse() throws InterruptedException {
		// TODO Auto-generated method stub
		while(true){
			if(this.projectNotifyTag)
				break;
			Thread.sleep(2000);
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

