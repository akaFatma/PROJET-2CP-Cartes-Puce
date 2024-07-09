package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

/*
 * Copyright (c) 2006-2007 Mesure Project
 * 
 * This software is a computer program whose purpose is to measure 
 * the performances of Java Card platforms.
 *
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */

/**
 * Some useful methods.
 */
public final class Util {

	/**
	 * Returns a string representation of a byte in base 16.
	 * 
	 * @param b the byte to be represented.
	 * @return a <tt>String</tt> object representing <tt>b</tt> in base 16.
	 */
	public static String byteToHexString(byte b) {
	    StringBuffer s = new StringBuffer();
	    if ((b & 0xff) < 0x10)
	        s.append("0");
	    s.append(Integer.toHexString(b & 0xff).toUpperCase());
	    return s.toString();
	}

	/**
	 * Returns a string representation of an array of bytes in base 16
	 * 
	 * @param buffer the vector of byte to be represented
	 * @param separator the string to be used to separate bytes.
	 * @return a <tt>String</tt> object representing <tt>bvec</tt> in base 16.
	 */
	public static String byteArrayToHexString(byte[] buffer, String separator) {
	    StringBuffer s = new StringBuffer();
	    int i;
	    for (i = 0; i < buffer.length-1; i++) 
	        s.append(byteToHexString(buffer[i]) + separator);
	    if (i >= 0)
	        s.append(byteToHexString(buffer[i]));
	    return s.toString();
	}

	/**
	 * Parses the string argument as an array of bytes in base 16.
	 * @param s the string to parse.
	 * @return the byte array representing <tt>s</tt>.
	 * @throws <tt>IllegalArgumentException</tt> if once delimiters have been 
	 * discarded, the length of <tt>s</tt> is not a multiple of 2.
	 */
	public static byte[] hexStringToByteArray(String s) { 
	    if (s == null)
	        return null;
	    s = s.replaceAll(" ","").replaceAll(":","").replaceAll("0x","").replaceAll("0X","");
	    if(s.length() % 2 != 0) s = '0'+s;
	//  throw new IllegalArgumentException("The length cannot be odd.");
	    byte[] output = new byte[s.length() / 2];
	    for(int i = 0; i < s.length(); i += 2)
	        output[i/2] = (byte)Integer.parseInt(s.substring(i,i + 2),16);
	    return output;
	}

	
	/**
	 * Parses the string argument and convert it in ASCII format.
	 * @param s the string to parse and convert.
	 * @return the string representing <tt>s</tt> in ASCII format.
	 * @throws <tt>IllegalArgumentException</tt> if once delimiters have been 
	 * discarded, the length of <tt>s</tt> is not a multiple of 2.
	 * @throws <tt>UnsupportedEncodingException</tt> if the ASCII charset is not
	 * supported.
	 */
	public static String hexToASCII(String s) throws UnsupportedEncodingException {
	  byte[] buffer = hexStringToByteArray(s);
	  return new String(buffer,"ASCII");
	}
	
	/**
	 * Parses the string argument and convert it in hexedecimal format.
	 * @param s the string to parse and convert.
	 * @return the string representing <tt>s</tt> in hexedecimal format.
	 */
	public static String ASCIIToHex(String s) {
      byte[] buffer = s.getBytes();
      return byteArrayToHexString(buffer,":");
	}
	/**************************************************************************************************/
	
	public static int byteArrayToUnsignedInt(byte[] byteArray) {
        int result = 0;
        for (int i = 0; i < byteArray.length; i++) {
            result = (result << 8) + (byteArray[i] & 0xFF);
        }
        return result;
    }
	public static byte[] unsignedIntToByteArray(int unsignedIntValue) {
        return ByteBuffer.allocate(4).putInt(unsignedIntValue).array();
    }
	/**************************************************************************************************/

	public static int compareArrays(byte[] array1, byte[] array2) {
		// Vérifier si les deux tableaux ont la même longueur
		if (array1.length != array2.length) {
			return ((array1.length > array2.length) ? -1 : 1);
		}
		for (short i = 0; i < array1.length; i++) {
			if (array1[i] != array2[i]) {
				return ((array1[i] > array2[i]) ? -1 : 1);
			}
		}
		return 0;
	}
	/**************************************************************************************************/

