package com.accela.contractorcentral.fragment;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.activity.EnterNewContactActivity;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.persistence.ContactsPersistence;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.ProjectsLoader;
import com.accela.contractorcentral.utils.Utils;
import com.accela.contractorcentral.view.ContactListView;
import com.accela.contractorcentral.view.ElasticScrollView;
import com.accela.contractorcentral.view.FeeListView;
import com.accela.record.model.ContactModel;


public class ProjectInfoFragment extends Fragment implements OnClickListener{
    
    private String projectId;
   
    private ContactListView contactListView;
    
    private FeeListView feeListView;
    
    private List<ContactModel> list;
    
    private ProjectModel projectModel;
       
    private View contentView, cardFee;
    
	private ProgressBar bar;
	
	private Button addFromBtn, addNewBtn;
	
	private TextView paymentText;
		
          
    ProjectsLoader projectsLoader = AppInstance.getProjectsLoader();
    
    private ContactsPersistence contactsPersistence;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
		if(savedInstanceState!=null) {
			projectId = savedInstanceState.getString("projectId");
		}
        projectModel = projectsLoader.getProjectById(projectId);
        contactsPersistence = new ContactsPersistence(getActivity());
        super.onCreate(savedInstanceState);
    }
	
    @Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("projectId", projectId);
		super.onSaveInstanceState(outState);
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {       
    	// Create content view.
        contentView = inflater.inflate(R.layout.fragment_project_info, container, false);                 
        bar = (ProgressBar) contentView.findViewById(R.id.feeProgressId);
        cardFee = contentView.findViewById(R.id.cardFeeId);
        paymentText = (TextView) contentView.findViewById(R.id.paymentTextId);
        //update UI
        updateContactListView(true);
        updateFeeListView();
        setupElasticScrollView();
        return contentView;
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	contactsPersistence.close();
    	contactsPersistence = null;
    }
    
    
    private void updateFeeListView() {
    	this.feeListView = (FeeListView) contentView.findViewById(R.id.listFeeId);
    	//not allow set focus by default. 
    	feeListView.setFocusable(false);
        //disable scroll the contact list. 
//    	feeListView.enableScroll(false);
    	feeListView.setFees(projectModel, bar, cardFee, paymentText);
        //make the fee height adjustable by the item count
	}

	public void setProjectId(String projectId) {
    	this.projectId = projectId;
    	if(this.getActivity()!=null) {
    		 throw new AssertionError("Must set project Id before onCreate()");
    	}
    }
    
    private void updateContactListView(boolean dataChanged) {

    	contactListView = (ContactListView) contentView.findViewById(R.id.listContact);
    	//not allow set focus by default. 
    	contactListView.setFocusable(false);
        //disable scroll the contact list. 
        contactListView.enableScroll(false);
        
        contactListView.setListAdjustable(true, Integer.MAX_VALUE);

		list = projectModel.getContacts();
		list.addAll(contactsPersistence.queryContacts(this.projectId));
		contactListView.setContacts(list);
    			
		addFromBtn = (Button) contentView.findViewById(R.id.buttonAddFromId);
		addNewBtn = (Button) contentView.findViewById(R.id.buttonAddNewId);
		addFromBtn.setVisibility(View.VISIBLE);
		addFromBtn.setOnClickListener(this);
		addNewBtn.setVisibility(View.VISIBLE);
		addNewBtn.setOnClickListener(this);
		Button buttonViewContacts = (Button) contentView.findViewById(R.id.buttonViewContacts);
		TextView contactText = (TextView)contentView.findViewById(R.id.contactTextId);
		FrameLayout line = (FrameLayout)contentView.findViewById(R.id.contactLineId);
		buttonViewContacts.setVisibility(View.GONE);
		contactText.setVisibility(View.GONE);
		line.setVisibility(View.GONE);
	}
    
    private void setupElasticScrollView() {
    	ElasticScrollView scrollView = (ElasticScrollView) contentView.findViewById(R.id.projectInfoScrollView);
    	scrollView.setMaxOverScrollDistance(0, 100); 
    }

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.buttonAddFromId) {
			Intent intent= new Intent(Intent.ACTION_PICK,  Contacts.CONTENT_URI);
	        startActivityForResult(intent, AppConstants.PICK_A_CONTACT);
		}
		if (v.getId() == R.id.buttonAddNewId) {
			Intent intent = new Intent(getActivity(), EnterNewContactActivity.class);
			intent.putExtra("projectId", projectId);
			startActivityForResult(intent, AppConstants.ADD_NEW_CONTACT_REQUEST);
		}
	}
	
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
	  super.onActivityResult(reqCode, resultCode, data);
	  switch (reqCode) {
	    case (AppConstants.PICK_A_CONTACT) :
	      if (resultCode == Activity.RESULT_OK) {
	        Uri contactData = data.getData();
	        this.contactsPersistence.fetchContactsToList(contactData, projectId, contactListView, list);
	      }
	    break;
	    case (AppConstants.ADD_NEW_CONTACT_REQUEST): {
			if (resultCode == Activity.RESULT_OK) { 
				ContactModel contactModel = new ContactModel();
				contactModel.setRecordId_id(projectId);
				contactModel.setFirstName(data.getStringExtra("FirstName"));
				contactModel.setLastName(data.getStringExtra("LastName"));
				contactModel.setPhone1(data.getStringExtra("PhoneNumber"));
				//Temporarily using Fax to store ImagePath
				contactModel.setProfileImagePath(data.getStringExtra("ImagePath"));
				if(Utils.isContactExist(list, contactModel)) {
					Toast.makeText(this.getActivity(), getString(R.string.contact_have_been_added), Toast.LENGTH_LONG)
					.show();
				} else {
					contactsPersistence.saveContact(contactModel);
					list.add(contactModel);
					contactListView.addContact(contactModel);
				}
			}
		}
		break;
	  }
	}
}


