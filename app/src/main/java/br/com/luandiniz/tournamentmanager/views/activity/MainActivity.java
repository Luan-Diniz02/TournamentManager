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
import br.com.luandiniz.tournamentmanager.views.fragment.CriarTorneioFragment;
import br.com.luandiniz.tournamentmanager.views.fragment.HistoricoFragment;
import br.com.luandiniz.tournamentmanager.views.fragment.RankFragment;
import br.com.luandiniz.tournamentmanager.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.bottomNavigation.setSelectedItemId(R.id.rank);

        inicializaListeners();

        // Carregar RankFragment por padrão
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new RankFragment())
                    .commit();
        }
    }

    private void inicializaListeners() {

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.rank) {
                selectedFragment = new RankFragment();
            } else if (itemId == R.id.historico) {
                selectedFragment = new HistoricoFragment();
            } else if (itemId == R.id.pvp) {
                selectedFragment = new CriarTorneioFragment();
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