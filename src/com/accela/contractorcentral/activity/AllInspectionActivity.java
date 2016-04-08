package com.accela.contractorcentral.activity;

import android.os.Bundle;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.utils.Utils;
import com.accela.contractorcentral.view.AllInspectionListView;
import com.accela.record.model.RecordInspectionModel;
import com.flurry.android.FlurryAgent;

public class AllInspectionActivity extends BaseActivity {

	AllInspectionListView inspectionListView;
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onPageView();
		FlurryAgent.logEvent("AllInspectionActivity");
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
		setContentView(R.layout.activity_all_inspection);
		setActionBarTitle(R.string.all_inspections);
		this.showScheduleBtn(true, false);
		final AllInspectionListView inspectionListView = (AllInspectionListView) findViewById(R.id.inspectionListViewId);
		inspectionListView.initAllInspection();
		inspectionListView.setMaxOverScrollDistance(0, 60);
		inspectionListView.setOnInspectionClickListener(new AllInspectionListView.onInspectionClickListener() {
			@Override
			public void onSelectItem(boolean isScheduled, RecordInspectionModel inspection) {
				if(inspection != null) {
					if(isScheduled){
						if(inspection.getRecordId_id()!=null && AppInstance.getProjectsLoader().getParentProject(inspection.getRecordId_id())!=null)
							ActivityUtils.startScheduleInspectionActivity(AllInspectionActivity.this, AppInstance.getProjectsLoader().getParentProject(inspection.getRecordId_id()).getProjectId(), 
									inspection.getRecordId_id(), inspection, AppConstants.CANCEL_INSPECTION_SOURCE_OTHER);
					}else{
						ActivityUtils.startInspectionDetailsActivity(AllInspectionActivity.this, inspection, Utils.isInspectionFailed(inspection), AppConstants.CANCEL_INSPECTION_SOURCE_OTHER);	
					}
				}

			}
		});
	}


}
