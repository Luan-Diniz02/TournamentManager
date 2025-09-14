package br.com.luandiniz.tournamentmanager.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    private static final int DATABASE_VERSION = 13; // Incrementado para forçar onUpgrade

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
                "participacoes INTEGER NOT NULL, " +
                "pontos INTEGER NOT NULL)";
        db.execSQL(sqlDuelistas);

        String sqlTorneios = "CREATE TABLE Torneios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "data INTEGER NOT NULL, " +
                "rodadas INTEGER NOT NULL, " +
                "idCampeao INTEGER DEFAULT 0, " +
                "topcut INTEGER NOT NULL)";
        db.execSQL(sqlTorneios);

        String sqlRodadas = "CREATE TABLE Rodadas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "idTorneio INTEGER NOT NULL, " +
                "duelos TEXT NOT NULL, " +
                "fase TEXT, " + // Nova coluna para armazenar a fase (Semi-final, Final, etc.)
                "FOREIGN KEY (idTorneio) REFERENCES Torneios(id))";
        db.execSQL(sqlRodadas);

        String sqlDuelos = "CREATE TABLE Duelos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "idRodada INTEGER NOT NULL, " +
                "duelista1 INTEGER NOT NULL, " +
                "duelista2 INTEGER NOT NULL, " +
                "vencedor INTEGER, " +
                "empate BOOL, " +
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
            return id;
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
        return null;
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
                torneio.setIdCampeao(cursor.getInt(cursor.getColumnIndexOrThrow("idCampeao")));
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

    public int adicionarRodada(int idTorneio, String duelos, String fase) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("idTorneio", idTorneio);
            values.put("duelos", duelos);
            values.put("fase", fase); // Salvar a fase da rodada
            long idRodada = db.insert("Rodadas", null, values);
            return (int) idRodada;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public void atualizarRodada(int idRodada, String duelos, String fase) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("duelos", duelos);
            values.put("fase", fase); // Atualizar a fase, se fornecida
            db.update("Rodadas", values, "id = ?", new String[]{String.valueOf(idRodada)});
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

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
                duelo.setEmpate(cursor.getInt(cursor.getColumnIndexOrThrow("empate")) == 1); // Carregar o campo empate
                duelos.add(duelo);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return duelos;
    }

    public long adicionarDuelo(int idRodada, int duelista1, int duelista2, Integer vencedor) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("idRodada", idRodada);
            values.put("duelista1", duelista1);
            values.put("duelista2", duelista2);
            values.put("vencedor", vencedor);
            values.put("empate", 0); // Definir empate como falso por padrão
            return db.insert("Duelos", null, values);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public void atualizarDuelo(int idDuelo, Integer idVencedor, boolean isEmpate) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("vencedor", idVencedor);
            values.put("empate", isEmpate ? 1 : 0); // Atualizar o campo empate
            db.update("Duelos", values, "id = ?", new String[]{String.valueOf(idDuelo)});
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public List<String> listarDuelos(int idRodada) {
        List<String> duelos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Duelos WHERE idRodada = ?", new String[]{String.valueOf(idRodada)});

        if (cursor.moveToFirst()) {
            do {
                int duelista1 = cursor.getInt(cursor.getColumnIndexOrThrow("duelista1"));
                int duelista2 = cursor.getInt(cursor.getColumnIndexOrThrow("duelista2"));
                Integer vencedor = cursor.isNull(cursor.getColumnIndexOrThrow("vencedor")) ? null : cursor.getInt(cursor.getColumnIndexOrThrow("vencedor"));
                boolean empate = cursor.getInt(cursor.getColumnIndexOrThrow("empate")) == 1;
                duelos.add("Duelista 1: " + duelista1 + ", Duelista 2: " + duelista2 + ", Vencedor: " + (vencedor != null ? vencedor : empate ? "Empate" : "N/A"));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return duelos;
    }

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

    public String getFaseRodada(int idRodada) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT fase FROM Rodadas WHERE id = ?", new String[]{String.valueOf(idRodada)});
            if (cursor.moveToFirst()) {
                String fase = cursor.getString(cursor.getColumnIndexOrThrow("fase"));
                return fase != null ? fase : ""; // Retorna vazio se fase for nula
            }
            return "";
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
    }

    public boolean exportarBancoDados(Context context, String nomeArquivo) {
        try {
            // Caminho do banco de dados interno
            String dbPath = context.getDatabasePath("torneios").getAbsolutePath();
            File dbFile = new File(dbPath);

            if (!dbFile.exists()) {
                Log.e("DAOSQLITE", "Banco de dados não encontrado");
                return false;
            }

            // Diretório de Downloads do dispositivo
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File exportFile = new File(downloadsDir, nomeArquivo + ".db");

            // Copiar o arquivo do banco
            try (FileInputStream fis = new FileInputStream(dbFile);
                 FileOutputStream fos = new FileOutputStream(exportFile)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fos.flush();
            }

            Log.i("DAOSQLITE", "Banco exportado para: " + exportFile.getAbsolutePath());
            return true;

        } catch (Exception e) {
            Log.e("DAOSQLITE", "Erro ao exportar banco: " + e.getMessage());
            return false;
        }
    }

    public boolean importarBancoDados(Context context, String caminhoArquivo) {
        try {
            File importFile = new File(caminhoArquivo);

            if (!importFile.exists()) {
                Log.e("DAOSQLITE", "Arquivo de importação não encontrado");
                return false;
            }

            // Fechar conexões existentes
            close();

            // Caminho do banco de dados interno
            String dbPath = context.getDatabasePath("torneios").getAbsolutePath();
            File dbFile = new File(dbPath);

            // Fazer backup do banco atual (opcional)
            if (dbFile.exists()) {
                File backupFile = new File(dbPath + ".backup");
                try (FileInputStream fis = new FileInputStream(dbFile);
                     FileOutputStream fos = new FileOutputStream(backupFile)) {

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }
            }

            // Importar o novo banco
            try (FileInputStream fis = new FileInputStream(importFile);
                 FileOutputStream fos = new FileOutputStream(dbFile)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fos.flush();
            }

            // Recriar a instância do DAO
            instance = null;
            instance = getInstance(context);

            Log.i("DAOSQLITE", "Banco importado com sucesso");
            return true;

        } catch (Exception e) {
            Log.e("DAOSQLITE", "Erro ao importar banco: " + e.getMessage());
            return false;
        }
    }

}