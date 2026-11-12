package com.projetointegrador.natysync;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class RegrasDeEscritaTest {

    private static final Pattern LITERAL_DE_TEXTO = Pattern.compile("\"(\\\\.|[^\"\\\\])*\"");
    private static final Pattern MARCADOR_NO_CODIGO =
            Pattern.compile("TODO|FIXME|@since|@author|alterado por", Pattern.CASE_INSENSITIVE);
    private static final Pattern NARRATIVA_NO_DOCUMENTO = Pattern.compile(
            "\\b(removeu|deixa de valer|passou a ser|nasce no lugar|substitu[ií]do por|por ora|nesta etapa"
                    + "|tempor[aá]rio|provis[oó]rio)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern TRAVESSAO = Pattern.compile("[\\u2013\\u2014\\u2015]");

    @Test
    void codigoSemComentarioExplicativoNemMarcador() throws IOException {
        List<String> violacoes = new ArrayList<>();
        for (Path arquivo : arquivos("src/main/java", ".java", "src/test/java")) {
            List<String> linhas = Files.readAllLines(arquivo);
            boolean comentarioAnterior = false;
            for (int i = 0; i < linhas.size(); i++) {
                String linha = LITERAL_DE_TEXTO.matcher(linhas.get(i)).replaceAll("\"\"");
                String local = arquivo + ":" + (i + 1);
                if (linha.contains("/*")) {
                    violacoes.add(local + " comentario em bloco");
                }
                int inicio = linha.indexOf("//");
                boolean comentarioDeLinha =
                        inicio >= 0 && linha.substring(0, inicio).isBlank();
                if (comentarioDeLinha && comentarioAnterior) {
                    violacoes.add(local + " comentario com mais de uma linha");
                }
                comentarioAnterior = comentarioDeLinha;
                if (inicio >= 0
                        && MARCADOR_NO_CODIGO.matcher(linha.substring(inicio)).find()) {
                    violacoes.add(local + " marcador temporal ou de autoria");
                }
                if (TRAVESSAO.matcher(linhas.get(i)).find()) {
                    violacoes.add(local + " travessao");
                }
            }
        }
        assertThat(violacoes).isEmpty();
    }

    @Test
    void documentosSemNarrativaDeMudancaNemTravessao() throws IOException {
        List<String> violacoes = new ArrayList<>();
        for (Path arquivo : documentos()) {
            List<String> linhas = Files.readAllLines(arquivo);
            boolean regraRaiz = arquivo.equals(Path.of("CLAUDE.md"));
            for (int i = 0; i < linhas.size(); i++) {
                String local = arquivo + ":" + (i + 1);
                if (TRAVESSAO.matcher(linhas.get(i)).find()) {
                    violacoes.add(local + " travessao");
                }
                if (!regraRaiz && NARRATIVA_NO_DOCUMENTO.matcher(linhas.get(i)).find()) {
                    violacoes.add(local + " narrativa de mudanca");
                }
            }
        }
        assertThat(violacoes).isEmpty();
    }

    private static List<Path> arquivos(String... raizesEExtensao) throws IOException {
        String extensao = raizesEExtensao[1];
        List<Path> encontrados = new ArrayList<>();
        for (String raiz : new String[] {raizesEExtensao[0], raizesEExtensao[2]}) {
            try (Stream<Path> caminhos = Files.walk(Path.of(raiz))) {
                caminhos.filter(c -> c.toString().endsWith(extensao)).forEach(encontrados::add);
            }
        }
        return encontrados;
    }

    private static List<Path> documentos() throws IOException {
        List<Path> encontrados = new ArrayList<>(List.of(Path.of("CLAUDE.md"), Path.of("README.md")));
        for (String raiz : new String[] {"docs", "openspec/specs", "src/main/java"}) {
            try (Stream<Path> caminhos = Files.walk(Path.of(raiz))) {
                caminhos.filter(c -> c.toString().endsWith(".md")).forEach(encontrados::add);
            }
        }
        return encontrados;
    }
}
