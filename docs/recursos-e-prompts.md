# Resources e prompts do catálogo

O servidor mantém as quatro ferramentas existentes e oferece duas capacidades
adicionais: leitura de contexto por resource e um modelo de instruções por prompt.
Elas usam métodos MCP próprios, não acrescentam ferramentas ao menu dinâmico.

## Resource: catalogo://consoles

- Nome: `catalogo-consoles`.
- URI: `catalogo://consoles`.
- MIME type: `application/json`.
- Acesso: somente leitura; não há assinatura de atualizações.
- Retorno: lista JSON de todos os consoles, calculada a partir do mesmo serviço
  das ferramentas. Cada item contém `id`, `nome`, `fabricante`, `anoLancamento`
  e `geracao`. O ano é numérico. No catálogo atual são 40 itens; catálogo vazio
  produz `[]`.
- URI inexistente: erro do protocolo MCP, sem interromper consultas posteriores.

O resource permite que um assistente receba os dados como contexto estruturado.
Não altera o catálogo nem exige banco de dados, API externa ou credenciais.

## Prompt: orientar_comparacao

Modelo de mensagem para orientar uma IA a comparar dois consoles usando os
dados cadastrados. Possui dois argumentos textuais obrigatórios:

| Argumento | Conteúdo |
| --- | --- |
| `primeiro` | ID ou nome completo do primeiro console |
| `segundo` | ID ou nome completo do segundo console |

A consulta ignora maiúsculas/minúsculas e espaços externos. A resposta MCP é
uma mensagem de papel `user`, contendo os cinco campos dos dois consoles e
instruções para apresentar uma tabela e explicar diferenças verificáveis.
O texto orienta a não inventar preço, desempenho, jogos ou especificações
ausentes e a não concluir qual é melhor para um perfil com esses dados limitados.

Argumentos ausentes, inválidos ou consoles inexistentes produzem erro MCP.
O prompt é um texto pronto para um assistente usar: o servidor Java não chama
um modelo de IA, e o cliente Java apenas exibe a mensagem, sem gerar uma análise.

## Usar pelo cliente Java

Na raiz, gere os artefatos com `mvn clean verify`. Depois:

```powershell
$clienteJar = 'mcp-client/target/mcp-client-1.0-SNAPSHOT.jar'
$servidorJar = 'mcp-server/target/mcp-server-1.0-SNAPSHOT.jar'

java -jar $clienteJar $servidorJar --recursos
java -jar $clienteJar $servidorJar --recurso catalogo://consoles
java -jar $clienteJar $servidorJar --prompts
java -jar $clienteJar $servidorJar --prompt orientar_comparacao primeiro=ps5-pro segundo=xbox-series-x
```

Os comandos de listagem percorrem todas as páginas retornadas pelo servidor.
`--prompts` também mostra a descrição e os argumentos de cada prompt.
Para nomes com espaços, coloque o argumento completo entre aspas, por exemplo
`"primeiro=PlayStation 2"`. Argumentos repetidos ou fora do formato `nome=valor`
são rejeitados antes da conexão. Falhas encerram o cliente com código 1.

O comando sem essas opções continua abrindo o menu das ferramentas. O modo
de busca por termo, como `java -jar $clienteJar $servidorJar Atari`, continua disponível.

## Testes e validação externa

Os testes unitários verificam JSON, catálogo vazio, conteúdo do prompt e erros
de argumentos ou identificadores. Os testes de integração verificam descoberta,
leitura, obtenção do prompt, erros MCP e atendimento após os erros. Também
executam os quatro comandos de contexto na sessão real do cliente.

Para conferir no MCP Inspector, conecte o novo JAR, liste os resources e leia
`catalogo://consoles`. Na área de prompts, selecione `orientar_comparacao`,
preencha `primeiro` e `segundo` e obtenha a mensagem. Reinicie o Claude Desktop
para carregar as novas capacidades; a forma de selecionar resources/prompts
depende do suporte oferecido pela versão do assistente.

Em 13/11/2026, o autor confirmou que os testes de resources e prompts no
Inspector funcionaram conforme o roteiro. Essa confirmação é uma validação
manual relatada pelo autor, distinta dos testes automatizados executados por
Maven e pelos comandos do cliente.

A transcrição adicional dos testes no Claude demonstra as quatro ferramentas,
incluindo erro de comparação seguido de nova chamada válida. A contagem foi
obtida pela ferramenta de estatísticas: ela não comprova leitura do resource.
Também não há obtenção do prompt MCP nessa transcrição. O acesso a resources e
prompts pela interface desta versão do Claude permanece sem confirmação e não
deve ser apresentado como validado. O funcionamento no servidor é demonstrado
pelo cliente Java, pela suíte de integração e pela conferência relatada no Inspector.

Referência: [resources e prompts no SDK Java oficial](https://java.sdk.modelcontextprotocol.io/v2.1.0/server/).
