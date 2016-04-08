package com.accela.contractorcentral.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.activity.RefreshTokenActivity;
import com.accela.contractorcentral.utils.Utils;
import com.accela.framework.AMException;
import com.accela.framework.persistence.AMAsyncEntityListActionDelegate;
import com.accela.framework.persistence.AMDataIncrementalResponse;
import com.accela.framework.persistence.AMStrategy;
import com.accela.framework.persistence.AMStrategy.AMAccessStrategy;
import com.accela.mobile.AMBatchSession;
import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.accela.record.action.RecordAction;
import com.accela.record.model.RecordInspectionModel;
import com.accela.record.model.RecordModel;



public class InspectionLoader extends Observable {
	
	//maximal current download for inspection
	private int MAX_CONCURRENT_DOWNLOAD = 3; 
//	private int MAX_BATCH_REQUEST_COUNT = 5;
	
	public static class RecordInspectionItems {
		public String recordId;
		public int downloadFlag;  // downloaded, partial data, failed, not download
		public List<RecordInspectionModel> listInspection; 
		public List<RecordInspectionModel> listFailedInspection;
		public List<RecordInspectionModel> listScheduledInspection;
		public List<RecordInspectionModel> listCompletedInspection; //complete inspection, include passed and failed inspection.
		public List<RecordInspectionModel> listApprovedInspection;
	}
	
	private  Vector<String> waitingDownloadQueue = new Vector<String>();
	private  Vector<String> downloadingQueue = new Vector<String>();
	
	//the map to store <Record ID, inspection items>
	public Map<String, RecordInspectionItems> downloadedInspections = 
			new ConcurrentHashMap<String, RecordInspectionItems>(); //make public for unit test
	private RecordAction action = new RecordAction();
	
	InspectionLoader() {}
	
	public void clearAll() {
		waitingDownloadQueue.clear();
		downloadingQueue.clear();
		downloadedInspections.clear();
		setChanged();
		notifyObservers();
	}
	
	public RecordInspectionItems getInspectionByRecord(String recordId) {
		if(recordId==null) {
			return null;
		} else {
			return downloadedInspections.get(recordId);
		}
	}
	
	public void loadInspectionByRecord(String recordId, boolean isReloadFailed) {
		//download the inspection list, include the latest one.
		RecordInspectionItems item = getInspectionByRecord(recordId);
		if( item != null && (item.downloadFlag == AppConstants.FLAG_FULL_DOWNLOAED 
				|| (item.downloadFlag==AppConstants.FLAG_DOWNLOADED_FAILED && !isReloadFailed)) ){
			//download finish, don't download twice
			return;
		}  
		
		if(downloadingQueue.contains(recordId)) {
			//downloading , did nothing
		} else if(waitingDownloadQueue.contains(recordId)) {
			//re-order the priority, move new request to the first one
			waitingDownloadQueue.remove(recordId);
			waitingDownloadQueue.add(0, recordId);
			setChanged();
			notifyObservers();
		} else {
			waitingDownloadQueue.add(0, recordId);
			AMLogger.logInfo("Get insection- add to waiting queue: %s , count: %d - %d", recordId,
					waitingDownloadQueue.size(), downloadingQueue.size());
			loadMoreInpsection();
		}
	}
	
	public void notifyAllAttachedObservers(String recordId){
		setChanged();
		notifyObservers(recordId);
	}
	
	public void setAction(RecordAction action){
		this.action  = action;
	}
	
	public void removeInspection(String permitId, RecordInspectionModel inspection){
		if(Utils.isInspectionFailed(inspection)) {
			//remove failed inspection after schedule.
			AppInstance.getInpsectionLoader().removeFailedInspection(permitId, inspection.getId());
		} else {
			//remove schedule inspection after reschedule
			AppInstance.getInpsectionLoader().removeScheduledInspection(permitId, inspection.getId());
		}
	}

	
	private void removeScheduledInspection(String permitId, Long inspectionId){
		if(inspectionId==null || getInspectionByRecord(permitId)==null || getInspectionByRecord(permitId).listScheduledInspection==null)
			return;
		for(int i=0; i<getInspectionByRecord(permitId).listScheduledInspection.size(); i++){
			if(inspectionId.equals(AppInstance.getInpsectionLoader().getInspectionByRecord(permitId).listScheduledInspection.get(i).getId())){
				getInspectionByRecord(permitId).listScheduledInspection.remove(i);
				notifyAllAttachedObservers(permitId);
				break;
			}
		}
	}
	
