package com.alce.tus.Fragments;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.alce.tus.Activities.MainActivity;
import com.alce.tus.Adapters.Lines_Adapter;
import com.alce.tus.Data.Database;
import com.alce.tus.Data.DatabaseManager;
import com.alce.tus.R;
import com.alce.tus.Types.Type_Lines;

import java.util.ArrayList;
import java.util.List;


/**
 * Lines Fragment.
 */
public class Lines extends Fragment {

    private Parcelable mListState;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<Type_Lines> arrayList;
    private RecyclerView mRecyclerView;
    private ViewFlipper viewFlipper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_basic, container, false);
        viewFlipper = (ViewFlipper) view.findViewById(R.id.view_flipper);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        viewFlipper.setDisplayedChild(1);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mRecyclerView.setNestedScrollingEnabled(false);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        arrayList = Type_Lines.getSublines();

        if (arrayList == null || arrayList.isEmpty()) {
            String DB_DATA = "info.db";
            Database database = new Database(getContext(), DB_DATA);
            SQLiteDatabase db = database.getReadableDatabase();
            DatabaseManager databaseManager = new DatabaseManager(getContext(), db, DB_DATA);
            databaseManager.extractLines();
            arrayList = Type_Lines.getSublines();
        }

        if (arrayList != null && !arrayList.isEmpty()) {

            List<Integer> headerPosition = Type_Lines.getSublineasPos();
            Lines_Adapter.setHeaderPosition(headerPosition);

            Lines_Adapter adapter = new Lines_Adapter(getContext(), arrayList);
            mRecyclerView.setAdapter(adapter);
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (dy > 0) {
                        MainActivity.bottomNavigation.hideBottomNavigation();
                    }
                    if (dy < 0) {
                        MainActivity.bottomNavigation.restoreBottomNavigation();
                    }
                }
            });
        } else {
            viewFlipper.setDisplayedChild(0);
            View view = getView();
            if (view != null) {
                TextView textView = (TextView) view.findViewById(R.id.emptyText);
                textView.setText(R.string.loadError);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mListState != null) {
            mLayoutManager.onRestoreInstanceState(mListState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        mListState = mLayoutManager.onSaveInstanceState();
        state.putParcelable("position", mListState);
        state.putParcelableArrayList("arrayList", arrayList);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable("position");
            arrayList = savedInstanceState.getParcelableArrayList("arrayList");
        }
    }
}