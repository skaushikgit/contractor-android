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

package com.accela.contractorcentral.activity;



import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.utils.APIHelper;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.framework.authorization.AMAuthManager;
import com.accela.mobile.view.CustomDialog;
import com.flurry.android.FlurryAgent;


public class BaseActivity extends ActionBarActivity {

	private TextView textTitle;
	private TextView textSmallTitle1;
	private TextView textSmallTitle2;
	private View  buttonBack;
	private Button  buttonRight;
	private View actionbarView;
	private ProgressDialog progressDialog;
	private View buttonPlus;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
	 
		//Lock the orientation
//		ActivityUtils.lockActivityOrientation(this);
		ActivityUtils.setActivityPortrait(this);
		ActionBar actionBar = this.getSupportActionBar(); 
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, 
				Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		actionbarView = LayoutInflater.from(this).inflate(R.layout.action_bar, null); 
		textTitle =(TextView) actionbarView.findViewById(R.id.textTitle);
		textSmallTitle1= (TextView) actionbarView.findViewById(R.id.textSmallTitle1);
		textSmallTitle2= (TextView) actionbarView.findViewById(R.id.textSmallTitle2);
		actionBar.setCustomView(actionbarView, lp);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		APIHelper.setPhoneStatusBarColor(this, getResources().getColor(R.color.default_phone_system_bar));
		
		buttonBack =  actionbarView.findViewById(R.id.buttonBack);
		buttonBack.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		buttonRight = (Button) actionbarView.findViewById(R.id.buttonRight);
		buttonRight.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onRightButtonPress();
			}
		});
		
		//jzhong( 4/2/2015) many issue!! remove it at this moment. need to discuss a good solution. 
	/*	int hours = (int) ( (System.currentTimeMillis()-AppContext.loginTime) / (1000*60*60) );
		if(hours>23 || SDKManager.getInstance().getAccelaMobile().getAuthorizationManager().getAccessToken() == null){
			if(this instanceof LoginActivity){
				return;
			}
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			this.finish();
			return;
		} */
	}
	
	public void setActionBarTitle(String text) {
		int splitIndex = text.indexOf("\n");
		if(splitIndex >0) {
			//display two line text in action bar
			String text1 = text.substring(0, splitIndex);
			String text2 = text.substring(splitIndex+1);
			textSmallTitle1.setText(text1);
			textSmallTitle1.setVisibility(View.VISIBLE);
			textSmallTitle2.setText(text2);
			textSmallTitle2.setVisibility(View.VISIBLE);
			textTitle.setVisibility(View.GONE);
		} else {
			textTitle.setText(text);
			
			textSmallTitle1.setVisibility(View.GONE);
			textSmallTitle2.setVisibility(View.GONE);
			textTitle.setVisibility(View.VISIBLE);
		}
	}
	
	public void setActionBarTitle(int stringId) {
		setActionBarTitle(getString(stringId));
	}
	
	public void showBackButton(boolean show) {
		buttonBack.setVisibility(show ? View.VISIBLE: View.INVISIBLE);
	}
	
	public void showRightButton(boolean show, String text) {
		buttonRight.setVisibility(show ? View.VISIBLE: View.GONE);
		if(text!=null) {
			buttonRight.setText(text);
		}
	}
	
	public void setActionBarColor(int color) {
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(color)); 
		actionbarView.setBackgroundColor(color);
		//actionBar.setDisplayShowTitleEnabled(true);
		//actionBar.setDisplayShowTitleEnabled(false); 
	}
	
	public void exitActivityWithAnimated() {
		finish();
		overridePendingTransition(R.anim.zoomin, R.anim.moveout_right);
	}
	
	@Override
	public void onBackPressed() {
		exitActivityWithAnimated();
	}
	
	
	
	public void showScheduleBtn(boolean show, boolean animated){
		if(buttonPlus == null && show) {
			DisplayMetrics displayMetrics = new DisplayMetrics();   
		    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int)(displayMetrics.density*60), (int) (displayMetrics.density*60));
			params.setMargins(0, 0, (int)(displayMetrics.density*10), (int)(displayMetrics.density*10));
			params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
			final ImageView imageView = new ImageView(this);
			
			imageView.setImageResource(R.drawable.btn_plus);
			imageView.setLayoutParams(params);
			this.addContentView(imageView, params);
			buttonPlus = imageView;
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					scheduleInspection();
				}
			});
		} 
		
		if(show) {
			if(buttonPlus.getVisibility() == View.VISIBLE) {
				//it is show, do nothing
			} else {
				buttonPlus.setVisibility(View.VISIBLE);
				if(animated) {
					Animation animation = AnimationUtils.loadAnimation(this, R.anim.movein_from_bottom);
					buttonPlus.startAnimation(animation);
				}
			}
		} else {
			if(buttonPlus.getVisibility() != View.INVISIBLE) {
				//do nothing
			} else {
				buttonPlus.setVisibility(View.GONE);
				if(animated) {
					Animation animation = AnimationUtils.loadAnimation(this, R.anim.moveout_to_bottom);
					buttonPlus.startAnimation(animation);
				}
			}
		}
		
		
	}
	
	protected void scheduleInspection() {
		// TODO Auto-generated method stub
		 Intent intent = new Intent(this, MenuActivity.class);
		 startActivity(intent);
	}

	public void showProgressDialog( String message,  boolean cancelable) {
		if(progressDialog!=null){
			progressDialog.cancel();
		}
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(message != null ? message: getResources().getString(
				R.string.processing_and_wait));
		progressDialog.setCanceledOnTouchOutside(cancelable);
		progressDialog.setCancelable(cancelable);
		progressDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				
			}
		});
		progressDialog.show();
	}

	public void closeProgressDialog() {
		if(progressDialog!=null){
			progressDialog.cancel();
			progressDialog = null;
		}
	}
	
	protected void showAlertMessage(String message) {
		CustomDialog.Builder alertDialog = new CustomDialog.Builder(this)
		.setTitle(getString(R.string.app_name)).setMessage(
				message);
		alertDialog.setPositiveButton(R.string.action_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						onAlertMessageDismiss();
					}
				});
		alertDialog.show();
	}
	
	protected void showLogoutConfirmMessage() {
		CustomDialog.Builder alertDialog = new CustomDialog.Builder(this)
		.setTitle(getString(R.string.sign_out)).setMessage(
				getString(R.string.logout_confirm_message));
		alertDialog.setNegativeButton(R.string.action_cancel, null);
		alertDialog.setPositiveButton(R.string.action_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						logout();
					}
				});
		alertDialog.show();
	}
	
	public void logout() {
		AppConstants.isLoggin = false;
		AMAuthManager.getInstance().getAMAuthorizationService().logout();
		AppInstance.clearAll();
		Intent intent = new Intent(BaseActivity.this, HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}
	
	protected void onLogout() {
		
	}
	
	protected void onAlertMessageDismiss() {
		
	}
	
	protected void onRightButtonPress() {
		
	}
	
	
	
	
}