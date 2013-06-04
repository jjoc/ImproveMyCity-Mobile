/** Service_Location */
package com.mk4droid.IMC_Services;

import java.util.Iterator;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.mk4droid.IMC_Activities.FActivity_TabHost;
import com.mk4droid.IMCity_PackDemo.R;

/**
 *  Find current location and broadcast if location has changed
 *
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Service_Location extends Service {


    /** User location */
	public static Location locUser    = new Location("point User");

	
	//public static ProgressDialog dialogLocationCh; // When location has changed then download new data (INACTIVE)
	Handler handlerBroadcastLocationCh;
	
	int tlv  = Toast.LENGTH_LONG;
	
	//----------- Location Manager for GPS and WIFI -------------
	LocationManager lm;
	LocationListener locationListenerGPS,locationListenerNetwork;
	GpsStatus.Listener mGpsStatusListener = null;
	
	static String CurrAddressSTR = "";
	String CurrSatsFound  = "";
	
	private final IBinder binder=new LocalBinder();
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return 1; 
	}
		
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
	    return false;
	}
	
	
	public class LocalBinder extends Binder {
		public Service_Location getService() {
			return Service_Location.this;
		}
	}
	
 
	//============= onCreate ===================
	/**
	 *   Set GPS, Wifi, and GPSStatus listeners
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		
		//-------------- LOCATION GPS and WIFI ------------------------
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                
        // Get Last known location
        Criteria crit = new Criteria();
        crit.setAccuracy(Criteria.ACCURACY_COARSE);
        String provider = lm.getBestProvider(crit, true);
        
        if (provider==null){
        	Toast.makeText(FActivity_TabHost.ctx, getResources().getString(R.string.NoGPSnoWiFi), tlv).show();
        	
        	//startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); // Bring GPS settings
        } else{
        	Location loc = lm.getLastKnownLocation(provider);
        	
        	//Log.e("LOC", " " + loc.getLatitude() + " "       + loc.getLongitude());
        	
        	if (loc!=null){
            	locUser.setLongitude(loc.getLongitude()); 
        		locUser.setLatitude(loc.getLatitude()); 
        		locUser.setAltitude(loc.getAltitude());
        		
//        		Log.e("LOC_A", " " + locUser.getLatitude() + " " 
//        	            + locUser.getLongitude());
        		
        	} else {
        		Toast.makeText(FActivity_TabHost.ctx, getResources().getString(R.string.Icannotfindyourpos), tlv).show();
        	}
        }
           
     	//---------------    Location Listener WIFI ------------ 
    	locationListenerNetwork = new LocationListener() {
    		public void onLocationChanged(Location location) {
    			if (location.getAccuracy()<50){
    			    DecideDataUpdateDueLoc(locUser.distanceTo(location), location);
    			}
    		}
    		public void onProviderDisabled(String provider) {}
    		public void onProviderEnabled(String provider) {}
    		public void onStatusChanged(String provider, int status, Bundle extras) {}
    	};

        
        //---------------- Location Listener GPS ---------------- 
    	locationListenerGPS = new LocationListener() {
    		@Override
    		public void onLocationChanged(Location location) {
    			if (location.getAccuracy()<50)
    				DecideDataUpdateDueLoc(locUser.distanceTo(location), location);
    		}
    		
    		@Override
    		public void onProviderDisabled(String provider) {}
    		@Override
    		public void onProviderEnabled(String provider) {}
    		@Override
    		public void onStatusChanged(String provider, int status, Bundle extras) {}
    	};

    	//----------- Register GPS and WIFI location listeners ---------
    	lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10f, locationListenerNetwork);
    	lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f, locationListenerGPS);
		
		mGpsStatusListener = new GpsStatusListener();
		lm.addGpsStatusListener(mGpsStatusListener);
		
		//-------- Broadcast new Issue was send through a handler ---------
        handlerBroadcastLocationCh = new Handler()
        {
           public void handleMessage(Message msg)
           {
              if (msg.arg1 == 1) // Refresh Button
            	  sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("LocChanged",   "LocChanged"));  
              
              super.handleMessage(msg);
           }
        };
		
	}
	
	
	
    //=========== onDestroy ===================
	/** Remove all location listeners */
    public void onDestroy() {
    	super.onDestroy();
      	lm.removeUpdates(locationListenerNetwork);       // Remove Wifi location listener
		lm.removeUpdates(locationListenerGPS);           // Remove GPS location listener
	    lm.removeGpsStatusListener(mGpsStatusListener);  // Remove GPS status listener
		stopSelf();
    };
	
	// ==========    GpsStatusListener ==============================
    /**
     *    Find status of GPS signal received: Satellites ok. 
     */
	public class GpsStatusListener implements GpsStatus.Listener {
		
		@Override
		public void onGpsStatusChanged(int event) {
			StringBuilder sbSats = new StringBuilder(512);
			String prnsSTR;
			int prn;
			//int NSatsOk = 0; 
		
			sbSats.delete(0, sbSats.length());

			if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
				CurrSatsFound = "";

				GpsStatus status = lm.getGpsStatus(null);
				Iterable<GpsSatellite> sats = status.getSatellites();

				@SuppressWarnings("rawtypes")
				Iterator it = sats.iterator() ;
				while ( it.hasNext() ){
					GpsSatellite objSat = (GpsSatellite) it.next() ;

					prn =objSat.getPrn();
					prnsSTR =  Integer.toString(prn);

					if (objSat.usedInFix() && prn>0){
						sbSats.append(prnsSTR + "▲,");
						//NSatsOk += 1;
					}else
						sbSats.append(prnsSTR+"▼,");
				}

				CurrSatsFound  = sbSats.toString();
				
				Intent i = new Intent("android.intent.action.MAIN").putExtra("CurrSatsFound", CurrSatsFound);
	            sendBroadcast(i);
				
				
			} // GPS_EVENT_SATELLITE_STATUS
		} // On Status Changed
	}

    //=========== DecideDataUpdateDueLoc ===================
	/**
	 *  Broadcast current address and trigger to update data due to significant location change.
	 * 
	 * @param distanceDiff threshold of distance. If distance differs at least this value then data should be updated.
	 * @param NewLocation The new location fix  
	 */
	public void DecideDataUpdateDueLoc(float distanceDiff, Location NewLocation){

		
		//--------- Broadcast User address has changed --------------
		if (Service_Data.HasInternet){
			//GeoPoint pt = new GeoPoint((int) (locUser.getLatitude()*1E6), (int) (locUser.getLongitude()*1E6));  // RR
	        //CurrAddressSTR = GEO.ConvertGeoPointToAddress(pt,getApplicationContext()) ;  
           sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("CurrAddressSTR", CurrAddressSTR));
		}
		
		
//		String diffSTR = new Float(distanceDiff).toString();
//		Log.e("distanceDiff",  diffSTR);
//		Toast.makeText(Activity_TabHost.ctx,  diffSTR,  Toast.LENGTH_LONG).show();
		
		//----------- Update data if diff > 200 -----------
		
//		if (distanceDiff > 500 && false){
//
//			//---- Show dialog Refresh ----------
//			dialogLocationCh = ProgressDialog.show(Activity_Main.ctx, 
//					Activity_Main.resources.getString(R.string.Downloading),
//					Activity_Main.resources.getString(R.string.LocationCh), true);
//
//			// ------- Broadcast Refresh through a handle
//			Message msg = new Message();
//			msg.arg1 = 1;
//			handlerBroadcastLocationCh.sendMessage(msg);
//		}
		
		//--- Update userLocation				
	  	locUser.setLongitude(NewLocation.getLongitude());
		locUser.setLatitude(NewLocation.getLatitude());
		locUser.setAltitude(NewLocation.getAltitude());
	}
}