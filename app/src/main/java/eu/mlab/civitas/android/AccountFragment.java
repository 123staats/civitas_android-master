package eu.mlab.civitas.android;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import eu.mlab.civitas.android.R;
import eu.mlab.civitas.android.registerlogin.MainActivity;
import eu.mlab.civitas.android.util.Util;

/**
 * Created by liisa_000 on 29/01/2017.
 * Updated by Christoph Stanik on 07/03/2017
 */

public class AccountFragment extends Fragment {
    private GoogleApiClient mGoogleApiClient;
    private Button logoutButton;
    private TextView emailText;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_account, container, false);

        emailText = (TextView) rootView.findViewById(R.id.text_email_value);

        logoutButton = (Button) rootView.findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logout();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences = getActivity().getSharedPreferences(Util.PREF_KEY_FILE, Context.MODE_PRIVATE);
        String email = sharedPreferences.getString(Util.PREF_USER_MAIL, getString(R.string.text_default_not_logged_in));
        emailText.setText(email);
    }

    private void logout() {
        // reset location camera on the map view to the current location
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(Util.PREF_KEY_FILE, Context.MODE_PRIVATE).edit();
        editor.putBoolean(Util.PREF_KEY_ZOOM_TO_CURRENT_LOCATION, false);
        // reset user mail address if the guest login was chosen
        editor.putString(Util.PREF_USER_MAIL, "");
        editor.commit();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            Intent i = new Intent(getContext(), MainActivity.class);
                            startActivity(i);
                        }
                    });
        } else {
            Intent i = new Intent(getContext(), MainActivity.class);
            startActivity(i);
        }
    }

    @Override
    public void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        super.onStart();
    }
}