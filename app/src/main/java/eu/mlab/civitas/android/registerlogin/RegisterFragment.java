package eu.mlab.civitas.android.registerlogin;

/**
 * Created by liisa_000 on 29/01/2017.
 * Updated by Christoph Stanik on 15/03/2017
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import eu.mlab.civitas.android.MapActivity;
import eu.mlab.civitas.android.R;
import eu.mlab.civitas.android.rest.RESTVolleyHandler;
import eu.mlab.civitas.android.model.UserRole;
import eu.mlab.civitas.android.util.Util;

public class RegisterFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener, RESTVolleyHandler.RESTStringResponse {
    public static final String TAG = RegisterFragment.class.getSimpleName();

    private Button registerButton;
    private EditText editTextRegisterFirstName;
    private EditText editTextRegisterLastName;
    private EditText editTextRegisterEmail;
    private EditText editTextRegisterPassword;

    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editor = getActivity().getSharedPreferences(Util.PREF_KEY_FILE, Context.MODE_PRIVATE).edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_register, container, false);

        editTextRegisterFirstName = (EditText) rootView.findViewById(R.id.edit_firstname);
        editTextRegisterLastName = (EditText) rootView.findViewById(R.id.edit_lastname);
        editTextRegisterEmail = (EditText) rootView.findViewById(R.id.edit_email);
        editTextRegisterPassword = (EditText) rootView.findViewById(R.id.edit_password);
        registerButton = (Button) rootView.findViewById(R.id.button_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                registerUser();
            }
        });

        return rootView;
    }

    private void registerUser(){
        final String firstName = editTextRegisterFirstName.getText().toString().trim();
        final String lastName = editTextRegisterLastName.getText().toString().trim();
        final String email = editTextRegisterEmail.getText().toString().trim();
        final String password = editTextRegisterPassword.getText().toString().trim();
        if (firstName.isEmpty()||lastName.isEmpty()||email.isEmpty()||password.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.toast_fill_out_form), Toast.LENGTH_LONG).show();
            return;
        }
        RESTVolleyHandler rest = new RESTVolleyHandler(getContext());
        rest.setRestStringResponseCallbackHandler(getContext());
        rest.registerUser(firstName,lastName,email,password);
    }

    private void openMapView(UserRole userRole) {
        String emailRegister = editTextRegisterEmail.getText().toString().trim().toLowerCase();
        String firstName = editTextRegisterFirstName.getText().toString().trim().toLowerCase();
        String lastName = editTextRegisterLastName.getText().toString().trim().toLowerCase();

        SharedPreferences sharedPref = getActivity().getSharedPreferences(Util.PREF_KEY_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Util.PREF_USER_MAIL, emailRegister);
        editor.putString(Util.PREF_USER_FIRST_NAME, firstName);
        editor.putString(Util.PREF_USER_LAST_NAME, lastName);
        editor.commit();

        Intent intent = new Intent(getActivity(), MapActivity.class);
        intent.putExtra(Util.PARAM_USER_ROLE, userRole);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.getErrorCode() == ConnectionResult.NETWORK_ERROR){
            Toast.makeText(getContext(), getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResponse(String response, int responseCode) {
        if (response != null && responseCode == Util.RESPONSE_REGISTER_USER) {
            Log.d(TAG, "onResponse=" + response);
            try {
                JSONObject j = new JSONObject(response);
                editor.putString(Util.PREF_USER_MAIL, j.getString(Util.JSON_EMAIL));
                editor.putString(Util.PREF_USER_ID, j.getString(Util.JSON_USER_ID));
                editor.putString(Util.PREF_USER_FIRST_NAME, j.getString(Util.JSON_FIRST_NAME));
                editor.putString(Util.PREF_USER_LAST_NAME, j.getString(Util.JSON_LAST_NAME));
                editor.commit();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Toast.makeText(getContext(), getString(R.string.toast_logged_in), Toast.LENGTH_SHORT).show();
            openMapView(UserRole.USER);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error, int responseCode) {
        Log.e(TAG, "volley error=" + error.toString());
            Toast.makeText(getContext(),getString(R.string.toast_register_error),Toast.LENGTH_LONG).show();
    }
}