package com.accela.contractorcentral.service;

import java.util.ArrayList;
import java.util.List;

import com.accela.contractorcentral.AppContext;
import com.accela.contractorcentral.activity.RefreshTokenActivity;
import com.accela.framework.AMException;
import com.accela.framework.action.AppAction;
import com.accela.framework.model.AppSettingModel;
import com.accela.framework.persistence.AMAsyncEntityListActionDelegate;
import com.accela.framework.persistence.AMDataIncrementalResponse;
import com.accela.framework.persistence.AMStrategy;
import com.accela.framework.persistence.AMStrategy.AMAccessStrategy;
import com.accela.mobile.AMError;

public class AppSettingsLoader {
	
	private final static String PHONE_NUMBER = "Agency Phone Number";
	private final static String ACTIVE_PERMIT_STATUS = "Active Record Statuses";
	
	private List<String> activeRecordStatus = new ArrayList<String>();
	private String phoneNumber;
	private AppAction action = new AppAction();
//	public static class AppSettingsItem{
//		String agency;
//		String phoneNumber;
//
//	}
//
//	
//	private Map<String, AppSettingsItem> downloadedSettings = new ConcurrentHashMap<String, AppSettingsItem>();
	
	AppSettingsLoader() {}
	
	private class RequestAppSettingsDelegate extends AMAsyncEntityListActionDelegate<AppSettingModel> {
		private String agency;
		private Boolean isContinueLoadProject;
		RequestAppSettingsDelegate(String agency, boolean isContinueLoadProject) {
			this.agency = agency;
			this.isContinueLoadProject = isContinueLoadProject;
		}
		
		@Override
		public void onCompleted(
				AMDataIncrementalResponse<AppSettingModel> response) {
			if (response != null && response.getResult() != null) {
				if (agency == null) {
					for (AppSettingModel setting : response.getResult()) {
						if (setting.getKey().equals(ACTIVE_PERMIT_STATUS)) {
							String statusList = setting.getValue();
							if (statusList != null) {
								for (String status : statusList.split(", ")) {
									activeRecordStatus.add(status);
								}
							}
						}
					}
				}

				for (AppSettingModel setting : response.getResult()) {
					if (setting.getKey().equals(PHONE_NUMBER)) {
						phoneNumber = setting.getValue();
					}
				}
			}

			if (isContinueLoadProject)
				AppInstance.getProjectsLoader().loadAllProjects(false);
		}

		@Override
		public void onFailure(Throwable error) {
			//remove from downloading queue
			if(error instanceof AMException) {
				AMException exception = (AMException) error;
				if(exception.getStatus() == AMError.ERROR_CODE_Unauthorized ) {
					RefreshTokenActivity.startRefreshTokenActivity();
				}
			}
			if(isContinueLoadProject)
				AppInstance.getProjectsLoader().loadAllProjects(false);
		}
	}
	
	public List<String> getActiveRecordStatus(){
		return this.activeRecordStatus;
	}
	
	public String getPhoneNumber(){
		return this.phoneNumber;
	}
	
	public void setAction(AppAction action){
		this.action = action;
	}
	
//	public AppSettingsItem getAppSettings(String agency){
//		if(this.downloadedSettings.containsKey(agency)){
//			return this.downloadedSettings.get(agency);
//		}
//		return null;
//	}
	
	public void loadAppSettings(String agency, boolean isContinueLoadProject){
			
			AMStrategy strategy = new  AMStrategy(AMAccessStrategy.Http);
			RequestAppSettingsDelegate appSettingsDelegate = new RequestAppSettingsDelegate(agency, isContinueLoadProject);
			action.getAppSettingsAsync(null, appSettingsDelegate, strategy, agency, AppContext.environment.toString(), AppContext.appId, AppContext.appSecret, null);
	}
}
