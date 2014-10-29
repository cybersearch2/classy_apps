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
package au.com.cybersearch2.classyjpa.entity;


import static org.fest.assertions.api.Assertions.assertThat;

import javax.persistence.EntityExistsException;

import android.test.InstrumentationTestCase;
import au.com.cybersearch2.classyfy.ClassyFyApplication;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.transaction.EntityTransactionImpl;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classyutil.Transcript;

/**
 * LoaderPersistenceContainerTest
 * @author Andrew Bowley
 * 16/07/2014
 */
public class LoaderPersistenceContainerTest extends InstrumentationTestCase
{
    private Transcript transcript;
    private LoaderPersistenceContainer testContainer;

    @Override
    protected void setUp() throws Exception 
    {  
        System.setProperty( "dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath() );
        System.setProperty("java.util.logging.config.file", "src/logging.properties");
        super.setUp();
        assertThat(ClassyFyApplication.getInstance().waitForApplicationSetup()).isEqualTo(WorkStatus.FINISHED);
    }

    protected void doSetup()
    {
        transcript = new Transcript();
        testContainer = new LoaderPersistenceContainer(ClassyFyApplication.PU_NAME);
    }
    
    public void test_all() throws Throwable
    {
        doSetup();
        do_background_called();
        doSetup();
        do_rollback_only(); 
        doSetup();
        do_exception_thrown();
    }
    
    public void do_background_called() throws Throwable
    {
        final PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        final Executable[] exeHolder = new Executable[1];
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                exeHolder[0] = testContainer.executeTask(persistenceWork);
            }});
        synchronized(exeHolder[0])
        {
            exeHolder[0].wait(10000);
        }
        transcript.assertEventsSoFar("background task", "onPostExecute true");
        assertThat(exeHolder[0].getStatus()).isEqualTo(WorkStatus.FINISHED);
    }

    public void do_rollback_only() throws Throwable
    {
        final PersistenceWork persistenceWork = new TestPersistenceWork(transcript,
                new TestPersistenceWork.Callable(){

                    @Override
                    public Boolean call(EntityManagerLite entityManager)
                            throws Exception 
                    {
                        // Return false to cause transaction setRollbackOnly() to be called
                        return false;
                    }});
        final Executable[] exeHolder = new Executable[1];
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                exeHolder[0] = testContainer.executeTask(persistenceWork);
            }});
        synchronized(exeHolder[0])
        {
            exeHolder[0].wait();
        }
        transcript.assertEventsSoFar("background task", "onPostExecute false");
        assertThat(exeHolder[0].getStatus()).isEqualTo(WorkStatus.FAILED);
    }

    public void do_exception_thrown() throws Throwable
    {   
        final EntityExistsException persistException = new EntityExistsException("Entity of class RecordCategory, primary key 1 already exists");
        final PersistenceWork persistenceWork = new TestPersistenceWork(transcript,
                new TestPersistenceWork.Callable(){

                    @Override
                    public Boolean call(EntityManagerLite entityManager)
                            throws Exception 
                    {
                        throw persistException;
                    }});
        final Executable[] exeHolder = new Executable[1];
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                exeHolder[0] = testContainer.executeTask(persistenceWork);
            }});
        synchronized(exeHolder[0])
        {
            exeHolder[0].wait();
        }
        transcript.assertEventsSoFar("background task", "onRollback " + persistException.toString());
        assertThat(exeHolder[0].getStatus()).isEqualTo(WorkStatus.FAILED);
    }

    public void do_npe_thrown() throws Throwable
    {
        final PersistenceWork persistenceWork = new TestPersistenceWork(transcript,
                new TestPersistenceWork.Callable(){

                    @SuppressWarnings("null")
                    @Override
                    public Boolean call(EntityManagerLite entityManager)
                            throws Exception 
                    {
                        Object object = null;
                        object.toString();
                        return true;
                    }});
        final Executable[] exeHolder = new Executable[1];
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                exeHolder[0] = testContainer.executeTask(persistenceWork);
            }});
        synchronized(exeHolder[0])
        {
            exeHolder[0].wait();
        }
        transcript.assertEventsSoFar("background task", "onRollback java.lang.NullPointerException");
        assertThat(exeHolder[0].getStatus()).isEqualTo(WorkStatus.FAILED);
    }

    public void do_user_transaction() throws Throwable
    {
        final PersistenceWork persistenceWork = new TestPersistenceWork(transcript,
                new TestPersistenceWork.Callable(){

                    @Override
                    public Boolean call(EntityManagerLite entityManager)
                            throws Exception 
                    {
                        // Return false to cause transaction setRollbackOnly() to be called
                        // User Transactions get access to actual transaction
                        return entityManager.getTransaction() instanceof EntityTransactionImpl;
                    }});
        final Executable[] exeHolder = new Executable[1];
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                testContainer.setUserTransactionMode(true);
                exeHolder[0] = testContainer.executeTask(persistenceWork);
            }});
        synchronized(exeHolder[0])
        {
            exeHolder[0].wait();
        }
        transcript.assertEventsSoFar("background task", "onPostExecute true");
        assertThat(exeHolder[0].getStatus()).isEqualTo(WorkStatus.FINISHED);
    }
}
