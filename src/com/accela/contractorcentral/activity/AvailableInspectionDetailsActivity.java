package com.accela.contractorcentral.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.utils.Utils;
import com.accela.record.model.DailyInspectionTypeModel;
import com.flurry.android.FlurryAgent;

public class AvailableInspectionDetailsActivity extends BaseActivity {
	DailyInspectionTypeModel inspectionType;
	String projectId;
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("AvailableInspectionDetailsActivity");
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
		inspectionType = (DailyInspectionTypeModel) intent.getExtras().getSerializable("inspectionType");
		projectId = (String) intent.getExtras().getSerializable("projectId");
		if(inspectionType==null) {
			Toast.makeText(this, "Something wrong, please set inspection", Toast.LENGTH_SHORT)
			.show();
			finish();
			return;
		}
		setActionBarTitle(R.string.inspection_scheduled);
		setContentView(R.layout.activity_available_inspection_details);
		
		Button buttonScheduleInspection = (Button) findViewById(R.id.buttonScheduleInspection);
		buttonScheduleInspection.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startChooseInspectionActivity();
			}
		});
		
		loadInspectionType(inspectionType);
	}

	private void startChooseInspectionActivity(){
		ActivityUtils.startChooseInspectionTimeActivity(this, projectId, projectId, inspectionType.getId(), inspectionType, null, false);
	}
	
	private void loadInspectionType(DailyInspectionTypeModel inspectionType) {
		TextView textInspectionGroup = (TextView) findViewById(R.id.textInspectionGroup);
		TextView textInspectionType = (TextView) findViewById(R.id.textInspectionType);
		
		String[] inspectionInfo = Utils.formatInspectionTypeInfo(inspectionType, projectId);
		textInspectionGroup.setText(inspectionInfo[0]);
		textInspectionType.setText(inspectionInfo[1]);
	}
}
