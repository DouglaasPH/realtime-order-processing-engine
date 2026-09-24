package io.github.douglaasph.icompras.pedidos.repository;

import io.github.douglaasph.icompras.pedidos.model.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
}
