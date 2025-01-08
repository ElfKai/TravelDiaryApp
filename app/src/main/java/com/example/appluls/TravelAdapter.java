package com.example.appluls;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class TravelAdapter extends RecyclerView.Adapter<TravelAdapter.TravelViewHolder> {

    private ArrayList<Travel> travelList; // Lista de objetos Travel
    private Context context;
    private OnItemClickListener onItemClickListener;

    // Constructor
    public TravelAdapter(ArrayList<Travel> travelList, Context context, OnItemClickListener onItemClickListener) {
        this.travelList = travelList;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public TravelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar la vista del elemento de la lista
        View view = LayoutInflater.from(context).inflate(R.layout.item_travel, parent, false);
        return new TravelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TravelViewHolder holder, int position) {
        // Obtener el objeto Travel en la posición actual
        Travel travel = travelList.get(position);

        // Configurar los datos del objeto en las vistas
        holder.title.setText(travel.getTitle() != null ? travel.getTitle() : "Título no disponible");
        holder.description.setText(travel.getDescription() != null ? travel.getDescription() : "Descripción no disponible");

        // Cargar la imagen usando Glide (URL o URI)
        if (travel.getImageUrl() != null && !travel.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(travel.getImageUrl())
                    .apply(new RequestOptions().placeholder(R.drawable.ic_placeholder))
                    .into(holder.image);
        } else if (travel.getImageUri() != null) {
            Glide.with(context)
                    .load(travel.getImageUri())
                    .apply(new RequestOptions().placeholder(R.drawable.ic_placeholder))
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.ic_placeholder);
        }

        // Configurar el botón de eliminación
        holder.deleteButton.setOnClickListener(v -> deleteTravel(travel));

        // Configurar el botón de compartir
        holder.shareButton.setOnClickListener(v -> shareTravel(travel));

        // Manejar el clic en el elemento
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return travelList.size();
    }

    // Método para actualizar la lista de viajes
    public void updateList(ArrayList<Travel> newTravelList) {
        this.travelList = newTravelList;
        notifyDataSetChanged();
    }

    // Interface para manejar clics en los elementos
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // Clase interna para manejar las vistas de cada elemento del RecyclerView
    public static class TravelViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        ImageView image;
        Button deleteButton;  // Referencia al botón de eliminar
        ImageButton shareButton; // Referencia al botón de compartir

        public TravelViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            image = itemView.findViewById(R.id.image);
            deleteButton = itemView.findViewById(R.id.delete_button);  // Inicializar el botón de eliminar
            shareButton = itemView.findViewById(R.id.btn_share);  // Inicializar el botón de compartir
        }
    }

    private void deleteTravel(Travel travel) {
        // Eliminar el viaje de Firestore usando su ID
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("viajes").document(travel.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Viaje eliminado con éxito", Toast.LENGTH_SHORT).show();
                    travelList.remove(travel);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Error al eliminar el viaje: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void shareTravel(Travel travel) {
        // Construir el contenido del mensaje
        String shareContent = "Título: " + travel.getTitle() +
                "\nDescripción: " + travel.getDescription() +
                "\nUbicación: " + travel.getLocation();

        // Verificar si hay una URL de imagen o un URI
        if (travel.getImageUrl() != null && !travel.getImageUrl().isEmpty()) {
            // Descargar la imagen usando Glide
            Glide.with(context)
                    .asBitmap()
                    .load(travel.getImageUrl())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            try {
                                // Crear un archivo temporal
                                File cachePath = new File(context.getCacheDir(), "images");
                                cachePath.mkdirs();
                                File file = new File(cachePath, "shared_image.png");
                                FileOutputStream stream = new FileOutputStream(file);
                                resource.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                stream.close();

                                // Obtener el URI usando FileProvider
                                Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);

                                // Configurar el intent de compartir
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("image/*");
                                shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                // Iniciar el intent
                                context.startActivity(Intent.createChooser(shareIntent, "Compartir viaje"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // No se necesita implementar
                        }
                    });
        } else {
            // Compartir solo el texto si no hay imagen
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);

            context.startActivity(Intent.createChooser(shareIntent, "Compartir viaje"));
        }
    }
}
