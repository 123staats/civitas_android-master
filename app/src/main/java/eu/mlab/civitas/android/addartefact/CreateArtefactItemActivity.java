package eu.mlab.civitas.android.addartefact;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import eu.mlab.civitas.android.ImageFullscreenActivity;
import eu.mlab.civitas.android.R;
import eu.mlab.civitas.android.model.ArtefactItem;
import eu.mlab.civitas.android.model.SupportedLicenses;
import eu.mlab.civitas.android.util.AudioHandler;
import eu.mlab.civitas.android.util.AudioHandler.AudioCompleted;
import eu.mlab.civitas.android.util.BitmapHandler;
import eu.mlab.civitas.android.util.Util;

import static android.support.v4.content.FileProvider.getUriForFile;

public class CreateArtefactItemActivity extends AppCompatActivity implements AudioCompleted {
    public static final String TAG = CreateArtefactItemActivity.class.getSimpleName();

    private final int PERMISSION_REQUEST = 200;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 3;

    public static final int REQUEST_PICTURE_FROM_GALLERY = 23;
    public static final int REQUEST_PICTURE_FROM_CAMERA = 24;
    public static final int REQUEST_CROP_PICTURE = 25;

    private SharedPreferences sharedPref;

    private File tmpImageFile = null;
    private String tmpImageFilePath = null;
    private Uri tmpImageUri = null;

    private File tmpImageFileFromCrop = null;

    private AudioHandler audioHandler = null;

    private Uri tmpImageUriFromCrop = null;
    private File tmpAudioFile = null;
    private String tmpAudioFilePath = null;
    private Uri tmpAudioUri = null;

    private LinearLayout holderNoImageAddedYet;
    private LinearLayout holderAddedAudio;
    private RelativeLayout holderImageAdded;
    private ImageView artefactItemImage;
    private Button buttonStartRecord;
    private Button buttonStopRecord;
    private ImageButton buttonPlayAudio;

    private Spinner licenseSpinner;

    private EditText editTextDescription;
    private ArtefactItem artefactItem;

    private int playerStatus = 0;

