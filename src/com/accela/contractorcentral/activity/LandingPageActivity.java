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
 *   Created by jzhong on 3/10/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */

package com.accela.contractorcentral.activity;




import java.util.Observable;
import java.util.Observer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.AppConstants.AgencyListType;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.ProjectsLoader;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.utils.Utils;
import com.accela.contractorcentral.view.ElasticScrollView;
import com.accela.contractorcentral.view.InspectionViewPager;
import com.accela.contractorcentral.view.InspectionViewPager.OnSelectInspectionListener;
import com.accela.framework.authorization.AMAuthManager;
import com.accela.framework.model.CivicIdProfileModel;
import com.accela.mobile.AMLogger;
import com.accela.record.model.RecordInspectionModel;
import com.flurry.android.FlurryAgent;


public class LandingPageActivity extends BaseActivity implements Observer, OnSelectInspectionListener  {

	ProjectsLoader projectsLoader;
	InspectionViewPager viewPager;
	ElasticScrollView scrollView;
	
	TextView textDate;
	TextView textTime;
	TextView textAddress;
	TextView textPermitType;
	Button buttonScheduleInspection;
	Button buttonViewInspections;
	ProgressBar loadingProgress;
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("LandingPageActivity");
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

		
		setContentView(R.layout.activity_landing_page);
		showScheduleBtn(true, false);
		showBackButton(false);
		scrollView = (ElasticScrollView) findViewById(R.id.scrollView);
    	scrollView.setMaxOverScrollDistance(0, 60); 
    	initView();
    	View buttonProjects = findViewById(R.id.buttonProjects);
    	buttonProjects.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ActivityUtils.startProjectListActivity(LandingPageActivity.this);
			}
		});
    	
    	View buttonSettings = findViewById(R.id.buttonSettings);
    	buttonSettings.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ActivityUtils.startAgencyListActivity(LandingPageActivity.this, AgencyListType.MYAGENCY);
			}
		});
    	
    	 
    	CivicIdProfileModel profile = AMAuthManager.getInstance().getProfile();
		if(profile != null) {
			StringBuffer nameSb = new StringBuffer();
			if(profile.getFirstName()!=null)
				nameSb.append(profile.getFirstName());
			if(profile.getLastName()!=null)
				nameSb.append(" ").append(profile.getLastName());
			this.setActionBarTitle(nameSb.toString());
		} else { 
			this.setActionBarTitle(R.string.app_name);
		}
		
    	projectsLoader = AppInstance.getProjectsLoader();
    	projectsLoader.requestLocation(this);
    	projectsLoader.loadAllProjects(false);
    	updateInspection();
    	 
    	viewPager = (InspectionViewPager) findViewById(R.id.viewPager);
    	viewPager.setOnSelectInspectionListener(this);
		 
    	
    	Button buttonViewInspections = (Button) findViewById(R.id.buttonViewInspections);
    	buttonViewInspections.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {			
				ActivityUtils.startAllInspectionActivity(LandingPageActivity.this);				
			}
		});
    	
    /*	scrollView.setCallbacks(new ElasticScrollView.ScrollCallbacks() {
			
			@Override
			public void onScrollChanged(int l, int t, int oldl, int oldt) {
				if(t<0) {
					viewPager.setY(t);
				} else {
					viewPager.setY(t);
				}
				
			}
		});*/
    	
    	AppInstance.getAgencyLoader().loadAllLinkedAgency(false);
    	projectsLoader.addObserver(this);
	}
	

	private void initView() {
		// TODO Auto-generated method stub
    	 textDate = (TextView) findViewById(R.id.nextInspectionDate);
    	 textTime = (TextView) findViewById(R.id.nextInspectionTime);
    	 textAddress = (TextView) findViewById(R.id.textAddressInspection);
    	 textPermitType = (TextView) findViewById(R.id.textPermitType);
    	 buttonScheduleInspection = (Button) findViewById(R.id.buttonScheduleInspection);
    	 buttonViewInspections = (Button) findViewById(R.id.buttonViewInspections);
    	 loadingProgress = (ProgressBar) findViewById(R.id.spinnerInpsection);
    	 LinearLayout landingNextInspectionId = (LinearLayout) findViewById(R.id.landingNextInspectionId);
    	 landingNextInspectionId.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(projectsLoader.getNextInspection()!=null){
					String recordId = projectsLoader.getNextInspection().getRecordId_id();
					if(recordId==null)
						return;
					ActivityUtils.startScheduleInspectionActivity(LandingPageActivity.this, projectsLoader.getParentProject(recordId).getProjectId(), recordId, projectsLoader.getNextInspection(), AppConstants.CANCEL_INSPECTION_SOURCE_OTHER);
				}
			}	
		});
	}

	private void updateInspection() {
    	
    	buttonScheduleInspection.setVisibility(View.GONE);
    	
    	if(projectsLoader.isAllInspectionsDownloaded()) {
    		loadingProgress.setVisibility(View.GONE);
    	} else {
    		loadingProgress.setVisibility(View.VISIBLE);
    	}
    	
    	//get first schedule inspection.
    	RecordInspectionModel inspectionModel = projectsLoader.getNextInspection();
    	
    	if(inspectionModel != null) {
    		String inspectionInfo[] = Utils.formatInspectionInfo(inspectionModel);
    		textAddress.setText(inspectionInfo[0]);
			textAddress.setVisibility(View.VISIBLE);
			textPermitType.setVisibility(View.VISIBLE);
 			String date = Utils.getInspectionDate(inspectionModel);
			String time = Utils.getInspectionTime(inspectionModel);
			if(date==null && time==null) {
				textDate.setText(getString(R.string.none_schedule));
				textTime.setText("");
			} else {
				if(date!=null) {
					textDate.setText(date);
				} else {
					textDate.setText("");
				}
				if(time!=null) {
					textTime.setText(time);
				} else {
					textTime.setText("");
				}
			}
			textPermitType.setText(inspectionInfo[1]);
			buttonScheduleInspection.setVisibility(View.GONE);
		} else {
			textDate.setText(getString(R.string.none_schedule));
			textTime.setText("");
			textAddress.setVisibility(View.GONE);
			textPermitType.setVisibility(View.GONE);
			/*buttonScheduleInspection.setVisibility(View.VISIBLE);
			buttonScheduleInspection.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//scheduleInspection();
				}
			});*/
		}
	}
	 
	@Override
	public void onResume(){
		super.onResume();
    	if(projectsLoader.isAllInspectionsDownloaded()) {
    		loadingProgress.setVisibility(View.GONE);
    	} else {
    		loadingProgress.setVisibility(View.VISIBLE);
    	}
    	viewPager.checkLoadingProgress();
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		projectsLoader.deleteObserver(this);
	}
	
	@Override
	public void onBackPressed() {
		this.showLogoutConfirmMessage();
	}

	@Override
	public void update(Observable observable, Object data) {
		if(observable instanceof ProjectsLoader && data instanceof Integer) {
			int flag = (Integer) data;
			switch (flag) {
				case AppConstants.PROJECT_LIST_SCHEDULED_INSPECTION_CHANGE:
					AMLogger.logInfo("project completed list changed ");
					this.updateInspection();
					break;
			}
			
		} 	
		
	}
	
	
	
	@Override
	public void onSelectInspection(RecordInspectionModel inspection,
			int position) {
		
	}
	
	
	

}