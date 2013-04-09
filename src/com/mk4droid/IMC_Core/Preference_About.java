/** Preference_About */
package com.mk4droid.IMC_Core;

import com.mk4droid.IMC_Activities.Activity_Information_Detailed;
import com.mk4droid.IMC_Activities.Activity_TabHost;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

/**
 * Custom preference to add in Setup view so as to have a button which leads to a new activity 
 * with information about the app and the authors
 *
 */
public class Preference_About extends Preference {
    Context ctx;
    SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(Activity_TabHost.ctx);
    
    public Preference_About(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
    }

    public Preference_About(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
   
   @Override
   protected void onClick() {
	   Activity_TabHost.ctx.startActivity(new Intent(Activity_TabHost.ctx, Activity_Information_Detailed.class));
	   super.onClick();
    }
}
