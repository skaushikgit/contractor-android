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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.utils.Utils;
import com.accela.contractorcentral.view.InspectionTypeListView;
import com.accela.mobile.AMLogger;
import com.accela.record.model.DailyInspectionTypeModel;
import com.accela.record.model.RecordInspectionModel;
import com.flurry.android.FlurryAgent;


public class ChooseInspectionTypeActivity extends BaseActivity  {

	InspectionTypeListView listView;
	String recordId;
	String projectId;
	DailyInspectionTypeModel inspectionType;
	ProjectModel projectModel;
	ImageView buttonConfirm;
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("ChooseInspectionTypeActivity");
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
		projectModel = AppInstance.getProjectsLoader().getProjectById(projectId);
		if(projectModel==null || recordId == null) {
			finish();
			AMLogger.logInfo("Error!!, Please select a project and permit");
			return;
		}
		setContentView(R.layout.activity_choose_inspection_type);
		setActionBarTitle(Utils.getAddressLine1AndUnit(projectModel.getAddress()));
		listView = (InspectionTypeListView) findViewById(R.id.listViewInspectionType);
		listView.setFocusItemVisibility(true);
		listView.setPermit(recordId, 0, null);
		listView.showAddress(false);
		listView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		listView.setMaxOverScrollDistance(0, 100); 
		
		buttonConfirm = (ImageView) findViewById(R.id.buttonConfirm);
		buttonConfirm.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startChooseInspectionTime();
			}
		});
		buttonConfirm.setVisibility(View.GONE);
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(position >=0 && buttonConfirm.getVisibility() != View.VISIBLE && 
						(listView.getRecordInspectionModel()!=null || listView.getInspectionType()!=null) ) {
					buttonConfirm.setVisibility(View.VISIBLE);
					Animation animation = AnimationUtils.loadAnimation(ChooseInspectionTypeActivity.this, R.anim.movein_from_bottom);
					buttonConfirm.startAnimation(animation);
				}
				
			}
		});
		
	}
	 
	protected void startChooseInspectionTime() {
		
		RecordInspectionModel inspection = listView.getRecordInspectionModel();
		
		if(inspection!=null){
			int status = Utils.checkInspectionStatus(inspection);
			if(status == AppConstants.INSPECTION_STATUS_FAILED) {
				//if inspection failed, show the details
				//ActivityUtils.startInspectionDetailsActivity(this, inspection);
				String projectId = AppInstance.getProjectsLoader().getParentProject(inspection.getRecordId_id()).getProjectId();
				ActivityUtils.startChooseInspectionTimeActivity(this, 
						projectId, inspection.getRecordId_id(), inspection.getId(), 
						Utils.generateDailyInspectionTypeModel(inspection.getType()), inspection, false);
			} else if(status == AppConstants.INSPECTION_STATUS_PASSED) {
				//if inspection passed, show the details
				ActivityUtils.startInspectionDetailsActivity(this, inspection, Utils.isInspectionFailed(inspection), AppConstants.CANCEL_INSPECTION_SOURCE_OTHER);
			} else {
				//if scheduled inspection, goto "cancel/reschedule" activity
				ActivityUtils.startScheduleInspectionActivity(this, projectId, recordId, listView.getRecordInspectionModel(), AppConstants.CANCEL_INSPECTION_SOURCE_OTHER);
			}
		} else {
			inspectionType = listView.getInspectionType();
			if(inspectionType != null) {
				if(inspectionType.getHasSchdulePermission()!=null && inspectionType.getHasSchdulePermission().equalsIgnoreCase("Y"))
					ActivityUtils.startChooseInspectionTimeActivity(this, projectId, recordId, 0l, inspectionType, null, false);
				else{
					Toast.makeText(this, getString(R.string.inspection_type_no_permisstion_to_schedule), Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}
	


}