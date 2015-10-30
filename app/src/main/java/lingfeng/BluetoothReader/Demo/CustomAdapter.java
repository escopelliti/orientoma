package lingfeng.BluetoothReader.Demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Element;

import java.util.List;


public class CustomAdapter extends ArrayAdapter<Element> {
    private final Context context;

    public CustomAdapter(Context context, List<Element> objects) {
        super(context, -1, objects);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.custom_list_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
        textView.setText("UID: "+getItem(position).getAttribute("id"));
        TextView tv = (TextView) rowView.findViewById(R.id.secondLine);
        Element p = (Element) getItem(position).getParentNode();
        tv.setText("Mapped to node "+p.getAttribute("id"));
        return rowView;
    }
}
