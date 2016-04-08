package com.accela.contractorcentral.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.accela.contractorcentral.service.ImageDecodeEngine;
import com.accela.document.model.DocumentModel;
import com.accela.framework.model.AgencyModel;

public class WebImageView extends ImageView {

	private final static int DOCODING_START = 1;
	private DocumentModel document;
	private AgencyModel agency;
	private String imagePath;
	protected Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if(msg.what==DOCODING_START ) {
				decodeImage();
			}
			super.handleMessage(msg);
		}
		
	};
	
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		handler.removeMessages(DOCODING_START);
		super.onDetachedFromWindow();
	}

	public WebImageView(Context context) {
		super(context);
		
	}

	public WebImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WebImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
		handler.removeMessages(DOCODING_START);
		agency = null;
		this.document =null;
		if(imagePath!=null) {
			handler.sendEmptyMessageDelayed(DOCODING_START, 100);
		}
	}
	
	public void setDocument(DocumentModel document) {
		//delay 100ms to decode, so it can be stop if don't need. (for example, move expanded view quickly)
		handler.removeMessages(DOCODING_START);
		agency = null;
		this.document = document;
		if(document!=null) {
			handler.sendEmptyMessageDelayed(DOCODING_START, 100);
		}
	}
	
	public void setAgency(AgencyModel agency) {
		//delay 100ms to decode, so it can be stop if don't need. (for example, move expanded view quickly)
		handler.removeMessages(DOCODING_START);
		this.agency = agency;
		this.document = null;
		if(agency!=null) {
			handler.sendEmptyMessageDelayed(DOCODING_START, 100);
		}
	}
	
	private void decodeImage() {
		if(document!=null) {
			//maximal decode 2048x2048 bitmap
			ImageDecodeEngine.decodeImage(document, 1024, new ImageDecodeEngine.DecordingDelegate() {
				
				@Override
				public void onDecodeComplete(Bitmap bitmap, Object owner) {
					
					if(bitmap!=null) {
						WebImageView.this.setImageBitmap(bitmap);
						WebImageView.this.setVisibility(View.VISIBLE);
					}
				}
			});
		} else if(agency!=null) {
			//maximal decode 1024x1024 bitmap
			ImageDecodeEngine.decodeImage(agency, 1024, new ImageDecodeEngine.DecordingDelegate() {
				
				@Override
				public void onDecodeComplete(Bitmap bitmap, Object owner) {
					
					if(bitmap!=null) {
						WebImageView.this.setImageBitmap(bitmap);
						WebImageView.this.setVisibility(View.VISIBLE);
					}
				}
			});
		} else if(imagePath!=null) {
			//maximal decode 2048x2048 bitmap
			int width = getWidth();
			int height = getHeight();
			int expectedSize = width > height? width: height;
			expectedSize = expectedSize > 1024? 1024: expectedSize;
			expectedSize = expectedSize < 128? 128: expectedSize;
			ImageDecodeEngine.decodeImage(imagePath, expectedSize, new ImageDecodeEngine.DecordingDelegate() {
				
				@Override
				public void onDecodeComplete(Bitmap bitmap, Object owner) {
					
					if(bitmap!=null) {
						WebImageView.this.setImageBitmap(bitmap);
						WebImageView.this.setVisibility(View.VISIBLE);
					}
				}
			});
		}
	}
	
}