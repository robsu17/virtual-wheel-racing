# Arquitetura do Volapex

Versão 1 · 7 de novembro de 2026 · antes do primeiro código

Este documento define a estrutura do código do app Android e do programa do Windows, seguindo arquitetura limpa e os princípios SOLID. Ele parte das decisões já tomadas (Kotlin nos dois lados, só Android, vJoy 2.1.9.1, configuração universal, código fechado) e do estudo em `reports/Volante virtual com celular.md`. Nada aqui foi implementado ou medido; os números de desempenho são metas a validar no protótipo.

## 1. O que a arquitetura precisa garantir

| Exigência | De onde vem | Consequência na estrutura |
|---|---|---|
| Atraso baixo e sem picos | RNF-01, RNF-02, RNF-05, RNF-06 | O caminho sensor → pacote → vJoy roda em thread própria, sem passar pela UI e sem criar objetos a cada amostra |
| Driver trocável | RNF-07; o JD SimWheel morreu preso a uma versão do vJoy | O vJoy fica atrás de uma interface; nenhuma outra parte do código sabe que ele existe |
| Segurança por padrão | RNF-10, RNF-16 | Pacote autenticado e failsafe vivem no domínio e no protocolo, testáveis sem rede |
| Um formato de pacote só | Kotlin nos dois lados | Protocolo e regras ficam em módulos compartilhados, usados pelo celular e pelo PC |
| Configuração universal | Decisão de 7/nov | Um volante genérico: sem perfis por jogo no domínio |
| Evolução sem reescrever | Telemetria, hápticos e outro driver são "Posterior" | Recursos novos entram como adaptadores novos, sem mexer no núcleo |

## 2. Visão geral

```
        CELULAR (Android)                                     PC (Windows)
┌──────────────────────────────┐                ┌──────────────────────────────────┐
│ android:app   (Compose, VMs) │                │ desktop:app  (Compose Desktop)   │
│        │ comandos / estado   │                │        │ comandos / estado       │
│        ▼  amostrado a 15 Hz  │                │        ▼  amostrado a 15 Hz      │
│ android:engine               │   UDP 32 B     │ desktop:receiver                 │
│  sensor → pipeline → pacote ─┼───────────────►│  pacote → validação → watchdog   │
│  toque  → pedais/botões      │  até 200 Hz    │        │                         │
└──────────────┬───────────────┘                │        ▼                         │
               │                                │ desktop:vjoy  (JNA → vJoyInterface.dll)
               │                                └──────────────┬───────────────────┘
               │      ambos dependem de                        │
               ▼                                               ▼
        ┌─────────────────────────────────────────────────────────┐
        │ shared:domain    regras puras (ângulo, filtro, curva,   │
        │                  zona morta, failsafe, estado do volante)│
        │ shared:protocol  formato do pacote, HMAC, pareamento     │
        └─────────────────────────────────────────────────────────┘
```

A regra de dependência é sempre de fora para dentro: telas e adaptadores dependem do domínio, nunca o contrário. `shared:domain` não conhece Android, rede, vJoy nem Compose.

## 3. Módulos

Um único repositório Gradle com vários módulos:

```
volapex/
├─ build-logic/          convenções de build (versão do Kotlin, alvo da JVM, lint)
├─ shared/
│  ├─ domain/            Kotlin/JVM puro — entidades, regras e portas
│  └─ protocol/          Kotlin/JVM puro — codec, autenticação, pareamento
├─ android/
│  ├─ app/               Compose, ViewModels, injeção de dependências
│  └─ engine/            motor de tempo real: sensor, toque, envio UDP
├─ desktop/
│  ├─ app/               Compose for Desktop, bandeja, injeção de dependências
│  ├─ receiver/          recepção UDP, sessão, watchdog, estatísticas
│  └─ vjoy/              adaptador do vJoy via JNA
├─ spikes/               experimentos descartáveis (vjoy-teste.ps1)
└─ docs/
```

| Módulo | Depende de | Não pode depender de |
|---|---|---|
| `shared:domain` | nada | qualquer outro módulo |
| `shared:protocol` | `shared:domain` | Android, rede, UI |
| `android:engine` | `shared:*`, SDK do Android | Compose, `android:app` |
| `android:app` | `android:engine`, `shared:*` | `desktop:*` |
| `desktop:receiver` | `shared:*` | `desktop:vjoy`, Compose |
| `desktop:vjoy` | `shared:domain` | `desktop:receiver`, Compose |
| `desktop:app` | todos os `desktop:*`, `shared:*` | `android:*` |

