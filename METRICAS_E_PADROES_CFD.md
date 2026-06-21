# Métricas de Fluxo e Padrões de CFD — Base Científica e Implementação

Este documento registra a fundamentação teórica utilizada para revisar e corrigir o cálculo
das métricas de fluxo (Cycle Time, Lead Time, Throughput, Velocity) e dos detectores de
padrões do Diagrama de Fluxo Cumulativo (CFD) implementados em:

- `src/main/java/com/agilesync/service/TrelloIntegrationService.java`
- `src/main/java/com/agilesync/service/CfdPatternAnalyzerService.java`

O objetivo é servir de referência para a redação da fundamentação teórica e da seção de
metodologia do TCC, vinculando cada fórmula/regra de código a uma fonte da literatura.

---

## 1. Métricas de Fluxo

### 1.1 Definições adotadas

| Métrica | Definição | Fonte |
|---|---|---|
| **Throughput** | Número de itens de trabalho que saem do sistema (alcançam o estado "Pronto") em um período de tempo fixo. | Vacanti (2015); Anderson (2010) |
| **WIP (Work in Progress)** | Número de itens que entraram no fluxo, mas ainda não saíram — ou seja, estão em qualquer estado entre o início e o fim do fluxo. | Anderson (2010); Reinertsen (2009) |
| **Cycle Time** | Tempo decorrido entre o início do trabalho em um item (entrada no fluxo "ativo", excluindo fila/backlog) e sua conclusão. | Vacanti (2015) |
| **Lead Time** | Tempo decorrido entre a entrada da demanda no sistema (incluindo o tempo em fila/backlog) e sua conclusão. Lead Time ≥ Cycle Time, pois inclui o tempo de espera antes do início do trabalho. | Vacanti (2015); Reinertsen (2009) |
| **Velocity** | Quantidade média de itens entregues por sprint (média do throughput ao longo das sprints analisadas). | Vacanti (2015) |
| **Eficiência de Fluxo (Flow Efficiency)** | Percentual do tempo total no fluxo em que o item está sendo efetivamente trabalhado (tempo ativo / tempo total). | Modig & Åhlström (2012); Anderson (2010) |
| **Net Flow** | Diferença entre a taxa de chegada (itens que entram no Backlog) e a taxa de saída (Throughput) em um período. | Vacanti (2015); Reinertsen (2009) |

### 1.2 Lei de Little (Little's Law)

A relação fundamental entre WIP, Cycle Time e Throughput é dada pela Lei de Little:

```
Cycle Time médio = WIP médio / Taxa de Throughput
```

> Little, J. D. C. (1961). *A Proof for the Queuing Formula: L = λW*. Operations Research, 9(3), 383–387.

Essa lei é a base teórica usada por Vacanti (2015) para derivar Cycle Time e Lead Time a
partir de contagens de WIP e Throughput observadas em quadros Kanban — exatamente o cenário
desta aplicação (dados extraídos do Trello por sprint).

### 1.3 Problema identificado na implementação original

A implementação anterior calculava:

```java
// ANTES
cycleTime = SPRINT_DAYS / throughput;                 // = "Takt Time", não Cycle Time
leadTime  = totalWipEmProgresso / throughput;          // unidade = "sprints", não dias
```

Problemas:

1. **`cycleTime` era, na verdade, a fórmula de *Takt Time*** (tempo disponível ÷ unidades
   entregues), um indicador de *cadência de entrega*, não de tempo de permanência de um
   item no fluxo (Reinertsen, 2009).
2. **`leadTime` tinha a estrutura correta da Lei de Little** (WIP / Throughput), mas:
   - o resultado ficava em **"sprints"**, sem conversão para dias;
   - o WIP usado **excluía o Backlog**, portanto representava Cycle Time (trabalho ativo),
     não Lead Time (que deve incluir o tempo de espera na fila).
3. Consequência: `cycleTime` (em dias) e `leadTime` (em sprints) tinham **escalas
   incompatíveis**, e a comparação `leadTime > cycleTime` (usada no detector "Curva-S")
   reduzia-se algebricamente a `totalWip > 15` — um limiar arbitrário e desconectado do
   conceito que o padrão deveria representar.

