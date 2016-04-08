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
 *   Created by jzhong on 4/15/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */
package com.accela.contractorcentral.view;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.accela.contractorcentral.R;



public class FontTextView extends TextView {
	//the following code is to manage the fonttype
    private AssetManager mgr;
    private static Map<String, Typeface> fonts = new HashMap<String, Typeface>();
   
    private Typeface getFont(String fontName) {
    	mgr = this.getContext().getAssets();
        if (fonts.containsKey(fontName))
            return fonts.get(fontName);
        Typeface font = null;

        try {
            font = Typeface.createFromAsset(mgr, fontName);
            fonts.put(fontName, font);
        } catch (Exception e) {

        }
        return font;
    }

	
	//FontTextView 
    public FontTextView(Context context) {
        this(context, null);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        if (isInEditMode())
            return;

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FontTextView);

        if (ta != null) {
            String fontType = ta.getString(R.styleable.FontTextView_fontType);
            int style = Typeface.NORMAL;
            if (getTypeface() != null)
                style = getTypeface().getStyle();
            if (fontType==null || fontType.length()==0) {
            	//load default font
            	if(style == Typeface.BOLD) {
            		fontType = "DINOT-Bold.otf";
            	} else {
            		fontType = "DINOT-Regular.otf";
            	}
            } 
            Typeface tf = getFont(fontType);
            
            if (tf != null)
                setTypeface(tf, style);
            else
                Log.d("FontTextView", String.format("Could not create a font from asset: %s", fontType));
            
            ta.recycle();
        }
        
    }
    
    
}
