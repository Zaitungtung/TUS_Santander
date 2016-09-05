package com.alce.tus.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.alce.tus.Adapters.PagerAdapter;
import com.alce.tus.R;
import com.alce.tus.Types.Type_Near;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.HashMap;
import java.util.List;

/**
 * MainActivity that's control the 3 fragments Home, Lines and Nearby.
 */

public class MainActivity extends AppCompatActivity {

    public static AHBottomNavigation bottomNavigation;
    private Context mContext;
    private AHBottomNavigationViewPager viewPager;
    private MaterialSearchView searchView;
    private View mTintView;

    private PagerAdapter adapter;
    private int previousStatusColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeSearchView();
        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        viewPager = (AHBottomNavigationViewPager) findViewById(R.id.view_pager);
        mTintView = findViewById(R.id.tint_view);
        mTintView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchView != null) {
                    if (searchView.isSearchOpen())
                        searchView.closeSearch();
                }
            }
        });

        tintTheme(bottomNavigation.getCurrentItem());
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position, boolean wasSelected) {
                if (searchView != null && searchView.isSearchOpen())
                    searchView.closeSearch();
                viewPager.setCurrentItem(position, false);
                tintTheme(position);
            }
        });

        viewPager.setOffscreenPageLimit(4);
        adapter = new PagerAdapter(getSupportFragmentManager(), getApplicationContext());
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        tintTheme(bottomNavigation.getCurrentItem());
        if (SettingsActivity.rebuild) {
            initializeSearchView();
            SettingsActivity.rebuild = false;
        }
        if (searchView != null && mTintView != null) {
            if (searchView.isSearchOpen()) {
                searchView.closeSearch();
                mTintView.setVisibility(View.GONE);
            }
        }

        int color1, color2, color3, color4;
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        if (prefs.getBoolean("color", true)) {
            bottomNavigation.setAccentColor(Color.WHITE);
            bottomNavigation.setInactiveColor(Color.GRAY);
            color1 = R.color.color_tab_1;
            color2 = R.color.color_tab_2;
            color3 = R.color.color_tab_3;
            color4 = R.color.color_tab_4;
        } else {
            bottomNavigation.setAccentColor(R.color.gray_night);
            bottomNavigation.setInactiveColor(R.color.gray_batman);
            color1 = color2 = color3 = color4 = R.color.gray_panther;
        }

        bottomNavigation.removeAllItems();

        boolean busShow = prefs.getBoolean("busShow", true);
        boolean bikeShow = prefs.getBoolean("bikeShow", true);

        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab1, R.drawable.ic_home, color1);
        AHBottomNavigationItem item2 = null;
        AHBottomNavigationItem item3 = null;
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.tab4, R.drawable.ic_nearby, color4);

        if (busShow && bikeShow) {
            item2 = new AHBottomNavigationItem(R.string.bus, R.drawable.ic_bus, color2);
            item3 = new AHBottomNavigationItem(R.string.tab3, R.drawable.ic_bike, color3);
        }
        if (busShow && !bikeShow) {
            item2 = new AHBottomNavigationItem(R.string.tab2, R.drawable.ic_lines, color2);
            item3 = null;
        }
        if (!busShow && bikeShow) {
            item2 = null;
            item3 = new AHBottomNavigationItem(R.string.tab3, R.drawable.ic_bike, color3);
        }

        bottomNavigation.addItem(item1);
        if (item2 != null)
            bottomNavigation.addItem(item2);
        if (item3 != null)
            bottomNavigation.addItem(item3);
        bottomNavigation.addItem(item4);

        bottomNavigation.setColored(true);
        bottomNavigation.setForceTint(true);
        bottomNavigation.setForceTitlesDisplay(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentItem", bottomNavigation.getCurrentItem());
        outState.putInt("statusColor", previousStatusColor);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null && bottomNavigation != null) {
            bottomNavigation.setCurrentItem(savedInstanceState.getInt("currentItem"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                previousStatusColor = savedInstanceState.getInt("statusColor");
                window.setStatusBarColor(previousStatusColor);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(R.anim.rtc, R.anim.ctl);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    private void tintTheme(int position) {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        boolean busShow = prefs.getBoolean("busShow", true);
        boolean bikeShow = prefs.getBoolean("bikeShow", true);
        if (busShow && !bikeShow) {
            if (position == 2)
                position = 3;
        }
        if ((!busShow && bikeShow)) {
            if (position == 2)
                position = 3;
            if (position == 1)
                position = 2;
        }

        if (prefs.getBoolean("color", true)) {
            String resource = "color_tab_" + (position + 1);
            int id = getResources().getIdentifier(resource, "color", "com.alce.tus");
            resource = resource + "_Dark";
            int id_Dark = getResources().getIdentifier(resource, "color", "com.alce.tus");

            assert getSupportActionBar() != null;
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(ContextCompat.getColor(mContext, id)));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getSupportActionBar() != null) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(ContextCompat.getColor(mContext, id_Dark));
            }
        } else {
            assert getSupportActionBar() != null;
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(ContextCompat.getColor(mContext,
                            getResources().getIdentifier("gray_panther", "color", "com.alce.tus"))));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getSupportActionBar() != null) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.gray_night));
            }
        }
    }

    private void initializeSearchView() {
        Log.d("DEBUG", "Inicializa la searchView");
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        assert searchView != null;
        searchView.setEllipsize();
        searchView.setHint(getString(R.string.search_hint));
        searchView.setHintTextColor(getResources().getColor(R.color.gray_panther));
        final HashMap<String, List<String>> hashMap = Type_Near.getSearchable();
        if (hashMap != null) {
            searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit() {
                    return false;
                }

                @Override
                public void onQueryTextChange() {
                    List<String> name = hashMap.get("NParada");
                    List<String> number = hashMap.get("NumParada");
                    List<String> type = hashMap.get("Type");
                    searchView.setSuggestions(name, number, type);
                }
            });

            searchView.setOnItemClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("DEBUG", "Eh me has abierto¡");
                    TextView textView = (TextView) view.findViewById(R.id.suggestion_text);
                    String text = textView.getText().toString();
                    List<String> list = hashMap.get("NParada");
                    int pos = list.indexOf(text);
                    List<String> ID = hashMap.get("NumParada");
                    List<String> Type = hashMap.get("Type");
                    List<String> Lat = hashMap.get("Lat");
                    List<String> Lng = hashMap.get("Lng");
                    searchView.closeSearch();
                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("type", Type.get(pos));
                    intent.putExtra("customColor", Type.get(pos));
                    if (Type.get(pos).equals("bike")) {
                        intent.putExtra("lat", Lat.get(pos));
                        intent.putExtra("lng", Lng.get(pos));
                    }
                    intent.putExtra("numParada", ID.get(pos));
                    intent.putExtra("nParada", text);
                    startActivity(intent);
                    overridePendingTransition(R.anim.rtc, R.anim.ctl);
                }
            });


            searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
                @Override
                public void onSearchViewShown() {
                }

                @Override
                public void onSearchViewClosed() {
                    Log.d("DEBUG", "Eh me has cerrado¡");
                    mTintView.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        Log.d("DEBUG Closed", Integer.toString(previousStatusColor));
                        window.setStatusBarColor(previousStatusColor);
                    }
                }

                @Override
                public void onSearchViewAnimationEnded() {
                    Log.d("DEBUG", "He terminado de abrirme¡¡¡");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        previousStatusColor = window.getStatusBarColor();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(getResources().getColor(R.color.gray_batman));
                    }
                    mTintView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onTintViewClick() {
                }

                @Override
                public void onEmptyButtonClick() {

                }
            });
        }
    }
}
