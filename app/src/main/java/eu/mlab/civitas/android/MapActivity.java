package eu.mlab.civitas.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;

import java.io.File;
import java.util.Calendar;

import eu.mlab.civitas.android.R;
import eu.mlab.civitas.android.addartefact.AddArtefactGeneralFragment;
import eu.mlab.civitas.android.addartefact.ArtefactItemsOverviewFragment;
import eu.mlab.civitas.android.rest.RESTVolleyHandler;
import eu.mlab.civitas.android.registerlogin.MainActivity;
import eu.mlab.civitas.android.model.UserRole;
import eu.mlab.civitas.android.util.LocationService;
import eu.mlab.civitas.android.util.Util;

public class MapActivity extends AppCompatActivity implements RESTVolleyHandler.RESTJSONArrayResponse, LocationListener {
    public static final String TAG = MapActivity.class.getSimpleName();

    private boolean doubleBackToExitPressedOnce = false;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private UserRole userRole;
    private String userEmail;

    private GoogleApiClient googleApiClient;

    private AddArtefactGeneralFragment addArtefactGeneralFragment;
    private ArtefactItemsOverviewFragment artefactItemsOverviewFragment;
    private MapsFragment mapsFragment;
    private MyArtefactsFragment myArtefactsFragment;
    private BookmarksFragment bookmarksFragment;
    private LocationManager mLocationManager;
    private LocationService locationService = LocationService.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        userRole = UserRole.valueOf(intent.getSerializableExtra(Util.PARAM_USER_ROLE).toString());
        userEmail = (String) intent.getSerializableExtra(Util.PARAM_USER_EMAIL);

        addArtefactGeneralFragment = new AddArtefactGeneralFragment();
        artefactItemsOverviewFragment = new ArtefactItemsOverviewFragment();
        mapsFragment = new MapsFragment();
        myArtefactsFragment = new MyArtefactsFragment();
        bookmarksFragment = new BookmarksFragment();

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        localize();

        new File(getFilesDir(), "images").mkdirs();
        new File(getFilesDir(), "audio").mkdirs();

        initDrawer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mapsFragment.isVisible()) {
            Bundle arguments = new Bundle();
            arguments.putSerializable(Util.PARAM_USER_ROLE, userRole);
            mapsFragment.setArguments(arguments);
            getSupportFragmentManager().popBackStackImmediate();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, mapsFragment)
                    .commit();
            setTitle(R.string.title_activity_maps);
        }
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
            googleApiClient = new GoogleApiClient.Builder(this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
            googleApiClient.connect();
        } catch (Exception e) {
            Log.e(TAG, "could not connect to google account");
        }

    }

    private void initDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close) {

            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectDrawerItem(item);
                return true;
            }
        });

        if (userRole == UserRole.GUEST) {
            Menu menu = navigationView.getMenu();
//            MenuItem myArtefactsItem = menu.findItem(R.id.navigation_view_item_my_artefacts);
//            MenuItem myBookmarksItem = menu.findItem(R.id.navigation_view_item_bookmarks);

//            myArtefactsItem.setVisible(false);
//            myBookmarksItem.setVisible(false);
        }
    }

    public void selectDrawerItem(MenuItem item) {
        item.setChecked(true);
        drawerLayout.closeDrawers();
        switch (item.getItemId()) {
            case R.id.navigation_view_item_map:
                if (!mapsFragment.isVisible()) {
                    Bundle arguments = new Bundle();
                    arguments.putSerializable(Util.PARAM_USER_ROLE, userRole);
                    mapsFragment.setArguments(arguments);
                    switchFragment(mapsFragment);
                    setTitle(item.getTitle());
                }
                break;
//            case R.id.navigation_view_item_my_artefacts:
//                switchFragment(myArtefactsFragment);
//                break;
//            case R.id.navigation_view_item_bookmarks:
//                switchFragment(bookmarksFragment);
//                break;
            case R.id.navigation_view_item_logout:
                logoutUser();
                break;
            case R.id.navigation_view_item_introduction:
                Uri uri = Uri.parse(Util.INTRODUCTION_URL);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case R.id.navigation_view_item_about:
                Intent impressumIntent = new Intent(this, ImpressumActivity.class);
                startActivity(impressumIntent);
                break;
            default:
                if (!mapsFragment.isVisible()) {
                    Bundle arg = new Bundle();
                    arg.putSerializable(Util.PARAM_USER_ROLE, userRole);
                    mapsFragment.setArguments(arg);
                    switchFragment(mapsFragment);
                    setTitle(item.getTitle());
                }
        }
    }

    private void switchFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStackImmediate();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    private void logoutUser() {
        SharedPreferences.Editor editor = getSharedPreferences(Util.PREF_KEY_FILE, Context.MODE_PRIVATE).edit();
        editor.putBoolean(Util.PREF_KEY_ZOOM_TO_CURRENT_LOCATION, false);
        // reset user mail address if the guest login was chosen
        editor.putString(Util.PREF_USER_MAIL, "");
        editor.commit();

        if (googleApiClient != null && googleApiClient.isConnected()) {
            Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                finish();
                            }
                        }
                    });
        } else {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        setTitle(getString(R.string.map));
        navigationView.getMenu().getItem(0).setChecked(true);
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            getSupportFragmentManager().popBackStack();
        }

        if (!doubleBackToExitPressedOnce) {
            doubleBackToExitPressedOnce = true;
            Toast.makeText(this,"Please click BACK again to exit.", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
        else {
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
            finish();
        }
    }

    @Override
    public void onResponse(JSONArray response, int responseCode) {
        if (response != null && responseCode == Util.RESPONSE_LOAD_ALL_ARTEFACTS) {
            if (mapsFragment.isVisible()) {
                mapsFragment.onResponse(response, responseCode);
            }
            else if (myArtefactsFragment.isVisible()) {
                myArtefactsFragment.onResponse(response, responseCode);
            }
        }
        if (response != null && responseCode == Util.RESPONSE_LOAD_ALL_CATEGORIES) {
            mapsFragment.onResponse(response, responseCode);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error, int responseCode) {
        Log.e(TAG, "volley error=" + error.toString());
        if (responseCode == Util.RESPONSE_LOAD_ALL_ARTEFACTS) {

            if (mapsFragment.isVisible()) {
                mapsFragment.onErrorResponse(error, responseCode);
            }
            else if (myArtefactsFragment.isVisible()) {
                myArtefactsFragment.onErrorResponse(error, responseCode);
            }
        }
    }

    public void localize(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            Log.v("Permissions", "Location permission not granted");
            return;
        }
        Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (!locationService.isLocationValid(location) && mLocationManager.isProviderEnabled(LocationManager
                .NETWORK_PROVIDER)){
            // localize using cell-network or wifi
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        else if (!locationService.isLocationValid(location) && mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            // localize using GPS
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (location == null){
            Toast.makeText(this, "Gerät konnte nicht lokalisiert werden. Aktivieren Sie bitte ihre Standortermittlung" +
                    " oder überprüfen Sie die Signalqualität.", Toast.LENGTH_LONG)
                    .show();
        }
        locationService.setUserLocation(location);
    }

    private boolean isLocationValid(Location location){
        boolean result = false;
        if (location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000){
            result = true;
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.v("Location Changed", location.getLatitude() + " and " + location.getLongitude());
            mLocationManager.removeUpdates(this);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}