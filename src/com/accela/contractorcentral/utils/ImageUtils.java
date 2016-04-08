package com.accela.contractorcentral.utils;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.accela.framework.util.AMUtils;
import com.accela.mobile.AMLogger;
import com.accela.mobile.util.UIUtils;

public class ImageUtils {

	/**
	 * Decode image, to avoid out of memory, need to pass width and height to get expected size
	 * @param imagePath
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap decodeFullImage(String imagePath, int expectedWidth, int expectedHeight) {
		Bitmap bitmap = null;
		FileInputStream stream = null;
		//to avoid out of memory, the expected width need to smaller than 2048.
		if(expectedWidth > 2048 || expectedWidth <= 0) {
			expectedWidth = 2048;
		} 
		if(expectedHeight > 2048 || expectedHeight <= 0) {
			expectedHeight = 2048;
		}
		try {
			stream = new FileInputStream(imagePath);
            FileDescriptor fd = stream.getFD();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            if (options.mCancel || options.outWidth <= 0 || options.outHeight <=0) {
                return null;
            }
           
            int sampleSizeX = 1;
            while(sampleSizeX * expectedWidth < options.outWidth ) {
            	sampleSizeX *=2;
            }
            int sampleSizeY = 1;
            while(sampleSizeY* expectedHeight < options.outHeight) {
            	sampleSizeY *=2;
            }
           
            int sampleSize = sampleSizeX > sampleSizeY ? sampleSizeY:sampleSizeX;
            AMLogger.logInfo("Original/expected image size: (%dx%d), (%dx%d), sampleSize: %d", 
            		options.outWidth, options.outHeight, expectedWidth, expectedHeight, sampleSize);
            
            options.inSampleSize = sampleSize;
            options.inJustDecodeBounds = false;

            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);
            //rotate image
            if(bitmap != null){
            	int angle = UIUtils.readPictureDegree(imagePath);
            	bitmap = UIUtils.rotaingImageView(angle, bitmap);
            }
            
		} catch (OutOfMemoryError e) {
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			AMUtils.closeStreams(stream);
        }
		return bitmap;
	}
	
}
