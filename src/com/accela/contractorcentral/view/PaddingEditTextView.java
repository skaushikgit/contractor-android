package com.accela.contractorcentral.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.EditText;

public class PaddingEditTextView extends EditText{
	private Context mContext;

	public PaddingEditTextView(Context context) {
		super(context);
		this.mContext = context;
		init();
	}
	
	public PaddingEditTextView(Context context, AttributeSet attrs) {
	    super(context, attrs);
		this.mContext = context;
	    init();
	}

	public PaddingEditTextView(Context context, AttributeSet attrs, int defStyleAttr) {
	    super(context, attrs, defStyleAttr);
		this.mContext = context;
	    init();
	}
	
	private void init(){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((Activity)(mContext)).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int leftPadding = (int)displayMetrics.density*15;
		int rightPadding = (int)displayMetrics.density*15;
	    int topPadding = this.getPaddingTop();
	    int bottomPadding = this.getPaddingBottom();
	    setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
	}
}