**Por que módulos Kotlin/JVM e não Kotlin Multiplatform:** o Android e o Windows rodam ambos sobre a JVM, então uma biblioteca Kotlin/JVM comum funciona nos dois sem a complexidade do KMP. `javax.crypto` (HMAC) e `java.nio` existem nos dois lados. O KMP só passaria a valer a pena com iOS, que está fora do escopo.

**Restrição de versão:** todos os módulos geram bytecode Java 17, para o Android aceitar e para rodar no JDK 17 já instalado. Por isso `desktop:vjoy` chama a `vJoyInterface.dll` pela biblioteca JNA; trocar para a API nativa do Java 22+ (FFM) é uma mudança interna desse módulo, sem efeito no resto.

## 4. Camadas dentro de cada lado

```
  Telas (Compose) ──► ViewModels ──► Casos de uso ──► Domínio
                                          │              ▲
                                          ▼              │ implementam portas
                                    Portas (interfaces) ◄┘
                                          ▲
                        Adaptadores: sensor, UDP, vJoy, armazenamento
```

- **Domínio:** entidades e regras puras, sem I/O. Exemplos: `WheelState`, `SteeringMath`, `Deadzone`, `ResponseCurve`, `OneEuroFilter`, `FailsafePolicy`.
- **Casos de uso:** orquestram o domínio por meio de portas. Exemplos: `DrivingSession`, `Recenter`, `PairWithPc`, `ReceiveSession`.
- **Portas:** interfaces declaradas por quem as usa (o caso de uso), e não por quem as implementa.
- **Adaptadores:** implementam as portas com Android, `DatagramSocket`, vJoy, DataStore etc.
- **Raiz de composição:** um único lugar por app (`AppContainer`) cria os objetos e liga portas a adaptadores.

## 5. Os princípios SOLID aplicados

### S — Responsabilidade única

Cada classe tem um só motivo para mudar. O envio de um pacote, por exemplo, é dividido assim:

| Classe | Única responsabilidade | Muda quando… |
|---|---|---|
| `PacketCodec` | Escrever e ler os 32 bytes | o formato do pacote muda |
| `PacketAuthenticator` | Calcular e verificar o HMAC | a criptografia muda |
| `SequenceGuard` | Rejeitar pacote velho ou repetido | a regra de ordem muda |
| `UdpWheelTransmitter` | Mandar bytes pela rede | o transporte muda (ex.: USB) |
| `DrivingSession` | Ligar as peças durante a direção | o fluxo de direção muda |

### O — Aberto para extensão, fechado para modificação

O processamento da direção é uma sequência de estágios plugáveis. Uma curva nova, um filtro diferente ou a predição entram como classe nova, sem editar o pipeline:

```kotlin
// shared:domain
fun interface SteeringStage {
    /** Recebe e devolve o ângulo em graus; nunca aloca. */
    fun process(degrees: Float, timestampNanos: Long): Float
}

class SteeringPipeline(private val stages: List<SteeringStage>) {
    fun process(degrees: Float, timestampNanos: Long): Float {
        var value = degrees
        for (i in stages.indices) value = stages[i].process(value, timestampNanos)
        return value
    }
}

// montado na raiz de composição, a partir dos ajustes:
SteeringPipeline(listOf(OneEuroFilter(params), Prediction(maxMs = 20), Deadzone(0.8f), ResponseCurve.Linear))
```

O mesmo vale para recursos futuros: telemetria entra como uma porta `TelemetrySource` com um adaptador por jogo, sem tocar no envio nem no vJoy.

### L — Substituição de Liskov

Qualquer implementação de `VirtualWheel` deve cumprir o mesmo contrato, para o receptor funcionar igual com o vJoy, com um falso de teste ou com um driver futuro (HIDMaestro, por exemplo). O contrato é verificado por uma bateria de testes abstrata, rodada contra cada implementação:

- depois de `neutral()`, pedais em 0 e direção no centro;
- `apply()` com o mesmo estado duas vezes não muda nada;
- `close()` sempre libera o dispositivo, mesmo após erro;
- valores fora da faixa são limitados, nunca lançam exceção no laço quente.

### I — Segregação de interfaces

Nada de interface "faz-tudo" para o driver. Cada cliente vê só o que usa:

```kotlin
// shared:domain — usada pelo laço de recepção
interface VirtualWheel : AutoCloseable {
    fun open(): OpenResult
    fun apply(state: WheelState)
    fun neutral()
}

// usada só pela tela de verificação do PC
interface DriverHealth {
    fun check(): DriverStatus   // instalado? versão? dispositivo habilitado? eixos X, Y, Z?
}

// futura, usada só se um jogo mandar force feedback
interface ForceFeedbackSource {
    fun onEffect(listener: (FfbEffect) -> Unit)
}
```

O `VJoyAdapter` pode implementar `VirtualWheel` e `DriverHealth` ao mesmo tempo, mas o receptor só enxerga `VirtualWheel`.

### D — Inversão de dependência

Os casos de uso dependem de portas, nunca de classes concretas:

```kotlin
// android:engine
class DrivingSession(
    private val orientation: OrientationSource,   // porta
    private val controls: ControlInput,           // porta
    private val pipeline: SteeringPipeline,       // domínio
    private val sink: WheelStateSink,             // porta
    private val clock: MonotonicClock,            // porta
)
```

Em produção, `orientation` é o `RotationVectorSource` do Android; nos testes, é uma fonte que reproduz uma gravação de sensor. A injeção é feita à mão, por construtor, em um `AppContainer` por app. Hilt ou Koin só entram se a quantidade de objetos crescer a ponto de a injeção manual atrapalhar.

## 6. Portas e adaptadores

### Celular

| Porta | Para que serve | Adaptador de produção | Adaptador de teste |
|---|---|---|---|
| `OrientationSource` | Entregar a orientação (quaternion + timestamp) | `RotationVectorSource` (`TYPE_GAME_ROTATION_VECTOR`, 200 Hz) | `RecordedOrientationSource` |
| `ControlInput` | Ler pedais e botões | `TouchControls` (alimentado pela tela, por pointer ID) | `ScriptedControls` |
| `WheelStateSink` | Enviar o estado | `UdpWheelTransmitter` | `CapturingSink` |
| `SettingsRepository` | Guardar ajustes globais | DataStore | em memória |
| `PairingRepository` | Guardar o PC pareado e a chave | DataStore criptografado | em memória |
| `MonotonicClock` | Tempo para filtro e latência | `SystemClock.elapsedRealtimeNanos` | relógio manual |
| `LinkQualityMonitor` | Avisar Wi-Fi ruim | `WifiManager` + lock de baixa latência | falso |

### PC

| Porta | Para que serve | Adaptador de produção | Adaptador de teste |
|---|---|---|---|
| `PacketSource` | Receber pacotes | `UdpPacketSource` (recepção bloqueante) | `QueuePacketSource` |
| `VirtualWheel` | Aplicar o estado no volante virtual | `VJoyAdapter` (JNA) | `FakeWheel` |
| `DriverHealth` | Verificar driver e eixos | `VJoyAdapter` | `FakeDriverHealth` |
| `PairingIssuer` | Gerar QR, PIN e chave | `SecureRandomPairingIssuer` | determinístico |
| `FirewallStatus` | Conferir a regra de entrada | `NetshFirewallStatus` | falso |
| `MonotonicClock` | Tempo do watchdog | `System.nanoTime` | relógio manual |

## 7. O caminho de tempo real

Arquitetura limpa costuma criar objetos à vontade; aqui isso causaria pausas de coleta de lixo justamente no caminho que não pode ter picos. Por isso, o caminho quente segue regras próprias, sem quebrar as camadas:

1. **Sem alocação por amostra.** `WheelState` é mutável e reaproveitado; o pacote é escrito em um `ByteArray(32)` fixo; o `Mac` do HMAC é criado uma vez e usa `doFinal(buffer, offset)`.
2. **Assinaturas com tipos primitivos.** As portas do laço recebem `Float`, `Long` e `Int`, não objetos novos, como na `SteeringStage` acima.
3. **Sem coroutines nem `Flow` no laço.** Eles ficam para a UI e o controle. O laço roda em uma thread dedicada: `HandlerThread` do sensor no celular, thread de recepção bloqueante no PC.
4. **A UI só observa.** O motor publica um resumo (`EngineSnapshot`) em um `StateFlow` a cerca de 15 Hz; a tela nunca fica no caminho entre o sensor e o pacote.
5. **Envio a cada amostra.** Até 200 Hz, recuando para 100 Hz se a perda de pacotes crescer.

