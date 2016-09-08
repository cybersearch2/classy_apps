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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import au.com.cybersearch2.classyfy.data.alfresco.AlfrescoFilePlanLoaderTest;
import au.com.cybersearch2.classyfy.provider.ClassyFyProviderTest;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngineTest;
import au.com.cybersearch2.classyfy.test.SqlParserTest;
import au.com.cybersearch2.classyfy.xml.XmlParserTest;

/**
 * ClassyFyTestSuite
 * @author Andrew Bowley
 * 08/07/2014
 */
// Encountered Robolectric 3.1.1 does not support API 24 Appcompat Toolbar or ActionBar:
//android.view.InflateException: XML file 
//./target/unpacked-libs/cas_appcompat-v7_24.2.0/res/layout/abc_action_menu_item_layout.xml 
// line #-1 (sorry, not yet implemented): null
// Therefore, IntegrateMainActivityTest and TitleSearchResultsActivityTest are skipped pending next Robolectric release
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ClassyfyLogicTest.class,
    MainActivityTest.class,
    // See above note
    //TitleSearchResultsActivityTest.class,
    //
    ClassyFyProviderTest.class,
    ClassyFySearchEngineTest.class,
    SqlParserTest.class,
    XmlParserTest.class,
    AlfrescoFilePlanLoaderTest.class,
    // See above note
    //IntegrateMainActivityTest.class
    //
})
public class ClassyFyTestSuite
{
}
