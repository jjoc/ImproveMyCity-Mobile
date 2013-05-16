/* Activity_NewIssueA */
package com.mk4droid.IMC_Activities;


import java.util.ArrayList;
import java.util.Locale;

import android.app.ActivityGroup;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.mk4droid.IMCity_PackDemo.R;
import com.mk4droid.IMC_Constructors.Category;
import com.mk4droid.IMC_Core.SpinnerAdapter_NewIssueCateg;
import com.mk4droid.IMC_Services.Security;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Store.Constants_API;


/**
 * First activity for submitting an issue: select category, write a title, write a description, and attach an image. 
 * Then proceed to Activity_NewIssueB.  
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class Activity_NewIssueA extends ActivityGroup{ 

	//------- System -----------
	public static Resources resources;
	DisplayMetrics metrics;
	static Context ctx;
	int tlv = Toast.LENGTH_LONG;
	
	boolean AuthFlag;
			
	//---------- Task Variables --------------
	String titleData_STR;
	String descriptionData_STR;
	String[] SpinnerArrString;
	private String usernameSTR;
	private String passwordSTR;
	static int[] SpinnerArrID;  // This contains category ids as in MySQL
	static Spinner sp;
	
	static EditText et_title,et_descr;
	static Button btAttachImage;
	TextView tv_TitleAct; 
	//static Button btTakeImage,btSelectImage;
	
	// ========== OnCreate ============
	/**
	 *  Check if user is authenticated else prohibit from submitting issue.
	 *  Set content view.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    	   
	    resources = SetResources();
	    //getLayoutInflater()
	    View viewToLoad = LayoutInflater.from(this.getParent()).inflate(R.layout.activity_newissue_a, null);
	    setContentView(viewToLoad);  
	    ctx = this;	    
	    
	    tv_TitleAct = (TextView)findViewById(R.id.tvNewIssueTitle);        
        //--------- Get Title Data ------------------
        et_title = (EditText) findViewById(R.id.etTitle_ni);
        et_descr = (EditText) findViewById(R.id.etDescription);
        
        //---     Set Categories to Spinner --------- 
        sp = (Spinner) findViewById(R.id.spinnerCateg);
        
        ArrayList<Category> mCatL_Sorted = SortCategList(Service_Data.mCategL);
        SpinnerArrString = initSpinner(mCatL_Sorted);
        
        SpinnerAdapter_NewIssueCateg adapterSP = new SpinnerAdapter_NewIssueCateg(Group_NewIssue.group,    //--- Set spinner adapter --
        		                      android.R.layout.simple_spinner_item, mCatL_Sorted);
	    
	    sp.setAdapter(adapterSP);
	    sp.setSelection(0);
	    	    
	    //-------- Take Image button -------
	    btAttachImage = (Button) findViewById(R.id.btAttach_image);
	    
	    if (Group_NewIssue.flagPictureTaken){
	       btAttachImage.setCompoundDrawablesWithIntrinsicBounds(
				Group_NewIssue.ImageThumb_DRW_S, null, null,  null);
	       btAttachImage.setPadding(5, 0, 0, 0);
	       btAttachImage.setCompoundDrawablePadding(5);
	    } else {
		    btAttachImage.setCompoundDrawablesWithIntrinsicBounds(
					android.R.drawable.ic_menu_gallery, 0, 0,  0);
	    }
	}// Endof Create
	
	//======== onBackPressed ======
	/** Hard-key back pressed */
	@Override
	public void onBackPressed() {
        finish();
		super.onBackPressed();
	}
	
	
	//============ On Resume ============
	/** Resume from changing tabs */
	@Override
	public void onResume() {
		super.onResume();

		resources = SetResources();

		tv_TitleAct.setText(resources.getString(R.string.ReportNew));
		et_title.setHint(resources.getString(R.string.STitle));
		et_descr.setHint(resources.getString(R.string.Description));
		btAttachImage.setText(resources.getString(R.string.Attach));
		
		Button btProceed = (Button) findViewById(R.id.btProceed_ni_B);
		btProceed.setText(resources.getString(R.string.Proceed));
				
		
		//AuthFlag = Security.AuthFun(usernameSTR, passwordSTR, resources, ctx);
		
		//---------- Make all Views invisible ---------------	    
		LinearLayout llnewissue = (LinearLayout) findViewById(R.id.llnewissue);

		for (int i=0; i< llnewissue.getChildCount(); i++)
			if (!AuthFlag)
				llnewissue.getChildAt(i).setVisibility(View.GONE);
			else 
				llnewissue.getChildAt(i).setVisibility(View.VISIBLE);

		//--------- Show Unauthorized message ------------
		TextView tvMes = (TextView) findViewById(R.id.tvUnauth);

		tvMes.setText(resources.getString(R.string.GoSetup));
		
		if (!AuthFlag)
			tvMes.setVisibility(View.VISIBLE);
		else
			tvMes.setVisibility(View.GONE);


		//----------- Flurry Analytics --------
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);

		if (AnalyticsSW)
			FlurryAgent.onStartSession(this, Constants_API.Flurry_Key);

	}  


	//============ onPause ============== 
	/** Pause when changing tab. Stop Flurry. */
	@Override
	public void onPause() {
		super.onPause();


		//-- Flurry Analytics --
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);

		if (AnalyticsSW)
			FlurryAgent.onEndSession(this);

	}  

	//============= Sort Category List ====================
    /** Sort categories in the list according to parent and child information */
	public ArrayList<Category> SortCategList(ArrayList<Category> InL){
		ArrayList<Category> OutL = new ArrayList<Category>(); 
		int i=0;
		while (i< InL.size()){
			if (InL.get(i)._level == 1){ // Parent
				OutL.add(InL.get(i));
				int parent_id = InL.get(i)._id; 
				int j = 0;
				while (j<InL.size()){
					if (InL.get(j)._parentid == parent_id){
						OutL.add(InL.get(j));
					}
					j=j+1;
				}
			}	  
			i=i+1;
		}
		return OutL;
	}
	  
	  
	
	//===============  initSpinner ================ 
    /** Initialize Spinner strings */
	public String[] initSpinner(ArrayList<Category> L){
		int NCategs = L.size();

		// --- Assign Strings and IDs for spinner use ----
		String[] Res     = new String[NCategs]; 
		SpinnerArrID     = new int[NCategs];  
		
		for (int i=0; i < NCategs; i++){
				Res[i]              = L.get(i)._name;   
				SpinnerArrID[i]     = L.get(i)._id;     
		}

		return Res;
	}
	
	//===========  Set Resources ==================
	/**
	 * Obtain resources from preferences 
	 * @return
	 */
	public Resources SetResources(){
	
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    String LangSTR          = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);
		
	    usernameSTR      = mshPrefs.getString("UserNameAR", "");
	    passwordSTR      = mshPrefs.getString("PasswordAR", "");
	    AuthFlag         = mshPrefs.getBoolean("AuthFlag", false);
	    
   	    Configuration conf = getResources().getConfiguration();
        conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return new Resources(getAssets(), metrics, conf);
    }
}