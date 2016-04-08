package com.accela.contractorcentral.fragment;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.activity.EnterNewContactActivity;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.persistence.ContactsPersistence;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.utils.Utils;
import com.accela.contractorcentral.view.ContactListView;
import com.accela.contractorcentral.view.ElasticScrollView;
import com.accela.record.model.ContactModel;

public class EditContactFragment extends Fragment implements OnClickListener{

	private EditText editFirstName;
	private EditText editLastName;
	private EditText editPhone;

	private String projectId;

	private String fname, lname;

	private String phone;

	private ContactListView contactListView;

	private ProjectModel projectModel;

	private View contentView;

	private List<ContactModel> list;

	private ContactsPersistence contactsPersistence;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		contactsPersistence = new ContactsPersistence(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {       
		// Create content view.
		contentView = inflater.inflate(R.layout.fragment_project_edit_contact, container, false);    
		projectModel = AppInstance.getProjectsLoader().getProjectById(projectId);
		editFirstName = (EditText) contentView.findViewById(R.id.editContactFirstnameId);
		editLastName = (EditText) contentView.findViewById(R.id.editContactLastnameId);
		editPhone = (EditText) contentView.findViewById(R.id.editContactPhoneId);
		editPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
		editFirstName.setText(fname);
		editLastName.setText(lname);
		editPhone.setText(phone);
		//update UI
		updateContactListView();
		setupElasticScrollView();
		return contentView;
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		contactsPersistence.close();
		contactsPersistence = null;
	}

	public String getContactFirstName(){
		return editFirstName.getText().toString();
	}

	public String getContactLastName(){
		return editLastName.getText().toString();
	}

	public String getContactPhone(){
		return editPhone.getText().toString();
	}

	private void updateContactListView() {
		// TODO Auto-generated method stub
		contactListView = (ContactListView) contentView.findViewById(R.id.listContact);
		//not allow set focus by default. 
		contactListView.setFocusable(false);
		//disable scroll the contact list. 
		contactListView.enableScroll(false);

		contactListView.setListAdjustable(true, Integer.MAX_VALUE);

		list = projectModel.getContacts();
		list.addAll(contactsPersistence.queryContacts(this.projectId));
		contactListView.setContacts(list);

		Button addFromBtn = (Button) contentView.findViewById(R.id.buttonAddFromId);
		Button addNewBtn = (Button) contentView.findViewById(R.id.buttonAddNewId);
		addFromBtn.setVisibility(View.VISIBLE);
		addFromBtn.setOnClickListener(this);
		addNewBtn.setOnClickListener(this);
		addNewBtn.setVisibility(View.VISIBLE);
		Button buttonViewContacts = (Button) contentView.findViewById(R.id.buttonViewContacts);
		TextView contactText = (TextView)contentView.findViewById(R.id.contactTextId);
		FrameLayout line = (FrameLayout)contentView.findViewById(R.id.contactLineId);
		buttonViewContacts.setVisibility(View.GONE);
		contactText.setVisibility(View.GONE);
		line.setVisibility(View.GONE);

		contactListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				saveContact(position);
			}
		});
	}

	private void saveContact(int position) {
		// TODO Auto-generated method stub
		if(list==null)
			return;
		ContactModel model = list.get(position);
		Intent returnIntent = new Intent();
		StringBuffer nameSb = new StringBuffer();
		if(model.getFirstName()!=null)
			nameSb.append(model.getFirstName());
		if(model.getMiddleName()!=null)
			nameSb.append(" ").append(model.getMiddleName());

		if(nameSb.length()>0){
			returnIntent.putExtra("ContactFirstName", nameSb.toString());
			returnIntent.putExtra("ContactLastName", model.getLastName());
		}else if(model.getOrganizationName()!=null){
			returnIntent.putExtra("OrganizationName", model.getOrganizationName());
		}
		
		returnIntent.putExtra("ContactPhone", Utils.getContactInfo(model));
		returnIntent.putExtra("PreferredChannel", model.getPreferredChannel_value());

		if(model.getProfileImagePath()!=null){
			returnIntent.putExtra("ProfileImagePath", model.getProfileImagePath());
		}
		getActivity().setResult(AppConstants.RESULT_OK, returnIntent);
		getActivity().finish();
	}	

	public void init(String projectId, String fname, String lname, String contact) {
		// TODO Auto-generated method stub
		this.projectId = projectId;
		this.fname = fname;
		this.lname = lname;
		this.phone = contact;
	}

	private void setupElasticScrollView() {
		// TODO Auto-generated method stub
		ElasticScrollView scrollView = (ElasticScrollView) contentView.findViewById(R.id.editContactScrollViewId);
		scrollView.setMaxOverScrollDistance(0, 100); 
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
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
