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
 *   Created by jzhong on 3/25/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */


package com.accela.contractorcentral.service;

import java.io.File;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.accela.contractorcentral.AppContext;
import com.accela.contractorcentral.utils.ImageUtils;
import com.accela.document.action.DocumentAction;
import com.accela.document.helper.AMDocumentLoader;
import com.accela.document.helper.AMDocumentLoader.DownloadDelegate;
import com.accela.document.model.DocumentModel;
import com.accela.framework.AMConstants;
import com.accela.framework.model.AgencyModel;
import com.accela.framework.persistence.AMClientRequest;
import com.accela.framework.persistence.request.AMGet;
import com.accela.mobile.AMLogger;
import com.accela.mobile.AccelaMobile;
import com.accela.mobile.util.UIUtils;

public class ImageDecodeEngine {
	
	public interface DecordingDelegate {
		public void onDecodeComplete(Bitmap bitmap, Object owner);
	}
	
	/*
	 * The following APIs is to decode the image file
	 */
	public static void decodeImage(final String imagePath, final int expectedThumbnailSize, 
			final DecordingDelegate delegate) {
		decodeImage(imagePath, expectedThumbnailSize, delegate, false );
	}
	
	private static void decodeImage(final String imagePath, final int expectedThumbnailSize, 
			final DecordingDelegate delegate, boolean forceUseFullImage){
		boolean needFullImage = false;
		if(expectedThumbnailSize > 512 || forceUseFullImage) {
			// it seems AA only return 128x96 thumbnail, if request bigger thumbnail, we should download full document
			needFullImage = true;
				
		} else {
			needFullImage = false;
		}
		decodeLocalImage(imagePath, expectedThumbnailSize, delegate, needFullImage);
		
	}
	
	/*
	 * The following APIs is to download and decode the image of document
	 */
	public static void decodeImage(final DocumentModel document, final int expectedThumbnailSize, 
			final DecordingDelegate delegate) {
		decodeImage(document, expectedThumbnailSize, delegate, false );
	}
	
	private static void decodeImage(final DocumentModel document, final int expectedThumbnailSize, 
			final DecordingDelegate delegate, boolean forceUseFullImage){
		if(null == document.getId()) {
			return;
		}
		boolean needFullImage = false;
		if(expectedThumbnailSize > 512 || forceUseFullImage) {
			// it seems AA only return 128x96 thumbnail, if request bigger thumbnail, we should download full document
			needFullImage = true;
				
		} else {
			needFullImage = false;
		}
		if(document.getThumbailFilePath()==null) {
			document.setThumbailFilePath(makeThumbnailPath(document, expectedThumbnailSize));
		}
		if(document.getSourceFilePath()==null) {
			document.setSourceFilePath(makeSourcePath(document));
		}
		String agency = AppInstance.getProjectsLoader().getRecordById(document.getRecordId()).getResource_agency();
		String environment = AppInstance.getProjectsLoader().getRecordById(document.getRecordId()).getResource_environment();
		if(needFullImage) {
			AMLogger.logInfo("try to decode full document image: " + document.getId());
			if(isSourceExist(document)){
				//if thumbnail file exist, just decode it directly. 
				AMLogger.logInfo("document file exist, just decode it");
				
				AMLogger.logInfo(document.getSourceFilePath());
				decodeFullImage(document, expectedThumbnailSize, delegate);
				
			} else {
				AMLogger.logInfo("document file not exist, download it at first");
				AMLogger.logInfo(document.getSourceFilePath());
				//if thumbnail file doesn't exist, download it then decode it.
				//String thumbSize = String.format("%d", expectedThumbnailSize);
				DownloadDelegate downloadDelegate = new DownloadDelegate() {
					
					@Override
					public void onSuccess(File file) {
						AMLogger.logInfo("full document download done:" + document.getId() +  " file:" + file.getAbsolutePath());
						
						decodeFullImage(document, expectedThumbnailSize, delegate);
					}
					
					@Override
					public void onStart() {
						
					}
					
					@Override
					public void onFailure(Exception e) {
						AMLogger.logInfo("full document download failed:" + document.getId());
						delegate.onDecodeComplete(null, document);
					}
				};
				new DocumentAction().downloadDocument(agency, environment, String.valueOf(document.getId()),  
						document.getSourceFilePath(), downloadDelegate);
			}
			
		} else {
			AMLogger.logInfo("try to decode document thumbnail image: " + document.getId());
			if(isThumbnailExist(document)){
				//if source file exist, just decode it directly. 
				AMLogger.logInfo("thubmnail file exist, just decode it");
				AMLogger.logInfo(document.getThumbailFilePath());
				decodeThumbnail(document, expectedThumbnailSize, delegate);
				
			} if(isSourceExist(document)){
				//if thumbnail file exist, just decode it directly. 
				AMLogger.logInfo("document file exist, just decode it");
				AMLogger.logInfo(document.getSourceFilePath());
				decodeFullImage(document, expectedThumbnailSize, delegate);
				
			} else {
			
				AMLogger.logInfo("thumbnail file not exist, download it at first:" + document.getId());
				AMLogger.logInfo(document.getThumbailFilePath());
				//if thumbnail file doesn't exist, download it then decode it.
				String thumbSize = String.format("%d", expectedThumbnailSize);
				DownloadDelegate downloadDelegate = new DownloadDelegate() {
					
					@Override
					public void onSuccess(File file) {
						AMLogger.logInfo("download document thumbnail done:" + file.getAbsolutePath());
						AMLogger.logInfo("fileExist:" + file.exists());
						
						decodeThumbnail(document, expectedThumbnailSize, delegate);
					}
					
					@Override
					public void onStart() {
						
					}
					
					@Override
					public void onFailure(Exception e) {
						AMLogger.logInfo("download document thumbnail failed: id-%d" , document.getId());
						//delegate.onDecodeComplete(null, document);
						//if failed to download thumbnail image, try to decode full image to get thumbnail.
						decodeImage(document, expectedThumbnailSize, delegate, true);
					}
				};
				new DocumentAction().downloadImageThumbnail(agency, environment, String.valueOf(document.getId()), thumbSize, thumbSize, 
						document.getThumbailFilePath(), downloadDelegate);

			}
		}
	}
	
