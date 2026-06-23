package com.example.trabalho2;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CadastroFragment extends Fragment {

    private TextInputEditText txtTitulo, txtDescricao, txtLocal;
    private RadioGroup rgTipo;
    private BD bd;
    private ImageView imgPreview;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    private String imagemPath = null;
    private Uri photoUri = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Launcher para permissao de camera (runtime)
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        abrirCamera();
                    } else {
                        Toast.makeText(requireContext(),
                                "Permissao de camera negada. Va em Configuracoes > Aplicativos > Permissoes.",
                                Toast.LENGTH_LONG).show();
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && photoUri != null) {
                        View v = getView();
                        if (v != null) {
                            ImageView preview = v.findViewById(R.id.imgPreview);
                            if (preview != null) {
                                preview.setVisibility(View.VISIBLE);
                                Bitmap bmp = ImageUtils.loadRotatedBitmap(
                                        requireContext(), photoUri.toString(), 600, 600);
                                if (bmp != null) {
                                    preview.setImageBitmap(bmp);
                                }
                            }
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cadastro, container, false);

        txtTitulo = view.findViewById(R.id.inputTitulo);
        txtDescricao = view.findViewById(R.id.inputDescricao);
        txtLocal = view.findViewById(R.id.inputLocal);
        rgTipo = view.findViewById(R.id.rgTipo);
        imgPreview = view.findViewById(R.id.imgPreview);

        view.findViewById(R.id.btnCadastrar).setOnClickListener(v -> cadastrarChamado());
        view.findViewById(R.id.btnCancelar).setOnClickListener(v -> voltarParaListagem());
        view.findViewById(R.id.btnCapturarFoto).setOnClickListener(v -> capturarFoto());

        bd = new BD(requireContext());

        if (savedInstanceState != null) {
            imagemPath = savedInstanceState.getString("imagemPath");
            String savedUri = savedInstanceState.getString("photoUri");
            if (savedUri != null) {
                photoUri = Uri.parse(savedUri);
                File f = imagemPath != null ? new File(imagemPath) : null;
                if (f != null && f.exists()) {
                    imgPreview.setVisibility(View.VISIBLE);
                    Bitmap bmp = ImageUtils.loadRotatedBitmap(
                            requireContext(), photoUri.toString(), 600, 600);
                    if (bmp != null) {
                        imgPreview.setImageBitmap(bmp);
                    }
                }
            }
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("imagemPath", imagemPath);
        if (photoUri != null) outState.putString("photoUri", photoUri.toString());
    }

    private void voltarParaListagem() {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).navigateTo(R.id.nav_listagem);
        }
    }

    private void cadastrarChamado() {
        String titulo = txtTitulo.getText().toString().trim();
        String descricao = txtDescricao.getText().toString().trim();
        String local = txtLocal.getText().toString().trim();

        if (titulo.isEmpty() || descricao.isEmpty() || local.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha todos os campos obrigatorios.", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedTipo = rgTipo.getCheckedRadioButtonId();
        String tipo = (selectedTipo == R.id.infra) ? "Infraestrutura" : "TI";
        String status = "Aberto";
        String dataAtual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        Chamado c = new Chamado(0, titulo, descricao, local, tipo, dataAtual, status, "", imagemPath);
        long id = bd.inserirChamado(c);

        if (id > 0) {
            c.setId((int) id);
            Back4AppHelper.enviarChamado(c, new Back4AppHelper.SyncCallback() {
                @Override public void onSuccess(String objectId) {}
                @Override public void onError(String mensagem) {}
            });
        }

        Toast.makeText(requireContext(), "Chamado registrado com sucesso em " + dataAtual + "!", Toast.LENGTH_LONG).show();
        voltarParaListagem();
    }

    private void capturarFoto() {
        // Verifica permissao de camera em runtime (necessario a partir do Android 6.0 / API 23)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            abrirCamera();
        }
    }

    private void abrirCamera() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "CHAMADO_" + timeStamp;

            // Usa MediaStore para criar URI (compativel com Android 10+)
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Chamados");

            photoUri = requireActivity().getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            imagemPath = photoUri != null ? photoUri.toString() : null;

            if (photoUri == null) {
                Toast.makeText(requireContext(), "Erro ao criar arquivo de imagem.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            cameraLauncher.launch(takePictureIntent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Erro ao abrir camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
