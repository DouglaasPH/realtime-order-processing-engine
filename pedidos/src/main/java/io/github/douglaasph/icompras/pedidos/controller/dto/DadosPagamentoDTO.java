package io.github.douglaasph.icompras.pedidos.controller.dto;

import io.github.douglaasph.icompras.pedidos.model.enums.TipoPagamento;

public record DadosPagamentoDTO (String dados, TipoPagamento tipoPagamento) {
}
