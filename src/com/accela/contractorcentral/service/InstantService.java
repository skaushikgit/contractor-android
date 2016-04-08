package com.accela.contractorcentral.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.accela.contractorcentral.service.AppInstance.AppServiceDelegate;
import com.accela.framework.UpdateItemResult;
import com.accela.framework.action.CivicIdAction;
import com.accela.framework.authorization.AMAuthManager;
import com.accela.framework.model.AddressModel;
import com.accela.framework.model.CivicIdProfileModel;
import com.accela.framework.model.InspectionModel;
import com.accela.framework.model.InspectionTypeModel;
import com.accela.framework.model.PeopleModel;
import com.accela.framework.persistence.AMAsyncEntityActionDelegate;
import com.accela.framework.persistence.AMAsyncEntityListActionDelegate;
import com.accela.framework.persistence.AMDataIncrementalResponse;
import com.accela.framework.persistence.AMDataResponse;
import com.accela.framework.persistence.AMStrategy;
import com.accela.framework.persistence.AMStrategy.AMAccessStrategy;
import com.accela.framework.service.CorelibManager;
import com.accela.inspection.action.InspectionAction;
import com.accela.inspection.model.InspectorModel;
import com.accela.mobile.AMLogger;
import com.accela.record.model.RecordInspectionModel;


public class InstantService{
	//private static final String TAG = "InstantService";

	public static void rescheduleInspection(final AppServiceDelegate<InspectionModel> delegate, RecordInspectionModel recordInspection, String recordId){
		InspectionAction action = new InspectionAction();
		AMStrategy strategy = new AMStrategy(AMAccessStrategy.Http);
		try {
			AMAsyncEntityActionDelegate<InspectionModel> actionDelegate = new AMAsyncEntityActionDelegate<InspectionModel>() {

				@Override
				public void onCompleted(AMDataResponse<InspectionModel> response) {

					if (response != null && response.getResult() != null) {
						List<InspectionModel> list = new ArrayList<InspectionModel>();
						list.add(response.getResult());
						delegate.onSuccess(list);
						AMLogger.logInfo("InstantService onCompleted()");
						return;
					}
					delegate.onFailure(new Throwable("Result is null!"));
				}

				@Override
				public void onFailure(Throwable error) {

					AMLogger.logInfo("InstantService onFailure()");
					delegate.onFailure(error);
				}
			};
			InspectionModel inspection = new InspectionModel();
			if (recordInspection.getAddress() != null)
				inspection.setAddress(recordInspection.getAddress());
			InspectionTypeModel typeModel = new InspectionTypeModel();
			typeModel.setId(recordInspection.getType().getId());
			inspection.setType(typeModel);
			if (recordInspection.getScheduleDate() != null)
				inspection.setScheduleDate(recordInspection.getScheduleDate());
			if (recordInspection.getScheduleStartTime() != null)
				inspection.setScheduleStartTime(recordInspection
						.getScheduleStartTime());
			if (recordInspection.getScheduleStartAMPM() != null)
				inspection.setScheduleStartAMPM(recordInspection
						.getScheduleStartAMPM());
			if (recordInspection.getScheduleEndTime() != null)
				inspection.setScheduleEndTime(recordInspection
						.getScheduleEndTime());
			if (recordInspection.getScheduleEndAMPM() != null)
				inspection.setScheduleEndAMPM(recordInspection
						.getScheduleEndAMPM());
			if (recordInspection.getRequestComment() != null)
				inspection.setRequestComment(recordInspection
						.getRequestComment());
			if (recordInspection.getRequestorFirstName() != null)
				inspection.setRequestorFirstName(recordInspection
						.getRequestorFirstName());
			if (recordInspection.getRequestorMiddleName() != null)
				inspection.setRequestorMiddleName(recordInspection
						.getRequestorMiddleName());
			if (recordInspection.getRequestorLastName() != null)
				inspection.setRequestorLastName(recordInspection
						.getRequestorLastName());
			if (recordInspection.getRequestorPhone() != null)
				inspection.setRequestorPhone(recordInspection
						.getRequestorPhone());
			if (recordInspection.getContact() != null)
				inspection.setContact(recordInspection.getContact());
			if (recordId != null)
				inspection.setRecordId_id(recordId);
			if (recordInspection.getId() != null)
				inspection.setId(recordInspection.getId());

			String agency = null;
			String environment = null;
			if (AppInstance.getProjectsLoader().getRecordById(recordId) != null) {
				agency = AppInstance.getProjectsLoader()
						.getRecordById(recordId).getResource_agency();
				environment = AppInstance.getProjectsLoader()
						.getRecordById(recordId).getResource_environment();
			}
			if (inspection.getId() != null)
				action.reScheduleInspectionAsync(null, actionDelegate,
						strategy, agency, environment, inspection,
						inspection.getId());
			else
				action.reScheduleInspectionAsync(null, actionDelegate,
						strategy, agency, environment, inspection, 0l);
		} catch (Exception e) {
			AMLogger.logError(e.toString());
			delegate.onFailure(e);
		}
	}
	
