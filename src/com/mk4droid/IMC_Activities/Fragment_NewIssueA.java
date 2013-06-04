/**
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 */
package com.mk4droid.IMC_Activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.mk4droid.IMC_Constructors.Category;
import com.mk4droid.IMC_Core.SpinnerAdapter_NewIssueCateg;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Utils.My_System_Utils;
import com.mk4droid.IMCity_PackDemo.R;

/**
 * A form for writing issue title, description, category, attaching photo, and proceed to localization of the issue (Fragment_NewIssueB)
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 * 
 */
public class Fragment_NewIssueA extends Fragment {

	
	/** The issue image to be attached */
	public static Drawable ImageThumb_DRW_S;

	/** path of temporary image File of issue */
	public static String image_path_source_temp =               
			Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+ Environment.DIRECTORY_DCIM + "/tmp_image.jpg";
	
	/** Attach and Proceed buttons */
	public static Button btAttachImage,btProceed;
	
	public static LinearLayout llnewissue_a;
	static EditText et_title,et_descr;

	static int[] SpinnerArrID;  // This contains category ids as in MySQL
	static int spPosition;
	static TextView tvUnauth;
	
	
	static int CAMERA_PIC_REQUEST = 1337;
	static int SELECT_PICTURE     = 1;
	private static File fimg;
	static boolean flagPictureTaken = false;

	//------- System -----------
	public static Resources resources;
	DisplayMetrics metrics;
	static Context ctx;
	int tlv = Toast.LENGTH_LONG;

	boolean AuthFlag;

	//---------- Task Variables --------------
	static String titleData_STR = "";
	static String descriptionData_STR = "";

	static Spinner sp;

	String[] SpinnerArrString;		 

	SharedPreferences mshPrefs;
 
	
	public static Fragment_NewIssueA mfrag_nIssueA;
	View vfrag_nIssueA;

	
	public static Fragment_NewIssueB mfrag_nIssueB;

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	
		Log.e("Frag_NIA","onCreate");
		
		FActivity_TabHost.isFStack1 = true;
		
		mshPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		resources = SetResources();
		
		
		
