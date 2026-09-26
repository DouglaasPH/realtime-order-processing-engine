package io.github.douglaasph.icompras.logistica.subscriber.representation;

import io.github.douglaasph.icompras.logistica.model.StatusPedido;

public record AtualizacaoFaturamentoRepresentation (
        Long codigo,
        StatusPedido status,
        String urlNotaFiscal
) {
}
