package io.github.douglaasph.icompras.produtos.service;

import io.github.douglaasph.icompras.produtos.model.Produto;
import io.github.douglaasph.icompras.produtos.repository.ProdutoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ProdutoService {
    private final ProdutoRepository repository;

    public Produto salvar(Produto produto) {
        return repository.save(produto);
    }

    public Optional<Produto> obterPorCodigo(Long codigo) {
        return repository.findById(codigo);
    }

    public void deletar(Produto produto) {
        produto.setAtivo(false);
        repository.save(produto);
    }
}
