package com.accela.contractorcentral.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.activity.BaseActivity;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.ProjectsLoader;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.utils.Utils;
import com.accela.framework.model.AddressModel;
import com.accela.mobile.AMLogger;
import com.accela.record.model.RecordInspectionModel;

public class AllInspectionListView extends ElasticListView implements Observer{

	View headerView;
	ProjectsLoader projectsLoader;
	private List<RecordInspectionModel> recentOneMonthInspectionList = new ArrayList<RecordInspectionModel> ();
	private List<RecordInspectionModel> scheduledInspetions = new ArrayList<RecordInspectionModel> ();
	AllInspectionListViewAdapter adapter;	
	onInspectionClickListener listener;
	private static int scheduledInspectionCount = 0;
	

	public interface onInspectionClickListener {
		public void onSelectItem(boolean isScheduled, RecordInspectionModel inspection);
	}

	public AllInspectionListView(Context context) {
		super(context);
	}

	public AllInspectionListView(Context context,  AttributeSet attrs) {
		super(context, attrs);
	}

	public AllInspectionListView(Context context,  AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setOnInspectionClickListener(onInspectionClickListener l) {
		listener = l;
	}

	public void initAllInspection() {
		projectsLoader = AppInstance.getProjectsLoader();
		recentOneMonthInspectionList = (List<RecordInspectionModel>) projectsLoader.getRecentOneMonthInpsections();
//		for(RecordInspectionModel inspection : recentOneMonthInspectionList){
//			AMLogger.logError("complete date:" + inspection.getCompletedDate() + "type:" + inspection.getType().getText());
//		}
		getScheduleInspection();
		scheduledInspectionCount = scheduledInspetions.size();
		
		adapter = new AllInspectionListViewAdapter();
		this.setAdapter(adapter);
		
		this.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(scheduledInspetions!=null && position <= scheduledInspectionCount && position<scheduledInspetions.size())
					listener.onSelectItem(true, scheduledInspetions.get(position));
				if( (position >= (scheduledInspectionCount + 1)) && (position - scheduledInspectionCount -1) < recentOneMonthInspectionList.size() )
					listener.onSelectItem(false, recentOneMonthInspectionList.get(position - scheduledInspectionCount - 1));
			}
		});
	}
	
	private void getScheduleInspection(){
		scheduledInspetions.clear();
		Calendar cal = Calendar.getInstance();
		cal.add(cal.DATE, -1);
		Date currentDate = cal.getTime();
		for(RecordInspectionModel inspection : projectsLoader.getScheduleInpsections()){
			if(inspection.getScheduleDate() != null && inspection.getScheduleDate().before(currentDate))
				continue;
			if(inspection.getAddress()!=null && inspection.getAddress().getCity()!=null && inspection.getAddress().getCity().length()>0)
				this.scheduledInspetions.add(inspection);
		}
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

	private static class ViewHolder {
		TextView textAddressLine1;
		TextView textAddressLine2;
		ImageView imageInspectionStatus;
		TextView textInspectionStatus;
		TextView textInspectionType;
		LinearLayout itemContainer;
	}

	private static class ViewHolderScheduledInspection {
		TextView textAddrLine1;
		TextView textAddrLine2;
		TextView textDate;
		TextView textTime;
		TextView textInspectionGroup;
		TextView textInspectionType;
		Button buttonContactFirstInspection;
		Button buttonContactSecondInspection;
	}

	private static class ViewHolderRecentlyCompleted {
		TextView textRecentlyCompleted;
	}
	
	private class AllInspectionListViewAdapter extends BaseAdapter {
		private static final int TYPE_ITEM1 = 0;
		private static final int TYPE_ITEM2 = 1;
		private static final int TYPE_ITEM3 = 2; 
		private static final int TYPE_ITEM4 = 3; 
		
		private int viewType;

		public AllInspectionListViewAdapter() {
			scheduledInspectionCount = scheduledInspetions.size();
		}

		@Override
		public int getCount() {
			return recentOneMonthInspectionList.size() + scheduledInspectionCount + 1;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public int getItemViewType(int position) {

			if(position == 0) {
				viewType = TYPE_ITEM1;
			} else if(position > 0 && position < scheduledInspectionCount) {
				viewType = TYPE_ITEM2;
			} else if(position == scheduledInspectionCount) {
				viewType = TYPE_ITEM3;
			}
			else {
				viewType = TYPE_ITEM4;
			}
			return viewType;
		}

		@Override
		public int getViewTypeCount() {
			return 4;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			int type = getItemViewType(position);

			switch(type) {

			case TYPE_ITEM1: {
				ViewHolderScheduledInspection viewHolderScheduledInspection = null;
				if (convertView == null) {
					convertView = LayoutInflater.from(getContext()).inflate(R.layout.first_coming_inspection, null);
					viewHolderScheduledInspection = new ViewHolderScheduledInspection();
					viewHolderScheduledInspection.textAddrLine1 = (TextView) convertView.findViewById(R.id.textAddressLine1FirstInspection);
					viewHolderScheduledInspection.textAddrLine2 = (TextView) convertView.findViewById(R.id.textAddressLine2FirstInspection);
					viewHolderScheduledInspection.textDate = (TextView) convertView.findViewById(R.id.textDateFirstInspection);
					viewHolderScheduledInspection.textTime = (TextView) convertView.findViewById(R.id.textTimeFirstInspection);
					viewHolderScheduledInspection.textInspectionGroup = (TextView) convertView.findViewById(R.id.textInspectionGroupFirstInspection);
					viewHolderScheduledInspection.textInspectionType = (TextView) convertView.findViewById(R.id.textInspectionTypeFirstInspection);
					viewHolderScheduledInspection.buttonContactFirstInspection = (Button) convertView.findViewById(R.id.buttonContactFirstInspection);

					convertView.setTag(viewHolderScheduledInspection);
				} else {
					viewHolderScheduledInspection = (ViewHolderScheduledInspection) convertView.getTag();
				}
				setScheduledInspectionRow(viewHolderScheduledInspection,position);
				return convertView;
			}

			case TYPE_ITEM2: {
				ViewHolderScheduledInspection viewHolderScheduledInspection = null;
				if (convertView == null) {
					convertView = LayoutInflater.from(getContext()).inflate(R.layout.second_comming_inspection, null);
					viewHolderScheduledInspection = new ViewHolderScheduledInspection();
					viewHolderScheduledInspection.textAddrLine1 = (TextView) convertView.findViewById(R.id.textAddressLine1SecondInspection);
					viewHolderScheduledInspection.textAddrLine2 = (TextView) convertView.findViewById(R.id.textAddressLine2SecondInspection);
					viewHolderScheduledInspection.textDate = (TextView) convertView.findViewById(R.id.textDateSecondInspection);
					viewHolderScheduledInspection.textTime = (TextView) convertView.findViewById(R.id.textTimeSecondInspection);
					viewHolderScheduledInspection.textInspectionGroup = (TextView) convertView.findViewById(R.id.textInspectionGroupSecondInspection);
					viewHolderScheduledInspection.textInspectionType = (TextView) convertView.findViewById(R.id.textInspectionTypeSecondInspection);
					viewHolderScheduledInspection.buttonContactSecondInspection = (Button) convertView.findViewById(R.id.buttonContactSecondInspection);

					convertView.setTag(viewHolderScheduledInspection);
				} else {
					viewHolderScheduledInspection = (ViewHolderScheduledInspection) convertView.getTag();
				}
				setScheduledInspectionRow(viewHolderScheduledInspection,position);
				return convertView;
			}

			case TYPE_ITEM3: {
				ViewHolderRecentlyCompleted viewHolderRecent = null;
				if(convertView == null) {
					convertView = LayoutInflater.from(getContext()).inflate(R.layout.recently_completed, null);
					viewHolderRecent = new ViewHolderRecentlyCompleted();
					viewHolderRecent.textRecentlyCompleted = (TextView) convertView.findViewById(R.id.textRecentlyID);
					convertView.setTag(viewHolderRecent);
				} else {
					viewHolderRecent = (ViewHolderRecentlyCompleted) convertView.getTag();
				}
				return convertView;
			}
			
			case TYPE_ITEM4: {
				ViewHolder viewHolder = null;
				if (convertView == null) {
					convertView = LayoutInflater.from(getContext()).inflate(R.layout.all_inspection_row, null);
					viewHolder = new ViewHolder();
					viewHolder.textAddressLine1 = (TextView) convertView.findViewById(R.id.textAddressLine1);
					viewHolder.textAddressLine2 = (TextView) convertView.findViewById(R.id.textAddressLine2);
					viewHolder.imageInspectionStatus = (ImageView) convertView.findViewById(R.id.imageInspectionStatus);
					viewHolder.textInspectionStatus = (TextView) convertView.findViewById(R.id.textInspectionStatus);
					viewHolder.textInspectionType = (TextView) convertView.findViewById(R.id.textInspectionType);
					viewHolder.itemContainer = (LinearLayout) convertView.findViewById(R.id.all_inspection_row_id);
					convertView.setTag(viewHolder);
				} else {
					viewHolder = (ViewHolder) convertView.getTag();
				}

				setAllInspectionList(position, viewHolder);
				return convertView;
			}

			}			
			return convertView;
		}


		private String getRecordIdByPosition(int position) {
			String recordId = null;

			//Get first scheduled inspection
			RecordInspectionModel inspectionModel = null;
			if(scheduledInspetions.size() > 0) {
				inspectionModel = scheduledInspetions.get(position);
			}

			//Setup inspection address
			if(inspectionModel != null) {
				recordId = inspectionModel.getRecordId_id();
			}
			return recordId;
		}
		
		private void setScheduledInspectionRow(
				ViewHolderScheduledInspection viewHolderFirst, final int position) {
			String recordId = null;

			//Get first scheduled inspection
			RecordInspectionModel inspectionModel = null;
			if(scheduledInspetions.size() > 0 && position<scheduledInspetions.size()) {
				inspectionModel = scheduledInspetions.get(position);
			}

			//Setup inspection address
			if(inspectionModel != null) {
					AddressModel addressModel = inspectionModel.getAddress();
					// Set address text
					if(addressModel != null) {
						//get primary address
						//need to format the address 
						viewHolderFirst.textAddrLine1.setText(Utils.getAddressLine1AndUnit(addressModel));
						viewHolderFirst.textAddrLine2.setText(Utils.getAddressLine2(addressModel));
					} else {
						viewHolderFirst.textAddrLine1.setText("");
						viewHolderFirst.textAddrLine2.setText("");
					}

				

				//Setup inspection Time & Date
				String date = Utils.getInspectionDate(inspectionModel);
				String time = Utils.getInspectionTime(inspectionModel);
				if(date==null && time==null) {
					viewHolderFirst.textDate.setText(R.string.none_schedule);
					viewHolderFirst.textTime.setText("");
				} else {
					if(date!=null) {
						viewHolderFirst.textDate.setText(date);
					} else {
						viewHolderFirst.textDate.setText("");
					}
					if(time!=null) {
						viewHolderFirst.textTime.setText(time);
					} else {
						viewHolderFirst.textTime.setText("");
					}
				}

				//Setup Inspection Info
				String[] inspectionInfo = Utils.formatInspectionInfo(inspectionModel);
				viewHolderFirst.textInspectionGroup.setText(inspectionInfo.length != 0 ? inspectionInfo[0]: "");
				viewHolderFirst.textInspectionType.setText((CharSequence) (inspectionInfo.length !=0 ? inspectionInfo[1]:(R.string.unknown_permit)));
			}

			if(position == 0) {
				viewHolderFirst.buttonContactFirstInspection.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String recordId = getRecordIdByPosition(position);
						ProjectModel project = AppInstance.getProjectsLoader().getParentProject(recordId);
						if(recordId!=null) {
							if(scheduledInspetions.get(position)!=null && scheduledInspetions.get(position).getInspectorId()!=null)
								AppInstance.getInspectorLoader().showInspectors((BaseActivity) getContext(), recordId, scheduledInspetions.get(position));
							else
								ActivityUtils.startInspectionContactActivity((Activity) getContext(), project.getProjectId() , recordId, scheduledInspetions.get(position), false);
						}
					}
				});
			} else {
				viewHolderFirst.buttonContactSecondInspection.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String recordId = getRecordIdByPosition(position);
						ProjectModel project = AppInstance.getProjectsLoader().getParentProject(recordId);
						if(recordId!=null) {
							if(scheduledInspetions.get(position)!=null && scheduledInspetions.get(position).getInspectorId()!=null)
								AppInstance.getInspectorLoader().showInspectors((BaseActivity) getContext(), recordId, scheduledInspetions.get(position));
							else
								ActivityUtils.startInspectionContactActivity((Activity) getContext(), project.getProjectId() , recordId, scheduledInspetions.get(position), false);
							}
					}
				});
			}
		}


		private void setAllInspectionList(final int position, ViewHolder viewHolder) {
			String recordId = null;
			boolean isInspectionFailed;

			RecordInspectionModel inspection = (RecordInspectionModel) recentOneMonthInspectionList.get(position - scheduledInspectionCount -1);
			if(inspection != null) {
				isInspectionFailed = Utils.isInspectionFailed(inspection);
				String[] inspectionInfo = Utils.formatInspectionInfo(inspection);
				String inspectionType = inspectionInfo[0] + ": " + inspectionInfo[1];
				

				viewHolder.textInspectionType.setText(inspectionType);
				if(isInspectionFailed) {
					//Set data
					viewHolder.imageInspectionStatus.setImageResource(R.drawable.white_x);
					if(inspection.getStatus_text()!=null){
						viewHolder.textInspectionStatus.setText(inspection.getStatus_text());
					}else{
						viewHolder.textInspectionStatus.setText(R.string.failed);
					}

					//Set appearance 
					viewHolder.itemContainer.setBackgroundResource(R.drawable.card_background_red_thin_bottom);
					viewHolder.textAddressLine1.setTextColor(Color.WHITE);
					viewHolder.textAddressLine2.setTextColor(Color.WHITE);
					viewHolder.textInspectionStatus.setTextColor(Color.WHITE);
					viewHolder.textInspectionType.setTextColor(Color.WHITE);
				} else {
					//Set data
					viewHolder.imageInspectionStatus.setImageResource(R.drawable.insp_pass);
					if(inspection.getStatus_text()!=null){
						viewHolder.textInspectionStatus.setText(inspection.getStatus_text());
					}else{
						viewHolder.textInspectionStatus.setText(R.string.passed);
					}

					//Set appearance 
					viewHolder.itemContainer.setBackgroundResource(R.drawable.card_background_white);
					viewHolder.textAddressLine1.setTextColor(Color.BLACK);
					viewHolder.textAddressLine2.setTextColor(Color.BLACK);
					viewHolder.textInspectionStatus.setTextColor(Color.BLACK);
					viewHolder.textInspectionType.setTextColor(Color.BLACK);
				}

				recordId = inspection.getRecordId_id();
				if(recordId != null) {
					ProjectModel projectModel = projectsLoader.getParentProject(recordId);
					AddressModel addressModel = projectModel.getAddress();
					// Set address text
					if(addressModel != null) {
						//get primary address
						//need to format the address 
						viewHolder.textAddressLine1.setText(Utils.getAddressLine1AndUnit(addressModel));
						viewHolder.textAddressLine2.setText(Utils.getAddressLine2(addressModel));
					} else {
						viewHolder.textAddressLine1.setText("");
						viewHolder.textAddressLine2.setText("");
					}
				}

			}
		}
	}
	
	private void updateInspections(){
		if(projectsLoader!=null){
			if(recentOneMonthInspectionList!=null)
				recentOneMonthInspectionList.clear();
			recentOneMonthInspectionList = (List<RecordInspectionModel>) projectsLoader.getRecentOneMonthInpsections();
			getScheduleInspection();
			scheduledInspectionCount = scheduledInspetions.size();
		}
	}


	@Override
	public void update(Observable observable, Object data) {
		if(observable instanceof ProjectsLoader && data instanceof Integer) {
			int flag = (Integer) data;
			switch (flag) {
			case AppConstants.PROJECT_LIST_RECENT_INSPECTION_CHANGE:
				updateInspections();
				AMLogger.logInfo("project completed list changed ");
				adapter.notifyDataSetChanged();
				break;
			}

		}

	}

}
