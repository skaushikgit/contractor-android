package com.accela.contractor.test.runner;


import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;

import com.accela.contractor.activity.test.AllInspectionActivityTest;
import com.accela.contractor.activity.test.LandingPageActivityTest;


public class ActivityTestRunner extends InstrumentationTestRunner {
	public ActivityTestRunner() {
	    super();
	}
	@Override
	public void onStart(){
		super.onStart();
	} 

	@Override
	public InstrumentationTestSuite getAllTests() {
		InstrumentationTestSuite suite = new InstrumentationTestSuite(this);
		suite.addTestSuite(LandingPageActivityTest.class);
		suite.addTestSuite(AllInspectionActivityTest.class);

	    return suite;
	}

	@Override
	public ClassLoader getLoader() {
	    return ServiceTestRunner.class.getClassLoader();
	}
}
