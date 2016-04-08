package com.accela.contractorcentral.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.AppContext;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.fee.model.FeeModel;
import com.accela.framework.model.AddressModel;
import com.accela.framework.model.CivicIdProfileModel;
import com.accela.framework.model.InspectionModel;
import com.accela.framework.model.InspectionTypeModel;
import com.accela.framework.model.PeopleModel;
import com.accela.inspection.model.InspectorModel;
import com.accela.mobile.AMLogger;
import com.accela.mobile.AccelaMobile.Environment;
import com.accela.mobile.util.APOHelper;
import com.accela.mobile.util.DateUtil;
import com.accela.mobile.util.UIUtils;
import com.accela.record.model.ContactModel;
import com.accela.record.model.DailyInspectionTypeModel;
import com.accela.record.model.RecordInspectionModel;
import com.accela.record.model.RecordModel;

public class Utils {
	public static List<RecordInspectionModel> loadLastMonthInspectionList(List<RecordInspectionModel> list) {
		List<RecordInspectionModel> recentOneMonthInspectionList = new ArrayList<RecordInspectionModel> ();

		//		recentOneMonthInspectionList.clear();
		Calendar cal = Calendar.getInstance();

		//Set calender 31 days back
		cal.add(cal.DATE, -31);
		Date lastMonthDate = cal.getTime();
		if(recentOneMonthInspectionList!=null)
			recentOneMonthInspectionList.clear();
		for(RecordInspectionModel inspection: list) {
			Date inspectionDate = inspection.getCompletedDate();
			if(inspectionDate != null && inspectionDate.after(lastMonthDate) && inspection.getAddress()!=null) {
				recentOneMonthInspectionList.add(inspection);
			}
		}
		sortInspectionListByCompleteDate(recentOneMonthInspectionList, true);
		return recentOneMonthInspectionList;
	}
	
	private static void sortInspectionListByCompleteDate(List<RecordInspectionModel> list, final boolean desc) {
		try{
			Collections.sort(list, new Comparator<RecordInspectionModel>() {

				@Override
				public int compare(RecordInspectionModel inspection1, RecordInspectionModel inspection2) {
					int result =  desc ? -Utils.compareInspectionCompleteDate(inspection1, inspection2) : Utils.compareInspectionCompleteDate(inspection1, inspection2);
					if(result==0 && inspection1.getType()!=null && inspection1.getType().getText()!=null && inspection2.getType()!=null && inspection2.getType().getText()!=null)
						return inspection1.getType().getText().compareTo(inspection2.getType().getText());
					return result;
				}
			
			});
		}catch(RuntimeException e){
			AMLogger.logError(e.toString());
		}
	}

	public static String filterNumbers(Context context, String str){
		if(str==null || str.length()==0){
			Toast.makeText(context, context.getResources().getString(R.string.contact_phone_required_title), Toast.LENGTH_SHORT).show();
			return str;
		}
		for(int i=0; i<str.length(); i++){
			if(str.charAt(i)==' ' || str.charAt(i)=='-' || str.charAt(i)=='(' || str.charAt(i)==')')
				continue;
			if(str.charAt(i)<'0' || str.charAt(i)>'9'){
				Toast.makeText(context, context.getResources().getString(R.string.contact_phone_required_title), Toast.LENGTH_SHORT).show();
				return "";
			}
		}
		return str;
	}
	/**
	 * Get address of reocord/permit. we only use primary address here
	 * @param record
	 * @return the primary address of record
	 */

	public static AddressModel getAddress(RecordModel record) {
		AddressModel primaryAddress = APOHelper.getPrimaryAddress(record.getAddresses());
		return primaryAddress;
	}


	public static boolean isSameAddress(AddressModel address1, AddressModel address2) {
		if(address1 == null || address2 == null) {
			return false;
		}
		String fullAddress1 = APOHelper.formatAddress(address1);
		String fullAddress2 = APOHelper.formatAddress(address2);
		//AMLogger.logInfo("address1:" + fullAddress1);
		//AMLogger.logInfo("address2:" + fullAddress2);
		return  fullAddress1.compareToIgnoreCase(fullAddress2)==0;
	}
	
