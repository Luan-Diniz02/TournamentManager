package br.com.luandiniz.tournamentmanager.views.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.luandiniz.tournamentmanager.PDFGenerator;
import br.com.luandiniz.tournamentmanager.PDFUtils;
import br.com.luandiniz.tournamentmanager.R;
import br.com.luandiniz.tournamentmanager.adapter.RankAdapter;
import br.com.luandiniz.tournamentmanager.dao.DAOSQLITE;
import br.com.luandiniz.tournamentmanager.model.Duelista;

public class RankFragment extends Fragment {

    private RecyclerView recyclerView;
    private RankAdapter rankAdapter;
    private DAOSQLITE daosqlite;
    private List<Duelista> duelistas;
    private SpeedDialView speedDialView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rank, container, false);

        // Inicializa o DAO
        carregarDuelistas();

        inicializaView(view);

        inicializaListener();

        // Configura o ItemTouchHelper para swipe
        setupItemTouchHelper();

        return view;
    }

    private void inicializaListener() {
        speedDialView.setOnActionSelectedListener(actionItem -> {
            if (actionItem.getId() == R.id.menu_acao1) {
                dialogAdicionarDuelista();
            }
            else if (actionItem.getId() == R.id.menu_acao2) {
                // Verifica se a lista de duelistas está vazia
                if(duelistas.isEmpty() || duelistas.size() == 0) {
                    Toast.makeText(getContext(), "Não há duelistas para remover", Toast.LENGTH_SHORT).show();
                }
                else{
                    // Limpar o rank
                    new AlertDialog.Builder(getContext())
                            .setTitle("Limpar Rank")
                            .setMessage("Tem certeza que deseja limpar o rank?")
                            .setPositiveButton("Sim", (dialog, which) -> {
                                for(Duelista duelista : duelistas) {
                                    daosqlite.removerDuelista(duelista.getId());
                                }
                                carregarDuelistas();
                                Toast.makeText(getContext(), "Rank resetado", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                }
            }
            else if (actionItem.getId() == R.id.menu_acao3) {
                gerarPDF();
                return true;
            }
            return false; // Fecha o SpeedDial após a ação
        });
    }

    private void inicializaView(View view) {
        // Inicializa RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_duelistas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializa SpeedDialView
        speedDialView = view.findViewById(R.id.rank_fragment_speeddial_menu);

        speedDialView.setVisibility(View.VISIBLE);

        speedDialView.setMainFabClosedDrawable(getResources().getDrawable(R.drawable.ic_barra));
        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.menu_acao1, R.drawable.ic_add)
                .setLabel("Adicionar")
                .setFabBackgroundColor(getResources().getColor(R.color.white))
                .create());
        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.menu_acao2, R.drawable.ic_remove)
                .setLabel("Limpar Rank")
                .setFabBackgroundColor(getResources().getColor(R.color.white))
                .create());
        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.menu_acao3, R.drawable.ic_pdf)
                .setLabel("Gerar PDF")
                .setFabBackgroundColor(getResources().getColor(R.color.white))
                .create());

        // Configura o adapter
        rankAdapter = new RankAdapter(duelistas, getContext());
        rankAdapter.setOnItemClickListener((duelista, position) -> {
            abrirOpcoesDuelista(duelista, position);
        });
        recyclerView.setAdapter(rankAdapter);
    }

    private void dialogAdicionarDuelista() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Adicionar Duelista");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.activity_add_duelista, null);

        EditText etNome = dialogView.findViewById(R.id.activity_add_duelista_nome);
        EditText etVitorias = dialogView.findViewById(R.id.activity_add_duelista_vitorias);
        EditText etDerrotas = dialogView.findViewById(R.id.activity_add_duelista_derrotas);
        EditText etEmpates = dialogView.findViewById(R.id.activity_add_duelista_empates);

        builder.setView(dialogView)
                .setPositiveButton("Adicionar", (dialog, which) -> {

                    try {
                        String nome = etNome.getText().toString();
                        int vitorias = Integer.parseInt(etVitorias.getText().toString());
                        int derrotas = Integer.parseInt(etDerrotas.getText().toString());
                        int empates = Integer.parseInt(etEmpates.getText().toString());

                        Duelista novoDuelista = new Duelista(
                                nome,
                                vitorias,
                                derrotas,
                                empates
                        );
                        // Adicionar no banco de dados
                        DAOSQLITE.getInstance(getContext()).adicionarDuelista(novoDuelista);

                        // Atualizar o RankFragment
                        atualizarListaDuelistas();
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Por favor, insira valores válidos", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Erro ao adicionar duelista: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void atualizarListaDuelistas() {
        Fragment currentFragment = getParentFragmentManager().findFragmentById(R.id.container);
        if (currentFragment instanceof RankFragment) {
            ((RankFragment) currentFragment).carregarDuelistas();
        }
    }

    public void carregarDuelistas() {
        // Inicializa DAO e carrega dados
        daosqlite = DAOSQLITE.getInstance(requireContext());

        duelistas = daosqlite.listarDuelistas();
        if (duelistas == null) {
            duelistas = Collections.emptyList();
        }
        ordenarDuelistas();

        if (rankAdapter != null) {
            rankAdapter.atualizarLista(duelistas);
        }
    }

    private void ordenarDuelistas() {
        Collections.sort(duelistas);
    }

    private void dialogEdicao(Duelista duelista, int position) {
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
            try {
                // Verifica se o nome está vazio
                if (editNome.getText().toString().trim().isEmpty()) {
                    Toast.makeText(requireContext(), "O nome não pode estar vazio.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Atualiza os dados do duelista
                duelista.setNome(editNome.getText().toString());
                duelista.setVitorias(Integer.parseInt(editVitorias.getText().toString()));
                duelista.setDerrotas(Integer.parseInt(editDerrotas.getText().toString()));
                duelista.setEmpates(Integer.parseInt(editEmpates.getText().toString()));
                duelista.setParticipacao(Integer.parseInt(editParticipacao.getText().toString()));

                // Atualiza no banco de dados
                daosqlite.atualizarDuelista(duelista);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Por favor, insira valores válidos.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Erro ao atualizar duelista: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }


            // Atualiza a lista
            carregarDuelistas();
        });

        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
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
                                rankAdapter.notifyItemChanged(position);
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
                            dialogAdicionarPontos(duelista, position);
                            break;
                        case 1: // Editar Dados
                            dialogEdicao(duelista, position);
                            break;
                        case 2: // Cancelar
                            dialog.dismiss();
                            break;
                    }
                })
                .show();
    }

    private void dialogAdicionarPontos(Duelista duelista, int position) {
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
                try {
                    // Obtém os valores a serem adicionados
                    int vitoriasAdd = Integer.parseInt(addVitorias.getText().toString());
                    int derrotasAdd = Integer.parseInt(addDerrotas.getText().toString());
                    int empatesAdd = Integer.parseInt(addEmpates.getText().toString());

                    // Atualiza os valores do duelista
                    duelista.setVitorias(duelista.getVitorias() + vitoriasAdd);
                    duelista.setDerrotas(duelista.getDerrotas() + derrotasAdd);
                    duelista.setEmpates(duelista.getEmpates() + empatesAdd);
                    duelista.setParticipacao(duelista.getParticipacao() + 1);

                    // Atualiza no banco de dados
                    daosqlite.atualizarDuelista(duelista);
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Por favor, insira valores válidos", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Erro ao atualizar duelista: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                // Atualiza a lista
                carregarDuelistas();
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();
        }

    // Adicione este método para gerar o PDF:
    private void gerarPDF() {
        if (duelistas.isEmpty()) {
            Toast.makeText(getContext(), "Não há duelistas para gerar o PDF", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            PDFGenerator pdfGenerator = new PDFGenerator(requireContext());
            File pdfFile = pdfGenerator.generateRankPDF(duelistas, "Tournament Manager");

            mostrarDialogoSucesso(pdfFile);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Erro ao gerar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoSucesso(File file) {
        new AlertDialog.Builder(requireContext())
                .setTitle("PDF Gerado com Sucesso")
                .setMessage("O PDF foi salvo em:\n" + file.getAbsolutePath())
                .setPositiveButton("Abrir", (dialog, which) -> abrirPDF(file))
                .setNegativeButton("Compartilhar", (dialog, which) -> compartilharPDF(file))
                .setNeutralButton("OK", null)
                .show();
    }

    private void abrirPDF(File file) {
        PDFUtils.abrirPDF(requireContext(), file);
    }

    private void compartilharPDF(File file) {
        PDFUtils.compartilharPDF(requireContext(), file);
    }
    }
