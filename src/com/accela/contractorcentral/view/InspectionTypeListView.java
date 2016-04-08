package com.accela.contractorcentral.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.InspectionLoader;
import com.accela.contractorcentral.service.InspectionTypeLoader;
import com.accela.contractorcentral.service.InspectionLoader.RecordInspectionItems;
import com.accela.contractorcentral.utils.Utils;
import com.accela.framework.util.AMUtils;
import com.accela.mobile.AMLogger;
import com.accela.record.model.DailyInspectionTypeModel;
import com.accela.record.model.RecordInspectionModel;


public class InspectionTypeListView extends ElasticListView implements Observer {	
 
	String permitId;
	ListViewAdapterEx adapter;
	

	private int itemMargin;
	private int itemPadding;
	private int indexSelected = -1;
	private OnItemClickListener listener;
	private InspectionTypeLoader inspectionTypeLoader;
	private InspectionLoader inspectionLoader = AppInstance.getInpsectionLoader();
	private DailyInspectionTypeModel inspectionType;
	private RecordInspectionModel currentRecordInspectionModel;
	private RecordInspectionModel canceledInspection;
	private boolean showApprovedFailedInspection;
	private View loadingRefreshView;
	private View addressHeaderView;
	private HashMap<String, InspectionTypeLoader> inspectionTypeLoaderCache = new HashMap<String, InspectionTypeLoader>();
	private int animationDirection;
	
	private List<DailyInspectionTypeModel> 	listAvailableInspection = new ArrayList<DailyInspectionTypeModel>() ;
	private List<RecordInspectionModel> 	listApprovedInspection	= new ArrayList<RecordInspectionModel>();
	private List<RecordInspectionModel> 	listFailedInspection	= new ArrayList<RecordInspectionModel>();	
	private List<RecordInspectionModel> 	listScheduledInspection	= new ArrayList<RecordInspectionModel>();
	private boolean focusItemVisiblity;
	private boolean isHashHeader;
	public InspectionTypeListView(Context context) {
		super(context);
		init();
	}
	
