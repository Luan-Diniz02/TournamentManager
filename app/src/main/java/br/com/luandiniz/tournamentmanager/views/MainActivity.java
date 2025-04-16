package br.com.luandiniz.tournamentmanager.views;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.Collections;
import java.util.List;

import br.com.luandiniz.tournamentmanager.R;
import br.com.luandiniz.tournamentmanager.adapter.Adapter;
import br.com.luandiniz.tournamentmanager.dao.DAOSQLITE;
import br.com.luandiniz.tournamentmanager.model.Duelista;

public class MainActivity extends AppCompatActivity {

    private SpeedDialView speedDialView;
    private RecyclerView recyclerView;
    private Adapter adapter;
    private DAOSQLITE daosqlite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicializaViews();
        inicializaListeners();
        daosqlite = DAOSQLITE.getInstance(this);
        verificaEAdicionaDuelistasExemplo();
        configurarRecyclerView();
    }

    private void inicializaViews() {
        speedDialView = findViewById(R.id.activity_main_speeddial_menu);
        recyclerView = findViewById(R.id.recycler_view_duelistas);

        speedDialView.setMainFabClosedDrawable(getDrawable(R.drawable.ic_barra));
        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.menu_acao1, R.drawable.ic_add)
                .setLabel("Adicionar")
                .setFabBackgroundColor(getResources().getColor(R.color.white))
                .create());
        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.menu_acao2, R.drawable.ic_edit)
                .setLabel("Editar")
                .setFabBackgroundColor(getResources().getColor(R.color.white))
                .create());
        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.menu_acao3, R.drawable.ic_remove)
                .setLabel("Remover")
                .setFabBackgroundColor(getResources().getColor(R.color.white))
                .create());
    }

    private void inicializaListeners() {
        speedDialView.setOnActionSelectedListener(actionItem -> {
            if (actionItem.getId() == R.id.menu_acao1) {
                // TODO: Implementar adição de duelista (ex.: abrir Activity ou Dialog)
            } else if (actionItem.getId() == R.id.menu_acao2) {
                // TODO: Implementar edição de duelista
            } else if (actionItem.getId() == R.id.menu_acao3) {
                // TODO: Implementar remoção de duelista
            }
            return false; // Fecha o SpeedDial após a ação
        });
    }

    private void verificaEAdicionaDuelistasExemplo() {
        if (daosqlite.listarDuelistas().isEmpty()) {
            daosqlite.adicionarDuelista(new Duelista("Duelista 1", 5, 2, 1));
            daosqlite.adicionarDuelista(new Duelista("Duelista 2", 3, 4, 0));
            daosqlite.adicionarDuelista(new Duelista("Duelista 3", 6, 1, 2));
        }
    }

    private void configurarRecyclerView() {
        List<Duelista> duelistas = daosqlite.listarDuelistas();
        // Ordenar por pontos em ordem decrescente
        Collections.sort(duelistas, (d1, d2) -> {
            int comparePontos = Integer.compare(d2.getPontos(), d1.getPontos());
            if (comparePontos == 0) {
                // Desempate por vitórias
                return Integer.compare(d2.getVitorias(), d1.getVitorias());
            }
            return comparePontos;
        });
        adapter = new Adapter(duelistas, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}