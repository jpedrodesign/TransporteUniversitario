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
