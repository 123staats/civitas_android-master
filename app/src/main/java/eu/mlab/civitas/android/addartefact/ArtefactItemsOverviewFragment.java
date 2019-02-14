package eu.mlab.civitas.android.addartefact;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import eu.mlab.civitas.android.R;
import eu.mlab.civitas.android.util.Util;

public class ArtefactItemsOverviewFragment extends Fragment {
    private RecyclerView recyclerView;
    private FloatingActionButton buttonAddArtefactElement;
    private TextView textInfoNoItemsAdded;
    private ArtefactItemAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_artefact_element_overview, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        textInfoNoItemsAdded = (TextView) rootView.findViewById(R.id.text_info_add_artefact_entry);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 1);
        buttonAddArtefactElement = (FloatingActionButton) rootView.findViewById(R.id.button_add_artefact_element);
        buttonAddArtefactElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreateArtefactItemActivity.class);
                intent.putExtra(Util.INTENT_REQUEST_CODE, Util.ADD_ARTEFACT_ELEMENT_REQUEST);
                getActivity().startActivityForResult(intent, Util.ADD_ARTEFACT_ELEMENT_REQUEST);
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = ((AddArtefactActivity) getActivity()).getArtefactItemAdapter();

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter.getItemCount() > 0 ) {
            textInfoNoItemsAdded.setVisibility(View.GONE);
        }
        else {
            textInfoNoItemsAdded.setVisibility(View.VISIBLE);
        }
    }
}