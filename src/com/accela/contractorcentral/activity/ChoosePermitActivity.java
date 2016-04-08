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
import android.view.View;
import android.widget.AdapterView;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.view.PermitListView;
import com.accela.mobile.AMLogger;
import com.accela.record.model.RecordModel;
import com.flurry.android.FlurryAgent;


public class ChoosePermitActivity extends BaseActivity  {

	PermitListView listViewPermit;
	ProjectModel projectModel;
	String projectId;
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("ChoosePermitActivity");
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
		setActionBarTitle(R.string.permits);
		//get project Id
		Intent intent = this.getIntent();
		projectId = intent.getStringExtra("projectId");
		projectModel = AppInstance.getProjectsLoader().getProjectById(projectId);
		if(projectModel==null) {
			finish();
			AMLogger.logInfo("Error!!, Please select a project");
			return;
		}
		if(projectModel.getRecords().size()==1 && projectModel.getRecords().get(0)!=null){
			startChooseInspectionType(projectModel.getRecords().get(0).getId());
			finish();
			return;
		}
		setContentView(R.layout.activity_choose_permit);
		
		PermitListView listViewPermit = (PermitListView) findViewById(R.id.permitListView);
		listViewPermit.setProject(projectModel);
		listViewPermit.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		listViewPermit.setMaxOverScrollDistance(0, 100); 
		listViewPermit.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(position == 0) {
					return;
				}
				RecordModel record = projectModel.getRecords().get(position - 1);
				startChooseInspectionType(record.getId());
			}
		});
		
	}
	
	protected void startChooseInspectionType(String permitId) {
		ActivityUtils.startChooseInspectionTypeActivity(this, projectId, permitId);
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}
	

}