package com.alce.tus.Adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alce.tus.R;
import com.alce.tus.Types.Type_Info;

import java.util.List;

/**
 * Adapter for the Detail Recycler View.
 */
public class Detail_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 1;
    private final List<Type_Info> data;
    private final String type;
    private final Context mContext;

    public Detail_Adapter(Context mContext, String type, List<Type_Info> data) {
        this.data = data;
        this.type = type;
        this.mContext = mContext;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_detail, parent, false);

            return new VHItem(view);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final Type_Info dataItem = getItem(position);

        if (type.equals("bike")) {

            TextView title = ((VHItem) holder).name;
            title.setVisibility(View.GONE);
            View line = ((VHItem) holder).line;
            line.setVisibility(View.GONE);
            TextView tiempo1 = ((VHItem) holder).tiempo1;
            tiempo1.setText(mContext.getString(R.string.stands));
            TextView distancia1 = ((VHItem) holder).distancia1;
            distancia1.setText(dataItem.getStands());
            TextView tiempo2 = ((VHItem) holder).tiempo2;
            tiempo2.setText(mContext.getString(R.string.tab3));
            TextView distancia2 = ((VHItem) holder).distancia2;
            distancia2.setText(dataItem.getBikes());

        } else {

            String time1, time2, distance1, distance2;
            if (dataItem.getTiempo1() > 1) {
                time1 = Integer.toString(dataItem.getTiempo1()) + " " + mContext.getString(R.string.minute) + "s";
            } else if (dataItem.getTiempo1() < 0) {
                time1 = "";
            } else {
                time1 = Integer.toString(dataItem.getTiempo1()) + " " + mContext.getString(R.string.minute);
            }

            if (dataItem.getTiempo2() > 1) {
                time2 = Integer.toString(dataItem.getTiempo2()) + " " + mContext.getString(R.string.minute) + "s";
            } else if (dataItem.getTiempo2() < 0) {
                time2 = "";
            } else {
                time2 = Integer.toString(dataItem.getTiempo2()) + " " + mContext.getString(R.string.minute);
            }

            if (dataItem.getDistancia1() > 1)
                if (dataItem.getDistancia1() / 1000.f > 1)
                    distance1 = Integer.toString(dataItem.getDistancia1() / 1000) + " " + mContext.getString(R.string.Km);
                else
                    distance1 = Integer.toString(dataItem.getDistancia1()) + " " + mContext.getString(R.string.meter) + "s";
            else
                distance1 = Integer.toString(dataItem.getDistancia1()) + " " + mContext.getString(R.string.meter);


            if (dataItem.getDistancia2() > 1)
                if (dataItem.getDistancia2() / 1000.f > 1)
                    distance2 = Integer.toString(dataItem.getDistancia2() / 1000) + " " + mContext.getString(R.string.Km);
                else
                    distance2 = Integer.toString(dataItem.getDistancia2()) + " " + mContext.getString(R.string.meter) + "s";
            else
                distance2 = Integer.toString(dataItem.getDistancia2()) + " " + mContext.getString(R.string.meter);


            TextView title = ((VHItem) holder).name;
            String text = dataItem.getLinea() + " - " + dataItem.getRuta();
            title.setText(text);
            TextView tiempo1 = ((VHItem) holder).tiempo1;
            tiempo1.setText(time1);
            TextView distancia1 = ((VHItem) holder).distancia1;
            distancia1.setText(distance1);
            TextView tiempo2 = ((VHItem) holder).tiempo2;
            tiempo2.setText(time2);
            TextView distancia2 = ((VHItem) holder).distancia2;
            distancia2.setText(distance2);

            RelativeLayout block1 = ((VHItem) holder).block1;
            RelativeLayout block2 = ((VHItem) holder).block2;
            CardView cardView = ((VHItem) holder).cardView;

            if (time1.equals(""))
                block1.setVisibility(View.GONE);
            if (time2.equals(""))
                block2.setVisibility(View.GONE);
            if (time1.equals("") && time2.equals(""))
                cardView.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    private Type_Info getItem(int position) {
        return data.get(position);
    }

    class VHItem extends RecyclerView.ViewHolder {
        final TextView name;
        final View line;
        final TextView tiempo1;
        final TextView distancia1;
        final TextView tiempo2;
        final TextView distancia2;
        final RelativeLayout block1;
        final RelativeLayout block2;
        final CardView cardView;

        public VHItem(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.cardView);
            name = (TextView) itemView.findViewById(R.id.name);
            line = itemView.findViewById(R.id.line);
            block1 = (RelativeLayout) itemView.findViewById(R.id.block1);
            tiempo1 = (TextView) itemView.findViewById(R.id.tiempo1);
            distancia1 = (TextView) itemView.findViewById(R.id.distancia1);
            block2 = (RelativeLayout) itemView.findViewById(R.id.block2);
            tiempo2 = (TextView) itemView.findViewById(R.id.tiempo2);
            distancia2 = (TextView) itemView.findViewById(R.id.distancia2);

        }
    }
}