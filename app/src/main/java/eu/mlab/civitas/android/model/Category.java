package eu.mlab.civitas.android.model;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import eu.mlab.civitas.android.R;

/**
 * Created by Glenn on 01.02.2017.
 * Updated by Christoph Stanik on 10/03/2017
 */

public class Category implements Parcelable {

    private int id;
    private String name;
    private String description;
    private Context context;

    public Category(int id, String name, String description){
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Category(int categoryId, Context context) {
        this.id = categoryId;
        this.context = context;
        this.description = "";

        if (context != null) {
            this.name = getLocalizedName(categoryId);
        }
        else {
            this.name = getName(categoryId);
        }
    }

    public String getLocalizedName(int id) {
        Resources r = context.getResources();
        switch (id){
            case 1: return r.getString(R.string.category_basilika);
            case 2: return r.getString(R.string.category_bogen);
            case 3: return r.getString(R.string.category_christentum);
            case 4: return r.getString(R.string.category_grab);
            case 5: return r.getString(R.string.category_gmythos);
            case 6: return r.getString(R.string.category_infra);
            case 7: return r.getString(R.string.category_kult);
            case 8:  return r.getString(R.string.category_platz);
            case 9: return r.getString(R.string.category_politik_inst);
            case 10: return r.getString(R.string.category_spiel);
            case 11: return r.getString(R.string.category_therme);
            case 12: return r.getString(R.string.category_wohnkomplex);
            default: return r.getString(R.string.category_default);
        }
    }

    public String getName(int id) {
        switch (id){
            case 1: return "Basilika";
            case 2: return "Bogen";
            case 3: return "Christentum";
            case 4: return "Grabstätte";
            case 5: return "Gründungsmythos";
            case 6: return "Infrastruktur";
            case 7: return "Kultstätte";
            case 8: return "Platzanlage";
            case 9: return "Politische Institution";
            case 10: return "Spielstätte";
            case 11: return "Therme";
            case 12: return "Wohnkomplex";
            default: return "";
        }
    }

    public Category(int id){
        this.id = id;
        this.name = getName(id);
        this.description = "";
    }

    private Category(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.description = in.readString();
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

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(description);
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
}
