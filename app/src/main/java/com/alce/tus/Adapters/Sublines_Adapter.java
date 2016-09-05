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
import com.alce.tus.Types.Type_Sublines;

import java.util.List;

/**
 * Adapter for the Sublines Recycler View.
 */
public class Sublines_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 1;
    private final List<Type_Sublines> data;
    private final Context mContext;

    public Sublines_Adapter(Context context, List<Type_Sublines> data) {
        this.mContext = context;
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_transport_item, parent, false);

            return new VHItem(view);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final Type_Sublines dataItem = getItem(position);

        TextView number = ((VHItem) holder).number;
        number.setText(dataItem.getNumParada());

        final TextView title = ((VHItem) holder).header;
        title.setText(dataItem.getnParada());


        SharedPreferences prefs = mContext.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        if (prefs.getBoolean("color", true))
            number.setTextColor(mContext.getResources().getColor(R.color.color_tab_2_Dark));
        else
            number.setTextColor(mContext.getResources().getColor(R.color.gray_panther));

        LinearLayout linearLayout = ((VHItem) holder).linearLayout;
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity activity = (Activity) mContext;
                Intent intent = new Intent(mContext, DetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("numParada", dataItem.getNumParada());
                intent.putExtra("nParada", dataItem.getnParada());
                intent.putExtra("type", "bus");
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.rtc, R.anim.ctl);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    private Type_Sublines getItem(int position) {
        return data.get(position);
    }


    class VHItem extends RecyclerView.ViewHolder {
        final TextView header;
        final TextView number;
        final LinearLayout linearLayout;

        public VHItem(View itemView) {
            super(itemView);
            header = (TextView) itemView.findViewById(R.id.name);
            number = (TextView) itemView.findViewById(R.id.number);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout);

        }
    }
}