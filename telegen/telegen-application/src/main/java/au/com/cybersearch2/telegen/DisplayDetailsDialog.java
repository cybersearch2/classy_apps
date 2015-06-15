package au.com.cybersearch2.telegen;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;


public class DisplayDetailsDialog extends DialogFragment 
{
    public static final String KEY_TITLE = "Telegen.Title";
    public static final String KEY_CONTEXT = "Telegen.Model";
    
    protected Dialog dialog;
  
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    
         View view = inflater.inflate(R.layout.node_details, container, false); 

         String title = getArguments().getString(KEY_TITLE);
         String context = getArguments().getString(KEY_CONTEXT);
         if ((title != null) && (title.length() > 0))
         {
             TextView tv1 = (TextView)view.findViewById(R.id.node_detail_title);
             tv1.setText(title);
         }
         if ((context != null) && (context.length() > 0))
         {
             TextView tv2 = (TextView)view.findViewById(R.id.node_detail_model);
             tv2.setText(context);
        }
         //LinearLayout propertiesLayout = (LinearLayout) view.findViewById(R.id.node_properties);
         //Node node = (Node) getArguments().get(MainActivity.NODE_KEY);
         //createDynamicLayout(propertiesLayout, node.getProperties());
         return view;
     }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) 
    {
        dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Troubleshooting");
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }
    
    public Dialog getDialog()
    {
        return dialog;
    }
    
    public void createDynamicLayout(LinearLayout propertiesLayout, Map<String,Object> valueMap)
    {
        /*
        FieldDescriptor descriptionField = new FieldDescriptor();
        descriptionField.setOrder(1);
        descriptionField.setName("description");
        descriptionField.setTitle("Description");
        FieldDescriptor createdField = new FieldDescriptor();
        createdField.setOrder(2);
        createdField.setName("created");
        createdField.setTitle("Created");
        FieldDescriptor creatorField = new FieldDescriptor();
        creatorField.setOrder(3);
        creatorField.setName("creator");
        creatorField.setTitle("Creator");
        FieldDescriptor modifiedField = new FieldDescriptor();
        modifiedField.setOrder(4);
        modifiedField.setName("modified");
        modifiedField.setTitle("Modified");
        FieldDescriptor modifier = new FieldDescriptor();
        modifier.setOrder(5);
        modifier.setName("modifier");
        modifier.setTitle("Modifier");
        FieldDescriptor identifierField = new FieldDescriptor();
        identifierField.setOrder(6);
        identifierField.setName("identifier");
        identifierField.setTitle("Identifier");
        Set<FieldDescriptor> fieldSet = new TreeSet<FieldDescriptor>();
        fieldSet.add(descriptionField);
        fieldSet.add(createdField);
        fieldSet.add(creatorField);
        fieldSet.add(modifiedField);
        fieldSet.add(modifier);
        fieldSet.add(identifierField);
        LinearLayout dynamicLayout = new LinearLayout(getActivity());
        dynamicLayout.setOrientation(LinearLayout.VERTICAL);
        int layoutHeight = LinearLayout.LayoutParams.MATCH_PARENT;
        int layoutWidth = LinearLayout.LayoutParams.MATCH_PARENT;
        for (FieldDescriptor descriptor: fieldSet)
        {
            Object value = valueMap.get(descriptor.getName());
            if (value == null)
                continue;
            TextView titleView = new TextView(getActivity());
            titleView.setText(descriptor.getTitle());
            TextView valueView = new TextView(getActivity());
            valueView.setText(value.toString());
            LinearLayout fieldLayout = new LinearLayout(getActivity());
            fieldLayout.setOrientation(LinearLayout.HORIZONTAL);
            LayoutParams titleLayoutParms = new LinearLayout.LayoutParams(layoutWidth, layoutHeight);
            titleLayoutParms.gravity = Gravity.LEFT;
            fieldLayout.addView(titleView, titleLayoutParms);
            LayoutParams valueLayoutParms = new LinearLayout.LayoutParams(layoutWidth, layoutHeight);
            valueLayoutParms.gravity = Gravity.RIGHT;
            fieldLayout.addView(valueView, valueLayoutParms);
            propertiesLayout.addView(fieldLayout);
        }
        */
    }
}