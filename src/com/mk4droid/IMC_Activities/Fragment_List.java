// Fragment_List
package com.mk4droid.IMC_Activities;

import java.util.ArrayList;
import java.util.Locale;

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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.mk4droid.IMC_Constructors.Issue;
import com.mk4droid.IMC_Constructors.IssueListItem;
import com.mk4droid.IMC_Core.Issues_ListAdapter;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMCity_PackDemo.R;

/**
 * Show a list containing all issues (Filtered)
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 * 
 */
public class Fragment_List extends Fragment {

	static Handler handlerBroadcastListRefresh;
	ProgressDialog dialogListRefresh;

	public static Context ctx;
	int tlv = Toast.LENGTH_LONG;
	ListView lv;
	String LangSTR;
	public static Resources resources;

	String UserID_STR = "";
	boolean MyIssuesSW;
	int UserID = -1;

	boolean ClosedSW = true, OpenSW = true, AckSW  = true;
	ArrayList<IssueListItem> list_data;
	public static ListView lvIssues;
	//TextView tvListTitle;
	//--------- List ---------	
	Issues_ListAdapter adapterIssues;
	SharedPreferences mshPrefs;

	public static View vFragment_List;
	public static Fragment mFragment_List;
	
	BroadcastReceiver mReceiverDataChanged;
	public static Fragment_Issue_Details newFrag_Issue_Details;
	
	//================ onCreate ======================
	/**
	 *    Create the list of issues
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.e("Fragment_List","onCreate");
		
		mshPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		resources = setResources();
	}
	
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onDestroyView()
	 */
	@Override
	public void onDestroyView() {
		
		Log.e("Fragment_List","onDestroyView");
		
		
		if (Fragment_SoloMap.gmapSolo!=null)
			Fragment_SoloMap.gmapSolo.setMyLocationEnabled(false);
		
		ctx.unregisterReceiver(mReceiverDataChanged);
		super.onDestroyView();
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		FActivity_TabHost.isFStack1 = true;
		
		Log.e("Fragment_List","onCreateView");
		
		FActivity_TabHost.IndexGroup = 1;
		
		if (vFragment_List != null) {
	        ViewGroup parent = (ViewGroup) vFragment_List.getParent();
	        if (parent != null)
	            parent.removeView(vFragment_List);
	    }
		
	    try {
	    	if (vFragment_List == null) 
	    		vFragment_List = inflater.inflate(R.layout.fragment_list, container, false);
	    } catch (InflateException e) {
	        /* map is already there, just return view as it is */
	    }
		
		
	    mFragment_List = this;
	    
	    ctx = mFragment_List.getActivity(); 
		
		//------------------------------------
		lvIssues    = (ListView)vFragment_List.findViewById(R.id.lvIssues);

		//--------------- Receiver for Data change ------------
		IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN"); // DataCh

		mReceiverDataChanged = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {

				String DataChanged = intent.getStringExtra("DataChanged");

				if (DataChanged!=null){
					onResume();

					//---------- When coming from filters ----------
					try{
						Fragment_Filters.dialogFiltersCh.dismiss();
					} catch (Exception e){}

					//---------- When coming from setup --------------
					try {
						dialogListRefresh.dismiss();
					} catch (Exception e){}

				} 
			}
		};

		getActivity().registerReceiver(mReceiverDataChanged, intentFilter);

		//----- When coming from Setup ------------
		handlerBroadcastListRefresh = new Handler() 
		{
			public void handleMessage(Message msg)
			{
				if (msg.arg1 == 2)
					getActivity().sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("DistanceChanged", "Indeed"));
				else if (msg.arg1 == 3)
					getActivity().sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("IssuesNoChanged", "yep"));

				super.handleMessage(msg);
			}
		};
		
		
		
		
		resources = setResources();
		//tvListTitle.setText(resources.getString(R.string.Issues));

		// == Check when coming from Setup ==
		String distanceData    = mshPrefs.getString("distanceData", "5000");
		String distanceDataOLD = mshPrefs.getString("distanceDataOLD", "5000");

		//----- Check if IssuesNo has changed -----
		String IssuesNoAR    =  mshPrefs.getString("IssuesNoAR", "40");
		String IssuesNoAROLD =  mshPrefs.getString("IssuesNoAROLD", "40");

		if (!distanceData.equals(distanceDataOLD)){

			//---- Show dialog Refresh ----------
			dialogListRefresh = ProgressDialog.show(ctx, 
					resources.getString(R.string.Downloading),
					resources.getString(R.string.DistanceView), true);

			// ------- Broadcast Refresh through a handle
			Message msg = new Message();
			msg.arg1 = 2;
			//Activity_Main.CentralMap.handlerBroadcastRefresh.sendMessage(msg);

		} else if (!IssuesNoAR.equals(IssuesNoAROLD)){

			//---- Show dialog Refresh ----------
			dialogListRefresh = ProgressDialog.show(ctx, 
					resources.getString(R.string.Downloading),
					resources.getString(R.string.IssuesNoCh), true);

			// ------- Broadcast Refresh through a handle
			Message msg = new Message();
			msg.arg1 = 3;
			//Activity_Main.CentralMap.handlerBroadcastRefresh.sendMessage(msg);
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
		adapterIssues = new Issues_ListAdapter(getActivity(), R.layout.listissues_item, list_data);
		lvIssues.setAdapter(adapterIssues);


		//----- Set on Click Listener -------
		lvIssues.setOnItemClickListener(
				new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, android.view.View view,
							int position, long id) {
						
						 // Instantiate a new fragment.
				        newFrag_Issue_Details = new Fragment_Issue_Details();
				        
				        Bundle args = new Bundle();
			            args.putInt("issueId", Integer.parseInt( list_data.get(position)._id.substring(1) ));
			            newFrag_Issue_Details.setArguments(args);
			            
				        // Add the fragment to the activity, pushing this transaction
				        // on to the back stack.
				        FragmentTransaction ft = getFragmentManager().beginTransaction();
				        ft.replace(R.id.llIssues, newFrag_Issue_Details,"FTAG_ISSUE_DETAILS_LIST");
				        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				        ft.addToBackStack(null);
				        ft.commit();
					}}
				);
					
		
		return vFragment_List;

		
	}

	
	//=========== setResources =================
	/**
	 *     Set Resources such as language, user id, category filtering 
	 */
	public Resources setResources(){

		LangSTR          = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);

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
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return new Resources(getActivity().getAssets(), metrics, conf);
	}

}