	public static void cancelInspection(final AppServiceDelegate<UpdateItemResult> delegate, String inspectionId, String recordId){
		InspectionAction action = new InspectionAction();
		AMStrategy strategy = new AMStrategy(AMAccessStrategy.Http);
		try {
			AMAsyncEntityListActionDelegate<UpdateItemResult> actionDelegate = new AMAsyncEntityListActionDelegate<UpdateItemResult>() {

				@Override
				public void onCompleted(
						AMDataIncrementalResponse<UpdateItemResult> response) {

					if (response != null && response.getResult() != null) {
						delegate.onSuccess(response.getResult());
						AMLogger.logInfo("InstantService onCompleted()");
						return;
					}
					delegate.onFailure(new Throwable("Result is null!"));
				}

				@Override
				public void onFailure(Throwable error) {

					AMLogger.logInfo("InstantService onFailure()");
					delegate.onFailure(error);
				}
			};
			String agency = null;
			String environment = null;
			if (AppInstance.getProjectsLoader().getRecordById(recordId) != null) {
				agency = AppInstance.getProjectsLoader().getRecordById(recordId).getResource_agency();
				environment = AppInstance.getProjectsLoader().getRecordById(recordId).getResource_environment();
			}
			action.cancelInspectionsAsync(null, actionDelegate, strategy, agency, environment, inspectionId);
		} catch (Exception e) {
			AMLogger.logError(e.toString());
			delegate.onFailure(e);
		}
	}
	
	public static void scheduleNewInspection(final AppServiceDelegate<InspectionModel> delegate, String recordId, AddressModel address, long typeId, Date scheduleDate, String scheduleStartTime, 
			String scheduleEndTime, String scheduleStartAMPM, String scheduleEndAMPM, String requestComment, String requestorFirstName, String requestorMiddleName, String requestorLastName, String requestorPhone,
			String inspectionFirstName, String inspectionMiddleName, String inspectionLastName, String inspectionNumberOrEmail, String preferredChannel, String organizationName){
		InspectionAction action = new InspectionAction();
		AMStrategy strategy = new AMStrategy(AMAccessStrategy.Http);
		try {
			InspectionModel inspection = new InspectionModel();
			if (address != null)
				inspection.setAddress(address);
			InspectionTypeModel typeModel = new InspectionTypeModel();
			typeModel.setId(typeId);
			inspection.setType(typeModel);
			if (scheduleDate != null)
				inspection.setScheduleDate(scheduleDate);
			if (scheduleStartTime != null)
				inspection.setScheduleStartTime(scheduleStartTime);
			if (scheduleStartAMPM != null)
				inspection.setScheduleStartAMPM(scheduleStartAMPM);
			if (scheduleEndTime != null)
				inspection.setScheduleEndTime(scheduleEndTime);
			if (scheduleEndAMPM != null)
				inspection.setScheduleEndAMPM(scheduleEndAMPM);
			if (requestComment != null)
				inspection.setRequestComment(requestComment);

			if (requestorFirstName != null || requestorMiddleName != null
					|| requestorLastName != null || requestorPhone != null) {
				if (requestorFirstName != null)
					inspection.setRequestorFirstName(requestorFirstName);
				if (requestorMiddleName != null)
					inspection.setRequestorMiddleName(requestorMiddleName);
				if (requestorLastName != null)
					inspection.setRequestorLastName(requestorLastName);
				if (requestorPhone != null)
					inspection.setRequestorPhone(requestorPhone);
			} else {
				if (inspectionFirstName != null)
					inspection.setRequestorFirstName(inspectionFirstName);
				if (inspectionMiddleName != null)
					inspection.setRequestorMiddleName(inspectionMiddleName);
				if (inspectionLastName != null)
					inspection.setRequestorLastName(inspectionLastName);
				if (inspectionNumberOrEmail != null && !inspectionNumberOrEmail.contains("@"))
					inspection.setRequestorPhone(inspectionNumberOrEmail);
			}

			PeopleModel people = new PeopleModel();
			if (inspectionFirstName != null)
				people.setFirstName(inspectionFirstName);
			if (inspectionMiddleName != null)
				people.setMiddleName(inspectionMiddleName);
			if (inspectionLastName != null)
				people.setLastName(inspectionLastName);
			if (inspectionNumberOrEmail != null && !inspectionNumberOrEmail.contains("@"))
				people.setPhone1(inspectionNumberOrEmail);
			if(inspectionNumberOrEmail != null && inspectionNumberOrEmail.contains("@"))
				people.setEmail(inspectionNumberOrEmail);
			if(organizationName!=null)
				people.setIndividualOrOrganization(organizationName);
			if(preferredChannel!=null)
				people.setPreferredChannel_value(preferredChannel);
			inspection.setContact(people);
			if (recordId != null)
				inspection.setRecordId_id(recordId);

			String agency = null;
			String environment = null;
			if (AppInstance.getProjectsLoader().getRecordById(recordId) != null) {
				agency = AppInstance.getProjectsLoader()
						.getRecordById(recordId).getResource_agency();
				environment = AppInstance.getProjectsLoader()
						.getRecordById(recordId).getResource_environment();
			}
			AMAsyncEntityActionDelegate<InspectionModel> actionDelegate = new AMAsyncEntityActionDelegate<InspectionModel>() {

				@Override
				public void onCompleted(AMDataResponse<InspectionModel> response) {

					if (response != null && response.getResult() != null) {
						List<InspectionModel> list = new ArrayList<InspectionModel>();
						list.add(response.getResult());
						delegate.onSuccess(list);
						AMLogger.logInfo("InstantService onCompleted()");
						return;
					}
					delegate.onFailure(new Throwable("Result is null!"));
				}

				@Override
				public void onFailure(Throwable error) {

					AMLogger.logInfo("InstantService onFailure()");
					delegate.onFailure(error);
				}
			};

			action.scheduleNewInspectionAsync(null, actionDelegate, strategy,
					agency, environment, inspection);
		} catch (Exception e) {
			AMLogger.logError(e.toString());
			delegate.onFailure(new Throwable(e.toString()));
		}
	}
	
