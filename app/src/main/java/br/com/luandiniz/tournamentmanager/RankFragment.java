package br.com.luandiniz.tournamentmanager;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import br.com.luandiniz.tournamentmanager.R;
import br.com.luandiniz.tournamentmanager.adapter.Adapter;
import br.com.luandiniz.tournamentmanager.dao.DAOSQLITE;
import br.com.luandiniz.tournamentmanager.model.Duelista;

public class RankFragment extends Fragment {

    private RecyclerView recyclerView;
    private Adapter adapter;
    private DAOSQLITE daosqlite;

    public RankFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rank, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_duelistas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize DAO and load data
        daosqlite = DAOSQLITE.getInstance(getContext());
        List<Duelista> duelistas = daosqlite.listarDuelistas();

        // Sort duelistas by points and victories
        Collections.sort(duelistas, (d1, d2) -> {
            int comparePontos = Integer.compare(d2.getPontos(), d1.getPontos());
            if (comparePontos == 0) {
                return Integer.compare(d2.getVitorias(), d1.getVitorias());
            }
            return comparePontos;
        });

        // Set up adapter
        adapter = new Adapter(duelistas, getContext());
        recyclerView.setAdapter(adapter);

        return view;
    }
}