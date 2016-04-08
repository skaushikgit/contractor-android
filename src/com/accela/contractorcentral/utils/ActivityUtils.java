/*
 * 
 * 
 *   Created by jzhong on 2/12/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */

package com.accela.contractorcentral.utils;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import com.accela.contractorcentral.R;
import com.accela.contractorcentral.AppConstants.AgencyListType;
import com.accela.contractorcentral.activity.AgencyConfigureActivity;
import com.accela.contractorcentral.activity.AgencyListActivity;
import com.accela.contractorcentral.activity.AllInspectionActivity;
import com.accela.contractorcentral.activity.AvailableInspectionDetailsActivity;
import com.accela.contractorcentral.activity.ChooseInspectionTimeActivity;
import com.accela.contractorcentral.activity.ChooseInspectionTypeActivity;
import com.accela.contractorcentral.activity.ChoosePermitActivity;
import com.accela.contractorcentral.activity.EnterNewContactActivity;
import com.accela.contractorcentral.activity.ExpandedImageActivity;
import com.accela.contractorcentral.activity.InspectionDetailsActivity;
import com.accela.contractorcentral.activity.LandingPageActivity;
import com.accela.contractorcentral.activity.ProjectDetailsActivity;
import com.accela.contractorcentral.activity.ProjectListActivity;
import com.accela.contractorcentral.activity.ScheduleConfirmActivity;
import com.accela.contractorcentral.activity.ScheduleSuccessActivity;
import com.accela.contractorcentral.activity.SelectProjectActivity;
import com.accela.contractorcentral.activity.ShowContactsLayout;
import com.accela.framework.model.AgencyModel;
import com.accela.framework.model.InspectionModel;
import com.accela.inspection.model.InspectionTimesModel;
import com.accela.record.model.DailyInspectionTypeModel;
import com.accela.record.model.RecordInspectionModel;

