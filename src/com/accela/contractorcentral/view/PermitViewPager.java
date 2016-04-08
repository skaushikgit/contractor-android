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
 *   Created by jzhong on 3/9/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */

package com.accela.contractorcentral.view;


import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Service;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.ProjectsLoader;
import com.accela.mobile.AMLogger;
import com.accela.record.model.RecordModel;



public class PermitViewPager extends FrameLayout implements Observer {
	
	protected Context mContext;
	
    private OnSelectPermitListener selectPermitListener;
    
    private ProjectsLoader projectsLoader = AppInstance.getProjectsLoader();
    ProjectModel projectModel;
	List<RecordModel> listPermit;
	
	private ViewPager viewPager;
    private PermitPagerAdapter pagerAdapter;
    private View buttonLeft;
    private View buttonRight;

    
    public interface OnSelectPermitListener {
    	public void onSelectPermit(RecordModel permit, int position);
    }

    
    public PermitViewPager(Context context) {
        this(context, null);
        
    }

    public PermitViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public PermitViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr);
    }
    
	public void setProject(ProjectModel project) {
		projectModel = project;
		listPermit = project.getRecords();
		if(pagerAdapter!= null) {
			this.updateNavigationButton(viewPager.getCurrentItem());
			pagerAdapter.notifyDataSetChanged();
		}
	}
	
    
    public void setOnSelectPermitListener(OnSelectPermitListener l) {
    	selectPermitListener = l;
    }
    
    protected void init(Context context, AttributeSet attrs, int defStyleAttr) {
    	mContext = context;
    	setBackgroundResource(R.drawable.card_background_white);
    	
    	viewPager = new ViewPager(context);
    	
   		//viewPager = (ViewPager) content.findViewById(R.id.viewPager); 
   		viewPager.setAdapter(pagerAdapter = new PermitPagerAdapter());
        
   		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			
			public void onPageSelected(int position) {
				int count = listPermit.size();
				if(position < count && selectPermitListener!=null) {
					RecordModel permit = listPermit.get(position);
					selectPermitListener.onSelectPermit(permit, position);
				} 
				updateNavigationButton(position);
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				
			}
		});
   		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
   		viewPager.setLayoutParams(lp);
   		this.addView(viewPager);
   		
   		//add navigation arrows (left/right button)
   		LayoutInflater layoutInflater = (LayoutInflater) context
      			 .getSystemService(Service.LAYOUT_INFLATER_SERVICE);
      	View navigationView = layoutInflater.inflate(R.layout.navigation_arrows, null, false);
      	lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
      	this.addView(navigationView, lp);
      	
      	buttonLeft = navigationView.findViewById(R.id.buttonLeft);
      	buttonLeft.setOnClickListener(new View.OnClickListener() {
   			
   			@Override
   			public void onClick(View v) {
   				if(viewPager.getCurrentItem() > 0) {
   					viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
   				} else if(pagerAdapter.getCount() > 1){
   					viewPager.setCurrentItem(pagerAdapter.getCount() - 1);
   				}
   			}
   		});

      	buttonRight = navigationView.findViewById(R.id.buttonRight);
  		buttonRight.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(viewPager.getCurrentItem() < pagerAdapter.getCount() - 1) {
					viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
				} else if(pagerAdapter.getCount() > 1) {
					viewPager.setCurrentItem(0);
				}
				
			}
		});	

  		updateNavigationButton(0);
    }
    
    private void updateNavigationButton(int currentPagePos) {
    	if(currentPagePos == 0 || pagerAdapter.getCount()<=1) {
			buttonLeft.setVisibility(View.INVISIBLE);
		} else {
			buttonLeft.setVisibility(View.VISIBLE);
		}
		if(currentPagePos == pagerAdapter.getCount() - 1 || pagerAdapter.getCount()<=1) {
			buttonRight.setVisibility(View.INVISIBLE);
		} else {
			buttonRight.setVisibility(View.VISIBLE);
		}
		
    }
    
    
    @Override
	protected void onAttachedToWindow() {
		projectsLoader.addObserver(this);
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		projectsLoader.deleteObserver(this);
		super.onDetachedFromWindow();
	}
    
	
    private class PermitPagerAdapter extends PagerAdapter{
    	
    	@Override
		public int getItemPosition(Object object) {
			// must add this. force refresh when call notifyDataSetChanged
			return POSITION_NONE;
		}
 
		@Override
  	  	public int getCount() {
			int count = listPermit == null ? 0: listPermit.size();
    		return count; 
  	  	}

  	  	@Override
  	  	public boolean isViewFromObject(View view, Object object) {
  	  		return view == object;
  	  	}
 
  	  	
  	  	@Override
  	  	public Object instantiateItem(ViewGroup container, int position) {
  	  		
  	  		View view = LayoutInflater.from(mContext).inflate(R.layout.viewpager_item_permit, null);
  	  		container.addView(view);
  	  		//View cardHeader = view.findViewById(R.id.cardHeader);
  	  		//cardHeader.getLayoutParams().height = (int) ((48 + 100) * density);
  	  		TextView textPermitType = (TextView) view.findViewById(R.id.textPermitType);
  	  		TextView textPermitId = (TextView) view.findViewById(R.id.textPermitId);
  	  		TextView textStatus = (TextView) view.findViewById(R.id.textStatus);
  	  		if(listPermit == null) {
  	  			return 0;
  	  		}
	  		final RecordModel record = (RecordModel)  listPermit.get(position);
			if (record != null) {
				String format =  "(%d " + mContext.getString(R.string.format_n_of_total) + " %d)";
				String text = record.getType_text() + String.format(format, position + 1, this.getCount());
				textPermitType.setText(text); //record.getType_module() 
				textPermitId.setText(record.getCustomId() != null ? record.getCustomId() : record.getId());
				if(record.getStatus_text()!=null) {
					textStatus.setText(record.getStatus_text());
				} else {
					textStatus.setText(R.string.Under_Review);
				}
			}
			
			
  	  		return view;
  	  	}
    	  

  	  	@Override
  	  	public void destroyItem(ViewGroup container, int position, Object object) {
  	  		
	        container.removeView((View) object);
  	  	}

    }

	@Override
	public void update(Observable observable, Object data) {
		if(observable instanceof ProjectsLoader && data instanceof Integer) {
			int flag = (Integer) data;
			switch (flag) {
				case AppConstants.PROJECT_LIST_CHANGE:
					AMLogger.logInfo("project list changed ");
					pagerAdapter.notifyDataSetChanged();
					updateNavigationButton(this.viewPager.getCurrentItem());
					break;
			}
			
		} 		
	}
}

