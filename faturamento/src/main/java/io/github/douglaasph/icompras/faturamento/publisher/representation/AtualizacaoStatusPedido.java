package io.github.douglaasph.icompras.faturamento.publisher.representation;

public record AtualizacaoStatusPedido(Long codigo, StatusPedido statusPedido, String urlNotaFiscal) {
}
