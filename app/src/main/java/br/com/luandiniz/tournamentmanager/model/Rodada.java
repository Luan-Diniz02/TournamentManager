package br.com.luandiniz.tournamentmanager.model;

import java.util.ArrayList;
import java.util.List;

public class Rodada {
    private int id, idTorneio;
    private List<Duelo> duelos;
    public Rodada(int idTorneio, List<Duelo> duelos) {
        this.idTorneio = idTorneio;
        this.duelos = duelos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdTorneio() {
        return idTorneio;
    }

    public void setIdTorneio(int idTorneio) {
        this.idTorneio = idTorneio;
    }

    public List<Duelo> getDuelos() {
        return new ArrayList<>(duelos);
    }

    public void setDuelos(List<Duelo> duelos) {
        this.duelos = duelos;
    }
}
