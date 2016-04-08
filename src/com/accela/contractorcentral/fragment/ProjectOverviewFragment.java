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
 *   Created by jzhong on 2/5/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */

package com.accela.contractorcentral.fragment;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.model.ProjectModel.ProjectInspection;
import com.accela.contractorcentral.persistence.ContactsPersistence;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.DocumentLoader;
import com.accela.contractorcentral.service.InspectionLoader;
import com.accela.contractorcentral.service.ProjectsLoader;
import com.accela.contractorcentral.utils.APIHelper;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.utils.Utils;
import com.accela.contractorcentral.view.ContactListView;
import com.accela.contractorcentral.view.DocumentThumbView;
import com.accela.contractorcentral.view.ElasticScrollView;
import com.accela.document.model.DocumentModel;
import com.accela.record.model.ContactModel;
import com.accela.record.model.RecordInspectionModel;
//import android.support.v4.app.Fragment;



public class ProjectOverviewFragment extends Fragment implements Observer  {

    private String projectId;
   
    ContactListView contactListView;
    
    ProjectModel projectModel;
    
    View contentView;
    
    DocumentThumbView thumbview;
    
    FrameLayout mapViewContainer;
    
    ProjectsLoader projectsLoader = AppInstance.getProjectsLoader();
    
    boolean nextInspectionReady = false;
    
