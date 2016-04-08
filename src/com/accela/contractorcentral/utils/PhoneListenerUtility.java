package com.accela.contractorcentral.utils;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class PhoneListenerUtility extends PhoneStateListener{
	private boolean onCall = false;

	@Override
	public void onCallStateChanged(int state, String incomingNumber) {

		switch (state) {
		case TelephonyManager.CALL_STATE_RINGING:
			// phone ringing...
//						Toast.makeText(ContactApprovedInspectionFragment.this, incomingNumber + " calls you", 
//								Toast.LENGTH_LONG).show();
			break;

		case TelephonyManager.CALL_STATE_OFFHOOK:
			// one call exists that is dialing, active, or on hold
			//			Toast.makeText(MainActivity.this, "on call...", 
			//					Toast.LENGTH_LONG).show();
			//			//because user answers the incoming call
			//			onCall = true;
			break;

		case TelephonyManager.CALL_STATE_IDLE:
			// in initialization of the class and at the end of phone call 

			// detect flag from CALL_STATE_OFFHOOK
			//			if (onCall == true) {
			//				Toast.makeText(MainActivity.this, "restart app after call", 
			//						Toast.LENGTH_LONG).show();
			//
			//				// restart our application
			//				Intent restart = getBaseContext().getPackageManager().
			//					getLaunchIntentForPackage(getBaseContext().getPackageName());
			//				restart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//				startActivity(restart);

			onCall = false;
			break;
		default:
			break;
		}

	}
}
