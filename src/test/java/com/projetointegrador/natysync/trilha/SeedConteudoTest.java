package com.projetointegrador.natysync.trilha;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SeedConteudoTest extends IntegracaoTest {

    @Autowired
    private TrilhaRepository trilhaRepository;

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Test
    void seedDeConteudoFoiAplicado() {
        assertThat(trilhaRepository.count()).isEqualTo(1);
        assertThat(atividadeRepository.count()).isEqualTo(6);
        assertThat(quizRepository.count()).isEqualTo(6);
    }
}
