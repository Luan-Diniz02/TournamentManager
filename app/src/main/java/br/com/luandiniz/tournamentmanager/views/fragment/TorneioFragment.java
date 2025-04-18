package br.com.luandiniz.tournamentmanager.views.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private static final String KEY_RODADA_VISUALIZADA = "rodada_visualizada";
    private static final String KEY_FASE_TOP_CUT = "fase_top_cut";
    private static final int TOP_CUT_SIZE = 4;
    private static final int FASE_NORMAL = 0;
    private static final int FASE_SEMI_FINAL = 1;
    private static final int FASE_FINAL = 2;

    private int torneioId;
    private DAOSQLITE dao;
    private Torneio torneio;
    private List<Duelista> duelistas;
    private List<Duelo> duelosAtuais;
    private int rodadaAtual;
    private int rodadaVisualizada;
    private RecyclerView rvDuelos;
    private TextView tvRodada, tvEstadoTorneio;
    private LinearLayout layoutNavegacaoRodadas;
    private MaterialButton btnSalvar, btnVoltar, btnEstatisticas, btnSair, btnRodadaAnterior, btnProximaRodada;
    private DueloAdapter adapter;
    private Map<Integer, Set<Integer>> confrontosAnteriores;
    private Set<Integer> duelistasComBye;
    private boolean torneioConcluido;
    private int totalRodadas;
    private int faseTopCut = FASE_NORMAL;

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
        if (savedInstanceState != null) {
            rodadaAtual = savedInstanceState.getInt(KEY_RODADA_ATUAL, 1);
            rodadaVisualizada = savedInstanceState.getInt(KEY_RODADA_VISUALIZADA, 1);
            duelosAtuais = savedInstanceState.getParcelableArrayList(KEY_DUELOS_ATUAIS);
            faseTopCut = savedInstanceState.getInt(KEY_FASE_TOP_CUT, FASE_NORMAL);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_RODADA_ATUAL, rodadaAtual);
        outState.putInt(KEY_RODADA_VISUALIZADA, rodadaVisualizada);
        outState.putParcelableArrayList(KEY_DUELOS_ATUAIS, new ArrayList<>(duelosAtuais));
        outState.putInt(KEY_FASE_TOP_CUT, faseTopCut);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_torneio, container, false);

        View erroView = inicializaDAO(view);
        if (erroView != null) return erroView;

        carregaTorneio();

        inicializaViews(view);
        inicializaListener();

        atualizarInterface();

        return view;
    }

    private void carregaTorneio() {
        carregaDuelos();
        List<Integer> rodadas = inicializaRodadas();
        verificaTopCut(rodadas);
        verificaRodadas(rodadas);
        inicializaDuelos(rodadas);

    }

    private void inicializaDuelos(List<Integer> rodadas) {
        // Carregar ou gerar duelos
        if (duelosAtuais == null) {
            duelosAtuais = new ArrayList<>();
            if (rodadas.isEmpty() && !torneioConcluido) {
                duelosAtuais = gerarDuelosParaRodada(1);
            } else if (faseTopCut != FASE_NORMAL) {
                duelosAtuais = dao.listarDuelosPorRodada(rodadas.get(rodadas.size() - 1), torneioId);
            } else {
                duelosAtuais = carregarDuelosDaRodada(rodadaVisualizada);
            }
        }
    }

    private void verificaRodadas(List<Integer> rodadas) {
        // Determinar rodada atual e visualizada
        if (rodadaAtual == 0) {
            rodadaAtual = rodadas.isEmpty() ? 1 : Math.min(rodadas.size(), totalRodadas);
        }
        if (rodadaVisualizada == 0) {
            rodadaVisualizada = torneioConcluido ? totalRodadas : rodadaAtual;
        }
    }

    private void verificaTopCut(List<Integer> rodadas) {
        // Verificar fase do Top Cut
        if (!rodadas.isEmpty()) {
            String ultimaFase = dao.getFaseRodada(rodadas.get(rodadas.size() - 1));
            if ("Semi-final".equals(ultimaFase)) {
                faseTopCut = FASE_SEMI_FINAL;
            } else if ("Final".equals(ultimaFase)) {
                faseTopCut = FASE_FINAL;
            }
        }
    }

    private List<Integer> inicializaRodadas() {
        // Determinar estado do torneio
        List<Integer> rodadas = dao.listarRodadas(torneioId);
        totalRodadas = torneio.getQuantRodadas();
        torneioConcluido = torneio.getIdCampeao() != 0;
        return rodadas;
    }

    private void carregaDuelos() {
        // Inicializar confrontos anteriores e byes
        confrontosAnteriores = new HashMap<>();
        duelistasComBye = dao.listarByes(torneioId);
        for (Duelista d : duelistas) {
            confrontosAnteriores.put(d.getId(), new HashSet<>());
        }
    }

    @Nullable
    private View inicializaDAO(View view) {
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
        return null;
    }

    private void inicializaListener() {
        // Configurar botões
        btnSalvar.setOnClickListener(v -> salvarResultados());
        btnVoltar.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        btnEstatisticas.setOnClickListener(v -> exibirEstatisticas());
        btnSair.setOnClickListener(v -> requireActivity().finish());
        btnRodadaAnterior.setOnClickListener(v -> mudarRodadaVisualizada(-1));
        btnProximaRodada.setOnClickListener(v -> mudarRodadaVisualizada(1));
    }

    private void inicializaViews(View view) {
        // Inicializar views
        rvDuelos = view.findViewById(R.id.rv_duelos);
        tvRodada = view.findViewById(R.id.tvRodada);
        tvEstadoTorneio = view.findViewById(R.id.tvEstadoTorneio);
        layoutNavegacaoRodadas = view.findViewById(R.id.layoutNavegacaoRodadas);
        btnRodadaAnterior = view.findViewById(R.id.btnRodadaAnterior);
        btnProximaRodada = view.findViewById(R.id.btnProximaRodada);
        btnSalvar = view.findViewById(R.id.btn_salvar);
        btnVoltar = view.findViewById(R.id.btn_voltar);
        btnEstatisticas = view.findViewById(R.id.btn_estatisticas);
        btnSair = view.findViewById(R.id.btn_sair);

        rvDuelos.setLayoutManager(new LinearLayoutManager(getContext()));
        // Configurar RecyclerView
        adapter = new DueloAdapter(duelosAtuais, duelistas, this, torneioConcluido);
        rvDuelos.setAdapter(adapter);

    }

    private boolean validarDuelistas(List<Duelista> duelistas) {
        List<Integer> ids = new ArrayList<>();
        for (Duelista d : duelistas) {
            if (ids.contains(d.getId())) {
                Toast.makeText(getContext(), "Erro: Duelista duplicado", Toast.LENGTH_SHORT).show();
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

    private List<Duelo> carregarDuelosDaRodada(int rodada) {
        List<Duelo> duelos = new ArrayList<>();
        List<Integer> rodadas = dao.listarRodadas(torneioId);

        if (rodada <= rodadas.size() && rodada <= totalRodadas) {
            duelos = dao.listarDuelosPorRodada(rodadas.get(rodada - 1), torneioId);
            Toast.makeText(getContext(), "Carregando duelos da rodada " + rodada, Toast.LENGTH_SHORT).show();
            for (Duelo duelo : duelos) {
                confrontosAnteriores.get(duelo.getIdDuelista1()).add(duelo.getIdDuelista2());
                confrontosAnteriores.get(duelo.getIdDuelista2()).add(duelo.getIdDuelista1());
            }
        } else {
            Toast.makeText(getContext(), "Nenhum duelo carregado para a rodada " + rodada, Toast.LENGTH_SHORT).show();
        }

        return duelos;
    }

    private List<Duelo> gerarDuelosParaRodada(int rodada) {
        List<Duelo> duelos = new ArrayList<>();
        List<Integer> rodadas = dao.listarRodadas(torneioId);

        if (rodada <= rodadas.size()) {
            return dao.listarDuelosPorRodada(rodadas.get(rodada - 1), torneioId);
        }

        List<Duelista> duelistasOrdenados = new ArrayList<>(duelistas);
        if (rodada == 1) {
            Collections.shuffle(duelistasOrdenados);
        } else {
            duelistasOrdenados.sort((d1, d2) -> Integer.compare(d2.getPontos(), d1.getPontos()));
        }

        List<Duelista> duelistasNaoPareados = new ArrayList<>(duelistasOrdenados);
        int idRodada = dao.adicionarRodada(torneioId, "", "");

        verificaBye(duelistasNaoPareados);

        gerarDuelos(duelistasNaoPareados, idRodada, duelos);

        dao.atualizarRodada(idRodada, duelos.toString(), "");
        return duelos;
    }

    private void gerarDuelos(List<Duelista> duelistasNaoPareados, int idRodada, List<Duelo> duelos) {
        // Gerar duelos
        while (duelistasNaoPareados.size() >= 2) {
            Duelista duelista1 = duelistasNaoPareados.get(0);
            Duelista duelista2 = null;

            for (int i = 1; i < duelistasNaoPareados.size(); i++) {
                Duelista possivelOponente = duelistasNaoPareados.get(i);
                if (!confrontosAnteriores.get(duelista1.getId()).contains(possivelOponente.getId())) {
                    duelista2 = possivelOponente;
                    break;
                }
            }

            if (duelista2 == null && !duelistasNaoPareados.isEmpty()) {
                duelista2 = duelistasNaoPareados.get(1);
            }

            if (duelista2 != null) {
                Duelo duelo = new Duelo(idRodada, duelista1.getId(), duelista2.getId());
                long dueloId = dao.adicionarDuelo(idRodada, duelo.getIdDuelista1(), duelo.getIdDuelista2(), null);
                duelo.setId((int) dueloId);
                duelos.add(duelo);
                confrontosAnteriores.get(duelista1.getId()).add(duelista2.getId());
                confrontosAnteriores.get(duelista2.getId()).add(duelista1.getId());
                duelistasNaoPareados.remove(duelista1);
                duelistasNaoPareados.remove(duelista2);
            } else {
                break;
            }
        }
    }

    private void verificaBye(List<Duelista> duelistasNaoPareados) {
        // Verificar se há número ímpar de duelistas
        if (duelistasNaoPareados.size() % 2 != 0) {
            Duelista duelistaBye = null;
            for (Duelista d : duelistasNaoPareados) {
                if (!duelistasComBye.contains(d.getId())) {
                    if (duelistaBye == null || d.getPontos() < duelistaBye.getPontos()) {
                        duelistaBye = d;
                    }
                }
            }

            if (duelistaBye != null) {
                duelistaBye.setVitorias(duelistaBye.getVitorias() + 1);
                dao.atualizarDuelista(duelistaBye);
                duelistasComBye.add(duelistaBye.getId());
                dao.adicionarBye(torneioId, duelistaBye.getId());
                duelistasNaoPareados.remove(duelistaBye);
            } else {
                duelistaBye = duelistasNaoPareados.get(duelistasNaoPareados.size() - 1);
                duelistaBye.setVitorias(duelistaBye.getVitorias() + 1);
                dao.atualizarDuelista(duelistaBye);
                duelistasComBye.clear();
                dao.adicionarBye(torneioId, duelistaBye.getId());
                duelistasComBye.add(duelistaBye.getId());
                duelistasNaoPareados.remove(duelistaBye);
            }
        }
    }

    private void atualizarInterface() {

        if (faseTopCut == FASE_SEMI_FINAL) {
            tvRodada.setText("Semi-finais");
        } else if (faseTopCut == FASE_FINAL) {
            tvRodada.setText("Final");
        } else {
            tvRodada.setText(rodadaVisualizada + "ª Rodada");
        }

        if (torneioConcluido) {
            tvEstadoTorneio.setText("Torneio Concluído");
            tvEstadoTorneio.setVisibility(View.VISIBLE);
            btnSalvar.setEnabled(false);
            layoutNavegacaoRodadas.setVisibility(View.VISIBLE);
        } else {
            tvEstadoTorneio.setVisibility(View.GONE);
            btnSalvar.setEnabled(true);
            layoutNavegacaoRodadas.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();

    }

    private void mudarRodadaVisualizada(int direcao) {
        if (faseTopCut != FASE_NORMAL) {
            return;
        }
        int novaRodada = rodadaVisualizada + direcao;
        if (novaRodada >= 1 && novaRodada <= totalRodadas && novaRodada <= dao.listarRodadas(torneioId).size()) {
            rodadaVisualizada = novaRodada;
            duelosAtuais.clear();
            duelosAtuais.addAll(carregarDuelosDaRodada(rodadaVisualizada));
            adapter.notifyDataSetChanged();
            atualizarInterface();
        }
    }

    @Override
    public void onResultadoSelecionado(int posicaoDuelo, String resultado) {
        if (torneioConcluido) {
            Toast.makeText(getContext(), "Torneio concluído, não é possível alterar resultados", Toast.LENGTH_SHORT).show();
            return;
        }

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

        switch (resultado) {
            case "VITORIA_A":
                duelista1.setVitorias(duelista1.getVitorias() + 1);
                duelista2.setDerrotas(duelista2.getDerrotas() + 1);
                duelo.setIdVencedor(duelista1.getId());
                duelo.setEmpate(false);
                break;
            case "VITORIA_B":
                duelista2.setVitorias(duelista2.getVitorias() + 1);
                duelista1.setDerrotas(duelista1.getDerrotas() + 1);
                duelo.setIdVencedor(duelista2.getId());
                duelo.setEmpate(false);
                break;
            case "EMPATE":
                duelista1.setEmpates(duelista1.getEmpates() + 1);
                duelista2.setEmpates(duelista2.getEmpates() + 1);
                duelo.setIdVencedor(null);
                duelo.setEmpate(true);
                break;
        }

        dao.atualizarDuelo(duelo.getId(), duelo.getIdVencedor(), duelo.isEmpate());

        if (faseTopCut == FASE_SEMI_FINAL || faseTopCut == FASE_FINAL) {
            boolean todosComResultado = true;
            for (Duelo d : duelosAtuais) {
                if (!d.resultadoDefinido()) {
                    todosComResultado = false;
                    break;
                }
            }
            btnSalvar.setEnabled(todosComResultado);
        }
    }

    private void salvarResultados() {
        if (torneioConcluido) {
            Toast.makeText(getContext(), "Torneio já concluído, não é possível salvar novos resultados", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Duelo duelo : duelosAtuais) {
            if (!duelo.resultadoDefinido()) {
                Toast.makeText(getContext(), "Todos os duelos devem ter resultados definidos!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        for (Duelista duelistaLocal : duelistas) {
            dao.atualizarDuelista(duelistaLocal);
        }

        if (faseTopCut == FASE_SEMI_FINAL) {
            avancarParaFinal();
            return;
        } else if (faseTopCut == FASE_FINAL) {
            finalizarTorneio();
            return;
        }

        rodadaAtual++;

        if (rodadaAtual > torneio.getQuantRodadas()) {
            for (Duelista duelista : duelistas) {
                duelista.setParticipacao(duelista.getParticipacao() + 1);
                dao.atualizarDuelista(duelista);
            }

            if (duelistas.size() >= TOP_CUT_SIZE) {
                iniciarTopCut();
            } else {
                finalizarTorneio();
            }
            return;
        }

        continuarRodadasNormais();
    }

    private void iniciarTopCut() {
        faseTopCut = FASE_SEMI_FINAL;
        List<Duelista> topCut = getTopCutDuelistas();
        List<Duelo> semiFinais = gerarTopCutDuelos(topCut, "Semi-final");

        duelosAtuais.clear();
        duelosAtuais.addAll(semiFinais);
        adapter.notifyDataSetChanged();
        atualizarInterface();
        btnSalvar.setEnabled(true);

        Toast.makeText(getContext(), "Iniciando semi-finais com " + topCut.size() + " duelistas", Toast.LENGTH_SHORT).show();
    }

    private void avancarParaFinal() {
        faseTopCut = FASE_FINAL;
        List<Duelo> finalDuelo = gerarTopCutDuelos(null, "Final");

        duelosAtuais.clear();
        duelosAtuais.addAll(finalDuelo);
        adapter.notifyDataSetChanged();
        atualizarInterface();
        btnSalvar.setEnabled(true);

        Toast.makeText(getContext(), "Iniciando final", Toast.LENGTH_SHORT).show();
    }

    private void continuarRodadasNormais() {
        duelosAtuais.clear();
        duelosAtuais.addAll(gerarDuelosParaRodada(rodadaAtual));
        rodadaVisualizada = rodadaAtual;
        adapter.notifyDataSetChanged();
        atualizarInterface();
        Toast.makeText(getContext(), "Resultados salvos! Avançando para a " + rodadaAtual + "ª rodada", Toast.LENGTH_SHORT).show();
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
        List<Duelista> copiaDuelistas = new ArrayList<>();
        for (Duelista original : duelistas) {
            copiaDuelistas.add(new Duelista(original));
        }

        // Filtrar candidatos com o maior número de pontos
        int maxPontos = Collections.max(copiaDuelistas, Comparator.comparingInt(Duelista::getPontos)).getPontos();
        List<Duelista> candidatos = copiaDuelistas.stream()
                .filter(d -> d.getPontos() == maxPontos)
                .collect(Collectors.toList());

        if (candidatos.size() == 1) {
            return encontrarOriginal(candidatos.get(0).getId());
        }

        // Filtrar candidatos com o maior número de vitórias
        int maxVitorias = Collections.max(candidatos, Comparator.comparingInt(Duelista::getVitorias)).getVitorias();
        List<Duelista> candidatosVitorias = candidatos.stream()
                .filter(d -> d.getVitorias() == maxVitorias)
                .collect(Collectors.toList());

        if (candidatosVitorias.size() == 1) {
            return encontrarOriginal(candidatosVitorias.get(0).getId());
        }

        // Filtrar candidatos com o maior número de empates
        int maxEmpates = Collections.max(candidatosVitorias, Comparator.comparingInt(Duelista::getEmpates)).getEmpates();
        List<Duelista> candidatosEmpates = candidatosVitorias.stream()
                .filter(d -> d.getEmpates() == maxEmpates)
                .collect(Collectors.toList());

        if (candidatosEmpates.size() == 1) {
            return encontrarOriginal(candidatosEmpates.get(0).getId());
        }

        Map<Duelista, Integer> buchholzMap = new HashMap<>();
        for (Duelista d : candidatosEmpates) {
            buchholzMap.put(d, calcularBuchholz(d));
        }

        // Filtrar candidatos com o maior Buchholz
        int maxBuchholz = Collections.max(buchholzMap.values());
        List<Duelista> campeoes = buchholzMap.entrySet().stream()
                .filter(entry -> entry.getValue() == maxBuchholz)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return encontrarOriginal(campeoes.get(0).getId());
    }

    private Duelista encontrarOriginal(int id) {
        for (Duelista d : duelistas) {
            if (d.getId() == id) {
                return d;
            }
        }
        return null;
    }

    private int calcularBuchholz(Duelista duelista) {
        Map<Integer, Integer> pontosOponentes = new HashMap<>();
        for (Duelista d : duelistas) {
            pontosOponentes.put(d.getId(), d.getPontos());
        }

        int pontuacaoOponentes = 0;
        List<Integer> rodadas = dao.listarRodadas(torneioId);

        for (Integer idRodada : rodadas) {
            List<Duelo> duelos = dao.listarDuelosPorRodada(idRodada, torneioId);
            for (Duelo duelo : duelos) {
                int idOponente = -1;
                if (duelo.getIdDuelista1() == duelista.getId()) {
                    idOponente = duelo.getIdDuelista2();
                } else if (duelo.getIdDuelista2() == duelista.getId()) {
                    idOponente = duelo.getIdDuelista1();
                }

                if (idOponente != -1) {
                    Integer pontos = pontosOponentes.get(idOponente);
                    if (pontos != null) {
                        pontuacaoOponentes += pontos;
                    }
                }
            }
        }
        return pontuacaoOponentes;
    }

    private List<Duelista> getTopCutDuelistas() {
        List<Duelista> ordenados = new ArrayList<>(duelistas);
        ordenados.sort((d1, d2) -> {
            int cmp = Integer.compare(d2.getPontos(), d1.getPontos());
            if (cmp != 0) return cmp;
            cmp = Integer.compare(d2.getVitorias(), d1.getVitorias());
            if (cmp != 0) return cmp;
            return Integer.compare(calcularBuchholz(d2), calcularBuchholz(d1));
        });

        return ordenados.subList(0, Math.min(TOP_CUT_SIZE, ordenados.size()));
    }

    private List<Duelo> gerarTopCutDuelos(List<Duelista> topCut, String fase) {
        List<Duelo> duelos = new ArrayList<>();
        int idRodada = dao.adicionarRodada(torneioId, "", fase);

        if (fase.equals("Semi-final")) {
            Duelo semi1 = new Duelo(idRodada, topCut.get(0).getId(), topCut.get(3).getId());
            Duelo semi2 = new Duelo(idRodada, topCut.get(1).getId(), topCut.get(2).getId());

            semi1.setId((int) dao.adicionarDuelo(idRodada, semi1.getIdDuelista1(), semi1.getIdDuelista2(), null));
            semi2.setId((int) dao.adicionarDuelo(idRodada, semi2.getIdDuelista1(), semi2.getIdDuelista2(), null));

            duelos.add(semi1);
            duelos.add(semi2);
        } else if (fase.equals("Final")) {
            Duelista finalista1 = encontrarVencedor(duelosAtuais.get(0));
            Duelista finalista2 = encontrarVencedor(duelosAtuais.get(1));

            Duelista perdedor1 = encontraPerdedor(duelosAtuais.get(0));
            Duelista perdedor2 = encontraPerdedor(duelosAtuais.get(1));

            // Adiciona a final
            if (finalista1 != null && finalista2 != null) {
                Duelo finalDuelo = new Duelo(idRodada, finalista1.getId(), finalista2.getId());
                finalDuelo.setId((int) dao.adicionarDuelo(idRodada, finalDuelo.getIdDuelista1(), finalDuelo.getIdDuelista2(), null));
                duelos.add(finalDuelo);
            }
            // Adiciona a disputa de 3º lugar
            if (perdedor1 != null && perdedor2 != null) {
                Duelo dueloPerdedores = new Duelo(idRodada, perdedor1.getId(), perdedor2.getId());
                dueloPerdedores.setId((int) dao.adicionarDuelo(idRodada, dueloPerdedores.getIdDuelista1(), dueloPerdedores.getIdDuelista2(), null));
                duelos.add(dueloPerdedores);
            }
        }

        return duelos;
    }

    private Duelista encontrarVencedor(Duelo duelo) {
        if (duelo.getIdVencedor() == null) return null;

        for (Duelista d : duelistas) {
            if (d.getId() == duelo.getIdVencedor()) {
                return d;
            }
        }
        return null;
    }

    private Duelista encontraPerdedor(Duelo duelo) {
        if (duelo.getIdVencedor() == null) return null;

        for (Duelista d : duelistas) {
            if (d.getId() != duelo.getIdVencedor() &&
                    (d.getId() == duelo.getIdDuelista1() ||
                            d.getId() == duelo.getIdDuelista2())) {
                return d;
            }
        }
        return null;
    }

    private void finalizarTorneio() {
        if (faseTopCut == FASE_SEMI_FINAL) {
            avancarParaFinal();
            return;
        }

        Duelista campeao = null;
        if (faseTopCut == FASE_FINAL && !duelosAtuais.isEmpty()) {
            for (Duelo duelo : duelosAtuais) {
                if (duelo.getIdVencedor() != null) {
                    campeao = encontrarVencedor(duelo);
                    break;
                }
            }
        }

        if (campeao == null) {
            campeao = determinarCampeao();
        }

        if (campeao != null) {
            torneio.setIdCampeao(campeao.getId());
            dao.atualizarTorneio(torneio);
            Toast.makeText(getContext(), "Torneio finalizado! Campeão: " + campeao.getNome(), Toast.LENGTH_LONG).show();
        }

        torneioConcluido = true;
        atualizarInterface();

        Fragment classificacaoFragment = ClassificacaoFragment.newInstance(torneioId);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, classificacaoFragment)
                .addToBackStack(null)
                .commit();
    }
};