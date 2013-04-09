/** Vote */
package com.mk4droid.IMC_Constructors;

/**
 * Construct a vote consisting of the vote id and the issue id that the vote is for 
 * 
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */

public class Vote {
	
        /** Vote unique identifier */
		public int _voteid;
		
		/** Issue that this vote was for */
		public int _issueid;
				
		public Vote(){}
		
		public Vote(int voteid, int issueid){
		
		   this._voteid       = voteid;
		   this._issueid      = issueid;
		}
		   
		
	

}
