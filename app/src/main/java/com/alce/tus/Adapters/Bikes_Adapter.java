package com.alce.tus.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alce.tus.Activities.DetailActivity;
import com.alce.tus.R;
import com.alce.tus.Types.Type_Bikes;

import java.util.ArrayList;

/**
 * Adapter for the Lines Recycler View.
 */
public class Bikes_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 1;
    private static int TYPE_HEADER;
    private final ArrayList<Type_Bikes> data;
    private final Context mContext;

    public Bikes_Adapter(Context context, ArrayList<Type_Bikes> data) {
        this.mContext = context;
        this.data = data;
    }

    public static void setHeaderPosition() {
        TYPE_HEADER = 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_transport_item, parent, false);

            return new VHItem(view);
        } else if (viewType == TYPE_HEADER) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_lines_header, parent, false);
            return new VHHeader(view);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof VHItem) {
            final Type_Bikes dataItem = getItem(position);

            TextView number = ((VHItem) holder).number;
            number.setText(dataItem.getId());

            final TextView title = ((VHItem) holder).name;
            title.setText(dataItem.getName());

            SharedPreferences prefs = mContext.getSharedPreferences("preferences", Context.MODE_PRIVATE);
            if (prefs.getBoolean("color", true))
                number.setTextColor(mContext.getResources().getColor(R.color.color_tab_3_Dark));
            else
                number.setTextColor(mContext.getResources().getColor(R.color.gray_panther));

            LinearLayout linearLayout = ((VHItem) holder).linearLayout;
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Activity activity = (Activity) mContext;
                    Intent intent = new Intent(mContext, DetailActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("type", "bike");
                    intent.putExtra("numParada", dataItem.getId());
                    intent.putExtra("nParada", dataItem.getName());
                    intent.putExtra("lat", dataItem.getLat());
                    intent.putExtra("lng", dataItem.getLng());
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.rtc, R.anim.ctl);
                }
            });
        } else if (holder instanceof VHHeader) {

            TextView title = ((VHHeader) holder).header;
            String text = mContext.getString(R.string.bikes);
            title.setText(text);
            View line = ((Bikes_Adapter.VHHeader) holder).line;

            SharedPreferences prefs = mContext.getSharedPreferences("preferences", Context.MODE_PRIVATE);
            if (prefs.getBoolean("color", true)) {
                title.setTextColor(mContext.getResources().getColor(R.color.color_tab_3_Dark));
                line.setBackgroundColor(mContext.getResources().getColor(R.color.color_tab_3_Dark));
            } else {
                title.setTextColor(mContext.getResources().getColor(R.color.gray_panther));
                line.setBackgroundColor(mContext.getResources().getColor(R.color.gray_panther));
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == TYPE_HEADER)
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private Type_Bikes getItem(int position) {
        return data.get(position);
    }

    class VHItem extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView number;
        final LinearLayout linearLayout;

        public VHItem(View itemView) {
            super(itemView);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout);
            name = (TextView) itemView.findViewById(R.id.name);
            number = (TextView) itemView.findViewById(R.id.number);

        }
    }

    class VHHeader extends RecyclerView.ViewHolder {
        final TextView header;
        final View line;

        private VHHeader(View itemView) {
            super(itemView);
            header = (TextView) itemView.findViewById(R.id.header);
            line = itemView.findViewById(R.id.line);

        }
    }
}