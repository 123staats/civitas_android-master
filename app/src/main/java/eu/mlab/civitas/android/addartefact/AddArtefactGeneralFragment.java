package eu.mlab.civitas.android.addartefact;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.mlab.civitas.android.R;
import eu.mlab.civitas.android.model.Artefact;
import eu.mlab.civitas.android.model.Category;
import eu.mlab.civitas.android.rest.RESTVolleyHandler;
import eu.mlab.civitas.android.util.LocationService;
import eu.mlab.civitas.android.util.Util;

public class AddArtefactGeneralFragment extends Fragment implements OnMapReadyCallback, RESTVolleyHandler.RESTJSONArrayResponse {

    public static final String TAG = AddArtefactGeneralFragment.class.getSimpleName();

    private Spinner spinnerCategory;
    private MapView mapView;
    private GoogleMap googleMap;
    private Marker centerMarker;
    private EditText name;
    private Spinner spinnerDating;
    private Location updatedLocation;
    private LocationService locationService;
    private Artefact artefact;

    public void setLocation(Location location) {
        updatedLocation = location;
    }

    public Location getLocation() {
        return updatedLocation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        locationService = LocationService.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_add_artefact_general, container, false);
        name = (EditText) rootView.findViewById(R.id.editTextArtefactName);

        spinnerCategory = (Spinner) rootView.findViewById(R.id.fragment_add_spinner_category);

        ArrayList<String> rawDates = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.time_periods)));
        rawDates.remove(0); // remove the "all" entry
        spinnerDating = (Spinner) rootView.findViewById(R.id.spinnerTimePeriod);
        spinnerDating.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, rawDates));

        mapView = (MapView) rootView.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        if(getArguments() != null) {
            artefact = getArguments().getParcelable(Util.INTENT_ARTEFACT) == null ? null : (Artefact) getArguments().getParcelable(Util.INTENT_ARTEFACT);
        } else {
            artefact = null;
        }

        if(artefact != null){
            name.setText(artefact.getName());
            spinnerCategory.setSelection(getIndexForSpinner(spinnerCategory, artefact.getCategory().getName()));
            spinnerDating.setSelection(getIndexForSpinner(spinnerDating, artefact.getDating()));
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
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

        Location location = locationService.getDefaultLocation();
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = locationService.getUserLocation();
        }
        if (location == null) {
            location = locationService.getDefaultLocation();
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location
                .getLongitude()), 15));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);

        centerMarker = googleMap.addMarker(new MarkerOptions().position(googleMap.getCameraPosition().target));

        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                setCenterMarker();
            }
        });

        RESTVolleyHandler rest = new RESTVolleyHandler(getContext());
        rest.setRestJSONArrayResponseCallbackHandler(getContext());
        rest.loadAllCategories();
    }

    public void setCenterMarker() {
        LatLng centerOfMap = googleMap.getCameraPosition().target;
        centerMarker.setPosition(centerOfMap);
    }

    public Spinner getSpinnerCategory() {
        return spinnerCategory;
    }

    public MapView getMapView() {
        return mapView;
    }

    public Marker getCenterMarker() {
        return centerMarker;
    }

    public EditText getName() {
        return name;
    }

    public Spinner getSpinnerDating() {
        return spinnerDating;
    }

    @Override
    public void onResponse(JSONArray response, int responseCode) {
        if (response != null && responseCode == Util.RESPONSE_LOAD_ALL_CATEGORIES) {
            ArrayList<String> rawCategories = new ArrayList<>();
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject o = response.getJSONObject(i);
                    rawCategories.add(o.getString(Util.JSON_ARTEFACT_CATEGORY_NAME));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (getActivity() != null){
                spinnerCategory.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, rawCategories));
            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error, int responseCode) {
        if (responseCode == Util.RESPONSE_LOAD_ALL_ARTEFACTS) {
            Log.e(TAG, "VolleyError: " + error.toString());
        }
    }

    private int getIndexForSpinner(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }
        return 0;
    }
}