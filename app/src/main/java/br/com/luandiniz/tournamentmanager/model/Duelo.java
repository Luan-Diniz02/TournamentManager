package br.com.luandiniz.tournamentmanager.model;

public class Duelo {
    private int id, idDuelista1, idDuelista2, idRodada, idVencedor;
    public Duelo(int idDuelista1, int idDuelista2, int idRodada) {
        this.idDuelista1 = idDuelista1;
        this.idDuelista2 = idDuelista2;
        this.idRodada = idRodada;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdDuelista1() {
        return idDuelista1;
    }

    public void setIdDuelista1(int idDuelista1) {
        this.idDuelista1 = idDuelista1;
    }

    public int getIdDuelista2() {
        return idDuelista2;
    }

    public void setIdDuelista2(int idDuelista2) {
        this.idDuelista2 = idDuelista2;
    }

    public int getIdRodada() {
        return idRodada;
    }

    public void setIdRodada(int idRodada) {
        this.idRodada = idRodada;
    }

    public int getIdVencedor() {
        return idVencedor;
    }

    public void setIdVencedor(int idVencedor) {
        this.idVencedor = idVencedor;
    }
}
