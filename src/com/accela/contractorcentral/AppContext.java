package com.accela.contractorcentral;

import android.content.Context;
import android.util.Log;

import com.accela.framework.AMApplication;
import com.accela.mobile.AccelaMobile.Environment;
import com.flurry.android.FlurryAgent;



public class AppContext extends AMApplication {
	public static Context context;

	//civic app.
		public final static String appId = "635575464179170502";
		public final static String appSecret = "4dd235bf3c464516a071a980a7029e86";
		public final static String apiHost = "https://apis.accela.com";//"https://apis.accela.com";
		public final static String authHost = "https://auth.accela.com";//"https://auth.accela.com";
	//dev

	//agency app (PROD)
//	public final static String appId = "635573752221099660";//"Agency"
//	public final static String appSecret = "b7cda3ce52f54b0bac11477c3d164547";

//	public final static String appId = "635576364485825111";//"Citizen";
//	public final static String appSecret = "85d00e35e22d4110acdaa6306b42f576";
////	
//	public final static String apiHost = "https://apis.dev.accela.com";
//	public final static String authHost = "https://auth.dev.accela.com";

	
//	public final static String apiHost = "https://apps-apis.dev.accela.com";
//	public final static String authHost = "https://apps-auth.dev.accela.com";
	
	//New staging environment (With latest production backup, hosted in WestUS)
	//https://accelaeng.atlassian.net/wiki/display/CF/Staging
//	public final static String apiHost = "https://construct-dev-apis.cloudapp.net";
//	public final static String authHost = "https://construct-dev-auth.cloudapp.net";

	//citizen app (apis.dev)
//	public final static String appIdCitizen = "635592073878866832";
//	public final static String appSecretCitizen = "a85675ce7d6247f298898392e6537666";
	
	//agency app(apis.dev)
	//public final static String appId = "635636127988391872";//"Agency"
	//public final static String appSecret = "622993c4c8364fc58327248861ff3ff2";

	//agency app(app-apis.dev) https://apps-developer.dev.accela.com
//	public final static String appIdAgency = "635636132723583445";//"Agency"
//    public final static String appSecretAgency = "9135bedbaf134b5cb7dd61defa5a1876";
 
	public static String CIVIC_ID_SIGN_UP_URL = "/ClientRegister/";
	public static String SING_UP_COMPLETE_URL = authHost + "/ClientRegister/RegisterFinish?returnUrl=contractor_app_sign_up_complete";
	
	//civic app scope
	public final static String[] appScopesCitizen = {"search_records", "get_records", "get_my_records",
		"get_record", "get_record_inspections", "schedule_inspection",
		"get_inspections", "search_inspections", "get_inspection", "get_record_documents", 
		"get_document", "create_record", "get_record_fees", "batch_request", "get_document_thumbnail", "download_document", "get_record_inspection_types", "cancel_inspection",
		"get_inspections_checkavailability", "get_inspection_documents" , "get_linked_accounts", "create_civicid_accounts", "reschedule_inspection",
		"delete_civicid_accounts", "get_app_settings", "get_inspector"
	};

	//agency login scope
//	public final static String[] appScopesAgency = {"search_records", "get_records", "get_my_records",
//		"get_record", "get_record_inspections", "schedule_inspection",
//		"get_inspections", "search_inspections", "get_inspection", "get_record_documents", 
//		"get_document", "create_record","create_record_document", "get_record_fees", "batch_request",
//		"create_record_documents", "get_document_thumbnail", "download_document", "get_record_inspection_types", "cancel_inspection",
//		"get_inspections_checkavailability", "get_inspection_documents" , "get_linked_accounts", "create_inspection_documents", "reschedule_inspection"
//		//"get_inspectors", 
//	};//, "get_inspections_checkavailability"//"get_inspector"//"get_inspection_available_dates"
	
	public final static String schema = "am" + appId;

	public final static Environment environment = Environment.PROD;
	public final static boolean isMultipleAgencies = true;
	 
	public static long loginTime = System.currentTimeMillis();
	
 
	@Override
	public void onCreate() {
		super.onCreate();
		context = this.getApplicationContext();

		//Configure Flurry
		FlurryAgent.setCaptureUncaughtExceptions(true);
		FlurryAgent.setReportLocation(true);
		FlurryAgent.setLogEnabled(true);
		FlurryAgent.setLogEvents(true);
		FlurryAgent.setLogLevel(Log.VERBOSE);
		
		//Init Flurry
		FlurryAgent.init(context, AppConstants.FLURRY_APIKEY);
	}
}
