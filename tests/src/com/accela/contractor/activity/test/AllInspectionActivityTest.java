
/** 
 * Copyright 2014 Accela, Inc. 
 * 
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to 
 * use, copy, modify, and distribute this software in source code or binary 
 * form for use in connection with the web services and APIs provided by 
 * Accela. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE. 
 * 
 * 
 * 
 */

/*
 * 
 * 
 *   Created by eyang on 6/16/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */
package com.accela.contractor.activity.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.json.JSONException;

import com.accela.contractor.AppConstants;
import com.accela.contractor.R;
import com.accela.contractor.activity.AllInspectionActivity;
import com.accela.contractor.mock.MockData;
import com.accela.contractor.model.ProjectModel;
import com.accela.contractor.service.AppInstance;
import com.accela.contractor.service.InspectionLoader;
import com.accela.contractor.service.ProjectsLoader;
import com.accela.contractor.service.InspectionLoader.RecordInspectionItems;
import com.accela.contractor.test.Utils;
import com.accela.contractor.view.AllInspectionListView;
import com.accela.record.model.RecordInspectionModel;
import com.accela.record.model.RecordModel;
import com.accela.sqlite.framework.util.JSONHelper;

import android.graphics.Rect;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.view.Gravity;


public class AllInspectionActivityTest extends ActivityInstrumentationTestCase2<AllInspectionActivity> {

	private AllInspectionActivity mAllInspectionActivity;
	private ProjectsLoader projectsLoader;
	private InspectionLoader inspectionLoader;

	private ProjectModel projectModel1, projectModel2, projectModel3;
	private JSONHelper jsonHelper = new JSONHelper();
	private RecordModel record;
	private RecordInspectionModel[] inspections1 = new RecordInspectionModel[2];
	private RecordInspectionModel[] inspections2 = new RecordInspectionModel[2];
	private RecordInspectionModel[] inspections3 = new RecordInspectionModel[2];
	
	public AllInspectionActivityTest() throws JSONException {
		    super("com.accela.contractor", AllInspectionActivity.class);
			projectsLoader = AppInstance.getProjectsLoader();
			inspectionLoader = AppInstance.getInpsectionLoader();
			projectModel1 = new ProjectModel();
			projectModel2 = new ProjectModel();
			projectModel3 = new ProjectModel();
			
			record = jsonHelper.parseObject(MockData.project1Json, RecordModel.class);
			projectModel1.addRecord(record);
			inspections1[0] = jsonHelper.parseObject(MockData.inspection1Json, RecordInspectionModel.class);
			inspections1[1] = jsonHelper.parseObject(MockData.inspection2Json, RecordInspectionModel.class);
			
			record = jsonHelper.parseObject(MockData.project2Json, RecordModel.class);
			projectModel2.addRecord(record);
			inspections2[0] = jsonHelper.parseObject(MockData.inspection3Json, RecordInspectionModel.class);
			inspections2[1] = jsonHelper.parseObject(MockData.inspection4Json, RecordInspectionModel.class);
			
			record = jsonHelper.parseObject(MockData.project3Json, RecordModel.class);
			projectModel3.addRecord(record);
			inspections3[0] = jsonHelper.parseObject(MockData.inspection5Json, RecordInspectionModel.class);
			inspections3[1] = jsonHelper.parseObject(MockData.inspectionLastMonthJson, RecordInspectionModel.class);
	    	inspectionLoader.deleteObservers();
	}
	
   
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.projectsLoader = AppInstance.getProjectsLoader();
		this.inspectionLoader = AppInstance.getInpsectionLoader();
		Utils.login();
		if(inspectionLoader!=null)
			inspectionLoader.addObserver(this.projectsLoader);
		assertNotNull("projectsLoader is null", projectsLoader);
		projectsLoader.addNewRecordToProjectList(projectsLoader.projectsList, projectModel3.getFirstRecord());
		RecordInspectionItems recordInspectionItems = new RecordInspectionItems();
		recordInspectionItems.downloadFlag = AppConstants.FLAG_FULL_DOWNLOAED;
		recordInspectionItems.recordId = projectModel3.getFirstRecord().getId();
		recordInspectionItems.listApprovedInspection = new ArrayList<RecordInspectionModel>();
		recordInspectionItems.listCompletedInspection = new ArrayList<RecordInspectionModel>();
		recordInspectionItems.listFailedInspection = new ArrayList<RecordInspectionModel>();;
		recordInspectionItems.listInspection = new ArrayList<RecordInspectionModel>();
		recordInspectionItems.listScheduledInspection = new ArrayList<RecordInspectionModel>();
		recordInspectionItems.listInspection.add(inspections3[0]);
		recordInspectionItems.listInspection.add(inspections3[1]);
		recordInspectionItems.listScheduledInspection.add(inspections1[0]);
		recordInspectionItems.listScheduledInspection.add(inspections1[1]);
		recordInspectionItems.listScheduledInspection.add(inspections2[0]);
		recordInspectionItems.listScheduledInspection.add(inspections2[1]);
		recordInspectionItems.listScheduledInspection.add(inspections3[0]);
		recordInspectionItems.listScheduledInspection.add(inspections3[1]);
		recordInspectionItems.listCompletedInspection.add(inspections1[0]);
		recordInspectionItems.listCompletedInspection.add(inspections1[1]);
		recordInspectionItems.listCompletedInspection.add(inspections2[0]);
		recordInspectionItems.listCompletedInspection.add(inspections2[1]);
		recordInspectionItems.listCompletedInspection.add(inspections3[0]);
		recordInspectionItems.listCompletedInspection.add(inspections3[1]);
		inspectionLoader.downloadedInspections.put(recordInspectionItems.recordId, recordInspectionItems);
		projectsLoader.setRecentInpsections(recordInspectionItems.listCompletedInspection);
		projectsLoader.setScheduleInspections(recordInspectionItems.listScheduledInspection);
//		List<RecordInspectionModel> inspections = projectsLoader.getRecentInpsections();
//		Assert.assertNotNull("inspections list is empty!", inspections);
		projectsLoader.setRecentInpsections(recordInspectionItems.listCompletedInspection);
        mAllInspectionActivity = getActivity();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		if(inspectionLoader!=null)
			inspectionLoader.deleteObservers();
    	mAllInspectionActivity = null;
		AppInstance.clearAll();
	}

	public void testInpsections() throws InterruptedException {
				// TODO Auto-generated method stub
		final AllInspectionListView inspectionListView = (AllInspectionListView) this.mAllInspectionActivity.findViewById(R.id.inspectionListViewId);
		Rect rect = new Rect();
		inspectionListView.getHitRect(rect);
		TouchUtils.dragViewToY(this, inspectionListView, Gravity.BOTTOM, rect.top); // To drag left. Make sure the view is to the right of rect.left
		Thread.sleep(500);
	    assertEquals(6, inspectionListView.getCount());
	}
}
