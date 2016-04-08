
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

package com.accela.contractor.activity.test;





import junit.framework.Assert;
import android.app.Activity;
import android.app.Instrumentation;
import android.graphics.Rect;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.accela.contractor.R;
import com.accela.contractor.activity.LandingPageActivity;
import com.accela.contractor.activity.ProjectListActivity;
import com.accela.contractor.mock.MockUtils;
import com.accela.contractor.service.AppInstance;
import com.accela.contractor.view.InspectionViewPager;



public class LandingPageActivityTest extends ActivityInstrumentationTestCase2<LandingPageActivity> {


	private static final int TIMEOUT_IN_MS = 5000;
	private LandingPageActivity mLadingActivity;


	public LandingPageActivityTest() {
	    super("com.accela.contractor", LandingPageActivity.class);
	}
	

   
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		AppInstance.getAppSettingsLoader().setAction(MockUtils.AppActionMock);
		AppInstance.getProjectsLoader().setAction(MockUtils.RecordActionMock);
		AppInstance.getInpsectionLoader().setAction(MockUtils.RecordActionMock);
		AppInstance.getAgencyLoader().setAction(MockUtils.CivicIdActionMock);
		AppInstance.getAppSettingsLoader().loadAppSettings(null, false);
		setActivityInitialTouchMode(true);
		mLadingActivity = getActivity();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		mLadingActivity = null;
		AppInstance.clearAll();
	}

	public void testInpsectionsPager() throws InterruptedException {
		final InspectionViewPager viewPager = (InspectionViewPager) mLadingActivity.findViewById(R.id.viewPager);
		getInstrumentation().runOnMainSync(new Runnable() {
		    @Override
		    public void run() {
		    	viewPager.requestFocus();
		    }
		});
		getInstrumentation().waitForIdleSync();
		Rect rect = new Rect();
		viewPager.getHitRect(rect);

		TouchUtils.dragViewToX(this, viewPager, Gravity.RIGHT, rect.left); // To drag left. Make sure the view is to the right of rect.left
		Thread.sleep(500);

		TouchUtils.dragViewToX(this, viewPager, Gravity.LEFT, rect.right); // To drag left. Make sure the view is to the left of rect.right
		final LinearLayout projects = (LinearLayout) mLadingActivity.findViewById(R.id.buttonProjects);
	    Instrumentation.ActivityMonitor monitor = getInstrumentation().addMonitor(ProjectListActivity.class.getName(), null, false);

		TouchUtils.clickView(this, projects);
		Thread.sleep(3000);
		Activity currentActivity = monitor.waitForActivityWithTimeout(TIMEOUT_IN_MS);
		assertNotNull("Activity is  null", currentActivity);
		FrameLayout frameLayout = (FrameLayout) currentActivity.findViewById(R.id.projectListView);
		ListView listview = (ListView) frameLayout.getChildAt(0);
		assertEquals("", listview.getCount(), 3);
		this.sendKeys(KeyEvent.KEYCODE_BACK);
		TouchUtils.dragViewToX(this, viewPager, Gravity.RIGHT, rect.left); 
		Thread.sleep(500);
		TouchUtils.dragViewToX(this, viewPager, Gravity.RIGHT, rect.left); 
		Thread.sleep(500);
		TouchUtils.dragViewToX(this, viewPager, Gravity.RIGHT, rect.left); 
		Thread.sleep(500);

        getInstrumentation().removeMonitor(monitor);

	}

}