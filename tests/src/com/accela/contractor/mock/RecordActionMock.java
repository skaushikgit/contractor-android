package com.accela.contractor.mock;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.Handler;
import android.os.Looper;

import com.accela.framework.persistence.AMAsyncEntityListActionDelegate;
import com.accela.framework.persistence.AMDataIncrementalResponse;
import com.accela.framework.persistence.AMDataResponse;
import com.accela.framework.persistence.AMStrategy;
import com.accela.framework.service.CorelibManager;
import com.accela.mobile.AMBatchSession;
import com.accela.record.action.RecordAction;
import com.accela.record.model.RecordInspectionModel;
import com.accela.record.model.RecordModel;


public class RecordActionMock extends RecordAction {



	@Override
	public void getMyRecordAsync(AMBatchSession batchSession, final AMAsyncEntityListActionDelegate<RecordModel> entityActionDelegate, AMStrategy strategy, Boolean isMultipleAgencies, String type, String openedDateFrom, String openedDateTo, String customId, String module, String status, String assignedDateFrom, String assignedDateTo, String completedDateFrom, String completedDateTo, String completedByDepartment, String completedByStaff, String closedDateFrom, String closedDateTo, String closedByDepartment, String closedByStaff, int limit, int offset)
	{	Thread thread = new Thread(new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(entityActionDelegate!=null){			
				List<RecordModel> records = new ArrayList<RecordModel>();
				AMDataResponse<RecordModel> amDataResponse= new AMDataResponse<RecordModel>(MockUtils.MockRequest, MockUtils.MockStrategy);
				try {
					records.add(MockUtils.MockJsonHelper.parseObject(MockData.project1Json, RecordModel.class));
					records.add(MockUtils.MockJsonHelper.parseObject(MockData.project2Json, RecordModel.class));
					records.add(MockUtils.MockJsonHelper.parseObject(MockData.project3Json, RecordModel.class));
					amDataResponse.setResult(records);
					amDataResponse.setHasMore(false);
					final AMDataIncrementalResponse<RecordModel> response = new AMDataIncrementalResponse<RecordModel>(amDataResponse, MockUtils.MockAmMobilityPersistence);
					Thread.sleep(2000);
					MockUtils.MockHandler.post(new Runnable(){
						@Override
						public void run() {
							// TODO Auto-generated method stub
							entityActionDelegate.onCompleted(response);
						}
					});
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		});
		thread.start();

	}
	
	@Override
	public void getRecordInspectionsAsync(AMBatchSession batchSession, final AMAsyncEntityListActionDelegate<RecordInspectionModel> entityActionDelegate, String agency, String environment, final AMStrategy strategy, final String recordId, int offset, int limit){
		
			Thread thread = new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(entityActionDelegate!=null && recordId!=null){			
					List<RecordInspectionModel> records = new ArrayList<RecordInspectionModel>();
					AMDataResponse<RecordInspectionModel> amDataResponse= new AMDataResponse<RecordInspectionModel>(MockUtils.MockRequest, MockUtils.MockStrategy);
					try {
						if(recordId.contains("15CAP-00000-00076")){
							records.add(MockUtils.MockJsonHelper.parseObject(MockData.inspection1Json, RecordInspectionModel.class));
							records.add(MockUtils.MockJsonHelper.parseObject(MockData.inspection2Json, RecordInspectionModel.class));
						}else if(recordId.contains("15CAP-00000-00077")){
							records.add(MockUtils.MockJsonHelper.parseObject(MockData.inspection3Json, RecordInspectionModel.class));
							records.add(MockUtils.MockJsonHelper.parseObject(MockData.inspection4Json, RecordInspectionModel.class));
						}else if(recordId.contains("15CAP-00000-00078")){
							records.add(MockUtils.MockJsonHelper.parseObject(MockData.inspection5Json, RecordInspectionModel.class));
							records.add(MockUtils.MockJsonHelper.parseObject(MockData.inspection6Json, RecordInspectionModel.class));
							records.add(MockUtils.MockJsonHelper.parseObject(MockData.inspectionLastMonthJson, RecordInspectionModel.class));
						}
						amDataResponse.setResult(records);
						amDataResponse.setHasMore(false);
						Thread.sleep(1000);
						final AMDataIncrementalResponse<RecordInspectionModel> response = new AMDataIncrementalResponse<RecordInspectionModel>(amDataResponse, MockUtils.MockAmMobilityPersistence);
						MockUtils.MockHandler.post(new Runnable(){
							@Override
							public void run() {
								// TODO Auto-generated method stub
								entityActionDelegate.onCompleted(response);
							}
						});
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			});
			thread.start();
	
		
	}

}
