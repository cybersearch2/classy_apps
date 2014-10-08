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
package au.com.cybersearch2.classyfy.data.alfresco;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.sql.SQLException;

import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * TestAlfrescoFilePlanLoader
 * @author Andrew Bowley
 * 14/04/2014
 */
public class TestAlfrescoFilePlanLoader extends AlfrescoFilePlanLoader	 
{
    DatabaseConnection databaseConnection;
    
    @Override
    protected Executable executeTask(ConnectionSource connectionSource)
    {
        databaseConnection = mock(DatabaseConnection.class);
        final boolean[] success =  { false };
        try
        {
            writeToDatabase(databaseConnection);
            success[0] = true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return new Executable(){

            @Override
            public WorkStatus getStatus() 
            {
                return success[0] ? WorkStatus.FINISHED : WorkStatus.FAILED;
            }};
    }
}
