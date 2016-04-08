package com.accela.contractorcentral.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.flurry.android.FlurryAgent;


public class TermsOfUseActivity extends BaseActivity{

	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("TermsOfUseActivity");
		FlurryAgent.onPageView();
		FlurryAgent.onStartSession(this, AppConstants.FLURRY_APIKEY);
	}

    protected void onStop(){
    	super.onStop();
    	FlurryAgent.onEndSession(this);
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Lock the orientation
		setContentView(R.layout.activity_term);
		setActionBarTitle(R.string.terms_of_Use);
		showBackButton(true);
		Button acceptBtn = (Button) findViewById(R.id.acceptTermBtnId);
		acceptBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(TermsOfUseActivity.this, SignUpActivity.class);
				intent.putExtra("term_accept_result", true);
				startActivity(intent);
				finish();
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		finish();
	}
}
