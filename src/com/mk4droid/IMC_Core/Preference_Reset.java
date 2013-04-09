/** Preference_Reset */
package com.mk4droid.IMC_Core;

import com.mk4droid.IMC_Activities.Activity_TabHost;
import com.mk4droid.IMC_Services.DatabaseHandler;
import com.mk4droid.IMCity_Pack.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import android.widget.Toast;

/**
 * Create a custom preference in Setup with the role of a button that clears all data in SQLite
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class Preference_Reset extends Preference {
    
//    private final String TAG = getClass().getName();
    Context ctx;
    SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(Activity_TabHost.ctx);
    
    public Preference_Reset(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
    }

    public Preference_Reset(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
 
   
   @Override
   protected void onClick() {

	    AlertDialog.Builder builder = new AlertDialog.Builder(Activity_TabHost.ctx);
	    builder.setTitle(Activity_TabHost.resources.getString(R.string.Reset));
	    builder.setIcon( android.R.drawable.ic_menu_preferences);
	    builder.setMessage(Activity_TabHost.resources.getString(R.string.Areyousure));
	    
	    // 1 select
	    builder.setPositiveButton(Activity_TabHost.resources.getString(R.string.Proceed),
	    		new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int id) {

	    		dialog.dismiss();
	    		DatabaseHandler dbHandler = new DatabaseHandler(Activity_TabHost.ctx);
	    		SQLiteDatabase db = dbHandler.getWritableDatabase();

	    		db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_Categories);
	    		db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_Issues);
	    		db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_IssuesPics);
	    		db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_IssuesThumbs);
	    		db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_Version);
	    		db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_CategVersion);
	    		db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_Votes);
	    		db.close();

	    		Toast.makeText(ctx, Activity_TabHost.resources.getString(R.string.Deleted), Toast.LENGTH_LONG).show();
	    		Intent i = ctx.getPackageManager().getLaunchIntentForPackage( ctx.getPackageName() );

	    		i.putExtra("SWUpdate", false);
	    		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    		ctx.startActivity(i);
	    	}
	    });

		// 3 clear 
	    builder.setNegativeButton(Activity_TabHost.resources.getString(R.string.Cancel),
	    		new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int id) {
	    		dialog.dismiss();
	    	}
	    });
		
	    builder.create();
	    builder.show();
	    super.onClick();
    }
}
