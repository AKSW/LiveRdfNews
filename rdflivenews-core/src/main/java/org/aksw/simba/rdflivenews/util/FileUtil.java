package org.aksw.simba.rdflivenews.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

import org.aksw.simba.rdflivenews.util.BufferedFileWriter.WRITER_WRITE_MODE;

public class FileUtil {

	/**
	 * Reads a file an returns new numbers of new lines contained.
	 * 
	 * @param filename - the path of the file 
	 * @return the number of lines
	 */
	public static int countLinesOfFile(String filename) {

		InputStream is = null;

		try {

			is = new BufferedInputStream(new FileInputStream(filename));
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			while ((readChars = is.read(c)) != -1) {
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n')
						++count;
				}
			}
			return count;
		}
		catch (Exception e) {

			e.printStackTrace();
		}
		finally {

			try {
				is.close();
			}
			catch (IOException e) {

				e.printStackTrace();
			}
		}
		return 0;
	}
	
	/**
	 * Reads a file line by line and returns the lines in that order
	 * in a list. The encoding can be specified but may also be null 
	 * or empty. In this case the java default encoding is used.
	 * 
	 * @param pathToFile - the path of the file to read
	 * @param encoding - the encoding of the file
	 * @return an ordered list with each line 
	 * @throws IOException - something went wrong traversing the file
	 * @throws UnsupportedEncodingException - the specified encoding is not supported
	 * @throws FileNotFoundException - the file can not be found at the specified path
	 */
	public static List<String> readFileInList(String pathToFile, String encoding, String commentSymbol) {
		
		BufferedFileReader br;
		
		// try to open file with specified encoding
		if ( encoding == null || encoding.isEmpty() ) {
		
			 br = new BufferedFileReader(pathToFile, encoding);
		}
		// if no encoding is specified use the java default one
		else {
			
			br = new BufferedFileReader(pathToFile);
		}
		
		// read the file, and add each line to the results
		String line;
		List<String> results = new ArrayList<String>();
		while ( (line = br.readLine()) != null ) {
			
		    if ( !line.startsWith(commentSymbol) ) results.add(line);
		}
		br.close();
		
		return results;
	}
	
	/**
	 * Opens a buffered writer for a given file with the given encoding or UTF-8 if no
	 * encoding was provided. Also one can select if the writer shall append to a given 
	 * file or override it.
	 * 
	 * @param pathToFile the path to the file to write to
	 * @param encoding the encoding of the file to write
	 * @param mode append or override the file
	 * @return a buffered write in the configured way
	 */
	public static BufferedFileWriter openWriter(String pathToFile, String encoding, WRITER_WRITE_MODE mode) {
		
		return new BufferedFileWriter(pathToFile, encoding, mode);
	}
	
	/**
	 * @param filename
	 * @return
	 */
	public static BufferedFileReader openReader(String filename) {
		
		return new BufferedFileReader(filename);
	}
	
	/**
	 * @param filename
	 * @param encoding
	 * @return
	 */
	public static BufferedFileReader openReader(String filename, String encoding) {
		
		return new BufferedFileReader(filename, encoding);
	}
}
