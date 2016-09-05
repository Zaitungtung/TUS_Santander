package com.alce.tus.Fragments;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.alce.tus.Adapters.FavsAdapter;
import com.alce.tus.Data.Database;
import com.alce.tus.Data.DatabaseManager;
import com.alce.tus.R;
import com.alce.tus.Types.Type_Fav;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

/**
 * Home Fragment.
 */
public class Home extends Fragment {

    private static final ArrayList<Type_Fav> arrayList = new ArrayList<>();
    private static DatabaseReference mDatabase;
    private static Boolean calledAlready = false;
    private static ViewFlipper viewFlipper;
    private static TextView emptyText;
    private static FavsAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private Type_Fav deleted = null;
    private Query database;
    private ChildEventListener childEventListener;

    private static void getDatabase() {
        if (mDatabase == null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            mDatabase = database.getReference();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!calledAlready) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            calledAlready = true;
        }
        View view = inflater.inflate(R.layout.fragment_basic, container, false);
        viewFlipper = (ViewFlipper) view.findViewById(R.id.view_flipper);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        emptyText = (TextView) view.findViewById(R.id.emptyText);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        initRecyclerView();
        viewFlipper.setDisplayedChild(0);
        emptyText.setText(getString(R.string.emptyFavs));

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null)
            getFavs(user.getUid());
        else {
            Task task = new Task();
            task.execute(getContext());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (database != null && childEventListener != null)
            database.removeEventListener(childEventListener);

    }

    private void getFavs(final String userId) {
        getDatabase();

        arrayList.clear();

        database = mDatabase.child("users").child(userId).orderByChild("pos");

        arrayList.add(0, new Type_Fav(null, null, 0, "", 0));
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                viewFlipper.setDisplayedChild(1);
                Type_Fav added = dataSnapshot.getValue(Type_Fav.class);
                if (deleted != null) {
                    if (deleted.getPos() < added.getPos()) {
                        Type_Fav fav = new Type_Fav(
                                added.getNumber(),
                                added.getName(),
                                added.getPos() - 1,
                                added.getCustomName(),
                                added.getCustomColor());
                        mDatabase.child("users").child(userId).child(added.getNumber()).setValue(fav);
                    }
                }
                arrayList.add(added);

                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                changeUI(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                viewFlipper.setDisplayedChild(1);
                deleted = dataSnapshot.getValue(Type_Fav.class);
                String number = deleted.getNumber();
                for (int i = 1; i != arrayList.size(); i++) {
                    if (arrayList.get(i).getNumber().equals(number)) {
                        arrayList.remove(i);
                        break;
                    }
                }

                if (arrayList.size() <= 1)
                    viewFlipper.setDisplayedChild(0);

                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d("DEBUG", "Han movido " + dataSnapshot.getValue(Type_Fav.class).getName());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        database.addChildEventListener(childEventListener);
    }

    private void changeUI(DataSnapshot dataSnapshot) {
        Type_Fav existingFav = dataSnapshot.getValue(Type_Fav.class);
        String number = existingFav.getNumber();
        for (int i = 1; i != arrayList.size(); i++) {
            if (arrayList.get(i).getNumber().equals(number)) {
                arrayList.remove(i);
                arrayList.add(i, existingFav);
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private void initRecyclerView() {
        mRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        if (Home.arrayList != null) {
            mAdapter = new FavsAdapter(getContext(), Home.arrayList);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setPadding(0, 0, 0, getResources().getDimensionPixelOffset(R.dimen.bottom_navigation_height));
        }
    }

    public static class Task extends AsyncTask<Context, Void, ArrayList<Type_Fav>> {

        Context mContext;

        @Override
        protected ArrayList<Type_Fav> doInBackground(Context... ctx) {
            String DB_FAV = "fav.db";
            mContext = ctx[0];
            Database database = new Database(mContext, DB_FAV);
            try {
                SQLiteDatabase db = database.getReadableDatabase();
                DatabaseManager databaseManager = new DatabaseManager(mContext, db, DB_FAV);
                ArrayList<Type_Fav> arrayList = databaseManager.getFav(true, null);
                database.close();
                databaseManager.close();
                return arrayList;
            } catch (SQLiteException e) {
                Log.e("SQLiteError", e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(final ArrayList<Type_Fav> values) {
            super.onPostExecute(values);
            if (values != null) {
                if (!values.isEmpty()) {
                    viewFlipper.setDisplayedChild(1);
                    arrayList.clear();
                    arrayList.add(0, new Type_Fav(null, null, 0, "", 0));
                    arrayList.addAll(values);
                    mAdapter.notifyDataSetChanged();
                } else {
                    viewFlipper.setDisplayedChild(0);
                    emptyText.setText(mContext.getString(R.string.emptyFavs));
                }
            }
        }
    }
}