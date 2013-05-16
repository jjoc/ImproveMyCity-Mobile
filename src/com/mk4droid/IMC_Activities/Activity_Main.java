/**
 *         Activity_Main
 */
package com.mk4droid.IMC_Activities;

import java.util.List;
import java.util.Locale;

import com.flurry.android.FlurryAgent;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.mk4droid.IMCity_PackDemo.R;
import com.mk4droid.IMC_Core.MapIssues_ItemizedOverlay;
import com.mk4droid.IMC_Services.InternetConnCheck;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Services.Service_Location;
import com.mk4droid.IMC_Services.Service_Location.LocalBinder;
import com.mk4droid.IMC_Store.Constants_API;


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
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * This is the activity that shows all issues overlaid on a map.
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class Activity_Main extends MapActivity implements OnClickListener{
	
	/** User id number  */
	public static String UserID_STR;
	
	/** User actual name  */
	public static String UserRealName;
	
	/** Handler for updating Markers */
	public static Handler handlerMarkersUPD;
	
	/** Handler for refreshing data */
	public static Handler handlerBroadcastRefresh;
	
	/** Handler for periodic updating */
	public static Handler handlerDialogsPeriodicSync;
	
    //============ System variables ======================	
	Context ctx;
	Resources resources;
	DisplayMetrics metrics;
	String LangSTR;
	
	//------- Filters --------
    boolean ClosedSW = true, OpenSW = true, AckSW  = true, MyIssuesSW ;
	    	    
    //----- Maps -------------------------------
	MapIssues_ItemizedOverlay itemizedoverlay_constr; // icons
    static SmallCompassOverlay myLocationOverlay;   // current location
	static MapView mapView;
    
	static List<Overlay> mapOverlays;
	boolean FirstAnimToLoc = true;
	static boolean isLocServBound = false;
    int minLat=0,minLong=0,maxLat=0,maxLong=0; // For Zooming correctly
	
	//----------- GUI ----------
	Button btMaps,btARView,btHandleD,btRefresh; // D stands for "Sliding Drawer"
	
	SharedPreferences mshPrefs;
	int tlv = Toast.LENGTH_LONG;

	//------ Dialogs, Threads, Intents, IntentFilters --------
	ProgressDialog dialogRefresh, dialogPeriodicSync = null;;
	Intent IntDataServ,IntLocServ;
	BroadcastReceiver mReceiverDataChanged;
	IntentFilter intentFilter;
	
	public static Service_Location mService_Location = null;
	
	private ServiceConnection mConnection = new ServiceConnection() {

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

	
	
	
	//==========  OnCreate ==================== 
	/**
	 *    Create GUI and Handlers for broadcasting messages
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    
		//---------------- Set GUI -------------------
	    resources = SetResources();
	    setContentView(R.layout.activity_main);
	    ctx = this;
	
	    dialogRefresh = new ProgressDialog(ctx);
	    
	    
	    btMaps   = (Button) findViewById(R.id.btMapChange);
	    //btARView = (Button) findViewById(R.id.btARView);
	    btRefresh = (Button) findViewById(R.id.btRefresh);
	    
	    //---- Bind Location Service
	    IntLocServ = new Intent(getApplicationContext(), Service_Location.class);

	    //startService(IntLocServ); // This leaves the service open until stopService is called explicitly, 
	    //                            // it is better to bind service with the Activities that actually need it 

	    if (!isLocServBound && Activity_TabHost.IndexGroup == 0){
	    	//Log.e("onCreate Main", " " + Activity_TabHost.IndexGroup );
	    	getApplicationContext().bindService(IntLocServ, mConnection, Context.BIND_AUTO_CREATE);
	    }
	    	          
	    Activity_TabHost.IndexGroup = 0;
		//-----Start Data Service
		IntDataServ = new Intent(this, Service_Data.class);
	    startService(IntDataServ);
        
	    //------------ Create Items on Maps -----------------------
	    mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    
	    mapOverlays        = mapView.getOverlays();
	    myLocationOverlay  = new SmallCompassOverlay(this, mapView);
	    
	    //----- Handler for Redrawing Markers from update thread ------------
	    handlerMarkersUPD = new Handler()
	    {
	    	public void handleMessage(Message msg)
	    	{
	    		if (msg.arg1 == 1) // Redraw markers
	    		{   

	    			InitiateMarkers();	
	    			PutMarkers();


	    			//-------- dismiss cases 1. RefreshButton 2. DistanceCh  3. IssuesNoCh 4. LocationCh
	    			if (dialogRefresh.isShowing())
	    				dialogRefresh.dismiss();

	    			//-------- dismiss case New Issue -----------
	    			try{
	    				if (Activity_NewIssueB.dialogNewIssue.isShowing())
	    					Activity_NewIssueB.dialogNewIssue.dismiss();
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
            	  sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("Refresh", "ok"));  
               else if (msg.arg1 == 2)
        		  sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("DistanceChanged", "Indeed"));
               else if (msg.arg1 == 3)
          		  sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("IssuesNoChanged", "yep"));
              
              super.handleMessage(msg);
           }
        };
        
        
        //----- Handler for Redrawing Markers from update thread ------------
        handlerDialogsPeriodicSync = new Handler()
        {
        	public void handleMessage(Message msg)
        	{
        		if (msg.arg1 == 1){
        			dialogPeriodicSync = ProgressDialog.show(Activity_TabHost.ctx, 
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
		
        this.registerReceiver(mReceiverDataChanged, intentFilter);

        //-------- Init markers when no internet ---------
        Message msg = new Message();
		msg.arg1 = 1;
		handlerMarkersUPD.sendMessage(msg);

		
		if(!InternetConnCheck.getInstance(ctx).isOnline(ctx))
			Toast.makeText(ctx, "No internet", tlv).show();
		
        
	}// end of Create 
      
	
	//=================== onResume ===========================
	/** 
	 *    On resuming from tabChange
	 */
	@Override
	public void onResume() {
		super.onResume();
				
		resources = SetResources(); // to get new UserID
		
				
		if (!isLocServBound){
			getApplicationContext().bindService(IntLocServ, mConnection, Context.BIND_AUTO_CREATE);
		}
		
		//---------- Maps ------------
		String bt_Tag = btMaps.getTag().toString();
		
        if (bt_Tag.equals("Normal")){
        	mapView.setSatellite(false);
        	btMaps.setText(resources.getString(R.string.NormalMap));
        	btMaps.setTag("Normal");
        } else if (bt_Tag.equals("Satellite")){
        	mapView.setSatellite(true);
        	btMaps.setText(resources.getString(R.string.Satellite));
        	btMaps.setTag("Satellite");
        }
		
        btRefresh.setText(resources.getString(R.string.Refresh));
        
		LinearLayout llmain = (LinearLayout)findViewById(R.id.llmain);
		llmain.setVisibility(View.VISIBLE);
		mapView.setVisibility(View.VISIBLE);
		
	    myLocationOverlay.enableCompass();
	    

	    myLocationOverlay.enableMyLocation();
	    
	    mapView.getOverlays().add(myLocationOverlay);
	    
    	//-------------- Check if distance has changed -------------
	    SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
    		FlurryAgent.onStartSession(this,Constants_API.Flurry_Key);
	}

	
	
	
	//==================== on Pausing  ===============
	/**
	 *      Make invisible the mapview and its overlays otherwise they cause conficts 
	 */
	@Override
	public void onPause() {
		
		
		LinearLayout llmain = (LinearLayout)findViewById(R.id.llmain);
		llmain.setVisibility(View.INVISIBLE);
		
		myLocationOverlay.disableCompass(); 
		myLocationOverlay.disableMyLocation();
		
				
		mapView.setVisibility(View.INVISIBLE);
		
		//------ Unbind Services -------
		//Log.e("isLocServBound PAUSE", " " + isLocServBound);
		
		if (isLocServBound){
			getApplicationContext().unbindService(mConnection);
			isLocServBound = false;	
		}
		
		
		//----------- Flurry Analytics --------
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);
    	
    	if (AnalyticsSW)
    		FlurryAgent.onEndSession(this);
    	
    	super.onPause();
	}
	
	//================ Destroy App from this Activity ===============
	/**     
	 *         Stop services when user exits app from this activity.   
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (Service_Data.dbHandler.db.isOpen())
			Service_Data.dbHandler.db.close();

		stopService(IntDataServ);
		//stopService(IntLocServ);

		if (isLocServBound){
			getApplicationContext().unbindService(mConnection);
			isLocServBound = false;
		}

		
	}	

	//--------------  isRouteDisplayed  ----------------
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}

	
	
	
	//===================== InitiateMarkers ===========
	/**
	 *   Clear overlays
	 */
	public static void InitiateMarkers(){
		
        //----------- Clear Previous- ------------
		mapView.getOverlays().clear();
        mapView.postInvalidate();		
        
        
        mapView.getOverlays().add(myLocationOverlay);
        myLocationOverlay.enableCompass();
                
		if (Activity_TabHost.IndexGroup == 0){
       	  myLocationOverlay.enableMyLocation();
		}
	}
	
	//=========== PutMarkers() =======================
	/**                
	 *      Markers are placed in overlays. Each overlay contains markers that have the same icon image.        
	 */       
	public void PutMarkers(){
	    
        if (Service_Data.mIssueL.size() > 0) {
        	minLat =  (int)(Service_Data.mIssueL.get(0)._latitude*1E6);
        	minLong = (int)(Service_Data.mIssueL.get(0)._longitude*1E6);
        	maxLat =  (int)(Service_Data.mIssueL.get(0)._latitude*1E6);
        	maxLong=  (int)(Service_Data.mIssueL.get(0)._longitude*1E6);
        }
		
	    //------------------ Urban Problems ----------------
		int NIssues = Service_Data.mIssueL.size(); 
		
		for (int j=0; j< Service_Data.mCategL.size(); j++){
			if (Service_Data.mCategL.get(j)._visible==1){  // Filters for Visibility

				// Every categ has an overlayitem with multiple overlays in it !!! 
				//---------- Drawable icon -------
				byte[] b  =  Service_Data.mCategL.get(j)._icon;

				Bitmap bm = BitmapFactory.decodeByteArray(b, 0, b.length);

				bm = Bitmap.createScaledBitmap(bm, (int) ((float)metrics.densityDpi/3.5), 
						                           (int) ((float)metrics.densityDpi/3), true);
				
				Drawable drawable_Categ = new BitmapDrawable(bm);
				MapIssues_ItemizedOverlay itemizedoverlay_Categ   = new MapIssues_ItemizedOverlay(drawable_Categ); //-init itemizedoverlay (1 for each category)

				for(int i=0; i<NIssues; i++){
					
					//------ iterate to find category of issue and icon to display
					if ( (Service_Data.mIssueL.get(i)._currentstatus == 1 && OpenSW) || 
							(Service_Data.mIssueL.get(i)._currentstatus == 2 && AckSW) || 
							(Service_Data.mIssueL.get(i)._currentstatus == 3 && ClosedSW) ){

						if (Service_Data.mIssueL.get(i)._catid == Service_Data.mCategL.get(j)._id ){

							if (MyIssuesSW && !Integer.toString(Service_Data.mIssueL.get(i)._userid).equals(UserID_STR))
								continue;
							
							//--------- upd view limits -------------
							maxLat  = Math.max((int) (Service_Data.mIssueL.get(i)._latitude*1E6) , maxLat );
							minLat  = Math.min((int) (Service_Data.mIssueL.get(i)._latitude*1E6), minLat );
							maxLong = Math.max((int) (Service_Data.mIssueL.get(i)._longitude*1E6) , maxLong);
							minLong = Math.min((int) (Service_Data.mIssueL.get(i)._longitude*1E6), minLong);

							//---------- GPS coords --------
							GeoPoint point = new GeoPoint((int) (Service_Data.mIssueL.get(i)._latitude*1E6),
									                      (int) (Service_Data.mIssueL.get(i)._longitude*1E6));

							OverlayItem overlayitem = new OverlayItem(point, Service_Data.mIssueL.get(i)._title, 
									           "I:" + Integer.toString(Service_Data.mIssueL.get(i)._id));
							
							//------------- Add to mapOverlays ------------
							itemizedoverlay_Categ.addOverlay(overlayitem);
							
						} // cat match 
					} // open closed 
					
				} // i

				if (itemizedoverlay_Categ.size()>0)
					  mapOverlays.add(itemizedoverlay_Categ);

				
			} // visible
			
		} // j

		
		
        if (FirstAnimToLoc)
        	animToLoc();
	}
    
	
  	// ============ AnimateToLoc ========================
	/**
	 *  Animate and zoom to the location of the issues
	 */
	public void animToLoc(){
			  if (Math.abs(minLat) + Math.abs(minLong) + Math.abs(maxLat) + Math.abs(maxLong) > 0){
		        	mapView.getController().zoomToSpan(Math.abs(maxLat - minLat), Math.abs(maxLong - minLong));
		        	mapView.getController().animateTo(new GeoPoint( minLat/2+maxLat/2,  maxLong/2 + minLong/2));
		        	mapView.invalidate();
					FirstAnimToLoc = false;
		        }
 	}
	
	//=======     OnClick issue  =======================================
	/**
	 *        See issue in details
	 */
	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();

		switch(id){
		case (R.id.btRefresh):

			//---- Show dialog Refresh ----------
			dialogRefresh = ProgressDialog.show(ctx, resources.getString(R.string.Downloading),
					resources.getString(R.string.Refresh), true);

			// ------- Broadcast Refresh through a handler
			Message msg = new Message();
			msg.arg1 = 1;
			handlerBroadcastRefresh.sendMessage(msg);
			break;
		case (R.id.btARView):
			//startActivity(new Intent(ctx, Activity_AR.class)); 
			break;
		case (R.id.btMapChange):
			//------------- Other maps views click --------

			String bt_Tag = btMaps.getTag().toString();

		if (bt_Tag.equals("Satellite")){
			mapView.setSatellite(false);
			btMaps.setText(resources.getString(R.string.NormalMap));
			btMaps.setTag("Normal");
		} else if (bt_Tag.equals("Normal")){
			mapView.setSatellite(true);
			btMaps.setText(resources.getString(R.string.Satellite));
			btMaps.setTag("Satellite");
		}
		// mapView.setStreetView(true); // May be available in your country
		// mapView.setTraffic(true);
		break;
		} 
	}

	//===========     Set Resources  ========================= 
	/**
	 *      Obtain resources from preferences 
	 * @return
	 */
	public Resources SetResources(){

		LangSTR          = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);
		UserID_STR       = mshPrefs.getString("UserID_STR", "");

		OpenSW                = mshPrefs.getBoolean("OpenSW", true);
		AckSW                 = mshPrefs.getBoolean("AckSW", true);
		ClosedSW              = mshPrefs.getBoolean("ClosedSW", true);
		MyIssuesSW            = mshPrefs.getBoolean("MyIssuesSW", false);

		UserRealName     = mshPrefs.getString("UserRealName", "");


		Configuration conf = getResources().getConfiguration();
		conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		getBaseContext().getResources().updateConfiguration(conf, getBaseContext().getResources().getDisplayMetrics());

		return new Resources(getAssets(), metrics, conf);
	}


	//=============  SmallCompassOverlay  ==================
    /**       
     *   Make compass a little smaller
     */
	private class SmallCompassOverlay extends MyLocationOverlay{
		public SmallCompassOverlay(Context context, MapView mapView) {
			super(context, mapView);
		}


		//========== drawCompass= =========================
		/**
		 * Make compass a little smaller 
		 */
		@Override
		protected void drawCompass(Canvas canvas, float bearing) {
			if (metrics.widthPixels < 800)
				canvas.scale(0.6f, 0.6f, 0.4f, 0.4f);

			super.drawCompass(canvas, bearing);

			if (metrics.widthPixels < 800)
				canvas.scale(1.66f, 1.66f);

		}

	}

	
	
}