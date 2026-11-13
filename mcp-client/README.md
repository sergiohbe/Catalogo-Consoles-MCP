# Cliente MCP

Além do menu de ferramentas, o cliente oferece `--recursos`, `--recurso URI`,
`--prompts` e `--prompt NOME parametro=valor`. Veja os
[exemplos de resources e prompts](../docs/recursos-e-prompts.md).

Requer Java 21 e Maven. Na raiz do projeto, compile os dois módulos:

```powershell
mvn package
```

Execute o cliente, informando o JAR do servidor:

```powershell
java -jar mcp-client/target/mcp-client-1.0-SNAPSHOT.jar mcp-server/target/mcp-server-1.0-SNAPSHOT.jar
```

O cliente inicia o servidor automaticamente por STDIO, inicializa a sessão MCP,
descobre todas as páginas de ferramentas disponíveis e abre um menu dinâmico.
As opções são ordenadas pelo nome recebido do servidor. Atualmente:

- `1 - buscar_consoles`: buscar consoles por um termo;
- `2 - comparar_consoles`: comparar dois consoles pelo ID ou nome completo;
- `3 - estatisticas_consoles`: consultar o total e a quantidade por fabricante;
- `4 - listar_consoles`: listar todos ou filtrar por fabricante;
- `0 - Sair`.

Os números podem mudar quando ferramentas forem adicionadas ou removidas.
As descrições e os parâmetros são obtidos dos esquemas MCP, sem uma lista de
operações fixa no menu. A descoberta acontece ao iniciar o cliente; reinicie-o
para refletir alterações na lista de ferramentas de um servidor em execução.

Para `listar_consoles`, pressione Enter no parâmetro opcional `fabricante` para
listar todos os consoles, ou informe Sony, Microsoft, Sega, Nintendo ou Atari
para filtrar. Parâmetros obrigatórios em branco cancelam a consulta e retornam
ao menu. Campos opcionais em branco são omitidos da chamada.

Para esquemas com propriedades de texto, o cliente solicita cada campo pelo
nome e mostra sua descrição, em ordem alfabética. Para outros esquemas, mostra
o esquema e recebe um objeto JSON em uma linha, preservando os tipos dos valores.
Por exemplo: `{"ano":2000,"ativo":true}`. A validação das demais restrições do
esquema fica a cargo do servidor. JSON inválido cancela a consulta.

A conexão permanece aberta entre as consultas. Opções inválidas e consultas
vazias exibem uma mensagem e retornam ao menu. O fim da entrada também encerra
o cliente. Ao terminar, fecha a conexão e o processo do servidor pelo transporte do SDK.
Não é necessário iniciar o servidor ou o MCP Inspector separadamente.

Na comparação, informe, por exemplo, `ps5-pro` e `xbox-series-x`.
São exibidos nome, ID, fabricante, ano de lançamento e geração de cada console.
Nomes completos também são aceitos, ignorando maiúsculas e minúsculas.
Se um console não existir, o cliente mostra a mensagem e volta ao menu.

As estatísticas são consultadas no servidor pela ferramenta MCP
`estatisticas_consoles`, sem parâmetros. As contagens são calculadas a partir
do catálogo, e os fabricantes aparecem em ordem alfabética.

O modo anterior continua disponível: informe um termo no segundo argumento
para listar todos os consoles, executar uma busca e sair sem abrir o menu:

```powershell
java -jar mcp-client/target/mcp-client-1.0-SNAPSHOT.jar mcp-server/target/mcp-server-1.0-SNAPSHOT.jar "Atari"
```

Caminhos com espaços devem estar entre aspas. Caminhos relativos são resolvidos
a partir da pasta em que o comando é executado. Falhas são exibidas na saída de
erro e encerram o cliente com código 1.

O caminho do servidor é validado antes da conexão. A inicialização e cada
requisição têm limite de 15 segundos. Em caso de tempo limite ou falha na
comunicação, o cliente fecha a sessão e apresenta uma orientação para executar
novamente. Erros retornados pelas ferramentas, como console inexistente na
comparação, mantêm o menu disponível.

## Organização e testes

`MainCliente` gerencia a inicialização e o encerramento da sessão.
`ClienteCatalogo` faz as chamadas MCP e trata as falhas de comunicação.
`MenuCliente` cuida da entrada e da saída do terminal, usando o contrato
`ConsultaCatalogo` para enviar consultas.

Execute `mvn test` na raiz para rodar os testes do servidor e do cliente.
Os testes do menu simulam as respostas das consultas, sem iniciar um servidor:
cobrem as operações do catálogo, uma ferramenta desconhecida pelo cliente,
parâmetros opcionais e obrigatórios, entrada JSON, ausência de ferramentas,
entradas inválidas, fim de entrada, erros das ferramentas e falhas de comunicação.

Para executar também os testes de integração com o servidor real, use
`mvn clean verify` na raiz. O Maven gera o JAR do servidor antes de iniciar
`McpClientIT`, que exercita as quatro ferramentas por STDIO, valida o menu
usando os esquemas reais e verifica o encerramento do processo.
Os relatórios ficam em `target/failsafe-reports/`
deste módulo. Consulte os [comandos de build e testes](../README.md).
