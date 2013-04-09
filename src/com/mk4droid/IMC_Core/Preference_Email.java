/** Preference_Email */
package com.mk4droid.IMC_Core;

import com.mk4droid.IMC_Activities.Activity_TabHost;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
/**
 * Create a custom preference in Setup with the role of a button that leads to a new activity, that is sending a mail with an
 * outside application, e.g. gmail.
 */
public class Preference_Email extends Preference {
//    private final String TAG = getClass().getName();

    Context ctx;
    SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(Activity_TabHost.ctx);
    
    public Preference_Email(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
    }

    public Preference_Email(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
 
   
   @Override
   protected void onClick() {
	   
	    	//----------- Data ---------------			
	   		Intent emailIntentQuest = new Intent(android.content.Intent.ACTION_SEND);  
	   
	   		//	----------- Data ---------------			
	   		String aEmailListQuest[] = { "improvemycitymobile@gmail.com", };  
	   
	   		emailIntentQuest.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailListQuest);  
	   		emailIntentQuest.putExtra(android.content.Intent.EXTRA_SUBJECT, "Improve my City");  
	   
	   		emailIntentQuest.setType("plain/text");  
	   		emailIntentQuest.putExtra(android.content.Intent.EXTRA_TEXT,"");
	   		// -------------------------
	   
	   		Activity_TabHost.ctx.startActivity(Intent.createChooser(emailIntentQuest, "Send your email with:"));
	   		//	-------------------------------
	super.onClick();
    }
}
