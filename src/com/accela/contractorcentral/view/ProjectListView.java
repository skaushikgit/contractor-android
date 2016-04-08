package com.accela.contractorcentral.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.model.ProjectModel.ProjectInspection;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.InspectionLoader;
import com.accela.contractorcentral.service.ProjectsLoader;
import com.accela.contractorcentral.utils.Utils;
import com.accela.framework.model.AddressModel;
import com.accela.mobile.AMLogger;
import com.accela.mobile.util.APOHelper;
import com.accela.record.model.RecordInspectionModel;


public class ProjectListView extends FrameLayout implements Observer {	

	public final static int PROJECT_LIST_FULL  		= 0;
	public final static int PROJECT_LIST_COMPACT 	= 1;
	
	protected int listStyle = PROJECT_LIST_FULL;
	
	InspectionLoader inspectionLoader = AppInstance.getInpsectionLoader();
	List<ProjectModel> listProjects = new ArrayList<ProjectModel>();
	ProjectListViewAdapter adapter;
	OnSelectProjectListener onSelectProjectListener;
	ProjectsLoader projectLoader =  AppInstance.getProjectsLoader();
	View loadingContainer;
	ElasticListView listView;
	
	public interface OnSelectProjectListener {
		public void onSelectProject(int position , ProjectModel project);
	}
	
	public ProjectListView(Context context) {
		super(context);
		init();
	}
	
	public ProjectListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public ProjectListView(Context context, AttributeSet attrs,  int defStyle) {
		super(context, attrs, defStyle);
		init();
		
	}
	
