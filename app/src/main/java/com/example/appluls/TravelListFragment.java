package com.example.appluls;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class TravelListFragment extends Fragment implements TravelAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private TravelAdapter travelAdapter;
    private ArrayList<Travel> travelList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_travel_list, container, false);

        // Inicializar RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        travelList = new ArrayList<>();
        travelAdapter = new TravelAdapter(travelList, getContext(), this); // Pasamos el listener
        recyclerView.setAdapter(travelAdapter);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Cargar datos desde Firestore
        loadTravelData();

        return view;
    }

    private void loadTravelData() {
        // Obtener referencia a la colección de viajes
        CollectionReference travelsRef = db.collection("viajes");

        // Escuchar los cambios en la colección de viajes
        travelsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    if (isAdded()) { // Verifica si el fragmento sigue activo
                        Toast.makeText(getContext(), "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                if (snapshot != null && !snapshot.isEmpty()) {
                    // Limpiar la lista y agregar los nuevos datos
                    travelList.clear();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        Travel travel = document.toObject(Travel.class);
                        if (travel != null) {
                            travelList.add(travel);
                        }
                    }
                    travelAdapter.notifyDataSetChanged(); // Notificar al adapter que los datos han cambiado
                } else if (isAdded()) { // Verifica si el fragmento sigue activo
                    Toast.makeText(getContext(), "No se encontraron viajes", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        // Obtener el viaje seleccionado
        Travel selectedTravel = travelList.get(position);

        // Crear instancia de TravelDetailFragment
        TravelDetailFragment detailFragment = new TravelDetailFragment();

        // Pasar los datos del viaje seleccionado al fragmento
        Bundle args = new Bundle();
        args.putString("title", selectedTravel.getTitle());
        args.putString("description", selectedTravel.getDescription());
        args.putString("location", selectedTravel.getLocation());
        args.putString("imageUrl", selectedTravel.getImageUrl());
        detailFragment.setArguments(args);

        // Reemplazar el fragmento actual con el TravelDetailFragment
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null) // Agregar a la pila para permitir volver atrás
                .commit();
    }
}
