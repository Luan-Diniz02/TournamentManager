package br.com.luandiniz.tournamentmanager.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Torneio {
    private String nome;
    private Date data;
    private List<Integer> duelistas;
    private List<Rodada> lista_rodadas;
    private int id, rodadas, idCampeao;
    private boolean topcut;
    public Torneio(String nome, Date data, List<Integer> duelistas, boolean topcut) {
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

    public List<Integer> getDuelistas() {
        return new ArrayList<>(duelistas);
    }

    public void setDuelistas(List<Integer> duelistas) {
        this.duelistas = duelistas;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRodadas() {
        return rodadas;
    }

    public void setRodadas(int rodadas) {
        this.rodadas = rodadas;
    }

    public int getIdCampeao() {
        return idCampeao;
    }

    public void setIdCampeao(int idCampeao) {
        this.idCampeao = idCampeao;
    }

    public boolean isTopcut() {
        return topcut;
    }

    public void setTopcut(boolean topcut) {
        this.topcut = topcut;
    }

    public List<Rodada> getLista_rodadas() {
        return lista_rodadas;
    }

    public void setLista_rodadas(List<Rodada> lista_rodadas) {
        this.lista_rodadas = lista_rodadas;
    }

    public Rodada gerarRodadaSuica() {
        // Ordenar os duelistas (aqui apenas por ID, mas pode ser por pontuação)
        List<Integer> duelistasOrdenados = new ArrayList<>(duelistas);
        duelistasOrdenados.sort(Integer::compareTo);

        // Criar os duelos
        List<Duelo> duelos = new ArrayList<>();
        for (int i = 0; i < duelistasOrdenados.size() - 1; i += 2) {
            int idDuelista1 = duelistasOrdenados.get(i);
            int idDuelista2 = duelistasOrdenados.get(i + 1);
            Duelo duelo = new Duelo(idDuelista1, idDuelista2, lista_rodadas.size() + 1);
            duelos.add(duelo);
        }

        // Criar a rodada
        Rodada rodada = new Rodada(this.id, duelos);
        if (lista_rodadas == null) {
            lista_rodadas = new ArrayList<>();
        }
        lista_rodadas.add(rodada);

        return rodada;
    }

}
