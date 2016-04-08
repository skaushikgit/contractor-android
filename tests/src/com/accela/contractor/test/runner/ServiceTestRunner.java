
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
package com.accela.contractor.test.runner;


import java.io.IOException;

import junit.framework.Assert;

import com.accela.contractor.AppContext;
import com.accela.contractor.service.test.AgencyLoaderTest;
import com.accela.contractor.service.test.AppSettingsLoaderTest;
import com.accela.contractor.service.test.FeeLoaderTest;
import com.accela.contractor.service.test.InspectionLoaderTest;
import com.accela.contractor.service.test.InspectionTimesLoaderTest;
import com.accela.contractor.service.test.InspectionTypeLoaderTest;
import com.accela.contractor.service.test.InspectorLoaderTest;
import com.accela.contractor.service.test.ProjectModelTest;
import com.accela.contractor.service.test.ProjectsLoaderTest;
import com.accela.contractor.service.test.InstantServiceTest;

import android.os.Message;
import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;


public class ServiceTestRunner extends InstrumentationTestRunner {
	

	public ServiceTestRunner() {
	    super();
	}
	@Override
	public void onStart(){
		super.onStart();
	} 

	@Override
	public InstrumentationTestSuite getAllTests() {
		InstrumentationTestSuite suite = new InstrumentationTestSuite(this);
		suite.addTestSuite(AppSettingsLoaderTest.class);
		suite.addTestSuite(AgencyLoaderTest.class);
		suite.addTestSuite(ProjectsLoaderTest.class);
		suite.addTestSuite(InspectionLoaderTest.class);
		suite.addTestSuite(ProjectModelTest.class);
		suite.addTestSuite(FeeLoaderTest.class);
		suite.addTestSuite(InstantServiceTest.class);
		suite.addTestSuite(InspectionTypeLoaderTest.class);
		suite.addTestSuite(InspectorLoaderTest.class);
		suite.addTestSuite(InspectionTimesLoaderTest.class);
	    return suite;
	}

	@Override
	public ClassLoader getLoader() {
	    return ServiceTestRunner.class.getClassLoader();
	}
}
