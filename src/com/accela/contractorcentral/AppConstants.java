package com.accela.contractorcentral;

public class AppConstants {
	//download status
	public final static int DOWNLOAD_IDLE 	= 0;
	public final static int DOWNLOAD_RUNING 	= 1;
	
	//observer change flag
	public final static int PROJECT_LIST_CHANGE = 10;
	public final static int PROJECT_LIST_LOAD_PROGRESS = 11;
	
	public final static int PROJECT_LIST_RECENT_INSPECTION_CHANGE = 15;
	public final static int PROJECT_NEARBY_PROJECT_CHANGE = 16;
	public final static int PROJECT_LIST_SCHEDULED_INSPECTION_CHANGE = 17;
	
	public final static int DOCUMENT_LIST_CHANGE = 20;
	
	
	public final static int INSPECTION_CHANGE  	= 25;
	
	public final static int FEE_CHANGE   		= 30;
	
	//project type
	public final static int ACTIVE_PROJECT 		= 0;
	public final static int COMPLETED_PROJECT 	= 1;
	
	
	//for data loader flag
	public final static int FLAG_NOT_DOWNLOADED  	= 0; 
	public final static int FLAG_FULL_DOWNLOAED  	= 1;
	public final static int FLAG_PARTIAL_DOWNLOAED 	= 2;
	public final static int FLAG_DOWNLOADED_FAILED 	= 3;
	
	
	//contact preferred channel (see AA define)
	public final static int CONTACT_PREFERRED_EMAIL 		= 1;
	public final static int CONTACT_PREFERRED_POSTAL_MAIL 	= 2; 
	public final static int CONTACT_PREFERRED_PHONE 		= 3;
	public final static int CONTACT_PREFERRED_FAX			= 4;
	public final static int CONTACT_PREFERRED_E_MAIL 		= 5;
	public final static int CONTACT_PREFERRED_HOME_PHONE 	= 6;
	public final static int CONTACT_PREFERRED_MOBILE_PHONE	= 7;
	public final static int CONTACT_PREFERRED_WORK_PHONE	= 8;
	
	//inspection category
	public final static String INSPECTION_SEARCH_CATEGORY_COMPLETED = "Insp Completed";
	public final static String INSPECTION_SEARCH_CATEGORY_PENDING = "Insp Pending";
	public final static String INSPECTION_SEARCH_CATEGORY_SCHEDULED = "Insp Scheduled";
	
	//activity 
	public final static int RESULT_OK = 100;
	public final static int RESULT_CANCELED = 110;
	

	
	//inspection status
	public final static int INSPECTION_STATUS_FAILED = 1; //failed
	public final static int INSPECTION_STATUS_SCHEDULED = 2; //scheduled
	public final static int INSPECTION_STATUS_PASSED  = 3;  //passed
	public final static int INSPECTION_STATUS_UNKNOWN = 4;  //unknown
	public final static int INSPECTION_STATUS_CANCELED = 5;// cancel or rescheduled
	
	//pick contact
	public final static int PICK_A_CONTACT = 0;
	public static final int EDIT_CONTACT_REQUEST = 1;
	public static final int ADD_NEW_CONTACT_REQUEST = 2;
	
	//login
	public final static String LAST_LOGIN_TIME = "contractor_login_time";
	public static boolean isLoggin = false;

	//Flurry App Key
	public final static String FLURRY_APIKEY = "SC6S74V54Z2GGHC5BMGS";
	
	//agency listview type
	public enum AgencyListType {
	    LOGIN, MYAGENCY, CHOOSEAGENCY, ADDAGENCY
	}
	
	public final static String ACCELA_AUTO_PREFERRED_CHANNEL_EMAIL = "Email";
	public final static String ACCELA_AUTO_PREFERRED_CHANNEL_E_MAIL = "E-mail";
	public final static String ACCELA_AUTO_PREFERRED__CHANNEL_WORK_PHONE = "Work Phone";
	
	public final static int CANCEL_INSPECTION_SOURCE_PERMITLIST = 1;
	public final static int CANCEL_INSPECTION_SOURCE_OTHER = 2;

}
