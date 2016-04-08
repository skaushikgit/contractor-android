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
 *   Created by jzhong on 03/13/2015.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */

package com.accela.contractorcentral.fragment;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Space;
import android.widget.TextView;

import com.accela.contractorcentral.R;
import com.accela.contractorcentral.activity.BaseActivity;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.DocumentLoader;
import com.accela.contractorcentral.service.ProjectsLoader;
import com.accela.contractorcentral.service.AppInstance.AppServiceDelegate;
import com.accela.contractorcentral.service.InspectorLoader.InspectionInspectorItem;
import com.accela.contractorcentral.utils.APIHelper;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.utils.Utils;
import com.accela.contractorcentral.view.ContactListView;
import com.accela.contractorcentral.view.DocumentThumbView;
import com.accela.inspection.model.InspectorModel;
import com.accela.mobile.util.UIUtils;
import com.accela.record.model.RecordInspectionModel;



public class CompletedInspectionFragment extends Fragment{



	private View contentView;

	private String recordId;

	private RecordInspectionModel inspectionModel;

	private boolean isLastRow;
	
	ContactListView contactList;

	ProjectsLoader projectsLoader = AppInstance.getProjectsLoader();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if(savedInstanceState!=null) {
			recordId = savedInstanceState.getString("recordId");
			inspectionModel = (RecordInspectionModel) savedInstanceState.getSerializable("inspectionModel");
		}

		super.onCreate(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("recordId", recordId);
		outState.putSerializable("inspectionModel", inspectionModel);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {       
		// Create content view.
		contentView = inflater.inflate(R.layout.fragment_completed_inspection_details, container, false);                 

		//update UI
		updateInspectionDocumentView();
		updateInspectionView();
		return contentView;
	}

	public void init(RecordInspectionModel inspectionModel) {
		this.recordId = inspectionModel.getRecordId_id();
		this.inspectionModel = inspectionModel;
	}

	public void setBottomSpace(boolean isLastRow) {
		this.isLastRow = isLastRow;
	}

	public void showInspectors(final String permitId, RecordInspectionModel inspection) {
		// TODO Auto-generated method stub
		final ProjectModel project = AppInstance.getProjectsLoader().getParentProject(permitId);
		if(project!=null) {

			InspectionInspectorItem item = AppInstance.getInspectorLoader().getInspectorByInspectionId(inspection.getId());
			if(item!=null){
				initContactList(item.listAllInspectors);
				return;
			}
			((BaseActivity) getActivity()).showProgressDialog(getString(R.string.load_contacts), false);
			AppServiceDelegate<InspectorModel> delegate = new AppServiceDelegate<InspectorModel>() {
				@Override
				public void onSuccess(List<InspectorModel> response) {
					// TODO Auto-generated method stub
					((BaseActivity) getActivity()).closeProgressDialog();
					initContactList(response);
				}

				@Override
				public void onFailure(Throwable error) {
					// TODO Auto-generated method stub
					((BaseActivity) getActivity()).closeProgressDialog();
							UIUtils.showMessageDialog(getActivity(), getString(R.string.warning_title), error.getMessage());
				}
			};
			AppInstance.getInspectorLoader().loadInspectorByInspection(inspection, delegate);
		}
	}
	
	private void initContactList(List<InspectorModel> listAllInspectors){
		if(contentView==null || listAllInspectors==null)
			return;
		contactList = (ContactListView) contentView.findViewById(R.id.listFailedContactInspector);
		contactList.setFocusable(false);
		contactList.enableScroll(false);
		contactList.setListAdjustable(true, Integer.MAX_VALUE);
		contactList.setContacts(Utils.generateContactList(getActivity(), listAllInspectors));
	}


	private void updateInspectionView() {
		//Inspection status view for Cancelled Inspection.
		View cancelInspectionStatusView = contentView.findViewById(R.id.cancelInspectionView);
		TextView textInspectionTimeDate = (TextView) cancelInspectionStatusView.findViewById(R.id.textTimeDate);

		//Contact View for Cancelled Inspection. 

		TextView textCommentTitle = (TextView) contentView.findViewById(R.id.textCommentTitle);

		if(Utils.isInspectionFailed(inspectionModel)) {
			textInspectionTimeDate.setText("Inspection time and date");
			if(inspectionModel.getInspectorId()!=null)
				showInspectors(inspectionModel.getRecordId_id(), inspectionModel);
		
			
			textCommentTitle.setText(R.string.inspection_failed);
			//don't show it, it is duplicated when failed.
			textCommentTitle.setVisibility(View.GONE);
			//Display Inspection status & time
			String inspectionCompletedDate = Utils.getInspectionCompletedDate(inspectionModel);
			String inspectionCompletedTime = Utils.getInspectionCompletedTime(inspectionModel);
			if(inspectionCompletedDate == null && inspectionCompletedTime != null) {
				textInspectionTimeDate.setText("Inspected at " + inspectionCompletedTime);
			} 
			if (inspectionCompletedTime == null && inspectionCompletedDate != null) {
				textInspectionTimeDate.setText("Inspected at " + inspectionCompletedDate);
			}
			if (inspectionCompletedTime == null && inspectionCompletedDate == null) {
				textInspectionTimeDate.setText("Unknow Inspection Date & Time");
			}
			if (inspectionCompletedTime != null && inspectionCompletedDate != null) {
				textInspectionTimeDate.setText("Inspected "+inspectionCompletedDate + " at " +inspectionCompletedTime );
			}

			cancelInspectionStatusView.setVisibility(View.VISIBLE);
		} else {
			textCommentTitle.setText(R.string.inspection_passed);
			View contactContainer = contentView.findViewById(R.id.contactContainer);
			contactContainer.setVisibility(View.GONE);
			//Hide Inspection status and time
			cancelInspectionStatusView.setVisibility(View.GONE);
		}
		TextView textComment = (TextView) contentView.findViewById(R.id.textViewComment);
		if(inspectionModel.getResultComment()!=null && inspectionModel.getResultComment().length() >0) {
			textComment.setText(inspectionModel.getResultComment());
		} else {
			textComment.setText(R.string.no_info_for_inspection);
		}

		//Bottom space
		Space bottomSpace = (Space) contentView.findViewById(R.id.bottomSpace);
		if(isLastRow) {
			bottomSpace.setVisibility(View.VISIBLE);
		} else {
			bottomSpace.setVisibility(View.GONE);
		}
	}

	private void updateInspectionDocumentView() {
		DocumentThumbView thumbview = (DocumentThumbView) contentView.findViewById(R.id.documentThumbView);
		thumbview.setInspection(inspectionModel);
		//not allow set focus by default. 
		thumbview.setViewStyle(1, 0.75f);
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
	}



	/*
	 * Upload the document for inspection. The testing helper.
	 * 
	 */

	private void uploadDocument(String filePath) {
		DocumentLoader service =  AppInstance.getDocumentLoader();
		service.uploadDocument(inspectionModel, filePath);
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

}