	private void removeFailedInspection(String permitId, Long inspectionId){
		if(inspectionId==null || getInspectionByRecord(permitId)==null || getInspectionByRecord(permitId).listFailedInspection==null)
			return;
		List<RecordInspectionModel> list = getInspectionByRecord(permitId).listFailedInspection;
		for(int i=0; i<list.size(); i++){
			if(inspectionId.equals(list.get(i).getId())){
				list.remove(i);
				notifyAllAttachedObservers(permitId);
				break;
			}
		}
	}
	
	public void addScheduledInspection(String permitId, RecordInspectionModel recordInspection){
		if(getInspectionByRecord(permitId).listScheduledInspection==null)
			return;
		getInspectionByRecord(permitId).listScheduledInspection.add(recordInspection);
		notifyAllAttachedObservers(permitId);
	}
	
public void loadInspectionByRecords(List<RecordModel> records, boolean isReloadFailed) {
		int countAdded = 0;
		for(RecordModel record: records) {
			String recordId = record.getId();
			RecordInspectionItems item = getInspectionByRecord(recordId);

			if(item != null && (item.downloadFlag == AppConstants.FLAG_FULL_DOWNLOAED
					|| (item.downloadFlag==AppConstants.FLAG_DOWNLOADED_FAILED && !isReloadFailed
					))){
				//download finish, don't download twice
				continue;
			} 
			//download the inspection list, include the latest one.
			if(downloadingQueue.contains(recordId)) {
				//downloading , did nothing
			} else if(waitingDownloadQueue.contains(recordId)) {
				//re-order the priority, move new request to the first one
				waitingDownloadQueue.remove(recordId);
				waitingDownloadQueue.add(0, recordId);
			} else {
				AMLogger.logInfo("Get insection- add to waiting queue: %s , count: %d - %d", recordId,
						waitingDownloadQueue.size(), downloadingQueue.size());
				countAdded++;
				waitingDownloadQueue.add(0, recordId);
			}
		}
		
		if(countAdded > 0) {
			loadMoreInpsection();
		} else {
			setChanged();
			notifyObservers();
		}
	}
	
	private void loadMoreInpsection() {
		if(downloadingQueue.size() >= MAX_CONCURRENT_DOWNLOAD) {
			return;
		} else {
			
			executeDownloadTask();
		}
	}
	
	private void saveInpsections(String recordId, int offset, AMDataIncrementalResponse<RecordInspectionModel> result) {
		RecordInspectionItems item = downloadedInspections.get(recordId);
		if(item==null) {
			item = new RecordInspectionItems();
			item.recordId = recordId;
			item.listInspection = new ArrayList<RecordInspectionModel>();
			item.listFailedInspection = new ArrayList<RecordInspectionModel>();
			item.listScheduledInspection = new ArrayList<RecordInspectionModel>();
			item.listCompletedInspection = new ArrayList<RecordInspectionModel>();
			item.listApprovedInspection = new ArrayList<RecordInspectionModel>();
			downloadedInspections.put(recordId, item);
		}
		if(result != null) {
			//download successfully
			AMLogger.logInfo("Get inspection for record: %s, count: %d, offset: %d", recordId, result.getResult().size(), offset);
			while (item.listInspection.size() > offset ) {
				// remove the old item. (it should not happen, just in case.
				item.listInspection.remove(item.listInspection.size() - 1);
			}
			item.downloadFlag = result.hasMoreItems() ? AppConstants.FLAG_PARTIAL_DOWNLOAED: AppConstants.FLAG_FULL_DOWNLOAED;
			item.listInspection.addAll(result.getResult());
			
			for(RecordInspectionModel inspection: result.getResult()) {
				AMLogger.logInfo(" inspection (category - id - status - type): (%s - %d - %s - %s - %s)", 
						inspection.getCategory(), inspection.getId(),
						inspection.getStatus_value(), inspection.getResultType(), inspection.getRecordId_id());
				
				int status = Utils.checkInspectionStatus(inspection);
				switch(status) {
				case AppConstants.INSPECTION_STATUS_PASSED:
					item.listCompletedInspection.add(inspection);
					item.listApprovedInspection.add(inspection);
					break;
				case AppConstants.INSPECTION_STATUS_SCHEDULED:
					item.listScheduledInspection.add(inspection);
					break;
				case AppConstants.INSPECTION_STATUS_FAILED:
					item.listFailedInspection.add(inspection);
					item.listCompletedInspection.add(inspection);
					break;
				case AppConstants.INSPECTION_STATUS_CANCELED:
					break;
				case AppConstants.INSPECTION_STATUS_UNKNOWN:
					break;
				}
			}
			
		} else {
			//download fails, also need to save the result, otherwise the app will keep send downlaod request 
			AMLogger.logInfo("Mark inspection request failed: %s", recordId);
			item.downloadFlag = AppConstants.FLAG_DOWNLOADED_FAILED;
		}
		
	}
	
