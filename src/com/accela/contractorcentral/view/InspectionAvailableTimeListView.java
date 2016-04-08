package com.accela.contractorcentral.view;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.service.InspectionTimesLoader;
import com.accela.contractorcentral.service.InspectionTimesLoader.InspectionDateItem;
import com.accela.contractorcentral.utils.Utils;
import com.accela.framework.util.AMUtils;
import com.accela.inspection.model.InspectionAvailableDatesModel;
import com.accela.inspection.model.InspectionTimesModel;
import com.accela.mobile.AMLogger;
import com.accela.mobile.util.DateUtil;
import com.accela.record.model.DailyInspectionTypeModel;


public class InspectionAvailableTimeListView extends LinearLayout implements Observer {	
 
	
	public interface OnInpsectionTimeListViewListener {
		public void OnViewMoreTimes();
		public void OnSelectInspectionTime(InspectionTimesModel model);
		
	}
	
	protected String permitId;
	protected ListViewAdapterEx adapter;
	protected List<InspectionAvailableDatesModel> listAvailableTime = new ArrayList<InspectionAvailableDatesModel>();

	
	private int indexSelected = -1;
	private OnItemClickListener listener;
	
	int maximalDisplayItems;
	

	private Context mContext;
	
	protected ElasticListView listView;
	protected View viewMoreTime;
	protected View headerView;
	
	private View loadingRefreshView;
	float density;
	
	
	//when groupByDate == true, the calendar must be shown
	protected boolean isListViewExpanded = false;
	
	protected AMCalendarView calendarView;
	protected View inspectionContainer;
	protected InspectionTimesLoader inspectionTimesLoader;
	
	protected int selectedYear;
	protected int selectedMonth;
	
	protected OnInpsectionTimeListViewListener onInpsectionTimeListViewListener;
	
	public InspectionAvailableTimeListView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public InspectionAvailableTimeListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public InspectionAvailableTimeListView(Context context, AttributeSet attrs,  int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}
	
	public void setOnInpsectionTimeListViewListener(OnInpsectionTimeListViewListener l) {
		onInpsectionTimeListViewListener = l;
	}
	
	public void setInspectionType(String projectId, String recordId, long inspectionId, DailyInspectionTypeModel inspectionTypeModel) {
		AMLogger.logInfo("InspectionAvailableTimeListView.setInspectionType - " + inspectionTypeModel.getId());
    	inspectionTimesLoader = new InspectionTimesLoader(recordId, inspectionId, inspectionTypeModel.getId().toString());
    	inspectionTimesLoader.addObserver(this);
    	//load the inspection of this month
    	Calendar todayDate = Calendar.getInstance();
    	todayDate.setTimeInMillis(System.currentTimeMillis());
    	selectedYear = todayDate.get(Calendar.YEAR);
    	selectedMonth = todayDate.get(Calendar.MONTH);
    	loadAavaileTime(selectedYear, selectedMonth, false);
    }
	
	public void setMaximalDisplayItems(int maximalDisplayItems) {
		this.maximalDisplayItems = maximalDisplayItems;
	}
	
