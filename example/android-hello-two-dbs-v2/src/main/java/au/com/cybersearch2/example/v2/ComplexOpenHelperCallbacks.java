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
package au.com.cybersearch2.example.v2;

import javax.persistence.PersistenceException;

import com.example.hellotwodbs.HelloTwoDbsApplication;
import com.j256.ormlite.support.ConnectionSource;

import android.database.sqlite.SQLiteDatabase;
import au.com.cybersearch2.classydb.ClassyOpenHelperCallbacks;
import au.com.cybersearch2.example.PersistenceTask;

/**
 * HelloTwoDbsOpenHelperCallbacks
 * @author Andrew Bowley
 * 21 Nov 2014
 */
public class ComplexOpenHelperCallbacks extends ClassyOpenHelperCallbacks 
{
	protected AndroidHelloTwoDbs androidHelloTwoDbs;

	/**
	 * @param puName
	 */
	public ComplexOpenHelperCallbacks() 
	{
		super(AndroidHelloTwoDbs.PU_NAME2);
	    androidHelloTwoDbs = HelloTwoDbsApplication.getAndroidHelloTwoDbsSingleton();
	}

    /**
     * What to do when your database needs to be created. Usually this entails creating the tables and loading any
     * initial data.
     * 
     * <p>
     * <b>NOTE:</b> You should use the connectionSource argument that is passed into this method call or the one
     * returned by getConnectionSource(). If you use your own, a recursive call or other unexpected results may result.
     * </p>
     * 
     * @param database
     *            Database being created.
     * @param connectionSource
     *            To use get connections to the database to be created.
     */
    @Override
    public void onCreate(SQLiteDatabase database,
            ConnectionSource connectionSource) 
    {
       	//PersistenceTask persistenceTask = androidHelloTwoDbs.getPopulateTask2();
        //persistenceTask.doTask(persistenceAdmin.createEntityManager(connectionSource));
    }
    
    /**
     * What to do when your database needs to be updated. This could mean careful migration of old data to new data.
     * Maybe adding or deleting database columns, etc..
     * 
     * <p>
     * <b>NOTE:</b> You should use the connectionSource argument that is passed into this method call or the one
     * returned by getConnectionSource(). If you use your own, a recursive call or other unexpected results may result.
     * </p>
     * 
     * @param database
     *            Database being upgraded.
     * @param connectionSource
     *            To use get connections to the database to be updated.
     * @param oldVersion
     *            The version of the current database so we can know what to do to the database.
     * @param newVersion
     *            The version that we are upgrading the database to.
     */
    @Override
    public void onUpgrade(SQLiteDatabase database,
            ConnectionSource connectionSource, int oldVersion,
            int newVersion) 
    {
    	AndroidDatabaseUpgrader upgrader = new AndroidDatabaseUpgrader();
    	try 
    	{
    		upgrader.doUpgrade(oldVersion, newVersion, AndroidHelloTwoDbs.PU_NAME2);
		} 
    	catch (InterruptedException e) 
    	{
			throw new PersistenceException(AndroidHelloTwoDbs.PU_NAME2 + " interrupted in onUpgrade()");
		}
   }
}