	private void init() {
		adapter = new ProjectListViewAdapter();
		listView = new ElasticListView(getContext());
		listView.setMaxOverScrollDistance(0, 80);
	
		//Remove the horizontal line 
		listView.setDivider(null);
		listView.setDividerHeight(0);
		
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		addView(listView, lp);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(onSelectProjectListener!=null) {
					ProjectModel project = listProjects.get(position);
					onSelectProjectListener.onSelectProject(position, project);
				}
			}
			
		});
		//R.string.empty_project_list_message_title,R.string.empty_project_list_message
		initProjectList();
	}
	
	public void setListStyle(int listStyle) {
		this.listStyle = listStyle;
		if(adapter!=null) {
			adapter.notifyDataSetChanged();
		}
	}
	
	public void setOnSelectProjectListener(OnSelectProjectListener l) {
		onSelectProjectListener = l;
	}
	
	private void initProjectList() {
    	List<ProjectModel> projectsList = projectLoader.getProjects();
		if(projectsList.size()>0) {
			//if there is project list, just show it.
			updateListViewByData(true);
		} else {
			//download the list
			projectLoader.loadAllProjects(false);
			//pDialog.show();
			if(loadingContainer!=null) {
				removeView(loadingContainer);
			}
			loadingContainer = LayoutInflater.from(getContext()).inflate(R.layout.loading_progress , null);
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lp.gravity = Gravity.CENTER;
			this.addView(loadingContainer, lp);
			TextView textProgress = (TextView) loadingContainer.findViewById(R.id.textLoading);
			textProgress.setText(R.string.loading_wait_message);
			//if no agency, don't show the loading dialog because no data
			if(AppInstance.getAgencyLoader().getAllLinkedAgencies().size() == 0) {
				loadingContainer.setVisibility(View.INVISIBLE);
			} else {
				loadingContainer.setVisibility(View.VISIBLE);
			}
		} 
        
    }
	
	@Override
	protected void onAttachedToWindow() {
		projectLoader.addObserver(this);
		inspectionLoader.addObserver(this);
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		projectLoader.deleteObserver(this);
		inspectionLoader.deleteObserver(this);
		super.onDetachedFromWindow();
	}
	
	private static class ViewHolder {
		TextView textAddressLine1;
		TextView textAddressLine2;
		TextView textAddressUnit;
		TextView textDistance;
		TextView textNextInspection;		
		//TextView textNextPaymentDue;
		ProgressBar spinnerInpsection;
		//ProgressBar spinnerPayment;
		ViewGroup detailsLayout;
	}	
	
	private class ProjectListViewAdapter extends BaseAdapter{
	
		@Override
		public long getItemId(int position) {
			return (long) position;
		}	 
		
		@Override
		public View getView(final int position,View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (null == convertView) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_project, null);
				viewHolder.textAddressLine1 = (TextView) convertView.findViewById(R.id.textAddressLine1);
				viewHolder.textAddressLine2 = (TextView) convertView.findViewById(R.id.textAddressLine2);
				viewHolder.textAddressUnit = (TextView) convertView.findViewById(R.id.textAddressUnit);
				viewHolder.textDistance = (TextView) convertView.findViewById(R.id.textDistance);
				viewHolder.textNextInspection = (TextView) convertView.findViewById(R.id.textNextInspection);
				//viewHolder.textNextPaymentDue = (TextView) convertView.findViewById(R.id.textNextPaymentDue);
				viewHolder.spinnerInpsection = (ProgressBar) convertView.findViewById(R.id.spinnerInpsection);
				//viewHolder.spinnerPayment = (ProgressBar) convertView.findViewById(R.id.spinnerPayment);
				viewHolder.detailsLayout = (ViewGroup) convertView.findViewById(R.id.detailsLayout);
				
				
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
			
			return listProjects.size();
		}

		@Override
		public Object getItem(int position) {
			
			return null;
		}	
		
		
	}

	private void updateListViewByData(boolean dataChanged) {
		//pDialog.dismiss();
		if(loadingContainer!=null) {
			loadingContainer.setVisibility(View.GONE);
		}
		listProjects.clear();
		listProjects.addAll(projectLoader.getProjects());
		Collections.sort(listProjects, new ProjectComparator());
		if(adapter!=null) {
			adapter.notifyDataSetChanged();
		}
	}
	
	class ProjectComparator implements Comparator<ProjectModel>{

		@Override
		public int compare(ProjectModel lhs, ProjectModel rhs) {
			// TODO Auto-generated method stub
			if(lhs.getDistance()>0 && rhs.getDistance()>0){
				if(lhs.getDistance() == rhs.getDistance())
					return 0;
				return lhs.getDistance()>rhs.getDistance() ? 1 : -1;
			}
			String addr1 = APOHelper.formatAddress(lhs.getAddress());
			String addr2 = APOHelper.formatAddress(rhs.getAddress());
			return addr1.compareTo(addr2);
		}
		
	}

	@Override
	public void update(Observable observable, Object data) {
		if(observable instanceof ProjectsLoader && data instanceof Integer) {
			int flag = (Integer) data;
			switch (flag) {
			case AppConstants.PROJECT_LIST_CHANGE:
				AMLogger.logInfo("ProjectFragment.update()");
				updateListViewByData(true);
				break;
			case AppConstants.PROJECT_LIST_LOAD_PROGRESS:
				{
					if(loadingContainer!=null) {
						TextView textProgress = (TextView) loadingContainer.findViewById(R.id.textLoading);
						textProgress.setText(String.format(getContext().getString(R.string.count_project_loaded), projectLoader.getProjects().size()));
					}
					
				}
				break;
			}
		} else if(observable instanceof InspectionLoader) {
			adapter.notifyDataSetChanged();
		}
		
	}
	
	
	
	
	private void setListViewItem(final int position, ViewHolder viewHolder) {
		Context context = this.getContext();
		// Set values for view elements.
		final ProjectModel projectModel = (ProjectModel)  listProjects.get(position);
		if (projectModel == null) {
			AMLogger.logError("ProjectListView + Can't get project model :" + position);
			return;
		}
		
		AddressModel addressModel = projectModel.getAddress();
		// Set address text
		if(addressModel == null) {
			AMLogger.logError("ProjectListView + Can't get address: " + position);
			viewHolder.textAddressLine1.setText(getContext().getString(R.string.unkonwn_address));
			viewHolder.textAddressLine2.setText("");
			viewHolder.textAddressUnit.setVisibility(View.GONE);
		} 
		else {
			//get primary address
			//need to format the address 
			String street = Utils.getAddressLine1(addressModel);
			viewHolder.textAddressLine1.setText(street.length()>0 ? street: getContext().getString(R.string.unkonwn_address));
			viewHolder.textAddressLine2.setText(Utils.getAddressLine2(addressModel));
			String addressUnit = Utils.getAddressUnit(addressModel);
			if(addressUnit.length()>0) {
				viewHolder.textAddressUnit.setVisibility(View.VISIBLE);
				viewHolder.textAddressUnit.setText(addressUnit);
			} else {
				viewHolder.textAddressUnit.setVisibility(View.GONE);
			}
		}
		
		float distance = projectModel.getDistance();
		if(distance >=0) {
			viewHolder.textDistance.setText(String.format("%.1f %s", distance, getContext().getString(R.string.mile)));
		} else {
			//set distance 
			viewHolder.textDistance.setText(R.string.unknown_mi);
		}
		
		if(listStyle == PROJECT_LIST_COMPACT) {
			viewHolder.detailsLayout.setVisibility(View.GONE);
		} else {
			viewHolder.detailsLayout.setVisibility(View.VISIBLE);
			//set next inspection
			ProjectInspection projectInspection = projectModel.getInspections(false); 
			if(projectInspection == null || projectInspection.downloadFlag == AppConstants.FLAG_NOT_DOWNLOADED ) {
				//request inspection here
				viewHolder.textNextInspection.setText(context.getString(R.string.loading));
				viewHolder.spinnerInpsection.setVisibility(View.VISIBLE);
			} else {
				RecordInspectionModel inspectionModel = projectInspection.nextInspection;
				if(inspectionModel!=null) {
					String date = Utils.formatInsepctionDateTime(inspectionModel);
					viewHolder.textNextInspection.setText(date!=null? date: context.getText(R.string.unknown_date));
					viewHolder.spinnerInpsection.setVisibility(View.GONE);
				} else {
					//viewHolder.textNextInspection.setText(context.getText(R.string.no_scheduled));
					//for test
					viewHolder.textNextInspection.setText(context.getText(R.string.none_schedule));
					viewHolder.spinnerInpsection.setVisibility(View.GONE);
				}
				
			} 
			
			
		/*	//set payment Due
			FeeModel fee = null;
			if(feeManager.isFeeDownloaded(projectModel.getProjectId(), true)) {
				fee = feeManager.getNextFee(projectModel.getProjectId());
				if(fee!=null && fee.getAmount()!=null) {
					StringBuffer feeDue = new StringBuffer();
					Date date = fee.getApplyDate();
					if(date!=null) {
						feeDue.append(new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date));
						feeDue.append(", ");
						feeDue.append(new SimpleDateFormat("MMM").format(date)+". "+date.getDay());
					}
					feeDue.append(", $" + fee.getAmount());
					viewHolder.textNextPaymentDue.setText(feeDue);
					viewHolder.spinnerPayment.setVisibility(View.GONE);
				} else {
					viewHolder.textNextPaymentDue.setText(context.getText(R.string.no_payment_due));
					viewHolder.spinnerPayment.setVisibility(View.GONE);
				}
				
			} else {
				//request fee here
				viewHolder.textNextPaymentDue.setText(context.getString(R.string.loading));
				viewHolder.spinnerPayment.setVisibility(View.VISIBLE);
			} */
		}

			
	}
}
