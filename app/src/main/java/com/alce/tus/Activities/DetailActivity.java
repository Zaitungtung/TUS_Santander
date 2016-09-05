package com.alce.tus.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.alce.tus.Adapters.Detail_Adapter;
import com.alce.tus.Data.Database;
import com.alce.tus.Data.DatabaseManager;
import com.alce.tus.Data.Soap;
import com.alce.tus.R;
import com.alce.tus.Types.FavDialog;
import com.alce.tus.Types.Type_Fav;
import com.alce.tus.Types.Type_Info;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jaouan.revealator.Revealator;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String DB_FAV = "fav.db";
    public static boolean isFav = false;
    public static View View;
    private static FloatingActionButton fab;
    private static String numParada, nParada, type;
    private static double Lat, Lng;
    private static DatabaseReference mDatabase;
    private static Query data;
    private static ValueEventListener valueEventListener;
    private static SwipeRefreshLayout swipeRefreshLayout;
    private static View mTintView;
    private static TextView title;
    private static CollapsingToolbarLayout collapsingToolbarLayout;
    private Parcelable mListState;
    private LinearLayoutManager mLayoutManager;
    private ViewFlipper viewFlipper;
    private boolean isShowTitle = false, isMapShow = false;
    private RelativeLayout revealSignIn;
    private Type_Fav fav;
    private boolean isDialogOpen;
    private int dialogColor;

    public static void changeFab(Boolean isFav, Context mContext) {
        if (isFav)
            fab.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_star_full));
        else
            fab.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_star));
    }

    public static void saveFav(Context mContext, final Type_Fav fav) {
        Database database = new Database(mContext, DB_FAV);
        SQLiteDatabase db = database.getWritableDatabase();
        DatabaseManager databaseManager = new DatabaseManager(mContext, db, DB_FAV);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {

            String userId = user.getUid();
            mDatabase.child("users").child(userId).child(fav.getNumber()).setValue(fav);

            data = mDatabase.child("users").child(userId).orderByChild("pos");
            valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        String userId = user.getUid();
                        mDatabase.child("users").child(userId).child(fav.getNumber()).setValue(fav);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            data.addValueEventListener(valueEventListener);
        } else {
            int pos = databaseManager.countFav() + 1;
            Type_Fav fav1 = new Type_Fav(fav.getNumber(), fav.getName(), pos, fav.getCustomName(),
                    fav.getCustomColor());
            databaseManager.insertFav(fav1);
            changeUI(mContext, fav1, "add");
        }
    }

    public static void editFav(Context mContext, Type_Fav type_fav) {
        Database database = new Database(mContext, DB_FAV);
        SQLiteDatabase db = database.getWritableDatabase();
        DatabaseManager databaseManager = new DatabaseManager(mContext, db, DB_FAV);
        databaseManager.updateFav(type_fav);
    }

    public static void deleteFav(Context mContext, Type_Fav fav1) {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        String numParada = fav1.getNumber();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            if (data != null && valueEventListener != null) {
                data.removeEventListener(valueEventListener);
            }
            mDatabase.child("users").child(userId).child(numParada).removeValue();
        } else {
            Database database = new Database(mContext, DB_FAV);
            SQLiteDatabase db = database.getWritableDatabase();
            DatabaseManager databaseManager = new DatabaseManager(mContext, db, DB_FAV);
            databaseManager.deleteFav(numParada);
            changeUI(mContext, fav1, "delete");
        }
    }

    private static void changeUI(Context mContext, Type_Fav existingFav, String type) {
        int color;
        String name;

        if (type.equals("delete")) {
            if (existingFav.getType() != null) {
                color = mContext
                        .getResources().getColor(R.color.color_tab_3_Dark);
            } else {
                color = mContext
                        .getResources().getColor(R.color.color_tab_2_Dark);
            }
            name = existingFav.getName();
        } else {
            if (existingFav.getCustomColor() == 0) {
                color = FavDialog.Colors[15];
            } else {
                color = existingFav.getCustomColor();
            }
            if (existingFav.getCustomName().equals("")) {
                name = existingFav.getName();
            } else {
                name = existingFav.getCustomName();
            }
        }

        if (color == FavDialog.Colors[14] ||
                color == FavDialog.Colors[13] ||
                color == FavDialog.Colors[15]) {
            fab.setBackgroundTintList(ColorStateList.valueOf(mContext
                    .getResources().getColor(R.color.pink_gum)));
        } else {
            fab.setBackgroundTintList(ColorStateList.valueOf(mContext
                    .getResources().getColor(R.color.color_tab_4)));
        }

        title.setText(name);

        SharedPreferences prefs = mContext
                .getSharedPreferences("preferences", Context.MODE_PRIVATE);
        if (prefs.getBoolean("color", true)) {
            mTintView.setBackgroundColor(color);
            collapsingToolbarLayout.setStatusBarScrimColor(color);
            swipeRefreshLayout.setColorSchemeColors(color);
        } else {
            color = mContext.getResources().getColor(R.color.gray_night);
            mTintView.setBackgroundColor(color);
            collapsingToolbarLayout.setStatusBarScrimColor(color);
            swipeRefreshLayout.setColorSchemeColors(color);
            fab.setBackgroundTintList(ColorStateList.valueOf(mContext.
                    getResources().getColor(R.color.gray_future)));
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int colorRef, color;
        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences("preferences", Context.MODE_PRIVATE);
        if (prefs.getBoolean("color", true)) {
            colorRef = R.color.color_tab_2_Dark;
            color = getResources().getColor(R.color.color_tab_2_Dark);
        } else {
            colorRef = R.color.gray_night;
            color = getResources().getColor(R.color.gray_night);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTintView = findViewById(R.id.tintView);
        title = (TextView) findViewById(R.id.title);
        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        viewFlipper.setDisplayedChild(0);
        revealSignIn = (RelativeLayout) findViewById(R.id.revealSignIn);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }
        numParada = extras.get("numParada").toString();
        nParada = extras.get("nParada").toString();
        type = extras.get("type").toString();
        if (type.equals("bike")) {
            if (colorRef == R.color.color_tab_2_Dark) {
                colorRef = R.color.color_tab_3_Dark;
                color = getResources().getColor(R.color.color_tab_3_Dark);
            }
            Lat = Double.parseDouble(extras.get("lat").toString());
            Lng = Double.parseDouble(extras.get("lng").toString());
        }
        title.setText(nParada);
        title.setPadding(0, getStatusBarHeight(), 0, 0);

        mTintView.setBackgroundColor(getResources().getColor(colorRef));

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        collapsingToolbarLayout.setTitle(" ");
        collapsingToolbarLayout.setStatusBarScrimColor(color);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                if (scrollRange + verticalOffset == 0) {
                    // On Top
                    collapsingToolbarLayout.setTitle(nParada);
                    isShowTitle = true;
                    changeUIVisibility();
                } else {
                    if (isShowTitle) {
                        collapsingToolbarLayout.setTitle(" ");
                        isShowTitle = false;
                    }
                }
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setNestedScrollingEnabled(true);
        swipeRefreshLayout.setColorSchemeColors(color);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Task task = new Task();
                task.execute(numParada);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        swipeRefreshLayout.setRefreshing(true);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View = view;
                String msg = actionFab(getApplicationContext(), getString(R.string.delete));
                snackButton(R.string.undo, getApplicationContext(), msg, getString(R.string.delete)
                        , view);
            }
        });

        if (prefs.getBoolean("color", true)) {
            fab.setBackgroundTintList(ColorStateList.valueOf(getApplicationContext().
                    getResources().getColor(R.color.color_tab_4_Dark)));
        } else {
            fab.setBackgroundTintList(ColorStateList.valueOf(getApplicationContext().
                    getResources().getColor(R.color.gray_future)));
        }

        String DB_INFO = "info.db";
        Database database = new Database(getApplicationContext(), DB_INFO);
        SQLiteDatabase db = database.getReadableDatabase();
        DatabaseManager databaseManager = new DatabaseManager(getApplicationContext(), db, DB_INFO);
        if (!type.equals("bike")) {
            Location location = databaseManager.getLatLng(numParada);
            Lat = location.getLatitude();
            Lng = location.getLongitude();
            databaseManager.close();
        }

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.getKey().equals(numParada)) {
                        changeFab(true, getApplicationContext());
                        isFav = true;
                        Type_Fav existingFav = dataSnapshot.getValue(Type_Fav.class);
                        changeUI(getApplicationContext(), existingFav, "add");
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Type_Fav existingFav = dataSnapshot.getValue(Type_Fav.class);
                    changeUI(getApplicationContext(), existingFav, "edit");
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getKey().equals(numParada)) {
                        isFav = false;
                        changeFab(false, getApplicationContext());
                        Type_Fav existingFav = dataSnapshot.getValue(Type_Fav.class);
                        changeUI(getApplicationContext(), existingFav, "delete");
                    } else {
                        isFav = true;
                        changeFab(true, getApplicationContext());
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mDatabase.addChildEventListener(childEventListener);
            isFav = false;
        } else {
            database = new Database(getApplicationContext(), DB_FAV);
            db = database.getReadableDatabase();
            databaseManager = new DatabaseManager(getApplicationContext(), db, DB_FAV);
            ArrayList<Type_Fav> arrayList = databaseManager.getFav(false, numParada);
            isFav = !databaseManager.getFav(false, numParada).isEmpty();

            if (isFav) {
                Type_Fav existingFav = new Type_Fav(
                        arrayList.get(0).getNumber(),
                        arrayList.get(0).getName(),
                        0,
                        arrayList.get(0).getCustomName(),
                        arrayList.get(0).getCustomColor()

                );
                changeUI(getApplicationContext(), existingFav, "add");
            }
        }

        SupportMapFragment mMap = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.GoogleMap));
        if (checkPlayServices()) {
            mMap.getMapAsync(this);
            mTintView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isMapShow) {
                        hideRevealEffect();
                        isMapShow = false;
                    } else {
                        revealEffect();
                        isMapShow = true;
                    }
                }
            });
        } else {
            mMap.getView().setVisibility(android.view.View.INVISIBLE);
        }

        mTintView.postDelayed(new Runnable() {
            @Override
            public void run() {
                initRevealFab();
            }
        }, 250);

        changeFab(isFav, getApplicationContext());

        Task task = new Task();
        task.execute(numParada);
    }

    public void snackButton(int text, final Context mContext, String msg,
                            final String delete, View view) {

        if (!msg.equals("")) {
            Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                    .setAction(text, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            actionFab(mContext, delete);
                            changeFab(isFav, mContext);
                        }
                    }).show();
            changeFab(isFav, mContext);
        }
    }

    private void initRevealFab() {
        final View showMe = findViewById(R.id.showMe);
        showMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Revealator.unreveal(revealSignIn)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                fab.show();
                                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                                overridePendingTransition(R.anim.rtc, R.anim.ctl);
                            }
                        })
                        .start();

            }
        });
        final View skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Revealator.unreveal(revealSignIn)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                fab.show();
                            }
                        })
                        .start();

                SharedPreferences prefs =
                        getApplicationContext().getSharedPreferences("preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("skip_signIn", true);
                editor.apply();

            }
        });

        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences("preferences", Context.MODE_PRIVATE);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();

        if (!prefs.getBoolean("skip_signIn", false)) {
            if (user == null) {
                Revealator.reveal(revealSignIn)
                        .from(fab)
                        .withChildsAnimation()
                        .start();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_streetView:
                try {
                    Uri gmmIntentUri = Uri.parse("google.streetview:cbll=" + Double.toString(Lat)
                            + "," + Double.toString(Lng));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(), getString(R.string.noStreetView_client), Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(
                R.anim.ltr,
                R.anim.ctr
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (data != null && valueEventListener != null)
            data.removeEventListener(valueEventListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mListState != null && mLayoutManager != null) {
            mLayoutManager.onRestoreInstanceState(mListState);
        }

        if (isDialogOpen && fav != null) {
            mTintView.setBackgroundColor(dialogColor);
            FavDialog.favDialog_init(getApplicationContext(), this.getSupportFragmentManager(),
                    "add", fav, mTintView, title);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if (mLayoutManager != null) {
            mListState = mLayoutManager.onSaveInstanceState();
            state.putParcelable("position", mListState);
        }

        if (fav != null) {
            Log.d("DEBUG", "No es nulo");
            state.putString("name", fav.getName());
            state.putString("number", fav.getNumber());
            state.putInt("pos", fav.getPos());
            state.putString("customName", fav.getCustomName());
            state.putInt("customColor", fav.getCustomColor());
            if (fav.getType() != null) {
                state.putString("type", fav.getType());
                state.putDouble("lat", fav.getLat());
                state.putDouble("lng", fav.getLng());
            }
        }

        state.putBoolean("isDialogOpen", FavDialog.isOpen());
        state.putInt("dialogColor", FavDialog.Color);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable("position");

            String name = savedInstanceState.getString("name");
            String number = savedInstanceState.getString("number");
            int pos = savedInstanceState.getInt("pos");
            String customName = savedInstanceState.getString("customName");
            int customColor = savedInstanceState.getInt("customColor");
            if (savedInstanceState.getString("type") != null) {
                String type = savedInstanceState.getString("type");
                Double lat = savedInstanceState.getDouble("lat");
                Double lng = savedInstanceState.getDouble("lng");
                fav = new Type_Fav(number, name, pos, customName, customColor, type, lat, lng);
            } else
                fav = new Type_Fav(number, name, pos, customName, customColor);

            isDialogOpen = savedInstanceState.getBoolean("isDialogOpen");
            dialogColor = savedInstanceState.getInt("dialogColor");
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(resultCode)) {
                GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode, 0).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private String actionFab(Context mContext, String delete) {
        String msg = "";

        if (type.equals("bike"))
            fav = new Type_Fav(numParada, nParada, 0, "", 0, type, Lat, Lng);
        else
            fav = new Type_Fav(numParada, nParada, 0, "", 0);

        if (isFav) {
            deleteFav(mContext, fav);
            msg = delete;
            isFav = false;
        } else {
            FavDialog.favDialog_init(mContext, this.getSupportFragmentManager(),
                    "add", fav, mTintView, title);
        }

        return msg;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void revealEffect() {
        final View myView = mTintView;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int cx = myView.getWidth() / 2;
            int cy = myView.getHeight() / 2;

            float initialRadius = (float) Math.hypot(cx, cy);

            Animator anim =
                    ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    title.setVisibility(android.view.View.GONE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    myView.setAlpha(0);
                }
            });

            anim.start();
        } else {
            myView.setAlpha(0);
            title.setVisibility(android.view.View.GONE);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void hideRevealEffect() {
        final View myView = mTintView;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int cx = myView.getWidth() / 2;
            int cy = myView.getHeight() / 2;

            float finalRadius = (float) Math.hypot(cx, cy);

            Animator anim =
                    ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
            myView.setAlpha(0.6f);
            title.setVisibility(android.view.View.VISIBLE);
            anim.start();
        } else {
            myView.setAlpha(0.6f);
            title.setVisibility(android.view.View.VISIBLE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng latLng = new LatLng(Lat, Lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        googleMap.moveCamera(cameraUpdate);

        MarkerOptions mark = new MarkerOptions()
                .position(new LatLng(Lat, Lng));
        googleMap.addMarker(mark);
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(false);

        googleMap.setPadding(0, 500, 0, 1000);
    }

    private void changeUIVisibility() {
        title.setVisibility(android.view.View.VISIBLE);
        mTintView.setAlpha(0.6f);
        isMapShow = !isMapShow;
    }

    private class Task extends AsyncTask<String, Void, ArrayList<Type_Info>> {

        @Override
        protected ArrayList<Type_Info> doInBackground(String... strings) {
            Soap data = new Soap();
            if (type.equals("bike")) {
                return data.getInfoBike(strings[0]);
            } else {
                return data.getInfoBus(strings[0]);
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Type_Info> values) {
            super.onPostExecute(values);
            if (values != null && !values.isEmpty()) {
                viewFlipper.setDisplayedChild(2);
                RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
                mRecyclerView.setNestedScrollingEnabled(true);
                mLayoutManager = new LinearLayoutManager(getApplicationContext());
                mRecyclerView.setLayoutManager(mLayoutManager);

                Detail_Adapter adapter;
                if (type.equals("bike")) {
                    adapter = new Detail_Adapter(getApplicationContext(), "bike", values);
                } else {
                    adapter = new Detail_Adapter(getApplicationContext(), "bus", values);
                }
                mRecyclerView.setAdapter(adapter);

                swipeRefreshLayout.setRefreshing(false);
            } else {
                viewFlipper.setDisplayedChild(1);
                TextView emptyText = (TextView) findViewById(R.id.emptyText);
                emptyText.setText(getString(R.string.emptyTime));
                emptyText.setPadding(0, 0, 0, getResources().getDimensionPixelOffset(R.dimen.bottom_navigation_height));
            }
        }
    }
}