		super.onCreate(savedInstanceState);
	}

	
	/**
	 *          OnCreateView 
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (mfrag_nIssueB!=null){
			Log.e("mfrag_nIssueB ----------> ", " " + mfrag_nIssueB);
			
			if (mfrag_nIssueB.getId() == 0){
			    Log.e("Why is zero","0");
				
			}
		}
		
		
		if ( mfrag_nIssueA == null)	
			mfrag_nIssueA  = this;
		
		Log.e("Frag_NIA","onCreateView");
				
		FActivity_TabHost.IndexGroup = 2;
		
		if ( vfrag_nIssueA != null) {
	        ViewGroup parent = (ViewGroup) vfrag_nIssueA.getParent();
	        if (parent != null)
	            parent.removeView(vfrag_nIssueA);
	    }
		
		
		if (vfrag_nIssueA != null) {
	        ViewGroup parent = (ViewGroup) vfrag_nIssueA.getParent();
	        if (parent != null)
	            parent.removeView(vfrag_nIssueA);
	    }
		
	    try {
	    	if (vfrag_nIssueA == null) 
	    		vfrag_nIssueA = inflater.inflate(R.layout.fragment_newissue_a, container, false);
	    } catch (InflateException e) {
	        /* map is already there, just return view as it is */
	    }

		//-------- tvUnauth ---- 
		tvUnauth = (TextView)vfrag_nIssueA.findViewById(R.id.tvUnauth);

		//---- Spinner -----
		ArrayList<Category> mCatL_Sorted = SortCategList(Service_Data.mCategL);
		SpinnerArrString = initSpinner(mCatL_Sorted);

		SpinnerAdapter_NewIssueCateg adapterSP = new SpinnerAdapter_NewIssueCateg(getActivity(),    //--- Set spinner adapter --
				android.R.layout.simple_spinner_item, mCatL_Sorted);

		sp = (Spinner)vfrag_nIssueA.findViewById(R.id.spinnerCateg);
		sp.setAdapter(adapterSP);

		sp.setSelection(spPosition);

		//--------- Title -----

		et_title = (EditText)vfrag_nIssueA.findViewById(R.id.etTitle_ni);
		
		if (et_title!=null)
			if (et_title.getText().toString().length()>0)
				titleData_STR = et_title.getText().toString();


		if(titleData_STR.length()>0)
		et_title.setText(titleData_STR);

		//------ Description ----

		et_descr = (EditText)vfrag_nIssueA.findViewById(R.id.etDescription);
		if (et_descr!=null)
		if (et_descr.getText().toString().length()>0)
			descriptionData_STR = et_descr.getText().toString();

		
		//------- Bt Attach image ---
		btAttachImage = (Button)vfrag_nIssueA.findViewById(R.id.btAttach_image);

		//-------- Bt Proceed -----
		btProceed = (Button)vfrag_nIssueA.findViewById(R.id.btProceed_ni_B);
		
		//- Set Proceed button colors
		btProceedResetButtonColors();
		
	
		mshPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		resources = SetResources();

		Log.e("Fragment_List","onCreateView");

		FActivity_TabHost.IndexGroup = 2;
		
		//--------- Layout -------
		llnewissue_a = (LinearLayout)vfrag_nIssueA.findViewById(R.id.llnewissue_a);
		llnewissue_a.setVisibility(View.VISIBLE);
		
		//-------- Take Image button -------
		if (flagPictureTaken){
			btAttachImage.setCompoundDrawablesWithIntrinsicBounds(ImageThumb_DRW_S, null, null,  null);
			btAttachImage.setPadding(5, 0, 0, 0);
			btAttachImage.setCompoundDrawablePadding(5);
		} else {
			btAttachImage.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_gallery, 0, 0,  0);
		}


		btAttachImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

				builder.setTitle(FActivity_TabHost.resources.getString(R.string.Attachanimage));
				builder.setIcon( android.R.drawable.ic_menu_gallery);

				// 1 select
				builder.setPositiveButton(FActivity_TabHost.resources.getString(R.string.Gallery),
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						dialog.dismiss();

						Intent intent = new Intent();
						intent.setType("image/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
					}
				});

				// 2 Shoot
				builder.setNeutralButton(FActivity_TabHost.resources.getString(R.string.Camera),
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						dialog.dismiss();
						// 2 shoot 
						fimg = new File (image_path_source_temp);
						Uri uri = Uri.fromFile(fimg);

						Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
						cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

						startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);  
					}
				});


				// 3 clear 
				builder.setNegativeButton(FActivity_TabHost.resources.getString(R.string.Clear),
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						//dialog.cancel();

						flagPictureTaken = false;
						File imagef = new File(image_path_source_temp);
						imagef.delete();

						dialog.dismiss();

						btAttachImage.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_gallery, 0, 0,  0);
					}
				});

				builder.create();
				builder.show();

			}});


		//------------- button Proceed ----------

		btProceedResetButtonColors();
		btProceed.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View vBt) {
				// Check if title is long enough and sent
				if ( et_title.getText().toString().length() > 2){                // RRR
                    
					 vBt.setBackgroundColor(Color.argb(100, 100, 100, 100));

					titleData_STR = et_title.getText().toString();

					spPosition = sp.getSelectedItemPosition();

					if ( et_descr.getText().toString().length() > 0)
						descriptionData_STR =  et_descr.getText().toString();

					// Close Keyboard
					InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(et_title.getWindowToken(), 0); 
					imm.hideSoftInputFromWindow(et_descr.getWindowToken(), 0);

					// Instantiate a new fragment.
					if (mfrag_nIssueB!=null){
						Log.e("mfrag_nIssueB ----------> ", " " + mfrag_nIssueB);
					} else {
						mfrag_nIssueB = new Fragment_NewIssueB();
					}

					
					
					
					

					Bundle args = new Bundle();
					args.putInt("IndexSpinner", sp.getSelectedItemPosition());
					mfrag_nIssueB.setArguments(args);
					
					if (Fragment_NewIssueB.btSubmit!=null)
						Fragment_NewIssueB.btSubmit.setBackgroundDrawable(resources.getDrawable(R.drawable.gradient_green));
					
					// Add the fragment to the activity, pushing this transaction
					// on to the back stack.
					FragmentTransaction ft = getFragmentManager().beginTransaction();
					ft.add(mfrag_nIssueA.getId(), mfrag_nIssueB, "FTAG_NEW_ISSUE_B");
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					ft.addToBackStack(null);
					ft.commit();
					
					
					

				}else{
					Toast.makeText(getActivity(), resources.getString(R.string.LongerTitle), tlv).show();
				}
			}
		});


		return vfrag_nIssueA;
	}// Endof Create


	//=============== onActivityResult ===============================
	/**
	 *    When returning from shooting an image from camera or selecting an image from Gallery. 
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intentdata) {  

		if ((requestCode == CAMERA_PIC_REQUEST || requestCode == SELECT_PICTURE) && resultCode == -1 ){   //  - 1 = RESULT_OK 

			flagPictureTaken = true;
			if ( requestCode == SELECT_PICTURE){
				Uri selectedImageUri = intentdata.getData();
				My_System_Utils.FCopy(image_path_source_temp, getPath(selectedImageUri));
			}

			CheckOrient();

			btAttachImage.setCompoundDrawablesWithIntrinsicBounds(ImageThumb_DRW_S, null, null,  null);
			btAttachImage.setPadding(5, 0, 0, 0);
			btAttachImage.setCompoundDrawablePadding(5);

		}  else {
			flagPictureTaken = false;
			File imagef = new File(image_path_source_temp);
			imagef.delete();
		} 
	}  

	//================ CheckOrient ============================= 	 
	/**
	 *        Check Image Orientation  
	 */
	public void CheckOrient(){  

		BitmapFactory.Options options=new BitmapFactory.Options(); // Resize is needed otherwize outofmemory exception
		options.inSampleSize = 6;

		//------------- read tmp file ------------------ 
		Bitmap Image_BMP   = BitmapFactory.decodeFile(image_path_source_temp, options); // , options
		Bitmap Image_BMP_S = null;

		//---------------- find exif header --------
		ExifInterface exif;
		String exifOrientation = "0"; // 0 = exif not working
		try {
			exif = new ExifInterface(image_path_source_temp);
			exifOrientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
		} catch (IOException e1) {
			e1.printStackTrace();
		}    

		//---------------- Resize ---------------------
		if (exifOrientation.equals("0")){   
			if ( Image_BMP.getWidth() < Image_BMP.getHeight() && Image_BMP.getWidth() > 400 ){
				Image_BMP   = Bitmap.createScaledBitmap(Image_BMP, 400, 640, true); // <- To sent
			} else if ( Image_BMP.getWidth() > Image_BMP.getHeight() && Image_BMP.getWidth() > 640 ) { 
				Image_BMP = Bitmap.createScaledBitmap(Image_BMP, 640, 400, true); // <- To sent
			}	    	
		} else {

			if (exifOrientation.equals("1") && Image_BMP.getWidth() > 640 ){  // normal

				Image_BMP   = Bitmap.createScaledBitmap(Image_BMP, 640, 400, true); // <- To sent

			} else if (exifOrientation.equals("6") && Image_BMP.getWidth() > 400 ){  // rotated 90 degrees

				// Rotate
				Matrix matrix = new Matrix();

				int bmwidth  = Image_BMP.getWidth();
				int bmheight = Image_BMP.getHeight();

				matrix.postRotate(90);

				Image_BMP = Bitmap.createBitmap(Image_BMP, 0, 0, bmwidth,
						bmheight, matrix, true);

				Image_BMP = Bitmap.createScaledBitmap(Image_BMP, 400, 640, true); // <- To sent
			}
		}

		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

		//---------- Create Thumbnail for button img preview -------------
		int icon_b = metrics.densityDpi/3;
		int icon_s = metrics.densityDpi/5;

		if ( Image_BMP.getWidth() < Image_BMP.getHeight() )
			Image_BMP_S = Bitmap.createScaledBitmap(Image_BMP,  icon_s,  icon_b, true);
		else 
			Image_BMP_S = Bitmap.createScaledBitmap(Image_BMP,  icon_b,  icon_s, true);

		ImageThumb_DRW_S = new BitmapDrawable(Image_BMP_S);

		//------------ now store as jpg over the temp jpg
		File imagef = new File(image_path_source_temp);
		try {
			Image_BMP.compress(Bitmap.CompressFormat.JPEG, 95, new FileOutputStream(imagef));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	//============ getPath ==================================	
	/**
	 *  Get path where image was stored after shooting with camera
	 * 
	 * @param uri general uri for android metadata
	 * @return path of the image
	 */
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	//============ On Resume ============
	/** Resume from changing tabs */
	@Override
	public void onResume() {
		super.onResume();
		resources = SetResources();
		
		//FActivity_TabHost.mTabWidget.setEnabled(true);
		
		Log.e("Frag_NIA","onResume");
		
		llnewissue_a.setVisibility(View.VISIBLE);
		
		//--------- Show Unauthorized message ------------
		for (int i=0; i< llnewissue_a.getChildCount(); i++)
			if (!AuthFlag || !Service_Data.HasInternet)
				llnewissue_a.getChildAt(i).setVisibility(View.GONE);
			else 
				llnewissue_a.getChildAt(i).setVisibility(View.VISIBLE);

		if (!AuthFlag || !Service_Data.HasInternet){
			tvUnauth.setVisibility(View.VISIBLE);
		}else{
			tvUnauth.setVisibility(View.GONE);
		}

		et_title.setHint(resources.getString(R.string.STitle));
		et_descr.setHint(resources.getString(R.string.Description));
		btAttachImage.setText(resources.getString(R.string.Attach));
		
		btProceedResetButtonColors();

		
		
		//----------- Flurry Analytics --------
		boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);
		if (AnalyticsSW)
			FlurryAgent.onStartSession(getActivity(), Constants_API.Flurry_Key);
	}  


	//============ onPause ============== 
	/** Pause when changing tab. Stop Flurry. */
	@Override
	public void onPause() {
		super.onPause();

		//-- Flurry Analytics --
		boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);

		if (AnalyticsSW)
			FlurryAgent.onEndSession(getActivity());
	}  

	//----- Change Proceed A button colors ------
	public static void btProceedResetButtonColors(){
		btProceed.setBackgroundDrawable(resources.getDrawable(R.drawable.gradient_green));
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

		String LangSTR          = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);

		AuthFlag         = mshPrefs.getBoolean("AuthFlag", false);

		Configuration conf = getResources().getConfiguration();
		conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
		metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return new Resources(getActivity().getAssets(), metrics, conf);
	}
}