package com.accela.contractorcentral.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.accela.contractorcentral.R;
import com.accela.contractorcentral.activity.BaseActivity;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.utils.Utils;
import com.accela.contractorcentral.view.InspectionAvailableTimeListView;
import com.accela.inspection.model.InspectionTimesModel;
import com.accela.mobile.AMLogger;
import com.accela.record.model.DailyInspectionTypeModel;
import com.accela.record.model.RecordInspectionModel;
//import android.support.v4.app.Fragment;




public class ChooseInspectionTimeFragment extends Fragment  {

	private String recordId;
	private String projectId;
	private DailyInspectionTypeModel inspectionType;
   
    InspectionAvailableTimeListView listViewAvailableTime;

    View contentView;
	private long inspectionId;
	private boolean isReschedule;
	private RecordInspectionModel inspection;
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);		
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
		if(savedInstanceState!=null) {
			projectId = savedInstanceState.getString("projectId");
			inspectionType = (DailyInspectionTypeModel) savedInstanceState.getSerializable("inspectionType");
		}
        super.onCreate(savedInstanceState);      
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {       
    	// Create content view.
        contentView = inflater.inflate(R.layout.fragment_choose_inspection_time, container, false);                 
        listViewAvailableTime= (InspectionAvailableTimeListView) contentView.findViewById(R.id.listAvailableTime);
        
        listViewAvailableTime.setOnInpsectionTimeListViewListener(new InspectionAvailableTimeListView.OnInpsectionTimeListViewListener() {

			@Override
			public void OnViewMoreTimes() {
				BaseActivity activity = (BaseActivity) getActivity();
				activity.setActionBarTitle(R.string.more_times);
				listViewAvailableTime.setListViewExpanded(true);
			}

			@Override
			public void OnSelectInspectionTime(InspectionTimesModel model) {
				
				ActivityUtils.startScheduleInspectionActivity(getActivity(), projectId, recordId, inspectionType, model, inspection, isReschedule);
			}
        	
        });
        AMLogger.logInfo("ChooseInspectionTimeFragment.onCreateView ");
        updateListHeader();
        updateAvailableTimeListView();
        setupElasticScrollView();
        return contentView;
    }
   
    public void setInspectionType1(String projectId, String recordId, long inspectionId, DailyInspectionTypeModel inspectionType) {
    	this.setInspectionType(projectId, recordId, inspectionId, inspectionType, null, false);
    }
    
    public void setInspectionType(String projectId, String recordId, long inspectionId, DailyInspectionTypeModel inspectionType, RecordInspectionModel inspection, boolean isReschedule) {
    	this.projectId = projectId;
    	this.recordId = recordId;
    	this.inspectionId = inspectionId;
    	this.inspectionType = inspectionType;
    	this.isReschedule = isReschedule;
    	this.inspection = inspection;
    	AMLogger.logInfo("ChooseInspectionTimeFragment.setInspectionType - " + this.inspectionType.getId());
    }
    
    private void updateListHeader() {
    	View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.list_header_view_more_time, null, false);    
    	ProjectModel projectModel = AppInstance.getProjectsLoader().getProjectById(projectId);
    	TextView textView = (TextView) headerView.findViewById(R.id.textAddressLine1);
    	textView.setText(Utils.getAddressLine1AndUnit(projectModel.getAddress()));
    	
    	textView = (TextView) headerView.findViewById(R.id.textAddressLine2);
    	textView.setText(Utils.getAddressLine2(projectModel.getAddress()));
    	String info[] = Utils.formatInspectionTypeInfo(inspectionType, recordId);

    	textView = (TextView) headerView.findViewById(R.id.textInspectionGroup);
    	textView.setText(info[0]);
    	
    	textView = (TextView) headerView.findViewById(R.id.textInspectionType);
    	textView.setText(info[1]);
		
    	
    	
    	listViewAvailableTime.addHeaderView(headerView);
    }
	
    private void updateAvailableTimeListView() {
    	AMLogger.logInfo("ChooseInspectionTimeFragment.updateAvailableTimeListView " + inspectionType.getId());
    	//disable scroll the contact list. 
    	listViewAvailableTime.setMaximalDisplayItems(3);
    	
    	listViewAvailableTime.setInspectionType(projectId, recordId, inspectionId, inspectionType);
	}
    
    private void setupElasticScrollView() {
    //	ElasticScrollView scrollView = (ElasticScrollView) contentView.findViewById(R.id.scrollView);
    //	scrollView.setMaxOverScrollDistance(0, 100); 
    	
    }
    

   

   
    @Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("projectId", projectId);
		outState.putSerializable("inspectionType", inspectionType);
		super.onSaveInstanceState(outState);
	}

    public boolean handleBackButton() {
    	if(listViewAvailableTime.isListViewExpanded()) {
    		listViewAvailableTime.setListViewExpanded(false);
    		BaseActivity activity = (BaseActivity) getActivity();
    		activity.setActionBarTitle(R.string.choose_time);
    		return true;
    	}
    	return false;
    }

}