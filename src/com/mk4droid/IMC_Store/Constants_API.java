/** Constants_API */
package com.mk4droid.IMC_Store;

/**
 * Customization for your application is feasible here.
 * HINT: Be careful when to use http:// or https:// in downloading and uploading data. It highly depends on your platform
 * HINT2: Be careful with the paths of your remote server. Paths may differ per installation configuration.

 * Constants visible everywhere in the app.
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class Constants_API {
	
	/** TAG for Log of messages to alleviate debugging  */
	public static String TAG = "ImproveMyCity";
		
	//==================== Communication parameters =================
	/** Transmitting protocol */
	public static String COM_Protocol   = "http://";
	
	/** Secure transmitting protocol. If your server supports SSL then this should be https:// */
	public static String COM_Protocol_S = "http://"; 
	
    /** Server address. It can be a XXX.XXX.XXX.XXX address instead */
	public static String ServerSTR      =  "smartcitydemos.urenio.org";
	
	/** Server path of application */
	public static String phpExec        = "/";
	
	/** Server path of issue images */ 
	public static String remoteImages      = "/";
	
	/** Encryption key for transmitting password (16 digits). It should be the same as in your ImproveMyCity joomla component. 
	 * The default value is 1234567890123456.
	 * */
	public static String EncKey =  "1234567890123456"; 
		
	/**
	 * Default menu language, options: "en - English" or  "el - Ελληνικά"
	 * 
	 * Go also to res/myprefs.xml to android:key="LanguageAR" and set 
	 * android:defaultValue="el - Ελληνικά"  or "en - English" in order to have a correct initial value for the radius buttons.
	 */
	public static String DefaultLanguage = "en - English";
	
	
	//====== GEOGRAPHIC  Limits ============
	/** Geographical limits (rectangle) from where issues can be sent 
    * Define the upper left and the lower right corners of the rectangle by setting the values of (LatMax, LatMin, LonMax, LongMin)               
    * These values can be easily obtained by GoogleMaps by right-clicking on the desired points and selecting “What is here”.
    * */ 
	                                        // {LatitudeMax ,  LatitudeMin ,  LongitudeMax ,  LongitudeMin}              
    public static double[] AppGPSLimits =   {90,        -90,    180,     -180};

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
     * Insert your key in res/string.xml -> google_maps_api_key
     * 
     * You should have two map api keys: 1) For debugging related to your android debug key 
     * 2) For release version related to your android release key. 
     * 
     * See in android developer about how to generate map api keys according to your android key.  
     */
}
