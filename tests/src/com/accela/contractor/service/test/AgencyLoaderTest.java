
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
package com.accela.contractor.service.test;

import java.util.Observable;
import java.util.Observer;

import android.test.InstrumentationTestCase;

import com.accela.contractor.service.AgencyLoader;
import com.accela.contractor.service.AgencyLoader.LinkAgencyDelegate;
import com.accela.contractor.service.AppInstance;
import com.accela.contractor.test.Utils;
import com.accela.mobile.AccelaMobile.Environment;

public class AgencyLoaderTest extends InstrumentationTestCase implements Observer{

	private AgencyLoader agencyLoader;
	private boolean agencyNotifyTag = false;
	private MyLinkAgencyDelegate delegate;
	
	
	public AgencyLoaderTest() {
		AppInstance.clearAll();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Utils.login();
		agencyLoader = AppInstance.getAgencyLoader();
		agencyLoader.addObserver(this);
		agencyNotifyTag = false;
		delegate = new MyLinkAgencyDelegate();
	}
	

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	

	@Override
	public void update(Observable observable, Object data) {
		if(observable instanceof AgencyLoader) {
			agencyNotifyTag = true;
		}
	}
	
	private class MyLinkAgencyDelegate implements LinkAgencyDelegate{
		@Override
		public void onComplete(int errorCode) {
			// TODO Auto-generated method stub
			agencyNotifyTag = true;
		}
	}
	
	
	public void testLoadAgencies() throws InterruptedException {
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				agencyLoader.loadAllAgency(false);
			}
		});
		thread.start();
		waitForAgenciesResponse();
		assertNotNull(agencyLoader.getAllAgencies());
		assertTrue(agencyLoader.getAllAgencies().size()>0);
	}
	
	public void testLoadLinkedAgencies() throws InterruptedException {
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				agencyLoader.loadAllLinkedAgency(false);
			}
		});
		thread.start();
		waitForAgenciesResponse();
		assertNotNull(agencyLoader.getAllLinkedAgencies());
		assertTrue(agencyLoader.getAllLinkedAgencies().size()>0);
	}
	
	
	public void testRemoveAndAddAgency() throws InterruptedException {
		assertNotNull(agencyLoader.getAllLinkedAgencies());
		assertTrue(agencyLoader.getAllLinkedAgencies().size()>0);
		int num = agencyLoader.getAllLinkedAgencies().size();
		final String agencyName = agencyLoader.getAllLinkedAgencies().get(0).getName();
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				agencyLoader.removeAgency(agencyName, Environment.PROD.toString(), delegate);
			}
		});
		thread.start();
		waitForAgenciesResponse();
		assertTrue(agencyLoader.getAllLinkedAgencies().size()==(num-1));
		
		thread = new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				agencyLoader.linkAgency(agencyName, Environment.PROD.toString(), "ca@accela.com", "test1234", delegate);
			}
		});
		thread.start();
		waitForAgenciesResponse();
		assertTrue(agencyLoader.getAllLinkedAgencies().size()==num);
	}
	
	private void waitForAgenciesResponse() throws InterruptedException {
		// TODO Auto-generated method stub
		agencyNotifyTag = false;
		while(true){
			if(this.agencyNotifyTag)
				break;
			Thread.sleep(2000);
		}
	}
	
}
