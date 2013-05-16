/* Group_NewIssue */
package com.mk4droid.IMC_Activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.mk4droid.IMC_Utils.My_System_Utils;
import com.mk4droid.IMCity_PackDemo.R;

import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * This is a group of two activities: NewIssueA for textual and image info; and NewIssueB for location information.
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class Group_NewIssue extends ActivityGroup {
	
	int CAMERA_PIC_REQUEST = 1337;
	int SELECT_PICTURE     = 1;
	private File fimg;
	public static Drawable ImageThumb_DRW_S;
	
	public static String image_path_source_temp =               // Temporary File of issue to send
		 Environment.getExternalStorageDirectory().getAbsolutePath() + "/" 
		               + Environment.DIRECTORY_DCIM + "/tmp_image.jpg";
	
	static boolean flagPictureTaken = false;
	
	
	static Group_NewIssue group;
	static ArrayList<View> history;

	//================= onCreate =========================
	/**
	 *    Set Activity_NewIssueA as current activity    
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      history = new ArrayList<View>();
	      group = this;
	      Activity_TabHost.IndexGroup = 2;
	      
          // Start the root activity within the group and get its view
	      View view = getLocalActivityManager().startActivity("Activity_NewIssueA", new
	    	      							Intent(this,Activity_NewIssueA.class)
	    	      							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
	    	                                .getDecorView();

          // Replace the view of this ActivityGroup
	      replaceView(view);
   }

	//================= replaceView =========================
	/** Replace current view with next view and keep it in an archive */
	public void replaceView(View v) {
		history.add(v);
		setContentView(v) ;
	}

	//==================== back =====================
	/** Go to previous view    */ 
	public void back() {
		if(history.size() > 1) {
			history.remove(history.size()-1);
			setContentView((View) history.get(history.size()-1)  );
		}else {
			flagPictureTaken = false;
			finish();
		}
	}

   //==================== onBackPressed =====================
   /** Hard key back press leads to previous view */
   @Override
    public void onBackPressed() {
    	Group_NewIssue.group.back();
        return;
    }

	
   //=========== onClick ================================
   /**
    *       Click listener for buttons 1) Attach image 2) Proceed to NewIssueB      
    *       
    */
   public void onClick(View v) {
	   int id = v.getId();

	   switch (id) {
	   case R.id.btAttach_image:	


		   AlertDialog.Builder builder = new AlertDialog.Builder(Activity_TabHost.ctx);
		   builder.setTitle(Activity_TabHost.resources.getString(R.string.Attachanimage));
		   builder.setIcon( android.R.drawable.ic_menu_gallery);
		   //builder.setMessage("");


		   // 1 select
		   builder.setPositiveButton(Activity_TabHost.resources.getString(R.string.Gallery),
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
		   builder.setNeutralButton(Activity_TabHost.resources.getString(R.string.Camera),
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
		   builder.setNegativeButton(Activity_TabHost.resources.getString(R.string.Clear),
				   new DialogInterface.OnClickListener() {
			   public void onClick(DialogInterface dialog, int id) {
				   //dialog.cancel();

				   flagPictureTaken = false;
				   File imagef = new File(image_path_source_temp);
				   imagef.delete();

				   dialog.dismiss();

				   Activity_NewIssueA.btAttachImage.setCompoundDrawablesWithIntrinsicBounds(
						   android.R.drawable.ic_menu_gallery, 0, 0,  0);

			   }
		   });

		   builder.create();
		   builder.show();

		   break;
	   case R.id.btProceed_ni_B:

		   // Check if title is long enough and sent
		   if ( Activity_NewIssueA.et_title.getText().toString().length() > 2){
			   Intent intentClick = new Intent(Activity_NewIssueA.ctx, Activity_NewIssueB.class);
			   intentClick.putExtra("IndexSpinner", Activity_NewIssueA.sp.getSelectedItemPosition());

			   // Create the view using FirstGroup's LocalActivityManager
			   View NewView = Group_NewIssue.group.getLocalActivityManager()
					   .startActivity("Activity_NewIssue", intentClick
							   .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
							   .getDecorView();

			   InputMethodManager imm = (InputMethodManager)this.getSystemService(Service.INPUT_METHOD_SERVICE);
			   imm.hideSoftInputFromWindow(Activity_NewIssueA.et_title.getWindowToken(), 0); 
			   imm.hideSoftInputFromWindow(Activity_NewIssueA.et_descr.getWindowToken(), 0);


			   // Again, replace the view
			   Group_NewIssue.group.replaceView(NewView);
		   }else{
			   Toast.makeText(Activity_NewIssueA.ctx, Activity_NewIssueA.resources.getString(R.string.LongerTitle), 
					   Toast.LENGTH_LONG).show();
		   }


		   break;

	   }

   }

   //=============== onActivityResult ===============================
   /**
    *    When returning from shooting an image from camera or selecting an image from Gallery. 
    */
   protected void onActivityResult(int requestCode, int resultCode, Intent intentdata) {  

	   if ((requestCode == CAMERA_PIC_REQUEST || requestCode == SELECT_PICTURE) && resultCode == RESULT_OK){   // 

		   flagPictureTaken = true;

		   if ( requestCode == SELECT_PICTURE){
			   Uri selectedImageUri = intentdata.getData();
			   My_System_Utils.FCopy(image_path_source_temp, getPath(selectedImageUri));
		   }

		   CheckOrient();

		   Activity_NewIssueA.btAttachImage.setCompoundDrawablesWithIntrinsicBounds(
				   ImageThumb_DRW_S, null, null,  null);
		   Activity_NewIssueA.btAttachImage.setPadding(5, 0, 0, 0);
		   Activity_NewIssueA.btAttachImage.setCompoundDrawablePadding(5);

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
	   getWindowManager().getDefaultDisplay().getMetrics(metrics);

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
	   Cursor cursor = managedQuery(uri, projection, null, null, null);
	   int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	   cursor.moveToFirst();
	   return cursor.getString(column_index);
   }
	
}
