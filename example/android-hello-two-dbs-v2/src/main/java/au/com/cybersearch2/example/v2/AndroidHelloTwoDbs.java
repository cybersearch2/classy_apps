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

import com.j256.ormlite.support.ConnectionSource;

import android.content.Context;
import au.com.cybersearch2.classyapp.ContextModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.persist.ConnectionSourceFactory;
import au.com.cybersearch2.classyapp.ApplicationContext;

/**
 * AndroidHelloTwoDbs
 * @author Andrew Bowley
 * 23 Sep 2014
 */
public class AndroidHelloTwoDbs extends HelloTwoDbsMain
{
    protected AndroidHelloTwoDbsModule androidHelloTwoDbsModule;
    protected Context context;
   
    public AndroidHelloTwoDbs(final Context context)
    {
       super();
       if (context == null)
            throw new IllegalArgumentException("Paramemter \"context\" is null");
       this.context = context;
    }

   /**
    * Initialize entity tables ensuring version is correct and contains initial data. 
    * Call this before doing any queries. 
    * Note the calling thread is suspended while the work is performed on a background thread. 
    * @throws InterruptedException
    */
    public void setUp() throws InterruptedException
    {
    	// Do not call super setUp() as it handles change of database version, for which Android has other arrangements
	   	if (!applicationInitialized)
	   	{
	   		initializeApplication();
	   		/*
	   		// Get a connectionSource to trigger potential database create/update
			ConnectionSourceFactory connectionSourceFactory = persistenceFactory.getPersistenceUnit(PU_NAME1).getPersistenceAdmin();
			connectionSourceFactory.getConnectionSource();
			connectionSourceFactory = persistenceFactory.getPersistenceUnit(PU_NAME2).getPersistenceAdmin();
			connectionSourceFactory.getConnectionSource();
			*/
			initializeDatabase();
			populateDatabase1();
			populateDatabase2();
	   		applicationInitialized = true;
	   	}
    }
    
    @Override
    protected void createObjectGraph()
    {
        // Set up dependency injection, which creates an ObjectGraph from a HelloTwoDbsModule configuration object
        androidHelloTwoDbsModule = new AndroidHelloTwoDbsModule();
        ContextModule contextModule = new ContextModule(context);
        new DI(androidHelloTwoDbsModule, contextModule).validate();
        ApplicationContext applicationContex = new ApplicationContext();
       if (applicationContex.getContext() == null)
            throw new IllegalStateException("ApplicationContext \"context\" is null");
       if (applicationContex.getContext().getAssets() == null)
            throw new IllegalStateException("ApplicationContext \"assets\" is null");
    }
}
