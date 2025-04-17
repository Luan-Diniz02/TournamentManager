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
import java.util.Collections;
import java.util.List;

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
        // Usar o layout fragment_rank.xml
        View view = inflater.inflate(R.layout.fragment_rank, container, false);

        rvClassificacao = view.findViewById(R.id.recycler_view_duelistas);
        rvClassificacao.setLayoutManager(new LinearLayoutManager(getContext()));

        dao = DAOSQLITE.getInstance(requireContext());
        duelistas = dao.listarDuelistasDoTorneio(torneioId);

        // Ordenar duelistas por pontos (descendente), com desempate por vitórias
        Collections.sort(duelistas, (d1, d2) -> {
            int comparePontos = Integer.compare(d2.getPontos(), d1.getPontos());
            if (comparePontos == 0) {
                return Integer.compare(d2.getVitorias(), d1.getVitorias());
            }
            return comparePontos;
        });

        // Configurar o RankAdapter sem listeners de clique ou swipe
        adapter = new RankAdapter(duelistas, getContext());
        rvClassificacao.setAdapter(adapter);

        return view;
    }
}