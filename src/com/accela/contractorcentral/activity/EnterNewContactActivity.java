package com.accela.contractorcentral.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.fragment.EnterNewContactFragment;
import com.accela.record.model.ContactModel;
import com.flurry.android.FlurryAgent;

public class EnterNewContactActivity extends BaseActivity {

	EnterNewContactFragment fragment;
	ContactModel contactModel;
	String projectId;

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("EnterNewContactActivity");
		FlurryAgent.onPageView();
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
		setContentView(R.layout.activity_enter_new_contact);
		setActionBarTitle(R.string.enter_new_contact);
		Intent intent = this.getIntent();
		projectId = intent.getStringExtra("projectId");

		final Button btnSave = (Button) this.findViewById(R.id.buttonRight);
		btnSave.setText(this.getResources().getString(R.string.action_save));
		btnSave.setVisibility(View.VISIBLE);
		btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveContact();
			}
		});

		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		fragment = new EnterNewContactFragment();
		ft.add(R.id.rootContainer, fragment);
		ft.commit();

	}

	private void saveContact() {
		String firstName = fragment.getContactFirstName();
		String lastName = fragment.getContactLastName();
		String phoneNumber = fragment.getContactPhone();
		String imagePath = fragment.getImagePath();

		if (firstName.length() == 0 || lastName.length() == 0 || phoneNumber.length() == 0) {
			Toast toast = Toast.makeText(getApplicationContext(), "Please fill contact details", Toast.LENGTH_LONG);
			toast.show();
		} else {
			Intent returnIntent = new Intent();
			returnIntent.putExtra("FirstName", firstName);
			returnIntent.putExtra("LastName", lastName);
			returnIntent.putExtra("PhoneNumber", phoneNumber);
			returnIntent.putExtra("ImagePath", imagePath);
			setResult(Activity.RESULT_OK, returnIntent);
			finish();
		}
	}

}
