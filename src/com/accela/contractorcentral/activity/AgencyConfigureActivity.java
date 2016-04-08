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




import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.service.AgencyLoader;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.view.ElasticScrollView;
import com.accela.contractorcentral.view.WebImageView;
import com.accela.framework.model.AgencyModel;
import com.accela.mobile.AMLogger;
import com.accela.mobile.view.CustomDialog;
import com.flurry.android.FlurryAgent;




public class AgencyConfigureActivity extends BaseActivity  {

	AgencyModel agency;
	ElasticScrollView scrollView;
	WebImageView imageViewLogo;
	Button buttonLogin;
	Button buttonRemove;
	AgencyLoader agencyLoader = AppInstance.getAgencyLoader();
	
	boolean exitAfterShowMessage;
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("AgencyConfigureActivity");
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

		setContentView(R.layout.activity_agency_configure);
		this.setActionBarTitle(R.string.app_name);
		Intent intent = this.getIntent();
		agency = (AgencyModel) intent.getExtras().getSerializable("agency");
		if(agency==null) {
			finish();
			AMLogger.logError("Please pass a agency into AgencyConfigureActivity");
			return;
		}
		scrollView = (ElasticScrollView) findViewById(R.id.scrollView);
		scrollView.setMaxOverScrollDistance(60, 60);
		
		imageViewLogo = (WebImageView) findViewById(R.id.imageViewLogo);
		imageViewLogo.setAgency(agency);
		
		buttonLogin = (Button) findViewById(R.id.buttonLogin);
		buttonLogin.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				linkAgency();
			}
		});
		
		buttonRemove = (Button) findViewById(R.id.buttonRemove);
		buttonRemove.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				confirmRemoveAgency();
			}
		});
		
		
		setActionBarTitle(String.format(getString(R.string.format_agency_log_in), agency.getName()));
		
		TextView textTitle = (TextView) findViewById(R.id.textTitle);
		textTitle.setText(String.format(getString(R.string.format_agency_login), agency.getName()));
		
		TextView textDesc = (TextView) findViewById(R.id.textDesc);
		
		
		EditText editPasswordConfirm = (EditText) findViewById(R.id.editPasswordConfirm);
		editPasswordConfirm.setVisibility(View.GONE);
		EditText editUserName = (EditText) findViewById(R.id.editUserName);
		editUserName.setHint(R.string.username);
		EditText editPassword = (EditText) findViewById(R.id.editPassword);
		editPassword.setHint(R.string.password);
		
		
		if(agency.getAccountId() != null) {
			//remove agency
			textDesc.setText(String.format(getString(R.string.currently_sign_in_agency), agency.getName()));
			editUserName.setText(agency.getAccountId());
			editUserName.setVisibility(View.GONE);
			editPassword.setVisibility(View.GONE);
			buttonLogin.setText(R.string.save_changes);
			buttonLogin.setVisibility(View.GONE);
			buttonRemove.setVisibility(View.VISIBLE);
		} else {
			//add agency
			textDesc.setText(String.format(getString(R.string.format_agency_login_desc), agency.getName()));
			buttonRemove.setVisibility(View.GONE);
		}
		
		
	}
	
	protected void linkAgency() {
		EditText editUserName = (EditText) findViewById(R.id.editUserName);
		EditText editPassword = (EditText) findViewById(R.id.editPassword);
		EditText editPasswordConfirm = (EditText) findViewById(R.id.editPasswordConfirm);
		
		String userName = editUserName.getEditableText().toString().trim();
		String password1 = editPassword.getEditableText().toString();
		String password2 = editPasswordConfirm.getEditableText().toString();
		if(userName.length() ==0) {
			this.showAlertMessage(getString(R.string.input_login_info_missing));
			editUserName.requestFocus();
			return;
		} else if(password1.length()==0) {
			this.showAlertMessage(getString(R.string.input_login_info_missing));
			editPassword.requestFocus();
			return;
		} else if(editPasswordConfirm.getVisibility() == View.VISIBLE && password1.compareTo(password2) !=0) {
			editPassword.requestFocus();
			this.showAlertMessage(getString(R.string.passwords_do_not_match));
			return;
		}
		
		showProgressDialog(null, false);
		agencyLoader.linkAgency(agency.getName(), "PROD", userName , password1, 
				new AgencyLoader.LinkAgencyDelegate() {
					
					@Override
					public void onComplete(int errorCode) {
						closeProgressDialog();
						if(errorCode==AgencyLoader.LINK_AGENCY_SUCCESSFULLY) {
							//showAlertMessage(getString(R.string.link_agency_successfully));
							setResult(RESULT_OK);
							AgencyConfigureActivity.this.exitActivityWithAnimated();
						} else if(errorCode == AgencyLoader.LINK_AGENCY_ERROR_LOGIN_INFO) {
							showAlertMessage(getString(R.string.link_agency_error_login_info));
						} else {
							showAlertMessage(getString(R.string.link_agency_unknown_error));
						}
						
					}
				});
	}
	
	protected void confirmRemoveAgency() {
		CustomDialog.Builder alertDialog = new CustomDialog.Builder(this)
		.setTitle(getString(R.string.app_name)).setMessage(
				R.string.remove_agency_confirm);
		alertDialog.setPositiveButton(R.string.action_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						removeAgency();
					}
				});
		alertDialog.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		alertDialog.show();
	}
	
	protected void removeAgency() {
		showProgressDialog(null, false);
		agencyLoader.removeAgency(agency.getName(), "PROD",  
				new AgencyLoader.LinkAgencyDelegate() {
					
					@Override
					public void onComplete(int errorCode) {
						closeProgressDialog();
						if(errorCode==AgencyLoader.REMOVE_AGENCY_SUCCESSFULLY) {
							exitAfterShowMessage = true;
							showAlertMessage(String.format(getString(R.string.remove_agency_successfully), agency.getName()));
						} else {
							showAlertMessage(getString(R.string.remove_agency_failed));
						}
						
					}
				});
	}
	
	
	@Override
	public void onResume(){
		super.onResume();
	}
	
	@Override
	protected void onAlertMessageDismiss() {
		if(exitAfterShowMessage) {
			exitActivityWithAnimated();
		}
	}
	
	
	

}