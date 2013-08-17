/** Constants_API */
package com.mk4droid.IMC_Store;

/**
 * Customization for your application is feasible here.
 * HINT: Be careful when to use http:// or https:// in downloading and uploading data. It highly depends on your platform
 * HINT2: Be careful with the paths of your remote server. Paths may differ per installation configuration.

 * Constants visible everywhere in the app.
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @authors     Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 * @tunning     OCP MASTER (Big Things come from Small Dreams)(http://smalldreams.org)
 */
public class Constants_API {
	
	/** TAG for Log of messages to alleviate debugging  */
	public static String TAG = "ImproveMyCity";
		
	//==================== Communication parameters =================
	/** Transmitting protocol */
	public static String COM_Protocol   = "http://";
	
	/** Secure transmitting protocol. If your server supports SSL then this should be https:// */
	public static String COM_Protocol_S = "https://"; 
	
    /** Server address. It can be a XXX.XXX.XXX.XXX address instead */
	public static String ServerSTR      =  "citizenship.es/alerts";
	
	/** Server path of application */
	public static String phpExec        = "/httpdocs/alerts/components/com_improvemycity";
	
	/** Server path of issue images */ 
	public static String remoteImages      = "/httpdocs/alerts/images/improvemycity/638/images";
	
	/** Encryption key for transmitting password (16 digits). It should be the same as in your ImproveMyCity joomla component. 
	 * The default value is 1234567890123456.
	 * */
	public static String EncKey =  "013SagittariuS32"; 
		
	/**
	 * Default menu language, options: "en - English" or  "el - ÅëëçíéêÜ"
	 * 
	 * Go also to res/myprefs.xml to android:key="LanguageAR" and set 
	 * android:defaultValue="el - ÅëëçíéêÜ"  or "en - English" in order to have a correct initial value for the radius buttons.
	 */
	public static String DefaultLanguage = "es - Spanish";
	
	
	//====== GEOGRAPHIC  Limits ============
	/** Geographical limits (a polygon) from where issues can be sent are located at /raw/polygoncoords.txt
    * polygon is created by Latitude,Longitude pairs. Latitude is separated with comma from Longitude. Points are separated with white space, e.g. 
    * 39.674956,-0.204728                
    *                
    * HINT: The polygon should be closed, i.e. first point should match last point               
    * HINT2: Latitude and Longitude can be obtained by from GoogleMaps by right-clicking on the desired points and selecting “What is here”.
    * */ 
	

    //============ Gather usage analytics ================
	/** Key for Flurry analytics that monitors usage of application see www.flurry.com 
	 *  Use your own key. 
	 *  
	 *  To set default value (enable or disable) Flurry analytics see at res/myprefs.xml go to 
	 *     
	 * android:key="AnalyticsSW"
	 * 
	 * and set 
	 * android:defaultValue="true" or "false"
	 *     
	 */
	 public static String Flurry_Key = "00000000000000000000";

	
    //====== Google map api key ====================
    /**
     * Insert your google maps v2 key in AndroidManifest.xml -> 
     *     <meta-data android:name="com.google.android.maps.v2.API_KEY"  android:value="XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"/>
     * 
     * You should have two map api keys: 1) For debugging related to your android debug key 
     * 2) For release version related to your android release key. 
     * 
     * See in android developer about how to generate map api keys according to your android key.  
     */
}

