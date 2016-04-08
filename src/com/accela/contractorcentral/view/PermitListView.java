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
 *   Created by jzhong on 3/20/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */


package com.accela.contractorcentral.view;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.accela.contractorcentral.R;
import com.accela.contractorcentral.model.ProjectModel;
import com.accela.contractorcentral.utils.Utils;
import com.accela.record.model.RecordModel;


public class PermitListView extends ElasticListView {	
 
	ProjectModel projectModel;
	ListViewAdapterEx adapter;
	List<RecordModel> listPermit;

	View headerView;
	private int expandIndex = 0;
	public PermitListView(Context context) {
		super(context);
	}
	
	public PermitListView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}
	
	public PermitListView(Context context, AttributeSet attrs,  int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setProject(ProjectModel project) {
		projectModel = project;
		listPermit = project.getRecords();
		
		if(headerView == null) {
			headerView = LayoutInflater.from(getContext()).inflate(R.layout.list_header_address, null);
			this.addHeaderView(headerView);
		}
		adapter = new ListViewAdapterEx();
		this.setAdapter(adapter);
		TextView textAddLine1 = (TextView) headerView.findViewById(R.id.textAddLine1);
		textAddLine1.setText(Utils.getAddressLine1AndUnit(projectModel.getAddress()));
		
		TextView textAddLine2 = (TextView) headerView.findViewById(R.id.textAddLine2);
		textAddLine2.setText(Utils.getAddressLine2(projectModel.getAddress()));
		this.setFadingEdgeLength(0);
		this.setHorizontalFadingEdgeEnabled(false);
		this.setVerticalFadingEdgeEnabled(false);

		
	}
	
	
	private static class ViewHolder {
		TextView textPermitType;
		TextView textPermitId;
		TextView textStatus;
		TextView textMoreInfo1;
		TextView textMoreInfo2;
		ImageView imageInfo;
		ViewGroup detailsLayout;
	}
	
	private class ListViewAdapterEx extends BaseAdapter{
		
		
		@Override
		public long getItemId(int position) {
			return (long) position;
		}	 
		
		@Override
		public View getView(final int position,View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (null == convertView) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_permit, null);
				viewHolder.textPermitType = (TextView) convertView.findViewById(R.id.textPermitType);
				viewHolder.textPermitId = (TextView) convertView.findViewById(R.id.textPermitId);
				viewHolder.textStatus = (TextView) convertView.findViewById(R.id.textStatus);
				viewHolder.textMoreInfo1 = (TextView) convertView.findViewById(R.id.textMoreInfo1);
				viewHolder.textMoreInfo2 = (TextView) convertView.findViewById(R.id.textMoreInfo2);
				viewHolder.imageInfo = (ImageView) convertView.findViewById(R.id.imageInfo);
				viewHolder.detailsLayout = (ViewGroup) convertView.findViewById(R.id.detailsLayout);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			
			// Hide or show the check box.
			setListViewItem(position, viewHolder);
			return convertView;		
		}

		@Override
		public int getCount() {
			if(listPermit!=null) {
				return listPermit.size();
			} else {
				return 0;
			}
		}
 
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}	
		
		
	}

	private void setListViewItem(final int position, ViewHolder viewHolder) {
		// Set values for view elements.

		final RecordModel record = (RecordModel)  listPermit.get(position);
		if (record == null) {
			return;
		}
		viewHolder.textPermitType.setText( record.getType_text()); //record.getType_module() 
		viewHolder.textPermitId.setText(record.getCustomId()!=null ? record.getCustomId(): record.getId());
		
		
		if(position== expandIndex) {
			viewHolder.detailsLayout.setVisibility(View.VISIBLE);
			if(record.getStatus_text()!=null) {
				viewHolder.textStatus.setText(record.getStatus_text());
			} else {
				viewHolder.textStatus.setText(R.string.not_available);
			}
			viewHolder.textMoreInfo1.setText(R.string.description);
			if(record.getDescription() !=null) {
				viewHolder.textMoreInfo2.setText(record.getDescription());
			} else {
				viewHolder.textMoreInfo2.setText(R.string.not_available);
			}
			viewHolder.imageInfo.setImageResource(R.drawable.info_on);
		} else {
			viewHolder.detailsLayout.setVisibility(View.GONE);
			viewHolder.imageInfo.setImageResource(R.drawable.info_off);
		}
		viewHolder.imageInfo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(expandIndex == position)
					expandIndex = -1;
				else
					expandIndex = position;
				adapter.notifyDataSetChanged();
				
			}
		});
	}
	
	
	


}
