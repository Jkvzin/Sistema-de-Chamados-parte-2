package com.example.trabalho2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CadastroDemanda extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private TextInputEditText txtTitulo, txtDescricao, txtLocal;
    private Spinner spinnerStatus;
    private RadioGroup rgTipo;
    private BD bd;
    private Button btnCadastrar, btnCancelar, btnCapturarFoto;
    private ImageView imgPreview;

    private String imagemPath = null;
    private Uri photoUri = null;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_demanda);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtTitulo = findViewById(R.id.inputTitulo);
        txtDescricao = findViewById(R.id.inputDescricao);
        txtLocal = findViewById(R.id.inputLocal);
        rgTipo = findViewById(R.id.rgTipo);
        imgPreview = findViewById(R.id.imgPreview);

        // Status Spinner
        spinnerStatus = findViewById(R.id.spinnerStatus);
        String[] opcoesStatus = {"Aberto", "Em Andamento", "Concluído"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, opcoesStatus);
        spinnerStatus.setAdapter(adapterSpinner);

        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnCapturarFoto = findViewById(R.id.btnCapturarFoto);

        btnCadastrar.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);
        btnCapturarFoto.setOnClickListener(this);

        bd = new BD(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnCadastrar) {
            cadastrarChamado();
        } else if (view.getId() == R.id.btnCancelar) {
            finish();
        } else if (view.getId() == R.id.btnCapturarFoto) {
            capturarFoto();
        }
    }

    private void cadastrarChamado() {
        String titulo = txtTitulo.getText().toString().trim();
        String descricao = txtDescricao.getText().toString().trim();
        String local = txtLocal.getText().toString().trim();

        if (titulo.isEmpty() || descricao.isEmpty() || local.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedTipo = rgTipo.getCheckedRadioButtonId();
        String tipo = (selectedTipo == R.id.infra) ? "Infraestrutura" : "TI";

        String status = spinnerStatus.getSelectedItem().toString();

        String dataAtual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        Chamado chamado = new Chamado(0, titulo, descricao, local, tipo, dataAtual, status, "", imagemPath);
        long id = bd.inserirChamado(chamado);

        // Sincronizar com Back4App em background
        if (id > 0) {
            chamado.setId((int) id);
            Back4AppHelper.enviarChamado(chamado, new Back4AppHelper.SyncCallback() {
                @Override
                public void onSuccess(String objectId) {
                    // Sincronizacao realizada com sucesso (silenciosa)
                }

                @Override
                public void onError(String mensagem) {
                    // Erro na sincronizacao - nao impede o fluxo principal
                }
            });
        }

        Toast.makeText(this, "Chamado registrado com sucesso em " + dataAtual + "!", Toast.LENGTH_LONG).show();
        finish();
    }

    private void capturarFoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Criar arquivo temporario para a foto
            File photoFile = null;
            try {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "CHAMADO_" + timeStamp;
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                photoFile = File.createTempFile(imageFileName, ".jpg", storageDir);
                imagemPath = photoFile.getAbsolutePath();
            } catch (IOException e) {
                Toast.makeText(this, "Erro ao criar arquivo de imagem.", Toast.LENGTH_SHORT).show();
                return;
            }

            photoUri = FileProvider.getUriForFile(this,
                    "com.example.trabalho2.fileprovider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Nenhum app de câmera disponível.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Exibir preview da imagem
            imgPreview.setVisibility(View.VISIBLE);
            imgPreview.setImageURI(photoUri);

            // Tambem adiciona a foto na galeria
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(photoUri);
            sendBroadcast(mediaScanIntent);
        }
    }
}
