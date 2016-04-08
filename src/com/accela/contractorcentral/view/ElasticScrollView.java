package com.accela.contractorcentral.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ScrollView;

public class ElasticScrollView extends ScrollView {
	


	private ScrollCallbacks mCallbacks;

	private int maxYOverscrollDistance = 0;
	private int maxXOverscrollDistance = 0;

	public ElasticScrollView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
	    super.onScrollChanged(l, t, oldl, oldt);
	    if (mCallbacks != null) {
	        mCallbacks.onScrollChanged(l, t, oldl, oldt);
	    }
	}
	


	@Override
	public int computeVerticalScrollRange() {
	    return super.computeVerticalScrollRange();
	}

	public void setMaxOverScrollDistance(int xMaxDistance, int yMaxDistance) {
		final DisplayMetrics metrics = getContext().getResources()
	            .getDisplayMetrics();
	    final float density = metrics.density;
		maxYOverscrollDistance = (int) (density*yMaxDistance);
		maxXOverscrollDistance = (int) (density*xMaxDistance);
		
	}
	
	public void setCallbacks(ScrollCallbacks listener) {
	    mCallbacks = listener;
	}

	
	public static interface ScrollCallbacks {
	    public void onScrollChanged(int l, int t, int oldl, int oldt);
	}


	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
	        int scrollY, int scrollRangeX, int scrollRangeY,
	        int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
	    return super.overScrollBy(deltaX, deltaY, scrollX, scrollY,
	            scrollRangeX, scrollRangeY, maxXOverscrollDistance,
	            maxYOverscrollDistance, isTouchEvent);
		}
}
