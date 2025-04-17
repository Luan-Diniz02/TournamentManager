package br.com.luandiniz.tournamentmanager.views.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.luandiniz.tournamentmanager.R;
import br.com.luandiniz.tournamentmanager.adapter.Adapter;
import br.com.luandiniz.tournamentmanager.dao.DAOSQLITE;
import br.com.luandiniz.tournamentmanager.model.Duelista;
import br.com.luandiniz.tournamentmanager.model.Torneio;

public class CriarTorneioFragment extends Fragment {

    private EditText nomeTorneio, nomeDuelista, rodadas;
    private Button addDuelista, criarTorneio, addDuelistaHistorico;
    private CheckBox topCut;
    private RecyclerView recyclerView;
    Adapter adapter;
    private List<Duelista> duelistasTorneio;
    private List<Duelista> listaDuelista;
    private DAOSQLITE daosqlite;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_criar_torneio, container, false);

        // No seu CriarTorneioFragment.java
        RecyclerView recyclerView = view.findViewById(R.id.fragment_criar_recycler_duelistas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializando o DAO
        daosqlite = DAOSQLITE.getInstance(requireContext());
        listaDuelista = daosqlite.listarDuelistas();

        // Reaproveitando o adapter existente
        duelistasTorneio = new ArrayList<>(); // Lista vazia inicial
        adapter = new Adapter(duelistasTorneio, getContext());

        // Configurando listeners se necessário
        adapter.setOnItemClickListener((duelista, position) -> {
            // Lógica quando clicar em um duelista
        });

        adapter.setOnItemLongClickListener((duelista, position) -> {
            // Lógica quando pressionar longo em um duelista
        });

        inicializaViews(view);

        addDuelista.setOnClickListener(v -> {
            adicionarDuelista();
        });

        addDuelistaHistorico.setOnClickListener(v -> {
            adicionarDuelistaHistorico();
        });

        criarTorneio.setOnClickListener(v -> {
            criarTorneio();
        });

        recyclerView.setAdapter(adapter);
        return view;
    }

    private void adicionarDuelistaHistorico() {
        if (listaDuelista == null || listaDuelista.isEmpty()) {
            Toast.makeText(getContext(), "Nenhum duelista no histórico", Toast.LENGTH_SHORT).show();
            return;
        }

        // Criar array de nomes e array de seleção
        String[] nomes = new String[listaDuelista.size()];
        boolean[] selecionados = new boolean[listaDuelista.size()];

        for (int i = 0; i < listaDuelista.size(); i++) {
            nomes[i] = listaDuelista.get(i).getNome();
            selecionados[i] = false; // Inicialmente não selecionado
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Selecione os duelistas")
                .setMultiChoiceItems(nomes, selecionados, (dialog, which, isChecked) -> {
                    selecionados[which] = isChecked;
                })
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    int adicionados = 0;
                    for (int i = 0; i < selecionados.length; i++) {
                        if (selecionados[i]) {
                            Duelista d = listaDuelista.get(i);

                            // Cria NOVA instância com estatísticas zeradas
                            Duelista novoDuelista = new Duelista(
                                    d.getNome(),  // Mantém apenas o nome
                                    0,            // Vitórias zeradas
                                    0,            // Derrotas zeradas
                                    0             // Empates zerados
                            );
                            novoDuelista.setId(d.getId()); // Mantém o ID original

                            // Verifica se já não foi adicionado (por nome)
                            boolean jaExiste = false;
                            for (Duelista existente : duelistasTorneio) {
                                if (existente.getNome().equalsIgnoreCase(novoDuelista.getNome())) {
                                    jaExiste = true;
                                    break;
                                }
                            }

                            if (!jaExiste) {
                                duelistasTorneio.add(novoDuelista);
                                adicionados++;
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(),
                            adicionados + " duelistas adicionados",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void inicializaViews(View view) {
        // Inicializando os campos
        nomeTorneio = view.findViewById(R.id.fragment_criar_nome_torneio);
        nomeDuelista = view.findViewById(R.id.fragment_criar_nome_duelista);
        addDuelista = view.findViewById(R.id.fragment_criar_btn_add_duelista);
        criarTorneio = view.findViewById(R.id.fragment_criar_btn_salvar_torneio);
        addDuelistaHistorico = view.findViewById(R.id.fragment_criar_btn_add_duelista_historico);
        rodadas = view.findViewById(R.id.fragment_criar_rodadas);
        topCut = view.findViewById(R.id.fragment_criar_check_top_cut);
    }

    private void adicionarDuelista() {
        String nome = nomeDuelista.getText().toString().trim();

        if (nome.isEmpty()) {
            Toast.makeText(getContext(), "Digite o nome do duelista", Toast.LENGTH_SHORT).show();
            return;
        }

        Duelista duelista = new Duelista(nome, 0, 0, 0);
        duelistasTorneio.add(duelista);
        adapter.notifyItemInserted(duelistasTorneio.size() - 1); // Notifica o adapter
        nomeDuelista.getText().clear();
    }

    private void criarTorneio() {
        String nomeTorneioStr = nomeTorneio.getText().toString().trim();
        String rodadasStr = rodadas.getText().toString().trim();

        if (nomeTorneioStr.isEmpty()) {
            Toast.makeText(getContext(), "Digite o nome do torneio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (duelistasTorneio.isEmpty()) {
            Toast.makeText(getContext(), "Adicione pelo menos um duelista", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rodadasStr.isEmpty()) {
            Toast.makeText(getContext(), "Informe o número de rodadas", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int numRodadas = Integer.parseInt(rodadasStr);

            // Cria o torneio
            Torneio torneio = new Torneio(
                    nomeTorneioStr,
                    new Date(), // Data atual
                    duelistasTorneio.stream().map(Duelista::getId).toList(),
                    topCut.isChecked()
            );
            torneio.setRodadas(numRodadas); // Define o número de rodadas

            // SALVA NO BANCO DE DADOS
            daosqlite.adicionarTorneio(torneio);

            Toast.makeText(getContext(), "Torneio criado com sucesso!", Toast.LENGTH_SHORT).show();

            // Limpa os campos
            nomeTorneio.getText().clear();
            rodadas.getText().clear();
            nomeDuelista.getText().clear();
            topCut.setChecked(false);
            duelistasTorneio.clear();
            adapter.notifyDataSetChanged();

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Número de rodadas inválido", Toast.LENGTH_SHORT).show();
        }
    }
}