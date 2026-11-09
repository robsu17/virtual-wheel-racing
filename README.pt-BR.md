<p align="center">
  <img src="desktop/app/icons/icon-256.png" width="120" alt="Logo do Virtual Wheel Racing: volante de corrida preto com pegadas vermelhas">
</p>

<h1 align="center">Virtual Wheel Racing</h1>

<p align="center">Transforma o celular Android em um volante para jogos de corrida e de caminhão no PC.</p>

<p align="center">
  <a href="https://github.com/robsu17/virtual-wheel-racing/actions/workflows/ci.yml"><img src="https://github.com/robsu17/virtual-wheel-racing/actions/workflows/ci.yml/badge.svg" alt="Status da CI"></a>
  <a href="https://github.com/robsu17/virtual-wheel-racing/releases"><img src="https://img.shields.io/github/v/release/robsu17/virtual-wheel-racing?include_prereleases&label=vers%C3%A3o" alt="Versão mais recente"></a>
  <a href="LICENSE"><img src="https://img.shields.io/github/license/robsu17/virtual-wheel-racing" alt="Licença GPL-3.0"></a>
  <img src="https://img.shields.io/badge/plataforma-Android%2010%2B%20%7C%20Windows%2010%2F11-informational" alt="Plataformas: Android 10 ou mais novo e Windows 10 ou 11">
</p>

<p align="center">
  <a href="README.md">English</a> · <strong>Português</strong>
</p>

---

