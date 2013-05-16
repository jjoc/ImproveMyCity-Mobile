/** AccountOperationPreference */

package com.mk4droid.IMC_Core;


import com.mk4droid.IMC_Activities.Activity_Setup;
import com.mk4droid.IMC_Activities.Activity_TabHost;
import com.mk4droid.IMC_Services.InternetConnCheck;
import com.mk4droid.IMC_Services.Security;
import com.mk4droid.IMC_Services.Upload_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Store.Phptasks;
import com.mk4droid.IMCity_PackDemo.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

/**
 * Custom preference item in setup menu for management of IMC account (remind, login, logout) 
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class Preference_AccountOperations extends Preference {
    
    int tlv = Toast.LENGTH_LONG;
    Context ctx;
    SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(Activity_TabHost.ctx);
    
    public Preference_AccountOperations(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
    }

    public Preference_AccountOperations(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
 
    
    
    
   /**
    *  Start a menu with 3 options login, reset, logout
    */
   @Override
   protected void onClick() {

	    AlertDialog.Builder builder = new AlertDialog.Builder(Activity_TabHost.ctx);
	    builder.setTitle(Activity_TabHost.resources.getString(R.string.AccountOperations));
	    builder.setIcon( android.R.drawable.ic_menu_preferences);
	    
	    
	    builder.setItems(R.array.AccountItems, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            	switch(which){
            	case 0: //------------------ LOGIN
            		final Dialog dlg0 = new Dialog(Activity_TabHost.ctx,R.style.dialog_register);
        			dlg0.setContentView(R.layout.dialog_login);
        			dlg0.setTitle(Activity_TabHost.resources.getString(R.string.Login));
            		
        			Button btSetupLogin= (Button) dlg0.findViewById(R.id.btSetupLogin);
        			
        			btSetupLogin.setOnClickListener(new OnClickListener(){

        				@Override
        				public void onClick(View arg0) {
        			
        					if (InternetConnCheck.getInstance(ctx).isOnline(ctx)){
        						
        						String UserNameSTR = ((EditText)dlg0.findViewById(R.id.etSetupUsername)).getText().toString();
        						String PassSTR = ((EditText)dlg0.findViewById(R.id.etSetupPassword)).getText().toString();
        						
        						Boolean AuthFlag = Security.AuthFun(UserNameSTR, PassSTR, Activity_Setup.resources, ctx);
        		
        						if (!AuthFlag)
        							Toast.makeText(ctx, Activity_Setup.resources.getString(R.string.IncorUser),tlv).show();
        						else {
        							SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);     	// Get Preferences -------
        					        
        					        UserNameSTR    = prefs.getString("UserNameAR" , "");
        					        
        							Toast.makeText(ctx, Activity_Setup.resources.getString(R.string.Welcome)+", "+ 
        									UserNameSTR, tlv).show();
        							
        							dlg0.dismiss();
        						}
        						savePreferences("AuthFlag", AuthFlag, "Boolean");
        					} else {
        						Toast.makeText(ctx, "No internet", tlv).show();
        					}
        				}});
        			
        			dlg0.show();
        			
            		break;
            	case 1: //----------------- REGISTER
            		
                	final Dialog dlg = new Dialog(Activity_TabHost.ctx,R.style.dialog_register);
        			dlg.setContentView(R.layout.dialog_register);
        			dlg.setTitle(Activity_TabHost.resources.getString(R.string.Createanaccount));
        			
        			Button bt_tf_regORcreate = (Button) dlg.findViewById(R.id.bt_imc_register);
        			
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
                            
        					if (imc_name.length()>0 && imc_username.length()>0 && imc_email.contains("@")){
        						//------------ URL CREATE ACCOUNT HERE ------------
        						String response = Upload_Data.SendRegistrStreaming(imc_username, imc_email, imc_password, imc_name); 
        												
        						//-------------------------------------------------
    						
        						dlg.dismiss();
        						
        						final Dialog dlgNotif = new Dialog(ctx,R.style.dialog_register);
        						dlgNotif.setContentView(R.layout.dialog_register_completed);
        						dlgNotif.show();
        						TextView tv_imc_reg_comp = (TextView)dlgNotif.findViewById(R.id.tv_imc_registration_response);
        						tv_imc_reg_comp.setText(response);
        												
        						Button bt_register_fin = (Button)dlgNotif.findViewById(R.id.bt_imc_register_completed_close);
        						
        						bt_register_fin.setOnClickListener(new OnClickListener() {
        							@Override
        							public void onClick(View v) {
        								dlgNotif.dismiss(); 
        							}
        						});
        					} else if (imc_username.length()==0){
        						Toast.makeText(ctx, Activity_TabHost.resources.getString(R.string.Giveausername), tlv).show();
        						
        					} else if (imc_name.length()==0){
        						Toast.makeText(ctx, Activity_TabHost.resources.getString(R.string.Givealsoyourname), tlv).show();
        						
        					} else if (!imc_email.contains("@")){
        						Toast.makeText(ctx, Activity_TabHost.resources.getString(R.string.NotValidEmail), tlv).show();
        					}
        					
        				}});
        			
        			dlg.show();

            		
            		break;
            	case 2: //------------------ REMIND
            		
                	Intent browserIntent2 = new Intent(Intent.ACTION_VIEW, 
     				       Uri.parse("http://"+ Constants_API.ServerSTR + 
     				    		   				Constants_API.phpExec   +
     				    		   				Phptasks.TASK_RESET_PASS));
     			
     			    Activity_TabHost.ctx.startActivity(browserIntent2);

            		
            		break;
            	case 3: // ------------------- LOGOUT
            		
                	savePreferences("AuthFlag", false, "Boolean");
        			savePreferences("PasswordAR", "", "String" );
            		savePreferences("UserNameAR", "", "String" );
        			
            		Toast.makeText(ctx, Activity_TabHost.resources.getString(R.string.LoggedOut), Toast.LENGTH_LONG).show();
            		
            		break;
            	}
            
            
            
            }
	});
	    
		
    builder.create();
    builder.show();
	   
	super.onClick();
    }
    
   /**
    * Save a value to preferences, either string or boolean
    * 
    * @param key       name of the parameters to save
    * @param value     value of the parameter to save 
    * @param type      either "String" or "Boolean" 
    */
	private void savePreferences(String key, Object value, String type){
		SharedPreferences shPrefs = PreferenceManager.getDefaultSharedPreferences(Activity_TabHost.ctx);
		SharedPreferences.Editor editor = shPrefs.edit();

		if (type.equals("String")) 
			editor.putString(key, (String) value);
		else 
			editor.putBoolean(key, (Boolean) value);

		editor.commit();
	}
    
    
}
