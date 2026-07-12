# Sistema de Planejamento de Rotas — Cruz das Almas/BA

Trabalho Final da Disciplina Estrutura de Dados

Este projeto foi desenvolvido como Trabalho Final da disciplina Estrutura de Dados, tendo como objetivo aplicar, de forma prática, os principais conceitos estudados durante a disciplina por meio da implementação de uma aplicação desktop em Java.

O sistema utiliza a teoria dos grafos para representar a malha viária da cidade de Cruz das Almas – BA, permitindo o cálculo, visualização e simulação de rotas em um mapa real. Durante o desenvolvimento foram aplicados diversos algoritmos clássicos de grafos, estruturas de dados e técnicas de programação orientada a objetos.

Aplicação desktop desenvolvida em Java Swing para estudar grafos, calcular rotas e visualizar o deslocamento de um veículo em um mapa real de Cruz das Almas, Bahia.

O projeto usa somente o JDK e bibliotecas JAR armazenadas localmente em `lib/`. Não utiliza Maven, Gradle, Node.js ou outro gerenciador de dependências.

## Funcionalidades

- mapa do OpenStreetMap exibido com JXMapViewer;
- abertura centralizada em Cruz das Almas (`-12.6723, -39.1054`), com zoom inicial 4;
- 14 bairros e 18 escolas com coordenadas WGS84 mapeadas no OpenStreetMap;
- marcadores distintos para bairros, escolas, universidade e pontos de embarque;
- cadastro, edição e remoção de pontos;
- seleção de origem e destino;
- desenho da rota completa pelas ruas;
- zoom automático para enquadrar todo o percurso calculado;
- distância individual entre paradas e quilometragem total;
- atualização da geometria e da distância após editar coordenadas;
- animação fluida do veículo sobre a rota;
- velocidade configurável, pausa e reinício da animação;
- orientação do veículo de acordo com a direção do movimento;
- abertura e salvamento de projetos;
- exportação de resultados em CSV, TXT e PDF;
- filtros por tipo de ponto e apresentação de estatísticas.

## Algoritmos disponíveis

- Dijkstra;
- Prim;
- Kruskal;
- busca em largura (BFS);
- busca em profundidade (DFS);
- estratégia gulosa por demanda;
- Caixeiro Viajante simplificado (TSP);
- distribuição de rotas entre veículos (VRP, disponível na camada de algoritmos).

Os algoritmos definem a ordem dos pontos. Mudanças entre vértices não adjacentes são expandidas para um caminho contínuo, evitando saltos no desenho e na animação.

## Rotas e serviços geográficos

O sistema envia a sequência calculada ao serviço OSRM, que devolve:

- geometria GeoJSON pelas ruas;
- distância de cada trecho em metros;
- distância total;
- duração estimada.

As distâncias recebidas são convertidas para quilômetros. Se o OSRM estiver indisponível, a aplicação continua funcionando com geometria local e distância calculada pela fórmula de Haversine.

Os tiles do mapa e as coordenadas padrão utilizam dados do OpenStreetMap. Por isso, a visualização do mapa e o trajeto viário completo requerem internet.

## Animação

A animação e o desenho utilizam exatamente a mesma lista de coordenadas da rota. Um `javax.swing.Timer` atualiza o veículo aproximadamente a cada 16 ms.

O deslocamento é calculado pelo tempo transcorrido e pela velocidade configurada, sem depender da quantidade de frames. A posição é interpolada dentro do segmento atual, e a orientação é obtida com `atan2`. O timer usa coalescência e limita intervalos excessivos para evitar travamentos e saltos após atrasos da interface.

## Requisitos

- JDK 21 ou superior disponível no `PATH`;
- Visual Studio Code;
- extensão **Extension Pack for Java**;
- internet para OpenStreetMap e OSRM.

Verifique o Java instalado:

```powershell
java -version
javac -version
```

## Estrutura do projeto

