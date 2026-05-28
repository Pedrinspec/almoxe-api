package com.almoxe.almoxeapi.item;

import com.almoxe.almoxeapi.common.RecursoNaoEncontradoException;
import com.almoxe.almoxeapi.common.RegraNegocioException;
import com.almoxe.almoxeapi.movimentacao.Movimentacao;
import com.almoxe.almoxeapi.movimentacao.MovimentacaoRepository;
import com.almoxe.almoxeapi.movimentacao.MovimentacaoResponse;
import com.almoxe.almoxeapi.movimentacao.TipoMovimentacao;
import com.almoxe.almoxeapi.obra.Obra;
import com.almoxe.almoxeapi.obra.ObraService;
import com.almoxe.almoxeapi.produto.Produto;
import com.almoxe.almoxeapi.produto.ProdutoRepository;
import com.almoxe.almoxeapi.produto.TipoControle;
import com.almoxe.almoxeapi.usuario.Usuario;
import com.almoxe.almoxeapi.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ItemEstoqueService {

    private final ItemEstoqueRepository itemRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObraService obraService;

    public ItemEstoqueService(ItemEstoqueRepository itemRepository,
                              MovimentacaoRepository movimentacaoRepository,
                              ProdutoRepository produtoRepository,
                              UsuarioRepository usuarioRepository,
                              ObraService obraService) {
        this.itemRepository = itemRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.obraService = obraService;
    }

    public ItemEstoqueResponse darEntrada(EntradaRequest request) {
        Produto produto = produtoRepository.findById(request.produtoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: " + request.produtoId()));
        Usuario usuario = buscarUsuario(request.usuarioId());

        validarCamposPorTipoControle(produto, request);

        ItemEstoque item = obterOuCriarItem(produto, request);
        item = itemRepository.save(item);

        registrarMovimentacao(item, usuario, TipoMovimentacao.ENTRADA, request.quantidade(), null, request.observacao());

        return ItemEstoqueResponse.from(item);
    }

    public ItemEstoqueResponse alocar(UUID itemId, AlocacaoRequest request) {
        ItemEstoque item = buscarItem(itemId);
        exigirStatus(item, StatusItem.DISPONIVEL);
        if (item.getProduto().getTipoControle() == TipoControle.QUANTIDADE) {
            throw new RegraNegocioException(
                    "Itens fungíveis (QUANTIDADE) não são alocados; registre o consumo via baixa.");
        }
        Usuario registrador = buscarUsuario(request.usuarioId());
        Usuario responsavel = buscarUsuario(request.responsavelId());
        Obra obra = obraService.buscarAtivaOuFalha(request.obraId());

        item.setStatus(StatusItem.ALOCADO);
        item.setResponsavel(responsavel);

        registrarMovimentacao(item, registrador, TipoMovimentacao.ALOCACAO, item.getQuantidade(), obra, request.observacao());
        return ItemEstoqueResponse.from(item);
    }

    public ItemEstoqueResponse usar(UUID itemId, OperacaoRequest request) {
        ItemEstoque item = buscarItem(itemId);
        exigirStatus(item, StatusItem.ALOCADO);
        Usuario registrador = buscarUsuario(request.usuarioId());

        item.setStatus(StatusItem.EM_USO);

        registrarMovimentacao(item, registrador, TipoMovimentacao.ALOCACAO, item.getQuantidade(), null, request.observacao());
        return ItemEstoqueResponse.from(item);
    }

    public ItemEstoqueResponse retornar(UUID itemId, OperacaoRequest request) {
        ItemEstoque item = buscarItem(itemId);
        exigirStatus(item, StatusItem.ALOCADO, StatusItem.EM_USO);
        Usuario registrador = buscarUsuario(request.usuarioId());

        item.setStatus(StatusItem.DISPONIVEL);
        item.setResponsavel(null);

        registrarMovimentacao(item, registrador, TipoMovimentacao.RETORNO, item.getQuantidade(), null, request.observacao());
        return ItemEstoqueResponse.from(item);
    }

    public ItemEstoqueResponse baixar(UUID itemId, BaixaRequest request) {
        ItemEstoque item = buscarItem(itemId);
        Usuario registrador = buscarUsuario(request.usuarioId());
        Obra obra = request.obraId() == null ? null : obraService.buscarAtivaOuFalha(request.obraId());

        BigDecimal quantidadeBaixa;
        if (item.getProduto().getTipoControle() == TipoControle.QUANTIDADE) {
            if (request.quantidade() == null) {
                throw new RegraNegocioException("Baixa de item fungível (QUANTIDADE) exige a quantidade a ser baixada.");
            }
            if (request.quantidade().compareTo(item.getQuantidade()) > 0) {
                throw new RegraNegocioException("Quantidade da baixa (" + request.quantidade()
                        + ") excede o disponível (" + item.getQuantidade() + ").");
            }
            item.setQuantidade(item.getQuantidade().subtract(request.quantidade()));
            quantidadeBaixa = request.quantidade();
        } else {
            exigirStatus(item, StatusItem.EM_USO);
            item.setStatus(StatusItem.CONSUMIDO);
            quantidadeBaixa = item.getQuantidade();
        }

        registrarMovimentacao(item, registrador, TipoMovimentacao.BAIXA, quantidadeBaixa, obra, request.observacao());
        return ItemEstoqueResponse.from(item);
    }

    @Transactional(readOnly = true)
    public List<ItemEstoqueResponse> listar() {
        return itemRepository.findAll().stream()
                .map(ItemEstoqueResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ItemEstoqueResponse buscarPorId(UUID id) {
        return itemRepository.findById(id)
                .map(ItemEstoqueResponse::from)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de estoque não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoResponse> historico(UUID itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new RecursoNaoEncontradoException("Item de estoque não encontrado: " + itemId);
        }
        return movimentacaoRepository.findByItemEstoqueIdOrderByDataHoraAsc(itemId).stream()
                .map(MovimentacaoResponse::from)
                .toList();
    }

    private void validarCamposPorTipoControle(Produto produto, EntradaRequest request) {
        switch (produto.getTipoControle()) {
            case LOTE -> {
                if (!StringUtils.hasText(request.lote())
                        || !StringUtils.hasText(request.notaFiscal())
                        || !StringUtils.hasText(request.numeroRi())) {
                    throw new RegraNegocioException(
                            "Produto com controle por LOTE exige lote, nota fiscal e número de RI.");
                }
            }
            case UNIDADE_UNICA -> {
                if (!StringUtils.hasText(request.numeroSerie())) {
                    throw new RegraNegocioException(
                            "Produto com controle por UNIDADE_UNICA exige número de série.");
                }
                if (request.quantidade().compareTo(BigDecimal.ONE) != 0) {
                    throw new RegraNegocioException(
                            "Produto com controle por UNIDADE_UNICA deve ter quantidade igual a 1 por entrada.");
                }
            }
            case QUANTIDADE -> {
                // fungível: sem campos de identidade/rastreio; nada obrigatório além de quantidade
            }
        }
    }

    private ItemEstoque obterOuCriarItem(Produto produto, EntradaRequest request) {
        if (produto.getTipoControle() == TipoControle.QUANTIDADE) {
            ItemEstoque existente = itemRepository
                    .findFirstByProdutoIdAndStatus(produto.getId(), StatusItem.DISPONIVEL)
                    .orElse(null);
            if (existente != null) {
                existente.setQuantidade(existente.getQuantidade().add(request.quantidade()));
                return existente;
            }
        }

        ItemEstoque item = new ItemEstoque();
        item.setProduto(produto);
        item.setResponsavel(null);
        item.setStatus(StatusItem.DISPONIVEL);
        item.setQuantidade(request.quantidade());

        switch (produto.getTipoControle()) {
            case UNIDADE_UNICA -> item.setNumeroSerie(request.numeroSerie());
            case LOTE -> {
                item.setLote(request.lote());
                item.setNotaFiscal(request.notaFiscal());
                item.setNumeroRi(request.numeroRi());
            }
            case QUANTIDADE -> {
                // sem campos adicionais
            }
        }
        return item;
    }

    private void registrarMovimentacao(ItemEstoque item, Usuario usuario, TipoMovimentacao tipo,
                                       BigDecimal quantidade, Obra obra, String observacao) {
        Movimentacao mov = new Movimentacao();
        mov.setItemEstoque(item);
        mov.setUsuario(usuario);
        mov.setObra(obra);
        mov.setTipo(tipo);
        mov.setQuantidade(quantidade);
        mov.setDataHora(Instant.now());
        mov.setObservacao(observacao);
        movimentacaoRepository.save(mov);
    }

    private ItemEstoque buscarItem(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de estoque não encontrado: " + id));
    }

    private Usuario buscarUsuario(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + id));
    }

    private void exigirStatus(ItemEstoque item, StatusItem... permitidos) {
        for (StatusItem permitido : permitidos) {
            if (item.getStatus() == permitido) {
                return;
            }
        }
        throw new RegraNegocioException("Transição inválida: item está em " + item.getStatus()
                + " e a operação exige um destes estados: " + Arrays.toString(permitidos) + ".");
    }
}
