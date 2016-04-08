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
 *   Created by jzhong on 2/5/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */


package com.accela.contractorcentral.service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.AppContext;
import com.accela.contractorcentral.activity.RefreshTokenActivity;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.model.ProjectModel.ProjectInspection;
import com.accela.contractorcentral.service.GeocoderService.GeocoderBatchDelegeate;
import com.accela.contractorcentral.service.InspectionLoader.RecordInspectionItems;
import com.accela.contractorcentral.utils.Utils;
import com.accela.framework.AMException;
import com.accela.framework.model.AddressModel;
import com.accela.framework.persistence.AMAsyncEntityListActionDelegate;
import com.accela.framework.persistence.AMDataIncrementalResponse;
import com.accela.framework.persistence.AMStrategy;
import com.accela.framework.persistence.AMStrategy.AMAccessStrategy;
import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.accela.mobile.util.APOHelper;
import com.accela.record.action.RecordAction;
import com.accela.record.model.RecordInspectionModel;
import com.accela.record.model.RecordModel;

public class ProjectsLoader extends Observable implements Observer{
	RecordAction action = new RecordAction();

	ProjectsLoader() {
		
	}
	
	/**************************************************************************************
	 *  Load all projects and get project location, nearby project, recent inspection list
	 **************************************************************************************
	 */

	private boolean hasMoreItems;
	public List<ProjectModel> projectsList = new ArrayList<ProjectModel>();  //public for unit test
//	private Map<String, List<RecordModel>> recordMap = new HashMap<String, List<RecordModel>>();
	private int downloadStatus = AppConstants.DOWNLOAD_IDLE; // DOWNLOAD_RUNNING or DOWNLOAD_IDLE
	private int downloadFlag = AppConstants.FLAG_NOT_DOWNLOADED; //
	private int maxRecordsLimit = 300;
	private int limitPerDownload = 25;
	private int totalRecordDownloaded = 0;
	private List<RecordInspectionModel> recentInspectionList = new ArrayList<RecordInspectionModel> ();
	private List<RecordInspectionModel> scheduledInspectionList = new ArrayList<RecordInspectionModel>();
	private List<ProjectModel> nearByProjectList = new ArrayList<ProjectModel>();
	private RecordInspectionModel nextInspection = null;
//	private Iterator<Entry<String, List<RecordModel>>> iterator;
	
	/**
	 * Get the projects list. it maybe still downloading. Observe the update PROJECT_LIST_CHANGE 
	 * @return
	 */ 
	public List<ProjectModel> getProjects() {
		return projectsList;
	}
	
	/**
	 * Get the recent completed inspection list. it maybe still downloading. 
	 * Observe the update PROJECT_LIST_RECENT_INSPECTION_CHANGE 
	 * @return
	 */
	public List<RecordInspectionModel> getRecentOneMonthInpsections() {
		recentInspectionList = Utils.loadLastMonthInspectionList(recentInspectionList);
		return recentInspectionList;
	}  
	
	public void setRecentInpsections(List<RecordInspectionModel> recentInspectionList) {
		this.recentInspectionList = recentInspectionList;
	}  
	
	/**
	 * Get the near by project. it maybe still downloading. 
	 * Observe the update PROJECT_NEARBY_PROJECT_CHANGE 
	 * @return
	 */
	
	public List<ProjectModel> getNearByProject() {
		return nearByProjectList;
	}
	
	/**
	 * Get the schedule inspection list. it maybe still downloading. 
	 * Observe the update PROJECT_LIST_SCHEDULED_INSPECTION_CHANGE 
	 * @return
	 */
	public List<RecordInspectionModel> getScheduleInpsections() {
		return scheduledInspectionList;
	}
	
