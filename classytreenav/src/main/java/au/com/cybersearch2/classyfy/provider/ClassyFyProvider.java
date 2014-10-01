/**
    Copyright (C) 2014  www.cybersearch2.com.au

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/> */
package au.com.cybersearch2.classyfy.provider;

import javax.inject.Inject;
import javax.inject.Named;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import au.com.cybersearch2.classyfy.ClassyFyApplication;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.AndroidPersistenceEnvironment;


/**
 * @author andrew
 *
 */
public class ClassyFyProvider extends ContentProvider
{
    protected ClassyFySearchEngine classyFySearchEngine;
    protected volatile boolean searchEngineAvailable;
    @Inject @Named(ClassyFyApplication.PU_NAME) AndroidPersistenceEnvironment androidPersistenceEnvironment;

    public ClassyFyProvider()
    {

    }
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate()
	{
        return true;
	}

	@Override
    public void shutdown() 
	{   //  TODO - Work out appropriate cleanup
	    //if (androidPersistenceEnvironment != null)
	    //    androidPersistenceEnvironment.getSQLiteOpenHelper().close();
	}

	   /**
	    * This is called when a client calls {@link android.content.ContentResolver#getType(Uri)}.
	    * Returns the "custom" or "vendor-specific" MIME data type of the URI given as a parameter.
	    * MIME types have the format "type/subtype". The type value is always "vnd.android.cursor.dir"
        * for multiple rows, or "vnd.android.cursor.item" for a single row. 
	    *
	    * @param uri The URI whose MIME type is desired.
	    * @return The MIME type of the URI.
	    * @throws IllegalArgumentException if the incoming URI pattern is invalid.
	    */
    @Override
    public String getType(Uri uri)
    {
        if (!searchEngineAvailable)
            startSearchEngine();
        return classyFySearchEngine.getType(uri);
    }

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
	        String[] selectionArgs, String sortOrder)
	{
        if (!searchEngineAvailable)
            startSearchEngine();
	    return classyFySearchEngine.query(uri, projection, selection, selectionArgs, sortOrder);
	}

    /* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
        if (!searchEngineAvailable)
            startSearchEngine();
	    return classyFySearchEngine.insert(uri, values);
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
        if (!searchEngineAvailable)
            startSearchEngine();
	    return classyFySearchEngine.delete(uri, selection, selectionArgs);
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
	        String[] selectionArgs)
	{
        if (!searchEngineAvailable)
            startSearchEngine();
	    return classyFySearchEngine.update(uri, values, selection, selectionArgs);
	}

	protected synchronized void startSearchEngine()
	{
	    if (!searchEngineAvailable)
	    {
	        DI.inject(this);
	        classyFySearchEngine = new ClassyFySearchEngine(); 
	        classyFySearchEngine.onCreate(androidPersistenceEnvironment.getSQLiteOpenHelper());
	        searchEngineAvailable = true;
	    }
	}
}
