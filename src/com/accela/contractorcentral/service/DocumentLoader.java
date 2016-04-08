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
 *   Created by jzhong on 3/24/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */



package com.accela.contractorcentral.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Vector;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.LruCache;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.AppContext;
import com.accela.contractorcentral.utils.Utils;
import com.accela.document.action.InspDocumentAction;
import com.accela.document.action.RecordDocumentAction;
import com.accela.document.helper.AMDocumentLoader;
import com.accela.document.model.DocumentModel;
import com.accela.document.model.UploadInfoModel;
import com.accela.document.strategy.DownloadDocumentStrategy;
import com.accela.framework.UpdateItemResult;
import com.accela.framework.authorization.UserPreferences;
import com.accela.framework.persistence.AMAsyncEntityListActionDelegate;
import com.accela.framework.persistence.AMClientRequest;
import com.accela.framework.persistence.AMDataIncrementalResponse;
import com.accela.framework.persistence.AMStrategy;
import com.accela.framework.persistence.AMStrategy.AMAccessStrategy;
import com.accela.framework.persistence.request.AMPost;
import com.accela.framework.util.AMUtils;
import com.accela.mobile.AMLogger;
import com.accela.mobile.AccelaMobile;
import com.accela.mobile.util.Constants;
import com.accela.mobile.util.UIUtils;
import com.accela.record.model.RecordInspectionModel;
import com.accela.record.model.RecordModel;



public class DocumentLoader extends Observable {
	
	
	private final static int MAX_DOCUMENTS_CACHE = 32;
	
	//maximal current download for document
	private int MAX_CONCURRENT_DOWNLOAD = 3; 
	//private int MAX_BATCH_REQUEST_COUNT = 1;
	
	public static class DocumentItem {
		@Override
		public boolean equals(Object o) {
			if(!(o instanceof DocumentItem)) {
				return false;
			}
			DocumentItem item = (DocumentItem) o;
			return ownerId.equals(item.ownerId);
		}
		@Override
		public int hashCode() {
			return ownerId.hashCode();
		}
		public String ownerId;     //owner can be record, inspection
		public int downloadFlag;  // downloaded, partial data, failed, not download
		public List<DocumentModel> listDocument; 
		boolean isRecord;
		public String recordId;
		
	}
	
	private  Vector<DocumentItem> waitingDownloadQueue = new Vector<DocumentItem>();
	private  Vector<DocumentItem> downloadingQueue = new Vector<DocumentItem>();
	
	
	private LruCache<String, DocumentItem> documentCache;

	DocumentLoader() {
		documentCache = new LruCache<String, DocumentItem>(MAX_DOCUMENTS_CACHE);
	}
	
	public DocumentItem getDocumentByRecord(String recordId) {
		if(recordId==null) {
			return null;
		} else {
			DocumentItem item = documentCache.get(recordId);
			if(item==null) {
				item = new DocumentItem();
				item.ownerId = recordId;
				item.isRecord = true;
				item.listDocument = new ArrayList<DocumentModel>();
				item.recordId = recordId;
				documentCache.put(recordId, item);
			}
			return item;
		}
	}
	
	public DocumentItem getDocumentByInspection(String recordId, String inspectionId) {
		if(inspectionId==null) {
			return null;
		} else {
			DocumentItem item = documentCache.get(inspectionId);
			if(item==null) {
				item = new DocumentItem();
				item.ownerId = inspectionId;
				item.recordId = recordId;
				item.isRecord = false;
				item.listDocument = new ArrayList<DocumentModel>();
				documentCache.put(inspectionId, item);
			}
			return item;
		}
	}
	
	public void loadDocumentByRecord(String recordId) {
		//download the inspection list, include the latest one.
		DocumentItem item = getDocumentByRecord(recordId);
		loadDocumentItem(item);
	}
	
	public void loadDocumentByInspection(String recordId, String inspectionId) {
		//download the inspection list, include the latest one.
		DocumentItem item = getDocumentByInspection(recordId, inspectionId);
		loadDocumentItem(item);
	}
	
	private void loadDocumentItem(DocumentItem item) {
		if(item != null && item.downloadFlag == AppConstants.FLAG_FULL_DOWNLOAED) {
			//download finish, don't download twice
			return;
		}  
		
		if(downloadingQueue.contains(item)) {
			//downloading , did nothing
		} else if(waitingDownloadQueue.contains(item)) {
			//re-order the priority, move new request to the first one
			waitingDownloadQueue.remove(item);
			waitingDownloadQueue.add(0, item);
			setChanged();
			notifyObservers();
		} else {
			
			waitingDownloadQueue.add(0, item);
			AMLogger.logInfo("Get insection- add to waiting queue: %s , count: %d - %d", item.ownerId,
					waitingDownloadQueue.size(), downloadingQueue.size());
			loadMoreDocument();
		}
	}
	
