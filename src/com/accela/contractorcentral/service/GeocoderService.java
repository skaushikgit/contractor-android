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
 *   Created by jzhong on 2/13/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */


package com.accela.contractorcentral.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.PointF;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.accela.contractorcentral.AppContext;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.utils.Utils;
import com.accela.framework.model.AddressModel;
import com.accela.mobile.AMLogger;

public class GeocoderService {

	public interface GeocoderDelegate {
		void onComplete(boolean successful, float latitude, float longitude);
		
	}
	
	public interface GeocoderBatchDelegeate {
		void onComplete();
		void onProgress(boolean successful, ProjectModel project);
	}
	
	/**
	 * get the geo location async for one address
	 * @param addressModel
	 * @param delegate
	 */
	public static void getGeoLocationByAddressAsync(AddressModel addressModel, GeocoderDelegate delegate) {
		if(addressModel == null || delegate == null) {
			return;
		}
		reverseGeoTask(addressModel, delegate);
	}
	
	public static void getGeoLocatoinByProjectsAsync(List<ProjectModel> projects, GeocoderBatchDelegeate delegate) {
		reverseGeoBatchTask(projects, delegate);
	}
	
	private static void reverseGeoTask(final AddressModel addressModel, final GeocoderDelegate delegate) {
		new AsyncTask<AddressModel, Integer, PointF> () {
			
			@Override
			protected PointF doInBackground(AddressModel... params) {
				AddressModel model = params[0];
				
				Geocoder geocoder = new Geocoder(AppContext.context);
				try {
					List<Address> list = geocoder.getFromLocationName(Utils.getAddressLine1(model) + "," + Utils.getAddressLine2(model), 1);
					if(list.size()>0) {
						Address address = list.get(0);
						PointF location = new PointF();
						location.x = (float) address.getLatitude();
						location.y = (float) address.getLongitude();
						AMLogger.logInfo("Address geo: (%f,  %f)", location.x , location.y);
						return location;
					}
					
				} catch (IOException e) {

				}
				AMLogger.logInfo("Failed to get Address geo: " + Utils.getAddressLine1(model) + "," + Utils.getAddressLine2(model));
				return null;
				
			}
			
			@Override
			protected void onPostExecute(PointF location) {
				if(location != null) {
					delegate.onComplete(true, location.x, location.y);
				} else {
					delegate.onComplete(false, 0, 0);
				}
			}
			
		}.execute(addressModel);
		
		
	}
	
	@SuppressWarnings("unchecked")
	private static void reverseGeoBatchTask (List<ProjectModel> projects, final GeocoderBatchDelegeate delegate) {
		//copy the list. it would be safe in case list is modified outside.
		List<ProjectModel> list = new ArrayList<ProjectModel>();
		list.addAll(projects);
		
		new AsyncTask<List<ProjectModel>, ProjectModel, Void> () {
			
			@Override
			protected void onPostExecute(Void result) {
				delegate.onComplete();
			}

			@Override
			protected void onProgressUpdate(ProjectModel... values) {
				ProjectModel project = values[0];
				boolean successful = true;
				delegate.onProgress(successful, project);
			}

			@Override
			protected Void doInBackground(final List<ProjectModel>... params) {
				final List<ProjectModel> projects = params[0];
				final Geocoder geocoder = new Geocoder(AppContext.context);
				
				for(int i=0; i< projects.size(); i++) {
					final ProjectModel project = projects.get(i);
					if(!project.isGeoLocationAvailable()) {
						final AddressModel addressModel = project.getAddress();
						final PointF location = new PointF(0,0);
						try {
							final List<Address> list = geocoder.getFromLocationName(Utils.getAddressLine1(addressModel) + "," + Utils.getAddressLine2(addressModel), 1);
							if(list.size()>0) {
								final Address address = list.get(0);
								
								location.x = (float) address.getLatitude();
								location.y = (float) address.getLongitude();
								AMLogger.logInfo("Address geo: (%f,  %f)", location.x , location.y);
								 // Escape early if cancel() is called
							} else {
								
							}
							
						} catch (final IOException e) {
							AMLogger.logInfo("Failed to get Address geo: " + Utils.getAddressLine1(addressModel) + "," + Utils.getAddressLine2(addressModel));
							
						}
						project.setGeoLocation(location);
					} else {
						//do nothing.
					}
					
					this.publishProgress(project);
					if (isCancelled()) 
						break;
				}
				return null;
				
			}

		}.execute(list);
		
	}
}