	public static byte[] concatenateArrays(byte[] a, byte[] b) {
		byte[] result = new byte[a.length + b.length];
		for (int i = 0; i < a.length; i++) {
			result[i] = a[i];
		}
		for (int i = 0; i < b.length; i++) {
			result[a.length + i] = b[i];
		}
		return result;
	}
	
	/**************************************************************************************************/

	public static byte[] toByteArray(BigInteger value) {
		byte[] byteArray = value.toByteArray(); // If the most significant byte (index 0) is 0 due to sign extension, remove it
		if (byteArray[0] == 0) {
			byte[] trimmedArray = new byte[byteArray.length - 1];
			System.arraycopy(byteArray, 1, trimmedArray, 0, trimmedArray.length);
			return trimmedArray;
		} // end if
		return byteArray;
	}// end method

	
	/**
	 * Returns the path of a file from its URL.
	 * If the specified file is in a JAR, a temporary copy of the file is created
	 * and the path of the copy file is returned; otherwise, the path part of the
	 * URL is returned.
	 * @param url the URL of the file to be considered.
	 * @param isJar <code>true</code> if and only if the specified file is a JAR
	 *        file.
	 * @param the output folder where the file is to be copied.
	 */
	public static String Url2Path(URL url, boolean isJar, File output) 
	    throws IOException, FileNotFoundException {
	// The specified file is contained in a JAR file
    if (url.openConnection() instanceof JarURLConnection) {
      // Prepares to read the specified file
	  JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
	  JarEntry fileInJarFile = (JarEntry) jarFile.getEntry(
	      url.getFile().substring(url.getFile().indexOf("!") + 2));
	  InputStream in = jarFile.getInputStream(fileInJarFile);
	  // The specified file is a JAR file
	  if (isJar)
	    in = new JarInputStream(in);
	  // The name of the specified file
	  String fileInJarName = fileInJarFile.getName();
	  fileInJarName = fileInJarName.substring(fileInJarName.lastIndexOf("/") + 1);
	  int n = fileInJarName.lastIndexOf('.');
	  File fileInJarCopy = null;
	  // The output folder is specified
	  if (output != null)
	    fileInJarCopy = File.createTempFile(fileInJarName.substring(0,n), 
		    fileInJarName.substring(n),output);
	  // The output folder is not specified
	  // The default temporary file directory is used
	  else
	    fileInJarCopy = File.createTempFile(fileInJarName.substring(0,n),
		    fileInJarName.substring(n));
	  // Prepares to copy the specified file
	  OutputStream out = new FileOutputStream(fileInJarCopy);
	  byte[] buffer;
	  int length;
	  // The specified file is a JAR file
	  // Copies the specified file to the output folder
	  if (isJar) {
	    out = new JarOutputStream(out);
		JarEntry jarEntry;
		while ((jarEntry = ((JarInputStream) in).getNextJarEntry()) != null) {
		  long size = jarEntry.getSize();
		  if (size == -1)
		    buffer = new byte[2048];
		  else
			buffer = new byte[(int) jarEntry.getSize()];
		  ((JarOutputStream) out).putNextEntry(new JarEntry(jarEntry));
		  while ((length = in.read(buffer)) > 0)
		    out.write(buffer, 0, length);
		}
	  } 
      // The specified file is not a JAR file
	  // Copies the specified file to the output folder 
	  else {
	    buffer = new byte[in.available()];
	    while ((length = in.read(buffer)) > 0)
		  out.write(buffer, 0, length);
	  }
	  in.close();
	  out.close();
	  // Returns the path of the copy
	  return fileInJarCopy.getPath();
	} 
    
    // The specified file is not contained in a JAR file 
    else
      return URLDecoder.decode(url.getPath(),"UTF-8");
  }
	
  public static void main(String[] args) throws Exception{
    if (args[0].equalsIgnoreCase("1"))
	 System.out.println(hexToASCII(args[1]));
    else if (args[0].equalsIgnoreCase("2"))
      System.out.println(ASCIIToHex(args[1]));
  }
}
