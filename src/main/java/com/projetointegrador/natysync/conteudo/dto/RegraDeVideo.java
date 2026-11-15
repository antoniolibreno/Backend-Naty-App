package com.projetointegrador.natysync.conteudo.dto;

import com.projetointegrador.natysync.trilha.UrlVideo;

final class RegraDeVideo {

    private static final int XP_PADRAO = 10;

    private RegraDeVideo() {}

    static String ausenteSeVazio(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    static int xpOuPadrao(Integer xp) {
        return xp == null ? XP_PADRAO : xp;
    }

    static boolean urlAceita(String videoUrl) {
        try {
            UrlVideo.validar(videoUrl);
            return true;
        } catch (IllegalArgumentException excecao) {
            return false;
        }
    }

    static boolean duracaoCoerente(String videoUrl, Integer duracaoSegundos) {
        if (videoUrl == null) {
            return duracaoSegundos == null;
        }
        return duracaoSegundos != null && duracaoSegundos > 0;
    }
}