	public InspectionTypeListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public InspectionTypeListView(Context context, AttributeSet attrs,  int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public void showApprovedFailedInspection(boolean show) {
		showApprovedFailedInspection = show;
		if(adapter!=null) {
			adapter.notifyDataSetChanged();
		}
	}
	/**
	 * 
	 * @param permitId
	 * @param animationDirection -1: move to left. 1: move to right if it is downloaded when initial, 0: no animation
	 */
	public void setPermit(String permitId, int animationDirection, RecordInspectionModel canceledInspection) {
		this.canceledInspection = canceledInspection;
		this.animationDirection = animationDirection;
		if(this.permitId==null || this.permitId.compareTo(permitId) != 0) {
			this.permitId = permitId;
			if(inspectionTypeLoader!=null) {
				inspectionTypeLoader.deleteObserver(this);
			}
			inspectionTypeLoader = inspectionTypeLoaderCache.get(permitId);
			if(inspectionTypeLoader==null) {
				AMLogger.logInfo("InspectionTypeLoader isn't exist, create a new one", permitId);
				inspectionTypeLoader = new InspectionTypeLoader(permitId);
				inspectionTypeLoaderCache.put(permitId, inspectionTypeLoader);
			} else {
				AMLogger.logInfo("InspectionTypeLoader exist, reuse it", permitId);
			}
			inspectionTypeLoader.addObserver(this);
		}
		//initial with existing data
		listAvailableInspection.clear();
		listAvailableInspection.addAll(inspectionTypeLoader.getInspectionType());
		setInspectionList();
		//
		loadInspectionType();
		if(adapter!=null) {
			adapter.notifyDataSetChanged();
		}
		if(canceledInspection!=null){
			int position = 0;
			for(; position<listScheduledInspection.size(); position++){
				if(listScheduledInspection==null || listScheduledInspection.get(position)==null || this.canceledInspection.getId()==null)
					continue;
				if(listScheduledInspection.get(position).getId().equals(this.canceledInspection.getId())){
					position += listApprovedInspection.size();
					position += listFailedInspection.size();
					this.smoothScrollToPositionFromTop(position-2, 0, 100);
					break;
				}
			}
		}
	}
	
	public DailyInspectionTypeModel getInspectionType() {
		return inspectionType;
	}

	private void loadInspectionType() {
		//show loading view
		loadingRefreshView.setVisibility(View.VISIBLE);
		Button buttonRefresh = (Button) loadingRefreshView.findViewById(R.id.buttonRefresh);
		buttonRefresh.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadInspectionType();
				
			}
		});

		View loadingContainer = (View) loadingRefreshView.findViewById(R.id.loadingContainer);
		if(inspectionTypeLoader.getDownloadFlag() == AppConstants.FLAG_FULL_DOWNLOAED) {
			loadingContainer.setVisibility(View.GONE);
			this.clearAnimation();
			Animation animation = null;
			if(animationDirection == -1) {
				animation = AnimationUtils.loadAnimation(getContext(), R.anim.movein_right); 
			} else if(animationDirection == 1) {
				animation = AnimationUtils.loadAnimation(getContext(), R.anim.movein_left); 
			}
			if(animation!=null) {
				this.startAnimation(animation);
			}
			
		} else {
			loadingContainer.setVisibility(View.VISIBLE);
		}
		View refreshContainer = (View) loadingRefreshView.findViewById(R.id.refreshContainer);
		refreshContainer.setVisibility(View.GONE);
		TextView textView = (TextView) loadingRefreshView.findViewById(R.id.textLoading);
		textView.setText(R.string.loading_inspection_type);
		//load inspection type
		inspectionTypeLoader.loadInpsectionType(false);
		inspectionLoader.loadInspectionByRecord(permitId, true);
	}
	
	public void setFocusItemVisibility(boolean focusItemVisiblity) {
		this.focusItemVisiblity = focusItemVisiblity;
	}
	
	@Override
	protected void onAttachedToWindow() {
		inspectionTypeLoader.addObserver(this);
		inspectionLoader.addObserver(this);
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		if(inspectionTypeLoader != null) {
			inspectionTypeLoader.deleteObserver(this);
		}
		inspectionLoader.deleteObserver(this);
		super.onDetachedFromWindow();
	}
	
	public void showAddress(boolean show) {
		addressHeaderView.setVisibility(show? View.VISIBLE:View.GONE);
		isHashHeader = show;
		if(show) {
			ProjectModel project = AppInstance.getProjectsLoader().getParentProject(permitId);
			TextView textAddLine1 = (TextView) addressHeaderView.findViewById(R.id.textAddLine1);
			textAddLine1.setText(Utils.getAddressLine1AndUnit(project.getAddress()));
			
			TextView textAddLine2 = (TextView) addressHeaderView.findViewById(R.id.textAddLine2);
			textAddLine2.setText(Utils.getAddressLine2(project.getAddress()));
		} else {
			this.removeHeaderView(addressHeaderView);
		}
	}
	
	protected void init() {
		if(loadingRefreshView == null) {
			loadingRefreshView = LayoutInflater.from(getContext()).inflate(R.layout.loading_and_refresh, null);
			this.addFooterView(loadingRefreshView);
		}
		if(addressHeaderView == null) {
			addressHeaderView = LayoutInflater.from(getContext()).inflate(R.layout.list_header_address, null);
			addressHeaderView.setVisibility(View.GONE);
			this.addHeaderView(addressHeaderView);
		}
		adapter = new ListViewAdapterEx();
		this.setAdapter(adapter);
		float density = this.getContext().getResources().getDisplayMetrics().density;
		itemMargin = (int) (density * 8); // 8dp
		itemPadding = (int) (density* 8);
		
		super.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(isHashHeader){
					if(position == 0) {
						return;
					}
					position -= 1;
				}
				onClickItem(position);
				if(listener != null) {
					listener.onItemClick(parent, view, position, id);
				}
			}	
		});
		
	}
	
	public RecordInspectionModel getRecordInspectionModel(){
		return this.currentRecordInspectionModel;
	}
	
	protected void onClickItem(int position) {
		
		
		
		int groupApproved = 0;
		int groupFailed = 0;
		int groupScheduled = 0;
		int groupAvailable = 0;
		int pos = position;
		
		
		
		
		if(showApprovedFailedInspection) {
			groupScheduled = listScheduledInspection.size();
			groupApproved = listApprovedInspection.size();
			groupFailed = listFailedInspection.size();
		}
		
		groupAvailable = this.listAvailableInspection.size();
		
		boolean pick = false;
		//pick approved inspection
		if(pos < groupApproved) {
			currentRecordInspectionModel = listApprovedInspection.get(pos);
			pick = true;
		} else {
			pos -= groupApproved;
		}
		
		//pick failed inspection
		if(!pick && pos < groupFailed) {
			currentRecordInspectionModel = listFailedInspection.get(pos);
			pick = true;
		} else {
			pos -= groupFailed;
		}

		//pick scheduled inspection
		if(!pick && pos < groupScheduled) {
			currentRecordInspectionModel = listScheduledInspection.get(pos);
			pick = true;
		} else {
			pos -= groupScheduled;
		}
		
		//pick available inspection
		if(!pick && pos < groupAvailable) {
			DailyInspectionTypeModel model = this.listAvailableInspection.get(pos);
			inspectionType = model;
			currentRecordInspectionModel = null;
			pick = true;
		} else {
			pos -= groupAvailable;
			inspectionType = null;
		}
		
		if(pick) {
			indexSelected = position;
			adapter.notifyDataSetChanged();
		}else{
			currentRecordInspectionModel = null;
		}
		
	}
	
	@Override
	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
		//super.setOnItemClickListener(listener);
	}
	
	private static class ViewHolder {
		ImageView imageInfo;
		TextView textInspectionCode;
		TextView textInspectionType;
		TextView textMoreInfo;
		LinearLayout itemContainer;
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
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_inspection_type, null);
				viewHolder.textInspectionCode = (TextView) convertView.findViewById(R.id.textInspectionCode);
				viewHolder.textInspectionType = (TextView) convertView.findViewById(R.id.textInspectionType);
				viewHolder.textMoreInfo = (TextView) convertView.findViewById(R.id.textMoreInfo);
				viewHolder.imageInfo = (ImageView) convertView.findViewById(R.id.imageInfo);
				viewHolder.itemContainer = (LinearLayout) convertView.findViewById(R.id.itemContainer);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			setListViewItem(position, viewHolder, convertView);
			

			
			return convertView;		
		}
		
		@Override
		public int getCount() {
			int totalCount = 0;
			
			totalCount = listAvailableInspection.size();
			
			
			if(showApprovedFailedInspection) {
				totalCount += listScheduledInspection.size();
				totalCount += listApprovedInspection.size();
				totalCount += listFailedInspection.size();
			} 
			
			
			return totalCount;
			
		}
 
		@Override
		public Object getItem(int position) {
			return null;
		}	
	}
	
	private void removeCancledInspection(final View view, RecordInspectionModel inspection){
		if(canceledInspection==null || inspection==null)
			return;
		if(inspection.getId().equals(canceledInspection.getId())){
			new Handler().postDelayed(new Runnable() {
			      @Override
			      public void run() {
						final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.moveout_left);
						view.startAnimation(animation);
					try {
						for (int i = 0; i < listScheduledInspection.size(); i++) {
							if (listScheduledInspection.get(i) != null && listScheduledInspection.get(i).getId() != null
									&& canceledInspection.getId() != null && listScheduledInspection.get(i).getId().equals(canceledInspection.getId())) {
								listScheduledInspection.remove(i);
								adapter.notifyDataSetChanged();
								break;
							}
						}
					} catch (RuntimeException e) {
						AMLogger.logError(e.toString());
					}
					if(canceledInspection==null || permitId==null)
						return;
					AppInstance.getInpsectionLoader().removeInspection(permitId, canceledInspection);
					canceledInspection = null;
			      }
			}, 1000);
		}
	}

	private void setListViewItem(final int position, ViewHolder viewHolder, View convertView) {
		// Set values for view elements.
		//setListViewItem(position, viewHolder);
		int groupApproved = 0;
		int groupFailed = 0;
		int groupScheduled = 0;
		int groupAvailable = 0;
		int textColor = Color.WHITE;
		int itemBackId = 0;
		int idxSelected = this.indexSelected;
		int pos = position;
		boolean itemSet = false;
		
		
		
		if(showApprovedFailedInspection) {
			groupScheduled = listScheduledInspection.size();
			groupApproved = listApprovedInspection.size();
			groupFailed = listFailedInspection.size();
		}
		groupAvailable = listAvailableInspection.size();
		 
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) viewHolder.itemContainer.getLayoutParams();
		if(lp == null) {
			lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		}
		lp.setMargins(itemMargin, itemMargin, itemMargin, itemMargin);
		
		//approved inspection
		if(!itemSet && pos >=0 && pos < groupApproved) { 
			//set the margin for completed inspection.
			if(pos ==0 ) {
				lp.topMargin = itemMargin;
				lp.bottomMargin = 0;
				itemBackId = R.drawable.card_background_white_thin_bottom;
			} else if(pos < groupApproved-1) {
				lp.topMargin = 1;
				lp.bottomMargin = 0;
				itemBackId = R.drawable.card_background_white_thin_bottom;
			} else if(pos == groupApproved-1) {
				lp.topMargin = 1;
				lp.bottomMargin = itemMargin;
				itemBackId = R.drawable.card_background_white;
			}
			//set the icon and background
			viewHolder.imageInfo.setImageResource(R.drawable.okay);
			viewHolder.textMoreInfo.setVisibility(View.VISIBLE);
			
			if(pos == idxSelected && focusItemVisiblity) {
				itemBackId = R.drawable.card_background_blue_thin_bottom;
				textColor = Color.WHITE;
			} else {
				textColor = Color.BLACK;
			}
			viewHolder.itemContainer.setBackgroundResource(itemBackId);
			
			RecordInspectionModel model = listApprovedInspection.get(pos);
			String info[] = Utils.formatInspectionInfo(model);
			if(model!=null) {
				viewHolder.textInspectionCode.setText(info[0]);
				viewHolder.textInspectionType.setText(info[1]);
				viewHolder.textMoreInfo.setText(model.getStatus_text());// + " ID: " + model.getId());
			}
			itemSet = true;
		} else {
			pos -= groupApproved;
			idxSelected -= groupApproved;
		}
		
		
		//failed inspection
		if(!itemSet && pos >= 0 && pos < groupFailed) { 
			//set the margin for completed inspection.
			if(pos ==0 ) {
				lp.topMargin = itemMargin;
				lp.bottomMargin = 0;
				itemBackId = R.drawable.card_background_red_thin_bottom;
			} else if(pos < groupFailed-1) {
				lp.topMargin = 1;
				lp.bottomMargin = 0;
				itemBackId = R.drawable.card_background_red_thin_bottom;
			} else if(pos == groupFailed-1) {
				lp.topMargin = 1;
				lp.bottomMargin = itemMargin;
				itemBackId = R.drawable.card_background_red;
			}
			//set the icon and background
			viewHolder.imageInfo.setImageResource(R.drawable.white_x);
			viewHolder.textMoreInfo.setVisibility(View.VISIBLE);
			textColor = Color.WHITE;
			if(pos == idxSelected && focusItemVisiblity) {
				itemBackId = R.drawable.card_background_blue_thin_bottom;
			} 
			viewHolder.itemContainer.setBackgroundResource(itemBackId);
			
			RecordInspectionModel model = listFailedInspection.get(pos);
			String info[] = Utils.formatInspectionInfo(model);
			if(model!=null) {
				viewHolder.textInspectionCode.setText(info[0]);
				viewHolder.textInspectionType.setText(info[1]);
				viewHolder.textMoreInfo.setText(model.getStatus_text());// + " ID: " + model.getId());
			}
			itemSet = true;
		} else {
			pos -= groupFailed;
			idxSelected -= groupFailed;
		}
		
		if(!itemSet && pos >= 0 && pos < groupScheduled) {
			//set the margin for scheduled inspection
			if(pos ==0) {
				lp.topMargin = itemMargin;
				lp.bottomMargin = 0;
				itemBackId = R.drawable.card_background_white_thin_bottom;
			} else if(pos < groupScheduled - 1) {
				lp.topMargin = 0;
				lp.bottomMargin = 0;
				itemBackId = R.drawable.card_background_white_thin_bottom;
			} else if(pos == groupScheduled - 1) {
				lp.topMargin = 0;
				lp.bottomMargin = itemMargin;
				itemBackId = R.drawable.card_background_white;
			} 
			//set the icon and background
			viewHolder.imageInfo.setImageResource(R.drawable.scheduled);
			//viewHolder.itemContainer.setBackgroundColor(this.getResources().getColor(R.color.white));
			
			
			viewHolder.textMoreInfo.setVisibility(View.VISIBLE);
			if(pos == idxSelected && focusItemVisiblity) {
				itemBackId = R.drawable.card_background_blue_thin_bottom;
				textColor = Color.WHITE;
			} else {
				textColor = Color.BLACK;
			}
			viewHolder.itemContainer.setBackgroundResource(itemBackId);
			
			RecordInspectionModel model = listScheduledInspection.get(pos);
			String info[] = Utils.formatInspectionInfo(model);
			if(model!=null) {
				viewHolder.textInspectionCode.setText(info[0]);
				viewHolder.textInspectionType.setText(info[1]);
				viewHolder.itemContainer.setBackgroundResource(itemBackId);
				viewHolder.textMoreInfo.setText(model.getStatus_text());// + " ID: " + model.getId());
			}
			itemSet = true;
			removeCancledInspection(convertView, model);
		} else {
			pos -= groupScheduled;
			idxSelected -= groupScheduled;
		}
		 
		
		if(!itemSet && pos >=0 && pos < groupAvailable) {
			//set the margin for available inspection
			if(pos ==0) {
				lp.topMargin = itemMargin;
				lp.bottomMargin = 0;
				itemBackId = R.drawable.card_background_white_thin_bottom;
			} else if(pos < groupAvailable - 1) {
				lp.topMargin = 0;
				lp.bottomMargin = 0;
				itemBackId = R.drawable.card_background_white_thin_bottom;
			} else if(pos == groupAvailable - 1) {
				lp.topMargin = 0;
				lp.bottomMargin = itemMargin;
				itemBackId = R.drawable.card_background_white;
			}
			 
			if(pos == idxSelected && focusItemVisiblity) {
				viewHolder.imageInfo.setImageResource(R.drawable.openschedule_selected);
				itemBackId = R.drawable.card_background_blue_thin_bottom;
				textColor = Color.WHITE;
			} else {
				viewHolder.imageInfo.setImageResource(R.drawable.openschedule);
				textColor = Color.BLACK;
			}
			viewHolder.itemContainer.setBackgroundResource(itemBackId);
			 
			viewHolder.textMoreInfo.setVisibility(View.GONE);
			 
			//set the open schedule inspection type
			DailyInspectionTypeModel model = this.listAvailableInspection.get(pos);
			
			String info[] = Utils.formatInspectionTypeInfo(model, permitId);
			if(model!=null) {
				viewHolder.textInspectionCode.setText(info[0]);
				viewHolder.textInspectionType.setText(info[1]);
			}
			
			itemSet = true;
		} 
		
		viewHolder.itemContainer.setPadding(itemPadding*2, itemPadding, itemPadding*2, itemPadding);
		viewHolder.itemContainer.setLayoutParams(lp);
		
		viewHolder.textInspectionCode.setTextColor(textColor);
		viewHolder.textInspectionType.setTextColor(textColor);
		viewHolder.textMoreInfo.setTextColor(textColor);
		 
	}
	
	private void setInspectionList() {
		RecordInspectionItems item = inspectionLoader.getInspectionByRecord(permitId);
		this.listApprovedInspection.clear();
		this.listFailedInspection.clear();
		this.listScheduledInspection.clear();
		if(item!=null) {
			this.listApprovedInspection.addAll(item.listApprovedInspection);
			this.listFailedInspection.addAll(item.listFailedInspection);
			this.listScheduledInspection.addAll(item.listScheduledInspection);
			sortInspectionList(listApprovedInspection);
			sortInspectionList(listFailedInspection);
			sortInspectionList(listScheduledInspection);
			filterOutAvailableInspection();
		}
	}
	
	private void sortInspectionList(List<RecordInspectionModel> list) {
		Collections.sort(list, new Comparator<RecordInspectionModel>() {

			@Override
			public int compare(RecordInspectionModel m1,
					RecordInspectionModel m2) {
				String info1[] = Utils.formatInspectionInfo(m1);
				String info2[] = Utils.formatInspectionInfo(m2);
				return info1[1].compareToIgnoreCase(info2[1]);
			}
			
		});
	}
	
	private void filterOutAvailableInspection() {
		//filter out available inspection from failed inspection
		DailyInspectionTypeModel inspectionType;
		for(int i = listAvailableInspection.size() - 1; i>=0; i--) {
			inspectionType = listAvailableInspection.get(i);
			//AMLogger.logInfo("Available inspection type: %d", inspectionType.getId());
			for(RecordInspectionModel inspection: this.listFailedInspection) {
				if(inspection.getType() != null && inspectionType.getId().equals(inspection.getType().getId())) {
					listAvailableInspection.remove(i);
					String info[] = Utils.formatInspectionTypeInfo(inspectionType, permitId);
					AMLogger.logInfo("Inspection type is filter out:" + info.toString());
					break;
				}
			}
		}
	}
	 
	@Override
	public void update(Observable observable, Object data) {
		if(observable instanceof InspectionLoader) {
			if((data instanceof String) && ((String) data).equals(permitId)) {
				setInspectionList();
				adapter.notifyDataSetChanged();
			}
		} else if(observable instanceof InspectionTypeLoader){
		
			if(inspectionTypeLoader.getDownloadFlag() == AppConstants.FLAG_DOWNLOADED_FAILED ) {
				View refreshContainer = (View) loadingRefreshView.findViewById(R.id.refreshContainer);
				refreshContainer.setVisibility(View.VISIBLE);
				
				View loadingContainer = (View) loadingRefreshView.findViewById(R.id.loadingContainer);
				loadingContainer.setVisibility(View.GONE);
				
				//show error message
				TextView tvMessage = (TextView) loadingRefreshView.findViewById(R.id.textMessage);
				if(!AMUtils.isNetworkConnected(getContext())) {
					tvMessage.setText(R.string.no_internet_connection);
				} else {
					tvMessage.setText(R.string.failed_to_load_inspection_type);
				}
			} else if (inspectionTypeLoader.getDownloadFlag() == AppConstants.FLAG_FULL_DOWNLOAED ) {
				if(inspectionTypeLoader.getInspectionType().size()==0) {
					View refreshContainer = (View) loadingRefreshView.findViewById(R.id.refreshContainer);
					refreshContainer.setVisibility(View.VISIBLE);
					
					View loadingContainer = (View) loadingRefreshView.findViewById(R.id.loadingContainer);
					loadingContainer.setVisibility(View.GONE);
					
					//show error message
					TextView tvMessage = (TextView) loadingRefreshView.findViewById(R.id.textMessage);
					tvMessage.setText(R.string.no_available_inspection_type);
				} else {
					//this.removeFooterView(loadingRefreshView);
					//loadingRefreshView = null;
					loadingRefreshView.setVisibility(View.GONE);
				}
			}  
			
			this.listAvailableInspection.clear();
			this.listAvailableInspection.addAll(inspectionTypeLoader.getInspectionType());
			filterOutAvailableInspection();
			adapter.notifyDataSetChanged();
		}
		
	}
}
