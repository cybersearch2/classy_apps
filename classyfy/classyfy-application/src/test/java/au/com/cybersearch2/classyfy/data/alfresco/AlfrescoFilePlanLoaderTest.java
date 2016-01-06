package au.com.cybersearch2.classyfy.data.alfresco;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.io.Writer;

import javax.inject.Named;
import javax.inject.Singleton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import android.net.Uri;
import org.robolectric.RobolectricTestRunner;

import au.com.cybersearch2.classyapp.ApplicationContext;
import au.com.cybersearch2.classyapp.ApplicationLocale;
import au.com.cybersearch2.classydb.DatabaseAdminImpl;
import au.com.cybersearch2.classydb.NativeScriptDatabaseWork;
import au.com.cybersearch2.classyfy.ClassyFyApplicationModule;
import au.com.cybersearch2.classyfy.ClassyFyStartup;
import au.com.cybersearch2.classyfy.TestClassyFyApplication;
import au.com.cybersearch2.classynode.Node;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import au.com.cybersearch2.classyfy.data.DataStreamParser;
import au.com.cybersearch2.classyfy.data.SqlFromNodeGenerator;
import au.com.cybersearch2.classyfy.data.TestDataStreamParser;
import au.com.cybersearch2.classyfy.data.alfresco.AlfrescoFilePlanLoader;
import au.com.cybersearch2.classyfy.data.alfresco.AlfrescoFilePlanXmlParser;
import au.com.cybersearch2.classyfy.provider.ClassyFyProvider;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classyfy.test.AndroidEnvironmentFile;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyinject.DependencyProvider;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;

@RunWith(RobolectricTestRunner.class)
public class AlfrescoFilePlanLoaderTest
{
    @Singleton
    @Component(modules = ClassyFyApplicationModule.class)  
    static interface TestComponent extends ApplicationModule
    {
        void inject(ApplicationContext applicationContext);
        void inject(ClassyFyStartup classyFyStartup); 
        void inject(ClassyFyProvider classyFyProvider);
        void inject(ClassyFySearchEngine classyFySearchEngine);
        void inject(ApplicationLocale ApplicationLocale);
        void inject(PersistenceContext persistenceContext);
        void inject(PersistenceFactory persistenceFactory);
        void inject(NativeScriptDatabaseWork nativeScriptDatabaseWork);
        void inject(DatabaseAdminImpl databaseAdminImpl);
        AlfrescoFilePlanLoaderComponent plus(AlfrescoFilePlanLoaderModule alfrescoFilePlanLoaderModule);
    }

    @Module
    static class AlfrescoFilePlanLoaderModule implements DependencyProvider<TestAlfrescoFilePlanLoader>
    {
        private TestDataStreamParser alfrescoFilePlanXmlParser;
        private SqlFromNodeGenerator sqlFromNodeGenerator;
        
        public AlfrescoFilePlanLoaderModule()
        {
            alfrescoFilePlanXmlParser = new TestDataStreamParser();
            sqlFromNodeGenerator = mock(SqlFromNodeGenerator.class);
        }
        
        @Provides @Named("AlfrescoFilePlan") DataStreamParser provideDataStreamParser() 
        {
            return alfrescoFilePlanXmlParser;
        }

        @Provides SqlFromNodeGenerator provideSqlFromNodeGenerator() 
        {
            return sqlFromNodeGenerator;
        }

        public TestDataStreamParser getTestDataStreamLoader()
        {
            return alfrescoFilePlanXmlParser;
        }
        
        public SqlFromNodeGenerator getSqlFromNodeGenerator()
        {
            return sqlFromNodeGenerator;
        }
    }
 
    /**
     * Inject AlfrescoFilePlanLoader object using subcomponent to permit scoping
     * @author Andrew Bowley
     * 4 Jan 2016
     */
    @Singleton // <- not @PerActivity
    @Subcomponent(modules = AlfrescoFilePlanLoaderModule.class)
    public interface AlfrescoFilePlanLoaderComponent
    {
        void inject(TestAlfrescoFilePlanLoader alfrescoFilePlanLoader);
    }
    
    private static String PUBLIC_DOWNLOADS_PATH = "src/test/java/External/Download";
    private static String DATA_FILENAME = "cybersearch2-fileplan.xml";
    private static AndroidEnvironmentFile testFile;
    private static AlfrescoFilePlanLoaderModule testAlfrescoFilePlanLoaderModule;

    @Before
    public void setup() throws Exception 
    {
        // Reset ShadowSQLiteConnection to avoid "Illegal connection pointer" exception 
        //ShadowSQLiteConnection.reset();
    	File tempfile = new File(".");
    	System.out.println("CWD = " + tempfile.getAbsolutePath());
        if (testFile == null)
        {
            testAlfrescoFilePlanLoaderModule = new AlfrescoFilePlanLoaderModule();
            TestClassyFyApplication testApplication = TestClassyFyApplication.getTestInstance();
            TestComponent component = 
                    DaggerAlfrescoFilePlanLoaderTest_TestComponent.builder()
                    .classyFyApplicationModule(new ClassyFyApplicationModule(testApplication))
                    .build();
            DI.getInstance(component, testAlfrescoFilePlanLoaderModule);
            testApplication.startup();
            testApplication.waitForApplicationSetup();
            testFile = new AndroidEnvironmentFile(PUBLIC_DOWNLOADS_PATH,DATA_FILENAME);
        }
    }

    @Test
    public void testAlfrescoFilePlanLoader() throws Exception
    {
        AlfrescoFilePlanLoader alfrescoFilePlanLoader = new TestAlfrescoFilePlanLoader();
        TestDataStreamParser dataStreamLoader = testAlfrescoFilePlanLoaderModule.getTestDataStreamLoader();
        assertThat(dataStreamLoader).isNotNull();
        AlfrescoFilePlanXmlParser alfrescoFilePlanXmlParser = mock(AlfrescoFilePlanXmlParser.class);
        Node rootNode = Node.rootNodeNewInstance();
        when(alfrescoFilePlanXmlParser.parseDataStream(isA(InputStream.class))).thenReturn(rootNode);
        dataStreamLoader.setDataStreamParser(alfrescoFilePlanXmlParser);
        Uri dataUri = Uri.fromFile(testFile.getTestFile());

        alfrescoFilePlanLoader.loadData(dataUri);
        SqlFromNodeGenerator sqlFromNodeGenerator = testAlfrescoFilePlanLoaderModule.getSqlFromNodeGenerator();
        verify(alfrescoFilePlanXmlParser).parseDataStream(isA(InputStream.class));
        verify(sqlFromNodeGenerator).generateSql(eq(rootNode), isA(Writer.class));
        assertThat(((TestAlfrescoFilePlanLoader)alfrescoFilePlanLoader).processFilesCallable).isNotNull();
    }
  
}