	private static String makeThumbnailPath(DocumentModel document, int expectedThumbnailSize) {
		final String thumbnailFilePath = UIUtils.makeFilePath(AppContext.context, AMConstants.RECORD, 
				"document_" + document.getId() + "-" + expectedThumbnailSize + "_thumb.jpg");
		return thumbnailFilePath; 
	}
	
	private static boolean isSourceExist(DocumentModel document) {
		 File file = new File(document.getSourceFilePath());
		 return file.exists();
	}
	
	private static String makeSourcePath(DocumentModel document) {
		final String thumbnailFilePath = UIUtils.makeFilePath(AppContext.context, AMConstants.RECORD, 
				"document_" + document.getId() + ".jpg");
		return thumbnailFilePath; 
	}
	
	private static boolean isThumbnailExist(DocumentModel document) {
		 File file = new File(document.getThumbailFilePath());
		 return file.exists();
	}
	
	/**
	 * This API assume the thumbnail of document is downloaded
	 * @param documentOwner
	 * @param expectedThumbnailSize
	 */
	private static void decodeThumbnail(final DocumentModel documentOwner, final int expectedThumbnailSize, final DecordingDelegate delegate) {
		new AsyncTask<DocumentModel, Integer, Bitmap> () {

			@Override
			protected Bitmap doInBackground(DocumentModel... params) {
				DocumentModel document = params[0];
				Bitmap bitmap = UIUtils.getImageThumbnail(document.getThumbailFilePath(), expectedThumbnailSize, expectedThumbnailSize);
				//UIUtils.transformBitmap(AppContext.context, document.getThumbailFilePath());
				return bitmap;
			}
			
			@Override
			protected void onPostExecute(Bitmap bitmap) {
				delegate.onDecodeComplete(bitmap, documentOwner);
			}
		}.execute(documentOwner);
		
		
	}
	
	/**
	 * This API assume the source of document is downloaded
	 * @param documentOwner
	 * @param expectedThumbnailSize
	 */
	private static void decodeFullImage(final DocumentModel documentOwner, final int expectedThumbnailSize, final DecordingDelegate delegate) {
		new AsyncTask<DocumentModel, Integer, Bitmap> () {

			@Override
			protected Bitmap doInBackground(DocumentModel... params) {
				
				DocumentModel document = params[0];
				Bitmap bitmap = ImageUtils.decodeFullImage(document.getSourceFilePath(), expectedThumbnailSize, expectedThumbnailSize);
				//UIUtils.transformBitmap(AppContext.context, document.getThumbailFilePath());
				if(bitmap != null) {
					AMLogger.logInfo("%s \ndecode full image done: %dx%d", document.getSourceFilePath(), bitmap.getWidth(), bitmap.getHeight());
				} else {
					AMLogger.logInfo("decode full image failed:" + document.getSourceFilePath());
				}
				return bitmap;
			}
			
			@Override
			protected void onPostExecute(Bitmap bitmap) {
				delegate.onDecodeComplete(bitmap, documentOwner);
			}
		}.execute(documentOwner);
		
		
	}
	
	/*
	 * The following APIs to download and decode the image of Agency logo
	 */
	