    int position;
    int requestCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_artefact_image_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_artefact_element);
        setSupportActionBar(toolbar);
        try {
            // Don't use the toolbar as the back button has undesired effects.
            // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Log.e(TAG, "the support action bar cannot be accessed");
        }

        sharedPref = getSharedPreferences(Util.PREF_KEY_FILE, Context.MODE_PRIVATE);


        holderNoImageAddedYet = (LinearLayout) findViewById(R.id.holder_no_image_added_yet);
        holderImageAdded = (RelativeLayout) findViewById(R.id.holder_artefact_element_image);
        artefactItemImage = (ImageView) findViewById(R.id.artefact_element_image);

        licenseSpinner = (Spinner) findViewById(R.id.spinner_license);
        licenseSpinner.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, SupportedLicenses.licenseList)
        );

        holderAddedAudio = (LinearLayout) findViewById(R.id.holder_added_audio);
        buttonStartRecord = (Button) findViewById(R.id.button_start_record_audio);
        buttonStopRecord = (Button) findViewById(R.id.button_stop_record_audio);
        buttonPlayAudio = (ImageButton) findViewById(R.id.button_play_audio);
        audioHandler = new AudioHandler();
        audioHandler.setAudioCompletedCallbackHandler(this);

        editTextDescription = (EditText) findViewById(R.id.edit_description);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {"android.permission.CAMERA",
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE"
            };
            requestPermissions(permissions, PERMISSION_REQUEST);
        }

        /*
         * Recover previous artefact item, so that it can be updated
         */
        requestCode = getIntent().getIntExtra(Util.INTENT_REQUEST_CODE, -1);
        if (requestCode == Util.ARTEFACT_ITEM_UPDATE_REQUEST) {
            artefactItem = getIntent().getExtras().getParcelable(Util.INTENT_ARTEFACT_ITEM);
            position = getIntent().getIntExtra(Util.INTENT_ARTEFACT_ITEM_POSITION, -1);
            tmpImageFilePath = artefactItem.getImagePath();
            tmpImageUri = getImageFileUri(tmpImageFilePath);
            tmpAudioFilePath = artefactItem.getAudioPath();
            editTextDescription.setText(artefactItem.getDescription());

            if (tmpAudioFilePath == null || tmpAudioFilePath.isEmpty()) {
                holderAddedAudio.setVisibility(View.GONE);
            }

        } else if (requestCode == Util.ADD_ARTEFACT_ELEMENT_REQUEST) {
            artefactItem = new ArtefactItem();
        } else {
            setResult(-1);
            finish();
        }

        // make the image visible when it could be recovered
        if (tmpImageFilePath != null) {
            displayImage(tmpImageFilePath);
        }

        // make the audio visible when it could be recovered
        if (tmpAudioFilePath != null && !tmpAudioFilePath.isEmpty()) {
            holderAddedAudio.setVisibility(View.VISIBLE);
            buttonStopRecord.setVisibility(View.GONE);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.artefact_element_image:
                showImageInFullScreen();
                break;
            case R.id.button_remove_image:
                removeImage();
                break;
            case R.id.button_expand_image:
                showImageInFullScreen();
                break;
            case R.id.button_add_image_camera:
                if (Build.VERSION.SDK_INT < 23) {
                    takePhotoWithCamera();
                } else {
                    initCameraPermission();
                }
                break;
            case R.id.button_add_image_gallery:
                if (Build.VERSION.SDK_INT < 23) {
                    selectImageFromGallery();
                } else {
                    initGalleryPermission();
                }
                break;
            case R.id.button_license_info:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(SupportedLicenses.LICENSES_EXPLANATION_LINK)));
                break;
            case R.id.button_start_record_audio:
                if (Build.VERSION.SDK_INT < 23) {
                    startAudioRecording();
                } else {
                    initRecordAudioPermission();
                }
                break;
            case R.id.button_stop_record_audio:
                stopAudioRecording();
                break;
            case R.id.button_stop_audio:
                stopAudio();
                break;
            case R.id.button_play_audio:
                playPauseResume();
                break;
            case R.id.button_remove_audio:
                deleteAudioFile();
                break;
            case R.id.button_submit_artefact_element:
                submitArtefactItem();
                break;
        }
    }

    private void deleteAudioFile() {
        holderAddedAudio.setVisibility(View.GONE);
        buttonStartRecord.setVisibility(View.VISIBLE);
        File audioFile = new File(tmpAudioFilePath);
        if (audioFile.exists()) {
            audioFile.delete();
            tmpAudioFilePath = "";
        }
    }

    private void submitArtefactItem() {
        Intent intent = new Intent();
        artefactItem.setImagePath(tmpImageFilePath);
        artefactItem.setImageLicense(licenseSpinner.getSelectedItem().toString());
        artefactItem.setImageLicenseLink(SupportedLicenses.licenseLinkMap.get(licenseSpinner.getSelectedItem().toString()));
        artefactItem.setAudioPath(tmpAudioFilePath == null ? "" : tmpAudioFilePath);
        artefactItem.setDescription(editTextDescription.getText().toString());
        if (!artefactItem.isValid()) {
            Toast.makeText(this, R.string.validation_artefact_item_incomplete, Toast.LENGTH_LONG).show();
            return;
        }

        if (requestCode == Util.ARTEFACT_ITEM_UPDATE_REQUEST) {
            intent.putExtra(Util.INTENT_ARTEFACT_ITEM, artefactItem);
            intent.putExtra(Util.INTENT_ARTEFACT_ITEM_POSITION, position);
        } else {
            String firstName = sharedPref.getString(Util.PREF_USER_FIRST_NAME, "");
            String lastName = sharedPref.getString(Util.PREF_USER_LAST_NAME, "");
            artefactItem.setAuthor(firstName + " " + lastName);
            intent.putExtra(Util.INTENT_ARTEFACT_ITEM, artefactItem);
        }

        int resultCode;
        if (artefactItem.getImagePath() == null || artefactItem.getImagePath().isEmpty() ||
                artefactItem.getDescription() == null || artefactItem.getDescription().isEmpty()) {
            resultCode = Activity.RESULT_CANCELED;
        } else {
            resultCode = Activity.RESULT_OK;
        }

        if (getParent() == null) {
            setResult(resultCode, intent);
        } else {
            getParent().setResult(resultCode, intent);
        }
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(R.string.add_picture);
    }

    public void showImageInFullScreen() {
        String license = licenseSpinner.getSelectedItem().toString();
        Intent intent = new Intent(this, ImageFullscreenActivity.class);
        intent.putExtra(Util.INTENT_IMAGE_PATH, tmpImageFilePath);
        intent.putExtra(Util.INTENT_IMAGE_URI, tmpImageUri.toString());
        intent.putExtra(Util.INTENT_IMAGE_LICENSE, license);
        intent.putExtra(Util.INTENT_IMAGE_LICENSE_LINK, SupportedLicenses.licenseLinkMap.get(license));
        startActivity(intent);
    }

    public void removeImage() {
        if (tmpImageFile != null) {
            tmpImageFile.delete();
        }
        tmpImageFilePath = null;
        tmpImageFile = null;
        tmpImageUri = null;
        artefactItemImage.setImageDrawable(null);
        holderImageAdded.setVisibility(View.GONE);
        holderNoImageAddedYet.setVisibility(View.VISIBLE);
    }

    /*
     * below: permission related code
     */
    private void initRecordAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.RECORD_AUDIO)) {
                    Toast.makeText(this, "Permission to record audio", Toast.LENGTH_SHORT).show();
                }
                String[] permissions = {Manifest.permission.RECORD_AUDIO};
                requestPermissions(permissions, REQUEST_RECORD_AUDIO_PERMISSION);
            } else {
                startAudioRecording();
            }
        }
    }

    private void initCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                    Toast.makeText(this, "Permission to use Camera", Toast.LENGTH_SHORT).show();
                }
                String[] permissions = {"android.permission.CAMERA"};
                requestPermissions(permissions, REQUEST_CAMERA_PERMISSION);
            } else {
                takePhotoWithCamera();
            }
        }
    }

    private void initGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "Permission to read Storage", Toast.LENGTH_SHORT).show();
                }
                String[] permissions = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
                requestPermissions(permissions, REQUEST_STORAGE_PERMISSION);
            } else {
                selectImageFromGallery();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhotoWithCamera();
            } else {
                Toast.makeText(this, "Permission denied by user", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImageFromGallery();
            } else {
                Toast.makeText(this, "Permission denied by user", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startAudioRecording();
            } else {
                Toast.makeText(this, "Permission denied by user", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /*
     * below: audio recording related code
     */
    private void startAudioRecording() {
        buttonStartRecord.setVisibility(View.GONE);
        buttonStopRecord.setVisibility(View.VISIBLE);
        try {
            tmpAudioFile = createAudioFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        audioHandler.onRecord(true, tmpAudioFile.getAbsolutePath());
    }

    private void stopAudioRecording() {
        holderAddedAudio.setVisibility(View.VISIBLE);
        buttonStopRecord.setVisibility(View.GONE);
        audioHandler.onRecord(false, tmpAudioFilePath);
    }

    public void playPauseResume() {
        if (playerStatus == 1) {
            resumeAudio();
        } else if (playerStatus == 2) {
            pauseAudio();
        } else if (playerStatus == 0) {
            playAudio();
        }
    }

    public void resumeAudio() {
        playerStatus = audioHandler.onPlay(Util.AUDIO_PLAYER_PLAY, tmpAudioFilePath);
        if (playerStatus == 2) {
            buttonPlayAudio.setImageResource(R.drawable.ic_pause_black_24px);
        } else {
            buttonPlayAudio.setImageResource(R.drawable.ic_play_arrow_black_24px);
        }
    }

    private void playAudio() {
        playerStatus = audioHandler.onPlay(Util.AUDIO_PLAYER_PLAY, tmpAudioFilePath);
        if (playerStatus == 2) {
            buttonPlayAudio.setImageResource(R.drawable.ic_pause_black_24px);
        } else {
            buttonPlayAudio.setImageResource(R.drawable.ic_play_arrow_black_24px);
        }
    }

    public void pauseAudio() {
        playerStatus = audioHandler.onPlay(Util.AUDIO_PLAYER_PAUSE, tmpAudioFilePath);
        buttonPlayAudio.setImageResource(R.drawable.ic_play_arrow_black_24px);
    }

    private void stopAudio() {
        playerStatus = audioHandler.onPlay(Util.AUDIO_PLAYER_STOP, tmpAudioFilePath);
        buttonPlayAudio.setImageResource(R.drawable.ic_play_arrow_black_24px);
    }

    private File createAudioFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String audioFileName = timeStamp + "_tmp_audio";
        File storageDir = new File(getFilesDir(), Util.FILE_DIR_AUDIO);
        File audio = File.createTempFile(
                audioFileName,  /* prefix */
                ".3gp",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        tmpAudioFilePath = audio.getAbsolutePath();
        return audio;
    }

    /*
     * below: image related code
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_PICTURE_FROM_GALLERY) && (resultCode == Activity.RESULT_OK)) {
            Log.d(TAG, "Image selected from gallery");
            onImageSelectedFromGallery(data.getData());
        } else if ((requestCode == REQUEST_PICTURE_FROM_CAMERA) && (resultCode == Activity.RESULT_OK)) {
            Log.d(TAG, "Image selected from camera");
            onImageTakenFromCamera();
        } else if ((requestCode == REQUEST_CROP_PICTURE) && (resultCode == Activity.RESULT_OK)) {
            Log.d(TAG, "Image returned from crop");
            onImageCropped(tmpImageUriFromCrop, tmpImageFileFromCrop);
        }
    }


    /**
     * Starts an intent for selecting image from gallery. The result is returned to the
     * onImageSelectedFromGallery() method of the ImageSelectionListener interface.
     */
    public void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //TODO: check file URI
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Ensure that there's a camera activity to handle the intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                try {
                    tmpImageFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Toast.makeText(this, "could not create an image file", Toast.LENGTH_LONG).show();
                }
                // Continue only if the File was successfully created
                if (tmpImageFile != null) {
                    tmpImageUri = getImageFileUri(tmpImageFile.getName());

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, tmpImageUri);
                    startActivityForResult(intent, REQUEST_PICTURE_FROM_GALLERY);
                }
            }
        }
    }

    /**
     * Starts an intent for taking photo with camera. The result is returned to the
     * onImageTakenFromCamera() method of the ImageSelectionListener interface.
     */
    public void takePhotoWithCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Ensure that there's a camera activity to handle the intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                try {
                    tmpImageFile = createImageFile(); // on nexus: /data/data/mlab.eu
                    // .eu.mlab.civitas.android/files/images/20170329_092357_tmp_camera-724933053.jpg
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Toast.makeText(this, "could not create an image file", Toast.LENGTH_LONG).show();
                }
                // Continue only if the File was successfully created
                if (tmpImageFile != null) {
                    tmpImageUri = getImageFileUri(tmpImageFile.getName()); // on nexus: content://mlab.eu.eu.mlab.civitas.android.provider/my_images/20170329_092357_tmp_camera-724933053.jpg

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, tmpImageUri);
                    startActivityForResult(intent, REQUEST_PICTURE_FROM_CAMERA);
                }
            }
        }
    }

    private Uri getImageFileUri(String fileName) {
        File imagePath = new File(getFilesDir(), Util.FILE_DIR_IMAGES);
        File newFile = new File(imagePath, fileName);

        return getUriForFile(this, Util.PROVIDER, newFile);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_tmp_camera";
        File storageDir = new File(getFilesDir(), Util.FILE_DIR_IMAGES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        tmpImageFilePath = image.getAbsolutePath();
        return image;
    }

    /**
     * Starts an intent for cropping an image that is saved in the uri. The result is
     * returned to the onImageCropped() method of the ImageSelectionListener interface.
     *
     * @param uri     uri that contains the data of the image to crop
     * @param outputX width of the result image
     * @param outputY height of the result image
     * @param aspectX horizontal ratio value while cutting the image
     * @param aspectY vertical ratio value of while cutting the image
     */
    public void requestCropImage(Uri uri, int outputX, int outputY, int aspectX, int aspectY) {
        if (tmpImageFileFromCrop == null) {
            try {
                tmpImageFileFromCrop = File.createTempFile("crop", "png", getExternalCacheDir());
                tmpImageUriFromCrop = Uri.fromFile(tmpImageFileFromCrop);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // open crop intent when user selects image
        final Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("output", tmpImageUriFromCrop);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("scale", true);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, REQUEST_CROP_PICTURE);
    }

    public void onImageSelectedFromGallery(Uri uri) {
        // cropping the taken photo. crop intent will have aspect ratio 16/9 and result image
        // will have size 800x450
        // requestCropImage(uri, 800, 450, 16, 9);

        // Copy image to the tmp file
        try {
            //Convert bitmap to byte array
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapData = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(tmpImageFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        displayImage(tmpImageFilePath);
    }

    public void onImageTakenFromCamera() {
        Log.d(TAG, "filename=" + tmpImageFilePath);
        displayImage(tmpImageFilePath);
    }

    public void onImageCropped(Uri uri, File imageFile) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            // showing bitmap in image view
//            displayImage(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayImage(String imagePath) {
        try {
            Bitmap bitmap = BitmapHandler.handleSamplingAndRotationBitmap(this, imagePath);
            holderNoImageAddedYet.setVisibility(View.GONE);
            artefactItemImage.setImageBitmap(bitmap);
            holderImageAdded.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAudioCompleted() {
        stopAudio();
    }
}