package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.AlteracaoDeConteudoResponse;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AlteracaoDeConteudoService {

    private final AlteracaoDeConteudoRepository repository;
    private final ConteudoMapper mapper;

    public AlteracaoDeConteudoService(AlteracaoDeConteudoRepository repository, ConteudoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public PaginaResponse<AlteracaoDeConteudoResponse> listar(UUID entidadeId, Pageable pagina) {
        PageRequest pedido = PageRequest.of(
                pagina.getPageNumber(), pagina.getPageSize(), Sort.by(Sort.Direction.DESC, "ocorridaEm", "id"));
        Page<AlteracaoDeConteudo> alteracoes =
                entidadeId == null ? repository.findAll(pedido) : repository.findByEntidadeId(entidadeId, pedido);
        return PaginaResponse.de(alteracoes, mapper::paraAlteracao);
    }
}
