package com.accela.contractorcentral.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.model.InvoiceModel;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.model.ProjectModel.ProjectFee;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.PaymentLoader;
import com.accela.fee.model.FeeModel;
import com.accela.framework.AMBaseModel;
import com.accela.mobile.AMLogger;
import com.accela.mobile.view.list.PullToRefreshListView;
import com.accela.record.model.PaymentModel;

public class FeeListView extends ListView implements Observer{
	private boolean scrollEnabled = true;
	
	int contactLastCount = 0;
	PullToRefreshListView listView;
	int listItemHeight;
	private int maximalDisplayItems = 5;
	int listViewHeight;
	private List<FeeModel> list = new ArrayList<FeeModel>();
	private List<InvoiceModel> invoices = new ArrayList<InvoiceModel>();
	private ProgressBar bar;
	private Context mContext;
	private FeeListViewAdapter adapter;
	private ProjectModel projectModel;

	private View cardFee;

	private TextView textPayment;
	
	public FeeListView(Context context) {
		super(context);
		this.mContext = context;
		init();
	}
	
	public FeeListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init();
	}
	
	public FeeListView(Context context, AttributeSet attrs,  int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		init();
	}
	
	
	protected void init() {
		setHeaderDividersEnabled(false);	
		setFooterDividersEnabled(false);
		setItemHeight();
		adapter = new FeeListViewAdapter();
		this.setAdapter(adapter);
		invoices.clear();
		setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				if(scrollEnabled) {
					return false;
				} else {
					// disable scroll
					return (event.getAction() == MotionEvent.ACTION_MOVE);
				}
			}
		});
	}
	
	public void setListAdjustable(int maximalDisplayItems) {
		this.maximalDisplayItems = maximalDisplayItems;
	}
	
	 protected void setItemHeight() {
		float density = this.getContext().getResources().getDisplayMetrics().density;
		//use the fix item height (hard code)
		listItemHeight = (int) (75*density);
	}
	 
	public void setFees(ProjectModel model, ProgressBar bar, View cardFee, TextView textPayment) {
		model.getFees();
		projectModel = model;
		this.bar = bar;
		this.cardFee = cardFee;
		this.textPayment = textPayment;
		ProjectFee projectFee = projectModel.getFees();
//		projectFee = projectModel.getPayments();
		list.clear();
		list.addAll(projectFee.listAllFee);
		updateInvoices(list);
		if(projectModel.getFees().downloadFeeFlag==AppConstants.FLAG_FULL_DOWNLOAED){//&& projectModel.getPayments().downloadPaymentFlag==AppConstants.FLAG_FULL_DOWNLOAED
			bar.setVisibility(View.GONE);
			if(invoices.size()==0) {
				this.setVisibility(View.GONE);
				this.cardFee.setVisibility(View.GONE);
				this.textPayment.setText(this.mContext.getResources().getText(R.string.no_recent_payment_history));
			} else {
				this.cardFee.setVisibility(View.VISIBLE);
				this.textPayment.setText(this.mContext.getResources().getText(R.string.recent_payment_history));
				this.setVisibility(View.VISIBLE);
			}
		} 
		
//		list.addAll(projectFee.listAllPayment);
		adapter.notifyDataSetChanged();
	}
	
	protected void setListViewHeight() {
		if(listViewHeight!=0) {
			this.getLayoutParams().height = listViewHeight;
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		setListViewHeight();
		AMLogger.logInfo("Contact ListView: l-%d,  t-%d, r-%d, b-%d", l, t, r, b);
		super.onLayout(changed, l, t, r, b);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	    calListViewHeight();
	    int width = getMeasuredWidth();
	    setMeasuredDimension(width, listViewHeight);
	}
	
	protected void calListViewHeight() {
		if(listItemHeight!=0 && maximalDisplayItems>0) {
			int totalItems =  invoices.size();
			AMLogger.logInfo("totalItems: %d", totalItems);

			if(totalItems == 0) {
				listViewHeight = listItemHeight;
			} else {
				listViewHeight = listItemHeight * (totalItems>maximalDisplayItems?maximalDisplayItems:totalItems);
			}
		}
	}
	
	@Override
	protected void onAttachedToWindow() {
		AppInstance.getFeeLoader().addObserver(this);
//		PaymentLoader.getInstance().addObserver(this);
		super.onAttachedToWindow();

	}

	@Override
	protected void onDetachedFromWindow() {
		AppInstance.getFeeLoader().deleteObserver(this);
//		PaymentLoader.getInstance().deleteObserver(this);
		super.onDetachedFromWindow();
	}
	
	
	private static class ViewHolder {
		TextView textAmount;
		TextView textPermitApplication;
		TextView textTime;
		ImageView imageStatus;
	}
	
//	private void generateCompleteList(){
//		if(list==null)
//			return;
//		List<AMBaseModel> localListFee = new ArrayList<AMBaseModel>();
//		List<AMBaseModel> localListPayment = new ArrayList<AMBaseModel>();
//
//		for(int i=0; i<list.size(); i++){
//			if (list.get(i) instanceof FeeModel) {
//				FeeModel feeModel = (FeeModel) list.get(i);
//				if(feeModel!=null && feeModel.getBalanceDue()!=null && feeModel.getBalanceDue()>=0.01){
//					localListFee.add(feeModel);
//				}
//				if(feeModel.getAmount()!=null && feeModel.getBalanceDue()!=null && feeModel.getAmount()>feeModel.getBalanceDue()){
//					PaymentModel payment = new PaymentModel();
//					payment.setAmount(feeModel.getAmount()-feeModel.getBalanceDue());
//					payment.setPaymentDate(feeModel.getApplyDate());
//					if(feeModel.getDescription_value()!=null)
//						payment.setPaymentStatus(feeModel.getDescription_value());
//					localListPayment.add(payment);
//				}
//			}
//		}
//		list.clear();
//		list.addAll(localListFee);
//		list.addAll(localListPayment);
//	}
	
	private class FeeListViewAdapter extends BaseAdapter{
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if(invoices!=null)
				return invoices.size();
			return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}	
		
		@Override
		public long getItemId(int position) {
			return position;
		}	 
		
		@Override
		public View getView(final int position,View convertView, ViewGroup parent) {
			
			ViewHolder viewHolder = null;
			if (null == convertView) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_fee, null);
				AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						listItemHeight, 0);
				convertView.setLayoutParams(lp);
				viewHolder.textAmount = (TextView) convertView.findViewById(R.id.textAmountId);
				viewHolder.textPermitApplication = (TextView) convertView.findViewById(R.id.textPermitApplicationId);
				viewHolder.imageStatus = (ImageView) convertView.findViewById(R.id.imageProfile);
				viewHolder.textTime = (TextView) convertView.findViewById(R.id.textFeeTimeId);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			//convertView.getLayoutParams().height = listItemHeight;
			setListViewItem(position, viewHolder);
			return convertView;		
		}	
	}

	private void setListViewItem(final int position, ViewHolder viewHolder) {
		// Set values for view elements.
		if (invoices.get(position) instanceof InvoiceModel) {
			final InvoiceModel invoiceModel = (InvoiceModel) invoices.get(position);
			if (invoiceModel == null) {
				return;
			}
			if (invoiceModel.getBalanceDue() != null && invoiceModel.getBalanceDue()>0.0) {
				viewHolder.textAmount.setText(this.mContext.getResources().getString(R.string.money_sign) + " "+ String.format("%.2f", invoiceModel.getBalanceDue().floatValue())
						+ " " + this.mContext.getResources().getString(R.string.due));
				viewHolder.imageStatus.setImageDrawable(getResources().getDrawable(R.drawable.money));
				if (invoiceModel.getApplyDate() != null) {
					StringBuffer data = new StringBuffer();
					data.append(this.mContext.getResources().getString(R.string.billed)  + " ")
							.append(new SimpleDateFormat("EEEE, MMM d", Locale
									.getDefault()).format(invoiceModel.getApplyDate()));
					viewHolder.textTime.setText(data);
				}
			}else{
				viewHolder.textAmount.setText(this.mContext.getResources().getString(R.string.money_sign) + " "+ String.format("%.2f", invoiceModel.getAmount().floatValue())
						+ " " + this.mContext.getResources().getString(R.string.paid));
				viewHolder.imageStatus.setImageDrawable(getResources().getDrawable(R.drawable.okay));
				if (invoiceModel.getApplyDate() != null) {
					StringBuffer data = new StringBuffer();
					data.append(this.mContext.getResources().getString(R.string.paid) + " ")
							.append(new SimpleDateFormat("EEEE, MMM d", Locale
									.getDefault()).format(invoiceModel.getApplyDate()));
					viewHolder.textTime.setText(data);
				}
			}
			if (invoiceModel.getRecordCustomId() != null)
				viewHolder.textPermitApplication.setText(invoiceModel.getRecordCustomId());
			
		}
	}
	
	private List<InvoiceModel> updateInvoices(List<FeeModel> listAllFee){
		Map<Long, InvoiceModel> map = new HashMap<Long, InvoiceModel>();
		for(FeeModel fee : listAllFee){
			if(fee.getInvoiceId()==null || fee.getInvoiceId()==0.0)
				continue;
			if(map.containsKey(fee.getInvoiceId())){
				InvoiceModel invoice = map.get(fee.getInvoiceId());
				invoice.setAmount(invoice.getAmount() + fee.getAmount());
				invoice.setApplyDate(fee.getApplyDate());
				invoice.setBalanceDue(invoice.getBalanceDue() + fee.getBalanceDue());
			}else{
				InvoiceModel invoice = new InvoiceModel();
				invoice.setAmount(fee.getAmount());
				invoice.setApplyDate(fee.getApplyDate());
				invoice.setBalanceDue(fee.getBalanceDue());
				invoice.setInvoiceId(fee.getInvoiceId());
				invoice.setRecordCustomId(fee.getRecordId_customId());
				map.put(fee.getInvoiceId(), invoice);
			}
		}
		invoices.addAll(map.values());
		if(invoices.size()==0) {
			this.setVisibility(View.GONE);
			this.cardFee.setVisibility(View.GONE);
			this.textPayment.setText(this.mContext.getResources().getText(R.string.no_recent_payment_history));
		} else {
			this.cardFee.setVisibility(View.VISIBLE);
			this.textPayment.setText(this.mContext.getResources().getText(R.string.recent_payment_history));
			this.setVisibility(View.VISIBLE);
		}
		return invoices;
	}
	
	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		adapter.notifyDataSetChanged();
		
		list.clear();
		if( AppInstance.getFeeLoader().getFeeItemByRecord((String)data)!=null)
			list.addAll(AppInstance.getFeeLoader().getFeeItemByRecord((String)data).listAllFee);
		updateInvoices(list);
		if(data!=null){
			bar.setVisibility(View.GONE);
		}
//		this.generateCompleteList();
		AMLogger.logInfo("update: %d", list.size());
		requestLayout();
	}
	
}
