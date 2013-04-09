/** GEO */
package com.mk4droid.IMC_Utils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.maps.GeoPoint;
import com.mk4droid.IMC_Store.Constants_API;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

/**
 * Reverse geocoding: (Latitude, Longitude) -> X MyStreet, MyCountry
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */

public class GEO {
	
	//===========  ConvertGeoPointToAddress ============================
    /**
     * reverse geocoding (latitude,longitude)  -> 23 MyStreet, MyCountry
     *  
     * @param pt    Longitude and latitude information
     * @param ctx   current activity context
     * @return
     */
	public static String ConvertGeoPointToAddress(GeoPoint pt, Context ctx){

	    double Lat_d = ((double) pt.getLatitudeE6())/1E6;
		double Long_d = ((double) pt.getLongitudeE6())/1E6;
		
		//Log.e("Lat Long", Double.toString(Lat_d) + " " + Double.toString(Long_d));
		
		Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
		Address maddress = null;
		try {
			List<Address> list = geocoder.getFromLocation(Lat_d, Long_d, 1);
			if (list != null && list.size() > 0) {
				maddress = list.get(0);
			}
		} catch (IOException e) {
             Log.e(Constants_API.TAG, "GEO: ConvertGeoPointToAddress: Reverse Geocoding failed" + e.getMessage()); 
		}
			
		String Address_STR = "";
		
		if (maddress!=null){
			for (int i=0; i< maddress.getMaxAddressLineIndex(); i++)
				Address_STR += maddress.getAddressLine(i) + ", ";

			Address_STR += maddress.getCountryName();
		}

		return Address_STR; 
	}
}

