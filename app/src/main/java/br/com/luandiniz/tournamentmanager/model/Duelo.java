package br.com.luandiniz.tournamentmanager.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Duelo implements Parcelable {
    private int id;
    private int idRodada;
    private int idDuelista1;
    private int idDuelista2;
    private Integer idVencedor;
    private boolean empate;

    public Duelo(int idRodada, int idDuelista1, int idDuelista2) {
        this.idRodada = idRodada;
        this.idDuelista1 = idDuelista1;
        this.idDuelista2 = idDuelista2;
    }

    protected Duelo(Parcel in) {
        id = in.readInt();
        idRodada = in.readInt();
        idDuelista1 = in.readInt();
        idDuelista2 = in.readInt();
        idVencedor = in.readInt() == -1 ? null : in.readInt();
    }

    public static final Creator<Duelo> CREATOR = new Creator<Duelo>() {
        @Override
        public Duelo createFromParcel(Parcel in) {
            return new Duelo(in);
        }

        @Override
        public Duelo[] newArray(int size) {
            return new Duelo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(idRodada);
        dest.writeInt(idDuelista1);
        dest.writeInt(idDuelista2);
        dest.writeInt(idVencedor == null ? -1 : idVencedor);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdRodada() {
        return idRodada;
    }

    public void setIdRodada(int idRodada) {
        this.idRodada = idRodada;
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

    public Integer getIdVencedor() {
        return idVencedor;
    }

    public void setIdVencedor(Integer idVencedor) {
        this.idVencedor = idVencedor;
    }

    public boolean isEmpate() {
        return empate;
    }

    public void setEmpate(boolean empate) {
        this.empate = empate;
    }

    public boolean temResultadoDefinido() {
        // Considera que o resultado está definido se:
        // 1. Tem um vencedor OU
        // 2. Foi marcado explicitamente como empate
        return this.idVencedor != null || this.empate;
    }
}