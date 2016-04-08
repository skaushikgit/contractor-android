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
 *   Created by jzhong on 3/9/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */

package com.accela.contractorcentral.view;


import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.activity.BaseActivity;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.ProjectsLoader;
import com.accela.contractorcentral.utils.APIHelper;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.utils.Utils;
import com.accela.mobile.AMLogger;
import com.accela.record.model.RecordInspectionModel;


public class InspectionViewPager extends FrameLayout implements Observer {

	private ProjectsLoader projectsLoader;
	protected Context mContext;
	private PageControlView pageControl;
	private float density;
	private List<RecordInspectionModel> recentInspectionList = new ArrayList<RecordInspectionModel> ();
	private List<ProjectModel> nearByProjectList = new ArrayList<ProjectModel>();
	private ProgressBar loadingProgress;
	private BaseActivity activity;
	private long mLastClickTime = 0;
	private static int MAX_PAGE_COUNT = 5;
	/**
	 * The viewpager 
	 */
	private ViewPager viewPager;

	private InspectionPagerAdapter pagerAdapter;

	private OnSelectInspectionListener selectInspectionListener;

	public interface OnSelectInspectionListener {
		public void onSelectInspection(RecordInspectionModel inspection, int position);
	}


	public InspectionViewPager(Context context) {
		this(context, null);

	}

	public InspectionViewPager(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}


