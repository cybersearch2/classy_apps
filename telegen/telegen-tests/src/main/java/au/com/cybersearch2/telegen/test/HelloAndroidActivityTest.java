package au.com.cybersearch2.telegen.test;

import android.test.ActivityInstrumentationTestCase2;
import au.com.cybersearch2.telegen.*;

public class HelloAndroidActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public HelloAndroidActivityTest() {
        super(MainActivity.class); 
    }

    public void testActivity() {
        MainActivity activity = getActivity();
        assertNotNull(activity);
    }
}

