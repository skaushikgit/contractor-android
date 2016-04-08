package com.accela.contractorcentral.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.AppContext;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.framework.service.CorelibManager;
import com.accela.mobile.AMLogger;
import com.flurry.android.FlurryAgent;

public class RefreshTokenActivity extends BaseActivity {
	
	private static boolean refreshing;
	private static long lastRefreshTokenTime;
	View loadingContainer;
	TextView textLoading;
	public static boolean startRefreshTokenActivity() {
		if(!AppConstants.isLoggin)
			return false;
		//TODO: check the current token refresh time
		if(Math.abs(lastRefreshTokenTime - System.currentTimeMillis()) < 20 * 1000) {
			return false;
		}
		
		if(refreshing) {
			
			return false;
		} else {
			refreshing = true;
			Intent intent = new Intent(AppContext.context, RefreshTokenActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
			AppContext.context.startActivity(intent);
			return true;
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("RefreshTokenActivity");
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
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		//           WindowManager.LayoutParams.FLAG_FULLSCREEN);
		refreshing = true;
		ActionBar actionBar = this.getSupportActionBar(); 
		actionBar.hide();
		setContentView(R.layout.activity_refresh_token);
		loadingContainer = (View) findViewById(R.id.loadingContainer);
		textLoading = (TextView) findViewById(R.id.textLoading);
	
		textLoading.setText(R.string.refresh_token_message);
		
		new AsyncTask<Void, Void, Boolean> () {

			@Override
			protected void onPostExecute(Boolean result) {
				if(result) {
					Toast.makeText(RefreshTokenActivity.this, "Refresh token sccucessfully", Toast.LENGTH_SHORT).show();
					//AppInstance.clearAll();
					AppInstance.getProjectsLoader().loadAllProjects(false);
					finish();
					overridePendingTransition(0, 0);
				} else {
					Toast.makeText(RefreshTokenActivity.this, "Refresh token failed, please login again", Toast.LENGTH_SHORT).show();
					logout();	
				}
				super.onPostExecute(result);
			}

			@Override
			protected Boolean doInBackground(Void... params) {
				Boolean result = false;
				lastRefreshTokenTime = System.currentTimeMillis();
				try {
					AMLogger.logInfo("Start refreshing token");
					result = CorelibManager.getInstance().getAMAuthorizationService().refreshAuthorized();
				} catch (Exception e) {
					AMLogger.logInfo("Refreshing token exception!!!!!");
					e.printStackTrace();
				}
				 
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return result;
			} 
			
			
			
			
		}.execute();
		
		
		
	}

	@Override
	protected void onDestroy() {
		refreshing = false;
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		//don't allow exit activity by press back key while refreshing token
	}
	
	
}
