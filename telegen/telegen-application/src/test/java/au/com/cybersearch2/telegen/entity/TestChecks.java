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
import au.com.cybersearch2.entity.Check;

/**
 * TestChecks
 * @author Andrew Bowley
 * 22 May 2015
 */
public class TestChecks 
{

	public static final String[][] CHECK_DATA = new String[][] 
	{
	        { "Power cord", "Make sure the AC power cord is securely plugged in to the wall outlet" },
            { "Wall outlet", "Make sure the wall outlet is working" },
            { "Remote", "Try pressing the POWER button on the TV to make sure the problem is nto the remote." },
            { "Connections", "Check cable connections (remove and reconnect all cables connected to the TV and external devices)" },
            { "Cables", "Set your external device's (Cable/Set Box, DVD, Blu-ray etc) video outputs to match the connections to the TV input." +
              "For example, if an external device's output is HDMI, it should be connected to an HDMI input on the TV." },
            { "Connected devices", "Make sure your connected devices are powered on." },
            { "Source", "Be sure to select the TV's correct source by pressing the SOURCE button on the remote control." },
            { "Running state",  "Reboot the connected device by unplugging and then reconnecting the device's power cable" },
            { "Batteries", "Replace the remote control batteries with the poles (+/-) in the right direction" },
            { "Sensor", "Clean the sensor's transmission window on the remote" },
            { "Pointing", "Try pointing the remote directly at the TV from 5-6 feet away" },
            { "Programme", "Programme the Cable/Set remote control to operate the TV. Refer to the Cable/Set user manual for the SAMSUNG TV code" },
            { "Support", "Contact Support" }
            
	};
	
	public TestChecks() 
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
        // Persistence work adds checks to the database using JPA.
        // Hence there will be an enclosing transaction to ensure data consistency.
        // Any failure will result in an IllegalStateExeception being thrown from
        // the calling thread.
        PersistenceWork setUpWork = new PersistenceWork(){

            @Override
            public void doInBackground(EntityManagerLite entityManager)
            {
            	for (String[] checkItem: CHECK_DATA)
            	{
            	    Check check = new Check(checkItem[0], checkItem[1]);
            	    entityManager.persist(check);
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