	public static List<ContactModel> generateContactList(Context context,List<InspectorModel> list){
		List<ContactModel> contacts = new ArrayList<ContactModel>();
		for(InspectorModel inspector : list){
			ContactModel contact = new ContactModel();
			if(inspector.getFirstName()!=null)
				contact.setFirstName(inspector.getFirstName());
			if(inspector.getMiddleName()!=null)
				contact.setMiddleName(inspector.getMiddleName());
			if(inspector.getLastName()!=null)
				contact.setLastName(inspector.getLastName());
			if(inspector.getEmail()!=null)
				contact.setEmail(inspector.getEmail());
			if(inspector.getMobilePhone()!=null)
				contact.setPhone1(inspector.getMobilePhone());
			if(inspector.getPreferredChannel()!=null){
				switch(inspector.getPreferredChannel()){
					case AppConstants.ACCELA_AUTO_PREFERRED_CHANNEL_E_MAIL:
					case AppConstants.ACCELA_AUTO_PREFERRED_CHANNEL_EMAIL:
						contact.setPreferredChannel_value(String.valueOf(AppConstants.CONTACT_PREFERRED_EMAIL));
						break;
					default:
						contact.setPreferredChannel_value(String.valueOf(AppConstants.CONTACT_PREFERRED_PHONE));
						break;
				}
				contact.setPreferredChannel_text(inspector.getPreferredChannel());
			}
			contact.setType_text(context.getString(R.string.reporting_inspector));
			contacts.add(contact);
		}
		return contacts;
	}
	
	public static ContactModel generateContactByInspection( RecordInspectionModel inspection){
			if(inspection==null) {
				return null;
			}
			PeopleModel people = inspection.getContact();
			if(people != null) {
				ContactModel contact = new ContactModel();
				contact.setFirstName(people.getFirstName());
				contact.setMiddleName(people.getMiddleName());
				contact.setLastName(people.getLastName());
				contact.setEmail(people.getEmail());
				contact.setPhone1(people.getPhone1());
				contact.setPhone1CountryCode(people.getPhone1CountryCode());
				contact.setPhone2(people.getPhone2());
				contact.setPhone2CountryCode(people.getPhone2CountryCode());
				contact.setPhone3(people.getPhone2());
				contact.setPhone3CountryCode(people.getPhone2CountryCode());
				contact.setEmail(people.getEmail());
				contact.setPreferredChannel_text(people.getPreferredChannel_text());
				contact.setPreferredChannel_value(people.getPreferredChannel_value());
				return contact;
			} else {
				return null;
			}
			
		
	}

	public static String getInspectionDate(RecordInspectionModel inspectionModel) {

		if(inspectionModel != null && inspectionModel.getScheduleDate()!=null) {
			StringBuffer inspectionDate = new StringBuffer();
			Date date = new Date();
			Calendar today = Calendar.getInstance();
			today.setTime(date);
			Calendar tomorrow = Calendar.getInstance();
			tomorrow.setTime(date);
			tomorrow.add(Calendar.DATE, 1);
			Calendar scheduledDate = Calendar.getInstance();
			scheduledDate.setTime(inspectionModel.getScheduleDate());
			if(scheduledDate.get(Calendar.MONTH)==today.get(Calendar.MONTH) && scheduledDate.get(Calendar.DAY_OF_MONTH)==today.get(Calendar.DAY_OF_MONTH) 
					&& scheduledDate.get(Calendar.YEAR)==today.get(Calendar.YEAR)){
				return "Today";
			}else if(scheduledDate.get(Calendar.MONTH)==tomorrow.get(Calendar.MONTH) && scheduledDate.get(Calendar.DAY_OF_MONTH)==tomorrow.get(Calendar.DAY_OF_MONTH) 
					&& scheduledDate.get(Calendar.YEAR)==tomorrow.get(Calendar.YEAR)){
				return "Tomorrow";
			}
			inspectionDate.append(new SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(inspectionModel.getScheduleDate()));
			return inspectionDate.toString();
		}
		return null;
	}
	
