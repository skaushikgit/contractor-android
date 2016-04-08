package com.accela.contractor.mock;

import java.util.ArrayList;
import java.util.List;

import com.accela.framework.action.AppAction;
import com.accela.framework.model.AppSettingModel;
import com.accela.framework.persistence.AMAsyncEntityListActionDelegate;
import com.accela.framework.persistence.AMDataIncrementalResponse;
import com.accela.framework.persistence.AMDataResponse;
import com.accela.framework.persistence.AMStrategy;
import com.accela.mobile.AMBatchSession;

public class AppActionMock extends AppAction{
	private final static String PHONE_KEY = "Agency Phone Number";
	private final static String PHONE_VALUE = "9255739045";
	private final static String STATUS_KEY = "Active Record Statuses";
	private final static String STATUS_VALUE = "INCOMPLETE, OPEN, PENDING, UNASSIGNED, APPROVED";
	
	@Override
	public void getAppSettingsAsync(AMBatchSession batchSession, final AMAsyncEntityListActionDelegate<AppSettingModel> entityActionDelegate, AMStrategy strategy, String agency, String environment, String appId, String appSecret, String keys){
		Thread thread = new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				List<AppSettingModel> settings = new ArrayList<AppSettingModel>();
				final AMDataResponse<AppSettingModel> amDataResponse= new AMDataResponse<AppSettingModel>(MockUtils.MockRequest, MockUtils.MockStrategy);
		
					AppSettingModel model1 = new AppSettingModel();
					model1.setKey(PHONE_KEY);
					model1.setValue(PHONE_VALUE);
					AppSettingModel model2 = new AppSettingModel();
					model2.setKey(STATUS_KEY);
					model2.setValue(STATUS_VALUE);
					settings.add(model1);
					settings.add(model2);
					amDataResponse.setResult(settings);
					amDataResponse.setHasMore(false);
					final AMDataIncrementalResponse<AppSettingModel> response = new AMDataIncrementalResponse<AppSettingModel>(amDataResponse, MockUtils.MockAmMobilityPersistence);
					MockUtils.MockHandler.post(new Runnable(){
						@Override
						public void run() {
							// TODO Auto-generated method stub
							entityActionDelegate.onCompleted(response);
						}
					});
					
				
				
			}
		});

		thread.start();
	}

}
