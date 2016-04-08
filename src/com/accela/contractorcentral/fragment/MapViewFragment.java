package com.accela.contractorcentral.fragment;


import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.accela.contractorcentral.R;
import com.accela.contractorcentral.service.AppInstance;
import com.accela.contractorcentral.service.GeocoderService;
import com.accela.contractorcentral.utils.Utils;
import com.accela.framework.model.AddressModel;
import com.accela.mobile.AMLogger;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;



public class MapViewFragment extends Fragment implements 
OnMarkerClickListener, OnInfoWindowClickListener {

	private MapView mapView;
	private GoogleMap map;
	private AddressModel addressModel;
	OnMakerClickListener makerClickListener;
	private TextView mapErrorInfo;
	private ArrayList<Marker> markerList = new ArrayList<Marker>();

	 public interface  OnMakerClickListener{
	        /**
	         * Callback for when an item has been selected.
	         */
	        public void onRecordSelected(int id);
	 }

	
	Handler handler = new Handler() {
		
	};
	
	
    public MapViewFragment() {
    	
    }
    
    public void setAddress(AddressModel address) {
    	
    	addressModel = address;
    	if(addressModel==null) {
    		return;
    	}
    	String x = address.getXCoordinate();
    	String y = address.getYCoordinate();
    	AMLogger.logInfo("AddressModel: %s, %s", x, y);
    	if(x != null && y!=null) {
    		final float latitude = Float.parseFloat(x);
    		final float longitude = Float.parseFloat(y);
    		new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					addMakerToMap(latitude, longitude);
				}
    			
    		}, 500);
    	} else {
    	//get the geo and set the map maker icon.
	    	GeocoderService.getGeoLocationByAddressAsync(address, new GeocoderService.GeocoderDelegate() {
				
				@Override
				public void onComplete(boolean successful, float latitude, float longitude) {
					if(successful) {
						addressModel.setXCoordinate(Float.toString(latitude));
						addressModel.setYCoordinate(Float.toString(longitude));
						if(isAdded())
							addMakerToMap(latitude, longitude);
					}
				}
			});
    	}
    }
    
    public void setOnMakerClickListener(OnMakerClickListener l) {
    	makerClickListener = l;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout view = (FrameLayout) inflater.inflate(R.layout.fragment_mapview,container, false);
        mapErrorInfo = (TextView) view.findViewById(R.id.mapInfo);
        mapView = new MapView(getActivity());
        view.addView(mapView);
        mapView.onCreate(savedInstanceState);
        mapView.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			StringBuffer sb = new StringBuffer();
			sb.append(Utils.getAddressLine1AndUnit(addressModel));
			sb.append(" ").append(Utils.getAddressLine1(addressModel));
			sb.append(" ").append(Utils.getAddressLine2(addressModel));
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+ sb.toString()));
			getActivity().startActivity(intent);
		}});
        setupMap();
        Location location = AppInstance.getProjectsLoader().getCurrentLocation();
        if(location != null) {
        	moveCameraTo((float) location.getLatitude(), (float) location.getLongitude());
        } else {
        	moveCameraTo(37.78f, -121.97f);
        }
        return view;
    }

    private boolean checkMap() {
    	if(map == null) {
    		map = mapView.getMap();
    		if(map!=null) {
    			AMLogger.logInfo("Get the map instance");
    			mapView.setVisibility(View.VISIBLE);
    			mapErrorInfo.setVisibility(View.GONE);
    			try {
					MapsInitializer.initialize(this.getActivity());
				} catch (GooglePlayServicesNotAvailableException e) {
					// TODO Auto-generated catch block
					AMLogger.logWarn(e.toString());
				}
    		} else {
        		//AMLogger.logError("Can't get the map: %s", Log.getStackTraceString(new Exception()));
    			this.mapErrorInfo.setVisibility(View.VISIBLE);
    			mapView.setVisibility(View.INVISIBLE);
        	}
    	} 
    	return map!=null;
    	
    }
    
    private void setupMap() {
    	if(!checkMap()) {
    		return;
    	}
    	map.getUiSettings().setZoomControlsEnabled(false);
    	map.setOnMarkerClickListener(this);
    	map.setOnInfoWindowClickListener(this);
    }
    
    private void moveCameraTo(float x, float y) {
    	if(!checkMap()) {
    		return;
    	}
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(x, y), 6);
		if(update!=null) {
			map.moveCamera(update);
		}
    }
    
    private void addMakerToMap(float x, float y) {
    	AMLogger.logInfo("addMakerToMap - 1");
    	if(!checkMap()) {
    		return;
    	}
    	AMLogger.logInfo("addMakerToMap - 2");
    	AMLogger.logInfo("maker geo: (%f,  %f)", x , y);
    	map.clear();
    	markerList.clear();
    	final LatLngBounds.Builder builder = new LatLngBounds.Builder();
    	//add location pin
        MarkerOptions options = new MarkerOptions();
		final LatLng latlng = new LatLng(x, y);
		options.position(latlng);
		Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.map_location);
		options.icon(BitmapDescriptorFactory.fromBitmap(bitmap
				));
		Marker marker = map.addMarker(options);
		markerList.add(marker);
		builder.include(latlng); 
		handler.postDelayed(new Runnable() {
 
			@Override
			public void run() {
				//Projection projection = map.getProjection();
				//Point point = projection.toScreenLocation(latlng);
				//CameraUpdate update = CameraUpdateFactory.zoomBy( point);
				
				CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latlng, 15);
				AMLogger.logInfo("moveCamera()");
				map.animateCamera(update, 500, null);
			}
    		
    	}, 1000);
    	
    }
    
    @Override
	public void onInfoWindowClick(Marker marker) {
    	if(makerClickListener!=null) {
    		int index = markerList.indexOf(marker);
    		makerClickListener.onRecordSelected(index);
    	}
		
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		return false;
	}
    
    
	
	public void refresh() {
	//	setupMap();
    //    addMakerToMap();
	}
    
	@Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        checkMap();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
    	mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    
}
