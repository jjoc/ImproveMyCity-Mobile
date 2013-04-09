/* Activity_NewIssueB */
package com.mk4droid.IMC_Activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;
import com.mk4droid.IMC_Services.Security;
import com.mk4droid.IMC_Services.Service_Location;
import com.mk4droid.IMC_Services.Upload_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Utils.GEO;
import com.mk4droid.IMCity_Pack.R;

/**
 * Second activity for submitting an issue: selecting the location of the issue and submit to remote server  
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class Activity_NewIssueB extends MapActivity implements View.OnClickListener{
	
	public static ProgressDialog dialogNewIssue;
	Handler handlerBroadcastNewIssue;
	
	public static Handler handlerSubmitButtonDisable;
	
	// ---------- WINDOW --------
	Resources resources;
	DisplayMetrics metrics;
	Context ctx;
	
	//----------- GPS -----------
	private MyLocationOverlay me=null;
	private MapView map=null;
	double Long_D =  Service_Location.locUser.getLongitude();  // REM: Get From GPS 
	double Lat_D  =  Service_Location.locUser.getLatitude();
	GeoPoint pt;
	String Address_STR = "";
	Handler handlerAddresUPD;
	private BallOverlay sitesOverlay;
	private Bitmap resBm;
	
	//------------VARs ------------
	Button btRep;
	boolean AuthFlag;
	String UserNameSTR,PasswordSTR,UserID_STR;
	EditText etAddress;
	int IndexCatSpinner;
	
	//============== OnCreate ==================
	/**
	 *  Set content view. 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    Bundle mBundle = getIntent().getExtras();
        IndexCatSpinner   = mBundle.getInt("IndexSpinner"); // Header for GUI
	    
	    resources = SetResources();
	    setContentView(R.layout.activity_newissue_b);
        ctx = this;	    
    
        
        AuthFlag                = Security.AuthFun(UserNameSTR, PasswordSTR, resources, ctx); //mshPrefs.getBoolean("AuthFlag"  , false);
        
        btRep = (Button) findViewById(R.id.btReport_new_issue);
        
	    //====================================================
	    // --------- GPS: Find current Location -----------
	    //==================================================
	    
	    //----- Handler for setting address string ------------
        handlerAddresUPD = new Handler()
        {
           public void handleMessage(Message msg)
           {
              if (msg.arg1 == 1) // Redraw markers
              {
           	    etAddress = (EditText) findViewById(R.id.etAddress);
         	    etAddress.setText(Address_STR);
	          }
              super.handleMessage(msg);
           }
        };
		
		pt = new GeoPoint((int) (Lat_D*1E6), (int) (Long_D*1E6));
        Address_STR = GEO.ConvertGeoPointToAddress(pt, ctx);
        Message msg = new Message();
        msg.arg1 = 1;
		handlerAddresUPD.sendMessage(msg);
	        
		//=============================================
		//             handlerSubmitButtonDisable
		//=============================================
		handlerSubmitButtonDisable = new Handler()
        {
           public void handleMessage(Message msg)
           {
              if (msg.arg1 == 1) // Redraw markers
              {
            		Toast.makeText(ctx, resources.getString(R.string.Sending),Toast.LENGTH_LONG).show();
			    	Button btReport_new_issue = (Button)findViewById(R.id.btReport_new_issue); 
					btReport_new_issue.setEnabled(false);
					btReport_new_issue.setVisibility(View.GONE);
					btReport_new_issue.invalidate();
	          }
              super.handleMessage(msg);
           }
        };

        //=============================================
        //            MapViewer
	    //=============================================
	    map=(MapView)findViewById(R.id.mapview_new_issue);
	    map.getController().setCenter(getPoint(Lat_D,Long_D));
	    map.getController().setZoom(17);
	    map.setBuiltInZoomControls(true);
	 
	    //--------------------- Marker -------------------
	    float ratioScr = ((float) metrics.widthPixels) /480f;
	    if (ratioScr<1)
	    	ratioScr = ratioScr/2; 
	    	    
	    Canvas canvas;	
	    
	    //----------- balloon ---------   
	    Drawable marker=getResources().getDrawable(R.drawable.hereballoon);
	    marker.setBounds((int) (60*ratioScr), (int) (80*ratioScr), (int) (160*ratioScr), (int) (220*ratioScr)); 

	    //---------------- Text ----------------
	    resBm = Bitmap.createBitmap((int) (230f*ratioScr), (int) (230f*ratioScr), Bitmap.Config.ARGB_8888);
	    canvas = new Canvas(resBm);
	    Paint paintText = new Paint();

	    Paint paintRect = new Paint();
	    paintText.setColor(Color.BLACK);
	    paintText.setAntiAlias(true);
	    paintText.setTextSize(40*ratioScr);
	    paintRect.setColor(Color.TRANSPARENT); 


	    canvas.drawRect(23*ratioScr, 0, 200*ratioScr, 55*ratioScr, paintRect);
	    canvas.drawText(resources.getString(R.string.DragMarker), 37*ratioScr, 40*ratioScr, paintText);
        //--------------------------------------

	    canvas.save();
	    marker.draw(canvas);
	    
	    sitesOverlay = new BallOverlay(new BitmapDrawable(resBm));
	    
	    map.getOverlays().add(sitesOverlay);
	    
	    //------------- My Position ---------
	    me=new MyLocationOverlay(this, map);
	    map.getOverlays().add(me);
	    
	    me.enableMyLocation();
	    		
		// Animate map to curr position
		map.getController().animateTo(pt);  
        map.invalidate();
        
		//-------- Broadcast new Issue was send through a handler ---------
        handlerBroadcastNewIssue = new Handler()
        {
           public void handleMessage(Message msg)
           {
              if (msg.arg1 == 1) // Refresh Button
            	  sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("NewIssueAdded", "ok"));  
              
              super.handleMessage(msg);
           }
        };
		
        
        //--------------- Versionify
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.FROYO){
        	Button handleSL = (Button) findViewById(R.id.handle_Address);
        	handleSL.setBackgroundDrawable(
        			getResources().getDrawable(R.drawable.shape_convex_right_andr2));
        }
        
        
        SlidingDrawer sdAddress = (SlidingDrawer) findViewById(R.id.slidingDrawer_Address);
        sdAddress.open();
	}
	
	
	
	//============== On Resume =====================
	/**
	 * Executed after activity is created or after changing tab
	 */
	@Override
	public void onResume() {
		super.onResume();

		resources = SetResources();

		//----------- Remove old market and put a new one on current position -----
		map.setVisibility(View.VISIBLE);
		map.getOverlays().remove(sitesOverlay);

		Long_D = Service_Location.locUser.getLongitude();
		Lat_D  = Service_Location.locUser.getLatitude();

		sitesOverlay = new BallOverlay(new BitmapDrawable(resBm));
		map.getOverlays().add(sitesOverlay);


		//----------- Flurry Analytics --------
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);

		if (AnalyticsSW)
			FlurryAgent.onStartSession(this, Constants_API.Flurry_Key);
	}


	//================= onPause =========================
	/**
	 *    Hinter map visibility and stop Flurry analytics
	 */
	@Override
	public void onPause() {
		super.onPause();

		btRep.setText(resources.getString(R.string.ReportIss));
		btRep.setEnabled(true);
		map.setVisibility(View.GONE);
		me.disableMyLocation();

		//----------- Flurry Analytics --------
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);

		if (AnalyticsSW)
			FlurryAgent.onEndSession(this);
	}  

	@Override
	protected boolean isRouteDisplayed() {
		return(false);
	}

	private GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1000000.0), (int)(lon*1000000.0)));
	}

	//================== BallOverlay ================================
	/**
	 * Drag Balloon overlay implementation
	 *  
	 * @author Dimitrios Ververidis, Dr.
	 *         Post-doctoral Researcher, 
	 *         Information Technologies Institute, ITI-CERTH,
	 *         Thermi, Thessaloniki, Greece      
	 *         ververid@iti.gr,  
	 *         http://mklab.iti.gr
	 *
	 */
	private class BallOverlay extends ItemizedOverlay<OverlayItem> {
		private List<OverlayItem> items=new ArrayList<OverlayItem>();
		private Drawable marker=null;
		private OverlayItem inDrag=null;
		private ImageView dragImage=null;
		private int xDragImageOffset=0;
		private int yDragImageOffset=0;
		private int xDragTouchOffset=0;
		private int yDragTouchOffset=0;

		public BallOverlay(Drawable marker) {
			super(marker);

			this.marker=marker;

			dragImage=(ImageView)findViewById(R.id.drag);

			if (metrics.heightPixels < 800){
				Bitmap b = ((BitmapDrawable)dragImage.getDrawable()).getBitmap();
				Bitmap bmnew = Bitmap.createScaledBitmap(b, 50, 50, false);


				Drawable drnew = new BitmapDrawable( bmnew);
				dragImage.setImageDrawable(drnew);
				dragImage.setMaxWidth(50);
				dragImage.setMaxHeight(50);
				dragImage.setPadding(0, 25, 0, 0);
			}

			xDragImageOffset=dragImage.getDrawable().getIntrinsicWidth()/2;
			yDragImageOffset=dragImage.getDrawable().getIntrinsicHeight();

			items.add(new OverlayItem(getPoint(Lat_D,Long_D), "", ""));

			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return(items.get(i));
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);
			boundCenterBottom(marker);
		}

		@Override
		public int size() {
			return(items.size());
		}


        /** On Drag marker */
		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView)  {

			final int action=event.getAction();
			final int x=(int)event.getX();
			final int y=(int)event.getY();
			boolean result=false;

			if (action==MotionEvent.ACTION_DOWN) {
				for (OverlayItem item : items) {
					Point p=new Point(0,0);

					map.getProjection().toPixels(item.getPoint(), p);

					if (hitTest(item, marker, x-p.x, y-p.y)) {
						result=true;
						inDrag=item;
						items.remove(inDrag);
						populate();

						xDragTouchOffset=0;
						yDragTouchOffset=0;

						setDragImagePosition(p.x, p.y);
						dragImage.setVisibility(View.VISIBLE);

						xDragTouchOffset=x-p.x;
						yDragTouchOffset=y-p.y;

						break;
					}
				}
			}
			else if (action==MotionEvent.ACTION_MOVE && inDrag!=null) {
				setDragImagePosition(x, y);
				result=true;
			}
			else if (action==MotionEvent.ACTION_UP && inDrag!=null) {
				dragImage.setVisibility(View.GONE);
				pt = map.getProjection().fromPixels(x-xDragTouchOffset, y-yDragTouchOffset);

				Lat_D  = pt.getLatitudeE6()/1E6;
				Long_D = pt.getLongitudeE6()/1E6;

				pt = new GeoPoint((int) (Lat_D*1E6), (int) (Long_D*1E6));
				Address_STR = GEO.ConvertGeoPointToAddress(pt, ctx);

				Message msg = new Message();
				msg.arg1 = 1;
				handlerAddresUPD.sendMessage(msg);

				if (Address_STR!=""){
					Toast myT = Toast.makeText(ctx, Address_STR, Toast.LENGTH_LONG);
					myT.setGravity(Gravity.CENTER, 0, 0);
					myT.show();
				}else{ 
					Toast myT = Toast.makeText(ctx, resources.getString(R.string.PleaseAddress), Toast.LENGTH_LONG);
					myT.setGravity(Gravity.CENTER, 0, 0);
					myT.show();	
				}
				etAddress.setText(Address_STR);

				OverlayItem toDrop=new OverlayItem(pt, inDrag.getTitle(), inDrag.getSnippet());

				items.add(toDrop);
				populate();

				inDrag=null;
				result=true;
			}

			return(result || super.onTouchEvent(event, mapView));
		}

		private void setDragImagePosition(int x, int y) {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)dragImage.getLayoutParams();

			lp.setMargins(x-xDragImageOffset-xDragTouchOffset,
					y-yDragImageOffset-yDragTouchOffset, 0, 0);
			dragImage.setLayoutParams(lp);
		}
	}

	//=========  OnClick ============= 
    /** Submit issue */
	@Override
	public void onClick(View v) {
		int id = v.getId();

		switch (id) {
		case R.id.btReport_new_issue:


			String titleData_STR       = Activity_NewIssueA.et_title.getText().toString();  // 1. Title Data
			String descriptionData_STR = Activity_NewIssueA.et_descr.getText().toString();  // 3. Description Data
			Address_STR                = etAddress.getText().toString();

			String ImageFN_target = "";


			// Check if Image is taken else do not sent  
			if (Group_NewIssue.flagPictureTaken){
				String FileNameExt = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss'.jpg'").format(new Date());

				FileNameExt  =  FileNameExt.replace("-", "_");
				FileNameExt  =  FileNameExt.replace(" ", "t");
				ImageFN_target = UserID_STR + "d" + FileNameExt;
			}


			// Check if title is long enough and sent
			if ( Activity_NewIssueA.et_title.getText().toString().length() > 2){

				if ( Lat_D>Constants_API.AppGPSLimits[1]  && Lat_D < Constants_API.AppGPSLimits[0] 
						&& Long_D>Constants_API.AppGPSLimits[3]  && Long_D <Constants_API.AppGPSLimits[2]){

					Message msgButton = new Message();
					msgButton.arg1 = 1;
					handlerSubmitButtonDisable.sendMessage(msgButton);

					boolean successIadd = false ;

					successIadd = Upload_Data.SendIssue(Group_NewIssue.image_path_source_temp, ImageFN_target, 
							titleData_STR, Activity_NewIssueA.SpinnerArrID[IndexCatSpinner],
							Lat_D, Long_D, descriptionData_STR, Address_STR, UserNameSTR, PasswordSTR );

					if (successIadd){
						btRep.setText(resources.getString(R.string.IssueReported));
						btRep.setEnabled(false);

						Activity_TabHost.mTabHost.setCurrentTab(0);
						//------- Reset GUI --------
						Group_NewIssue.flagPictureTaken = false;
						Activity_NewIssueA.btAttachImage.setCompoundDrawablesWithIntrinsicBounds(
								getResources().getDrawable(android.R.drawable.ic_menu_gallery), null, null,  null);
						//						Activity_NewIssueA.btTakeImage.setCompoundDrawablesWithIntrinsicBounds(
						//								getResources().getDrawable(android.R.drawable.ic_menu_camera), null, null,  null);

						Activity_NewIssueA.et_title.setText("");
						Activity_NewIssueA.et_descr.setText("");

						Toast.makeText(ctx, resources.getString(R.string.Reported), 
								Toast.LENGTH_LONG).show();

						Group_NewIssue.group.back();

						//---- Show dialog Refresh for new Issue ----------
						dialogNewIssue = ProgressDialog.show(Activity_TabHost.ctx, 
								resources.getString(R.string.Downloading),
								resources.getString(R.string.NewIssueCh), true);

						// ------- Send Broadcast through a handler -------
						Message msg = new Message();
						msg.arg1 = 1;
						handlerBroadcastNewIssue.sendMessage(msg);

					} else {
						Dialog md = new Dialog(Activity_TabHost.ctx);
						md.setTitle("Failed to upload issue!");
						md.show();
					}
				} else {
					Toast.makeText(Activity_NewIssueA.ctx, 
							Activity_NewIssueA.resources.getString(R.string.Issueoutofmunicipalitylimits), 
							Toast.LENGTH_LONG).show();
				}
				me.disableMyLocation();
			}else{
				Toast.makeText(Activity_NewIssueA.ctx, 
						Activity_NewIssueA.resources.getString(R.string.LongerTitle), 
						Toast.LENGTH_LONG).show();
			}
			break;
		}

	}

	//============   Set Resources =========================== 
    /** Retrieve preferences and set resources language */ 
	public Resources SetResources(){

		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String LangSTR          = mshPrefs.getString("LanguageAR", "el");
		UserID_STR              = mshPrefs.getString("UserID_STR", "");

		UserNameSTR      = mshPrefs.getString("UserNameAR", "");
		PasswordSTR      = mshPrefs.getString("PasswordAR", "");



		Configuration conf = getResources().getConfiguration();
		conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return new Resources(getAssets(), metrics, conf);
	}




}
