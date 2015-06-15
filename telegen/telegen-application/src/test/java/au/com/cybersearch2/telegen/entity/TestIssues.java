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
package au.com.cybersearch2.telegen.entity;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceContainer;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.entity.Issue;

/**
 * TestIssues
 * @author Andrew Bowley
 * 22 May 2015
 */
public class TestIssues 
{

	public static final String[][] ISSUE_DATA = new String[][] 
	{
	        { "Start", "The TV won't turn on" },
            { "Video", "There is no picture/video" },
            { "Remote", "The remote control does not work" },
            { "Set top box", "The cable/set top box remote control doesn't turn the TV on or off, or adjust the volume" }
	};
	
	public TestIssues() 
	{
	}

    /**
     * Populate entity tables. Call this before doing any queries. 
     * Note the calling thread is suspended while the work is performed on a background thread. 
     * @param puName Persistence Unit name
     * @throws InterruptedException
     */
    public void setUp(String puName) throws InterruptedException
    {
        // Persistence work adds issues to the database using JPA.
        // Hence there will be an enclosing transaction to ensure data consistency.
        // Any failure will result in an IllegalStateExeception being thrown from
        // the calling thread.
        PersistenceWork setUpWork = new PersistenceWork(){

            @Override
            public void doInBackground(EntityManagerLite entityManager)
            {
            	for (String[] issueItem: ISSUE_DATA)
            	{
            	    Issue issue = new Issue(issueItem[0], issueItem[1]);
            	    entityManager.persist(issue);
            	}
            }

            @Override
            public void onPostExecute(boolean success)
            {
                if (!success)
                    throw new IllegalStateException("Database set up failed. Check console for error details.");
            }

            @Override
            public void onRollback(Throwable rollbackException)
            {
                throw new IllegalStateException("Database set up failed. Check console for stack trace.", rollbackException);
            }
        };
        // Execute work and wait synchronously for completion
        PersistenceContainer container = new PersistenceContainer(puName);
        container.executeTask(setUpWork).waitForTask();
    }

}