	public InspectionViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		init(context, attrs, defStyleAttr);
	}

	public void setOnSelectInspectionListener(OnSelectInspectionListener l) {
		selectInspectionListener = l;
	}

	protected void init(Context context, AttributeSet attrs, int defStyleAttr) {
		mContext = context;
		activity = (BaseActivity) context;
		density = context.getResources().getDisplayMetrics().density;
		setBackgroundResource(R.drawable.card_background_white);

		projectsLoader = AppInstance.getProjectsLoader();


		viewPager = new ViewPager(context);

		viewPager.setAdapter(pagerAdapter = new InspectionPagerAdapter());

		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {


			public void onPageSelected(int position) {
				pageControl.setCurrrentIndex(position);
				udpatePhoneAndTitleBarColor(position);
				int count = recentInspectionList.size();
				if(position < count && selectInspectionListener!=null) {
					RecordInspectionModel inspection = recentInspectionList.get(position);
					selectInspectionListener.onSelectInspection(inspection, position);
				} 

			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		viewPager.setLayoutParams(lp);
		this.addView(viewPager);

		//add page control view
		pageControl = new PageControlView(context);
		lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lp.setMargins(0, 0, 0, (int) (10* density));
		lp.gravity = Gravity.BOTTOM;
		pageControl.setLayoutParams(lp);

		this.addView(pageControl);
		pageControl.setCount(pagerAdapter.getCount());
		udpatePhoneAndTitleBarColor(viewPager.getCurrentItem());

		//add loading progress
		loadingProgress = new ProgressBar(context, null, android.R.attr.progressBarStyle);
		lp = new FrameLayout.LayoutParams((int) (28 * density), (int) (28 * density));
		lp.setMargins(0, (int) (20 * density), (int) (20 * density), 0);
		lp.gravity = Gravity.RIGHT | Gravity.TOP;
		loadingProgress.setLayoutParams(lp);
		this.addView(loadingProgress);

		updateListsItem(0);

	}

	private void udpatePhoneAndTitleBarColor(int position) {
		int color;
		if(position < recentInspectionList.size()) {
			RecordInspectionModel model = recentInspectionList.get(position);

			boolean failed = Utils.isInspectionFailed(model);

			if(failed) {
				color = mContext.getResources().getColor(R.color.red_failed_header);
			} else {
				color = mContext.getResources().getColor(R.color.green_pass_header);
			}
		} else {
			color = mContext.getResources().getColor(R.color.blue_nearby_header);
		}
		APIHelper.setPhoneStatusBarColor((Activity) getContext(), color);
		activity.setActionBarColor(color);
	}

	@Override
	protected void onAttachedToWindow() {
		projectsLoader.addObserver(this);
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		projectsLoader.deleteObserver(this);
		super.onDetachedFromWindow();
	}

	//private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm");

	private class InspectionPagerAdapter extends PagerAdapter{

		@Override
		public int getItemPosition(Object object) {
			// must add this. force refresh when call notifyDataSetChanged
			return POSITION_NONE;
		}

		@Override
		public int getCount() {
			int count = recentInspectionList.size(); 
			count += nearByProjectList.size();
			count = count > 0? count: 1;  //if no any nearby project and inspection, Need to show loading screen
			count = count <= MAX_PAGE_COUNT ? count: MAX_PAGE_COUNT; //only show 5 recent inspection.
			return count; 
		}
 
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}


		@Override
		public Object instantiateItem(ViewGroup container, final int position) {

			View view = LayoutInflater.from(mContext).inflate(R.layout.card_inspection_status, null);
			container.addView(view);
			//View cardHeader = view.findViewById(R.id.cardHeader);
			//cardHeader.getLayoutParams().height = (int) ((48 + 100) * density);
			View cardContentBack = view.findViewById(R.id.cardContentBack);
			View cardContent = view.findViewById(R.id.cardContent);

			ImageView imageStatus = (ImageView) view.findViewById(R.id.imageStatus);

			Button buttonDetails = (Button) view.findViewById(R.id.buttonDetails);
			Button buttonContact = (Button) view.findViewById(R.id.buttonContact);

			TextView inspectionTitle = (TextView) view.findViewById(R.id.inspectionTitle);
			TextView textStatus = (TextView) view.findViewById(R.id.textStatus);

			TextView textAddressLine1 = (TextView) view.findViewById(R.id.textAddressLine1);
			TextView textAddressLine2 = (TextView) view.findViewById(R.id.textAddressLine2);
			TextView textGroup = (TextView) view.findViewById(R.id.textGroup);
			TextView textType = (TextView) view.findViewById(R.id.textType);
			//display recent inspection first, then display nearby project
			if(position < recentInspectionList.size()) {
				final RecordInspectionModel inspection = recentInspectionList.get(position);
				final String permitId = inspection.getRecordId_id();
				
				boolean failed = Utils.isInspectionFailed(inspection);
				String inspectionInfo[] = Utils.formatInspectionInfo(inspection);
				if(failed) {
					//cardHeader.setBackgroundColor(mContext.getResources().getColor(R.color.red_failed_header));
					cardContentBack.setBackgroundColor(mContext.getResources().getColor(R.color.red_failed_content));
					textStatus.setText(R.string.FAILED);
					imageStatus.setImageResource(R.drawable.insp_fail);
				} else {
					//cardHeader.setBackgroundColor(mContext.getResources().getColor(R.color.green_pass_header));
					cardContentBack.setBackgroundColor(mContext.getResources().getColor(R.color.green_pass_content));
					textStatus.setText(R.string.PASSED);
					imageStatus.setImageResource(R.drawable.insp_pass);
				}
				textAddressLine1.setText(Utils.getAddressLine1AndUnit(inspection.getAddress()));
				textAddressLine2.setText(Utils.getAddressLine2(inspection.getAddress()));
				String text = inspectionInfo[0];
				if(inspection.getScheduleDate()!=null) {
					//for debug and verify, will remove it later
					//text = text + " [" + dateFormat.format(inspection.getScheduleDate()) + "]";
				} 
				textGroup.setText(text);
				text = inspectionInfo[1];
				textType.setText(text);

				buttonDetails.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
				            return;
				        }
						mLastClickTime = SystemClock.elapsedRealtime();
						ActivityUtils.startInspectionDetailsActivity(activity, inspection, Utils.isInspectionFailed(inspection), AppConstants.CANCEL_INSPECTION_SOURCE_OTHER);
					}
				});

				buttonContact.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
				            return;
				        }
						mLastClickTime = SystemClock.elapsedRealtime();
						AppInstance.getInspectorLoader().showInspectors(activity, permitId, inspection);
					}
				});

			} else {
				//cardHeader.setBackgroundColor(mContext.getResources().getColor(R.color.blue_nearby_header));
				cardContentBack.setBackgroundColor(mContext.getResources().getColor(R.color.blue_nearby_content));
				inspectionTitle.setText(R.string.NEARBY);
				textStatus.setText(R.string.PROJECT);
				buttonDetails.setText(R.string.Directions);
				buttonDetails.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
				            return;
				        }
						mLastClickTime = SystemClock.elapsedRealtime();
						if(nearByProjectList==null || nearByProjectList.size()==0)
							return;
						int index = position - recentInspectionList.size();
						if(index < 0 && index>= nearByProjectList.size()) {
							return;
						}
						ProjectModel project = nearByProjectList.get(index);
						if(project!=null && project.getAddress()!=null){
							String address = Utils.getAddressFullLine(project.getAddress());
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+ address));
							mContext.startActivity(intent);
						}
					}
				});
				buttonContact.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
				            return;
				        }
						mLastClickTime = SystemClock.elapsedRealtime();
						if(nearByProjectList==null || nearByProjectList.size()==0)
							return;
						int index = position - recentInspectionList.size();
						if(index < 0 && index>= nearByProjectList.size()) {
							return;
						}
						ProjectModel project = nearByProjectList.get(index);
						if(project!=null) {
							ActivityUtils.startInspectionContactActivity(activity, project.getProjectId(), null, null, false);
						}
					}
				});
				imageStatus.setImageResource(R.drawable.crnt_loc);
				//show nearby project

				int index = position - recentInspectionList.size();
				ProjectModel project = null;
				if(index >=0 && index < nearByProjectList.size()) {
					project = nearByProjectList.get(index);
				}

				if(project != null) {
					cardContent.setVisibility(View.VISIBLE);
					textAddressLine1.setText(Utils.getAddressLine1AndUnit(project.getAddress()));
					textAddressLine2.setText(Utils.getAddressLine2(project.getAddress()));
					textGroup.setText(R.string.ETA);
					if(project.getDistance() <0) {
						textType.setText(R.string.unknown_mi);
					} else { 
						textType.setText(String.format("%.1f %s", project.getDistance(), mContext.getString(R.string.mile)));
					}
				} else {
					//TextView textLoading = (TextView) loadingContainer.findViewById(R.id.textLoading);
					//textLoading.setText(R.string.loading);
					//cardContent.setVisibility(View.INVISIBLE);
					textAddressLine1.setText("");
					textAddressLine2.setText("");
					textGroup.setText("");
					textType.setText("");

				}
			}
			return view;
		}


		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {

			container.removeView((View) object);
		}

	}

	private void updateListsItem(int changeFlag) {
		switch (changeFlag) {
		case AppConstants.PROJECT_LIST_RECENT_INSPECTION_CHANGE:
			AMLogger.logInfo("project completed list changed ");
			recentInspectionList.clear();
			recentInspectionList.addAll(projectsLoader.getRecentOneMonthInpsections());
			pageControl.setCount(pagerAdapter.getCount());
			udpatePhoneAndTitleBarColor(viewPager.getCurrentItem());
			pagerAdapter.notifyDataSetChanged();
			break;
		case AppConstants.PROJECT_NEARBY_PROJECT_CHANGE:
			AMLogger.logError("Recent project update");
			if(recentInspectionList.size()<MAX_PAGE_COUNT) {
				//don't update if recent inspection >= MAX_PAGE_COUNT
				this.nearByProjectList.clear();
				nearByProjectList.addAll(projectsLoader.getNearByProject());
				pageControl.setCount(pagerAdapter.getCount());
				udpatePhoneAndTitleBarColor(viewPager.getCurrentItem());
				pagerAdapter.notifyDataSetChanged();	
			}
			break;
		default:
		{
			recentInspectionList.clear();
			recentInspectionList.addAll(projectsLoader.getRecentOneMonthInpsections());
			this.nearByProjectList.clear();
			nearByProjectList.addAll(projectsLoader.getNearByProject());
			pageControl.setCount(pagerAdapter.getCount());
			udpatePhoneAndTitleBarColor(viewPager.getCurrentItem());
			pagerAdapter.notifyDataSetChanged();
		}
		break;
		}
		if(projectsLoader.isAllInspectionsDownloaded()) {
			loadingProgress.setVisibility(View.GONE);
		} else {
			loadingProgress.setVisibility(View.VISIBLE);
		}
	}
	
	public void checkLoadingProgress(){
		if(projectsLoader.isAllInspectionsDownloaded()) {
			loadingProgress.setVisibility(View.GONE);
		} else {
			loadingProgress.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		if(observable instanceof ProjectsLoader && data instanceof Integer) {
			int flag = (Integer) data;
			updateListsItem(flag);

		} 		
	}
}

