package com.projetointegrador.natysync.painel;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

final class ViolacaoDeConstraint {

    static final String EMAIL_UNICO = "usuario_email_idx";

    private ViolacaoDeConstraint() {}

    static boolean foi(DataIntegrityViolationException excecao, String constraint) {
        for (Throwable causa = excecao; causa != null; causa = causa.getCause()) {
            if (causa instanceof ConstraintViolationException violacao) {
                return constraint.equalsIgnoreCase(violacao.getConstraintName());
            }
        }
        return false;
    }
}
