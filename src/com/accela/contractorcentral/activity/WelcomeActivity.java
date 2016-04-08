package com.accela.contractorcentral.activity;

import android.app.Activity;
import android.os.Bundle;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.AppContext;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.framework.authorization.AMAuthManager;
import com.accela.framework.authorization.AMAuthManager.AuthType;
import com.accela.mobile.AccelaMobile.Environment;
import com.flurry.android.FlurryAgent;

public class WelcomeActivity extends Activity {

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("WelcomeActivity");
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
		//Lock the orientation
		ActivityUtils.lockActivityOrientation(this);
		AMAuthManager.getInstance().initialize(AppContext.appId, AppContext.appSecret, AppContext.apiHost, AppContext.authHost, AppContext.appScopesCitizen, true, AuthType.Accela);
		AMAuthManager.getInstance().getAMAuthorizationService().setAmIsRemember(true);
		AMAuthManager.getInstance().getAMAuthorizationService().setLoginActionName("com.accela.authorization.contractor.login");
		AMAuthManager.getInstance().getAMAuthorizationService().setUrlSchema(AppContext.schema);

		AMAuthManager.getInstance().getAMAuthorizationService().setEnvironment(Environment.PROD);
//		AMAuthManager.getInstance().setIsWaitForProfile(false);
//		AMAuthManager.getInstance().getAMAuthorizationService().authorize(AppContext.appScopes, "");

//		AMAuthManager.getInstance().getAMAuthorizationService().authorize(AppContext.appScopesAgency, "ISLANDTON");
		AMAuthManager.getInstance().getAMAuthorizationService().authorize(AppContext.appScopesCitizen, "");
//		AMAuthManager.getInstance().getAMAuthorizationService().authorize(AppContext.appScopes, "Petaluma-SQA1"); //Petaluma-SQA1
		//AMAuthManager.getInstance().getAMAuthorizationService().authorize(AppContext.appScopes, "OAKLAND-APPS");
		this.finish();
	}
}
