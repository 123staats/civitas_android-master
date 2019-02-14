package eu.mlab.civitas.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import eu.mlab.civitas.android.R;

/**
 * Updated by Christoph Stanik on 08/03/2017
 */

public class ImpressumActivity extends AppCompatActivity {
    public static final String TAG = ImpressumActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impressum);
    }
}
