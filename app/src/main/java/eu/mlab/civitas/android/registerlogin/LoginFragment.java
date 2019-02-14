package eu.mlab.civitas.android.registerlogin;

/**
 * Created by liisa_000 on 29/01/2017.
 * Updated by Maksud Ganapijev on 10/05/2018
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
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import eu.mlab.civitas.android.MapActivity;
import eu.mlab.civitas.android.R;
import eu.mlab.civitas.android.rest.RESTVolleyHandler;
import eu.mlab.civitas.android.model.UserRole;
import eu.mlab.civitas.android.util.Util;

import static android.widget.Toast.LENGTH_LONG;

public class LoginFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener, RESTVolleyHandler.RESTStringResponse, View.OnClickListener {
    private static final String TAG = LoginFragment.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editor = getActivity().getSharedPreferences(Util.PREF_KEY_FILE, Context.MODE_PRIVATE).edit();
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        Log.d(TAG, "onCreate: testcommit");
        Log.d(TAG, "onCreate: kasndkja ");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        SignInButton signInButton = (SignInButton) rootView.findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_ICON_ONLY);

        rootView.findViewById(R.id.sign_in_button).setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this.getContext());
    }

    private void loginUser() {
        final String emailLogin = ""; // = editTextEmail.getText().toString().trim();
        final String passwordLogin = ""; // = editTextPassword.getText().toString().trim();
        if (emailLogin.isEmpty() || passwordLogin.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.toast_fill_out_form), Toast.LENGTH_LONG).show();
            return;
        }
        // send login request
        RESTVolleyHandler rest = new RESTVolleyHandler(getContext());
        rest.setRestStringResponseCallbackHandler(getContext());
        rest.login(emailLogin, passwordLogin);
    }

    private void openMapView(String userEmail, String userRole) {
        Intent intent = new Intent(getActivity(), MapActivity.class);
        intent.putExtra(Util.PARAM_USER_EMAIL, userEmail);
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
        if (response != null) {
            if(responseCode == Util.RESPONSE_REGISTER_USER) {
                Log.d(TAG, "onResponse=" + response);
                try {
                    JSONObject j = new JSONObject(response);
                    editor.putString(Util.PREF_USER_MAIL, j.getString(Util.JSON_EMAIL));
                    editor.putString(Util.PREF_USER_ID, j.getString(Util.JSON_USER_ID));
                    editor.putString(Util.PREF_USER_FIRST_NAME, j.getString(Util.JSON_FIRST_NAME));
                    editor.putString(Util.PREF_USER_LAST_NAME, j.getString(Util.JSON_LAST_NAME));
                    editor.putString(Util.PREF_USER_LAST_NAME, j.getString(Util.JSON_USER_ROLE));
                    editor.commit();
                    Toast.makeText(getContext(), getString(R.string.toast_logged_in), Toast.LENGTH_SHORT).show();
                    openMapView(j.getString(Util.JSON_EMAIL), j.getString(Util.JSON_USER_ROLE));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error, int responseCode) {
        Log.e(TAG, "volley error=" + error.toString());
        //Toast.makeText(getContext(), getString(R.string.toast_login_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            updateUI(account);
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void updateUI(GoogleSignInAccount account){
        if(account != null){
            String userEmail = account.getEmail();
            String firstName = "";
            String lastName = "";
            String[] fullName = account.getDisplayName().split(" ");
            if(fullName.length>1){
                firstName = fullName[0];
                lastName = fullName[1];
            } else {
                firstName = fullName[0];
                lastName = "";
            }
            String userRole = "USER";
            RESTVolleyHandler restHandler = new RESTVolleyHandler(getContext());
            restHandler.setRestStringResponseCallbackHandler(getContext());
            restHandler.registerUser(firstName, lastName, userEmail, userRole);
        }
    }
}