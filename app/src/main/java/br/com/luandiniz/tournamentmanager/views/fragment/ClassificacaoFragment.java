package br.com.luandiniz.tournamentmanager.views.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import br.com.luandiniz.tournamentmanager.R;
import br.com.luandiniz.tournamentmanager.adapter.RankAdapter;
import br.com.luandiniz.tournamentmanager.dao.DAOSQLITE;
import br.com.luandiniz.tournamentmanager.model.Duelista;
import br.com.luandiniz.tournamentmanager.model.Duelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassificacaoFragment extends Fragment {

    private static final String ARG_TORNEIO_ID = "torneio_id";
    private int torneioId;
    private DAOSQLITE dao;
    private RecyclerView rvClassificacao;
    private RankAdapter adapter;
    private List<Duelista> duelistas;

    public static ClassificacaoFragment newInstance(int torneioId) {
        ClassificacaoFragment fragment = new ClassificacaoFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TORNEIO_ID, torneioId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            torneioId = getArguments().getInt(ARG_TORNEIO_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rank, container, false);

        rvClassificacao = view.findViewById(R.id.recycler_view_duelistas);
        rvClassificacao.setLayoutManager(new LinearLayoutManager(getContext()));

        dao = DAOSQLITE.getInstance(requireContext());
        duelistas = dao.listarDuelistasDoTorneio(torneioId);

        // Calcular estatísticas específicas do torneio
        Map<Integer, Duelista> duelistaMap = new HashMap<>();
        for (Duelista duelista : duelistas) {
            Duelista torneioDuelista = new Duelista(
                    duelista.getNome(),
                    0, // Inicializar vitórias como 0
                    0, // Inicializar derrotas como 0
                    0  // Inicializar empates como 0
            );
            torneioDuelista.setId(duelista.getId());
            torneioDuelista.setPontos(0); // Inicializar pontos como 0
            torneioDuelista.setParticipacao(1);
            duelistaMap.put(duelista.getId(), torneioDuelista);
        }

        // Carregar rodadas do torneio
        List<Integer> rodadas = dao.listarRodadas(torneioId);
        for (Integer idRodada : rodadas) {
            List<Duelo> duelos = dao.listarDuelosPorRodada(idRodada, torneioId);
            for (Duelo duelo : duelos) {
                Integer vencedor = duelo.getIdVencedor();
                Duelista duelista1 = duelistaMap.get(duelo.getIdDuelista1());
                Duelista duelista2 = duelistaMap.get(duelo.getIdDuelista2());

                if (duelista1 == null || duelista2 == null) {
                    continue; // Ignorar duelos inválidos
                }

                if (vencedor != null) {
                    if (vencedor == duelista1.getId()) {
                        duelista1.setVitorias(duelista1.getVitorias() + 1);
                        duelista2.setDerrotas(duelista2.getDerrotas() + 1);
                    } else if (vencedor == duelista2.getId()) {
                        duelista2.setVitorias(duelista2.getVitorias() + 1);
                        duelista1.setDerrotas(duelista1.getDerrotas() + 1);
                    }
                } else {
                    // Empate (se não houver vencedor registrado)
                    duelista1.setEmpates(duelista1.getEmpates() + 1);
                    duelista2.setEmpates(duelista2.getEmpates() + 1);
                }
            }
        }

        // Incluir pontos de bye
        Set<Integer> byes = dao.listarByes(torneioId);
        for (Integer idDuelista : byes) {
            Duelista duelista = duelistaMap.get(idDuelista);
            if (duelista != null) {
                duelista.setVitorias(duelista.getVitorias() + 1);
            }
        }

        // Converter mapa para lista
        duelistas = new ArrayList<>(duelistaMap.values());

        // Ordenar duelistas por pontos (descendente), com desempate por vitórias
        Collections.sort(duelistas, (d1, d2) -> {
            int comparePontos = Integer.compare(d2.getPontos(), d1.getPontos());
            if (comparePontos == 0) {
                return Integer.compare(d2.getVitorias(), d1.getVitorias());
            }
            return comparePontos;
        });

        // Configurar o RankAdapter
        adapter = new RankAdapter(duelistas, getContext());
        rvClassificacao.setAdapter(adapter);

        return view;
    }
}