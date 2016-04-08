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
 *   Created by jzhong on 2/10/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */
package com.accela.contractorcentral.activity;



import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.fragment.ChooseInspectionTimeFragment;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.ProjectsLoader;
import com.accela.mobile.AMLogger;
import com.accela.record.model.DailyInspectionTypeModel;
import com.accela.record.model.RecordInspectionModel;
import com.accela.record.model.RecordModel;
import com.flurry.android.FlurryAgent;


public class ChooseInspectionTimeActivity extends BaseActivity {

	ChooseInspectionTimeFragment fragmentChooseTime;

	
	ViewPager viewPager;

	
	String projectId;
	String recordId;
	long inspectionId;
	ProjectModel projectModel;
	DailyInspectionTypeModel inspectionType;
	RecordModel model;
	
	ProjectsLoader projectsLoader = AppInstance.getProjectsLoader();
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("ChooseInspectionTimeActivity");
		FlurryAgent.onPageView();
		FlurryAgent.onStartSession(this, AppConstants.FLURRY_APIKEY);
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//get project Id
		Intent intent = this.getIntent();
		recordId = intent.getStringExtra("recordId");
		projectId = intent.getStringExtra("projectId");
		inspectionId = intent.getLongExtra("inspectionId", 0l);
		RecordInspectionModel inspection = (RecordInspectionModel) intent.getSerializableExtra("recordInspectionModel");
		boolean isReschedule = intent.getBooleanExtra("isReschedule", false);
		inspectionType = (DailyInspectionTypeModel) intent.getExtras().getSerializable("inspectionType");
		projectModel = AppInstance.getProjectsLoader().getProjectById(projectId);
		if(projectModel==null || recordId == null || inspectionType == null) {
			finish();
			AMLogger.logInfo("Error!!, Please select a inspection type");
			return;
		}
		AMLogger.logInfo("start ChooseInspectionTimeActivity");
		setContentView(R.layout.activity_choose_inspection_time);
		this.setActionBarTitle(R.string.choose_time);
		
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		
		fragmentChooseTime = new ChooseInspectionTimeFragment();
		AMLogger.logInfo("ChooseInspectionTimeActivity::fragmentChooseTime.setInspectionType:" + inspectionType.getId());
		fragmentChooseTime.setInspectionType(projectId, recordId, inspectionId, inspectionType, inspection, isReschedule);
		ft.replace(R.id.rootContainer,  fragmentChooseTime);
		ft.commit();
		//get project Id
		
	}
	
	@Override
	public void onBackPressed() {
		if(fragmentChooseTime.handleBackButton()) {
			return;
		} else {
			exitActivityWithAnimated();
		}
	}


}