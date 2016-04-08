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
 *   Created by jzhong on 3/24/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */

package com.accela.contractorcentral.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.accela.contractorcentral.AppConstants;
import com.accela.contractorcentral.R;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.DocumentLoader;
import com.accela.contractorcentral.service.Thumbnail;
import com.accela.contractorcentral.service.ThumbnailEngine;
import com.accela.contractorcentral.service.DocumentLoader.DocumentItem;
import com.accela.contractorcentral.utils.ActivityUtils;
import com.accela.contractorcentral.view.DocumentThumbView;
import com.accela.contractorcentral.view.WebImageView;
import com.accela.document.model.DocumentModel;
import com.accela.mobile.AMLogger;
import com.accela.record.model.RecordModel;
import com.flurry.android.FlurryAgent;

public class ExpandedImageActivity extends BaseActivity implements Observer {
	
	
	String projectId;
	ProjectModel projectModel;
	ViewPager viewPager;
	List<DocumentModel> listDocument = new ArrayList<DocumentModel>();
	ImageViewPagerAdapter adapter;
	DocumentThumbView thumbView; 
	DocumentLoader documentLoader = AppInstance.getDocumentLoader();
	int focusedIndex;
	
	ThumbnailEngine imageDecoder = ThumbnailEngine.getInstance();
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.logEvent("ExpandedImageActivity");
		FlurryAgent.onPageView();
		FlurryAgent.onStartSession(this, AppConstants.FLURRY_APIKEY);
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	           WindowManager.LayoutParams.FLAG_FULLSCREEN);
		ActionBar actionBar = this.getSupportActionBar(); 
		actionBar.hide();
		//Lock the orientation
		ActivityUtils.setActivityPortrait(this);
		//get project model
		Intent intent = this.getIntent();
		projectId = intent.getStringExtra("projectId");
		projectModel = AppInstance.getProjectsLoader().getProjectById(projectId);
		focusedIndex = intent.getIntExtra("focusedIndex", 0);
		if(projectModel==null) {
			finish();
			AMLogger.logInfo("Error!!, Please select a project");
			return;
		}
		
		setContentView(R.layout.activity_expanded_image);
		ImageView buttonClose = (ImageView) findViewById(R.id.buttonClose);
		buttonClose.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				closeActivity();
			}
		});
		
		//setup thumbnail view
		thumbView = (DocumentThumbView) findViewById(R.id.thumbView);
		getAllDocumentsFromLoader();
		thumbView.setDocumentList(listDocument, focusedIndex);
		thumbView.setViewStyleHorizontal(60);
		thumbView.setGridViewFocusable(false);
		thumbView.setOnDocumentClickListener(new DocumentThumbView.OnDocumentClickListener() {
			  
			@Override
			public void onDocumentClick(View thumbView, DocumentModel model,
					int position) {
				viewPager.setCurrentItem(position);
			}
		});
		
		//setup view pager
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		viewPager.setAdapter(adapter = new ImageViewPagerAdapter());
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			public void onPageSelected(int position) {
				thumbView.setFocusedIndex(position);
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				
			}
		});
		if(focusedIndex>=0) {
			viewPager.setCurrentItem(focusedIndex);
		}
		
		//delay 100ms to set layout 
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				setLayout();
				loadDocuments();
			}
		}, 100);
		
	}

	private class ImageViewPagerAdapter extends PagerAdapter{
    	
    	@Override
		public int getItemPosition(Object object) {
			// must add this. force refresh when call notifyDataSetChanged
			return POSITION_NONE;
		}
 
		@Override
  	  	public int getCount() {
			
    		return listDocument.size(); 
  	  	}

  	  	@Override
  	  	public boolean isViewFromObject(View view, Object object) {
  	  		return view == object;
  	  	}
 
  	  	
  	  	@Override
  	  	public Object instantiateItem(ViewGroup container, int position) {
  	  		
  	  		View contentView = LayoutInflater.from(ExpandedImageActivity.this)
					.inflate(R.layout.thumbnail_item_document, null);
  	  		contentView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
			WebImageView imageView = (WebImageView) contentView.findViewById(R.id.imageView);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			ProgressBar spinLoading = (ProgressBar) contentView.findViewById(R.id.spinLoading);
			
			DocumentModel document = listDocument.get(position);
			
			//get or request thumbnail
			Thumbnail thumbnail = imageDecoder.queryThumbnail(document);
			if(thumbnail!=null) {
				spinLoading.setVisibility(View.GONE);
				if(thumbnail.bitmap!=null) {
					imageView.setImageBitmap(thumbnail.bitmap);
				} else {
					//can't load the thumbnail, display a broken image.
					imageView.setImageResource(R.drawable.noimage);
				}
			} else {
				spinLoading.setVisibility(View.VISIBLE);
				imageView.setImageResource(R.drawable.noimage);// R.drawable.blank_image);
			}
			imageView.setDocument(document);
  	  		
  	  		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
  	  		container.addView(contentView, lp);
  	  		return contentView;
  	  	}

  	  	@Override
  	  	public void destroyItem(ViewGroup container, int position, Object object) {
  	  		View contentView = (View) object;
  	  		WebImageView imageView = (WebImageView) contentView.findViewById(R.id.imageView);
  	  		imageView.setDocument(null);
	        container.removeView((View) object);
  	  	}

    }

	
	private void setLayout() {
		int w = viewPager.getWidth();
		if(w >0) {
			viewPager.getLayoutParams().height = w;
		}
		
	}
	
	private void loadDocuments() {
		getAllDocumentsFromLoader();
		documentLoader.addObserver(this);
		for(RecordModel record: projectModel.getRecords()) {
			documentLoader.loadDocumentByRecord(record.getId());
		}
	}
	
	private void closeActivity() {
		finish();
	}

	@Override
	protected void onDestroy() {
		documentLoader.deleteObserver(this);
		super.onDestroy();
	}

	
	@Override
	public void onBackPressed() {
		closeActivity();
	}

	private void getAllDocumentsFromLoader() {
		listDocument.clear();
		for(RecordModel record: projectModel.getRecords()) {
			DocumentItem item = documentLoader.getDocumentByRecord(record.getId());
			listDocument.addAll(item.listDocument);
		}

	}
	
	@Override
	public void update(Observable observable, Object data) {
		if(observable instanceof  DocumentLoader) {
			getAllDocumentsFromLoader();
			AMLogger.logInfo("Request layout and refresh expanded view: %d", this.adapter.getCount());
			thumbView.setDocumentList(listDocument, -1);
		}
	}
	
}