Segure o celular como um volante: ele envia direção, pedais e botões pela sua rede Wi-Fi. O programa do Windows repassa tudo para um controle virtual ([vJoy](https://github.com/jshafer817/vJoy)), que os jogos enxergam como um volante comum. Sem hardware extra, sem conta, sem internet.

> **Status:** primeira versão pública (0.1.1). Validado no rFactor com um Poco X8 Pro (Android 16) e Windows 11. Testes em outros jogos e celulares são a ajuda mais valiosa agora.

## Download

**[⬇ Baixar a versão mais recente](https://github.com/robsu17/virtual-wheel-racing/releases)**. Você precisa dos dois arquivos:

| Arquivo | Onde instalar |
|---|---|
| `virtual-wheel-racing-<versão>-windows.msi` | No PC (Windows 10 ou 11). Já traz o instalador do driver vJoy. |
| `virtual-wheel-racing-<versão>-android.apk` | No celular (Android 10 ou mais novo, com giroscópio). Permita "instalar apps desconhecidos" para o navegador ou o gerenciador de arquivos. |

Cada versão traz o SHA-256 de cada arquivo, para você conferir o download.

## Primeiros passos

1. **PC:** instale o MSI e abra o Virtual Wheel Racing. Clique em **Preparar meu PC**: ele instala o vJoy, configura o dispositivo e libera o firewall. O Windows pede confirmação em cada passo.
2. **Celular:** abra o app, toque em **Escanear o código do PC** e aponte para o código que aparece no PC. O celular lembra do PC.
3. **Celular:** toque em **Jogar** e segure o celular como um volante. Em **Direção**, escolha Suave, Normal ou Rápida.
4. **Jogo:** nas opções de controle, escolha o **vJoy Device** e atribua a direção (eixo X), o acelerador (Y), o freio (Z) e os botões. Em **Configurar meu jogo** há um passo a passo por jogo e o **modo configuração**, que manda um comando por vez para o jogo detectar o certo. Deixe a zona morta do jogo em 0%.

## Recursos

- **Direção pelos sensores do celular**, até 200 vezes por segundo, com a fusão de sensores do Android e o filtro One Euro: estável parado e rápido nas curvas.
- **Pedais analógicos** nas laterais da tela, pela posição do polegar; você escolhe de que lado fica o freio.
- **16 botões** em duas páginas, atribuídos livremente em cada jogo.
- **Pareamento por QR** com chave aleatória de 128 bits; cada conexão abre uma sessão com chave própria.
- **Segurança na pista:** se o Wi-Fi cair, o PC solta os pedais em até 100 ms e centraliza a direção em até 500 ms.
- **Teste de sensores** que mede a taxa real do seu celular e usa um modo reduzido em sensores lentos.
- **Configuração universal:** um volante só para todos os jogos, configurado dentro do jogo como um volante de verdade.

## Jogos

| Jogo | Situação |
|---|---|
| rFactor | ✅ Validado |
| Euro Truck Simulator 2, American Truck Simulator, Assetto Corsa, BeamNG.drive | 🎯 Alvos principais, ainda não testados |
| Série F1, EA WRC, Forza Horizon, GTA V | ⛔ Fora de foco por enquanto (anti-trapaça ou só controle de Xbox) |

A lista completa, com dicas, está em **[docs/games.md](docs/games.md)** (em inglês). Testou outro jogo? Abra uma issue com o modelo **Compatibilidade de jogo**.

## Privacidade e permissões

- **Só rede local.** Celular e PC conversam direto pelo seu Wi-Fi, na porta UDP 47820. Nada vai para a internet.
- **Nenhuma coleta de dados.** Sem conta, análises, anúncios ou telemetria.
- **Permissão de administrador só em três passos,** cada um confirmado por você no aviso do Windows: instalar o vJoy, configurar o dispositivo dele e criar uma regra de firewall limitada a este programa e a redes privadas.
- **O celular não pede permissão especial.** A leitura do QR usa o leitor do Google, que abre a câmera por conta própria, sem dar acesso à câmera para o app.
- **Todo pacote é assinado.** Aparelhos que não leram o seu QR não conseguem controlar o volante.

## Solução de problemas

| Problema | O que conferir |
|---|---|
| O celular fica em "Procurando o PC" | Celular e PC na mesma rede (não na rede de convidados); **Preparar meu PC** concluído (veja em *Detalhes técnicos*); o Virtual Wheel Racing aberto no PC. |
| O Windows mostra "O Windows protegeu o computador" | O instalador ainda não tem assinatura digital. Clique em **Mais informações › Executar assim mesmo**. Você pode comparar o SHA-256 do arquivo com o da página da versão. |
| O jogo não mostra o vJoy Device | Clique em **Preparar meu PC** ou confira em *Detalhes técnicos > Verificação do PC*. Reinicie o jogo depois de instalar o vJoy. |
| Direção invertida ou rápida demais | Use a opção de inverter ou a rotação do volante no jogo. No celular, ajuste o **Giro do celular** em Ajustes. |
| A direção trava com o celular deitado | O app congela a direção acima de uns 70° de inclinação. Segure o celular mais em pé. |
| Atraso ou travadas | Use Wi-Fi de 5 GHz e o PC ligado ao roteador por cabo. Veja o "Intervalo entre pacotes" no Painel do PC. |

## Limitações conhecidas

- O instalador do Windows ainda não tem assinatura digital, então o Windows mostra o aviso do SmartScreen. O APK do Android é assinado.
- O MSI não instala o vJoy durante a instalação; isso acontece na primeira abertura, pelo botão Preparar meu PC.
- A chave de pareamento fica guardada sem proteção extra (arquivo no PC, preferências do app no celular).
- Ainda não há vibração com a pista nem telemetria dos jogos.
- O leitor de QR depende do Google Play Services, que não é software livre; a troca está planejada.

## Compilando a partir do código

Requisitos: Windows 10 ou 11, JDK 17, Android SDK com a plataforma 36 (`sdk.dir` em `local.properties`) e o vJoy 2.1.9.1 para testar com jogos. O Gradle vem pelo wrapper.

```bash
gradlew.bat test :android:engine:testDebugUnitTest
```

```bash
gradlew.bat :android:app:assembleDebug :desktop:app:packageMsi
```

```bash
gradlew.bat :desktop:app:run
```

O APK sai em `android/app/build/outputs/apk/debug/` e o MSI em `desktop/app/build/compose/binaries/main/msi/`. O `assembleRelease` gera um APK otimizado, assinado só no PC do mantenedor (veja [docs/release.md](docs/release.md)). O `tools/vjoy-teste.ps1` mexe os eixos do vJoy sem o celular, útil para atribuir controles num jogo.

| Pasta | O que tem |
|---|---|
| `shared/domain` | Regras puras: cálculo da direção, filtro, curvas, failsafe, estatísticas do sensor |
| `shared/protocol` | Pacote de 32 bytes, HMAC, troca de chaves, código QR |
| `android/engine`, `android/app` | Motor de tempo real e telas do celular (Jetpack Compose) |
| `desktop/receiver`, `desktop/vjoy`, `desktop/app` | Receptor do PC, adaptador do vJoy, programa do Windows e instalador (Compose for Desktop) |
| `docs/` | [Arquitetura](docs/arquitetura.md) e compatibilidade de jogos |

## Contribuindo

Testes em jogos e celulares, relatos de bug, traduções, código e documentação são bem-vindos. Leia o [guia de contribuição](CONTRIBUTING.md) e o [código de conduta](CODE_OF_CONDUCT.md). Os commits seguem o [Conventional Commits](https://www.conventionalcommits.org/pt-br/v1.0.0/). Falhas de segurança devem ser relatadas em particular, conforme o [SECURITY.md](SECURITY.md).

## Créditos

- [vJoy](https://github.com/jshafer817/vJoy), de Shaul Eizikovich e mantido por Justin Shafer, fornece o controle virtual.
- Inspirado no JD SimWheel, um app Android descontinuado que fazia isso anos atrás.
- Fontes: Barlow Semi Condensed e Inter (SIL Open Font License).

## Licença

O Virtual Wheel Racing é software livre, sob a [GNU General Public License v3.0](LICENSE): você pode usar, estudar, modificar e compartilhar, inclusive comercialmente, desde que versões distribuídas continuem abertas sob a mesma licença. Componentes de terceiros mantêm as próprias licenças, listadas em [licenses/THIRD-PARTY.md](licenses/THIRD-PARTY.md).
