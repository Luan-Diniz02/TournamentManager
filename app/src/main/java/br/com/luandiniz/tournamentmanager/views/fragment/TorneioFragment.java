package br.com.luandiniz.tournamentmanager.views.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.luandiniz.tournamentmanager.R;
import br.com.luandiniz.tournamentmanager.adapter.DueloAdapter;
import br.com.luandiniz.tournamentmanager.dao.DAOSQLITE;
import br.com.luandiniz.tournamentmanager.model.Duelista;
import br.com.luandiniz.tournamentmanager.model.Duelo;
import br.com.luandiniz.tournamentmanager.model.Torneio;

public class TorneioFragment extends Fragment implements DueloAdapter.OnResultadoSelecionadoListener {

    private static final String ARG_TORNEIO_ID = "torneio_id";
    private static final String KEY_RODADA_ATUAL = "rodada_atual";
    private static final String KEY_DUELOS_ATUAIS = "duelos_atuais";
    private int torneioId;
    private DAOSQLITE dao;
    private Torneio torneio;
    private List<Duelista> duelistas;
    private List<Duelo> duelosAtuais;
    private int rodadaAtual;
    private RecyclerView rvDuelos;
    private TextView tvRodada;
    private MaterialButton btnSalvar, btnVoltar, btnEstatisticas, btnSair;
    private DueloAdapter adapter;
    private Map<Integer, Set<Integer>> confrontosAnteriores;