	public void setScheduleInspections(List<RecordInspectionModel> scheduledInspections) {
		this.scheduledInspectionList = scheduledInspections;
	}
	/**
	 * Get next inpsection, should be today or later than today
	 * @return
	 */
	public RecordInspectionModel getNextInspection() {
		return nextInspection;
		
	}
	
	
	/**
	 * Get the inspections history (the inspections with same record id and inspection type id, are for the same permit (record)
	 * @param inspection
	 * @return the list of inspection with same type for the same permit (not include self).
	 */
	public List<RecordInspectionModel> getInspectionHistory(RecordInspectionModel inspection) {
		List<RecordInspectionModel> list = new ArrayList<RecordInspectionModel>();
		AMLogger.logInfo("getInspectionHistory: recordId: %s,  typeId: %d, Id: %d", inspection.getRecordId_id(), inspection.getType().getId(), inspection.getId());
		
		for(RecordInspectionModel model: recentInspectionList) {
			AMLogger.logInfo("recordId: %s,  typeId: %d, Id: %d", model.getRecordId_id(), model.getType().getId(), model.getId());
			if(model.getRecordId_id().compareTo(inspection.getRecordId_id())==0) {
				AMLogger.logInfo("recordId is same");
			} else {
				continue;
			}
			if(model.getType().getId().longValue() == inspection.getType().getId().longValue()) {
				AMLogger.logInfo("type id is same");
			} else {
				AMLogger.logInfo("type id: %d, %d is different", model.getType().getId(), inspection.getType().getId());
				continue;
			}
					
			if(model.getId().longValue() != inspection.getId().longValue() //if same inspection, don't add it
					) {
				AMLogger.logInfo("add to list");
				list.add(model);
			}
		}
		AMLogger.logInfo("Inspetions includes: %d items", list.size());
		return list;
	}
	
	public void setAction(RecordAction action){
		this.action = action;
	}
	
	
	/**
	 * Load all projects
	 * @param forceRefresh - clear all old projects and download it again 
	 */
	public void loadAllProjects(boolean forceRefresh) {
		
		if(isDownloading()) {
			return;
		}
		
		if(forceRefresh) {
			if(AppInstance.getInpsectionLoader()!=null) {
				AppInstance.getInpsectionLoader().clearAll();
			}
			resetData();
		}
		if(downloadFlag == AppConstants.FLAG_FULL_DOWNLOAED) {
			//load finish and , don't load it again
			return;
		}
		loadMoreProjects();
	}
	
	public void clearAll() {
		//need to consider the case: call clearAll() while projects still downloading.
		resetData();
	}
	
	public boolean isDownloading() {
		return downloadStatus == AppConstants.DOWNLOAD_RUNING;
	}
	
	public ProjectModel getProjectById(String projectId) {
		if(projectId==null) {
			return null;
		}
		for(ProjectModel model: projectsList) {
			if(projectId.equalsIgnoreCase(model.getProjectId())) {
				return model;
			}
		}
		return null;
	}
	
	public ProjectModel getParentProject(String recordId) {
		if(recordId==null) {
			return null;
		}
		for(ProjectModel model: projectsList) {
			if(model.isIncludeRecord(recordId)) {
				return model;
			}
		}
		return null;
	}
	
	public RecordModel getRecordById(String recordId) {
		if(recordId==null) {
			return null;
		}
		for(ProjectModel model: projectsList) {
			RecordModel record = model.getRecordById(recordId);
			if(record!=null){
				return record;
			}
		}
		return null;
	}
	
	public boolean isAllInspectionsDownloaded() {
		if(isDownloading()) {
			//if projects list still on downloading
			return false;
		}
		//only all inspections downloaded complete or failed
		for(ProjectModel project: projectsList) {
			if(project.getInspectionDownloadFlag() == AppConstants.FLAG_NOT_DOWNLOADED
					|| project.getInspectionDownloadFlag() == AppConstants.FLAG_PARTIAL_DOWNLOAED) {			
				//if downloading is not started or download partial. 
				return false;
			}
		}
		return true;
	}
	
