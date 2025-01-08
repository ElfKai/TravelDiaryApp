package com.example.appluls;


import android.net.Uri;

public class Travel {
    private String id;
    private String title;
    private String description;
    private String imageUrl;  // Para la URL de la imagen
    private String location;
    private Uri imageUri;  // Nuevo campo para la URI de la imagen desde la galería

    // Constructor vacío necesario para Firestore
    public Travel() {}

    // Constructor con parámetros
    public Travel(String id, String title, String description, String imageUrl, String location, Uri imageUri) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.location = location;
        this.imageUri = imageUri;  // Inicializar la URI
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTitle() {
        return title != null ? title : "";  // Retorna un valor predeterminado si es null
    }

    public String getDescription() {
        return description != null ? description : "";  // Retorna un valor predeterminado si es null
    }

    // Nuevo getter y setter para la URI de la imagen
    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }
}

