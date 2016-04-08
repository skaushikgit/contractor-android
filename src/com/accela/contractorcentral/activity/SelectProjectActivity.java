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
 *   Created by jzhong on 1/20/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */

package com.accela.contractorcentral.activity;



import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.fragment.ProjectFragment;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.view.ProjectListView;
import com.accela.contractorcentral.view.ProjectListView.OnSelectProjectListener;
import com.flurry.android.FlurryAgent;


public class SelectProjectActivity extends BaseActivity implements OnSelectProjectListener  {

	
	ProjectFragment projectFragment;
	private boolean fromScheduleAnother;
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("SelectProjectActivity");
		FlurryAgent.onPageView();
		FlurryAgent.onStartSession(this, AppConstants.FLURRY_APIKEY);
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarTitle(R.string.schedule_inspection);
		
		setContentView(R.layout.activity_blank);
		projectFragment = new ProjectFragment();
		projectFragment.setListStyle(ProjectListView.PROJECT_LIST_COMPACT);
		FragmentManager fm = this.getSupportFragmentManager();
		fm.beginTransaction()
			.add(R.id.rootContainer, projectFragment)
			.commit();
		
		projectFragment.setListSelectListener(this);
		fromScheduleAnother = getIntent().getBooleanExtra("fromScheduleAnother", false);
	}
	
	private void onSelectProject(String projectId) {
		ActivityUtils.startChoosePermitActivity(this, projectId);
	}
	
	private void cancelSelectProject() {
		if(fromScheduleAnother==true)
			ActivityUtils.startProjectListActivity(this);
		this.exitActivityWithAnimated();
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}
	
	
	@Override
	public void onBackPressed() {
		cancelSelectProject();
	}

	@Override
	public void onSelectProject(int position, ProjectModel project) {
		onSelectProject(project.getProjectId());
	}
	
	
	



}