/** Activity_Setup */

package com.mk4droid.IMC_Activities;

import java.util.Locale;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.internal.bt;
import com.mk4droid.IMCity_PackDemo.R;
import com.mk4droid.IMC_Services.InternetConnCheck;
import com.mk4droid.IMC_Store.Constants_API;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the activity where settings can be modified. Contact information is also included. 
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Activity_Setup extends PreferenceActivity {
		
	int tlv = Toast.LENGTH_LONG;
	
    public static Resources resources;     //  for Language
    public static Context ctx;
    
    //preferenceUN, preferencePass
    
    Preference prefAccountOper, prefLang, prefDistance,
               prefRefrate, prefIssuesNo, prefFlurryAnal, prefVersion, prefAbout, prefEmail, prefReset;
    Preference CategCustomPref,CategLangPref,CategSystemPref, CategAboutPref; 

    SharedPreferences prefs;
    
    
    String LangSTR, PassSTR, UserNameSTR;

	public static String UserRealName;

	String RefrateSTR;

	String emailSTR = "";
    int UserID;
    boolean AuthFlag;

	static Button btReturn;
    
    //================ onCreate ====================
    /**
     *    Set contents of setup activity   
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        FActivity_TabHost.IndexGroup = 3;
        FActivity_TabHost.isFStack1 = false;
        
        
        //-------- Set resources ------
        resources  = setResources();

        PreferenceManager.setDefaultValues(this, R.xml.myprefs, true);
                
        //------ Load User Name and Pass from preferences
        setContentView(R.layout.activity_setup);
        addPreferencesFromResource(R.xml.myprefs);
        
        //----------- DATA --------------------
                
        CategCustomPref= findPreference("CategCustom");
        CategLangPref  = findPreference("CategLang");
        CategSystemPref   = findPreference("CategSystem");
        CategAboutPref = findPreference("CategAbout");
        prefAccountOper= findPreference("Account_Operations_IMC");
        
        
    	prefLang        = findPreference("LanguageAR");
    	prefRefrate     = findPreference("RefrateAR");
    	prefIssuesNo     = findPreference("IssuesNoAR");
    	prefFlurryAnal   = findPreference("AnalyticsSW");
    	prefDistance     = findPreference("distance_seekBar");
    	prefLang.setOnPreferenceChangeListener(prefLang_change);
    	
    	prefVersion = findPreference("Version");
    	prefAbout   = findPreference("About");
    	prefEmail   = findPreference("Email");
    	prefReset   = findPreference("Reset");
    	
    	try {
    		PackageInfo pack_inf = getPackageManager().getPackageInfo(getPackageName(), 0);
			String versionName = pack_inf.versionName;
			int versionCode = pack_inf.versionCode;
			prefVersion.setSummary(versionName +", serial code: " + Integer.toString(versionCode));
		} catch (NameNotFoundException e) {
		}
    	
    	
    	
    	btReturn = (Button)findViewById(R.id.btReturn);
    	btReturn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
                    finish();
				
			}
		});
    	
        
    }

    //============ onResume =================
    /**
     *     on resume from tab changing
     */
    @Override
    protected void onResume(){
    	super.onResume();
    	resources  = setResources();
    	
    	if (InternetConnCheck.getInstance(ctx).isOnline(ctx)){
    		prefIssuesNo.setEnabled(true);
    		prefRefrate.setEnabled(true);
    	} else {
    		prefIssuesNo.setEnabled(false);
    		prefRefrate.setEnabled(false);
    		prefDistance.setEnabled(false);
    		Toast.makeText(ctx, resources.getString(R.string.NoInternet), tlv).show();
    	}

    	//----------- Flurry Analytics --------
    	SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);

    	if (AnalyticsSW)
    		FlurryAgent.onStartSession(this, Constants_API.Flurry_Key);
    }
    
    //============= onPause =================
    /**
     *     Pausing this activity 
     */
    @Override
    public void onPause()
    {
    	super.onPause();
    	SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	    	
    	if (mshPrefs.getBoolean("AnalyticsSW", true))
    		FlurryAgent.onEndSession(this);
    }
    
    //================= prefLang_change =================================
    /**
	 *   Get language from GUI input and change widgets accordingly 
	 */
    private OnPreferenceChangeListener prefLang_change = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object NewValue) {
        
        	LangSTR     = NewValue.toString();
        	savePreferences("LanguageAR",LangSTR,"String");
        	resources   = setResources();
       
((TextView)FActivity_TabHost.mTabHost.getTabWidget().getChildAt(0)).setText(resources.getString(R.string.Main));
((TextView)FActivity_TabHost.mTabHost.getTabWidget().getChildAt(1)).setText(resources.getString(R.string.List));
((TextView)FActivity_TabHost.mTabHost.getTabWidget().getChildAt(2)).setText(resources.getString(R.string.Report));
((TextView)FActivity_TabHost.mTabHost.getTabWidget().getChildAt(3)).setText(resources.getString(R.string.Filters));
((TextView)FActivity_TabHost.mTabHost.getTabWidget().getChildAt(4)).setText(resources.getString(R.string.Setup));


			CategCustomPref.setTitle(resources.getString(R.string.CustAccount));
			CategSystemPref.setTitle(resources.getString(R.string.System));
			CategAboutPref.setTitle(resources.getString(R.string.About));
			
			prefAccountOper.setTitle(resources.getString(R.string.AccountOperations));
			prefAccountOper.setSummary(resources.getString(R.string.LoginRegisterRemindLogout));
			
			prefLang.setSummary(resources.getString(R.string.LangSel));
		    
			prefDistance.setTitle(resources.getString(R.string.ViewRange)); 
			prefDistance.setSummary(resources.getString(R.string.ViewRadius));
			
			prefRefrate.setTitle(resources.getString(R.string.Refrinter));
			prefRefrate.setSummary(resources.getString(R.string.Restartneed));
			
			prefIssuesNo.setTitle(resources.getString(R.string.IssuesNo));
			prefIssuesNo.setSummary(resources.getString(R.string.IssuesMaxNo));
			
			prefFlurryAnal.setTitle(resources.getString(R.string.Analytics));
			prefFlurryAnal.setSummary(resources.getString(R.string.HelpUs));
		
			prefVersion.setTitle(resources.getString(R.string.Version));
			
	    	prefAbout.setTitle(resources.getString(R.string.About));
	    	prefAbout.setSummary(resources.getString(R.string.Helpinformationandcredits));
	    	
	    	prefEmail.setTitle(resources.getString(R.string.Yourproposal));
	    	prefEmail.setSummary(resources.getString(R.string.Sendemailwithyourideas));
			
	    	prefReset.setTitle(resources.getString(R.string.Reset));
	    	prefReset.setSummary(resources.getString(R.string.Deleteallissuedatastoredlocallytoyourphone));
	    	
	    	btReturn.setText(resources.getString(R.string.Return));
	    	
        	return true;
        }
    };  
    
    //================== setResources ==========================
    /**
     *  Set language Resources depending on the language saved in the preferences   
     * @return resources depending on the language chosen
     */
    public Resources setResources(){

        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);     	// Get Preferences -------
        
        UserNameSTR    = prefs.getString("UserNameAR" , "");
        emailSTR       = prefs.getString("emailAR"    , "");
		PassSTR        = prefs.getString("PasswordAR" , "");
		UserRealName   = prefs.getString("UserRealName" , "");
		LangSTR        = prefs.getString("LanguageAR" , Constants_API.DefaultLanguage);
		RefrateSTR     = prefs.getString("RefrateAR"  , "");
    	    	
   	    Configuration conf = getResources().getConfiguration();
        conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return new Resources(getAssets(), metrics, conf);
    }
    
    //=============== savePreferences ====================
    /**
     * Save a value to preferences, either string or boolean
     * 
     * @param key       name of the parameters to save
     * @param value     value of the parameter to save 
     * @param type      either "String" or "Boolean" 
     */
    private void savePreferences(String key, Object value, String type){
    	SharedPreferences shPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    	SharedPreferences.Editor editor = shPrefs.edit();
    	
    	if (type.equals("String")) 
    		editor.putString(key, (String) value);
    	else 
    		editor.putBoolean(key, (Boolean) value);
    	
    	editor.commit();
    }
}