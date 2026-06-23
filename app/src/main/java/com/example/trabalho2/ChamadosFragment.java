package com.example.trabalho2;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChamadosFragment extends Fragment {

    private RecyclerView recyclerChamados;
    private BD bd;
    private ChamadoAdapter adaptador;
    private TextView txtVazio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chamados, container, false);

        recyclerChamados = view.findViewById(R.id.recyclerChamados);
        txtVazio = view.findViewById(R.id.txtVazio);
        recyclerChamados.setLayoutManager(new LinearLayoutManager(getContext()));

        Button btnNovoChamado = view.findViewById(R.id.btnNovoChamado);
        btnNovoChamado.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new CadastroFragment())
                    .commit();
            // Atualiza titulo da toolbar
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).setToolbarTitle("Novo Chamado");
            }
        });

        bd = new BD(requireContext());
        carregarLista();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarLista();
    }

    private void carregarLista() {
        ArrayList<Chamado> lista = bd.getLista();
        if (lista.isEmpty()) {
            recyclerChamados.setVisibility(View.GONE);
            txtVazio.setVisibility(View.VISIBLE);
        } else {
            recyclerChamados.setVisibility(View.VISIBLE);
            txtVazio.setVisibility(View.GONE);
            atualizarAdapter(lista);
        }
    }

    private void atualizarAdapter(ArrayList<Chamado> lista) {
        adaptador = new ChamadoAdapter(lista, getContext(), chamadoClicado -> {
            Intent intent = new Intent(getContext(), DetalhesChamado.class);
            intent.putExtra("CHAMADO_ID", chamadoClicado.getId());
            startActivity(intent);
        });
        recyclerChamados.setAdapter(adaptador);
    }
}
