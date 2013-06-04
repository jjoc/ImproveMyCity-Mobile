// Fragment_Filters
package com.mk4droid.IMC_Activities;

import java.util.ArrayList;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.flurry.android.FlurryAgent;
import com.mk4droid.IMC_Core.FilterCateg_ExpandableListAdapter;
import com.mk4droid.IMC_Services.DatabaseHandler;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMCity_PackDemo.R;

/**
 *  This fragment contains the fiterling options for the issues 
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 * 
 */
public class Fragment_Filters extends Fragment implements OnCheckedChangeListener {
	
	
	/** A Flag to indicated whether something has changed in Filters */
	public static boolean FiltersChangedFlag = false;
	static boolean OpenSW, AckSW, ClosedSW, MyIssuesSW;
	
	/** Object for inflating views */
	public static LayoutInflater FiltInflater;
	
	static ProgressDialog dialogFiltersCh;
	
	static Resources resources;
	

	int tlv = Toast.LENGTH_LONG;
	String LangSTR;
	Button btSelectAllFilters, btReverseAllFilters;

	/** Context of this activity */
	public static Context ctx;

	/** The expandable listview object */
	public static ExpandableListView elv;
	FilterCateg_ExpandableListAdapter adapter_explist;

	int NParents; // number parents (the same with groups)

	/** The parental categories */
	public static String[] groups; // = { "People Names", "Dog Names", "Cat Names", "Fish Names" };

	/** Is parental category checked or not */
	public static boolean[] groups_check_values; // = {false, true, true, true};

	/** Icons of parental categories */
	public static Drawable[] groups_icon_values;

	/** category id of parental group */
	public static int[] groups_id;

	/** Children categories */
	public static String[][] children;

	/** id of children categories */
	public static int[][] children_id;

	/** is children category checked */
	public static boolean[][] children_check_values;

	/** Icon of children category */
	public static Drawable[][] children_icon_values;

	public static ArrayList<ArrayList<String>>  ChildrenL;

	//-- State Categs Vars 
	CheckBox chb_Open,chb_Ack,chb_Closed,chb_MyIssues;
	

	int icon_on = android.R.drawable.checkbox_on_background;
	int icon_off = android.R.drawable.checkbox_off_background;

	View v;
	
