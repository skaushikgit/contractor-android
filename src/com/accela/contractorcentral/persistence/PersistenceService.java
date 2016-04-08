package com.accela.contractorcentral.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

class PersistenceService extends SQLiteOpenHelper{
	//Database name
	private static final String DATABASE_NAME = "contractor.db";

	//Common column names
	public static final String COLUMN_ID = "_id";

	//Contact Table - Column names
	public static final String TABLE_CONTACTS = "contacts";
	public static final String COLUMN_PROJECT_ID = "recordId";
	public static final String COLUMN_FIRST_NAME = "firstName";
	public static final String COLUMN_LAST_NAME = "lastName";
	public static final String COLUMN_PHONE = "phone";
	public static final String COLUMN_IMAGE_PATH = "imagePath";

	//Calender Table - Column names
	public static final String TABLE_CALENDER_FLAG = "calenderFlag";
	public static final String COLUMN_INSPECTION_ID = "inspectionId";
	public static final String COLUMN_STATUS = "status";
	
	private static final String CREATE_TABLE_CONTACTS = "create table "
			+ TABLE_CONTACTS + " ( " + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_PROJECT_ID + " text, "+ COLUMN_FIRST_NAME
			+ " text, " + COLUMN_LAST_NAME
			+ " text," + COLUMN_PHONE
			+ " text," + COLUMN_IMAGE_PATH
			+ " text );";


	private static final String CREATE_TABLE_CALENDER_FLAG = "create table "
			+ TABLE_CALENDER_FLAG + " ( " 
			+ COLUMN_ID + " integer primary key autoincrement, " 
			+ COLUMN_INSPECTION_ID + " text, "
			+ COLUMN_STATUS + " INTEGER );";
	

	public PersistenceService(Context context){
		this(context, DATABASE_NAME, null, 1);
	}

	public PersistenceService(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_TABLE_CONTACTS);
		db.execSQL(CREATE_TABLE_CALENDER_FLAG);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALENDER_FLAG);
		onCreate(db);
	}

}
