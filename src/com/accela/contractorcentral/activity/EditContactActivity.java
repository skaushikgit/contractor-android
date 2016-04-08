package com.accela.contractorcentral.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.fragment.EditContactFragment;
import com.flurry.android.FlurryAgent;



public class EditContactActivity extends BaseActivity{
	private EditContactFragment fragment;
	private LinearLayout.LayoutParams params;
	private LinearLayout layout;
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("EditContactActivity");
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
		setActionBarTitle(R.string.Edit_onsite_contact);
		Intent intent = this.getIntent();
		String projectId = intent.getStringExtra("projectId");
	    String fname =intent.getStringExtra("editContactFirstName");
	    String lname =intent.getStringExtra("editContactLastName");
	    String contact = intent.getStringExtra("editContactNumber");
		setContentView(R.layout.activity_edit_contact);
		layout = (LinearLayout)this.findViewById(R.id.actionCenterLayoutId);
		params = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
		params.setMargins(0, 0, 0, 0);
		layout.setLayoutParams(params);
		final Button btnSave = (Button)this.findViewById(R.id.buttonRight);
		btnSave.setText(this.getResources().getString(R.string.action_save));
		btnSave.setVisibility(View.VISIBLE);
		btnSave.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				saveContact();
				btnSave.setVisibility(View.GONE);
			}
		});
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		fragment = new EditContactFragment();
		fragment.init(projectId, fname, lname, contact);
		ft.add(R.id.rootContainer,  fragment);
		ft.commit();
	}
	
	

	private void saveContact() {
		// TODO Auto-generated method stub
		Intent returnIntent = new Intent();
		returnIntent.putExtra("ContactFirstName", fragment.getContactFirstName());
		returnIntent.putExtra("ContactLastName", fragment.getContactLastName());
		returnIntent.putExtra("ContactPhone", fragment.getContactPhone());
		setResult(AppConstants.RESULT_OK, returnIntent);
		params.setMargins(0, 0, 40, 0);
		layout.setLayoutParams(params);
		finish();
	}
}
