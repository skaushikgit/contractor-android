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
 *   Created by jzhong on 3/19/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */


package com.accela.contractorcentral.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.activity.ProjectDetailsActivity;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.ProjectsLoader;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.utils.Utils;
import com.accela.contractorcentral.view.InspectionTypeListView;
import com.accela.contractorcentral.view.PermitViewPager;
import com.accela.record.model.RecordInspectionModel;
import com.accela.record.model.RecordModel;
//import android.support.v4.app.Fragment;


public class ProjectPermitFragment extends Fragment{

	private String projectId;
	private String permitId;
	ProjectModel projectModel;

	View contentView;
	InspectionTypeListView listViewInspection;
	PermitViewPager viewPagerPermit;


	ProjectsLoader projectsLoader = AppInstance.getProjectsLoader();
	int lastIndex = 0;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if(savedInstanceState!=null) {
			projectId = savedInstanceState.getString("projectId");
		}
		super.onCreate(savedInstanceState);      
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {       
		// Create content view.
		contentView = inflater.inflate(R.layout.fragment_project_permits, container, false);                 
		projectModel = projectsLoader.getProjectById(projectId);

		//update UI
		updatePermitViewPager();
		setupElasticListView();
		updateInspectionTypeView(null, 0);
		return contentView;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		updateInspectionTypeView(null, 0);
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
		
		if(this.getActivity()!=null) {
			throw new AssertionError("Must set project Id before onCreate()");
		}
	}

	private void updateInspectionTypeView(String permitId, int animationDirection) {

		if (lastIndex < projectModel.getRecords().size()) {
			if (permitId == null) {
				// get first permit
				permitId = projectModel.getRecords().get(lastIndex).getId();
			}
			this.permitId = permitId;
			listViewInspection.setPermit(permitId, animationDirection, ((ProjectDetailsActivity) this.getActivity()).getCanceledInspectionModel());
		}
	}

	private void updatePermitViewPager() {
		viewPagerPermit = (PermitViewPager) contentView.findViewById(R.id.permitViewPager);
		viewPagerPermit.setProject(projectModel);
		viewPagerPermit.setOnSelectPermitListener(new PermitViewPager.OnSelectPermitListener() {
			@Override
			public void onSelectPermit(RecordModel permit, int position) {
				updateInspectionTypeView(permit.getId(), position > lastIndex? 1: -1);
				lastIndex = position;
			}
		});
	}

	private void setupElasticListView() {
		listViewInspection = (InspectionTypeListView) contentView.findViewById(R.id.listViewInspectionType);
		listViewInspection.showApprovedFailedInspection(true);
		listViewInspection.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		listViewInspection.setMaxOverScrollDistance(0, 100); 
		listViewInspection.showAddress(false);
		final DisplayMetrics metrics = this.getActivity().getResources()
				.getDisplayMetrics();
		final float density = metrics.density;
		final int topMargin = (int) (1 * density);
		viewPagerPermit.setY(topMargin);
		/*
		listViewInspection.setCallbacks(new ElasticListView.ScrollCallbacks() {

			@Override
			public void onScrollChanged(int l, int t, int oldl, int oldt) {
				viewPagerPermit.setY(t);
				if(-t >= topMargin) {
					viewPagerPermit.setY(-t);
				} else {
					viewPagerPermit.setY(topMargin);
				}
			}
		}); */

		listViewInspection.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				RecordInspectionModel inspection = listViewInspection.getRecordInspectionModel();
				if(inspection!=null){
					int status = Utils.checkInspectionStatus(inspection);
					if(status == AppConstants.INSPECTION_STATUS_FAILED) {
						//if inspection failed, show the details
						ActivityUtils.startInspectionDetailsActivity(getActivity(), inspection, Utils.isInspectionFailed(inspection), AppConstants.CANCEL_INSPECTION_SOURCE_PERMITLIST);
					/*	String projectId = AppInstance.getProjectsLoader().getParentProject(inspection.getRecordId_id()).getProjectId();
						ActivityUtils.startChooseInspectionTimeActivity(getActivity(), 
								projectId, inspection.getRecordId_id(), inspection.getId(), 
								Utils.generateDailyInspectionTypeModel(inspection.getType()), inspection, false);
						*/
					} else if(status == AppConstants.INSPECTION_STATUS_PASSED) {
						//if inspection passed, show the details
						ActivityUtils.startInspectionDetailsActivity(getActivity(), inspection, Utils.isInspectionFailed(inspection), AppConstants.CANCEL_INSPECTION_SOURCE_PERMITLIST);
					} else {
						ActivityUtils.startScheduleInspectionActivity(getActivity(), projectId, permitId, inspection, AppConstants.CANCEL_INSPECTION_SOURCE_PERMITLIST);
					}
				} else {
					if(listViewInspection.getInspectionType() != null) {
						if(listViewInspection.getInspectionType().getHasSchdulePermission()!=null && listViewInspection.getInspectionType().getHasSchdulePermission().equalsIgnoreCase("Y"))
							ActivityUtils.startAvailableInspectionDetailsActivity(getActivity(), listViewInspection.getInspectionType(), projectId); 
						else{
							Toast.makeText(getActivity(), getString(R.string.inspection_type_no_permisstion_to_schedule), Toast.LENGTH_SHORT).show();
						}
					}
				} 

			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("projectId", projectId);

		super.onSaveInstanceState(outState);
	}

}