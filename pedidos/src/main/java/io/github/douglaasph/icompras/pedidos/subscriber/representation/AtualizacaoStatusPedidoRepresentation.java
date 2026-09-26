package io.github.douglaasph.icompras.pedidos.subscriber.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.douglaasph.icompras.pedidos.model.enums.StatusPedido;

public record AtualizacaoStatusPedidoRepresentation(
        Long codigo,
        StatusPedido statusPedido,
        String urlNotaFiscal,
        String codigoRastreio
) {
}