### 1.4 Fórmulas corrigidas

```java
// DEPOIS
cycleTime = wipEmProgresso * SPRINT_DAYS / throughput;
leadTime  = (wipEmProgresso + wipBacklog) * SPRINT_DAYS / throughput;
```

Onde:

- `wipEmProgresso` = soma do WIP nos estágios "Desenvolvimento" e "Testes" (trabalho
  efetivamente iniciado, mas não concluído).
- `wipBacklog` = soma do WIP no estágio "Backlog" (demanda admitida no sistema, mas ainda
  não iniciada).
- `SPRINT_DAYS` = 15 (duração da sprint em dias), usado como fator de conversão de
  "sprints" para "dias", aplicando a Lei de Little (`Cycle Time = WIP / Throughput rate`,
  onde a taxa de throughput é `throughput / SPRINT_DAYS` itens/dia).

Com isso:

- `cycleTime` passa a representar o tempo médio (em dias) que um item leva **desde o
  início do trabalho ativo** até a conclusão.
- `leadTime` passa a representar o tempo médio (em dias) **desde a entrada no backlog**
  até a conclusão — sempre `>= cycleTime`, como esperado pela definição de Vacanti (2015).

### 1.5 Métricas adicionais implementadas

#### 1.5.1 Eficiência de Fluxo (Flow Efficiency)

```java
flowEfficiency = (wipEmProgresso * 100) / (wipEmProgresso + wipBacklog);
```

- **Definição**: percentual do tempo em que um item está sendo efetivamente trabalhado em
  relação ao tempo total que permanece no sistema (trabalho ativo + espera em fila).
- **Origem teórica**: o conceito de Flow Efficiency (ou "Process Efficiency") vem do Lean,
  formalizado por Modig & Åhlström (2012) como `tempo de valor agregado / tempo total de
  lead time`. No contexto Kanban, Anderson (2010) discute a mesma relação entre tempo
  ativo e tempo em fila.
- **Adaptação ao modelo de dados**: como o modelo não possui timestamps por item, a
  eficiência é aproximada pela proporção entre o WIP em progresso (Desenvolvimento +
  Testes) e o WIP total não concluído (em progresso + Backlog) na sprint — uma
  aproximação por "estoque" (snapshot) em vez de "tempo" por item, mas que preserva a
  mesma relação conceitual (quanto maior a proporção de itens parados em Backlog, menor a
  eficiência).
- **Interpretação**: valores baixos (ex.: < 25%, frequentemente observados na literatura)
  indicam que a maior parte do tempo os itens ficam esperando em fila, não sendo
  trabalhados.

#### 1.5.2 Net Flow (Fluxo Líquido)

```java
netFlow = wipBacklog - throughput; // chegadas - saídas da sprint
```

- **Definição**: diferença entre a taxa de chegada (itens que entraram no Backlog na
  sprint) e a taxa de saída (Throughput, itens concluídos na sprint).
- **Origem teórica**: Vacanti (2015) descreve que, quando a Taxa Média de Chegada
  (Average Arrival Rate) excede a Taxa Média de Saída (Average Departure Rate), o sistema
  acumula "Flow Debt" (dívida de fluxo), levando ao aumento do Cycle Time ou à redução do
  Throughput ao longo do tempo. Reinertsen (2009) descreve a mesma relação sob a ótica de
  teoria de filas.
- **Interpretação**: `netFlow > 0` indica que mais itens estão entrando no sistema do que
  saindo (acúmulo de demanda); `netFlow < 0` indica que a equipe está consumindo o
  backlog mais rápido do que ele cresce; `netFlow ≈ 0` indica um sistema em equilíbrio
  (arrival rate ≈ departure rate), condição ideal segundo Vacanti (2015).

---

## 2. Padrões de Leitura do CFD

A literatura de referência para leitura de anomalias em CFDs deriva do trabalho de
David J. Anderson (2010) sobre Kanban e é sintetizada por fontes como Nave (2023) e
BusinessMap. Os 6 padrões abaixo foram mapeados 1:1 para os detectores implementados em
`CfdPatternAnalyzerService`.

### 2.1 Linhas Planas (Flat Lines)

