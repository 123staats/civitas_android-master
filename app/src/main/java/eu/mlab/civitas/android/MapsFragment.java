package eu.mlab.civitas.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.kml.KmlLayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import eu.mlab.civitas.android.R;
import eu.mlab.civitas.android.addartefact.AddArtefactActivity;
import eu.mlab.civitas.android.model.Artefact;
import eu.mlab.civitas.android.model.Category;
import eu.mlab.civitas.android.model.UserRole;
import eu.mlab.civitas.android.rest.RESTVolleyHandler;
import eu.mlab.civitas.android.util.LocationService;
import eu.mlab.civitas.android.util.Util;


public class MapsFragment extends Fragment implements OnMapReadyCallback, RESTVolleyHandler.RESTJSONArrayResponse {
    public static final String TAG = MapsFragment.class.getSimpleName();

    private UserRole userRole;
    private MapView mapView;
    private static GoogleMap googleMap;
    private static final String LOCATION_REQUESTED_KEY = "location_requested";

    private final int PERMISSION_REQUEST = 200;

    private FloatingActionButton floatingActionButton;
    private FrameLayout frameLayout;

    private Spinner spinnerTimePeriod;
    private Spinner spinnerCategory;
    private SharedPreferences sharedPref;

    private List<Artefact> artefactList;
    private HashMap<Marker, Artefact> artefactsMap = new HashMap<>();
    private List<Marker> settlements;
    private List<Category> categories;
    private Menu MENU;


