package br.com.luandiniz.tournamentmanager.views;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import br.com.luandiniz.tournamentmanager.R;
import br.com.luandiniz.tournamentmanager.RankFragment;
import br.com.luandiniz.tournamentmanager.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private SpeedDialView speedDialView;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
                // TODO: Implementar adição de duelista
            } else if (actionItem.getId() == R.id.menu_acao2) {
                // TODO: Implementar edição de duelista
            } else if (actionItem.getId() == R.id.menu_acao3) {
                // TODO: Implementar remoção de duelista
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
}