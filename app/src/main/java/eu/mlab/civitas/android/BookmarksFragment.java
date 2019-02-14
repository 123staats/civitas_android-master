package eu.mlab.civitas.android;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import eu.mlab.civitas.android.R;
import eu.mlab.civitas.android.rest.RESTVolleyHandler;
import eu.mlab.civitas.android.model.Artefact;


/**
 * Created by Christoph on 20.12.2016.
 */

public class BookmarksFragment extends Fragment {
    private ListView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public static final int USERID = 1;
    private static final String URL = "http://134.100.14.92:8080/eu.mlab.civitas.android/items/favorites/";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_artefact_element_overview, container, false);

        recyclerView = (ListView) rootView.findViewById(R.id.recycler_view_bookmarks);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 1);
        /*recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());*/

        addEmptyCard();

        getFavourites();

        return rootView;
    }

    /*public void loadFavorites(List<ArtefactImage> list){
        this.recyclerView.setAdapter(((MapActivity) getActivity()).getImageAdapter(list));
    }*/

    public void addEmptyCard() {
        //((MapActivity) getActivity()).getArtefactImageList().add(new ArtefactImage());
//        List<ArtefactImage> list = new ArrayList<>();
//        list.add(new ArtefactImage());
//        ((MapActivity) getActivity()).getImageAdapter(list).notifyDataSetChanged();
    }

    public void addCard(List<Artefact> list){
        //((MapActivity) getActivity()).getImageAdapter(list).notifyDataSetChanged();
    }

    private void getFavourites() {
        RESTVolleyHandler m = new RESTVolleyHandler(getContext());
        m.loadAllArtefactByUserID(USERID, this);


    /*RequestQueue queue = Volley.newRequestQueue(getActivity());
    // Request a string response from the provided URL.
    StringRequest stringRequest = new StringRequest(Request.Method.GET, URL + USERID,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Toast.makeText(getContext(), response, Toast.LENGTH_LONG);
                }
            },
        new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.toString(), LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
               // map.put(USERID, userID);
                return map;
            }
        };
        queue.start();
        queue.add(stringRequest);*/
    }
}