- **Definição canônica**: uma faixa do CFD permanece horizontal — não há "saídas"
  (departures) daquele estado durante o período.
- **Implementação**: `throughput == 0` na sprint.
- **Avaliação**: correspondência direta — throughput zero significa que a faixa "Pronto"
  não cresceu (zero departures do sistema).

### 2.2 Diferença no Gradiente (Differences in Gradient)

- **Definição canônica**: a inclinação (slope) da linha de chegadas (arrivals) difere da
  inclinação da linha de saídas (departures); se a linha de chegadas é mais inclinada, o
  WIP está aumentando.
- **Implementação (corrigida)**: compara a *variação* (tendência) do WIP total e do
  throughput entre a sprint atual e a anterior:
  ```java
  wipTrend > 0 && throughputTrend <= 0
  ```
  Ou seja, o WIP cresceu enquanto o throughput não cresceu — a taxa de chegada está
  superando a taxa de saída.
- **Antes**: comparava `throughput < totalWip * 0.8` (um instantâneo de uma única sprint,
  sem noção de tendência/inclinação). A versão corrigida passou a comparar **variações
  entre sprints consecutivas**, alinhando-se ao conceito de "diferença de inclinação"
  entre as duas curvas.

### 2.3 Curva-S (S-Curve)

- **Definição canônica**: uma única faixa do CFD apresenta o formato "plano → inclinado →
  plano", causado por períodos de WIP zero alternados com períodos de atividade — ou seja,
  variação de inclinação ao longo do tempo dentro de uma mesma curva.
- **Implementação (corrigida)**: detecta a transição de uma sprint sem entregas
  (`throughput == 0`, trecho "plano") para uma sprint com entregas (`throughput > 0`,
  trecho "inclinado"):
  ```java
  previousThroughput == 0 && currentThroughput > 0
  ```
- **Antes**: comparava `leadTime > cycleTime && totalWip > 5`, o que não tinha relação com
  o formato da curva cumulativa e, devido ao bug descrito na seção 1.3, equivalia a
  `totalWip > 15`. A versão corrigida detecta diretamente a transição plano→inclinado que
  dá origem ao "S" na curva cumulativa.

### 2.4 Espaçamentos Protuberantes (Bulging Bands)

- **Definição canônica**: a largura de uma faixa do CFD **aumenta** ao longo do tempo —
  WIP acumulando naquele estado (gargalo).
- **Implementação (corrigida)**: compara o WIP total da sprint atual com o da sprint
  anterior:
  ```java
  previousWip > 0 && currentWip > previousWip * 1.5
  ```
  Um crescimento de mais de 50% no WIP em relação à sprint anterior indica alargamento da
  faixa de trabalho em progresso.
- **Antes**: `totalWip > throughput * 2` era um instantâneo de desproporção dentro da
  mesma sprint, sem comparação histórica — não captava "crescimento" (tendência), apenas
  "desproporção" pontual.

### 2.5 Degraus da Escada (Stair Steps)

- **Definição canônica**: um período "plano" seguido por um salto vertical súbito —
  causado por entregas em lote (batching).
- **Implementação (corrigida)**:
  ```java
  currentThroughput > 0 && currentThroughput > previousThroughput * 1.5
  ```
  Restrita a **aumentos** súbitos (>50%) de throughput em relação à sprint anterior.
- **Antes**: a condição usava `||` para capturar tanto aumentos quanto **quedas** de
  throughput acima de 50%. Uma queda brusca não representa "entrega em lote" (a causa
  canônica do padrão) — representa o oposto (início de um trecho plano). A versão
  corrigida restringe a detecção a saltos de aumento, que é a assinatura de batching.

> **Observação**: por trabalhar com dados agregados por sprint (poucos pontos), os
> detectores de Curva-S e Degraus da Escada podem ocasionalmente coincidir na mesma
> sprint (uma sprint com throughput zero seguida de uma sprint com entregas dispara
> ambos). Isso é esperado e documentado: a literatura também trata esses padrões como
> não mutuamente exclusivos em CFDs reais.

### 2.6 Espaçamentos Desaparecendo (Disappearing Bands)

- **Definição canônica**: a faixa de um estado do processo desaparece do diagrama —
  causado por itens "pulando" aquele estado ou bloqueio upstream.
