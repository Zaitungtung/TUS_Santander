package com.alce.tus.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alce.tus.Adapters.CustomViewPager;
import com.alce.tus.Data.Database;
import com.alce.tus.Data.DatabaseManager;
import com.alce.tus.Data.Json;
import com.alce.tus.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.merhold.extensiblepageindicator.ExtensiblePageIndicator;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;

/**
 * Introduction that's show the user the fragments with the permissions and the update database.
 */

public class Introduction extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 0;
    private static final int REQUEST_FINE_LOCATION = 1;
    private static final int TOTAL_PAGES = 4;
    private static NotificationManager mNotifyManager;
    private static Boolean downloading = false;
    private static CustomViewPager mViewPager;
    private static Boolean active = true;
    private static int Type;
    private static Activity mActivity;
    private static boolean result;

    private static String updateText;

    /**
     * Create the notification that indicate the data is updating.
     *
     * @param mContext Context which need to search the database.
     */
    private static void createNotification(Context mContext) {
        mNotifyManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setContentTitle(mContext.getString(R.string.notifyBig))
                .setContentText(mContext.getString(R.string.notifyLittle))
                .setSmallIcon(R.drawable.ic_notify);
        mBuilder.setProgress(0, 0, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mBuilder.setPriority(Notification.PRIORITY_HIGH);
        }
        mBuilder.setAutoCancel(true);
        mNotifyManager.notify(0, mBuilder.build());
    }

    private static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            Log.e("ERROR", e.toString());
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else
            return dir != null && dir.isFile() && dir.delete();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);

        mActivity = this;

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }
        Type = extras.getInt("Type", 2);

        int resource;
        String season = currentSeason();
        if (season.equals("Summer")) {
            resource = R.drawable.summer;
        } else {
            resource = R.drawable.winter;
        }

        updateText = getString(R.string.intro_page3);
        if (Type == 1) {
            String event = extras.getString("event");
            if (event != null) {
                switch (event) {
                    case "bikeUpdate":
                        resource = R.drawable.bikes_update;
                        updateText = getString(R.string.updateBikes);
                        break;
                    case "winter":
                        resource = R.drawable.winter;
                        updateText = getString(R.string.updateSeason_Winter);
                        break;
                    default:
                        resource = R.drawable.summer;
                        updateText = getString(R.string.updatedDatabase);
                        break;
                }
            }
        }

        ImageView frontPicture = (ImageView) findViewById(R.id.frontPicture);
        Glide.with(this)
                .load(resource)
                .into(frontPicture);

        // Initialize the View Pager.
        mViewPager = (CustomViewPager) findViewById(R.id.view_pager);
        assert mViewPager != null;
        mViewPager.setAdapter(new IntroPagerAdapter(getSupportFragmentManager()));
        mViewPager.setPagingEnabled(false);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (position == TOTAL_PAGES - 1 && ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (!downloading && result) {
                            Introduction.createNotification(getApplicationContext());
                            DownloadInfo downloadInfo = new DownloadInfo();
                            downloadInfo.execute(getApplicationContext());
                        }
                    }
                    if (position == TOTAL_PAGES - 1)
                        mViewPager.setPagingEnabled(false);
                } else {
                    if (Type != 1) {
                        if (position == 1 && !downloading && result) {
                            mViewPager.setPagingEnabled(false);
                            Introduction.createNotification(getApplicationContext());
                            DownloadInfo downloadInfo = new DownloadInfo();
                            downloadInfo.execute(getApplicationContext());
                        }
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // Add the indicator to the mViewPager.
        ExtensiblePageIndicator extensiblePageIndicator = (ExtensiblePageIndicator)
                findViewById(R.id.flexibleIndicator);
        assert extensiblePageIndicator != null;
        extensiblePageIndicator.initViewPager(mViewPager);

        if (Type == 1)
            extensiblePageIndicator.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    private String currentSeason() {

        Calendar currentTime = Calendar.getInstance();
        int currentTime_MONTH = currentTime.get(Calendar.MONTH);
        int startSummer_MONTH = Calendar.JULY;
        int startWinter_MONTH = Calendar.OCTOBER;

        if (currentTime_MONTH >= startSummer_MONTH && currentTime_MONTH < startWinter_MONTH)
            return "Summer";
        else
            return "Winter";
    }

    private static class IntroPagerAdapter extends FragmentPagerAdapter {

        private IntroPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            int pages;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                pages = TOTAL_PAGES;
            else
                pages = 2;

            if (Type == 1)
                pages = 1;

            return pages;
        }

        @Override
        public Fragment getItem(int position) {
            return PageFragment.newInstance(position + 1, position == getCount() - 1);
        }
    }


    public static class PageFragment extends Fragment implements
            FragmentCompat.OnRequestPermissionsResultCallback,
            GoogleApiClient.OnConnectionFailedListener {

        private static final int RC_SIGN_IN = 9001;
        private ProgressDialog mProgressDialog;
        private TextView text, login_text;
        private Button button;
        private ImageView success, fail;
        private ProgressBar progressCircle;
        private SignInButton sign_in_button;
        private ImageView userProfile;
        private GoogleApiClient mGoogleApiClient;
        private FirebaseAuth mAuth;
        private FirebaseAuth.AuthStateListener mAuthListener;

        private static PageFragment newInstance(int page, boolean isLast) {
            Bundle args = new Bundle();
            args.putInt("page", page);
            if (isLast)
                args.putBoolean("isLast", true);
            final PageFragment fragment = new PageFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.intro_fragment, container, false);
            text = (TextView) view.findViewById(R.id.text);
            button = (Button) view.findViewById(R.id.button);
            success = (ImageView) view.findViewById(R.id.success);
            fail = (ImageView) view.findViewById(R.id.fail);
            sign_in_button = (SignInButton) view.findViewById(R.id.sign_in_button);
            userProfile = (ImageView) view.findViewById(R.id.userProfile);
            login_text = (TextView) view.findViewById(R.id.login_text);
            progressCircle = (ProgressBar) view.findViewById(R.id.progressCircle);

            return view;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            int page = 0;
            if (Type == 1)
                page = TOTAL_PAGES;
            else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    if (getArguments().getInt("page", 0) == 1) {
                        mViewPager.setPagingEnabled(true);
                        page = 2;
                    }
                    if (getArguments().getInt("page", 0) == 2) {
                        page = 4;
                    }
                } else
                    page = getArguments().getInt("page", 0);
            }

            switch (page) {
                case 1:
                    text.setText(getString(R.string.intro_page1));
                    button.setVisibility(View.VISIBLE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_EXTERNAL_STORAGE);
                        }
                    });
                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        animationInsideOut(true);
                        mViewPager.setPagingEnabled(true);
                    }
                    break;
                case 2:
                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        mViewPager.setPagingEnabled(true);
                    }
                    button.setVisibility(View.GONE);
                    success.setVisibility(View.GONE);
                    fail.setVisibility(View.GONE);
                    sign_in_button.setVisibility(View.VISIBLE);
                    text.setText(getString(R.string.sign_in));
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build();

                    mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                            .enableAutoManage(getActivity(), 0, this)
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

                    sign_in_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            signIn();
                        }
                    });
                    break;
                case 3:
                    mViewPager.setPagingEnabled(true);
                    button.setVisibility(View.VISIBLE);
                    sign_in_button.setVisibility(View.GONE);
                    text.setText(getString(R.string.intro_page2));
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_FINE_LOCATION);
                        }
                    });
                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        animationInsideOut(true);
                    }
                    break;
                case 4:
                    if (Type == 1) {
                        if (!downloading) {
                            CheckConnection checkConnection = new CheckConnection();
                            checkConnection.execute(getContext());

                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    CheckConnection checkConnection = new CheckConnection();
                                    checkConnection.execute(getContext());
                                }
                            });
                        }
                    } else {

                        CheckConnection checkConnection = new CheckConnection();
                        checkConnection.execute(getContext());

                        text.setText(getString(R.string.intro_page3));
                        button.setVisibility(View.GONE);
                        progressCircle.setVisibility(View.VISIBLE);
                        progressCircle.setIndeterminate(true);
                    }
                    break;
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            Introduction.active = true;
        }

        @Override
        public void onStart() {
            super.onStart();
            Introduction.active = true;
            if (mAuth != null && mAuthListener != null)
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
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("DEBUG", "signInWithCredential:onComplete:" + task.isSuccessful());
                            if (!task.isSuccessful()) {
                                Log.w("DEBUG", "signInWithCredential", task.getException());
                                Toast.makeText(
                                        getActivity().getApplicationContext(),
                                        "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            hideProgressDialog();
                        }
                    });
        }

        private void signIn() {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }

        private void updateUI(FirebaseUser user) {
            hideProgressDialog();
            if (user != null) {
                sign_in_button.setVisibility(View.GONE);
                userProfile.setVisibility(View.VISIBLE);
                login_text.setVisibility(View.VISIBLE);
                text.setVisibility(View.GONE);
                Glide.with(this).load(user.getPhotoUrl()).into(userProfile);

                String welcome = getString(R.string.welcome) + " " + user.getDisplayName();
                login_text.setText(welcome);
            } else {
                sign_in_button.setVisibility(View.VISIBLE);
                text.setVisibility(View.VISIBLE);
                userProfile.setVisibility(View.GONE);
                login_text.setVisibility(View.GONE);
            }
        }

        private void showProgressDialog() {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage(getActivity().getApplicationContext()
                    .getString(R.string.sign_in_process));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        private void hideProgressDialog() {
            if (mProgressDialog != null)
                mProgressDialog.dismiss();
        }

        private void checkPermission(String permission, int requestPermision) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    permission)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{permission},
                        requestPermision);
            } else {
                animationInsideOut(true);
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                               @NonNull int[] grantResults) {
            switch (requestCode) {
                case REQUEST_EXTERNAL_STORAGE:
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        animationInsideOut(true);
                        mViewPager.setPagingEnabled(true);
                    } else {
                        text.setText(getString(R.string.rejectStorage));
                    }
                    break;

                case REQUEST_FINE_LOCATION:
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        animationInsideOut(true);
                    } else {
                        animationInsideOut(false);
                    }
                    break;
            }
        }

        private void animationInsideOut(boolean accept) {
            final ImageView imageView;
            if (accept)
                imageView = success;
            else
                imageView = fail;

            Animation circleOut = AnimationUtils.loadAnimation(getContext(), R.anim.popout);
            final Animation circleIn = AnimationUtils.loadAnimation(getContext(), R.anim.popin);
            button.startAnimation(circleOut);
            circleOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    imageView.startAnimation(circleIn);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    button.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            circleIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    imageView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }

        private class CheckConnection extends AsyncTask<Context, Boolean, Boolean> {

            private Context mContext;

            @Override
            protected Boolean doInBackground(Context... contexts) {
                mContext = contexts[0];
                ConnectivityManager connectivityManager = (ConnectivityManager)
                        mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

                if (connectivityManager.getActiveNetworkInfo() != null) {
                    try {
                        InetAddress ipAddr = InetAddress.getByName("www.google.com");
                        return !ipAddr.equals("");
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        return false;
                    }
                } else {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);

                Introduction.result = result;

                if (result) {
                    text.setText(updateText);
                    progressCircle.setVisibility(View.VISIBLE);
                    progressCircle.setIndeterminate(true);
                    button.setVisibility(View.GONE);
                    if (Type == 1) {
                        Introduction.createNotification(getContext());
                        DownloadInfo downloadInfo = new DownloadInfo();
                        downloadInfo.execute(getContext());
                    }
                } else {
                    text.setText(mContext.getString(R.string.connectionError));
                    progressCircle.setVisibility(View.GONE);
                    button.setVisibility(View.VISIBLE);
                    button.setText(R.string.download);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CheckConnection checkConnection = new CheckConnection();
                            checkConnection.execute(mContext);
                        }
                    });
                }

            }
        }
    }

    private static class DownloadInfo extends AsyncTask<Context, Context, Context> {

        @Override
        protected Context doInBackground(Context... context) {
            Context mContext = context[0];

            downloading = true;

            if (Type != 2) {
                String DB_DATA = "info.db";
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
            }

            return mContext;
        }

        @Override
        protected void onPostExecute(Context context) {
            super.onPostExecute(context);
            if (mNotifyManager != null)
                mNotifyManager.cancel(0);
            deleteCache(context);
            if (active) {
                Intent intent = new Intent(context, MainActivity.class);
                mActivity.startActivity(intent);
                mActivity.overridePendingTransition(R.anim.rtc, R.anim.ctl);
                mActivity.finish();
            } else {
                Intent resultIntent = new Intent(context, MainActivity.class);
                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                context,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                mNotifyManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
                mBuilder.setContentTitle(context.getString(R.string.notifyBigComplete))
                        .setContentText(context.getString(R.string.notifyLittleComplete))
                        .setSmallIcon(R.drawable.ic_notify);
                mBuilder.setAutoCancel(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mBuilder.setPriority(Notification.PRIORITY_HIGH);
                }
                mBuilder.setContentIntent(resultPendingIntent);
                mNotifyManager.notify(1, mBuilder.build());
                mActivity.finish();
            }

            Calendar rightNow = Calendar.getInstance();
            if (Type == 1) {
                SharedPreferences prefs =
                        context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                if (rightNow.get(Calendar.MONTH) == Calendar.JULY && rightNow.get(Calendar.DAY_OF_MONTH) == 1)
                    editor.putBoolean("summer_update", true);
                if (rightNow.get(Calendar.MONTH) == Calendar.OCTOBER && rightNow.get(Calendar.DAY_OF_MONTH) == 1)
                    editor.putBoolean("winter_update", true);
                editor.apply();
            }
        }
    }
}
