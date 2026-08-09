package com.acougue.modules.balanca;

import com.acougue.entity.Produto;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;

/**
 * Gera arquivos de carga de PLU para balanças Toledo MGV7 e Filizola Smart.
 * Todos os arquivos usam encoding ISO-8859-1 e terminação de linha CRLF.
 */
@Component
public class BalancaArquivoGerador {

    /**
     * Formato Toledo MGV6/MGV7:
     * Cabeçalho: 99|CARGA|1|1
     * Dados:     PLU(5)|NOME(22)|PRECO(7.2f)|VALIDADE(3)|TARA(5)
     */
    public String gerarToledoMGV7(List<Produto> produtos) {
        StringBuilder sb = new StringBuilder();
        sb.append("99|CARGA|1|1\r\n");

        for (Produto p : produtos) {
            if (p.getCodigoBalanca() == null || !Boolean.TRUE.equals(p.getAtivo())) continue;
            sb.append(String.format("%05d|%-22s|%07.2f|%03d|%05d\r\n",
                p.getCodigoBalanca(),
                sanitizar(p.getNome(), 22),
                p.getPrecoVenda(),
                0,
                0
            ));
        }
        return sb.toString();
    }

    /**
     * Formato Filizola Smart (CSV posicional):
     * 1;PLU(5);NOME(30);PRECO(vírgula);VALIDADE_DIAS
     */
    public String gerarFilizolaSmart(List<Produto> produtos) {
        StringBuilder sb = new StringBuilder();

        for (Produto p : produtos) {
            if (p.getCodigoBalanca() == null || !Boolean.TRUE.equals(p.getAtivo())) continue;
            sb.append(String.format("1;%05d;%-30s;%s;0\r\n",
                p.getCodigoBalanca(),
                sanitizar(p.getNome(), 30),
                p.getPrecoVenda().toPlainString().replace(".", ",")
            ));
        }
        return sb.toString();
    }

    /** Remove acentos, converte para maiúsculo, trunca e preenche com espaços */
    private String sanitizar(String nome, int tamanhoMax) {
        String sem = Normalizer.normalize(nome.toUpperCase(), Normalizer.Form.NFD)
                               .replaceAll("\\p{InCombiningDiacriticalMarks}", "")
                               .replaceAll("[^A-Z0-9 /\\-]", "")
                               .trim();
        return sem.length() > tamanhoMax
            ? sem.substring(0, tamanhoMax)
            : String.format("%-" + tamanhoMax + "s", sem);
    }
}