- **Implementação (corrigida)**:
  ```java
  desenvolvimentoVazio = todos os registros de "Desenvolvimento" na sprint têm quantityCards == 0
  prontoComItens       = existe registro de "Pronto" na sprint com quantityCards > 0
  return desenvolvimentoVazio && prontoComItens
  ```
- **Antes**: o código verificava se o conjunto de estágios da sprint (`Set<ScrumTrelloEnum>`)
  continha ou não "Desenvolvimento"/"Pronto". Como `processMetricsCfd` sempre gera um
  registro de `CfdDataDTO` para **todo** estágio mapeado em **toda** sprint (mesmo com
  contagem zero), esse conjunto é constante — a condição nunca variava de fato entre
  sprints, tornando o detector não funcional. A versão corrigida passou a comparar as
  **contagens** (`quantityCards`) em vez da mera presença do estágio no conjunto,
  tornando o detector sensível à variação real dos dados por sprint.

---

## 3. Referências Bibliográficas

- LITTLE, J. D. C. *A Proof for the Queuing Formula: L = λW*. Operations Research, v. 9,
  n. 3, p. 383–387, 1961.
- ANDERSON, D. J. *Kanban: Successful Evolutionary Change for Your Technology Business*.
  Sequim: Blue Hole Press, 2010.
- VACANTI, D. S. *Actionable Agile Metrics for Predictability: An Introduction*. Leanpub,
  2015.
- REINERTSEN, D. G. *The Principles of Product Development Flow: Second Generation Lean
  Product Development*. Redondo Beach: Celeritas Publishing, 2009.
- MODIG, N.; ÅHLSTRÖM, P. *This is Lean: Resolving the Efficiency Paradox*. Estocolmo:
  Rheologica Publishing, 2012.
- NAVE. *Reading the Signs: Kanban CFD Patterns*. Disponível em:
  https://getnave.com/blog/kanban-cfd-patterns/
- NAVE. *5 Practical Tips for Interpreting Flow Metrics in the Cumulative Flow Diagram*.
  Disponível em: https://getnave.com/blog/5-practical-tips-for-interpreting-flow-metrics-in-the-cumulative-flow-diagram/
- BUSINESSMAP. *What Is the Cumulative Flow Diagram?*. Disponível em:
  https://businessmap.io/kanban-resources/kanban-analytics/cumulative-flow-diagram
- WIKIPEDIA. *Cumulative flow diagram*. Disponível em:
  https://en.wikipedia.org/wiki/Cumulative_flow_diagram
- NAVE. *Borrowed Time: Kanban Flow Debt*. Disponível em:
  https://getnave.com/blog/kanban-flow-debt/
- KANBAN GUIDES. *The Kanban Guide*. Disponível em: https://kanbanguides.org/

---

## 4. Resumo das Alterações de Código

| Arquivo | Alteração |
|---|---|
| `TrelloIntegrationService.java` | `calculateCycleTime` e `calculateLeadTime` reescritos para usar a Lei de Little com conversão para dias via `SPRINT_DAYS`; nova função `calculateBacklogWipBySprint` para incluir o WIP do Backlog no Lead Time; novas funções `calculateFlowEfficiency` e `calculateNetFlow`. |
| `CfdPatternAnalyzerService.java` | `isGradientDifference`, `isSCurve`, `isBulgingSpacing` e `isStairSteps` passaram a comparar a sprint atual com a anterior (análise de tendência); `isDisappearingSpacing` passou a comparar contagens (`quantityCards`) em vez da presença do estágio no conjunto; textos explicativos do relatório atualizados para refletir a nova lógica. |
| `SprintCfdDataDTO.java` | Novos campos `flowEfficiency` e `netFlow` por sprint. |
| Frontend: `sprint-cfd-data.model.ts`, `metrics.model.ts`, `dashboard.component.*`, `documentation.modal.ts` | Novos campos espelhando o backend; tabela de métricas do dashboard ganhou colunas "Eficiência de Fluxo" e "Net Flow", com scroll horizontal para evitar corte de colunas; modal de documentação ganhou as definições de Eficiência de Fluxo e Net Flow. |
