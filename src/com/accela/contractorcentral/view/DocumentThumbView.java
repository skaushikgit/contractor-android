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
 *   Created by jzhong on 2/2015.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */

package com.accela.contractorcentral.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.accela.contractorcentral.R;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.DocumentLoader;
import com.accela.contractorcentral.service.Thumbnail;
import com.accela.contractorcentral.service.ThumbnailEngine;
import com.accela.contractorcentral.service.DocumentLoader.DocumentItem;
import com.accela.document.model.DocumentModel;
import com.accela.mobile.AMLogger;
import com.accela.record.model.RecordInspectionModel;
import com.accela.record.model.RecordModel;



public class DocumentThumbView extends FrameLayout implements Observer {	
 

	private boolean scrollEnabled;
	private ThumbnailViewAdapter adapter;
	private int numColumns;
	private int itemHeight;
	private int itemWidth;
	private ThumbnailEngine thumbEngine;
	private int gridViewHeight = 0;
	private int gridViewWidth = 0;
	
	private GridView gridView;
	private HorizontalScrollView scrollView;
	private float density;
	
	private DocumentLoader documentLoader; 
	private ProjectModel project;
	private RecordInspectionModel inspection;
	private int itemSpace;
	private OnDocumentClickListener onClickListener;
	
	private int expectedItemHeight; //dp
	float ratioHeightToWidth;
	int focusedIndex;
	
	private final static int STYLE_1_FIXED_ITEM_HEIGHT 	= 1;
	private final static int STYLE_2_FIXED_COLUMNS_COUNT = 2;
	private final static int STYLE_3_HORIZONTAL_SCROLL = 3;
	int viewStyle = STYLE_1_FIXED_ITEM_HEIGHT;
	
	private List<DocumentModel> listDocument = new ArrayList<DocumentModel>();
	
	
	public interface OnDocumentClickListener {
		public void onDocumentClick(View thumbView, DocumentModel model, int position);
	};
	
	public DocumentThumbView(Context context) {
		super(context);
		init();  
	}
	
