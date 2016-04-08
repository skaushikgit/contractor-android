
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
 *   Created by eyang on 6/15/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */
package com.accela.contractorcentral.view;

import com.accela.contractorcentral.R;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutViewPager extends FrameLayout{
	private Context mContext;
	private ViewPager mViewPager;
	private AboutPagerAdapter mAdapter;
	private PageControlView controlView;
	
	public AboutViewPager(Context context) {
		this(context, null);
    }

    public AboutViewPager(Context context, AttributeSet attrs) {
    	this(context, null, 0);
    }

    public AboutViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

	private void init() {
		// TODO Auto-generated method stub
		mViewPager = new ViewPager(mContext);
        mAdapter = new AboutPagerAdapter();
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){

            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
            	controlView.setCurrrentIndex(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        float density = this.mContext.getResources().getDisplayMetrics().density;
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mViewPager.setLayoutParams(lp);
        this.addView(this.mViewPager);

        controlView = new PageControlView(mContext);
        lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, (int)(10*density));
        lp.gravity = Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;
        this.controlView.setLayoutParams(lp);
        controlView.setCount(4);
        this.addView(controlView);
	}
	
	private class AboutPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.viewpager_item_about, null);
            TextView textView1 = (TextView) view.findViewById(R.id.learnMoreTextId1);
            TextView textView2 = (TextView) view.findViewById(R.id.learnMoreTextId2);
            ImageView imageView = (ImageView) view.findViewById(R.id.learnMoreImageId);
            if (position==0){
                textView1.setText(R.string.about1);
                textView2.setText(R.string.about_description1);
                imageView.setImageResource(R.drawable.about1);
            }else if (position==1){
                textView1.setText(R.string.about2);
                textView2.setText(R.string.about_description2);
                imageView.setImageResource(R.drawable.about2);
            }else if (position==2){
                textView1.setText(R.string.about3);
                textView2.setText(R.string.about_description3);
                imageView.setImageResource(R.drawable.about3);
            }else {
                textView1.setText(R.string.about4);
                textView2.setText(R.string.about_description4);
                imageView.setImageResource(R.drawable.about4);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
	

}