	public static void callPhone(Activity activity, ContactModel contactModel){	
		String phoneNumber = null;
		if(contactModel.getPhone1() != null) 
			phoneNumber = contactModel.getPhone1();
		else if(contactModel.getPhone2() != null)
			phoneNumber = contactModel.getPhone2();
		else if(contactModel.getPhone3() != null)
			phoneNumber = contactModel.getPhone3();
		
		if(phoneNumber == null || phoneNumber.length()==0) {
			Toast.makeText(activity, "Invalid Phone Number", Toast.LENGTH_LONG).show();
		}
		else {
			Intent phoneIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phoneNumber));
			activity.startActivity(phoneIntent);
		}
	}
	
	public static void sendEmail(Activity activity, ContactModel contactModel){
		String emailId = null;
		if(contactModel.getEmail() != null)
			emailId = contactModel.getEmail(); 
		if(emailId == null) {
			Toast.makeText(activity, "Invalid Email", Toast.LENGTH_LONG).show();
		} else {
			Intent emailIntent = new Intent(Intent.ACTION_SEND);
			emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailId});
			emailIntent.setType("message/rfc822");
			activity.startActivity(Intent.createChooser(emailIntent, "Choose an Email client :"));	
		}
	}

	public static boolean isSameAgency(String recordId, String agency){
		if(agency==null){
			return true;
		}
		RecordModel record = AppInstance.getProjectsLoader().getRecordById(recordId);
		if(record!=null) {
			return record.getResource_agency().equalsIgnoreCase(agency);
		} else {
			return false;
		}
	}

	public static String getInspectionTime(RecordInspectionModel inspectionModel) {

		if(inspectionModel != null && inspectionModel.getScheduleDate()!=null) {
			StringBuffer inspectionDate = new StringBuffer();
			if(inspectionModel.getScheduleStartTime()!=null){
				inspectionDate.append(Utils.formatTime(inspectionModel.getScheduleStartTime()));
			}
			if(inspectionModel.getScheduleStartAMPM()!=null){
				inspectionDate.append(inspectionModel.getScheduleStartAMPM());
			}
			if(inspectionModel.getScheduleEndTime()!=null){
				inspectionDate.append(" - " +  Utils.formatTime(inspectionModel.getScheduleEndTime()));
			}
			if(inspectionModel.getScheduleEndAMPM()!=null){
				inspectionDate.append(inspectionModel.getScheduleEndAMPM());
			}
			return inspectionDate.toString();
		}
		return null;

	}

	public static String getInspectionCompletedTime(RecordInspectionModel inspectionModel) {
		if(inspectionModel != null && inspectionModel.getCompletedTime()!=null) {
			StringBuffer inspectionTime = new StringBuffer();
			if(inspectionModel.getCompletedTime()!=null){
				inspectionTime.append(Utils.formatTime(inspectionModel.getCompletedTime()));
			}
			if(inspectionModel.getCompletedAMPM()!=null){
				inspectionTime.append(inspectionModel.getCompletedAMPM());
			}
			return inspectionTime.toString();
		}
		return null;
	}

	public static String getInspectionCompletedDate(RecordInspectionModel inspectionModel) {
		if(inspectionModel != null && inspectionModel.getCompletedDate()!=null) {
			StringBuffer inspectionDate = new StringBuffer();
			if(inspectionModel.getCompletedDate()!=null){
				inspectionDate.append(new SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(inspectionModel.getCompletedDate()));
			}
			return inspectionDate.toString();
		}
		return null;
	}

	public static String getFeeDateTime(FeeModel feeModel) {

		if(feeModel != null && feeModel.getApplyDate()!=null) {
			StringBuffer feeDue = new StringBuffer();
			Date date = feeModel.getApplyDate();

			feeDue.append(new SimpleDateFormat("EEEE", Locale.getDefault()).format(date));
			feeDue.append(", ");
			feeDue.append(new SimpleDateFormat("MMM").format(date)+". "+date.getDay());

			return feeDue.toString();
		}
		return null;
	}

	public static String getAddressLine1(AddressModel address) {
		StringBuffer street = new StringBuffer();
		if(address==null) {
			return "";
		}
		if(address.getStreetStart()!=null)
			street.append(address.getStreetStart().trim());
		if(address.getDirection_text()!=null){
			if(street.length()>0)
				street.append(" ");
			street.append(address.getDirection_text().trim());
		} 
		if(address.getStreetName()!=null){
			if(street.length()>0)
				street.append(" ");
			street.append(address.getStreetName().trim());
		} 
		if(address.getStreetSuffix_value()!=null){
			if(street.length()>0)
				street.append(" ");
			street.append(address.getStreetSuffix_value().trim());
		}
		return street.toString();
	}

	public static String getAddressLine1AndUnit(AddressModel address) {
		String line1 = getAddressLine1(address);
		String unit = getAddressUnit(address);
		if(unit.length() > 0) {
			line1 = line1 + ", " + unit;
		}
		return line1;
	}

	public static String getAddressLine2(AddressModel address) {
		if(address == null) {
			return "";
		}
		StringBuffer addressLine = new StringBuffer();
		if(address.getCity()!=null)
			addressLine.append(address.getCity().trim());
		if(address.getState_value()!=null){
			addressLine.append(", ");
			addressLine.append(address.getState_value().trim());
		}
		if(address.getPostalCode()!=null){
			addressLine.append(" ");
			addressLine.append(address.getPostalCode().trim());
		}
		return addressLine.toString();
	}

	public static String getAddressUnit(AddressModel address) {
		if(address == null) {
			return "";
		}
		StringBuffer addressLine = new StringBuffer();
		if(address.getUnitType_text() !=null)
			addressLine.append(address.getUnitType_text());
		if(address.getUnitStart()!=null){
			if(addressLine.length() > 0)
				addressLine.append(" ");
			addressLine.append(address.getUnitStart());
		}
		if(address.getUnitEnd()!=null){
			if(addressLine.length() > 0)
				addressLine.append(" - ");
			addressLine.append(address.getUnitEnd());
		}
		return addressLine.toString();
	}
	
	public static String getAddressFullLine(AddressModel address) {
		if(address == null) {
			return "";
		}
		return getAddressLine1AndUnit(address) + " " + getAddressLine2(address);
	}

	public static String getFullName(ContactModel contact) {
		StringBuffer name = new StringBuffer();
		if(contact.getFirstName()!=null){
			name.append(contact.getFirstName().trim());
		} 

		if(contact.getMiddleName()!=null) {
			if(name.length()>0) {
				name.append(" ");
			}
			name.append(contact.getMiddleName().trim());
		}
		if(contact.getLastName()!=null) {
			if(name.length()>0) {
				name.append(" ");
			}
			name.append(contact.getLastName().trim());
		}

		if(name.length() == 0 && contact.getOrganizationName() !=null) {
			name.append(contact.getOrganizationName().trim());
		}

		return name.toString();
	}

	/**
	 * 
	 * Compare the date of two inspection
	 * 
	 * @param first inspection
	 * @param second inspection
	 * 
	 * @return 1: first is new, 0: same, -1: first is old
	 * 
	 * 
	 */

	public static int compareInspectionDate(RecordInspectionModel inspection1, RecordInspectionModel inspection2) {
		if(inspection1==null || inspection2==null)
			return 0;
		if(inspection1.getScheduleDate() == null) {
			return 1;
		} else if(inspection2.getScheduleDate() == null) {
			return -1;
		} else {
			int ret = inspection1.getScheduleDate().compareTo(inspection2.getScheduleDate());
			if(ret==0){
				Date date1 = DateUtil.toDateTime(inspection1.getScheduleDate(), inspection1.getScheduleStartTime(), inspection1.getScheduleStartAMPM());
				Date date2 = DateUtil.toDateTime(inspection2.getScheduleDate(), inspection2.getScheduleStartTime(), inspection2.getScheduleStartAMPM());
				if(date1==null)
					return 1;
				if(date2==null)
					return -1;
				ret = date1.compareTo(date2);
				if(ret==0 && inspection1.getType()!=null && inspection1.getType().getText()!=null && inspection2.getType()!=null && inspection2.getType().getText()!=null)
					return inspection1.getType().getText().compareTo(inspection2.getType().getText());
			}
			return ret;
		}
	}
	
	public static int compareInspectionCompleteDate(RecordInspectionModel inspection1, RecordInspectionModel inspection2) {
		if(inspection1==null || inspection2==null)
			return 0;
		if(inspection1.getCompletedDate() == null) {
			return 1;
		} else if(inspection2.getCompletedDate() == null) {
			return -1;
		} else {
			int ret = inspection1.getCompletedDate().compareTo(inspection2.getCompletedDate());
			if(ret==0){
				Date date1 = DateUtil.toDateTime(inspection1.getCompletedDate(), inspection1.getCompletedTime(), inspection1.getCompletedAMPM());
				Date date2 = DateUtil.toDateTime(inspection2.getCompletedDate(), inspection2.getCompletedTime(), inspection2.getCompletedAMPM());
				if(date1==null)
					return 1;
				if(date2==null)
					return -1;
				ret = date1.compareTo(date2);
				if(ret==0 && inspection1.getType()!=null && inspection1.getType().getText()!=null && inspection2.getType()!=null && inspection2.getType().getText()!=null)
					return inspection1.getType().getText().compareTo(inspection2.getType().getText());
			}
			return ret;
		}
	}


	public static String formatInsepctionDateTime(RecordInspectionModel inspectionModel) {
		if(inspectionModel == null || inspectionModel.getScheduleDate()==null) {
			return null;
		} else {
			StringBuffer nextInspectionDate = new StringBuffer();
			nextInspectionDate.append(getInspectionDate(inspectionModel));

			if(inspectionModel.getScheduleStartTime()!=null){
				nextInspectionDate.append(" " + Utils.formatTime(inspectionModel.getScheduleStartTime()));
			}
			if(inspectionModel.getScheduleStartAMPM()!=null){
				nextInspectionDate.append(inspectionModel.getScheduleStartAMPM());
			}
			if(inspectionModel.getScheduleEndTime()!=null){
				nextInspectionDate.append(" - " +  Utils.formatTime(inspectionModel.getScheduleEndTime()));
			}
			if(inspectionModel.getScheduleEndAMPM()!=null){
				nextInspectionDate.append(inspectionModel.getScheduleEndAMPM());
			} 
			return nextInspectionDate.toString();
		}
	}

	public static String getNameInitials(ContactModel contact) {
		StringBuffer nameInit = new StringBuffer();
		String name = contact.getFirstName();
		if(name!=null && name.length()>0){
			nameInit.append(name.charAt(0));
		} 
		name = contact.getLastName();
		if(name!=null && name.length()>0) {
			nameInit.append(name.charAt(0));
		}

		//Setting initials for organization name
		if(nameInit.length() == 0 && contact.getOrganizationName()!=null) {
			String orgName = contact.getOrganizationName();
			if(orgName != null) {
				nameInit.append(orgName.substring(0, 1).toUpperCase());
				nameInit.append(orgName.substring(1, 2).toLowerCase());
			}
		}

		return nameInit.toString();
	}

	public static String getNameInitials(String fullName) {
		if(fullName==null) {
			return "";
		}
		StringBuffer nameInit = new StringBuffer();
		String [] nameArray = fullName.trim().split("\\s");
		
		if(nameArray !=null && nameArray.length>0 && nameArray[0] != null && nameArray[0].length()>0) {
			nameInit.append(nameArray[0].charAt(0));
		}

		if(nameArray !=null && nameArray.length>1 && nameArray[1] != null && nameArray[1].length()>0) {
			nameInit.append(nameArray[1].charAt(0));
		}
		return nameInit.toString();
	}

	public static String getNameInitials(CivicIdProfileModel model) {
		StringBuffer nameInit = new StringBuffer();
		String name = model.getFirstName();
		if(name!=null && name.length()>0){
			nameInit.append(name.charAt(0));
		} 
		name = model.getLastName();
		if(name!=null && name.length()>0) {
			nameInit.append(name.charAt(0));
		}
		return nameInit.toString();
	}

	public static String getContactInfo(ContactModel contact) {
		int preferredChannel = getContactPreferredChannel(contact);
		switch(preferredChannel) {
		case AppConstants.CONTACT_PREFERRED_EMAIL:
			if(contact.getEmail()!=null){
				return contact.getEmail();
			}			
		default:
			if(contact.getPhone2()!=null){ //mobile phone
				return contact.getPhone2();
			}else if(contact.getPhone3()!=null){
				return contact.getPhone3();
			}else if(contact.getPhone1()!=null){//home phone
				return contact.getPhone1();
			}else if(contact.getEmail()!=null){
				return contact.getEmail();
			}
			return null;
		}
	}
	
	public static int getContactPreferredIcon(ContactModel contact) {
		int preferredChannel = AppConstants.CONTACT_PREFERRED_MOBILE_PHONE;
		int icon;
		if(contact.getPreferredChannel_value()!=null) {
			try {
				preferredChannel = Integer.parseInt(contact.getPreferredChannel_value());
			} catch (NumberFormatException e) {

			}
		}else{
			if(contact.getPhone1()!=null || contact.getPhone2()!=null || contact.getPhone3()!=null){
				contact.setPreferredChannel_value(String.valueOf(AppConstants.CONTACT_PREFERRED_MOBILE_PHONE));
				return R.drawable.cnt_call;
			}
			if(contact.getEmail()!=null){
				contact.setPreferredChannel_value(String.valueOf(AppConstants.CONTACT_PREFERRED_EMAIL));
				return R.drawable.cnt_eml;
			}
		}

		switch (preferredChannel) {
		case AppConstants.CONTACT_PREFERRED_EMAIL:
		case AppConstants.CONTACT_PREFERRED_E_MAIL:
			icon = R.drawable.cnt_eml;
			break;
		default:
			icon = R.drawable.cnt_call;
		}
		return icon;
	}

	public static int getContactPreferredChannel(ContactModel contact) {
		int preferredChannel = AppConstants.CONTACT_PREFERRED_MOBILE_PHONE;
		if(contact.getPreferredChannel_value()!=null) {
			try {
				preferredChannel = Integer.parseInt(contact.getPreferredChannel_value());
			} catch (NumberFormatException e) {

			}
		}

		switch(preferredChannel) {
		case AppConstants.CONTACT_PREFERRED_EMAIL:
		case AppConstants.CONTACT_PREFERRED_E_MAIL:
			preferredChannel = AppConstants.CONTACT_PREFERRED_EMAIL;
			break;
		default:
			preferredChannel = AppConstants.CONTACT_PREFERRED_MOBILE_PHONE;
		} 
		return preferredChannel;
	}

	public static boolean isFeePaid(FeeModel feeModel) {
		//Need to be implemented

		return false;
	}

	/**
	 * 
	 * Compare the date of two fee
	 * 
	 * @param first fee
	 * @param second fee
	 * 
	 * @return 1: first is new, 0: same, -1: first is old
	 * 
	 * 
	 */

	public static int compareFeeExpireDate(FeeModel fee1, FeeModel fee2) {
		if(fee1.getExpireDate()  == null) {
			return 1;
		} else if(fee2.getExpireDate() == null) {
			return -1;
		} else {
			return fee1.getExpireDate().compareTo(fee2.getExpireDate());
		}
	}


	public static boolean isInspectionFailed(RecordInspectionModel inspection) {
		return checkInspectionStatus(inspection) == AppConstants.INSPECTION_STATUS_FAILED;
	}


	public static int checkInspectionStatus(RecordInspectionModel inspection) {
		int status = AppConstants.INSPECTION_STATUS_UNKNOWN;
		if(inspection!=null) {
			if(inspection.getStatus_value()!=null) {
				if(inspection.getStatus_value().compareToIgnoreCase("Passed") == 0
						//|| inspection.getStatus_value().compareToIgnoreCase("Pending") == 0
						)  {
					status = AppConstants.INSPECTION_STATUS_PASSED;
				} else if(inspection.getStatus_value().compareToIgnoreCase("Scheduled") == 0
						//|| inspection.getStatus_value().compareToIgnoreCase("Pending") == 0
						) {
					status = AppConstants.INSPECTION_STATUS_SCHEDULED;
				} else if(inspection.getStatus_value().compareToIgnoreCase("Failed") == 0 
						|| inspection.getStatus_value().compareToIgnoreCase("Not passed") == 0
						|| inspection.getStatus_value().compareToIgnoreCase("In Violation") ==0 ) {
					status = AppConstants.INSPECTION_STATUS_FAILED;
				} else if(inspection.getStatus_value().compareToIgnoreCase("Rescheduled") == 0 ||
						inspection.getStatus_value().compareToIgnoreCase("Cancelled") == 0) {
					status = AppConstants.INSPECTION_STATUS_CANCELED;
				}
			} 

			if(inspection.getResultType() != null && status == AppConstants.INSPECTION_STATUS_UNKNOWN) { 
				if(inspection.getResultType().compareToIgnoreCase("APPROVED")==0) {
					status = AppConstants.INSPECTION_STATUS_PASSED;
				} else if(inspection.getResultType().compareToIgnoreCase("PENDING")==0) {
					//status = AppConstants.INSPECTION_STATUS_SCHEDULED;
				} else if(inspection.getResultType().compareToIgnoreCase("DENIED")==0) {
					if(inspection.getStatus_value() != null && inspection.getStatus_value().contains("Cancelled")) {
						//cancel
					} else {
						status = AppConstants.INSPECTION_STATUS_FAILED;
					}
				}
			} 
		} 
		return status;
	}

	/**
	 * Get the main info for inspection (to display consistent info in where need to show inspection info) 
	 * @param inspection the inspection model
	 * @return the 2 element of string[]. 1st: inspection group, 2nd: inspection type
	 */

	public static String[] formatInspectionInfo(RecordInspectionModel inspection) {
		String[] info = new String[2];
		if(inspection == null) {
			info[0] = "";
			info[1] = "";
		} else {
			RecordModel record = AppInstance.getProjectsLoader().getRecordById(inspection.getRecordId_id());
			if(record!=null) {
				info[0]  = record.getType_text();// record.getType_group() != null? record.getType_group(): "";
				info[1]  = "";//record.getType_text() + ", ";
			} else {
				info[0]  = inspection.getType() != null ? inspection.getType().getGroup() : "" ;
				info[1] = "";
			}
			info[1] += inspection.getType().getText() ;

		}
		return info;

	}

	/**
	 * Get the main info for inspection type (to display consistent info in where need to show inspection type info) 
	 * @param inspection the inspection type model
	 * @return the 2 element of string[]. 1st: inspection group, 2nd: inspection type
	 */

	public static String[] formatInspectionTypeInfo(DailyInspectionTypeModel inspectionType, String recordId) {
		String[] info = new String[2];
		if(inspectionType == null) {
			info[0] = "";
			info[1] = "";
		} else {
			RecordModel record = AppInstance.getProjectsLoader().getRecordById(recordId);
			if(record!=null) {
				info[0]  = record.getType_text();//record.getType_group() != null? record.getType_group(): "";
				info[1]  = "";//record.getType_text() + ", ";
			} else {
				info[0]  = inspectionType.getGroup() != null ? inspectionType.getGroup() : "" ;
				info[1] = "";
			}
			info[1] += inspectionType.getText() ;

		}
		return info;

	}

	//PROD, TEST, DEV, STAGE, CONFIG, SUPP
	public static Environment StringToEnvironment(String environment){
		Environment ret;
		switch(environment.toUpperCase()){
		case "PROD":
			ret = Environment.PROD;
			break;
		case "TEST":
			ret = Environment.TEST;
			break;
		case "DEV":
			ret = Environment.DEV;
			break;
		case "STAGE":
			ret = Environment.STAGE;
			break;
		case "CONFIG":
			ret = Environment.CONFIG;
			break;
		case "SUPP":
			ret = Environment.SUPP;
			break;
		default:
			ret = Environment.PROD;
			break;
		}
		return ret;
	}

	public static String getContactImagePath(ContactModel contact) {
		final String thumbnailFilePath = UIUtils.makeFilePath(AppContext.context, "Contact", 
				"contact_" + contact.getId() + "_profile.jpg");
		return thumbnailFilePath; 
	}

	public static DailyInspectionTypeModel generateDailyInspectionTypeModel(InspectionTypeModel type) {
		// TODO Auto-generated method stub
		if(type==null)
			return null;
		DailyInspectionTypeModel inspectionTypeModel = new DailyInspectionTypeModel();
		inspectionTypeModel.setGroup(type.getGroup());
		inspectionTypeModel.setText(type.getText());
		inspectionTypeModel.setValue(type.getValue());
		inspectionTypeModel.setId(type.getId());
		return inspectionTypeModel;
	}
	
	public static Date[] getStartEndDates(int year, int month){

		Date[] dates = new Date[2];
		
//		Calendar curCal = Calendar.getInstance();
		Calendar calendar = Calendar.getInstance();
		dates[0] = calendar.getTime();
		calendar.add(Calendar.DATE, 30);
		dates[1] = calendar.getTime();
//		if(curCal.get(Calendar.MONTH)==month){
//			dates[0] = new Date();
//			Calendar calendar = Calendar.getInstance();
//			calendar.add(Calendar.DATE, 31);
//			dates[1] = calendar.getTime();
//		}else{
//			Calendar cal = new GregorianCalendar(year, month, 1);
//			// Get the number of days in that month
//			int daysMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
//			dates[0] = DateUtil.setDate(year, month+1, 1);
//			dates[1] = DateUtil.setDate(year, month+1, daysMonth);
//		}
		return dates;
	}

