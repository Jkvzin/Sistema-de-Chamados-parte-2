package com.example.trabalho2;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EstatisticasActivity extends AppCompatActivity {

    private TextView txtTotal, txtAbertos, txtEmAndamento, txtConcluidos;
    private BD bd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_estatisticas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtTotal = findViewById(R.id.txtTotal);
        txtAbertos = findViewById(R.id.txtAbertos);
        txtEmAndamento = findViewById(R.id.txtEmAndamento);
        txtConcluidos = findViewById(R.id.txtConcluidos);

        bd = new BD(this);
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
