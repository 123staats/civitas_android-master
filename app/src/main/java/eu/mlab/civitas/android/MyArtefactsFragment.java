package eu.mlab.civitas.android;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import eu.mlab.civitas.android.R;
import eu.mlab.civitas.android.rest.RESTVolleyHandler;
import eu.mlab.civitas.android.model.Artefact;
import eu.mlab.civitas.android.util.Util;

/**
 * Created by Christoph on 20.12.2016.
 */

public class MyArtefactsFragment extends Fragment implements RESTVolleyHandler.RESTJSONArrayResponse {
    public static final String TAG = MyArtefactsFragment.class.getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RESTVolleyHandler rest = new RESTVolleyHandler(getContext());
        rest.setRestJSONArrayResponseCallbackHandler(getContext());
        rest.loadAllArtefacts();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_my_artefacts, container, false);

        return rootView;
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
//                    Log.d(TAG, "artefacts response: " + artefact.toString());
                    list.add(artefact);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error, int responseCode) {
        Log.e(TAG, "volley error=" + error.toString());
        if (responseCode == Util.RESPONSE_LOAD_ALL_ARTEFACTS) {
        }
    }
}