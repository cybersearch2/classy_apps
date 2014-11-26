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
package au.com.cybersearch2.example;

import java.sql.SQLException;

import javax.persistence.PersistenceException;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableUtils;

import android.content.Context;
import au.com.cybersearch2.classyapp.ApplicationContext;
import au.com.cybersearch2.classyapp.ContextModule;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.EntityManagerDelegate;
import au.com.cybersearch2.classyjpa.entity.PersistenceDao;
import au.com.cybersearch2.classyjpa.persist.ConnectionSourceFactory;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.entity.PersistenceTask;

/**
 * HelloTwoDbsMain_v1 Version 1 uses ConnectionType.memory and drops all tables
 * at the start to achieve repeatable test of version upgrade.
 * @author Andrew Bowley
 * 20 Nov 2014
 */
public class HelloTwoDbsMain_v1 extends au.com.cybersearch2.example.HelloTwoDbsMain
{
	public static String DATABASE_INFO_NAME = "User_info";
	
    /** Dependency injection data object */
    private HelloTwoDbsModule_v1 helloTwoDbsModule;
    protected Context context;
   
    public HelloTwoDbsMain_v1(final Context context)
    {
       super();
       if (context == null)
            throw new IllegalArgumentException("Paramemter \"context\" is null");
       this.context = context;
    }

    /**
     * Populate entity tables. Call this before doing any queries. 
     * Note the calling thread is suspended while the work is performed on a background thread. 
     * @throws InterruptedException
     */
    @Override
    public void setUp() throws InterruptedException
    {
    	if (!applicationInitialized)
    	{
    		initializeApplication();
    		dropDatabaseTables();
    		initializeDatabase();
    		if (connectionType != ConnectionType.memory)
    		{
    			clearDatabaseTables();
    		}
    		applicationInitialized = true;
    	}
    	super.setUp();
    }
	/**
	 * Set up dependency injection, which creates an ObjectGraph from a HelloTwoDbsModule_v1 configuration object.
	 */
	@Override
	protected void createObjectGraph()
	{
        // Set up dependency injection, which creates an ObjectGraph from a HelloTwoDbsModule_v1 configuration object
        helloTwoDbsModule = new HelloTwoDbsModule_v1();
        ContextModule contextModule = new ContextModule(context);
        new DI(helloTwoDbsModule, contextModule).validate();
        ApplicationContext applicationContex = new ApplicationContext();
        if (applicationContex.getContext() == null)
            throw new IllegalStateException("ApplicationContext \"context\" is null");
        if (applicationContex.getContext().getAssets() == null)
            throw new IllegalStateException("ApplicationContext \"assets\" is null");
	}
	
	public void dropDatabaseTables() throws InterruptedException
	{
        // Persistence task clears Simple table the helloTwoDb1.db database using JPA. 
		performPersistenceWork(PU_NAME1, new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
		    	EntityManagerDelegate delegate = (EntityManagerDelegate) entityManager.getDelegate();
		    	@SuppressWarnings("unchecked")
				PersistenceDao<SimpleData, Integer> simpleDao = (PersistenceDao<SimpleData, Integer>) delegate.getDaoForClass(SimpleData.class);
		    	try 
		    	{
		    		if (simpleDao.isTableExists())
		    			TableUtils.dropTable(simpleDao.getConnectionSource(), SimpleData.class, false);
				} 
		    	catch (SQLException e) 
		    	{
					throw new PersistenceException(e);
				}
                logMessage(PU_NAME1, "Dropped table \"Simple\"");
			}});
    	// Persistence task drops then creates Complex table in the helloTwoDb2.db database using JPA.
		performPersistenceWork(PU_NAME2, new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
		    	EntityManagerDelegate delegate = (EntityManagerDelegate) entityManager.getDelegate();
		    	@SuppressWarnings("unchecked")
		    	PersistenceDao<ComplexData, Integer> complexDao = (PersistenceDao<ComplexData, Integer>) delegate.getDaoForClass(ComplexData.class);
		    	try 
		    	{
		    		if (complexDao.isTableExists())
		    			TableUtils.dropTable(complexDao.getConnectionSource(), ComplexData.class, false);
				} 
		    	catch (SQLException e) 
		    	{
					throw new PersistenceException(e);
				}
				logMessage(PU_NAME2, "Dropped table \"Complex\"");
			}});
	}
	
}