	//========================== OnCreate =================================
	/** 
	 *    Create an expandable list of filters from database categories contents
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		FActivity_TabHost.isFStack1 = true;
	}
	
	
	/**
	 *          OnCreateView 
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        
		Log.e("Filters","onCreateView");
		
		FActivity_TabHost.IndexGroup = 3;
		
		
		v = inflater.inflate(R.layout.fragment_filters, container, false);
		
		
		
		ctx = this.getActivity();
		
		resources = setResources();
		
		dialogFiltersCh = new ProgressDialog(ctx);
		
		FiltInflater = inflater;
		
		//--------- Categories: Separate Parents (or Groups) from Children ----------------
		int NCategs = Service_Data.mCategL.size();
		ArrayList<String>  ParentsL         = new ArrayList<String>();
		ArrayList<Integer> ParentsL_index   = new ArrayList<Integer>();
		ArrayList<Boolean> ParentsL_values  = new ArrayList<Boolean>();
		ArrayList<Bitmap>  ParentsL_Bitmaps = new ArrayList<Bitmap>();

		// Find Parents 
		for (int i=0; i < NCategs; i ++ ){
			if (Service_Data.mCategL.get(i)._level == 1){
				ParentsL.add(Service_Data.mCategL.get(i)._name);
				ParentsL_values.add(Service_Data.mCategL.get(i)._visible==1);
				ParentsL_index.add(Service_Data.mCategL.get(i)._id);
				byte[] ic = Service_Data.mCategL.get(i)._icon;
				ParentsL_Bitmaps.add(BitmapFactory.decodeByteArray(ic, 0, ic.length));
			}
		}

		NParents = ParentsL_values.size();
		groups              = new String[NParents];
		groups_check_values = new boolean[NParents];
		groups_icon_values  = new Drawable[NParents];
		groups_id           = new int[NParents];

		for (int i=0; i<NParents; i++){
			groups[i]              = ParentsL.get(i);
			groups_check_values[i] = ParentsL_values.get(i);
			
			groups_icon_values[i]  = new BitmapDrawable(ParentsL_Bitmaps.get(i));
			groups_id[i]           = ParentsL_index.get(i);
		}

		//------------- Children -------------------------------
		ChildrenL = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<Boolean>> ChildrenC = new ArrayList<ArrayList<Boolean>>();
		ArrayList<ArrayList<Drawable>> ChildrenD = new ArrayList<ArrayList<Drawable>>();
		ArrayList<ArrayList<Integer>>  ChildrenID = new ArrayList<ArrayList<Integer>>();

		for (int iParentSerial=0; iParentSerial<NParents; iParentSerial ++ ){
			int IndexParent = ParentsL_index.get(iParentSerial);

			ChildrenL.add(new ArrayList<String>()); 
			ChildrenC.add(new ArrayList<Boolean>());
			ChildrenD.add(new ArrayList<Drawable>());
			ChildrenID.add(new ArrayList<Integer>());


			for (int i=0; i < NCategs; i ++ ){
				if (Service_Data.mCategL.get(i)._parentid == IndexParent){
					ChildrenL.get(iParentSerial).add(Service_Data.mCategL.get(i)._name);
					ChildrenC.get(iParentSerial).add(Service_Data.mCategL.get(i)._visible==1);

					byte[] ic = Service_Data.mCategL.get(i)._icon;

					ChildrenD.get(iParentSerial).add(new BitmapDrawable(
							BitmapFactory.decodeByteArray(ic, 0, ic.length)));

					ChildrenID.get(iParentSerial).add(Service_Data.mCategL.get(i)._id);
				}
			}
		}

		children              = new String[NParents][];
		children_check_values = new boolean[NParents][];
		children_icon_values  = new Drawable[NParents][];        
		children_id           = new int[NParents][];

		for (int i=0; i < NParents; i ++ ){
			int NChildren = ChildrenL.get(i).size();
			children[i]              = new String[NChildren];
			children_check_values[i] = new boolean[NChildren];
			children_icon_values[i]  = new Drawable[NChildren];
			children_id[i]            = new int[NChildren];

			for (int j=0; j < NChildren; j++){
				children[i][j]             = ChildrenL.get(i).get(j).toString();
				children_check_values[i][j]= ChildrenC.get(i).get(j);  
				children_icon_values[i][j] = ChildrenD.get(i).get(j);
				children_id[i][j]          = ChildrenID.get(i).get(j);
			}
		}

		//------------
		

		btSelectAllFilters = (Button) v.findViewById(R.id.btSelectAllFilters);
		btReverseAllFilters= (Button) v.findViewById(R.id.btReverseAllFilters);

		//------------ Set All other categories --------
		elv     = (ExpandableListView) v.findViewById(R.id.elvMain);
		adapter_explist = new FilterCateg_ExpandableListAdapter();

		// -------- Exp List View -----------
		elv.setAdapter(adapter_explist);

		//----------- Radio button for State Categories ---------
		chb_Open   = (CheckBox)v.findViewById(R.id.chb_Open);
		chb_Ack    = (CheckBox)v.findViewById(R.id.chb_Ack);
		chb_Closed = (CheckBox)v.findViewById(R.id.chb_Closed);
		chb_MyIssues = (CheckBox)v.findViewById(R.id.chb_MyIssues);

		chb_Open.setChecked(OpenSW);
		chb_Ack.setChecked(AckSW);    
		chb_Closed .setChecked(ClosedSW);
		chb_MyIssues .setChecked(MyIssuesSW);

		chb_Open.setOnCheckedChangeListener(this);
		chb_Ack.setOnCheckedChangeListener(this);
		chb_Closed.setOnCheckedChangeListener(this);
		chb_MyIssues.setOnCheckedChangeListener(this);

		chb_Open.setText(resources.getString(R.string.OpenIssue));
		chb_Ack.setText(resources.getString(R.string.AckIssue));
		chb_Closed.setText(resources.getString(R.string.ClosedIssue));
		chb_MyIssues.setText(resources.getString(R.string.MyIssues));

		savePreferences("ClosedSW", chb_Closed.isChecked(), "boolean");
		savePreferences("OpenSW", chb_Open.isChecked(), "boolean");
		savePreferences("AckSW", chb_Ack.isChecked(), "boolean");
		savePreferences("MyIssuesSW", chb_MyIssues.isChecked(), "boolean");


		//--------------- Button Reverse All Filters -------------

		btReverseAllFilters.setOnClickListener( new OnClickListener(){

			@Override
			public void onClick(View arg0) {

				FiltersChangedFlag = true;

				//--------- Set DB ----------------
				DatabaseHandler dbHandler = new DatabaseHandler(ctx);
				for (int i=0; i < NParents; i ++ ){
					int NChildren = ChildrenL.get(i).size();
					groups_check_values[i] = !groups_check_values[i]  ;
					dbHandler.setCategory(groups_id[i], groups_check_values[i]?1:0); // Send as integer

					for (int j=0; j < NChildren; j++){
						children_check_values[i][j]= !children_check_values[i][j];  
						dbHandler.setCategory(children_id[i][j], children_check_values[i][j]?1:0); // Send as integer
					}
				}
				dbHandler.db.close();

				//--------- Set GUI ----------------
				adapter_explist = new FilterCateg_ExpandableListAdapter();

				elv.setAdapter(adapter_explist); 
			}
		});

		//--------------- Button Select All Filters -------------


		//------ Check to see if all inactive --------
		int sumVisible = 0;
		for (int i=0; i<NCategs; i++ )
			sumVisible += Service_Data.mCategL.get(i)._visible;

		if (sumVisible > 0)
			btSelectAllFilters.setTag(false);
		else 
			btSelectAllFilters.setTag(true);

		//-------------
		btSelectAllFilters.setOnClickListener( new OnClickListener(){

			@Override
			public void onClick(View arg0) {

				FiltersChangedFlag = true;

				//----- Set DB ---------
				DatabaseHandler dbHandler = new DatabaseHandler(ctx);
				for (int i=0; i < NParents; i ++ ){
					int NChildren = ChildrenL.get(i).size();

					groups_check_values[i] = (Boolean) btSelectAllFilters.getTag()  ;

					dbHandler.setCategory(groups_id[i], groups_check_values[i]?1:0); // Send as integer

					for (int j=0; j < NChildren; j++){
						children_check_values[i][j]= (Boolean) btSelectAllFilters.getTag();  
						dbHandler.setCategory(children_id[i][j], children_check_values[i][j]?1:0); // Send as integer
					}
				}

				dbHandler.db.close();
				btSelectAllFilters.setTag( !(Boolean) btSelectAllFilters.getTag() );

				//------ Set GUI -----
				adapter_explist = new FilterCateg_ExpandableListAdapter();
				elv.setAdapter(adapter_explist); 



			}
		});
		
		return v;
	}//--------------------- End of CreateView  -----------------

	//========================== OnDestroy =================================
//	@Override
//	protected void onDestroy() {
//		try{
//			dialogFiltersCh.dismiss();
//		} catch (Exception e){}
//		super.onDestroy();
//	}

	

	//========================== onCheckedChanged =================================
	/**      Save to preferences the state of Issue Status filters and myIssues filter */
	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {

		FiltersChangedFlag = true;

		savePreferences("ClosedSW", chb_Closed.isChecked(), "boolean");
		savePreferences("OpenSW", chb_Open.isChecked(), "boolean");
		savePreferences("AckSW", chb_Ack.isChecked(), "boolean");
		savePreferences("MyIssuesSW", chb_MyIssues.isChecked(), "boolean");

	}

	

