package com.accela.contractorcentral.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.accela.contractorcentral.R;
import com.accela.contractorcentral.view.ProjectListView;
import com.accela.contractorcentral.view.ProjectListView.OnSelectProjectListener;
import com.accela.mobile.AMLogger;
//import android.support.v4.app.Fragment;



public class ProjectFragment extends Fragment  {

    private ProjectListView projectListView;

    private int listStyle = ProjectListView.PROJECT_LIST_FULL;
    OnSelectProjectListener onSelectProjectListener;
    
   public ProjectFragment() {}
   
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);	
		AMLogger.logInfo("ProjectFragment.onActivityCreated()");
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AMLogger.logInfo("ProjectFragment.onCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  
    	AMLogger.logInfo("ProjectFragment.onCreateView()");
    	//create progress dialog
    	/*pDialog = new ProgressDialog(getActivity());
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDialog.setMessage(getResources().getString(
					R.string.loading_wait_message));
		pDialog.setCanceledOnTouchOutside(true);
		pDialog.setCancelable(true);*/
    	
    	// Create content view.
        View contentView = inflater.inflate(R.layout.fragment_project_list, container, false);                 
        projectListView = (ProjectListView) contentView.findViewById(R.id.projectListView);	
       
        projectListView.setListStyle(listStyle);
        projectListView.setOnSelectProjectListener(onSelectProjectListener);

        return contentView;
    }
   
    public void setListStyle(int listStyle) {
		this.listStyle = listStyle;
		if(projectListView!=null) {
			projectListView.setListStyle(listStyle);
		}
	}
    
    
    public void setListSelectListener(OnSelectProjectListener l) {
    	onSelectProjectListener = l;
    	if(projectListView!=null) {
    		projectListView.setOnSelectProjectListener(onSelectProjectListener);
    	}
    }

    
}