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
 * ClassyFyProvider
 * Implements android.content.ContentProvider and binds to persistence implementation through Dagger Dependency Injection.
 * This class is only a shell delegating the Provider implementation to ClassyFySearchEngine which implements SearchEngineBase. 
 * Note that Android calls onCreate() before the ClassyFyApplication onCreate() is called where Dependency Injection is
 * initialized. Therefore, any attempt access to an injected variable when onCreate() in this class is called fails with a
 * NullPointerException. Therefore, a dynamic approach has to be used where the dependency binding is delayed until the first 
 * call to this Content Provider is made. This call is orchestrated by  ClassyFyStartup class which upon completion of database
 * initilazation, calls ContentResolver getType() to get the ball rolling.  
 * 
 * @author Andrew Bowley
 * 12/07/2014
 * @see au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine
 * @see au.com.cybersearch2.classyfts.SearchEngineBase
 * @see au.com.cybersearch2.classyfy.ClassyFyStartup
 */
public class ClassyFyProvider extends ContentProvider
{
	/** The actual ContentProvider implementation */
    protected ClassyFySearchEngine classyFySearchEngine;
    /** Flag to control ContentProvider initialization */
    protected volatile boolean searchEngineAvailable;
    /** Android Persistence implementation which has a custom feature of providing the underlying SQLiteOpenHelper */
    @Inject @Named(ClassyFyApplication.PU_NAME) AndroidPersistenceEnvironment androidPersistenceEnvironment;

    /**
     * Create a ClassyFyProvider object
     */
    public ClassyFyProvider()
    {

    }
    
	/**
	 * onCreate() called before Application onCreate(), so can do nothing as DI not initialized.
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate()
	{
        return true;
	}

	/**
	 * Nothing to do on shutdown. The Application object is responsible for Persistence implementation.
	 * @see android.content.ContentProvider#shutdown()
	 */
	@Override
    public void shutdown() 
	{
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
    {   // ClassyFyStartup class upon completion of database initilazation will call this to start the Provider
        if (!searchEngineAvailable)
            startSearchEngine();
        return classyFySearchEngine.getType(uri);
    }

	/**
	 * Perform query with given SQL search parameters
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

    /**
     * Insert content
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
        if (!searchEngineAvailable)
            startSearchEngine();
	    return classyFySearchEngine.insert(uri, values);
	}

	/**
	 * Delete content
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
        if (!searchEngineAvailable)
            startSearchEngine();
	    return classyFySearchEngine.delete(uri, selection, selectionArgs);
	}

	/**
	 * Update content
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

	/**
	 * Start ContentProvider implemetation. This is delayed until first ContentProvider call
	 * because this class's onCreate() is called before Application onCreate(), when DI is not initialized.
	 */
	protected synchronized void startSearchEngine()
	{
	    if (!searchEngineAvailable)
	    {
	        DI.inject(this);
	        classyFySearchEngine = new ClassyFySearchEngine(); 
	        // Get the Android SQLiteOpenHelper for "classyfy" persistence unit
	        classyFySearchEngine.onCreate(androidPersistenceEnvironment.getSQLiteOpenHelper());
	        searchEngineAvailable = true;
	    }
	}
}
