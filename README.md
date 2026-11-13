# Catálogo de Consoles MCP

Projeto acadêmico de Serviços Web que permite consultar um catálogo de videogames
por meio do **Model Context Protocol (MCP)**. O MCP padroniza como um cliente descobre
e utiliza ferramentas, dados e modelos de mensagens oferecidos por um servidor.

O projeto contém um **servidor Java**, responsável pelos dados e pelas consultas,
e um **cliente Java de terminal**, que oferece um menu interativo. O mesmo servidor
também pode ser utilizado pelo MCP Inspector e pelo Claude Desktop.

São disponibilizadas quatro ferramentas: listar consoles, buscar consoles,
comparar dois consoles e consultar estatísticas. Há também um resource para ler
o catálogo em JSON e um prompt para orientar uma comparação.

## Índice

- [Primeira execução](#primeira-execução)
- [Dados do catálogo](#dados-do-catálogo)
- [Funcionamento e organização](#funcionamento-e-organização)
- [Ferramentas disponíveis](#ferramentas-disponíveis)
- [Resource e prompt](#resource-e-prompt)
- [Build e testes](#build-e-testes)
- [Abrir no Eclipse](#abrir-no-eclipse)
- [Testar com o MCP Inspector](#testar-com-o-mcp-inspector)
- [Usar com o Claude Desktop](#usar-com-o-claude-desktop)
- [Problemas comuns](#problemas-comuns)
- [Roteiro de avaliação](#roteiro-de-avaliação)

## Primeira execução

### 1. Preparar o ambiente

Para compilar e executar o projeto, são necessários:

- **JDK 21**, disponível pelo comando `java` e configurado como Java do Maven.
- **Apache Maven**, disponível pelo comando `mvn`.
- Acesso à internet para baixar as dependências na primeira compilação.
- Git, caso opte por clonar o repositório. Também é possível baixar e extrair o ZIP pelo GitHub.

Confira o ambiente no terminal:

```powershell
java -version
mvn -version
```

Os dois comandos devem indicar Java 21. Os exemplos deste documento usam
PowerShell no Windows. Node.js, Inspector e Claude Desktop são necessários apenas
para as respectivas integrações; o cliente Java funciona sem eles.

### 2. Obter o código e gerar os executáveis

```powershell
git clone https://github.com/sergiohbe/Catalogo-Consoles-MCP.git
cd Catalogo-Consoles-MCP
mvn clean verify
```

Se já tiver o projeto ou tiver extraído o ZIP, abra o terminal na pasta que contém
o `pom.xml` principal, `mcp-server` e `mcp-client`, e execute `mvn clean verify`.
Todos os comandos seguintes partem dessa pasta.

Ao concluir com **BUILD SUCCESS**, o Maven terá executado os testes unitários e de
integração e gerado estes arquivos:

| Arquivo | Finalidade |
| --- | --- |
| `mcp-server/target/mcp-server-1.0-SNAPSHOT.jar` | Servidor MCP, iniciado pelo cliente que o utiliza |
| `mcp-client/target/mcp-client-1.0-SNAPSHOT.jar` | Cliente de terminal com menu e comandos de consulta |

Esses JARs incluem as dependências necessárias para execução; o Java continua
sendo necessário na máquina. Arquivos com prefixo `original-` são intermediários
do empacotamento. Use os nomes da tabela para executar o projeto.

### 3. Abrir o menu

```powershell
java -jar mcp-client/target/mcp-client-1.0-SNAPSHOT.jar mcp-server/target/mcp-server-1.0-SNAPSHOT.jar
```

O primeiro JAR é o cliente; o segundo argumento informa onde está o servidor.
**O cliente inicia o servidor automaticamente.** Não é necessário abrir outro
terminal para executar o servidor.

Depois da conexão, o menu apresenta as ferramentas descobertas por MCP, atualmente:

```text
1 - buscar_consoles
2 - comparar_consoles
3 - estatisticas_consoles
4 - listar_consoles
0 - Sair
```

Para uma primeira consulta, escolha `4` e pressione Enter sem preencher
`fabricante`: os 40 consoles serão listados. Repita a opção e digite `Sega`
para receber apenas os quatro consoles dessa fabricante. Use `0` para sair;
o cliente fecha a sessão e encerra o processo do servidor.

O menu é gerado pelos nomes, descrições e esquemas enviados pelo servidor.
Os números podem mudar se a lista de ferramentas mudar. Parâmetros obrigatórios
em branco cancelam a consulta e retornam ao menu; opcionais em branco são omitidos.

Também existe um modo sem menu: o comando abaixo lista todos os consoles,
busca por `Atari` e encerra a sessão.

```powershell
java -jar mcp-client/target/mcp-client-1.0-SNAPSHOT.jar mcp-server/target/mcp-server-1.0-SNAPSHOT.jar "Atari"
```

Coloque caminhos com espaços entre aspas. Consulte o [manual do cliente](mcp-client/README.md)
para mais detalhes sobre a entrada de parâmetros e o tratamento de falhas.

## Dados do catálogo

O catálogo contém **40 consoles**, definidos no código e mantidos em memória:

| Fabricante | Quantidade |
| --- | ---: |
| Sony | 12 |
| Microsoft | 11 |
| Nintendo | 9 |
| Sega | 4 |
| Atari | 4 |

Cada console possui cinco campos:

| Campo | Tipo | Exemplo |
| --- | --- | --- |
| `id` | Texto | `ps2` |
| `nome` | Texto | `PlayStation 2` |
| `fabricante` | Texto | `Sony` |
| `anoLancamento` | Inteiro | `2000` |
| `geracao` | Texto | `6ª geração` |

As operações são somente de leitura. Não há cadastro, edição, exclusão, banco de
dados ou consulta a APIs externas. Para alterar os dados, é necessário editar
`ConsoleRepository` e recompilar. Preço, desempenho, jogos e especificações de
hardware não fazem parte do catálogo.

## Funcionamento e organização

O projeto usa Java 21, Maven, o SDK Java MCP 2.1.0 e JUnit 5.12.2.
A aplicação não utiliza Spring nem Lombok.

```text
Cliente Java / MCP Inspector / Claude Desktop
                    |
               MCP por STDIO
                    |
              Servidor Java
                    |
              ConsoleService
                    |
            ConsoleRepository
```

**STDIO** significa que as mensagens MCP passam pela entrada e saída padrão de
um processo local. Cada cliente inicia sua própria instância do servidor.
Não há servidor HTTP, endereço web ou porta do catálogo a configurar.
O cliente Java não chama um modelo de IA; ele exibe os resultados recebidos.

```text
catalogo-consoles-mcp/
├── pom.xml                 Projeto Maven principal: reúne os dois módulos
├── mcp-server/
│   ├── pom.xml
│   └── src/
│       ├── main/java/br/edu/ifpr/catalogo/
│       │   ├── MainServidor.java
│       │   ├── model/      Modelo Console
│       │   ├── repository/ Dados do catálogo
│       │   ├── service/    Regras de consulta e contagem
│       │   ├── tools/      Quatro ferramentas MCP
│       │   ├── resource/   Leitura do catálogo em JSON
│       │   └── prompt/     Modelo de mensagem para comparação
│       └── test/           Testes unitários do servidor
├── mcp-client/
│   ├── pom.xml
│   ├── README.md
│   └── src/
│       ├── main/java/br/edu/ifpr/catalogo/
│       │                   Conexão, menu, comandos e tratamento de falhas
│       └── test/           Testes unitários e de integração MCP
└── docs/                   Guias e registros das validações externas
```

`MainServidor` registra as capacidades MCP. `ConsoleService` concentra as
consultas aos dados de `ConsoleRepository`, compartilhadas pelas ferramentas,
pelo resource e pelo prompt. No cliente, `MainCliente` controla a sessão,
`ClienteCatalogo` realiza as chamadas, `MenuCliente` cuida da interação e
`ComandosContexto` trata os comandos de resources e prompts.

## Ferramentas disponíveis

Os exemplos JSON abaixo são **argumentos das chamadas MCP**, não comandos para
colar no PowerShell. No cliente Java, escolha a ferramenta e preencha os campos
solicitados; no Inspector, use os campos da ferramenta selecionada.

### `listar_consoles`

Lista todos os consoles ou filtra por uma fabricante.

| Parâmetro | Tipo | Obrigatório | Comportamento |
| --- | --- | --- | --- |
| `fabricante` | Texto | Não | Sem valor ou em branco, lista todos; com valor, filtra por uma fabricante |

O filtro aceita `Sony`, `Microsoft`, `Nintendo`, `Sega` ou `Atari`, ignorando
maiúsculas/minúsculas e espaços nas extremidades. A correspondência é pelo nome
completo da fabricante: `Seg` não equivale a `Sega`.

Exemplo de argumentos: `{"fabricante":"Sega"}`. Resultado:

```text
Consoles da fabricante Sega (4):
- Master System | Fabricante: Sega | Lançamento: 1985
- Mega Drive (Genesis) | Fabricante: Sega | Lançamento: 1988
- Sega Saturn | Fabricante: Sega | Lançamento: 1994
- Dreamcast | Fabricante: Sega | Lançamento: 1998
```

Com `{}`, o resultado começa com `Consoles cadastrados (40):`. Cada item mostra
nome, fabricante e ano. Se o filtro não encontrar consoles, retorna normalmente:
`Nenhum console encontrado para o fabricante informado.`

### `buscar_consoles`

Busca pela ocorrência de um termo no **ID, nome, fabricante ou geração**,
ignorando maiúsculas/minúsculas e espaços nas extremidades do termo.
O ano de lançamento não participa da busca.

| Parâmetro | Tipo | Obrigatório | Conteúdo |
| --- | --- | --- | --- |
| `termo` | Texto | Sim | Nome, fabricante, ID ou geração, como `Dreamcast`, `Atari`, `ps2` ou `6ª` |

Exemplo de argumentos: `{"termo":"Dreamcast"}`. Resultado:

```text
Resultados da busca por "Dreamcast" (1):
- Dreamcast | Fabricante: Sega | Lançamento: 1998 | Geração: 6ª geração
```

É uma busca por trecho de texto, sem tratamento especial para um número
específico. Por exemplo, `6` encontra o PlayStation 2 por sua geração e também
pode corresponder a IDs ou nomes de outros consoles. O resultado inclui a geração
para tornar essas correspondências compreensíveis.

- Sem correspondência: `Nenhum console encontrado para o termo informado.`
- Termo vazio ou somente com espaços: erro da ferramenta, com
  `Informe um termo para realizar a busca.`

### `comparar_consoles`

Apresenta os cinco campos de dois consoles na mesma resposta textual.
A comparação não determina qual console é melhor.

| Parâmetro | Tipo | Obrigatório | Conteúdo |
| --- | --- | --- | --- |
| `primeiro` | Texto | Sim | ID ou nome completo do primeiro console |
| `segundo` | Texto | Sim | ID ou nome completo do segundo console |

Os identificadores ignoram maiúsculas/minúsculas e espaços nas extremidades.
Nomes parciais não são aceitos nessa operação.

Exemplo de argumentos: `{"primeiro":"ps5-pro","segundo":"xbox-series-x"}`. Resultado:

```text
Comparação de consoles:
- PlayStation 5 Pro | ID: ps5-pro | Fabricante: Sony | Lançamento: 2024 | Geração: 9ª geração
- Xbox Series X | ID: xbox-series-x | Fabricante: Microsoft | Lançamento: 2020 | Geração: 9ª geração
```

- Console não cadastrado, como na comparação de `ps3` com `3do`:
  `Console não encontrado: 3do`. **É uma resposta normal, sem `Tool Error`.**
- Identificador obrigatório vazio ou somente com espaços: erro da ferramenta,
  com `Informe o ID ou nome completo dos dois consoles.`

### `estatisticas_consoles`

Não recebe parâmetros; os argumentos são `{}`. Calcula o total e a quantidade
por fabricante a partir do catálogo, com fabricantes em ordem alfabética:

```text
Total de consoles: 40
Consoles por fabricante:
- Atari: 4
- Microsoft: 11
- Nintendo: 9
- Sega: 4
- Sony: 12
```

### Resultados vazios e erros de entrada

Uma busca sem resultados, uma fabricante não encontrada ou um console inexistente
na ferramenta de comparação são respostas normais. A sessão permanece disponível.
Já parâmetros obrigatórios vazios são sinalizados como erros (`isError: true`).

Argumentos ausentes ou de tipo diferente do esquema, como `{"termo":6}`, também
são entradas inválidas. O SDK ou o cliente pode rejeitá-los antes de executar a
ferramenta; nesse caso, a mensagem depende da camada que fez a validação.
No menu Java, campos obrigatórios em branco cancelam a consulta antes do envio.

## Resource e prompt

Essas capacidades usam operações MCP próprias. Por isso, não aparecem como novas
opções no menu das quatro ferramentas.

| Capacidade | Identificador | Finalidade |
| --- | --- | --- |
| Resource | `catalogo://consoles` | Ler os 40 consoles como uma lista JSON, com os cinco campos de cada console; MIME type `application/json`, somente leitura |
| Prompt | `orientar_comparacao` | Obter uma mensagem que orienta a comparação de dois consoles; exige `primeiro` e `segundo`, por ID ou nome completo |

O prompt inclui os dados dos dois consoles e orienta uma IA a produzir uma tabela
e explicar diferenças sem inventar informações ausentes. Ele **não executa uma
IA**: o cliente Java apenas exibe a mensagem preparada pelo servidor.

Para descobrir e consultar essas capacidades pelo terminal, execute na raiz:

```powershell
$clienteJar = 'mcp-client/target/mcp-client-1.0-SNAPSHOT.jar'
$servidorJar = 'mcp-server/target/mcp-server-1.0-SNAPSHOT.jar'

java -jar $clienteJar $servidorJar --recursos
java -jar $clienteJar $servidorJar --recurso catalogo://consoles
java -jar $clienteJar $servidorJar --prompts
java -jar $clienteJar $servidorJar --prompt orientar_comparacao primeiro=ps5-pro segundo=xbox-series-x
```

Cada comando inicia uma sessão, faz a consulta e encerra. Para nomes com espaços,
coloque o argumento inteiro entre aspas: `"primeiro=PlayStation 2"`.
Argumentos de prompt devem seguir `nome=valor`, sem nomes repetidos.

Uma URI inexistente produz erro MCP. No prompt, argumentos inválidos ou consoles
inexistentes também produzem erro MCP — comportamento diferente da resposta
normal de console ausente na ferramenta `comparar_consoles`.
Veja os [contratos e exemplos completos](docs/recursos-e-prompts.md).

## Build e testes

Execute o Maven na raiz para incluir os dois módulos:

| Comando | Resultado |
| --- | --- |
| `mvn clean test` | Compila e executa os testes unitários; não gera os JARs nem executa a integração |
| `mvn clean package` | Executa os testes unitários e gera os dois JARs executáveis |
| `mvn clean verify` | Executa os testes unitários, gera os JARs e executa os testes de integração |
| `mvn -pl mcp-server -am package` | Compila, testa e empacota apenas o servidor e os projetos necessários |
| `mvn -pl mcp-client -am verify` | Inclui o servidor necessário e executa a validação do cliente, com integração |

O servidor é uma dependência de teste do cliente para garantir a ordem do build;
ele não é incorporado ao JAR do cliente. Para integração, use `verify` na raiz ou
o comando com `-am` acima, garantindo que o JAR do servidor seja gerado antes.

### Testes unitários

Executados pelo Maven Surefire. Verificam o modelo, o repositório, filtros,
buscas, comparações, estatísticas, ferramentas, resource, prompt, comandos de
terminal, menu e tratamento de falhas. Incluem casos de sucesso, entradas
inválidas, ausência de resultados e proteção dos dados contra alterações.
O menu é testado com respostas simuladas, sem iniciar um processo servidor.

Relatórios:

- `mcp-server/target/surefire-reports/`
- `mcp-client/target/surefire-reports/`

### Testes de integração

Executados pelo Maven Failsafe na classe `McpClientIT`. A suíte inicia o JAR real
do servidor por STDIO, inicializa a sessão MCP e verifica descoberta e execução
das ferramentas, leitura do resource, obtenção do prompt e respostas inválidas.
Também exercita o menu com os esquemas reais, a continuidade após erros e o
encerramento do processo filho ao fechar a sessão.

Não é necessário abrir previamente servidor, Inspector ou Claude Desktop.
A inicialização e cada requisição do cliente têm limite de 15 segundos.
Os relatórios ficam em `mcp-client/target/failsafe-reports/`.

As pastas `target` são geradas pelo Maven e ignoradas pelo Git.

## Abrir no Eclipse

Use uma instalação do Eclipse com suporte a Java 21 e integração Maven (m2e).

1. Em **Window > Preferences > Java > Installed JREs**, adicione o JDK 21,
   se necessário, e selecione-o como padrão. Veja a
   [documentação do Eclipse sobre JREs](https://help.eclipse.org/latest/topic/org.eclipse.jdt.doc.user/tasks/tasks-JREs.htm).
2. Escolha **File > Import > Maven > Existing Maven Projects**.
3. Em **Root Directory**, selecione a pasta que contém o `pom.xml` principal.
4. Mantenha selecionados o projeto principal e os módulos `mcp-server` e
   `mcp-client`, e conclua com **Finish**.
5. Aguarde o download das dependências e a compilação. Se necessário, selecione
   os projetos e use **Maven > Update Project**.
6. Para a validação completa, abra o PowerShell na raiz e execute
   `mvn clean verify`. Também é possível executar **Run As > Maven build...**
   no projeto principal, com **Goals** igual a `clean verify` e JDK 21 na execução.

O Maven do terminal e o Eclipse podem utilizar instalações diferentes do Java;
confira ambos se houver erro de versão. Após alterações ou builds feitos fora
do Eclipse, atualize os projetos com **Refresh (F5)**.

## Testar com o MCP Inspector

O Inspector permite descobrir e executar as capacidades MCP por uma interface
visual. Requer Node.js e npm compatíveis com a versão utilizada. O roteiro
registrado neste projeto utiliza **Inspector 2.13.0** com **Node.js 24.14.0**.
Consulte também o [repositório oficial do Inspector](https://github.com/modelcontextprotocol/inspector).

Depois de gerar os JARs, execute na raiz:

```powershell
npx --yes @modelcontextprotocol/inspector@2.13.0
```

Abra no navegador a URL informada pelo Inspector, incluindo o token de acesso
quando fornecido. Configure a conexão:

| Campo | Valor |
| --- | --- |
| Transport Type | `STDIO` |
| Command | `java` |
| Arguments | `-jar` seguido do caminho absoluto do JAR do servidor |

Para descobrir o caminho do JAR na sua máquina:

```powershell
(Resolve-Path mcp-server/target/mcp-server-1.0-SNAPSHOT.jar).Path
```

Se o caminho contiver espaços, mantenha-o como um único argumento, entre aspas.
O Inspector deve iniciar o **servidor**, não o JAR do cliente Java.

Após conectar, liste as ferramentas e execute os exemplos deste README.
Nas áreas de resources e prompts, leia `catalogo://consoles` e obtenha
`orientar_comparacao` com dois consoles válidos.
O [roteiro de validação do Inspector](docs/validacao-inspector.md) contém
comandos CLI e resultados registrados. A validação manual de resource e prompt
está descrita no [guia dessas capacidades](docs/recursos-e-prompts.md).

## Usar com o Claude Desktop

O Claude Desktop inicia o mesmo JAR do servidor por STDIO e pode usar suas
ferramentas durante uma conversa. Essa integração requer o aplicativo instalado
e uma conta conectada; não é preciso fornecer uma chave de API ao projeto Java.

1. Gere os JARs com `mvn clean verify`.
2. Identifique os caminhos absolutos do Java e do JAR do servidor:

   ```powershell
   (Get-Command java).Source
   (Resolve-Path mcp-server/target/mcp-server-1.0-SNAPSHOT.jar).Path
   ```

3. Abra o arquivo de configuração pela opção de editar configuração do Claude.
   Use o [exemplo JSON](docs/claude-desktop-config.example.json), substituindo os
   caminhos do Java e do JAR pelos da sua máquina. Preserve outras configurações
   e servidores existentes. Em caminhos Windows dentro de JSON, use `\\`.
4. Encerre completamente o Claude, inclusive pelo ícone da bandeja do Windows,
   e abra-o novamente para carregar a configuração.
5. Confira se `catalogo-consoles` está em execução e peça, por exemplo:
   **“Use a ferramenta listar_consoles de catalogo-consoles para listar os consoles da Sega.”**
   Confira a chamada da ferramenta na conversa; apenas uma resposta sobre consoles
   não comprova que o catálogo foi consultado.

O caminho do arquivo de configuração pode variar entre instalações, especialmente
na versão Microsoft Store. O [guia do Claude Desktop](docs/validacao-claude-desktop.md)
explica essa diferença e contém o roteiro das quatro ferramentas.

As quatro ferramentas foram validadas no Claude Desktop. A seleção de resources
e prompts pela interface dessa instalação permanece sem confirmação; essas
capacidades foram verificadas pelo cliente Java, pelos testes de integração e
pela conferência manual relatada no Inspector.

### Como interpretar os registros de validação

Os documentos e arquivos de evidência em `docs` registram execuções realizadas
em 12 e 13/11/2026. Eles preservam as saídas da versão testada naquele momento.
Após esses registros, a apresentação foi revisada: a busca passou a exibir
geração e a comparação com console inexistente passou a ser uma resposta normal.
Por isso, registros antigos podem mostrar `isError: true` para esse último caso.
Os contratos acima descrevem o comportamento atual; os registros históricos não
substituem uma nova execução da versão que será avaliada.

## Problemas comuns

| Situação | O que conferir |
| --- | --- |
| `java` ou `mvn` não é reconhecido | Instalação e `PATH`; abra um novo terminal depois de alterar as variáveis |
| Maven informa que a versão 21 não é suportada | Execute `mvn -version` e ajuste `JAVA_HOME` para o JDK 21 |
| Dependências não são baixadas | Acesso à internet e configuração de proxy/repositório do Maven, se aplicável |
| JAR não encontrado | Execute `mvn clean verify` na raiz e confira o caminho; `mvn test` não gera JARs |
| `clean` ou empacotamento falha porque o JAR está em uso | Desconecte o Inspector e encerre completamente Claude ou outros clientes que utilizem o servidor; repita o build |
| Servidor parece ficar aguardando sem mostrar menu | Isso é esperado ao executar somente o servidor: ele aguarda mensagens MCP; para o menu, execute o cliente |
| Cliente informa falha de comunicação ou tempo limite | Confira o JAR informado e execute novamente o cliente; a sessão anterior é encerrada |
| Avisos de ausência de provedor SLF4J | Indicam falta de um provedor de logs; isoladamente, não impedem as consultas MCP |
| Acentos aparecem incorretos no terminal | Confira a configuração UTF-8 do terminal; o código-fonte e o build utilizam UTF-8 |

No transporte STDIO, a saída padrão do servidor é reservada ao protocolo MCP.
Mensagens de diagnóstico devem utilizar a saída de erro (`System.err`).

## Roteiro de avaliação

Para conferir uma cópia do projeto desde o início:

1. Obtenha o código em uma nova pasta e confira Java 21 e Maven.
2. Execute `mvn clean verify` e confirme **BUILD SUCCESS**.
3. Importe o projeto Maven no Eclipse e confira os dois módulos.
4. Abra o cliente Java; teste as quatro ferramentas, incluindo filtro vazio,
   busca sem resultado e comparação com console inexistente.
5. Execute os comandos de resource e prompt e confira o JSON e a mensagem retornada.
6. Conecte o servidor no Inspector e teste ferramentas, resource e prompt.
7. Configure o Claude Desktop e confirme chamadas reais das quatro ferramentas.

Esse roteiro pode ser repetido pelo avaliador. Ele não indica que uma validação
em cópia limpa já foi realizada. Os guias em `docs` fornecem os detalhes e as
evidências das verificações anteriores.
