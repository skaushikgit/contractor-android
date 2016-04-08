package com.accela.contractor.mock;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.accela.framework.action.CivicIdAction;
import com.accela.framework.model.AccountModel;
import com.accela.framework.model.LinkedAccountModel;
import com.accela.framework.persistence.AMAsyncEntityActionDelegate;
import com.accela.framework.persistence.AMDataIncrementalResponse;
import com.accela.framework.persistence.AMDataResponse;
import com.accela.framework.persistence.AMStrategy;
import com.accela.mobile.AMBatchSession;
import com.accela.record.model.RecordInspectionModel;



public class CivicIdActionMock extends CivicIdAction {



	@Override

public void getLinkedAccountsAsync(AMBatchSession batchSession, final AMAsyncEntityActionDelegate<LinkedAccountModel> entityActionDelegate, AMStrategy strategy){

		Thread thread = new Thread(new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<LinkedAccountModel> accounts = new ArrayList<LinkedAccountModel>();
			final AMDataResponse<LinkedAccountModel> amDataResponse= new AMDataResponse<LinkedAccountModel>(MockUtils.MockRequest, MockUtils.MockStrategy);
			try {
//				accounts.add(MockUtils.MockJsonHelper.parseObject(MockData.project1Json, RecordModel.class));
				LinkedAccountModel linkedaccount = new LinkedAccountModel();
				AccountModel account = new AccountModel();
				account.setName("account for test");
				List<AccountModel> list = new ArrayList<AccountModel>();
				list.add(account);
				linkedaccount.setCitizenAccounts(list);
				accounts.add(linkedaccount);
				amDataResponse.setResult(accounts);
				amDataResponse.setHasMore(false);
				Thread.sleep(500);
				MockUtils.MockHandler.post(new Runnable(){
					@Override
					public void run() {
						// TODO Auto-generated method stub
						entityActionDelegate.onCompleted(amDataResponse);
					}
				});
				
			}catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
			
		}
	});

	thread.start();
}
	


}
