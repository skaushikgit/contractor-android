package com.accela.contractorcentral.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.flurry.android.FlurryAgent;

public class MenuActivity extends Activity implements View.OnClickListener{
	
	private String projectId;
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("MenuActivity");
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
		//Lock the orientation
		ActivityUtils.lockActivityOrientation(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_project_menu);
		Button scheduleButton = (Button) findViewById(R.id.buttonSchedule);
		scheduleButton.setOnClickListener(this);
		
		ImageView scheduleImage = (ImageView) findViewById(R.id.imageSchedule);
		scheduleImage.setOnClickListener(this);
		
		View rootContainer = (View) findViewById(R.id.rootContainer);
		projectId = getIntent().getStringExtra("projectId");
		//if click on blank space, exit 
		rootContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	}


	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.imageSchedule:
		case R.id.buttonSchedule:
//			onSelectMenuButton(R.id.buttonSchedule);
			startSelectProject();
			break;
		default:
			break;
		}
        finish();//finishing activity 
	}
	
	private void startSelectProject() {
		if(projectId!=null && projectId.length()>0)
			ActivityUtils.startChoosePermitActivity(this, projectId);
		else
			ActivityUtils.startSelectProjectActivity(this);
	}
}
