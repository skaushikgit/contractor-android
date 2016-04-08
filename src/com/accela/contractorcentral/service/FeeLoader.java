/** 
  * Copyright 2014 Accela, Inc. 
  * 
  * You are hereby granted a non-exclusive, worldwide, royalty-free license to 
  * use, copy, modify, and distribute this software in source code or binary 
  * form for use in connection with the web services and APIs provided by 
  * Accela. 
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
  * DEALINGS IN THE SOFTWARE. 
  * 
  * 
  * 
  */

/*
 * 
 * 
 *   Created by jzhong on 3/09/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */

package com.accela.contractorcentral.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.activity.RefreshTokenActivity;
import com.accela.fee.action.FeeAction;
import com.accela.fee.model.FeeModel;
import com.accela.framework.AMException;
import com.accela.framework.persistence.AMAsyncEntityListActionDelegate;
import com.accela.framework.persistence.AMDataIncrementalResponse;
import com.accela.framework.persistence.AMStrategy;
import com.accela.framework.persistence.AMStrategy.AMAccessStrategy;
import com.accela.mobile.AMBatchSession;
import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.accela.record.model.RecordModel;



public class FeeLoader extends Observable {
	
	//maximal current download for Fee
	private int MAX_CONCURRENT_DOWNLOAD = 3; 
//	private int MAX_BATCH_REQUEST_COUNT = 5;
	
	public static class RecordFeeItem {
		public String recordId;
		public int downloadFlag;  // downloaded, partial data, failed, not download
		public List<FeeModel> listAllFee;
//		public List<FeeModel> listFeeUnpaid;
	}
	
	private  Vector<String> waitingDownloadQueue = new Vector<String>();
	private  Vector<String> downloadingQueue = new Vector<String>();
	
	//the map to store <Record ID, inspection items>
	private Map<String, RecordFeeItem> downloadedFees = 
			new ConcurrentHashMap<String, RecordFeeItem>();
	
	FeeLoader() {}
	
	public RecordFeeItem getFeeItemByRecord(String recordId) {
		if(recordId==null) {
			return null;
		} else {
			return downloadedFees.get(recordId);
		}
	}
	
	public void loadFeeByRecord(String recordId) {
		//download the inspection list, include the latest one.
		RecordFeeItem item = getFeeItemByRecord(recordId);
		if(item != null && item.downloadFlag == AppConstants.FLAG_FULL_DOWNLOAED) {
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
			AMLogger.logInfo("Get Fee- add to waiting queue: %s , count: %d - %d", recordId,
					waitingDownloadQueue.size(), downloadingQueue.size());
			loadMoreFee();
		}
	}
	
	public void loadFeeByRecords(List<RecordModel> records) {
		int countAdded = 0;
		for(RecordModel record: records) {
			String recordId = record.getId();
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
			loadMoreFee();
		} else {
			setChanged();
			notifyObservers();
		}
	}
	
	private void loadMoreFee() {
		if(downloadingQueue.size() >= MAX_CONCURRENT_DOWNLOAD) {
			return;
		} else {
			
			executeDownloadTask();
		}
	}
	
	private void saveFees(String recordId, int offset, AMDataIncrementalResponse<FeeModel> result) {
		RecordFeeItem item = downloadedFees.get(recordId);
		if(item==null) {
			item = new RecordFeeItem();
			item.recordId = recordId;
			item.listAllFee = new ArrayList<FeeModel>();
//			item.listFeeUnpaid = new ArrayList<FeeModel>();
			downloadedFees.put(recordId, item);
		}
		if(result != null) {
			//download successfully
			AMLogger.logInfo("Get fee for record: %s, count: %d, offset: %d", recordId, result.getResult().size(), offset);
			while (item.listAllFee.size() > offset ) {
				// remove the old item. (it should not happen, just in case.
				item.listAllFee.remove(item.listAllFee.size() - 1);
			}
			item.downloadFlag = result.hasMoreItems() ? AppConstants.FLAG_PARTIAL_DOWNLOAED: AppConstants.FLAG_FULL_DOWNLOAED;
			item.listAllFee.addAll(result.getResult());		
		} else {
			//download fails, also need to save the result, otherwise the app will keep send downlaod request 
			AMLogger.logInfo("Mark fee request failed: %s", recordId);
			item.downloadFlag = AppConstants.FLAG_DOWNLOADED_FAILED;
		}
		
	}
	
	private class RequestDelegate extends AMAsyncEntityListActionDelegate<FeeModel> {
		private String recordId;
		private int offset;
		//private int limit;
		RequestDelegate(String recordId) {
			this.recordId = recordId;
		}
		
		@Override
		public void onCompleted(AMDataIncrementalResponse<FeeModel> response) {
			AMLogger.logInfo("Fee request successful");
			saveFees(recordId, offset, response);	
			//remove from downloading queue
			downloadingQueue.remove(recordId);
			if(response.hasMoreItems()) {
				waitingDownloadQueue.add(recordId);
			}
			
			setChanged();
			notifyObservers(recordId);
			loadMoreFee();
		}

		@Override
		public void onFailure(Throwable error) {
			AMLogger.logInfo("Fee request failed");
			saveFees(recordId, offset, null);	
			//remove from downloading queue
			downloadingQueue.remove(recordId);
			setChanged();
			notifyObservers(recordId);
			loadMoreFee();
			if(error instanceof AMException) {
				AMException exception = (AMException) error;
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
		FeeAction action = new FeeAction();
		AMStrategy strategy = new  AMStrategy(AMAccessStrategy.Http);
		AMBatchSession session = null;//AccelaMobile.batchBegin();
		
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
			/*	int offset = 0;
				int limit = 20;
				RecordFeeItem item = downloadedFees.get(recordId);
				if(item != null) {
					//The inspection in this record has been downloaded partially, set the offset to download remains.
					offset = item.listAllFee.size();
				} */
				AMLogger.logInfo("Batch request fee for record: %s ", recordId);
				action.getFeeItemsByCapIDAsync(session, requestDelegate, strategy, currentAgency,  currentEnvironment, recordId, null);
				
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
//						loadMoreFee();
//					}
//
//					@Override
//					public void onFailed(AMError paramAMError) {
//						//If batch request is failed, need to remove the record id from downloading queue.
//						for(String recordId: downloadingList) {
//							downloadingQueue.remove(recordId);
//						}
//						AMLogger.logInfo("batchCommitd error:" + paramAMError.toString());
//						loadMoreFee();
//					}
//				});				//service.getAsync(new AMBatchSession(), path, delegate);	
		
	}	
}
