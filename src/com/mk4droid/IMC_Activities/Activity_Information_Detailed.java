/**   Activity_Information_Detailed  */
package com.mk4droid.IMC_Activities;


import com.flurry.android.FlurryAgent;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMCity_PackDemo.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;


/**
 *  Details about the application and the authors
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class Activity_Information_Detailed extends Activity implements OnClickListener{
	
	static Context ctx;
	
	//====================== On Create Activity ==================
	/** Set content view only */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        setContentView(R.layout.activity_information_detailed);
    }

    //================= onClick ======================
    /** e-mail button */
	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();

		switch(id){
		case (R.id.btSendMail):
			//----------- Data ---------------			
			Intent emailIntentQuest = new Intent(android.content.Intent.ACTION_SEND);  

			//	----------- Data ---------------			
			String aEmailListQuest[] = { "improvemycitymobile@gmail.com", };  

			emailIntentQuest.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailListQuest);  
			emailIntentQuest.putExtra(android.content.Intent.EXTRA_SUBJECT, "Improve my City");  

			emailIntentQuest.setType("plain/text");  
			emailIntentQuest.putExtra(android.content.Intent.EXTRA_TEXT,"");
			// 	-------------------------

			startActivity(Intent.createChooser(emailIntentQuest, "Send your email with:"));
			//	-------------------------------
		break;
		}
	}

	
    //=============== Flurry on Start - onStop =====================
	/** Flurry start */
    public void onStart()
    {
    	super.onStart();
    	SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);
    	
    	if (AnalyticsSW)
    		FlurryAgent.onStartSession(this, Constants_API.Flurry_Key);
    }
    
    /** Flurry stop */
    public void onPause()
    {
    	super.onPause();
    	SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);
    	
    	if (AnalyticsSW)
    		FlurryAgent.onEndSession(this);
    }
    //----------------------------------------    
	
}