//	public static Date[] getStartEndDates(int year, int month){
//		// Create a calendar object and set year and month
//		Calendar mycal = new GregorianCalendar(year, month, 1);
//		// Get the number of days in that month
//		int daysInMonth = mycal.getActualMaximum(Calendar.DAY_OF_MONTH);
//		Date[] dates = new Date[2];
//		
//		Calendar cal = Calendar.getInstance();
//		int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
//		
//		if(cal.get(Calendar.MONTH)==month){
//			dates[0] = DateUtil.setDate(year, month+1, dayOfMonth);
//			if(daysInMonth-dayOfMonth>=30)
//				dates[1] = DateUtil.setDate(year, month+1, 30);
//			else
//				dates[1] = DateUtil.setDate(year, month+1, daysInMonth);
//		}else{
//			dates[0] = DateUtil.setDate(year, month+1, 1);
//			dates[1] = DateUtil.setDate(year, month+1, dayOfMonth-1);
//		}
//
//
//		return dates;
//	}
	
	public static RecordInspectionModel modelConvert(InspectionModel inspection){
		RecordInspectionModel recordInspection = new RecordInspectionModel();
		if(inspection.getAddress()!=null)
			recordInspection.setAddress(inspection.getAddress());
		if(inspection.getType()!=null)
			recordInspection.setType(inspection.getType());
		if (inspection.getScheduleDate() != null)
			recordInspection.setScheduleDate(inspection.getScheduleDate());
		if (inspection.getScheduleStartTime() != null)
			recordInspection.setScheduleStartTime(inspection.getScheduleStartTime());
		if (inspection.getScheduleStartAMPM() != null)
			recordInspection.setScheduleStartAMPM(inspection.getScheduleStartAMPM());
		if (inspection.getScheduleEndTime() != null)
			recordInspection.setScheduleEndTime(inspection.getScheduleEndTime());
		if (inspection.getScheduleEndAMPM() != null)
			recordInspection.setScheduleEndAMPM(inspection.getScheduleEndAMPM());
		if (inspection.getRequestComment() != null)
			recordInspection.setRequestComment(inspection.getRequestComment());
		if (inspection.getRequestorFirstName() != null)
			recordInspection.setRequestorFirstName(inspection.getRequestorFirstName());
		if (inspection.getRequestorMiddleName() != null)
			recordInspection.setRequestorMiddleName(inspection.getRequestorMiddleName());
		if (inspection.getRequestorLastName() != null)
			recordInspection.setRequestorLastName(inspection.getRequestorLastName());
		if (inspection.getRequestorPhone() != null)
			recordInspection.setRequestorPhone(inspection.getRequestorPhone());
		if (inspection.getRecordId_id()!=null)
			recordInspection.setRecordId_id(inspection.getRecordId_id());
		if (inspection.getId()!=null)
			recordInspection.setId(inspection.getId());
		if(inspection.getContact()!=null)
			recordInspection.setContact(inspection.getContact());
		if(inspection.getStatus_text()!=null)
			recordInspection.setStatus_text(inspection.getStatus_text());
		if(inspection.getStatus_value()!=null)
			recordInspection.setStatus_value(inspection.getStatus_value());
		return recordInspection;
	}
	
	/**
	 * This API use to format time string like "08:00:00" or "08:00:00 AM" to "08:00" or "08:00 AM"
	 * @param timeStr
	 * @return
	 */
	public static String formatTime(String timeStr) {
		if(timeStr == null) {
			return "";
		}
		return timeStr.replaceAll("(:\\d{1,2})(:\\d{1,2})", "$1");
	}
	
	public static boolean isSameContact(ContactModel contact1, ContactModel contact2) {
		if(contact1 == null || contact2 == null) {
			return false;
		}
		String compareStr1 = contact1.getFirstName() + contact1.getLastName() + contact1.getOrganizationName()
				+ contact1.getPhone1() + contact1.getPhone2() + contact1.getPhone3() + contact1.getEmail() + 
				contact1.getFullName();
		
		String compareStr2 = contact2.getFirstName() + contact2.getLastName() + contact2.getOrganizationName()
				+ contact2.getPhone1() + contact2.getPhone2() + contact2.getPhone3() + contact2.getEmail() + 
				contact2.getFullName();
		
		return compareStr1.compareTo(compareStr2) == 0;
	}
	
	public static boolean isContactExist(List<ContactModel> contacts, ContactModel contact) {
		boolean isExist = false;
		for(ContactModel c: contacts) {
			if(Utils.isSameContact(contact, c)) {
				isExist = true;
				break;
			}
		}			
		return isExist;
	}
	
	
}
