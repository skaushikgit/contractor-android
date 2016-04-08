
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

import java.util.Observable;
import java.util.Observer;

import android.test.InstrumentationTestCase;

import com.accela.contractor.service.AppInstance;
import com.accela.contractor.service.InspectionTimesLoader;
import com.accela.contractor.service.ProjectsLoader;
import com.accela.contractor.test.Utils;
import com.accela.mobile.AMLogger;

public class InspectionTimesLoaderTest extends InstrumentationTestCase implements Observer{

	private ProjectsLoader projectsLoader;
	private InspectionTimesLoader inspectionTimesLoader;
	private boolean projectNotifyTag = false;
	private boolean inspectionTimesNotifyTag = false;
	private boolean inspectionNotifyTag = false;
	
	
	public InspectionTimesLoaderTest() {
		projectsLoader = AppInstance.getProjectsLoader();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Utils.login();
        assertNotNull("projectsLoader is null", projectsLoader);
	}
	

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	

	@Override
	public void update(Observable observable, Object data) {
		if(observable instanceof InspectionTimesLoader){
			inspectionTimesNotifyTag = true;
		}
	}
	

	public void testLoadInspectionTimes() throws InterruptedException {
		AMLogger.logInfo("testLoadInspectionTimes:" + projectsLoader.getProjects().get(0).getProjectId());
		inspectionTimesLoader = new InspectionTimesLoader(projectsLoader.getProjects().get(0).getProjectId(), projectsLoader.getScheduleInpsections().get(1).getId(), 
				projectsLoader.getScheduleInpsections().get(1).getType().getId().toString());
		inspectionTimesLoader.addObserver(this);
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				inspectionTimesLoader.loadInspectionDateByMonth(2015, 7);
			}
		});
		thread.start();
		waitForInspectionTimesResponse();
		assertNotNull(inspectionTimesLoader.getInspectionDatesByMonth(2015, 7));
		assertTrue(inspectionTimesLoader.getInspectionDatesByMonth(2015, 7).listInspectionAvailableDates.size()>0);
		AMLogger.logInfo("testLoadInspectionTimes:" + inspectionTimesLoader.getInspectionDatesByMonth(2015, 7).listInspectionAvailableDates.size());

	}
	
	private void waitForInspectionTimesResponse() throws InterruptedException {
		// TODO Auto-generated method stub
		while(true){
			if(this.inspectionTimesNotifyTag)
				break;
			Thread.sleep(2000);
		}
	}
		

	
}
