package br.com.luandiniz.tournamentmanager.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.luandiniz.tournamentmanager.model.Duelista;
import br.com.luandiniz.tournamentmanager.model.Duelo;
import br.com.luandiniz.tournamentmanager.model.Torneio;

public class DAOSQLITE extends SQLiteOpenHelper {

    private static DAOSQLITE instance;
    private static final int DATABASE_VERSION = 12;

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
        db.execSQL("PRAGMA foreign_keys=ON;");

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
                "idCampeao INTEGER DEFAULT 0, " +
                "topcut INTEGER NOT NULL)"; // Armazena os IDs dos duelistas como uma string separada por vírgulas
        db.execSQL(sqlTorneios);

        String sqlRodadas = "CREATE TABLE Rodadas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "idTorneio INTEGER NOT NULL, " +
                "duelos TEXT NOT NULL, " + // Armazena os duelos como uma string
                "FOREIGN KEY (idTorneio) REFERENCES Torneios(id))";
        db.execSQL(sqlRodadas);

        String sqlDuelos = "CREATE TABLE Duelos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "idRodada INTEGER NOT NULL, " +
                "duelista1 INTEGER NOT NULL, " +
                "duelista2 INTEGER NOT NULL, " +
                "vencedor INTEGER, " +
                "FOREIGN KEY (idRodada) REFERENCES Rodadas(id), " +
                "FOREIGN KEY (duelista1) REFERENCES Duelistas(id), " +
                "FOREIGN KEY (duelista2) REFERENCES Duelistas(id))";
        db.execSQL(sqlDuelos);

        String sqlTorneioDuelista = "CREATE TABLE TorneioDuelista (" +
                "idTorneio INTEGER NOT NULL, " +
                "idDuelista INTEGER NOT NULL, " +
                "PRIMARY KEY (idTorneio, idDuelista), " +
                "FOREIGN KEY (idTorneio) REFERENCES Torneios(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (idDuelista) REFERENCES Duelistas(id) ON DELETE CASCADE)";
        db.execSQL(sqlTorneioDuelista);

        String sqlTorneioByes = "CREATE TABLE TorneioByes (" +
                "idTorneio INTEGER NOT NULL, " +
                "idDuelista INTEGER NOT NULL, " +
                "PRIMARY KEY (idTorneio, idDuelista), " +
                "FOREIGN KEY (idTorneio) REFERENCES Torneios(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (idDuelista) REFERENCES Duelistas(id) ON DELETE CASCADE)";
        db.execSQL(sqlTorneioByes);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Duelistas");
        db.execSQL("DROP TABLE IF EXISTS Torneios");
        db.execSQL("DROP TABLE IF EXISTS Rodadas");
        db.execSQL("DROP TABLE IF EXISTS Duelos");
        db.execSQL("DROP TABLE IF EXISTS TorneioDuelista");
        db.execSQL("DROP TABLE IF EXISTS TorneioByes");
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

