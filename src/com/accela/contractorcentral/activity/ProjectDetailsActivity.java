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
 *   Created by jzhong on 3/16/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */



package com.accela.contractorcentral.activity;



import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.fragment.ProjectInfoFragment;
import com.accela.contractorcentral.fragment.ProjectOverviewFragment;
import com.accela.contractorcentral.fragment.ProjectPermitFragment;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.ProjectsLoader;
import com.accela.contractorcentral.utils.Utils;
import com.accela.mobile.AMLogger;
import com.accela.record.model.RecordInspectionModel;
import com.accela.record.model.RecordModel;
import com.flurry.android.FlurryAgent;


@SuppressWarnings("deprecation")
public class ProjectDetailsActivity extends BaseActivity {

	
	ProjectOverviewFragment overviewFragment;
	ProjectInfoFragment infoFragment;
	ProjectPermitFragment permitFragment;
	
	ViewPager viewPager;
	ProjectDetailsPagerAdapter pagerAdapter;
	
	String projectId;
	ProjectModel projectModel;
	RecordInspectionModel cancelInspectionModel;
	
	RecordModel model;
	
	ProjectsLoader projectsLoader = AppInstance.getProjectsLoader();
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("ProjectDetailsActivity");
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
		setContentView(R.layout.activity_project_details);
		this.showScheduleBtn(true, false);
		//get project Id
		Intent intent = this.getIntent();
		projectId = intent.getStringExtra("projectId");
		int index = intent.getIntExtra("tabIndex", 0);
		cancelInspectionModel = (RecordInspectionModel) intent.getSerializableExtra("inspectionModel");

		projectModel = projectsLoader.getProjectById(projectId);
		if(projectModel==null) {
			finish();
			AMLogger.logInfo("Error!!, Please select a project");
			return;
		}
		if(Utils.getAddressUnit(projectModel.getAddress()).length()>0) {
			this.setActionBarTitle(Utils.getAddressLine1(projectModel.getAddress()) + "\n" 
					+ Utils.getAddressUnit(projectModel.getAddress()));
		} else {
			this.setActionBarTitle(Utils.getAddressLine1(projectModel.getAddress()));
			
		}
		setupDetailsTabs(index);
	}
	
	public RecordInspectionModel getCanceledInspectionModel(){
		return this.cancelInspectionModel;
	}
	
	@Override
	public void onResume(){
		super.onResume();

	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
		cancelInspectionModel = (RecordInspectionModel) intent.getSerializableExtra("inspectionModel");
	}
	
	protected void scheduleInspection(){
		 Intent intent = new Intent(this, MenuActivity.class);
		 intent.putExtra("projectId", projectId);
		 startActivity(intent);
	}
	
	private void setupDetailsTabs(int position) {
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		viewPager.setOnPageChangeListener(
	            new ViewPager.SimpleOnPageChangeListener() {
	                
					@Override
	                public void onPageSelected(int position) {
	                    // When swiping between pages, select the
	                    // corresponding tab.
	                    getSupportActionBar().setSelectedNavigationItem(position);
	                }
	            });
		
		viewPager.setAdapter(pagerAdapter = new ProjectDetailsPagerAdapter(getSupportFragmentManager()));
		
		final ActionBar actionBar = this.getSupportActionBar();
	    // Specify that tabs should be displayed in the action bar.
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

	    // Create a tab listener that is called when the user changes tabs. 
	    
	    ActionBar.TabListener tabListener = new ActionBar.TabListener() {

			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				viewPager.setCurrentItem(tab.getPosition());
				View view = tab.getCustomView();
				if(view!=null) {
					TextView textTitle = (TextView) view.findViewById(R.id.textTitle);
					textTitle.setTextColor(getResources().getColor(R.color.header_blue));
				}
			}

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
				View view = tab.getCustomView();
				if(view!=null) {
					TextView textTitle = (TextView) view.findViewById(R.id.textTitle);
					textTitle.setTextColor(getResources().getColor(R.color.mid_gray));
				}
			}

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
				
			}


	        
	    };
	    //set overview tab
	    ActionBar.Tab tab = actionBar.newTab()
	    		//.setText(R.string.overview)
                .setTabListener(tabListener);
	    View view = LayoutInflater.from(this).inflate(R.layout.tab_item, null);
	    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	    view.setLayoutParams(lp);
	    TextView textTitle = (TextView) view.findViewById(R.id.textTitle);
	    textTitle.setText(R.string.overview);
		textTitle.setTextColor(getResources().getColor(R.color.header_blue));
	    tab.setCustomView(view);
	    actionBar.addTab(tab);
	    
	    

	    //set permit tab
	    tab = actionBar.newTab()
              //  .setText(R.string.permits)
	    		.setTabListener(tabListener);
	    view = LayoutInflater.from(this).inflate(R.layout.tab_item, null);
	    lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	    view.setLayoutParams(lp);
	    textTitle = (TextView) view.findViewById(R.id.textTitle);
	    textTitle.setText(R.string.permits);
	    tab.setCustomView(view);
	    actionBar.addTab(tab);
	    
	    //set info tab
	    tab = actionBar.newTab()
	    	//	.setText(R.string.info)
                .setTabListener(tabListener);
	    view = LayoutInflater.from(this).inflate(R.layout.tab_item, null);
	    lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	    view.setLayoutParams(lp);
	    textTitle = (TextView) view.findViewById(R.id.textTitle);
	    textTitle.setText(R.string.info);
	    tab.setCustomView(view);
	    actionBar.addTab(tab);
	    
	    viewPager.setCurrentItem(position);
	}
	
	private class ProjectDetailsPagerAdapter extends FragmentPagerAdapter {
	    public ProjectDetailsPagerAdapter(FragmentManager fm) {
	        super(fm);
	    }
     
	    @Override
	    public Fragment getItem(int i) {
	        if(i==0) { 
	        	if(overviewFragment==null) {
	        		overviewFragment = new ProjectOverviewFragment();
	        		overviewFragment.setProjectId(projectId);
	        	}
	        	return overviewFragment; 
	        } else if(i==1){
	        	if(permitFragment==null) {
	        		permitFragment = new ProjectPermitFragment();
	        		permitFragment.setProjectId(projectId);
	        	}
	        	return permitFragment;
	        } else {
	        	if(infoFragment==null) {
	        		infoFragment = new ProjectInfoFragment();
	        		infoFragment.setProjectId(projectId);
	        	}
	        	return infoFragment;
	        }

	    }
 
	    @Override
	    public int getCount() {
	        return 3;
	    }

	    @Override
	    public CharSequence getPageTitle(int position) {
	    	switch(position) {
	    	case 0:
	    		return getString(R.string.info);
	    	case 1:
	    		return getString(R.string.permits);
	    	case 2:
	    		return getString(R.string.fees);
	    	}
	        return "";
 	    }
	} 
}