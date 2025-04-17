package br.com.luandiniz.tournamentmanager.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Torneio {
    private String nome;
    private Date data;
    private List<Duelista> duelistas;
    private int id, quantRodadas;
    private Integer idCampeao;
    private boolean topcut;
    public Torneio(String nome, Date data, List<Duelista> duelistas, boolean topcut) {
        this.nome = nome;
        this.data = data;
        this.duelistas = duelistas;
        this.topcut = topcut;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public List<Duelista> getDuelistas() {
        return new ArrayList<>(duelistas);
    }

    public void setDuelistas(List<Duelista> duelistas) {
        this.duelistas = duelistas;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuantRodadas() {
        return quantRodadas;
    }

    public void setQuantRodadas(int quantRodadas) {
        this.quantRodadas = quantRodadas;
    }

    public Integer getIdCampeao() {
        return idCampeao;
    }

    public void setIdCampeao(Integer idCampeao) {
        this.idCampeao = idCampeao;
    }

    public boolean isTopcut() {
        return topcut;
    }

    public void setTopcut(boolean topcut) {
        this.topcut = topcut;
    }

    public Duelista buscarDuelistaPorId(int id) {
        for (Duelista duelista : duelistas) {
            if (duelista.getId() == id) {
                return duelista;
            }
        }
        return null; // Retorna null se não encontrar o duelista
    }

}
