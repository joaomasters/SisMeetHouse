package com.acougue.modules.balanca;

import com.acougue.entity.Produto;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;

@Component
public class BalancaArquivoGerador {

    

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