	private void loadMoreDocument() {
		if(downloadingQueue.size() >= MAX_CONCURRENT_DOWNLOAD) {
			return;
		} else {
			
			executeDownloadTask();
		}
	}
	
	private void saveDocuments(DocumentItem item , int offset, AMDataIncrementalResponse<DocumentModel> result) {
		
		if(result != null) {
			//download successfully
			AMLogger.logInfo("Get document for Id: %s, count: %d, offset: %d", 
					item.ownerId, result.getResult().size(), offset);
			item.downloadFlag = AppConstants.FLAG_FULL_DOWNLOAED;
			item.listDocument.addAll(result.getResult());
			
			//set the recordId for each document manually
			for(DocumentModel documentModel : item.listDocument){
				documentModel.setRecordId(item.recordId);
			}
			
		} else {
			//download fails, also need to save the result, otherwise the app will keep send downloaded request 
			AMLogger.logInfo("Mark document request failed: %s", item.ownerId);
			item.downloadFlag = AppConstants.FLAG_DOWNLOADED_FAILED;
		}
		
	}
	
	private class RequestDelegate extends AMAsyncEntityListActionDelegate<DocumentModel> {
		private DocumentItem item;
		private int offset;
		RequestDelegate( DocumentItem item) {
			this.item = item;
		}
		
		@Override
		public void onCompleted(AMDataIncrementalResponse<DocumentModel> response) {
			AMLogger.logInfo("document request successful");
			saveDocuments(item, offset, response);	
			//remove from downloading queue
			downloadingQueue.remove(item);
			if(response.hasMoreItems()) {
				waitingDownloadQueue.add(item);
			}
			setChanged();
			notifyObservers(item.ownerId);
			
			loadMoreDocument();
		}

		@Override
		public void onFailure(Throwable error) {
			AMLogger.logInfo("document request failed");
			saveDocuments(item, offset, null);	
			//remove from downloading queue
			downloadingQueue.remove(item);
			setChanged();
			notifyObservers(item.ownerId);
			loadMoreDocument();
		}
	}
	
	private void executeDownloadTask() {
		if(waitingDownloadQueue.size()==0) {
			return;
		}
		AMLogger.logInfo("executeDownloadTask document start");
		AMStrategy strategy = new  AMStrategy(AMAccessStrategy.Http);
		//something wrong in batch request document, don't use it.
		//AMBatchSession session = AccelaMobile.batchBegin();
		InspDocumentAction actionInsp = new InspDocumentAction();
		RecordDocumentAction actionRecord = new RecordDocumentAction();
		
		String currentAgency = null, currentEnvironment = null;
		final List<DocumentItem> downloadingList = new ArrayList<DocumentItem>();
		for(int i=0; i< waitingDownloadQueue.size(); i++) {
			DocumentItem item = waitingDownloadQueue.get(0);
			if(currentAgency==null && AppInstance.getProjectsLoader().getRecordById(item.recordId)!=null){
				currentAgency = AppInstance.getProjectsLoader().getRecordById(item.recordId).getResource_agency();
				currentEnvironment = AppInstance.getProjectsLoader().getRecordById(item.recordId).getResource_environment();
			}
			if(Utils.isSameAgency(item.recordId, currentAgency)){
				RequestDelegate requestDelegate = new RequestDelegate(item);
				AMLogger.logInfo("Batch request document for record/inspection: %s ", item.ownerId);
				strategy = new DownloadDocumentStrategy(item.ownerId);
				strategy.setAmAccessStrategy(AMAccessStrategy.Http);
				
				if(item.isRecord) {
					actionRecord.getDocumentListByRecordIdAsync(null, requestDelegate, strategy, currentAgency, currentEnvironment, item.ownerId);
				} else {
					actionInsp.getInspectionDocumentListAsync(null, requestDelegate, strategy, currentAgency, currentEnvironment, item.ownerId);
				}

				waitingDownloadQueue.remove(0);
				downloadingQueue.add(item);
				downloadingList.add(item);
			}

		//	if(count > this.MAX_BATCH_REQUEST_COUNT) {
		//		break;
		//	}
		}
		
		/*
		AccelaMobile.batchCommit(session,
				new AMBatchRequestDelegate() {
					@Override
					public void onSuccessful() {
						AMLogger.logInfo("batchCommitd successful");
						loadMoreDocument();
					}

					@Override
					public void onFailed(AMError paramAMError) {
						//If batch request is failed, need to remove the record id from downloading queue.
						for(DocumentItem item: downloadingList) {
							downloadingQueue.remove(item);
						}
						AMLogger.logInfo("batchCommitd error:" + paramAMError.toString());
						loadMoreDocument();
					}
				});				//service.getAsync(new AMBatchSession(), path, delegate);	
		
		*/
		
	}	
	
	
	/*
	 * Upload document, just for testing
	 * 
	 */
	 
