
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
 *   Created by eyang on 6/16/15.
 *   Copyright (c) 2015 Accela. All rights reserved.
 *   -----------------------------------------------------------------------------------------------------
 *   
 */
package com.accela.contractor.activity.test;



import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.accela.contractor.AppConstants;
import com.accela.contractor.R;
import com.accela.contractor.fragment.ProjectInfoFragment;
import com.accela.contractor.fragment.ProjectOverviewFragment;
import com.accela.contractor.fragment.ProjectPermitFragment;
import com.accela.contractor.model.ProjectModel;
import com.accela.contractor.service.AppInstance;
import com.accela.contractor.service.ProjectsLoader;
import com.accela.contractor.utils.Utils;
import com.accela.mobile.AMLogger;
import com.accela.record.model.RecordInspectionModel;
import com.accela.record.model.RecordModel;
import com.flurry.android.FlurryAgent;


@SuppressWarnings("deprecation")
public class ProjectDetailsActivityTest extends BaseActivityTest {

	
	
}