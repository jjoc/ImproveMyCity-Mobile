/**
 *       Activity_SoloMap
 */

package com.mk4droid.IMC_Activities;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.mk4droid.IMC_Constructors.Issue;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMCity_PackDemo.R;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 *        Show a single issue on the map 
 *     
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class Activity_SoloMap extends MapActivity {
    
	Context ctx;
	DisplayMetrics metrics;
	String LangSTR = "en";
	Resources resources;
    Intent mInt;
    Button btMapTypes;
    
    //----- Maps -------------------------------
	MyMapsOver itemizedoverlay_vens; // icons
    MyLocationOverlay myLocationOverlay;   // current location
	MapView mapView;
	List<Overlay> mapOverlays;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    resources  = SetResourc();
	    setContentView(R.layout.activity_solomap);
	    ctx = this;

	    //-------------------- Get input --------------
	    Bundle mBundle = getIntent().getExtras();
	    Issue mIssue  = (Issue) mBundle.getSerializable("mIssue");
	
    	//------------ Create Items on Maps -----------------------
	    mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(false);
        
	    mapOverlays        = mapView.getOverlays();
	    myLocationOverlay = new MyLocationOverlay(this, mapView);
	    
	    myLocationOverlay.enableMyLocation();
	    
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
	   
	    GeoPoint point = new GeoPoint((int) (mIssue._latitude*1E6),	(int) (mIssue._longitude*1E6));;
	    
		// -----  add 
		itemizedoverlay_vens = new MyMapsOver(new BitmapDrawable(bmCateg));
		
		OverlayItem overlayitem = new OverlayItem(point, "","");
		
		itemizedoverlay_vens.addOverlay(overlayitem);
		mapOverlays.add(itemizedoverlay_vens);
		mapView.invalidate();
				
	    //-------------------- Zooom ------------------------
	    MapController mapController = mapView.getController();
	    mapController.animateTo(point);

	    //---------- Button map -----------
	    btMapTypes = (Button) findViewById(R.id.btMapTypes);
	    btMapTypes.setOnClickListener(new OnClickListener(){
	    	@Override
	    	public void onClick(View arg0) {

	    		String bt_Tag = btMapTypes.getTag().toString();

	    		if (bt_Tag.equals("Normal")){
	    			mapView.setSatellite(true);
	    			btMapTypes.setText(resources.getString(R.string.Satellite));
	    			btMapTypes.setTag("Satellite");
	    		} else if (bt_Tag.equals("Satellite")){
	    			mapView.setSatellite(false);
	    			btMapTypes.setText(resources.getString(R.string.NormalMap));
	    			btMapTypes.setTag("Normal");
	    		}

	    		mapView.invalidate();
	    	}});
	}
	
//	@Override
//	protected void onResume() {
//		mapView.setVisibility(View.VISIBLE);
//		super.onResume();
//	}
	
	
	@Override
	protected void onPause() {
		mapView.setVisibility(View.INVISIBLE);
        myLocationOverlay.disableMyLocation();
		
		finish();
		super.onPause();
	}
	
	//=================== Map Overlay =====================
    /**
     * 
     *  Create an overlay to put the marker of the issue
     *
     */
    public class MyMapsOver extends ItemizedOverlay<OverlayItem>  {

    	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
        
    	public MyMapsOver(Drawable defaultMarker) {
    		super(boundCenterBottom(defaultMarker));
    	}
    	
    	public void addOverlay(OverlayItem overlay) {
    	    mOverlays.add(overlay);
    	    populate();
    	}
    	
    	@Override
    	protected OverlayItem createItem(int i) {
    		return mOverlays.get(i);
    	}

    	@Override
    	public int size() {
    		return mOverlays.size();
    	}
   	
    	//------- on Tap -------------------
    	@Override
    	protected boolean onTap(int index)  {
    		finish();
    	  return true;
    	}
    }
    
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
    
	//=======  Set resources ===========
	/**
	 * Set resources language from information retrieved from preferences
	 * 
	 * @return 
	 */
    public Resources SetResourc(){
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		LangSTR                    = mshPrefs.getString("Language", Constants_API.DefaultLanguage).substring(0, 2);
		    	
        Configuration conf = getResources().getConfiguration();
        conf.locale = new Locale(LangSTR);
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return new Resources(getAssets(), metrics, conf);
    }
}
