package io.github.douglaasph.icompras.pedidos.subscriber.representation;

import io.github.douglaasph.icompras.pedidos.model.enums.StatusPedido;

public record AtualizacaoStatusPedidoRepresentation(
        Long codigo,
        StatusPedido status,
        String urlNotaFiscal,
        String codigoRastreio
) {
}
