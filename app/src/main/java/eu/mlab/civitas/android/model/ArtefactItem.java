package eu.mlab.civitas.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import eu.mlab.civitas.android.util.Util;

/**
 * Created by christophstanik on 3/10/17.
 */

public class ArtefactItem implements Parcelable {
    private String imagePath;
    private String audioPath;
    private String description;
    private String imageLicense;
    private String imageLicenseLink;
    private String author;
    private String language = "DE";

    public ArtefactItem() {
    }

    public ArtefactItem(String imagePath, String audioPath, String description, String imageLicense, String imageLicenseLink, String author) {
        this.imagePath = imagePath;
        this.audioPath = audioPath;
        this.description = description;
        this.imageLicense = imageLicense;
        this.imageLicenseLink = imageLicenseLink;
        this.author = author;
    }

    public ArtefactItem(Parcel in) {
        imagePath = in.readString();
        imageLicense = in.readString();
        imageLicenseLink = in.readString();
        audioPath = in.readString();
        description = in.readString();
        author = in.readString();
    }

    public static final Creator<ArtefactItem> CREATOR = new Creator<ArtefactItem>() {
        @Override
        public ArtefactItem createFromParcel(Parcel in) {
            return new ArtefactItem(in);
        }

        @Override
        public ArtefactItem[] newArray(int size) {
            return new ArtefactItem[size];
        }
    };

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageLicense() {
        return imageLicense;
    }

    public void setImageLicense(String imageLicense) {
        this.imageLicense = imageLicense;
    }

    public String getImageLicenseLink() {
        return imageLicenseLink;
    }

    public void setImageLicenseLink(String imageLicenseLink) {
        this.imageLicenseLink = imageLicenseLink;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imagePath);
        dest.writeString(imageLicense);
        dest.writeString(imageLicenseLink);
        dest.writeString(audioPath);
        dest.writeString(description);
        dest.writeString(author);
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getImageName() {
        if (imagePath != null && !imagePath.isEmpty()) {
            if ((imagePath.split((Util.FILE_DIR_IMAGES+"/")).length > 1)) {
                return imagePath.split((Util.FILE_DIR_IMAGES + "/"))[1];
            }
            return imagePath;
        }
        else {
            return "";
        }
    }

    public String getAudioName() {
        if (audioPath != null && !audioPath.isEmpty()) {
            if ((audioPath.split((Util.FILE_DIR_AUDIO+"/")).length > 1)) {
                return audioPath.split((Util.FILE_DIR_AUDIO + "/"))[1];
            }
            else {
                return audioPath;
            }
        }
        else {
            return "";
        }
    }

    /**
     * @return true if all compulsory variables have values
     */
    public boolean isValid() {
        if (getImagePath() == null || getImagePath().isEmpty()) {
            return false;
        }
        if (getDescription() == null || getDescription().isEmpty()) {
            return false;
        }
        if (getImageLicense() == null || getImageLicense().isEmpty()) {
            return false;
        }
        if (getImageLicenseLink()== null || getImageLicenseLink().isEmpty()) {
            return false;
        }
        return true;
    }
}