```text
Projeto/
├── src/
│   ├── Main.java
│   ├── algoritmos/    # Algoritmos de grafos e roteamento
│   ├── gui/           # Interface Swing, mapa e animação
│   ├── model/         # Grafo, pontos, arestas, rotas e veículos
│   ├── services/      # Dados, projetos, simulação e OSRM
│   └── util/          # Distâncias, geometria e exportadores
├── dados/
│   ├── vertices.csv
│   └── edges.csv
├── lib/
│   ├── jxmapviewer2-2.8.jar
│   └── commons-logging-1.3.0.jar
├── tests/
│   ├── RouteSmokeTest.java
│   └── OsrmIntegrationTest.java
├── bin/               # Classes geradas pelo javac
├── .vscode/
└── README.md
```

Os pacotes foram mantidos para separar interface, domínio, algoritmos, serviços e utilitários sem duplicar código.

## Compilar no terminal do VS Code

Abra a pasta raiz no VS Code e execute no PowerShell:

```powershell
$sources = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object FullName
New-Item -ItemType Directory -Force -Path bin | Out-Null
javac --release 21 -encoding UTF-8 -cp "lib/*" -d bin $sources
```

Também é possível pressionar `Ctrl+Shift+B` e selecionar **Compilar Java para bin**.

## Executar

Windows/PowerShell:

```powershell
java -cp "bin;lib/*" Main
```

Linux ou macOS:

```bash
java -cp "bin:lib/*" Main
```

No VS Code, abra `src/Main.java` e pressione `F5` ou use **Run Java**.

## Executar os testes

Depois de compilar a aplicação:

```powershell
$tests = Get-ChildItem -Path tests -Filter *.java | ForEach-Object FullName
javac --release 21 -encoding UTF-8 -cp "bin;lib/*" -d bin $tests
java -ea -cp "bin;lib/*" RouteSmokeTest
java -ea -cp "bin;lib/*" OsrmIntegrationTest
```

- `RouteSmokeTest` verifica os dados padrão, coordenadas, conectividade, algoritmos e continuidade das rotas.
- `OsrmIntegrationTest` valida a resposta viária do OSRM e necessita de internet.

## Uso básico

1. Inicie a aplicação.
2. Escolha um algoritmo na barra de ferramentas.
3. Se solicitado, selecione origem e destino.
4. Aguarde o cálculo e o carregamento da geometria viária.
5. Consulte as distâncias no mapa e no painel inferior.
6. Ajuste a velocidade em km/h.
7. Use **Iniciar veículo**, **Pausar** ou **Parar**.

## Legenda dos marcadores

- `E`: escola;
- `U`: universidade;
- `B`: bairro;
- `P`: ponto de embarque;
- `S`: início da rota;
- `D`: destino da rota.

## Solução de problemas no VS Code

### Biblioteca indicada como ausente

Confirme que estes arquivos existem:

```text
lib/jxmapviewer2-2.8.jar
lib/commons-logging-1.3.0.jar
```

Depois execute:

1. `Ctrl+Shift+P`;
2. **Java: Clean Java Language Server Workspace**;
3. **Restart and delete**;
4. aguarde a reimportação do projeto.

### O mapa não aparece

Verifique a conexão com a internet. Os algoritmos e cálculos locais continuam disponíveis, mas os tiles do OpenStreetMap não podem ser carregados offline.

### O percurso aparece como aproximação local

O OSRM não respondeu dentro do limite configurado. Verifique a internet e calcule a rota novamente.

### Caracteres acentuados incorretos

Compile sempre com `-encoding UTF-8`, conforme os comandos deste README.

## Dependências locais

| Biblioteca | Finalidade |
|---|---|
| `jxmapviewer2-2.8.jar` | Exibição e navegação do mapa |
| `commons-logging-1.3.0.jar` | Dependência de logging utilizada pelo visualizador |

Todas as dependências ficam em `lib/`; nenhuma é baixada durante a compilação.
