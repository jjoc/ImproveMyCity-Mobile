// Fragment_Main 
package com.mk4droid.IMC_Activities;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;

import java.util.ArrayList;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.mk4droid.IMC_Services.InternetConnCheck;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Services.Service_Location;
import com.mk4droid.IMC_Services.Service_Location.LocalBinder;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Utils.GEO;
import com.mk4droid.IMCity_PackDemo.R;

/**
 *  Main Fragment that contains a map showing all issues 
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 * 
 */
public class Fragment_Main extends Fragment implements OnMarkerClickListener, OnInfoWindowClickListener{ 
		
	static Context ctx;
	
	/** User id number  */
	public String UserID_STR;

	/** User actual name  */
	public String UserRealName;

	/** Handler for updating Markers */
	public Handler handlerMarkersUPD;

	/** Handler for refreshing data */
	public Handler handlerBroadcastRefresh;

	/** Handler for periodic updating */
	public Handler handlerDialogsPeriodicSync;


	SharedPreferences mshPrefs; // Shared preferences for storin/retrieving setting
	
	Polygon mPoly = null;       // polygon of LatLng coordinates from where issues can be sent

	
	View vfragment_main;        // this fragment
	SupportMapFragment fmap;
	GoogleMap gmap;
	
	public static Fragment fmap_main;
	public static Fragment_Issue_Details frag_issue_details;
	

	//============ System variables ======================	
	Resources resources;    // string, drawables etc.
	DisplayMetrics metrics;  // screen size var
	String LangSTR;          // Language

	//------- Switches for filterin issues --------
	boolean ClosedSW = true, OpenSW = true, AckSW  = true, MyIssuesSW ;

	//----- Map -------------------------------
	ArrayList<Marker> mMarkers = new ArrayList<Marker>();
	Marker lastOpenned = null;

	boolean FirstAnimToLoc = true;
	boolean isLocServBound = false;
	double minLat=0,minLong=0,maxLat=0,maxLong=0; // For Zooming correctly

	//----------- GUI ----------
	Button btMaps,btRefresh; 
	int tlv = Toast.LENGTH_LONG;

	//------ Dialogs, Threads, Intents, IntentFilters --------
	ProgressDialog dialogRefresh, dialogPeriodicSync = null;;
	Intent IntDataServ,IntLocServ;
	BroadcastReceiver mReceiverDataChanged;
	IntentFilter intentFilter;

	public Service_Location mService_Location = null;

	private ServiceConnection mLocConnection = new ServiceConnection() {

		@Override 
		public void onServiceConnected(ComponentName className, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mService_Location = binder.getService();

			if(mService_Location != null){
				Log.d("service-bind", "Service is bonded successfully!");
			} else {
				Log.e("service-bind", "null");
			}
			isLocServBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			Log.d("service-bind", "disconnected");
			isLocServBound = false;
		}
	};
	



	/**
	 *   OnCreate Create GUI and Handlers for broadcasting messages
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mshPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		resources = SetResources();
	}
	
	@Override
	public void onDestroyView() {
		ctx.unregisterReceiver(mReceiverDataChanged);
		super.onDestroyView();
	}
	
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onDestroy()
	 */
	@Override
	public void onDestroy() {
	
		if (isLocServBound){
			ctx.unbindService(mLocConnection);
			isLocServBound = false;
		}
		
		super.onDestroy();
	}

