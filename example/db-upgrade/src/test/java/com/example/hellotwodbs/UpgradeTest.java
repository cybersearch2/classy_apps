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
package com.example.hellotwodbs;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.example.HelloTwoDbsMain;

/**
 * UpgradeTest
 * @author Andrew Bowley
 * 31 Mar 2015
 */
@RunWith(RobolectricTestRunner.class)
public class UpgradeTest 
{
	@Test
	public void test_Upgrade() throws SQLException, IOException, InterruptedException
	{
		Runnable runnable = new Runnable(){

			@Override
			public void run() {
				PersistenceContext persistenceContext = new PersistenceContext();
				PersistenceAdmin simpleAdmin = persistenceContext.getPersistenceAdmin(HelloTwoDbsMain.PU_NAME1);
				ConnectionSource connectionSource1 = simpleAdmin.getConnectionSource();
				int simpleVersion = persistenceContext.getDatabaseSupport().getVersion(connectionSource1);
				System.out.println("Simple version = " + simpleVersion);
				synchronized(this)
				{
					notifyAll();
				}
			}};
		Thread starter = new Thread(runnable);
		starter.start();
		synchronized(runnable)
		{
			runnable.wait();
		}
		
	}
}