	private boolean isActiveRecords(RecordModel model){
		AppSettingsLoader loader = AppInstance.getAppSettingsLoader();
		if(model.getStatusType()==null || loader.getActiveRecordStatus()==null)
			return true;
		if(loader.getActiveRecordStatus()!=null){
			for(String status : loader.getActiveRecordStatus()){
				if(status.equalsIgnoreCase(model.getStatusType())){
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isValidAddress(RecordModel model){ //to make sync with ios to make sure records need at least have address city
		if(model.getAddresses()!=null && model.getAddresses().size()>0){
			AddressModel primaryAddress = Utils.getAddress(model);
			String address = null;
			if(primaryAddress!=null)
				address = APOHelper.formatAddress(primaryAddress);
			else
				address = APOHelper.formatAddress(model.getAddresses().get(0));
			if(address!=null && address.length()>0)
				return true;
		}
		return false;
	}
	
	private AMAsyncEntityListActionDelegate<RecordModel> actionDelegate = new AMAsyncEntityListActionDelegate<RecordModel>() {
		@Override
		public void onCompleted(AMDataIncrementalResponse<RecordModel> response) {
			for(RecordModel model: response.getResult()) {
				if(isActiveRecords(model) && isValidAddress(model)){
					addNewRecordToProjectList(projectsList, model);
				}
			}
			totalRecordDownloaded += response.getResult().size();
			hasMoreItems = response.hasMoreItems();
			AMLogger.logInfo("--Download project list onCompleted()");
			if(hasMoreItems && totalRecordDownloaded <  maxRecordsLimit) {
				//if more items, continues load more
				setChanged();
				notifyObservers(AppConstants.PROJECT_LIST_LOAD_PROGRESS);
				loadMoreProjects();
			} else {
				//no more items, load finish
				hasMoreItems = false;
				downloadStatus = AppConstants.DOWNLOAD_IDLE;
				downloadFlag = AppConstants.FLAG_FULL_DOWNLOAED;
				onLoadAllProjectComplete(true);
			}
		}
 
		@Override
		public void onFailure(Throwable error) {
			downloadStatus = AppConstants.DOWNLOAD_IDLE;
			downloadFlag = AppConstants.FLAG_DOWNLOADED_FAILED;
			hasMoreItems = false;
			AMLogger.logInfo("--Download project list onFailure()");
			onLoadAllProjectComplete(false);
			if(error instanceof AMException) {
				AMException exception = (AMException) error;
				if(exception.getStatus() == AMError.ERROR_CODE_Unauthorized ) {
					RefreshTokenActivity.startRefreshTokenActivity();
				}
			}
		}
	};
	
	private void onLoadAllProjectComplete(boolean successful) {
		//notify observer
		setChanged();
		notifyObservers(AppConstants.PROJECT_LIST_CHANGE);
		//reverse geo location for projects
		reverseGeoLocation();
		//start to load all inspections
		loadAllInspections();
	}
	
	private void loadMoreProjects() {
		
		AMLogger.logInfo("--Download more records ---");
		int offset = totalRecordDownloaded;
		AMStrategy strategy = new AMStrategy(AMAccessStrategy.Http);
		action.getMyRecordAsync(null, actionDelegate, strategy, AppContext.isMultipleAgencies, null,  null, null, null, null, null, null, null, null, null,
				null, null, null, null, null, null, limitPerDownload, offset);
		
	}
	 
	public void addNewRecordToProjectList(List<ProjectModel> projectList, RecordModel record) { //public for unit test
		AddressModel primaryAddress = Utils.getAddress(record);
		if(primaryAddress==null) {
			AMLogger.logError("ProjectLoader: this record has no address: id-" + record.getId());
		}
		 for(ProjectModel project: projectList) {
			 if(Utils.isSameAddress(project.getAddress(), primaryAddress)) {
				 //AMLogger.logInfo("address is same");
				 if(!record.getId().equals(project.getProjectId()))
					 project.addRecord(record);
				 return;
			 } else {
				 //AMLogger.logInfo("address is different");
			 }
		 }
		 ProjectModel project = new ProjectModel();
		 project.setAddress(primaryAddress);
		 
		 project.setProjectId(record.getId()); // use the first record ID. just for internal use.
		 project.addRecord(record);
		 projectList.add(project);
	}
	
	private void resetData(){
		hasMoreItems = false;
		nextInspection = null;
		if(projectsList.size() > 0) {
			projectsList.clear();
			setChanged();
			notifyObservers(AppConstants.PROJECT_LIST_CHANGE);
		} 
		
		if(nearByProjectList.size()>0) {
			nearByProjectList.clear();
			setChanged();
			notifyObservers(AppConstants.PROJECT_NEARBY_PROJECT_CHANGE);
		} 
		
		if(recentInspectionList.size()>0) {
			recentInspectionList.clear();
			setChanged();
			notifyObservers(AppConstants.PROJECT_LIST_RECENT_INSPECTION_CHANGE);
		}
		
		if(scheduledInspectionList.size()>0) {
			scheduledInspectionList.clear();
			setChanged();
			notifyObservers(AppConstants.PROJECT_LIST_SCHEDULED_INSPECTION_CHANGE);
		} 
		
		downloadFlag = AppConstants.FLAG_NOT_DOWNLOADED;
		downloadStatus = AppConstants.DOWNLOAD_IDLE;
		totalRecordDownloaded = 0;
	}

	/*********************************************
	 *  Request the current location, 
	 *  Reverse project address to location and update project distance
	 *********************************************
	 */
	protected LocationManager locationManager;
	protected Location location;
	LocationListener locationListener = new LocationListener() {
		@Override
		public void onStatusChanged(String provider, int status,
				Bundle extras) {
			
		}

		@Override
		public void onLocationChanged(Location newLocation) {
			locationManager = null;
			//locationManager.removeUpdates(locationListener);
			if(newLocation == null) {
				return;
			}
			if(!isDownloading()) {
				location = newLocation;
				
				for(ProjectModel project: projectsList) {
					PointF point = new PointF();
					point.x = (float) location.getLatitude();
					point.y = (float) location.getLongitude();
					project.updateDistance(point);
					
				}
				updateNearByProjectList();
				if(!isDownloading()) {
					setChanged();
					notifyObservers(AppConstants.PROJECT_NEARBY_PROJECT_CHANGE);
				}
				
				if(projectsList.size()>0) {
					setChanged();
					notifyObservers(AppConstants.PROJECT_LIST_CHANGE);
				}
			}
		}

		@Override
		public void onProviderEnabled(String provider) {
			
			
		}

		@Override
		public void onProviderDisabled(String provider) {
			
		}
	  };
	
	public Location getCurrentLocation() {
		return location;
	}
	  
	public boolean requestLocation(Context context) {
		//if locationManager is not null , it is requesting
		if(locationManager!= null) {
			return true;
		}
		
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(location == null) {
			location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		// Register the listener with the Location Manager to receive location updates
		boolean serviceDisable = true;
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,  locationListener, null);
			serviceDisable = false;
		} 
		if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
			serviceDisable = false;
		}
		if(serviceDisable)
		{
			locationManager = null;
			return false;
		} else {
			return true;
		}
	}
	
	private void reverseGeoLocation() {
		GeocoderService.getGeoLocatoinByProjectsAsync(projectsList, new GeocoderBatchDelegeate() {

			@Override
			public void onComplete() {
				//update nearby project 
				updateNearByProjectList();
				setChanged();
				notifyObservers(AppConstants.PROJECT_NEARBY_PROJECT_CHANGE);
				
			}

			@Override
			public void onProgress(boolean successful, ProjectModel project) {
				if(location!=null) {
					PointF point = new PointF();
					point.x = (float) location.getLatitude();
					point.y = (float) location.getLongitude();
					project.updateDistance(point);
					
				}
				setChanged();
				notifyObservers(AppConstants.PROJECT_LIST_CHANGE);
			}
		});
	}
	 
	private void updateNearByProjectList() {
		
		nearByProjectList.clear();
		for(ProjectModel project: projectsList) {
//			if(project.getDistance()>=0) {
				if(project.getAddress()!=null && project.getAddress().getCity()!=null)
					nearByProjectList.add(project);
//			}
		}
		sortPrjectListByDistance(nearByProjectList);
		
	}
	
//	private void groupRecordsByAgency(){
//		if(projectsList==null)
//			return;
//		for(ProjectModel project: projectsList) {
//			for(RecordModel record : project.getRecords()){
//				if(recordMap.containsKey(record.getResource_agency())){
//					recordMap.get(record.getResource_agency()).add(record);
//				}else{
//					List<RecordModel> list = new ArrayList<RecordModel>();
//					list.add(record);
//					recordMap.put(record.getResource_agency(), list);
//				}
//			}
//		}
//	}
	
	/******************************************************************
	 *  Load all inspections for all project, get all completed inspection
	 ******************************************************************
	 */
	public void loadAllInspections() {
		InspectionLoader loader = AppInstance.getInpsectionLoader();
		loader.addObserver(this);
		for(ProjectModel mdoel: projectsList) {
			loader.loadInspectionByRecords(mdoel.getRecords(), true);
		}
	}
//	private void loadAllInspections() {
//		inspectionLoader = InspectionLoader.getInstance();
//		inspectionLoader.addObserver(this);
//		inspectionLoader.setLoaderServiceStatus(this);
//		groupRecordsByAgency();
//		iterator = recordMap.entrySet().iterator();
//		loadNextBatchInspections();
//	}
	
//	private void loadNextBatchInspections(){
//	    if (iterator!=null && iterator.hasNext()) {
//	        Map.Entry<String, List<RecordModel>> entry = (Map.Entry<String, List<RecordModel>>)iterator.next();
//	        String agency = entry.getKey();
//	        List<RecordModel> recordList = entry.getValue();
//	        SDKManager.getInstance().getAccelaMobile().setAgency(agency);
//	        if(recordList!=null && recordList.size()>0)
//	        	SDKManager.getInstance().getAccelaMobile().setEnvironment(Utils.StringToEnvironment(recordList.get(0).getResource_environment()));
//	        inspectionLoader.loadInspectionByRecords(recordList);
//	    }
//	}
	
	private void cleanInspectionByPermitId(String recordId){
		List<RecordInspectionModel> schedulelistClone = new ArrayList<RecordInspectionModel>();
		for(int i=0; i<scheduledInspectionList.size(); i++){
			if(!this.scheduledInspectionList.get(i).getRecordId_id().equals(recordId)){
				schedulelistClone.add(scheduledInspectionList.get(i));
			}
		}
		scheduledInspectionList = new ArrayList<RecordInspectionModel>(schedulelistClone);
		
		List<RecordInspectionModel> recentlistClone = new ArrayList<RecordInspectionModel>();
		for(int i=0; i<recentInspectionList.size(); i++){
			if(!this.recentInspectionList.get(i).getRecordId_id().equals(recordId)){
				recentlistClone.add(recentInspectionList.get(i));
			}
		}
		recentInspectionList = new ArrayList<RecordInspectionModel>(recentlistClone);
	}

	@Override
	public void update(Observable observable, Object data) {
		if(observable instanceof InspectionLoader) {
			
			if(data != null && data instanceof String) {
				//if inspection list loaded completed, update the recently completed list
				String recordId = (String) data;
				InspectionLoader loader = AppInstance.getInpsectionLoader();
				RecordInspectionItems item = loader.getInspectionByRecord(recordId);
				if(item!=null && (item.downloadFlag == AppConstants.FLAG_FULL_DOWNLOAED || (item.downloadFlag == AppConstants.FLAG_DOWNLOADED_FAILED))) {
					//update recent completed inspection
					cleanInspectionByPermitId(recordId);
					recentInspectionList.addAll(item.listCompletedInspection);
					sortInspectionListByCompleteDate(recentInspectionList, true);
					//update schedule inspection
					scheduledInspectionList.addAll(item.listScheduledInspection);
					sortInspectionListByScheduleDate(scheduledInspectionList, false);
					//update next inspection 
					//get the newest next inspection
					ProjectModel project = this.getParentProject(recordId);
					if(project!=null)
						project.updateInspections(recordId);
					if(project!=null && project.getInspections(false).nextInspection != null) {	
						ProjectInspection projectInspection = project.getInspections(false);
						if(nextInspection==null || (nextInspection.getRecordId_id()!=null && nextInspection.getRecordId_id().equals(recordId)) || Utils.compareInspectionDate(projectInspection.nextInspection, nextInspection)<0){			
							nextInspection = projectInspection.nextInspection;
						}
						AMLogger.logInfo("Update next inspection for all projects" + (nextInspection != null? nextInspection.getId():
							"null"));
					} else if( nextInspection!=null && project.getInspections(false).nextInspection==null && nextInspection.getRecordId_id()!=null && nextInspection.getRecordId_id().equals(recordId) ){
						nextInspection = reCaculateNextInspection();
					}
					setChanged();
					notifyObservers(AppConstants.PROJECT_LIST_RECENT_INSPECTION_CHANGE);
					setChanged();
					notifyObservers(AppConstants.PROJECT_LIST_SCHEDULED_INSPECTION_CHANGE);
				}
			}
		}
	}
	
	private RecordInspectionModel reCaculateNextInspection(){
		RecordInspectionModel inspection = null; 
		for(ProjectModel project : projectsList){
			if(project.getInspections(false).nextInspection==null){
				continue;
			}else if(inspection==null){
				inspection = project.getInspections(false).nextInspection;
			}else if(Utils.compareInspectionDate(project.getInspections(false).nextInspection, inspection)<0){
				inspection = project.getInspections(false).nextInspection;
			}
		}
		return inspection;
	}
	
	
	private void sortInspectionListByScheduleDate(List<RecordInspectionModel> list, final boolean desc) {
		try{
			Collections.sort(list, new Comparator<RecordInspectionModel>() {

				@Override
				public int compare(RecordInspectionModel m1, RecordInspectionModel m2) {
//					long t1 =  (m1.getScheduleDate() !=null ? m1.getScheduleDate().getTime() : 0);
//					long t2 =  (m2.getScheduleDate() !=null ? m2.getScheduleDate().getTime() : 0);
//					
//					int result = t2 > t1? 1: -1;
//					//AMLogger.logInfo("t2 > t1:" + result);
//					return desc ? result: -result;
					return Utils.compareInspectionDate(m1, m2);
				}
			
			});
		}catch(RuntimeException e){
			AMLogger.logError(e.toString());
		}
	}
	
	private void sortInspectionListByCompleteDate(List<RecordInspectionModel> list, final boolean desc) {
		try{
			Collections.sort(list, new Comparator<RecordInspectionModel>() {

				@Override
				public int compare(RecordInspectionModel inspection1, RecordInspectionModel inspection2) {
					int result =  desc ? -Utils.compareInspectionCompleteDate(inspection1, inspection2) : Utils.compareInspectionCompleteDate(inspection1, inspection2);
					if(result==0 && inspection1.getType()!=null && inspection1.getType().getText()!=null && inspection2.getType()!=null && inspection2.getType().getText()!=null)
						return inspection1.getType().getText().compareTo(inspection2.getType().getText());
					return result;
				}
			
			});
		}catch(RuntimeException e){
			AMLogger.logError(e.toString());
		}
	}
	
	//sort the project by distance (asc order)
	private void sortPrjectListByDistance(List<ProjectModel> list) {
		Collections.sort(list, new Comparator<ProjectModel>() {

			@Override
			public int compare(ProjectModel p1, ProjectModel p2) {
				float d1 = p1.getDistance() >= 0 ? p1.getDistance():Float.MAX_VALUE;
				float d2 = p2.getDistance() >= 0 ? p2.getDistance():Float.MAX_VALUE;
				if(d1 == d2) {
					return 0;
				} else {
					return  (d1 - d2) > 0 ? 1:-1;
				}
			}
			
		});
	}

	
	//searchRecentInspection need an enhanced APIs in AA.
	/*
	public void searchRecentInspection(int limit) {
		limit = 100;
		AMStrategy strategy = new AMStrategy();
		strategy.setAmAccessStrategy(AMAccessStrategy.Http);
		strategy.setOffset(0);
		strategy.setLimit(limit); 
		 
		InspectionSearchRequest searchRequest = new InspectionSearchRequest();
		searchRequest.setCategory(AppConstants.INSPECTION_SEARCH_CATEGORY_COMPLETED);
		//searchRequest.setRecordTypes(recordTypes);
		
		InspectionAction action = new InspectionAction();
		action.searchInspectionsAsync(null, new  AMAsyncEntityListActionDelegate<InspectionModel>() {
			@Override
			public void onCompleted(
					AMDataIncrementalResponse<InspectionModel> response) {
				recentInspectionList.clear();
				recentInspectionList.addAll(response.getResult());
				for(InspectionModel model: recentInspectionList) {
					AMLogger.logInfo("category:%s - record type: %s, resultType: %s", model.getCategory(), 
							model.getRecordType_value(), model.getResultType());
					AMLogger.logInfo("address: %s", Utils.getAddressLine1(model.getAddress()));
				}
				setChanged();
				notifyObservers(AppConstants.PROJECT_LIST_RECENT_INSPECTION_CHANGE);
			}

			@Override
			public void onFailure(Throwable error) {
				setChanged();
				notifyObservers(AppConstants.PROJECT_LIST_RECENT_INSPECTION_CHANGE);
			}
		}, strategy, searchRequest, 0, limit);
		
	}
	*/
}
