package com.accela.contractorcentral.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.fragment.ScheduleConfirmFragment;
import com.accela.inspection.model.InspectionTimesModel;
import com.accela.record.model.DailyInspectionTypeModel;
import com.flurry.android.FlurryAgent;

public class ScheduleSuccessActivity extends BaseActivity {

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("ScheduleSuccessActivity");
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
		setActionBarTitle(R.string.Inspection_scheduled);
		Intent intent = this.getIntent();
		String projectId = intent.getStringExtra("projectId");
		String recordId = intent.getStringExtra("recordId");
		String comment = intent.getStringExtra("scheduleInspectionComment");
		String fName = intent.getStringExtra("ContactFirstName");
		String lName = intent.getStringExtra("ContactLastName");
		String phoneNum = intent.getStringExtra("ContactPhone");
		Long inspectionId = intent.getLongExtra("inspectionId", 0l);
		String profilePath = intent.getStringExtra("profilePath");
		DailyInspectionTypeModel inspectionTypeModel = (DailyInspectionTypeModel) intent.getExtras().getSerializable("inspectionTypeModel");
		InspectionTimesModel timeModel = (InspectionTimesModel) intent.getExtras().getSerializable("inspectionAvailableTime");
		setContentView(R.layout.activity_confirm_details);
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		ScheduleConfirmFragment fragment = new ScheduleConfirmFragment();
		fragment.initForResult(projectId, recordId, inspectionTypeModel, timeModel, comment);
		fragment.setContact(phoneNum, fName, lName, profilePath);
		ft.add(R.id.rootContainer,  fragment);
		ft.commit();
	}
	
	@Override
	public void onBackPressed() {
		finish();
		Intent intent = new Intent(this, ProjectListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startActivity(intent);
	}
}
