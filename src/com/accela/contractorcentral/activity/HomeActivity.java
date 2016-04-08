package com.accela.contractorcentral.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.accela.framework.authorization.AMAuthManager;
import com.accela.framework.authorization.AMAuthManager.AuthType;
import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import io.fabric.sdk.android.Fabric;
import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.AppContext;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.activity.TermsOfUseActivity;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.utils.ActivityUtils;

public class HomeActivity extends Activity {
	 private int ACCEPT_TERM_REQUEST;
	 
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("HomeActivity");
		FlurryAgent.onPageView();
		FlurryAgent.onStartSession(this, AppConstants.FLURRY_APIKEY);
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Fabric.with(this, new Crashlytics());
		this.setContentView(R.layout.activity_home);
		ActivityUtils.lockActivityOrientation(this);
		AMAuthManager.getInstance().initialize(AppContext.appId, AppContext.appSecret, AppContext.apiHost, AppContext.authHost, AppContext.appScopesCitizen, false, AuthType.Accela);
		AMAuthManager.getInstance().getAMAuthorizationService().setAmIsRemember(true);
		AMAuthManager.getInstance().getAMAuthorizationService().setUrlSchema(AppContext.schema);
		AMAuthManager.getInstance().getAMAuthorizationService().setEnvironment(AppContext.environment);
		AMAuthManager.getInstance().setIsAuthBackUI(false);
	/*	SharedPreferences userPreferences = UserPreferences.getSharedPreferences(getApplicationContext());
		//remove auto login
		AppContext.loginTime = userPreferences.getLong(AppConstants.LAST_LOGIN_TIME, System.currentTimeMillis());
		int hours = (int) ( (System.currentTimeMillis()-AppContext.loginTime) / (1000*60*60) );
		if(hours>=0 && hours<23 && SDKManager.getInstance().getAccelaMobile().getAuthorizationManager().getAccessToken() != null){
			
			AMLogger.logInfo(SDKManager.getInstance().getAccelaMobile().getAuthorizationManager().getAccessToken());
			String token = SDKManager.getInstance().getAccelaMobile().getAuthorizationManager().getAccessToken();
			SDKManager.getInstance().getAccelaMobile().getAuthorizationManager().setClientInfo(AppContext.appIdCitizen, AppContext.appSecretCitizen, AppContext.environment.toString(), null, AppContext.authHost, AppContext.apiHost);
			ActivityUtils.startAgencyListActivity(this, AgencyListType.LOGIN);
		} */
	
		Button buttonLogin = (Button) findViewById(R.id.buttonLogin);
		TextView learnMoreId = (TextView) findViewById(R.id.learnMoreId);
		learnMoreId.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(HomeActivity.this, AboutActivity.class);
				startActivity(intent);
			}
		});
		
		buttonLogin.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AppInstance.clearAll();
				AMAuthManager.getInstance().initialize(AppContext.appId, AppContext.appSecret, AppContext.apiHost, AppContext.authHost, AppContext.appScopesCitizen, false, AuthType.Accela);
				AMAuthManager.getInstance().getAMAuthorizationService().setAmIsRemember(true);
				AMAuthManager.getInstance().getAMAuthorizationService().setUrlSchema(AppContext.schema);
				AMAuthManager.getInstance().getAMAuthorizationService().setEnvironment(AppContext.environment);
				Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
				startActivity(intent);
			}
		});

		
		Button buttonSignup = (Button) findViewById(R.id.buttonSignup);
		buttonSignup.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, TermsOfUseActivity.class);
				startActivity(intent);
			}
		});
		/* the following code is for test purpose (login as Agency app)
		TextView textViewLearnMore = (TextView) findViewById(R.id.learnMore);
		textViewLearnMore.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AMAuthManager.getInstance().initialize(AppContext.appIdAgency, AppContext.appSecretAgency, AppContext.apiHost, AppContext.authHost, AppContext.appScopesAgency, false, AuthType.Accela);
				AMAuthManager.getInstance().getAMAuthorizationService().setAmIsRemember(true);
				AMAuthManager.getInstance().getAMAuthorizationService().setUrlSchema(AppContext.schema);
				AMAuthManager.getInstance().getAMAuthorizationService().setEnvironment(AppContext.environment);
				Intent intent = new Intent(HomeActivity.this, WelcomeActivity.class);
				startActivity(intent);
			}
		}); 
		*/
	}
	
	
	
}