	/**
	 * The Fragment's UI 
	 * 
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		FActivity_TabHost.isFStack1 = true;
		
        // ----- Inflate the view ---------
		if (vfragment_main != null) {
			ViewGroup parent = (ViewGroup) vfragment_main.getParent();
			if (parent != null){
				parent.removeView(vfragment_main);
			}
		}

		try {
			if (vfragment_main==null)
				vfragment_main = inflater.inflate(R.layout.fragment_main, container, false);
		} catch (InflateException e) {
			Log.e("Fragment_Main","map is already there, just return view as it is ");
		}
		
		//------------ Create Items on Maps -----------------------
		// Do a null check to confirm that we have not already instantiated the map.
		if (gmap == null) {
			// Try to obtain the map from the SupportMapFragment.
    		fmap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.gmap));
    		
    		
			gmap = fmap.getMap();
			if (gmap != null) {
				gmap.setMyLocationEnabled(true);
				gmap.setInfoWindowAdapter(new InfoWindowAdapterButtoned());
			}
		} else {
			gmap.setMyLocationEnabled(true);
			
		}
        //------------------------------------------------------------		
		

		ctx = vfragment_main.getContext();
		fmap_main = this;

		dialogRefresh = new ProgressDialog(ctx);

		//------- Button Maps Types  ------------
		btMaps   = (Button)  vfragment_main.findViewById(R.id.btMapChange);

		btMaps.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//------------- Other maps views click --------
				String bt_Tag = btMaps.getTag().toString();
				if (bt_Tag.equals("Satellite")){
					gmap.setMapType(MAP_TYPE_NORMAL);
					btMaps.setText(resources.getString(R.string.NormalMap));
					btMaps.setTag("Normal");
				} else if (bt_Tag.equals("Normal")){
					gmap.setMapType(MAP_TYPE_HYBRID);
					btMaps.setText(resources.getString(R.string.Satellite));
					btMaps.setTag("Satellite");
				}
			}
		});



		btRefresh = (Button) vfragment_main.findViewById(R.id.btRefresh);

		btRefresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//---- Show dialog Refresh ----------
				dialogRefresh = ProgressDialog.show(ctx, resources.getString(R.string.Downloading),
						resources.getString(R.string.Refresh), true);

				// ------- Broadcast Refresh through a handler
				Message msg = new Message();
				msg.arg1 = 1;
				handlerBroadcastRefresh.sendMessage(msg);
			}
		});


		//---- Bind Location Service --------------
		IntLocServ = new Intent(ctx, Service_Location.class);

		if (!isLocServBound && FActivity_TabHost.IndexGroup == 0)
			ctx.bindService(IntLocServ, mLocConnection, Context.BIND_AUTO_CREATE);
		

		FActivity_TabHost.IndexGroup = 0;
		//-----Start Data Service
		IntDataServ = new Intent(ctx, Service_Data.class);
		ctx.startService(IntDataServ);



		//----- Handler for Redrawing Markers from update thread ------------
		handlerMarkersUPD = new Handler()
		{
			public void handleMessage(Message msg)
			{
				if (msg.arg1 == 1) // Redraw markers
				{
					setUpMap(); 	
					PutMarkers();

					//-------- dismiss cases 1. RefreshButton 2. DistanceCh  3. IssuesNoCh 4. LocationCh
					if (dialogRefresh.isShowing())
						dialogRefresh.dismiss();

					//-------- dismiss case New Issue -----------
					try{
						if (Fragment_NewIssueB.dialogNewIssue.isShowing())  // RR
							Fragment_NewIssueB.dialogNewIssue.dismiss();
					} catch (Exception e){}; // In case Activity_NewIssueB not instantiated yet

					//-------- dismiss case Location change -----------
					//	    			try{
					//	    				if (Service_Location.dialogLocationCh.isShowing())
					//	    					Service_Location.dialogLocationCh.dismiss();
					//	    			} catch (Exception e){}; // In case Activity_NewIssueB not instantiated yet

				}
				super.handleMessage(msg);
			}
		};



		//----- Handler for Redrawing Markers from update thread ------------
		handlerBroadcastRefresh = new Handler() // Broadcast 1. Refresh Button 2. DistanceCh 3. IssuesNoCh
		{
			public void handleMessage(Message msg)
			{
				if (msg.arg1 == 1) // Refresh Button
					ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("Refresh", "ok"));  
				else if (msg.arg1 == 2)
					ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("DistanceChanged", "Indeed"));
				else if (msg.arg1 == 3)
					ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("IssuesNoChanged", "yep"));

				super.handleMessage(msg);
			}
		};


		//----- Handler for Redrawing Markers from update thread ------------
		handlerDialogsPeriodicSync = new Handler()
		{
			public void handleMessage(Message msg)
			{
				if (msg.arg1 == 1){
					dialogPeriodicSync = ProgressDialog.show(FActivity_TabHost.ctx, 
							resources.getString(R.string.Downloading),
							resources.getString(R.string.SyncData), true);
				}else if (msg.arg1 == 0){
					if (dialogPeriodicSync!=null)
						dialogPeriodicSync.dismiss();
				}
				super.handleMessage(msg);
			}
		};

		//--------------- Receiver for Data change ------------
		intentFilter = new IntentFilter("android.intent.action.MAIN"); // DataCh

		mReceiverDataChanged = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String DataChanged = intent.getStringExtra("DataChanged");

				if (DataChanged!=null){
					Message msg = new Message();
					msg.arg1 = 1;
					handlerMarkersUPD.sendMessage(msg);
				} 
			}
		};

		ctx.registerReceiver(mReceiverDataChanged, intentFilter);

		//-------- Init markers when no internet ---------
		Message msg = new Message();
		msg.arg1 = 1;
		handlerMarkersUPD.sendMessage(msg);

		if(!InternetConnCheck.getInstance(ctx).isOnline(ctx))
			Toast.makeText(ctx, resources.getString(R.string.NoInternet), tlv).show();


		return vfragment_main;
	}// end of CreateView 

	/**
	 *  on Information window click -> go to Issue_Details
	 */
	@Override
	public void onInfoWindowClick(Marker marker) {

		
		
		String markerSnippet = marker.getSnippet();
		// find id
		int Snippet_id = Integer.parseInt(markerSnippet.substring(2));
		
		
		// remove location services
		gmap.setMyLocationEnabled(false);
		if (isLocServBound){
			ctx.unbindService(mLocConnection);
			isLocServBound = false;
		}

		FragmentTransaction ft = getFragmentManager().beginTransaction();

		// new 			        
		frag_issue_details = new Fragment_Issue_Details();
		Bundle args = new Bundle();
		args.putInt("issueId", Snippet_id);
		frag_issue_details.setArguments(args);

		ft.replace(R.id.flmapmain,  frag_issue_details,"MAIN_FTAG_ISSUE_DETAILS");
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.addToBackStack(null);
		ft.commit();
	}


