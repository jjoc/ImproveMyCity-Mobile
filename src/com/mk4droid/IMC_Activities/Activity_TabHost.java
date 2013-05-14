/**
 *         Activity_TabHost
 */

package com.mk4droid.IMC_Activities;


import java.util.List;
import java.util.Locale;
import com.mk4droid.IMCity_Pack.R;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Store.Constants_API;


import android.app.ActivityManager;
import android.app.TabActivity;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

/**
 *  Tabs (at the bottom of application) are hosted in this activity.
 *  (1=Main, 2=List, 3=New, 4=Filters, 5=Setup) 
 *  Upon clicking a tab, this activity guides user to the next group of activities or a single activity. 
 *  Groups of activities are 2=List and 3=New.    
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class Activity_TabHost extends TabActivity implements OnTabChangeListener,OnTouchListener {
	
	/**
	 *  The object that hosts all tabs
	 */
	public static TabHost mTabHost;
	
	/** This context is related to the whole application. 
	 * It is useful for presenting messages with Toast from everywhere in the application (READ-only). */
	public static Context ctx;
	
	/**
	 *  These resources are related mainly to the language of the GUI and they can be retrieved from the whole application
	 *  for presenting localized messages (READ-only)
	 */
	public static Resources resources;
	
	/**
	 *  Refresh rate in minutes for updating data (DEFAULT:5, Here READ-ONLY, can be modified by Activity_Setup) 
	 */
	public static int RefrateAR      = 5;
	
	/**
	 *  Current active tab defines the group of activities (1=Main, 2=List, 3=New, 4=Filters, 5=Setup)
	 */
	public static int IndexGroup = 0;
	
	
	int display_width, tabhost_width;
	int tlv = Toast.LENGTH_LONG;
	ImageView imvTr;
	
	Configuration conf;
	String UserNameSTR = "";
	String PasswordSTR = "";
	String LangSTR; 
	     
    DisplayMetrics metrics;
        
    int NTabs = 5;
    int prevTab=0; // previous tab
    
	TabSpec[] mTabSpec = new TabSpec[NTabs];     // Each Tab
	Drawable mD_Main,mD_View3D,mD_Report,mD_Setup,mD_Filters,mD_List; 
    
    //------------------- on CREATE --------------------
	/**
	 *  Executed when tabhost is created     
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    //----------------- GUI ------------
	    resources   = SetResources();  //---Load Prefs and Modify resources accordingly
 	    setContentView(R.layout.activity_tabhost);         //---------- Content view
	    ctx = this; 

	    mTabHost = (TabHost)findViewById(android.R.id.tabhost);      // Set TabHost
        imvTr = (ImageView)findViewById(R.id.imvTriangle);
        
	    //--------- Display-------
	    Display display = getWindowManager().getDefaultDisplay();
	    display_width   = display.getWidth();
	    tabhost_width   = (int) (((float)display_width)/NTabs); 
        tabhost_width   = (int) (((float) metrics.widthPixels)/NTabs); // Tabs Width  
        
        //---------------------------------------
	    for (int i=0; i<NTabs ; i++)
             mTabSpec[i]  = mTabHost.newTabSpec("tid"+ Integer.toString(i));  
                 
        mD_Main   = getResources().getDrawable(R.drawable.map);
        mD_List   = getResources().getDrawable(R.drawable.list);
        mD_Report = getResources().getDrawable(R.drawable.plus);
        mD_Filters= getResources().getDrawable(R.drawable.filter);
        mD_Setup  = getResources().getDrawable(R.drawable.setup);

        //-------------- Set icons and texts localized per tab -------------
        TextView tvhelperA = makeTabIndicatorActive(resources.getString(R.string.Main),mD_Main);
        TextView tvhelperB = makeTabIndicatorInActive(resources.getString(R.string.List),mD_List);
        TextView tvhelperC = makeTabIndicatorInActive(resources.getString(R.string.Report),mD_Report);
        TextView tvhelperD = makeTabIndicatorInActive(resources.getString(R.string.Filters),mD_Filters);
        TextView tvhelperE = makeTabIndicatorInActive(resources.getString(R.string.Setup),mD_Setup);

        int drPad = 2;     // padding
        int topPad = 5;    // margin
        
        tvhelperA.setCompoundDrawablePadding(drPad);
        tvhelperA.setPadding(0, topPad, 0, 0);
        tvhelperB.setCompoundDrawablePadding(drPad);
        tvhelperB.setPadding(0, topPad, 0, 0);
        tvhelperC.setCompoundDrawablePadding(drPad);
        tvhelperC.setPadding(0, topPad, 0, 0);
        tvhelperD.setCompoundDrawablePadding(drPad);
        tvhelperD.setPadding(0, topPad, 0, 0);
        tvhelperE.setCompoundDrawablePadding(drPad);
        tvhelperE.setPadding(0, topPad, 0, 0);
        
        
        mTabSpec[0].setIndicator(tvhelperA);
        mTabSpec[0].setContent(new Intent(this, Activity_Main.class));
        mTabSpec[1].setIndicator(tvhelperB); 
        mTabSpec[1].setContent( new Intent(this, Group_ListOfIssues.class));
        mTabSpec[2].setIndicator(tvhelperC);
        mTabSpec[2].setContent(new Intent(this, Group_NewIssue.class));
        mTabSpec[3].setIndicator(tvhelperD);
        mTabSpec[3].setContent( new Intent(this, Activity_Filters.class));
        mTabSpec[4].setIndicator(tvhelperE); 
        mTabSpec[4].setContent( new Intent(this, Activity_Setup.class)); 
        
        
        // Add tabSpec to the TabHost to display 
        for (int i=0; i<NTabs ; i++)  
           mTabHost.addTab(mTabSpec[i]);  
        
        mTabHost.setOnTabChangedListener(this);
	    
        // Add touch listener to each tab 
        TabWidget mTabWidget = mTabHost.getTabWidget();
        for (int i = 0; i <NTabs; i++) {
            View v = mTabWidget.getChildAt(i);
            v.setOnTouchListener(this);
        }

	    //--------- bottom bar Triangle Indicator set ---------------
        imvTr.scrollTo(display_width/2/NTabs * (NTabs - 2*0 - 1), 0);
	}// ---- End OnCreate ----

	//============ on Destroy Application ===================
	/**
	 *  Close database 
	 */
    @Override
    protected void onDestroy() {
        	super.onDestroy();
        	if (Service_Data.dbHandler.db.isOpen())
        		Service_Data.dbHandler.db.close();
        	
//        	if (isServiceRunning("com.mk4droid.IMC_Services.Service_Location"))
//        		stopService(new Intent(this, Service_Location.class)); //startService(new Intent(this, Service_Location.class));
//
        	if (isServiceRunning("com.mk4droid.IMC_Services.Service_Data"))
        		stopService(new Intent(this, Service_Data.class)); //startService(new Intent(this, Service_Location.class));
    }
    
    
    /**        isServiceRunning
	 * 
	 * Check if a service is running (Data or Location)
	 * 
	 * @param serviceClassName
	 * @return
	 */
	public static boolean isServiceRunning(String serviceClassName){
        final ActivityManager activityManager = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
                return true;
            }
        }
        return false;
     }
	
	//=====   Orientation Changed ==========
    /**
     *   Re-draw tab with new width
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
	    Display display = getWindowManager().getDefaultDisplay();
	    display_width   = display.getWidth();
	    tabhost_width   = (int) (((float)display_width)/NTabs); 
        
	    imvTr.scrollTo(display_width/2/NTabs * (NTabs - 2*mTabHost.getCurrentTab() - 1), 0);
    }

   //========== Menu hard button ============
   /**
    *      Menu hard button (only to exit) 
    */
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
	   
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.menu, menu);
       
       return true;
   }
   
   //=========== Menu hard button pressed option =====
   /**
    *  Only exit option for the time being
    */
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
           case R.id.exit:     
        	   finish();
               break;
       }
       return true;
   }
   
	//==================   OnTabChange =========================
    /**
     *    Changes current activity and colors of the tabSpecs      
     */
	@Override
	public void onTabChanged(String arg0) {
		
		TextView[] v  = new TextView[NTabs];

		for (int i=0; i<NTabs ; i++)
			v[i] = (TextView) mTabHost.getTabWidget().getChildAt(i);
		
		//--------- Set Color of Tabs ---------------
		for (int i=0; i<NTabs ; i++){
			String txt    = v[i].getText().toString();
			Drawable[] dr = v[i].getCompoundDrawables();
			
						
			if (mTabHost.getCurrentTab() == i){

				v[i] = ChangeViewAct(v[i], txt,dr[1]);
				imvTr.scrollTo(display_width/2/NTabs * (NTabs - 2*i - 1), 0);
				
				Activity_TabHost.IndexGroup = i;
				
				//------- Reset tab 1 to remove history -------
				if (prevTab == 1){
					if(Group_ListOfIssues.history.size() > 1) {
						Group_ListOfIssues.group.back();
				}}
				//---------------------------------------	
				
				prevTab = i;
			} else
				v[i] = ChangeViewInAct(v[i], txt, dr[1]);
     	}	
	}
	
	//================== onTouch ====================
	/**
	 *    if current tab is Main and Group List of issues has a history of 
	 *         many activities then reset Group_list to the first activity of the group so as to remove any history
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		if (mTabHost.getCurrentTab()==1){
			if (mTabHost.getCurrentTab()==1 && Group_ListOfIssues.history.size()>1)
				while( Group_ListOfIssues.history.size() > 1) {
					Group_ListOfIssues.history.remove(Group_ListOfIssues.history.size()-1);
					Group_ListOfIssues.group.setContentView((View) 
					   Group_ListOfIssues.history.get(Group_ListOfIssues.history.size()-1)  );
				}
		}
		
		return false;
	}
	
	//------------------ Colorize Tabs ----------------------------
	private TextView makeTabIndicatorActive(String text, Drawable dr){
		TextView tabView = new TextView(this);
		return ChangeViewAct(tabView, text, dr);
		}

	
	private TextView ChangeViewAct(TextView v, String text, Drawable dr){
		
		v.setText(text);
		v.setTextSize(13);
		v.setTextColor(Color.WHITE);
		v.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
		
//		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO)
//	          v.setPadding(0, 15, 0, 0);
		
		dr.setColorFilter(0xFF95baec, android.graphics.PorterDuff.Mode.MULTIPLY);
		
		v.setCompoundDrawablesWithIntrinsicBounds(null, dr, null, null);
		v.setBackgroundDrawable( getResources().getDrawable(R.drawable.gradient_blue) );
		
		return v;
	}
	
	private TextView makeTabIndicatorInActive(String text, Drawable dr){
		TextView tabView = new TextView(this);
				
		return ChangeViewInAct(tabView, text, dr);
		}
	
	
	private TextView ChangeViewInAct(TextView v, String text, Drawable dr){
		v.setText(text);
		v.setTextSize(12);
		v.setTextColor(Color.GRAY);
		v.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
		          
		dr.setColorFilter(0xFF888888, android.graphics.PorterDuff.Mode.MULTIPLY);
		v.setCompoundDrawablesWithIntrinsicBounds(null, dr, null, null);
		v.setBackgroundColor( Color.parseColor("#00000000") );
		
		return v;
	}
	
	
	
	
	//=================     Set Resources =============
	/**    Retrieve
	 *      Language, Username, Password, and AuthenticationFlag, Refresh rate as it was stored in preferences
	 */
	
	public Resources SetResources(){
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    LangSTR          = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);
        UserNameSTR      = mshPrefs.getString("UserNameAR", "");
	    PasswordSTR      = mshPrefs.getString("PasswordAR", "");
	    
	    	    	    
	    RefrateAR        = Integer.parseInt( mshPrefs.getString("RefrateAR", "5") );
	    
   	    conf = getResources().getConfiguration();
        conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return new Resources(getAssets(), metrics, conf);
    }
	
}