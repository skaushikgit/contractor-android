package com.accela.contractorcentral.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.fragment.ContactApprovedInspectionFragment;
import com.accela.record.model.RecordInspectionModel;
import com.flurry.android.FlurryAgent;

public class ShowContactsLayout extends BaseActivity {

	ContactApprovedInspectionFragment fragment;
	String projectId;
	boolean isFailed;
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("ShowContactsLayout");
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
		setContentView(R.layout.activity_show_contacts_layout);
		setActionBarTitle(R.string.contacts);
		Intent intent = this.getIntent();
		projectId = intent.getStringExtra("projectId");
		isFailed = intent.getBooleanExtra("isFailed", false);
		RecordInspectionModel recordInspectionModel = (RecordInspectionModel)intent.getExtras().getSerializable("recordInspectionModel");

		String permitId = intent.getStringExtra("permitId");
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		fragment = new ContactApprovedInspectionFragment();
		ft.add(R.id.rootContainer, fragment);
		fragment.init(projectId, recordInspectionModel, isFailed);
		ft.commit();
	}
	
	public void exitActivityWithAnimated() {
		finish();
		overridePendingTransition(0, R.anim.moveout_to_bottom);
	}

}
