package com.example.appluls;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configurar la barra de herramientas
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Diario de viajes");
        setSupportActionBar(toolbar);

        // Inicializar el DrawerLayout y el NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        // Configurar el toggle del menú de navegación
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Establecer listener para el menú de navegación
        navigationView.setNavigationItemSelectedListener(this);

        // Cargar el fragmento inicial
        if (savedInstanceState == null) {
            loadFragment(new TravelListFragment());
            navigationView.setCheckedItem(R.id.nav_travel_list);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        // Seleccionar el fragmento correspondiente según el ítem del menú
        if(item.getItemId() == R.id.nav_travel_list){
            selectedFragment = new TravelListFragment();
        }
        if(item.getItemId() == R.id.nav_add_travel){
            selectedFragment = new AddTravelFragment();
        }
        if (selectedFragment != null) {
            loadFragment(selectedFragment);
        }

        // Cerrar el menú lateral
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        // Verificar si el fragmento ya está añadido para evitar agregarlo nuevamente
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Solo reemplazar el fragmento si es diferente al actual
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null ||
                !getSupportFragmentManager().findFragmentById(R.id.fragment_container).getClass().equals(fragment.getClass())) {
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


}
