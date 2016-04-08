

package com.accela.contractorcentral.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.accela.contractorcentral.AppConstants;
import com.accela.framework.persistence.AMAsyncEntityListActionDelegate;
import com.accela.framework.persistence.AMDataIncrementalResponse;
import com.accela.framework.persistence.AMStrategy;
import com.accela.framework.persistence.AMStrategy.AMAccessStrategy;
import com.accela.mobile.AMBatchResponse.AMBatchRequestDelegate;
import com.accela.mobile.AMBatchSession;
import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.accela.mobile.AccelaMobile;
import com.accela.record.action.RecordAction;
import com.accela.record.model.PaymentModel;
import com.accela.record.model.RecordModel;



public class PaymentLoader extends Observable {
	
	//maximal current download for Fee
	private int MAX_CONCURRENT_DOWNLOAD = 10; 
	private int MAX_BATCH_REQUEST_COUNT = 5;
	
	public static class RecordPaymentItem {
		public String recordId;
		public int downloadFlag;  // downloaded, partial data, failed, not download
		public List<PaymentModel> listAllPayments;
	}
	
	private  Vector<String> waitingDownloadQueue = new Vector<String>();
	private  Vector<String> downloadingQueue = new Vector<String>();
	
	//the map to store <Record ID, inspection items>
	private Map<String, RecordPaymentItem> downloadedPayments = 
			new ConcurrentHashMap<String, RecordPaymentItem>();
	
	private static PaymentLoader instance;
	
	private PaymentLoader() {}
	
	public static PaymentLoader getInstance() {
		if (instance == null) {
			synchronized (FeeLoader.class) {
				// Double check
				if (instance == null) {
					instance = new PaymentLoader();
				}
			}
		}
		return instance;
	}
	
	public RecordPaymentItem getPaymentItemByRecord(String recordId) {
		if(recordId==null) {
			return null;
		} else {
			return downloadedPayments.get(recordId);
		}
	}
	
	public void loadFeeByRecord(String recordId) {
		//download the inspection list, include the latest one.
		RecordPaymentItem item = getPaymentItemByRecord(recordId);
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
			loadMorePayments();
		}
	}
	
	public void loadPaymentByRecords(List<RecordModel> records) {
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
			loadMorePayments();
		} else {
			setChanged();
			notifyObservers();
		}
	}
	
	private void loadMorePayments() {
		if(downloadingQueue.size() >= MAX_CONCURRENT_DOWNLOAD) {
			return;
		} else {
			executeDownloadTask();
		}
	}
	
	private void savePayments(String recordId, int offset, AMDataIncrementalResponse<PaymentModel> result) {
		RecordPaymentItem item = downloadedPayments.get(recordId);
		if(item==null) {
			item = new RecordPaymentItem();
			item.recordId = recordId;
			item.listAllPayments = new ArrayList<PaymentModel>();
//			item.listFeeUnpaid = new ArrayList<FeeModel>();
			downloadedPayments.put(recordId, item);
		}
		if(result != null) {
			//download successfully
			while (item.listAllPayments.size() > offset ) {
				// remove the old item. (it should not happen, just in case.
				item.listAllPayments.remove(item.listAllPayments.size() - 1);
			}
			item.downloadFlag = result.hasMoreItems() ? AppConstants.FLAG_PARTIAL_DOWNLOAED: AppConstants.FLAG_FULL_DOWNLOAED;
			item.listAllPayments.addAll(result.getResult());		
		} else {
			//download fails, also need to save the result, otherwise the app will keep send downlaod request 
			item.downloadFlag = AppConstants.FLAG_DOWNLOADED_FAILED;
		}
		
	}
	
	private class RequestDelegate extends AMAsyncEntityListActionDelegate<PaymentModel> {
		private String recordId;
		private int offset;
		//private int limit;
		RequestDelegate(String recordId) {
			this.recordId = recordId;
		}
		
		@Override
		public void onCompleted(AMDataIncrementalResponse<PaymentModel> response) {
			AMLogger.logInfo("payment request successful");
			savePayments(recordId, offset, response);	
			//remove from downloading queue
			downloadingQueue.remove(recordId);
			setChanged();
			notifyObservers(recordId);
		}

		@Override
		public void onFailure(Throwable error) {
			AMLogger.logInfo("payment request failed");
			savePayments(recordId, offset, null);	
			//remove from downloading queue
			downloadingQueue.remove(recordId);
			setChanged();
			notifyObservers(recordId);
		}
	}
	
	private void executeDownloadTask() {
		if(waitingDownloadQueue.size()==0) {
			return;
		}
		AMLogger.logInfo("executeDownloadTask start");
		RecordAction action = new RecordAction();
		AMStrategy strategy = new  AMStrategy(AMAccessStrategy.Http);
		AMBatchSession session = AccelaMobile.batchBegin();
		int count = 0;
		final List<String> downloadingList = new ArrayList<String>();
		for(int i=0; i< waitingDownloadQueue.size(); i++) {
			String recordId = waitingDownloadQueue.get(0);
			RequestDelegate requestDelegate = new RequestDelegate(recordId);
			/*int offset = 0;
			int limit = 20;
			RecordPaymentItem item = downloadedPayments.get(recordId);
			if(item != null) {
				//The inspection in this record has been downloaded partially, set the offset to download remains.
				offset = item.listAllPayments.size();
			}*/
			AMLogger.logInfo("Batch request payment for record: %s ", recordId);
			action.getPaymentsCapIDAsync(session, requestDelegate, strategy, recordId);
			count++;
			waitingDownloadQueue.remove(0);
			downloadingQueue.add(recordId);
			downloadingList.add(recordId);
			if(count > this.MAX_BATCH_REQUEST_COUNT) {
				break;
			}
		}
		Map<String, String> customParam = new HashMap<String, String>();
		customParam.put(AccelaMobile.ENVIRONMENT_NAME, null);
		customParam.put(AccelaMobile.AGENCY_NAME, null);
		AccelaMobile.batchCommit(session, customParam,
				new AMBatchRequestDelegate() {
					@Override
					public void onSuccessful() {
						AMLogger.logInfo("batchCommitd successful");
						loadMorePayments();
					}

					@Override
					public void onFailed(AMError paramAMError) {
						//If batch request is failed, need to remove the record id from downloading queue.
						for(String recordId: downloadingList) {
							downloadingQueue.remove(recordId);
						}
						AMLogger.logInfo("batchCommitd error:" + paramAMError.toString());
						loadMorePayments();
					}
				});				//service.getAsync(new AMBatchSession(), path, delegate);	
		
	}	
}
