package au.com.cybersearch2.telegen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import au.com.cybersearch2.classy_logic.ProviderManager;
import au.com.cybersearch2.classy_logic.QueryProgram;
import au.com.cybersearch2.classy_logic.Result;
import au.com.cybersearch2.classy_logic.list.AxiomTermList;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.AndroidPersistenceEnvironment;
import au.com.cybersearch2.classywidget.PropertiesListAdapter;
import au.com.cybersearch2.classywidget.PropertiesListAdapter.Value;

@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity 
{
    static public final String TELEGEN_XPL =
            "axiom issue : resource \"telegen\";\n" +
            "axiom check : resource \"telegen\";\n" +
            "axiom issue_param (issue_name): parameter;\n" +
            "axiom check_param (check_name): parameter;\n" +
            "template issue_item (name, observation);\n" +
            "template check_item (name, instruction);\n" +
            "choice first_check\n"  +
            "    (  issue_name,           check_name): \n" +
            "    (\"Start\",            \"Power cord\"),\n" +
            "    (\"Video\",            \"Connections\"),\n" +
            "    (\"Remote\",           \"Batteries\"),\n" +
            "    (\"Set top box\",      \"Programme\"),\n" +
            "    (true,                 \"Support\");\n" +
            "choice next_check\n"  +
            "    (  check_name,           next_check): \n" +
            "    (\"Power cord\",       \"Wall outlet\"),\n" +
            "    (\"Wall outlet\",      \"Remote\"),\n" +
            "    (\"Remote\",           \"Support\"),\n" +
            "    (\"Connections\",      \"Cables\"),\n" +
            "    (\"Cables\",           \"Connected devices\"),\n" +
            "    (\"Connected devices\",\"Source\"),\n" +
            "    (\"Source\",           \"Running state\"),\n" +
            "    (\"Running state\",    \"Support\"),\n" +
            "    (\"Batteries\",        \"Sensor\"),\n" +
            "    (\"Sensor\",           \"Pointing\"),\n" +
            "    (\"Pointing\",         \"Support\"),\n" +
            "    (\"Programme\",        \"Support\"),\n" +
            "    (true,                 \"Support\");\n" +
            "list issues_list(issue_item);\n" +
            "query issues (issue:issue_item);\n" +
            "query checks (check:check_item);\n" +
            "query first_check_query calc(issue_param: first_check);\n" +
            "query next_check_query calc(check_param: next_check);";

    private static final String ISSUES_QUERY = "issues";

    protected Dialog dialog;
    protected DisplayListFragment displayListFragment;
    protected PropertiesListAdapter adapter;
    protected List<Value> fieldList;
    protected String currentQuery;
    
    /** Android Persistence implementation which has a custom feature of providing the underlying SQLiteOpenHelper */
    @Inject @Named(TelegenApplication.PU_NAME) 
    AndroidPersistenceEnvironment androidPersistenceEnvironment;
    @Inject
    ProviderManager providerManager;

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after 
     * previously being shut down then this Bundle contains the data it most 
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_list_fragment);
        displayListFragment = getIssuesListFragment();
        adapter = new PropertiesListAdapter(this);
        displayListFragment.setListAdapter(adapter);
        DI.inject(this);
        providerManager.putAxiomProvider(new TelegenAxiomProvider(TelegenApplication.PU_NAME));
        displayIssues();
    }

    protected DisplayListFragment getIssuesListFragment()
    {
        return (DisplayListFragment) getSupportFragmentManager().findFragmentById(R.id.display_list_fragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
	    // Inflate the menu; this adds items to the action bar if it is present.
	    getMenuInflater().inflate(R.menu.main, menu);
	    return true;
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);      
        setIntent(intent);
        parseIntent(intent);
    }

    protected void parseIntent(Intent intent)
    {
        if (Intent.ACTION_MAIN.equals(intent.getAction())) 
            displayIssues();
    }
    
    protected Iterator<AxiomTermList> getIssues()
    {
        currentQuery = ISSUES_QUERY;
        QueryProgram queryProgram = new QueryProgram(TELEGEN_XPL);
        Result result = queryProgram.executeQuery(ISSUES_QUERY);
        return result.getIterator("issues_list");
    }

    
    protected void displayIssues()
    {
        fieldList = new ArrayList<Value>();
        Iterator<AxiomTermList> iterator = getIssues();
        while (iterator.hasNext())
        {
            AxiomTermList axiomTermList =iterator.next();
            fieldList.add(new Value(axiomTermList.getItem(0).getValue().toString(), 
                                    axiomTermList.getItem(1).getValue().toString()));
        }
        adapter.changeData(fieldList);
        displayListFragment.getListView().setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title = fieldList.get(position).getName();
                String context = fieldList.get(position).getValue();
                Bundle args = new Bundle();
                //args.putLong(ClassyFySearchEngine.KEY_ID, nodeId);
                args.putString(DisplayDetailsDialog.KEY_TITLE, title);
                args.putString(DisplayDetailsDialog.KEY_CONTEXT, context);
                dialog = showDialog(getSupportFragmentManager(), args);
            }
        });
    }

    protected Dialog showDialog(FragmentManager fragmentManager, Bundle args) 
    {
        DisplayDetailsDialog newFragment = new DisplayDetailsDialog();
        newFragment.setArguments(args);
        newFragment.show(fragmentManager, "dialog");
        return newFragment.getDialog();
    }

}

