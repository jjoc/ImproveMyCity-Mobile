/** Service_Data */
package com.mk4droid.IMC_Services;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;


import com.mk4droid.IMCity_PackDemo.R;

import com.mk4droid.IMC_Activities.FActivity_TabHost;
import com.mk4droid.IMC_Constructors.Category;
import com.mk4droid.IMC_Constructors.Issue;
import com.mk4droid.IMC_Constructors.VersionDB;
import com.mk4droid.IMC_Store.Constants_API;


/**
 * It is a controller to decide when to update local database.
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Service_Data extends Service {

	/** Provides information about connectivity to internet */ 
	public static boolean HasInternet    = false;
	
	/** Access to local database contents */ 
	public static DatabaseHandler dbHandler;

	/** Data of Categories */
	public static ArrayList<Category> mCategL; // List of Issues Categories
	
	/** Data of Issues */
	public static ArrayList<Issue> mIssueL;    // List of Issues 

	
	//------- Internet connection Listener ---
    MyConnectivityListener connListener  = null;
    IntentFilter connIntentFilter        = null;
    boolean connIntentFilterIsRegistered = false;
    
    //------- Receivers -----
	private BroadcastReceiver mReceiverRefreshData, mReceiverRefreshCategs; 
	IntentFilter intentFilter;
	
    //------- Database       -----------
    
	VersionDB versionDB, versionDB_Past, versionCategDB, versionCategDB_Past;//Hold versions of MySQL  

	Thread updThr;
	private boolean stopThread = false;

	String LangSTR;
	Resources resources;
	
	boolean StartedUPD =false;
		
	String UserNameSTR,PasswordSTR;
	
    //------------------- onBind ----------------------
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	//=======================   onCreate        ================================
	/**
	 *   1. Register internet connectivity listener
	 *   2. Get local db data
	 *   3. Register receiver for refreshing data if any out of 5 refreshing events occurs
	 *   4. Register receiver for refresing visualization if any category filter has changed (no downloading). 
	 */
	@Override
	public void onCreate() {
		resources = setResources();
		
		//------- Register internet connectivity listener --------
		connIntentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        connListener = new MyConnectivityListener();
        
		if (!connIntentFilterIsRegistered) {
            registerReceiver(connListener, connIntentFilter);
            connIntentFilterIsRegistered = true;
        }
		
		HasInternet = InternetConnCheck.getInstance(this).isOnline(this);
		
	    //----------- GET LOCAL DATA --------------------
	    dbHandler = new DatabaseHandler(this);
	    versionDB_Past       = dbHandler.getVersion();
	    versionCategDB_Past  = dbHandler.getCategVersion();
	    mCategL              = dbHandler.getAllCategories();
	    mIssueL              = dbHandler.getAllIssues();
	    
	    dbHandler.db.close();
	    

	    //-------------   Receiver for changes in DB --------------- 

	    intentFilter = new IntentFilter("android.intent.action.MAIN");
	    
	    mReceiverRefreshData = new BroadcastReceiver() {
	    	@Override
	    	public void onReceive(Context context, Intent intent) {

	    		if (HasInternet ){ 

	    			String Refresh          = intent.getStringExtra("Refresh");         // 1
	    			String DistanceChanged  = intent.getStringExtra("DistanceChanged"); // 2
	    			String IssuesNoChanged  = intent.getStringExtra("IssuesNoChanged"); // 3
	    			String NewIssueAdded    = intent.getStringExtra("NewIssueAdded");   // 4
	    			String LocChanged       = intent.getStringExtra("LocChanged");      // 5 

	    		    SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    		    int distanceData    = Integer.parseInt(  mshPrefs.getString("distanceData"   ,  "5000") );
	    		    int IssuesNoAR      = Integer.parseInt(  mshPrefs.getString("IssuesNoAR", "40"));
	    			
	    			if (!StartedUPD){
	    				if (Refresh!=null || DistanceChanged!=null ||  IssuesNoChanged!=null || 
	    						    NewIssueAdded != null || LocChanged!= null){
	    					
	    					Log.e("ServData: Refresh DistCh IssuesNoCh NewIssue LocCh ", 
	    							Refresh + DistanceChanged +  IssuesNoChanged + NewIssueAdded  + LocChanged);

	    					//----- Refresh DB -----------------
    						DBRefreshActions(distanceData, IssuesNoAR, Download_Data.DownloadTimeStamp());

    						savePreferences("distanceDataOLD", Integer.toString(distanceData), "String");
    						savePreferences("IssuesNoAROLD", Integer.toString(IssuesNoAR), "String");
    						
    						
	    				}
	    			}
	    		}
	    	}
	    };
		
        
       this.registerReceiver(mReceiverRefreshData, intentFilter);

       
       //----------- Receiver for category change (no downloading) -------------
       
       mReceiverRefreshCategs = new BroadcastReceiver() {
	    	@Override
	    	public void onReceive(Context context, Intent intent) {
       
	    		String FiltersChanged = intent.getStringExtra("FiltersChanged");         // 1
	    		
	    		if (FiltersChanged!=null){
	    			mCategL =  dbHandler.getAllCategories();
	        		sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("DataChanged", "ok"));	
	    		}
	    		
        	}
   	   };
       
   	   this.registerReceiver(mReceiverRefreshCategs, intentFilter);
       
    }

	//=======================  onDestroy         ================================
	/**
	 * On destroy service unregister receivers and stop updating if thread is alive
	 */
	@Override
	public void onDestroy() {

		// --- Unregister Receivers
		if (connIntentFilterIsRegistered) { 		// Unregister Connectivity Listener 
            unregisterReceiver(connListener);
            connIntentFilterIsRegistered = false;
        }

		this.unregisterReceiver(mReceiverRefreshData);    //Downloading
		this.unregisterReceiver(mReceiverRefreshCategs);  //Category filtering

		// ------ stop updating thread if is alive ---------
		stopThread = true;
		try{
			if (updThr.isAlive())
				updThr.interrupt();
			
		} catch (Exception e){
			
		}
			
		
		dbHandler.db.close();
		
		stopSelf();
		super.onDestroy();
	}
	

	//========================= onStart ==================================
	/**    
	 *   Start a thread for periodic check (default is 5 minutes) if the local database has the same 
	 *   version of remote database. If differ than perform an update of local database.
	 */
	@Override
	public void onStart(Intent intent, int startid) {
		   
		updThr = new Thread(new Runnable() { 
			public void run(){

				while(!stopThread && ! Thread.interrupted()){
					if (HasInternet){ 
						
						try{						
						versionDB = Download_Data.DownloadTimeStamp();
						
						//------- Get previous session distance range of data 
						if (versionDB!=null && !StartedUPD ){				
														
							if (!versionDB._time.equals(versionDB_Past._time) ){
								
								//---- dialog show ------
								Message msg1 = new Message();
								msg1.arg1 = 1;
								//Activity_Main.CentralMap.handlerDialogsPeriodicSync.sendMessage(msg1);
								//---------------------

								try{
									SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
									int distanceData    = Integer.parseInt(  mshPrefs.getString("distanceData"   ,  "5000") );
									int IssuesNoAR      = Integer.parseInt(  mshPrefs.getString("IssuesNoAR", "40"));
									
									DBRefreshActions(distanceData, IssuesNoAR, versionDB);
								} catch (Exception e){
									Message msg2 = new Message();
									msg2.arg1 = 0;
									//Activity_Main.CentralMap.handlerDialogsPeriodicSync.sendMessage(msg2);
									StartedUPD = false;
								}

								//---- dialog dismiss ------
								Message msg2 = new Message();
								msg2.arg1 = 0;
								//Activity_Main.CentralMap.handlerDialogsPeriodicSync.sendMessage(msg2);
							}}
						} catch (NullPointerException e){
							Log.e(Constants_API.TAG, "Service_Data:Failed to periodically syncronize because app was closed"
						                                 +e.getMessage());
						} catch (Exception e){
							Log.e(Constants_API.TAG, "Service_Data:Failed to periodically syncronize because of unkown event"
									          +e.getMessage());
						}
					}

					try{
						Thread.sleep(FActivity_TabHost.RefrateAR * 60 * 1000); 
					} catch (InterruptedException e) {
						//Log.e(Constants_API.TAG, "Service_Data:Thread was unable to sleep:" + e.getMessage() );
						stopThread = true;
					}
				}}});
		updThr.start();
	}
	
	//=======================  DBRefreshActions         ================================
	/**
	 *   Refresh local database and count how many bytes received.  
	 *   
	 *   1. Download category timestamp and perform update of categories
	 *   2. Update local table issues
	 *   3. Broadcast that data has changed
	 * 
	 * @param distanceData
	 * @param IssuesNoAR
	 * @param NewVersionDB
	 */
	public void DBRefreshActions(int distanceData, int IssuesNoAR, VersionDB NewVersionDB){

		if (!HasInternet)
			return;
		
		try {
			StartedUPD  = true;
			
			dbHandler = new DatabaseHandler(getApplicationContext());
			
			//--------- Check to update categories in sqlitedb -------
		    VersionDB versionCategDB_Down = Download_Data.DownloadCategTimeStamp();
		    
		    int kbCateg=0;
		    if (!versionCategDB_Down._time.equals(versionCategDB_Past._time) || 
		    		                                   versionCategDB_Down._id==0){
			    kbCateg = dbHandler.addUpdCateg();
			    dbHandler.AddUpdCategVersion(versionCategDB_Down);
			    
			    versionCategDB_Past = versionCategDB_Down;
		    }
		    
		    //---------Update Issues ---------------------------------
			int kbIssues = dbHandler.addUpdIssues(Service_Location.locUser.getLongitude(), 
					                              Service_Location.locUser.getLatitude(),
					                distanceData, IssuesNoAR);
			
			int kbVotes = dbHandler.AddUpdUserVotes(UserNameSTR, PasswordSTR);
			
			mCategL =  dbHandler.getAllCategories();
			mIssueL =  dbHandler.getAllIssues();
			
			if (mIssueL.size()>0)
				dbHandler.AddUpdVersion(NewVersionDB);

			//---------
			dbHandler.db.close();
			StartedUPD = false;
			
			int KB_down = kbIssues + kbVotes + kbCateg;
		    
			//-----  Broadcast Data has changed ---------------
			sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("DataChanged", "ok"));
			
			Toast.makeText(getBaseContext(), resources.getString(R.string.Downloaded) + ": " +
                                                         Integer.toString( KB_down/1000 ) + " kB ", Toast.LENGTH_LONG).show();
			
		} catch (Exception e){
			Log.e(Constants_API.TAG,"Service_DATA: DBRefreshActions: Unable to perform all actions");
			dbHandler.db.close();
			
			//-----  Broadcast Data has changed to close all dialogues ---------------
			sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("DataChanged", "ok"));
		}
		
	}
	
	
	//=======================  MyConnectivityListener         ================================
	/**
	 * Receiver for any change in internet connectivity
	 */
	protected class MyConnectivityListener extends BroadcastReceiver {
		Context ctx;
		
		@Override
		public void onReceive(Context context, Intent intent) {
			ctx = context;
			
			HasInternet = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

			//-------- Caused by ------------
			//String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
			//boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
			//NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			//NetworkInfo otherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
			// do application-specific task(s) based on the current network state, such
			// as enabling queuing of HTTP requests when currentNetworkInfo is connected etc.
			//ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			//NetworkInfo info = cm.getActiveNetworkInfo();
		}
	}; 
	
	//================= savePreferences ====================================
	 /**
     * Save a value to preferences, either string or boolean
     * 
     * @param key       name of the parameters to save
     * @param value     value of the parameter to save 
     * @param type      either "String" or "Boolean" 
     */
	private void savePreferences(String key, Object value, String type){
		SharedPreferences shPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = shPrefs.edit();

		if (type.equals("String")) 
			editor.putString(key, (String) value);
		else 
			editor.putBoolean(key, (Boolean) value);

		editor.commit();
	}
	
	//=======================   setResources        ================================
	/**
     *  Set language Resources depending on the language saved in the preferences   
     * @return resources depending on the language chosen
     */
	public Resources setResources(){
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    LangSTR          = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);
	    UserNameSTR      = mshPrefs.getString("UserNameAR", "");
	    PasswordSTR      = mshPrefs.getString("PasswordAR", "");
	    
   	    Configuration conf = getResources().getConfiguration();
        conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
        DisplayMetrics metrics = new DisplayMetrics();
        return new Resources(getAssets(), metrics, conf);
    }
}