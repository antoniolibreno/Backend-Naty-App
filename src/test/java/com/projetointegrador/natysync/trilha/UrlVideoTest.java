package com.projetointegrador.natysync.trilha;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UrlVideoTest {

    @ParameterizedTest
    @ValueSource(
            strings = {
                "https://www.youtube.com/watch?v=abcdefghijk",
                "https://youtube.com/watch?list=teste&v=abcdefghijk&t=10",
                "https://m.youtube.com/watch?v=abcdefghijk",
                "https://youtu.be/abcdefghijk?si=teste",
                "https://www.youtube.com/embed/abcdefghijk",
                "https://www.youtube.com/shorts/abcdefghijk"
            })
    void aceitaVideoDoYoutube(String url) {
        assertThatCode(() -> new Atividade().setVideoUrl(url)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "https://youtube.com.exemplo.org/watch?v=abcdefghijk",
                "https://exemplo.org/youtube.com/watch?v=abcdefghijk",
                "https://www.youtube.com@exemplo.org/watch?v=abcdefghijk",
                "https://usuario@www.youtube.com/watch?v=abcdefghijk",
                "https://www.youtube.com:8443/watch?v=abcdefghijk",
                "http://www.youtube.com/watch?v=abcdefghijk",
                "javascript:alert(1)",
                "//www.youtube.com/watch?v=abcdefghijk",
                "https://www.youtube.com/watch?v=curto",
                "https://www.youtube.com/watch?v=abcdefghijk&v=lmnopqrstuv",
                "https://www.youtube.com/",
                "https://youtu.be/",
                "https://www.youtube.com/watch?v=abc defghij",
                ""
            })
    void recusaUrlMalformadaOuDeOutraOrigem(String url) {
        assertThatThrownBy(() -> new Atividade().setVideoUrl(url)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void permiteAtividadeSemVideo() {
        assertThatCode(() -> new Atividade().setVideoUrl(null)).doesNotThrowAnyException();
    }
}