    private LocationService locationService = LocationService.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {"android.permission.RECORD_AUDIO", "android.permission.ACCESS_FINE_LOCATION"};
            requestPermissions(permissions, PERMISSION_REQUEST);
        }

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            userRole = (UserRole) bundle.get(Util.PARAM_USER_ROLE);
        }
        sharedPref = getActivity().getSharedPreferences(Util.PREF_KEY_FILE, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        mapView = (MapView) rootView.findViewById(R.id.map);
        frameLayout = (FrameLayout) rootView.findViewById(R.id.filter_frame);

        String[] rawTimePeriods = getResources().getStringArray(R.array.time_periods);
        spinnerTimePeriod = (Spinner) rootView.findViewById(R.id.spinner_time);
        spinnerTimePeriod.setAdapter(
                new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_list_item_1,
                        new ArrayList<>(Arrays.asList(rawTimePeriods))
                )
        );
        spinnerTimePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Iterator it = artefactsMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    int iMarker = calculateRating(((Artefact) pair.getValue()).getDating());
                    int iSpinner = calculateRating(spinnerTimePeriod.getSelectedItem().toString());
                    Log.d("spinnerTimePeriod", iMarker + " | " + iSpinner);
                    if (spinnerTimePeriod.getSelectedItem().equals("Alle") | iMarker >= iSpinner) {
                        ((Marker) pair.getKey()).setVisible(true);
                    } else {
                        ((Marker) pair.getKey()).setVisible(false);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

            private int calculateRating(String s) {
                int result = 0;

                switch (s) {
                    case "Alle":
                        result = 99;
                        break;
                    case "1. Hälfte 8. Jh. v. Chr.":
                        result = 1;
                        break;
                    case "2. Hälfte 8. Jh. v. Chr.":
                        result = 2;
                        break;
                    case "1. Hälfte 7. Jh. v. Chr.":
                        result = 3;
                        break;
                    case "2. Hälfte 7. Jh. v. Chr.":
                        result = 4;
                        break;
                    case "1. Hälfte 6. Jh. v. Chr.":
                        result = 5;
                        break;
                    case "2. Hälfte 6. Jh. v. Chr.":
                        result = 6;
                        break;
                    case "1. Hälfte 5. Jh. v. Chr.":
                        result = 7;
                        break;
                    case "2. Hälfte 5. Jh. v. Chr.":
                        result = 8;
                        break;
                    case "1. Hälfte 4. Jh. v. Chr.":
                        result = 9;
                        break;
                    case "2. Hälfte 4. Jh. v. Chr.":
                        result = 10;
                        break;
                    case "1. Hälfte 3. Jh. v. Chr.":
                        result = 11;
                        break;
                    case "2. Hälfte 3. Jh. v. Chr.":
                        result = 12;
                        break;
                    case "1. Hälfte 2. Jh. v. Chr.":
                        result = 13;
                        break;
                    case "2. Hälfte 2. Jh. v. Chr.":
                        result = 14;
                        break;
                    case "1. Hälfte 1. Jh. v. Chr.":
                        result = 15;
                        break;
                    case "2. Hälfte 1. Jh. v. Chr.":
                        result = 16;
                        break;
                    case "1. Hälfte 1. Jh. n. Chr.":
                        result = 17;
                        break;
                    case "2. Hälfte 1. Jh. n. Chr.":
                        result = 18;
                        break;
                    case "1. Hälfte 2. Jh. n. Chr.":
                        result = 19;
                        break;
                    case "2. Hälfte 2. Jh. n. Chr.":
                        result = 20;
                        break;
                    case "1. Hälfte 3. Jh. n. Chr.":
                        result = 21;
                        break;
                    case "2. Hälfte 3. Jh. n. Chr.":
                        result = 22;
                        break;
                    case "1. Hälfte 4. Jh. n. Chr.":
                        result = 23;
                        break;
                    case "2. Hälfte 4. Jh. n. Chr.":
                        result = 24;
                        break;
                }

                return result;
            }
        });

        spinnerCategory = (Spinner) rootView.findViewById(R.id.spinner_category);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Iterator it = artefactsMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    String aCategory = ((Artefact) pair.getValue()).getCategoryName();
                    String sCategory = (String) spinnerCategory.getSelectedItem();
                    if (spinnerCategory.getSelectedItem().equals("Alle") | aCategory.equals(sCategory)) {
                        ((Marker) pair.getKey()).setVisible(true);
                    } else {
                        ((Marker) pair.getKey()).setVisible(false);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        floatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.buttonAddArtefact);
        if (userRole == UserRole.GUEST) {
            floatingActionButton.setVisibility(View.INVISIBLE);
        } else {
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startAddArtefactActivity();
                }
            });
        }

        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        MENU = menu;
        if (Util.readMode){
            MENU.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_pin_drop_black_24px));
        }
        else{
            MENU.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_pin_drop_white_24px));
        }
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_filter) {
            if (frameLayout.getVisibility() == View.VISIBLE) {
                frameLayout.setVisibility(View.INVISIBLE);
            } else {
                frameLayout.setVisibility(View.VISIBLE);
            }
        }
        if (id == R.id.action_localize) {
            ((MapActivity) getActivity()).localize();
            localizeAndZoomToUser();
        }
        if (id == R.id.action_read_mode){
            if (Util.readMode){
                Util.readMode = false;
                MENU.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_pin_drop_white_24px));
            }
            else {
                Util.readMode = true;
                MENU.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_pin_drop_black_24px));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void startAddArtefactActivity() {
        Intent intent = new Intent(getActivity(), AddArtefactActivity.class);
        intent.putExtra(Util.PARAM_USER_ROLE, UserRole.USER);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (this.googleMap != null) {
            zoomToUser();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setBuildingsEnabled(false);
        googleMap.setTrafficEnabled(false);
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.google_map_style));
        if (!sharedPref.getBoolean(Util.PREF_KEY_ZOOM_TO_CURRENT_LOCATION, false)) {
            zoomToUser();
        }
        loadMapMarkings();
        addMarkerListener();
        //placeArtefacts();
        RESTVolleyHandler rest = new RESTVolleyHandler(getContext());
        rest.setRestJSONArrayResponseCallbackHandler(getContext());
        rest.loadAllArtefacts();
        rest.loadAllCategories();
        loadSettlements();
    }

    /**
     * Localize the user's position and zoom on map to location.
     */
    private void localizeAndZoomToUser() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            MapActivity mapActivity = (MapActivity) getActivity();
            mapActivity.localize();
            Location location = locationService.getUserLocation();
            if (location == null) {
                Toast.makeText(mapActivity, "Gerät konnte nicht lokalisiert werden. Aktivieren Sie bitte ihre Standortermittlung" +
                        " oder überprüfen Sie die Signalqualität.", Toast.LENGTH_SHORT);
            } else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),
                        16));
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(17), 1500, null);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(Util.PREF_KEY_ZOOM_TO_CURRENT_LOCATION, true);
                editor.commit();
            }
        }
    }

    /**
     * Zoom to location of shortly created artefact or user location.
     */
    private void zoomToUser() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && (googleMap!=null)) {
            Location location = null;
            Location lastCreatedArtefactLocation = locationService.getLastCreatedArtefactLocation();
            Location lastUserLocation = locationService.getUserLocation();
            MapActivity mapActivity = (MapActivity) getActivity();
            // location priority 1. last created artefact; 2. last known user location; 3. default hardcoded location.
            if (locationService.isLocationValid(lastCreatedArtefactLocation)) {
                location = lastCreatedArtefactLocation;
//                locationService.setLastCreatedArtefactLocation(null);
            } else if (locationService.isLocationValid(lastUserLocation)) {
                location = lastUserLocation;
            } else if (!locationService.isLocationValid(lastUserLocation)) {
                mapActivity.localize();
                location = locationService.getUserLocation();
            }
            if (Util.readMode){
                location = locationService.getLastVisitedArtefactLocation();
            }
            if (location == null) {
                location = locationService.getDefaultLocation();
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),
                    15));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(16), 1000, null);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(Util.PREF_KEY_ZOOM_TO_CURRENT_LOCATION, true);
            //consider using apply instead of commit
            editor.commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (!sharedPref.getBoolean(Util.PREF_KEY_ZOOM_TO_CURRENT_LOCATION, false)) {
                            zoomToUser();
                        }
                    }
                }
            }
        }
    }

    private void loadSettlements() {
        settlements = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(loadJSONFromAsset());
            for (int i = 0; i <= jsonArray.length(); i++) {
                JSONObject jo = (JSONObject) jsonArray.get(i);

                settlements.add(googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble((String) jo.get("longitude")), Double.parseDouble((String) jo.get("latitude"))))
                        .title((String) jo.get("title"))
                        .icon(BitmapDescriptorFactory.fromResource(R.raw.settlement_small))
                        .visible(false)));

                googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        Log.d("settlements", String.valueOf(googleMap.getCameraPosition().zoom));
                        if (googleMap.getCameraPosition().zoom >= 9.0) {
                            Log.d("settlements", "Markers are visible");
                            for (Marker m : settlements) {
                                m.setVisible(true);
                            }
                        } else if (googleMap.getCameraPosition().zoom < 9.0) {
                            for (Marker m : settlements) {
                                m.setVisible(false);
                            }
                        }
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String loadJSONFromAsset() {
        String result = null;
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.settlements);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            result = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    public void loadMapMarkings() {
        try {
            KmlLayer romanRoads = new KmlLayer(googleMap, R.raw.provinces, getActivity().getApplicationContext());
            romanRoads.addLayerToMap();
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    public void placeArtefacts(List<Artefact> list) {
        Log.d("placeArtefacts", "placeArtefacts started");
        Log.d("placeArtefacts", "listSize: " + list.size());
        for (Artefact artefact : list) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(artefact.getLatitude(), artefact.getLongitude())).title(artefact.getName());

            //TODO
            //Icons from flaticon.com. They have to be mentioned in the app.

            switch (artefact.getCategory().getId()) {
                case 1:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_basilica));
                    break;
                case 2:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_bogen));
                    break;
                case 3:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_christentum));
                    break;
                case 4:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_grabstaette));
                    break;
                case 5:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_grundungsmythos));
                    break;
                case 6:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_infrastruktur));
                    break;
                case 7:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_kultstaette));
                    break;
                case 8:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_platzanlage));
                    break;
                case 9:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_politische_institution));
                    break;
                case 10:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_spielstaette));
                    break;
                case 11:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_therme));
                    break;
                case 12:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_wohnkomplex));
                    break;
            }
            artefactsMap.put(googleMap.addMarker(markerOptions), artefact);
        }
    }

    public void addMarkerListener() {
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (artefactsMap.get(marker) != null)
                    startDetailActivity(marker);
                return true;
            }
        });
    }

    public void startDetailActivity(Marker marker) {
        Intent intent = new Intent(getActivity(), ArtefactDetailActivity.class);
        intent.putExtra(Util.INTENT_ARTEFACT, artefactsMap.get(marker));
        startActivity(intent);
    }

    @Override
    public void onResponse(JSONArray response, int responseCode) {
        if (response != null && responseCode == Util.RESPONSE_LOAD_ALL_ARTEFACTS) {
            List<Artefact> list = new ArrayList<>();
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject o = response.getJSONObject(i);
                    Artefact artefact = new Artefact(
                            o.getInt(Util.JSON_ARTEFACT_ID),
                            o.getString(Util.JSON_ARTEFACT_NAME),
                            o.getString(Util.JSON_ARTEFACT_DATING),
                            o.getDouble(Util.JSON_ARTEFACT_LONGITUDE),
                            o.getDouble(Util.JSON_ARTEFACT_LATITUDE),
                            o.getJSONObject(Util.JSON_ARTEFACT_CATEGORY).getInt(Util.JSON_ARTEFACT_CATEGORY_ID)
                    );
                    list.add(artefact);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            placeArtefacts(list);
        }

        if (response != null && responseCode == Util.RESPONSE_LOAD_ALL_CATEGORIES) {
            ArrayList<String> rawCategories = new ArrayList<>();
            rawCategories.add(0,"Alle");
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject o = response.getJSONObject(i);
                    rawCategories.add(i+1, o.getString(Util.JSON_ARTEFACT_CATEGORY_NAME));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            spinnerCategory.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, rawCategories));
        }


    }

    @Override
    public void onErrorResponse(VolleyError error, int responseCode) {
        Log.e(TAG, "volley error=" + error.toString());
    }
}
