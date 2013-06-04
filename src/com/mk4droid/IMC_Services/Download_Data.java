/** Download_Data */
package com.mk4droid.IMC_Services;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.mk4droid.IMC_Constructors.Comment;
import com.mk4droid.IMC_Constructors.VersionDB;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Store.Phptasks;
import com.mk4droid.IMC_Utils.My_Date_Utils;
import com.mk4droid.IMC_Utils.RestClient;
import com.mk4droid.IMC_Utils.RestClient.RequestMethod;

import android.util.Log;

/**
 * This class performs any download of data from remote server
 *
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Download_Data {
		
	String TAG_Class = getClass().getName();
	
	//================  RestCaller          =======================================
    /**
     * Call REST (http with get or post) with specified parameters
     * 
     * @param url the url to call
     * @param rm  RestClient.RequestMethod.GET or RestClient.RequestMethod.POST 
     * @param args Pairs of parameters to send with php {"Latitude","40.5","Longitude","23"}      
     * @param encoding  "ANSI" or "UTF-8" to support multiple languages. HINT: utf-8 increases about x6 times the amount 
     *                     of transfered data 
     * @return
     */
	public static String RestCaller(String url, RequestMethod rm, String[] args, String encoding ){ // String mfile
		

		
		
		RestClient clientR = new RestClient(url, encoding);

		// args should be pairs of Strings
		if (args!=null)
			for (int i=0; i< args.length; i=i+2) 
				clientR.AddParam(args[i], args[i+1]);
		
    	try {
    	    clientR.Execute(rm);
    	} catch (Exception e) {
    		Log.e(Constants_API.TAG, "Download_Data:RestCaller:"+ e.getMessage());
    	}
		
		return clientR.getResponse();
	}
	
	//================  Download_Categories  =======================================
	/**
	 * Download Categories
	 * 
	 * @return as JSON
	 */
	public static String Download_Categories(){
	
	 String response =  Download_Data.RestCaller(
			Constants_API.COM_Protocol + Constants_API.ServerSTR + Constants_API.phpExec, 
					        RestClient.RequestMethod.GET,  
					        new String[]{
										"option","com_improvemycity",
										"task", Phptasks.TASK_GET_CATEG,
										"format","json"}, "UTF-8");
	 
	 return response;
	}

	//================ Download_Version  =======================================
	/**
	 * Download remote database issues version
	 * 
	 * @return as JSON
	 */
	public static String Download_Version(){

		String response =  Download_Data.RestCaller(
				Constants_API.COM_Protocol + Constants_API.ServerSTR + Constants_API.phpExec ,
				RestClient.RequestMethod.GET,
				new String[]{"option","com_improvemycity",
						"task", Phptasks.TASK_GET_VERSION, 
						"format","json"}, "UTF-8");
		return response;
	}
    
	//================  Download_CategVersion  =======================================
	/**
	 * Download categories version of remote database 
	 * 
	 * @return as JSON
	 */
	public static String Download_CategVersion(){
		
		String response =  Download_Data.RestCaller(
				Constants_API.COM_Protocol + Constants_API.ServerSTR + Constants_API.phpExec ,
				RestClient.RequestMethod.GET,
				new String[]{"option","com_improvemycity",
						"task", Phptasks.TASK_GET_CATEGVERSION, 
						"format","json"}, "UTF-8");

		return response;
	}

	//================ Download_Issues  =======================================
	/**
	 * Download issues in a certain geographic rectangle 
	 * 
	 * @param x0down       minimum longitude of the rectangle
	 * @param x0up         maximum longitude of the rectangle
	 * @param y0down       minimum latitude of the rectangle 
	 * @param y0up         maximum latitude of the rectangle
	 * @param IssueNolimit  maximum number of issues to download within this rectangle starting from the most recent ones
	 * @return
	 */
	public static String Download_Issues(double x0down, double x0up, double y0down, double y0up, int IssueNolimit){

		String response =  Download_Data.RestCaller(
				Constants_API.COM_Protocol + Constants_API.ServerSTR + Constants_API.phpExec,
				RestClient.RequestMethod.GET,  new String[]{"option","com_improvemycity",
						"task", Phptasks.TASK_GET_ISSUES, //_Zipped 
						"format","json",
						"x0down", Double.toString(x0down),
						"x0up",  Double.toString(x0up),
						"y0down", Double.toString(y0down),
						"y0up",   Double.toString(y0up),        		                             
						"limit", Integer.toString(IssueNolimit)}, "UTF-8"); 

			
		
		return response;
	}

	//================ Download_UserVotes  =======================================
	/**
	 * Download authorized user votes
	 * 
	 * @param UserNameSTR
	 * @param PasswordSTR
	 * @return
	 */
    public static String Download_UserVotes(String UserNameSTR, String PasswordSTR){
	
	    String response =  Download_Data.RestCaller(
	    		Constants_API.COM_Protocol + Constants_API.ServerSTR + Constants_API.phpExec,
			                 RestClient.RequestMethod.GET,
			                 new String[]{"option","com_improvemycity",
					                      "task", Phptasks.TASK_GET_USER_VOTES, 
					                      "format","json",
					                      "username", UserNameSTR    ,
					                      "password", Security.EncWrapper(PasswordSTR)    }, "UTF-8");
	
	   return response;
    }
           
    
	//================  Download_CommentsByIssueID  =======================================
    /**
     * Get comments on a certain issue
     * 
     * @param IssueID
     * @return
     */
	public static ArrayList<Comment> Download_CommentsByIssueID(int IssueID) {
		
		ArrayList<Comment> mCommentL = new ArrayList<Comment>();
		
		//------ http post to send preferences and receive results --------------
        String response =  Download_Data.RestCaller(
        		Constants_API.COM_Protocol +Constants_API.ServerSTR + Constants_API.phpExec,
                               RestClient.RequestMethod.GET,  
                               new String[]{"option","com_improvemycity",
                    		                "task", Phptasks.TASK_GET_ISSUE,
                    		                "format","json",
                    		                "issueId", Integer.toString(IssueID),
                    		                "showComments", "1"}, "UTF-8");
		
		try{
			JSONObject jOIssue   = new JSONObject(response);
			
            JSONArray jArray = new JSONArray(jOIssue.getString("discussion"));
            int NComments = jArray.length();
            
            for(int i=0; i<NComments; i++){
            	JSONObject jO = jArray.getJSONObject(i);
            	int id          = jO.getInt("id"); 
            	int issueid     = jO.getInt("improvemycityid"); 
            	int userid      = jO.getInt("userid"); 
            	String created     = jO.getString("created"); 
            	String description = jO.getString("description"); 
            	String fullname    = jO.getString("fullname");

            	Comment mComment = new Comment(id, issueid, userid,	My_Date_Utils.ConvertToDate(created), description, fullname);

				mCommentL.add(mComment);
            }
		}catch(JSONException e){
			Log.e(Constants_API.TAG, "Error parsing data "+e.toString());
		}                  	

		return mCommentL;
	}
    
    
	//================ DownloadCategTimeStamp =======================================
    /**
     * Download from remote database the version (timestamp) of categories 
     *     
     * @return
     */
    public static VersionDB DownloadCategTimeStamp(){
        
        String result = null;
        if (Service_Data.HasInternet)
        	result =  Download_CategVersion();
        else 
        	return null;
        
        VersionDB mVersionDB = new VersionDB(0, "");
        
		try {
			JSONArray jArray = new JSONArray(result);
			mVersionDB = new VersionDB( jArray.getInt(0), jArray.getString(1) );
		} catch (JSONException e) {
			
			Log.e(Constants_API.TAG, "Download_Data: DownloadCategTimeStamp: "+ result +" "+ e.getMessage());
		}
        return mVersionDB;
    }
    
	//================ DownloadTimeStamp =======================================
    /**
     * Download from remote database the version (timestamp) of issues 
     *     
     * @return
     */
    public static VersionDB DownloadTimeStamp(){
        
        String result = null;
        if (Service_Data.HasInternet)
        	result =  Download_Version();
        else 
        	return null;
        
        VersionDB mVersionDB = new VersionDB(0, "");
        
		try {
			JSONArray jArray = new JSONArray(result);
			mVersionDB = new VersionDB( jArray.getInt(0), jArray.getString(1) );
		} catch (JSONException e) {
			Log.e(Constants_API.TAG, "Download_Data: DownloadTimeStamp: " + e.getMessage());
		}
        return mVersionDB;
    }
    
    
	//================ Down_Image =======================================                    
    /**
     * Download Image from a certain url
     * 
     * @param fullPath the url of the image
     * @return
     */
    public static byte[] Down_Image(String fullPath){
    	
    	try{
    		//----- Split----
    		String[] AllInfo = fullPath.split("/");
    		
    		// Encode filename as UTF8 -------
    		String fnExt = AllInfo[AllInfo.length-1];
    		
    		String fnExt_UTF8 = URLEncoder.encode(fnExt, "UTF-8");  
    		
    		//- Replace new fn to old
    		AllInfo[AllInfo.length-1] = fnExt_UTF8;
    		
    		//------ Concatenate to a single string -----
    		String newfullPath = AllInfo[0];
    		for (int i=1; i< AllInfo.length; i++)
    			newfullPath += "/" + AllInfo[i];
    		
    		// empty space becomes + after UTF8, then replace with %20
    		newfullPath = newfullPath.replace("+", "%20");
    		
    		//------------ Download -------------
    		URL myFileUrl= new URL( newfullPath );
    		
    		HttpURLConnection conn= (HttpURLConnection)myFileUrl.openConnection();
    		conn.setDoInput(true); 
    		
    		conn.connect();
    		InputStream isBitmap = conn.getInputStream();

    		return readBytes(isBitmap);

    	}catch(Exception e){
    		Log.e(Constants_API.TAG, "Download_Data: Down_Image: Error in http connection "+e.getMessage());
    		return null;
    	}
    	
    }
    
	//================ readBytes =======================================
    /**
     * Convert inputStream to byte array using a buffer
     * 
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readBytes(InputStream inputStream) throws IOException {
    	// this dynamically extends to take the bytes you read
    	ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

    	// this is storage overwritten on each iteration with bytes
    	int bufferSize = 1024;
    	byte[] buffer = new byte[bufferSize];

    	// we need to know how may bytes were read to write them to the byteBuffer
    	int len = 0;
    	while ((len = inputStream.read(buffer)) != -1) 
    		byteBuffer.write(buffer, 0, len);
    	
    	// and then we can return your byte array.
    	return byteBuffer.toByteArray();
    }
}