package com.alce.tus.Fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.alce.tus.Adapters.Sublines_Adapter;
import com.alce.tus.R;
import com.alce.tus.Types.Type_Sublines;

import java.util.ArrayList;

/**
 * Sublines Fragment.
 */
public class Sublines extends Fragment {

    private ArrayList<Type_Sublines> arrayList;
    private Parcelable mListState;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_basic, container, false);
        ViewFlipper viewFlipper = (ViewFlipper) view.findViewById(R.id.view_flipper);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        viewFlipper.setDisplayedChild(1);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        Sublines_Adapter adapter = new Sublines_Adapter(getContext(), arrayList);

        mRecyclerView.setAdapter(adapter);

    }

    public void data(ArrayList<Type_Sublines> arrayList) {
        this.arrayList = arrayList;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mListState != null) {
            mLayoutManager.onRestoreInstanceState(mListState);
        }
    }

    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if (mLayoutManager != null) {
            mListState = mLayoutManager.onSaveInstanceState();
            state.putParcelable("position", mListState);
            state.putParcelableArrayList("arrayList", arrayList);
        }
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