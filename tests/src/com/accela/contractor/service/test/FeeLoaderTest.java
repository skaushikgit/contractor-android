
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
import com.accela.contractor.service.FeeLoader;
import com.accela.contractor.service.ProjectsLoader;
import com.accela.contractor.test.Utils;
import com.accela.mobile.AMLogger;
import com.accela.sqlite.framework.util.Log;

public class FeeLoaderTest extends InstrumentationTestCase implements Observer{

	private ProjectsLoader projectsLoader;
	private FeeLoader feeLoader;
	private boolean feeNotifyTag = false;
	private String recordId = "";


	
	public FeeLoaderTest() throws JSONException {
		AppInstance.clearAll();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Utils.login();
		projectsLoader = AppInstance.getProjectsLoader();
		feeLoader = AppInstance.getFeeLoader();
		projectsLoader.addObserver(this);
		feeLoader.addObserver(this);
        assertNotNull("projectsLoader is null", projectsLoader);
        feeNotifyTag = false;
	}
	

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	

	@Override
	public void update(Observable observable, Object data) {
		
		if(observable instanceof FeeLoader) {
			feeNotifyTag = true;
			return;
		}
	}
	

	
	public void testFeeLoad(){
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(projectsLoader.getProjects().get(0).getProjectId()!=null){
					recordId = projectsLoader.getProjects().get(0).getProjectId();
					AMLogger.logInfo("testFeeLoad:" + recordId  );
					AMLogger.logInfo(recordId);
					feeLoader.loadFeeByRecord(recordId);
				}else{
					fail("project id is null");
				}
			}
		});
		thread.start();
		waitForFeeResponse();
		assertNotNull(feeLoader.getFeeItemByRecord(recordId).listAllFee);
//		assertTrue(feeLoader.getFeeItemByRecord(recordId).listAllFee.size()>0);
	}
	
	private void waitForFeeResponse() {
		// TODO Auto-generated method stub
		while(true){
			if(this.feeNotifyTag)
				break;
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
