package com.projetointegrador.natysync.gamificacao;

import com.projetointegrador.natysync.gamificacao.dto.PosicaoRankingResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RankingRepository {

    private static final String CLASSIFICACAO = """
            select u.id, u.nome, coalesce(sum(p.xp), 0) as pontos,
                   row_number() over (
                       order by coalesce(sum(p.xp), 0) desc, max(p.concedido_em) asc nulls last,
                                u.nome asc, u.id asc) as posicao
            from usuario u
            left join ponto_concedido p on p.usuario_id = u.id
            where u.empresa_id = :empresaId and u.ativo
            group by u.id, u.nome
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public RankingRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<PosicaoRankingResponse> pagina(UUID empresaId, int limite, long deslocamento) {
        return jdbc.query(
                "select * from (" + CLASSIFICACAO + ") c order by c.posicao limit :limite offset :deslocamento",
                Map.of("empresaId", empresaId, "limite", limite, "deslocamento", deslocamento),
                RankingRepository::posicao);
    }

    public long contar(UUID empresaId) {
        return jdbc.queryForObject(
                "select count(*) from usuario where empresa_id = :empresaId and ativo",
                Map.of("empresaId", empresaId),
                Long.class);
    }

    public Optional<PosicaoRankingResponse> posicaoDe(UUID empresaId, UUID usuarioId) {
        return jdbc
                .query(
                        "select * from (" + CLASSIFICACAO + ") c where c.id = :usuarioId",
                        Map.of("empresaId", empresaId, "usuarioId", usuarioId),
                        RankingRepository::posicao)
                .stream()
                .findFirst();
    }

    private static PosicaoRankingResponse posicao(ResultSet linha, int indice) throws SQLException {
        return new PosicaoRankingResponse(
                linha.getLong("posicao"),
                linha.getObject("id", UUID.class),
                linha.getString("nome"),
                linha.getLong("pontos"));
    }
}
