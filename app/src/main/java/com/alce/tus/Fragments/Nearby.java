package com.alce.tus.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.alce.tus.Activities.DetailActivity;
import com.alce.tus.Activities.MainActivity;
import com.alce.tus.Adapters.Nearby_Adapter;
import com.alce.tus.Data.Database;
import com.alce.tus.Data.DatabaseManager;
import com.alce.tus.R;
import com.alce.tus.Types.Type_Near;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

@SuppressWarnings("FieldCanBeLocal")
public class Nearby extends Fragment implements OnMapReadyCallback {

    private static final int MAX_DISTANCE = 250; // metros
    private static final int MIN_DISTANCE = 50; // metros
    private static int distance = 125;
    private static int THRESHOLD = distance; // metros

    private static MenuItem menu_distance;
    private static Circle circle = null;
    private static Location mLocation;
    private static GoogleMap gMap;
    private static Context mContext;
    private Boolean mShowingBack = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nearby, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState == null) {
            getActivity().getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new CardFrontFragment())
                    .commit();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        SupportMapFragment mMap = ((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.GoogleMap));
        mMap.getMapAsync(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new CardFrontFragment())
                .commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_nearby, menu);
        menu_distance = menu.findItem(R.id.action_distance);
        if (mLocation != null)
            menu_distance.setVisible(true);
        else
            menu_distance.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_distance) {
            flipCard();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        if (mLocation == null) {
            LatLng latLng = new LatLng(43.4584300, -3.8071200);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f);
            googleMap.moveCamera(cameraUpdate);
        } else {
            LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f);
            googleMap.moveCamera(cameraUpdate);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    || getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                gMap.setMyLocationEnabled(true);
            }
        }
    }

    private void flipCard() {

        Log.d("DEBUG", "Mostrando Back " + Boolean.toString(mShowingBack));

        if (mShowingBack) {
            getActivity().getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.animator.card_flip_right_in,
                            R.animator.card_flip_right_out,
                            R.animator.card_flip_left_in,
                            R.animator.card_flip_left_out)
                    .replace(R.id.container, new CardFrontFragment())
                    .addToBackStack(null)
                    .commit();
        } else {
            getActivity().getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.animator.card_flip_right_in,
                            R.animator.card_flip_right_out,
                            R.animator.card_flip_left_in,
                            R.animator.card_flip_left_out)
                    .replace(R.id.container, new CardBackFragment())
                    .addToBackStack(null)
                    .commit();
        }
        mShowingBack = !mShowingBack;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mShowingBack", mShowingBack);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null)
            mShowingBack = savedInstanceState.getBoolean("mShowingBack");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    /**
     * A fragment representing the front of the card.
     */
    public static class CardFrontFragment extends android.app.Fragment implements LocationListener,
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {

        private static final int REQUEST_FINE_LOCATION = 0;
        private static final int UPDATE_INTERVAL = 120000; // 2 min
        private static final int FASTEST_INTERVAL = 60000; // 1 min
        private static final int DISPLACEMENT = 20; // 20 meter
        private static final String DB_DATA = "info.db";
        private GoogleApiClient mGoogleApiClient;
        private LocationRequest mLocationRequest;
        private RecyclerView mRecyclerView;
        private TextView emptyText, text;
        private ViewFlipper viewFlipper;
        private Button button;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.nearby_front_card, container, false);
            mRecyclerView = (RecyclerView) view.findViewById(R.id.nearbyList);
            viewFlipper = (ViewFlipper) view.findViewById(R.id.view_flipper);
            emptyText = (TextView) view.findViewById(R.id.emptyText);
            text = (TextView) view.findViewById(R.id.text);
            button = (Button) view.findViewById(R.id.button);
            return view;
        }

        @Override
        public void onStart() {
            super.onStart();

            Log.d("DEBUG", "Front Card onStart");

            if (circle != null)
                circle.remove();
            if (viewFlipper.getDisplayedChild() == 0) {
                emptyText.setText(getString(R.string.emptyNearby));
                viewFlipper.setPadding(0, 0, 0, getResources().getDimensionPixelOffset(R.dimen.bottom_navigation_height));
            }

            if (mLocationRequest == null)
                createLocationRequest();
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
                mGoogleApiClient.connect();

            } else
                mGoogleApiClient.connect();

            if (mLocation != null && mContext != null)
                refreshInfo(mContext);

        }

        @Override
        public void onStop() {
            super.onStop();
            if (mGoogleApiClient != null) {
                if (mGoogleApiClient.isConnected())
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }
        }

        @SuppressWarnings("MissingPermission")
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            switch (requestCode) {
                case REQUEST_FINE_LOCATION: {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        viewFlipper.setDisplayedChild(0);
                        viewFlipper.setPadding(0, 0, 0,
                                getActivity().getApplicationContext().getResources().getDimensionPixelOffset(R.dimen.bottom_navigation_height));
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                        gMap.setMyLocationEnabled(true);
                        if (mLocation != null)
                            refreshInfo(mContext);
                        else
                            menu_distance.setVisible(false);
                    }
                }
            }
        }

        private void refreshInfo(final Context mContext) {
            if (menu_distance != null)
                menu_distance.setVisible(true);
            gMap.clear();
            viewFlipper.setDisplayedChild(2);
            viewFlipper.setPadding(0, 0, 0, 0);
            if (circle != null)
                circle.remove();
            final Activity activity = getActivity();
            if (mContext != null) {
                Database database = new Database(mContext, DB_DATA);
                SQLiteDatabase db = database.getReadableDatabase();
                DatabaseManager databaseManager = new DatabaseManager(mContext, db, DB_DATA);

                ArrayList<Type_Near> arrayList = databaseManager.getNearStops(mLocation.getLatitude(),
                        mLocation.getLongitude());
                final ArrayList<Type_Near> nearbyStops = new ArrayList<>();
                final ArrayList<String> indexStops = new ArrayList<>();

                if (arrayList.size() > 0) {
                    mRecyclerView.setNestedScrollingEnabled(false);
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
                    mRecyclerView.setLayoutManager(mLayoutManager);

                    for (int i = 0; i != arrayList.size(); i++) {
                        if (arrayList.get(i).getDist() <= THRESHOLD) {
                            MarkerOptions mark;
                            if (arrayList.get(i).getType().equals("bike")) {
                                mark = new MarkerOptions()
                                        .position(new LatLng(arrayList.get(i).getLat(), arrayList.get(i).getLng()))
                                        .title(arrayList.get(i).getNParada())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bike_marker));
                            } else {
                                mark = new MarkerOptions()
                                        .position(new LatLng(arrayList.get(i).getLat(), arrayList.get(i).getLng()))
                                        .title(arrayList.get(i).getNParada())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_marker));
                            }
                            gMap.addMarker(mark);
                            nearbyStops.add(arrayList.get(i));
                            indexStops.add(arrayList.get(i).getNParada());
                        }
                    }

                    if (indexStops.size() > 1) {
                        final Nearby_Adapter nearby_adapter = new Nearby_Adapter(activity, mContext, nearbyStops);
                        mRecyclerView.setAdapter(nearby_adapter);
                        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);
                                if (dy > 0)
                                    MainActivity.bottomNavigation.hideBottomNavigation();

                                if (dy < 0)
                                    MainActivity.bottomNavigation.restoreBottomNavigation();
                            }
                        });

                        LatLng userLoc = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc, gMap.getCameraPosition().zoom));
                        gMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                            @Override
                            public boolean onMyLocationButtonClick() {
                                LatLng userLoc = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 16.0f));
                                return true;
                            }
                        });
                        gMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                int pos = indexStops.indexOf(marker.getTitle());
                                Intent intent = new Intent(mContext, DetailActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("type", nearbyStops.get(pos).getType());
                                if (nearbyStops.get(pos).getType().equals("bike")) {
                                    intent.putExtra("lat", nearbyStops.get(pos).getLat());
                                    intent.putExtra("lng", nearbyStops.get(pos).getLng());
                                }
                                intent.putExtra("numParada", nearbyStops.get(pos).getNumParada());
                                intent.putExtra("nParada", nearbyStops.get(pos).getNParada());
                                if (activity != null) {
                                    activity.startActivity(intent);
                                    activity.overridePendingTransition(R.anim.rtc, R.anim.ctl);
                                } else {
                                    if (isAdded())
                                        Toast.makeText(mContext, getString(R.string.error), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        viewFlipper.setDisplayedChild(0);
                        viewFlipper.setPadding(0, 0, 0, mContext.getResources().getDimensionPixelOffset(R.dimen.bottom_navigation_height));
                        emptyText.setText(mContext.getString(R.string.emptyDistance));
                        MainActivity.bottomNavigation.restoreBottomNavigation();
                    }
                } else {
                    viewFlipper.setDisplayedChild(0);
                    viewFlipper.setPadding(0, 0, 0, mContext.getResources().getDimensionPixelOffset(R.dimen.bottom_navigation_height));
                    emptyText.setText(mContext.getString(R.string.emptyNearby));
                    MainActivity.bottomNavigation.restoreBottomNavigation();
                }
            }
        }

        private void createLocationRequest() {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        }

        @Override
        public void onLocationChanged(Location location) {
            mLocation = location;

            if (gMap != null && mLocation != null) {
                LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f);
                gMap.moveCamera(cameraUpdate);
            }

            if (menu_distance != null)
                if (mLocation != null)
                    menu_distance.setVisible(true);
                else
                    menu_distance.setVisible(false);
            if (mContext != null)
                refreshInfo(mContext);
        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            String perm = Manifest.permission.ACCESS_FINE_LOCATION;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (mContext.checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                    viewFlipper.setDisplayedChild(1);
                    viewFlipper.setPadding(0, 0, 0, mContext.getResources().getDimensionPixelOffset(R.dimen.bottom_navigation_height));
                    text.setText(R.string.nearbyPermission);
                    button.setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("NewApi")
                        @Override
                        public void onClick(View v) {
                            getActivity().requestPermissions(
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_FINE_LOCATION);
                        }
                    });
                } else {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                }
            } else {
                viewFlipper.setDisplayedChild(0);
                if (mContext != null)
                    viewFlipper.setPadding(0, 0, 0,
                            mContext.getResources().getDimensionPixelOffset(R.dimen.bottom_navigation_height));
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                gMap.setMyLocationEnabled(true);
                if (mLocation != null)
                    refreshInfo(mContext);
                else if (menu_distance != null)
                    menu_distance.setVisible(false);
            }
        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }
    }

    /**
     * A fragment representing the back of the card.
     */
    public static class CardBackFragment extends android.app.Fragment {

        private TextView textView;
        private SeekBar seekBar;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            SharedPreferences prefs = getActivity().getApplicationContext()
                    .getSharedPreferences("preferences", Context.MODE_PRIVATE);
            int theme;
            if (prefs.getBoolean("color", true))
                theme = R.style.AppTheme_Nearby;
            else
                theme = R.style.AppTheme;
            final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), theme);
            LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
            View view = localInflater.inflate(R.layout.nearby_back_card, container, false);
            textView = (TextView) view.findViewById(R.id.distance);
            seekBar = (SeekBar) view.findViewById(R.id.seekBar);
            return view;

        }

        @Override
        public void onStart() {
            super.onStart();

            Log.d("DEBUG", "Back Card onStart");

            MainActivity.bottomNavigation.restoreBottomNavigation();
            String text = Integer.toString(THRESHOLD) + " " + getString(R.string.m);
            textView.setText(text);
            circle = gMap.addCircle(new CircleOptions()
                    .center(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()))
                    .radius(distance)
                    .strokeWidth(2)
                    .strokeColor(R.color.gray_panther));
            seekBar.setEnabled(true);
            seekBar.setMax(MAX_DISTANCE - MIN_DISTANCE);
            seekBar.setProgress(distance - MIN_DISTANCE);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    distance = i + MIN_DISTANCE;
                    String text = Integer.toString(distance) + " " + getString(R.string.m);
                    textView.setText(text);
                    if (gMap != null && mLocation != null) {
                        if (circle != null)
                            circle.remove();
                        circle = gMap.addCircle(new CircleOptions()
                                .center(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()))
                                .radius(distance)
                                .strokeWidth(2)
                                .strokeColor(R.color.gray_panther));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    if (circle != null)
                        circle.remove();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    THRESHOLD = distance;
                }
            });

        }
    }
}