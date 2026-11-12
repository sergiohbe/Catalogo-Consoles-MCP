# Validação com MCP Inspector

Validação executada em 12/11/2026 com MCP Inspector **2.13.0**, Node.js
**24.14.0** e o servidor Java do catálogo por STDIO. Foram usados os JARs
gerados por `mvn clean verify`.

Os nove cenários abaixo foram executados pelo **modo CLI do próprio Inspector**.
A conferência pela interface visual foi realizada pelo autor do projeto e
confirmada nesta conversa em 12/11/2026. Os arquivos JSON anexos documentam
somente a execução CLI; não foram coletadas capturas da interface visual.
Claude Desktop é uma validação separada, ainda pendente.

## Preparar o ambiente

Na raiz do repositório, com Java 21, Maven, Node.js e npm disponíveis:

```powershell
mvn clean verify
node --version
npx --yes @modelcontextprotocol/inspector@2.13.0 --version
```

O `npx` obtém o Inspector pelo npm e pode precisar de internet na primeira
execução. Não é necessário adicioná-lo como dependência Maven ou iniciar o
servidor separadamente. A versão foi fixada para reproduzir esta validação.

## Resultados registrados

| Cenário | Esperado | Evidência |
| --- | --- | --- |
| Descoberta | Quatro ferramentas com descrições e esquemas | [Ferramentas](inspector-resultados/01-ferramentas.json) |
| Listagem sem filtro | 40 consoles | [Listagem](inspector-resultados/02-listagem.json) |
| Filtro Sega | 4 consoles | [Filtro](inspector-resultados/03-filtro-sega.json) |
| Busca por Dreamcast | Um console da Sega, de 1998 | [Busca](inspector-resultados/04-busca.json) |
| Busca sem correspondência | Mensagem de nenhum resultado | [Sem resultados](inspector-resultados/05-busca-vazia.json) |
| Comparação | PlayStation 5 Pro e Xbox Series X, com os cinco campos | [Comparação](inspector-resultados/06-comparacao.json) |
| Estatísticas | Total 40; Atari 4, Microsoft 11, Nintendo 9, Sega 4 e Sony 12 | [Estatísticas](inspector-resultados/07-estatisticas.json) |
| Console inexistente na comparação | Erro controlado, com `isError: true` | [Console inexistente](inspector-resultados/08-console-inexistente.json) |
| Segundo parâmetro ausente na comparação | Erro controlado, com `isError: true` | [Parâmetro ausente](inspector-resultados/09-parametro-ausente.json) |

O [resumo da execução](inspector-resultados/resumo.json) contém os argumentos
usados e os códigos de saída. Nos dois cenários de erro esperado, código de
saída diferente de zero indica que o Inspector reconheceu o erro MCP; o cenário
é aprovado quando o erro corresponde ao esperado. Os arquivos registram uma
execução desta versão do catálogo e não são atualizados automaticamente.

## Repetir pelo terminal

Descobrir as ferramentas:

```powershell
npx --yes @modelcontextprotocol/inspector@2.13.0 --cli java -jar mcp-server/target/mcp-server-1.0-SNAPSHOT.jar --method tools/list
```

Executar comparação:

```powershell
npx --yes @modelcontextprotocol/inspector@2.13.0 --cli java -jar mcp-server/target/mcp-server-1.0-SNAPSHOT.jar --method tools/call --tool-name comparar_consoles --tool-arg primeiro=ps5-pro --tool-arg segundo=xbox-series-x
```

Para repetir os demais cenários, use os argumentos registrados no resumo.
Cada execução CLI estabelece sua própria conexão e a encerra ao terminar.
Isso complementa os testes Maven, que verificam múltiplas chamadas na mesma
sessão e o encerramento do processo do servidor.

## Conferência na interface visual

Na raiz do projeto:

```powershell
npx --yes @modelcontextprotocol/inspector@2.13.0 java -jar mcp-server/target/mcp-server-1.0-SNAPSHOT.jar
```

Use a URL local fornecida no terminal para abrir a interface e mantenha esse
terminal aberto. Se for necessário preencher a conexão manualmente:

- Transporte: `STDIO`.
- Comando: `java`, ou o caminho completo do executável do JDK 21.
- Argumentos: `-jar` e o caminho absoluto do JAR do servidor.

Você pode obter o caminho do JAR com:

```powershell
(Resolve-Path mcp-server/target/mcp-server-1.0-SNAPSHOT.jar).Path
```

Conecte ao servidor, abra a área de ferramentas, confira os quatro esquemas e
execute os cenários da tabela usando os campos apresentados pela interface.
Registre capturas da descoberta, de uma consulta bem-sucedida e de um erro
controlado para apoiar a demonstração do trabalho. Desconecte ao terminar e
encerre o Inspector com `Ctrl+C` no terminal.

Os avisos de ausência de provedor SLF4J são enviados pela saída de erro do
servidor. Nesta validação não impediram a negociação ou a execução das ferramentas.

Referências: [Inspector oficial](https://github.com/modelcontextprotocol/inspector)
e [documentação do modo CLI](https://github.com/modelcontextprotocol/inspector/blob/main/clients/cli/README.md).
