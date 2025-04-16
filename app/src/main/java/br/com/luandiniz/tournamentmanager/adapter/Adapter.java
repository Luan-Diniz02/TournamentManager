package br.com.luandiniz.tournamentmanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.luandiniz.tournamentmanager.R;
import br.com.luandiniz.tournamentmanager.model.Duelista;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

    private List<Duelista> duelistas;
    private Context context;

    public Adapter(List<Duelista> duelistas, Context context) {
        this.duelistas = duelistas;
        this.context = context;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView classificacao;
        TextView nome;
        TextView vitorias;
        TextView derrotas;
        TextView empates;
        TextView participacao;
        TextView pontosTotal;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            classificacao = itemView.findViewById(R.id.item_duelista_classificacao);
            nome = itemView.findViewById(R.id.item_duelista_nome);
            vitorias = itemView.findViewById(R.id.item_duelista_vitorias);
            derrotas = itemView.findViewById(R.id.item_duelista_derrotas);
            empates = itemView.findViewById(R.id.item_duelista_empates);
            participacao = itemView.findViewById(R.id.item_duelista_participacao);
            pontosTotal = itemView.findViewById(R.id.item_duelista_pontos_total);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_duelista, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Duelista duelista = duelistas.get(position);

        // Classificação
        holder.classificacao.setText(String.format("%dº", position + 1));

        // Nome
        holder.nome.setText(duelista.getNome());

        // Estatísticas
        holder.vitorias.setText(String.valueOf(duelista.getVitorias()));
        holder.derrotas.setText(String.valueOf(duelista.getDerrotas()));
        holder.empates.setText(String.valueOf(duelista.getEmpates()));
        holder.participacao.setText(String.valueOf(duelista.getParticipacao()));

        // Pontuação total
        holder.pontosTotal.setText(String.valueOf(duelista.getPontos()));
    }

    @Override
    public int getItemCount() {
        return duelistas.size();
    }

    public void atualizarLista(List<Duelista> novaLista) {
        this.duelistas = novaLista;
        notifyDataSetChanged();
    }
}