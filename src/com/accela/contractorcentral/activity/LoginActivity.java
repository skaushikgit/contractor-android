package com.accela.contractorcentral.activity;

import java.io.IOException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.AppContext;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.AppConstants.AgencyListType;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.view.ElasticScrollView;
import com.accela.framework.authorization.AMAuthorizationHelper;
import com.accela.framework.authorization.Preferences;
import com.accela.framework.service.AMLoginInfo;
import com.accela.framework.service.CorelibManager;
import com.accela.framework.util.AMUtils;
import com.accela.mobile.AccelaMobileInternal;
import com.accela.mobile.util.UIUtils;
import com.flurry.android.FlurryAgent;

public class LoginActivity extends BaseActivity{
	private ProgressDialog progressDialog;
	private SharedPreferences userPreferences;
	private static final int CANCELED = 0;
	private final int SHOW_LOGIN_FAILED_MESSAGE = 0;
	private final int SHOW_NETWORK_NOT_CONNECTED_MESSAGE = 1;
	private final int SHOW_NETWORK_NOT_OPENED_MESSAGE = 2;
	private LoginTask task;
	/**
	 * send message to update UI
	 */
	private Handler handler;
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("LoginActivity");
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
		//Lock the orientation
		setContentView(R.layout.activity_login);
		setActionBarTitle(R.string.Log_in);
		final EditText nameEditText = (EditText) findViewById(R.id.loginUsernameId);
		final EditText passwordEditText = (EditText) findViewById(R.id.loginPasswordId);
		
		userPreferences =  getSharedPreferences("ACCELA_CONTRACTOR", Context.MODE_PRIVATE);
		String username = userPreferences.getString(Preferences.LOGIN_USERNAME_LABLE, null);
		if(username!=null)
			nameEditText.setText(username);
		Button loginBtn = (Button) findViewById(R.id.loginButtonLoginId);
		loginBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	String name = nameEditText.getText().toString();
		    	String password = passwordEditText.getText().toString();
		    	if(name!=null && name.length()>0 && password!=null && password.length()>0){
		    		onLogin(name, password);
		    	}else{
		    		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
		    		alertDialogBuilder.setTitle(getResources().getString(R.string.Log_in));
		    		alertDialogBuilder.setMessage(R.string.username_password_invalid);
		    	}
		    }
		});
		
		ElasticScrollView scrollView = (ElasticScrollView) findViewById(R.id.scrollView);
		scrollView.setMaxOverScrollDistance(0, 100); 
		final LinearLayout view = (LinearLayout) findViewById(R.id.loginLayoutId);
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		LayoutParams params = (LayoutParams) view.getLayoutParams();
		params.height = size.y;
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if(imm!=null)
					imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}
		});

		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					String message = (String) msg.obj;
					// When login failed, show warning message
					if (!TextUtils.isEmpty(message)) {
						UIUtils.showMessageDialog(LoginActivity.this,
								getString(R.string.warning_title), message);
					}
					break;
				case 1:
					// When network not connected, show warning message
					UIUtils.showMessageDialog(LoginActivity.this,
							getString(R.string.warning_title),
							getString(R.string.network_not_connected_message));
					break;
				case 2:
					// When network not opened, show warning message
					UIUtils.showMessageDialog(LoginActivity.this,
							getString(R.string.warning_title),
							getString(R.string.network_not_opened_message));
					break;
				default:
					break;
				}
			}

		};

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	private void onLogin(String name, String password) {
		// TODO Auto-generated method stub
		progressDialog = new ProgressDialog(this);
		task = new LoginTask();
		AMLoginInfo info = new AMLoginInfo();
		info.setUserName(name);
		info.setPassword(password);
		info.setPreviewMode(false);
		task.execute(info);
	} 
	
	private void onLoginSuccess() {
		// TODO Auto-generated method stub
		ActivityUtils.startAgencyListActivity(LoginActivity.this, AgencyListType.LOGIN);
		this.finish();
	}
	
	class LoginTask extends AsyncTask<AMLoginInfo, Integer, Boolean> {
		public LoginTask() {
			progressDialog.setMessage(getResources().getString(
					R.string.login_progress_message));
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setCancelable(true);
			progressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if (task != null && task.getStatus() != AsyncTask.Status.FINISHED)
						task.cancel(true);
				}
			});
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
			UIUtils.setupProgressDialogParams(getApplicationContext(),
					progressDialog);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result!=null && result) {
				progressDialog.dismiss();
				onLoginSuccess();
			} else {
				progressDialog.dismiss();
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			switch (values[0]) {
			case CANCELED:
				Toast.makeText(getApplicationContext(),R.string.login_cancel_message, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}

		@Override
		protected Boolean doInBackground(AMLoginInfo... params) {

			if (params != null) {
				AMLoginInfo info = params[0];
				AMAuthorizationHelper authHelper = new AMAuthorizationHelper(
						 (AccelaMobileInternal) CorelibManager.getInstance().getAccelaMobile());

				if (AMUtils.isNetworkConnected(getApplicationContext())) {
					// Network is connected
					try {
						Boolean result = authHelper.authorizedLogin(
								info.getAgency(), info.getUserName(),
								info.getPassword(), AppContext.environment.toString());
						if (isCancelled()) {
							publishProgress(CANCELED);
							return false;
						}
						if (result) {
							AppConstants.isLoggin = true;
							SharedPreferences.Editor editor = userPreferences.edit();
							editor.clear();
							editor.putString(Preferences.LOGIN_USERNAME_LABLE, info.getUserName());
//							editor.putLong(AppConstants.LAST_LOGIN_TIME, System.currentTimeMillis());
							editor.commit();

							// Flurry: Adding username to flurry
							FlurryAgent.setUserId(info.getUserName());
						}
						return result;
					} catch (IOException ioe) {
						Message msg = new Message();
						msg.what = SHOW_NETWORK_NOT_CONNECTED_MESSAGE;
						handler.sendMessage(msg);
						ioe.printStackTrace();
						return false;
					} catch (Exception e) {
						Message msg = new Message();
						msg.what = SHOW_LOGIN_FAILED_MESSAGE;
						msg.obj = e.getMessage();
						handler.sendMessage(msg);
						e.printStackTrace();
						return false;
					}
				} else {
					// The network is not connected.
					boolean isLogined = false;
					if (!isLogined) {
						// Network is not connected
						Message msg = new Message();
						msg.what = SHOW_NETWORK_NOT_OPENED_MESSAGE;
						handler.sendMessage(msg);
					}
					return isLogined;
				}
			} else{
				return false;
			}
		}
	}
}
