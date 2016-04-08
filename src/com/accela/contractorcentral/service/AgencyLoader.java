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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;

import com.accela.contractorcentral.activity.RefreshTokenActivity;
import com.accela.framework.AMException;
import com.accela.framework.UpdateItemResult;
import com.accela.framework.action.AgencyAction;
import com.accela.framework.action.CivicIdAction;
import com.accela.framework.model.AccountModel;
import com.accela.framework.model.AgencyModel;
import com.accela.framework.model.LinkedAccountModel;
import com.accela.framework.persistence.AMAsyncEntityActionDelegate;
import com.accela.framework.persistence.AMAsyncEntityListActionDelegate;
import com.accela.framework.persistence.AMDataIncrementalResponse;
import com.accela.framework.persistence.AMDataResponse;
import com.accela.framework.persistence.AMStrategy;
import com.accela.framework.persistence.AMStrategy.AMAccessStrategy;
import com.accela.mobile.AMBatchSession;
import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
 
public class AgencyLoader extends Observable {
	public static final int LINK_AGENCY_SUCCESSFULLY = 200;
	public static final int LINK_AGENCY_ERROR_NO_ACCOUNT = 500;
	public static final int LINK_AGENCY_ERROR_LOGIN_INFO = 400;
	public static final int LINK_AGENCY_ERROR_ACCOUNT_EXISTED = 400;
	public static final int LINK_AGENCY_ERROR_UNKNOWN = 500;
	
	public static final int REMOVE_AGENCY_SUCCESSFULLY = 600;
	public static final int REMOVE_AGENCY_FAILED  = 700;
	
	public interface LinkAgencyDelegate {
		public void onComplete(int errorCode);
	}
	private CivicIdAction action = new CivicIdAction();
	private List<AgencyModel> listAgency;
	private List<AgencyModel> listLinkedAgency;
	private List<AccountModel> listLinkedAccount;
	private boolean isAgencyDownloaded = false;
	private boolean isAgencyDownloading = false;
	
	private boolean isLinkedAccountDownloaded = false;
	private boolean isLinkedAccountDownloading = false;
	private boolean isAgencyModified = false;
	AgencyLoader() {
		listAgency = new ArrayList<AgencyModel>();
		listLinkedAgency = new ArrayList<AgencyModel>();
		listLinkedAccount = new ArrayList<AccountModel>();
	}
	
	public boolean isAgencyModified() {
		return isAgencyModified;
	}
	
	public void setAgencyModified(boolean modified) {
		isAgencyModified = modified;
	}
	
	
	public void clearAll() {
		isAgencyDownloaded = false;
		isAgencyDownloading = false;
		
		isLinkedAccountDownloaded = false;
		isLinkedAccountDownloading = false;
		listAgency.clear();
		listLinkedAgency.clear();
		listLinkedAccount.clear();
	}
	
	public void setAction(CivicIdAction action){
		this.action = action;
	}

	class ActionDelegateRemoveAgency extends AMAsyncEntityActionDelegate<UpdateItemResult> {
		public LinkAgencyDelegate delegate;
		public AgencyModel agency;
		@Override
		public void onCompleted(AMDataResponse<UpdateItemResult> response) {
			UpdateItemResult itemResult = response.getResult();
			AMLogger.logInfo("Remove agency successfully: %d, %s" , itemResult.getCode(), itemResult.getMessage());
			
			//remove the agency from listLinkedAgency, don't use listLinkedAgency.remove(agency);
			for(int i = 0; i< listLinkedAgency.size(); i++) {
				AgencyModel agencyModel = listLinkedAgency.get(i);
				if(agencyModel.getName().compareToIgnoreCase(agency.getName())==0) {
					listLinkedAgency.remove(i);
					break;
				}
			}
			
			//set the agency linked flag
			agency.setAccountId(null);
			setAgencyModified(true);
			setChanged();
			notifyObservers();
			
			if(delegate!=null) {
				delegate.onComplete(REMOVE_AGENCY_SUCCESSFULLY);
			}
		}

