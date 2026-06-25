package com.example.trabalho2;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;

public class ChamadosFragment extends Fragment {

    private RecyclerView recyclerChamados;
    private BD bd;
    private ChamadoAdapter adaptador;
    private TextView txtVazio;

    // Filtros
    private Spinner spinnerStatusFiltro;
    private EditText editDataInicio;
    private EditText editDataFim;
    private TextView btnLimparFiltros;

    private String statusSelecionado = "Todos";
    private String dataInicio = "";
    private String dataFim = "";

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
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).navigateTo(R.id.nav_novo_chamado);
            }
        });

        // Configurar filtros
        configurarFiltros(view);

        bd = new BD(requireContext());
        carregarLista();

        return view;
    }

    private void configurarFiltros(View view) {
        spinnerStatusFiltro = view.findViewById(R.id.spinnerStatusFiltro);
        editDataInicio = view.findViewById(R.id.editDataInicio);
        editDataFim = view.findViewById(R.id.editDataFim);
        btnLimparFiltros = view.findViewById(R.id.btnLimparFiltros);

        // Spinner de Status
        String[] opcoesStatus = {"Todos", "Aberto", "Em Andamento", "Concluído"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                opcoesStatus
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFiltro.setAdapter(spinnerAdapter);

        spinnerStatusFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                statusSelecionado = opcoesStatus[position];
                aplicarFiltros();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                statusSelecionado = "Todos";
                aplicarFiltros();
            }
        });

        // DatePicker ao tocar no campo de data início
        editDataInicio.setOnClickListener(v -> abrirDatePicker(editDataInicio, true));

        // DatePicker ao tocar no campo de data fim
        editDataFim.setOnClickListener(v -> abrirDatePicker(editDataFim, false));

        // Botão Limpar
        btnLimparFiltros.setOnClickListener(v -> limparFiltros());
    }

    private void abrirDatePicker(EditText campo, boolean isInicio) {
        Calendar cal = Calendar.getInstance();
        int ano = cal.get(Calendar.YEAR);
        int mes = cal.get(Calendar.MONTH);
        int dia = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, anoSelecionado, mesSelecionado, diaSelecionado) -> {
                    String dataFormatada = String.format("%02d/%02d/%04d",
                            diaSelecionado, mesSelecionado + 1, anoSelecionado);
                    campo.setText(dataFormatada);

                    if (isInicio) {
                        dataInicio = dataFormatada;
                    } else {
                        dataFim = dataFormatada;
                    }
                    aplicarFiltros();
                },
                ano, mes, dia
        );
        dialog.show();
    }

    private void limparFiltros() {
        statusSelecionado = "Todos";
        dataInicio = "";
        dataFim = "";

        spinnerStatusFiltro.setSelection(0);
        editDataInicio.setText("");
        editDataFim.setText("");

        carregarLista();
    }

    private void aplicarFiltros() {
        ArrayList<Chamado> listaFiltrada = bd.getListaFiltrada(statusSelecionado, dataInicio, dataFim);

        if (listaFiltrada.isEmpty()) {
            recyclerChamados.setVisibility(View.GONE);
            txtVazio.setVisibility(View.VISIBLE);
            txtVazio.setText("Nenhum chamado encontrado com esses filtros.");
        } else {
            recyclerChamados.setVisibility(View.VISIBLE);
            txtVazio.setVisibility(View.GONE);

            if (adaptador == null) {
                adaptador = new ChamadoAdapter(listaFiltrada, getContext(), chamadoClicado -> {
                    Intent intent = new Intent(getContext(), DetalhesChamado.class);
                    intent.putExtra("CHAMADO_ID", chamadoClicado.getId());
                    startActivity(intent);
                });
                recyclerChamados.setAdapter(adaptador);
            } else {
                adaptador.atualizarLista(listaFiltrada);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarLista();
    }

    private void carregarLista() {
        // Se tem filtro ativo, usa getListaFiltrada; senão, carrega tudo
        boolean temFiltro = !statusSelecionado.equals("Todos")
                || !dataInicio.isEmpty()
                || !dataFim.isEmpty();

        ArrayList<Chamado> lista;
        if (temFiltro) {
            lista = bd.getListaFiltrada(statusSelecionado, dataInicio, dataFim);
        } else {
            lista = bd.getLista();
        }

        if (lista.isEmpty()) {
            recyclerChamados.setVisibility(View.GONE);
            txtVazio.setVisibility(View.VISIBLE);
            if (temFiltro) {
                txtVazio.setText("Nenhum chamado encontrado com esses filtros.");
            } else {
                txtVazio.setText("Nenhum chamado cadastrado.\nToque em '+ NOVO CHAMADO' para começar.");
            }
        } else {
            recyclerChamados.setVisibility(View.VISIBLE);
            txtVazio.setVisibility(View.GONE);

            if (adaptador == null) {
                adaptador = new ChamadoAdapter(lista, getContext(), chamadoClicado -> {
                    Intent intent = new Intent(getContext(), DetalhesChamado.class);
                    intent.putExtra("CHAMADO_ID", chamadoClicado.getId());
                    startActivity(intent);
                });
                recyclerChamados.setAdapter(adaptador);
            } else {
                adaptador.atualizarLista(lista);
            }
        }
    }
}
