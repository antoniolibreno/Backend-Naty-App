package com.projetointegrador.natysync.gamificacao;

import com.projetointegrador.natysync.gamificacao.dto.PosicaoRankingResponse;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RankingService {

    private final RankingRepository rankingRepository;

    public RankingService(RankingRepository rankingRepository) {
        this.rankingRepository = rankingRepository;
    }

    public PaginaResponse<PosicaoRankingResponse> listar(UUID empresaId, Pageable pagina) {
        PageRequest pedido = PageRequest.of(pagina.getPageNumber(), pagina.getPageSize());
        List<PosicaoRankingResponse> itens =
                rankingRepository.pagina(empresaId, pedido.getPageSize(), pedido.getOffset());
        long total = rankingRepository.contar(empresaId);
        return PaginaResponse.de(new PageImpl<>(itens, pedido, total), Function.identity());
    }
}
