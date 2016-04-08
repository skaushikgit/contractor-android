
package com.accela.contractorcentral.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.fragment.ProjectFragment;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.view.ProjectListView;
import com.accela.contractorcentral.view.ProjectListView.OnSelectProjectListener;
import com.flurry.android.FlurryAgent;

 
public class ProjectListActivity extends BaseActivity implements  OnSelectProjectListener{



	
	ProjectFragment activeProjectFragment;
	//ProjectFragment completedProjectFragment;
	//ViewPager viewPager;
	//ProjectListPagerAdapter pagerAdapter;
	 
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("ProjectListActivity");
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
		
		   
		this.setActionBarTitle(R.string.projects);
		
		setContentView(R.layout.activity_project);
		showScheduleBtn(true, false);

		//setupProjectTabs();
		
		activeProjectFragment = new ProjectFragment();
		activeProjectFragment.setListStyle(ProjectListView.PROJECT_LIST_FULL);
		activeProjectFragment.setListSelectListener(this);
		
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		ft.add(R.id.rootContainer,  (Fragment) activeProjectFragment);
		ft.commit();
		
		
	}
	
	@Override
	public void onResume(){
		AppInstance.getProjectsLoader().requestLocation(this);
		super.onResume();
		//plusImage.setVisibility(View.VISIBLE);
	}

	private void viewProjectDetails(ProjectModel model) {
		ActivityUtils.startProjectDetailsActivity(this, model.getProjectId(), 0, null);
	}
	
	@Override
	public void onBackPressed() {
		ActivityUtils.startLandingPageActivityFromLeft(this);
		this.finish();
	}
	/*
	private void setupProjectTabs() {
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
		
		viewPager.setAdapter(pagerAdapter = new ProjectListPagerAdapter(getSupportFragmentManager()));
		
		final ActionBar actionBar = this.getSupportActionBar();
	    // Specify that tabs should be displayed in the action bar.
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

	    // Create a tab listener that is called when the user changes tabs.
	    ActionBar.TabListener tabListener = new ActionBar.TabListener() {

			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				
				viewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
				
				
			}

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
				
				
			}
	        
	    };

	    actionBar.addTab(actionBar.newTab()
                        .setText(getString(R.string.active))
                        .setTabListener(tabListener));
	    actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.completed))
                .setTabListener(tabListener));
	}
	
	private class ProjectListPagerAdapter extends FragmentPagerAdapter {
	    public ProjectListPagerAdapter(FragmentManager fm) {
	        super(fm);
	    }

	    @Override
	    public Fragment getItem(int i) {
	        if(i==0) {
	        	if(activeProjectFragment==null) {
	        		activeProjectFragment = new ProjectFragment();
	        		activeProjectFragment.setListStyle(ProjectListView.PROJECT_LIST_FULL);
	        		activeProjectFragment.setProjectType(ProjectManager.ACTIVE_PROJECT);
	        		activeProjectFragment.setListSelectListener(ProjectListActivity.this);
	        	}
	        	return activeProjectFragment;
	        } else if(i==1){
	        	if(completedProjectFragment==null) {
	        		completedProjectFragment = new ProjectFragment();
	        		completedProjectFragment.setListStyle(ProjectListView.PROJECT_LIST_COMPACT);
	        		completedProjectFragment.setProjectType(ProjectManager.COMPLETED_PROJECT);
	        	}
	        	return completedProjectFragment;
	        }
	        return null;
	    }
 
	    @Override
	    public int getCount() {
	        return 2;
	    }

	    @Override
	    public CharSequence getPageTitle(int position) {
	        if(position == 0) {
	        	return getString(R.string.active);
	        } else {
	        	return getString(R.string.completed);
	        }
 	    }
	} 
	*/
	
	
	@Override
	public void onSelectProject(int position, ProjectModel project) {
		viewProjectDetails(project);
	}



}