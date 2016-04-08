
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

import com.accela.contractor.service.AgencyLoader;
import com.accela.contractor.service.AppInstance;
import com.accela.contractor.service.AgencyLoader.LinkAgencyDelegate;
import com.accela.contractor.service.AppSettingsLoader;
import com.accela.contractor.test.Utils;
import com.accela.mobile.AccelaMobile.Environment;

public class AppSettingsLoaderTest extends InstrumentationTestCase{

	private AppSettingsLoader appSettingsLoader;
	
	
	public AppSettingsLoaderTest() {
		appSettingsLoader = AppInstance.getAppSettingsLoader();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Utils.login();	
	}
	

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	

	
	
	public void testLoadAppSettings() throws InterruptedException {
		
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				appSettingsLoader.loadAppSettings(null, false);
			}
		});
		thread.start();
		Thread.sleep(6000);
		assertNotNull(appSettingsLoader.getPhoneNumber());
		assertNotNull(appSettingsLoader.getActiveRecordStatus());
		assertTrue(appSettingsLoader.getActiveRecordStatus().size()>0);

	}
	
	
	
}