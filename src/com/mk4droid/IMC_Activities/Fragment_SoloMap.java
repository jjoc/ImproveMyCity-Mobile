//     Activity_SoloMap 
package com.mk4droid.IMC_Activities;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;

import java.util.Locale;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.mk4droid.IMC_Constructors.Issue;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Utils.GEO;
import com.mk4droid.IMCity_PackDemo.R;

/**
 * 
 * Fragment showing a single issue on the map
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Fragment_SoloMap extends Fragment {
    
	/**
	 *   Shared preferences for storing/retrieving settings 
	 */
	public SharedPreferences mshPrefs;
	
	//----- Maps -------------------------------
    /** The view of this fragment */
    public  static View vfrag_solo_map;
    
    /** The GoogleMap object for handling the map */
    public  static GoogleMap gmapSolo;
	Marker mMarker;
	
	/** This fragment */
	public static Fragment mfrag_solomap;
	
	/** Polygon of Municipality borders*/
	public static Polygon mPoly;
	
	
	//------------------------------
	DisplayMetrics metrics;
	String LangSTR = "en";
	Resources resources;
    Button btMapTypes;
    Issue mIssue;
    
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    Log.e("Fragment_Solo","onCreate()");
	    
		mshPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		resources = SetResourc();

		int issueId = getArguments().getInt("issueId"); // Serial Index of the issue
				
		for (int i = 0; i<Service_Data.mIssueL.size(); i++)
			if (issueId == Service_Data.mIssueL.get(i)._id)
				mIssue = Service_Data.mIssueL.get(i);
		
		
	}
	
	@Override
	public void onDestroyView() {
		gmapSolo.setMyLocationEnabled(false);
		super.onDestroyView();
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    
		
		FActivity_TabHost.isFStack1 = false;
		Log.e("Fragment_Solo","onCreateView()");
		
	    if (vfrag_solo_map != null) {
	        ViewGroup parent = (ViewGroup) vfrag_solo_map.getParent();
	        if (parent != null)
	            parent.removeView(vfrag_solo_map);
	    }
	    try {
	    	vfrag_solo_map = inflater.inflate(R.layout.fragment_solomap, container, false);
	    } catch (InflateException e) {
	        /* map is already there, just return view as it is */
	    }
	    
	    mfrag_solomap = this;
	
		//--------- Setup map --------------------
		//------------ Create Items on Maps -----------------------
		// Do a null check to confirm that we have not already instantiated the map.
		if (gmapSolo == null) {
			// Try to obtain the map from the SupportMapFragment.
			gmapSolo = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.gmap_solo)).getMap();

			// Check if we were successful in obtaining the map.
			if (gmapSolo != null) {
				//------- Create Polygon ----------
				if (mPoly==null)
					mPoly = GEO.MakeBorders(gmapSolo, getResources());
			}
		}
		
		
		if (gmapSolo != null) 
			gmapSolo.setMyLocationEnabled(true);
		
		//-------------- Button Map Types ------
		btMapTypes   = (Button)  vfrag_solo_map.findViewById(R.id.btMapTypes);
		
		btMapTypes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//------------- Other maps views click --------
				String bt_Tag = btMapTypes.getTag().toString();
				if (bt_Tag.equals("Satellite")){
					gmapSolo.setMapType(MAP_TYPE_NORMAL);
					btMapTypes.setText(resources.getString(R.string.NormalMap));
					btMapTypes.setTag("Normal");
				} else if (bt_Tag.equals("Normal")){
					gmapSolo.setMapType(MAP_TYPE_HYBRID);
					btMapTypes.setText(resources.getString(R.string.Satellite));
					btMapTypes.setTag("Satellite");
				}
			}
		});
		
		
    	//------------ Create Items on Maps -----------------------
	    Bitmap bmCateg = null;
	    for (int j=0; j < Service_Data.mCategL.size(); j++){
	    	if ( mIssue._catid ==  Service_Data.mCategL.get(j)._id ){
	    		 byte[] b = Service_Data.mCategL.get(j)._icon;
	    		 bmCateg  =  BitmapFactory.decodeByteArray(b, 0, b.length);
	    		 
	    		 if ( metrics.heightPixels > 1000)
	    			 bmCateg = Bitmap.createScaledBitmap(bmCateg, 90, 104, true);
	    		 else if (metrics.heightPixels>=800 && metrics.heightPixels < 1000)
	    			 bmCateg = Bitmap.createScaledBitmap(bmCateg, 64, 74, true);
	    		 else if (metrics.heightPixels>=240 && metrics.heightPixels < 800)
	    			 bmCateg = Bitmap.createScaledBitmap(bmCateg, 20, 25, true);	
	    	}
	    }
	   
	    
	    
		// -----  add 
	    mMarker = gmapSolo.addMarker(new MarkerOptions()
			.position(new LatLng(mIssue._latitude, mIssue._longitude))
			.title(mIssue._title)
			.snippet("# " + Integer.toString(mIssue._id))
			.icon(BitmapDescriptorFactory.fromBitmap(bmCateg)));
	    
	    
	    //-------------------- Zoom when map is visible------------------------
	    final View mapView = getFragmentManager().findFragmentById(R.id.gmap_solo).getView();

	    if (mapView.getViewTreeObserver().isAlive()) {
	    	mapView.getViewTreeObserver().addOnGlobalLayoutListener(
	    			new OnGlobalLayoutListener() {
	    				public void onGlobalLayout() {
	    				gmapSolo.moveCamera(CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(),16));
	    				}
	    			}
   			);
	    }
	    
		return vfrag_solo_map;
	}
	
	//=======  Set resources ===========
	/**
	 * Set resources language from information retrieved from preferences
	 * 
	 * @return 
	 */
    public Resources SetResourc(){
		LangSTR                    = mshPrefs.getString("Language", Constants_API.DefaultLanguage).substring(0, 2);
		    	
        Configuration conf = getResources().getConfiguration();
        conf.locale = new Locale(LangSTR);
        metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return new Resources(getActivity().getAssets(), metrics, conf);
    }
}