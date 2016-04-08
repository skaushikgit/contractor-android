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
 *   Created by jzhong on 3/26/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */

package com.accela.contractorcentral.activity;




import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.AppConstants.AgencyListType;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.InstantService;
import com.accela.contractorcentral.service.AppInstance.AppServiceDelegate;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.view.AgencyListView;
import com.accela.framework.authorization.AMAuthManager;
import com.accela.framework.model.AgencyModel;
import com.accela.framework.model.CivicIdProfileModel;
import com.accela.mobile.AMLogger;
import com.flurry.android.FlurryAgent;


public class AgencyListActivity extends BaseActivity  {


	AgencyListType agencyListType;
	private AgencyListView agencyListView ;
	private final static int REQUEST_ADD_AGENCY = 1;
	private AgencyModel agency;
	CivicIdProfileModel civicProfile;
	private static String version;
	private static String UID = "";
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("AgencyListActivity");
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

		setContentView(R.layout.activity_agency_setting);
		TextView textView = (TextView) this.findViewById(R.id.versionTextId);
		try {
			version = this.getString(R.string.Version) + " " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			textView.setText(version);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			AMLogger.logWarn(e.toString());
		}
		Intent intent = this.getIntent();
		agencyListType = (AgencyListType) intent.getSerializableExtra("agencyListType");
		Button buttonLogout = (Button) findViewById(R.id.buttonLogout);
		Button buttonSupport = (Button) findViewById(R.id.buttonSupport);
		
		agencyListView = (AgencyListView) findViewById(R.id.listViewAgency);
		if(agencyListType == AgencyListType.LOGIN){
			StringBuilder sb = new StringBuilder();
			if(AMAuthManager.getInstance().getProfile()!=null && AMAuthManager.getInstance().getProfile().getFirstName()!=null)
				sb.append(" ").append(AMAuthManager.getInstance().getProfile().getFirstName());
			if(AMAuthManager.getInstance().getProfile()!=null && AMAuthManager.getInstance().getProfile().getLastName()!=null)
				sb.append(" ").append(AMAuthManager.getInstance().getProfile().getLastName());
			this.setActionBarTitle(R.string.success);
			agencyListView.setHeaderTitle(getString(R.string.welcome_) + sb.toString(), getString(R.string.login_description));
			AppInstance.getAppSettingsLoader().loadAppSettings(null, true);
			buttonLogout.setVisibility(View.GONE);
			buttonSupport.setVisibility(View.GONE);
		}else if(agencyListType == AgencyListType.ADDAGENCY) {
			setActionBarTitle(R.string.add_new_agency);
			agencyListView.setHeaderTitle(getString(R.string.where_you_licensed), getString(R.string.where_you_licensed_desc));
			buttonLogout.setVisibility(View.GONE);
			buttonSupport.setVisibility(View.GONE);
		}else if(agencyListType == AgencyListType.MYAGENCY) {
			this.setActionBarTitle(R.string.Settings);
			agencyListView.setHeaderTitle(getString(R.string.your_agencies), getString(R.string.your_agencies_desc));
			buttonLogout.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					showLogoutConfirmMessage();
				}
			});
			buttonSupport.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(civicProfile != null) {
						if(civicProfile.getEmail() != null) { 
							UID = civicProfile.getEmail();	
							setEmailDetails(UID);
						}						
					} else {
						AppServiceDelegate<CivicIdProfileModel> delegate = new AppServiceDelegate<CivicIdProfileModel>() {
							@Override
							public void onSuccess(List<CivicIdProfileModel> response) {
								CivicIdProfileModel civicModel = response.get(0);
								if(civicModel != null) {
									if(civicModel.getEmail() != null) {
										UID = civicModel.getEmail();
										setEmailDetails(UID);
									}
								}
							}
							@Override
							public void onFailure(Throwable error) {
								AMLogger.logWarn(error.toString());
								setEmailDetails(UID);
							}
						};
						InstantService.getUserProfile(delegate);
					}
				}
			});
		} else if(agencyListType == AgencyListType.CHOOSEAGENCY) {
			this.setActionBarTitle(R.string.choose_agencies);
			agencyListView.setHeaderTitle(getString(R.string.where_you_licensed), getString(R.string.where_you_licensed_desc));
			buttonLogout.setVisibility(View.GONE);
		}
		
		agencyListView.loadAgency(agencyListType);
		agencyListView.setMaxOverScrollDistance(60, 60);
		agencyListView.OnClickAgencyListener(new AgencyListView.OnClickAgencyListener() {
			@Override
			public void onClickAgency(AgencyModel agency) {
				AgencyListActivity.this.agency = agency;
				ActivityUtils.startAgencyConfigureActivity(AgencyListActivity.this, agency, REQUEST_ADD_AGENCY);
			}
		});
		
	}
	
	
	@Override
	public void onResume(){
		if(agencyListType == AgencyListType.LOGIN) {
			agencyListView.enableCheckAndAutoJumpLandingPage(true);
		}
		civicProfile = AMAuthManager.getInstance().getProfile();
		super.onResume();
	}
	
	@Override
	public void onPause() {
		if(agencyListType == AgencyListType.LOGIN) {
			agencyListView.enableCheckAndAutoJumpLandingPage(false);
		}
		super.onPause();
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_ADD_AGENCY && resultCode == RESULT_OK) {
			String headerTitle = String.format(getString(R.string.format_agency_add_successfully), agency.getName());
			String headerDesc = getString(R.string.agency_add_successfully_desc);
			agencyListView.setHeaderTitle(headerTitle, headerDesc);
			showRightButton(true, getString(R.string.Done));
		}
	}
	 
	protected void onRightButtonPress() {
		this.onBackPressed();
	}
	
	@Override
	public void onBackPressed() {
		if(agencyListType == AgencyListType.LOGIN || agencyListType == AgencyListType.MYAGENCY) {
			//reload projects list if agency modified.
			if(AppInstance.getAgencyLoader().isAgencyModified()) {
				AppInstance.getAgencyLoader().setAgencyModified(false);
				AppInstance.getProjectsLoader().loadAllProjects(true);
			}
		}
		exitActivityWithAnimated();
	}
	
	private void setEmailDetails(String UID) {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String timeStamp = df.format(c.getTime());
		
		String to = getString(R.string.support_email_to) ;
		String subject = getString(R.string.support_email_subject)+UID;
		String body = getString(R.string.support_email_body)+getString(R.string.time_stamp)+timeStamp+"\n"+getString(R.string.uid)+UID+"\n"+getString(R.string.app_version)+version;

		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, body);
		emailIntent.setType("message/rfc822");
		startActivity(Intent.createChooser(emailIntent, "Choose an Email client :"));
	}

}