	public boolean uploadDocument(RecordInspectionModel inspection, String fileName) {
		// just for testing. 
		List<UploadInfoModel> uploadInfos = new ArrayList<UploadInfoModel>();
		
		File file = new File(fileName);
		SharedPreferences userPreferences = UserPreferences.getSharedPreferences(AppContext.mContext);
		
		// set the upload image size
		userPreferences.edit().putString(Constants.SETTINGS_UPLOAD_IMAGE_SIZE, "1280,960").commit();
		
		if (file.exists() && file.isFile()) {
			UploadInfoModel uploadInfo = new UploadInfoModel();
			String filePath = null;
			try {
				filePath = UIUtils.transformUploadImage(AppContext.mContext, file, userPreferences);
				uploadInfo.setFileName(filePath);
				uploadInfo.setType(AMUtils.getMimeType(file));
				uploadInfo.setDescription("test upload!");
				uploadInfos.add(uploadInfo);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}				

		try {
			uploadInspectionDocument(inspection.getId().toString(), "Contractor", "Android", "mdeveloper", "accela", uploadInfos);
		} catch (Exception e) {
			
			AMLogger.logInfo("upload error");
			e.printStackTrace();
		}
		
		AMLogger.logInfo("upload finish");
		//UpdateItemResult result1 = result.get(0);
		//AMLogger.logInfo(result1.toString());
	
		return true;
	}
	
	private List<UpdateItemResult> uploadInspectionDocument(String inspectionId, String group, String category, String userId, String password, 
			List<UploadInfoModel> uploadInfos) throws Exception
	{
		AMClientRequest action = new AMPost("/v4/inspections/{inspectionId}/documents");
		action.addUrlParam("inspectionId",inspectionId,true);
		action.addUrlParam("group",group,true);
		action.addUrlParam("category",category,true);
		action.addUrlParam("userId",userId,true);
		action.addUrlParam("password",password,true);
		return new AMDocumentLoader().upload(action, uploadInfos); 
	}
	  
	public boolean uploadDocument(RecordModel record, String fileName) {
		// just for testing. 
		final List<UploadInfoModel> uploadInfos = new ArrayList<UploadInfoModel>();
		
		File file = new File(fileName);
		SharedPreferences userPreferences = UserPreferences.getSharedPreferences(AppContext.mContext);
		
		// set the upload image size
		userPreferences.edit().putString(Constants.SETTINGS_UPLOAD_IMAGE_SIZE, "1280,960").commit();
		
		if (file.exists() && file.isFile()) {
			UploadInfoModel uploadInfo = new UploadInfoModel();
			String filePath = null;
			try {
				filePath = UIUtils.transformUploadImage(AppContext.mContext, file, userPreferences);
				uploadInfo.setFileName(filePath);
				uploadInfo.setType(AMUtils.getMimeType(file));
				uploadInfo.setDescription("test upload!");
				uploadInfos.add(uploadInfo);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}				
		 
		
		new AsyncTask<RecordModel, Void, Void> () {

			@Override
			protected void onPostExecute(Void result) {
				
			}

			@Override
			protected Void doInBackground(RecordModel... params) {
				RecordModel record = params[0];
				try {
					uploadRecordDocument(record.getId(), "Contractor", "Android", "mdeveloper", "accela", record.getResource_agency(),
							record.getResource_environment(),
							uploadInfos);
				} catch (Exception e) {
					
					AMLogger.logInfo("upload error");
					e.printStackTrace();
				}
				
				AMLogger.logInfo("upload finish");
				//UpdateItemResult result1 = result.get(0);
				//AMLogger.logInfo(result1.toString());
				super.onPostExecute(null);
				return null;
			}
			
			
			
		}.execute(record);
		
	
		return true;
	}
	
	private List<UpdateItemResult> uploadRecordDocument(String recordId, String group, String category, String userId, String password, 
			String agency, String environment,
			List<UploadInfoModel> uploadInfos) throws Exception
	{
		AMClientRequest action = new AMPost("/v4/records/{recordId}/documents");
		action.addUrlParam("recordId",recordId,true);
		action.addUrlParam("group",group,true);
		action.addUrlParam("category",category,true);
		action.addUrlParam("userId",userId,true);
		action.addUrlParam("password",password,true);
		if(agency!=null)
			action.addCustomParam(AccelaMobile.AGENCY_NAME, agency);
		if(environment!=null)
			action.addCustomParam(AccelaMobile.ENVIRONMENT_NAME, environment);
		return new AMDocumentLoader().upload(action, uploadInfos);
	}
	
}
