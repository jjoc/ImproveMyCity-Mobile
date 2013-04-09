/*   Activity_List */
package com.mk4droid.IMC_Activities;

import java.util.ArrayList;
import java.util.Locale;

import com.mk4droid.IMCity_Pack.R;
import com.mk4droid.IMC_Constructors.Issue;
import com.mk4droid.IMC_Constructors.IssueListItem;
import com.mk4droid.IMC_Core.Issues_ListAdapter;
import com.mk4droid.IMC_Services.Service_Data;

import android.app.ActivityGroup;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 *    View of issues as a list
 *    
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class Activity_List extends ActivityGroup {
	
	static Handler handlerBroadcastListRefresh;
	ProgressDialog dialogListRefresh;
	
	Context ctx;
	int tlv = Toast.LENGTH_LONG;
	ListView lv;
	String LangSTR = "el";
	public static Resources resources;
	
	String UserID_STR = "";
    boolean MyIssuesSW;
    int UserID = -1;
	
	boolean ClosedSW = true, OpenSW = true, AckSW  = true;
	ArrayList<IssueListItem> list_data;
	ListView lvIssues;
	TextView tvListTitle;
    //--------- List ---------	
 	Issues_ListAdapter adapterIssues;
 	SharedPreferences mshPrefs;
 	
 	//================ onCreate ======================
 	/**
 	 *    Create the list of issues
 	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	 	
	    mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    resources = setResources();
	    
	    setContentView(R.layout.activity_list);
        ctx = this;
        lvIssues = (ListView)findViewById(R.id.lvIssues);
        tvListTitle = (TextView)findViewById(R.id.tvListIssuesTitle);
        
        //--------------- Receiver for Data change ------------
	    IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN"); // DataCh
	    
	    BroadcastReceiver mReceiverDataChanged = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            	String DataChanged = intent.getStringExtra("DataChanged");
            	
            	if (DataChanged!=null){
            		onResume();
            		
            		//---------- When coming from filters ----------
            		try{
                    	Activity_Filters.dialogFiltersCh.dismiss();
                    } catch (Exception e){}
                    
                    //---------- When coming from setup --------------
                    try {
                    	dialogListRefresh.dismiss();
                    } catch (Exception e){}
                    
            	} 
            }
        };
		
        this.registerReceiver(mReceiverDataChanged, intentFilter);
        
        //----- When coming from Setup ------------
        handlerBroadcastListRefresh = new Handler() 
        {
           public void handleMessage(Message msg)
           {
               if (msg.arg1 == 2)
        		  sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("DistanceChanged", "Indeed"));
               else if (msg.arg1 == 3)
          		  sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("IssuesNoChanged", "yep"));
              
              super.handleMessage(msg);
           }
        };
   
	}
	
	//================= onResume =======================
	/** Resuming from tabChange */
	@Override
	protected void onResume() {
		super.onResume();
		
		resources = setResources();
		tvListTitle.setText(resources.getString(R.string.Issues));
		
		// == Check when coming from Setup ==
		String distanceData    = mshPrefs.getString("distanceData", "5000");
    	String distanceDataOLD = mshPrefs.getString("distanceDataOLD", "5000");
    	
    	//----- Check if IssuesNo has changed -----
    	String IssuesNoAR    =  mshPrefs.getString("IssuesNoAR", "40");
    	String IssuesNoAROLD =  mshPrefs.getString("IssuesNoAROLD", "40");
    	
        if (!distanceData.equals(distanceDataOLD)){
    		
        	//---- Show dialog Refresh ----------
        	dialogListRefresh = ProgressDialog.show(Activity_TabHost.ctx, 
        			resources.getString(R.string.Downloading),
        			resources.getString(R.string.DistanceView), true);
        	
            // ------- Broadcast Refresh through a handle
        	Message msg = new Message();
        	msg.arg1 = 2;
        	Activity_Main.handlerBroadcastRefresh.sendMessage(msg);
    		
    	} else if (!IssuesNoAR.equals(IssuesNoAROLD)){
    		
    		//---- Show dialog Refresh ----------
        	dialogListRefresh = ProgressDialog.show(Activity_TabHost.ctx, 
        			resources.getString(R.string.Downloading),
        			resources.getString(R.string.IssuesNoCh), true);
        	
            // ------- Broadcast Refresh through a handle
        	Message msg = new Message();
        	msg.arg1 = 3;
        	Activity_Main.handlerBroadcastRefresh.sendMessage(msg);
    	}
		
        //== Initialization of List == 
        list_data = new ArrayList<IssueListItem>();

        // Add each issue to the list  --------
        int NIssues = 0;
        if (Service_Data.mIssueL!=null)
             NIssues = Service_Data.mIssueL.size();
        
        
        for (int i= 0; i< NIssues; i++){

        	for (int j=0; j< Service_Data.mCategL.size(); j++){
        		//------ iterate to find category of issue and icon to display
        		if ( (Service_Data.mIssueL.get(i)._currentstatus == 1 && OpenSW) || 
        				(Service_Data.mIssueL.get(i)._currentstatus == 2 && AckSW) || 
        				(Service_Data.mIssueL.get(i)._currentstatus == 3 && ClosedSW)){

        			if (Service_Data.mIssueL.get(i)._catid == Service_Data.mCategL.get(j)._id ){
        				if (Service_Data.mCategL.get(j)._visible==1){  // Filters for Visibility

        					if (MyIssuesSW && Service_Data.mIssueL.get(i)._userid != UserID)
        						continue;

        					Issue mIssue = Service_Data.mIssueL.get(i);

        					IssueListItem mIli = new IssueListItem(	   null,  			 // bm is null
        							mIssue._id,  
        							mIssue._title,
        							mIssue._currentstatus,
        							mIssue._address,
        							mIssue._reported,
        							mIssue._votes,
        							mIssue._latitude,
        							mIssue._longitude,
        							mIssue._photo,
        							mIssue);

        					list_data.add(mIli);
        				}}}}}


        //--- Set Adapter ------------
        adapterIssues = new Issues_ListAdapter(this, R.layout.listissues_item, list_data);
        lvIssues.setAdapter(adapterIssues);


        //----- Set on Click Listener -------
        lvIssues.setOnItemClickListener(
        		new OnItemClickListener() {
        			@Override
        			public void onItemClick(AdapterView<?> parent, android.view.View view,
        					int position, long id) {

        				Intent intentClick = new Intent();
        				intentClick.putExtra("Issue", list_data.get(position)._issue);
        				intentClick.setClass(ctx, Activity_Issue_Details.class);

        				// Create the view using FirstGroup's LocalActivityManager
        				View NewView = Group_ListOfIssues.group.getLocalActivityManager()
        						.startActivity("Activity_Issue_Details", intentClick
        								.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        								.getDecorView();

        				// Again, replace the view
        				Group_ListOfIssues.group.replaceView(NewView);
        			}}
        		);
	}
	
	//============= onBackPressed =================
	/** HardKey back pressed */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}
	
	//=========== setResources =================
	/**
	 *     Set Resources such as language, user id, category filtering 
	 */
	public Resources setResources(){
		
	    LangSTR          = mshPrefs.getString("LanguageAR", "el");
	    	    
	    OpenSW                = mshPrefs.getBoolean("OpenSW", true);
    	AckSW                 = mshPrefs.getBoolean("AckSW", true);
    	ClosedSW              = mshPrefs.getBoolean("ClosedSW", true);
	    
        UserID_STR       = mshPrefs.getString("UserID_STR", "");
    	MyIssuesSW       = mshPrefs.getBoolean("MyIssuesSW", false);
	    
        if (UserID_STR.length()>0) 
        	UserID = Integer.parseInt(UserID_STR);
        else 
        	UserID = -1;
	    
   	    Configuration conf = getResources().getConfiguration();
        conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return new Resources(getAssets(), metrics, conf);
    }
	
}
