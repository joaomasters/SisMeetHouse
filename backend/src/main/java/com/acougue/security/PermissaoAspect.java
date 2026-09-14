package com.acougue.security;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/*
 Intercepta qualquer método anotado com @ExigirPermissao e confere a
 permissão ANTES do método rodar — se o usuário logado não tiver a ação
 marcada no módulo indicado, a requisição é barrada com 403 e o método
 anotado nunca chega a ser executado.
 */

@Aspect
@Component
public class PermissaoAspect {

    @Before("@annotation(exigirPermissao)")
    public void verificar(ExigirPermissao exigirPermissao) {
        ContextoUsuario.exigirPermissao(exigirPermissao.modulo(), exigirPermissao.acao());
    }
}