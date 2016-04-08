package com.accela.contractorcentral.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CalenderPersistence {
	private SQLiteDatabase database;
	private PersistenceService dbHelper;
	private Context mContext;

	public CalenderPersistence(Context context) {
		mContext = context;
		dbHelper = new PersistenceService(context);
		database = dbHelper.getWritableDatabase();
		database = dbHelper.getReadableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public long saveCalenderStatus(String inspectionId, int status) {
		ContentValues values = new ContentValues();
		values.put(PersistenceService.COLUMN_INSPECTION_ID, inspectionId);
		values.put(PersistenceService.COLUMN_STATUS, status);
		return database.insert(PersistenceService.TABLE_CALENDER_FLAG, null, values);
	}

	public int getCalenderStaus(String inspectionId) {
		int status = 0;
		String selectQuery = "SELECT  * FROM " + PersistenceService.TABLE_CALENDER_FLAG + " WHERE "
				+ PersistenceService.COLUMN_INSPECTION_ID + " = " + inspectionId;

		Cursor c = database.rawQuery(selectQuery, null);

		if(c != null && c.moveToFirst()) {
			int statusIndex = c.getColumnIndex(PersistenceService.COLUMN_STATUS);
			status = c.getInt(statusIndex);
		}
		return status;
	}
}
