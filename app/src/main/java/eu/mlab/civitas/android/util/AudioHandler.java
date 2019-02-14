package eu.mlab.civitas.android.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by christophstanik on 3/14/17.
 */

public class AudioHandler {
    public static final String LOG_TAG = AudioHandler.class.getSimpleName();
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    private int playerStatus = 0; //2 playing, 1 paused, 0 stopped
    private boolean isRecording = false;
    private AudioCompleted audioCompletedCallback;
    private int audioTrackPosition = 0;

    public AudioHandler() {

    }

    public void setAudioCompletedCallbackHandler(Context context) {
        try {
            this.audioCompletedCallback = (AudioCompleted) context;
        } catch (ClassCastException e) {
            // not the AddArtefactActivity.java activity
            e.printStackTrace();
        }
    }

    public boolean onRecord(boolean start, String audioFileName) {
        if (start && !isRecording) {
            startRecording(audioFileName);
        } else {
            stopRecording();
        }
        return isRecording;
    }

    public int onPlay(int start, String audioFileName) {
        if (start == Util.AUDIO_PLAYER_PLAY && playerStatus == 0) {
            this.playerStatus = 2;
            startPlaying(audioFileName);
        } else if (start == Util.AUDIO_PLAYER_PLAY && playerStatus == 1) {
            this.playerStatus = 2;
            resumePlaying();
        } else if (start == Util.AUDIO_PLAYER_PAUSE && playerStatus == 2) {
            this.playerStatus = 1;
            pausePlaying();
        } else if (start == Util.AUDIO_PLAYER_STOP && (playerStatus == 2 || playerStatus == 1)){
            this.playerStatus = 0;
            stopPlaying();
        }
        return playerStatus;
    }

//    public boolean onPlay(boolean start, String audioFileName) {
//        if (start && !playerStatus) {
//            startPlaying(audioFileName);
//        } else {
//            stopPlaying();
//        }
//        return playerStatus;
//    }

    private void resumePlaying() {
        playerStatus = 2;
        try {
            mediaPlayer.seekTo(audioTrackPosition);
            mediaPlayer.start();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Resume playing failed.");
        }
    }

    private void startPlaying(String audioFileName) {
        playerStatus = 2;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (audioCompletedCallback != null) {
                    try {
                        audioCompletedCallback.onAudioCompleted();
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "could not reach source when on completed was called");
                    }
                }
            }
        });
        try {
            mediaPlayer.setDataSource(audioFileName);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        playerStatus = 0;
        try {
            mediaPlayer.release();
            mediaPlayer = null;
        } catch (Exception e) {
            // already released
        }
    }

    private void pausePlaying() {
        playerStatus = 1;
        try {
            mediaPlayer.pause();
            audioTrackPosition = mediaPlayer.getCurrentPosition();
        } catch (Exception e) {
            Log.e(LOG_TAG, "pausePlaying() failed");
        }
    }

    private void startRecording(String audioFileName) {
        isRecording = true;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(audioFileName);
        Log.e(LOG_TAG, "audio file path: " + audioFileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "prepare() failed");
        }

        mediaRecorder.start();
    }

    private void stopRecording() {
        isRecording = false;
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    public interface AudioCompleted {
        void onAudioCompleted();
    }


    public static String transformAudioToString(File audioFile, Context context) {
        byte[] soundBytes = null;

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(Uri.fromFile(audioFile));
            soundBytes = new byte[inputStream.available()];
            soundBytes = toByteArray(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (soundBytes == null) {
            return "";
        }
        return Base64.encodeToString(soundBytes, Base64.DEFAULT);

//        return "";
    }

    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read = 0;
        byte[] buffer = new byte[1024];
        while (read != -1) {
            read = in.read(buffer);
            if (read != -1)
                out.write(buffer, 0, read);
        }
        out.close();
        return out.toByteArray();
    }

    //TODO: transformStringToArray(){}
}