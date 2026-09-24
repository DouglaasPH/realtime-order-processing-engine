package io.github.douglaasph.icompras.pedidos.service;

import io.github.douglaasph.icompras.pedidos.model.Pedido;
import io.github.douglaasph.icompras.pedidos.repository.ItemPedidoRepository;
import io.github.douglaasph.icompras.pedidos.repository.PedidoRepository;
import io.github.douglaasph.icompras.pedidos.validator.PedidoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final PedidoValidator validator;

    public Pedido criarPedido(Pedido pedido) {
        pedidoRepository.save(pedido);
        itemPedidoRepository.saveAll(pedido.getItens());
        return pedido;
    }
}
