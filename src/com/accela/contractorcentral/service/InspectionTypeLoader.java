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
 *   Created by jzhong on 2/2/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */

package com.accela.contractorcentral.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.activity.RefreshTokenActivity;
import com.accela.contractorcentral.utils.Utils;
import com.accela.framework.AMException;
import com.accela.framework.persistence.AMAsyncEntityListActionDelegate;
import com.accela.framework.persistence.AMDataIncrementalResponse;
import com.accela.framework.persistence.AMStrategy;
import com.accela.framework.persistence.AMStrategy.AMAccessStrategy;
import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.accela.record.action.RecordAction;
import com.accela.record.model.DailyInspectionTypeModel;
import com.accela.record.model.RecordInspectionTypeModel;

public class InspectionTypeLoader extends Observable {
	
	private List<DailyInspectionTypeModel> listInspectionType = new LinkedList<DailyInspectionTypeModel>();
	private int requesting;
	private String recordId;
	private int flagDownload = AppConstants.FLAG_NOT_DOWNLOADED;
	public InspectionTypeLoader(String recordId) {
		this.recordId = recordId;
	}
	
	public List<DailyInspectionTypeModel> getInspectionType() {
		return listInspectionType;
	}
	
	public boolean loadInpsectionType(boolean forceReload) {
		
		if(flagDownload == AppConstants.FLAG_FULL_DOWNLOAED && !forceReload) {
			// inspection type is loaded.
			AMLogger.logInfo("inspection type is loaded : %d ", requesting);
			return false;
		}
		AMLogger.logInfo("loadInpsectionType : %d ", requesting);
		if(requesting>0) {
			return false;
		}
		
		
		requesting = 1;
		AMLogger.logInfo("Load InpsectionType start");
		RecordAction action = new RecordAction();
		AMStrategy strategy = new AMStrategy(AMAccessStrategy.Http);
		RequestDelegate requestDelegate = new RequestDelegate(recordId);
		String agency = null;
		String environment = null;
		if(AppInstance.getProjectsLoader().getRecordById(recordId) != null) {
			agency = AppInstance.getProjectsLoader().getRecordById(recordId).getResource_agency();
			environment = AppInstance.getProjectsLoader().getRecordById(recordId).getResource_environment();
		}
		action.getInspectionTypesByRecordIdsAsync(null, requestDelegate, strategy, agency, environment, recordId);
		
		return true;
	} 
	
	public int getDownloadFlag() {
		return flagDownload;
	}
	
	private class RequestDelegate extends AMAsyncEntityListActionDelegate<RecordInspectionTypeModel> {
		String recordId;
		
		RequestDelegate(String recordId) {
			this.recordId = recordId;
		}
		
		@Override
		public void onCompleted(
				AMDataIncrementalResponse<RecordInspectionTypeModel> response) {
			AMLogger.logInfo("Inspection type load successfully:%d", response.getResult().size()  );
			listInspectionType.clear();
			for(RecordInspectionTypeModel model: response.getResult()) {
				if(model.getInspectionTypes() != null) {
					listInspectionType.addAll(model.getInspectionTypes());
				}
			}
			sortInspectionTypeList(listInspectionType);
			requesting = 0;
			flagDownload = AppConstants.FLAG_FULL_DOWNLOAED;
			setChanged();
			notifyObservers(recordId);
		}
 
		@Override
		public void onFailure(Throwable error) {
			AMLogger.logInfo("Inspection type load failed");
			listInspectionType.clear();
			requesting = 0;
			flagDownload = AppConstants.FLAG_DOWNLOADED_FAILED;
			setChanged();
			notifyObservers(recordId);
			if(error instanceof AMException) {
				AMException exception = (AMException) error;
				if(exception.getStatus() == AMError.ERROR_CODE_Unauthorized ) {
					RefreshTokenActivity.startRefreshTokenActivity();
				}
			}
		}
		
	}
	
	private void sortInspectionTypeList(List<DailyInspectionTypeModel> list) {
		Collections.sort(list, new Comparator<DailyInspectionTypeModel>() {

			@Override
			public int compare(DailyInspectionTypeModel m1,
					DailyInspectionTypeModel m2) {
				String info1[] = Utils.formatInspectionTypeInfo(m1, recordId);
				String info2[] = Utils.formatInspectionTypeInfo(m2, recordId);
				return info1[1].compareToIgnoreCase(info2[1]);
			}
			
		});
	}
}
