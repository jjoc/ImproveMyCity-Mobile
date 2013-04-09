/**
 *    Group_ListOfIssues
 */

package com.mk4droid.IMC_Activities;

import java.util.ArrayList;

import com.mk4droid.IMC_Store.Constants_API;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Group containing the sequence of activities for the tab list
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class Group_ListOfIssues extends ActivityGroup {
	
	
	static ArrayList<View> history;
	static Group_ListOfIssues group;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	
	      history = new ArrayList<View>();
	      group = this;
	      Activity_TabHost.IndexGroup = 1;
	      
        // Start the root activity within the group and get its view
	      View view = getLocalActivityManager().startActivity("Activity_List", new
	    	      							Intent(this,Activity_List.class)
	    	      							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
	    	                                .getDecorView();

        // Replace the view of this ActivityGroup
	      replaceView(view);
	}

	//============ replaceView ====================
	/**
	 * replace previous activity with next activity
	 * 
	 * @param v
	 */
	public void replaceView(View v) {
		history.add(v);
		setContentView(v) ;
	}

	//============= back ===================
	/**
	 *  Go back one activity
	 */
	public void back() {
		if(history.size() > 1) {
			history.remove(history.size()-1);
			setContentView((View) history.get(history.size()-1)  );
		}else {
			Log.d(Constants_API.TAG,"Group_ListOfIssues:Destroy");
			finish();
		}
	}

	//============= onBackPressed ===============
	/**
	 * On hard-key back pressed go back one Activity
	 */
	@Override
	public void onBackPressed() {
		Group_ListOfIssues.group.back();
		return;
	}
}