	@Override
	public void onResume() {
		super.onResume();

		fmap.getView().setVisibility(View.VISIBLE);
		
		resources = SetResources(); // to get new UserID

		if (!isLocServBound)
			ctx.bindService(IntLocServ, mLocConnection, Context.BIND_AUTO_CREATE);

		//---------- Maps ------------
		String bt_Tag = btMaps.getTag().toString();

		if (bt_Tag.equals("Normal")){
			gmap.setMapType(MAP_TYPE_NORMAL);
			btMaps.setText(resources.getString(R.string.NormalMap));
			btMaps.setTag("Normal");
		} else if (bt_Tag.equals("Satellite")){
			gmap.setMapType(MAP_TYPE_HYBRID);
			btMaps.setText(resources.getString(R.string.Satellite));
			btMaps.setTag("Satellite");
		}

		btRefresh.setText(resources.getString(R.string.Refresh));
		gmap.setMyLocationEnabled(true);

		//-------------- Check if distance has changed -------------
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		String distanceData    = mshPrefs.getString("distanceData", "5000");
		String distanceDataOLD = mshPrefs.getString("distanceDataOLD", "5000");

		//---------- Check if IssuesNo has changed ---------
		String IssuesNoAR    =  mshPrefs.getString("IssuesNoAR", "40");
		String IssuesNoAROLD =  mshPrefs.getString("IssuesNoAROLD", "40");

		if (InternetConnCheck.getInstance(ctx).isOnline(ctx)){

			if (!distanceData.equals(distanceDataOLD)){

				//---- Show dialog Refresh ----------
				dialogRefresh = ProgressDialog.show(ctx, 
						resources.getString(R.string.Downloading),
						resources.getString(R.string.DistanceView), true);

				// ------- Broadcast Refresh through a handle
				Message msg = new Message();
				msg.arg1 = 2;
				handlerBroadcastRefresh.sendMessage(msg);

			} else if (!IssuesNoAR.equals(IssuesNoAROLD)){

				//---- Show dialog Refresh ----------
				dialogRefresh = ProgressDialog.show(ctx, 
						resources.getString(R.string.Downloading),
						resources.getString(R.string.IssuesNoCh), true);

				// ------- Broadcast Refresh through a handle
				Message msg = new Message();
				msg.arg1 = 3;
				handlerBroadcastRefresh.sendMessage(msg);
			}
			btRefresh.setVisibility(View.VISIBLE);
		} else {
			btRefresh.setVisibility(View.GONE);
		}

		
		
		//----------- Flurry Analytics --------
		if (mshPrefs.getBoolean("AnalyticsSW", true))
			FlurryAgent.onStartSession(ctx,Constants_API.Flurry_Key);
	}