		@Override
		public void onFailure(Throwable error) {
			AMLogger.logInfo("Remove agency error: %s", agency.getName());
			if(delegate!=null) {
				delegate.onComplete(REMOVE_AGENCY_FAILED);
			}
			if(error instanceof AMException) {
				AMException exception = (AMException) error;
				if(exception.getStatus() == AMError.ERROR_CODE_Unauthorized ) {
					RefreshTokenActivity.startRefreshTokenActivity();
				}
			}
			
		}
		
	};
	
	
	class ActionDelegateLinkAgency extends AMAsyncEntityActionDelegate<UpdateItemResult> {
		public LinkAgencyDelegate delegate;
		public AgencyModel agency;
		public String accountId;
		@Override
		public void onCompleted(AMDataResponse<UpdateItemResult> response) {
			UpdateItemResult itemResult = response.getResult();
			AMLogger.logInfo("Link agency: %s, %d, %s" ,agency.getName(), itemResult.getCode(), itemResult.getMessage());
			
			//add the agency into listLinkedAgency
			boolean agencyExisted = false;
			for(AgencyModel agencyModel: listLinkedAgency) {
				if(agencyModel.getName().compareToIgnoreCase(agency.getName())==0) {
					agencyExisted = true;
					return;
				}
			}
			setAgencyModified(true);
			if(!agencyExisted) {
				listLinkedAgency.add(agency);
				//set the agency linked flag
				agency.setAccountId(accountId);
				setChanged();
				notifyObservers();
			}
			if(delegate!=null) {
				delegate.onComplete(LINK_AGENCY_SUCCESSFULLY);
			}
		}

		@Override
		public void onFailure(Throwable error) {
			if(error instanceof AMException) {
				AMException exception = (AMException) error;
				AMLogger.logInfo("Link agency error: %d", exception.getStatus());
				if(delegate!=null) {
					delegate.onComplete(exception.getStatus());
				}
				
			} else {
				if(delegate!=null) {
					delegate.onComplete(LINK_AGENCY_ERROR_UNKNOWN);
				}
			}
			
		}
		
	};
	
	public void removeAgency(String agencyName, String environment, final LinkAgencyDelegate delegate) {
		
		AgencyModel agency = null;
		for(AgencyModel agencyModel: listAgency) {
			if(agencyModel.getName().compareToIgnoreCase(agencyName)==0) {
				agency = agencyModel;
				break;
			}
		}
		if(agency==null) {
			return;
		}
		//find account 
		AccountModel account = null;
		for(AccountModel accountModel:listLinkedAccount ) {
			if(accountModel.getAgencyName().equals(agency.getName())) {
				account = accountModel;
				break;
			}
		}
		if(account == null) {
			return;
		}
		
		ActionDelegateRemoveAgency entityActionDelegate = new ActionDelegateRemoveAgency();
		entityActionDelegate.agency = agency;
		entityActionDelegate.delegate = delegate;
		AMLogger.logInfo("Remove Agency: id-%s name-%s (%s)", account.getId(),  agency.getName(), environment);
		AMStrategy strategy = new AMStrategy(AMAccessStrategy.Http);
		CivicIdAction.removeLinkedAgencyAccountAsync(null, entityActionDelegate, strategy, account.getId(), agency.getName(), environment);
	}
	
	public void linkAgency(String agencyName, String environment, String loginName, String password, final LinkAgencyDelegate delegate) {
		
		AgencyModel agency = null;
		for(AgencyModel agencyModel: listAgency) {
			if(agencyModel.getName().compareToIgnoreCase(agencyName)==0) {
				agency = agencyModel;
			}
		}
		if(agency==null) {
			return;
		}
		ActionDelegateLinkAgency entityActionDelegate = new ActionDelegateLinkAgency();
		entityActionDelegate.agency = agency;
		entityActionDelegate.delegate = delegate;
		entityActionDelegate.accountId = agency.getName();

		AMStrategy strategy = new AMStrategy(AMAccessStrategy.Http);
		CivicIdAction.createLinkedAgencyAccountAsync(null, entityActionDelegate, strategy, agency.getName(), environment, loginName, password);
	}
	
	public List<AgencyModel> getAllLinkedAgencies() {
		//AMLogger.logInfo("Document number: %d", listDocument.size());
		return listLinkedAgency;
	}
	
	public List<AgencyModel> getAllAgencies() {
		//AMLogger.logInfo("Document number: %d", listDocument.size());
		return listAgency;
	}

