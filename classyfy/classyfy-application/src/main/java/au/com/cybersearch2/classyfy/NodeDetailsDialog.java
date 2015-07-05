package au.com.cybersearch2.classyfy;

import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classywidget.ListItem;
import au.com.cybersearch2.classywidget.PropertiesListAdapter;


public class NodeDetailsDialog extends DialogFragment 
{
    class ItemClickListener implements OnItemClickListener
    {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id)
        {
            Uri actionUri = Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, String.valueOf(id));
            Intent wordIntent = new Intent(getActivity(), TitleSearchResultsActivity.class);
            wordIntent.setData(actionUri);
            wordIntent.setAction(Intent.ACTION_VIEW);
            startActivity(wordIntent);
            dialog.dismiss();
        }
        
    }
    
    public final static String DIALOG_TITLE = "Node Details";
    public final static String FOLDER_LIST = "Folders";
    public final static String CATEGORY_LIST = "Categories";
    public final static String HIERARCHY_LIST = "Hierarchy";
    
    Dialog dialog;
    ItemClickListener itemClickListener;
  
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
         View view = inflater.inflate(R.layout.node_details, container, false); 

         String title = getArguments().getString(ClassyFySearchEngine.KEY_TITLE);
         String model = getArguments().getString(ClassyFySearchEngine.KEY_MODEL);
         StringBuilder builder = new StringBuilder();
         if ((model != null) && (model.length() > 0))
             builder.append(model).append(": ");
         if ((title != null) && (title.length() > 0))
             builder.append(title);
         else
             builder.append('?'); // This is an error. Handle gracefully.
         dialog.setTitle(builder.toString());
         LinearLayout propertiesLayout = (LinearLayout) view.findViewById(R.id.node_properties);
         List<ListItem> hierarchy = getArguments().getParcelableArrayList(HIERARCHY_LIST);
         if ((hierarchy != null) && (hierarchy.size() > 0))
             propertiesLayout.addView(createDynamicLayout("Hierarchy", hierarchy));
         List<ListItem> categoryTitles = getArguments().getParcelableArrayList(CATEGORY_LIST);
         if ((categoryTitles != null) && (categoryTitles.size() > 0))
             propertiesLayout.addView(createDynamicLayout("Categories", categoryTitles));
         List<ListItem> folderTitles = getArguments().getParcelableArrayList(FOLDER_LIST);
         if ((folderTitles != null) && (folderTitles.size() > 0))
             propertiesLayout.addView(createDynamicLayout("Folders", folderTitles));
         //Node node = (Node) getArguments().get(MainActivity.NODE_KEY);
         //createDynamicLayout(propertiesLayout, node.getProperties());
         return view;
     }

    protected View createDynamicLayout(String title, List<ListItem> items)
    {
        if (itemClickListener == null)
            itemClickListener = new ItemClickListener();
        LinearLayout dynamicLayout = new LinearLayout(getActivity());
        dynamicLayout.setOrientation(LinearLayout.VERTICAL);
        int layoutHeight = LinearLayout.LayoutParams.MATCH_PARENT;
        int layoutWidth = LinearLayout.LayoutParams.MATCH_PARENT;
        TextView titleView = new TextView(getActivity());
        titleView.setText(title);
        titleView.setTextColor(Color.BLUE);
        LinearLayout titleLayout = new LinearLayout(getActivity());
        titleLayout.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams titleLayoutParms = new LinearLayout.LayoutParams(layoutWidth, layoutHeight);
        titleLayout.addView(titleView, titleLayoutParms);
        dynamicLayout.addView(titleLayout);
        ListView itemList = new ListView(getActivity());
        PropertiesListAdapter listAdapter = new PropertiesListAdapter(getActivity(), items);
        listAdapter.setSingleLine(true);
        itemList.setAdapter(listAdapter);
        itemList.setOnItemClickListener(itemClickListener);
        dynamicLayout.addView(itemList);
        return dynamicLayout;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) 
    {
        dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(DIALOG_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }
    
    public Dialog getDialog()
    {
        return dialog;
    }
}