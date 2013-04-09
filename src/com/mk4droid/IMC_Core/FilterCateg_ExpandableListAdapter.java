/** FilterCategories_ExpandableListAdapter */

package com.mk4droid.IMC_Core;

import com.mk4droid.IMCity_Pack.R;
import com.mk4droid.IMC_Activities.Activity_Filters;
import com.mk4droid.IMC_Services.DatabaseHandler;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;

/**
 *  Expandable list of categories to be used in Filters Activity
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class FilterCateg_ExpandableListAdapter extends BaseExpandableListAdapter{
    
	int icon_on                = android.R.drawable.checkbox_on_background;
	int icon_off               = android.R.drawable.checkbox_off_background;
    	
	/** Set groups */
    public void setGroupsAndValues(String[] g, boolean[] v) {
        Activity_Filters.groups = g;
        Activity_Filters.groups_check_values = v;
    }
    
    /** Set children of groups */
    public void setChildrenAndValues(String[][] c, boolean[][] v) {
    	Activity_Filters.children = c;
    	Activity_Filters.children_check_values = v;
    }
    
    /** Get Child of a certain group */
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return Activity_Filters.children[groupPosition][childPosition];
	}

	/** Get Child id of a certain group */
	@Override
	 public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    /** Get Child view of a certain group */ 
	@Override
	public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {

		//---------------- Get Layout from resources ----------------
		LayoutInflater mInflater = ((Activity)Activity_Filters.ctx).getLayoutInflater();;
		View v = mInflater.inflate(R.layout.expander_child, null);

		ImageView imv = (ImageView) v.findViewById(R.id.imageViewExpCat);
		imv.setImageDrawable(Activity_Filters.children_icon_values[groupPosition][childPosition]);		
		
        final CheckedTextView ctb = (CheckedTextView) v.findViewById(R.id.checkBoxExpCat);
        
        //----------------- Set Text ------------
        ctb.setText(getChild(groupPosition, childPosition).toString());
        
        // --------------- Set State and Drawable depending on State ------------
        boolean ChildState = Activity_Filters.children_check_values[groupPosition][childPosition];
        ctb.setChecked(ChildState);

        if (ChildState)
        	ctb.setCheckMarkDrawable(icon_on);
        else
        	ctb.setCheckMarkDrawable(icon_off);
        
        //----------------- Set Listener on Click ----------------------------
        ctb.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		Activity_Filters.FiltersChangedFlag = true;
        		
        		Activity_Filters.children_check_values[groupPosition][childPosition] = 
        			!Activity_Filters.children_check_values[groupPosition][childPosition];
        			 
        		boolean ChildState = 
        			      Activity_Filters.children_check_values[groupPosition][childPosition];
        		
        		int ChildID = Activity_Filters.children_id[groupPosition][childPosition];
        		
        		DatabaseHandler dbHandler = new DatabaseHandler(Activity_Filters.ctx);
        	    dbHandler.setCategory(ChildID, ChildState?1:0); // Send as integer
        		dbHandler.db.close();
        	    
       			ctb.setChecked(ChildState);
       			if (ChildState)
        			ctb.setCheckMarkDrawable(icon_on);
       			else
       				ctb.setCheckMarkDrawable(icon_off);
        	}
        });
        
        ctb.invalidate();
        return v;
    }

	@Override
	 public int getChildrenCount(int groupPosition) {
        return Activity_Filters.children[groupPosition].length;
    }

	@Override
	public Object getGroup(int groupPosition) {
        return Activity_Filters.groups[groupPosition];
    }

	@Override
	public int getGroupCount() {
        return Activity_Filters.groups.length;
    }

	@Override
	public long getGroupId(int groupPosition) {
        return groupPosition;
    }
	
	/** Get the view of a certain group */ 
	@Override
	public View getGroupView(final int groupPosition, boolean isExpanded, final View convertView,
			final ViewGroup parent) {

		//------------------ Get Layout from Resources ---------------------
		LayoutInflater mInflater = ((Activity)Activity_Filters.ctx).getLayoutInflater();;
		View v = mInflater.inflate(R.layout.expander_child, null);

		ImageView imv = (ImageView) v.findViewById(R.id.imageViewExpCat);
		imv.setPadding(-10, 0, 0, 0);
		imv.setImageDrawable(Activity_Filters.groups_icon_values[groupPosition]);

		final CheckedTextView ctb = (CheckedTextView) v.findViewById(R.id.checkBoxExpCat);


		// --------------- Set Text -------------------------------------------
		ctb.setText(getGroup(groupPosition).toString());
		ctb.setTextSize(16);

		// ----Set State and Drawable depending on State ----------------------
		boolean ParentState = (Boolean) Activity_Filters.groups_check_values[groupPosition];
		ctb.setChecked( ParentState );

		if (ParentState)
			ctb.setCheckMarkDrawable(icon_on);
		else
			ctb.setCheckMarkDrawable(icon_off);


		//----------------- Set Listener on Click ----------------------------
		ctb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Activity_Filters.FiltersChangedFlag = true;
				Activity_Filters.elv.collapseGroup(groupPosition);

				Activity_Filters.groups_check_values[groupPosition] = 
						!Activity_Filters.groups_check_values[groupPosition];

				boolean ParentState = Activity_Filters.groups_check_values[groupPosition];

				ctb.setChecked( ParentState );

				if (ParentState)
					ctb.setCheckMarkDrawable(icon_on);
				else
					ctb.setCheckMarkDrawable(icon_off);

				DatabaseHandler dbHandler = new DatabaseHandler(Activity_Filters.ctx);
				int ParentID = Activity_Filters.groups_id[groupPosition];
				dbHandler.setCategory(ParentID, ParentState?1:0); // Send as integer
				dbHandler.db.close();

				int NChildren  =  Activity_Filters.children_check_values[groupPosition].length;

				for (int iChild=0; iChild < NChildren; iChild ++ ){
					Activity_Filters.children_check_values[groupPosition][iChild] = ParentState;

					boolean ChildState = 
							Activity_Filters.children_check_values[groupPosition][iChild];

					int ChildID = Activity_Filters.children_id[groupPosition][iChild];

					dbHandler = new DatabaseHandler(Activity_Filters.ctx);
					dbHandler.setCategory(ChildID, ChildState?1:0); 
					dbHandler.db.close();
				}
			}
		});

		return v;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
