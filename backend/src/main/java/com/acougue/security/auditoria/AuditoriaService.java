package com.acougue.security.auditoria;

import com.acougue.entity.LogAuditoria;
import com.acougue.repository.LogAuditoriaRepository;
import com.acougue.security.ContextoUsuario;
import com.acougue.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Grava e consulta o histórico de ações administrativas (CRIAR/EDITAR/
 * EXCLUIR) feitas no sistema. Chamado pelo {@code AuditoriaAspect} logo
 * após cada ação bem-sucedida.
 *
 * Princípio importante: registrar um log NUNCA pode derrubar a ação real
 * do usuário. Qualquer erro aqui é engolido e só vai pro log da aplicação
 * (slf4j) — o aspecto que chama este serviço também está preparado pra
 * isso, mas a garantia vive nos dois lugares por segurança.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final LogAuditoriaRepository repo;

    public void registrar(String modulo, String acao, String descricao) {
        try {
            UsuarioAutenticado usuario = ContextoUsuario.atual();
            LogAuditoria entrada = LogAuditoria.builder()
                    .usuarioId(usuario.getUsuarioId())
                    .nomeUsuario(usuario.getNome())
                    .perfilUsuario(usuario.getPerfilNome())
                    .modulo(modulo)
                    .acao(acao)
                    .descricao(descricao)
                    .build();
            repo.save(entrada);
        } catch (Exception e) {
            log.warn("Falha ao gravar log de auditoria ({} / {} / {}): {}", modulo, acao, descricao, e.getMessage());
        }
    }

    public List<LogAuditoria> listar(LocalDate inicio, LocalDate fim, String modulo, String acao, Long usuarioId) {
        LocalDateTime desde = inicio != null ? inicio.atStartOfDay() : LocalDateTime.now().minusDays(7).toLocalDate().atStartOfDay();
        LocalDateTime ate   = fim    != null ? fim.atTime(LocalTime.MAX) : LocalDateTime.now();
        String moduloFiltro = (modulo == null || modulo.isBlank()) ? null : modulo;
        String acaoFiltro   = (acao == null || acao.isBlank()) ? null : acao;
        return repo.buscarComFiltros(desde, ate, moduloFiltro, acaoFiltro, usuarioId);
    }
}