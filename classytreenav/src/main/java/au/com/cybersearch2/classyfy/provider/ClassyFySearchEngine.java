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

import java.util.HashMap;
import java.util.Map;

import android.app.SearchManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import au.com.cybersearch2.classyfts.FtsEngine;
import au.com.cybersearch2.classyfts.FtsOpenHelper;
import au.com.cybersearch2.classyfts.FtsQueryBuilder;
import au.com.cybersearch2.classyfts.SearchEngineBase;
import au.com.cybersearch2.classyfts.WordFilter;
import au.com.cybersearch2.classyfy.R;

/**
 * ClassyFySearchEngine
 * @author Andrew Bowley
 * 11/07/2014
 */
public class ClassyFySearchEngine extends SearchEngineBase
{
    
    public static final String PROVIDER_AUTHORITY = "au.com.cybersearch2.classyfy.ClassyFyProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://au.com.cybersearch2.classyfy.ClassyFyProvider/all_nodes");
    public static Uri LEX_CONTENT_URI = Uri.parse("content://" + PROVIDER_AUTHORITY + "/" + LEX + "/" + SearchManager.SUGGEST_URI_PATH_QUERY);
    public static final String ALL_NODES_VIEW = "all_nodes";
    
    // Column names
    // Android expects RowIDColumn to be "_id", so do not use any other value.
    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MODEL = "model";

    // Create the constants used to differntiate between the different URI requests
    // Note values 1 - 4 are reserved for 
    // SEARCH_SUGGEST, REFRESH_SHORTCUT, LEXICAL_SEARCH_SUGGEST and LEXICAL_REFRESH_SHORTCUT
    protected static final int ALL_NODES_TYPES = PROVIDER_TYPE;
    protected static final int ALL_NODES_TYPE_ID = ALL_NODES_TYPES + 1;

    
    protected final UriMatcher uriMatcher;
    protected SQLiteOpenHelper sqLiteOpenHelper;
    
    // Search suggestions support. A Cursor must be returned with a set of pre-defined columns
    protected final Map<String, String> ALL_NODES_TYPE_SEARCH_PROJECTION_MAP;
    

    public ClassyFySearchEngine()
    {
        super();
        uriMatcher = createUriMatcher();
        ALL_NODES_TYPE_SEARCH_PROJECTION_MAP = createProjectionMap();
    }

    @Override
    public void onCreate(SQLiteOpenHelper sqLiteOpenHelper)
    {
        this.sqLiteOpenHelper = sqLiteOpenHelper;
        WordFilter text2Filter = new WordFilter(){
            /**
             * Search result word filter 
             * @param key Database column name 
             * @param word Word from column identified by the key
             * @return Same value as "word" parameter or a replacement value
             */
            @Override
            public String filter(String key, String word) {
                if ("model".equals(key))
                    return word.replace("record", "");
                else
                    return word;
            }
        };
        Map<String,String> COLUMN_MAP = new HashMap<String,String>();
        COLUMN_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1, "title");
        COLUMN_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_2, "model");
        COLUMN_MAP.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "_id");
        FtsOpenHelper ftsOpenHelper = new FtsOpenHelper(sqLiteOpenHelper);
        FtsEngine ftsEngine = new FtsEngine(ftsOpenHelper, "all_nodes", COLUMN_MAP);
        ftsEngine.setOrderbyText2(true);
        ftsEngine.setText2Filter(text2Filter);
        if (LEX.equals(getSearchSuggestPath(R.xml.searchable))) 
            startFtsEngine(ftsEngine);
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
 
     public String getType(Uri uri)
     {
         int queryType = uriMatcher.match(uri);
         switch (queryType)
         {
         case ALL_NODES_TYPES: 
             return "vnd.android.cursor.dir/vnd.classyfy.node";
         case ALL_NODES_TYPE_ID: 
             return "vnd.android.cursor.item/vnd.classyfy.node";
         default: 
             return super.getType(queryType, uri);
         }
     }


    public Cursor query(Uri uri, final String[] projection, final String selection,
            final String[] selectionArgs, final String sortOrder)
    {
        final int queryType = uriMatcher.match(uri);
        FtsQueryBuilder qb = new FtsQueryBuilder(
                queryType, 
                uri, 
                projection, 
                selection,
                selectionArgs, 
                sortOrder);
        return query(uri, qb);
    }
    
    
    protected Cursor query(Uri uri, FtsQueryBuilder qb)
    {
        qb.setTables(ALL_NODES_VIEW);
        // If this is a row query then limit the result set to the passed in row
        switch (qb.getQueryType())
        {
        case ALL_NODES_TYPE_ID: 
            if (uri.getPathSegments().size() < 2)
                throw new IllegalArgumentException("Invalid quiery Uri: " + uri.toString());
            qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1)); 
            break;
        case ALL_NODES_TYPES:
            break; 
        case LEXICAL_SEARCH_SUGGEST: // Fall back if Fts not available
        case SEARCH_SUGGEST:         // Search Suggestions support query appended with: where title like "%<search-term>%" Note: uri can have /?limit=50
        {
            qb.appendWhere(KEY_TITLE + " like \"%" + qb.getSearchTerm() + "%\"");
            qb.setProjectionMap(ALL_NODES_TYPE_SEARCH_PROJECTION_MAP);
            break;
        }          
        case REFRESH_SHORTCUT:
        case LEXICAL_REFRESH_SHORTCUT:
           // TODO
            break;
        default: break;
        }
        // If no sort order is specified, sort by title
        if (TextUtils.isEmpty(qb.getSortOrder()))
            qb.setSortOrder(KEY_NAME + " ASC");
        // Apply the query to the underlying database
        return query(qb, sqLiteOpenHelper);
    }

    /* (non-Javadoc)
     * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
     */
    public Uri insert(Uri uri, ContentValues values)
    {
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
        // Insert the new row. The call to database.insert will return the row number if it is successful
        long rowId = database.insert(ALL_NODES_VIEW, null, values);
        // Return a URI to the newly inserted row on success
        if (rowId > 0)
        {
            Uri resultUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            notifyChange(resultUri);
            return resultUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    /* (non-Javadoc)
     * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
     */
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
        int count;
        switch (uriMatcher.match(uri))
        {
        case ALL_NODES_TYPES: 
            count = database.delete(ALL_NODES_VIEW, selection, selectionArgs);
            break;
        case ALL_NODES_TYPE_ID:
            String segment = uri.getPathSegments().get(1);
            String rowSelection = (!TextUtils.isEmpty(selection) ? " and (" + selection + ')' : "");
            count = database.delete(ALL_NODES_VIEW, KEY_ID + "=" + segment + rowSelection, selectionArgs);
            break;
        default: throw new IllegalArgumentException("Unsupported URI: " + uri);
            
        }
        notifyChange(uri);
        return count;
    }

    /* (non-Javadoc)
     * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
     */
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs)
    {
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
        int count;
        switch (uriMatcher.match(uri))
        {
        case ALL_NODES_TYPES: 
            count = database.update(ALL_NODES_VIEW, values, selection, selectionArgs);
            break;
        case ALL_NODES_TYPE_ID:
            String segment = uri.getPathSegments().get(1);
            String rowSelection = (!TextUtils.isEmpty(selection) ? " and (" + selection + ')' : "");
            count = database.update(ALL_NODES_VIEW, values,
                    KEY_ID + "=" + segment + rowSelection, selectionArgs);
            break;
        default: throw new IllegalArgumentException("Unknown URI " + uri);
            
        }
        notifyChange(uri);
        return count;
    }

    protected static UriMatcher createUriMatcher() 
    {
        // Allocate the UriMatcher object where a URI ending is 'all_nodes' will correspond to a request for all nodes, 
        // and 'all_nodes' with a trailing '/[rowID]' will represent a single all_nodes row

        UriMatcher newUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        newUriMatcher.addURI(PROVIDER_AUTHORITY, "all_nodes", ALL_NODES_TYPES);
        newUriMatcher.addURI(PROVIDER_AUTHORITY, "all_nodes/#", ALL_NODES_TYPE_ID);
        // to get suggestions...
        newUriMatcher.addURI(PROVIDER_AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        newUriMatcher.addURI(PROVIDER_AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
        newUriMatcher.addURI(PROVIDER_AUTHORITY, LEX + "/" + SearchManager.SUGGEST_URI_PATH_QUERY, LEXICAL_SEARCH_SUGGEST);
        newUriMatcher.addURI(PROVIDER_AUTHORITY, LEX + "/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*", LEXICAL_SEARCH_SUGGEST);
        /* The following are unused in this implementation, but if we include
         * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our suggestions table, we
         * could expect to receive refresh queries when a shortcutted suggestion is displayed in
         * Quick Search Box, in which case, the following Uris would be provided and we
         * would return a cursor with a single item representing the refreshed suggestion data.
         */
        newUriMatcher.addURI(PROVIDER_AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT);
        newUriMatcher.addURI(PROVIDER_AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", REFRESH_SHORTCUT);
        newUriMatcher.addURI(PROVIDER_AUTHORITY, LEX + "/" + SearchManager.SUGGEST_URI_PATH_SHORTCUT, LEXICAL_REFRESH_SHORTCUT);
        newUriMatcher.addURI(PROVIDER_AUTHORITY, LEX + "/" + SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", LEXICAL_REFRESH_SHORTCUT);
        return newUriMatcher;
    }

    protected static Map<String, String> createProjectionMap() 
    {
        Map<String, String> newProjectionMap = new HashMap<String, String>();
        newProjectionMap.put(BaseColumns._ID, 
                KEY_ID + " as " + BaseColumns._ID);
        newProjectionMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, 
                KEY_TITLE + " as " + SearchManager.SUGGEST_COLUMN_TEXT_1);
        newProjectionMap.put(SearchManager.SUGGEST_COLUMN_TEXT_2, 
                KEY_MODEL + " as " + SearchManager.SUGGEST_COLUMN_TEXT_2);
        newProjectionMap.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, 
                KEY_ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        return newProjectionMap;
    }



}
