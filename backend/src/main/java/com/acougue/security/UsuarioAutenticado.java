package com.acougue.security;

import com.acougue.entity.Modulo;

import java.util.Map;

/*
 Identidade do usuário logado, reconstruída a partir das claims do JWT em
 cada requisição (ver JwtAuthFilter). Fica disponível via
 SecurityContextHolder.getContext().getAuthentication().getPrincipal().

 SUPER_ADMIN sempre tem acesso total, independente da matriz de permissões
 gravada no banco — é uma segunda camada de segurança contra permissão
 corrompida ou faltando.
 */
public class UsuarioAutenticado {

    private final Long usuarioId;
    private final String login;
    private final String nome;
    private final String perfilNome;
    private final boolean superAdmin;
    private final Map<Modulo, Permissoes> permissoes;

    public record Permissoes(boolean ver, boolean criar, boolean editar, boolean excluir) {}

    public UsuarioAutenticado(Long usuarioId, String login, String nome, String perfilNome,
                              boolean superAdmin, Map<Modulo, Permissoes> permissoes) {
        this.usuarioId = usuarioId;
        this.login = login;
        this.nome = nome;
        this.perfilNome = perfilNome;
        this.superAdmin = superAdmin;
        this.permissoes = permissoes;
    }

    public boolean pode(Modulo modulo, Acao acao) {
        if (superAdmin) return true;
        Permissoes p = permissoes.get(modulo);
        if (p == null) return false;
        return switch (acao) {
            case VER -> p.ver();
            case CRIAR -> p.criar();
            case EDITAR -> p.editar();
            case EXCLUIR -> p.excluir();
        };
    }

    public Long getUsuarioId()     { return usuarioId; }
    public String getLogin()       { return login; }
    public String getNome()        { return nome; }
    public String getPerfilNome()  { return perfilNome; }
    public boolean isSuperAdmin()  { return superAdmin; }
    public Map<Modulo, Permissoes> getPermissoes() { return permissoes; }
}