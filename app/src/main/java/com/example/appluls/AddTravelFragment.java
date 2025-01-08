package com.example.appluls;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class AddTravelFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;  // Código de solicitud para abrir la galería

    private EditText editTitle, editDescription, editLocation, editImageUrl;
    private ImageView imagePreview;
    private Button btnAddTravel, btnSelectImage;
    private ProgressBar progressBar;
    private Uri selectedImageUri;
    private String imageFileName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_travel, container, false);

        // Inicializar vistas
        editTitle = view.findViewById(R.id.edit_title);
        editDescription = view.findViewById(R.id.edit_description);
        editLocation = view.findViewById(R.id.edit_location);
        editImageUrl = view.findViewById(R.id.edit_image_url);  // Nuevo campo para la URL de la imagen
        imagePreview = view.findViewById(R.id.image_preview);
        btnAddTravel = view.findViewById(R.id.btn_add_travel);
        btnSelectImage = view.findViewById(R.id.btn_select_image);
        progressBar = view.findViewById(R.id.progress_bar);

        // Configurar acción del botón para seleccionar imagen
        btnSelectImage.setOnClickListener(v -> openGallery());

        // Configurar acción del botón para agregar el viaje
        btnAddTravel.setOnClickListener(v -> addTravel());

        return view;
    }

    private void openGallery() {
        // Crea un Intent para abrir la galería
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");  // Solo imágenes
        startActivityForResult(intent, PICK_IMAGE_REQUEST);  // Llama a startActivityForResult
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            if (data != null && data.getData() != null) {
                selectedImageUri = data.getData();  // Obtiene la URI de la imagen seleccionada
                imagePreview.setImageURI(selectedImageUri);  // Muestra la imagen en el ImageView
            }
        }
    }

    private void addTravel() {
        // Obtener los valores ingresados por el usuario
        String title = editTitle.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String location = editLocation.getText().toString().trim();
        String imageUrl = editImageUrl.getText().toString().trim();  // Obtener URL de la imagen

        // Validar entradas
        if (TextUtils.isEmpty(title)) {
            editTitle.setError("El título es obligatorio");
            return;
        }
        if (TextUtils.isEmpty(description)) {
            editDescription.setError("La descripción es obligatoria");
            return;
        }
        if (TextUtils.isEmpty(location)) {
            editLocation.setError("La ubicación es obligatoria");
            return;
        }

        // Validar que al menos una de las opciones (imagen o URL) sea seleccionada
        if (selectedImageUri == null && TextUtils.isEmpty(imageUrl)) {
            Toast.makeText(getContext(), "Debe seleccionar una imagen o ingresar una URL", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar barra de progreso
        progressBar.setVisibility(View.VISIBLE);

        // Crear un ID único para el nuevo viaje
        String travelId = UUID.randomUUID().toString();

        // Si se seleccionó una imagen de la galería, guardarla localmente
        if (selectedImageUri != null) {
            saveImageLocally(selectedImageUri, travelId, title, description, location, imageUrl);
        } else {
            // Si se proporcionó una URL, guardamos el viaje con la URL de la imagen
            saveTravelWithImageUrl(travelId, title, description, imageUrl, location);
        }
    }

    private void saveImageLocally(Uri imageUri, String travelId, String title, String description, String location, String imageUrl) {
        try {
            // Obtener el bitmap de la imagen seleccionada
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);

            // Crear un nombre de archivo único para la imagen
            imageFileName = "travel_image_" + UUID.randomUUID().toString() + ".jpg";

            // Guardar la imagen en el almacenamiento interno
            File directory = getContext().getFilesDir();
            File file = new File(directory, imageFileName);
            try (OutputStream os = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);  // Guardamos la imagen como JPG
                os.flush();
                // Llamamos a la función que guarda el viaje en Firestore, pasando la ruta del archivo
                saveTravelWithImage(travelId, title, description, file.getAbsolutePath(), location, imageUrl);
            }
        } catch (IOException e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error al guardar la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveTravelWithImageUrl(String travelId, String title, String description, String imageUrl, String location) {
        // Crear un objeto Travel con la URL de la imagen
        Travel travel = new Travel(travelId, title, description, imageUrl, location, null);

        // Obtener una referencia a la colección de viajes
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference travelsRef = db.collection("viajes");

        // Guardar el viaje en Firestore
        travelsRef.document(travelId)  // Se usa el ID único del viaje como el documento
                .set(travel)  // Guardamos el objeto Travel
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Viaje agregado con éxito", Toast.LENGTH_SHORT).show();
                    // Limpiar los campos
                    editTitle.setText("");
                    editDescription.setText("");
                    editLocation.setText("");
                    editImageUrl.setText("");  // Limpiar el campo de la URL
                    imagePreview.setImageResource(R.drawable.ic_placeholder);  // Restablecer la imagen
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error al agregar el viaje: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveTravelWithImage(String travelId, String title, String description, String imagePath, String location, String imageUrl) {
        // Crear un objeto Travel con la ruta local de la imagen
        Travel travel = new Travel(travelId, title, description, imagePath, location, null);

        // Guardar el viaje en Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference travelsRef = db.collection("viajes");

        travelsRef.document(travelId)
                .set(travel)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Viaje agregado con éxito", Toast.LENGTH_SHORT).show();
                    // Limpiar los campos
                    editTitle.setText("");
                    editDescription.setText("");
                    editLocation.setText("");
                    editImageUrl.setText("");
                    imagePreview.setImageResource(R.drawable.ic_placeholder);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error al agregar el viaje: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

