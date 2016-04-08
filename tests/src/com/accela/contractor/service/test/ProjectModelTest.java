package com.accela.contractor.service.test;


import org.json.JSONException;

import com.accela.contractor.AppConstants;
import com.accela.contractor.mock.MockData;
import com.accela.contractor.model.ProjectModel;
import com.accela.record.model.RecordInspectionModel;
import com.accela.record.model.RecordModel;
import com.accela.sqlite.framework.util.JSONHelper;

import android.test.InstrumentationTestCase;

public class ProjectModelTest extends InstrumentationTestCase{
	private ProjectModel projectModel;
	private JSONHelper jsonHelper;
	private RecordModel record;
	private RecordInspectionModel[] inspections = new RecordInspectionModel[2];

	
	public ProjectModelTest() throws JSONException {
		projectModel = new ProjectModel();
		jsonHelper = new JSONHelper();
		record = jsonHelper.parseObject(MockData.project1Json, RecordModel.class);
		inspections[0] = jsonHelper.parseObject(MockData.inspection1Json, RecordInspectionModel.class);
		inspections[1] = jsonHelper.parseObject(MockData.inspection2Json, RecordInspectionModel.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();		
		projectModel.addRecord(record);
        assertNotNull("projectModel is null", projectModel);
	}
	

	protected void tearDown() throws Exception {
		super.tearDown();
		projectModel.removeRecord(record);
	}
	
	public void testGetRecords(){
        assertNotNull("projectModel.getRecords is null", projectModel.getRecords());
        assertTrue("Record number is >0 in projectModel!", projectModel.getRecords().size()>0);
	}
	
	public void testGetAddress(){
        assertNotNull("projectModel.getAddress() is null", projectModel.getAddress());
        assertEquals("projectModel.getAddress() getStreetStart is wrong", projectModel.getAddress().getStreetStart(), "2633");
        assertEquals("projectModel.getAddress() getPostalCode is wrong", projectModel.getAddress().getPostalCode(), "94583");
        assertEquals("projectModel.getAddress() getCity is wrong", projectModel.getAddress().getCity(), "San Ramon");
	}
	
	public void testGetContacts(){
		assertNotNull("Record number is >0 in projectModel!", projectModel.getContacts());
		assertEquals("contact model number should be equal", projectModel.getContacts().size(), record.getContacts().size());
		assertEquals("contact model phone1 should be equal", projectModel.getContacts().get(0).getPhone1(), record.getContacts().get(0).getPhone1());
		assertEquals("contact model firstname should be equal", projectModel.getContacts().get(0).getFirstName(), record.getContacts().get(0).getFirstName());
		assertEquals("contact model lastname should be equal", projectModel.getContacts().get(0).getLastName(), record.getContacts().get(0).getLastName());
	}
	
	public void testProjectInspections(){
		assertEquals("projectInspection listAllInspection is not 0!", projectModel.projectInspection.listAllInspection.size(), 0);
		projectModel.projectInspection.listAllInspection.add(this.inspections[0]);
		projectModel.projectInspection.listAllInspection.add(this.inspections[1]);
		projectModel.projectInspection.downloadFlag = AppConstants.FLAG_FULL_DOWNLOAED; 
		assertNotNull("projectInspection is null!", projectModel.projectInspection);
		assertNotNull("projectInspection listAllInspection is null!", projectModel.projectInspection.listAllInspection);
		assertEquals("projectInspection number is wrong!", projectModel.projectInspection.listAllInspection.size(), 2);
	}
//	
//	public void testgetRecords(){
//        assertTrue("Record number is >0 in projectModel!", projectModel.getRecords().size()>0);
//	}
//	
//	public void testgetRecords(){
//        assertTrue("Record number is >0 in projectModel!", projectModel.getRecords().size()>0);
//	}
	
}
