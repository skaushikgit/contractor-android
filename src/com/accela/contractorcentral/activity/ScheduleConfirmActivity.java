package com.accela.contractorcentral.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.inputmethod.InputMethodManager;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.fragment.ScheduleConfirmFragment;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.utils.Utils;
import com.accela.inspection.model.InspectionTimesModel;
import com.accela.mobile.AMLogger;
import com.accela.mobile.util.DateUtil;
import com.accela.record.model.DailyInspectionTypeModel;
import com.accela.record.model.RecordInspectionModel;
import com.flurry.android.FlurryAgent;

public class ScheduleConfirmActivity extends BaseActivity {

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("ScheduleConfirmActivity");
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
		String projectId = intent.getStringExtra("projectId");
		String recordId = intent.getStringExtra("recordId");
		boolean isReschedule = intent.getBooleanExtra("isReschedule", false);
		String comment = intent.getStringExtra("scheduleInspectionComment");
		DailyInspectionTypeModel inspectionTypeModel = (DailyInspectionTypeModel) intent.getExtras().getSerializable("inspectionTypeModel");
		InspectionTimesModel timeModel = (InspectionTimesModel) intent.getExtras().getSerializable("inspectionAvailableTime");
		RecordInspectionModel inspection = (RecordInspectionModel) intent.getExtras().getSerializable("recordInspectionModel");
		int source = intent.getIntExtra("source", AppConstants.CANCEL_INSPECTION_SOURCE_OTHER);
		setContentView(R.layout.activity_confirm_details);
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		ScheduleConfirmFragment fragment = new ScheduleConfirmFragment();
		if (inspection!=null) {
			//the insepction is scheduled or failed
			StringBuffer sb = new StringBuffer();
			ProjectModel project = AppInstance.getProjectsLoader().getProjectById(projectId);
			int status = Utils.checkInspectionStatus(inspection);
			
			if (project != null && project.getRecordById(recordId) != null) {
				String[] info = Utils.formatInspectionInfo(inspection);
				sb.append(info[0]);
				if (sb.length()>0)
					sb.append("\n");
				sb.append(info[1]);
				if(sb.length()> 0) {
					setActionBarTitle(sb.toString());
				} else {
					setActionBarTitle(status == AppConstants.INSPECTION_STATUS_FAILED ? R.string.inspection_failed: R.string.inspection_scheduled);
				}
			}
			if(timeModel!=null && timeModel.getStartDate()!=null && timeModel.getEndDate()!=null){

				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
				inspection.setScheduleDate(timeModel.getCurrentDate());
				try {
					inspection.setScheduleStartTime(DateUtil.to12HourTimeString(sdf.parse(timeModel.getStartDate())));
					inspection.setScheduleEndTime(DateUtil.to12HourTimeString(sdf.parse(timeModel.getEndDate())));
					inspection.setScheduleStartAMPM(DateUtil.toAMPMString(sdf.parse(timeModel.getStartDate())));
					inspection.setScheduleEndAMPM(DateUtil.toAMPMString(sdf.parse(timeModel.getEndDate())));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					AMLogger.logWarn(e.toString());
				}
			}
				
			fragment.initForInspection(projectId, recordId,  inspection, isReschedule, source);
		} else {
			setActionBarTitle(R.string.confirm_details);
			fragment.initForSchedule(projectId, recordId, inspectionTypeModel, timeModel, comment);
		}
		ft.add(R.id.rootContainer, fragment);
		ft.commit();
	}
}
