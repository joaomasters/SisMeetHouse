package com.acougue.security;

import com.acougue.entity.Modulo;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

public class ContextoUsuario {

    private ContextoUsuario() {}

    public static UsuarioAutenticado atual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UsuarioAutenticado usuario) {
            return usuario;
        }
        throw new IllegalStateException("Nenhum usuário autenticado no contexto atual.");
    }

    // Lança 403 se o usuário logado não tiver a permissão exigida naquele módulo
    public static void exigirPermissao(Modulo modulo, Acao acao) {
        if (!atual().pode(modulo, acao)) {
            throw new AccessDeniedException(
                    "Seu perfil não tem permissão de " + acao.name().toLowerCase() +
                            " no módulo " + modulo.getRotulo() + ".");
        }
    }
}