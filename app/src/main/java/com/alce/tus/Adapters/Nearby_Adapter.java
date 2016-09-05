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
import com.alce.tus.Types.Type_Near;

import java.util.ArrayList;

/**
 * Adapter for the Nearby Recycler View.
 */

public class Nearby_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<Type_Near> data;
    private final Activity activity;
    private final Context mContext;

    public Nearby_Adapter(Activity activity, Context mContext, ArrayList<Type_Near> data) {
        this.activity = activity;
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_nearby_item, parent, false);
            return new Nearby_Adapter.VHItem(view);
        } else if (viewType == 1) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_lines_header, parent, false);
            return new Nearby_Adapter.VHHeader(view);
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof Nearby_Adapter.VHItem) {

            final int pos = holder.getAdapterPosition();
            final Type_Near dataItem = getItem(pos);

            TextView nearbyName = ((VHItem) holder).nearbyName;
            nearbyName.setText(dataItem.getNParada());

            TextView distance = ((VHItem) holder).distance;
            String text = Integer.toString(dataItem.getDist().intValue()) + " " + mContext.getResources().getString(R.string.m);
            distance.setText(text);

            SharedPreferences prefs = mContext.getSharedPreferences("preferences", Context.MODE_PRIVATE);
            if (prefs.getBoolean("color", true))
                distance.setTextColor(mContext.getResources().getColor(R.color.color_tab_4_Dark));
            else
                distance.setTextColor(mContext.getResources().getColor(R.color.gray_panther));

            LinearLayout linearLayout = ((VHItem) holder).linearNearby;
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, DetailActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("type", data.get(pos).getType());
                    if (data.get(pos).getType().equals("bike")) {
                        intent.putExtra("lat", data.get(pos).getLat());
                        intent.putExtra("lng", data.get(pos).getLng());
                    }
                    intent.putExtra("numParada", data.get(pos).getNumParada());
                    intent.putExtra("nParada", data.get(pos).getNParada());
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.rtc, R.anim.ctl);
                }
            });
        } else if (holder instanceof Nearby_Adapter.VHHeader) {
            TextView title = ((Nearby_Adapter.VHHeader) holder).header;
            title.setText(R.string.nearbyStops);
            View line = ((Nearby_Adapter.VHHeader) holder).line;

            SharedPreferences prefs = mContext.getSharedPreferences("preferences", Context.MODE_PRIVATE);
            if (prefs.getBoolean("color", true)) {
                title.setTextColor(mContext.getResources().getColor(R.color.color_tab_4_Dark));
                line.setBackgroundColor(mContext.getResources().getColor(R.color.color_tab_4_Dark));
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
        if (position == 0)
            return 1;
        else
            return 0;
    }

    private Type_Near getItem(int position) {
        return data.get(position);
    }

    class VHItem extends RecyclerView.ViewHolder {
        final TextView nearbyName;
        final TextView distance;
        final LinearLayout linearNearby;

        private VHItem(View itemView) {
            super(itemView);
            nearbyName = (TextView) itemView.findViewById(R.id.nearbyName);
            distance = (TextView) itemView.findViewById(R.id.distance);
            linearNearby = (LinearLayout) itemView.findViewById(R.id.linearNearby);
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