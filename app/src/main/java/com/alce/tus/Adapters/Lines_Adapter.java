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

import com.alce.tus.Activities.SublinesActivity;
import com.alce.tus.R;
import com.alce.tus.Types.Type_Lines;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the Lines Recycler View.
 */
public class Lines_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 1;
    private static List<Integer> TYPE_HEADER;
    private final ArrayList<Type_Lines> data;
    private final Context mContext;

    public Lines_Adapter(Context context, ArrayList<Type_Lines> data) {
        this.mContext = context;
        this.data = data;
    }

    public static void setHeaderPosition(List<Integer> type_header) {
        TYPE_HEADER = type_header;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_transport_item, parent, false);

            return new VHItem(view);
        } else if (TYPE_HEADER.contains(viewType)) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_lines_header, parent, false);
            return new VHHeader(view);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof VHItem) {
            final Type_Lines dataItem = getItem(position);

            TextView number = ((VHItem) holder).number;
            number.setText(dataItem.getNumero());

            final TextView title = ((VHItem) holder).name;
            title.setText(dataItem.getNombre());

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
                    Intent intent = new Intent(mContext, SublinesActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("name", dataItem.getNombre());
                    intent.putExtra("line", dataItem.getLinea());
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.rtc, R.anim.ctl);
                }
            });
        } else if (holder instanceof VHHeader) {
            final Type_Lines dataItem = getItem(position);

            TextView title = ((VHHeader) holder).header;
            String text = "";
            switch (dataItem.getType()) {
                case "Urbano":
                    text = mContext.getResources().getString(R.string.urbano);
                    break;
                case "Especial":
                    text = mContext.getResources().getString(R.string.especial);
                    break;
                case "Nocturno":
                    text = mContext.getResources().getString(R.string.nocturno);
                    break;
            }
            title.setText(text);
            View line = ((Lines_Adapter.VHHeader) holder).line;

            SharedPreferences prefs = mContext.getSharedPreferences("preferences", Context.MODE_PRIVATE);
            if (prefs.getBoolean("color", true)) {
                title.setTextColor(mContext.getResources().getColor(R.color.color_tab_2_Dark));
                line.setBackgroundColor(mContext.getResources().getColor(R.color.color_tab_2_Dark));
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
        if (isPositionHeader(position))
            return TYPE_HEADER.get(position);

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return TYPE_HEADER.contains(position);
    }

    private Type_Lines getItem(int position) {
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