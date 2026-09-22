package io.github.douglaasph.icompras.clientes.repository;

import io.github.douglaasph.icompras.clientes.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
