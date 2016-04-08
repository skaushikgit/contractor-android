
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
import com.accela.contractor.model.ProjectModel;
import com.accela.contractor.service.AppInstance;
import com.accela.contractor.service.InspectionLoader;
import com.accela.contractor.service.ProjectsLoader;
import com.accela.contractor.test.Utils;
import com.accela.sqlite.framework.util.Log;

public class InspectionLoaderTest extends InstrumentationTestCase implements Observer{

	private ProjectsLoader projectsLoader;
	private InspectionLoader inspectionLoader;
	private boolean projectNotifyTag = false;
	private boolean inspectionNotifyTag = false;
	


	
	public InspectionLoaderTest() {
		AppInstance.clearAll();

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Utils.login();
		projectsLoader = AppInstance.getProjectsLoader();
		inspectionLoader = AppInstance.getInpsectionLoader();
		projectsLoader.addObserver(this);
		inspectionLoader.addObserver(this);
		assertNotNull("projectsLoader is null", projectsLoader);
	}
	

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	

	@Override
	public void update(Observable observable, Object data) {
		Log.e("PROJECT_LIST_LOAD_PROGRESS");
		if(data instanceof Integer) {
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

	
	public void testLoadInspections() throws InterruptedException {
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				projectsLoader.loadAllProjects(false);
			}
		});
		thread.start();
		waitForInspectionResponse();
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
