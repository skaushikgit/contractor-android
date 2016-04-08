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


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.AppContext;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.flurry.android.FlurryAgent;


@SuppressWarnings("deprecation")
public class SignUpActivity extends BaseActivity {

    private String url;
    private String returnUrl;
    private WebView webView;
    private ProgressDialog spinner;
    private int ACCEPT_TERM_REQUEST;
    private boolean term_result;
    
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("SignUpActivity");
		FlurryAgent.onPageView();
		FlurryAgent.onStartSession(this, AppConstants.FLURRY_APIKEY);
	}

    protected void onStop(){
    	super.onStop();
    	FlurryAgent.onEndSession(this);
    	dismiss();
    }

    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityUtils.setActivityPortrait(this);		
		setContentView(R.layout.activity_signup);
		setActionBarTitle(R.string.Sign_Up);
		showBackButton(true);
		spinner = new ProgressDialog(this);
		spinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		spinner.setMessage(this.getString(R.string.loading));
		term_result = getIntent().getBooleanExtra("term_accept_result", false);

		this.returnUrl = "contractor_app_sign_up_complete";
		this.url = AppContext.authHost + AppContext.CIVIC_ID_SIGN_UP_URL + "ClientRegister?returnUrl="
				+ returnUrl;// + "&gotoText=" + getString(R.string.Log_in);
		setUpWebView();
		webView.loadUrl(url);
	}
    
    
    /**
	 * Private method, used to initialize the web view control.
	 */	

	@SuppressLint("SetJavaScriptEnabled")
    private void setUpWebView() {
		webView = (WebView)findViewById(R.id.webView);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setHapticFeedbackEnabled(true);
		try{
			CookieSyncManager.createInstance(this); 
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.removeAllCookie();
			cookieManager.setAcceptCookie(false);
			WebSettings ws = webView.getSettings();
			ws.setSaveFormData(false);
			ws.setSavePassword(false); // Not needed for API level 18 or greater (deprecated)
			
			webView.clearCache(true);
			webView.clearFormData();
			webView.clearHistory();
			webView.clearMatches();
			webView.getSettings().setAllowContentAccess(true);
			webView.getSettings().setAllowFileAccess(true);
		//	webView.getSettings().setAllowFileAccessFromFileURLs(true);
		//	webView.getSettings().setAllowUniversalAccessFromFileURLs(true); 
	        webView.setWebViewClient(new WebViewClientEx());        
		}catch(Exception exc){
			finish();
		}
    }
    
    private void dismiss() {
   	 if (spinner!=null && spinner.isShowing()) {
            spinner.dismiss();
        }
       if (webView != null) {
           webView.stopLoading();
       }
   }
     
	/**
	 * Private WebViewClient class, used by the web view control.
	 */	
    private class WebViewClientEx extends WebViewClient {        
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            spinner.dismiss();
            webView.setVisibility(View.VISIBLE);
        }        
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        	super.onPageStarted(view, url, favicon);
        	spinner.show();
        }
        
        @Override
        public void onFormResubmission (WebView view, Message dontResend, Message resend){
        	super.onFormResubmission(view, dontResend, resend);
        }
        
        @SuppressLint("NewApi") @Override
        public WebResourceResponse shouldInterceptRequest (WebView view,  WebResourceRequest request){
			return super.shouldInterceptRequest(view, request);
        }
        
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        	super.onReceivedError(view, errorCode, description, failingUrl);
        	// Dismiss spinner
            spinner.dismiss();
            // Invoke session delegate
            AMError amError = new AMError(AMError.STATUS_CODE_OTHER,String.valueOf(errorCode),String.valueOf(errorCode), description, failingUrl);
            AMLogger.logError(amError.toString());
        }        
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//            super.onReceivedSslError(view, handler, error);
//            // Dismiss spinner
            spinner.dismiss();
            handler.proceed();
            // Invoke session delegate
            AMError amError = new AMError(AMError.STATUS_CODE_OTHER,null,String.valueOf(AMError.ERROR_CODE_Bad_Request), error.toString(), null);
            AMLogger.logError(amError.toString());
        } 
        
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
        	AMLogger.logInfo("sign up url:" + url);
        	if (!url.startsWith(AppContext.authHost))  {
        		//don't go anywhere else.
        		return true;
        	}
        	            	
        	
        	if (url.compareToIgnoreCase(AppContext.authHost + AppContext.CIVIC_ID_SIGN_UP_URL + returnUrl) == 0) { // Redirect to the predefined authorization schema.  
            	// Dismiss spinner
            	spinner.dismiss();
	            // Handle the redirected URL
            	startLogin();
            	//authorizeService.getAccelaMobile().getAuthorizationManager().handleOpenURL(currentIntent);
            } else { //  Redirect to the normal login URL. 
            	webView.loadUrl(url);
            }
            return true;
        }
    }
    
    
    private void startLogin() {
    	Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
		finish();
    }
    
}
