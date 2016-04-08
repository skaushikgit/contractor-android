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
 *   Created by jzhong on 03/13/2015.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */

package com.accela.contractorcentral.activity;



import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.fragment.CompletedInspectionFragment;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.utils.APIHelper;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.utils.Utils;
import com.accela.contractorcentral.view.ElasticScrollView;
import com.accela.mobile.AMLogger;
import com.accela.record.model.RecordInspectionModel;
import com.flurry.android.FlurryAgent;


public class InspectionDetailsActivity extends BaseActivity {

	RecordInspectionModel inspectionModel;
	ProgressDialog  mProgressDialog;
	private int ii = 0;
	private boolean isFailed;

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("InspectionDetailsActivity");
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

		Intent intent = this.getIntent();
		inspectionModel = (RecordInspectionModel) intent.getExtras().getSerializable("inspectionModel");
		isFailed = intent.getBooleanExtra("isFailed", false);
		if(inspectionModel==null) {
			Toast.makeText(this, "Something wrong, please set inspection", Toast.LENGTH_SHORT)
			.show();
			finish();
			return;
		}

		setContentView(R.layout.activity_inspection_details);
		setupElasticScrollView();
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();

		CompletedInspectionFragment fragment = new CompletedInspectionFragment();
		fragment.init(inspectionModel);
		//ft.add(R.id.rootContainer,  fragment);
		ft.replace(R.id.rootContainer, fragment);
		ft.commit();
		AMLogger.logInfo("InspectionDetailsActivity.onCreate:" + (ii++));
		//set action bar color and inspection history
		int actionColor;
		int statusBarColor;
		FrameLayout rescheduleInspectionView = (FrameLayout) findViewById(R.id.rescheduleInspectionView); 
		
		if(Utils.isInspectionFailed(inspectionModel)) {
		//	List<RecordInspectionModel> list = AppInstance.getProjectsLoader().getInspectionHistory(inspectionModel);
			fragment.setBottomSpace(true);
			//don't show inspection history because one permit can schedule inspection more than once. 
		/*	if(list.size() == 0) {
				fragment.setBottomSpace(true);
			} else {
				showInspectionHistory(list, list.size());	
			} */
			actionColor = getResources().getColor(R.color.red_failed_content);
			statusBarColor = getResources().getColor(R.color.red_failed_header);
			rescheduleInspectionView.setVisibility(View.VISIBLE);
		} else {
			actionColor = getResources().getColor(R.color.green_pass_content);
			statusBarColor = getResources().getColor(R.color.green_pass_header);
			rescheduleInspectionView.setVisibility(View.GONE);
		}
		this.setActionBarColor(actionColor);
		APIHelper.setPhoneStatusBarColor(this, statusBarColor);		
		//set action bar title
		String info[] = Utils.formatInspectionInfo(inspectionModel);
		StringBuffer sb = new StringBuffer();
		sb.append(info[0]);
		if (sb.length()>0)
			sb.append("\n");
		sb.append(info[1]);
		this.setActionBarTitle(sb.toString());

		showAddress(true);
		Button rescheduleBtn = (Button) this.findViewById(R.id.buttonRescheduleInspection);
		rescheduleBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				reschedule();
			}
		});
	} 
	

	private void reschedule(){
			if(inspectionModel == null) {
				//need to select inspection Type
				return;
			}
			if(inspectionModel.getRecordId_id()!=null && AppInstance.getProjectsLoader().getParentProject(inspectionModel.getRecordId_id())!=null){
				ActivityUtils.startChooseInspectionTimeActivity(this, AppInstance.getProjectsLoader().getParentProject(inspectionModel.getRecordId_id()).getProjectId(), 
						inspectionModel.getRecordId_id(), inspectionModel.getId(), Utils.generateDailyInspectionTypeModel(inspectionModel.getType()), inspectionModel, true);
				this.finish();
			}
	}
		

	private void showAddress(boolean show) {
		View view = findViewById(R.id.addressContainer);
		if(!show) {
			view.setVisibility(View.GONE);
			return;
		}
		ProjectModel projectModel = AppInstance.getProjectsLoader().getParentProject(inspectionModel.getRecordId_id());
		if(projectModel==null)
			return;
		TextView textView = (TextView) view.findViewById(R.id.textAddLine1);
		textView.setText(Utils.getAddressLine1AndUnit(projectModel.getAddress()));

		textView = (TextView) view.findViewById(R.id.textAddLine2);
		textView.setText(Utils.getAddressLine2(projectModel.getAddress()));
	}

	protected void showInspectionHistory(List<RecordInspectionModel> list, int listSize) {
		FragmentTransaction ft;
		CompletedInspectionFragment fragment;
		int row_count = 1;		
		for(RecordInspectionModel inspection: list) {
			ft = this.getSupportFragmentManager().beginTransaction();
			fragment = new CompletedInspectionFragment();
			if(row_count == list.size()) {
				fragment.setBottomSpace(true);
			} else {
				fragment.setBottomSpace(false);
			}
			fragment.init(inspection);
			ft.add(R.id.rootContainer,  fragment);
			ft.commit();
			row_count++;
		}
	}


	private void setupElasticScrollView() {
		ElasticScrollView scrollView = (ElasticScrollView) findViewById(R.id.scrollView);
		scrollView.setMaxOverScrollDistance(0, 100); 
	}
}
