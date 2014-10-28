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

import static org.fest.assertions.api.Assertions.assertThat;
import android.database.Cursor;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.test.mock.MockContext;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import au.com.cybersearch2.classyfy.ClassyFyApplication;
import au.com.cybersearch2.classytask.WorkStatus;


/**
 * @author andrew
 *
 */
public class ClassyFyProviderTest extends ProviderTestCase2<ClassyFyProvider>
{
    private static final String TAG = ClassyFyProviderTest.class.getSimpleName();
    static boolean firstTime = true;

    private MockContentResolver mockResolver;

	public ClassyFyProviderTest()
    {
	    super(au.com.cybersearch2.classyfy.provider.ClassyFyProvider.class, ClassyFySearchEngine.PROVIDER_AUTHORITY);
        super.setContext(new MockContext());
    }

    @Override
    protected void setUp() throws Exception 
    {
        if (firstTime)
        {
            firstTime = false;
            System.setProperty( "dexmaker.dexcache", "/data/data/au.com.cybersearch2.classyfy/cache");
            System.setProperty("java.util.logging.config.file", "src/logging.properties");
            super.setUp();
            assertThat(ClassyFyApplication.getInstance().waitForApplicationSetup()).isEqualTo(WorkStatus.FINISHED);
        }
        super.setUp();
        Log.d(TAG, "setUp: ");
        mockResolver = getMockContentResolver();
    }

    @Override
    protected void tearDown() throws Exception 
    {
        Log.d(TAG, "tearDown:");
        super.tearDown();
    }

    @SmallTest
    public void testAll_Nodes_Query()
    {
        // Defines a projection of column names to return for a query
        final String[] TEST_PROJECTION = {
                ClassyFySearchEngine.KEY_TITLE,
                ClassyFySearchEngine.KEY_NAME,
                ClassyFySearchEngine.KEY_MODEL
        };
        // Defines a selection column for the query. When the selection columns are passed
        // to the query, the selection arguments replace the placeholders.
        final String TITLE_SELECTION = ClassyFySearchEngine.KEY_TITLE + " = " + "?";

        // Defines the selection columns for a query.
        final String SELECTION_COLUMNS =
            TITLE_SELECTION + " OR " + TITLE_SELECTION + " OR " + TITLE_SELECTION;

         // Defines the arguments for the selection columns. Put in sort order for sort check below
        final String[] SELECTION_ARGS = { "Policy & Procedures", "Premises", "Professional Associations"  };

         // Defines a query sort order
        final String SORT_ORDER = ClassyFySearchEngine.KEY_TITLE + " ASC";

        // A query that uses selection criteria should return only those rows that match the
        // criteria. Use a projection so that it's easy to get the data in a particular column.
        Cursor projectionCursor = mockResolver.query(
            ClassyFySearchEngine.CONTENT_URI, // the URI for the main data table
            TEST_PROJECTION,           // get the title, and model columns
            SELECTION_COLUMNS,         // select on the title column
            SELECTION_ARGS,            // select titles "Note0", "Note1", or "Note5"
            SORT_ORDER                 // sort ascending on the title column
        );

        // Asserts that the cursor has the same number of rows as the number of selection arguments
        assertEquals(SELECTION_ARGS.length, projectionCursor.getCount());

        int index = 0;

        while (projectionCursor.moveToNext()) {

            // Asserts that the selection argument at the current index matches the value of
            // the title column (column 0) in the current record of the cursor
            assertEquals(SELECTION_ARGS[index], projectionCursor.getString(0));
            //System.out.println(projectionCursor.getString(0));
            index++;
        }

        // Asserts that the index pointer is now the same as the number of selection arguments, so
        // that the number of arguments tested is exactly the same as the number of rows returned.
        assertEquals(SELECTION_ARGS.length, index);
    }
    /** @return a ContentValues object with a value set for each MetadataType column */
    /*
    public static ContentValues getFullMetadataTypeContentValues() 
    {
        ContentValues v = new ContentValues(2);
        v.put(ClassyFyProvider.KEY_NAME,       VALID_METADATA_TYPE_NAME);
        v.put(ClassyFyProvider.KEY_DESCRIPTION ,   VALID_METADATA_TYPE_DESCRIPTION);
        return v;
    }

    @SmallTest
    public void testMetadataTypeInsert__inserts_a_valid_record() {
        Uri uri = mockResolver.insert(ClassyFyProvider.CONTENT_URI, getFullMetadataTypeContentValues());
        assertEquals(1L, ContentUris.parseId(uri));
    }

*/
    
}
