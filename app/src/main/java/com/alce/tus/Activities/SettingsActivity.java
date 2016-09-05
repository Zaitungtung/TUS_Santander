package com.alce.tus.Activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alce.tus.Data.Database;
import com.alce.tus.Data.DatabaseManager;
import com.alce.tus.Data.Json;
import com.alce.tus.R;
import com.alce.tus.Types.Type_Fav;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final int RC_SIGN_IN = 9001;
    public static Boolean rebuild = false;
    private static ProgressDialog mProgressDialog;
    private static Boolean downloading = false;
    private static boolean bikeShow, busShow;
    private TextView userMail;
    private TextView userName;
    private ImageView userProfile;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userName = (TextView) findViewById(R.id.userName);
        userMail = (TextView) findViewById(R.id.userMail);
        userProfile = (ImageView) findViewById(R.id.userProfile);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d("DEBUG", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d("DEBUG", "onAuthStateChanged:signed_out");
                }
                updateUI(user);
            }
        };

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new SettingsPreference())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!downloading)
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!downloading) {
            finish();
            overridePendingTransition(
                    R.anim.ltr,
                    R.anim.ctr);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else
                updateUI(null);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("DEBUG", "firebaseAuthWithGoogle:" + acct.getId());
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("DEBUG", "signInWithCredential:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w("DEBUG", "signInWithCredential", task.getException());
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            migrateData();
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        mAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            userName.setVisibility(View.VISIBLE);
            userName.setText(user.getDisplayName());
            userMail.setVisibility(View.VISIBLE);
            userMail.setText(user.getEmail());
            Glide.with(this).load(user.getPhotoUrl()).into(userProfile);
            userProfile.setVisibility(View.VISIBLE);

            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
        } else {
            userName.setVisibility(View.GONE);
            userMail.setVisibility(View.GONE);
            userProfile.setVisibility(View.GONE);

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("DEBUG", "onConnectionFailed:" + connectionResult);
        Toast.makeText(getApplicationContext(),
                "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private void migrateData() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            String DB_FAV = "fav.db";
            Database database = new Database(getApplicationContext(), DB_FAV);
            SQLiteDatabase db = database.getReadableDatabase();
            DatabaseManager databaseManager = new DatabaseManager(getApplicationContext(), db, DB_FAV);

            ArrayList<Type_Fav> type_favs = databaseManager.getFav(true, null);

            for (int i = 0; i != type_favs.size(); i++) {
                mDatabase.child("users").child(userId).child(type_favs.get(i).getNumber())
                        .setValue(type_favs.get(i));
            }

            Database.deleteDatabase(getApplicationContext(), DB_FAV);

        }
    }

    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getApplicationContext().getString(R.string.sign_in_process));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    public static class SettingsPreference extends PreferenceFragment implements
            SharedPreferences.OnSharedPreferenceChangeListener {

        private SwitchPreference styleSwitch;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            Preference myPref = findPreference("reportButton");
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    if (!downloading) {
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"zaitungapps@gmail.com"});
                        i.putExtra(Intent.EXTRA_SUBJECT, "Report");
                        try {
                            startActivity(Intent.createChooser(i, getString(R.string.send_email)));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.noEmail_client), Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                }
            });
            myPref = findPreference("comunityButton");
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    if (!downloading) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://plus.google.com/u/1/communities/107159156808662756892"));
                        try {
                            startActivity(browserIntent);
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.noEmail_client), Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                }
            });
            myPref = findPreference("dataButton");
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {

                    mProgressDialog = new ProgressDialog(getActivity());
                    mProgressDialog.setMessage(getActivity().getApplicationContext().getString(R.string.notifyBig));
                    mProgressDialog.setIndeterminate(true);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();

                    Download download = new Download();
                    download.execute(getActivity().getApplicationContext());

                    return true;
                }
            });
            myPref = findPreference("transportButton");
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {

                    final SharedPreferences prefs =
                            getActivity().getApplicationContext().getSharedPreferences("preferences", Context.MODE_PRIVATE);
                    busShow = prefs.getBoolean("busShow", true);
                    bikeShow = prefs.getBoolean("bikeShow", true);

                    final CharSequence[] items = {
                            getString(R.string.bus),
                            getString(R.string.tab3)};

                    boolean[] checkValues = {busShow, bikeShow};
                    AlertDialog dialog = new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.transportTitle))
                            .setMultiChoiceItems(items, checkValues, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                                    if (((AlertDialog) dialog).getListView().getCheckedItemCount() == 0) {
                                        Toast.makeText(getActivity().getApplicationContext(),
                                                getString(R.string.emptyTransport),
                                                Toast.LENGTH_SHORT).show();
                                        ((AlertDialog) dialog).getListView().setItemChecked(0, true);
                                        busShow = true;
                                    }

                                    if (indexSelected == 0) {
                                        busShow = isChecked;
                                    }
                                    if (indexSelected == 1) {
                                        bikeShow = isChecked;
                                    }
                                }
                            }).setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {

                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putBoolean("bikeShow", bikeShow);
                                    editor.putBoolean("busShow", busShow);
                                    editor.apply();

                                    Intent mStartActivity = new Intent(
                                            getActivity().getApplicationContext(),
                                            SplashActivity.class);
                                    mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    int mPendingIntentId = 123456;
                                    PendingIntent mPendingIntent = PendingIntent.getActivity(
                                            getActivity().getApplicationContext(),
                                            mPendingIntentId,
                                            mStartActivity,
                                            PendingIntent.FLAG_CANCEL_CURRENT);
                                    AlarmManager mgr = (AlarmManager)
                                            getActivity()
                                                    .getApplicationContext()
                                                    .getSystemService(Context.ALARM_SERVICE);
                                    mgr.set(
                                            AlarmManager.RTC,
                                            System.currentTimeMillis() + 100, mPendingIntent);
                                    getActivity().finishAffinity();
                                }
                            }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    //  Your code when user clicked on Cancel
                                }
                            }).create();
                    dialog.show();

                    return true;
                }
            });
            styleSwitch = (SwitchPreference) findPreference("styleSwitch");
            SharedPreferences prefs =
                    getActivity().getApplicationContext().getSharedPreferences("preferences", Context.MODE_PRIVATE);
            styleSwitch.setChecked(prefs.getBoolean("color", true));
            styleSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    boolean color = (Boolean) o;

                    SharedPreferences prefs =
                            getActivity().getApplicationContext().getSharedPreferences("preferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("color", color);
                    editor.apply();

                    styleSwitch.setChecked(color);

                    return false;
                }
            });

        }


        @Override
        public void onCreatePreferences(Bundle bundle, String s) {

        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if (s.equals("color")) {
                boolean color = sharedPreferences.getBoolean("color", false);
                styleSwitch.setChecked(color);
            }
        }

        private static class Download extends AsyncTask<Context, String, String> {

            @Override
            protected String doInBackground(Context... context) {
                Context mContext = context[0];

                downloading = true;

                String DB_DATA = "info.db";
                Database.deleteDatabase(mContext, DB_DATA);
                Database database = new Database(mContext, DB_DATA);
                SQLiteDatabase db = database.getWritableDatabase();

                Json json = new Json();
                json.main(mContext, db);

                db = database.getReadableDatabase();
                DatabaseManager databaseManager = new DatabaseManager(mContext, db, DB_DATA);
                databaseManager.extractLines();
                databaseManager.extractBikes();
                databaseManager.getSearchInfo();

                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) {
                    String DB_FAV = "fav.db";
                    database = new Database(mContext, DB_FAV);
                    database.getWritableDatabase();
                }

                downloading = false;
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                mProgressDialog.dismiss();
                rebuild = true;
            }
        }
    }
}