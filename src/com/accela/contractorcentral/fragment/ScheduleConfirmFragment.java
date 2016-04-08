package com.accela.contractorcentral.fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract.Events;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.activity.BaseActivity;
import com.accela.contractorcentral.activity.EditContactActivity;
import com.accela.contractorcentral.activity.ProjectListActivity;
import com.accela.contractorcentral.activity.SelectProjectActivity;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.persistence.ContactsPersistence;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.InstantService;
import com.accela.contractorcentral.service.Thumbnail;
import com.accela.contractorcentral.service.ThumbnailEngine;
import com.accela.contractorcentral.service.AppInstance.AppServiceDelegate;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.utils.Utils;
import com.accela.contractorcentral.view.ElasticScrollView;
import com.accela.framework.UpdateItemResult;
import com.accela.framework.authorization.AMAuthManager;
import com.accela.framework.model.CivicIdProfileModel;
import com.accela.framework.model.InspectionModel;
import com.accela.framework.model.InspectionTypeModel;
import com.accela.framework.model.PeopleModel;
import com.accela.inspection.model.InspectionTimesModel;
import com.accela.mobile.AMLogger;
import com.accela.mobile.util.DateUtil;
import com.accela.mobile.util.UIUtils;
import com.accela.record.model.ContactModel;
import com.accela.record.model.DailyInspectionTypeModel;
import com.accela.record.model.RecordInspectionModel;

