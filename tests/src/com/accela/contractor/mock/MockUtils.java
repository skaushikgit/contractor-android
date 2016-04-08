package com.accela.contractor.mock;

import java.util.HashMap;
import java.util.List;

import android.os.Handler;
import android.os.Looper;

import com.accela.framework.model.V3pResponseWrap;
import com.accela.framework.persistence.AMClientRequest;
import com.accela.framework.persistence.AMMobilityPersistence;
import com.accela.framework.persistence.AMStrategy;
import com.accela.framework.persistence.request.AMGet;
import com.accela.framework.serializer.AMModelSerializer;
import com.accela.record.model.RecordModel;
import com.accela.sqlite.framework.util.JSONHelper;

public class MockUtils {
	public static HashMap<String,Object> MockContext = new HashMap<String, Object>();
	public static AMModelSerializer MockModelSerializer = new AMModelSerializer(MockContext);
	public static  JSONHelper MockJsonHelper = new JSONHelper();
	public static AMMobilityPersistence<RecordModel, V3pResponseWrap<List<RecordModel>>> MockAmMobilityPersistence = new AMMobilityPersistence<RecordModel, V3pResponseWrap<List<RecordModel>>>();
	public static AMClientRequest MockRequest = new AMGet("mock");
	public static AMStrategy MockStrategy = new AMStrategy();
	public static Handler MockHandler = new Handler(Looper.getMainLooper());
	
	public static RecordActionMock RecordActionMock = new RecordActionMock();
	public static CivicIdActionMock CivicIdActionMock = new CivicIdActionMock();
	public static AppActionMock AppActionMock = new AppActionMock();

}
