package org.studia.barterapplication.inventory;

public class Inventory {
    private String id;
    private String name;
    private String photoUrl;
    private String description;

    public Inventory() {
    }

    public Inventory(String id, String name, String photoUrl, String description) {
        this.id = id;
        this.name = name;
        this.photoUrl = photoUrl;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return this.description;
    }
}
