package eu.mlab.civitas.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by christophstanik on 3/10/17.
 */

class ArtefactAudio implements Parcelable {
    private String audioFilePath;

    public ArtefactAudio() {
    }

    public ArtefactAudio(String audioPath) {
        this.audioFilePath = audioPath;
    }

    private ArtefactAudio(Parcel in) {
        String[] data = new String[1];

        in.readStringArray(data);
        audioFilePath = data[0];
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.audioFilePath});
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ArtefactAudio> CREATOR = new Parcelable.Creator<ArtefactAudio>() {
        @Override
        public ArtefactAudio createFromParcel(Parcel in) {
            return new ArtefactAudio(in);
        }

        @Override
        public ArtefactAudio[] newArray(int size) {
            return new ArtefactAudio[size];
        }
    };

    public String getAudioFilePath() {
        return audioFilePath;
    }

    public void setAudioFilePath(String path) {
        this.audioFilePath = path;
    }
}
