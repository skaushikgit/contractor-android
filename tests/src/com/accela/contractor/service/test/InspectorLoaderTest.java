
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

import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import junit.framework.Assert;

import org.json.JSONException;

import android.test.InstrumentationTestCase;

import com.accela.contractor.AppConstants;
import com.accela.contractor.R;
import com.accela.contractor.model.ProjectModel;
import com.accela.contractor.service.AppInstance;
import com.accela.contractor.service.InspectorLoader;
import com.accela.contractor.service.ProjectsLoader;
import com.accela.contractor.service.AppInstance.AppServiceDelegate;
import com.accela.contractor.test.Utils;
import com.accela.contractor.utils.ActivityUtils;
import com.accela.inspection.model.InspectorModel;
import com.accela.mobile.util.UIUtils;
import com.accela.record.model.RecordInspectionModel;
import com.accela.sqlite.framework.util.Log;

public class InspectorLoaderTest extends InstrumentationTestCase implements Observer{

	private ProjectsLoader projectsLoader;
	private InspectorLoader inspectorLoader;
	private boolean projectNotifyTag = false;
	private boolean inspectorNotifyTag = false;
	
	
	public InspectorLoaderTest() {
		projectsLoader = AppInstance.getProjectsLoader();
		inspectorLoader = AppInstance.getInspectorLoader();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Utils.login();
		projectsLoader.addObserver(this);
        assertNotNull("projectsLoader is null", projectsLoader);
	}
	

	protected void tearDown() throws Exception {
		super.tearDown();
		projectsLoader.deleteObserver(this);
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
			}
		}
	}
	
	
	public void testLoadInspector() throws InterruptedException {
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				inspectorLoader.loadInspectorByInspection(projectsLoader.getRecentInpsections().get(0), new AppServiceDelegate<InspectorModel>() {
					@Override
					public void onSuccess(List<InspectorModel> response) {
						// TODO Auto-generated method stub
						assertNotNull(response);
						inspectorNotifyTag = true;
					}

					@Override
					public void onFailure(Throwable error) {
						// TODO Auto-generated method stub
						fail(error.getLocalizedMessage());
					}
				});
			}
		});
		thread.start();
		waitForInspectorResponse();
	}
	
	private void waitForInspectorResponse() throws InterruptedException {
		// TODO Auto-generated method stub
		while(true){
			if(this.inspectorNotifyTag)
				break;
			Thread.sleep(2000);
		}
	}
		

}
