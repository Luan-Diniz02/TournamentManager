package br.com.luandiniz.tournamentmanager.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import br.com.luandiniz.tournamentmanager.R;
import br.com.luandiniz.tournamentmanager.dao.DAOSQLITE;
import br.com.luandiniz.tournamentmanager.model.Duelista;
import br.com.luandiniz.tournamentmanager.views.fragment.RankFragment;
import br.com.luandiniz.tournamentmanager.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private SpeedDialView speedDialView;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.bottomNavigation.setSelectedItemId(R.id.rank);

        inicializaViews();
        inicializaListeners();

        // Carregar RankFragment por padrão
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new RankFragment())
                    .commit();
        }
    }

    private void inicializaViews() {
        speedDialView = findViewById(R.id.activity_main_speeddial_menu);

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
                mostrarDialogAdicionarDuelista();
            } else if (actionItem.getId() == R.id.menu_acao2) {
                Toast.makeText(this, "Em desenvolvimento", Toast.LENGTH_SHORT).show();
            } else if (actionItem.getId() == R.id.menu_acao3) {
                Toast.makeText(this, "Em desenvolvimento", Toast.LENGTH_SHORT).show();
            }
            return false; // Fecha o SpeedDial após a ação
        });

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.rank) {
                selectedFragment = new RankFragment();
            } else if (itemId == R.id.historico) {
//                selectedFragment = new HistoricoFragment();
                Toast.makeText(this, "Em desenvolvimento", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.pvp) {
//                selectedFragment = new TorneiosFragment();
                Toast.makeText(this, "Em desenvolvimento", Toast.LENGTH_SHORT).show();

            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }

    private void mostrarDialogAdicionarDuelista() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar Duelista");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_add_duelista, null);

        EditText etNome = dialogView.findViewById(R.id.activity_add_duelista_nome);
        EditText etVitorias = dialogView.findViewById(R.id.activity_add_duelista_vitorias);
        EditText etDerrotas = dialogView.findViewById(R.id.activity_add_duelista_derrotas);
        EditText etEmpates = dialogView.findViewById(R.id.activity_add_duelista_empates);

        builder.setView(dialogView)
                .setPositiveButton("Adicionar", (dialog, which) -> {
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
                    DAOSQLITE.getInstance(this).adicionarDuelista(novoDuelista);

                    // Atualizar o RankFragment
                    atualizarListaDuelistas();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void atualizarListaDuelistas() {
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.container);

        if (currentFragment instanceof RankFragment) {
            ((RankFragment) currentFragment).carregarDuelistas();

            // Opcional: Rolagem para o topo após atualização
            RecyclerView recyclerView = currentFragment.getView().findViewById(R.id.recycler_view_duelistas);
            if (recyclerView != null) {
                recyclerView.scrollToPosition(0);
            }
        }
    }
}