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
    private int torneioId;
    private DAOSQLITE dao;
    private Torneio torneio;
    private List<Duelista> duelistas;
    private List<Duelo> duelosAtuais;
    private int rodadaAtual; // Rodada atual do torneio (em andamento ou última rodada)
    private int rodadaVisualizada; // Rodada sendo visualizada (para navegação)
    private RecyclerView rvDuelos;
    private TextView tvRodada;
    private TextView tvEstadoTorneio;
    private LinearLayout layoutNavegacaoRodadas;
    private MaterialButton btnSalvar, btnVoltar, btnEstatisticas, btnSair, btnRodadaAnterior, btnProximaRodada;
    private DueloAdapter adapter;
    private Map<Integer, Set<Integer>> confrontosAnteriores;
    private Set<Integer> duelistasComBye; // IDs dos duelistas que já receberam bye
    private boolean torneioConcluido;
    private int totalRodadas;

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
            rodadaVisualizada = savedInstanceState.getInt(KEY_RODADA_VISUALIZADA, 1);
            duelosAtuais = savedInstanceState.getParcelableArrayList(KEY_DUELOS_ATUAIS);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_RODADA_ATUAL, rodadaAtual);
        outState.putInt(KEY_RODADA_VISUALIZADA, rodadaVisualizada);
        outState.putParcelableArrayList(KEY_DUELOS_ATUAIS, new ArrayList<>(duelosAtuais));
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_torneio, container, false);

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

        // Inicializar confrontos anteriores e byes
        confrontosAnteriores = new HashMap<>();
        duelistasComBye = dao.listarByes(torneioId); // Carregar byes do banco
        for (Duelista d : duelistas) {
            confrontosAnteriores.put(d.getId(), new HashSet<>());
        }

        // Determinar estado do torneio
        List<Integer> rodadas = dao.listarRodadas(torneioId);
        totalRodadas = torneio.getQuantRodadas();


        if (rodadaAtual == 0) { // Só calcular se não foi restaurado
            rodadaAtual = rodadas.isEmpty() ? 1 : rodadas.size();
            if (rodadaAtual > totalRodadas && !torneioConcluido) {
                rodadaAtual = rodadas.size();
            }
        }

        torneioConcluido = torneio.getIdCampeao() != 0 && rodadas.size() >= totalRodadas;

        // Determinar rodada a ser visualizada
        if (rodadaVisualizada == 0) {
            rodadaVisualizada = torneioConcluido ? totalRodadas : rodadaAtual;
        }

        // Gerar duelos da primeira rodada se necessário
        if (rodadas.isEmpty() && !torneioConcluido) {
            duelosAtuais = gerarDuelosParaRodada(1);
        } else {
            duelosAtuais = carregarDuelosDaRodada(rodadaVisualizada);
        }

        adapter = new DueloAdapter(duelosAtuais, duelistas, this, torneioConcluido);
        rvDuelos.setAdapter(adapter);

        // Atualizar UI
        atualizarInterface();

        // Configurar botões
        btnSalvar.setOnClickListener(v -> salvarResultados());
        btnVoltar.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        btnEstatisticas.setOnClickListener(v -> exibirEstatisticas());
        btnSair.setOnClickListener(v -> requireActivity().finish());
        btnRodadaAnterior.setOnClickListener(v -> mudarRodadaVisualizada(-1));
        btnProximaRodada.setOnClickListener(v -> mudarRodadaVisualizada(1));

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

    private List<Duelo> carregarDuelosDaRodada(int rodada) {
        List<Duelo> duelos = new ArrayList<>();
        List<Integer> rodadas = dao.listarRodadas(torneioId);

        if (rodada <= rodadas.size()) {
            duelos = dao.listarDuelosPorRodada(rodadas.get(rodada - 1), torneioId);
            Log.d("TorneioFragment", "Carregados " + duelos.size() + " duelos para rodada " + rodada);
            // Atualizar confrontos anteriores (se necessário)
            for (Duelo duelo : duelos) {
                confrontosAnteriores.get(duelo.getIdDuelista1()).add(duelo.getIdDuelista2());
                confrontosAnteriores.get(duelo.getIdDuelista2()).add(duelo.getIdDuelista1());
            }
        } else {
            Log.d("TorneioFragment", "Nenhum duelo carregado para a rodada " + rodada + " (ainda não gerada)");
        }

        return duelos;
    }

    private List<Duelo> gerarDuelosParaRodada(int rodada) {
        List<Duelo> duelos = new ArrayList<>();
        List<Integer> rodadas = dao.listarRodadas(torneioId);

        if (rodada <= rodadas.size()) {
            // Rodada já existe, apenas carregar os duelos
            return dao.listarDuelosPorRodada(rodadas.get(rodada - 1), torneioId);
        }

        List<Duelista> duelistasOrdenados = new ArrayList<>(duelistas);
        if (rodada == 1) {
            Collections.shuffle(duelistasOrdenados); // Primeira rodada: embaralhar duelistas
        } else {
            duelistasOrdenados.sort((d1, d2) -> Integer.compare(d2.getPontos(), d1.getPontos())); // Ordenar por pontos
        }

        List<Duelista> duelistasNaoPareados = new ArrayList<>(duelistasOrdenados);
        int idRodada = dao.adicionarRodada(torneioId, "");

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
                duelistaBye.setPontos(duelistaBye.getPontos() + 3);
                duelistaBye.setVitorias(duelistaBye.getVitorias() + 1);
                dao.atualizarDuelista(duelistaBye);
                duelistasComBye.add(duelistaBye.getId());
                dao.adicionarBye(torneioId, duelistaBye.getId());
                duelistasNaoPareados.remove(duelistaBye);
            } else {
                duelistaBye = duelistasNaoPareados.get(duelistasNaoPareados.size() - 1);
                duelistaBye.setPontos(duelistaBye.getPontos() + 3);
                duelistaBye.setVitorias(duelistaBye.getVitorias() + 1);
                dao.atualizarDuelista(duelistaBye);
                duelistasComBye.clear();
                dao.adicionarBye(torneioId, duelistaBye.getId());
                duelistasComBye.add(duelistaBye.getId());
                duelistasNaoPareados.remove(duelistaBye);
            }
        }

        // Gerar duelos para os duelistas restantes
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

        dao.atualizarRodada(idRodada, duelos.toString());
        return duelos;
    }

    private void atualizarInterface() {
        // Atualizar texto da rodada
        tvRodada.setText(rodadaVisualizada + "ª Rodada");

        // Mostrar estado do torneio
        if (torneioConcluido) {
            tvEstadoTorneio.setText("Torneio Concluído");
            tvEstadoTorneio.setVisibility(View.VISIBLE);
            btnSalvar.setEnabled(false);
            layoutNavegacaoRodadas.setVisibility(View.VISIBLE);
        } else {
            tvEstadoTorneio.setVisibility(View.GONE);
            btnSalvar.setEnabled(rodadaVisualizada == rodadaAtual);
            layoutNavegacaoRodadas.setVisibility(View.GONE);
        }

        // Atualizar botões de navegação
        btnRodadaAnterior.setEnabled(rodadaVisualizada > 1);
        btnProximaRodada.setEnabled(rodadaVisualizada < totalRodadas && rodadaVisualizada < dao.listarRodadas(torneioId).size());
    }

    private void mudarRodadaVisualizada(int direcao) {
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

        // Atualizar estatísticas dos duelistas
        switch (resultado) {
            case "VITORIA_A":
                duelista1.setVitorias(duelista1.getVitorias() + 1);
                duelista2.setDerrotas(duelista2.getDerrotas() + 1);
                duelo.setIdVencedor(duelista1.getId());
                break;
            case "VITORIA_B":
                duelista2.setVitorias(duelista2.getVitorias() + 1);
                duelista1.setDerrotas(duelista1.getDerrotas() + 1);
                duelo.setIdVencedor(duelista2.getId());
                break;
            case "EMPATE":
                duelista1.setEmpates(duelista1.getEmpates() + 1);
                duelista2.setEmpates(duelista2.getEmpates() + 1);
                duelo.setIdVencedor(null);
                break;
        }

        // Atualizar duelo no banco
        dao.atualizarDuelo(duelo.getId(), duelo.getIdVencedor());
    }

    private void salvarResultados() {
        // Verificar se o torneio está concluído
        if (torneioConcluido) {
            Toast.makeText(getContext(), "Torneio já concluído, não é possível salvar novos resultados", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar se todos os duelos têm resultado
        for (Duelo duelo : duelosAtuais) {
            if (duelo.getIdDuelista1() == 0 || duelo.getIdDuelista2() == 0) {
                Toast.makeText(getContext(), "Erro: Duelo inválido detectado", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Atualizar duelistas no banco
        for (Duelista duelistaLocal : duelistas) {
            dao.atualizarDuelista(duelistaLocal);
        }

        // Avançar para a próxima rodada
        rodadaAtual++;
        if (rodadaAtual > torneio.getQuantRodadas()) {

            for (Duelista duelista : duelistas) {
                duelista.setParticipacao(duelista.getParticipacao() + 1); // Adiciona 1 ponto de participação
                dao.atualizarDuelista(duelista);
            }
            // Finalizar torneio e exibir classificação
            Duelista campeao = determinarCampeao();

            if (campeao != null) {
                torneio.setIdCampeao(campeao.getId());
                dao.atualizarTorneio(torneio);
                Toast.makeText(getContext(), "Torneio finalizado! Campeão: " + campeao.getNome(), Toast.LENGTH_LONG).show();
            }
            torneioConcluido = true;
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

        // Gerar duelos para a próxima rodada
        duelosAtuais.clear();
        duelosAtuais.addAll(gerarDuelosParaRodada(rodadaAtual));
        rodadaVisualizada = rodadaAtual;
        adapter.notifyDataSetChanged();
        atualizarInterface();
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
        // 1. Criar uma cópia imutável de todos os duelistas com seus dados atuais
        List<Duelista> copiaDuelistas = new ArrayList<>();
        for (Duelista original : duelistas) {
            copiaDuelistas.add(new Duelista(original)); // Assume que Duelista tem construtor de cópia
        }

        // 2. Processar os critérios usando apenas a cópia
        // Primeiro critério: Pontuação
        int maxPontos = Collections.max(copiaDuelistas, Comparator.comparingInt(Duelista::getPontos)).getPontos();
        List<Duelista> candidatos = copiaDuelistas.stream()
                .filter(d -> d.getPontos() == maxPontos)
                .collect(Collectors.toList());

        if (candidatos.size() == 1) {
            return encontrarOriginal(candidatos.get(0).getId());
        }

        // Segundo critério: Vitórias
        int maxVitorias = Collections.max(candidatos, Comparator.comparingInt(Duelista::getVitorias)).getVitorias();
        List<Duelista> candidatosVitorias = candidatos.stream()
                .filter(d -> d.getVitorias() == maxVitorias)
                .collect(Collectors.toList());

        if (candidatosVitorias.size() == 1) {
            return encontrarOriginal(candidatosVitorias.get(0).getId());
        }

        // Terceiro critério: Empates
        int maxEmpates = Collections.max(candidatosVitorias, Comparator.comparingInt(Duelista::getEmpates)).getEmpates();
        List<Duelista> candidatosEmpates = candidatosVitorias.stream()
                .filter(d -> d.getEmpates() == maxEmpates)
                .collect(Collectors.toList());

        if (candidatosEmpates.size() == 1) {
            return encontrarOriginal(candidatosEmpates.get(0).getId());
        }

        // Quarto critério: Buchholz
        Map<Duelista, Integer> buchholzMap = new HashMap<>();
        for (Duelista d : candidatosEmpates) {
            buchholzMap.put(d, calcularBuchholz(d));
        }

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
        // Criar cópia local dos duelistas para evitar concorrência
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
}