package com.accela.contractorcentral.persistence;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.widget.Toast;

import com.accela.contractorcentral.R;
import com.accela.contractorcentral.utils.Utils;
import com.accela.contractorcentral.view.ContactListView;
import com.accela.record.model.ContactModel;

public class ContactsPersistence {
	private SQLiteDatabase database;
	private PersistenceService dbHelper;
	private String[] allColumns = { PersistenceService.COLUMN_FIRST_NAME, PersistenceService.COLUMN_LAST_NAME, PersistenceService.COLUMN_PHONE,
			PersistenceService.COLUMN_IMAGE_PATH};
	private Context mContext;
	
	public ContactsPersistence(Context context) {
		mContext = context;
		dbHelper = new PersistenceService(context);
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}
	
	public long saveContact(ContactModel contact){
		ContentValues values = new ContentValues();
		values.put(PersistenceService.COLUMN_PROJECT_ID, contact.getRecordId_id());
	    values.put(PersistenceService.COLUMN_FIRST_NAME, contact.getFirstName());
	    values.put(PersistenceService.COLUMN_LAST_NAME, contact.getLastName());
	    if(contact.getPhone1()!=null)
	    	values.put(PersistenceService.COLUMN_PHONE, contact.getPhone1());
	    else if(contact.getPhone2()!=null)
	    	values.put(PersistenceService.COLUMN_PHONE, contact.getPhone2());
	    else if(contact.getPhone3()!=null)
	    	values.put(PersistenceService.COLUMN_PHONE, contact.getPhone3());
	    String imagePath = "";
	    if(contact.getProfileImagePath()!=null) {
	    	imagePath = contact.getProfileImagePath();
	    } 
	    values.put(PersistenceService.COLUMN_IMAGE_PATH, imagePath);
	    
	    return database.insert(PersistenceService.TABLE_CONTACTS, null, values);
	}
	
	public List<ContactModel> queryContacts(String projectId) {
		List<ContactModel> contacts = new ArrayList<ContactModel>();
		Cursor cursor = database.query(PersistenceService.TABLE_CONTACTS, allColumns, PersistenceService.COLUMN_PROJECT_ID + "=?", new String[] {projectId}, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ContactModel contact = cursorToNextContact(cursor);
			contacts.add(contact);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return contacts;
	}

	private ContactModel cursorToNextContact(Cursor cursor) {
		ContactModel contact = new ContactModel();
		contact.setFirstName(cursor.getString(0));
		contact.setLastName(cursor.getString(1));
		contact.setPhone1(cursor.getString(2));
		contact.setProfileImagePath(cursor.getString(3));
		return contact;
	}

	private Cursor getPhoneCursor(int type, String contactID){
		return mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = ? AND "
								+ ContactsContract.CommonDataKinds.Phone.TYPE
								+ " = "
								+ type,
						new String[] { contactID }, null);
	}
	
	private Bitmap getImage(String contactID){
		Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactID));
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = mContext.getContentResolver().query(photoUri, new String[] {ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return BitmapFactory.decodeStream(new ByteArrayInputStream(data));
                }
            }
        } finally {
            cursor.close();
        }
        return null;
	}
	
	public void fetchContactsToList(final Uri contactData, final String projectId, final View view, final List<ContactModel> contactList) {
		// TODO Auto-generated method stub
			Thread thread = new Thread(new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
				 	Cursor cursor = mContext.getContentResolver().query(contactData, null, null, null, null);
					String contactID = "";
					String contactNumber = "";
					String name = "";
					if (cursor.moveToFirst()) {
						int nameIndex = cursor.getColumnIndex(Contacts.DISPLAY_NAME);
						name = cursor.getString(nameIndex);
						contactID = cursor.getString(cursor
								.getColumnIndex(ContactsContract.Contacts._ID));
					}

					// Using the contact ID now we will get contact phone number
					Cursor cursorPhone = getPhoneCursor(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, contactID);
					if(cursorPhone.getCount()==0)
						cursorPhone = getPhoneCursor(ContactsContract.CommonDataKinds.Phone.TYPE_WORK, contactID);
					if(cursorPhone.getCount()==0)
						cursorPhone = getPhoneCursor(ContactsContract.CommonDataKinds.Phone.TYPE_HOME, contactID);
					if (cursorPhone.moveToFirst()) {
						contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					}
					
					Bitmap bitmap = getImage(contactID);
					
					ContactModel contact = new ContactModel();
					if(name!=null && name.length()>0){
						String[] names = name.split(" ");
						if(names.length>1){
							contact.setFirstName(names[0]);
							contact.setLastName(names[1]);
						}else{
							contact.setFirstName(name);
						}
					}
					contact.setRecordId_id(projectId);
					contact.setPhone1(contactNumber);
					contact.setProfileImagePath(saveFile(bitmap));
					if(Utils.isContactExist(contactList, contact)) {
						view.post(new Runnable(){
							@Override
							public void run() {
								
								Toast.makeText(view.getContext(), view.getContext().getString(R.string.contact_have_been_added), Toast.LENGTH_LONG)
								.show();
							}
						});
						
						
					} else {
					
						contactList.add(contact);
						view.post(new Runnable(){
							@Override
							public void run() {
								
								if(view instanceof ContactListView)
									((ContactListView) view).setContacts(contactList);
							}
						});
						saveContact(contact);
					}
					cursorPhone.close();
					cursor.close();
				}
			});
			thread.start();
	}
	
	String saveFile(Bitmap bmp){
		FileOutputStream out = null;
		String filename = String.valueOf(mContext.getFilesDir() + "/" + System.currentTimeMillis()) + ".png";
		try {
		    out = new FileOutputStream(filename);
		    bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
		    // PNG is a lossless format, the compression factor (100) is ignored
		} catch (Exception e) {
		    e.printStackTrace();
		} finally {
		    try {
		        if (out != null) {
		            out.close();
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
		return filename;
	}
}
