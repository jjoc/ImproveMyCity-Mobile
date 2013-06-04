/**
 *         Activity_TabHost
 */

package com.mk4droid.IMC_Activities;

import java.util.Locale;

import com.mk4droid.IMCity_PackDemo.R;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Utils.My_System_Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

/**
 * Tabs are hosted in this FragmentActivity. (1=Main, 2=List, 3=New, 4=Filters,
 * 5=Setup)
 * 
 * @copyright Copyright (C) 2012 - 2013 Information Technology Institute
 *            ITI-CERTH. All rights reserved.
 * @license GNU Affero General Public License version 3 or later; see
 *          LICENSE.txt
 * @author Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr).
 * 
 */
public class FActivity_TabHost extends FragmentActivity implements
		OnTabChangeListener {

	/** The object that hosts all tabs */
	public static FragmentTabHost mTabHost;

	/**
	 * This context is related to the whole application. It is useful for
	 * presenting messages with Toast from everywhere in the application
	 * (READ-only).
	 */
	public static Context ctx;

	/**
	 * These resources are related mainly to the language of the GUI and they
	 * can be retrieved from the whole application for presenting localized
	 * messages (READ-only)
	 */
	public static Resources resources;

	/**
	 * Refresh rate in minutes for updating data (DEFAULT:5, Here READ-ONLY, can
	 * be modified by Activity_Setup)
	 */
	public static int RefrateAR = 5;

	/** Current active tab (1=Main, 2=List, 3=New, 4=Filters, 5=Setup) */
	public static int IndexGroup = 0;

	int tlv = Toast.LENGTH_LONG;
	public static Button btSetup;

	Configuration conf;
	String LangSTR;
	DisplayMetrics metrics;

	int NTabs = 5;
	int prevTab = 0; // previous tab

	TabSpec[] mTabSpec = new TabSpec[NTabs]; // Each Tab
	Drawable mD_Main, mD_Report, mD_Setup, mD_Filters, mD_List;

	/** Indicator for being in the first level of the fragment stack */
	public static boolean isFStack1 = true; 

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onResumeFragments()
	 */
	@Override
	protected void onResumeFragments() {
		resources = SetResources();
		btSetup.setText(resources.getString(R.string.Setup));
		super.onResumeFragments();
	}

	// ------------------- on CREATE --------------------
	/**
	 * Executed when tabhost is created
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ----------------- GUI ------------
		resources = SetResources(); // ---Load Prefs and Modify resources
									// accordingly
		setContentView(R.layout.factivity_tabhost); // ---------- Content view
		ctx = this;

		btSetup = (Button) findViewById(R.id.btSetup);
		btSetup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (Fragment_Issue_Details.mfrag_issue_details != null) {
					FragmentTransaction ft = getSupportFragmentManager()
							.beginTransaction();
					ft.remove(Fragment_Issue_Details.mfrag_issue_details);
					ft.commit();
					getSupportFragmentManager().popBackStack(null,
							FragmentManager.POP_BACK_STACK_INCLUSIVE);
				}

				v.setBackgroundColor(resources.getColor(R.color.MainThemeColor));
				startActivity(new Intent(ctx, Activity_Setup.class));
			}
		});

		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost); // Set
																			// TabHost
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

		// ---------------------------------------
		for (int i = 0; i < NTabs; i++)
			mTabSpec[i] = mTabHost.newTabSpec("tid" + Integer.toString(i));

		mD_Main = getResources().getDrawable(R.drawable.map);
		mD_List = getResources().getDrawable(R.drawable.list);
		mD_Report = getResources().getDrawable(R.drawable.plus);
		mD_Filters = getResources().getDrawable(R.drawable.filter);
		mD_Setup = getResources().getDrawable(R.drawable.setup);

		// -------------- Set icons and texts localized per tab -------------
		TextView tvhelperA = makeTabIndicatorActive(
				resources.getString(R.string.Main), mD_Main);
		TextView tvhelperB = makeTabIndicatorInActive(
				resources.getString(R.string.List), mD_List);
		TextView tvhelperC = makeTabIndicatorInActive(
				resources.getString(R.string.Report), mD_Report);
		TextView tvhelperD = makeTabIndicatorInActive(
				resources.getString(R.string.Filters), mD_Filters);
		TextView tvhelperE = makeTabIndicatorInActive(
				resources.getString(R.string.Setup), mD_Setup);

		int drPad = 2; // padding
		int topPad = 6; // margin

		tvhelperA.setCompoundDrawablePadding(drPad);
		tvhelperA.setPadding(0, topPad, 0, 0);
		tvhelperB.setCompoundDrawablePadding(drPad);
		tvhelperB.setPadding(0, topPad, 0, 0);
		tvhelperC.setCompoundDrawablePadding(drPad);
		tvhelperC.setPadding(0, topPad, 0, 0);
		tvhelperD.setCompoundDrawablePadding(drPad);
		tvhelperD.setPadding(0, topPad, 0, 0);
		tvhelperE.setCompoundDrawablePadding(drPad);
		tvhelperE.setPadding(0, topPad, 0, 0);

		mTabSpec[0].setIndicator(tvhelperA);
		mTabSpec[1].setIndicator(tvhelperB);
		mTabSpec[2].setIndicator(tvhelperC);
		mTabSpec[3].setIndicator(tvhelperD);
		mTabSpec[4].setIndicator(tvhelperE);

		// Add tabSpec to the TabHost to display
		mTabHost.addTab(mTabSpec[0], Fragment_Main.class, null);
		mTabHost.addTab(mTabSpec[1], Fragment_List.class, null);

		mTabHost.addTab(mTabSpec[2], Fragment_NewIssueA.class, null);

		mTabHost.addTab(mTabSpec[3], Fragment_Filters.class, null);
		mTabHost.addTab(mTabSpec[4], null, null); // implemented with a button
													// overlapped because there
													// was no fragment for
													// Preferences in support
													// lib v4

		mTabHost.setOnTabChangedListener(this);

	}// ---- End OnCreate ----

	@Override
	protected void onResume() {
		btSetup.setBackgroundColor(Color.BLACK);
		super.onResume();
	}

	// ============ on Destroy Application ===================
	/**
	 * Close database
	 */
	@Override
	protected void onDestroy() {

		Log.e("TabHost", "onDestroy");

		if (Service_Data.dbHandler.db.isOpen())
			Service_Data.dbHandler.db.close();

		if (My_System_Utils.isServiceRunning(
				"com.mk4droid.IMC_Services.Service_Data", ctx))
			stopService(new Intent(this, Service_Data.class));

		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		// Check current level = 1
		
		if (isFStack1)
			android.os.Process.killProcess(android.os.Process.myPid());
		
		super.onBackPressed();
	}

	// ========== Menu hard button ============
	/**
	 * Menu hard button (only to exit)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	// =========== Menu hard button pressed option =====
	/**
	 * Only exit option for the time being
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.exit:
			finish();
			break;
		}
		return true;
	}

	// ================== OnTabChange =========================
	/**
	 * Changes current activity and colors of the tabSpecs
	 */
	@Override
	public void onTabChanged(String arg0) {

		isFStack1 = true;
		
		// remove some irrelevant fragments from previous tabs

		// if issue details are in stack of Main tab then remove
		if (prevTab == 0 && Fragment_Main.frag_issue_details != null) {
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.remove(Fragment_Main.frag_issue_details);
			ft.commit();
			getSupportFragmentManager().popBackStack(null,
					FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}

		// Stack of tab list
		if (prevTab == 1 && Fragment_List.newFrag_Issue_Details != null) {

			if (Fragment_Issue_Details.newfrag_map_solo != null) {
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.remove(Fragment_Issue_Details.newfrag_map_solo);
				ft.commit();
				getSupportFragmentManager().popBackStack(null,
						FragmentManager.POP_BACK_STACK_INCLUSIVE);
			}

			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.remove(Fragment_List.newFrag_Issue_Details);
			ft.commit();
			getSupportFragmentManager().popBackStack(null,
					FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}

		// Stack of tab new Issue: Two maps v2 are not allowed, so remove the
		// newIssueMap
		if (prevTab == 2) {

			if (Fragment_NewIssueA.btProceed != null)
				Fragment_NewIssueA.btProceed.setBackgroundDrawable(resources
						.getDrawable(R.drawable.gradient_green));

			if (Fragment_NewIssueB.mfrag_nIssueB != null) {
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.hide(Fragment_NewIssueA.mfrag_nIssueB);

				ft.commit();
				getSupportFragmentManager().popBackStack(null,
						FragmentManager.POP_BACK_STACK_INCLUSIVE);
			}

			Fragment_NewIssueA.llnewissue_a.setVisibility(View.VISIBLE);
		}

		// -------- Change colors of Tabs ------
		TextView[] v = new TextView[NTabs];

		for (int i = 0; i < NTabs; i++)
			v[i] = (TextView) mTabHost.getTabWidget().getChildAt(i);

		// --------- Set Color of Tabs ---------------
		for (int i = 0; i < NTabs; i++) {
			String txt = v[i].getText().toString();
			Drawable[] dr = v[i].getCompoundDrawables();

			if (mTabHost.getCurrentTab() == i) {

				v[i] = ChangeViewAct(v[i], txt, dr[1]);

				FActivity_TabHost.IndexGroup = i;

				if (i == 2) {

					Log.e("Tab", "2");
					if (Fragment_NewIssueA.mfrag_nIssueA != null) {
						Log.e("Fragment_NewIssueA.mfrag_nIssueA.getId()", " "
								+ Fragment_NewIssueA.mfrag_nIssueA.getId());

						// Fragment_NewIssueA.mfrag_nIssueA
					}

					if (Fragment_NewIssueA.mfrag_nIssueB != null) {
						Log.e("Fragment_NewIssueA.mfrag_nIssueB.getId()", " "
								+ Fragment_NewIssueA.mfrag_nIssueB.getId());
					}

					if (Fragment_NewIssueB.mfrag_nIssueB != null) {
						Log.e("Fragment_NewIssueB.mfrag_nIssueB.getId()", " "
								+ Fragment_NewIssueB.mfrag_nIssueB.getId());
					}

				}

				// ------- Reset tab 1 to remove history -------
				// if (prevTab == 1){
				// if(Group_ListOfIssues.history.size() > 1) {
				// Group_ListOfIssues.group.back();
				// }}
				// ---------------------------------------

				prevTab = i;
			} else
				v[i] = ChangeViewInAct(v[i], txt, dr[1]);
		}
	}

	// ------------------ Colorize Tabs ----------------------------
	private TextView makeTabIndicatorActive(String text, Drawable dr) {
		TextView tabView = new TextView(this);
		return ChangeViewAct(tabView, text, dr);
	}

	private TextView ChangeViewAct(TextView v, String text, Drawable dr) {
		v.setText(text);
		v.setTextSize(10);
		v.setTextColor(Color.WHITE);
		v.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

		dr.setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);

		v.setCompoundDrawablesWithIntrinsicBounds(null, dr, null, null);
		v.setBackgroundColor(0xFF790102);

		return v;
	}

	private TextView makeTabIndicatorInActive(String text, Drawable dr) {
		TextView tabView = new TextView(this);
		return ChangeViewInAct(tabView, text, dr);
	}

	private TextView ChangeViewInAct(TextView v, String text, Drawable dr) {
		v.setText(text);
		v.setTextSize(10);
		v.setTextColor(Color.GRAY);
		v.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

		dr.setColorFilter(0xFF888888, android.graphics.PorterDuff.Mode.MULTIPLY);
		v.setCompoundDrawablesWithIntrinsicBounds(null, dr, null, null);
		v.setBackgroundColor(Color.parseColor("#00000000"));

		return v;
	}

	// ================= Set Resources =============
	/**
	 * Retrieve Language, Username, Password, and AuthenticationFlag, Refresh
	 * rate as it was stored in preferences
	 */
	public Resources SetResources() {
		SharedPreferences mshPrefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		LangSTR = mshPrefs.getString("LanguageAR",
				Constants_API.DefaultLanguage);
		RefrateAR = Integer.parseInt(mshPrefs.getString("RefrateAR", "5"));

		conf = getResources().getConfiguration();
		conf.locale = new Locale(LangSTR.substring(0, 2)); // ----- Convert
															// Greek -> el
															// ---------
		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return new Resources(getAssets(), metrics, conf);
	}
}