package com.example.trabalho2;

import android.os.AsyncTask;
import android.util.Log;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Helper para sincronizacao com Back4App via REST API.
 *
 * Para usar:
 * 1. Crie um app no Back4App (https://www.back4app.com/)
 * 2. Obtenha o Application ID e REST API Key em App Settings > Security & Keys
 * 3. Substitua APPLICATION_ID e REST_API_KEY abaixo
 */
public class Back4AppHelper {

    private static final String BASE_URL = "https://parseapi.back4app.com/classes/Chamado";
    private static final String APPLICATION_ID = "E81CpqV9ZSTcU4u5ZAIrrHpAEh8S92O2bQClJDzb";
    private static final String REST_API_KEY = "83NRotZurYd4PalaTMLH1hvHrHmnMnosDBFmxWQB";
    private static final String TAG = "Back4App";

    public interface SyncCallback {
        void onSuccess(String objectId);
        void onError(String mensagem);
    }

    /**
     * Envia um chamado para o Back4App de forma assincrona.
     */
    public static void enviarChamado(Chamado chamado, SyncCallback callback) {
        new AsyncTask<Chamado, Void, String>() {
            private String errorMsg = null;

            @Override
            protected String doInBackground(Chamado... params) {
                Chamado c = params[0];
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(BASE_URL);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("X-Parse-Application-Id", APPLICATION_ID);
                    conn.setRequestProperty("X-Parse-REST-API-Key", REST_API_KEY);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);

                    String dataAtual = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",
                            Locale.getDefault()).format(new Date());

                    String json = "{"
                            + "\"titulo\":\"" + escapeJson(c.getTitulo()) + "\","
                            + "\"descricao\":\"" + escapeJson(c.getDescricao()) + "\","
                            + "\"local\":\"" + escapeJson(c.getLocal()) + "\","
                            + "\"status\":\"" + escapeJson(c.getStatus()) + "\","
                            + "\"dataCadastro\":\"" + escapeJson(dataAtual) + "\","
                            + "\"imagemNome\":\"" + escapeJson(extrairNomeImagem(c.getImagemPath())) + "\""
                            + "}";

                    OutputStream os = conn.getOutputStream();
                    os.write(json.getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 201) {
                        // Sucesso - ler resposta para obter objectId
                        java.util.Scanner s = new java.util.Scanner(
                                conn.getInputStream(), "UTF-8").useDelimiter("\\A");
                        String resposta = s.hasNext() ? s.next() : "";
                        s.close();

                        // Extrai objectId da resposta JSON
                        String objectId = extrairObjectId(resposta);
                        Log.i(TAG, "Chamado enviado ao Back4App. ObjectId: " + objectId);
                        return objectId != null ? objectId : "OK";
                    } else {
                        errorMsg = "Erro " + responseCode + " ao enviar para Back4App";
                        Log.e(TAG, errorMsg);
                        return null;
                    }
                } catch (Exception e) {
                    errorMsg = "Exceção: " + e.getMessage();
                    Log.e(TAG, "Erro ao enviar para Back4App", e);
                    return null;
                } finally {
                    if (conn != null) conn.disconnect();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError(errorMsg != null ? errorMsg : "Erro desconhecido");
                }
            }
        }.execute(chamado);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String extrairNomeImagem(String path) {
        if (path == null || path.isEmpty()) return "";
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    private static String extrairObjectId(String json) {
        // Procura por "objectId":"..."
        int idx = json.indexOf("\"objectId\":\"");
        if (idx < 0) return null;
        idx += 13;
        int endIdx = json.indexOf("\"", idx);
        if (endIdx < 0) return null;
        return json.substring(idx, endIdx);
    }
}
