package br.edu.ifpr.catalogo;

import java.util.concurrent.CountDownLatch;

import br.edu.ifpr.catalogo.repository.ConsoleRepository;
import br.edu.ifpr.catalogo.service.ConsoleService;
import br.edu.ifpr.catalogo.tools.ConsoleTools;
import br.edu.ifpr.catalogo.resource.CatalogoResource;
import br.edu.ifpr.catalogo.prompt.ComparacaoPrompt;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;

public class MainServidor {

    public static void main(String[] args) {
        var transporte = new StdioServerTransportProvider(
                McpJsonDefaults.getMapper()
        );

        var repositorio = new ConsoleRepository();
        var servico = new ConsoleService(repositorio);
        var ferramentas = new ConsoleTools(servico);

        McpSyncServer servidor = McpServer.sync(transporte)
                .serverInfo("catalogo-consoles-mcp", "1.0.0")
                .capabilities(
                        McpSchema.ServerCapabilities.builder()
                                .tools(true)
                                .resources(false, true)
                                .prompts(true)
                                .build()
                )
                .build();

        servidor.addTool(ferramentas.listarConsolesTool());
        servidor.addTool(ferramentas.buscarConsolesTool());
        servidor.addTool(ferramentas.compararConsolesTool());
        servidor.addTool(ferramentas.estatisticasConsolesTool());
        servidor.addResource(new CatalogoResource(servico).especificacao());
        servidor.addPrompt(new ComparacaoPrompt(servico).especificacao());

        Runtime.getRuntime().addShutdownHook(
                new Thread(servidor::close)
        );

        System.err.println("Servidor MCP do catálogo iniciado.");

        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
