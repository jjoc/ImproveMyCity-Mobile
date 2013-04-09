/** IssuePic */
package com.mk4droid.IMC_Constructors;

/**
 * Issue Picture as an object
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class IssuePic {
	
	 /** Unique identifier of the issue that this picture belongs to */
	 public int _id;	 
	 
	 /** Image data as a byte array */
	 public byte[] _IssuePicData;
	
	// Empty constructor
	public IssuePic(){ 	}
		
	// constructor
	public IssuePic(int id, byte[] IssuePicData){
		this._id           = id;  
		this._IssuePicData = IssuePicData;
	}
}





