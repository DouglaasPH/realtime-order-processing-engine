package io.github.douglaasph.icompras.produtos.repository;

import io.github.douglaasph.icompras.produtos.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
