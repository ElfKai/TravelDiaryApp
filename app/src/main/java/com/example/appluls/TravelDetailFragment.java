package com.example.appluls;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

public class TravelDetailFragment extends Fragment {
    private ImageView imageDetail;
    private TextView titleDetail, descriptionDetail, locationDetail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar la vista desde el archivo XML
        View view = inflater.inflate(R.layout.fragment_travel_details, container, false);

        // Vincular las vistas del XML
        imageDetail = view.findViewById(R.id.image_detail);
        titleDetail = view.findViewById(R.id.title_detail);
        descriptionDetail = view.findViewById(R.id.description_detail);
        locationDetail = view.findViewById(R.id.location_detail);

        // Cargar los datos del viaje
        Bundle args = getArguments();
        if (args != null) {
            titleDetail.setText(args.getString("title"));
            descriptionDetail.setText(args.getString("description"));
            locationDetail.setText(args.getString("location"));

            // Cargar la imagen (usando Glide, Picasso o similar)
            String imageUrl = args.getString("imageUrl");
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(imageDetail);
        }

        return view;
    }


}

