package br.com.luandiniz.tournamentmanager.views.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.com.luandiniz.tournamentmanager.R;
import br.com.luandiniz.tournamentmanager.adapter.HistoricoTorneioAdapter;
import br.com.luandiniz.tournamentmanager.dao.DAOSQLITE;
import br.com.luandiniz.tournamentmanager.model.Duelista;
import br.com.luandiniz.tournamentmanager.model.Torneio;

public class HistoricoFragment extends Fragment implements HistoricoTorneioAdapter.OnTorneioClickListener {

    private RecyclerView recyclerView;
    private TextView tvEmptyView;
    private HistoricoTorneioAdapter adapter;
    private DAOSQLITE dao;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historico, container, false);

        recyclerView = view.findViewById(R.id.rv_historico_torneios);
        tvEmptyView = view.findViewById(R.id.tv_empty_view);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Obter dados do banco
        dao = DAOSQLITE.getInstance(requireContext());
        List<Torneio> torneios = dao.listarTorneios();
        List<Duelista> duelistas = dao.listarDuelistas();

        Log.d("HistoricoFragment", "Torneios carregados: " + torneios.size());
        for (Torneio t : torneios) {
            Log.d("HistoricoFragment", "Torneio: " + t.getNome() + ", Duelistas: " + t.getDuelistas().size());
        }

        adapter = new HistoricoTorneioAdapter(torneios, duelistas, this);
        adapter.setOnTorneioClickListener(torneio -> {
            TorneioFragment fragment = TorneioFragment.newInstance(torneio.getId());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment) // Ajuste o ID do contêiner
                    .addToBackStack(null)
                    .commit();
        });
        recyclerView.setAdapter(adapter);

        // Mostrar mensagem se não houver torneios
        if (torneios.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyView.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onTorneioClick(Torneio torneio) {
        TorneioFragment fragment = TorneioFragment.newInstance(torneio.getId());
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }
}