/** GEO */
package com.mk4droid.IMC_Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMCity_PackDemo.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

/**
 * Reverse geocoding: (Latitude, Longitude) -> X MyStreet, MyCountry
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
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
	public static String ConvertGeoPointToAddress(LatLng pt, Context ctx){
		
		//Log.e("Lat Long", Double.toString(Lat_d) + " " + Double.toString(Long_d));
		
		
		Address maddress = null;
		try {
			Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
			List<Address> list = geocoder.getFromLocation(pt.latitude, pt.longitude, 1);
			if (list != null && list.size() > 0) {
				maddress = list.get(0);
			}
		} catch (Exception e) {
             Log.e(Constants_API.TAG, "GEO: ConvertGeoPointToAddress: Reverse Geocoding failed" + e.getMessage());
             return "";
		}
			
		String Address_STR = "";
		
		if (maddress!=null){
			for (int i=0; i< maddress.getMaxAddressLineIndex(); i++)
				Address_STR += maddress.getAddressLine(i) + ", ";

			Address_STR += maddress.getCountryName();
		}

		return Address_STR; 
	}
	
	
	/**
	 * Draw polygon borders on the map defining the municipality
	 * 
	 * @param mgmap
	 * @param resources
	 */
	
	
	public static Polygon MakeBorders (GoogleMap mgmap, Resources res) {  

		String coords = "";

		// parse from raw.polygoncoords.txt
		try {
			
			InputStream in_s = res.openRawResource(R.raw.polygoncoords);
			byte[] b = new byte[in_s.available()];
			in_s.read(b);
			coords =  new String(b) ;
		} catch (Exception e) {
			// e.printStackTrace();
			Log.e("Error","can't show help.");
		}

		
		Polygon mPoly = null;
						
		if (coords.length() > 0){
			String[] points = coords.split(" ");
			PolygonOptions options = new PolygonOptions();

			for (int i=0; i<points.length; i++){
				String[] ll = points[i].split(","); 
				options.add(new LatLng( Double.parseDouble(ll[1]), Double.parseDouble(ll[0])));
			}

			mPoly = mgmap.addPolygon(options
					.strokeWidth(4)
					.strokeColor(Color.BLACK)
					.fillColor(Color.argb(10, 0, 100, 0)));
		}
		
		
		return mPoly;
		
	}
	
	
	/** 
	 *      x is long, y is lat 
	 */
	public static boolean insidePoly(Polygon poly, double lng, double  lat){
		
		List<LatLng> p = poly.getPoints();
		
		int polyPoints = poly.getPoints().size();
		int polySides  = polyPoints - 1;
		
		double[] polyY = new double[polyPoints];
		double[] polyX = new double[polyPoints];
		
		for (int i = 0; i < polyPoints; i++){
			polyY[i] = p.get(i).latitude;
			polyX[i] = p.get(i).longitude;
		}
		
		boolean oddTransitions = false;
        for( int i = 0, j = polySides -1; i < polySides; j = i++ ) {
            if( ( polyY[ i ] < lat && polyY[ j ] >= lat ) || ( polyY[ j ] < lat && polyY[ i ] >= lat ) ) {
                if( polyX[ i ] + ( lat - polyY[ i ] ) / ( polyY[ j ] - polyY[ i ] ) * ( polyX[ j ] - polyX[ i ] ) < lng ) {
                    oddTransitions = !oddTransitions;          
                }
            }
        }
        return oddTransitions;
	}
	

	
	
}

