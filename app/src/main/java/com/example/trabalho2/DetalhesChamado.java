package com.example.trabalho2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;

public class DetalhesChamado extends AppCompatActivity implements View.OnClickListener {

    private TextInputEditText txtTitulo, txtData, txtDescricao, txtLocal, txtTipo, txtSolucao;
    private RadioGroup rgStatus;
    private RadioButton rbAberto, rbEmAtendimento, rbConcluido;
    private ImageView imgChamado;

    private Button btnSalvar, btnVoltar;
    private BD bd;
    private int chamadoId = -1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalhes_chamado);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtTitulo = findViewById(R.id.inputTitulo);
        txtData = findViewById(R.id.inputData);
        txtDescricao = findViewById(R.id.inputDescricao);
        txtLocal = findViewById(R.id.inputLocal);
        txtTipo = findViewById(R.id.inputTipo);
        txtSolucao = findViewById(R.id.inputSolucao);
        imgChamado = findViewById(R.id.imgChamado);

        rgStatus = findViewById(R.id.rgStatus);
        rbAberto = findViewById(R.id.aberto);
        rbEmAtendimento = findViewById(R.id.emAtendimento);
        rbConcluido = findViewById(R.id.concluido);

        btnSalvar = findViewById(R.id.btnSalvar);
        btnVoltar = findViewById(R.id.btnVoltar);
        btnSalvar.setOnClickListener(this);
        btnVoltar.setOnClickListener(this);

        bd = new BD(this);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("CHAMADO_ID")) {
            chamadoId = intent.getIntExtra("CHAMADO_ID", -1);
            carregarDadosDoChamado(chamadoId);
        } else {
            Toast.makeText(this, "Erro ao carregar o chamado.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void carregarDadosDoChamado(int id) {
        Chamado c = bd.getChamado(id);

        if (c != null) {
            txtTitulo.setText(c.getTitulo());
            txtData.setText(c.getData());
            txtDescricao.setText(c.getDescricao());
            txtLocal.setText(c.getLocal());
            txtTipo.setText(c.getTipo());
            txtSolucao.setText(c.getSolucao());

            // Status
            if (c.getStatus().equals("Aberto")) {
                rbAberto.setChecked(true);
            } else if (c.getStatus().equals("Em Andamento")) {
                rbEmAtendimento.setChecked(true);
            } else {
                rbConcluido.setChecked(true);
            }

            // Imagem
            String imagemPath = c.getImagemPath();
            if (imagemPath != null && !imagemPath.isEmpty()) {
                File imgFile = new File(imagemPath);
                if (imgFile.exists()) {
                    imgChamado.setVisibility(View.VISIBLE);
                    imgChamado.setImageBitmap(BitmapFactory.decodeFile(imagemPath));
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnSalvar) {
            salvarAtendimento();
        } else if (view.getId() == R.id.btnVoltar) {
            finish();
        }
    }

    private void salvarAtendimento() {
        String novoStatus = "Aberto";
        int statusSelecionado = rgStatus.getCheckedRadioButtonId();
        if (statusSelecionado == rbEmAtendimento.getId()) {
            novoStatus = "Em Andamento";
        } else if (statusSelecionado == rbConcluido.getId()) {
            novoStatus = "Concluído";
        }

        String novaSolucao = txtSolucao.getText().toString();

        bd.atualizarAtendimento(chamadoId, novoStatus, novaSolucao);

        Toast.makeText(this, "Atendimento atualizado com sucesso!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
