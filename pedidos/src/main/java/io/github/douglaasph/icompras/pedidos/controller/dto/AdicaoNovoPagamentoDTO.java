package io.github.douglaasph.icompras.pedidos.controller.dto;

import io.github.douglaasph.icompras.pedidos.model.enums.TipoPagamento;

public record AdicaoNovoPagamentoDTO(
        Long codigoPedido,
        String dados,
        TipoPagamento tipoPagamento
) {
}
