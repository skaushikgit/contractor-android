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
 *   Created by jzhong on 3/26/15.
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
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.accela.contractorcentral.R;
import com.accela.contractorcentral.AppConstants.AgencyListType;
import com.accela.contractorcentral.service.AgencyLoader;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.framework.model.AgencyModel;
import com.accela.mobile.AMLogger;


public class AgencyListView extends ElasticListView implements Observer {	
 
	public interface OnClickAgencyListener {
		public void onClickAgency(AgencyModel agency);
	}
	
	ListViewAdapterEx adapter;
	ProgressBar mProgressBar;
	List<AgencyModel> listAgency = new ArrayList<AgencyModel>();
	AgencyLoader agencyLoader = AppInstance.getAgencyLoader();
	private AgencyListType agencyListType;
	View headerView;
	OnClickAgencyListener onClickAgencyListener;
	private CountDownTimer countDownTimer;
	private boolean isCheckAndGotoLandingPage = false;
	
	public AgencyListView(Context context) {
		super(context);
	}
	
	public AgencyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public AgencyListView(Context context, AttributeSet attrs,  int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setHeaderTitle(String title, String description) {
		if(headerView == null) {
			headerView = LayoutInflater.from(getContext()).inflate(R.layout.list_header_agency, null);
			
		}
		if(this.mProgressBar == null){
			this.mProgressBar = (ProgressBar) headerView.findViewById(R.id.agencyListViewProgressBarId);
		}
		TextView textTitle = (TextView) headerView.findViewById(R.id.textTitle);
		textTitle.setText(title);
		
		TextView textDesc = (TextView) headerView.findViewById(R.id.textDesc);
		textDesc.setText(description);
	}
	
	public void loadAgency(AgencyListType agencyListType) {
		if(headerView == null) {
			headerView = LayoutInflater.from(getContext()).inflate(R.layout.list_header_agency, null);
		}
		this.addHeaderView(headerView);
		adapter = new ListViewAdapterEx();
		this.setAdapter(adapter);
		
		this.setFadingEdgeLength(0);
		this.setHorizontalFadingEdgeEnabled(false);
		this.setVerticalFadingEdgeEnabled(false);
		this.agencyListType = agencyListType;
		getAgencyData();
		agencyLoader.loadAllAgency(false);
		agencyLoader.loadAllLinkedAgency(false);
		agencyLoader.addObserver(this);
		
		boolean dataLoaded = agencyLoader.isAllLinkedAgenciesDownloaded();
		if(dataLoaded) {
			mProgressBar.setVisibility(View.GONE);
		} else {
			mProgressBar.setVisibility(View.VISIBLE);
		}
		
	
			
		this.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(position <= 0) 
					return;
				position--;
				if(position==listAgency.size()) {
					//add new agency
					ActivityUtils.startAgencyListActivity((Activity) getContext(), AgencyListType.ADDAGENCY);
				} else {
					AgencyModel agency = listAgency.get(position);
					if(onClickAgencyListener!=null) {
						onClickAgencyListener.onClickAgency(agency);
					}
					//ActivityUtils.startAgencyConfigureActivity((Activity) getContext(), agency);
					
				}
			}
			
		});
		
		
	}
	
	public void OnClickAgencyListener(OnClickAgencyListener l) {
		onClickAgencyListener = l;
	}
	
	public void enableCheckAndAutoJumpLandingPage(boolean isCheckAndGotoLandingPage) {
		this.isCheckAndGotoLandingPage = isCheckAndGotoLandingPage;
		//if agency load finish, start the animation.
		if(isCheckAndGotoLandingPage && agencyLoader.isAllLinkedAgenciesDownloaded()) {
			startCheckAgencyAnimation();
		}
	}
	
	@Override
	protected void onAttachedToWindow() {
		agencyLoader.addObserver(this);
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		if(agencyLoader!=null) {
			agencyLoader.deleteObserver(this);
		}
		super.onDetachedFromWindow();
	}
	
	private static class ViewHolder {
		
		TextView textAgency;
		ImageView imageStatus;
		ImageView imageForward;
		ProgressBar progressBar;
	}
	
	private class ListViewAdapterEx extends BaseAdapter{
		
		
		@Override
		public long getItemId(int position) {
			return (long) position;
		}	 
		
		@Override
		public View getView(final int position,View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (null == convertView) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_agency, null);
				viewHolder.textAgency = (TextView) convertView.findViewById(R.id.textAgency);
				viewHolder.imageStatus = (ImageView) convertView.findViewById(R.id.imageStatus);
				viewHolder.imageForward = (ImageView) convertView.findViewById(R.id.imageForward);
				viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.agencyListItemProgressBarId);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			
			// Hide or show the check box.
			setListViewItem(position, viewHolder);
			return convertView;		
		}

		@Override
		public int getCount() {
			int count = listAgency.size();
			if(agencyListType == AgencyListType.MYAGENCY) {
				count++; //show add new agency button
			} else if(count==0 && agencyListType == AgencyListType.LOGIN && agencyLoader.isAllAgenciesDownloaded()) {
				//after login, if no ageny, show the add new agency button.
				count++;
			}
			
			return count;
		}
 
		@Override
		public Object getItem(int position) {
			
			return null;
		}	
		 
		 
	}

	
	private void setListViewItem(final int position, ViewHolder viewHolder) {
		// Set values for view elements.
		if(position >= listAgency.size()) {
			viewHolder.textAgency.setText(R.string.add_new_agency); 
			viewHolder.textAgency.setGravity(Gravity.CENTER);
			
			viewHolder.imageStatus.setVisibility(View.GONE); 
			viewHolder.imageForward.setVisibility(View.GONE);
			
		} else {
			viewHolder.textAgency.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			if(agencyListType == AgencyListType.LOGIN) {
				viewHolder.imageForward.setVisibility(View.INVISIBLE);
			}else{
				viewHolder.imageForward.setVisibility(View.VISIBLE);
			}
			final AgencyModel agency = (AgencyModel)  listAgency.get(position);
			if (listAgency == null) {
				return;
			}
			if( agency.getDisplay() != null &&  agency.getDisplay().length()>0) {
				viewHolder.textAgency.setText( agency.getDisplay()); 
			} else {
				viewHolder.textAgency.setText( agency.getName()); 
			}
			if(this.agencyListType == AgencyListType.MYAGENCY || agency.getAccountId() != null) {
				viewHolder.imageStatus.setVisibility(View.VISIBLE);
			} else { 
				viewHolder.imageStatus.setVisibility(View.INVISIBLE);
			}
			if(agencyListType == AgencyListType.LOGIN) { 
				if(position > indexAnimating) {
					viewHolder.progressBar.setVisibility(View.GONE);
					viewHolder.imageStatus.setVisibility(View.GONE);
				} else if(position == indexAnimating) {
					viewHolder.progressBar.setVisibility(View.VISIBLE);
					viewHolder.imageStatus.setVisibility(View.GONE);
				}else {
					viewHolder.progressBar.setVisibility(View.GONE);
					viewHolder.imageStatus.setVisibility(View.VISIBLE);
				}
			}
		}
		
	}
	
	@Override
	public void update(Observable observable, Object data) {
		AMLogger.logInfo("Update agency list: " +  agencyListType.toString() );
		getAgencyData();
		adapter.notifyDataSetChanged();
		boolean dataLoaded = agencyLoader.isAllLinkedAgenciesDownloaded();
		if(dataLoaded) {
			mProgressBar.setVisibility(View.GONE);
		}

	}
	
	int indexAnimating = -1;
	private void animateAgencyProgress() {
		if(countDownTimer!=null) {
			countDownTimer.cancel();
		}
		
		int totalTime = 3000;
		int count = adapter.getCount();
		if(count == 0) {
			count=1;
		}
		if(count<3)
			totalTime = count*1000;
		countDownTimer = new CountDownTimer(totalTime, totalTime / (count + 1)) {
			public void onTick(long millisUntilFinished) {
				indexAnimating++;
				adapter.notifyDataSetChanged();
			}

			public void onFinish() {
				Activity activity = (Activity) getContext();
				ActivityUtils.startLandingPageActivity(activity);
				activity.finish();
			}
		}.start();
	}
	

	private void getAgencyData() {
		listAgency.clear();
		if(this.agencyListType == AgencyListType.ADDAGENCY || agencyListType == AgencyListType.CHOOSEAGENCY) {
			listAgency.addAll(agencyLoader.getAllAgencies());
		} else {
			listAgency.addAll(agencyLoader.getAllLinkedAgencies());
		}
		if(agencyListType == AgencyListType.LOGIN) {
			startCheckAgencyAnimation();
		}
	}

	private void startCheckAgencyAnimation() {
		if(listAgency.size()>0 && isCheckAndGotoLandingPage) {
			indexAnimating = -1;
			animateAgencyProgress();
		}
	}

}
