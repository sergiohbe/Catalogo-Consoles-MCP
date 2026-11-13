# Integração com Claude Desktop

## Estado da validação

Configuração e roteiro preparados em 12/11/2026. A entrada `catalogo-consoles`
foi registrada no arquivo ativo desta instalação do Claude com os caminhos
absolutos do Java 21 e do JAR desta máquina, preservando as demais configurações
e criando backup. O login foi concluído pelo autor do projeto. Foi identificada
a diferença entre o caminho padrão em Roaming e o caminho ativo da versão
Microsoft Store, descrito abaixo.
Após a reinicialização completa, o log MCP do Claude confirmou o início do
servidor e a sequência `initialize`, `notifications/initialized` e `tools/list`.
A interface de Desenvolvedor mostrou `catalogo-consoles` com status
**Em execução** no Claude Desktop 5.1231.2. A conexão e a negociação foram verificadas.
**Validação concluída em 12/11/2026.** O autor enviou o pedido de teste
manualmente e compartilhou a resposta do Claude. Os sete resultados corresponderam
ao roteiro, incluindo o erro de console inexistente e a consulta posterior.
O log MCP local confirmou sete chamadas `tools/call`, IDs 2 a 8, cada uma com
resposta do servidor, entre 18:04:55 e 18:05:12 no horário de São Paulo.
O log consultado registra métodos e IDs, sem os argumentos ou textos completos;
os resultados detalhados abaixo têm como fonte a resposta compartilhada pelo autor.
Os testes Maven e a validação pelo Inspector são evidências separadas.

### Resultados observados

| Operação executada no Claude | Resultado | Estado |
| --- | --- | --- |
| Listagem completa | 40 consoles: Sony 12, Microsoft 11, Nintendo 9, Sega 4 e Atari 4 | Aprovado |
| Filtro Sega | Master System, Mega Drive, Sega Saturn e Dreamcast | Aprovado |
| Busca Dreamcast | Um resultado: Sega, 1998 | Aprovado |
| Comparação ps5-pro / xbox-series-x | Sony, 2024, 9ª geração / Microsoft, 2020, 9ª geração; nomes e IDs corretos | Aprovado |
| Estatísticas | Total 40 e distribuição correta por fabricante | Aprovado |
| Comparação ps2 / console-inexistente | `Console não encontrado: console-inexistente` | Erro esperado confirmado |
| Estatísticas após o erro | Mesmo total e distribuição; servidor continuou atendendo | Aprovado |

A execução cobriu as quatro ferramentas por uma conversa no Claude Desktop.
Não foram coletadas capturas de tela da conversa, nem copiados os logs pessoais
do aplicativo para o repositório.

### Conferência adicional em 13/11/2026

O autor forneceu uma transcrição de novos testes manuais no Claude. Ela registra:

- Listagem dos nove consoles da Nintendo.
- Estatísticas com total 40 e distribuição Sony 12, Microsoft 11, Nintendo 9,
  Atari 4 e Sega 4.
- Busca por `x`, retornando os onze consoles Xbox. A busca é por ocorrência
  do termo, não exclusivamente por prefixo, conforme explicado na resposta.
- Comparação de `ps3` com `xbox-one`, com fabricantes, anos e gerações corretos.
- Erro esperado ao comparar `xbox` com `3do`, ausente do catálogo.
- Comparação válida de `ps5` com `xbox-360` após esse erro.

Esses resultados reforçam a validação das quatro ferramentas. A transcrição
mostra que a contagem foi obtida por `estatisticas_consoles`; não comprova leitura
do resource `catalogo://consoles` nem obtenção de `orientar_comparacao` via
`prompts/get`. A tela de permissões compartilhada pelo autor mostra ferramentas;
não foi identificado um controle de resources ou prompts nessa tela.
O suporte dessas capacidades na interface do Claude permanece sem confirmação,
sem inferir que o aplicativo inteiro não as suporta. Consulte a validação pelo
Inspector e pelo cliente Java no guia de resources e prompts.

## Funcionamento

Claude Desktop inicia o mesmo servidor Java utilizado pelo cliente do projeto.
O processo é executado localmente por STDIO. Ao pedir uma consulta no chat,
Claude pode chamar uma ferramenta e usar os dados retornados para responder.
É necessário verificar a chamada real, pois uma resposta baseada no conhecimento
geral do assistente não comprova a integração com este catálogo.

## Preparar os artefatos

