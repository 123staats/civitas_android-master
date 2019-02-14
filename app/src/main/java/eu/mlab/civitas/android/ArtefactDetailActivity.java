package eu.mlab.civitas.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import eu.mlab.civitas.android.R;
import eu.mlab.civitas.android.addartefact.AddArtefactActivity;
import eu.mlab.civitas.android.model.UserRole;
import eu.mlab.civitas.android.rest.RESTVolleyHandler;
import eu.mlab.civitas.android.model.Artefact;
import eu.mlab.civitas.android.model.ArtefactItem;
import eu.mlab.civitas.android.util.AudioHandler;
import eu.mlab.civitas.android.util.AudioHandler.AudioCompleted;
import eu.mlab.civitas.android.util.BitmapHandler;
import eu.mlab.civitas.android.util.LocationService;
import eu.mlab.civitas.android.util.Util;

import static android.support.v4.content.FileProvider.getUriForFile;
import static eu.mlab.civitas.android.rest.RESTVolleyHandler.RESTAudioResponse;
import static eu.mlab.civitas.android.rest.RESTVolleyHandler.RESTBitmapResponse;
import static eu.mlab.civitas.android.rest.RESTVolleyHandler.RESTJSONArrayResponse;

public class ArtefactDetailActivity extends AppCompatActivity implements RESTJSONArrayResponse, RESTBitmapResponse, RESTAudioResponse, AudioCompleted {
    private static final String TAG = ArtefactDetailActivity.class.getSimpleName();

    private ImageView artefactItemImageView;
    private Button previousArtefactItemButton;
    private Button nextArtefactItemButton;
    private ImageButton buttonPlayAudio;
    private ImageButton buttonStopAudio;
    private ImageButton editArtefactItemButton;
    private ImageButton deleteArtefactItemButton;
    private TextView artefactItemDescription;
    private LinearLayout audioLayoutGroup;
    private boolean artefactIsBookmarked = false;

    private int currentArtefactItem;

    private Artefact artefact;
    private AudioHandler audioHandler;

    private RESTVolleyHandler rest;
    private int playerStatus = 0; //2 playing, 1 paused, 0 stopped
    private LocationService locationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationService = LocationService.getInstance();
        setContentView(R.layout.activity_artefact_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_artefact_detail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false); // back button is broken, see stack documentation

        artefact = getIntent().getExtras().getParcelable(Util.INTENT_ARTEFACT);
        Location lastVisitedArtefactLocation = new Location(LocationManager.PASSIVE_PROVIDER);
        lastVisitedArtefactLocation.setLatitude(artefact.getLatitude());
        lastVisitedArtefactLocation.setLongitude(artefact.getLongitude());
        lastVisitedArtefactLocation.setTime(Calendar.getInstance().getTimeInMillis());
        locationService.setLastVisitedArtefactLocation(lastVisitedArtefactLocation);
        currentArtefactItem = 0;

        rest = new RESTVolleyHandler(this);
        rest.setRestJSONArrayResponseCallbackHandler(this);
        rest.setRestBitmapResponseCallbackHandler(this);
        rest.setRestAudioResponseCallbackHandler(this);
        rest.setRestStringResponseCallbackHandler(this);
        rest.loadAllArtefactItemsByArtefactID(artefact.getId());

        audioLayoutGroup = (LinearLayout) findViewById(R.id.holder_added_audio);
        artefactItemImageView = (ImageView) findViewById(R.id.artefact_image);
        previousArtefactItemButton = (Button) findViewById(R.id.button_previous);
        nextArtefactItemButton = (Button) findViewById(R.id.button_next);
        editArtefactItemButton = (ImageButton) findViewById(R.id.button_edit_artefact);
        deleteArtefactItemButton = (ImageButton) findViewById(R.id.button_delete_artefact);
        buttonPlayAudio = (ImageButton) findViewById(R.id.button_play_audio);
        buttonStopAudio = (ImageButton) findViewById(R.id.button_stop_audio);
        artefactItemDescription = (TextView) findViewById(R.id.description);
        artefactItemDescription.setMovementMethod(new ScrollingMovementMethod());

        audioHandler = new AudioHandler();
        audioHandler.setAudioCompletedCallbackHandler(this);

        previousArtefactItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPreviousImage();
            }
        });

        nextArtefactItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNextImage();
            }
        });

        buttonPlayAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playerStatus == 0 && getCurrentArtefactItem().getAudioPath() != null) {
                    playAudio();
                }
                else if (playerStatus == 1 && getCurrentArtefactItem().getAudioPath() != null){
                    resumeAudio();
                }
                else if (playerStatus == 2 && getCurrentArtefactItem().getAudioPath() != null){
                    pauseAudio();
                }
            }
        });

        editArtefactItemButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(TAG, "editArtefactItemButton");
                Intent intent = new Intent(ArtefactDetailActivity.this, AddArtefactActivity.class);
                intent.putExtra(Util.INTENT_ARTEFACT, artefact);
                startActivity(intent);
                //add return to main screen and remove marker
            }
        });

        deleteArtefactItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "deleteArtefactItemButton");
                rest.deleteArtefactItems(artefact);
            }
        });

        buttonStopAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getCurrentArtefactItem().getAudioPath() != null)
                    stopAudio();
            }
        });

        artefactItemImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageInFullScreen();
            }
        });

        updateNavigationButtons();
    }

    public boolean fillArtefactItemImageViewIfExists(String imageName, int index) {
        artefact.getArtefactItems().get(index).setImagePath(imageName);

        File image = new File(new File(getFilesDir(), Util.FILE_DIR_IMAGES), imageName);
        if (image.exists()) {
//            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
            Bitmap bitmap = null; // BitmapHandler.decodeFile(imageName, this);
            try {
                bitmap = BitmapHandler.handleSamplingAndRotationBitmap(this, image.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (bitmap != null) {
                artefactItemImageView.setImageBitmap(bitmap);
                return true;
            }
        }
        return false;
    }

    public boolean fillArtefactItemAudioIfExists(String audioName, int index) {
        artefact.getArtefactItems().get(index).setAudioPath(audioName);
        File audio = new File(new File(getFilesDir(), Util.FILE_DIR_AUDIO), audioName);
        if (audio.exists()) {
            audioLayoutGroup.setVisibility(View.VISIBLE);
            return true;
        }
        audioLayoutGroup.setVisibility(View.GONE);
        return false;
    }

    public void downloadArtefactItemsMedia() {
        String imageName = getCurrentArtefactItem().getImageName();
        String audioName = getCurrentArtefactItem().getAudioName();
        if (!imageName.isEmpty()) {
            if (!fillArtefactItemImageViewIfExists(imageName, currentArtefactItem)) {
                rest.downloadImageFile(imageName);
                Log.d(TAG, "download image from server");
            }
        }

        if (audioName.isEmpty()) {
            audioLayoutGroup.setVisibility(View.GONE);
        } else {
            audioLayoutGroup.setVisibility(View.GONE);
            rest.downloadAudioFile(audioName);
            File audio = new File(new File(getFilesDir(), Util.FILE_DIR_AUDIO), audioName);
            if (audio.exists()) {
                audioLayoutGroup.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(artefact.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.add_bookmark, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        switch (id) {
//            case R.id.add_bookmark:
//                if (artefactIsBookmarked) {
//                    artefactIsBookmarked = false;
//                    item.setIcon(R.drawable.ic_add_bookmark);
//                }
//                else {
//                    artefactIsBookmarked = true;
//                    item.setIcon(R.drawable.ic_bookmark_added);
//                }
//                break;
//        }

        return super.onOptionsItemSelected(item);
    }

    public void updateNavigationButtons() {
        if (artefact.getArtefactItems() == null) {
            nextArtefactItemButton.setEnabled(false);
            previousArtefactItemButton.setEnabled(false);
            return;
        }
        if (currentArtefactItem == 0) {
            previousArtefactItemButton.setEnabled(false);
            if (artefact.getArtefactItems().size() > 1) {
                nextArtefactItemButton.setEnabled(true);
            }
        }
        if (currentArtefactItem == artefact.getArtefactItems().size() - 1) {
            nextArtefactItemButton.setEnabled(false);
            previousArtefactItemButton.setEnabled(true);
        }
        if (currentArtefactItem < artefact.getArtefactItems().size() - 1 &&
                currentArtefactItem > 0) {
            previousArtefactItemButton.setEnabled(true);
            nextArtefactItemButton.setEnabled(true);
        }
        if (artefact.getArtefactItems().size() == 1) {
            previousArtefactItemButton.setEnabled(false);
        }
    }

    public void showPreviousImage() {
        // do not allow to go to the previous page if there is no
        if (currentArtefactItem == 0) {
            updateNavigationButtons();
            return;
        }
        currentArtefactItem--;
        updateNavigationButtons();
        audioLayoutGroup.setVisibility(View.GONE);

        stopAudio();
        downloadArtefactItemsMedia();
        artefactItemDescription.setText(getCurrentArtefactItem().getDescription());
        artefactItemDescription.scrollTo(0, 0);
    }

    public void showNextImage() {
        // do not allow to go further than the number of items we have to show
        if (artefact.getArtefactItems() == null) {
            return;
        }
        if (currentArtefactItem == artefact.getArtefactItems().size() - 1) {
            updateNavigationButtons();
            return;
        }
        currentArtefactItem++;
        updateNavigationButtons();
        audioLayoutGroup.setVisibility(View.GONE);

        stopAudio();
        downloadArtefactItemsMedia();
        artefactItemDescription.setText(getCurrentArtefactItem().getDescription());
        artefactItemDescription.scrollTo(0, 0);
    }


    public void resumeAudio() {
        String audioName = getCurrentArtefactItem().getAudioPath();
        File audio = new File(new File(getFilesDir(), Util.FILE_DIR_AUDIO), audioName);
        if (audio.exists()) {
            audioLayoutGroup.setVisibility(View.VISIBLE);
            playerStatus = audioHandler.onPlay(Util.AUDIO_PLAYER_PLAY, audio.getAbsolutePath());
            if (playerStatus == 2) {
                buttonPlayAudio.setImageResource(R.drawable.ic_pause_black_24px);
            } else {
                buttonPlayAudio.setImageResource(R.drawable.ic_play_arrow_black_24px);
            }
        }
    }
    public void playAudio() {
        String audioName = getCurrentArtefactItem().getAudioPath();
        File audio = new File(new File(getFilesDir(), Util.FILE_DIR_AUDIO), audioName);
        if (audio.exists()) {
            audioLayoutGroup.setVisibility(View.VISIBLE);
            playerStatus = audioHandler.onPlay(Util.AUDIO_PLAYER_PLAY, audio.getAbsolutePath());
            if (playerStatus == 2) {
                buttonPlayAudio.setImageResource(R.drawable.ic_pause_black_24px);
            } else {
                buttonPlayAudio.setImageResource(R.drawable.ic_play_arrow_black_24px);
            }
        }
    }

    public void pauseAudio() {
        String audioName = getCurrentArtefactItem().getAudioPath();
        File audio = new File(new File(getFilesDir(), Util.FILE_DIR_AUDIO), audioName);

        if (audio.exists()) {
            audioLayoutGroup.setVisibility(View.VISIBLE);
            playerStatus = audioHandler.onPlay(Util.AUDIO_PLAYER_PAUSE, audio.getAbsolutePath());
            buttonPlayAudio.setImageResource(R.drawable.ic_play_arrow_black_24px);
        }
    }

    public void stopAudio() {
        String audioName = getCurrentArtefactItem().getAudioPath();
        File audio = new File(new File(getFilesDir(), Util.FILE_DIR_AUDIO), audioName);
        if (audio.exists()) {
            audioLayoutGroup.setVisibility(View.VISIBLE);
            playerStatus = audioHandler.onPlay(Util.AUDIO_PLAYER_STOP, audio.getAbsolutePath());
            buttonPlayAudio.setImageResource(R.drawable.ic_play_arrow_black_24px);
        }
    }

    public void showImageInFullScreen() {
        String imageName = getCurrentArtefactItem().getImagePath();
        File image = new File(new File(getFilesDir(), Util.FILE_DIR_IMAGES), imageName);
        if (image.exists()) {
            Uri imageUri = getUriForFile(this, Util.PROVIDER, new File(image.getAbsolutePath()));
            String author = getCurrentArtefactItem().getAuthor();
            String license = getCurrentArtefactItem().getImageLicense();
            String licenseLink = getCurrentArtefactItem().getImageLicenseLink();

            Intent intent = new Intent(this, ImageFullscreenActivity.class);
            intent.putExtra(Util.INTENT_IMAGE_PATH, image.getAbsolutePath());
            intent.putExtra(Util.INTENT_IMAGE_NAME, image.getAbsolutePath());
            intent.putExtra(Util.INTENT_IMAGE_URI, imageUri.toString());
            intent.putExtra(Util.INTENT_IMAGE_AUTHOR, author);
            intent.putExtra(Util.INTENT_IMAGE_LICENSE, license);
            intent.putExtra(Util.INTENT_IMAGE_LICENSE_LINK, licenseLink);
            startActivity(intent);
        }
    }

    @Override
    public void onResponse(JSONArray response, int responseCode) {
        if (response != null && responseCode == Util.RESPONSE_LOAD_ARTEFACT_ITEMS) {
            Log.d(TAG, "number of attached artefact items: " + response.length());
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject j = response.getJSONObject(i);
                    Log.e(TAG, "response=" + j.toString());
                    artefact.addArtefactItem(new ArtefactItem(
                            j.getString(Util.JSON_ARTEFACT_ITEM_IMAGE_NAME),
                            j.getString(Util.JSON_ARTEFACT_ITEM_AUDIO_NAME),
                            j.getString(Util.JSON_ARTEFACT_ITEM_DESCRIPTION),
                            j.getString(Util.JSON_ARTEFACT_ITEM_LICENSE_TYPE),
                            j.getString(Util.JSON_ARTEFACT_ITEM_LICENSE_LINK),
                            j.getString(Util.JSON_ARTEFACT_ITEM_AUTHOR))
                    );
                    downloadArtefactItemsMedia();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            artefactItemDescription.setText(getCurrentArtefactItem().getDescription());
            updateNavigationButtons();
        }
    }

    public ArtefactItem getCurrentArtefactItem() {
        try {
            return artefact.getArtefactItems().get(currentArtefactItem);
        } catch (NullPointerException e) {
            Toast.makeText(this, "there are no artefact items", Toast.LENGTH_SHORT).show();
            finish();
            return new ArtefactItem();
        }
    }

    @Override
    public void onResponse(Bitmap response, int responseCode, String imageName) {
        if (response != null && responseCode == Util.RESPONSE_LOAD_ARTEFACT_IMAGE) {
            File image = new File(new File(getFilesDir(), Util.FILE_DIR_IMAGES), imageName);

            FileOutputStream fo;
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            response.compress(Bitmap.CompressFormat.JPEG, 50, bytes);

            try {
                fo = new FileOutputStream(image);
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fillArtefactItemImageViewIfExists(imageName, currentArtefactItem);
        }
    }

    @Override
    public void onResponse(byte[] response, int responseCode, String audioName) {
        if (response != null && responseCode == Util.RESPONSE_LOAD_ARTEFACT_AUDIO) {
            try {
                File outputFile = new File(new File(getFilesDir(), Util.FILE_DIR_AUDIO), audioName);
                FileOutputStream outputStream;
                try {
                    outputStream = new FileOutputStream(outputFile);
                    outputStream.write(response);
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                fillArtefactItemAudioIfExists(audioName, currentArtefactItem);
            } catch (Exception e) {
                Log.d(TAG, "could not load the audio file to disk");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error, int responseCode) {
        if (responseCode == Util.RESPONSE_LOAD_ARTEFACT_ITEMS) {
            Log.e(TAG, "volley error=" + error.toString());
        } else if (responseCode == Util.RESPONSE_LOAD_ARTEFACT_IMAGE) {
            Log.e(TAG, "volley error=" + error.toString());
        } else if (responseCode == Util.RESPONSE_LOAD_ARTEFACT_AUDIO) {
            Log.e(TAG, "volley error=" + error.toString());
        }
        updateNavigationButtons();
    }

    @Override
    protected void onPause() {
        super.onPause();
        audioHandler.onPlay(Util.AUDIO_PLAYER_PAUSE, null);
    }

    @Override
    protected void onDestroy() {
        audioHandler.onPlay(Util.AUDIO_PLAYER_STOP, null);
        super.onDestroy();
    }

    @Override
    public void onAudioCompleted() {
        stopAudio();
    }
}