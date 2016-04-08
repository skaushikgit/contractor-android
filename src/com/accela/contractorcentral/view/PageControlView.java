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
 *   Created by jzhong on 3/12/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */

package com.accela.contractorcentral.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.accela.contractorcentral.R;


public class PageControlView extends FrameLayout {

	private float density;
	private LinearLayout layoutContainer;
	private ImageView[] viewPageDots;
	private int focusedIndex;
	
	
    public PageControlView(Context context) {
        this(context, null);
    }

    public PageControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public PageControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }
    
    public void setCurrrentIndex(int index) {
    	focusedIndex = index;
    	for(int i=0; i< viewPageDots.length; i++) {
    		ImageView image = viewPageDots[i];
    		if(i == focusedIndex) {
    			image.setImageResource(R.drawable.active);
    		} else {
    			image.setImageResource(R.drawable.inactive);
    		}
    	}
    }
    
    public void setCount(int count) {
    	if(count <=0) {
    		viewPageDots = null;
    		layoutContainer.removeAllViews();
    	} else {
    		if(viewPageDots == null || count != viewPageDots.length) {
    			addPagnationView(count);
    		}
    	}
    	if(focusedIndex <0 || focusedIndex >= count) {
    		focusedIndex = 0;
    	}
    }
    
    public int getCount() {
    	if(viewPageDots != null) {
    		return viewPageDots.length;
    	} else {
    		return 0;
    	}
    }
    
    private void addPagnationView(int count) {
    	layoutContainer.removeAllViews();
    	viewPageDots = new ImageView[count];
    	int margin = (int) (5*density);
    	int size = (int) (12*density);
    	for(int i=0; i< count; i++) {
    		ImageView image = new ImageView(getContext());
    		viewPageDots[i] = image;
    		image.setScaleType(ScaleType.FIT_XY);
    		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
    		lp.setMargins(margin, 0, margin, 0);
    		//image.setLayoutParams(lp);
    		layoutContainer.addView(image, lp);
    	}
    	setCurrrentIndex(focusedIndex);
    }
    
    protected void init(Context context, AttributeSet attrs, int defStyleAttr) {
    	density = context.getResources().getDisplayMetrics().density;
    	
   		layoutContainer = new LinearLayout(context);
   		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
   		lp.gravity = Gravity.CENTER;
   		this.addView(layoutContainer, lp);
   		
    }
    
    
}