public class ActivityUtils {
	public static void startLandingPageActivity(Activity activity) {
		Intent intent = new Intent(activity, LandingPageActivity.class);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.movein_left, R.anim.zoomout);
	}
	
	public static void startLandingPageActivityFromLeft(Activity activity) {
		Intent intent = new Intent(activity, LandingPageActivity.class);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.zoomin, R.anim.moveout_right);
	}
	
	public static void startProjectListActivity(Activity activity) {
		Intent intent = new Intent(activity, ProjectListActivity.class);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.movein_left, R.anim.zoomout);
	}

	public static void startProjectDetailsActivity(Activity activity, String projectId, int tabIndex, RecordInspectionModel inspectionModel) {
		Intent intent = new Intent(activity, ProjectDetailsActivity.class);
		intent.putExtra("projectId", projectId);
		intent.putExtra("tabIndex", tabIndex);
		intent.putExtra("inspectionModel", inspectionModel);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.movein_left, R.anim.zoomout);
	}
	
	public static void startSelectProjectActivity(Activity activity) {
		Intent intent = new Intent(activity, SelectProjectActivity.class);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.movein_left, R.anim.zoomout);
	}
	
	public static void startAllInspectionActivity(Activity activity) {
		Intent intent = new Intent(activity, AllInspectionActivity.class);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.movein_left, R.anim.zoomout);
	}

	public static void startInspectionDetailsActivity(Activity activity, RecordInspectionModel inspectionModel, boolean isFailed, int source) {
		Intent intent = new Intent(activity, InspectionDetailsActivity.class);
		intent.putExtra("inspectionModel", inspectionModel);
		intent.putExtra("isFailed", isFailed);
		intent.putExtra("source", source);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.movein_left, R.anim.zoomout);
	}
	
	public static void startAvailableInspectionDetailsActivity(Activity activity, DailyInspectionTypeModel inspectionType, String projectId) {
		Intent intent = new Intent(activity, AvailableInspectionDetailsActivity.class);
		intent.putExtra("inspectionType", inspectionType);
		intent.putExtra("projectId", projectId);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.movein_left, R.anim.zoomout);
	}
	
	public static void startChoosePermitActivity(Activity activity, String projectId) {
		Intent intent = new Intent(activity, ChoosePermitActivity.class);
		intent.putExtra("projectId", projectId);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.movein_left, R.anim.zoomout);
	}
	
	public static void startChooseInspectionTypeActivity(Activity activity, String projectId, String permitId) {
		Intent intent = new Intent(activity, ChooseInspectionTypeActivity.class);
		intent.putExtra("recordId", permitId);
		intent.putExtra("projectId", projectId);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.movein_left, R.anim.zoomout);
	}
	
	public static void startInspectionContactActivity(Activity activity, String projectId, String permitId, RecordInspectionModel inspection,  boolean isFailed) {
		Intent intent = new Intent(activity, ShowContactsLayout.class);
		intent.putExtra("projectId", projectId);
		intent.putExtra("isFailed", isFailed);
		intent.putExtra("permitId", permitId);
		intent.putExtra("recordInspectionModel", inspection);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.movein_from_bottom, 0);
	}
	
	public static void startChooseInspectionTimeActivity(Activity activity, String projectId, String permitId, long inspectionId, DailyInspectionTypeModel inspectionType, RecordInspectionModel inspection,  boolean isReschedule) {
		Intent intent = new Intent(activity, ChooseInspectionTimeActivity.class);
		intent.putExtra("inspectionType", inspectionType);
		intent.putExtra("inspectionId", inspectionId);
		intent.putExtra("recordInspectionModel", inspection);
		intent.putExtra("recordId", permitId);
		intent.putExtra("projectId", projectId);
		intent.putExtra("isReschedule", isReschedule);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.movein_left, R.anim.zoomout);
	}
	
	public static void startScheduleInspectionActivity(Activity activity, String projectId, String permitId,
			DailyInspectionTypeModel inspectionTypeModel, InspectionTimesModel timeModel, RecordInspectionModel inspection, boolean isReschedule) {
		Intent intent = new Intent(activity, ScheduleConfirmActivity.class);
		intent.putExtra("inspectionTypeModel", inspectionTypeModel);
		intent.putExtra("inspectionAvailableTime", timeModel);
		intent.putExtra("recordId", permitId);
		intent.putExtra("projectId", projectId);
		intent.putExtra("isReschedule", isReschedule);
		intent.putExtra("recordInspectionModel", inspection);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.movein_left, R.anim.zoomout);
	}
	
	public static void startScheduleInspectionActivity(Activity activity, String projectId, String permitId,
			DailyInspectionTypeModel inspectionTypeModel, InspectionTimesModel timeModel, RecordInspectionModel inspection, boolean isReschedule, int source) {
		Intent intent = new Intent(activity, ScheduleConfirmActivity.class);
		intent.putExtra("inspectionTypeModel", inspectionTypeModel);
		intent.putExtra("inspectionAvailableTime", timeModel);
		intent.putExtra("recordId", permitId);
		intent.putExtra("projectId", projectId);
		intent.putExtra("isReschedule", isReschedule);
		intent.putExtra("recordInspectionModel", inspection);
		intent.putExtra("source", source);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.movein_left, R.anim.zoomout);
	}
	
	public static void startScheduleInspectionActivity(Activity activity, String projectId, String permitId,
			RecordInspectionModel inspection, int source) {
		Intent intent = new Intent(activity, ScheduleConfirmActivity.class);
		intent.putExtra("recordInspectionModel", inspection);
		intent.putExtra("recordId", permitId);
		intent.putExtra("projectId", projectId);
		intent.putExtra("source", source);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.movein_left, R.anim.zoomout);
	}
	
	public static void startExpandedImageActivity(Activity activity, String projectId, int focusedIndex) {
		Intent intent = new Intent(activity, ExpandedImageActivity.class);
		intent.putExtra("projectId", projectId);
		intent.putExtra("focusedIndex", focusedIndex);
		activity.startActivity(intent);
		//activity.overridePendingTransition(R.anim.movein_left, R.anim.zoomout);
	}
	
	public static void startEnterNewContactActivity(Activity activity) {
		Intent intent = new Intent(activity, EnterNewContactActivity.class);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.movein_left, R.anim.zoomout);
	}

	public static void startAgencyListActivity(Activity activity) {
//		Intent intent = new Intent(activity, AgencySettingActivity.class);
	}
	
	public static void startAgencyListActivity(Activity activity, AgencyListType agencyListType) {
		Intent intent = new Intent(activity, AgencyListActivity.class);
		intent.putExtra("agencyListType", agencyListType);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.movein_left, R.anim.zoomout);
	}
	
