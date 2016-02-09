/**
    Copyright (C) 2016  www.cybersearch2.com.au

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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceUnitInfo;

import org.xmlpull.v1.XmlPullParserException;

import com.j256.ormlite.dao.DaoManager;

import android.content.Context;
import android.support.v4.content.Loader;
import android.util.Log;
import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classyfts.FtsEngine;
import au.com.cybersearch2.classyfy.ClassyFyApplication;
import au.com.cybersearch2.classyfy.ClassyFyComponent;
import au.com.cybersearch2.classyfy.DaggerClassyFyComponent;
import au.com.cybersearch2.classyfy.data.RecordCategory;
import au.com.cybersearch2.classyfy.data.RecordFolder;
import au.com.cybersearch2.classyfy.data.RecordModel;
import au.com.cybersearch2.classyfy.module.ClassyFyApplicationModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classynode.EntityByNodeIdGenerator;
import au.com.cybersearch2.classynode.Node;
import au.com.cybersearch2.classytask.AsyncBackgroundTask;

/**
 * FactoryTask
 * @author Andrew Bowley
 * 8 Feb 2016
 */
public class FactoryTask extends AsyncBackgroundTask
{
    public interface MainThreadCallback
    {
        void onTaskComplete(ClassyFyComponent classyFyComponent, ClassyFySearchEngine classyFySearchEngine);
        void onTaskFail(String cause);
    }
    
    public static final String TAG = "FactoryTask";
    /** Name of query to get Category record by id */
    public static final String CATEGORY_BY_NODE_ID = Node.NODE_BY_PRIMARY_KEY_QUERY + RecordModel.recordCategory.ordinal();
    /** Name of query to get Folder record by id */
    public static final String FOLDER_BY_NODE_ID = Node.NODE_BY_PRIMARY_KEY_QUERY + RecordModel.recordFolder.ordinal();

    private ClassyFySearchEngine classyFySearchEngine;
    private ClassyFyComponent classyFyComponent;
    private MainThreadCallback mainThreadCallback;
    private String errorMessage;
    
    /**
     * @param context
     */
    public FactoryTask(Context context, MainThreadCallback mainThreadCallback)
    {
        super(context);
        this.mainThreadCallback = mainThreadCallback;
        // Default error message
        errorMessage = TAG + " failed to complete";
    }

    @Override
    public Boolean loadInBackground()
    {
        Log.i(TAG, "Loading in background...");
        // Get perisistence context to trigger database initialization
        // Build Dagger2 configuration
        if (Log.isLoggable(TAG, Log.INFO))
            Log.i(TAG, "ClassyFy application Dagger build");
        DaoManager.clearCache();
        final ClassyFyApplication application = ClassyFyApplication.getInstance();
        try
        {
            classyFyComponent = 
                    DaggerClassyFyComponent.builder()
                    .classyFyApplicationModule(new ClassyFyApplicationModule(application))
                    .build();
            ResourceEnvironment resourceEnvironment = classyFyComponent.resourceEnvironment();
            Map<String, PersistenceUnitInfo> puMap = PersistenceFactory.getPersistenceUnitInfo(resourceEnvironment);
            List<String> managedClassNames = puMap.get(ClassyFyProvider.PU_NAME).getManagedClassNames();
            Log.i(TAG, managedClassNames.toString());
            PersistenceContext persistenceContext = classyFyComponent.persistenceContext();
            startApplicationSetup(persistenceContext);
        }
        catch (PersistenceException e)
        {
            errorMessage = e.getMessage();
            Log.e(TAG, "Database error on initialization", e);
            return Boolean.FALSE;
        }
        catch (IOException e)
        {
            Log.e(TAG, "Database error on initialization", e);
            return Boolean.FALSE;
        }
        catch (XmlPullParserException e)
        {
            Log.e(TAG, "Database error on initialization", e);
            return Boolean.FALSE;
        }
        classyFySearchEngine = classyFyComponent.classyFySearchEngine();
        FtsEngine ftsEngine = classyFyComponent.ftsEngine();
        classyFySearchEngine.setFtsQuery(ftsEngine);
        return Boolean.TRUE;
    }

    @Override
    public void onLoadComplete(Loader<Boolean> loader, Boolean success)
    {
        if (success)
            mainThreadCallback.onTaskComplete(classyFyComponent, classyFySearchEngine);
        else
            mainThreadCallback.onTaskFail(errorMessage);
        Log.i(TAG, "Loading completed " + success);
     }

    public ClassyFySearchEngine getClassyFySearchEngine()
    {
        return classyFySearchEngine;
    }

    public ClassyFyComponent getClassyFyComponent()
    {
        return classyFyComponent;
    }

    protected static void startApplicationSetup(PersistenceContext persistenceContext)
    {
        try
        {
            // Persistence system configured by persistence.xml contains one or more Persistence Unitst
            // Set up named queries to find Category and Folder by Node ID
            PersistenceAdmin persistenceAdmin = persistenceContext.getPersistenceAdmin(ClassyFyProvider.PU_NAME);
            EntityByNodeIdGenerator entityByNodeIdGenerator = new EntityByNodeIdGenerator();
            persistenceAdmin.addNamedQuery(RecordCategory.class, CATEGORY_BY_NODE_ID, entityByNodeIdGenerator);
            persistenceAdmin.addNamedQuery(RecordFolder.class, FOLDER_BY_NODE_ID, entityByNodeIdGenerator);
        }
        catch (PersistenceException e)
        {   // All SQLExceptions are rethrown as PersistenceExceptions
            Log.e(TAG, "Database initialisation failed", e);
        }
    }
}
