package com.accela.contractorcentral.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.activity.BaseActivity;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.InstantService;
import com.accela.contractorcentral.service.Thumbnail;
import com.accela.contractorcentral.service.ThumbnailEngine;
import com.accela.contractorcentral.service.AppInstance.AppServiceDelegate;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.utils.Utils;
import com.accela.framework.UpdateItemResult;
import com.accela.mobile.AMLogger;
import com.accela.mobile.util.UIUtils;
import com.accela.mobile.view.list.PullToRefreshListView;
import com.accela.record.model.ContactModel;


public class ContactListView extends ListView implements Observer {	

	private boolean scrollEnabled = true;

	int contactLastCount = 0;
	PullToRefreshListView listView;
	int listItemHeight;
	boolean adjustable;
	int maximalDisplayItems;
	int listViewHeight;
	private ThumbnailEngine thumbEngine;
	private Context mContext;

	List<ContactModel> listContact = new ArrayList<ContactModel>();
	ContactListViewAdapter adapter;

	public ContactListView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public ContactListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public ContactListView(Context context, AttributeSet attrs,  int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}


	protected void init() {
		thumbEngine = ThumbnailEngine.getInstance();
		thumbEngine.setThumbnailExpectedSize(128);
		setHeaderDividersEnabled(false);	
		setFooterDividersEnabled(false);
		adapter = new ContactListViewAdapter();
		setItemHeight();
		this.setAdapter(adapter);
		setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if(scrollEnabled) {
					return false;
				} else {
					// disable scroll
					return (event.getAction() == MotionEvent.ACTION_MOVE);
				}
			}
		});
		
		this.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				try{
					if(listContact!=null && position<listContact.size()){
						final ContactModel contact = listContact.get(position);
						String contactString = Utils.getContactInfo(contact);
						if(contactString==null || contactString.length()==0)
							return;
						if(contact.getPreferredChannel_value()!=null 
								&&  (Integer.parseInt(contact.getPreferredChannel_value())==AppConstants.CONTACT_PREFERRED_EMAIL||Integer.parseInt(contact.getPreferredChannel_value())==AppConstants.CONTACT_PREFERRED_E_MAIL) ){
							UIUtils.showConfirmDialog((Activity) mContext, mContext.getString(R.string.warning_title), 
									mContext.getResources().getString(R.string.email_message_button) + " " + Utils.getContactInfo(contact), new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									Utils.sendEmail((Activity) mContext, contact);
									if(dialog!=null)
										dialog.dismiss();
								}
							});
						}else{
							UIUtils.showConfirmDialog((Activity) mContext, mContext.getString(R.string.warning_title), 
									mContext.getResources().getString(R.string.call) + " " + Utils.getContactInfo(contact), new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									Utils.callPhone((Activity) mContext, contact);
									if(dialog!=null)
										dialog.dismiss();
								}
							});
								 
						}
					}
				}catch(RuntimeException e){
					AMLogger.logError(e.toString());
				}
			}
		});
	}

	protected void setItemHeight() {
		float density = this.getContext().getResources().getDisplayMetrics().density;
		//use the fix item height (hard code), need to check list_item_contact.xml
		listItemHeight = (int) (70*density);

	}

	public void enableScroll(boolean enabled) {
		scrollEnabled = enabled;
	}



	public void setListAdjustable(boolean adjustable, int maximalDisplayItems) {
		this.adjustable = adjustable;
		this.maximalDisplayItems = maximalDisplayItems;
	}

	public void setContacts(List<ContactModel> list) {
		listContact.clear();
		listContact.addAll(list);
		adapter.notifyDataSetChanged();
	}

	public void addContact(ContactModel contact) {
		listContact.add(contact);
		adapter.notifyDataSetChanged();
	}

	private static class ViewHolder {
		TextView textName;
		TextView textContactType;
		ImageView imagePhoneType;
		TextView textPhone;
		TextView textNameInitials;
		RoundedImageView imageProfile;
	}

	@Override
	protected void onAttachedToWindow() {
		if(thumbEngine!=null) {
			thumbEngine.addObserver(this);
		}
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		if(thumbEngine!=null) {
			thumbEngine.deleteObserver(this);
		}
		super.onDetachedFromWindow();
	}

	private class ContactListViewAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return listContact.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}	

		@Override
		public long getItemId(int position) {
			return (long) position;
		}	 

		@Override
		public View getView(final int position,View convertView, ViewGroup parent) {

			ViewHolder viewHolder = null;
			if (null == convertView) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_contact, null);
				AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						listItemHeight, 0);
				convertView.setLayoutParams(lp);
				viewHolder.textName = (TextView) convertView.findViewById(R.id.textName);
				viewHolder.textPhone = (TextView) convertView.findViewById(R.id.textPhone);
				viewHolder.imagePhoneType = (ImageView) convertView.findViewById(R.id.imagePhoneType);
				viewHolder.textContactType = (TextView) convertView.findViewById(R.id.textContactType);
				viewHolder.imageProfile =  (RoundedImageView) convertView.findViewById(R.id.imageProfile);
				viewHolder.textNameInitials = (TextView) convertView.findViewById(R.id.textNameInitials);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			//convertView.getLayoutParams().height = listItemHeight;
			// Hide or show the check box.
			setListViewItem(position, viewHolder);

			return convertView;		
		}
	}


	private void setListViewItem(final int position, ViewHolder viewHolder) {
		// Set values for view elements.
		final ContactModel contactModel = (ContactModel)  listContact.get(position);
		if (contactModel == null) {
			return;
		}

		AMLogger.logInfo("Contact prefer contact channel: %s, %s" , contactModel.getPreferredChannel_value() 
				, contactModel.getPreferredChannel_text());
		viewHolder.textName.setText(Utils.getFullName(contactModel));

		if(contactModel.getType_text()!=null){
			viewHolder.textContactType.setText(contactModel.getType_text());
			viewHolder.textContactType.setVisibility(View.VISIBLE);
			if(contactModel.getType_text().equals(getContext().getString(R.string.reporting_inspector)))
				viewHolder.textContactType.setTextColor(getResources().getColor(R.color.red_orange));
		}else{
			//			viewHolder.textContactType.setText(Utils.getFullName(contactModel));
			//			viewHolder.textContactType.setTextColor(getResources().getColor(R.color.black));
			//			viewHolder.textContactType.setTextSize(14);
			viewHolder.textContactType.setVisibility(View.GONE);
		}
		viewHolder.textNameInitials.setText(Utils.getNameInitials(contactModel));
		String contactInfo = Utils.getContactInfo(contactModel);
		if(contactInfo != null) {
			viewHolder.textPhone.setText(contactInfo);
		}

		int resId = Utils.getContactPreferredIcon(contactModel);
		viewHolder.imagePhoneType.setImageResource(resId);
		if(resId==R.drawable.cnt_eml){
			if(contactModel.getEmail()!=null)
				viewHolder.textPhone.setText(contactModel.getEmail());
			else
				viewHolder.textPhone.setText("");
		}
		

		//get or request thumbnail
		String imagePath = contactModel.getProfileImagePath();
		boolean showDefaultProfile = true;
		if(imagePath!=null && imagePath.length()>0) {
			AMLogger.logInfo("contact profile path:" + imagePath);
			Thumbnail thumbnail = thumbEngine.requestThumbnail(imagePath);
			if(thumbnail!=null && thumbnail.bitmap!=null) {
				viewHolder.textNameInitials.setVisibility(View.INVISIBLE);
				viewHolder.imageProfile.setImageBitmap(thumbnail.bitmap);
				showDefaultProfile = false;
			} 
		} 

		if(showDefaultProfile){
			//can't load the contact profile, display a dot image.
			viewHolder.textNameInitials.setVisibility(View.VISIBLE);
			viewHolder.imageProfile.setImageResource(R.drawable.contact);
			viewHolder.imageProfile.invalidate();
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		adapter.notifyDataSetChanged();

	}

	protected void calListViewHeight() {
		if(listItemHeight!=0 && maximalDisplayItems>0) {
			int totalItems =  listContact.size();
			if(totalItems == 0) {
				listViewHeight = listItemHeight;
			} else {
				listViewHeight = listItemHeight * (totalItems>maximalDisplayItems?maximalDisplayItems:totalItems) ;

			}
		}
	}

	protected void setListViewHeight() {
		if(listViewHeight!=0 && adjustable) {
			this.getLayoutParams().height = listViewHeight;
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		setListViewHeight();
		AMLogger.logInfo("Contact ListView: l-%d,  t-%d, r-%d, b-%d", l, t, r, b);
		super.onLayout(changed, l, t, r, b);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		calListViewHeight();
		int width = getMeasuredWidth();
		//int height = getMeasuredHeight();
		//height = listViewHeight + height;
		if(adjustable) {
			setMeasuredDimension(width, listViewHeight);
		}
	}

}
