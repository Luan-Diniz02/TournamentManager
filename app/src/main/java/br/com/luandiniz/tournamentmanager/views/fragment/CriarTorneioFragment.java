package br.com.luandiniz.tournamentmanager.views.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import br.com.luandiniz.tournamentmanager.R;
import br.com.luandiniz.tournamentmanager.adapter.RankAdapter;
import br.com.luandiniz.tournamentmanager.dao.DAOSQLITE;
import br.com.luandiniz.tournamentmanager.model.Duelista;
import br.com.luandiniz.tournamentmanager.model.Torneio;

public class CriarTorneioFragment extends Fragment {

    private EditText nomeTorneio, nomeDuelista, rodadas;
    private MaterialButton addDuelista, criarTorneio, addDuelistaHistorico;
    private CheckBox topCut;
    RankAdapter rankAdapter;
    private List<Duelista> duelistasTorneio;
    private List<Duelista> listaDuelista;
    private DAOSQLITE daosqlite;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_criar_torneio, container, false);

        carregaListaDuelista();

        inicializaViews(view);

        inicializaListener();

        setupItemTouchHelper();

        return view;
    }

    private void carregaListaDuelista() {
        // Inicializando o DAO
        daosqlite = DAOSQLITE.getInstance(requireContext());
        listaDuelista = daosqlite.listarDuelistas();
    }

    private void inicializaListener() {
        rankAdapter.setOnItemClickListener((duelista, position) -> {
            // Ação ao clicar no item
        });

        addDuelista.setOnClickListener(v -> {
            adicionarDuelista();
        });

        addDuelistaHistorico.setOnClickListener(v -> {
            adicionarDuelistaHistorico();
        });

        criarTorneio.setOnClickListener(v -> {
            criarTorneio();
        });
    }

    private void inicializaViews(View view) {
        // Inicializando os campos
        recyclerView = view.findViewById(R.id.fragment_criar_recycler_duelistas);
        nomeTorneio = view.findViewById(R.id.fragment_criar_nome_torneio);
        nomeDuelista = view.findViewById(R.id.fragment_criar_nome_duelista);
        addDuelista = view.findViewById(R.id.fragment_criar_btn_add_duelista);
        criarTorneio = view.findViewById(R.id.fragment_criar_btn_salvar_torneio);
        addDuelistaHistorico = view.findViewById(R.id.fragment_criar_btn_add_duelista_historico);
        rodadas = view.findViewById(R.id.fragment_criar_rodadas);
        topCut = view.findViewById(R.id.fragment_criar_check_top_cut);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializando o adapter
        duelistasTorneio = new ArrayList<>();
        rankAdapter = new RankAdapter(duelistasTorneio, getContext());
        recyclerView.setAdapter(rankAdapter);
    }

    private void adicionarDuelista() {
        String nome = nomeDuelista.getText().toString().trim();

        if (nome.isEmpty()) {
            Toast.makeText(getContext(), "Digite o nome do duelista", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verifica se o duelista já existe na lista geral
        Duelista existente = null;
        for (Duelista d : listaDuelista) {
            if (d.getNome().equalsIgnoreCase(nome)) {
                existente = d;
                break;
            }
        }

        Duelista duelista;
        // Usa o duelista existente
        // Cria novo duelista (sem ID ainda)
        duelista = Objects.requireNonNullElseGet(existente, () -> new Duelista(nome));

        // Verifica se já não foi adicionado ao torneio
        for (Duelista d : duelistasTorneio) {
            if (d.getId() > 0 && d.getId() == duelista.getId()) {
                Toast.makeText(getContext(), "Duelista já adicionado", Toast.LENGTH_SHORT).show();
                return;
            }
            if (d.getNome().equalsIgnoreCase(duelista.getNome())) {
                Toast.makeText(getContext(), "Duelista já adicionado", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        duelistasTorneio.add(duelista);
        rankAdapter.notifyItemInserted(duelistasTorneio.size() - 1);
        nomeDuelista.getText().clear();
    }

    private void adicionarDuelistaHistorico() {
        if (listaDuelista == null || listaDuelista.isEmpty()) {
            Toast.makeText(getContext(), "Nenhum duelista no histórico", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] nomes = new String[listaDuelista.size()];
        boolean[] selecionados = new boolean[listaDuelista.size()];

        for (int i = 0; i < listaDuelista.size(); i++) {
            nomes[i] = listaDuelista.get(i).getNome();
            selecionados[i] = false;
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
                            Duelista d = new Duelista(listaDuelista.get(i).getNome());
                            d.setId(listaDuelista.get(i).getId()); // Adiciona o ID do duelista existente

                            // Verifica se já não foi adicionado (por ID agora, não por nome)
                            boolean jaExiste = false;
                            for (Duelista existente : duelistasTorneio) {
                                if (existente.getId() == d.getId()) {
                                    jaExiste = true;
                                    break;
                                }
                            }

                            if (!jaExiste) {
                                // Adiciona o duelista diretamente (com ID existente)
                                duelistasTorneio.add(d);
                                adicionados++;
                            }
                        }
                    }
                    rankAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(),
                            adicionados + " duelistas adicionados",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
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

        if (rodadasStr.isEmpty() || Integer.parseInt(rodadasStr) <= 0) {
            Toast.makeText(getContext(), "Informe um número válido de rodadas", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int numRodadas = Integer.parseInt(rodadasStr);

            // Cria o torneio
            Torneio torneio = new Torneio(
                    nomeTorneioStr,
                    new Date(),
                    new ArrayList<>(),
                    topCut.isChecked()
            );
            torneio.setQuantRodadas(numRodadas);
            torneio.setIdCampeao(0); // Garante que idCampeao seja 0 ao criar

            // SALVA NO BANCO DE DADOS
            long idTorneio = daosqlite.adicionarTorneio(torneio);

            // Adiciona os duelistas à tabela TorneioDuelista
            for (Duelista duelista : duelistasTorneio) {
                if (duelista.getId() <= 0) {
                    // Insere o duelista e obtém o ID gerado
                    long idDuelista = daosqlite.adicionarDuelista(duelista);
                    duelista.setId((int) idDuelista); // Atualiza o ID no objeto
                }
                // Associa ao torneio
                daosqlite.adicionarDuelistaAoTorneio((int) idTorneio, duelista.getId());
            }

            Toast.makeText(getContext(), "Torneio criado com sucesso!", Toast.LENGTH_SHORT).show();

            // Limpa os campos
            nomeTorneio.getText().clear();
            rodadas.getText().clear();
            nomeDuelista.getText().clear();
            topCut.setChecked(false);
            duelistasTorneio.clear();
            rankAdapter.notifyDataSetChanged();

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Número de rodadas inválido", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Erro ao criar torneio: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void setupItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < duelistasTorneio.size()) {
                    Duelista duelistaRemovido = duelistasTorneio.get(position);

                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("Remover Duelista")
                            .setMessage("Tem certeza que deseja remover este duelista?")
                            .setPositiveButton("Sim", (dialog, which) -> {
                                    duelistasTorneio.remove(position);
                                    carregaListaDuelista();
                                    rankAdapter.atualizarLista(duelistasTorneio);
                            })
                            .setNegativeButton("Cancelar", (dialog, which) -> {
                                rankAdapter.notifyItemChanged(position);
                            })
                            .setCancelable(false)
                            .show();
                }
            }

        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

}