	//========================== savePreferences =================================
	/**
	 * Save preferences
	 * 
	 * @param key    the name of the preference
	 * @param value  the value of the preference
	 * @param type either "Boolean" or "String"
	 */
	private void savePreferences(String key, Object value, String type){
		SharedPreferences shPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);

		//SharedPreferences shPrefs = getSharedPreferences("myprefs",MODE_PRIVATE);
		SharedPreferences.Editor editor = shPrefs.edit();

		if (type.equals("String")) 
			editor.putString(key, (String) value);
		else 
			editor.putBoolean(key, (Boolean) value);

		editor.commit();
	}

	
	//====================== SetResources ============================
	/**
	 * set resources as saved in preferences from previous session, e.g. Language, Open, Closed filters 
	 * 
	 * @return the current resources depending on the language chosen
	 */
	public Resources setResources(){
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String LangSTR          = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);
		OpenSW                = mshPrefs.getBoolean("OpenSW", true);
		AckSW                 = mshPrefs.getBoolean("AckSW", true);
		ClosedSW              = mshPrefs.getBoolean("ClosedSW", true);
		MyIssuesSW            = mshPrefs.getBoolean("MyIssuesSW", false);

		Configuration conf = getActivity().getResources().getConfiguration();
		conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return new Resources(ctx.getAssets(), metrics, conf);
	}

	
	
	//========================== OnPause =================================
	@Override
	public void onPause() {

		//-----  Broadcast Filters have changed
		if (FiltersChangedFlag)
			ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("FiltersChanged", "ok"));

		//---------- Flurry Analytics -------------    	
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);

		if (AnalyticsSW)
			FlurryAgent.onEndSession(ctx);

		super.onPause();
	}
	
	
	//========================== onResume =================================
	/** On resuming from tabChange */
	@Override
	public void onResume() {
		super.onResume();

		FiltersChangedFlag = false;

//		resources = setResources();
//		chb_Open.setText(resources.getString(R.string.OpenIssue));
//		chb_Ack.setText(resources.getString(R.string.AckIssue));
//		chb_Closed.setText(resources.getString(R.string.ClosedIssue));
//		chb_MyIssues.setText(resources.getString(R.string.MyIssues));
//
//		tvFiltTitle_Issues.setText(resources.getString(R.string.Filters));
//		btSelectAllFilters.setText(resources.getString(R.string.SelAll));
//		btReverseAllFilters.setText(resources.getString(R.string.Reverse));

		//---------- Flurry Analytics --------------        
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);

		if (AnalyticsSW)
			FlurryAgent.onStartSession(ctx, Constants_API.Flurry_Key);


	}
	
}
