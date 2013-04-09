/* My_System_Utils */
package com.mk4droid.IMC_Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.mk4droid.IMC_Store.Constants_API;

import android.util.Log;

/**
 * Android system utilities: Copy a file
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class My_System_Utils {
   
	//------------- Copy file to temp -------------
	/**
	 * Copy a file locally from Source to Target paths
	 * 
	 * @param fnameS
	 * @param fnameT
	 */
	public static void FCopy(String fnameS, String fnameT){

		try{
			InputStream in=new FileInputStream(fnameT);
			OutputStream out = new FileOutputStream(new File(fnameS));                       

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			in = null;

			// write the output file
			out.flush();
			out.close();
			out = null;
		}  catch (FileNotFoundException fnfe1) {
			Log.e(Constants_API.TAG, "My_System_Utils" + fnfe1.getMessage());
		}
		catch (Exception e) {
			Log.e(Constants_API.TAG, "My_System_Utils" + e.getMessage());
		}
		//------------------------------------
	}

}


//============ Compression schemes for future use ===================

////============= UTFtoString ================
///** 
// * Convert UTF-8 word to string word
// */
//public static String UTFtoString(String myString){ 
//	
//	String str = myString.split(" ")[0];
//	str = str.replace("\\","");
//	String[] arr = str.split("u");
//	String text = "";
//	for(int i = 1; i < arr.length; i++){
//		int hexVal = Integer.parseInt(arr[i], 16);
//		text += (char)hexVal;
//	}
//	// Text will now have Hello
//	return text;
//}


///**
// * Decompresses a zlib compressed string.
// */
//public static String decompress(String compressedSTR, String Encoding)
//{
//	
//	
//	byte[] compressedBytes = null; // Compress(Encoding);
//	try {
//		compressedBytes = compressedSTR.getBytes(Encoding);
//		Log.e("compressedBytes", new String(compressedBytes,Encoding));
//	} catch (UnsupportedEncodingException e1) {
//		e1.printStackTrace();
//	}
//	
//	// ----------------  Decompress the bytes ----------
//	 int uncompressedBufferLength = 0;
//	 Inflater decompresser = new Inflater(false);
//	 
//	 decompresser.setInput(compressedBytes, 0, compressedBytes.length);
//
//	 byte[] uncompressedBuffer = new byte[700];
//	 try {
//		 uncompressedBufferLength =	 decompresser.inflate(uncompressedBuffer);
//		 Log.e("resultLength", "A " + Integer.toString(uncompressedBufferLength));
//	 } catch (DataFormatException e) {
//		 Log.e("DataFormatException","A " + e.getCause());
//		 e.printStackTrace();
//	 }
//	 decompresser.end();
//	
//	 //-------------- Convert bytes to String ----------
//	 String resSTR = "";
//	try {
//		resSTR = new String(uncompressedBuffer,Encoding);
//	} catch (UnsupportedEncodingException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	 Log.e("result", resSTR);
//    
//    return resSTR;
//}


//public static byte[] Compress(String Encoding){
//
//	//// Encode a String into bytes
//	 //String inputString = "blahblahblah??";
//	   
//	  String inputString = ReadFile("mnt/sdcard/data/result.txt");
//	   
//	 byte[] input = null;
//	 try {
//	 	input = inputString.getBytes(Encoding);
//	 } catch (UnsupportedEncodingException e1) {
//	 	e1.printStackTrace();
//	 }
//	 
//	 // Compress the bytes
//	 byte[] output = new byte[500000];
//	 Deflater compresser = new Deflater();
//	 compresser.setInput(input);
//	 compresser.finish();
//	 int compressedDataLength = compresser.deflate(output);
//	
//	  
//	 Log.e("When compressed then take bytes=", Integer.toString(compressedDataLength ));
//	 
//	 
//	 
//	 return output;
//   }
//    
//   //------------- ReadFile from sdcard ----------------
//   public static String ReadFile(String path){
//	   
//	   File file = new File(path);
//	   StringBuilder text = new StringBuilder();
//
//	   try {
//	       BufferedReader br = new BufferedReader(new FileReader(file));
//	       String line;
//
//	       while ((line = br.readLine()) != null) {
//	           text.append(line);
//	       }
//	   }
//	   catch (IOException e) {
//	   }
//
//	   return text.toString();
//   }