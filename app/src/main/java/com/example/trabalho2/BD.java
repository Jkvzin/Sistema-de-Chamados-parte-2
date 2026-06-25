package com.example.trabalho2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class BD extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "bancoChamados";

    // Classe interna para retorno de estatisticas
    public static class Estatisticas {
        public int total;
        public int abertos;
        public int emAndamento;
        public int concluidos;
    }

    public BD(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS chamado (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "titulo TEXT," +
                        "descricao TEXT," +
                        "local TEXT," +
                        "tipo TEXT," +
                        "data TEXT," +
                        "status TEXT," +
                        "solucao TEXT," +
                        "imagem TEXT" +
                        ")"
        );
        Log.i("BD", "Tabela chamado criada com sucesso");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            sqLiteDatabase.execSQL("ALTER TABLE chamado ADD COLUMN imagem TEXT");
            Log.i("BD", "Coluna imagem adicionada (upgrade v1 -> v2)");
        }
    }

    public void deletarRegistro(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("chamado", "_id = ?", new String[]{String.valueOf(id)});
        db.close();
        Log.i("BD", "Chamado deletado com sucesso");
    }

    public long inserirChamado(Chamado c) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("titulo", c.getTitulo());
        valores.put("descricao", c.getDescricao());
        valores.put("local", c.getLocal());
        valores.put("tipo", c.getTipo());
        valores.put("data", c.getData());
        valores.put("status", c.getStatus());
        valores.put("solucao", c.getSolucao());
        valores.put("imagem", c.getImagemPath());
        long id = db.insert("chamado", null, valores);
        db.close();
        Log.i("BD", "Chamado inserido com ID: " + id);
        return id;
    }

    public ArrayList<Chamado> getLista() {
        ArrayList<Chamado> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("chamado", null, null, null, null, null, "_id DESC");

        if (cursor.moveToFirst()) {
            do {
                Chamado c = cursorParaChamado(cursor);
                lista.add(c);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        for (Chamado chamado : lista) {
            Log.i("BD", "Chamado: " + chamado.getTitulo() + " | Status: " + chamado.getStatus());
        }
        return lista;
    }

    public Chamado getChamado(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("chamado", null, "_id = ?",
                new String[]{String.valueOf(id)}, null, null, null);

        Chamado c = null;
        if (cursor.moveToFirst()) {
            c = cursorParaChamado(cursor);
        }
        cursor.close();
        db.close();
        return c;
    }

    public void atualizarAtendimento(int id, String status, String solucao) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("status", status);
        valores.put("solucao", solucao);

        db.update("chamado", valores, "_id = ?", new String[]{String.valueOf(id)});
        db.close();
        Log.i("BD", "Atendimento atualizado com sucesso");
    }

    public ArrayList<Chamado> getListaFiltrada(@Nullable String statusFiltro,
                                               @Nullable String dataInicio,
                                               @Nullable String dataFim) {
        ArrayList<Chamado> todos = getLista();
        ArrayList<Chamado> filtrada = new ArrayList<>();

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());

        for (Chamado c : todos) {
            // Filtro por status
            if (statusFiltro != null && !statusFiltro.isEmpty() && !statusFiltro.equals("Todos")) {
                if (!c.getStatus().equalsIgnoreCase(statusFiltro)) {
                    continue;
                }
            }

            // Filtro por data
            if (dataInicio != null && !dataInicio.isEmpty()) {
                try {
                    java.util.Date dataChamado = sdf.parse(c.getData());
                    java.util.Date dataInicioDate = sdf.parse(dataInicio);
                    if (dataChamado != null && dataChamado.before(dataInicioDate)) {
                        continue;
                    }
                } catch (java.text.ParseException e) {
                    // ignora chamado com data inválida
                    continue;
                }
            }

            if (dataFim != null && !dataFim.isEmpty()) {
                try {
                    java.util.Date dataChamado = sdf.parse(c.getData());
                    java.util.Date dataFimDate = sdf.parse(dataFim);
                    if (dataChamado != null && dataChamado.after(dataFimDate)) {
                        continue;
                    }
                } catch (java.text.ParseException e) {
                    continue;
                }
            }

            filtrada.add(c);
        }

        return filtrada;
    }

    public Estatisticas getEstatisticas() {
        Estatisticas est = new Estatisticas();
        SQLiteDatabase db = getReadableDatabase();

        // Total
        Cursor cTotal = db.rawQuery("SELECT COUNT(*) FROM chamado", null);
        if (cTotal.moveToFirst()) est.total = cTotal.getInt(0);
        cTotal.close();

        // Abertos
        Cursor cAbertos = db.rawQuery("SELECT COUNT(*) FROM chamado WHERE status = ?",
                new String[]{"Aberto"});
        if (cAbertos.moveToFirst()) est.abertos = cAbertos.getInt(0);
        cAbertos.close();

        // Em Andamento
        Cursor cAndamento = db.rawQuery("SELECT COUNT(*) FROM chamado WHERE status = ?",
                new String[]{"Em Andamento"});
        if (cAndamento.moveToFirst()) est.emAndamento = cAndamento.getInt(0);
        cAndamento.close();

        // Concluidos
        Cursor cConcluidos = db.rawQuery("SELECT COUNT(*) FROM chamado WHERE status = ?",
                new String[]{"Concluído"});
        if (cConcluidos.moveToFirst()) est.concluidos = cConcluidos.getInt(0);
        cConcluidos.close();

        db.close();
        return est;
    }

    private Chamado cursorParaChamado(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        String titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"));
        String descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao"));
        String local = cursor.getString(cursor.getColumnIndexOrThrow("local"));
        String tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"));
        String data = cursor.getString(cursor.getColumnIndexOrThrow("data"));
        String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
        String solucao = cursor.getString(cursor.getColumnIndexOrThrow("solucao"));

        String imagem = null;
        int idxImagem = cursor.getColumnIndex("imagem");
        if (idxImagem >= 0) {
            imagem = cursor.getString(idxImagem);
        }

        return new Chamado(id, titulo, descricao, local, tipo, data, status, solucao, imagem);
    }
}
