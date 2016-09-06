package com.alce.tus.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alce.tus.Activities.DetailActivity;
import com.alce.tus.Data.Database;
import com.alce.tus.Data.DatabaseManager;
import com.alce.tus.Fragments.Home;
import com.alce.tus.R;
import com.alce.tus.Types.FavDialog;
import com.alce.tus.Types.Type_Fav;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Adapter for the Favs Recycler View.
 */
public class FavsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String DB_FAV = "fav.db";
    private final Context mContext;
    private final ArrayList<Type_Fav> arrayList;
    private Type_Fav fav;
    private TextView customName;
    private View selectedItem;

    private boolean isActionMode;
    public static ActionMode actionMode;
    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            actionMode = mode;
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_actionbar, menu);
            selectedItem.setBackgroundColor(Color.parseColor("#E5E5E5"));
            mode.setTitle(R.string.editFav);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AppCompatActivity activity = (AppCompatActivity) mContext;
                if (activity != null)
                    activity.getWindow().setStatusBarColor(mContext.getResources()
                            .getColor(R.color.gray_night));
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {
                case R.id.edit:
                    AppCompatActivity activity = (AppCompatActivity) mContext;
                    FavDialog.favDialog_init(mContext, activity.getSupportFragmentManager(),
                            "edit", fav, null, customName);
                    mode.finish();
                    return true;
                case R.id.delete:
                    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        Database database = new Database(mContext, DB_FAV);
                        SQLiteDatabase db = database.getWritableDatabase();
                        DatabaseManager databaseManager = new DatabaseManager(mContext, db, DB_FAV);
                        databaseManager.deleteFav(fav.getNumber());
                        Home.Task task = new Home.Task();
                        task.execute(mContext);
                    } else {
                        DetailActivity.deleteFav(mContext, fav);
                    }

                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }


        @Override
        public void onDestroyActionMode(ActionMode mode) {
            isActionMode = false;
            selectedItem.setBackgroundColor(mContext.getResources()
                    .getColor(android.R.color.transparent));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AppCompatActivity activity = (AppCompatActivity) mContext;
                if (activity != null)
                    activity.getWindow().setStatusBarColor(mContext.getResources()
                            .getColor(R.color.color_tab_1_Dark));
            }
        }
    };

    public FavsAdapter(Context c, ArrayList<Type_Fav> arrayList) {
        this.mContext = c;
        this.arrayList = arrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_home_item, parent, false);
            return new FavsAdapter.VHItem(view);
        } else if (viewType == 1) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_lines_header, parent, false);
            return new FavsAdapter.VHHeader(view);
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VHItem) {
            final Type_Fav dataItem = getItem(position);

            TextView name = ((VHItem) holder).name;
            name.setText(dataItem.getName());

            customName = ((VHItem) holder).customName;
            if (dataItem.getCustomName().equals("")) {
                customName.setText(dataItem.getName());
                name.setVisibility(View.GONE);
            } else {
                customName.setText(dataItem.getCustomName());
                name.setVisibility(View.VISIBLE);
            }

            CircleImageView typeView = ((VHItem) holder).typeView;
            if (dataItem.getType() != null)
                Glide.with(mContext).load(R.drawable.ic_mini_other).into(typeView);
            else
                Glide.with(mContext).load(R.drawable.ic_mini_bus).into(typeView);

            SharedPreferences prefs = mContext.getSharedPreferences("preferences", Context.MODE_PRIVATE);
            if (prefs.getBoolean("color", true)) {
                if (dataItem.getCustomColor() == 0) {
                    typeView.setFillColor(FavDialog.Colors[15]);
                } else
                    typeView.setFillColor(dataItem.getCustomColor());
            } else
                typeView.setFillColor(mContext.getResources().getColor(R.color.gray_panther));

            LinearLayout linearLayout = ((FavsAdapter.VHItem) holder).linearLayout;
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isActionMode)
                        actionMode.finish();

                    Activity activity = (Activity) mContext;
                    Intent intent = new Intent(mContext, DetailActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    String DB_INFO = "info.db";
                    Database database = new Database(mContext, DB_INFO);
                    SQLiteDatabase db = database.getReadableDatabase();
                    DatabaseManager databaseManager = new DatabaseManager(mContext, db, DB_INFO);
                    Location location = databaseManager.isBike(dataItem.getName());
                    if (location != null) {
                        intent.putExtra("type", "bike");
                        intent.putExtra("lat", location.getLatitude());
                        intent.putExtra("lng", location.getLongitude());
                        databaseManager.close();
                    } else {
                        intent.putExtra("type", "bus");
                    }
                    intent.putExtra("numParada", dataItem.getNumber());
                    intent.putExtra("nParada", dataItem.getName());

                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.rtc, R.anim.ctl);
                }
            });

            linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    fav = dataItem;
                    selectedItem = view;
                    AppCompatActivity activity = (AppCompatActivity) mContext;
                    activity.startSupportActionMode(mActionModeCallback);
                    isActionMode = true;
                    return true;
                }
            });

        } else if (holder instanceof FavsAdapter.VHHeader) {
            TextView title = ((FavsAdapter.VHHeader) holder).header;
            title.setText(R.string.favStops);
            View line = ((FavsAdapter.VHHeader) holder).line;

            SharedPreferences prefs = mContext.getSharedPreferences("preferences", Context.MODE_PRIVATE);
            if (prefs.getBoolean("color", true)) {
                title.setTextColor(mContext.getResources().getColor(R.color.color_tab_1_Dark));
                line.setBackgroundColor(mContext.getResources().getColor(R.color.color_tab_1_Dark));
            } else {
                title.setTextColor(mContext.getResources().getColor(R.color.gray_panther));
                line.setBackgroundColor(mContext.getResources().getColor(R.color.gray_panther));
            }
        }
    }

    @Override
    public int getItemCount() {
        if (arrayList == null)
            return 0;
        else
            return arrayList.size();

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return 1;
        else
            return 0;
    }

    private Type_Fav getItem(int position) {
        if (position != 0)
            return arrayList.get(position);
        else
            return null;
    }

    class VHItem extends RecyclerView.ViewHolder {
        final TextView name, customName;
        final CircleImageView typeView;
        final LinearLayout linearLayout;

        private VHItem(View itemView) {
            super(itemView);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout);
            name = (TextView) itemView.findViewById(R.id.name);
            customName = (TextView) itemView.findViewById(R.id.customName);
            typeView = (CircleImageView) itemView.findViewById(R.id.typeView);

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