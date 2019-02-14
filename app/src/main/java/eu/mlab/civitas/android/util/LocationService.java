package eu.mlab.civitas.android.util;

import android.content.Context;
import android.location.Location;

import java.util.Calendar;

/**
 * Created by volodymyrbiryuk on 05/04/2017.
 * Provide location functions to be accessed throughout the app.
 */
public class LocationService{
    private Location lastCreatedArtefactLocation;
    private Location lastVisitedArtefactLocation;
    private Location userLocation;
    private final Location defaultLocation;

    private static final LocationService LOCATION_SERVICE = new LocationService();

    private LocationService(){
        // Default location set on Rome (Piazza della Repubblica).
        defaultLocation = new Location(Context.LOCATION_SERVICE);
        defaultLocation.setLatitude(41.902783);
        defaultLocation.setLongitude(12.496366);
        defaultLocation.setSpeed(0f);
    }

    /**
     * Check if location is not null and not older than 2 minutes.
     * @param location The location.
     * @return true if location is valid.
     */
    public boolean isLocationValid(Location location){
        boolean result = false;
        if (location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000){
            result = true;
        }
        return result;
    }

    public static LocationService getInstance(){
        return LOCATION_SERVICE;
    }

    public Location getDefaultLocation() {
        return defaultLocation;
    }
    //TODO FIX THIS!?
    public Location getLastCreatedArtefactLocation() {
        return lastCreatedArtefactLocation;
    }

    public void setLastCreatedArtefactLocation(Location lastCreatedArtefactLocation) {
        this.lastCreatedArtefactLocation = lastCreatedArtefactLocation;
    }

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public Location getLastVisitedArtefactLocation() {
        return lastVisitedArtefactLocation;
    }

    public void setLastVisitedArtefactLocation(Location lastVisitedArtefactLocation) {
        this.lastVisitedArtefactLocation = lastVisitedArtefactLocation;
    }

}
