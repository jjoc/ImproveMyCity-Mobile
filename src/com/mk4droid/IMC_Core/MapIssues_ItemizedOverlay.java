/** MyMapsItemizedOverlay */
package com.mk4droid.IMC_Core;

import java.util.ArrayList;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.mk4droid.IMC_Activities.Activity_Issue_Details;
import com.mk4droid.IMC_Activities.Activity_TabHost;
import com.mk4droid.IMC_Services.Service_Data;

/**
 *  Create issues overlay. Each overlay has issues of the same category (the same icon).
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class MapIssues_ItemizedOverlay extends ItemizedOverlay<OverlayItem>  {
	
	private ArrayList<OverlayItem> mOverlayItemsList = new ArrayList<OverlayItem>();
	Context ctx;
    
	/** Set default style for the icon, i.e. in the middle */
	public MapIssues_ItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}
	
	/** Add overlay to the list of overlays */
	public void addOverlay(OverlayItem overlay) {
		mOverlayItemsList.add(overlay);
	    populate();
	}
	
	/** return item */
	@Override
	protected OverlayItem createItem(int i) {
		return mOverlayItemsList.get(i);
	}

	/** Number of issues in the overlay */
	@Override
	public int size() {
		return mOverlayItemsList.size();
	}

	//-------------------- on Tap -------------------
	@Override
	protected boolean onTap(int index)  {
		
	  OverlayItem item = mOverlayItemsList.get(index);
	  String Snippet = item.getSnippet();
	  // find id
	  int Snippet_id = Integer.parseInt(Snippet.substring(2, Snippet.length() ));
	  
	  // ------------- when an Issue is tapped ----------- 
	  if (Snippet.substring(0, 1).equals("I")) {
		  
		  // find serial id
		  int iSerial = 0;
		  boolean flagfound = false; 
		  
		  while(!flagfound){
			  if (Service_Data.mIssueL.get(iSerial)._id == Snippet_id)
				  flagfound = true;
			  else 
				  iSerial += 1;
		  }
		  
		  Intent mInt = new Intent(Activity_TabHost.ctx, Activity_Issue_Details.class);
		  mInt.putExtra("Issue", Service_Data.mIssueL.get(iSerial));

		  Activity_TabHost.ctx.startActivity(mInt);
	      
	  } 
	  return true;
	}
}
