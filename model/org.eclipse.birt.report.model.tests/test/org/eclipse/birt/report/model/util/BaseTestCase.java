/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.model.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.birt.report.model.api.DesignConfig;
import org.eclipse.birt.report.model.api.DesignEngine;
import org.eclipse.birt.report.model.api.DesignFileException;
import org.eclipse.birt.report.model.api.ErrorDetail;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.birt.report.model.api.LibraryHandle;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.SessionHandle;
import org.eclipse.birt.report.model.core.ContainerSlot;
import org.eclipse.birt.report.model.elements.ReportDesign;
import org.eclipse.birt.report.model.i18n.ThreadResources;
import org.eclipse.birt.report.model.metadata.MetaDataDictionary;

import com.ibm.icu.util.ULocale;

/**
 * This class is abstract class used for tests, which contains the design file
 * name and report design handle, and provides the basic design file reading
 * methods.
 * 
 * This class performs mainly the following functionalities:
 * <p>
 * <ul>
 * <li>In Setup(), initialize the meda data and store information about meta
 * data in MetaDataDictionary</li>
 * <li>Open a design file, and store the
 * {@link org.eclipse.birt.report.model.elements.ReportDesign}instance and its
 * handle, {@link org.eclipse.birt.report.model.api.ReportDesignHandle}</li>
 * <li>After opening the design file, if the design file contains some syntax
 * or semantic error, the error list can be accessed by this class. This is to
 * make it easy when developing the test cases</li>
 * </ul>
 * <p>
 * Note:
 * <li>The derived unit test class must call 'super.setUp()' in their 'setUp'
 * method.</li>
 * <li>In testing environment, when open a design file by calling openDesign(
 * String fileName, InputStream is ), you can simply pass 'null' as the file
 * name; but, when printing out the error list, to make the file name appear in
 * the message, you can call 'design.setfileName( fileName )' in the child test
 * case.</li>
 * 
 */
public abstract class BaseTestCase extends TestCase
{

	/**
	 * The design engine.
	 */

	protected static IDesignEngine engine = null;

	/**
	 * The report design handle.
	 */

	protected ReportDesignHandle designHandle = null;

	/**
	 * The report design handle.
	 */

	protected LibraryHandle libraryHandle = null;

	/**
	 * The opened module handle.
	 */

	protected ModuleHandle moduleHandle = null;

	/**
	 * The session handle.
	 */

	protected SessionHandle sessionHandle = null;

	/**
	 * the root element for this design.
	 */
	protected ReportDesign design = null;

	/**
	 * Byte array output stream.
	 */

	protected ByteArrayOutputStream os = null;

	/**
	 * The file name of metadata file.
	 */
	protected static final String ROM_DEF_NAME = "rom.def"; //$NON-NLS-1$

	protected static final String TEST_FOLDER = "test/"; //$NON-NLS-1$
	protected static final String OUTPUT_FOLDER = "/output/"; //$NON-NLS-1$
	protected static final String INPUT_FOLDER = "input/"; //$NON-NLS-1$
	protected static final String GOLDEN_FOLDER = "golden/"; //$NON-NLS-1$

