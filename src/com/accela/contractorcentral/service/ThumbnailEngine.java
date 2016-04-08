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
 *   Created by jzhong on 2/2/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */


package com.accela.contractorcentral.service;

import java.util.Observable;
import java.util.concurrent.LinkedBlockingQueue;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.accela.document.model.DocumentModel;
import com.accela.mobile.AMLogger;

public class ThumbnailEngine extends Observable{
	
	private LruCache<String, Thumbnail> mThumbnailCache ;

	LinkedBlockingQueue<String> requestQueue;
	private static ThumbnailEngine instance;
	private int expectedThumbnailSize = 128;
	
	//ThumbnailRequestTask thumbnailRequestTask;
	
	public static ThumbnailEngine getInstance() {
		if (instance == null) {
			synchronized (ThumbnailEngine.class) {
				// Double check
				if (instance == null) {
					instance = new ThumbnailEngine();
				}
			}
		}
		return instance;
	}
	
	private ThumbnailEngine() {
		initThumbnailCache();
		requestQueue = new LinkedBlockingQueue<String>();
	}
	
	private void initThumbnailCache() {
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

	    // Use 1/8th of the available memory for this memory cache.
	    final int cacheSize = maxMemory / 8;

	    mThumbnailCache = new LruCache<String, Thumbnail>(cacheSize) {
	        @Override
	        protected int sizeOf(String key, Thumbnail thumbnail) {
	            // The cache size will be measured in kilobytes rather than
	            // number of items.
	        	if(thumbnail!=null && thumbnail.bitmap!=null) {
	        		return thumbnail.bitmap.getByteCount() / 1024;
	        	} else {
	        		return 0;
	        	}
	        }
	    };
	}
	
	/**
	 * Set expected thumbnail size, only support 64x64 * N (64, 128, 192, 256 ... 512)
	 * @param width
	 */
	public void setThumbnailExpectedSize(int width) {
		int newExpectedThumbSize;
		
		newExpectedThumbSize = 64 * (width  / 64);
		if(newExpectedThumbSize < 64) {
			newExpectedThumbSize = 64;
		} else if(newExpectedThumbSize > 512) {
			newExpectedThumbSize = 512;
		}
		
		if(newExpectedThumbSize != expectedThumbnailSize) {
			//clear all cache thumbnail. 
			 //mThumbnailCache.evictAll();
		}
		expectedThumbnailSize = newExpectedThumbSize;
	}
	
	public Thumbnail queryThumbnail(Object objectKey) {
		Thumbnail thumbnail = null;
		String key = getKeyByObject(objectKey);
		thumbnail = mThumbnailCache.get(key);
		return thumbnail;
	}
	
	public Thumbnail requestThumbnail(Object objectKey) {
		Thumbnail thumbnail = null;
		String key = getKeyByObject(objectKey);
		thumbnail = mThumbnailCache.get(key);
		if(thumbnail!=null) {
			//check and return the thumbnail in cache.
			return thumbnail;
		} else if(requestQueue.contains(key)) {
			//if it is in the request. just return null.
			return null;
		} else {
			AMLogger.logError("Request Queue Size:" + requestQueue.size());
			//add to request queue
			requestQueue.add(key);
		/*	if(thumbnailRequestTask==null) {
				thumbnailRequestTask.execute();
			}*/
			decodeThumbnailForObject(objectKey);
		}
		
		return thumbnail;
	}
	
	private String getKeyByObject(Object objectKey) {
		if(objectKey==null) {
			return "";
		}
		if(objectKey instanceof String) {
			return (String) objectKey;
		} else if(objectKey instanceof DocumentModel) {
			DocumentModel model = (DocumentModel) objectKey;
			return model.getId().toString();
		}
		return "";
	}
	
/*	private class ThumbnailRequestTask extends AsyncTask<Object, Object, Long> {
	     protected Long doInBackground(Object... object) {
	    	 int retry = 0;
	    	 while (retry>=10) {
		    	 Object request = requestQueue.peek();
		    	 if(request==null) {
		    		 retry++;
		    		 try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	 } else {
		    		 decodeThumbnailForObject(request);
		    		 publishProgress(request);
		    		 requestQueue.remove(request);
		    		 
		    	 }
	    	 }
	    	 thumbnailRequestTask = null;
	         return 0l;
	     }

	     protected void onProgressUpdate(Object... request) {
	         setChanged();
	         notifyObservers(request);
	     }

	     protected void onPostExecute(Long result) {
	         
	     }
	}
	*/
	
	
	private void decodeThumbnailForObject(final Object objectKey) {
		ImageDecodeEngine.DecordingDelegate decodingDelegate = new ImageDecodeEngine.DecordingDelegate() {
			@Override
			public void onDecodeComplete(Bitmap bitmap, Object owner) {
				//AMLogger.logInfo("download & decodeThumbnail failed");
				Thumbnail thumbnail = new Thumbnail();
				thumbnail.bitmap = bitmap;
				thumbnail.objectOwner = objectKey; 
				mThumbnailCache.put(getKeyByObject(objectKey), thumbnail);
				requestQueue.remove(getKeyByObject(objectKey));
				setChanged();
				notifyObservers(thumbnail);
			}
		};
		
		if(objectKey instanceof String) {
			String url = (String) objectKey;
			if(url.startsWith("http")) {
				//download from internet
				
			} else {
				//decode the local file
				
			}
			ImageDecodeEngine.decodeImage(url, expectedThumbnailSize, decodingDelegate);
			
		} else if(objectKey instanceof DocumentModel) {
			final DocumentModel document = (DocumentModel) objectKey;
			AMLogger.logInfo("****Start decode thumbnail" + document.getId());
			AMLogger.logInfo("document type:" + document.getEntityType());
			//downloadAndDecodeThumbnail(model);
			ImageDecodeEngine.decodeImage(document, expectedThumbnailSize,  decodingDelegate);
		}
	}
	
	

}
