package eu.mlab.civitas.android;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;

import eu.mlab.civitas.android.R;
import eu.mlab.civitas.android.model.SupportedLicenses;
import eu.mlab.civitas.android.util.BitmapHandler;
import eu.mlab.civitas.android.util.Util;

public class ImageFullscreenActivity extends AppCompatActivity {
    private LinearLayout holderLicenseLayout;
    private TextView authorTextView;
    private TextView licenseTextView;
    private TextView licenseLinkTextView;
    private String author;
    private String license;
    private String licenseLink;
    private String imageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        Bundle extras = getIntent().getExtras();
        imageName = extras.getString(Util.INTENT_IMAGE_NAME);
        String imagePath = extras.getString(Util.INTENT_IMAGE_PATH);
        Uri imageUri =  Uri.parse(extras.getString(Util.INTENT_IMAGE_URI));

        author = extras.getString(Util.INTENT_IMAGE_AUTHOR);
        license = extras.getString(Util.INTENT_IMAGE_LICENSE);
        licenseLink = extras.getString(Util.INTENT_IMAGE_LICENSE_LINK);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.buttonInfo);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfo();
            }
        });

        holderLicenseLayout = (LinearLayout) findViewById(R.id.holder_license);
        authorTextView = (TextView) findViewById(R.id.text_author);
        licenseTextView = (TextView) findViewById(R.id.text_info);
        licenseLinkTextView = (TextView) findViewById(R.id.text_license_link);


        String authorInfoText = getString(R.string.text_author_info);
        authorTextView.setText(authorInfoText + " " + author);
        String licenseInfoText = getString(R.string.text_license_info);
        licenseTextView.setText(licenseInfoText + " " + license);

        // try to retrieve a cc license link, if no link is given
        if (!license.isEmpty() && (license.contains("CC") && license.contains("3.0"))) {
            if (SupportedLicenses.licenseLinkMap.containsKey(license)) {
                licenseLink = SupportedLicenses.licenseLinkMap.get(license);
            }
        }
        licenseLinkTextView.setText(licenseLink);

        ImageView imageView = (ImageView) findViewById(R.id.image_view);
        Bitmap bitmap;
        try {
            bitmap = BitmapHandler.handleSamplingAndRotationBitmap(this, imagePath);
        } catch (IOException e) {
            // could not handle the rotation
            bitmap = null;
            e.printStackTrace();
            Log.e("TAGTAGTAG", "io exception + imgUri="+imageUri+" imagePath="+imagePath);
        } catch (NullPointerException e) {
            Log.e("TAGTAGTAG", "nullpointer exception");
            bitmap = null;
        }

        if (bitmap == null) {
            bitmap = BitmapHandler.decodeFile(imagePath, this);
        }
        imageView.setImageBitmap(bitmap);
    }

    public void showInfo() {
        if(holderLicenseLayout.getVisibility() == View.VISIBLE)
            holderLicenseLayout.setVisibility(View.INVISIBLE);
        else
            holderLicenseLayout.setVisibility(View.VISIBLE);
    }
}