	public void addHeaderView(View headerView) {
		this.headerView = headerView;
		if(calendarView == null) {
			calendarView = new AMCalendarView(mContext);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					(int) (250 * density));
			this.addView(calendarView, lp);
			
			calendarView.setOnDateChangeListener(new AMCalendarView.OnDateChangeListener() {
				
				@Override
				public void onSelectedDayChange(AMCalendarView view, int year, int month,
						int dayOfMonth) {
					if(year!= selectedYear || month != selectedMonth) {
						selectedYear = year;
						selectedMonth = month;
//						loadAavaileTime(year, month, true); //hard coded now for get 31 days
					} else {
						if(calendarView.selectAvailableDay(year, month, dayOfMonth)) {
							scrollToListItem(year, month, dayOfMonth);
						}
					}
				}

				@Override
				public void onFocusedMonthChange(AMCalendarView view,
						int newYear, int newMonth) {
					if(newYear!= selectedYear || newMonth != selectedMonth) {
						selectedYear = newYear;
						selectedMonth = newMonth;
//						loadAavaileTime(newYear, newMonth, true); //hard coded now for get 31 days
					} else {
						calendarView.selectFirstAvailableDay();
					}
				}
			});
		}
		calendarView.setVisibility(View.GONE);
		inspectionContainer = (View) headerView.findViewById(R.id.inspectionContainer);
		buildListView(false);
		
	}
	
	public void removeHeaderView(View headerView) {
		listView.removeHeaderView(headerView);
	}
	
	public boolean isListViewExpanded() {
		return isListViewExpanded;
	}
	
	public void setListViewExpanded(boolean expanded) {
		isListViewExpanded = expanded;
		calendarView.setVisibility(isListViewExpanded?View.VISIBLE:View.GONE);
		viewMoreTime.setVisibility(isListViewExpanded?View.GONE:View.VISIBLE);
		inspectionContainer.setVisibility(isListViewExpanded?View.GONE:View.VISIBLE);
		
		calendarView.clearAnimation();
		Animation animation = AnimationUtils.loadAnimation(getContext(), isListViewExpanded ? R.anim.fadein:R.anim.fadeout);
		calendarView.startAnimation(animation);
		

		buildListView(true);
		//adapter.notifyDataSetChanged();
	}
	
	protected void loadAavaileTime(int year, int month, boolean animated) {

		InspectionDateItem item = inspectionTimesLoader.getInspectionDatesByMonth(year , month);
		//refresh the UI by current selected year and month
		refreshView(animated);
		AMLogger.logInfo("Load vailable times: %d / %d", year, month);
		//if not download completely, continue download it. 
		if(item == null || item.downloadFlag != AppConstants.FLAG_FULL_DOWNLOAED) {
			//show loading view
			
			loadingRefreshView.setVisibility(View.VISIBLE);
			View loadingContainer = (View) loadingRefreshView.findViewById(R.id.loadingContainer);
			loadingContainer.setVisibility(View.VISIBLE);
		
			View refreshContainer = (View) loadingRefreshView.findViewById(R.id.refreshContainer);
			refreshContainer.setVisibility(View.GONE);
			TextView textView = (TextView) loadingRefreshView.findViewById(R.id.textLoading);
			textView.setText(R.string.loading_inspection_time);
			//load the available inspection time by year and month
			inspectionTimesLoader.loadInspectionDateByMonth(year, month);
			
		}
		
	}
	
	protected void scrollToListItem(int year, int month, int dayOfMonth) {

		int position = 0;
		int lastPosition = 0;
		Calendar cal2 = Calendar.getInstance();
		cal2.set(Calendar.YEAR, year);
		cal2.set(Calendar.MONTH, month);
		cal2.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		
		Calendar cal = Calendar.getInstance();
		for(InspectionAvailableDatesModel model: listAvailableTime) {
			Date date = model.getDate();
			cal.setTimeInMillis(date.getTime());
			if(cal.getTimeInMillis() > cal2.getTimeInMillis()) {
				break;
			}

			lastPosition = position;
			position += model.getTimes() != null? model.getTimes().size() : 0;
		}
		listView.smoothScrollToPositionFromTop(lastPosition + 1, 0);
		//listView.smoothScrollToPosition(lastPosition);	
	}
	
	protected void refreshView(boolean animated) {
		InspectionDateItem item = inspectionTimesLoader.getInspectionDatesByMonth(selectedYear, selectedMonth);
		listAvailableTime.clear();
		if(item != null) {
			listAvailableTime.addAll(item.listInspectionAvailableDates);
		}
		updateCalendarByAvailableTime(selectedYear, selectedMonth);
		calendarView.selectFirstAvailableDay();
		updateLoadingRefreshUI(true);
		if(animated) {
			buildListView(true);
		} else if(adapter!=null) {
			adapter.notifyDataSetChanged();
		}
	}
	
	void updateLoadingRefreshUI(boolean successful) {
		if(adapter.getCount() == 0 ) {
			//show refresh button if no available time
			View refreshContainer = (View) loadingRefreshView.findViewById(R.id.refreshContainer);
			refreshContainer.setVisibility(View.VISIBLE);
			
			View loadingContainer = (View) loadingRefreshView.findViewById(R.id.loadingContainer);
			loadingContainer.setVisibility(View.GONE);
			
			//show error message
			TextView tvMessage = (TextView) loadingRefreshView.findViewById(R.id.textMessage);
			if(!AMUtils.isNetworkConnected(getContext())) {
				tvMessage.setText(R.string.no_internet_connection);
			} else {
				tvMessage.setText(successful? R.string.no_available_inspection_time:
					R.string.failed_to_load_inspection_available_time);
			}
		}  else {
			loadingRefreshView.setVisibility(View.GONE);
		}
		 
		//check if need to show view More time (need to revisit here later)
		viewMoreTime.setVisibility(isListViewExpanded? View.GONE: View.VISIBLE);
		
	}
	

	
	protected void updateCalendarByAvailableTime(int year, int month) {
		calendarView.unmarkAvailableDate(year, month);
		for(InspectionAvailableDatesModel model: listAvailableTime) {
			if(model.getTimes()!=null) {
				
				calendarView.markAvaiableDate(model.getDate(), true);
			}
		}
		calendarView.refreshAllViews();
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}
	

	protected void buildListView(boolean animated) {
		if(listView!=null) {
			listView.clearAnimation();
			if(headerView!=null) {
				listView.removeHeaderView(headerView);
			}
			if(animated) {
				Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.fadeout);
				listView.startAnimation(animation);
			}
			removeView(listView);
		}
		adapter = new ListViewAdapterEx();
		listView = new ElasticListView(getContext());
		if(headerView != null) {
			listView.addHeaderView(headerView);
		}
		View footerView = LayoutInflater.from(getContext()).inflate(R.layout.list_footer_view_more_time, null);
		loadingRefreshView =footerView.findViewById(R.id.loadingRefreshView);

		
		loadingRefreshView.setVisibility(View.GONE);
		Button buttonRefresh = (Button) loadingRefreshView.findViewById(R.id.buttonRefresh);
		buttonRefresh.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//loadAavaileTime();
			}
		});
		
		viewMoreTime = footerView.findViewById(R.id.buttonMoreTimes);
		
		listView.addFooterView(footerView);
		viewMoreTime.setVisibility(isListViewExpanded?View.GONE:View.VISIBLE);
		
		viewMoreTime.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(onInpsectionTimeListViewListener!=null) {
					onInpsectionTimeListViewListener.OnViewMoreTimes();
				}
				setListViewExpanded(true);
			}
		});
		listView.setAdapter(adapter);
		listView.setDivider(null);
		listView.setDividerHeight(0);
		LinearLayout.LayoutParams lp  = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		//listView.setLayoutParams(lp);
		this.addView(listView, lp);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				if(listener != null) {
					listener.onItemClick(parent, view, position, id);
				}
				if(position == 0) {
					//header view is first item.ignore it
					return;
				}
				InspectionTimesModel model = getAvailableTimeByPosition(position - 1);
				if(model!=null && onInpsectionTimeListViewListener!=null) {
					onInpsectionTimeListViewListener.OnSelectInspectionTime(model);
				}
			}	
		});
		listView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		listView.setMaxOverScrollDistance(0,100);
		if(inspectionContainer != null) {
			inspectionContainer.setVisibility(isListViewExpanded?View.GONE:View.VISIBLE);
		}
		
		if(animated) {
			Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fadein);
			listView.startAnimation(fadeIn);
		}
		
		
	/*	listView.setCallbacks(new ElasticListView.ScrollCallbacks() {
			
			@Override
			public void onScrollChanged(int l, int t, int oldl, int oldt) {
				if(t <= 0) {
					calendarView.setY(-t);
				} else {
					calendarView.setY(0);
				}
			}
		}); */
	}
	
	private InspectionTimesModel getAvailableTimeByPosition(int position){
		InspectionTimesModel modelTime = null;
		InspectionAvailableDatesModel modelDate = null;
		for(InspectionAvailableDatesModel model: listAvailableTime) {
			if(model.getTimes()!=null) {
				if(position >= model.getTimes().size()) {
					position -= model.getTimes().size();
				} else {
					modelDate = model; 
					modelTime = model.getTimes().get(position);
					modelTime.setCurrentDate(model.getDate());
					break;
				}
			}
		}
		return modelTime;	
			
	}
	
	
	protected void init() {
		density = this.getContext().getResources().getDisplayMetrics().density;
		buildListView(false);
				
	}
	
	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
		//super.setOnItemClickListener(listener);
	}
	
	private static class ViewHolder {
		TextView textDateGroup;
		TextView textDate;
		TextView textTime;
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
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_available_time, null);
				viewHolder.textDateGroup = (TextView) convertView.findViewById(R.id.textDateGroup);
				viewHolder.textDate = (TextView) convertView.findViewById(R.id.textDate);
				viewHolder.textTime = (TextView) convertView.findViewById(R.id.textTime);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			setListViewItem(position, viewHolder);
			
			return convertView;		
		}

		@Override
		public int getCount() {
			int totalCount = 0;
			for(InspectionAvailableDatesModel model: listAvailableTime) {
				if(model.getTimes()!=null) {
					totalCount += model.getTimes().size();
				}
			}
			if(!isListViewExpanded) {
				totalCount = totalCount > maximalDisplayItems? maximalDisplayItems: totalCount;
			}

			return totalCount;
			
		}
 
		@Override
		public Object getItem(int position) {
			return null;
		}	
		
		
	}

	private void setListViewItem(int position, ViewHolder viewHolder) {
		// Set values for list view item.
		InspectionTimesModel modelTime = null;
		InspectionAvailableDatesModel modelDate = null;
		boolean showDayGroup = false;
		for(InspectionAvailableDatesModel model: listAvailableTime) {
			if(model.getTimes()!=null) {
				if(position >= model.getTimes().size()) {
					position -= model.getTimes().size();
				} else {
					if(position == 0 && isListViewExpanded) {
						showDayGroup = true;
					}
					modelDate = model; 
					modelTime = model.getTimes().get(position);
					break;
				}
			}
		}
		

		SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, MM/dd/yyyy", Locale.getDefault());
		if(showDayGroup) {
			
			viewHolder.textDateGroup.setVisibility(View.VISIBLE);
			if(modelDate!=null) {
				viewHolder.textDateGroup.setText(dayFormat.format(modelDate.getDate()));
			} 
			
		} else {
			viewHolder.textDateGroup.setVisibility(View.GONE);
		}
		 
		if(isListViewExpanded) {
			viewHolder.textDate.setVisibility(View.GONE);
			viewHolder.textTime.setTextColor(Color.BLACK);
		} else {
			viewHolder.textDate.setVisibility(View.VISIBLE);
		}
		
		if(modelDate!=null) {
			viewHolder.textDate.setText(dayFormat.format(modelDate.getDate()));
		} 
		
		if(modelTime!=null) {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			StringBuilder time = new StringBuilder();
			if(modelTime.getStartDate()!=null) {
				try {
					time.append(DateUtil.to12HourTimeString(sdf.parse(modelTime.getStartDate()))).append(" ").append(DateUtil.toAMPMString(sdf.parse(modelTime.getStartDate())));
					time.append(" - ");
					if(modelTime.getEndDate()!=null) {
						time.append(DateUtil.to12HourTimeString(sdf.parse(modelTime.getEndDate()))).append(" ").append(DateUtil.toAMPMString(sdf.parse(modelTime.getEndDate())));
					}
					viewHolder.textTime.setText(time); 
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					AMLogger.logWarn(e.toString());
				}
			}

		}
	}

	@Override
	public void update(Observable observable, Object data) {
		AMLogger.logInfo("Inspection View get update notify");
		if(data instanceof Integer) {
			int year = ((Integer) data ) / 100;
			int month =  ((Integer) data ) % 100;
			//refresh only if the new data is current year and month
			AMLogger.logInfo("year : %d / month : %d", year, month);
			if(year == selectedYear && month == selectedMonth) { 
				refreshView(false);
			}
		} 
	}
	
	
	
}
