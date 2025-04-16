package br.com.luandiniz.tournamentmanager.views.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private List<Duelista> duelistas;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rank, container, false);

        // Inicializa RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_duelistas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializa DAO e carrega dados
        daosqlite = DAOSQLITE.getInstance(requireContext());
        carregarDuelistas();

        // Configura o adapter
        adapter = new Adapter(duelistas, getContext());
        adapter.setOnItemClickListener((duelista, position) -> {
            abrirOpcoesDuelista(duelista, position);
        });
        recyclerView.setAdapter(adapter);

        // Configura o ItemTouchHelper para swipe
        setupItemTouchHelper();

        return view;
    }

    public void carregarDuelistas() {
        duelistas = daosqlite.listarDuelistas();
        if (duelistas == null) {
            duelistas = Collections.emptyList();
        }
        ordenarDuelistas();

        if (adapter != null) {
            adapter.atualizarLista(duelistas);
        }
    }

    private void ordenarDuelistas() {
        Collections.sort(duelistas, (d1, d2) -> {
            int comparePontos = Integer.compare(d2.getPontos(), d1.getPontos());
            if (comparePontos == 0) {
                return Integer.compare(d2.getVitorias(), d1.getVitorias());
            }
            return comparePontos;
        });
    }

    private void abrirDialogEdicao(Duelista duelista, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Editar Duelista");

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.activity_edit_duelista, null);

        EditText editNome = dialogView.findViewById(R.id.activity_edit_nome);
        EditText editVitorias = dialogView.findViewById(R.id.activity_edit_vitorias);
        EditText editDerrotas = dialogView.findViewById(R.id.activity_edit_derrotas);
        EditText editEmpates = dialogView.findViewById(R.id.activity_edit_empates);
        EditText editParticipacao = dialogView.findViewById(R.id.activity_edit_participacao);


        // Preenche os campos com os dados atuais
        editNome.setText(duelista.getNome());
        editVitorias.setText(String.valueOf(duelista.getVitorias()));
        editDerrotas.setText(String.valueOf(duelista.getDerrotas()));
        editEmpates.setText(String.valueOf(duelista.getEmpates()));
        editParticipacao.setText(String.valueOf(duelista.getParticipacao()));

        builder.setView(dialogView);

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            // Atualiza os dados do duelista
            duelista.setNome(editNome.getText().toString());
            duelista.setVitorias(Integer.parseInt(editVitorias.getText().toString()));
            duelista.setDerrotas(Integer.parseInt(editDerrotas.getText().toString()));
            duelista.setEmpates(Integer.parseInt(editEmpates.getText().toString()));
            duelista.setParticipacao(Integer.parseInt(editParticipacao.getText().toString()));

            // Recalcula pontos (se necessário)
            duelista.setPontos(calcularPontos(duelista));

            // Atualiza no banco de dados
            daosqlite.atualizarDuelista(duelista);

            // Atualiza a lista
            carregarDuelistas();
        });

        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    private int calcularPontos(Duelista duelista) {
        // Implemente sua lógica de cálculo de pontos aqui
        // Exemplo simples: 3 pontos por vitória, 1 por empate
        return duelista.getVitorias() * 3 + duelista.getEmpates() + duelista.getParticipacao();
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
                if (position != RecyclerView.NO_POSITION && position < duelistas.size()) {
                    Duelista duelistaRemovido = duelistas.get(position);

                    new AlertDialog.Builder(requireContext())
                            .setTitle("Remover Duelista")
                            .setMessage("Tem certeza que deseja remover este duelista?")
                            .setPositiveButton("Sim", (dialog, which) -> {
                                if (daosqlite.removerDuelista(duelistaRemovido.getId())) {
                                    duelistas.remove(position);
                                    carregarDuelistas();
                                }
                            })
                            .setNegativeButton("Cancelar", (dialog, which) -> {
                                adapter.notifyItemChanged(position);
                            })
                            .setCancelable(false)
                            .show();
                }
            }

        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    private void abrirOpcoesDuelista(Duelista duelista, int position) {
        String[] opcoes = {"Adicionar Pontos","Editar Dados", "Cancelar"};

        new AlertDialog.Builder(requireContext())
                .setTitle(duelista.getNome())
                .setItems(opcoes, (dialog, which) -> {
                    switch (which) {
                        case 0: // Adicionar Pontos
                            abrirDialogAdicionarPontos(duelista, position);
                            break;
                        case 1: // Editar Dados
                            abrirDialogEdicao(duelista, position);
                            break;
                        case 2: // Cancelar
                            dialog.dismiss();
                            break;
                    }
                })
                .show();
    }

    private void abrirDialogAdicionarPontos(Duelista duelista, int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Adicionar Pontos para " + duelista.getNome());

            View dialogView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.activity_edit_duelista, null);

            EditText addNome = dialogView.findViewById(R.id.activity_edit_nome);
            EditText addVitorias = dialogView.findViewById(R.id.activity_edit_vitorias);
            EditText addDerrotas = dialogView.findViewById(R.id.activity_edit_derrotas);
            EditText addEmpates = dialogView.findViewById(R.id.activity_edit_empates);
            EditText addParticipacao = dialogView.findViewById(R.id.activity_edit_participacao);

            addNome.setVisibility(View.GONE);
            addParticipacao.setVisibility(View.GONE);

            builder.setView(dialogView);

            builder.setPositiveButton("Adicionar", (dialog, which) -> {
                // Obtém os valores a serem adicionados
                int vitoriasAdd = Integer.parseInt(addVitorias.getText().toString());
                int derrotasAdd = Integer.parseInt(addDerrotas.getText().toString());
                int empatesAdd = Integer.parseInt(addEmpates.getText().toString());

                // Atualiza os valores do duelista
                duelista.setVitorias(duelista.getVitorias() + vitoriasAdd);
                duelista.setDerrotas(duelista.getDerrotas() + derrotasAdd);
                duelista.setEmpates(duelista.getEmpates() + empatesAdd);
                duelista.setParticipacao(duelista.getParticipacao() + 1);
                duelista.setPontos(calcularPontos(duelista));

                // Atualiza no banco de dados
                daosqlite.atualizarDuelista(duelista);

                // Atualiza a lista
                carregarDuelistas();
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();
        }
    }
