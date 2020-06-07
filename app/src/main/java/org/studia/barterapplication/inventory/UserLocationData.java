package org.studia.barterapplication.inventory;

import com.google.firebase.firestore.GeoPoint;

public class UserLocationData {
    private GeoPoint location;

    public UserLocationData() {
    }

    public UserLocationData(GeoPoint geoPoint, String uid, String displayName) {
        this.location = geoPoint;
        this.uid = uid;
        this.displayName = displayName;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    private String uid;
    private String displayName;
}
