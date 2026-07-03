# Planejamento de Rotas para Transporte Universitario

Projeto em Java Swing para a disciplina de Estrutura de Dados, usando grafos
para simular o planejamento de rotas de onibus escolar/universitario em Cruz
das Almas.

## Problema

Onibus precisam buscar estudantes em diferentes bairros e pontos de embarque,
reduzindo distancia, tempo de percurso, consumo de combustivel e atrasos.

## Modelagem com grafos

- Vertices: UFRB, bairros e pontos de embarque.
- Arestas: ruas e ligacoes urbanas.
- Pesos: distancia aproximada em quilometros.
- Demanda: quantidade de alunos em cada ponto.

## Algoritmos demonstrados

- Dijkstra: menor caminho a partir da UFRB.
- Prim: arvore geradora minima para conectar todos os pontos com menor custo.
- TSP simplificado: sequencia aproximada de paradas usando estrategia gulosa.
- Guloso por demanda: priorizacao dos pontos com mais alunos.

## Funcionalidades

- Mapa esquematico de Cruz das Almas com pontos iniciais.
- Calculo da rota eficiente do onibus.
- Distancia total, tempo estimado e total de alunos atendidos.
- Priorizacao de pontos com maior demanda.
- Simulacao de adicao e remocao de pontos.
- Visualizacao grafica das rotas e da arvore minima.

## Pontos iniciais

- UFRB - Campus
- Centro
- Coplan
- Assembleia
- Itapicuru
- Lauro Passos
- Inocoop
- Suzana

## Execucao

Coloque o arquivo principal da biblioteca JXMapViewer2 dentro de uma pasta
`lib` na raiz do projeto. O arquivo correto normalmente se chama
`jxmapviewer2-2.8.jar`.

Importante: `jxmapviewer2-2.8-javadoc.jar` nao serve para compilar ou executar
o sistema. Ele contem apenas a documentacao da biblioteca.

```text
TransporteUniversitario/
  lib/
    jxmapviewer2-2.8.jar
```

Compile todos os arquivos Java informando o jar no classpath:

```bash
javac -encoding UTF-8 -cp ".;lib/jxmapviewer2-2.8.jar" Main.java gui/*.java model/*.java algoritmos/*.java mapa/*.java util/*.java org/apache/commons/logging/*.java
```

Execute:

```bash
java -cp ".;lib/jxmapviewer2-2.8.jar" Main
```

No Windows PowerShell, se o JDK nao estiver no PATH, use o caminho completo do
`javac.exe` e do `java.exe`.

Observacao: o JXMapViewer2 usa tiles do OpenStreetMap. Para carregar o mapa
real, o computador precisa estar conectado a internet.
