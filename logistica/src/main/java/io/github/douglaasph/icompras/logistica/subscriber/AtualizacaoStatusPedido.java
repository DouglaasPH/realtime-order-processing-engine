package io.github.douglaasph.icompras.logistica.subscriber;

import io.github.douglaasph.icompras.logistica.model.StatusPedido;

public record AtualizacaoStatusPedido(
        Long codigo,
        StatusPedido status,
        String urlNotaFiscal
) {
}
