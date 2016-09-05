package com.alce.tus.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.alce.tus.Data.Database;
import com.alce.tus.Data.DatabaseManager;
import com.alce.tus.R;
import com.alce.tus.Types.Type_Sublines;

import java.util.ArrayList;
import java.util.List;

/**
 * Show an Activity with the Sublines in tabs.
 */
public class SublinesActivity extends AppCompatActivity {

    private ArrayList<Type_Sublines> sublines;
    private int[] pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences("preferences", Context.MODE_PRIVATE);
        if (prefs.getBoolean("color", true))
            setTheme(R.style.AppTheme_SublinesTheme);
        else
            setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sublines);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }

        String name = extras.getString("name");
        String line = extras.getString("line");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(name);
        }

        if (line != null) {
            String DB_DATA = "info.db";
            Database database = new Database(getApplicationContext(), DB_DATA);
            SQLiteDatabase db = database.getReadableDatabase();
            DatabaseManager databaseManager = new DatabaseManager(getApplicationContext(), db, DB_DATA);
            databaseManager.extractSublines(line);

            pos = Type_Sublines.getSublineasPos();
            sublines = Type_Sublines.getSublines();

            ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
            setupViewPager(viewPager);

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
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

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        int initPos;

        for (int i = 0; i < pos.length; i++) {
            if (i == 0)
                initPos = 0;
            else
                initPos = pos[i - 1];

            String firstElement = sublines.get(pos[i]).getnSubline();
            com.alce.tus.Fragments.Sublines fragment = new com.alce.tus.Fragments.Sublines();
            ArrayList<Type_Sublines> tab = new ArrayList<>(sublines.subList(initPos, pos[i]));
            fragment.data(tab);

            adapter.addFragment(fragment, firstElement);
        }

        viewPager.setAdapter(adapter);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<com.alce.tus.Fragments.Sublines> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        private ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public com.alce.tus.Fragments.Sublines getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        private void addFragment(com.alce.tus.Fragments.Sublines fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
