package com.acougue.modules.balanca;

import com.acougue.entity.CargaBalanca;
import com.acougue.entity.Produto;
import com.acougue.modules.balanca.dto.EanParseResult;
import com.acougue.modules.estoque.ProdutoService;
import com.acougue.repository.CargaBalancaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/balanca")
@RequiredArgsConstructor
public class BalancaController {

    private final EanBalancaParser        eanParser;
    private final BalancaArquivoGerador   gerador;
    private final ProdutoService          produtoService;
    private final CargaBalancaRepository  cargaRepo;

    @GetMapping("/parse/{ean13}")
    public ResponseEntity<EanParseResult> parsearEan(
            @PathVariable String ean13,
            @RequestParam(required = false) BigDecimal precoKg) {
        return ResponseEntity.ok(eanParser.parse(ean13, precoKg));
    }

    @GetMapping("/carga/toledo-mgv7")
    public ResponseEntity<byte[]> downloadToledoMGV7() {
        List<Produto> produtos = produtoService.listarParaBalanca();
        String conteudo = gerador.gerarToledoMGV7(produtos);
        byte[] bytes = conteudo.getBytes(StandardCharsets.ISO_8859_1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "carga_toledo.txt");
        headers.setContentLength(bytes.length);

        registrarCarga(produtos, "TOLEDO_MGV7");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @GetMapping("/carga/filizola-smart")
    public ResponseEntity<byte[]> downloadFilizolaSmart() {
        List<Produto> produtos = produtoService.listarParaBalanca();
        String conteudo = gerador.gerarFilizolaSmart(produtos);
        byte[] bytes = conteudo.getBytes(StandardCharsets.ISO_8859_1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "carga_filizola.txt");
        headers.setContentLength(bytes.length);

        registrarCarga(produtos, "FILIZOLA_SMART");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @GetMapping("/carga/preview/{tipo}")
    public ResponseEntity<String> previewCarga(@PathVariable String tipo) {
        List<Produto> produtos = produtoService.listarParaBalanca();
        String conteudo = switch (tipo.toUpperCase()) {
            case "TOLEDO_MGV7", "TOLEDO_MGV6" -> gerador.gerarToledoMGV7(produtos);
            case "FILIZOLA_SMART"             -> gerador.gerarFilizolaSmart(produtos);
            default -> throw new IllegalArgumentException("Tipo de balança inválido: " + tipo);
        };
        return ResponseEntity.ok(conteudo);
    }

    private void registrarCarga(List<Produto> produtos, String tipoBalanca) {
        produtos.forEach(p -> {
            if (p.getCodigoBalanca() != null) {
                CargaBalanca carga = CargaBalanca.builder()
                        .produto(p)
                        .tipoBalanca(tipoBalanca)
                        .codigoPlu(p.getCodigoBalanca())
                        .precoEnviado(p.getPrecoVenda())
                        .status("ENVIADO")
                        .build();
                cargaRepo.save(carga);
            }
        });
    }
}