    public static TorneioFragment newInstance(int torneioId) {
        TorneioFragment fragment = new TorneioFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TORNEIO_ID, torneioId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            torneioId = getArguments().getInt(ARG_TORNEIO_ID);
        }
        // Restaurar estado
        if (savedInstanceState != null) {
            rodadaAtual = savedInstanceState.getInt(KEY_RODADA_ATUAL, 1);
            duelosAtuais = savedInstanceState.getParcelableArrayList(KEY_DUELOS_ATUAIS);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_RODADA_ATUAL, rodadaAtual);
        outState.putParcelableArrayList(KEY_DUELOS_ATUAIS, new ArrayList<>(duelosAtuais));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_torneio, container, false);

        // Inicializar views
        rvDuelos = view.findViewById(R.id.rv_duelos);
        tvRodada = view.findViewById(R.id.tvRodada);
        btnSalvar = view.findViewById(R.id.btn_salvar);
        btnVoltar = view.findViewById(R.id.btn_voltar);
        btnEstatisticas = view.findViewById(R.id.btn_estatisticas);
        btnSair = view.findViewById(R.id.btn_sair);

        // Configurar RecyclerView
        rvDuelos.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar DAO
        dao = DAOSQLITE.getInstance(requireContext());

        // Carregar torneio e duelistas
        torneio = carregarTorneio(torneioId);
        if (torneio == null) {
            Toast.makeText(getContext(), "Torneio não encontrado", Toast.LENGTH_SHORT).show();
            return view;
        }
        duelistas = dao.listarDuelistasDoTorneio(torneioId);

        // Validar lista de duelistas
        if (!validarDuelistas(duelistas)) {
            Toast.makeText(getContext(), "Erro: Lista de duelistas inválida", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Inicializar confrontos anteriores
        confrontosAnteriores = new HashMap<>();
        for (Duelista d : duelistas) {
            confrontosAnteriores.put(d.getId(), new HashSet<>());
        }

        // Determinar rodada atual
        List<Integer> rodadas = dao.listarRodadas(torneioId);
        if (rodadaAtual == 0) { // Só calcular se não foi restaurado
            rodadaAtual = rodadas.isEmpty() ? 1 : rodadas.size();
        }
        if (rodadaAtual > torneio.getQuantRodadas()) {
            Toast.makeText(getContext(), "Torneio finalizado", Toast.LENGTH_SHORT).show();
            btnSalvar.setEnabled(false);
            return view;
        }

        // Carregar ou restaurar duelos
        if (duelosAtuais == null || duelosAtuais.isEmpty()) {
            duelosAtuais = carregarOuGerarDuelos(rodadaAtual);
        }
        adapter = new DueloAdapter(duelosAtuais, duelistas, this);
        rvDuelos.setAdapter(adapter);

        // Atualizar texto da rodada
        tvRodada.setText(rodadaAtual + "ª Rodada");

        // Configurar botões
        btnSalvar.setOnClickListener(v -> salvarResultados());
        btnVoltar.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        btnEstatisticas.setOnClickListener(v -> exibirEstatisticas());
        btnSair.setOnClickListener(v -> requireActivity().finish());

        return view;
    }

    private boolean validarDuelistas(List<Duelista> duelistas) {
        List<Integer> ids = new ArrayList<>();
        for (Duelista d : duelistas) {
            if (ids.contains(d.getId())) {
                Log.e("TorneioFragment", "Duelista duplicado: " + d.getId() + ", Nome: " + d.getNome());
                return false;
            }
            ids.add(d.getId());
        }
        return true;
    }

    private Torneio carregarTorneio(int torneioId) {
        List<Torneio> torneios = dao.listarTorneios();
        for (Torneio t : torneios) {
            if (t.getId() == torneioId) {
                return t;
            }
        }
        return null;
    }

    private List<Duelo> carregarOuGerarDuelos(int rodada) {
        List<Duelo> duelos = new ArrayList<>();
        List<Integer> rodadas = dao.listarRodadas(torneioId);

        // Carregar duelos existentes
        if (rodada <= rodadas.size()) {
            duelos = dao.listarDuelosPorRodada(rodadas.get(rodada - 1), torneioId);
            Log.d("TorneioFragment", "Carregados " + duelos.size() + " duelos para rodada " + rodada);
            // Restaurar confrontos anteriores
            for (Duelo duelo : duelos) {
                confrontosAnteriores.get(duelo.getIdDuelista1()).add(duelo.getIdDuelista2());
                confrontosAnteriores.get(duelo.getIdDuelista2()).add(duelo.getIdDuelista1());
            }
        } else if (rodada == 1) {
            // Gerar duelos para a primeira rodada (aleatoriamente)
            List<Duelista> duelistasEmbaralhados = new ArrayList<>(duelistas);
            Collections.shuffle(duelistasEmbaralhados);
            int idRodada = dao.adicionarRodada(torneioId, "");
            for (int i = 0; i < duelistasEmbaralhados.size() - 1; i += 2) {
                int idDuelista1 = duelistasEmbaralhados.get(i).getId();
                int idDuelista2 = duelistasEmbaralhados.get(i + 1).getId();
                if (idDuelista1 != idDuelista2) {
                    Duelo duelo = new Duelo(idRodada, idDuelista1, idDuelista2);
                    long dueloId = dao.adicionarDuelo(idRodada, duelo.getIdDuelista1(), duelo.getIdDuelista2(), null);
                    duelo.setId((int) dueloId);
                    duelos.add(duelo);
                    // Atualizar confrontos anteriores
                    confrontosAnteriores.get(idDuelista1).add(idDuelista2);
                    confrontosAnteriores.get(idDuelista2).add(idDuelista1);
                } else {
                    Log.e("TorneioFragment", "Tentativa de emparelhar duelista com ele mesmo: " + idDuelista1);
                }
            }
            dao.atualizarRodada(idRodada, duelos.toString());
        }
        return duelos;
    }

    @Override
    public void onResultadoSelecionado(int posicaoDuelo, String resultado) {
        Duelo duelo = duelosAtuais.get(posicaoDuelo);
        Duelista duelista1 = null, duelista2 = null;
        for (Duelista d : duelistas) {
            if (d.getId() == duelo.getIdDuelista1()) {
                duelista1 = d;
            } else if (d.getId() == duelo.getIdDuelista2()) {
                duelista2 = d;
            }
        }

        if (duelista1 == null || duelista2 == null) {
            Toast.makeText(getContext(), "Erro: Duelista não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Atualizar estatísticas dos duelistas
        switch (resultado) {
            case "VITORIA_A":
                duelista1.setVitorias(duelista1.getVitorias() + 1);
                duelista1.setPontos(duelista1.getPontos() + 3);
                duelista2.setDerrotas(duelista2.getDerrotas() + 1);
                duelo.setIdVencedor(duelista1.getId());
                break;
            case "VITORIA_B":
                duelista2.setVitorias(duelista2.getVitorias() + 1);
                duelista2.setPontos(duelista2.getPontos() + 3);
                duelista1.setDerrotas(duelista1.getDerrotas() + 1);
                duelo.setIdVencedor(duelista2.getId());
                break;
            case "EMPATE":
                duelista1.setEmpates(duelista1.getEmpates() + 1);
                duelista1.setPontos(duelista1.getPontos() + 1);
                duelista2.setEmpates(duelista2.getEmpates() + 1);
                duelista2.setPontos(duelista2.getPontos() + 1);
                duelo.setIdVencedor(null);
                break;
        }

        // Atualizar duelo no banco
        dao.atualizarDuelo(duelo.getId(), duelo.getIdVencedor());
    }

    private void salvarResultados() {
        // Verificar se todos os duelos têm resultado
        for (Duelo duelo : duelosAtuais) {
            if (duelo.getIdDuelista1() == 0 || duelo.getIdDuelista2() == 0) {
                Toast.makeText(getContext(), "Erro: Duelo inválido detectado", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Atualizar duelistas no banco
        for (Duelista duelista : duelistas) {
            dao.atualizarDuelista(duelista);
        }

        // Avançar para a próxima rodada
        rodadaAtual++;
        if (rodadaAtual > torneio.getQuantRodadas()) {
            // Finalizar torneio e exibir classificação
            Duelista campeao = determinarCampeao();
            if (campeao != null) {
                torneio.setIdCampeao(campeao.getId());
                dao.atualizarTorneio(torneio);
                Toast.makeText(getContext(), "Torneio finalizado! Campeão: " + campeao.getNome(), Toast.LENGTH_LONG).show();
            }
            btnSalvar.setEnabled(false);

            // Navegar para o ClassificacaoFragment
            Fragment classificacaoFragment = ClassificacaoFragment.newInstance(torneioId);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, classificacaoFragment)
                    .addToBackStack(null)
                    .commit();
            return;
        }

        // Gerar duelos para a próxima rodada usando o sistema suíço
        duelosAtuais.clear();
        List<Duelista> duelistasOrdenados = new ArrayList<>(duelistas);
        duelistasOrdenados.sort((d1, d2) -> Integer.compare(d2.getPontos(), d1.getPontos())); // Ordenar por pontos (descendente)

        List<Duelista> duelistasNaoPareados = new ArrayList<>(duelistasOrdenados);
        List<Duelo> novosDuelos = new ArrayList<>();
        int idRodada = dao.adicionarRodada(torneioId, "");

        while (duelistasNaoPareados.size() >= 2) {
            Duelista duelista1 = duelistasNaoPareados.get(0);
            Duelista duelista2 = null;

            // Encontrar um oponente com pontuação próxima e que ainda não foi enfrentado
            for (int i = 1; i < duelistasNaoPareados.size(); i++) {
                Duelista possivelOponente = duelistasNaoPareados.get(i);
                if (!confrontosAnteriores.get(duelista1.getId()).contains(possivelOponente.getId())) {
                    duelista2 = possivelOponente;
                    break;
                }
            }

            if (duelista2 == null && !duelistasNaoPareados.isEmpty()) {
                // Se não houver oponente não enfrentado, pegar o próximo disponível (pode ser ajustado para bye)
                duelista2 = duelistasNaoPareados.get(1);
            }

            if (duelista2 != null) {
                Duelo duelo = new Duelo(idRodada, duelista1.getId(), duelista2.getId());
                long dueloId = dao.adicionarDuelo(idRodada, duelo.getIdDuelista1(), duelo.getIdDuelista2(), null);
                duelo.setId((int) dueloId);
                novosDuelos.add(duelo);
                // Atualizar confrontos anteriores
                confrontosAnteriores.get(duelista1.getId()).add(duelista2.getId());
                confrontosAnteriores.get(duelista2.getId()).add(duelista1.getId());
                // Remover os duelistas pareados
                duelistasNaoPareados.remove(duelista1);
                duelistasNaoPareados.remove(duelista2);
            } else {
                break; // Não há mais oponentes disponíveis
            }
        }

        duelosAtuais.addAll(novosDuelos);
        dao.atualizarRodada(idRodada, duelosAtuais.toString());

        // Atualizar UI
        adapter.notifyDataSetChanged();
        tvRodada.setText(rodadaAtual + "ª Rodada");
        Toast.makeText(getContext(), "Resultados salvos! Avançando para a próxima rodada", Toast.LENGTH_SHORT).show();
    }

    private void exibirEstatisticas() {
        Fragment classificacaoFragment = ClassificacaoFragment.newInstance(torneioId);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, classificacaoFragment)
                .addToBackStack(null)
                .commit();
    }

    private Duelista determinarCampeao() {
        Duelista campeao = null;
        int maxPontos = -1;
        for (Duelista d : duelistas) {
            if (d.getPontos() > maxPontos) {
                maxPontos = d.getPontos();
                campeao = d;
            }
        }
        return campeao;
    }
}