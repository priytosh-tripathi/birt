/*************************************************************************************
 * Copyright (c) 2004 Actuate Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Actuate Corporation - Initial implementation.
 ************************************************************************************/

package org.eclipse.birt.report.taglib.util;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.exception.ViewerException;
import org.eclipse.birt.report.model.api.IModuleOption;
import org.eclipse.birt.report.resource.ResourceConstants;
import org.eclipse.birt.report.service.BirtViewerReportDesignHandle;
import org.eclipse.birt.report.service.ReportEngineService;
import org.eclipse.birt.report.service.api.IViewerReportDesignHandle;
import org.eclipse.birt.report.taglib.component.ViewerField;
import org.eclipse.birt.report.utility.BirtUtility;
import org.eclipse.birt.report.utility.ParameterAccessor;

/**
 * Utilities for Birt tags
 * 
 */
public class BirtTagUtil
{

	/**
	 * Convert String to correct boolean value.
	 * 
	 * @param bool
	 * @return
	 */
	public static String convertBooleanValue( String bool )
	{
		boolean b = Boolean.valueOf( bool ).booleanValue( );
		return String.valueOf( b );
	}

	/**
	 * Convert String to boolean.
	 * 
	 * @param bool
	 * @return
	 */
	public static boolean convertToBoolean( String bool )
	{
		if ( bool == null )
			return false;

		return Boolean.valueOf( bool ).booleanValue( );
	}

	/**
	 * Returns the output format.Default value is html.
	 * 
	 * @param format
	 * @return
	 */
	public static String getFormat( String format )
	{
		if ( format == null || format.length( ) <= 0 )
			return ParameterAccessor.PARAM_FORMAT_HTML;

		if ( format.equalsIgnoreCase( ParameterAccessor.PARAM_FORMAT_HTM ) )
			return ParameterAccessor.PARAM_FORMAT_HTML;

		return format;
	}

	/**
	 * Get report locale.
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param locale
	 *            String
	 * @return locale
	 */

	public static Locale getLocale( HttpServletRequest request, String sLocale )
	{
		Locale locale = null;

		// Get Locale from String value
		locale = ParameterAccessor.getLocaleFromString( sLocale );

		// Get Locale from client browser
		if ( locale == null )
			locale = request.getLocale( );

		// Get Locale from Web Context
		if ( locale == null )
			locale = ParameterAccessor.webAppLocale;

		return locale;
	}

	/**
	 * If a report file name is a relative path, it is relative to document
	 * folder. So if a report file path is relative path, it's absolute path is
	 * synthesized by appending file path to the document folder path.
	 * 
	 * @param file
	 * @return
	 */

	public static String createAbsolutePath( String filePath )
	{
		if ( filePath != null && filePath.trim( ).length( ) > 0
				&& ParameterAccessor.isRelativePath( filePath ) )
		{
			return ParameterAccessor.workingFolder + File.separator + filePath;
		}
		return filePath;
	}

	/**
	 * Returns report design handle
	 * 
	 * @param request
	 * @param viewer
	 * @return
	 * @throws Exception
	 */
	public static IViewerReportDesignHandle getDesignHandle(
			HttpServletRequest request, ViewerField viewer ) throws Exception
	{
		if ( viewer == null )
			return null;

		IViewerReportDesignHandle design = null;
		IReportRunnable reportRunnable = null;

		// Get the absolute report design and document file path
		String designFile = ParameterAccessor.getReport( request, viewer
				.getReportDesign( ) );
		String documentFile = ParameterAccessor.getReportDocument( request,
				viewer.getReportDocument( ), false );

		// check if document file path is valid
		boolean isValidDocument = ParameterAccessor.isValidFilePath( viewer
				.getReportDocument( ) );
		if ( documentFile != null && isValidDocument )
		{
			// open the document instance
			IReportDocument reportDocumentInstance = ReportEngineService
					.getInstance( ).openReportDocument( designFile,
							documentFile, getModuleOptions( viewer ) );

			if ( reportDocumentInstance != null )
			{
				viewer.setDocumentInUrl( true );
				reportRunnable = reportDocumentInstance.getReportRunnable( );
				reportDocumentInstance.close( );
			}
		}

		// if report runnable is null, then get it from design file
		if ( reportRunnable == null )
		{
			// if only set __document parameter, throw exception directly
			if ( documentFile != null && designFile == null )
			{
				if ( isValidDocument )
					throw new ViewerException(
							ResourceConstants.GENERAL_EXCEPTION_DOCUMENT_FILE_ERROR,
							new String[]{documentFile} );
				else
					throw new ViewerException(
							ResourceConstants.GENERAL_EXCEPTION_DOCUMENT_ACCESS_ERROR,
							new String[]{documentFile} );
			}

			// check if the report file path is valid
			if ( !ParameterAccessor.isValidFilePath( viewer.getReportDesign( ) ) )
			{
				throw new ViewerException(
						ResourceConstants.GENERAL_EXCEPTION_REPORT_ACCESS_ERROR,
						new String[]{designFile} );
			}
			else
			{
				reportRunnable = BirtUtility.getRunnableFromDesignFile(
						request, designFile, getModuleOptions( viewer ) );

				if ( reportRunnable == null )
				{
					throw new ViewerException(
							ResourceConstants.GENERAL_EXCEPTION_REPORT_FILE_ERROR,
							new String[]{designFile} );
				}
			}
		}

		if ( reportRunnable != null )
		{
			design = new BirtViewerReportDesignHandle(
					IViewerReportDesignHandle.RPT_RUNNABLE_OBJECT,
					reportRunnable );
		}

		return design;
	}

	/**
	 * Create Module Options
	 * 
	 * @param viewer
	 * @return
	 */
	public static Map getModuleOptions( ViewerField viewer )
	{
		if ( viewer == null )
			return null;

		Map options = new HashMap( );
		String resourceFolder = viewer.getResourceFolder( );
		if ( resourceFolder == null || resourceFolder.trim( ).length( ) <= 0 )
			resourceFolder = ParameterAccessor.birtResourceFolder;

		options.put( IModuleOption.RESOURCE_FOLDER_KEY, resourceFolder );
		return options;
	}
}