	public DocumentThumbView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public DocumentThumbView(Context context, AttributeSet attrs,  int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	/**
	 * Set the view style 1 (default), adjust the cell of thumbnail number to fit the width, the ratio (width to height) is 1
	 * @param expectedItemHeight, the expected item height, will be adjust to fit the width of view
	 */
	
	public void setViewStyle(int expectedItemHeight) {
		this.expectedItemHeight = expectedItemHeight;
		viewStyle = STYLE_1_FIXED_ITEM_HEIGHT;
		createLayout();
		
	}
	
	/**
	 * Set the view to style 1
	 * @param numColumns the number of columns, the width of cell will be width of view/columns number
	 * @param ratioHeightToWidth , ratio of width to height, 
	 */
	
	public void setViewStyle(int numColumns, float ratioHeightToWidth) {
		this.numColumns = numColumns;
		expectedItemHeight = 0;
		this.ratioHeightToWidth = ratioHeightToWidth;
		viewStyle = STYLE_2_FIXED_COLUMNS_COUNT;
		createLayout();
	}
	
	/**
	 * Set the view style 1 (default), adjust the cell of thumbnail number to fit the width, the ratio (width to height) is 1
	 * the gridView width should be large to contain all image into one row.
	 * @param expectedItemHeight, the expected item height, will be adjust to fit the width of view
	 */
	public void setViewStyleHorizontal(int expectedItemHeight) {
		//setBackgroundResource(R.drawable.card_background_white);
		this.expectedItemHeight = expectedItemHeight;
		viewStyle = STYLE_3_HORIZONTAL_SCROLL;
		createLayout();
		
	}
	
	public void enableScroll(boolean enabled) {
		scrollEnabled = enabled;
	}
	
	@Override
	protected void onAttachedToWindow() {
		thumbEngine.addObserver(this);
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		if(thumbEngine!=null) {
			thumbEngine.deleteObserver(this);
		}
		if(documentLoader!=null) {
			documentLoader.deleteObserver(this);
		}
		super.onDetachedFromWindow();
	}

	
	public void setProjectId(String projectId) {
		if(this.project!= null && documentLoader!=null) {
			if(!this.project.getProjectId().equalsIgnoreCase(projectId)) {
				//delete old document service because project Id is different
				//documentLoader.deleteObserver(this);
				//documentLoader = null;
				
			}
		}
		
		project = AppInstance.getProjectsLoader().getProjectById(projectId);
		documentLoader.addObserver(this);
		for(RecordModel record: project.getRecords()) {
			documentLoader.loadDocumentByRecord(record.getId());
		}
		getAllDocumentsFromLoader();		
	}
	 
	public void setInspection(RecordInspectionModel inspection) {
		if(this.inspection!= null && documentLoader!=null) {
			if(this.inspection.getId() != inspection.getId()) {
				
			}
		}
		this.inspection = inspection;
		documentLoader.loadDocumentByInspection(inspection.getRecordId_id(), inspection.getId().toString());
		getAllDocumentsFromLoader();
	}
	
	public void setDocumentList(List<DocumentModel> list, int focusedIndex) {
		listDocument = list;
		
		documentLoader.deleteObserver(this);
		setFocusedIndex(focusedIndex);
	}
	 
	public void setFocusedIndex(int focusedIndex) {
		int oldFocused = this.focusedIndex;
		if(focusedIndex >=0) {
			this.focusedIndex = focusedIndex;
		}
		
		//make the focused item visible
		//only for style 3
		if(viewStyle == STYLE_3_HORIZONTAL_SCROLL && oldFocused != focusedIndex) {
			int scrollX = scrollView.getScrollX();
			int scrollY = scrollView.getScrollY();
			int containerW = this.getWidth();
			int minX = -containerW + (focusedIndex +1)* (itemWidth + itemSpace);
			int maxX = minX + containerW - (itemWidth + itemSpace);
			if(scrollX < minX) {
				scrollView.smoothScrollTo(minX, scrollY);
			} else if(scrollX > maxX) {
				scrollView.smoothScrollTo(maxX, scrollY);
			}

		}
		if(adapter!=null) {
			adapter.notifyDataSetChanged();
		}
	}
	
	public void setOnDocumentClickListener(OnDocumentClickListener l) {
		this.onClickListener = l;
	}
	
	public void setGridViewFocusable (boolean focusable) {
		
		gridView.setFocusable(focusable);
		gridView.setFocusableInTouchMode(focusable);
	}
	
	protected void init() {	
		documentLoader = AppInstance.getDocumentLoader();
		density = this.getContext().getResources().getDisplayMetrics().density;
		thumbEngine = ThumbnailEngine.getInstance();
		
		
	} 
	
	protected void createLayout() {
		this.removeAllViews();
		

		gridView = new GridView(getContext());
		itemSpace =  (int) (density * 3);
		gridView.setVerticalSpacing((int) itemSpace);
		gridView.setHorizontalSpacing((int) itemSpace);
		gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		if(viewStyle == STYLE_3_HORIZONTAL_SCROLL) {
			gridView.setBackgroundColor(Color.BLACK);
			gridView.setStretchMode(GridView.NO_STRETCH);
			//if horizontal scroll, add a horizontal scroll view (this only a quick solution for small amount of images.
			scrollView = new HorizontalScrollView(getContext());
			scrollView.setFillViewport(true);
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
			this.addView(scrollView, lp);
			gridView.setLayoutParams(new GridView.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT));
			
			//Need to wrap the grid view to a LinearLayout before add it to scroll view.
			LinearLayout layout = new LinearLayout(getContext());
			layout.setOrientation(LinearLayout.VERTICAL);
			LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lp2.setMargins(0, 0, 0, 0);
			layout.addView(gridView, lp2);
			
			LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lp3.setMargins(0, 0, 0, 0);
			scrollView.addView(layout, lp3);
			
		} else {
			gridView.setLayoutParams(new GridView.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
			this.addView(gridView);
		}

		gridView.setColumnWidth(GridView.AUTO_FIT);
		if(adapter==null) {
			adapter = new ThumbnailViewAdapter();
		}
		gridView.setAdapter(adapter);
		
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(position >= listDocument.size()) {
					return;
				}
				setFocusedIndex(position);
				DocumentModel model = listDocument.get(position);
				
				if(onClickListener!=null) {
					onClickListener.onDocumentClick(DocumentThumbView.this, model, position);
				}
				
			}
			
		});
		
	}
	
	protected void setItemSize(int width) {
		if(width <= 0 || gridView == null) {
			return;
		}
		
		if(STYLE_1_FIXED_ITEM_HEIGHT == viewStyle)  {
			//calculate item height and columns by style 1:
			float expectItemHeightDp = expectedItemHeight * density;// dp
			numColumns = (int) (width/expectItemHeightDp);
			if(numColumns != 0) {
				itemWidth = width / numColumns ;
				itemHeight = itemWidth;// + 4;	
			}
			itemWidth = itemHeight;  
			gridView.setColumnWidth(itemWidth);
			gridView.setNumColumns(numColumns); 
			AMLogger.logInfo("setItemSize: width-%d, itemHeight-%d, colNums-%d", width, itemHeight, numColumns);
		} else if(STYLE_2_FIXED_COLUMNS_COUNT == viewStyle) {
			//calculate item height and columns by style 2:
			itemWidth = width / numColumns ;
			//itemWidth += 4;
			itemHeight = (int) (itemWidth * ratioHeightToWidth);
			gridView.setColumnWidth(itemWidth);
			gridView.setNumColumns(numColumns); 
			AMLogger.logInfo("setItemSize: width-%d, item-%dx%d, colNums-%d", width,itemWidth,itemHeight, numColumns);
			
		} else if(STYLE_3_HORIZONTAL_SCROLL == viewStyle) {
			
			//calculate item height and columns by style 1:
			float expectItemHeightDp = expectedItemHeight * density;// dp
			numColumns = (int) (width/expectItemHeightDp);
			if(numColumns != 0) {
				itemWidth = width / numColumns;// - 2;
				itemHeight = itemWidth;// + 4;	
			}
			itemWidth = itemHeight;  
			gridView.setColumnWidth(itemWidth );
			gridView.setNumColumns( adapter.getCount()); 
			
			AMLogger.logInfo("setItemSize: width-%d, itemHeight-%d, colNums-%d", width, itemHeight, numColumns);
		}
		
		thumbEngine.setThumbnailExpectedSize(itemWidth);
		
		
	}
	
	protected void calGridViewSize() {
		//need to check how many items then decide the height of gridview.
/*		if(adapter.getCount()==0) {
			gridViewHeight = 1;
		} 
		else if(adapter.getCount() <= numColumns) {
			gridViewHeight = itemHeight;
		} else { 
			gridViewHeight = 2*itemHeight;
		} */
		int row = 0;
		if(viewStyle == STYLE_3_HORIZONTAL_SCROLL) {
			row = 1;
			
		} else {
			
			if(numColumns > 0) {
				row = adapter.getCount() / numColumns + (adapter.getCount() % numColumns > 0? 1: 0) ;
			} else {
				row = 1;
			}
		}
		gridViewHeight = (int) (row * (itemHeight + itemSpace));
		AMLogger.logInfo("=======calculate gridview height: %d", gridViewHeight);
	}
	
	protected void setLayoutSize(int width, int height) {
		if(gridView == null) {
			return;
		}
		boolean needRelayout = false;
		if(viewStyle == STYLE_3_HORIZONTAL_SCROLL) {
			gridViewWidth = this.adapter.getCount() * itemWidth  ;
			if(adapter.getCount()>1) {
				gridViewWidth += itemSpace * (adapter.getCount() - 1);
			}
			if(gridViewWidth != gridView.getLayoutParams().width) {
				gridView.getLayoutParams().width = gridViewWidth;
				scrollView.getLayoutParams().width = width;
				needRelayout = true;
			}
		}
		
		if(gridView.getLayoutParams().height != gridViewHeight) {
			//gridView.getLayoutParams().height = gridViewHeight;
			//gridView.forceLayout();
			//if grid view height change, recreate the gridview. because sometime it can't display sometimes
			//gridView.getLayoutParams().width = width;
			gridView.getLayoutParams().height = gridViewHeight;
			if(scrollView!=null) {
				scrollView.getLayoutParams().height = gridViewHeight ;
			}
			
			AMLogger.logInfo("set gridview height: %d", gridViewHeight);
			needRelayout = true;
		} 
		
		if(needRelayout) {
			//Fix thumbnail view can't display sometimes
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					if(scrollView!=null) {
						scrollView.requestLayout();
					}
					gridView.requestLayout();
					//reset focus index because layout is changed.
					int i = focusedIndex;
					focusedIndex = -1;
					setFocusedIndex(i);
				}
				
			}, 100);

		}
	}
	
	
	  
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		setItemSize(r-l);
		setLayoutSize(r-l, b-t);
		AMLogger.logInfo("DocumentThumbView: l-%d,  t-%d, r-%d, b-%d", l, t, r, b);
		super.onLayout(changed, l, t, r, b);
	}
 
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	    calGridViewSize();
	    int width = getMeasuredWidth();
	    AMLogger.logInfo("DocumentThumbView.onMeasure(): %d x %d", width, gridViewHeight);	    
	    setMeasuredDimension(width, gridViewHeight );
	   
	}
	
	private static class ViewHolder {
		ImageView imageThumbnail;
		ProgressBar spinLoading;
		ImageView imageFrame;
	}	
	
	private class ThumbnailViewAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return listDocument.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder viewHolder = null;
			if (null == convertView) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(DocumentThumbView.this.getContext())
						.inflate(R.layout.thumbnail_item_document, null);
				convertView.setLayoutParams(new GridView.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
				viewHolder.imageThumbnail = (ImageView) convertView.findViewById(R.id.imageView);
				viewHolder.imageThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
				viewHolder.spinLoading = (ProgressBar) convertView.findViewById(R.id.spinLoading);
				viewHolder.imageFrame = (ImageView) convertView.findViewById(R.id.imageFrame);
				
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			if(viewStyle == STYLE_3_HORIZONTAL_SCROLL) {
				if(position==focusedIndex) {
					viewHolder.imageFrame.setVisibility(View.VISIBLE);
					
				} else {
					viewHolder.imageFrame.setVisibility(View.INVISIBLE);
				}
			} else {
				viewHolder.imageFrame.setVisibility(View.INVISIBLE);
			}

			viewHolder.imageThumbnail.getLayoutParams().height = itemHeight ;
			viewHolder.imageThumbnail.getLayoutParams().width = itemWidth ;

			DocumentModel document = listDocument.get(position);
			
			//get or request thumbnail
			Thumbnail thumbnail = thumbEngine.requestThumbnail(document);
			if(thumbnail!=null) {
				viewHolder.spinLoading.setVisibility(View.GONE);
				if(thumbnail.bitmap!=null) {
					viewHolder.imageThumbnail.setImageBitmap(thumbnail.bitmap);
				} else {
					//can't load the thumbnail, display a broken image.
					viewHolder.imageThumbnail.setImageResource(R.drawable.noimage);
				}
			} else {
				viewHolder.spinLoading.setVisibility(View.VISIBLE);
				viewHolder.imageThumbnail.setImageResource(R.drawable.noimage);// R.drawable.blank_image);
			}
			
			return convertView;
		}
		
		
		
	}

	private void getAllDocumentsFromLoader() {
		listDocument.clear();
		if(project != null) {
			for(RecordModel record: project.getRecords()) {
				DocumentItem item = documentLoader.getDocumentByRecord(record.getId());
				listDocument.addAll(item.listDocument);
			}
		}
		else if(inspection != null) {
			DocumentItem item = documentLoader.getDocumentByInspection(inspection.getRecordId_id(), inspection.getId().toString());
			listDocument.addAll(item.listDocument);
		}
	}
	
	
	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		if(observable instanceof  DocumentLoader) {
			getAllDocumentsFromLoader();
			if(adapter!=null) {
				adapter.notifyDataSetChanged();
			}
			this.requestLayout();
		} else if(observable instanceof ThumbnailEngine && (data instanceof Thumbnail)) {
			if(adapter!=null) {
				adapter.notifyDataSetChanged();
			}
		}
	}	
}
