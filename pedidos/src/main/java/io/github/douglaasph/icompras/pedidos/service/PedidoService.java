package io.github.douglaasph.icompras.pedidos.service;

import io.github.douglaasph.icompras.pedidos.client.ServicoBancarioClient;
import io.github.douglaasph.icompras.pedidos.model.DadosPagamento;
import io.github.douglaasph.icompras.pedidos.model.Pedido;
import io.github.douglaasph.icompras.pedidos.model.enums.StatusPedido;
import io.github.douglaasph.icompras.pedidos.model.enums.TipoPagamento;
import io.github.douglaasph.icompras.pedidos.model.exception.ItemNaoEncontradoException;
import io.github.douglaasph.icompras.pedidos.repository.ItemPedidoRepository;
import io.github.douglaasph.icompras.pedidos.repository.PedidoRepository;
import io.github.douglaasph.icompras.pedidos.validator.PedidoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final PedidoValidator validator;
    private final ServicoBancarioClient servicoBancarioClient;

    @Transactional
    public Pedido criarPedido(Pedido pedido) {
        validator.validar(pedido);
        realizarPersistencia(pedido);
        enviarSolicitacaoPagamento(pedido);
        return pedido;
    }

    private void enviarSolicitacaoPagamento(Pedido pedido) {
        String chavePagamento = servicoBancarioClient.solicitarPagamento(pedido);
        pedido.setChavePagamento(chavePagamento);
    }

    private void realizarPersistencia(Pedido pedido) {
        pedidoRepository.save(pedido);
        itemPedidoRepository.saveAll(pedido.getItens());
    }

    public void atualizarStatusPagamento(
            Long codigoPedido, String chavePagamento, Boolean sucesso, String observacoes) {
        var pedidoEncontrado = pedidoRepository.findByCodigoAndChavePagamento(codigoPedido, chavePagamento);

        if(pedidoEncontrado.isEmpty()) {
            var msg = String.format(
                    "Pedido não encontrado para o código %d e chave pagamento %s",
                    codigoPedido,
                    chavePagamento);
            log.error(msg);
            return;
        }

        Pedido pedido = pedidoEncontrado.get();

        if (sucesso) {
            pedido.setStatus(StatusPedido.PAGO);
        } else {
            pedido.setStatus(StatusPedido.ERRO_PAGAMENTO);
            pedido.setObservacoes(observacoes);
        }
        pedidoRepository.save(pedido);
    }

    @Transactional
    public void adicionarNovoPagamento(
            Long codigoPedido, String dadosCartao, TipoPagamento tipo) {
        var pedidoEncontrado = pedidoRepository.findById(codigoPedido);

        if (pedidoEncontrado.isEmpty()) {
            throw new ItemNaoEncontradoException("Pedido não encontrado para o código informado.");
        }

        var pedido = pedidoEncontrado.get();

        DadosPagamento dadosPagamento = new DadosPagamento();
        dadosPagamento.setTipoPagamento(tipo);
        dadosPagamento.setDados(dadosCartao);

        pedido.setDadosPagamento(dadosPagamento);
        pedido.setStatus(StatusPedido.REALIZADO);
        pedido.setObservacoes("Novo pagamento realizado. Aguardando o processamento.");

        String novaChavePagamento = servicoBancarioClient.solicitarPagamento(pedido);
        pedido.setChavePagamento(novaChavePagamento);

        pedidoRepository.save(pedido);
    }
}