    private ContactsPersistence contactsPersistence;

  
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);		
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
		if(savedInstanceState!=null) {
			projectId = savedInstanceState.getString("projectId");
		}
        contactsPersistence = new ContactsPersistence(getActivity());
        super.onCreate(savedInstanceState);      
    }
	

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {       
    	// Create content view.
        contentView = inflater.inflate(R.layout.fragment_project_overview, container, false);                 
        projectModel = projectsLoader.getProjectById(projectId);
        
        //setup UI
        setupFeeUI();
        setupContactListView(true);
        setupDocumentThumbView();
        addMapView();
        setupElasticScrollView();
        return contentView;
    }
   
    public void setProjectId(String projectId) {
    	this.projectId = projectId;
    	if(this.getActivity()!=null) {
    		 throw new AssertionError("Must set project Id before onCreate()");
    	}
    }
    
    private void scheduleInspection() {
    	ActivityUtils.startChoosePermitActivity(this.getActivity(), projectId);
	}
    
    private void addMapView() {
    	FragmentManager fm = this.getActivity().getSupportFragmentManager();
    	MapViewFragment fg = new MapViewFragment();
    	fg.setAddress(projectModel.getAddress());
    	
    	FragmentTransaction ft = fm.beginTransaction();
    	ft.add(R.id.mapViewContainer, fg);
    	ft.commit();
    	
    	mapViewContainer = (FrameLayout) contentView.findViewById(R.id.mapViewContainer);
    	
    }
    
    
    private void setupInspectionUI() {
    	if(projectModel==null) {
    		return;
    	}
    	TextView textDate = (TextView) contentView.findViewById(R.id.nextInspectionDate);
    	TextView textTime = (TextView) contentView.findViewById(R.id.nextInspectionTime);
    	TextView textAddress = (TextView) contentView.findViewById(R.id.textAddressInspection);
    	TextView textPermitType = (TextView) contentView.findViewById(R.id.textPermitType);
    	Button buttonScheduleInspection = (Button) contentView.findViewById(R.id.buttonScheduleInspection);
    	Button buttonViewInspections = (Button) contentView.findViewById(R.id.buttonViewInspections);
    	buttonViewInspections.setVisibility(View.GONE);
    	ProgressBar loadingProgress = (ProgressBar) contentView.findViewById(R.id.spinnerInpsection);

		ProjectInspection projectInspection = projectModel.getInspections(true); 
		final RecordInspectionModel inspectionModel = projectInspection.nextInspection;
		if(projectInspection.downloadFlag == AppConstants.FLAG_FULL_DOWNLOAED
				|| projectInspection.downloadFlag == AppConstants.FLAG_DOWNLOADED_FAILED) {
			//need to display load failed here.
			if(inspectionModel != null) {
				textDate.setVisibility(View.VISIBLE);
				textTime.setVisibility(View.VISIBLE);
				String inspectionInfo[] = Utils.formatInspectionInfo(inspectionModel);
				textAddress.setText(inspectionInfo[0]);
				textAddress.setVisibility(View.VISIBLE);
				textPermitType.setVisibility(View.VISIBLE);
				String date = Utils.getInspectionDate(inspectionModel);
				String time = Utils.getInspectionTime(inspectionModel);
				if(date==null && time==null) {
					textDate.setText(getActivity().getString(R.string.none_schedule));
					textTime.setText("");
				} else {
					if(date!=null) {
						textDate.setText(date);
					} else {
						textDate.setText("");
					}
					if(time!=null) {
						textTime.setText(time);
					} else {
						textTime.setText("");
					}
				}
				textPermitType.setText(inspectionInfo[1]);
				buttonScheduleInspection.setVisibility(View.GONE);
				LinearLayout layout = (LinearLayout) contentView.findViewById(R.id.landingNextInspectionId);
				layout.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						ActivityUtils.startScheduleInspectionActivity(getActivity(), projectModel.getProjectId(), inspectionModel.getRecordId_id(), inspectionModel, AppConstants.CANCEL_INSPECTION_SOURCE_OTHER);
					}
				});
			} else {
				textDate.setVisibility(View.VISIBLE);
				textTime.setVisibility(View.VISIBLE);
				textDate.setText(getActivity().getString(R.string.none_schedule));
				textTime.setText("");
				textAddress.setVisibility(View.GONE);
				textPermitType.setVisibility(View.GONE);
				buttonScheduleInspection.setVisibility(View.VISIBLE);
				buttonScheduleInspection.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						scheduleInspection();
						
					}
				});
			}
			loadingProgress.setVisibility(View.GONE);
			 AppInstance.getInpsectionLoader().deleteObserver(this);
			nextInspectionReady = true;
		}
		else {
			loadingProgress.setVisibility(View.VISIBLE);
			textDate.setVisibility(View.GONE);
			textTime.setVisibility(View.INVISIBLE);
			textAddress.setVisibility(View.INVISIBLE);
			textPermitType.setVisibility(View.INVISIBLE);
			AppInstance.getInpsectionLoader().addObserver(this);
			nextInspectionReady = false;
			buttonScheduleInspection.setVisibility(View.GONE);
		}
		
    }
		
	private void setupFeeUI() {
	/*	if(projectModel==null) {
    		return;
    	}
		TextView textDate = (TextView) contentView.findViewById(R.id.nextPaymentDate);
    	TextView textFee = (TextView) contentView.findViewById(R.id.nextPaymentAmount);
    	TextView textAddress = (TextView) contentView.findViewById(R.id.textAddressFee);
    	TextView textPermitType = (TextView) contentView.findViewById(R.id.textFeePermitType);
    	
    	AddressModel addressModel = projectModel.getAddress();
		// Set address text
    	if(addressModel!=null) {
			//get primary address
			//need to format the address 
			textAddress.setText(Utils.getAddressLine1(addressModel));
		} else {
			textAddress.setText("");
		}
		FeeManager feeMan = FeeManager.getInstance();
 
		FeeModel feeModel = null;
		if(feeMan.isFeeDownloaded(projectModel.getProjectId(), false)) {
			feeModel = feeMan.getNextFee(projectModel.getProjectId());
			String date = Utils.getFeeDateTime(feeModel);
			if(date!=null) {
				textDate.setText(date);
			} else {
				textDate.setText(getActivity().getString(R.string.no_payment_due));
			}
			
			if(feeModel!=null) {
				textFee.setText("$" + feeModel.getAmount());
			} else {
				textFee.setText("");
			}
			
			textPermitType.setText("Unknown Permit Type");
		}
		*/ 
	}
    
    private void setupContactListView(boolean dataChanged) {

    	contactListView = (ContactListView) contentView.findViewById(R.id.listContact);
    	//not allow set focus by default. 
    	contactListView.setFocusable(false);
        //disable scroll the contact list. 
        contactListView.enableScroll(false);
        //make the contact height adjustable by the item count
        contactListView.setListAdjustable(true, 3);
        
		List<ContactModel> list = projectModel.getContacts();
		list.addAll(contactsPersistence.queryContacts(this.projectId));
		contactListView.setContacts(list);
		
		Button buttonViewContacts = (Button) contentView.findViewById(R.id.buttonViewContacts);
		
		//button "view all contacts" need to be hide if contacts <= 3 
		if(list.size()<=3) {
			buttonViewContacts.setVisibility(View.GONE);
		} else {
			buttonViewContacts.setVisibility(View.VISIBLE);
		}
		
		buttonViewContacts.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				viewAllContacts();
			}
		});
		
	}
    
    private void setupElasticScrollView() {
    	ElasticScrollView scrollView = (ElasticScrollView) contentView.findViewById(R.id.scrollView);
    	scrollView.setMaxOverScrollDistance(0, 100); 
    	
    	final DisplayMetrics metrics = this.getActivity().getResources()
	            .getDisplayMetrics();
	    final float density = metrics.density;
	    final int mapViewTopMargin = (int) (-60 * density);
	    mapViewContainer.setY(mapViewTopMargin);
	    
    	scrollView.setCallbacks(new ElasticScrollView.ScrollCallbacks() {

			@Override
			public void onScrollChanged(int l, int t, int oldl, int oldt) {
			//	AMLogger.logInfo("onScrollChanged: l-%d,  t-%d, oldl-%d, oldt-%d", l, t, oldl, oldt);
				if(t <= 0) {
					mapViewContainer.setY((float) (mapViewTopMargin -  (t * 0.5)));
				} else {
					mapViewContainer.setY(mapViewTopMargin-t);
				}
				
			}
    	});
    	
    	
    }
    
    private void viewAllContacts() {
    	ActivityUtils.startInspectionContactActivity(getActivity(), projectId, null, null, false);
    }
    
    private void setupDocumentThumbView() {
    	thumbview = (DocumentThumbView) contentView.findViewById(R.id.documentThumbView);
    	thumbview.setProjectId(projectId);
    	//thumbview.setViewStyle(60);
    	thumbview.setViewStyle(4, 1.0f);
    	//not allow set focus by default. 
    	thumbview.setGridViewFocusable(false);
    	//just for test the upload image
    	Button uploadDocument = (Button) contentView.findViewById(R.id.buttonUploadDocument);
    	uploadDocument.setVisibility(View.GONE);
    	uploadDocument.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
			}
		});
    	
    	//on click document
    	thumbview.setOnDocumentClickListener(new DocumentThumbView.OnDocumentClickListener() {
			
			@Override
			public void onDocumentClick(View thumbView, DocumentModel model,
					int position) {
				ActivityUtils.startExpandedImageActivity(getActivity(), projectId, position);
			}
		});
    	
    }
    
    private void uploadDocument(String filePath) {
		ProjectModel model = projectsLoader.getProjectById(projectId);
		DocumentLoader service = AppInstance.getDocumentLoader();
		service.uploadDocument(model.getFirstRecord(), filePath);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	setupInspectionUI();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();    	
    	contactsPersistence.close();
    	contactsPersistence = null;
    }
    
    @Override
    public void onDestroyView() {
    	AppInstance.getInpsectionLoader().deleteObserver(this);
    	super.onDestroyView();

    }

   	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("projectId", projectId);
		
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) { 
        case 1:
            if(resultCode == Activity.RESULT_OK){
            	final Uri imageUri = data.getData();
            	
				uploadDocument(APIHelper.getPath(this.getActivity(), imageUri));
            }
        }
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void update(Observable observable, Object data) {
		if(observable instanceof InspectionLoader && !nextInspectionReady) {
			//need to optimize here, don't need to update every inspection loaded.
			if(data != null && data instanceof String) {
				String recordId = (String) data;
				if(projectModel.isIncludeRecord(recordId)) {
					this.setupInspectionUI();
				}
			} 
		} 		
	}

}