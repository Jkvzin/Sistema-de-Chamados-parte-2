package com.example.trabalho2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EstatisticasFragment extends Fragment {

    private TextView txtTotal, txtAbertos, txtEmAndamento, txtConcluidos;
    private BD bd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_estatisticas, container, false);

        txtTotal = view.findViewById(R.id.txtTotal);
        txtAbertos = view.findViewById(R.id.txtAbertos);
        txtEmAndamento = view.findViewById(R.id.txtEmAndamento);
        txtConcluidos = view.findViewById(R.id.txtConcluidos);

        bd = new BD(requireContext());
        carregarEstatisticas();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarEstatisticas();
    }

    private void carregarEstatisticas() {
        BD.Estatisticas est = bd.getEstatisticas();
        txtTotal.setText(String.valueOf(est.total));
        txtAbertos.setText(String.valueOf(est.abertos));
        txtEmAndamento.setText(String.valueOf(est.emAndamento));
        txtConcluidos.setText(String.valueOf(est.concluidos));
    }
}
