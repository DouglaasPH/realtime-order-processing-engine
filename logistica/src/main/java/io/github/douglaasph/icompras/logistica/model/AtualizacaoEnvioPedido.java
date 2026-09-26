package io.github.douglaasph.icompras.logistica.model;

public record AtualizacaoEnvioPedido(
        Long codigo,
        StatusPedido statusPedido,
        String codigoRastreio
) {
}
