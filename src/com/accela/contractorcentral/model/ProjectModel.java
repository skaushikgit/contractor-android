package com.accela.contractorcentral.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.graphics.PointF;
import android.location.Location;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.FeeLoader;
import com.accela.contractorcentral.service.InspectionLoader;
import com.accela.contractorcentral.service.PaymentLoader;
import com.accela.contractorcentral.service.FeeLoader.RecordFeeItem;
import com.accela.contractorcentral.service.InspectionLoader.RecordInspectionItems;
import com.accela.contractorcentral.service.PaymentLoader.RecordPaymentItem;
import com.accela.contractorcentral.utils.Utils;
import com.accela.fee.model.FeeModel;
import com.accela.framework.AMBaseModel;
import com.accela.framework.model.AddressModel;
import com.accela.mobile.AMLogger;
import com.accela.mobile.util.APOHelper;
import com.accela.record.model.ContactModel;
import com.accela.record.model.PaymentModel;
import com.accela.record.model.RecordInspectionModel;
import com.accela.record.model.RecordModel;
import com.accela.sqlite.annotation.Table;

@SuppressWarnings("serial")
@Table(name = "ProjectModel")
public class ProjectModel extends AMBaseModel implements Serializable
{

	public static class ProjectInspection {
		public int downloadFlag = AppConstants.FLAG_NOT_DOWNLOADED;
		public RecordInspectionModel nextInspection;
		public List<RecordInspectionModel> listAllInspection = new ArrayList<RecordInspectionModel>(); 
		public List<RecordInspectionModel> listScheduledInspection = new ArrayList<RecordInspectionModel>(); 
		public List<RecordInspectionModel> listFailedInspection  = new ArrayList<RecordInspectionModel>();
	}
	
	public static class ProjectFee {
		public int downloadFeeFlag = AppConstants.FLAG_NOT_DOWNLOADED;
		public int downloadPaymentFlag = AppConstants.FLAG_NOT_DOWNLOADED;
		public FeeModel nextFee;
		public List<FeeModel> listAllFee = new ArrayList<FeeModel>();
		public List<PaymentModel> listAllPayment = new ArrayList<PaymentModel>();
	}
	
	private String projectId;

	private AddressModel address;

	private List<RecordModel> records = new ArrayList<RecordModel>();
	
	private PointF location = new PointF();
	
	public ProjectInspection projectInspection = new ProjectInspection(); //change public for unit test
	
	private ProjectFee projectFee = new ProjectFee();
	
	private float distance = -1f;

	public String getProjectId()
	{
		return this.projectId;
	}

	public void setProjectId(String projectId)
	{
		this.projectId = projectId;
	}
	
	public AddressModel getAddress()
	{
		return this.address;
	}

	public void setAddress(AddressModel address)
	{
		this.address = address;
	}
	
	public List<RecordModel> getRecords()
	{
		return this.records;
	}

	public boolean isIncludeRecord(String recordId) {
		return getRecordById(recordId) != null;
	}
	
	public RecordModel getRecordById(String recordId) {
		for(RecordModel record: records) {
			if(record.getId().compareTo(recordId) == 0) {
				return record;
			}
		}
		return null;
	}
	
	public void setGeoLocation(PointF location) {
		this.location.x = location.x;
		this.location.y = location.y;
	}
	
	public PointF getGeoLocation() {
		return location;
	}
	
	public boolean isGeoLocationAvailable() {
		return location.x != 0 && location.y !=0;
	}
	
	public void updateDistance(PointF point) {
		
		if(location.x != 0 && location.y !=0) {
			float[] results = new float[1];
			Location.distanceBetween(point.x, point.y, location.x, location.y, results);
			distance = results[0] * 0.000621371f;
		}
	}
	
	public float getDistance () {
		return distance;
	}
	
	public void setRecords(List<RecordModel> records)
	{
		if(records!=null) {
			this.records.clear();
			this.records.addAll(records);
		}
	}
	
	public void addRecord(RecordModel model) {
		if(!isIncludeRecord(model.getId())) {
			records.add(model);
			if(address==null) {
				address = APOHelper.getPrimaryAddress(model.getAddresses());
			}
			//if the address is still none. get the first address
			if(address==null && model.getAddresses()!=null && model.getAddresses().size()>0) {
				address = model.getAddresses().get(0);
			}
		}
	}
	
