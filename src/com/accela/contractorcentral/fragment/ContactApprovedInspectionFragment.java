package com.accela.contractorcentral.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.activity.EnterNewContactActivity;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.persistence.ContactsPersistence;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.InspectorLoader.InspectionInspectorItem;
import com.accela.contractorcentral.utils.Utils;
import com.accela.contractorcentral.view.ContactListView;
import com.accela.contractorcentral.view.ElasticScrollView;
import com.accela.mobile.AMLogger;
import com.accela.record.model.ContactModel;
import com.accela.record.model.RecordInspectionModel;

@SuppressLint({ "ServiceCast", "ResourceAsColor" }) public class ContactApprovedInspectionFragment extends Fragment implements OnClickListener{

	private View contentView;
	private ContactListView contactListView;
	
	private List<ContactModel> contactList = new ArrayList<ContactModel>();
	private ProjectModel projectModel;
	private String projectId;
	private ContactsPersistence contactsPersistence;
	private Button addFromBtn, addNewBtn;
	private boolean isInspectionFailed = false;
//	private String permitId;
	private ContactListView inspectorListView;
	private RecordInspectionModel inspection; 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		contactsPersistence = new ContactsPersistence(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = inflater.inflate(R.layout.fragment_contact_approved_inspection, container, false);
		projectModel = AppInstance.getProjectsLoader().getProjectById(projectId);
		addFromBtn = (Button) contentView.findViewById(R.id.buttonAddFromId);
		addNewBtn = (Button) contentView.findViewById(R.id.buttonAddNewId);
		addFromBtn.setOnClickListener(this);
		addNewBtn.setOnClickListener(this);
		if(projectModel==null)
			return contentView;
		updateContactListView();
		setupElasticScrollView();

		// add PhoneStateListener for monitoring
//		PhoneListenerUtility phoneListener = new PhoneListenerUtility();
//		TelephonyManager telephonyManager = 
//				(TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
//		// receive notifications of telephony state changes 
//		telephonyManager.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);


		contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ContactModel contactModel = contactList.get(position);
				int preferredChannel = Utils.getContactPreferredChannel(contactModel);
				switch(preferredChannel) {
				case 1:
					Utils.sendEmail(getActivity(), contactModel);
					break;
				default:
					Utils.callPhone(getActivity(), contactModel);
					break;
				}
			}
		});

		return contentView;
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		contactsPersistence.close();
		contactsPersistence = null;
	}

	public void init(String projectId, RecordInspectionModel inspection, boolean isFailed) {
		this.projectId = projectId;
		this.inspection = inspection;
		this.isInspectionFailed = isFailed;
	}

	private void updateContactListView() {
		contactListView = (ContactListView) contentView.findViewById(R.id.listContact);
		contactListView.setFocusable(false);
		//disable scroll the contact list. 
		contactListView.enableScroll(false);
		contactListView.setListAdjustable(true, Integer.MAX_VALUE);
		
		if(inspection!=null){
			InspectionInspectorItem item = AppInstance.getInspectorLoader().getInspectorByInspectionId(inspection.getId());
			if(item!=null && item.listAllInspectors!=null && item.listAllInspectors.size()>0){
				LinearLayout inspectorContactContainer = (LinearLayout) contentView.findViewById(R.id.inspectorContactId);
				inspectorContactContainer.setVisibility(View.VISIBLE);
					inspectorListView = (ContactListView) contentView.findViewById(R.id.listContactInspector);
					inspectorListView.setFocusable(false);
					inspectorListView.enableScroll(false);
					inspectorListView.setListAdjustable(true, Integer.MAX_VALUE);
					inspectorListView.setContacts(Utils.generateContactList(getActivity(), item.listAllInspectors));
			}
		}
		 

		contactList.clear();
		contactList.addAll(projectModel.getContacts());
		contactList.addAll(contactsPersistence.queryContacts(this.projectId));
		//add inspection onsite contact
		if(inspection!=null) {
			AMLogger.logInfo("Generate contact model from inspection");
			ContactModel contact = Utils.generateContactByInspection(inspection);
			if(contact!=null) {
				AMLogger.logInfo("Generate contact model successful and add it into list");
				contactList.add(contact);
			} else {
				AMLogger.logInfo("No onsite contact in inspection?????");
			}
		} else {
			AMLogger.logInfo("No inspection to get onsite contact");
		}
		contactListView.setContacts(contactList);
		
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.buttonAddFromId) {
			Intent intent= new Intent(Intent.ACTION_PICK,  ContactsContract.Contacts.CONTENT_URI);
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
				this.contactsPersistence.fetchContactsToList(contactData, projectId, contactListView, contactList);
			}
		break;
		case (AppConstants.ADD_NEW_CONTACT_REQUEST): {
			if (resultCode == Activity.RESULT_OK) { 
				ContactModel contactModel = new ContactModel();
				contactModel.setRecordId_id(projectId);
				contactModel.setFirstName(data.getStringExtra("FirstName"));
				contactModel.setLastName(data.getStringExtra("LastName"));
				contactModel.setPhone1(data.getStringExtra("PhoneNumber"));
				contactModel.setProfileImagePath(data.getStringExtra("ImagePath"));
				if(Utils.isContactExist(contactList, contactModel)) {
					Toast.makeText(this.getActivity(), getString(R.string.contact_have_been_added), Toast.LENGTH_LONG)
					.show();
				} else {
					contactsPersistence.saveContact(contactModel);
					contactList.add(contactModel);
					contactListView.addContact(contactModel);
				}
			}
		}
		break;
		}
	}

	private void setupElasticScrollView() {
		// TODO Auto-generated method stub
		ElasticScrollView scrollView = (ElasticScrollView) contentView.findViewById(R.id.contactApprovedInspectionId);
		scrollView.setMaxOverScrollDistance(0, 100); 
	}
}