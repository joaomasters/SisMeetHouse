package com.acougue.agent;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

public class BalancaAgentApp {

    private static final Logger LOG = Logger.getLogger(BalancaAgentApp.class.getName());

    private final String  backendUrl;
    private final String  backendToken;
    private final String  balancaIp;
    private final int     balancaPorta;
    private final int     balancaTimeoutSeg;
    private final int     pollIntervaloSeg;

    private final HttpClient http;

    public BalancaAgentApp(Properties cfg) {
        this.backendUrl        = cfg.getProperty("backend.url").stripTrailing().replaceAll("/$", "");
        this.backendToken      = cfg.getProperty("backend.token", "");
        this.balancaIp         = cfg.getProperty("balanca.ip", "");
        this.balancaPorta      = Integer.parseInt(cfg.getProperty("balanca.porta", "8000"));
        this.balancaTimeoutSeg = Integer.parseInt(cfg.getProperty("balanca.timeout.segundos", "15"));
        this.pollIntervaloSeg  = Integer.parseInt(cfg.getProperty("poll.intervalo.segundos", "300"));

        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void iniciar() {
        LOG.info("=== Agente Balanca iniciado ===");
        LOG.info("Backend : " + backendUrl);
        LOG.info("Balanca : " + balancaIp + ":" + balancaPorta);
        LOG.info("Poll    : " + pollIntervaloSeg + "s");

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::verificarEAplicar, 5, pollIntervaloSeg, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Agente encerrado.");
            scheduler.shutdownNow();
        }));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException ignored) {}
    }

    private void verificarEAplicar() {
        try {
            LOG.info("Consultando carga pendente...");
            HttpResponse<String> resp = http.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(backendUrl + "/balanca/agente/carga-pendente"))
                            .header("Authorization", "Bearer " + backendToken)
                            .GET()
                            .timeout(Duration.ofSeconds(10))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 204) {
                LOG.info("Sem carga pendente.");
                return;
            }

            if (resp.statusCode() != 200) {
                LOG.warning("Backend retornou " + resp.statusCode() + ": " + resp.body());
                return;
            }

            String json       = resp.body();
            long   cargaId    = extrairLong(json, "id");
            String conteudo   = extrairString(json, "conteudo");
            String ipOverride = extrairString(json, "ipBalanca");
            int    porta      = (int) extrairLong(json, "portaBalanca");

            String ip = (ipOverride != null && !ipOverride.isBlank()) ? ipOverride : balancaIp;
            int    pt = porta > 0 ? porta : balancaPorta;

            LOG.info("Carga #" + cargaId + " encontrada. Enviando para balanca " + ip + ":" + pt);

            try {
                enviarParaBalanca(ip, pt, conteudo);
                confirmar(cargaId);
                LOG.info("Carga #" + cargaId + " aplicada com sucesso.");
            } catch (Exception e) {
                LOG.severe("Falha ao enviar para balanca: " + e.getMessage());
                registrarErro(cargaId, e.getMessage());
            }

        } catch (Exception e) {
            LOG.warning("Erro ao consultar backend: " + e.getMessage());
        }
    }

    private void enviarParaBalanca(String ip, int porta, String conteudo) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, porta),
                    balancaTimeoutSeg * 1000);
            socket.setSoTimeout(balancaTimeoutSeg * 1000);

            OutputStream out = socket.getOutputStream();
            out.write(conteudo.getBytes(StandardCharsets.ISO_8859_1));
            out.flush();

            try {
                InputStream in  = socket.getInputStream();
                byte[]      buf = new byte[128];
                int         n   = in.read(buf);
                if (n > 0) {
                    LOG.info("Resposta da balanca: " + new String(buf, 0, n).trim());
                }
            } catch (SocketTimeoutException ignored) {}
        }
    }

    private void confirmar(long cargaId) throws Exception {
        http.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(backendUrl + "/balanca/agente/confirmar/" + cargaId))
                        .header("Authorization", "Bearer " + backendToken)
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .timeout(Duration.ofSeconds(10))
                        .build(),
                HttpResponse.BodyHandlers.discarding());
    }

    private void registrarErro(long cargaId, String mensagem) {
        try {
            String body = "{\"mensagem\":\"" + mensagem.replace("\"", "'") + "\"}";
            http.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(backendUrl + "/balanca/agente/erro/" + cargaId))
                            .header("Authorization", "Bearer " + backendToken)
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .timeout(Duration.ofSeconds(10))
                            .build(),
                    HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {}
    }

    private long extrairLong(String json, String chave) {
        String pattern = "\"" + chave + "\":";
        int idx = json.indexOf(pattern);
        if (idx < 0) return 0;
        int start = idx + pattern.length();
        int end   = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        try { return Long.parseLong(json.substring(start, end)); } catch (Exception e) { return 0; }
    }

    private String extrairString(String json, String chave) {
        String pattern = "\"" + chave + "\":\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return "";
        int start = idx + pattern.length();
        int end   = json.indexOf('"', start);
        return end < 0 ? "" : json.substring(start, end)
                .replace("\\n", "\r\n")
                .replace("\\r", "")
                .replace("\\t", "\t");
    }

    public static void main(String[] args) throws Exception {
        setupLogging();

        String cfgPath = args.length > 0 ? args[0] : "agente.properties";
        Properties cfg = new Properties();

        File cfgFile = new File(cfgPath);
        if (cfgFile.exists()) {
            try (FileInputStream fis = new FileInputStream(cfgFile)) {
                cfg.load(fis);
            }
        } else {
            try (InputStream is = BalancaAgentApp.class
                    .getClassLoader().getResourceAsStream("agente.properties")) {
                if (is != null) cfg.load(is);
            }
        }

        new BalancaAgentApp(cfg).iniciar();
    }

    private static void setupLogging() {
        Logger root = Logger.getLogger("");
        root.setLevel(Level.INFO);
        for (Handler h : root.getHandlers()) root.removeHandler(h);

        ConsoleHandler console = new ConsoleHandler();
        console.setFormatter(new SimpleFormatter() {
            @Override public String format(LogRecord r) {
                return String.format("[%tT] [%s] %s%n",
                        r.getMillis(), r.getLevel(), r.getMessage());
            }
        });
        root.addHandler(console);

        try {
            FileHandler file = new FileHandler("balanca-agent.log", 5_000_000, 3, true);
            file.setFormatter(console.getFormatter());
            root.addHandler(file);
        } catch (IOException ignored) {}
    }
}