	public void removeRecord(RecordModel model) {
		records.remove(model);
	}
	
	public RecordModel getFirstRecord() {
		if(records.size()>0) {
			return records.get(0);
		} else {
			return null;
		}
	}

	public List<ContactModel> getContacts() {
		List<ContactModel> contactList = new ArrayList<ContactModel>();

		for(RecordModel record : records){
			List<ContactModel> contacts = record.getContacts();
			if(contacts == null) {
				continue;
			}
			for(ContactModel contact: contacts) {
				boolean added = false;
				for(ContactModel c: contactList) {
					if(Utils.isSameContact(contact, c)) {
						added = true;
						break;
					}
				}
				if(added) {
					continue;
				} else {
					contactList.add(contact);
				}
			}			
		}
		return contactList;
	}
	
	public int getInspectionDownloadFlag() {
		return projectInspection.downloadFlag;
	}
	
	public void updateInspections(String recordId){
		projectInspection.downloadFlag = AppConstants.FLAG_NOT_DOWNLOADED;
		getInspections(false);
	}
	
	//get inspections for project. it maybe in loading or failed or downloaded completed 
	public ProjectInspection getInspections(boolean isReloadFailed) {
		if( (projectInspection.downloadFlag == AppConstants.FLAG_FULL_DOWNLOAED) || (projectInspection.downloadFlag == AppConstants.FLAG_DOWNLOADED_FAILED
			&& !isReloadFailed) ) {
			return projectInspection;
		}
		
		//combine all record inspection into project model
		InspectionLoader loader = AppInstance.getInpsectionLoader();
		projectInspection.downloadFlag = AppConstants.FLAG_NOT_DOWNLOADED;
		boolean isAllDownloaded = true;
		projectInspection.listAllInspection.clear();
		projectInspection.listScheduledInspection.clear();
		projectInspection.listFailedInspection.clear();
		for(RecordModel record: records) {
			RecordInspectionItems item = loader.getInspectionByRecord(record.getId());
			if(item !=null) {
				projectInspection.listAllInspection.addAll(item.listInspection);
				projectInspection.listScheduledInspection.addAll(item.listScheduledInspection);
				projectInspection.listFailedInspection.addAll(item.listFailedInspection);
				projectInspection.downloadFlag = item.downloadFlag;
				if(projectInspection.downloadFlag == AppConstants.FLAG_DOWNLOADED_FAILED)
					break;
				AMLogger.logInfo("record inspection status: %s (%d)", record.getId(), item.downloadFlag);
			} 
			//check if all inspection downloaded
			if(item == null || item.downloadFlag != AppConstants.FLAG_FULL_DOWNLOAED) {
				isAllDownloaded = false;
			}  
			 
		}

		Date currentDate = Calendar.getInstance().getTime();
		//get the newest next inspection
		projectInspection.nextInspection = null;
		for(RecordInspectionModel inspection: projectInspection.listScheduledInspection) {
			Calendar scheduleCal = Calendar.getInstance();
			Calendar currrentCal = Calendar.getInstance();
			currrentCal.setTime(currentDate);
			scheduleCal.setTime(inspection.getScheduleDate());
			if(currrentCal.get(Calendar.DAY_OF_MONTH)==scheduleCal.get(Calendar.DAY_OF_MONTH) && currrentCal.get(Calendar.MONTH)==scheduleCal.get(Calendar.MONTH) 
					&& currrentCal.get(Calendar.YEAR)==scheduleCal.get(Calendar.YEAR)){
				; //check if on the same day
			}else if( (inspection.getScheduleDate()!=null && inspection.getScheduleDate().compareTo(currentDate)<=0) || (inspection.getScheduleDate()==null) )
				continue;
			if(projectInspection.nextInspection==null) {
				projectInspection.nextInspection = inspection;
			} else if(Utils.compareInspectionDate(inspection, projectInspection.nextInspection) < 0) {
				projectInspection.nextInspection = inspection;
			}
		}
		
		if(projectInspection.downloadFlag == AppConstants.FLAG_DOWNLOADED_FAILED){
			
			if(isReloadFailed) {
				loader.loadInspectionByRecords(records, true);
			}
			return projectInspection;
		}
		
		if(isAllDownloaded) {
			//all are downloaded
			projectInspection.downloadFlag = AppConstants.FLAG_FULL_DOWNLOAED;
		} else if(projectInspection.listAllInspection.size()>0) {
			//partial downloaded, request download again.
			projectInspection.downloadFlag = AppConstants.FLAG_PARTIAL_DOWNLOAED;
			loader.loadInspectionByRecords(records, false);
		} else {
			projectInspection.downloadFlag = AppConstants.FLAG_NOT_DOWNLOADED;
			loader.loadInspectionByRecords(records, false);
		}
		return projectInspection;
	}
	
