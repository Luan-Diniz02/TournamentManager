package br.com.luandiniz.tournamentmanager.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.luandiniz.tournamentmanager.model.Duelista;
import br.com.luandiniz.tournamentmanager.model.Torneio;

public class DAOSQLITE extends SQLiteOpenHelper {

    private static DAOSQLITE instance;
    private static final int DATABASE_VERSION = 5;

    public DAOSQLITE(Context context) {
        super(context, "torneios", null, DATABASE_VERSION);
    }

    public static synchronized DAOSQLITE getInstance(Context context) {
        if (instance == null) {
            instance = new DAOSQLITE(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlDuelistas = "CREATE TABLE Duelistas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "vitorias INTEGER NOT NULL, " +
                "derrotas INTEGER NOT NULL, " +
                "empates INTEGER NOT NULL, " +
                "participacoes INTEGER NOT NULL, " + // Novo campo
                "pontos INTEGER NOT NULL)";
        db.execSQL(sqlDuelistas);

        String sqlTorneios = "CREATE TABLE Torneios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "data INTEGER NOT NULL, " + // Armazena a data como timestamp
                "rodadas INTEGER NOT NULL, " +
                "idCampeao INTEGER, " +
                "topcut INTEGER NOT NULL, " + // 0 para false, 1 para true
                "duelistas TEXT NOT NULL)"; // Armazena os IDs dos duelistas como uma string separada por vírgulas
        db.execSQL(sqlTorneios);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Duelistas");
        db.execSQL("DROP TABLE IF EXISTS Torneios");
        onCreate(db);
    }

    public List<Duelista> listarDuelistas() {
        List<Duelista> duelistas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Duelistas", null);

        if (cursor.moveToFirst()) {
            do {
                Duelista duelista = new Duelista(
                        cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("vitorias")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("derrotas")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("empates"))
                );
                duelista.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                duelista.setParticipacao(cursor.getInt(cursor.getColumnIndexOrThrow("participacoes")));
                duelista.setPontos(cursor.getInt(cursor.getColumnIndexOrThrow("pontos")));
                duelistas.add(duelista);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return duelistas;
    }

    public void adicionarDuelista(Duelista duelista) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("nome", duelista.getNome());
            values.put("vitorias", duelista.getVitorias());
            values.put("derrotas", duelista.getDerrotas());
            values.put("empates", duelista.getEmpates());
            values.put("participacoes", duelista.getParticipacao());
            values.put("pontos", duelista.getPontos());
            db.insert("Duelistas", null, values);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

    }

    public void atualizarDuelista(Duelista duelista) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("nome", duelista.getNome());
            values.put("vitorias", duelista.getVitorias());
            values.put("derrotas", duelista.getDerrotas());
            values.put("empates", duelista.getEmpates());
            values.put("participacoes", duelista.getParticipacao());
            values.put("pontos", duelista.getPontos());
            db.update("Duelistas", values, "id = ?", new String[]{String.valueOf(duelista.getId())});
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public boolean removerDuelista(int id) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            int rowsAffected = db.delete("Duelistas", "id = ?", new String[]{String.valueOf(id)});
            return rowsAffected > 0;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public Duelista buscarDuelistaPorId(int id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM Duelistas WHERE id = ?", new String[]{String.valueOf(id)});
            if (cursor.moveToFirst()) {
                Duelista duelista = new Duelista(
                        cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("vitorias")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("derrotas")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("empates"))
                );
                duelista.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                duelista.setParticipacao(cursor.getInt(cursor.getColumnIndexOrThrow("participacoes")));
                duelista.setPontos(cursor.getInt(cursor.getColumnIndexOrThrow("pontos")));
                return duelista;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return null; // Retorna null se o duelista não for encontrado
    }

    public void adicionarTorneio(Torneio torneio) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("nome", torneio.getNome());
            values.put("data", torneio.getData().getTime()); // Converte a data para timestamp
            values.put("rodadas", torneio.getRodadas());
            values.put("idCampeao", torneio.getIdCampeao());
            values.put("topcut", torneio.isTopcut() ? 1 : 0); // Converte boolean para inteiro
            values.put("duelistas", String.join(",",
                    torneio.getDuelistas().stream().map(String::valueOf).toArray(String[]::new))); // Converte lista para string
            db.insert("Torneios", null, values);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public List<Torneio> listarTorneios() {
        List<Torneio> torneios = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Torneios", null);

        if (cursor.moveToFirst()) {
            do {
                Torneio torneio = new Torneio(
                        cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                        new Date(cursor.getLong(cursor.getColumnIndexOrThrow("data"))),
                        parseDuelistas(cursor.getString(cursor.getColumnIndexOrThrow("duelistas"))),
                        cursor.getInt(cursor.getColumnIndexOrThrow("topcut")) == 1
                );
                torneio.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                torneio.setRodadas(cursor.getInt(cursor.getColumnIndexOrThrow("rodadas")));
                torneio.setIdCampeao(cursor.getInt(cursor.getColumnIndexOrThrow("idCampeao")));
                torneios.add(torneio);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return torneios;
    }

    private List<Integer> parseDuelistas(String duelistasString) {
        List<Integer> duelistas = new ArrayList<>();
        if (duelistasString != null && !duelistasString.isEmpty()) {
            for (String id : duelistasString.split(",")) {
                duelistas.add(Integer.parseInt(id));
            }
        }
        return duelistas;
    }




}