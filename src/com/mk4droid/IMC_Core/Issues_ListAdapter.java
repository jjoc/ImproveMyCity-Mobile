/** Issues_ListAdapter */
package com.mk4droid.IMC_Core;

import java.io.IOException;
import java.util.ArrayList;

import com.mk4droid.IMCity_PackDemo.R;
import com.mk4droid.IMC_Activities.Fragment_List;
import com.mk4droid.IMC_Constructors.IssuePic;
import com.mk4droid.IMC_Constructors.IssueListItem;
import com.mk4droid.IMC_Services.Download_Data;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Utils.My_Date_Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 *   List of issues adapter.
 * 
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Issues_ListAdapter extends ArrayAdapter<IssueListItem>{
		
	Context context; 
    int layoutResourceId;    
    ArrayList<IssueListItem> data = null;
    
    public Issues_ListAdapter(Context context, int layoutResourceId, ArrayList<IssueListItem> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        IssueHolder holder = null;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new IssueHolder();
            holder.txtTitle      = (TextView)row.findViewById(R.id.txtTitle);
            holder.txtState      = (TextView)row.findViewById(R.id.txtState);
            holder.txtAddress    = (TextView)row.findViewById(R.id.txtAddress);
            holder.txtReported   = (TextView)row.findViewById(R.id.txtReported);
            holder.txtVotes      = (TextView)row.findViewById(R.id.txtVotes);
            holder.imgIcon       = (ImageView)row.findViewById(R.id.imgIcon);
            
            
            row.setTag(holder);
        }
        else
        {
            holder = (IssueHolder)row.getTag();
        }
        
        IssueListItem litem = data.get(position);
        
        holder.txtTitle.setText(litem._id +" "+ litem._title);   
        
        if (litem._currstate==1){
        	holder.txtState.setText(Fragment_List.resources.getString(R.string.OpenIssue));
        	holder.txtState.setTextColor(Fragment_List.resources.getColor(R.color.op));
        }else if (litem._currstate==2){
        	holder.txtState.setText(Fragment_List.resources.getString(R.string.AckIssue));
        	holder.txtState.setTextColor(Fragment_List.resources.getColor(R.color.ack));
        }else if (litem._currstate==3){
        	holder.txtState.setText(Fragment_List.resources.getString(R.string.ClosedIssue));
        	holder.txtState.setTextColor(Fragment_List.resources.getColor(R.color.cl));
        }
        
        holder.txtAddress.setText(litem._address);
        
        //------------- Reported by Author and XX days ago ----------
        String TimeStampRep = litem._reported;
        TimeStampRep        = TimeStampRep.replace("-", "/");
        
        holder.txtReported.setText( Fragment_List.resources.getString(R.string.Reported) + " "+ 
        		My_Date_Utils.SubtractDate(TimeStampRep) + " " + 
        		Fragment_List.resources.getString(R.string.ago));
        
        // -------------------------------------------------------------
        holder.txtVotes.setText(Fragment_List.resources.getString(R.string.Votes) + " " + litem._votes);
        
        int IssueID = Integer.parseInt( litem._id.substring(1,litem._id.length()) );
                
        
        Bitmap bm = null;
        
        if (!litem._photo.equals(null))
            bm = ReceiveThumb(IssueID, litem._photo);
        
        if (bm != null){ 
        	holder.imgIcon.setVisibility(View.VISIBLE);
        	
        	if (bm.getHeight() > 100)
        		bm = Bitmap.createScaledBitmap(bm, 100, 50, false);
        	
        	holder.imgIcon.setImageBitmap(bm); //litem._icon);
        }    
        else 
        	holder.imgIcon.setVisibility(View.GONE);
        
        return row;
    }
    
    
    /**  Holder holds the widgets as defined in the custom item layout */
    static class IssueHolder{
        ImageView imgIcon;
        TextView txtTitle;
        TextView txtState;
        TextView txtAddress;
        TextView txtReported;
        TextView txtVotes;
    }
	
    /**
     * Get thumbnail of the issue. If does not exist in the SQLite, then download and store it in the SQLite.
     * 
     * @param IssueID
     * @param IssueTPhotoSTR  Photo URL
     * @return the bitmap of the issue thumbnail
     */
    public Bitmap ReceiveThumb(int IssueID, String IssueTPhotoSTR){
    	
		IssuePic mIssueThumb = Service_Data.dbHandler.getIssueThumb(IssueID);
		
		Bitmap bm = null;
		byte[] bmBytes = null;
		
		// NOT EXISTS IN DB = GET FROM INTERNET and add to DB
		if (mIssueThumb._id == -1){    
			if (Service_Data.HasInternet ){
				int N_PString = IssueTPhotoSTR.length();
				String EXT = "";
				if (N_PString > 0)
					EXT = IssueTPhotoSTR.substring(N_PString-3, N_PString);

				if (EXT.equalsIgnoreCase("jpg")){

					try{
						bmBytes = Download_Data.Down_Image(
								Constants_API.COM_Protocol + Constants_API.ServerSTR + Constants_API.remoteImages +  IssueTPhotoSTR );
						bm = BitmapFactory.decodeByteArray(bmBytes, 0, bmBytes.length);
					} catch (Exception e){}

					try{
						Service_Data.dbHandler.addUpdIssueThumb(IssueID, bmBytes);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} 
		} else { // Exists in DB: GET FROM DB
			bmBytes = mIssueThumb._IssuePicData;
			bm = BitmapFactory.decodeByteArray(bmBytes, 0, bmBytes.length);
		}
    	
		return bm;
    }
}





    