    public long adicionarDuelista(Duelista duelista) {
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
            long id = db.insert("Duelistas", null, values);
            return id; // Retorna o ID gerado
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Atualizar duelista
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

    public long adicionarTorneio(Torneio torneio) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put("nome", torneio.getNome());
            values.put("data", torneio.getData().getTime());
            values.put("rodadas", torneio.getQuantRodadas());
            values.put("topcut", torneio.isTopcut() ? 1 : 0);

            long idTorneio = db.insert("Torneios", null, values);
            if (idTorneio == -1) {
                throw new SQLException("Falha ao inserir torneio");
            }

            for (Duelista duelista : torneio.getDuelistas()) {
                adicionarDuelistaAoTorneio((int) idTorneio, duelista.getId());
            }

            db.setTransactionSuccessful();
            return idTorneio;
        } catch (Exception e) {
            Log.e("DAOSQLITE", "Erro ao adicionar torneio: " + e.getMessage());
            throw e;
        } finally {
            if (db != null) {
                if (db.inTransaction()) {
                    db.endTransaction();
                }
                if (db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    // Atualizar torneio
    public void atualizarTorneio(Torneio torneio) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("nome", torneio.getNome());
            values.put("data", torneio.getData().getTime());
            values.put("rodadas", torneio.getQuantRodadas());
            values.put("idCampeao", torneio.getIdCampeao());
            values.put("topcut", torneio.isTopcut() ? 1 : 0);
            db.update("Torneios", values, "id = ?", new String[]{String.valueOf(torneio.getId())});
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
                        listarDuelistasDoTorneio(cursor.getInt(cursor.getColumnIndexOrThrow("id"))),
                        cursor.getInt(cursor.getColumnIndexOrThrow("topcut")) == 1
                );
                torneio.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                torneio.setQuantRodadas(cursor.getInt(cursor.getColumnIndexOrThrow("rodadas")));
                torneio.setIdCampeao(cursor.getInt(cursor.getColumnIndexOrThrow("idCampeao"))); // Simplesmente usar o valor do banco
                torneios.add(torneio);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return torneios;
    }

    public boolean isDuelistaNoTorneio(int idTorneio, int idDuelista) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT 1 FROM TorneioDuelista WHERE idTorneio = ? AND idDuelista = ?",
                    new String[]{String.valueOf(idTorneio), String.valueOf(idDuelista)});
            return cursor.getCount() > 0;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
    }

    // Adicionar uma rodada
    public int adicionarRodada(int idTorneio, String duelos) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("idTorneio", idTorneio);
            values.put("duelos", duelos);
            long idRodada = db.insert("Rodadas", null, values);
            return (int) idRodada;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Atualizar uma rodada
    public void atualizarRodada(int idRodada, String duelos) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("duelos", duelos);
            db.update("Rodadas", values, "id = ?", new String[]{String.valueOf(idRodada)});
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Listar IDs de rodadas
    public List<Integer> listarRodadas(int idTorneio) {
        List<Integer> rodadas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM Rodadas WHERE idTorneio = ?", new String[]{String.valueOf(idTorneio)});
        if (cursor.moveToFirst()) {
            do {
                rodadas.add(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return rodadas;
    }

    // Listar duelos por rodada
    public List<Duelo> listarDuelosPorRodada(int idRodada, int idTorneio) {
        List<Duelo> duelos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Duelos WHERE idRodada = ?", new String[]{String.valueOf(idRodada)});
        if (cursor.moveToFirst()) {
            do {
                Duelo duelo = new Duelo(
                        cursor.getInt(cursor.getColumnIndexOrThrow("idRodada")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("duelista1")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("duelista2"))
                );
                duelo.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                if (!cursor.isNull(cursor.getColumnIndexOrThrow("vencedor"))) {
                    duelo.setIdVencedor(cursor.getInt(cursor.getColumnIndexOrThrow("vencedor")));
                }
                duelos.add(duelo);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return duelos;
    }

    // Adicionar duelo
    public long adicionarDuelo(int idRodada, int duelista1, int duelista2, Integer vencedor) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("idRodada", idRodada);
            values.put("duelista1", duelista1);
            values.put("duelista2", duelista2);
            values.put("vencedor", vencedor);
            return db.insert("Duelos", null, values);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Atualizar duelo
    public void atualizarDuelo(int idDuelo, Integer idVencedor) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("vencedor", idVencedor);
            db.update("Duelos", values, "id = ?", new String[]{String.valueOf(idDuelo)});
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Listar duelos de uma rodada
    public List<String> listarDuelos(int idRodada) {
        List<String> duelos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Duelos WHERE idRodada = ?", new String[]{String.valueOf(idRodada)});

        if (cursor.moveToFirst()) {
            do {
                int duelista1 = cursor.getInt(cursor.getColumnIndexOrThrow("duelista1"));
                int duelista2 = cursor.getInt(cursor.getColumnIndexOrThrow("duelista2"));
                Integer vencedor = cursor.isNull(cursor.getColumnIndexOrThrow("vencedor")) ? null : cursor.getInt(cursor.getColumnIndexOrThrow("vencedor"));
                duelos.add("Duelista 1: " + duelista1 + ", Duelista 2: " + duelista2 + ", Vencedor: " + (vencedor != null ? vencedor : "N/A"));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return duelos;
    }

    // Adiciona um duelista a um torneio
    public void adicionarDuelistaAoTorneio(int idTorneio, int idDuelista) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("idTorneio", idTorneio);
            values.put("idDuelista", idDuelista);
            db.insert("TorneioDuelista", null, values);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Remove um duelista de um torneio
    public boolean removerDuelistaDeTorneio(int idTorneio, int idDuelista) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            int rowsAffected = db.delete("TorneioDuelista",
                    "idTorneio = ? AND idDuelista = ?",
                    new String[]{String.valueOf(idTorneio), String.valueOf(idDuelista)});
            return rowsAffected > 0;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Lista todos os duelistas de um torneio
    public List<Duelista> listarDuelistasDoTorneio(int idTorneio) {
        List<Duelista> duelistas = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            String query = "SELECT Duelistas.* FROM Duelistas " +
                    "INNER JOIN TorneioDuelista ON Duelistas.id = TorneioDuelista.idDuelista " +
                    "WHERE TorneioDuelista.idTorneio = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(idTorneio)});

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
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return duelistas;
    }

    // Adicionar um bye para um duelista em um torneio
    public void adicionarBye(int idTorneio, int idDuelista) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("idTorneio", idTorneio);
            values.put("idDuelista", idDuelista);
            db.insert("TorneioByes", null, values);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Listar duelistas que receberam bye em um torneio
    public Set<Integer> listarByes(int idTorneio) {
        Set<Integer> byes = new HashSet<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT idDuelista FROM TorneioByes WHERE idTorneio = ?",
                    new String[]{String.valueOf(idTorneio)});
            if (cursor.moveToFirst()) {
                do {
                    byes.add(cursor.getInt(cursor.getColumnIndexOrThrow("idDuelista")));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return byes;
    }


}