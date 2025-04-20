package br.com.luandiniz.tournamentmanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.luandiniz.tournamentmanager.R;
import br.com.luandiniz.tournamentmanager.model.Duelista;

public class RankAdapter extends RecyclerView.Adapter<RankAdapter.MyViewHolder> {

    private List<Duelista> duelistas;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public RankAdapter(List<Duelista> duelistas, Context context) {
        this.duelistas = duelistas != null ? duelistas : new ArrayList<>();
        this.context = context;
    }

    // Interfaces para os listeners
    public interface OnItemClickListener {
        void onItemClick(Duelista duelista, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Duelista duelista, int position);
    }

    // Setters para os listeners
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView classificacao, nome, vitorias, derrotas, empates, participacao, pontosTotal;

        public MyViewHolder(@NonNull View itemView,
                            OnItemClickListener clickListener,
                            OnItemLongClickListener longClickListener,
                            List<Duelista> duelistas) {
            super(itemView);

            // Inicializa as views
            classificacao = itemView.findViewById(R.id.item_duelista_classificacao);
            nome = itemView.findViewById(R.id.item_duelista_nome);
            vitorias = itemView.findViewById(R.id.item_duelista_vitorias);
            derrotas = itemView.findViewById(R.id.item_duelista_derrotas);
            empates = itemView.findViewById(R.id.item_duelista_empates);
            participacao = itemView.findViewById(R.id.item_duelista_participacao);
            pontosTotal = itemView.findViewById(R.id.item_duelista_pontos_total);

            // Configura o clique normal
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onItemClick(duelistas.get(position), position);
                }
            });

            // Configura o clique longo
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onItemLongClick(duelistas.get(position), position);
                }
                return true; // Indica que o evento foi consumido
            });
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_duelista, parent, false);
        return new MyViewHolder(view, onItemClickListener, onItemLongClickListener, duelistas);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Duelista duelista = duelistas.get(position);

        holder.classificacao.setText(String.format("%dº", position + 1));
        holder.nome.setText(duelista.getNome());
        holder.vitorias.setText(String.valueOf(duelista.getVitorias()));
        holder.derrotas.setText(String.valueOf(duelista.getDerrotas()));
        holder.empates.setText(String.valueOf(duelista.getEmpates()));
        holder.participacao.setText(String.valueOf(duelista.getParticipacao()));
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

    public void atualizarItem(int position, Duelista duelistaAtualizado) {
        if (position >= 0 && position < duelistas.size()) {
            duelistas.set(position, duelistaAtualizado);
            notifyItemChanged(position);
        }
    }
}