package com.accela.contractor.test;

import java.io.IOException;

import junit.framework.Assert;

import com.accela.contractor.AppContext;
import com.accela.framework.authorization.AMAuthManager;
import com.accela.framework.authorization.AMAuthorizationHelper;
import com.accela.framework.authorization.AMAuthManager.AuthType;
import com.accela.framework.service.CorelibManager;
import com.accela.mobile.AccelaMobileInternal;

public class Utils {
	static boolean isLogin = false;
	public static void login(){
//		AMAuthManager.getInstance().getAMAuthorizationService().authorize(AppContext.appScopesCitizen, "");
		if(isLogin)
			return;
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					AMAuthManager.getInstance().initialize(AppContext.appId, AppContext.appSecret, AppContext.apiHost, AppContext.authHost, AppContext.appScopesCitizen, false, AuthType.Accela);
					AMAuthManager.getInstance().getAMAuthorizationService().setAmIsRemember(true);
					AMAuthManager.getInstance().getAMAuthorizationService().setUrlSchema(AppContext.schema);
					AMAuthManager.getInstance().getAMAuthorizationService().setEnvironment(AppContext.environment);
					AMAuthorizationHelper authHelper = new AMAuthorizationHelper((AccelaMobileInternal) CorelibManager.getInstance().getAccelaMobile());
					Boolean result = authHelper.authorizedLogin(
							null, "ca@accela.com", "test1234", AppContext.environment.toString());
					Assert.assertTrue("Login in failed!", result);
					isLogin = result;
				} catch (IOException ioe) {
//					Assert.fail(ioe.toString());
				} catch (Exception e) {
//					Assert.fail(e.toString());
				}
			}
		});
		thread.start();
		try {
			thread.join(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Assert.fail(e.toString());
		}
	}

}
