

package com.accela.contractorcentral.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.activity.BaseActivity;
import com.accela.contractorcentral.activity.RefreshTokenActivity;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.service.AppInstance.AppServiceDelegate;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.utils.Utils;
import com.accela.framework.AMException;
import com.accela.framework.persistence.AMAsyncEntityActionDelegate;
import com.accela.framework.persistence.AMDataResponse;
import com.accela.framework.persistence.AMStrategy;
import com.accela.framework.persistence.AMStrategy.AMAccessStrategy;
import com.accela.inspection.action.InspectionAction;
import com.accela.inspection.model.InspectorModel;
import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.accela.mobile.util.UIUtils;
import com.accela.record.model.RecordInspectionModel;



public class InspectorLoader {
	
	private int MAX_CONCURRENT_DOWNLOAD = 3; 
	
	public static class InspectionInspectorItem {
		public Long inspectionId;
		public List<InspectorModel> listAllInspectors;
	}
	private AppServiceDelegate<InspectorModel> delegate;
	private  Vector<RecordInspectionModel> waitingDownloadQueue = new Vector<RecordInspectionModel>();
	private  Vector<RecordInspectionModel> downloadingQueue = new Vector<RecordInspectionModel>();
	
	//the map to store <Inspection ID, inspetor items>
	private Map<Long, InspectionInspectorItem> downloadedInspectors = new ConcurrentHashMap<Long, InspectionInspectorItem>();
	
	InspectorLoader() {}
	
	public InspectionInspectorItem getInspectorByInspectionId(Long inspectionId) {
		if(inspectionId==null) {
			return null;
		} else {
			return downloadedInspectors.get(inspectionId);
		}
	}
	
	public void showInspectors(final BaseActivity activity, final String permitId, final RecordInspectionModel inspection) {
		// TODO Auto-generated method stub
		final ProjectModel project = AppInstance.getProjectsLoader().getParentProject(permitId);
		if(project!=null) {

					InspectionInspectorItem item = getInspectorByInspectionId(inspection.getId());
					if(item!=null || inspection.getInspectorId()==null){
						ActivityUtils.startInspectionContactActivity(activity, project.getProjectId(), permitId, inspection, true);
						return;
					}
					activity.showProgressDialog(activity.getString(R.string.load_contacts), true);
					AppServiceDelegate<InspectorModel> delegate = new AppServiceDelegate<InspectorModel>() {
						@Override
						public void onSuccess(List<InspectorModel> response) {
							// TODO Auto-generated method stub
							activity.closeProgressDialog();
							ActivityUtils.startInspectionContactActivity(activity, project.getProjectId(), permitId, inspection, true);
						}

						@Override
						public void onFailure(Throwable error) {
							// TODO Auto-generated method stub
							activity.closeProgressDialog();
							UIUtils.showMessageDialog(activity, activity.getString(R.string.warning_title), error.getMessage());
						}
					};
					loadInspectorByInspection(inspection, delegate);
		}
	}
	
	public void loadInspectorByInspection(RecordInspectionModel inspection, AppServiceDelegate<InspectorModel> delegate) {
		//download the inspection list, include the latest one.
		this.delegate = delegate;
		InspectionInspectorItem item = getInspectorByInspectionId(inspection.getId());
		if(item != null && item.listAllInspectors!=null) {
			//download finish, don't download twice
			delegate.onSuccess(item.listAllInspectors);
			return;
		}  
		
		if(downloadingQueue.contains(inspection)) {
			//downloading , did nothing
		} else if(waitingDownloadQueue.contains(inspection)) {
			//re-order the priority, move new request to the first one
			waitingDownloadQueue.remove(inspection);
			waitingDownloadQueue.add(0, inspection);
		} else {
			
			waitingDownloadQueue.add(0, inspection);
			loadMoreInspectors();
		}
	}
	
	
	private void loadMoreInspectors() {
		if(downloadingQueue.size() >= MAX_CONCURRENT_DOWNLOAD) {
			return;
		} else {
			executeDownloadTask();
		}
	}
	
	private void saveInspectors(RecordInspectionModel inspection, InspectorModel inspector) {
		InspectionInspectorItem item = downloadedInspectors.get(inspection.getId());
		if(item==null) {
			item = new InspectionInspectorItem();
			item.inspectionId = 0l;
			item.listAllInspectors = new ArrayList<InspectorModel>();
			downloadedInspectors.put(inspection.getId(), item);
		}
		if(inspector != null) {
			//download successfully
			item.listAllInspectors.clear();
			item.listAllInspectors.add(inspector);
		}
	}
	
	private InspectorModel checkForOfficePhone(InspectorModel inspector){
		if(inspector!=null && inspector.getPreferredChannel()!=null){
			if(inspector.getPreferredChannel().equals(AppConstants.ACCELA_AUTO_PREFERRED__CHANNEL_WORK_PHONE)){
				if(AppInstance.getAppSettingsLoader().getPhoneNumber()!=null && AppInstance.getAppSettingsLoader().getPhoneNumber().length()>0)
					inspector.setMobilePhone(AppInstance.getAppSettingsLoader().getPhoneNumber());
			}
		}
		return inspector;
	}
	
	private class RequestDelegate extends AMAsyncEntityActionDelegate<InspectorModel> {
		private RecordInspectionModel inspection;
		RequestDelegate(RecordInspectionModel inspection) {
			this.inspection = inspection;
		}
		
		@Override
		public void onCompleted(AMDataResponse<InspectorModel> response) {
			List<InspectorModel> list = new ArrayList<InspectorModel>();
			if (response != null && response.getResult() != null) {
				InspectorModel inspector = checkForOfficePhone(response.getResult());
				saveInspectors(inspection, inspector);
				// remove from downloading queue
				for(int i=0; i<downloadingQueue.size(); i++){
					if(downloadingQueue.get(i).getId()==inspection.getId())
						downloadingQueue.remove(i);

				}
				list.add(inspector);
			}
			delegate.onSuccess(list);
		}

		@Override
		public void onFailure(Throwable error) {
			AMLogger.logInfo("Fee request failed");
			//remove from downloading queue
			for(int i=0; i<downloadingQueue.size(); i++){
				if(downloadingQueue.get(i).getId()==inspection.getId())
					downloadingQueue.remove(i);

			}			delegate.onFailure(error);
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
		InspectionAction action = new InspectionAction();
		AMStrategy strategy = new  AMStrategy(AMAccessStrategy.Http);
		
		String currentAgency = null, currentEnvironment = null;
		if(waitingDownloadQueue.size()==0)
			return;
		RecordInspectionModel inspection = waitingDownloadQueue.get(0);
		if(currentAgency==null && inspection.getRecordId_id()!=null){
			currentAgency = AppInstance.getProjectsLoader().getRecordById(inspection.getRecordId_id()).getResource_agency();
			currentEnvironment = AppInstance.getProjectsLoader().getRecordById(inspection.getRecordId_id()).getResource_environment();
		}
		final AMAsyncEntityActionDelegate<InspectorModel> requestDelegate = new RequestDelegate(inspection);
		action.getInspectorAsync(null, requestDelegate, strategy, currentAgency, currentEnvironment, inspection.getInspectorId());
		waitingDownloadQueue.remove(0);
		downloadingQueue.add(inspection);
	}	
}