	private class RequestDelegate extends AMAsyncEntityListActionDelegate<RecordInspectionModel> {
		private String recordId;
		private int offset;
		private int limit;
		RequestDelegate(String recordId) {
			this.recordId = recordId;
		}
		
		@Override
		public void onCompleted(AMDataIncrementalResponse<RecordInspectionModel> response) {
			AMLogger.logInfo("inspection request successful: %s, offset: %d, limit: %d" , recordId, offset, limit);
			saveInpsections(recordId, offset, response);	
			//remove from downloading queue
			downloadingQueue.remove(recordId);
			if(response.hasMoreItems()) { 
				//if has more item, add the record into waitingDownloadQueue to continue download
				waitingDownloadQueue.add(recordId);
			}
			setChanged();
			notifyObservers(recordId);
			loadMoreInpsection();
		}

		@Override
		public void onFailure(Throwable error) {
			AMLogger.logInfo("inspection request failed: %s, offset: %d, limit: %d" , recordId, offset, limit);		
			saveInpsections(recordId, offset, null);	
			//remove from downloading queue
			downloadingQueue.remove(recordId);
			setChanged();
			notifyObservers(recordId);
			loadMoreInpsection();
			if(error instanceof AMException) {
				AMException exception = (AMException) error;
				AMLogger.logInfo("inspection request status: %d", exception.getStatus());
				if(exception.getStatus() == AMError.ERROR_CODE_Unauthorized ) {
					RefreshTokenActivity.startRefreshTokenActivity();
				}
			}
		}
	} 
	
	private void executeDownloadTask() {
		if(waitingDownloadQueue.size()==0) {
			return;
		}
		AMLogger.logInfo("executeDownloadTask start");
		AMStrategy strategy = new  AMStrategy(AMAccessStrategy.Http);
		AMBatchSession session = null;//AccelaMobile.batchBegin();
		//int count = 0;
		final List<String> downloadingList = new ArrayList<String>();
		String currentAgency = null, currentEnvironment = null;
//		for(int i=0; i< waitingDownloadQueue.size(); i++) {
			if(waitingDownloadQueue.size()==0)
				return;
			String recordId = waitingDownloadQueue.get(0);
			if(currentAgency==null && AppInstance.getProjectsLoader().getRecordById(recordId)!=null){
				currentAgency = AppInstance.getProjectsLoader().getRecordById(recordId).getResource_agency();
				currentEnvironment = AppInstance.getProjectsLoader().getRecordById(recordId).getResource_environment();
			}
//			if(Utils.isSameAgency(recordId, currentAgency)){
				RequestDelegate requestDelegate = new RequestDelegate(recordId);
				int offset = 0;
				int limit = 20;
				RecordInspectionItems item = downloadedInspections.get(recordId);
				if(item != null) {
					//The inspection in this record has been downloaded partially, set the offset to download remains.
					offset = item.listInspection.size();
				}
				AMLogger.logInfo("Batch request inspection for record: %s ", recordId);
				requestDelegate.offset = offset;
				requestDelegate.limit = limit;
				action.getRecordInspectionsAsync(session, requestDelegate, currentAgency, currentEnvironment, strategy, recordId, offset, limit);
				//count++;
				waitingDownloadQueue.remove(0);
				downloadingQueue.add(recordId);
				downloadingList.add(recordId);
//			}
//			if(count > this.MAX_BATCH_REQUEST_COUNT) {
//				break;
//			}
//		}
//		Map<String, String> customParam = new HashMap<String, String>();
//		customParam.put(AccelaMobile.ENVIRONMENT_NAME, currentEnvironment);
//		customParam.put(AccelaMobile.AGENCY_NAME, currentAgency);
//		AccelaMobile.batchCommit(session, customParam,
//				new AMBatchRequestDelegate() {
//					@Override
//					public void onSuccessful() {
//						AMLogger.logInfo("batchCommitd successful");
//						loadMoreInpsection();
//					}
//
//					@Override
//					public void onFailed(AMError paramAMError) {
//						//If batch request is failed, need to remove the record id from downloading queue.
//						for(String recordId: downloadingList) {
//							downloadingQueue.remove(recordId);
//						}
//						AMLogger.logInfo("batchCommitd error:" + paramAMError.toString());
//						loadMoreInpsection();
//					}
//				});				//service.getAsync(new AMBatchSession(), path, delegate);	
		
	}	
}
