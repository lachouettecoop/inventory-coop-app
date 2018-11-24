package org.coop.inventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.coop.inventory.model.CountView;

import java.util.List;

public class CountsArrayAdapter extends ArrayAdapter<CountView> {
    private final Context context;
    private final List<CountView> counts;

    public CountsArrayAdapter(Context context, List<CountView> counts) {
        super(context, -1, counts);
        this.context = context;
        this.counts = counts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.count, parent, false);
        TextView productName = rowView.findViewById(R.id.productName);
        productName.setText(counts.get(position).productName);
        TextView qty = rowView.findViewById(R.id.qty);
        qty.setText(Double.toString(counts.get(position).quantity));

        return rowView;
    }
}