	/**
	 *    SetUp Map
	 */
	private void setUpMap() {
		//------- Create Polygon ----------
		if (mPoly==null)
			mPoly = GEO.MakeBorders(gmap, getResources());
		//-----------------------------
		// Set listeners for marker events.  See the bottom of this class for their behavior.
		gmap.setOnMarkerClickListener(this);
		gmap.setOnInfoWindowClickListener(this);

	}


	//===================================================
	/**
	 *       OnPause 
	 */
	@Override
	public void onPause() {
		gmap.setMyLocationEnabled(false);
		fmap.getView().setVisibility(View.INVISIBLE);
		
		//------ Unbind Services -------
		if (isLocServBound){
			ctx.unbindService(mLocConnection);
			isLocServBound = false;	
		}

		//----------- Flurry Analytics --------
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);

		if (AnalyticsSW)
			FlurryAgent.onEndSession(ctx);

		super.onPause();
	}

	//=========== PutMarkers() =======================
	/**                
	 *      Markers are placed in overlays. Each overlay contains markers that have the same icon image.        
	 */       
	public void PutMarkers(){

		if (mMarkers.size()>0)
			for (int i=0; i< mMarkers.size(); i++)
				mMarkers.get(i).remove();

		if (Service_Data.mIssueL==null)
			return;

		if (Service_Data.mIssueL.size() > 0) {
			minLat =  Service_Data.mIssueL.get(0)._latitude;
			minLong = Service_Data.mIssueL.get(0)._longitude;
			maxLat =  Service_Data.mIssueL.get(0)._latitude;
			maxLong=  Service_Data.mIssueL.get(0)._longitude;
		}

		//------------------ Urban Problems ----------------
		int NIssues = Service_Data.mIssueL.size(); 


		for (int j=0; j< Service_Data.mCategL.size(); j++){
			if (Service_Data.mCategL.get(j)._visible==1){  // Filters for Visibility

				// Every categ has an overlayitem with multiple overlays in it !!! 
				//---------- Drawable icon -------
				byte[] b  =  Service_Data.mCategL.get(j)._icon;

				Bitmap bm = BitmapFactory.decodeByteArray(b, 0, b.length);

				bm = Bitmap.createScaledBitmap(bm, (int) ((float)metrics.densityDpi/4.5), 
						(int) ((float)metrics.densityDpi/4), true);

				for(int i=0; i<NIssues; i++){

					//------ iterate to find category of issue and icon to display
					if ( (Service_Data.mIssueL.get(i)._currentstatus == 1 && OpenSW) || 
							(Service_Data.mIssueL.get(i)._currentstatus == 2 && AckSW) || 
							(Service_Data.mIssueL.get(i)._currentstatus == 3 && ClosedSW) ){

						if (Service_Data.mIssueL.get(i)._catid == Service_Data.mCategL.get(j)._id ){

							if (MyIssuesSW && !Integer.toString(Service_Data.mIssueL.get(i)._userid).equals(UserID_STR))
								continue;

							//--------- upd view limits -------------
							maxLat  = Math.max( Service_Data.mIssueL.get(i)._latitude  , maxLat );
							minLat  = Math.min( Service_Data.mIssueL.get(i)._latitude  , minLat );
							maxLong = Math.max( Service_Data.mIssueL.get(i)._longitude , maxLong);
							minLong = Math.min( Service_Data.mIssueL.get(i)._longitude , minLong);

							mMarkers.add( gmap.addMarker(new MarkerOptions()
							.position(new LatLng(Service_Data.mIssueL.get(i)._latitude, Service_Data.mIssueL.get(i)._longitude))
							.title(Service_Data.mIssueL.get(i)._title)
							.snippet("# " + Integer.toString(Service_Data.mIssueL.get(i)._id))
							.icon(BitmapDescriptorFactory.fromBitmap(bm)))
									);
						} // cat match 
					} // open closed 
				} // i
			} // visible
		} // j


		if (Math.abs((maxLat - minLat)) + Math.abs(maxLong - minLong) !=0){
			LatLngBounds bounds = new LatLngBounds.Builder()
			.include(new LatLng(maxLat,minLong))           // Upper Right 
			.include(new LatLng(minLat,maxLong))           // Lower Left
			.build();

			
			metrics = new DisplayMetrics();
			getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
			
			gmap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, metrics.widthPixels, metrics.heightPixels, 50));

		}else {
			gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Service_Location.locUser.getLatitude(),
					                                                 Service_Location.locUser.getLongitude()),14));
			Toast.makeText(ctx, resources.getString(R.string.DownloadingFirst) , tlv).show();
		}
	}


	
	
	/**
	 *  On marker click show issue title and id in Information Window
	 */
	@Override
	public boolean onMarkerClick(Marker marker) {

		if (lastOpenned != null) {
			// Close the info window
			lastOpenned.hideInfoWindow();

			// Is the marker the same marker that was already open
			if (lastOpenned.equals(marker)) {
				// Nullify the lastOpenned object
				lastOpenned = null;
				// Return so that the info window isn't openned again
				return true;
			} 
		}

		// Open the info window for the marker
		marker.showInfoWindow();
		// Re-assign the last openned such that we can close it later
		lastOpenned = marker;

		CameraUpdate camUpdate = CameraUpdateFactory.newLatLng(marker.getPosition());

		gmap.animateCamera(camUpdate, 10, null); 
		return true;
	}



	//===========     Set Resources  ========================= 
	/**
	 *      Obtain resources from preferences 
	 * @return
	 */
	public Resources SetResources(){

		LangSTR          = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);
		UserID_STR       = mshPrefs.getString("UserID_STR", "");
		UserRealName     = mshPrefs.getString("UserRealName", "");

		OpenSW                = mshPrefs.getBoolean("OpenSW", true);
		AckSW                 = mshPrefs.getBoolean("AckSW", true);
		ClosedSW              = mshPrefs.getBoolean("ClosedSW", true);
		MyIssuesSW            = mshPrefs.getBoolean("MyIssuesSW", false);

		Configuration conf = getResources().getConfiguration();
		conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
		metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

		getActivity().getResources().updateConfiguration(conf, getActivity().getResources().getDisplayMetrics());

		return new Resources(getActivity().getAssets(), metrics, conf);
	}
	
	
	/**
	 *  This class creates a custom Info window that has a title, a snippet and an icon.
	 * 
	 * 
	 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
	 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
	 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
	 *
	 */
	public class InfoWindowAdapterButtoned implements InfoWindowAdapter{

		
		// This viewgroup contains an ImageView with id "badge" and two TextViews with id "title" and "snippet".
	    private final View mWindow;

	    InfoWindowAdapterButtoned() {
	        mWindow =  getActivity().getLayoutInflater().inflate(R.layout.custom_info_window, null);
	    }

	    @Override
	    public View getInfoWindow(Marker marker) {
	        render(marker, mWindow);
	        return mWindow;
	    }

	    @Override
	    public View getInfoContents(Marker marker) {
	        render(marker, mWindow);
	        return mWindow;
	    }

	    private void render(Marker marker, View view) {
	        
	        String title = marker.getTitle();
	        TextView titleUi = ((TextView) view.findViewById(R.id.title));
	        if (title != null) {
	            titleUi.setText(title);
	        } else {
	            titleUi.setText("");
	        }

	        String snippet = marker.getSnippet();
	        TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
            snippetUi.setText(snippet);
	    }

	}
	
	
} // end of Fragment_Map

