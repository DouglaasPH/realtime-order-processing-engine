package io.github.douglaasph.icompras.pedidos.repository;

import io.github.douglaasph.icompras.pedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Optional<Pedido> findByCodigoAndChavePagamento(Long codigo, String chavePagamento);
}
