package eu.mlab.civitas.android.addartefact;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import eu.mlab.civitas.android.MapActivity;
import eu.mlab.civitas.android.R;
import eu.mlab.civitas.android.rest.RESTVolleyHandler;
import eu.mlab.civitas.android.model.Artefact;
import eu.mlab.civitas.android.model.ArtefactItem;
import eu.mlab.civitas.android.model.Category;
import eu.mlab.civitas.android.model.UserRole;
import eu.mlab.civitas.android.util.LocationService;
import eu.mlab.civitas.android.util.Util;

public class AddArtefactActivity extends AppCompatActivity implements RESTVolleyHandler.RESTStringResponse, RESTVolleyHandler.RESTJSONArrayResponse {
    public static final String TAG = AddArtefactActivity.class.getSimpleName();

    private String name;
    private String category;
    private String date;
    private LatLng latLng;
    private List<ArtefactItem> artefactItemList;
    private ArtefactItemAdapter artefactItemAdapter;
    private ArtefactItemsOverviewFragment artefactItemsOverviewFragment;
    private AddArtefactGeneralFragment addArtefactGeneralFragment;
    private ProgressDialog uploadProgressDialog;
    private Handler handle;
    private RESTVolleyHandler rest;
    private int maxProgress;
    private LocationService locationService;
    private Location location;
    private Artefact artefact;
    private Bundle bundle;
    private Context mContext;
    private int artefactId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_artefact);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try{
            //TODO:FIX THE BACK BUTTON
            // Don't use the toolbar as the back button has undesired effects.
            // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Log.e("err", "could not find the support action bar");
        }
        artefactId = -1;
        rest = new RESTVolleyHandler(this);
        rest.setRestJSONArrayResponseCallbackHandler(this);
        mContext = this;
        artefact = getIntent().getExtras().getParcelable(Util.INTENT_ARTEFACT);
        artefactItemList = new ArrayList<>();

        if(artefact != null){
            bundle = new Bundle();
            bundle.putParcelable(Util.INTENT_ARTEFACT, artefact);
            artefactId = artefact.getId();
            rest.loadAllArtefactItemsByArtefactID(artefactId);
        }

        //AddArtefactGeneralFragment fragment = (AddArtefactGeneralFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
        //fragment.setLocation(location); // set the Location for the map in the "new

        addArtefactGeneralFragment = new AddArtefactGeneralFragment();
        artefactItemsOverviewFragment = new ArtefactItemsOverviewFragment();

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(2);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText(getString(R.string.tab_title_first_step));
        tabLayout.getTabAt(1).setText(getString(R.string.tab_title_second_step));

        if((artefact!= null) && (artefact.getArtefactItems() != null)) {
            artefactItemList = artefact.getArtefactItems();
        }

        artefactItemAdapter = new ArtefactItemAdapter(this, artefactItemList);
        locationService = LocationService.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(R.string.create_artefact);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_artefact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.next) {
            uploadArtefact();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public int getMaxProgress() {
        int maxItems = 0;
        for (ArtefactItem item :artefactItemList) {
            if (item.getAudioPath() != null && !item.getAudioPath().isEmpty()) {
                maxItems++;
            }
            if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
                maxItems++;
            }
            maxItems++;
            maxProgress = ++ maxItems;
        }
        return maxProgress;
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return AddArtefactGeneralFragment.instantiate(mContext, AddArtefactGeneralFragment.class.getName(), bundle);
                case 1:
                    return ArtefactItemsOverviewFragment.instantiate(mContext, ArtefactItemsOverviewFragment.class.getName(), bundle);
                default:
                    return AddArtefactGeneralFragment.instantiate(mContext, AddArtefactGeneralFragment.class.getName(), bundle);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult called with request code: " + requestCode);
        if (requestCode == Util.ADD_ARTEFACT_ELEMENT_REQUEST && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "onActivityResult ADD_ARTEFACT_ELEMENT_REQUEST");
            ArtefactItem artefactItem = data.getParcelableExtra(Util.INTENT_ARTEFACT_ITEM);
            artefactItemList.add(artefactItem);
            artefactItemAdapter.notifyDataSetChanged();
        }
        else if (requestCode == Util.ARTEFACT_ITEM_UPDATE_REQUEST && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "onActivityResult ARTEFACT_ITEM_UPDATE_REQUEST");
            ArtefactItem artefactItem = data.getParcelableExtra(Util.INTENT_ARTEFACT_ITEM);
            int artefactItemListPosition = data.getIntExtra(Util.INTENT_ARTEFACT_ITEM_POSITION, 0);
            artefactItemList.remove(artefactItemListPosition);
            artefactItemList.add(artefactItem);
            artefactItemAdapter.notifyDataSetChanged();
        }
    }

    public void uploadArtefact() {
        // 1. validation
        // 2. create upload dialog
        // 3. rest calls

        // 1. validation
        AddArtefactGeneralFragment fragment = (AddArtefactGeneralFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
        fragment.setLocation(location); // set the Location for the map in the "new
        // Artefact view"
        Artefact artefact = new Artefact();
        //if artefact id is set (and not equal to default value), we update artefact
        if (artefactId != -1){
            artefact.setId(artefactId);
        }
        artefact.setName(fragment.getName().getText().toString());
        artefact.setLongitude(fragment.getCenterMarker().getPosition().longitude);
        artefact.setLatitude(fragment.getCenterMarker().getPosition().latitude);
        artefact.setDating(fragment.getSpinnerDating().getSelectedItem().toString());
        artefact.setCategory(new Category(fragment.getSpinnerCategory().getSelectedItemPosition(), this));
        artefact.setCategoryId(fragment.getSpinnerCategory().getSelectedItemPosition());

        for (ArtefactItem artefactItem : artefactItemList) {
            artefact.addArtefactItem(artefactItem);
        }
        if (!artefact.isValid()) {
            Toast.makeText(this, R.string.validation_artefact_incomplete, Toast.LENGTH_LONG).show();
            return;
        }

        // 2. create upload dialog
        uploadProgressDialog = new ProgressDialog(this);
        uploadProgressDialog.setMax(getMaxProgress());
        uploadProgressDialog.setTitle(getString(R.string.progress_artefact_upload_title));
        uploadProgressDialog.setMessage(getString(R.string.progress_artefact_upload_message));
        uploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        uploadProgressDialog.show();

        location = new Location(LocationManager.PASSIVE_PROVIDER);
        location.setLongitude(fragment.getCenterMarker().getPosition().longitude);
        location.setLatitude(fragment.getCenterMarker().getPosition().latitude);
        location.setTime(Calendar.getInstance().getTimeInMillis());

        handle = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                uploadProgressDialog.incrementProgressBy(1);
                if (uploadProgressDialog.getProgress() == maxProgress) {
                    finishedUploading();
                }
            }
        };

        // 3. rest calls
        rest = new RESTVolleyHandler(this);
        rest.setRestStringResponseCallbackHandler(this);
        rest.uploadArtefact(artefact);
    }

    public void finishedUploading(){
        uploadProgressDialog.dismiss();

        //set the location of the last crated artefact in the location service.
        locationService.setLastCreatedArtefactLocation(location);

        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(Util.PARAM_USER_ROLE, UserRole.USER);
        startActivity(intent);
    }

    /**
     * just call this, when the general artefact information was uploaded correctly
     */
    public void uploadArtefactItems(int artefactId) {
        SharedPreferences sharedPref = getSharedPreferences(Util.PREF_KEY_FILE, Context.MODE_PRIVATE);
        for (ArtefactItem artefactItem : artefactItemList) {
            rest.uploadArtefactItem(artefactId, artefactItem, sharedPref.getString(Util.PREF_USER_ID, ""));
            rest.uploadImage(artefactItem.getImagePath(), artefactItem.getImageName());
            // audio upload is optional
            if (artefactItem.getAudioPath() != null && !artefactItem.getAudioPath().isEmpty()) {
                rest.uploadAudio(artefactItem.getAudioPath(), artefactItem.getAudioName());
            }
        }
        if (uploadProgressDialog.isShowing()) {
            finishedUploading();
        }
    }

    @Override
    public void onResponse(String response, int responseCode) {
        if (response != null && responseCode == Util.RESPONSE_ARTEFACT_UPLOAD) {
            Log.d(TAG, "onResponse=" + response);
            try {
                JSONObject j = new JSONObject(response);
                uploadArtefactItems(j.getInt(Util.JSON_ARTEFACT_ID));
                handle.sendMessage(handle.obtainMessage());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if (response != null && responseCode == Util.RESPONSE_POST_ARTEFACT_ITEM) {
            handle.sendMessage(handle.obtainMessage());
        }
        else if (response != null && responseCode == Util.RESPONSE_POST_ARTEFACT_IMAGE) {
            handle.sendMessage(handle.obtainMessage());
        }
        else if (response != null && responseCode == Util.RESPONSE_POST_ARTEFACT_AUDIO) {
            handle.sendMessage(handle.obtainMessage());
        }
        else if (response != null && responseCode == Util.RESPONSE_DELETE_ARTEFACT_ITEMS){
            handle.sendMessage(handle.obtainMessage());
            rest.deleteArtefact(artefact);
        }
        else if (response != null && responseCode == Util.RESPONSE_DELETE_ARTEFACT){
            handle.sendMessage(handle.obtainMessage());
        }
    }

    @Override
    public void onResponse(JSONArray response, int responseCode) {
        if (response != null && responseCode == Util.RESPONSE_LOAD_ALL_CATEGORIES) {
            addArtefactGeneralFragment.onResponse(response, responseCode);
        }

        if (response != null && responseCode == Util.RESPONSE_LOAD_ARTEFACT_ITEMS) {
            Log.d(TAG, "number of attached artefact items: " + response.length());
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject j = response.getJSONObject(i);
                    Log.e(TAG, "response=" + j.toString());
                    artefact.addArtefactItem(new ArtefactItem(
                            j.getString(Util.JSON_ARTEFACT_ITEM_IMAGE_NAME),
                            j.getString(Util.JSON_ARTEFACT_ITEM_AUDIO_NAME),
                            j.getString(Util.JSON_ARTEFACT_ITEM_DESCRIPTION),
                            j.getString(Util.JSON_ARTEFACT_ITEM_LICENSE_TYPE),
                            j.getString(Util.JSON_ARTEFACT_ITEM_LICENSE_LINK),
                            j.getString(Util.JSON_ARTEFACT_ITEM_AUTHOR))
                    );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            for (ArtefactItem item :artefact.getArtefactItems()) {
                artefactItemList.add(item);
                artefactItemAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error, int responseCode) {
        Log.e(TAG, "volley error=" + error.toString());
        handle.sendMessage(handle.obtainMessage());
        if (responseCode == Util.RESPONSE_ARTEFACT_UPLOAD) {
            Toast.makeText(this, "could not upload artefact to server", Toast.LENGTH_LONG).show();
        }

        if (responseCode == Util.RESPONSE_DELETE_ARTEFACT) {
            Toast.makeText(this, "could not delete an artefact", Toast.LENGTH_LONG).show();
        }

        if (responseCode == Util.RESPONSE_DELETE_ARTEFACT_ITEMS) {
            Toast.makeText(this, "could not delete an artefact items", Toast.LENGTH_LONG).show();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public ArtefactItemAdapter getArtefactItemAdapter() {
        return artefactItemAdapter;
    }

    public void setArtefactItemAdapter(ArtefactItemAdapter artefactItemAdapter) {
        this.artefactItemAdapter = artefactItemAdapter;
    }
}