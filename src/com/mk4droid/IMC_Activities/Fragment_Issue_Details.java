//    Activity_Issue_Details
package com.mk4droid.IMC_Activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import com.flurry.android.FlurryAgent;
import com.mk4droid.IMCity_PackDemo.R;
import com.mk4droid.IMC_Constructors.Comment;
import com.mk4droid.IMC_Constructors.Issue;
import com.mk4droid.IMC_Constructors.IssuePic;
import com.mk4droid.IMC_Services.DatabaseHandler;
import com.mk4droid.IMC_Services.Download_Data;
import com.mk4droid.IMC_Services.InternetConnCheck;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Services.Service_Location;
import com.mk4droid.IMC_Services.Upload_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Utils.My_Date_Utils;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;


import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.ZoomButton;

/**
 * Show issue details.
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Fragment_Issue_Details extends Fragment {

    boolean HasVotedSW, OwnIssue;
    DatabaseHandler dbHandler;
    TableLayout TabComments;

    LayoutParams params;
	String LangSTR = "en";
	String UserNameSTR, PasswordSTR, IssuesNoSTR, distanceDataSTR;
	boolean AuthFlag;
	Resources resources;
	Context ctx;
	Issue mIssue;
	Bitmap bmI = null;
	String UserID_STR;
	private String UserRealName;
	int issueId;
	
	/** Fragment for viewing a single issue*/
	public static Fragment_SoloMap newfrag_map_solo;
	
	/**  The view of this fragment  */
	public static View vfrag_issue_details;
	
	/** This Fragment */
	public static Fragment mfrag_issue_details;
	
	//================= onCreate ===========================
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		issueId = getArguments().getInt("issueId"); // id of the issue
				
		for (int i = 0; i<Service_Data.mIssueL.size(); i++){
			if (issueId == Service_Data.mIssueL.get(i)._id)
				mIssue = Service_Data.mIssueL.get(i);
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			
		FActivity_TabHost.isFStack1 = false;
		
       	vfrag_issue_details = inflater.inflate(R.layout.fragment_issue_details, container, false);
		ctx = vfrag_issue_details.getContext();
		resources = setResources();
		mfrag_issue_details = this;
		
		//--------------------------------------------

		// set title
		final int IssueID =  mIssue._id;

		String IssueID_STR = Integer.toString(IssueID);

		TextView tvTit = new TextView(ctx);
		tvTit.setText("# "+ IssueID_STR + " " + mIssue._title);
		tvTit.setTextSize(24);
		tvTit.setTextColor(Color.BLACK);
		tvTit.setPadding(5, 5, 0, 5);

	
		LinearLayout ll = (LinearLayout) vfrag_issue_details.findViewById(R.id.llissue_details);
		ll.addView(tvTit, 0);

		//================  STATUS ================ 	  
		int CurrStat = mIssue._currentstatus;

		ImageView imvArrAck     = (ImageView) vfrag_issue_details.findViewById(R.id.ImageViewArrAck);
		TextView tvAck          = (TextView ) vfrag_issue_details.findViewById(R.id.TextViewAcknowledged);
		ImageView imvArrClose   = (ImageView) vfrag_issue_details.findViewById(R.id.ImageViewArrClosed); 
		TextView tvClosed       = (TextView ) vfrag_issue_details.findViewById(R.id.TextViewClosed); 
		TextView tvStatus       = (TextView ) vfrag_issue_details.findViewById(R.id.textViewStatusContent);

		if (CurrStat == 2){
			imvArrAck.setImageResource(R.drawable.arrow_ack);
			tvAck.setTextColor(Color.parseColor("#F2B21C"));

			tvStatus.setText(resources.getString(R.string.AckIssue));
			tvStatus.setTextColor(Color.parseColor("#F2B21C"));
		} else if (CurrStat == 3){
			imvArrAck.setImageResource(R.drawable.arrow_ack);
			tvAck.setTextColor(Color.parseColor("#F2B21C"));
			imvArrClose.setImageResource(R.drawable.arrow_closed);
			tvClosed.setTextColor(Color.parseColor("#1F95AF"));

			tvStatus.setText(resources.getString(R.string.ClosedIssue));
			tvStatus.setTextColor(Color.parseColor("#1F95AF"));
		}

	
		// ==============  Set Views Objects ================
		TextView tvCateg      = (TextView ) vfrag_issue_details.findViewById(R.id.textViewCategContent);
		TextView tvAddr       = (TextView ) vfrag_issue_details.findViewById(R.id.textViewAddressContent);
		TextView tvReportedby = (TextView ) vfrag_issue_details.findViewById(R.id.textViewReportedbyContent);
		TextView tvViewed     = (TextView ) vfrag_issue_details.findViewById(R.id.textViewViewedContent);
		TextView tvDescription= (TextView ) vfrag_issue_details.findViewById(R.id.textViewDescription);
		TextView tvVotes      = (TextView ) vfrag_issue_details.findViewById(R.id.textViewVotes);

		Button btNotifyReg   = (Button) vfrag_issue_details.findViewById(R.id.btNotifyRegister);

		btNotifyReg.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {

				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.remove(mfrag_issue_details);
				ft.commit();
				getChildFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);  


				FActivity_TabHost.btSetup.performClick();
			}
		});



			
		//---- find Category name
		int CatID = mIssue._catid;

		int iCatSerial = 0;
		boolean flagfound = false; 

		while(!flagfound){
			if (Service_Data.mCategL.get(iCatSerial)._id == CatID)
				flagfound = true;
			else 
				iCatSerial += 1;
		}

		tvCateg.setText(Service_Data.mCategL.get(iCatSerial)._name);  
		//--------------	
		tvAddr.setText(mIssue._address);

		//------------- Reported by Author and XX days ago ----------
		String TimeStampRep = mIssue._reported;

		TimeStampRep = TimeStampRep.replace("-", "/");

		tvReportedby.setText(" " + 	mIssue._username + " " 
				                 + My_Date_Utils.SubtractDate(TimeStampRep) + " " +  
				                   resources.getString(R.string.ago));

		//------------- Viewed --------
		tvViewed.setText(Integer.toString(mIssue._hits));

		//----------- Description ------
		if (!mIssue._description.equals(""))
			tvDescription.setText(mIssue._description);
		else {
			tvDescription.setVisibility(View.GONE);
			TextView tvTitleDescr = (TextView)vfrag_issue_details.findViewById(R.id.tvDescrTitle);
			tvTitleDescr.setVisibility(View.GONE);
		}

		//----------- Image -----------
		dbHandler = new DatabaseHandler(ctx);
		ImageView imvFull = (ImageView) vfrag_issue_details.findViewById(R.id.imvIssue_Full);
		imvFull.setAdjustViewBounds(true);

		IssuePic issuepic = dbHandler.getIssuePic(mIssue._id);
		
		byte[] bm_byte    = issuepic._IssuePicData;
		if (bm_byte!=null){
			// 1. Image exists in db
			bmI        = BitmapFactory.decodeByteArray(bm_byte, 0, bm_byte.length);
			
			imvFull.setImageBitmap(bmI);
		} else {

			//------- Try to download from internet --------------  
			if (InternetConnCheck.getInstance(ctx).isOnline(ctx) && !mIssue._photo.equals("null")){
				// ok downloaded from internet 
				try {
					mIssue._photo = mIssue._photo.replaceFirst("/thumbs", "");

					String URL_STR = Constants_API.COM_Protocol + Constants_API.ServerSTR + Constants_API.remoteImages +  mIssue._photo;
					byte[] bmBytes = Download_Data.Down_Image(URL_STR);

					bmI = BitmapFactory.decodeByteArray(bmBytes, 0, bmBytes.length);
					imvFull.setImageBitmap(bmI);

					dbHandler.addUpdIssuePic(mIssue._id, bmBytes);

				} catch (IOException e) {
					e.printStackTrace();
				}
			} else { // Internet Not available or no photo exists
				imvFull.setVisibility(View.GONE);
			}
		}


		dbHandler.db.close();
		
		// ------ Enable or disable commenting feature ----------
		if (AuthFlag){

			// -------- Enable Commenting
			EditText etComment = (EditText) vfrag_issue_details.findViewById(R.id.etComment);       	  
			etComment.setVisibility(View.VISIBLE);
			
			Button btComment =  (Button) vfrag_issue_details.findViewById(R.id.btAddComment);
			btComment.setVisibility(View.VISIBLE);

			// --------- Vanish the hint to login
			TextView tvNotifyReg = (TextView ) vfrag_issue_details.findViewById(R.id.tvNotifyRegister);
			tvNotifyReg.setVisibility(View.GONE);

			btNotifyReg.setVisibility(View.GONE);
			
			btComment.setOnClickListener(new View.OnClickListener() {

				public void onClick(View arg0) {
					//================ SEND COMMENT TO DB ===============
					EditText etComment = (EditText) vfrag_issue_details.findViewById(R.id.etComment);
					String CommSTR = etComment.getText().toString();
					
					if ( CommSTR.length()>0){
						Upload_Data.SendCommentStreaming(IssueID, Integer.parseInt(UserID_STR), CommSTR, FActivity_TabHost.ctx,
								UserNameSTR, PasswordSTR);

						// ==== AddCommentOnIssueView(Activity_Main.UserNameSTR, CommSTR); ===		        	 
						// ------------ Inflate Child --------
						View ChildComm = LayoutInflater.from(FActivity_TabHost.ctx).inflate(
								R.layout.issue_comment_layout,null);  

						// ----------- Set comment title and content -------
						TextView CommTitle = (TextView) ChildComm.findViewById(R.id.tvTitleComment);
						CommTitle.setText(resources.getString(R.string.CommentCreat) + 
								" 0 minutes ago by " + UserRealName);

						TextView CommContent = (TextView) ChildComm.findViewById(R.id.tvContentComment);
						CommContent.setText(CommSTR);

						//-------------- Add View -----------
						TabComments.addView(ChildComm, 0, params);

						// ----------- Clear comment edit text -----
						etComment.setText("");

						//-------------- Update Table of Comments ------
						//  					  dbHandler = new DatabaseHandler(Activity_Main.ctx);
						//  					  dbHandler.AddUpdComments();
						//  					  dbHandler.db.close();
					}
					//===================================================
				}
			});

	  
		} // AuthFlag 

		

		ZoomButton btZoom = (ZoomButton) vfrag_issue_details.findViewById(R.id.btzIssue);

		if (bmI == null){
			btZoom.setVisibility(View.GONE);
		}

		btZoom.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				
				// Phone orientation 
				Configuration conf = getResources().getConfiguration();

				int ort = conf.orientation;
				int bmwidth  = bmI.getWidth();
				int bmheight = bmI.getHeight();

				String imgRot ="";
				if (bmwidth > bmheight)
					imgRot = "LANDSCAPE";
				else 
					imgRot = "PORTRAIT";


				DisplayMetrics metrics = new DisplayMetrics(); // screen size
				getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

				Bitmap bmIscaled = bmI;
				if (ort == Configuration.ORIENTATION_PORTRAIT && imgRot.equals("PORTRAIT")){
					bmIscaled = Bitmap.createScaledBitmap(bmI, 
							metrics.widthPixels - 40, 
							metrics.heightPixels- 40, true);
				} else if (ort == Configuration.ORIENTATION_PORTRAIT && imgRot.equals("LANDSCAPE")){
					bmIscaled = bmI;
				} else if (ort == Configuration.ORIENTATION_LANDSCAPE && imgRot.equals("PORTRAIT")){
					bmIscaled = bmI;
				} else if (ort == Configuration.ORIENTATION_LANDSCAPE && imgRot.equals("LANDSCAPE")){
					bmIscaled = Bitmap.createScaledBitmap(bmI, 
							metrics.widthPixels  - 100, 
							metrics.heightPixels - 50, true);
				}

				Dialog dialog = null;
				if (FActivity_TabHost.IndexGroup == 0)
					dialog = new Dialog(ctx);
				else if (FActivity_TabHost.IndexGroup == 1) 
					dialog = new Dialog(FActivity_TabHost.ctx);
				
				dialog.setContentView(R.layout.custom_dialog);
				//dialog.setTitle("Custom Dialog");
				ImageView image = (ImageView) dialog.findViewById(R.id.image);
				image.setImageBitmap(bmIscaled);
				dialog.setCancelable(true);
				dialog.closeOptionsMenu();
				dialog.show();
			}});  
		



		// Votes 
		tvVotes.setText( Integer.toString(mIssue._votes) );

		//------------ Button Votes -------

		Button btVote = (Button) vfrag_issue_details.findViewById(R.id.buttonVote);
		//-------- Check if state is Ack or Closed then can not vote ----
		if (CurrStat==2 || CurrStat==3)
			btVote.setEnabled(false);

		//-------- Check if Has Voted ----------
		DatabaseHandler dbHandler = new DatabaseHandler(ctx);
		HasVotedSW = dbHandler.CheckIfHasVoted(IssueID);
		
		
		OwnIssue = false;
		if (UserID_STR.length()>0)
			OwnIssue  = dbHandler.checkIfOwnIssue(Integer.toString(IssueID), UserID_STR);
		
		
		dbHandler.db.close();

		// if has not voted, it is not his issue, and authenticated then able to vote 
		if (!OwnIssue && !HasVotedSW && AuthFlag)
			btVote.setEnabled(true);
		
		
		if (OwnIssue || HasVotedSW) {
			btVote.setEnabled(false);
			btVote.setText(resources.getString(R.string.AlreadyVoted));
		}

		if (!AuthFlag) {
			btVote.setEnabled(false);
			btVote.setText(resources.getString(R.string.VoteToFix));
		}
		
		btVote.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {

				Upload_Data.SendVote(Integer.parseInt(UserID_STR), 
						IssueID,  UserNameSTR, PasswordSTR);

				DatabaseHandler dbHandler = new DatabaseHandler(ctx);

				dbHandler.AddUpdUserVotes(UserNameSTR, PasswordSTR);
				
				dbHandler.addUpdIssues(Service_Location.locUser.getLongitude(),   // Update Issues votes 
						Service_Location.locUser.getLatitude(), 
						Integer.parseInt(distanceDataSTR),
						Integer.parseInt(IssuesNoSTR));

				Service_Data.mIssueL =  dbHandler.getAllIssues();

				dbHandler.db.close();
				//-------- Update IssuesList (in case of + vote)-------


				TextView tvVotes = (TextView ) vfrag_issue_details.findViewById(R.id.textViewVotes);
				int NVotesNew = Integer.parseInt(tvVotes.getText().toString())+ 1;
				tvVotes.setText( Integer.toString(NVotesNew) );


				Button btVote = (Button) vfrag_issue_details.findViewById(R.id.buttonVote);
				btVote.setEnabled(false);
				btVote.setText(resources.getString(R.string.AlreadyVoted));
			}
		});


		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


		//--Buttop Map -
		Button btMap = (Button) vfrag_issue_details.findViewById(R.id.btMapSolo);
		
		if (FActivity_TabHost.IndexGroup==0)
			btMap.setVisibility(View.GONE);
		else if (FActivity_TabHost.IndexGroup==1)
			btMap.setVisibility(View.VISIBLE);
		
		btMap.setOnClickListener(new View.OnClickListener() {   // RRR
			@Override
			public void onClick(View v) {
				
				// Instantiate a new fragment.
		        newfrag_map_solo = new Fragment_SoloMap();
		        
		        Bundle args = new Bundle();
	            args.putInt("issueId", issueId);
	            newfrag_map_solo.setArguments(args);

		        // Add the fragment to the activity, pushing this transaction on to the back stack.
		        FragmentTransaction ft = getFragmentManager().beginTransaction();

		        if (mfrag_issue_details.isVisible()) {
		        	// add your code here
		        	
		        	ft.replace(R.id.llIssues, newfrag_map_solo, "FTAG_MAPSOLO");
		        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		        	ft.addToBackStack(null);
		        	ft.commit();
		        }
			}
		});
		
				
		return vfrag_issue_details;
	}// -------- End OnCreate -----------
	
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {

		super.onResume();
		
		
		if (Service_Data.HasInternet){
			// Comments added from a new thread so as to avoid delays  
			TabComments = (TableLayout)vfrag_issue_details.findViewById(R.id.tlComments);

			params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

			// Create Comment Rows 

			new Thread(new Runnable() {
				final ArrayList<Comment> mCommentsL = Download_Data.Download_CommentsByIssueID(issueId);

				int NComments = mCommentsL.size();

				@Override
				public void run() {
					for (int i=0; i<NComments ;i++){   
						//------------ Inflate Child --------
						final View ChildComm = LayoutInflater.from(FActivity_TabHost.ctx).inflate(R.layout.issue_comment_layout,null);  

						//--------- Convert Date to String 
						String DateCreated_STR = My_Date_Utils.DateToString(  mCommentsL.get(i)._created );
						DateCreated_STR = DateCreated_STR.replace("-", "/");
						DateCreated_STR = My_Date_Utils.SubtractDate(DateCreated_STR);

						// ----------- Set comment title and content -------
						TextView CommTitle = (TextView) ChildComm.findViewById(R.id.tvTitleComment);
						CommTitle.setText(resources.getString(R.string.CommentCreat) + 
								" " + DateCreated_STR + " " + resources.getString(R.string.AgoBy)+" "+ mCommentsL.get(i)._username);

						TextView CommContent = (TextView) ChildComm.findViewById(R.id.tvContentComment);
						CommContent.setText(mCommentsL.get(i)._description);

						//-------------- Add View -----------
		                getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								TabComments.addView(ChildComm, params);
							}
						});				
						
					}
				}
			}
					).start();
		}


		
		
	}
	
	//=============  Set Resources ============================ 
	/** Load session settings */
	public Resources setResources(){
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	    LangSTR          = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);
        UserNameSTR      = mshPrefs.getString("UserNameAR", "");
	    PasswordSTR      = mshPrefs.getString("PasswordAR", "");
	    IssuesNoSTR      = mshPrefs.getString("IssuesNoAR", "40");
	    distanceDataSTR  = mshPrefs.getString("distanceData", "5000");
	    AuthFlag         = mshPrefs.getBoolean("AuthFlag", false);
	    
	    if (!Service_Data.HasInternet)
	    	AuthFlag = false;
	      
	    UserID_STR       = mshPrefs.getString("UserID_STR", "");
		UserRealName     = mshPrefs.getString("UserRealName", "");
	    
   	    Configuration conf = getResources().getConfiguration();
        conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return new Resources(getActivity().getAssets(), metrics, conf);
    }
	
	
	 //---------- Flurry on Start - onStop ----------
	/** Flurry start */
    public void onStart(){
    	super.onStart();
    	SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    	boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);
    	
    	if (AnalyticsSW)
    		FlurryAgent.onStartSession(ctx, Constants_API.Flurry_Key);
    }
    
    /** Flurry stop */
    public void onPause(){
    	super.onPause();
    	
    	
    	SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    	boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);
    	
    	if (AnalyticsSW)
    		FlurryAgent.onEndSession(ctx);
    }
    //----------------------------------------    

	
}
