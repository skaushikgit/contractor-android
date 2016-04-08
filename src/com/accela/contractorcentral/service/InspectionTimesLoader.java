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
 *   Created by jzhong on 2/26/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */


package com.accela.contractorcentral.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Vector;

import android.widget.Toast;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.AppContext;
import com.accela.contractorcentral.activity.RefreshTokenActivity;
import com.accela.contractorcentral.utils.Utils;
import com.accela.framework.AMException;
import com.accela.framework.persistence.AMAsyncEntityListActionDelegate;
import com.accela.framework.persistence.AMDataIncrementalResponse;
import com.accela.framework.persistence.AMStrategy;
import com.accela.framework.persistence.AMStrategy.AMAccessStrategy;
import com.accela.inspection.action.InspectionAction;
import com.accela.inspection.model.InspectionAvailableDatesModel;
import com.accela.inspection.model.InspectionAvailableTimeModel;
import com.accela.inspection.model.InspectionTimesModel;
import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;


public class InspectionTimesLoader extends Observable {
	
	//maximal current download for inspection
	private int MAX_CONCURRENT_DOWNLOAD = 10; 
	private int MAX_BATCH_REQUEST_COUNT = 5;
	
	public static class InspectionDateItem {
		public int itemLoad;
		public int year;
		public int month;
		public int downloadFlag;  // downloaded, partial data, failed, not download
		public List<InspectionAvailableDatesModel> listInspectionAvailableDates; 
	}
	
	private  Vector<Integer> waitingDownloadQueue = new Vector<Integer>();
	private  Vector<Integer> downloadingQueue = new Vector<Integer>();
	
	//the map to store available inspection dates by month <year * 100 + month, AvailableInspectionItems items>
	private Map<Integer, InspectionDateItem> inspectionDateItemSet = 
			new HashMap<Integer, InspectionDateItem>();
	private long inspectionId;
	private String recordId;
	private String inspectionTypeId;
	
	public InspectionTimesLoader(String recordId, long inspectionId, String inspectionTypeId) {
		this.recordId = recordId;
		this.inspectionId = inspectionId;
		this.inspectionTypeId = inspectionTypeId;
	}
	
	private Integer getIdByMonth(int year, int month) {
		return year * 100 + month;
	}
	
	public InspectionDateItem getInspectionDatesByMonth(int year, int month) {
		return inspectionDateItemSet.get(getIdByMonth(year, month));
	}
	
	public void loadInspectionDateByMonth(int year, int month) {
		//download the inspection list, include the latest one.
		AMLogger.logInfo("loadInspectionDateByMonth : %d / %d", year, month);
		Integer itemId = getIdByMonth(year, month);
		InspectionDateItem item = getInspectionDatesByMonth(year , month);
		if(item != null && item.downloadFlag == AppConstants.FLAG_FULL_DOWNLOAED) {
			//download finish, don't download twice
			AMLogger.logInfo("available time has been downloaed : %d / %d", year, month);
			setChanged();
			notifyObservers(itemId);
			return;
		}  
		
		if(downloadingQueue.contains(itemId)) {
			//downloading , did nothing
			AMLogger.logInfo("available time is downloading : %d / %d", year, month);
			setChanged();
			notifyObservers();
		} else if(waitingDownloadQueue.contains(itemId)) {
			//re-order the priority, move new request to the first one
			AMLogger.logInfo("available time is waiting to download : %d / %d", year, month);
			waitingDownloadQueue.remove(itemId);
			waitingDownloadQueue.add(0, itemId);
			setChanged();
			notifyObservers();
		} else {
			AMLogger.logInfo("add to waiting download queue: %d / %d", year, month);
			waitingDownloadQueue.add(itemId);
			Date[] dates = Utils.getStartEndDates(year, month);
			loadMoreInpsectionDate(dates[0], dates[1]);
		}
	}
	
	private void loadMoreInpsectionDate(Date startDate, Date endDate) {
		if(downloadingQueue.size() >= MAX_CONCURRENT_DOWNLOAD) {
			return;
		} else {
			executeDownloadTask(startDate, endDate);
		}
	}
	
//	
//	protected void addTestingAvaibleTime(List<InspectionAvailableDatesModel> listAvailableTime, int year, int month) {
//		
//		InspectionAvailableDatesModel model;
//		Calendar today = Calendar.getInstance();
//		today.setTimeInMillis(System.currentTimeMillis());
//		today.set(Calendar.HOUR_OF_DAY, 0);
//		today.set(Calendar.MINUTE, 0);
//		for(int i=0; i< 7; i++) {
//			Calendar cal = Calendar.getInstance();
//			cal.set(Calendar.YEAR, year);
//			cal.set(Calendar.MONTH, month);
//			cal.set(Calendar.DAY_OF_MONTH, month + i * 3 );
//			if(cal.getTimeInMillis() < today.getTimeInMillis()) {
//				continue;
//			}
//			model = new InspectionAvailableDatesModel();
//			model.setDate(cal.getTime());
//			listAvailableTime.add(model);
//			List<InspectionTimesModel> times = new ArrayList<InspectionTimesModel>();
//			for(int j=0; j< 3+i; j++) {
//				InspectionTimesModel modelTime = new InspectionTimesModel();
//				cal.add(Calendar.HOUR, j);
//				modelTime.setStartDate(cal.getTime().toString());
//				cal.add(Calendar.HOUR, 1);
//				cal.add(Calendar.MINUTE, i);
//				modelTime.setEndDate(cal.getTime().toString());
//				times.add(modelTime);
//			}
//			model.setTimes(times);
//		}
//		
//	}
	
