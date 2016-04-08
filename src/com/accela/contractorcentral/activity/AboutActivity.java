package com.accela.contractorcentral.activity;

import android.os.Bundle;
import android.view.View;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.flurry.android.FlurryAgent;


public class AboutActivity extends BaseActivity{
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		setActionBarTitle(R.string.About_Contractor_App);
		showBackButton(false);
		showRightButton(true, this.getString(R.string.close));
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("AboutActivity");
		FlurryAgent.onPageView();
		FlurryAgent.onStartSession(this, AppConstants.FLURRY_APIKEY);
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	@Override
	protected void onRightButtonPress() {
		this.finish();
	}

}
