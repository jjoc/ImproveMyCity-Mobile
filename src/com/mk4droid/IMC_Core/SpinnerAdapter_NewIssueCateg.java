/** NewIssueCateg_SpinnerAdapter  */
package com.mk4droid.IMC_Core;

import java.util.ArrayList;

import com.mk4droid.IMC_Constructors.Category;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import com.mk4droid.IMCity_Pack.R;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * This is the adapter for the spinner in Activity_NewIssueA. Custom spinner with 
 * category icon and text dynamically retrieved from the SQLite
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class SpinnerAdapter_NewIssueCateg extends ArrayAdapter<Category>{

	private Activity context;
    ArrayList<Category> data = null;
    final float dens;
    
    /** Constructor. Get density of the screen to scale icons of categories */
    public SpinnerAdapter_NewIssueCateg(Activity context, int resource, ArrayList<Category> data)
    {
        super(context, resource, data);
        this.context = context;
        this.data = data;
        dens = getContext().getResources().getDisplayMetrics().density;
    }
   
    
    
    /** Ordinary view in Spinner. Scaling of icons. */
    @Override
    public View getView(int position, View convertView, ViewGroup parent){ 
    	
        View row = convertView;
        
        if(row == null){
            LayoutInflater inflater = context.getLayoutInflater();
            row = inflater.inflate(R.layout.spinner_categ_item, parent, false);
        }

        Category item = data.get(position);

        // Parse the data from each object and set it.
        if(item != null){
            ImageView cat_imv   = (ImageView) row.findViewById(R.id.categIcon);
            TextView cat_tv = (TextView) row.findViewById(R.id.categName);
            
            if(cat_imv != null){
            	Bitmap bmicon = BitmapFactory.decodeByteArray(item._icon, 0, item._icon.length);
            	
            	if (item._level == 2)
            		bmicon = Bitmap.createScaledBitmap(bmicon, (int) (25 * dens + 0.5f), (int) (30 * dens + 0.5f), true);
            	else 
            	    bmicon = Bitmap.createScaledBitmap(bmicon, (int) (30 * dens + 0.5f), (int) (35 * dens + 0.5f), true);
            	       
            	cat_imv.setImageBitmap(bmicon);
            }
            
            if(cat_tv != null)
                cat_tv.setText(item._name);
        }
        
        return row;
    }

    /** Get drop down view */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent){ 
    	// This view starts when we click the spinner.
        View row = convertView;
        
        if(row == null){
            LayoutInflater inflater = context.getLayoutInflater();
            row = inflater.inflate(R.layout.spinner_categ_item, parent, false);
        }

        Category item = data.get(position);

        // Parse the data from each object and set it.
        if(item != null){ 
            ImageView cat_imv   = (ImageView) row.findViewById(R.id.categIcon);
            
            if(cat_imv != null){
            	Bitmap bmicon = BitmapFactory.decodeByteArray(item._icon, 0, item._icon.length);
            	
            	if (item._level == 2)
            		bmicon = Bitmap.createScaledBitmap(bmicon, (int) (25 * dens + 0.5f), (int) (30 * dens + 0.5f), true);
            	else 
            	    bmicon = Bitmap.createScaledBitmap(bmicon, (int) (30 * dens + 0.5f), (int) (35 * dens + 0.5f), true);
            		
            	cat_imv.setImageBitmap(bmicon);
            }    
            
            TextView cat_tv     = (TextView)  row.findViewById(R.id.categName);
            if(cat_tv != null)
            	cat_tv.setText(item._name);
        }
        return row;
    }
}