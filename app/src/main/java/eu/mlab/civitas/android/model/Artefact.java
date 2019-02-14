package eu.mlab.civitas.android.model;

import android.content.Context;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liisa_000 on 09/01/2017.
 * Updated by Christoph Stanik on 10/03/2017
 */

public class Artefact implements Parcelable {
    private int id;
    private String name;
    private String dating;
    private String description;
    private String image;
    private double latitude = -1;
    private double longitude = -1;
    private Location location;
    private Category category;
    private int categoryId;
    private List<ArtefactItem> artefactItems;

    public Artefact() {
    }

    public Artefact(int id, String name, String dating, double latitude, double longitude, int categoryId){
        this.id = id;
        this.name = name;
        this.dating = dating;
        //latitude and longitude are mixed up.
        this.latitude = longitude;
        this.longitude = latitude;
        this.category = new Category(categoryId);
        this.categoryId = categoryId;
    }

    public Artefact(int id, String name, String dating, Location location, int categoryId, Context context){
        this.id = id;
        this.name = name;
        this.dating = dating;
        this.location = location;
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
        this.category = new Category(categoryId, context);
        this.categoryId = categoryId;
        this.artefactItems = new ArrayList<>();
    }

    private Artefact(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.dating = in.readString();
        this.description = in.readString();
        this.image = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.category = in.readParcelable(Category.class.getClassLoader());
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getDating() {
        return dating;
    }

    public void setDating(String dating) {
        this.dating = dating;
    }

    public static Creator<Artefact> getCREATOR() {
        return CREATOR;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String title) {
        this.name = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Category getCategory() {
        return category;
    }

    public String getCategoryName(){
        return this.category.getName();
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void addArtefactItem(ArtefactItem artefactItem) {
        if (artefactItems == null) {
            artefactItems = new ArrayList<>();
        }
        artefactItems.add(artefactItem);
    }

    public List<ArtefactItem> getArtefactItems() {
        return artefactItems;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(dating);
        dest.writeString(description);
        dest.writeString(image);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeParcelable(category, flags);
    }

    public static final Creator<Artefact> CREATOR = new Creator<Artefact>() {
        @Override
        public Artefact createFromParcel(Parcel in) {
            return new Artefact(in);
        }

        @Override
        public Artefact[] newArray(int size) {
            return new Artefact[size];
        }
    };

    @Override
    public String toString() {
        return "Artefact{" +
                "id="+ id + '\'' +
                ",name='" + name + '\'' +
                ", dating='" + dating + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", category=" + category.getName() +
                '}';
    }

    public boolean isValid() {
        if (getName() == null || getName().isEmpty()) {
            return false;
        }

        if (getLongitude() == -1 ) {
            return false;
        }

        if (getLatitude() == -1 ) {
            return false;
        }

        if (getDating() == null || getDating().isEmpty()) {
            return false;
        }
        if (getCategory() == null) {
            return false;
        }
        if (artefactItems == null || artefactItems.size() == 0){
            return false;
        }

        return true;
    }
}