	protected static final ULocale TEST_LOCALE = new ULocale( "aa" ); //$NON-NLS-1$

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp( ) throws Exception
	{
		super.setUp( );
		ThreadResources.setLocale( ULocale.ENGLISH );

		if ( engine == null )
		{
			engine = new DesignEngine( new DesignConfig( ) );
			MetaDataDictionary.reset( );
			// initialize the metadata.

			engine.getMetaData( );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#teardown()
	 */

	protected void tearDown( ) throws Exception
	{
		if ( designHandle != null )
			designHandle.close( );

		if ( libraryHandle != null )
			libraryHandle.close( );

		if ( os != null )
			os.close( );

		super.tearDown( );
	}

	/**
	 * Creates a new report.
	 * 
	 * @return the handle for new report
	 */

	protected ReportDesignHandle createDesign( )
	{
		return createDesign( null );
	}

	/**
	 * Creates a new report with given locale.
	 * 
	 * @param locale
	 *            the user locale
	 * @return the handle for new report
	 */

	protected ReportDesignHandle createDesign( ULocale locale )
	{
		if ( engine == null )
			engine = new DesignEngine( new DesignConfig( ) );
		sessionHandle = engine.newSessionHandle( locale );
		designHandle = sessionHandle.createDesign( );
		design = (ReportDesign) designHandle.getModule( );

		removeExtensionStyles( design );
		return designHandle;
	}

	private void removeExtensionStyles( ReportDesign design )
	{

		ContainerSlot styles = design.getSlot( ReportDesign.STYLE_SLOT );
		styles.clear( );
//		if ( styles != null )
//		{
//			for ( int i = 0; i < styles.getCount( ); i++ )
//			{
//				styles.remove( i );
//			}
//		}
	}

	/**
	 * Creates a new library with default locale.
	 * 
	 * @return the handle for new library
	 */

	protected LibraryHandle createLibrary( )
	{
		return createLibrary( null );
	}

	/**
	 * Creates library with given locale.
	 * 
	 * @param locale
	 *            the user locale
	 * @return the handle for new library
	 */

	protected LibraryHandle createLibrary( ULocale locale )
	{
		if ( engine == null )
			engine = new DesignEngine( new DesignConfig( ) );
		sessionHandle = engine.newSessionHandle( locale );
		libraryHandle = sessionHandle.createLibrary( );

		return libraryHandle;
	}

	/**
	 * Opens design file with default locale.
	 * 
	 * @param fileName
	 *            design file name
	 * @throws DesignFileException
	 *             if any exception
	 */

	protected void openDesign( String fileName ) throws DesignFileException
	{
		openDesign( fileName, true );
	}

	/**
	 * Opens design file with default locale.
	 * 
	 * @param fileName
	 *            design file name
	 * @param inSingleJarMode
	 *            <code>true</code> if open the design that is in the single
	 *            jar. Otherwise <code>false</code>.
	 * @throws DesignFileException
	 *             if any exception
	 */

	protected void openDesign( String fileName, boolean inSingleJarMode )
			throws DesignFileException
	{
		openDesign( fileName, ULocale.getDefault( ), inSingleJarMode );
	}

	/**
	 * Opens design file providing the file name and the locale.
	 * 
	 * @param fileName
	 *            the design file to be opened
	 * @param locale
	 *            the user locale
	 * @param inSingleJarMode
	 *            <code>true</code> if open the design that is in the single
	 *            jar. Otherwise <code>false</code>.
	 * @throws DesignFileException
	 *             if any exception.
	 */

	protected void openDesign( String fileName, ULocale locale,
			boolean inSingleJarMode ) throws DesignFileException
	{
		if ( inSingleJarMode )
			fileName = INPUT_FOLDER + fileName;

		if ( engine == null )
			engine = new DesignEngine( new DesignConfig( ) );
		sessionHandle = engine.newSessionHandle( locale );
		assertNotNull( sessionHandle );

		if ( inSingleJarMode )
			designHandle = sessionHandle.openDesign( getResource( fileName ).toString( ) );
		else
			designHandle = sessionHandle.openDesign( fileName );

		design = (ReportDesign) designHandle.getModule( );
	}

	/**
	 * Opens design file providing the file name and the locale.
	 * 
	 * @param fileName
	 *            the design file to be opened
	 * @param locale
	 *            the user locale
	 * @param inSingleJarMode
	 *            <code>true</code> if open the design that is in the single
	 *            jar. Otherwise <code>false</code>.
	 * @throws DesignFileException
	 *             if any exception.
	 */

	protected void openDesign( String fileName, ULocale locale )
			throws DesignFileException
	{
		openDesign( fileName, locale, true );
	}

	/**
	 * Opens library file with given file name.
	 * 
	 * @param fileName
	 *            the library file name
	 * @throws DesignFileException
	 *             if any exception
	 */

	protected void openLibrary( String fileName ) throws DesignFileException
	{
		openLibrary( fileName, true );
	}

	/**
	 * Opens library file with given file name.
	 * 
	 * @param fileName
	 *            the library file name
	 * @param inSingleJarMode
	 *            <code>true</code> if open the design that is in the single
	 *            jar. Otherwise <code>false</code>.
	 * @throws DesignFileException
	 *             if any exception
	 */

	protected void openLibrary( String fileName, boolean inSingleJarMode )
			throws DesignFileException
	{
		openLibrary( fileName, ULocale.getDefault( ), inSingleJarMode );
	}

	/**
	 * Opens library file with given file name and locale.
	 * 
	 * @param fileName
	 *            the library file name
	 * @param locale
	 *            the user locale
	 * @throws DesignFileException
	 *             if any exception
	 */

	protected void openLibrary( String fileName, ULocale locale )
			throws DesignFileException
	{
		openLibrary( fileName, locale, true );
	}

	/**
	 * Opens library file with given file name and locale.
	 * 
	 * @param fileName
	 *            the library file name
	 * @param locale
	 *            the user locale
	 * @param inSingleJarMode
	 *            <code>true</code> if open the design that is in the single
	 *            jar. Otherwise <code>false</code>.
	 * @throws DesignFileException
	 *             if any exception
	 */

	protected void openLibrary( String fileName, ULocale locale,
			boolean inSingleJarMode ) throws DesignFileException
	{
		if ( inSingleJarMode )
			fileName = INPUT_FOLDER + fileName;

		if ( engine == null )
			engine = new DesignEngine( new DesignConfig( ) );
		sessionHandle = engine.newSessionHandle( locale );
		assertNotNull( sessionHandle );

		if ( inSingleJarMode )
			libraryHandle = sessionHandle.openLibrary( getResource( fileName ),
					getResourceAStream( fileName ) );
		else
			libraryHandle = sessionHandle.openLibrary( fileName );
	}

	/**
	 * Opens a module file with given file name.
	 * 
	 * @param fileName
	 *            the module file name
	 * @throws DesignFileException
	 *             if any exception
	 */

	protected void openModule( String fileName ) throws DesignFileException
	{
		openModule( fileName, ULocale.getDefault( ) );
	}

	/**
	 * Opend a module given file name and locale.
	 * 
	 * @param fileName
	 *            the module file name
	 * @param locale
	 *            the user locale
	 * @throws DesignFileException
	 */

	protected void openModule( String fileName, ULocale locale )
			throws DesignFileException
	{
		if ( engine == null )
			engine = new DesignEngine( new DesignConfig( ) );
		fileName = INPUT_FOLDER + fileName;
		sessionHandle = engine.newSessionHandle( locale );
		assertNotNull( sessionHandle );

		moduleHandle = sessionHandle.openModule( getResource( fileName ).toString( ),
				getResourceAStream( fileName ) );
	}

	/**
	 * Reads design file as InputStream.
	 * 
	 * @param fileName
	 *            Design file name
	 * @param is
	 *            InputStream of this design file
	 * @throws DesignFileException
	 *             if any exception.
	 */

	protected void openDesign( String fileName, InputStream is )
			throws DesignFileException
	{
		openDesign( fileName, is, ULocale.getDefault( ) );
	}

	/**
	 * Opens a design file.
	 * 
	 * @param fileName
	 *            the design file name
	 * @param is
	 *            the input stream of the design file.
	 * @param locale
	 *            the user locale.
	 * @throws DesignFileException
	 *             if any exception.
	 */
	protected void openDesign( String fileName, InputStream is, ULocale locale )
			throws DesignFileException
	{
		if ( engine == null )
			engine = new DesignEngine( new DesignConfig( ) );
		sessionHandle = engine.newSessionHandle( locale );
		designHandle = sessionHandle.openDesign( fileName, is );
		design = (ReportDesign) designHandle.getModule( );
	}

	/**
	 * Compares two text file. The comparison will ignore the line containing
	 * "modificationDate".
	 * 
	 * @param goldenFileName
	 *            the 1st file name to be compared.
	 * @param outputFileName
	 *            the 2nd file name to be compared.
	 * @return true if two text files are same line by line
	 * @throws Exception
	 *             if any exception.
	 */
	protected boolean compareFile( String goldenFileName, String outputFileName )
			throws Exception
	{
		Reader readerA = null;
		FileReader readerB = null;
		boolean same = true;
		StringBuffer errorText = new StringBuffer( );

		try
		{
			goldenFileName = GOLDEN_FOLDER + goldenFileName;
			outputFileName = getTempFolder( ) + OUTPUT_FOLDER + outputFileName;

			readerA = new InputStreamReader( getResourceAStream( goldenFileName ) );
			readerB = new FileReader( outputFileName );

			same = compareFile( readerA, readerB );
		}
		catch ( IOException e )
		{
			errorText.append( e.toString( ) );
			errorText.append( "\n" ); //$NON-NLS-1$
			e.printStackTrace( );
		}
		finally
		{
			try
			{
				if ( readerA != null )
					readerA.close( );
				if ( readerB != null )
					readerB.close( );
			}
			catch ( Exception e )
			{
				readerA = null;
				readerB = null;

				errorText.append( e.toString( ) );

				throw new Exception( errorText.toString( ) );
			}
		}

		return same;
	}

	/**
	 * Compares two text file. The comparison will ignore the line containing
	 * "modificationDate".
	 * 
	 * @param goldenFileName
	 *            the 1st file name to be compared.
	 * @param os
	 *            the 2nd output stream to be compared.
	 * @return true if two text files are same char by char
	 * @throws Exception
	 *             if any exception.
	 */
	protected boolean compareFile( String goldenFileName ) throws Exception
	{
		goldenFileName = GOLDEN_FOLDER + goldenFileName;

		InputStream streamA = getResourceAStream( goldenFileName );
		if ( os == null )
			return false;
		InputStream streamB = new ByteArrayInputStream( os.toByteArray( ) );
		InputStreamReader readerA = new InputStreamReader( streamA );
		InputStreamReader readerB = new InputStreamReader( streamB );
		return compareFile( readerA, readerB );
	}

	/**
	 * Compares the two text files.
	 * 
	 * @param goldenReader
	 *            the reader for golden file
	 * @param outputReader
	 *            the reader for output file
	 * @return true if two text files are same.
	 * @throws Exception
	 *             if any exception
	 */
	private boolean compareFile( Reader goldenReader, Reader outputReader )
			throws Exception
	{
		StringBuffer errorText = new StringBuffer( );

		BufferedReader lineReaderA = null;
		BufferedReader lineReaderB = null;
		boolean same = true;
		int lineNo = 1;
		try
		{
			lineReaderA = new BufferedReader( goldenReader );
			lineReaderB = new BufferedReader( outputReader );

			String strA = lineReaderA.readLine( ).trim( );
			String strB = lineReaderB.readLine( ).trim( );
			while ( strA != null )
			{
				same = strA.trim( ).equals( strB.trim( ) );
				if ( !same )
				{
					StringBuffer message = new StringBuffer( );

					message.append( "line=" ); //$NON-NLS-1$
					message.append( lineNo );
					message.append( " is different:\n" );//$NON-NLS-1$
					message.append( " The line from golden file: " );//$NON-NLS-1$
					message.append( strA );
					message.append( "\n" );//$NON-NLS-1$
					message.append( " The line from output file: " );//$NON-NLS-1$
					message.append( strB );
					message.append( "\n" );//$NON-NLS-1$
					throw new Exception( message.toString( ) );
				}

				strA = lineReaderA.readLine( );
				strB = lineReaderB.readLine( );
				lineNo++;
			}
			same = strB == null;
		}
		finally
		{
			try
			{
				if ( lineReaderA != null )
					lineReaderA.close( );
				if ( lineReaderB != null )
					lineReaderB.close( );
			}
			catch ( Exception e )
			{
				lineReaderA = null;
				lineReaderB = null;

				errorText.append( e.toString( ) );

				throw new Exception( errorText.toString( ) );
			}
		}

		return same;
	}

	/**
	 * Prints out all semantic errors stored in the error list during parsing
	 * the design file.
	 */

	protected void printSemanticErrors( )
	{
		printSemanticError( design );
	}

	/**
	 * Prints out all semantic errors stored in the error list during parsing
	 * the design file.
	 * 
	 * @param design
	 *            report design
	 */

	protected void printSemanticError( ReportDesign design )
	{
		if ( design != null )
			printErrorList( design.getAllErrors( ) );
	}

	/**
	 * Prints out all syntax errors stored in the error list during parsing the
	 * design file.
	 * 
	 * @param e
	 *            <code>DesignFileException</code> containing syntax error
	 *            list.
	 */

	protected void printSyntaxError( DesignFileException e )
	{
		if ( e != null )
			printErrorList( e.getErrorList( ) );
	}

	/**
	 * Prints error list.
	 * 
	 * @param errors
	 *            error list
	 */
	private void printErrorList( List errors )
	{
		if ( errors != null && !errors.isEmpty( ) )
		{
			for ( Iterator iter = errors.iterator( ); iter.hasNext( ); )
			{
				ErrorDetail ex = (ErrorDetail) iter.next( );
				System.out.println( ex );
			}
		}
	}

	/**
	 * Dumps the parsing errors into a text file.
	 * 
	 * @param filename
	 *            the file name into which the error will be dumped.
	 * @throws Exception
	 *             if any exception.
	 */

	protected void dumpErrors( String filename ) throws Exception
	{
		String outputFolder = getTempFolder( ) + OUTPUT_FOLDER;
		File f = new File( outputFolder );
		if ( !f.exists( ) && !f.mkdir( ) )
		{
			throw new Exception( "Can not create the output folder!" ); //$NON-NLS-1$
		}
		filename = outputFolder + filename;

		if ( design == null )
			return;
		PrintWriter writer = new PrintWriter( new FileOutputStream( filename ) );
		List errors = design.getAllErrors( );
		ErrorDetail ex = null;
		for ( int i = 0; i < errors.size( ); i++ )
		{
			ex = (ErrorDetail) errors.get( i );
			writer.print( design.getFileName( ) );
			writer.println( ex );
		}
		writer.close( );
	}

	/**
	 * Eventually, this method will call
	 * {@link ReportDesignHandle#serialize(java.io.OutputStream)}to save the
	 * output file of some unit test.
	 * 
	 * @param filename
	 *            the test output file to be saved.
	 * @throws IOException
	 *             if error occurs while saving the file.
	 */

	protected void save( ) throws IOException
	{
		save( designHandle );
	}

	/**
	 * Eventually, this method will call
	 * {@link ReportDesignHandle#serialize(java.io.OutputStream)}to save the
	 * output file of some unit test.
	 * 
	 * @param moduleHandle
	 *            the module to save, either a report design or a library
	 * @throws IOException
	 *             if error occurs while saving the file.
	 */

	protected void save( ModuleHandle moduleHandle ) throws IOException
	{
		os = new ByteArrayOutputStream( );
		if ( moduleHandle != null )
			moduleHandle.serialize( os );
		os.close( );
	}

	/**
	 * Saves library as the given file name.
	 * 
	 * @param filename
	 *            the file name for saving
	 * @throws IOException
	 *             if any exception
	 */

	protected void saveLibrary( ) throws IOException
	{
		save( libraryHandle );
	}

	/**
	 * Gets the temp folder of this class.
	 * 
	 * @return temp folder of this class
	 */

	protected String getTempFolder( )
	{
		String tempDir = System.getProperty( "java.io.tmpdir" ); //$NON-NLS-1$
		if ( !tempDir.endsWith( File.separator ) )
			tempDir += File.separator;

		String outputPath = tempDir + "org.eclipse.birt.report.model" //$NON-NLS-1$
				+ getFullQualifiedClassName( );
		return outputPath;
	}

	/**
	 * Gets the input stream of the given name resources.
	 */

	protected InputStream getResourceAStream( String name )
	{
		return this.getClass( ).getResourceAsStream( name );
	}

	/**
	 * gets the url of the resource.
	 * 
	 * @param name
	 *            name of the resource
	 * @return the url of the resource
	 */

	protected URL getResource( String name )
	{
		return this.getClass( ).getResource( name );
	}

	/**
	 * Returns the full qualified class name. For example,
	 * "/org/eclipse/birt/report/model".
	 * 
	 * @return the full qualified class name
	 */

	protected String getFullQualifiedClassName( )
	{
		String className = this.getClass( ).getName( );
		int lastDotIndex = className.lastIndexOf( "." ); //$NON-NLS-1$
		className = className.substring( 0, lastDotIndex );
		className = "/" + className.replace( '.', '/' ); //$NON-NLS-1$

		return className;
	}

	/**
	 * Checks the platform.
	 * 
	 * @return <code>true</code> if the platform is windows. Otherwise
	 *         <code>false</code>.
	 */

	protected boolean isWindowsPlatform( )
	{
		return System.getProperty( "os.name" ).toLowerCase( ).indexOf( //$NON-NLS-1$
		"windows" ) >= 0; //$NON-NLS-1$
	}

	/**
	 * Copies the file to the temporary folder.
	 * 
	 * @param resourceName
	 *            the resource name. Based on the class folder.
	 * @return the file path on the disk
	 * @throws Exception
	 */

	protected String copyContentToFile( String resourceName ) throws Exception
	{
		URL url = getResource( resourceName );
		InputStream is = url.openStream( );

		String folder = getTempFolder( );

		int index = resourceName.lastIndexOf( INPUT_FOLDER );
		if ( index > 0 )
		{
			String relateDir = resourceName.substring( 0, index - 1 );
			folder = folder + "/" + relateDir; //$NON-NLS-1$
		}

		folder = folder + "/" + INPUT_FOLDER; //$NON-NLS-1$

		File tmpFolder = new File( folder );
		if ( !tmpFolder.exists( ) )
			tmpFolder.mkdirs( );

		String filename = ""; //$NON-NLS-1$
		int lastSlash = resourceName.lastIndexOf( "/" ); //$NON-NLS-1$
		if ( lastSlash != -1 )
		{
			filename = resourceName.substring( lastSlash + 1 );
		}

		FileOutputStream fos = new FileOutputStream( folder + filename );
		byte[] fileData = new byte[5120];
		int readCount = -1;
		while ( ( readCount = is.read( fileData ) ) != -1 )
		{
			fos.write( fileData, 0, readCount );
		}

		fos.close( );
		is.close( );

		return folder + filename;
	}

	/**
	 * Saves the output stream into the output file.
	 * 
	 * @param fileName
	 *            the resource name. Based on the class folder.
	 * @throws Exception
	 */

	protected void saveOutputFile( String fileName ) throws Exception
	{
		String folder = getTempFolder( ) + OUTPUT_FOLDER;
		File tmpFolder = new File( folder );
		if ( !tmpFolder.exists( ) )
			tmpFolder.mkdirs( );

		String strDesign = os.toString( );
		FileOutputStream fos = new FileOutputStream( folder + fileName );
		fos.write( strDesign.getBytes( "UTF-8" ) ); //$NON-NLS-1$

		fos.close( );
	}

	/**
	 * @param strs
	 * @return
	 */

	protected static String serializeStringList( List strs )
	{
		if ( strs == null )
			return null;

		StringBuffer sb = new StringBuffer( );
		for ( int i = 0; i < strs.size( ); i++ )
		{
			sb.append( (String) strs.get( i ) );

			if ( i != strs.size( ) - 1 )
				sb.append( ", " ); //$NON-NLS-1$
		}

		return sb.toString( );
	}
}