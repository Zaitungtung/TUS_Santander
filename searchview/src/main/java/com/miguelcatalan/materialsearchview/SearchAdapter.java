package com.miguelcatalan.materialsearchview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Suggestions Adapter.
 *
 * @author Miguel Catalan Bañuls
 */
public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private final HashMap<String, SearchTypes> dataMap;
    private final List<String> suggestionsName;
    private final List<String> suggestionsNumber;
    private final List<String> type;
    private final List<String> filterType = new ArrayList<>();
    private final Context mContext;
    private final LayoutInflater inflater;
    private final boolean ellipsize;
    private final View.OnClickListener onItemClickListener;
    private ArrayList<String> data;
    public SearchAdapter(Context context, List<String> suggestionsName, List<String> suggestionsNumber,
                         List<String> type, boolean ellipsize,
                         View.OnClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        this.mContext = context;
        inflater = LayoutInflater.from(context);
        data = new ArrayList<>();
        this.suggestionsName = suggestionsName;
        this.suggestionsNumber = suggestionsNumber;
        this.type = type;
        this.ellipsize = ellipsize;
        this.dataMap = new HashMap<>();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                // Retrieve the autocomplete results.
                List<String> searchData = new ArrayList<>();
                dataMap.clear();
                if (!TextUtils.isEmpty(constraint)) {
                    if (suggestionsName != null) {
                        for (int i = 0; i != suggestionsName.size(); i++) {
                            String number = suggestionsNumber.get(i);
                            String name = suggestionsName.get(i);
                            String types = type.get(i);
                            if (number.contains(constraint.toString())
                                    && !dataMap.containsKey(number) ||
                                    name.toLowerCase().contains(constraint.toString().toLowerCase())
                                            && !dataMap.containsKey(name)) {
                                dataMap.put(number, SearchTypes.SUGGESTION_TYPE);
                                dataMap.put(name, SearchTypes.SUGGESTION_TYPE);
                                searchData.add(name);
                                filterType.add(types);
                            }
                        }
                    }
                }
                // Assign the data to the FilterResults
                filterResults.values = searchData;
                filterResults.count = searchData.size();
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values != null && results.values instanceof ArrayList) {
                    data = (ArrayList<String>) results.values;
                    notifyDataSetChanged();
                }
            }
        };
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private String getItem(int position) {
        return data.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.suggest_item, parent, false);
        return new VHItem(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView textView = ((VHItem) holder).textView;
        ImageView imageView = ((VHItem) holder).imageView;
        RelativeLayout searchList = ((VHItem) holder).searchList;

        if (filterType.get(position).equals("bike"))
            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_other));
        else
            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_original));

        String currentListData = getItem(position);
        textView.setText(currentListData);
        if (ellipsize) {
            textView.setSingleLine();
            textView.setEllipsize(TextUtils.TruncateAt.END);
        }

        searchList.setOnClickListener(onItemClickListener);
    }

    @Override
    public int getItemViewType(int position) {
        return dataMap.get(data.get(position)).ordinal();
    }

    private enum SearchTypes {
        SUGGESTION_TYPE
    }

    class VHItem extends RecyclerView.ViewHolder {
        final RelativeLayout searchList;
        final TextView textView;
        final ImageView imageView;

        public VHItem(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.suggestion_text);
            imageView = (ImageView) itemView.findViewById(R.id.suggestion_icon);
            searchList = (RelativeLayout) itemView.findViewById(R.id.searchList);
        }
    }
}