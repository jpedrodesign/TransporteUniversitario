# Planejamento de Rotas para Transporte Universitário

Sistema desenvolvido em Java Swing para a disciplina de Estrutura de Dados, utilizando Grafos e algoritmos clássicos de otimização para simular o planejamento inteligente de rotas de transporte universitário na cidade de Cruz das Almas - BA.

O projeto tem como objetivo demonstrar, de forma visual e prática, a aplicação de estruturas de dados em problemas reais de mobilidade urbana e logística de transporte estudantil.

## Problema

Desenvolver um sistema capaz de calcular rotas eficientes para ônibus universitários, reduzindo:

-distância percorrida;
-tempo de viagem;
-consumo de combustível;
-atrasos no transporte dos estudantes.

O sistema considera diferentes bairros e pontos de embarque da cidade, priorizando locais com maior quantidade de alunos.

## Modelagem com grafos

O sistema utiliza a estrutura de Grafos para representar o mapa urbano de Cruz das Almas.

###Vértices
Representam: 
-UFRB;
-bairros;
-pontos de embarque;
-áreas de coleta de estudantes.

###Arestas
Representam:
-ruas;
-conexões urbanas;
-trajetos entre bairros.

###Pesos
Cada aresta possui pesos relacionados a:
-distância aproximada em quilômetros;
-custo de deslocamento;
-tempo estimado de percurso.

###Demanda
Cada ponto possui:
-quantidade de alunos atendidos;
-prioridade de coleta..

## Algoritmos demonstrados

###Dijkstra
Calcula o menor caminho entre os pontos do grafo, encontrando rotas mais eficientes para o ônibus universitário.

###Prim
Gera uma Árvore Geradora Mínima (MST), conectando todos os pontos com o menor custo total possível.

###TSP Simplificado
Utiliza uma estratégia gulosa inspirada no problema do Caixeiro Viajante para definir uma sequência eficiente de paradas.

###Estratégia Gulosa por Demanda
Prioriza bairros e pontos com maior quantidade de estudantes, melhorando a eficiência do transporte.

## Funcionalidades

-Cadastro de pontos de embarque;
-Cadastro de conexões entre bairros;
-Visualização gráfica do mapa de Cruz das Almas;
-Cálculo automático de rotas eficientes;
-Priorização de pontos com maior demanda;
-Exibição da distância total percorrida;
-Estimativa de tempo de viagem;
-Simulação de adição e remoção de pontos;
-Recalculo automático das rotas;
-Visualização gráfica das conexões do grafo;
-Exibição da Árvore Geradora Mínima;
-Interface gráfica desenvolvida em Java Swing
