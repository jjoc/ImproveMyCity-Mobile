/**   Comment  */
package com.mk4droid.IMC_Constructors;

import java.util.Date;

/**
 * A structure for storing comments.
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class Comment {

	/** Unique identification of the comment */
	public int _id;
	
	/** Unique identification of the issue that this comments is addressed to */ 
	public int _issueid;
	
	/** Unique identification of the user created this comment */
	public int _userid;
	
	/** When this comment was created */
	public Date _created;
	
	/** Comment as textual information  */
	public String _description;
	
	/** Name of the user that created this comment */
	public String _username;
	
	public Comment(){}
	
	public Comment(int id, int issueid, int userid, Date created, String description,
			      String username){
	
	   this._id            = id;
	   this._issueid       = issueid;
	   this._userid        = userid;
	   this._created       = created;
	   this._description   = description;
	   this._username      = username;
   }

}