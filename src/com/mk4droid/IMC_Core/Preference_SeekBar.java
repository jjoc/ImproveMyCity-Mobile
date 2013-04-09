/** SeekBarPreference */

package com.mk4droid.IMC_Core;

import com.mk4droid.IMC_Activities.Activity_TabHost;
import com.mk4droid.IMC_Services.InternetConnCheck;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMCity_Pack.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;


/**
 * Implement a horizontal SeekBar for Setup->Setting range 
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class Preference_SeekBar extends Preference implements OnSeekBarChangeListener {
    
    private final String TAG_Class = getClass().getName();

    private static final int DEFAULT_VALUE = 50;
    
    private int mMaxValue      = 100;
    private int mMinValue      = 0;
    private int mInterval      = 1;
    private int mCurrentValue;
    private String mUnitsLeft  = "";
    private String mUnitsRight = "";
    private SeekBar mSeekBar;
    Context ctx;
    private TextView mStatusText;

    SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(Activity_TabHost.ctx);
    int distanceData            = Integer.parseInt(  mshPrefs.getString("distanceData", "5000") );
    
    
    public Preference_SeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
        initPreference(context, attrs);
    }

    public Preference_SeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPreference(context, attrs);
    }

    private void initPreference(Context context, AttributeSet attrs) {
        setValuesFromXml(attrs);
        mSeekBar = new SeekBar(context, attrs);
        mSeekBar.setMax(mMaxValue - mMinValue);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setThumb(Activity_TabHost.resources.getDrawable(R.drawable.slidingbutton));
        
        if (InternetConnCheck.getInstance(ctx).isOnline(ctx)){
        	mSeekBar.setEnabled(true);
    	} else {
     		mSeekBar.setEnabled(false);
    	}
        
    }
    
    
 
    
    private void setValuesFromXml(AttributeSet attrs) {
        mMaxValue =     100; 
        mMinValue =       0;
        
        mUnitsLeft   = "< "; 
        mUnitsRight  = ""; 
        
        try {
            String newInterval =   "1";
            if(newInterval != null)
                mInterval = Integer.parseInt(newInterval);
        }
        catch(Exception e) {
            Log.e(Constants_API.TAG, TAG_Class +":Invalid interval value" + e);
        }
        
    }
   
    @Override
    protected View onCreateView(ViewGroup parent){
        
        RelativeLayout layout =  null;
        
        try {
            LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = (RelativeLayout)mInflater.inflate(R.layout.preference_seekbar, parent, false);
        } catch(Exception e){
            Log.e(Constants_API.TAG, TAG_Class + " :Error creating seek bar preference " + e);
        }
        return layout;
    }
    
    @Override
    public void onBindView(View view) {
        super.onBindView(view);

        try {
            // move our seekbar to the new view we've been given
            ViewParent oldContainer = mSeekBar.getParent();
            ViewGroup newContainer = (ViewGroup) view.findViewById(R.id.seekBarPrefBarContainer);
            
            if (oldContainer != newContainer) {
                // remove the seekbar from the old view
                if (oldContainer != null) {
                    ((ViewGroup) oldContainer).removeView(mSeekBar);
                }
                // remove the existing seekbar (there may not be one) and add ours
                newContainer.removeAllViews();
                newContainer.addView(mSeekBar, ViewGroup.LayoutParams.FILL_PARENT,
                                                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        } catch(Exception ex) {
            Log.e(Constants_API.TAG, TAG_Class + ": Error binding view: " + ex.toString());
        }

        updateView(view);
    }
    
    /**
     * Update a SeekBarPreference view with our current state
     * @param view
     */
    protected void updateView(View view) {

        try {
            RelativeLayout layout = (RelativeLayout)view;

            mStatusText = (TextView)layout.findViewById(R.id.seekBarPrefValue);
            String distanceDataSTR = DistanceToText(distanceData);
            mStatusText.setText(distanceDataSTR);
            mStatusText.setMinimumWidth(30);

            if (distanceData < 4000000)
            	mSeekBar.setProgress(distanceData/200);  
            else 
            	mSeekBar.setProgress(100);

            TextView unitsRight = (TextView)layout.findViewById(R.id.seekBarPrefUnitsRight);
            unitsRight.setText(mUnitsRight);
            
            TextView unitsLeft = (TextView)layout.findViewById(R.id.seekBarPrefUnitsLeft);
            unitsLeft.setText(mUnitsLeft);
        } catch(Exception e) {
            Log.e(Constants_API.TAG, TAG_Class + " :Error updating seek bar preference " + e);
        }
    }

    /**
     *  Executes after the seekbar has changed value.
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int newValue = progress + mMinValue;
        
        if(newValue > mMaxValue)
            newValue = mMaxValue;
        else if(newValue < mMinValue)
            newValue = mMinValue;
        else if(mInterval != 1 && newValue % mInterval != 0)
            newValue = Math.round(((float)newValue)/mInterval)*mInterval;  
        
        if (newValue < 90)
        	distanceData = newValue * 200;
        else if (newValue >= 90)  
        	distanceData = 4000000;
        else if (newValue < 1)  
        	distanceData = 200;

        String distanceDataSTR = DistanceToText(distanceData);
        //-------- store to preferences ------
        SavePreferences("distanceData", Integer.toString(distanceData), "String");
        
        // change rejected, revert to the previous value
        if(!callChangeListener(newValue)){
            seekBar.setProgress(mCurrentValue - mMinValue); 
            return; 
        }

        // change accepted, store it
        mCurrentValue = newValue;
        mStatusText.setText(distanceDataSTR);
        persistInt(newValue);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        notifyChanged();
    }


    @Override 
    protected Object onGetDefaultValue(TypedArray ta, int index){
        int defaultValue = ta.getInt(index, DEFAULT_VALUE);
        return defaultValue;
    }


    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

    	if(restoreValue) 
    		mCurrentValue = getPersistedInt(mCurrentValue);
    	else {
    		int temp = 0;
    		try {
    			temp = (Integer)defaultValue;
    		} catch(Exception ex) {
    			Log.e(Constants_API.TAG, TAG_Class + ": Invalid default value: " + defaultValue.toString());
    		}

    		persistInt(temp);
    		mCurrentValue = temp;
    	}

    }
    
  
    /**
     * Convert the distance to a string of distance plus its units e.g. m or km 
     *     
     * @param distanceData
     * @return
     */
    public String DistanceToText(int distanceData){
    	String res = "5000 m";
    	
		if (distanceData > 10000 && distanceData < 4000000 )
			res = Float.toString(distanceData/1000) + " km";
        else if (distanceData < 10000)
        	res = Integer.toString(distanceData) + " m";
        else if (distanceData == 4000000)
        	res = "Inf";
        
		return res;
	}
    

    /**
     * Save a value to preferences, either string or boolean
     * 
     * @param key       name of the parameters to save
     * @param value     value of the parameter to save 
     * @param type      either "String" or "Boolean" 
     */
	private void SavePreferences(String key, Object value, String type){
		SharedPreferences shPrefs = PreferenceManager.getDefaultSharedPreferences(Activity_TabHost.ctx);
		SharedPreferences.Editor editor = shPrefs.edit();

		if (type.equals("String")) 
			editor.putString(key, (String) value);
		else 
			editor.putBoolean(key, (Boolean) value);

		editor.commit();
	}
    
    
}
