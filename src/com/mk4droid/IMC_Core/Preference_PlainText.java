/** Preference_PlainText */
package com.mk4droid.IMC_Core;

import com.mk4droid.IMC_Activities.Activity_TabHost;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

/**
 *   In setup include a preference outlining the version of the application 
 *
 */
public class Preference_PlainText extends Preference {
    Context ctx;
    SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(Activity_TabHost.ctx);
    
    public Preference_PlainText(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
    }

    public Preference_PlainText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
   
   @Override
   protected void onClick() {
	super.onClick();
   }
}
