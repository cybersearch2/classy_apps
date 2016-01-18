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
package au.com.cybersearch2.classyfy;

import javax.persistence.PersistenceException;

import com.j256.ormlite.dao.DaoManager;

import android.app.Application;
import android.content.Context;
import android.support.v4.content.Loader;
import android.util.Log;
import au.com.cybersearch2.classyfy.data.RecordCategory;
import au.com.cybersearch2.classyfy.data.RecordFolder;
import au.com.cybersearch2.classyfy.data.RecordModel;
import au.com.cybersearch2.classyfy.data.alfresco.AlfrescoFilePlanSubcomponent;
import au.com.cybersearch2.classyfy.helper.ConfigureLog4J;
import au.com.cybersearch2.classyfy.module.AlfrescoFilePlanModule;
import au.com.cybersearch2.classyfy.module.ClassyFyApplicationModule;
import au.com.cybersearch2.classyfy.module.ClassyLogicModule;
import au.com.cybersearch2.classyfy.provider.ClassyFyProvider;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classynode.EntityByNodeIdGenerator;
import au.com.cybersearch2.classynode.Node;
import au.com.cybersearch2.classytask.AsyncBackgroundTask;

/**
 * ClassyFyApplication
 * Launches ClassyFy persistence
 * @author Andrew Bowley
 * 19 Jun 2015
 */
public class ClassyFyApplication extends Application implements ClassyFyComponent
{
    public static final String TAG = "ClassyFyApplication";
    /** Persistence unit name refers to peristence.xml in assets */
    public static final String PU_NAME = "classyfy";
    /** Name of query to get Category record by id */
    public static final String CATEGORY_BY_NODE_ID = Node.NODE_BY_PRIMARY_KEY_QUERY + RecordModel.recordCategory.ordinal();
    /** Name of query to get Folder record by id */
    public static final String FOLDER_BY_NODE_ID = Node.NODE_BY_PRIMARY_KEY_QUERY + RecordModel.recordFolder.ordinal();
    /** Limit maximum number of search results */
    public static final int SEARCH_RESULTS_LIMIT = 50; // Same as Android
    
    /** Object to initialize ClassyFy persistence, on which the appliication depends */
    /** Singleton */
    static ClassyFyApplication singleton;
    /** Dagger2 Application Component - ClassyFy will not run until this variable is set */
    protected ClassyFyComponent classyFyComponent;
    /** Start state tracks appplication initialization progress */
    volatile protected StartState startState;

    /**
     * Construct ClassyFyApplication object
     */
    public ClassyFyApplication()
    {
        singleton = this;
        startState = StartState.precreate;
    }
 
    /**
     * onCreate
     * @see android.app.Application#onCreate()
     */
    @Override public void onCreate() 
    {
        super.onCreate();
        startApplication();
    }
    
    public void startApplication()
    {
        // Configure Log4j used by Ormlite SQLite library
        ConfigureLog4J.configure();
        if (Log.isLoggable(TAG, Log.INFO))
            Log.i(TAG, "Start ClassyFy application");
        // Use Async Loader build Dagger2 configuration which includes
        // database initialization
        final Context context = this;
        AsyncBackgroundTask starter = 
            new AsyncBackgroundTask(this)
            {
                @Override
                public Boolean loadInBackground()
                {
                    if (Log.isLoggable(TAG, Log.INFO))
                        Log.i(TAG, "ClassyFy application Dagger build");
                    startState = StartState.build;
                    // Clear out ORMLite internal caches.
                    DaoManager.clearCache();
                    classyFyComponent = 
                            DaggerClassyFyComponent.builder()
                            .classyFyApplicationModule(new ClassyFyApplicationModule(context))
                            .build();
                     startApplicationSetup(classyFyComponent.persistenceContext());
                     return Boolean.TRUE;
                }
                @Override
                public void onLoadComplete(Loader<Boolean> loader, Boolean success)
                {
                    startState = success ? StartState.run : StartState.fail;
                    if (Log.isLoggable(TAG, Log.INFO))
                        Log.i(TAG, "ClassyFy application state: " + startState);
                    synchronized(startState)
                    {
                        startState.notifyAll();
                    }
                }
            };
        starter.startLoading();
    }

    /**
     * Returns Dagger2 application component but blocks if
     * it is not available due to initialization in progress.
     * NOTE
     * MainActivity and other injectees must call inject() from 
     * background thread when responding to OnCreate event.
     * @return
     */
    public ClassyFyComponent getClassyFyComponent()
    {
        if (classyFyComponent == null)
        {
            synchronized(startState)
            {
                if (startState.isStarting())
                    try
                    {
                        startState.wait();
                    }
                    catch (InterruptedException e)
                    {
                    }
            }
            if (startState != StartState.run)
                throw new RuntimeException("ClassyFy Application failed to start");
        }
        return classyFyComponent;
    }
    
    @Override
    public void inject(MainActivity mainActivity)
    {
        getClassyFyComponent().inject(mainActivity);
    }
    
    @Override
    public void inject(TitleSearchResultsActivity titleSearchResultsActivity)
    {
        classyFyComponent.inject(titleSearchResultsActivity);
    }
    
    @Override
    public void inject(ClassyFyProvider classyFyProvider)
    {
        getClassyFyComponent().inject(classyFyProvider);
    }
    
    @Override
    public ClassyLogicComponent plus(ClassyLogicModule classyLogicModule)
    {
        return classyFyComponent.plus(classyLogicModule);
    }

    @Override
    public AlfrescoFilePlanSubcomponent plus(AlfrescoFilePlanModule alfrescoFilePlanModule)
    {
        return classyFyComponent.plus(alfrescoFilePlanModule);
    }

    @Override
    public PersistenceContext persistenceContext()
    {
        return classyFyComponent.persistenceContext();
    }

    @Override
    public ClassyFySearchEngine classyFySearchEngine()
    {
        return classyFyComponent.classyFySearchEngine();
    }

    protected void onAndroidCreate()
    {
        super.onCreate();
    }
    
    protected void startApplicationSetup(PersistenceContext persistenceContext)
    {
        try
        {
            // Persistence system configured by persistence.xml contains one or more Persistence Unitst
            // Set up named queries to find Category and Folder by Node ID
            PersistenceAdmin persistenceAdmin = persistenceContext.getPersistenceAdmin(ClassyFyApplication.PU_NAME);
            EntityByNodeIdGenerator entityByNodeIdGenerator = new EntityByNodeIdGenerator();
            persistenceAdmin.addNamedQuery(RecordCategory.class, ClassyFyApplication.CATEGORY_BY_NODE_ID, entityByNodeIdGenerator);
            persistenceAdmin.addNamedQuery(RecordFolder.class, ClassyFyApplication.FOLDER_BY_NODE_ID, entityByNodeIdGenerator);
        }
        catch (PersistenceException e)
        {   // All SQLExceptions are rethrown as PersistenceExceptions
            Log.e(TAG, "Database initialisation failed", e);
        }
    }

    public static ClassyFyApplication getInstance()
    {
        return singleton;
    }



}
