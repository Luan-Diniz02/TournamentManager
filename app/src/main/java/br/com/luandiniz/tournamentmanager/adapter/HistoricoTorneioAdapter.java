package br.com.luandiniz.tournamentmanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import br.com.luandiniz.tournamentmanager.R;
import br.com.luandiniz.tournamentmanager.model.Duelista;
import br.com.luandiniz.tournamentmanager.model.Torneio;

public class HistoricoTorneioAdapter extends RecyclerView.Adapter<HistoricoTorneioAdapter.TorneioViewHolder> {

    private List<Torneio> torneios;
    private List<Duelista> duelistas; // Para mapear IDs para nomes
    private OnTorneioClickListener listener;

    public interface OnTorneioClickListener {
        void onTorneioClick(Torneio torneio);
    }

    public HistoricoTorneioAdapter(List<Torneio> torneios, List<Duelista> duelistas, OnTorneioClickListener listener) {
        this.torneios = torneios;
        this.duelistas = duelistas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TorneioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historico_torneio, parent, false);
        return new TorneioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TorneioViewHolder holder, int position) {
        Torneio torneio = torneios.get(position);
        holder.bind(torneio, duelistas, listener);
    }

    @Override
    public int getItemCount() {
        return torneios.size();
    }

    public void atualizarLista(List<Torneio> novosTorneios) {
        this.torneios = novosTorneios;
        notifyDataSetChanged();
    }

    static class TorneioViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNomeTorneio, tvCampeao, tvData, tvRodadas, tvParticipantes;

        public TorneioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeTorneio = itemView.findViewById(R.id.tv_nome_torneio);
            tvCampeao = itemView.findViewById(R.id.tv_campeao);
            tvData = itemView.findViewById(R.id.tv_data);
            tvRodadas = itemView.findViewById(R.id.tv_rodadas);
            tvParticipantes = itemView.findViewById(R.id.tv_participantes);
        }

        public void bind(Torneio torneio, List<Duelista> duelistas, OnTorneioClickListener listener) {
            tvNomeTorneio.setText(torneio.getNome());

            // Encontrar nome do campeão
            String nomeCampeao = "Não definido";
            if (torneio.getIdCampeao() > 0) {
                for (Duelista d : duelistas) {
                    if (d.getId() == torneio.getIdCampeao()) {
                        nomeCampeao = d.getNome();
                        break;
                    }
                }
            }
            tvCampeao.setText(nomeCampeao);

            // Formatar data
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvData.setText(sdf.format(torneio.getData()));

            tvRodadas.setText(String.valueOf(torneio.getRodadas()));
            tvParticipantes.setText(String.valueOf(torneio.getDuelistas().size()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTorneioClick(torneio);
                }
            });
        }
    }
}