```
CELULAR  ── thread do sensor (HandlerThread) ──────────────────────────────────
  onSensorChanged(q, t) → SteeringMath.angle(q) → pipeline.process()
                        → WheelState.update(direção, pedais, botões)
                        → PacketCodec.write() → HMAC → DatagramSocket.send()

PC       ── thread de recepção ────────────────────────────────────────────────
  socket.receive(pacoteReaproveitado) → HMAC ok? → seq novo? → PacketCodec.read()
                        → WheelState latest → VirtualWheel.apply()
         ── thread do watchdog (a cada 10 ms) ──────────────────────────────────
  FailsafePolicy.evaluate(silêncio) → manter | soltar pedais | centralizar | neutro
```

No PC, a thread de recepção pode ganhar prioridade de multimídia chamando `AvSetMmThreadCharacteristicsW("Games")` pelo FFM. É opcional e só entra se a medição mostrar necessidade.

## 8. Domínio e protocolo em detalhe

### `shared:domain`

| Elemento | Tipo | Regra |
|---|---|---|
| `WheelState` | entidade mutável | direção −1..1, acelerador e freio 0..1, botões em bits (16) |
| `SteeringMath` | função pura | ângulo pelo vetor "para cima" do quaternion, `atan2` com sinal calibrado (fórmula do relatório) |
| `TiltFallback` | estágio | inclinação alta: congela a gravidade e integra o giroscópio |
| `OneEuroFilter`, `Prediction`, `Deadzone`, `ResponseCurve` | `SteeringStage` | parâmetros iniciais do relatório |
| `AxisMapper` | função pura | graus do celular → eixo normalizado, conforme o giro configurado |
| `FailsafePolicy` | função pura | tabela de silêncio: < 100 ms mantém; > 100 ms solta pedais; > 500 ms centraliza; > 3 s desconectado |
| `AxisAssignment` | enum | "mexer só um eixo" (direção, acelerador ou freio) para configurar o jogo |
| `GlobalSettings` | valor imutável | giro do celular, zona morta, curva, lado do freio, curso do pedal, nomes dos botões |

Não existe `GameProfile`: a configuração é universal.

### `shared:protocol`

| Elemento | Responsabilidade |
|---|---|
| `PacketLayout` | constantes de offset do pacote de 32 bytes (magic, versão, tipo, flags, sessão, seq, tempo, eixos, botões, HMAC) |
| `PacketCodec` | escrever e ler o layout em `ByteArray` |
| `PacketType` | `HELLO`, `WELCOME`, `STATE`, `BYE`, `PING` |
| `PacketAuthenticator` | HMAC-SHA256 truncado em 8 bytes, com chave de sessão |
| `SessionKeyDerivation` | chave de sessão a partir da chave de pareamento e de dois nonces (HKDF) |
| `SequenceGuard` | descarta `seq` antigo ou repetido |
| `PairingPayload` | conteúdo do QR: endereços, porta, chave de pareamento, impressão digital; versão de formato |

Os testes do protocolo usam pacotes "de ouro": bytes fixos gravados em arquivo, que o celular precisa produzir e o PC precisa aceitar. Se alguém mudar o formato sem querer, os dois lados quebram no teste, e não na estrada.

## 9. Interface (Compose nos dois lados)

Cada tela segue fluxo de dados em uma direção só, parecido com React:

```
Tela (Composable) ──eventos──► ViewModel ──chama──► Caso de uso
       ▲                           │
       └──── UiState (StateFlow) ◄─┘
```

- **Uma tela por pasta:** `ui/inicio`, `ui/dirigir`, `ui/ajustes`, `ui/calibracao`, `ui/pareamento` no celular; `ui/painel`, `ui/pareamento` no PC.
- **`UiState` imutável** por tela, como as props de um componente; os eventos são funções passadas para baixo.
- **Exceção na tela Dirigir:** os pedais leem o toque com `pointerInput` e gravam a posição direto no `TouchControls`, que o motor lê a cada pacote. O toque não passa por recomposição; só o desenho do nível do pedal é refeito. Isso substituiu a ideia inicial de uma view nativa, porque o Compose separa de forma confiável dois dedos em dois pedais.
- **Tema** com as cores e fontes do design aprovado, definido uma vez em `ui/theme` e usado por todas as telas.

