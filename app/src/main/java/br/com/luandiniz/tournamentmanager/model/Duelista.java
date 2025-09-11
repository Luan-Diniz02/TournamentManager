package br.com.luandiniz.tournamentmanager.model;

public class Duelista implements Comparable<Duelista> {
    private String nome;
    private int vitorias, derrotas, empates, pontos, id, participacoes;

    public Duelista(String nome, int vitorias, int derrotas, int empates) {
        this.nome = nome;
        this.vitorias = vitorias;
        this.derrotas = derrotas;
        this.empates = empates;
        this.participacoes = 1;
        this.pontos = vitorias * 3 + empates + participacoes;
    }

    public Duelista(String nome){
        this.nome = nome;
        this.vitorias = 0;
        this.derrotas = 0;
        this.empates = 0;
        this.participacoes = 0;
        this.pontos = 0;
    }

    public Duelista(Duelista outro) {
        this.id = outro.id;
        this.nome = outro.nome;
        this.pontos = outro.pontos;
        this.vitorias = outro.vitorias;
        this.derrotas = outro.derrotas;
        this.empates = outro.empates;
        this.participacoes = outro.participacoes;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVitorias() {
        return vitorias;
    }

    public void setVitorias(int vitorias) {
        this.vitorias = vitorias;
        calcularPontos();
    }

    public int getDerrotas() {
        return derrotas;
    }

    public void setDerrotas(int derrotas) {
        this.derrotas = derrotas;
        calcularPontos();
    }

    public int getEmpates() {
        return empates;
    }

    public void setEmpates(int empates) {
        this.empates = empates;
        calcularPontos();
    }

    public int getPontos() {
        return pontos;
    }

    public void setPontos(int pontos) {
        this.pontos = pontos;
    }

    public void calcularPontos(){
        this.pontos = (vitorias * 3) + empates + participacoes;
    }

    public int getParticipacao() {
        return participacoes;
    }

    public void setParticipacao(int participacoes) {
        this.participacoes = participacoes;
        calcularPontos();
    }


    @Override
    public int compareTo(Duelista duelista) {
        // Primeiro compara por pontos (ordem decrescente)
        int comparacaoPontos = Integer.compare(duelista.pontos, this.pontos);
        if (comparacaoPontos != 0) {
            return comparacaoPontos;
        }

        // Se pontos iguais, compara por vitórias (ordem decrescente)
        int comparacaoVitorias = Integer.compare(duelista.vitorias, this.vitorias);
        if (comparacaoVitorias != 0) {
            return comparacaoVitorias;
        }

        // Se vitórias iguais, compara por derrotas (ordem crescente)
        int comparacaoDerrotas = Integer.compare(this.derrotas, duelista.derrotas);
        if (comparacaoDerrotas != 0) {
            return comparacaoDerrotas;
        }

        // Se tudo igual, pode-se comparar por nome (ordem alfabética)
        return this.nome.compareToIgnoreCase(duelista.nome);
    }
}