	public static void decodeImage(final AgencyModel agency, final int expectedThumbnailSize, 
			final DecordingDelegate delegate) {
		
		if(null == agency.getName()) {
			return;
		}
		
		if(agency.getLogoFileName()==null) {
			agency.setLogoFileName(makeAgencyLogoPath(agency, expectedThumbnailSize));
		}

		if(isAgencyLogoFileExist(agency)){
			//if thumbnail file exist, just decode it directly. 
			AMLogger.logInfo("agency logo source file exist, just decode it");
			
			AMLogger.logInfo(agency.getLogoFileName());
			decodeFullImage(agency, expectedThumbnailSize, delegate);
			
		} else {
			AMLogger.logInfo("thumbnail file not exist, download it at first");
			AMLogger.logInfo(agency.getLogoFileName());
			//if thumbnail file doesn't exist, download it then decode it.
			//String thumbSize = String.format("%d", expectedThumbnailSize);
			DownloadDelegate downloadDelegate = new DownloadDelegate() {
				
				@Override
				public void onSuccess(File file) {
					AMLogger.logInfo("download done:" + file.getAbsolutePath());
					AMLogger.logInfo("fileExist:" + file.exists());
					
					decodeFullImage(agency, expectedThumbnailSize, delegate);
				}
				
				@Override
				public void onStart() {
					
				}
				
				@Override
				public void onFailure(Exception e) {
					//AMLogger.logInfo("download thumbnail failed");
					delegate.onDecodeComplete(null, agency);
				}
			};
			downloadAgencyLogo(null, null, agency.getName(), agency.getLogoFileName(), downloadDelegate);

		}
		
	}
	
	private static String makeAgencyLogoPath(AgencyModel agency, int expectedThumbnailSize) {
		final String filePath = UIUtils.makeFilePath(AppContext.context, AMConstants.RECORD, 
				"agency_logo_" + agency.getName() + ".jpg");
		return filePath; 
	}
	
	private static boolean isAgencyLogoFileExist(AgencyModel agency) {
		 File file = new File(agency.getLogoFileName());
		 return file.exists();
	}
	
	private static void downloadAgencyLogo(String agency, String environment, String agencyName, String localFile, DownloadDelegate downloadDelegate)
	{
		AMClientRequest action = new AMGet("/v4/agencies/{agencyName}/logo");
		if(agency!=null)
			action.addCustomParam(AccelaMobile.AGENCY_NAME, agency);
		if(environment!=null)
			action.addCustomParam(AccelaMobile.ENVIRONMENT_NAME, environment);
		action.addUrlParam("agencyName",agencyName,true);
		AMDocumentLoader.download(action, localFile, downloadDelegate);
	}
	
	/**
	 * This API assume the source of Agency Logo is downloaded
	 * @param documentOwner
	 * @param expectedThumbnailSize
	 */
	private static void decodeFullImage(final AgencyModel agency, final int expectedThumbnailSize, final DecordingDelegate delegate) {
		new AsyncTask<AgencyModel, Integer, Bitmap> () {

			@Override
			protected Bitmap doInBackground(AgencyModel... params) {
				
				AgencyModel agency = params[0];
				Bitmap bitmap = ImageUtils.decodeFullImage(agency.getLogoFileName(), expectedThumbnailSize, expectedThumbnailSize);
				//UIUtils.transformBitmap(AppContext.context, document.getThumbailFilePath());
				if(bitmap != null) {
					AMLogger.logInfo("%s \ndecode full image done: %dx%d", agency.getLogoFileName(), bitmap.getWidth(), bitmap.getHeight());
				} else {
					AMLogger.logInfo("decode full image failed:" + agency.getLogoFileName());
				}
				return bitmap;
			}
			
			@Override
			protected void onPostExecute(Bitmap bitmap) {
				delegate.onDecodeComplete(bitmap, agency);
			}
		}.execute(agency);
		
		
	}
	
	
	/**
	 * This API assume the image file is downloaded
	 * @param documentOwner
	 * @param expectedThumbnailSize
	 */
	private static void decodeLocalImage(final String imagePath, final int expectedThumbnailSize, final DecordingDelegate delegate, 
			final boolean forceUseFullImage) {
		new AsyncTask<String, Integer, Bitmap> () {
			@Override
			protected Bitmap doInBackground(String... params) {
				
				String imagePath = params[0];
				Bitmap bitmap = null;
				if(forceUseFullImage) {
					bitmap = ImageUtils.decodeFullImage(imagePath, expectedThumbnailSize, expectedThumbnailSize);
				} else {
					 bitmap = UIUtils.getImageThumbnail(imagePath, expectedThumbnailSize, expectedThumbnailSize);
				}
				//UIUtils.transformBitmap(AppContext.context, document.getThumbailFilePath());
				if(bitmap != null) {
					AMLogger.logInfo("%s \ndecode full image done: %dx%d", imagePath, bitmap.getWidth(), bitmap.getHeight());
				} else {
					AMLogger.logInfo("decode full image failed:" + imagePath);
				}
				return bitmap;
			}
			
			@Override
			protected void onPostExecute(Bitmap bitmap) {
				delegate.onDecodeComplete(bitmap, imagePath);
			}
		}.execute(imagePath);
		
		
	}
	

}
