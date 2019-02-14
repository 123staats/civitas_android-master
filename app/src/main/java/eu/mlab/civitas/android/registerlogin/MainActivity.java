package eu.mlab.civitas.android.registerlogin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import com.android.volley.VolleyError;
import eu.mlab.civitas.android.R;
import eu.mlab.civitas.android.rest.RESTVolleyHandler;
import eu.mlab.civitas.android.util.Util;

/**
 * Created by liisa_000 on 29/01/2017.
 * Updated by Christoph Stanik on 08/03/2017
 * Last update Maksud Ganapijev on 15/09/2018
 */

public class MainActivity extends AppCompatActivity implements RESTVolleyHandler.RESTStringResponse {
    public static final String TAG = MainActivity.class.getSimpleName();

    private boolean doubleBackToExitPressedOnce = false;

    private LoginFragment loginFragment;
    private AboutFragment aboutFragment;
    private RegisterFragment registerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginFragment = new LoginFragment();
        aboutFragment = new AboutFragment();
        registerFragment = new RegisterFragment();

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new MainActivity.ViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(2);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText(getResources().getString(R.string.tab_title_login));
        tabLayout.getTabAt(1).setText(getResources().getString(R.string.tab_title_about));

        getFragmentManager().popBackStackImmediate();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // reset location camera on the map view to the current location
        SharedPreferences.Editor editor = getSharedPreferences(Util.PREF_KEY_FILE, Context.MODE_PRIVATE).edit();
        editor.putBoolean(Util.PREF_KEY_ZOOM_TO_CURRENT_LOCATION, false);
        editor.apply();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.next) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResponse(String response, int responseCode) {
        Log.d(TAG, "onResponse=" + response);
        /*if (response != null && responseCode == Util.RESPONSE_REGISTER_USER) {
            aboutFragment.onResponse(response, responseCode);
        } else*/
        if (response != null && responseCode == Util.RESPONSE_REGISTER_USER) {
            //registerFragment.onResponse(response, responseCode);
            loginFragment.onResponse(response, Util.RESPONSE_REGISTER_USER);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error, int responseCode) {
        Log.e(TAG, "volley error=" + error.toString());
        /*if (responseCode == Util.RESPONSE_REGISTER_USER) {
            aboutFragment.onErrorResponse(error, responseCode);
        } else */
        if (responseCode == Util.RESPONSE_REGISTER_USER) {
            //registerFragment.onErrorResponse(error, responseCode);
            loginFragment.onErrorResponse(error, Util.RESPONSE_REGISTER_USER);
        }
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return loginFragment;
                case 1:
                    return aboutFragment;
                default:
                    return loginFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    public void onBackPressed() {
        //Checking for fragment count on backstack
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }

        if (!doubleBackToExitPressedOnce) {
            doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit.", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        } else {
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
            finish();
        }
    }
}
