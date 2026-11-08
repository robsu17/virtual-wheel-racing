<p align="center">
  <img src="desktop/app/icons/icon-256.png" width="120" alt="Logo do Virtual Wheel Racing: volante de corrida preto com pegadas vermelhas">
</p>

<h1 align="center">Virtual Wheel Racing</h1>

<p align="center">Transforma o celular Android em um volante para jogos de corrida e de caminhão no PC.</p>

<p align="center">
  <a href="README.md">English</a> · <strong>Português</strong> · <a href="LICENSE">Licença GPL-3.0</a> · <a href="CONTRIBUTING.md">Como contribuir</a> · <a href="docs/arquitetura.md">Arquitetura</a>
</p>

---

O celular, segurado como um volante, envia pela rede Wi-Fi a direção, os pedais e os botões. O programa do Windows recebe e repassa tudo para um volante virtual ([vJoy](https://github.com/jshafer817/vJoy)), que os jogos enxergam como um controle comum. Cada pessoa configura os controles dentro de cada jogo, como faria com um volante comprado.

> **Status:** protótipo funcional (versão 0.1.1). Validado com rFactor 1 em um Poco X8 Pro (Android 16) e Windows 11 com o vJoy 2.1.9.1. Código aberto sob a GPL-3.0: testes em outros jogos e celulares são a contribuição mais valiosa agora.

## Como funciona

```
 Celular (Android)                                  PC (Windows)
 sensor de rotação ─┐                         ┌─► sessão autenticada ─► vJoy ─► jogo
 pedais e botões ───┴─► pacote UDP assinado ──┘     failsafe: solta pedais e
                        até 200 vezes por segundo   centraliza se a rede cair
```

- **Direção:** o ângulo vem da fusão de sensores do próprio Android, a 200 Hz, filtrado pelo One Euro Filter.
- **Pedais:** freio e acelerador são faixas laterais da tela, controladas pela posição do polegar.
- **Pareamento:** o celular lê um código QR mostrado no PC, com uma chave aleatória de 128 bits. Cada conexão abre uma sessão com chave própria.
- **Segurança na pista:** sem pacotes por 100 ms, o PC solta os pedais; por 500 ms, centraliza a direção.

Detalhes em [docs/arquitetura.md](docs/arquitetura.md).

## Estrutura do repositório

| Pasta | O que tem |
|---|---|
| `shared/domain` | Regras puras: cálculo do ângulo, filtro, curvas, failsafe, estatísticas do sensor |
| `shared/protocol` | Formato do pacote de 32 bytes, HMAC, troca de chaves e código QR |
| `android/engine` | Motor de tempo real do celular: sensor, rede, sessão |
| `android/app` | Telas do app Android (Jetpack Compose) |
| `desktop/receiver` | Recepção UDP, sessão, watchdog e estatísticas no PC |
| `desktop/vjoy` | Único módulo que conversa com o vJoy (via JNA) |
| `desktop/app` | Programa do Windows (Compose for Desktop), verificação do PC e instalador |
| `docs/` | Arquitetura e decisões técnicas |
| `reports/`, `research_notes/` | Estudo de viabilidade que originou o projeto |
| `spikes/` | Experimentos descartáveis (como o script de teste do vJoy) |
| `licenses/` | Licenças de fontes e componentes de terceiros |

## Pré-requisitos para desenvolver

- **Windows 10 ou 11** (o receptor e o vJoy só existem no Windows)
- **JDK 17** (`JAVA_HOME` apontando para ele)
- **Android SDK** com a plataforma 36, configurado em `local.properties` (`sdk.dir=...`)
- **vJoy 2.1.9.1**, para testar com jogos (o programa também instala, pela tela de Verificação)
- Um celular **Android 10 ou mais novo** com giroscópio, e a depuração USB ativada para instalar pelo cabo

O Gradle vem pelo wrapper (`gradlew`); não é preciso instalá-lo.

## Comandos

Rodar todos os testes da JVM (domínio, protocolo, receptor):

```bash
gradlew.bat test
```

Rodar também o teste de contrato contra o vJoy real (mexe no volante virtual):

```bash
gradlew.bat :desktop:vjoy:test -Pvwr.vjoy=true
```

Abrir o programa do Windows em modo de desenvolvimento:

```bash
gradlew.bat :desktop:app:run
```

Gerar e instalar o app no celular conectado por USB:

```bash
gradlew.bat :android:app:assembleDebug
```

```bash
adb install -r android\app\build\outputs\apk\debug\app-debug.apk
```

Gerar o instalador do Windows (o WiX é baixado automaticamente na primeira vez):

```bash
gradlew.bat :desktop:app:packageMsi
```

| Saída | Caminho |
|---|---|
| APK de depuração | `android/app/build/outputs/apk/debug/app-debug.apk` |
| Instalador do Windows | `desktop/app/build/compose/binaries/main/msi/Virtual Wheel Racing-0.1.1.msi` |

## Usando

1. **No PC:** instale o `Virtual Wheel Racing-0.1.1.msi` e abra o Virtual Wheel Racing. Na **Verificação do PC**, use os botões para instalar o vJoy, configurar o dispositivo e liberar o firewall, até todos os itens ficarem OK.
2. **No celular:** abra o app, toque em **Ler código QR** e aponte para o código na seção **Conectar celular** do PC. O celular lembra do PC nas próximas vezes.
3. **No celular:** toque em **Calibrar** uma vez, para testar o sensor, e depois em **Começar a dirigir**.
4. **No jogo:** nas opções de controle, escolha o **vJoy Device**. Use os botões **Atribuir** da tela de direção para mexer um eixo por vez enquanto o jogo pede cada controle (eixo X = direção, Y = acelerador, Z = freio, botões 1 a 16). Deixe a zona morta do jogo em 0%.

Celular e PC precisam estar na mesma rede, de preferência Wi-Fi de 5 GHz com o PC no cabo. O programa usa a porta **UDP 47820**.

## Jogos

| Jogo | Situação |
|---|---|
| rFactor 1 | Validado: sem inverter eixos, sem atraso perceptível |
| Euro Truck Simulator 2, American Truck Simulator, Assetto Corsa, BeamNG.drive | Alvos principais, ainda não testados com o Virtual Wheel Racing |
| F1, EA WRC, Forza Horizon, GTA V | Fora de foco por enquanto (anti-cheat ou só aceitam controle de Xbox) |

A matriz completa, com as fontes, está em [reports/Volante virtual com celular.md](reports/Volante%20virtual%20com%20celular.md).

Testou em outro jogo? Abra uma issue com o modelo **Compatibilidade de jogo**, funcionando ou não: é assim que a lista cresce.

## Limitações conhecidas

- O instalador e o app ainda **não têm assinatura digital**; o Windows mostra o aviso do SmartScreen e o APK é de depuração.
- O instalador não instala o vJoy nem libera o firewall durante a instalação: isso é feito na primeira abertura, pela Verificação do PC.
- A chave de pareamento fica guardada sem proteção extra (arquivo no PC, preferências no celular).
- Não há vibração com a pista nem telemetria dos jogos ainda.

A lista completa, com o que entra em cada etapa, está na seção 13 de [docs/arquitetura.md](docs/arquitetura.md).

## Próximos passos

- Versão release assinada do APK e certificado para o instalador do Windows
- Beta fechado pela Play Store
- Vibração por eventos da pista (telemetria ou force feedback do jogo)
- Testes formais de atraso e de sessões longas

## Contribuindo

Contribuições são bem-vindas: testes em jogos e celulares, relatos de bug, melhorias de código e de documentação. Leia o [guia de contribuição](CONTRIBUTING.md) e o [código de conduta](CODE_OF_CONDUCT.md). Falhas de segurança devem ser relatadas em particular, conforme o [SECURITY.md](SECURITY.md).

## Licença

Virtual Wheel Racing é software livre, distribuído sob a [GNU General Public License v3.0](LICENSE). Você pode usar, estudar, modificar e redistribuir, inclusive comercialmente, desde que versões distribuídas mantenham o código aberto sob a mesma licença.

Componentes de terceiros mantêm as licenças próprias, listadas em [licenses/THIRD-PARTY.md](licenses/THIRD-PARTY.md): o instalador do vJoy (MIT), as fontes (OFL) e bibliotecas Apache 2.0, todas compatíveis com a GPL-3.0.
