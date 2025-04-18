package br.com.luandiniz.tournamentmanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.luandiniz.tournamentmanager.R;
import br.com.luandiniz.tournamentmanager.model.Duelista;
import br.com.luandiniz.tournamentmanager.model.Duelo;

public class DueloAdapter extends RecyclerView.Adapter<DueloAdapter.DueloViewHolder> {

    private List<Duelo> duelos;
    private List<Duelista> duelistas;
    private OnResultadoSelecionadoListener listener;
    private boolean torneioConcluido;

    public interface OnResultadoSelecionadoListener {
        void onResultadoSelecionado(int posicaoDuelo, String resultado);
    }

    public DueloAdapter(List<Duelo> duelos, List<Duelista> duelistas, OnResultadoSelecionadoListener listener, boolean torneioConcluido) {
        this.duelos = duelos != null ? duelos : new ArrayList<>();
        this.duelistas = duelistas;
        this.listener = listener;
        this.torneioConcluido = torneioConcluido;
    }

    @NonNull
    @Override
    public DueloViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_duelo, parent, false);
        return new DueloViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DueloViewHolder holder, int position) {
        Duelo duelo = duelos.get(position);
        holder.bind(duelo, duelistas, listener, torneioConcluido);
    }

    @Override
    public int getItemCount() {
        return duelos.size();
    }

    static class DueloViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDuelo;
        private RadioGroup rgResultado;
        private RadioButton rbVitoriaA, rbEmpate, rbVitoriaB;

        public DueloViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDuelo = itemView.findViewById(R.id.tv_duelo);
            rgResultado = itemView.findViewById(R.id.rg_resultado);
            rbVitoriaA = itemView.findViewById(R.id.rb_vitoria_a);
            rbEmpate = itemView.findViewById(R.id.rb_empate);
            rbVitoriaB = itemView.findViewById(R.id.rb_vitoria_b);
        }

        public void bind(Duelo duelo, List<Duelista> duelistas, OnResultadoSelecionadoListener listener, boolean torneioConcluido) {
            Duelista duelista1 = buscarDuelistaPorId(duelistas, duelo.getIdDuelista1());
            Duelista duelista2 = buscarDuelistaPorId(duelistas, duelo.getIdDuelista2());

            String nomeDuelista1 = duelista1 != null ? duelista1.getNome() : "Desconhecido";
            String nomeDuelista2 = duelista2 != null ? duelista2.getNome() : "Desconhecido";
            tvDuelo.setText(nomeDuelista1 + " vs " + nomeDuelista2);

            rgResultado.setOnCheckedChangeListener(null); // Limpa listeners anteriores
            rgResultado.clearCheck(); // Limpa seleção

            if (torneioConcluido) {
                // Modo somente leitura: exibir resultado salvo e desabilitar opções
                rbVitoriaA.setEnabled(false);
                rbEmpate.setEnabled(false);
                rbVitoriaB.setEnabled(false);

                if (duelo.getIdVencedor() == null) {
                    rbEmpate.setChecked(true);
                } else if (duelo.getIdVencedor() == duelo.getIdDuelista1()) {
                    rbVitoriaA.setChecked(true);
                } else if (duelo.getIdVencedor() == duelo.getIdDuelista2()) {
                    rbVitoriaB.setChecked(true);
                }
            } else {
                // Modo editável: permitir seleção de resultado
                rgResultado.setOnCheckedChangeListener((group, checkedId) -> {
                    String resultado = "";
                    if (checkedId == R.id.rb_vitoria_a) {
                        resultado = "VITORIA_A";
                    } else if (checkedId == R.id.rb_empate) {
                        resultado = "EMPATE";
                    } else if (checkedId == R.id.rb_vitoria_b) {
                        resultado = "VITORIA_B";
                    }

                    if (listener != null && !resultado.isEmpty()) {
                        listener.onResultadoSelecionado(getAdapterPosition(), resultado);
                    }
                });
            }
        }

        private Duelista buscarDuelistaPorId(List<Duelista> duelistas, int id) {
            for (Duelista duelista : duelistas) {
                if (duelista.getId() == id) {
                    return duelista;
                }
            }
            return null;
        }
    }
}