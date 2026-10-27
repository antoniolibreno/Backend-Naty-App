package com.projetointegrador.natysync.trilha;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.regex.Pattern;

public final class UrlVideo {

    private static final Set<String> DOMINIOS = Set.of("youtube.com", "www.youtube.com", "m.youtube.com", "youtu.be");
    private static final Pattern IDENTIFICADOR = Pattern.compile("[a-zA-Z0-9_-]{11}");

    private UrlVideo() {}

    public static void validar(String valor) {
        if (valor == null) {
            return;
        }
        try {
            URI uri = new URI(valor);
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null
                    || !DOMINIOS.contains(uri.getHost().toLowerCase(java.util.Locale.ROOT))
                    || uri.getUserInfo() != null
                    || uri.getPort() != -1
                    || !possuiIdentificador(uri)) {
                throw new IllegalArgumentException("URL de video deve apontar para um video HTTPS do YouTube");
            }
        } catch (URISyntaxException excecao) {
            throw new IllegalArgumentException("URL de video invalida");
        }
    }

    private static boolean possuiIdentificador(URI uri) {
        String caminho = uri.getRawPath();
        if ("youtu.be".equalsIgnoreCase(uri.getHost())) {
            return caminho != null
                    && caminho.startsWith("/")
                    && IDENTIFICADOR.matcher(caminho.substring(1)).matches();
        }
        if ("/watch".equals(caminho) && uri.getRawQuery() != null) {
            String identificador = null;
            for (String parametro : uri.getRawQuery().split("&")) {
                if (parametro.startsWith("v=")) {
                    if (identificador != null) {
                        return false;
                    }
                    identificador = parametro.substring(2);
                }
            }
            return identificador != null && IDENTIFICADOR.matcher(identificador).matches();
        }
        return caminho != null && caminho.matches("/(embed|shorts)/[a-zA-Z0-9_-]{11}");
    }
}