	public boolean isAllLinkedAgenciesDownloaded() {
		return isLinkedAccountDownloaded && isAgencyDownloaded;
	}
	
	public boolean isAllAgenciesDownloaded() {
		return isAgencyDownloaded;
	}
	
	public boolean loadAllLinkedAgency(boolean forceReload) {
		if(isLinkedAccountDownloading) {
			return isLinkedAccountDownloading;
		} 

		if(!forceReload && isLinkedAccountDownloaded) {
			return isLinkedAccountDownloading;
		}
		isLinkedAccountDownloading = true;
		
		AMStrategy strategy = new AMStrategy(AMAccessStrategy.Http);
		action.getLinkedAccountsAsync(null, new AMAsyncEntityActionDelegate<LinkedAccountModel> () {

			@Override
			public void onCompleted(AMDataResponse<LinkedAccountModel> response) {
				listLinkedAccount.clear();
				LinkedAccountModel linkedAccount = response.getResult();
				List<AccountModel> linkedAccounts = linkedAccount.getCitizenAccounts();
				if(linkedAccounts!=null) {
					listLinkedAccount.addAll(linkedAccounts);
				}
				findAllLinkedAgency();
				isLinkedAccountDownloaded = true;
				isLinkedAccountDownloading = false;
				setChanged();
				notifyObservers();
			}

			@Override
			public void onFailure(Throwable error) {
				AMLogger.logInfo("get linked account failed");
				isLinkedAccountDownloading = false;
				
			}
		}, strategy);
		return isLinkedAccountDownloading;
	}
	
	public boolean loadAllAgency(boolean forceReload) {
		if(isAgencyDownloading) {
			return isAgencyDownloading;
		} 

		if(!forceReload && isAgencyDownloaded) {
			return isAgencyDownloading;
		}
		isAgencyDownloading = true;

		AMStrategy strategy = new AMStrategy(AMAccessStrategy.Http);
		AMBatchSession batchSession = null;
		AMAsyncEntityListActionDelegate<AgencyModel> entityActionDelegate = new AMAsyncEntityListActionDelegate<AgencyModel>() {

			@Override
			public void onCompleted(
					AMDataIncrementalResponse<AgencyModel> response) {
				listAgency.clear();
				AMLogger.logInfo("get agencies successful:%d", response.getResult().size());
				for(AgencyModel agency: response.getResult()) {
					checkAndSaveAgency(agency);
				}
				findAllLinkedAgency();
				isAgencyDownloading = false;
				isAgencyDownloaded = true;
				setChanged();
				notifyObservers();

			}

			@Override
			public void onFailure(Throwable error) {
				AMLogger.logInfo("get agencies failed:");
				isAgencyDownloading = false;
				setChanged();
				notifyObservers();
			}

		};
		AgencyAction.getAgenciesAsync(batchSession, entityActionDelegate, strategy, 0, 100);
		return isAgencyDownloading;
	}

	

	private void checkAndSaveAgency(AgencyModel agency) {
		AMLogger.logInfo("agency %s,  %s", agency.getName(), agency.getDisplay());
		if(agency.getEnabled() != null && agency.getEnabled() && agency.getHostedACA()!= null && agency.getHostedACA()) {
			listAgency.add(agency);
			AMLogger.logInfo("agency can be linked added");
		}
		sortAgencyList(listAgency);
	}
	
	private void findAllLinkedAgency() {
		listLinkedAgency.clear();
		for(AgencyModel agency: listAgency) {
			//check the agency is in linked account
			for(AccountModel account: listLinkedAccount) {
				if(account.getAgencyName().compareToIgnoreCase(agency.getName())==0) {
					agency.setAccountId(account.getName());
					listLinkedAgency.add(agency);
				}
			}
		}
		sortAgencyList(listLinkedAgency);
	}
	
	private void sortAgencyList(List<AgencyModel> list) {
		Collections.sort(list, new Comparator<AgencyModel>() {

			@Override
			public int compare(AgencyModel a1, AgencyModel a2) {
				String s1 = a1.getDisplay() != null? a1.getDisplay(): a1.getName();
				String s2 = a2.getDisplay() != null? a2.getDisplay(): a2.getName();
				return s1.compareToIgnoreCase(s2);
			}
			
		});
	}
	
}