	private void saveInpsectionDate(int year, int month, int offset, AMDataIncrementalResponse<InspectionAvailableTimeModel> result) {
		Integer itemId = getIdByMonth(year, month);
		InspectionDateItem item = inspectionDateItemSet.get(itemId);
		if(item==null) {
			item = new InspectionDateItem();
			item.year = year;
			item.month = month;
			item.listInspectionAvailableDates = new ArrayList<InspectionAvailableDatesModel>();
			inspectionDateItemSet.put(itemId, item);
		}
		if(result != null) {
			//download successfully
			
			for(InspectionAvailableTimeModel model: result.getResult()) {
				item.itemLoad++;
				if(model.getAvailableDates() != null) {
					item.listInspectionAvailableDates.addAll(model.getAvailableDates());
				}
			}
			
			item.downloadFlag = result.hasMoreItems() ? AppConstants.FLAG_PARTIAL_DOWNLOAED: AppConstants.FLAG_FULL_DOWNLOAED;
			
		} else {
			//download fails, also need to save the result, otherwise the app will keep send downlaod request 
			//item.downloadFlag = AppConstants.FLAG_DOWNLOADED_FAILED;
			
			item.downloadFlag = AppConstants.FLAG_FULL_DOWNLOAED;
		}
		
	}
	
	private class RequestDelegate extends AMAsyncEntityListActionDelegate<InspectionAvailableTimeModel> {
		private int year;
		private int month;
		private int offset;
		//private int limit;
		RequestDelegate(int year, int month) {
			this.year = year;
			this.month = month;
		}
		
		@Override
		public void onCompleted(AMDataIncrementalResponse<InspectionAvailableTimeModel> response) {
			saveInpsectionDate(year, month, offset, response);	
			Integer itemId = getIdByMonth(year, month);
			//remove from downloading queue
			downloadingQueue.remove(itemId);
			setChanged();
			notifyObservers(itemId);
		}

		@Override
		public void onFailure(Throwable error) {
//			saveInpsectionDate(year, month, offset, null);
			Integer itemId = getIdByMonth(year, month);
			//remove from downloading queue
			downloadingQueue.remove(itemId);
			setChanged();
			notifyObservers(itemId);
			Toast.makeText(AppContext.context, error.getMessage(), Toast.LENGTH_LONG).show();
			if(error instanceof AMException) {
				AMException exception = (AMException) error;
				if(exception.getStatus() == AMError.ERROR_CODE_Unauthorized ) {
					RefreshTokenActivity.startRefreshTokenActivity();
				}
			}
		}
	}
	
	
	
	private void executeDownloadTask(Date startDate, Date endDate) {
		if(waitingDownloadQueue.size()==0) {
			return;
		}
		
		int count = 0;
		for(int i=0; i< waitingDownloadQueue.size(); i++) {
			Integer itemId = waitingDownloadQueue.get(0);
			/*int offset = 0;
			//int limit = 0;
			InspectionDateItem item = inspectionDateItemSet.get(itemId);
			if(item != null) {
				//The inspection in this record has been downloaded partially, set the offset to download remains.
				offset = item.listInspectionAvailableDates.size();
			}*/
			
			InspectionAction action = new InspectionAction();
			AMStrategy strategy = new AMStrategy(AMAccessStrategy.Http);
			
			String agency = null;
			String environment = null;
			if(AppInstance.getProjectsLoader().getRecordById(recordId) != null) {
				agency = AppInstance.getProjectsLoader().getRecordById(recordId).getResource_agency();
				environment = AppInstance.getProjectsLoader().getRecordById(recordId).getResource_environment();
			}
			try {
				action.checkTimeAvailabilityAsync(null, new RequestDelegate(itemId / 100, itemId % 100), 
						strategy, agency, environment, null, startDate, endDate, null, inspectionId, inspectionTypeId, recordId);
			} catch (Exception e) {
				e.printStackTrace();
			}

			
			count++;
			waitingDownloadQueue.remove(0);
			downloadingQueue.add(itemId);
			if(count > this.MAX_BATCH_REQUEST_COUNT) {
				break;
			}
		}
			
		
	}	
}