	public static void getInspector(final AppServiceDelegate<InspectorModel> delegate, final String id, final String department)throws RuntimeException{
		InspectionAction action = new InspectionAction();
		AMStrategy strategy = new AMStrategy(AMAccessStrategy.Http);
		try {
			AMAsyncEntityActionDelegate<InspectorModel> actionDelegate1 = new AMAsyncEntityActionDelegate<InspectorModel>() {

				@Override
				public void onCompleted(AMDataResponse<InspectorModel> response) {

					if (response != null && response.getResult() != null) {
						List<InspectorModel> list = new ArrayList<InspectorModel>();
						list.add(response.getResult());
						delegate.onSuccess(list);
						AMLogger.logInfo("InstantService onCompleted()");
						return;
					}
					delegate.onFailure(new Throwable("Result is null!"));
				}

				@Override
				public void onFailure(Throwable error) {

					AMLogger.logInfo("InstantService onFailure()");
					delegate.onFailure(error);
				}

			};
			AMAsyncEntityListActionDelegate<InspectorModel> actionDelegate2 = new AMAsyncEntityListActionDelegate<InspectorModel>() {
				@Override
				public void onCompleted(
						AMDataIncrementalResponse<InspectorModel> response) {

					if (response != null && response.getResult() != null) {
						delegate.onSuccess(response.getResult());
						AMLogger.logInfo("InstantService onCompleted()");
						return;
					}
					delegate.onFailure(new Throwable("Result is null!"));
				}

				@Override
				public void onFailure(Throwable error) {

					AMLogger.logInfo("InstantService onFailure()");
					delegate.onFailure(error);
				}
			};
			if (id != null)
				action.getInspectorAsync(null, actionDelegate1, strategy, id);
			else if (department != null)
				action.getInspectorsAsync(null, actionDelegate2, strategy, department);
		} catch (Exception e) {
			AMLogger.logError(e.toString());
			delegate.onFailure(new Throwable(e.toString()));
		}
	}
	
	
	
	public static void getUserProfile(final AppServiceDelegate<CivicIdProfileModel> delegate)throws RuntimeException{
		try {
			AMAsyncEntityActionDelegate<CivicIdProfileModel> actionDelegate = new AMAsyncEntityActionDelegate<CivicIdProfileModel>() {

				@Override
				public void onCompleted(
						AMDataResponse<CivicIdProfileModel> response) {

					if (response != null && response.getResult() != null) {
						AMAuthManager.getInstance().setProfile(
								response.getResult());
						CorelibManager
								.getInstance()
								.getAccelaMobile()
								.getAuthorizationManager()
								.saveUserName(
										response.getResult().getLoginName());
						List<CivicIdProfileModel> list = new ArrayList<CivicIdProfileModel>();
						list.add(response.getResult());
						delegate.onSuccess(list);
					} else {
						delegate.onFailure(new Throwable("Result is empty!"));
					}
				}

				@Override
				public void onFailure(Throwable error) {

					delegate.onFailure(error);
				}
			};

			CivicIdAction action = new CivicIdAction();
			action.getCivicProfileAsync(null, actionDelegate, new AMStrategy(
					AMAccessStrategy.Http));
		} catch (Exception e) {
			AMLogger.logError(e.toString());
			delegate.onFailure(new Throwable(e.toString()));
		}
	}
 
}


