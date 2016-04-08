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
 *   Created by jzhong on 3/26/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */


package com.accela.contractorcentral.service;

import java.util.List;

import com.accela.framework.AMBaseModel;

 
/*
 * The class to store the data instance for the app. 
 */

public class AppInstance {
	
	public static void clearAll() {
		//clear all projects
		if(projectsLoader!=null) {
			projectsLoader.deleteObservers();
			projectsLoader.clearAll();
		}
		projectsLoader = null;
		agencyLoader = null;
	//	documentLoader = null;
		if(inspectionLoader!=null) {
			inspectionLoader.clearAll();
		}
		inspectionLoader = null;
		feeLoader = null;
		inspectorLoader = null;
		appSettingsLoader = null;
	}
	
	
	// project loader instance
	private static ProjectsLoader projectsLoader;
	public static ProjectsLoader getProjectsLoader() {
		if (projectsLoader == null) {
			synchronized (ProjectsLoader.class) {
				// Double check
				if (projectsLoader == null) {
					projectsLoader = new ProjectsLoader();
				}
			}
		}
		return projectsLoader;
	}
	
	private static AgencyLoader agencyLoader;
	public static AgencyLoader getAgencyLoader() {
		if (agencyLoader == null) {
			synchronized (AgencyLoader.class) {
				// Double check
				if (agencyLoader == null) {
					agencyLoader = new AgencyLoader();
				}
			}
		}
		return agencyLoader;
	}
	
	private static DocumentLoader documentLoader;
	public static DocumentLoader getDocumentLoader() {
		if (documentLoader == null) {
			synchronized (DocumentLoader.class) {
				// Double check
				if (documentLoader == null) {
					documentLoader = new DocumentLoader();
				}
			}
		}
		return documentLoader;
	}
	
	private static FeeLoader feeLoader;
	public static FeeLoader getFeeLoader() {
		if (feeLoader == null) {
			synchronized (FeeLoader.class) {
				// Double check
				if (feeLoader == null) {
					feeLoader = new FeeLoader();
				}
			}
		}
		return feeLoader;
	}
	
	private static InspectionLoader inspectionLoader;
	public static InspectionLoader getInpsectionLoader() {
		if (inspectionLoader == null) {
			synchronized (InspectionLoader.class) {
				// Double check
				if (inspectionLoader == null) {
					inspectionLoader = new InspectionLoader();
				}
			}
		}
		return inspectionLoader;
		
	}
	
	private static InspectorLoader inspectorLoader;
	public static InspectorLoader getInspectorLoader() {
		if(inspectorLoader == null){
			synchronized (InspectorLoader.class) {
				// Double check
				if (inspectorLoader == null) {
					inspectorLoader = new InspectorLoader();
				}
			}
		}
		return inspectorLoader;
	}
	
	private static AppSettingsLoader appSettingsLoader;
	public static AppSettingsLoader getAppSettingsLoader() {
		if(appSettingsLoader == null){
			synchronized (AppSettingsLoader.class) {
				// Double check
				if (appSettingsLoader == null) {
					appSettingsLoader = new AppSettingsLoader();
				}
			}
		}
		return appSettingsLoader;
	}

	public interface AppServiceDelegate<T extends AMBaseModel> {
		
		public abstract void onSuccess(List<T> response);
		
		public abstract void onFailure(Throwable error);
	}

}