public class ScheduleConfirmFragment extends Fragment implements
OnClickListener {
	private TextView textViewTitle;
	private EditText editText;
	private TextView commentTextView;
	private Button addNoteBtn;
	private Button addToCalendarBtn;
	private TextView name;
	private TextView contact;
	private ImageView imagePen;
	private Button scheduleInspectionBtn;
	private String projectId;
	private DailyInspectionTypeModel inspectionTypeModel;
	private ProjectModel projectModel;
	private InspectionTimesModel timeModel;
	//private ProgressDialog mProgressDialog;
	private int status;
	private int downloadStatus = AppConstants.DOWNLOAD_IDLE;
	private View contentView;
	private TextView initials;
	private String comment, fName, lName, phoneNum;
	private RecordInspectionModel inspection;
	private String recordId;
	private TextView progressText;
	private boolean isReschedule;
	
	//Saved to Calender 
	private Date startDate, endDate;
	private int source = AppConstants.CANCEL_INSPECTION_SOURCE_OTHER;
	private ImageView imageProfile;
	private String imagePath = null;
	private String preferredChannel;
	private String organizationName;

	//schedule inspection
	public final static int INSPECTION_SCHEDULE_CONFIRM = 0;
	public final static int INSPECTION_SHOW_SCHEDULE_RESULT = 1;
	public final static int INSPECTION_TO_RESCHEDULE = 2;
	private static final int SAVE_EVENT_REQUEST = 3;
    private ContactsPersistence contactsPersistence;


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		AMLogger.logInfo("ScheduleConfirmFragment.onActivityCreated()");
		contactsPersistence = new ContactsPersistence(getActivity());
		if(this.inspection!=null){
			String id = formatId(inspection);
			List<ContactModel> list = this.contactsPersistence.queryContacts(id);
			if(list!=null && list.size()>0 && this.imagePath==null){
				imagePath = list.get(0).getProfileImagePath();
				this.setImage();
			}
		}
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AMLogger.logInfo("ScheduleConfirmFragment.onCreate()");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		AMLogger.logInfo("ScheduleConfirmFragment.onCreateView()");

		// Create content view.
		contentView = inflater.inflate(R.layout.fragment_confirm_details,
				container, false);
		projectModel = AppInstance.getProjectsLoader().getProjectById(projectId);
		if (projectModel == null) {
			this.getActivity().finish();
			AMLogger.logInfo("Error!!, Please select a inspection type");
			return contentView;
		}
		initView();
		setupElasticScrollView();
		return contentView;
	}
	
	@Override
	public void onDetach(){
		super.onDetach();
    	contactsPersistence.close();
    	contactsPersistence = null;
	}

	private void setupElasticScrollView() {
		ElasticScrollView scrollView = (ElasticScrollView) contentView
				.findViewById(R.id.confirmInspectionScrollView);
		scrollView.setMaxOverScrollDistance(0, 100);
	}

	public void initForInspection(String projectId, String recordId,
			RecordInspectionModel inspection) {
		// TODO Auto-generated method stub
		this.initForInspection(projectId, recordId, inspection, false, AppConstants.CANCEL_INSPECTION_SOURCE_OTHER);
	}

	public void initForInspection(String projectId, String recordId,
			RecordInspectionModel inspection, boolean isReschedule, int source) {
		// TODO Auto-generated method stub
		this.projectId = projectId;
		this.recordId = recordId;
		this.inspection = inspection;
		this.isReschedule = isReschedule;
		this.source = source;
		status = INSPECTION_TO_RESCHEDULE;
	}


	public void initForResult(String projectId, String recordId,
			DailyInspectionTypeModel inspectionTypeModel,
			InspectionTimesModel timeModel, 
			String comment) {
		// TODO Auto-generated method stub
		this.projectId = projectId;
		this.recordId = recordId;
		this.inspectionTypeModel = inspectionTypeModel;
		this.timeModel = timeModel;
		this.comment = comment;
		status = INSPECTION_SHOW_SCHEDULE_RESULT;
	}

	public void initForSchedule(String projectId, String recordId,
			DailyInspectionTypeModel inspectionTypeModel,
			InspectionTimesModel timeModel, 
			String comment) {
		// TODO Auto-generated method stub
		this.projectId = projectId;
		this.recordId = recordId;
		this.inspectionTypeModel = inspectionTypeModel;
		this.timeModel = timeModel;
		this.comment = comment;
		status = INSPECTION_SCHEDULE_CONFIRM;
	}

	public void setContact(String phoneNum, String fName, String lName, String profilePath){
		this.fName = fName;
		this.lName = lName;
		this.phoneNum = phoneNum;
		this.imagePath = profilePath;
	}

	@SuppressLint("ResourceAsColor") 
	private void initView() {
		textViewTitle = (TextView) contentView.findViewById(R.id.textConfirmTitleId);
		addNoteBtn = (Button) contentView.findViewById(R.id.addNoteId);
		addToCalendarBtn = (Button) contentView.findViewById(R.id.addToCalendarId);
		editText = (EditText) contentView.findViewById(R.id.editNoteId);
		imagePen = (ImageView) contentView.findViewById(R.id.imagePenId);
		commentTextView = (TextView) contentView.findViewById(R.id.editTextId);
		if (this.comment != null)
			commentTextView.setText(comment);
		editText.setBackgroundResource(R.drawable.frame);
		LinearLayout confirm_card_Id = (LinearLayout) contentView.findViewById(R.id.confirm_card_Id);
		confirm_card_Id.setOnClickListener(this);
		commentTextView.setOnClickListener(this);
		addNoteBtn.setOnClickListener(this);
		addToCalendarBtn.setOnClickListener(this);
		editText.setOnClickListener(this);
		imageProfile = (ImageView) contentView.findViewById(R.id.imageProfile);
		TextView time1 = (TextView) contentView.findViewById(R.id.textTimeLine1);
		TextView time2 = (TextView) contentView.findViewById(R.id.textTimeLine2);
		TextView street = (TextView) contentView.findViewById(R.id.textAddressLine1);
		TextView streetPrefix = (TextView) contentView.findViewById(R.id.textAddressLine2);
		TextView cityAndState = (TextView) contentView.findViewById(R.id.textAddressLine3);
		TextView group = (TextView) contentView.findViewById(R.id.textInspectionGroup);
		TextView type = (TextView) contentView.findViewById(R.id.textInspectionType);
		initials = (TextView) contentView.findViewById(R.id.textConfirmNameInitials);
		this.setImage();
		name = (TextView) contentView.findViewById(R.id.textConfirmName);
		contact = (TextView) contentView.findViewById(R.id.textConfirmContact);
		imagePen = (ImageView) contentView.findViewById(R.id.imagePenId);
		scheduleInspectionBtn = (Button) contentView.findViewById(R.id.scheduleInspectionId);
		if(this.status == INSPECTION_SHOW_SCHEDULE_RESULT || this.status == INSPECTION_SCHEDULE_CONFIRM){
			if(timeModel.getCurrentDate()!=null)
				time1.setText(new SimpleDateFormat("EEEE, MMM d. ", Locale.ENGLISH).format(timeModel.getCurrentDate()));
			if(timeModel.getStartDate()!=null && timeModel.getEndDate()!=null){
					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
					StringBuilder timeBuilder = new StringBuilder();
						try {
							timeBuilder.append(DateUtil.to12HourTimeString(sdf.parse(timeModel.getStartDate()))).append(" ").append(DateUtil.toAMPMString(sdf.parse(timeModel.getStartDate())));
							timeBuilder.append(" - ");
							timeBuilder.append(DateUtil.to12HourTimeString(sdf.parse(timeModel.getEndDate()))).append(" ").append(DateUtil.toAMPMString(sdf.parse(timeModel.getEndDate())));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							AMLogger.logWarn(e.toString());
						}
				time2.setText(timeBuilder.toString());
			}
			parseTimeModel(timeModel);
			street.setText(Utils.getAddressLine1(projectModel.getAddress()));
			String addressUnit = Utils.getAddressUnit(projectModel.getAddress());
			if (addressUnit.length() == 0)
				streetPrefix.setVisibility(View.GONE);
			else {
				streetPrefix.setText(addressUnit);
			}

			cityAndState.setText(Utils.getAddressLine2(projectModel.getAddress()));
			String info[] = Utils.formatInspectionTypeInfo(inspectionTypeModel, recordId);
			group.setText(info[0]);
			type.setText(info[1]);

		}
		if (this.status == INSPECTION_SHOW_SCHEDULE_RESULT) {
			if(timeModel.getStartDate()!=null && timeModel.getEndDate()!=null){
				StringBuilder timeBuilder = new StringBuilder();
				timeBuilder.append(timeModel.getStartDate()).append(" - ").append(timeModel.getEndDate());
				time2.setText(timeBuilder.toString());
			}

			scheduleInspectionBtn.setText(getResources().getString(R.string.back_to_projects));
			scheduleInspectionBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					backToProjects();
				}
			});
			Button button = (Button) contentView.findViewById(R.id.scheduleAnotherInspectionId);
			button.setVisibility(View.VISIBLE);
			button.setOnClickListener(ScheduleConfirmFragment.this);
			((BaseActivity) this.getActivity()).showBackButton(false);
			((BaseActivity) getActivity()).setActionBarTitle(getResources().getString(R.string.inspection_scheduled));
			textViewTitle.setText(getResources().getString(R.string.inspection_details));
			addNoteBtn.setVisibility(View.GONE);
			imagePen.setVisibility(View.GONE);
			addToCalendarBtn.setVisibility(View.VISIBLE);
			commentTextView.setClickable(false);

			StringBuffer nameSb = new StringBuffer();
			StringBuffer initSb = new StringBuffer();
			if (this.fName != null && this.fName.length()>0) {
				nameSb.append(this.fName);
				initSb.append(this.fName.charAt(0));
			}
			if (this.lName != null && this.lName.length()>0) {
				nameSb.append(" ").append(this.lName);
				initSb.append(this.lName.charAt(0));
			}
			this.name.setText(nameSb.toString());
			if(this.imagePath==null)
				this.initials.setText(initSb.toString());
			if(this.phoneNum!=null)
				this.contact.setText(Utils.filterNumbers(getActivity(), this.phoneNum));
		} else if (status == INSPECTION_SCHEDULE_CONFIRM) {
			scheduleInspectionBtn.setOnClickListener(this);
			imagePen.setOnClickListener(this);
			
			CivicIdProfileModel profile = AMAuthManager.getInstance()
					.getProfile();
			if (profile != null) {
				StringBuffer nameSb = new StringBuffer();
				if (profile.getFirstName() != null) {
					nameSb.append(profile.getFirstName());
					this.fName = profile.getFirstName();
				}
				if (profile.getLastName() != null) {
					nameSb.append(" ").append(profile.getLastName());
					this.lName = profile.getLastName();
				}
				this.name.setText(nameSb.toString());
				if (profile.getPhoneNumber() != null)
					this.contact.setText(Utils.filterNumbers(getActivity(), getProfilePhoneNumber()));
				if(this.imagePath==null)
					initials.setText(Utils.getNameInitials(profile));
			}
			checkOnSiteContact();
		} else if (status == INSPECTION_TO_RESCHEDULE) {
			
			textViewTitle.setText(getResources().getString(
					R.string.inspection_scheduled));
			imagePen.setVisibility(View.VISIBLE);
			updateInspectionTime();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			if (inspection.getScheduleDate() != null)
				time1.setText(new SimpleDateFormat("EEEE, MMM d. ", Locale.ENGLISH).format(inspection.getScheduleDate()));
			StringBuffer sb = new StringBuffer();
			if (inspection.getScheduleStartTime() != null){
				String startTime = null;
				try {
					startTime = DateUtil.to12HourTimeString((sdf.parse( Utils.formatTime(  inspection.getScheduleStartTime()))));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					AMLogger.logWarn(e.toString());
				}
				sb.append(startTime);
				inspection.setScheduleStartTime(startTime);
			}
			if (inspection.getScheduleStartAMPM() != null)
				sb.append(inspection.getScheduleStartAMPM());
			if (inspection.getScheduleEndTime() != null){
				String endTime = null;
				try {
					endTime = DateUtil.to12HourTimeString((sdf.parse( Utils.formatTime( inspection.getScheduleEndTime()))));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					AMLogger.logWarn(e.toString());
				}
				sb.append(" - ").append(endTime);
				inspection.setScheduleEndTime(endTime);
			}
			if (inspection.getScheduleEndAMPM() != null)
				sb.append(inspection.getScheduleEndAMPM());
			time2.setText(sb.toString());
			street.setText(Utils.getAddressLine1(inspection.getAddress()));
			String addressUnit = Utils.getAddressUnit(inspection.getAddress());
			
			if (addressUnit.length()==0)
				streetPrefix.setVisibility(View.GONE);
			else {
				streetPrefix.setText(addressUnit);
			}
			if(inspection.getRequestComment()!=null)
				commentTextView.setText(inspection.getRequestComment());
			cityAndState.setText(Utils.getAddressLine2(inspection.getAddress()));
			String info[] = Utils.formatInspectionInfo(inspection);
			group.setText(info[0]);
			type.setText(info[1]);

			if (inspection.getContact() != null) {
				PeopleModel contactModel = inspection.getContact();
				StringBuffer nameSb = new StringBuffer();
				StringBuffer initialStringBuffer = new StringBuffer();
				if (contactModel.getFirstName() != null) {
					nameSb.append(contactModel.getFirstName());
					this.fName = contactModel.getFirstName();
					if (this.fName != null && this.fName.length() > 0)
						initialStringBuffer.append(this.fName.charAt(0));
				}
				if (contactModel.getLastName() != null) {
					nameSb.append(" ").append(contactModel.getLastName());
					this.lName = contactModel.getLastName();
					if (this.lName != null && this.lName.length() > 0)
						initialStringBuffer.append(this.lName.charAt(0));
				}
				this.name.setText(nameSb.toString());
				if (contactModel.getPhone1() != null)
					this.contact.setText(Utils.filterNumbers(getActivity(), contactModel.getPhone1()));
				else if (contactModel.getPhone2() != null)
					this.contact.setText(Utils.filterNumbers(getActivity(), contactModel.getPhone2()));
				else if (contactModel.getPhone3() != null)
					this.contact.setText(Utils.filterNumbers(getActivity(), contactModel.getPhone3()));
				if(this.imagePath==null)
					initials.setText(initialStringBuffer.toString());
			}
			checkOnSiteContact();
			if(this.isReschedule){
				scheduleInspectionBtn.setOnClickListener(this);
				scheduleInspectionBtn.setText(getResources().getString(R.string.reschedule_inspection_inspection));
				imagePen.setOnClickListener(this);
				if(commentTextView.getText()!=null && commentTextView.getText().length()>0){
					addNoteBtn.setVisibility(View.GONE);
				}else{
					addNoteBtn.setOnClickListener(this);
				}
			}else{
				ViewGroup viewgroup = (ViewGroup) contentView.findViewById(R.id.cancelRescheduleLayoutId);
				viewgroup.setVisibility(View.VISIBLE);
				Button rescheduleBtn = (Button) contentView.findViewById(R.id.RescheduleInspectionId);
				rescheduleBtn.setText(R.string.reschedule_inspection);
				rescheduleBtn.setVisibility(View.VISIBLE);
				rescheduleBtn.setOnClickListener(this);
				Button cancelBtn = (Button) contentView.findViewById(R.id.cancelInspectionId);
				cancelBtn.setText(R.string.cancel_inspection);
				cancelBtn.setVisibility(View.VISIBLE);
				cancelBtn.setOnClickListener(this);
				scheduleInspectionBtn.setVisibility(View.GONE);
				imagePen.setVisibility(View.GONE);
				addNoteBtn.setVisibility(View.GONE);
				commentTextView.setClickable(false);
			}
		}
	}
	
	private boolean showContactFromProfile(){
		StringBuilder sb = new StringBuilder();
		boolean hasContact = false;
		if(AMAuthManager.getInstance().getProfile()!=null && AMAuthManager.getInstance().getProfile().getFirstName()!=null){
			sb.append(AMAuthManager.getInstance().getProfile().getFirstName());
			this.fName = AMAuthManager.getInstance().getProfile().getFirstName();
		}
		if(AMAuthManager.getInstance().getProfile()!=null && AMAuthManager.getInstance().getProfile().getLastName()!=null){
			sb.append(" ").append(AMAuthManager.getInstance().getProfile().getLastName());
			this.lName = AMAuthManager.getInstance().getProfile().getLastName();
		}
		if(sb.length()>0){
			name.setText(sb.toString());
			hasContact = true;
		}
		if(AMAuthManager.getInstance().getProfile()!=null && AMAuthManager.getInstance().getProfile().getPhoneNumber()!=null){
			contact.setText(Utils.filterNumbers(getActivity(), getProfilePhoneNumber()));
			hasContact = true;
		}
		
		//show contact initial
		CivicIdProfileModel profile = AMAuthManager.getInstance()
				.getProfile();
		if (profile != null) {
			initials.setText(Utils.getNameInitials(profile));
		}
		return hasContact;
	}
	
	private String getProfilePhoneNumber(){
		StringBuffer contactBuilder = new StringBuffer();
		if(AMAuthManager.getInstance().getProfile()!=null && AMAuthManager.getInstance().getProfile().getPhoneNumber()!=null){
			if(AMAuthManager.getInstance().getProfile().getPhoneAreaCode()!=null)
				contactBuilder.append(AMAuthManager.getInstance().getProfile().getPhoneAreaCode()).append(" ");
			contactBuilder.append(AMAuthManager.getInstance().getProfile().getPhoneNumber());
		}
		return contactBuilder.toString();
	}
	
	
	private void checkOnSiteContact(){
		if( (this.name.getText()==null || this.name.getText().length()==0) && (this.contact.getText()==null || this.contact.getText().length()==0) ){
			if(!showContactFromProfile()){
				final ProgressBar progressBar = (ProgressBar) contentView.findViewById(R.id.contactProgressId);
				this.name.setVisibility(View.GONE);
				this.contact.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
				AppServiceDelegate<CivicIdProfileModel> delegate = new AppServiceDelegate<CivicIdProfileModel>() {
					@Override
					public void onSuccess(List<CivicIdProfileModel> response) {
						// TODO Auto-generated method stub
						if (progressBar != null)
							progressBar.setVisibility(View.GONE);
						if(name!=null)
							name.setVisibility(View.VISIBLE);
						if(contact!=null)
							contact.setVisibility(View.VISIBLE);
						showContactFromProfile();
						downloadStatus = AppConstants.DOWNLOAD_IDLE;
					}

					@Override
					public void onFailure(Throwable error) {
						// TODO Auto-generated method stub
						if (progressBar != null)
							progressBar.setVisibility(View.GONE);
						Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();;
						downloadStatus = AppConstants.DOWNLOAD_IDLE;
					}
				};
				InstantService.getUserProfile(delegate);
				this.downloadStatus = AppConstants.DOWNLOAD_RUNING;
			}
		}
	}
	
	private void parseTimeModel(InspectionTimesModel timeModel){
		String startDate = null;
		String startAMPM = null;
		String endDate = null;
		String endAMPM = null;
		if(timeModel.getStartDate()!=null){
			String [] arrs = timeModel.getStartDate().split(" ");
			if(arrs.length>1){
				startDate = arrs[0];
				startAMPM = arrs[1];
			}else{
				startDate = arrs[0];
			}
			this.startDate = DateUtil.toDateTime(timeModel.getCurrentDate(), startDate, startAMPM);

		}
		if(timeModel.getStartDate()!=null){
			String [] arrs = timeModel.getEndDate().split(" ");
			if(arrs.length>1){
				endDate = arrs[0];
				endAMPM = arrs[1];
			}else{
				endDate = arrs[0];
			}
			this.endDate = DateUtil.toDateTime(timeModel.getCurrentDate(), endDate, endAMPM);
		}
	}

	private void showProgressDialog(String str) {
		((BaseActivity) getActivity()).showProgressDialog(str, false);
		
	}

	@SuppressLint("ResourceAsColor") @Override
	public void onClick(View v) {
		InputMethodManager imm = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm!=null)
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		if(downloadStatus==AppConstants.DOWNLOAD_RUNING){
			Toast.makeText(getActivity(), getString(R.string.rescource_init_wait), Toast.LENGTH_SHORT).show();
			return;
		}
		// TODO Auto-generated method stub
		if (v.getId() == R.id.scheduleInspectionId) {
			if(this.isReschedule){
				reScheduleInspection();
			}else{
				scheduleInspection();
			}
		} else if (v.getId() == R.id.scheduleAnotherInspectionId) {
			scheduleAnotherInspection();
		} else if(v.getId() == R.id.RescheduleInspectionId){
			this.reschedule();
		} else if(v.getId() == R.id.cancelInspectionId){
			cancelInspection();
		}else if(v.getId() == R.id.scheduleInspectionId){
			reschedule();
		}else if (v.getId() != R.id.editNoteId) {
			commentTextView.setVisibility(View.VISIBLE);
			String editStr = editText.getText().toString();
			if(editText.getVisibility()==View.VISIBLE){
				commentTextView.setText(editStr);
			}
			if (editStr.length() == 0 && (this.status == INSPECTION_SCHEDULE_CONFIRM || (this.status==INSPECTION_TO_RESCHEDULE && this.isReschedule) )) {
				if(commentTextView.getText()==null || commentTextView.getText().length()==0){
					addNoteBtn.setVisibility(View.VISIBLE);
					editText.setVisibility(View.GONE);
					commentTextView.setText("");
				}
			} else {
				if (comment != null && comment.length() > 0)
					commentTextView.setText(comment);
				else{
					if(editText.getText()!=null && editText.getText().length()>0)
						commentTextView.setText(editText.getText());
				}
				editText.setVisibility(View.GONE);
			}
		}

		if (v.getId() == R.id.editTextId) {
			editText.setVisibility(View.VISIBLE);
			commentTextView.setVisibility(View.GONE);
			editText.setText(commentTextView.getText());
			editText.setFocusableInTouchMode(true);
			editText.setFocusable(true);
			if(imm != null) {
				imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
			}
			editText.setSelection(editText.getText().length());
		} else if (v.getId() == R.id.addNoteId) {
			editText.setVisibility(View.VISIBLE);
			addNoteBtn.setVisibility(View.GONE);
			editText.setFocusableInTouchMode(true);
			editText.setFocusable(true);
			if(imm != null) {
	            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
	        }
		}  else if (v.getId() == R.id.imagePenId) {
			Intent intent = new Intent(getActivity(), EditContactActivity.class);
			intent.putExtra("projectId", projectId);
			intent.putExtra("editContactFirstName", this.fName);
			intent.putExtra("editContactLastName", this.lName);
			intent.putExtra("editContactNumber", contact.getText().toString());
			this.startActivityForResult(intent, AppConstants.EDIT_CONTACT_REQUEST);
		} else if(v.getId() == R.id.addToCalendarId) {
			saveEvent();
		} else if(v.getId() == R.id.editNoteId){
			if(imm != null) {
	            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
	        }
		}
	}
	
	private void saveEvent( ) {
		try {
			ContentResolver cr = this.getActivity().getContentResolver();
			ContentValues values = new ContentValues();
			long calID = 3;

			if (startDate != null) {
				values.put(Events.DTSTART, startDate.getTime());
			}
			if (endDate != null) {
				values.put(Events.DTEND, endDate.getTime());
			} else if (startDate != null) {
				// If no end Time, set duration for 1 hr by default.
				Calendar endTime = Calendar.getInstance();
				endTime.setTime(startDate);
				endTime.add(Calendar.HOUR, 1);
				long endMilis = endTime.getTimeInMillis();
				values.put(Events.DTEND, endMilis);
			} else {
				Toast.makeText(getActivity(),
						"start date and end date are not available",
						Toast.LENGTH_SHORT).show();
				return;
			}

			values.put(Events.TITLE, "Contractor App Event");

			TimeZone tz = TimeZone.getDefault();
			values.put(Events.EVENT_TIMEZONE, tz.getID());
			int[] calIds = this.getCalendarId();
			if (calIds != null && calIds.length > 0) {
				values.put(Events.CALENDAR_ID, calIds[0]);
			} else {
				values.put(Events.CALENDAR_ID, calID);
			}
			if (inspectionTypeModel != null
					&& inspectionTypeModel.getText() != null) {
				values.put(Events.TITLE, inspectionTypeModel.getText());
			}

			if (projectModel != null && projectModel.getAddress() != null) {
				StringBuilder sb = new StringBuilder();
				sb.append(Utils.getAddressLine1AndUnit(projectModel.getAddress()));
				sb.append(" " + Utils.getAddressLine2(projectModel.getAddress()));
				values.put(Events.EVENT_LOCATION, sb.toString());
			}

			Uri uri = cr.insert(Events.CONTENT_URI, values);
			if (uri != null) {
				addToCalendarBtn.setText("");
				addToCalendarBtn.setBackgroundResource(R.drawable.added_cal);
				addToCalendarBtn.setClickable(false);
				Toast.makeText(getActivity(), "Calender Updated Successfully!",
						Toast.LENGTH_SHORT).show();
				return;
			} else {
				Toast.makeText(getActivity(), "Calender Updated Failed!",
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT)
					.show();
		}
	}
	
    public int[] getCalendarId(){
       int[] ids = null;
       try {
           Cursor cursor = getActivity().getContentResolver().query(Uri.parse("content://com.android.calendar/calendars"),
                   new String[]{"_id"}, null, null, null);
           cursor.moveToFirst();
// fetching calendars id
           ids = new int[cursor.getCount()];
           for (int i = 0; i < ids.length; i++) {
               ids[i] = cursor.getInt(0);
               cursor.moveToNext();
           }
       }catch (Exception e){
           Log.d("", e.toString());
       }
       return ids;
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == SAVE_EVENT_REQUEST) {
//			checkEvents();
		}
		if (requestCode == AppConstants.EDIT_CONTACT_REQUEST) {
			if (resultCode == AppConstants.RESULT_OK) {
				this.fName = data.getStringExtra("ContactFirstName");
				this.lName = data.getStringExtra("ContactLastName");
				String phoneText = data.getStringExtra("ContactPhone");
				preferredChannel = data.getStringExtra("PreferredChannel");
				this.organizationName = data.getStringExtra("OrganizationName");
				imagePath = data.getStringExtra("ProfileImagePath");
				StringBuffer sb = new StringBuffer();
				StringBuffer sbInitialsSb = new StringBuffer();
				
				if (this.fName != null && this.fName.length() > 0) {
					sb.append(this.fName);
					sbInitialsSb.append(this.fName.charAt(0));
				}
				if (this.lName != null && this.lName.length() > 0) {
					sb.append(" ").append(this.lName);
					sbInitialsSb.append(this.lName.charAt(0));
				}
				if(sb.length()>0)
					name.setText(sb.toString());
				else if(organizationName!=null && organizationName.length()>0){
					name.setText(organizationName);
					sbInitialsSb.append(organizationName.charAt(0));
				}
				if (phoneText != null)
					contact.setText(Utils.filterNumbers(getActivity(), phoneText));
				else
					contact.setText("");
				if (imagePath==null || imagePath.length()==0 || !setImage()) {
					initials.setText(sbInitialsSb.toString());
					imageProfile.setImageDrawable(getResources().getDrawable(R.drawable.contact));
				}
			}
		}
	}
	
	private boolean setImage(){
		if(imagePath!=null && imagePath.length()>0) {
			ThumbnailEngine thumbEngine = ThumbnailEngine.getInstance();
			thumbEngine.setThumbnailExpectedSize(128);
			Thumbnail thumbnail = thumbEngine.requestThumbnail(imagePath);
			if(thumbnail!=null && thumbnail.bitmap!=null) {
				imageProfile.setImageBitmap(thumbnail.bitmap);
				initials.setText("");
				return true;
			} 
       	} 
		return false;
	}

	private void scheduleAnotherInspection() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this.getActivity(), SelectProjectActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("fromScheduleAnother", true);
		this.startActivity(intent);
		this.getActivity().finish();
	}

	private void backToProjects() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this.getActivity(),
				ProjectListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startActivity(intent);
		this.getActivity().finish();
	}

	private void cancelInspection() throws RuntimeException{
		// TODO Auto-generated method stub
	 try{
		UIUtils.showConfirmDialog(getActivity(), getString(R.string.warning_title), getString(R.string.cancel_dialog_message), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if(dialog!=null)
					dialog.dismiss();
				if(inspection == null) {
					//need to select inspection Type
					return;
				}
				showProgressDialog(getString(R.string.cancelling_inspection));

				copyModel(inspection.getType());

				AppServiceDelegate<UpdateItemResult> delegate = new AppServiceDelegate<UpdateItemResult>() {
					@Override
					public void onSuccess(List<UpdateItemResult> response) {
						// TODO Auto-generated method stub
						((BaseActivity) getActivity()).closeProgressDialog();
						if(source==AppConstants.CANCEL_INSPECTION_SOURCE_PERMITLIST){
							ActivityUtils.startProjectDetailsActivity(getActivity(), projectId, 1, inspection);
						}else{
							AppInstance.getInpsectionLoader().removeInspection(recordId, inspection);
							getActivity().finish();
						}
					}

					@Override
					public void onFailure(Throwable error) {
						// TODO Auto-generated method stub
						((BaseActivity) getActivity()).closeProgressDialog();
						UIUtils.showMessageDialog(getActivity(), getString(R.string.warning_title), error.getMessage());
						//						ActivityUtils.startProjectDetailsActivity(getActivity(), projectId, 1, inspection);
						//						getActivity().finish();
					}
				};
				InstantService.cancelInspection(delegate, inspection.getId().toString(), recordId);
			}
		});
	 } catch (RuntimeException e) {
		AMLogger.logError(e.toString());
	 }
   }

	private void reschedule() {
		// TODO Auto-generated method stub
		if(this.inspection == null) {
			//need to select inspection Type
			return;
		}
		copyModel(inspection.getType());
		ActivityUtils.startChooseInspectionTimeActivity(getActivity(), projectId, recordId, inspection.getId(), inspectionTypeModel, inspection, true);
		getActivity().finish();
	}

	private void copyModel(InspectionTypeModel type) {
		// TODO Auto-generated method stub
		if(type==null)
			return;
		if(inspectionTypeModel==null)
			inspectionTypeModel = new DailyInspectionTypeModel();
		inspectionTypeModel.setGroup(type.getGroup());
		inspectionTypeModel.setText(type.getText());
		inspectionTypeModel.setValue(type.getValue());
		inspectionTypeModel.setId(type.getId());
	}


	private void reScheduleInspection() throws RuntimeException{
		// TODO Auto-generated method stub
	 try{		
		UIUtils.showConfirmDialog(getActivity(), getString(R.string.warning_title), getString(R.string.reschedule_dialog_message), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(dialog!=null)
					dialog.dismiss();
				// TODO Auto-generated method stub
				if(inspection == null) {
					//need to select inspection Type
					return;
				}
				showProgressDialog(getString(R.string.rescheduling_inspectioning));

				AppServiceDelegate<InspectionModel> delegate = new AppServiceDelegate<InspectionModel>() {
					@Override
					public void onSuccess(List<InspectionModel> response) {
						// TODO Auto-generated method stub
						((BaseActivity) getActivity()).closeProgressDialog();
						if(response!=null && response.size()>0){
							RecordInspectionModel recordInspection = Utils.modelConvert(response.get(0));
							AppInstance.getInpsectionLoader().removeInspection(recordId, inspection);
							AppInstance.getInpsectionLoader().addScheduledInspection(recordId, recordInspection);
							ActivityUtils.inspectionSuccessScreen(getActivity(), projectId, recordId, response.get(0), imagePath);
							if(imagePath!=null && recordInspection!=null)
								saveImageProfile(recordInspection);
							return;
						}
						UIUtils.showMessageDialog(getActivity(), getString(R.string.warning_title), getString(R.string.inspection_schedule_empty_failed));
					}

					@Override
					public void onFailure(Throwable error) {
						// TODO Auto-generated method stub
						((BaseActivity) getActivity()).closeProgressDialog();
						UIUtils.showMessageDialog(getActivity(), getString(R.string.warning_title), error.getMessage());
					}
				};

				String requestComment = commentTextView.getText().toString();
				if (requestComment == null || requestComment.length() == 0)
					requestComment = editText.getText().toString();
				inspection.setRequestComment(requestComment);
				String names = name.getText().toString();
				if (names != null) {
					String[] strs = names.split(" ");
					if (strs.length == 3) {
						setInspectionContact(inspection, strs[0], strs[1], strs[2], contact.getText().toString());
					} else if (strs.length == 2) {
						setInspectionContact(inspection, strs[0], null, strs[1], contact.getText().toString());
					} else if (strs.length == 1) {
						setInspectionContact(inspection, strs[0], null, null, contact.getText().toString());
					}
				}
				CivicIdProfileModel profileModel = AMAuthManager.getInstance().getProfile();
				if(profileModel!=null && (profileModel.getFirstName()!=null || profileModel.getLastName()!=null || profileModel.getPhoneNumber()!=null)){
						if(profileModel.getFirstName()!=null)
							inspection.setRequestorFirstName(profileModel.getFirstName());
						if(profileModel.getLastName()!=null)
							inspection.setRequestorLastName(profileModel.getLastName());
						StringBuilder requestNumBuilder = new StringBuilder();
						if(profileModel.getPhoneAreaCode()!=null)
							requestNumBuilder.append(profileModel.getPhoneAreaCode());
						if(profileModel.getPhoneNumber()!=null)
							requestNumBuilder.append(profileModel.getPhoneNumber());
						inspection.setRequestorPhone(requestNumBuilder.toString());

				}else{
					PeopleModel contact = inspection.getContact();
					if(contact!=null){
						if(contact.getFirstName()!=null)
							inspection.setRequestorFirstName(contact.getFirstName());
						if(contact.getMiddleName()!=null)
							inspection.setRequestorMiddleName(contact.getMiddleName());
						if(contact.getLastName()!=null)
							inspection.setRequestorLastName(contact.getLastName());
						StringBuilder requestNumBuilder = new StringBuilder();
						if(contact.getPhone1CountryCode()!=null)
							requestNumBuilder.append(contact.getPhone1CountryCode());
						if(contact.getPhone1()!=null)
							requestNumBuilder.append(contact.getPhone1());
						inspection.setRequestorPhone(requestNumBuilder.toString());
					}
				}
				InstantService.rescheduleInspection(delegate, inspection, recordId);
			}
		});
	 } catch (RuntimeException e) {
			AMLogger.logError(e.toString());
	 }
	}
	
	private void setInspectionContact(RecordInspectionModel inspection, String firstName, String middleName, String lastName, String number){
		PeopleModel people = new PeopleModel();
		if(firstName!=null )
			people.setFirstName(firstName);
		if(middleName!=null)
			people.setMiddleName(middleName);
		if(lastName!=null)
			people.setLastName(lastName);
		if(number!=null)
			people.setPhone1(number);
		inspection.setContact(people);
	}
	
	private String formatId(RecordInspectionModel inspection){
		StringBuilder sb = new StringBuilder();
		if(inspection.getRecordId_id()!=null){
			sb.append(inspection.getRecordId_id());
		}
		if(inspection.getId()!=null)
			sb.append(inspection.getId().toString());
		return sb.toString();
	}
	private void saveImageProfile(RecordInspectionModel inspection){
		String Id = formatId(inspection);
		ContactModel contactModel = new ContactModel();
		contactModel.setRecordId_id(Id);
		contactModel.setProfileImagePath(this.imagePath);
		this.contactsPersistence.saveContact(contactModel);
	}

	private void scheduleInspection() {
		// TODO Auto-generated method stub
		try {
			String names = name.getText().toString();
			if (names == null || names.length() == 0) {
				Toast.makeText(getActivity(),
						getString(R.string.input_onsite_concat),
						Toast.LENGTH_SHORT).show();
				return;
			}
			String[] nameArray = new String[3];
			if (names != null) {
				String[] strs = names.split(" ");
				if (strs.length == 3) {
					nameArray[0] = strs[0];
					nameArray[1] = strs[1];
					nameArray[2] = strs[2];
				} else if (strs.length == 2) {
					nameArray[0] = strs[0];
					nameArray[2] = strs[1];
				} else if (strs.length == 1) {
					nameArray[0] = strs[0];
				}
			}
			showProgressDialog(this.getResources().getString(
					R.string.scheduling_inspection));
			AppServiceDelegate<InspectionModel> delegate = new AppServiceDelegate<InspectionModel>() {
				@Override
				public void onSuccess(List<InspectionModel> response) {
					// TODO Auto-generated method stub
					if (isAdded() && getActivity() != null) {
						((BaseActivity) getActivity()).closeProgressDialog();
						if (response != null && response.size() > 0) {
							RecordInspectionModel recordInspection = Utils.modelConvert(response.get(0));
							AppInstance.getInpsectionLoader().addScheduledInspection(recordId, recordInspection);
							ActivityUtils.inspectionSuccessScreen(getActivity(), projectId, recordId, response.get(0), imagePath);
							if (imagePath != null && recordInspection != null)
								saveImageProfile(recordInspection);
							return;
						}
						UIUtils.showMessageDialog(
								getActivity(),
								getString(R.string.warning_title),
								getString(R.string.inspection_schedule_empty_failed));
					}
				}

				@Override
				public void onFailure(Throwable error) {
					// TODO Auto-generated method stub
					if (isAdded() && getActivity() != null) {
						((BaseActivity) getActivity()).closeProgressDialog();
						UIUtils.showMessageDialog(getActivity(), getString(R.string.warning_title), android.text.Html.fromHtml(error.getMessage()).toString());
					}
				}
			};

			String requestComment = commentTextView.getText().toString();
			if (requestComment == null || requestComment.length() == 0)
				requestComment = editText.getText().toString();

			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			CivicIdProfileModel profileModel = AMAuthManager.getInstance()
					.getProfile();

			if (profileModel == null)
				return;
			StringBuilder requestNumBuilder = new StringBuilder();
			if(profileModel.getPhoneAreaCode()!=null)
				requestNumBuilder.append(profileModel.getPhoneAreaCode());
			if(profileModel.getPhoneNumber()!=null)
				requestNumBuilder.append(profileModel.getPhoneNumber());

			InstantService.scheduleNewInspection(delegate, recordId,
					projectModel.getAddress(), inspectionTypeModel.getId(),
					timeModel.getCurrentDate(), DateUtil.to12HourTimeString(sdf
							.parse(timeModel.getStartDate())), DateUtil.to12HourTimeString(sdf.parse(timeModel.getEndDate())), DateUtil.toAMPMString(sdf.parse(timeModel.getStartDate())), 
							DateUtil.toAMPMString(sdf.parse(timeModel.getEndDate())),requestComment, profileModel.getFirstName(), null, profileModel.getLastName(),  requestNumBuilder.toString(),
					nameArray[0], nameArray[1], nameArray[2], contact.getText().toString(), this.preferredChannel, organizationName);
		} catch (ParseException e) {
			AMLogger.logError(e.toString());
		} catch (RuntimeException e) {
			AMLogger.logError(e.toString());
		} catch (Exception e){
			AMLogger.logError(e.toString());
		}
	}
	
	private void updateInspectionTime() {
		if (this.timeModel != null && this.inspection != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			try {
				if (timeModel.getStartDate() != null)

					this.inspection.setScheduleStartTime(DateUtil
							.to12HourTimeString(sdf.parse(timeModel
									.getStartDate())));

				if (timeModel.getEndDate() != null)
					this.inspection.setScheduleEndTime(DateUtil
							.to12HourTimeString(sdf.parse(timeModel
									.getEndDate())));
				if (timeModel.getStartDate() != null)
					this.inspection.setScheduleStartAMPM(DateUtil
							.to12HourTimeString(sdf.parse(timeModel
									.getStartDate())));
				if (timeModel.getEndDate() != null)
					this.inspection.setScheduleEndAMPM(DateUtil
							.to12HourTimeString(sdf.parse(timeModel
									.getEndDate())));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