Instale o Claude Desktop pelo [site oficial](https://claude.ai/download),
entre na conta pelo aplicativo e tenha o JDK 21 disponível.
Na raiz do projeto, execute:

```powershell
mvn clean verify
(Get-Command java).Source
(Resolve-Path mcp-server/target/mcp-server-1.0-SNAPSHOT.jar).Path
```

Os dois últimos comandos mostram os caminhos a usar na configuração. O exemplo
do tutorial oficial usa um servidor Node.js; para este projeto, o comando é
`java.exe` e os argumentos são `-jar` e o JAR do servidor. Node.js não é exigido
para executar nosso servidor Java pelo Claude.

## Configuração local no Windows

O arquivo padrão é `%APPDATA%\Claude\claude_desktop_config.json`.
Na instalação Microsoft Store observada neste projeto (Claude Desktop
5.1231.2), a configuração ativa foi encontrada em
`%LOCALAPPDATA%\Claude-Data\claude_pzs8sxrjxfjjc\claude_desktop_config.json`.
O arquivo padrão em Roaming não foi carregado nessa instalação. Confirme o
arquivo usado pela opção de editar configuração do aplicativo antes de salvar.
Use o [exemplo de configuração](claude-desktop-config.example.json), substituindo
os dois caminhos pelos obtidos na sua máquina. Os caminhos absolutos evitam
dependência da pasta em que o Claude foi iniciado. No JSON, barras invertidas
devem ser duplicadas, como no exemplo.

Se o arquivo já existir, preserve suas configurações e outros servidores.
Acrescente apenas a entrada `catalogo-consoles` dentro de `mcpServers`, após
guardar uma cópia do arquivo original. O exemplo é um modelo, com caminhos
fictícios; não funciona sem ajuste. Não versione a configuração pessoal do Claude.

Na versão do aplicativo, procure a área de servidores MCP locais nas
configurações de desenvolvedor ou de extensões. O local varia conforme a versão.
Use a opção de editar configuração, quando disponível. Depois de salvar, encerre
completamente o Claude e abra-o novamente para carregar a configuração.

Não inicie o menu Java nem o Inspector para esse teste: o próprio Claude inicia
uma instância do servidor. Verifique se `catalogo-consoles` está conectado e se
estas quatro ferramentas aparecem:

- `listar_consoles`
- `buscar_consoles`
- `comparar_consoles`
- `estatisticas_consoles`

## Roteiro de validação

Em uma conversa nova, habilite o conector do catálogo, se necessário. Use os
pedidos abaixo, confira o nome e os argumentos das chamadas e autorize o uso
das ferramentas quando o aplicativo solicitar. O usuário deve tratar essas
solicitações de permissão diretamente no Claude.

| Pedido no chat | Chamada esperada | Resultado esperado |
| --- | --- | --- |
| Use a ferramenta do catálogo para listar todos os consoles cadastrados. | `listar_consoles`, sem fabricante | 40 consoles |
| Use o catálogo para listar somente os consoles da Sega. | `listar_consoles`, fabricante `Sega` | 4 consoles |
| Use a busca do catálogo para encontrar Dreamcast. | `buscar_consoles`, termo `Dreamcast` | Sega, 1998, um resultado |
| Use o catálogo para comparar ps5-pro com xbox-series-x. | `comparar_consoles`, primeiro `ps5-pro`, segundo `xbox-series-x` | Os cinco campos de cada console |
| Consulte as estatísticas do catálogo pela ferramenta. | `estatisticas_consoles`, sem parâmetros | Total 40; Atari 4, Microsoft 11, Nintendo 9, Sega 4 e Sony 12 |
| Use o catálogo para comparar ps2 com console-inexistente. | `comparar_consoles` com esses identificadores | Erro controlado informando que o segundo console não existe |

Após o erro, repita as estatísticas para confirmar que o servidor continua
atendendo. Se Claude solicitar confirmação do identificador inexistente,
explique que está testando o tratamento de erros e mantenha o identificador.

Registre versão do Claude, data e resultados. Se guardar capturas, inclua
somente a conversa de teste e os dados públicos do catálogo. A etapa está
concluída quando a conexão, as quatro ferramentas e o erro controlado tiverem
sido conferidos no aplicativo. Não marque aprovação apenas por salvar o JSON.

## Solução de problemas

- **Java não encontrado:** use o caminho completo de `java.exe` no campo `command`.
- **JAR não encontrado:** gere os artefatos e confira o caminho absoluto.
- **Erro no JSON:** confira vírgulas, aspas e barras duplicadas. Preserve os outros servidores.
- **Ferramentas não aparecem:** encerre completamente o Claude, reabra e consulte o status e os logs do servidor local.
- **Permissão pendente:** responda à solicitação no aplicativo para permitir a chamada pretendida.
- **JAR em uso no build:** encerre o servidor no Claude antes de executar `mvn clean verify` novamente.
- **Respostas sem chamadas MCP:** confira se o conector está habilitado e peça explicitamente o uso do catálogo.

## Referências oficiais

- [Servidores MCP locais no Claude Desktop](https://support.claude.com/en/articles/10949351-getting-started-with-local-mcp-servers-on-claude-desktop).
- [Conectar servidores locais e configurar o arquivo JSON](https://modelcontextprotocol.io/docs/develop/connect-local-servers).
