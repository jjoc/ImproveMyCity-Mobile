/** VersionDB */

package com.mk4droid.IMC_Constructors;

/**
 *  Database (remote MySQL) version object consists of an id and the timestamp of this version. 
 *   
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class VersionDB {

	/** Identification of the version of the DB */
	public int _id;
	
	/** Timestamp of the version of the DB */
	public String _time;
		
	public VersionDB(){}
	
	public VersionDB(int id, String time){
	
	   this._id          = id;
	   this._time        = time;
   }

}