	public ProjectFee getPayments() {
		if(projectFee.downloadPaymentFlag == AppConstants.FLAG_FULL_DOWNLOAED) {
			return projectFee;
		}
		//combine all record inspection into project model
		PaymentLoader loader = PaymentLoader.getInstance();
		projectFee.downloadPaymentFlag = AppConstants.FLAG_NOT_DOWNLOADED;
		boolean isAllDownloaded = true;
		projectFee.listAllPayment.clear();
		for(RecordModel record: records) {
			RecordPaymentItem item = loader.getPaymentItemByRecord(record.getId());
			if(item !=null) {
				projectFee.listAllPayment.addAll(item.listAllPayments);
			} 
			//check if all Payment downloaded
			if(item == null || item.downloadFlag != AppConstants.FLAG_FULL_DOWNLOAED) {
				isAllDownloaded = false;
			}  
		}
		if(isAllDownloaded) {
			//all are downloaded
			projectFee.downloadPaymentFlag = AppConstants.FLAG_FULL_DOWNLOAED;
		} else if(projectFee.listAllPayment.size()>0) {
			//partial downloaded, request download again.
			projectFee.downloadPaymentFlag = AppConstants.FLAG_PARTIAL_DOWNLOAED;
			loader.loadPaymentByRecords(records);
		} else {
			projectFee.downloadPaymentFlag = AppConstants.FLAG_NOT_DOWNLOADED;
			loader.loadPaymentByRecords(records);
		}
		return projectFee;
	}
		
	//get fees for project. it maybe in loading or failed or downloaded completed 
	public ProjectFee getFees() {
			if(projectFee.downloadFeeFlag == AppConstants.FLAG_FULL_DOWNLOAED) {
				return projectFee;
			}
			
			//combine all record inspection into project model
			FeeLoader loader = AppInstance.getFeeLoader();
			projectFee.downloadFeeFlag = AppConstants.FLAG_NOT_DOWNLOADED;
			boolean isAllDownloaded = true;
			projectFee.listAllFee.clear();
			for(RecordModel record: records) {
				RecordFeeItem item = loader.getFeeItemByRecord(record.getId());
				if(item !=null) {
					projectFee.listAllFee.addAll(item.listAllFee);
				} 
				//check if all Fee downloaded
				if(item == null || item.downloadFlag != AppConstants.FLAG_FULL_DOWNLOAED) {
					isAllDownloaded = false;
				}  
				
			}

		Date currentDate = Calendar.getInstance().getTime();
		//get the newest next inspection
		for(FeeModel fee: projectFee.listAllFee) {
			
			if( (fee.getExpireDate() != null && fee.getExpireDate().compareTo(currentDate)<0) || (fee.getExpireDate()==null) )
				continue;
			if(projectFee.nextFee==null) {
				projectFee.nextFee = fee;
			} else if(Utils.compareFeeExpireDate(fee, projectFee.nextFee) < 0) {
				projectFee.nextFee = fee;
			}
		}
		if(isAllDownloaded) {
			//all are downloaded
			projectFee.downloadFeeFlag = AppConstants.FLAG_FULL_DOWNLOAED;
		} else if(projectFee.listAllFee.size()>0) {
			//partial downloaded, request download again.
			projectFee.downloadFeeFlag = AppConstants.FLAG_PARTIAL_DOWNLOAED;
			loader.loadFeeByRecords(records);
		} else {
			projectFee.downloadFeeFlag = AppConstants.FLAG_NOT_DOWNLOADED;
			loader.loadFeeByRecords(records);
		}
		return projectFee;
	}
	
	//for debug
	public void printRecordId() {
		AMLogger.logInfo("****Project records:");
		for(RecordModel record: records) {
			AMLogger.logInfo("Record Id:" + record.getId());
		}
	}
	
}

