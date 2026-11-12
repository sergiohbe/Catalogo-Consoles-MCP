# Cliente MCP

Requer Java 21 e Maven. Na raiz do projeto, compile os dois módulos:

```powershell
mvn package
```

Execute o cliente, informando o JAR do servidor:

```powershell
java -jar mcp-client/target/mcp-client-1.0-SNAPSHOT.jar mcp-server/target/mcp-server-1.0-SNAPSHOT.jar
```

O cliente inicia o servidor automaticamente por STDIO, inicializa a sessão MCP,
lista as ferramentas disponíveis e abre um menu interativo:

- `1`: listar os 40 consoles cadastrados;
- `2`: filtrar por fabricante (Sony, Microsoft, Sega, Nintendo ou Atari);
- `3`: buscar consoles por um termo;
- `4`: comparar dois consoles pelo ID ou nome completo;
- `5`: consultar o total de consoles e a quantidade por fabricante;
- `0`: sair.

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
cobrem as cinco opções, entradas inválidas, campos vazios, fim de entrada,
erros retornados pelas ferramentas e falhas de comunicação.
