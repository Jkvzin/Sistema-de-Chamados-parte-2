package com.example.trabalho2;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private int pendingNavItem = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if (pendingNavItem != -1) {
                    int id = pendingNavItem;
                    pendingNavItem = -1;
                    toolbar.post(() -> navegarPara(id));
                }
            }
        });
        toggle.syncState();

        if (savedInstanceState == null) {
            navegarPara(R.id.nav_listagem);
            navigationView.setCheckedItem(R.id.nav_listagem);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        pendingNavItem = item.getItemId();
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void navegarPara(int id) {
        Fragment fragment;
        String titulo;

        if (id == R.id.nav_novo_chamado) {
            fragment = new CadastroFragment();
            titulo = "Novo Chamado";
        } else if (id == R.id.nav_listagem) {
            fragment = new ChamadosFragment();
            titulo = "Listagem de Chamados";
        } else if (id == R.id.nav_estatisticas) {
            fragment = new EstatisticasFragment();
            titulo = "Estatisticas";
        } else if (id == R.id.nav_sobre) {
            fragment = new SobreFragment();
            titulo = "Sobre o Sistema";
        } else {
            return;
        }

        // Atualiza titulo primeiro
        toolbar.setTitle(titulo);

        // Depois troca o fragment
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    public void setToolbarTitle(String titulo) {
        toolbar.setTitle(titulo);
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
