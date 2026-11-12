# Catálogo de Consoles MCP

Projeto acadêmico de Serviços Web em Java 21 e Apache Maven, com servidor e
cliente MCP separados. O catálogo contém 40 consoles de Sony, Microsoft,
Nintendo, Sega e Atari, definidos em memória no servidor.

## Pré-requisitos

- JDK 21 configurado para o Maven e disponível pelo comando `java`.
- Apache Maven disponível pelo comando `mvn`.
- Acesso à internet na primeira compilação para obter as dependências.

Execute os comandos abaixo na raiz do repositório.

## Testes e builds

| Comando | Resultado |
| --- | --- |
| `mvn clean test` | Compila os módulos e executa os testes unitários. Não inicia o servidor nem executa os testes de integração. |
| `mvn clean package` | Executa os testes unitários e gera os dois JARs executáveis. |
| `mvn clean verify` | Executa os testes unitários, gera os JARs e executa os testes de integração com um processo real do servidor. |
| `mvn -pl mcp-server -am package` | Gera o servidor e executa seus testes unitários. |
| `mvn -pl mcp-client -am verify` | Constrói também o servidor necessário e valida o cliente com testes unitários e de integração. |

O servidor é uma dependência de teste do cliente, garantindo a ordem do build
Maven. Ele não é incluído como dependência de execução no JAR do cliente.
Para os testes de integração, prefira `verify` na raiz ou use `-am` como acima;
executar apenas `mvn test` não gera o JAR exigido por esses testes.

### Testes unitários

Executados pelo Maven Surefire, cobrem o serviço do catálogo, ferramentas,
tratamento de falhas e interação com o menu. Os testes do menu simulam as
respostas MCP e não precisam iniciar outro processo.

No servidor, os cenários incluem campos textuais nulos ou vazios, normalização
dos dados, limite inferior do ano de lançamento, identificadores únicos,
proteção contra alteração do catálogo, filtros, buscas sem resultado,
identificadores inválidos, comparação com consoles ausentes e estatísticas de
um catálogo vazio. Esses cenários complementam os testes de sucesso; não
representam uma medição percentual de cobertura.

Relatórios: `mcp-server/target/surefire-reports/` e
`mcp-client/target/surefire-reports/`.

### Testes de integração

Executados pelo Maven Failsafe nas fases `integration-test` e `verify`.
`McpClientIT` inicia o JAR recém-gerado do servidor por STDIO, negocia a sessão,
descobre as quatro ferramentas e executa consultas pelo `ClienteCatalogo`.

Os cenários verificam listagem e filtro, busca com e sem resultados,
comparação por ID ou nome, estatísticas e entradas inválidas. Também verificam
que o servidor continua atendendo após erros de comparação e que o processo
filho termina depois do fechamento da sessão. Uma falha no encerramento faz o
teste falhar; a limpeza força o término do processo se necessário.

Um dos testes executa o menu com as ferramentas e os esquemas descobertos no
servidor real. O cliente não depende de opções fixas: gera o menu a partir da
descoberta MCP e solicita os parâmetros descritos pelo servidor.

Não é necessário iniciar o servidor, Claude Desktop ou MCP Inspector para
executar essa suíte. Cada requisição e a inicialização têm limite de 15 segundos.

Relatórios: `mcp-client/target/failsafe-reports/`.

## Executar o cliente

Depois de `mvn clean verify`, execute:

```powershell
java -jar mcp-client/target/mcp-client-1.0-SNAPSHOT.jar mcp-server/target/mcp-server-1.0-SNAPSHOT.jar
```

O cliente inicia o servidor automaticamente. Veja o [manual do cliente](mcp-client/README.md)
para as opções do menu e o modo de busca por argumento.

Os artefatos executáveis são:

- `mcp-server/target/mcp-server-1.0-SNAPSHOT.jar`
- `mcp-client/target/mcp-client-1.0-SNAPSHOT.jar`

Os arquivos com prefixo `original-` são intermediários do empacotamento e não
devem ser usados para executar a aplicação. As pastas `target` são geradas pelo
Maven e não precisam ser versionadas.

## Validação com MCP Inspector

As quatro ferramentas foram validadas pelo modo CLI do MCP Inspector 2.13.0,
com cenários de sucesso, ausência de resultados e erros de comparação.
Veja o [roteiro e as evidências](docs/validacao-inspector.md), incluindo os
comandos para repetir a validação e a conferência manual na interface visual.
Essa validação é independente da suíte Maven e da futura conferência no Claude Desktop.

### JAR em uso durante o build

No Windows, uma conexão aberta no Inspector ou em outro cliente pode manter
o JAR do servidor bloqueado. Se `mvn clean verify` informar que o arquivo está
sendo usado por outro processo, desconecte o servidor no Inspector e encerre
os clientes que o estejam usando antes de repetir o build. `mvn test` permite
executar os testes unitários sem recriar o JAR, mas não substitui o ciclo
completo de empacotamento e integração.