## 10. Erros e falhas

| Situação | Onde é tratada | Comportamento |
|---|---|---|
| Pacote com HMAC inválido | `ReceiveSession` | descartado e contado; nunca chega ao vJoy |
| Silêncio na rede | `FailsafePolicy` | segue a tabela do domínio |
| vJoy ausente ou ocupado | `VJoyAdapter.open()` → `OpenResult` | mensagem clara na tela de verificação, sem exceção |
| `EPERM` no envio (rede local bloqueada) | `UdpWheelTransmitter` | estado de erro com instrução para liberar a permissão |
| Sensor lento ou repetindo valores | caso de uso de diagnóstico | modo reduzido com aviso, conforme RF-06 e RF-07 |
| App vai para segundo plano | `DrivingSession` | envia `BYE`; o PC solta pedais e centraliza |

Resultados esperados usam `sealed interface` (ex.: `OpenResult.Ok`, `OpenResult.DriverMissing`, `OpenResult.Busy`); exceções ficam para defeitos de programação.

## 11. Testes

| Nível | O que cobre | Onde roda |
|---|---|---|
| Unitário | domínio e protocolo inteiros, incluindo a matemática do ângulo e a tabela de failsafe | JVM, em segundos |
| Pacotes de ouro | celular e PC concordam byte a byte | JVM |
| Contrato | toda `VirtualWheel` cumpre o mesmo comportamento | JVM (falso) e Windows com vJoy |
| Replay | gravações reais do sensor do Poco X8 Pro passam pelo pipeline com saída esperada | JVM |
| Integração | celular falso → UDP local → receptor → `FakeWheel` | JVM |
| Bancada | latência p50/p95/p99 e maior falha, no aparelho e no Wi-Fi reais | manual, com o painel do PC |

## 12. Decisões e trocas

| Decisão | Ganho | Custo | Rever quando |
|---|---|---|---|
| Kotlin/JVM em vez de KMP | build simples | nenhum, sem iOS | se o iOS voltar ao escopo |
| Injeção manual | sem mágica, fácil de entender | código de montagem à mão | se o `AppContainer` passar de ~40 objetos |
| Pipeline de estágios | filtros e curvas trocáveis | uma chamada virtual por estágio | se o perfil de CPU mostrar custo relevante |
| Estado mutável no laço quente | sem pausas de GC | menos "puro" que objetos imutáveis | nunca, enquanto latência for prioridade |
| JNA para o vJoy | roda no Java 17, sem código nativo próprio | uma biblioteca a mais | trocar por FFM se o Java embarcado passar a ser 22+ |
| Protocolo próprio | autenticado e mínimo | precisa de testes de compatibilidade | se surgir necessidade de ponte DSU |

## 13. Estado do protótipo (7 de novembro de 2026)

O protótipo de ponta a ponta segue esta arquitetura, com simplificações conscientes, a resolver antes do MVP:

| Simplificação | Por quê | O que entra no MVP |
|---|---|---|
| Pareamento por PIN de 6 dígitos (`PinPairing`) | rápido de testar | QR com chave aleatória de 128 bits e impressão digital (RF-08) |
| Sem troca `HELLO`/`WELCOME` | menos peças no primeiro teste | chave de sessão por HKDF; hoje, um pacote gravado de uma sessão antiga ainda pode ser repetido |
| Telas com estado em `remember`, sem ViewModel | só duas telas | ViewModels por tela, conforme a seção 9 |
| Sem `build-logic/` | sete módulos ainda são poucos | convenções de build quando a configuração repetida crescer |
| Sem predição nem `TiltFallback` com giroscópio | medir antes de otimizar | estágios novos no `SteeringPipeline`, se o teste no Poco X8 Pro pedir |
| Telas mínimas, sem as fontes Barlow e Inter | foco no funcionamento | telas do design aprovado |

## 14. O que ainda está em aberto

- **Medidas:** atraso real e pausas de GC só serão conhecidos no protótipo.
- **Biblioteca de UDP no Android:** `DatagramSocket` comum é o ponto de partida; alternativas só se a medição pedir.
- **Instalador do Windows:** `jpackage` gera o MSI com o Java embarcado; o encadeamento do instalador do vJoy e da regra de firewall será definido junto com o RF-14.
- **Nome dos pacotes Kotlin:** depende do nome definitivo do app; até lá, `app.volapex`.
