/** MapView_ZoomTap */
package com.mk4droid.IMC_Core;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.google.android.maps.MapView;


/** 
 * Google map view with double-tap zooming capability.
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class MapView_TapZoom extends MapView {
    private long lastTouchTime = -1;

    
    public MapView_TapZoom(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     *   Detect double tap and zoom in
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            long thisTime = System.currentTimeMillis();
            if (thisTime - lastTouchTime < ViewConfiguration.getDoubleTapTimeout()) {
                // Double tap
                this.getController().zoomInFixing((int) ev.getX(), (int) ev.getY());
                lastTouchTime = -1;
            } else {
                // Too slow :)
                lastTouchTime = thisTime;
            }
        }

        return super.onInterceptTouchEvent(ev);
    }
}