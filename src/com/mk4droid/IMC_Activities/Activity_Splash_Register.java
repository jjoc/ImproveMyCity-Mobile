/**  Activity_Splash_Register */

package com.mk4droid.IMC_Activities;

import java.util.Locale;

import com.mk4droid.IMC_Services.InternetConnCheck;
import com.mk4droid.IMC_Services.Security;
import com.mk4droid.IMC_Services.Upload_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Store.Phptasks;
import com.mk4droid.IMCity_PackDemo.R;
import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Shown before the start of the actual application to prompt user to register. 
 *
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Activity_Splash_Register extends Activity implements OnClickListener{
	
	static Handler handlerRegisterButtonDisable;
	EditText et_username, et_password;
	String LangSTR, usernameSTR="", passwordSTR="", emailSTR = "";
	static Context ctx;
	Resources resources;
	boolean AuthFlag = false;
	int tlv = Toast.LENGTH_LONG;
	String userRealName;
	Button bt_tf_regORcreate;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    resources = setResources();
	    setContentView(R.layout.activitiy_splash_register);
	    ctx  = this;
	    
	    TextView tv_title = (TextView) findViewById(R.id.tv_app_title);
	    //-------- Set IMC Logo from cropping from icon ---------
	    ImageView imvIMCLogo = (ImageView)findViewById(R.id.imvIMCLogo); 
	    Bitmap bmIMCLogoFull = ((BitmapDrawable) resources.getDrawable(R.drawable.imc_logo) ).getBitmap();
	    	    
	    Bitmap bmIMCLogo = Bitmap.createBitmap(bmIMCLogoFull,  10,    5,
	    		 (int)  (0.9*bmIMCLogoFull.getWidth()),  (int)   (0.72*bmIMCLogoFull.getHeight()));
	    
	    imvIMCLogo.setImageBitmap(bmIMCLogo);
	    //---------------------------------------
	    
 	    Animation animation = AnimationUtils.loadAnimation(this, R.anim.tv_anim_left);
 	    tv_title.setAnimation(animation); 
 	    tv_title.startAnimation(animation);
 	    
	    et_username = (EditText) findViewById(R.id.et_username_splash);
	    et_password = (EditText) findViewById(R.id.et_password_splash);
	    
	    //------- Check GPS ------
	    LocationManager lm = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
	    if ( !lm.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
	        Toast.makeText(this, "No GPS", tlv).show();
	    }
	    
	    //------- Check Internet -----
	    if (InternetConnCheck.getInstance(this).isOnline(this)){
	        AuthFlag = Security.AuthFun(usernameSTR, passwordSTR, resources, ctx);
	        if (AuthFlag)
	        	Toast.makeText(ctx, resources.getString(R.string.Welcome)+", "+userRealName, tlv).show();
	    }else 
	    	Toast.makeText(this, "No Internet", tlv).show();
	    
	    
		if (AuthFlag || !InternetConnCheck.getInstance(this).isOnline(this)){
			finish();
			startActivity(new Intent(this,FActivity_TabHost.class));
		}
		
		// ------ handlerSubmitButtonDisable --------
		handlerRegisterButtonDisable = new Handler(){
           public void  handleMessage(Message msg){
              if (msg.arg1 == 1) {   // Redraw markers
            		Toast.makeText(ctx, resources.getString(R.string.Sending),Toast.LENGTH_LONG).show();
            		bt_tf_regORcreate.setEnabled(false);
            		bt_tf_regORcreate.setVisibility(View.GONE);
            		bt_tf_regORcreate.invalidate();
	          }
              super.handleMessage(msg);
           }};
		
	}

	//=========== onClick =================================================
	/**
	 *     click listening of any button
	 */
	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()){
		case R.id.btLoginSplash:
			
			String usernameSTR = et_username.getText().toString();
			String passwordSTR = et_password.getText().toString();
			
			
			InputMethodManager imm = (InputMethodManager)ctx.getSystemService(Service.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(et_username.getWindowToken(), 0); 
			imm.hideSoftInputFromWindow(et_password.getWindowToken(), 0);

			
			
			//------------ Authenticate -----------
		    boolean AuthFlag = Security.AuthFun(usernameSTR, passwordSTR, resources, ctx);
		    
	    
		    savePreferences("AuthFlag", AuthFlag, "Boolean");
		    
			if (AuthFlag){
				resources = setResources(); // To get the UserRealName
	        	Toast.makeText(ctx, resources.getString(R.string.Welcome)+", "+userRealName, tlv).show();
				
				finish();
				startActivity(new Intent(this,FActivity_TabHost.class));
			}else 
				Toast.makeText(ctx, resources.getString(R.string.tryagain), Toast.LENGTH_LONG).show();
			
			break;
		case R.id.btSkipLogin:
			
			startActivity(new Intent(this,FActivity_TabHost.class));
			
			break;
		case R.id.tvRegisterSplash:
			
			final Dialog dlg = new Dialog(ctx,R.style.dialog_register);
			dlg.setContentView(R.layout.dialog_register);
			dlg.setTitle(resources.getString(R.string.Createanaccount));
			
			bt_tf_regORcreate = (Button) dlg.findViewById(R.id.bt_imc_register);
			
			bt_tf_regORcreate.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					EditText et_imc_username = (EditText) dlg.findViewById(R.id.et_imc_username);
					EditText et_imc_email = (EditText) dlg.findViewById(R.id.et_imc_email);
					EditText et_imc_Password = (EditText) dlg.findViewById(R.id.et_imc_Password);
					EditText et_imc_name = (EditText) dlg.findViewById(R.id.et_imc_name);
					
					String imc_username = et_imc_username.getText().toString();
                    String imc_email    = et_imc_email.getText().toString();
                    String imc_password = et_imc_Password.getText().toString();
                    String imc_name     = et_imc_name.getText().toString();
                    
					if (imc_name.length()>0 && imc_username.length()>0 && imc_email.contains("@") && imc_password.length()<=16){
						//------------ URL CREATE ACCOUNT HERE ------------
						dlg.dismiss();
						
						Message msgButton = new Message();
						msgButton.arg1 = 1;
						handlerRegisterButtonDisable.sendMessage(msgButton);
						
						
						final Dialog dlgNotif = new Dialog(ctx,R.style.dialog_register);
						dlgNotif.setContentView(R.layout.dialog_register_completed);
						dlgNotif.show();
						TextView tv_imc_reg_comp = (TextView)dlgNotif.findViewById(R.id.tv_imc_registration_response);
						
						String response = Upload_Data.SendRegistrStreaming(imc_username, imc_email, imc_password, imc_name); 
						
						tv_imc_reg_comp.setText(response);
												
						Button bt_register_fin = (Button)dlgNotif.findViewById(R.id.bt_imc_register_completed_close);
						
						bt_register_fin.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								dlgNotif.dismiss(); 
							}
						});
						
						
					} else if (imc_username.length()==0){
						Toast.makeText(ctx, resources.getString(R.string.Giveausername), tlv).show();
						
					} else if (imc_name.length()==0){
						Toast.makeText(ctx, resources.getString(R.string.Givealsoyourname), tlv).show();
						
					} else if (!imc_email.contains("@")){
						Toast.makeText(ctx, resources.getString(R.string.NotValidEmail), tlv).show();
					} else if (imc_password.length()>16){
						Toast.makeText(ctx, resources.getString(R.string.PasswordShorter), tlv).show();
					}
					
				}});
			
			dlg.show();
			
			break;
		case R.id.tvRemindSplash:
			 startActivity(new Intent(Intent.ACTION_VIEW, 
				       Uri.parse(Constants_API.COM_Protocol+ Constants_API.ServerSTR + 
	    		   				           Constants_API.phpExec + Phptasks.TASK_RESET_PASS)));
			break;
		}
	}
	
	//=============== setResources =============================
    /** Retrieve preferences and set resources language */ 
	public Resources setResources(){
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    LangSTR          = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);
        
	    usernameSTR      = mshPrefs.getString("UserNameAR", "");
	    emailSTR         = mshPrefs.getString("emailAR", "");
	    passwordSTR      = mshPrefs.getString("PasswordAR", "");
	    userRealName     = mshPrefs.getString("UserRealName", "");
	    
   	    Configuration conf = getResources().getConfiguration();
        conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return new Resources(getAssets(), metrics, conf);
    }
	
	//================ savePreferences =======================
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