//	public static void startPickAgencyListActivity(Activity activity) {
//		Intent intent = new Intent(activity, AgencyListActivity.class);
//		intent.putExtra("pickAgency", AppConstants.AgencyListType.ADDAGENCY.toString());
//		activity.startActivity(intent);
//		activity.overridePendingTransition(R.anim.movein_left, R.anim.zoomout);
//	}
	
	public static void startAgencyConfigureActivity(Activity activity, AgencyModel agency, int requestCode) {
		Intent intent = new Intent(activity, AgencyConfigureActivity.class);
		intent.putExtra("agency", agency);
		activity.startActivityForResult(intent, requestCode);
		activity.overridePendingTransition(R.anim.movein_left, R.anim.zoomout);
	}
	
	public static void lockActivityOrientation(Activity activity) {
		int currentOrientation = activity.getResources().getConfiguration().orientation;
		if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		}
		else {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		}
	}
	
	public static void setActivityPortrait(Activity activity) {
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
	}
	
	public static void inspectionSuccessScreen(Activity activity, String projectId, String recordId, InspectionModel inspection, String profilePath){
		Intent intent = new Intent(activity, ScheduleSuccessActivity.class);

		DailyInspectionTypeModel dailyTypeModel = new DailyInspectionTypeModel();
		if(inspection.getType()!=null){
			dailyTypeModel.setGroup(inspection.getType().getGroup());
			dailyTypeModel.setText(inspection.getType().getText());
			dailyTypeModel.setValue(inspection.getType().getValue());
			dailyTypeModel.setId(inspection.getType().getId());
		}
		intent.putExtra("inspectionTypeModel", dailyTypeModel);
		InspectionTimesModel timeModel = new InspectionTimesModel();
		StringBuffer startDate = new StringBuffer();
		if(inspection.getScheduleStartTime()!=null)
			startDate.append(Utils.formatTime(inspection.getScheduleStartTime()));
		if(inspection.getScheduleStartAMPM()!=null)
			startDate.append(" ").append(inspection.getScheduleStartAMPM());
		StringBuffer endDate = new StringBuffer();
		if(inspection.getScheduleEndTime()!=null)
			endDate.append(Utils.formatTime(inspection.getScheduleEndTime()));
		if(inspection.getScheduleEndAMPM()!=null)
			endDate.append(" ").append(inspection.getScheduleEndAMPM());

		timeModel.setCurrentDate(inspection.getScheduleDate());
		timeModel.setStartDate(startDate.toString());
		timeModel.setEndDate(endDate.toString());
		intent.putExtra("inspectionAvailableTime",  timeModel);
		intent.putExtra("scheduleInspectionComment", inspection.getRequestComment());//textView.getText().toString());
		intent.putExtra("projectId", projectId);
		intent.putExtra("recordId", recordId);
		intent.putExtra("profilePath", profilePath);
		if(inspection.getContact()!=null){
			intent.putExtra("ContactFirstName", inspection.getContact().getFirstName());
			intent.putExtra("ContactLastName", inspection.getContact().getLastName());
			intent.putExtra("ContactPhone", inspection.getContact().getPhone1());
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(intent);
		activity.finish();
	}
	
	
}
