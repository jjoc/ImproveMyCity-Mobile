/* Activity_NewIssueB */
package com.mk4droid.IMC_Activities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SlidingDrawer;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.mk4droid.IMC_Services.Security;
import com.mk4droid.IMC_Services.Service_Location;
import com.mk4droid.IMC_Services.Upload_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Utils.GEO;
import com.mk4droid.IMCity_PackDemo.R;

/**
 * Second fragment for submitting an issue: selecting the location of the issue and submit to remote server  
 *
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Fragment_NewIssueB extends Fragment implements OnMarkerDragListener{
	
	static ProgressDialog dialogNewIssue;
	Handler handlerBroadcastNewIssue;
	
	int tlv = Toast.LENGTH_LONG;
	
	double Lat_D,Long_D;
	
	// ---------- WINDOW --------
	Resources resources;
	DisplayMetrics metrics;
	Context ctx;
	
	//----------- GPS -----------
	String Address_STR = "";
	Handler handlerAddresUPD;
	
	
	static Marker mMarker;
	static LatLng pos;
	static Button btSubmit;
	static EditText etAddress;
	Polygon poly = null;
	//------------VARs ------------
	
	String UserNameSTR,PasswordSTR,UserID_STR;
	SharedPreferences mshPrefs;
	int IndexCatSpinner;
	
	
	
	static Fragment mfrag_nIssueB;
	
	
    View vfrag_nIssueB;
	GoogleMap gmap;
	SupportMapFragment fmap;
	
	
	
	//============== OnCreate ==================
	/**
	 *  Set content view. 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
		
		
	    
	    pos = new LatLng(Service_Location.locUser.getLatitude(), Service_Location.locUser.getLongitude() );
	    mshPrefs        = PreferenceManager.getDefaultSharedPreferences(getActivity());
	    
	    IndexCatSpinner = getArguments() != null ? getArguments().getInt("IndexSpinner") : -1; // Serial Index of the issue
	    resources       = SetResources();
	    
	    
	    
	}
			

	
	/**
	 *    on Create View of this fragment
	 */
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);		
	
		FActivity_TabHost.isFStack1 = false;
		Log.e("Fragment_NewIssue_B","onCreateView");
		
		if (mfrag_nIssueB == null){
		   mfrag_nIssueB = this;
		} else if(mfrag_nIssueB.getId()==0){
           Log.e("The B fragment has zero id","0");
		}
		
		if(mfrag_nIssueB.getId()!=0)
			Log.e("The B fragment has id", " " + mfrag_nIssueB.getId());
		
		ctx = this.getActivity();
		
        // EXECUTED ON ROTATION ONLY
		if (vfrag_nIssueB != null) {
			ViewGroup parent = (ViewGroup) vfrag_nIssueB.getParent();
			if (parent != null)
				parent.removeView(vfrag_nIssueB);
		}

		
		if (vfrag_nIssueB==null)
			try {
				vfrag_nIssueB = inflater.inflate(R.layout.fragment_newissue_b, container, false);
			} catch (InflateException e) {
				Log.e("NIB","Can not inflate");
			}
		

		
		
		Log.e("X"," " + mfrag_nIssueB);
		
		if (vfrag_nIssueB!=null){
			Log.e("A", " " + vfrag_nIssueB);
			Log.e("B", " " + vfrag_nIssueB.getId());
		}
		
		
		if (vfrag_nIssueB==null){
			Log.e("B"," " + mfrag_nIssueB.getId());
			Log.e("D"," " + fmap);
			Log.e("G"," " + gmap);
			return super.onCreateView(inflater, container, savedInstanceState);
		}
		
		
		resources = SetResources();
		
        btSubmit     = (Button) vfrag_nIssueB.findViewById(R.id.btReport_new_issue);
        
	    //====================================================
	    // --------- GPS: Find current Location -----------
	    //==================================================
	    
        Lat_D = Service_Location.locUser.getLatitude();
        Long_D = Service_Location.locUser.getLongitude();
        
	    //----- Handler for setting address string ------------
        handlerAddresUPD = new Handler()
        {
           public void handleMessage(Message msg)
           {
              if (msg.arg1 == 1) // Redraw markers
              {
           	    etAddress = (EditText) vfrag_nIssueB.findViewById(R.id.etAddress);
           		
        		Lat_D  = mMarker.getPosition().latitude;
        		Long_D = mMarker.getPosition().longitude;
           		
        		LatLng pt = new LatLng(Lat_D, Long_D); 
           		
           		Address_STR = GEO.ConvertGeoPointToAddress(pt, ctx);
           		
           		if (Address_STR!=""){
//        			Toast myT = Toast.makeText(ctx, Address_STR, tlv);
//        			myT.setGravity(Gravity.CENTER, 0, 0);
//        			myT.show();
        			etAddress.setText(Address_STR);
        		}else{ 
        			Toast myT = Toast.makeText(ctx, resources.getString(R.string.PleaseAddress), tlv);
        			myT.setGravity(Gravity.CENTER, 0, 0);
        			myT.show();
        			etAddress.setText("");
        		}
           		
	          }
              super.handleMessage(msg);
           }
        };
		
	
	    
        Message msg = new Message();
        msg.arg1 = 1;
		handlerAddresUPD.sendMessage(msg);
	        

		//---------- Map ------
		//------------ Create Items on Maps -----------------------
		// Do a null check to confirm that we have not already instantiated the map.
		if (gmap == null) {
			// Try to obtain the map from the SupportMapFragment.
			fmap =  ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.gmap_newissue));

			gmap = fmap.getMap();

			// Check if we were successful in obtaining the map.
			if (gmap != null) {
				gmap.setMyLocationEnabled(true);
				setUpMap(); // Sets polygon
			}
		} else {
			gmap.setMyLocationEnabled(true);
			Log.e("FActivity_Main","gmap is not null");
		}
        
	         
		//-------- Broadcast new Issue was send through a handler ---------
        handlerBroadcastNewIssue = new Handler()
        {
           public void handleMessage(Message msg)
           {
              if (msg.arg1 == 1) // Refresh Button
            	  ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("NewIssueAdded", "ok"));  
              
              super.handleMessage(msg);
           }
        };
		
        
        //--------------- Versionify
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.FROYO){
        	Button handleSL = (Button) vfrag_nIssueB.findViewById(R.id.handle_Address);
        	handleSL.setBackgroundDrawable(
        			getResources().getDrawable(R.drawable.shape_convex_right_andr2));
        }
        
        
        SlidingDrawer sdAddress = (SlidingDrawer) vfrag_nIssueB.findViewById(R.id.slidingDrawer_Address);
        sdAddress.open();
        
        //--------- Button submit -----------
        btSubmit = (Button) vfrag_nIssueB.findViewById(R.id.btReport_new_issue);
		
        btSubmit.setOnClickListener(new OnClickListener() {

        	@Override
        	public void onClick(View v) {

        		String titleData_STR       = Fragment_NewIssueA.et_title.getText().toString();  // 1. Title Data
        		String descriptionData_STR = Fragment_NewIssueA.et_descr.getText().toString();  // 3. Description Data
        		Address_STR                = etAddress.getText().toString();

        		String ImageFN_target = "";

        		// Check if Image is taken else do not sent  
        		if (Fragment_NewIssueA.flagPictureTaken){
        			String FileNameExt = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss'.jpg'").format(new Date());

        			FileNameExt  =  FileNameExt.replace("-", "_");
        			FileNameExt  =  FileNameExt.replace(" ", "t");
        			ImageFN_target = UserID_STR + "d" + FileNameExt;
        		}


        		double Lat_D  = mMarker.getPosition().latitude;
        		double Long_D = mMarker.getPosition().longitude;

        		// Check if title is long enough and sent
        		if ( Fragment_NewIssueA.et_title.getText().toString().length() > 2){

        			if (GEO.insidePoly(poly, Long_D, Lat_D)){
        			    
        				Log.e("INSIDE", "OK");
        				
        				
        				Message msgButton = new Message();
        				msgButton.arg1 = 1;
        				

        				Toast.makeText(ctx, resources.getString(R.string.Sending),Toast.LENGTH_LONG).show();

        				getActivity().runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								btSubmit.setEnabled(false);
								btSubmit.setBackgroundColor(Color.argb(255, 100, 100, 100));
							}
						});
    					
    					

        				
        				boolean successIadd = false ;

        				successIadd = Upload_Data.SendIssue(Fragment_NewIssueA.image_path_source_temp, ImageFN_target, 
        						titleData_STR, Fragment_NewIssueA.SpinnerArrID[IndexCatSpinner],
        						Lat_D, Long_D, descriptionData_STR, Address_STR, UserNameSTR, PasswordSTR );

        				if (successIadd){
        					btSubmit.setText(resources.getString(R.string.IssueReported));
        					btSubmit.setEnabled(false);

        					getActivity().getSupportFragmentManager().beginTransaction().remove(mfrag_nIssueB).commit();
        					
        					        					
        					FActivity_TabHost.mTabHost.setCurrentTab(0);
        					
        					FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.remove(mfrag_nIssueB);
                             
        					
        					ft.commit();
        					getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        					
        					
        					
        					
        					//------- Reset GUI --------
        					Fragment_NewIssueA.flagPictureTaken = false;
        					Fragment_NewIssueA.btAttachImage.setCompoundDrawablesWithIntrinsicBounds(
        							getResources().getDrawable(android.R.drawable.ic_menu_gallery), null, null,  null);
       

        					Fragment_NewIssueA.et_title.setText("");
        					Fragment_NewIssueA.et_descr.setText("");

        					Fragment_NewIssueA.btProceed.setBackgroundDrawable(resources.getDrawable(R.drawable.gradient_green));
        					Fragment_NewIssueA.btProceed.setCompoundDrawables(resources.getDrawable(R.drawable.tick), null, null, null);
        					        					
        					Toast.makeText(ctx, resources.getString(R.string.Reported), tlv).show();



        					//---- Show dialog Refresh for new Issue ----------
        					dialogNewIssue = ProgressDialog.show(ctx, 
        							resources.getString(R.string.Downloading),
        							resources.getString(R.string.NewIssueCh), true);

        					// ------- Send Broadcast through a handler -------
        					Message msg = new Message();
        					msg.arg1 = 1;
        					handlerBroadcastNewIssue.sendMessage(msg);

        				} else {
        					Dialog md = new Dialog(ctx);
        					md.setTitle("Failed to upload issue!");
        					md.show();
        				}
        			} else {
        				Toast.makeText(ctx,	resources.getString(R.string.Issueoutofmunicipalitylimits),	tlv).show();
        				v.setBackgroundDrawable(resources.getDrawable(R.drawable.gradient_green));
        			}
        		}else{
        			Toast.makeText(ctx, resources.getString(R.string.LongerTitle), tlv).show();
        			v.setBackgroundDrawable(resources.getDrawable(R.drawable.gradient_green));
        		}


        	}});

        
        if (Fragment_NewIssueA.mfrag_nIssueB!=null){
			Log.e("Fragment_NewIssueA.mfrag_nIssueB.getId()", " " + Fragment_NewIssueA.mfrag_nIssueB.getId());						
		}
		
		
		if (Fragment_NewIssueB.mfrag_nIssueB!=null){
			Log.e("Fragment_NewIssueB.mfrag_nIssueB.getId()", " " + Fragment_NewIssueB.mfrag_nIssueB.getId());						
		}
        
		Fragment_NewIssueA.btProceedResetButtonColors();
        return vfrag_nIssueB;
	}
	
	//===================================================
	/**
	 *    SetUp Map
	 */
	private void setUpMap() {

		Log.e("FActivity_Main","setUpMap");

		gmap.clear();
		//------- Create Polygon ----------
		if (poly==null)
			poly = GEO.MakeBorders(gmap, getResources());
		
		// Pan to see all markers in view.
		// Cannot zoom to bounds until the map has a size.

		gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos,14));
						
		mMarker = gmap.addMarker(new MarkerOptions()
						.position(pos)
						.title("Issue position")
						.snippet("Drag 'n drop")
						.draggable(true));
		
		
		gmap.setOnMarkerDragListener(this);
		

	}
	

	//============== On Resume =====================
	/**
	 * Executed after activity is created or after changing tab
	 */
	@Override
	public void onResume() {
		super.onResume();

		resources = SetResources();
	 
        
		
		//----------- Flurry Analytics --------
		boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);

		if (AnalyticsSW)
			FlurryAgent.onStartSession(ctx, Constants_API.Flurry_Key);
	}


	//================= onPause =========================
	/**
	 *    Hinter map visibility and stop Flurry analytics
	 */
	@Override
	public void onPause() {
		super.onPause();
		
		Log.e("NewIssueB", "onPause");

		btSubmit.setText(resources.getString(R.string.ReportIss));
		btSubmit.setEnabled(true);
		
		gmap.setMyLocationEnabled(false);
		
		//----------- Flurry Analytics --------
		boolean AnalyticsSW = mshPrefs.getBoolean("AnalyticsSW", true);

		if (AnalyticsSW)
			FlurryAgent.onEndSession(ctx);
	}  
		

	//============   Set Resources =========================== 
    /** Retrieve preferences and set resources language */ 
	public Resources SetResources(){

		String LangSTR          = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);
		UserID_STR              = mshPrefs.getString("UserID_STR", "");

		UserNameSTR      = mshPrefs.getString("UserNameAR", "");
		PasswordSTR      = mshPrefs.getString("PasswordAR", "");

		Configuration conf = getResources().getConfiguration();
		conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
		metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return new Resources(getActivity().getAssets(), metrics, conf);
	}


	/* (non-Javadoc)
	 * @see com.google.android.gms.maps.GoogleMap.OnMarkerDragListener#onMarkerDragEnd(com.google.android.gms.maps.model.Marker)
	 */
	@Override
	public void onMarkerDragEnd(Marker arg0) {
		
		

		Message msg = new Message();
		msg.arg1 = 1;
		handlerAddresUPD.sendMessage(msg);
	}


	@Override
	public void onMarkerDrag(Marker arg0) {}

	@Override
	public void onMarkerDragStart(Marker arg0) {}
}
