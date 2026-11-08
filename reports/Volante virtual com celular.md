# Refazer o JD SimWheel é viável com vJoy, sem foco em F1

Construir um app do tipo JD SimWheel é viável em novembro de 2026, e a parte difícil já não é ler sensores: é manter um driver virtual funcionando no Windows. As APIs de sensores do Android, o UDP em rede local e o vJoy bastam para um produto funcional, e há concorrentes ativos fazendo isso. **Com as decisões de 7 de novembro (só Android, Kotlin + Jetpack Compose, vJoy como driver e F1 fora de foco), os alvos são Euro Truck Simulator 2, American Truck Simulator, Assetto Corsa e BeamNG.** O maior risco deixa de ser o anti-cheat da EA e passa a ser o próprio vJoy, que tem manutenção mínima e relatos conflitantes no Windows 11. A arquitetura é um app Android em Kotlin enviando pacotes UDP autenticados a um receptor Windows, que alimenta um volante DirectInput via vJoy, atrás de uma interface que permita trocar de driver. A estimativa de latência específica do celular é de cerca de 23 ms sem predição e 9-14 ms efetivos com predição em aparelho topo de linha, mais do que a estimativa mais otimista das notas sugeria. O custo direto de entrada é baixo, cerca de USD 245-347 no primeiro ano, e o esforço estimado é de 18-32 semanas em tempo parcial até Android com telemetria. A demanda existe (cerca de 296 mil instalações cumulativas em três apps), mas é de nicho e sem dado de receita. **Nada foi testado em hardware nem dentro de jogos nesta pesquisa**: toda célula de compatibilidade e todo número de latência precisa ser confirmado pelos spikes listados no fim, antes de qualquer compromisso de implementação.

## As decisões de escopo: Android, Kotlin, vJoy e F1 fora de foco

Estas decisões foram tomadas em 7 de novembro de 2026, depois da pesquisa, e o relatório já as reflete. Os dados sobre iOS, ViGEmBus, HIDMaestro e F1 ficam só como referência, caso o escopo mude.

| Decisão | Escolha | Consequência |
|---|---|---|
| Plataforma móvel | Só Android | Sem cliente iOS, Apple Developer ou fase de iOS |
| Stack móvel | Kotlin + Jetpack Compose | Sem React Native; uma linguagem só, com o motor em módulo e thread próprios |
| Stack do PC | Kotlin + Compose for Desktop | Mesma linguagem do celular, com protocolo compartilhado; instalador maior por levar o Java; pausas de GC a medir |
| Celular de teste | Poco X8 Pro, Android 16 | Taxa do sensor e comportamento do sistema da Xiaomi medidos nesse aparelho |
| Licença | Código fechado | Sem código GPL; referências MIT com aviso de licença; certificado OV no Windows |
| Driver no PC | vJoy | Sem backend XInput; o app depende de um único driver, com versão ainda a definir |
| Jogos | F1 fora de foco | Some o maior risco de anti-cheat; EA WRC, Forza Horizon e GTA V também saem do MVP |
| Configuração | Universal, sem perfis por jogo | Um volante genérico com ajustes globais; a pessoa mapeia eixos, botões e rotação dentro de cada jogo |

## Como ler as evidências: quatro níveis e nenhum teste

Pesquisa de 6 de novembro de 2026, com escopo atualizado em 7 de novembro sem nova pesquisa, e sem execução de nenhum app, driver ou jogo. Cada afirmação relevante leva um rótulo, e o rótulo vale mais que o tom da frase. **[F]** é fato verificado: documentação oficial ou página primária lida diretamente, API do GitHub ou licença do próprio projeto. **[F\*]** é fonte oficial ou primária vista apenas por trecho de busca, porque a página bloqueou o acesso (páginas da EA, de suporte da Forza e várias do Nordic DevZone retornaram 403 ou falharam). **[R]** é relato de comunidade ou alegação do próprio fornecedor: fórum, issue, avaliação de loja, README sobre si mesmo. **[E]** é estimativa, cálculo próprio, simulação sintética ou inferência de engenharia.

As lacunas de cobertura pesam: **Reddit, texto de avaliações do Google Play, XDA, AppBrain, fóruns SCS e RaceDepartment não puderam ser lidos**, então os temas de reclamação de usuários são hipóteses. A página odd.blog sobre o JD SimWheel continha uma injeção de prompt e foi tratada como não confiável, de modo que o pouco que vem dela está marcado como tal e não recebe link aqui. O conteúdo jurídico é informativo e não constitui aconselhamento legal.

## O veredito: viável com escopo por jogo, não como promessa universal

| Dimensão | Veredito | Confiança | Por quê |
|---|---|---|---|
| Sensores e cálculo do ângulo | Viável | Alta [E] | Fusão do SO a 200 Hz no Android; simulações e documentação sustentam o método, falta bancada |
| Transporte Wi-Fi/USB | Viável | Média-alta [E] | Média baixa esperada; a cauda é o risco, sem medição controlada em celulares |
| Dispositivo virtual no Windows | Viável, frágil | Média [R] | vJoy exige admin, tem manutenção mínima e relatos conflitantes no Win 11 |
| ETS2, ATS, AC, BeamNG | Viável | Média [R] | Apps e projetos usam vJoy nesses títulos (ATS por inferência) |
| Simuladores restantes | Provável, a testar | Baixa-média [R/E] | Relatos esparsos ou de fornecedor único |
| F1, EA WRC | Fora de foco | Baixa | Javelin em kernel; nenhum teste confirmado; sem compromisso por ora |
| Forza Horizon, GTA V | Fora do MVP | Baixa [R] | Funcionam melhor como controle Xbox; o FH5 depende de vJoy + EmuWheel |
| Force feedback real | **Inviável no sentido físico** [E] | Alta | Celular não aplica torque; só pistas hápticas |
| Negócio | Nicho | Baixa | Instalações sem receita, âncora de preço perto de zero |

O produto deve ser anunciado por lista pública de jogos testados, nunca como "funciona em tudo". F1 não deve ser prometido, já que não será testado por enquanto.

## O JD SimWheel sumiu sem explicação, e o mercado já foi reocupado

O JD SimWheel (`jd.simwheel`) era um app Android gratuito atribuído a John Diamantis, que falava por Wi-Fi com um servidor Windows, e esse servidor alimentava o driver vJoy. Inclinar o celular esterçava; movimentos para frente e para trás controlavam acelerador e freio. A última versão conhecida é a 4.2, de meados de 2015, e um rastreador indica remoção do Google Play em **3 de janeiro de 2016** [R, só trecho] ([AppBrain](https://www.appbrain.com/app/jd-simwheel-steering-wheel/jd.simwheel), [APKCombo](https://apkcombo.com/jd-simwheel-steering-wheel/jd.simwheel/)). A URL do Play retorna 404 hoje [F] ([Google Play](https://play.google.com/store/apps/details?id=jd.simwheel)). **Nenhuma fonte diz por que saiu.** A leitura de que um projeto de um desenvolvedor só parou de ser mantido com a chegada do Windows 10 é inferência [E], embora coerente com os relatos de servidor que não iniciava por incompatibilidade de versão do vJoy e de jogos travando no Windows 10 [R] ([XDA](https://xdaforums.com/t/app-2-3-3-jd-simwheel-virtual-steering-wheel-for-pc-racing-games.2400867/page-6)). Um relato de 2014 em blog pessoal, de fonte tratada como não confiável, dizia que o ETS2 funcionava bem e que dar ré era difícil [R]. Existe um espelho não oficial de 2016 com o APK e o servidor, e ele não deve ser indicado a usuários: não nomeia o desenvolvedor e encaminha links por um encurtador.

A lição técnica é a que importa: o app morreu preso a uma versão de driver de terceiros. Esse é o mesmo risco que qualquer sucessor herda, e o motivo de manter o vJoy atrás de uma interface na arquitetura.

Hoje a categoria é povoada por apps pequenos, com ao menos quatro lançados ou atualizados em 2025-2026, e quase todos ainda exigem um receptor Windows mais vJoy ou emulação de controle Xbox. Só um publica número de latência ("< 10 ms", sem método).

| Produto | Situação | Plataforma e conexão | Dispositivo virtual | Modelo | Tração |
|---|---|---|---|---|---|
| [Sim Wheel Controller](https://appgoblin.info/apps/com.ik.simwheel) (`com.ik.simwheel`) | Ativo, ago/2026 | Android; Wi-Fi, USB | vJoy; sem FFB | Grátis + IAP | 4,48; 35 mil+ |
| [Steering App](https://apkpure.com/steering-app/com.simracing.control) | Novo, out/2026 | Android; Wi-Fi via conta | Volante DirectInput, mecanismo não revelado | Grátis + pacotes únicos | Sem nota |
| [Touch Racer](https://appgoblin.info/apps/ru.thelv.joy) | Mantido, out/2025 | Android; Wi-Fi, cabo | Joystick, driver não nomeado | Grátis | 4,15; 114 mil+ |
| [Steering Wheel for PC](https://appgoblin.info/apps/com.mssalt.gamingwheel) | Parado, nov/2023 | Android | Não encontrado | Grátis | 3,51; 147 mil+ |
| [VSteer](https://apps.apple.com/app/id6757863011) | Ativo | iOS; LAN | DirectInput, pad, mouse | "Combustível" por anúncio; USD 1 e 9,99 | 4,1; 8 notas |
| [RojX](https://apps.apple.com/app/id6783482537) | Ativo | iOS; UDP, QR | Xbox 360 ou vJoy | Grátis listado | 3,0; 2 notas |
| [simSteer](https://simsteer.nexs.works/) | Em desenvolvimento | Android/web; LAN, USB | vJoy; sem FFB | Não informado | Não encontrado |

As métricas de loja vêm de rastreadores de terceiros [R]. Os três apps Android com dados de instalação somam cerca de **296 mil, cumulativas e com sobreposição**, não usuários ativos, e não há dado de receita ou retenção para nenhum app ([AppGoblin](https://appgoblin.info/apps/ru.thelv.joy)). As reclamações encontradas giram em torno de driver e instalação, monetização agressiva, falhas de conexão e sensação de direção [R], mas a amostra é fraca. As lacunas de mercado, em ordem de força e todas por julgamento [E]: instalação confiável sem configuração manual de vJoy (o Steering App já parece mirar nisso, então a janela pode estar fechando); latência medida e publicada com método; profundidade em simuladores de caminhão, com botões prontos para luzes, retarder e bloqueio de diferencial; sensação sem FFB via telemetria; preço justo sem limite de tempo nem anúncio como "combustível"; e prioridade a PT-BR. Um cluster de repositórios novos em 2025-2026 indica necessidade não atendida, mas também que a versão básica é fácil de construir e não é vantagem defensável.

Como referências abertas, **DriveLink** ([desktop](https://github.com/DevBoiAgru/DriveLinkDesktop), [mobile](https://github.com/DevBoiAgru/DriveLinkMobile); MIT, ativo, C++/Kotlin, UDP para vJoy, cerca de 100 Hz alegados) e **AndroidSteering** ([MIT](https://github.com/teamclouday/AndroidSteering), parado desde 2022, modo Xbox e notas de ajuste por jogo) são os melhores pontos de partida. **PhoneWheel de MiguelM-bit** ([repo](https://github.com/MiguelM-bit/PhoneWheel)) tem watchdog útil, mas sem licença detectada: pode ser lido, não copiado. [Engine](https://github.com/totoneei/Engine) e o [receptor do SimWheel](https://github.com/imkh778/SimWheeel_Receiver_) são GPL-3.0, o que impõe copyleft a derivados distribuídos [E].

## Os requisitos: 22 funcionais e 18 não funcionais, com o produto restrito a Android e Windows

A prioridade é **MVP** ou **Posterior**, com a fase do roteiro entre parênteses. **Fora do escopo** marca o que as decisões de 7 de novembro retiraram; os IDs foram mantidos. As metas numéricas são propostas de engenharia [E], não limiares estabelecidos, e serão recalibradas pelos spikes.

### Requisitos funcionais

| ID | Requisito | Prioridade |
|---|---|---|
| RF-01 | Calcular o ângulo de direção pelo vetor "para cima" derivado da orientação fundida do SO, em ambas as orientações paisagem | MVP |
| RF-02 | Recentrar por botão, gravando θ₀, e calibrar o sinal na primeira execução | MVP |
| RF-03 | Configurar arco físico (60-90°), zona morta (0,5-1°) e curva de resposta, em ângulos sem wrap | MVP |
| RF-04 | Pedais analógicos por posição do polegar nas bordas da tela, com multitoque e calibração | MVP |
| RF-05 | Conjunto fixo de botões na tela, com duração mínima de 3 pacotes por pressão | MVP |
| RF-06 | Executar autodiagnóstico de sensores no 1º uso: 10 s, taxa mediana, intervalo p95 e valores repetidos | MVP |
| RF-07 | Alternar para giroscópio em inclinação alta e sinalizar modo degradado em aparelhos de 50 Hz | MVP |
| RF-08 | Parear por QR com endereços candidatos, porta, chave e impressão digital; IP manual + PIN como alternativa | MVP |
| RF-09 | Reconectar pelo último IP sem novo pareamento | MVP |
| RF-10 | Transportar por UDP em Wi-Fi e em tethering USB, com mesmo código e aviso de qualidade do link | MVP |
| RF-11 | Backend Xbox 360 (XInput) | Fora do escopo |
| RF-12 | Volante DirectInput via vJoy, com versão fixada conforme S1 | MVP |
| RF-13 | Volante genérico, sem perfis por jogo: eixos fixos (X direção, Y acelerador, Z freio) e botões numerados, mapeados pela pessoa em cada jogo; modo de atribuição que move um eixo por vez; guia de configuração em PT-BR e inglês para ETS2, ATS, AC e BeamNG | MVP |
| RF-14 | Instalador com consentimento do driver, regra de firewall, autoteste que confirma que o dispositivo apareceu, reparo e desinstalação completa | MVP |
| RF-15 | Tela de teste com eixos e botões ao vivo, e painel de diagnóstico com p50/p95/p99, perda e maior lacuna | MVP |
| RF-16 | Failsafe em estágios e pacote `bye` em onPause, tela apagada e saída | MVP |
| RF-17 | Desligar o dispositivo virtual por chave e criá-lo só durante a sessão | MVP |
| RF-18 | Descoberta por mDNS/NSD e broadcast; seletor NSD quando a permissão for negada | Posterior (2) |
| RF-19 | Telemetria normalizada no PC, com hápticos por evento e painel/LEDs de RPM (BeamNG, memória compartilhada do AC e plugin SCS) | Posterior (3) |
| RF-20 | Editor de layout dos botões na tela, com nomes definidos pela pessoa | Posterior (3) |
| RF-21 | Cliente iOS | Fora do escopo |
| RF-22 | Modos Bluetooth HID (Android), ponte DSU local, integração SimHub, PWA de teste, Linux `uinput`, ADB, FFB PID convertido em háptico | Posterior (4+) |

### Requisitos não funcionais

| ID | Requisito | Meta proposta [E] | Prioridade |
|---|---|---|---|
| RNF-01 | Latência específica do celular, topo de linha, 5 GHz | p50 ≤ 25 ms bruto; ≤ 15 ms efetivo com predição; p99 < 40 ms (ESTIMATIVA de meta) | MVP |
| RNF-02 | Lacunas no fluxo | Menos de 1 lacuna > 100 ms a cada 10 min em rede boa | MVP |
| RNF-03 | Taxa de amostragem | Pedir 200 Hz; medir pelos timestamps | MVP |
| RNF-04 | Resolução | 16 bits no fio e 15 bits no vJoy; ruído equivalente a 0,05-0,25° de volante em ganho 5 | MVP |
| RNF-05 | Tempo real no celular | Sensor, filtro e socket em Kotlin, em módulo e thread próprios, fora da thread principal do Compose | MVP |
| RNF-06 | Receptor PC | Thread dedicada com recepção bloqueante; sem spin; UI separada do caminho de entrada | MVP |
| RNF-07 | Driver trocável | Interface `VirtualDeviceBackend` (criar, enviar estado, evento de saída/FFB, destruir), com vJoy como única implementação | MVP |
| RNF-08 | Windows | 10 e 11, incluindo Home, com Secure Boot e Integridade de Memória ligados; **nunca** exigir modo de teste | MVP |
| RNF-09 | Android | Alvo API 36 para o Play; pronto para API 37 com `ACCESS_LOCAL_NETWORK`; mínimo API 29 proposto, por causa do lock de baixa latência | MVP |
| RNF-10 | Segurança | Chave ≥ 128 bits por QR, chave de sessão, HMAC truncado, rejeição de `seq` antigo; sem modo não autenticado; firewall em Local Subnet; UI e rede sem privilégio | MVP |
| RNF-11 | Integridade de jogo | Sem injeção, sem escrita em memória, sem macro, sem ajuda de pilotagem ligada à telemetria, sem evasão de anti-cheat | MVP |
| RNF-12 | Cadeia de fornecimento | vJoy somente de release oficial, com hash fixado; instalador assinado | MVP |
| RNF-13 | Privacidade (LGPD) | Telemetria só em rede local; análise opt-in e crash anonimizado; política publicada e contato | MVP |
| RNF-14 | Instalação | Do download à primeira curva sem tocar no vJoyConf e sem abrir o firewall à mão | MVP |
| RNF-15 | Energia e calor | Tela ligada por `FLAG_KEEP_SCREEN_ON`; `getThermalHeadroom` no máx. 1× a cada 10 s; háptico com ajuste de intensidade | MVP |
| RNF-16 | Ergonomia | Sessões de teste de ≥ 20 min; apoio de cotovelo orientado no guia | MVP |
| RNF-17 | Honestidade | Metodologia de latência publicada; lista pública por jogo; modo online "por conta do usuário" | MVP |
| RNF-18 | Licenças e marca | Avisos MIT; sem código GPL em versão fechada; nome distinto | MVP |

## A arquitetura recomendada separa o celular do driver por um protocolo próprio

```
Celular (Kotlin + Compose)                        PC Windows
Sensor 200 Hz -> up + atan2 -> 1€ -> predição     recepção UDP bloqueante (thread MMCSS "Games")
Toque -> pedais/botões                     UDP    -> validação HMAC e seq -> estado mais recente
Empacota no novo sample ------------------------> -> watchdog/failsafe
                                                  -> VirtualDeviceBackend
Hápticos, painel <------- eventos, telemetria ---    └ Volante DirectInput: vJoy (versão conforme S1)
                                                  Adaptadores de jogo -> stream normalizado
```

A decisão central é manter toda a lógica de sensor no celular e todo o conhecimento de jogo e driver no PC, de modo que um driver quebrado ou trocado não altere o app móvel [E].

### Sensores: fusão do SO, nunca Euler nem acelerômetro cru

A documentação limita `registerListener()` a 200 Hz em apps com alvo Android 12+, e o direct channel a cerca de 50 Hz sem `HIGH_SAMPLING_RATE_SENSORS` [F] ([Android](https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview)). O `TYPE_GAME_ROTATION_VECTOR` dispensa magnetômetro e sua deriva ocorre em torno do eixo vertical [F] ([AOSP](https://source.android.com/docs/core/interaction/sensors/sensor-types)). O CDD do Android 16 exige em portáteis giroscópio e acelerômetro ≥ 100 Hz, mas o atraso geral permitido é de 100 ms + 2 × período, e só sensores de alta fidelidade têm entrega ≤ 5 ms [F] ([CDD](https://source.android.com/docs/compatibility/16/android-16-cdd)). Taxas reais variam muito: um aparelho vendido com giroscópio de 400 Hz rodava a 50 Hz com valores repetidos [R] ([arXiv](https://arxiv.org/pdf/2501.04886)). Daí o RF-06.

O cálculo é derivado e checado numericamente nas notas [E]. Como a deriva gira em torno do eixo z do mundo, ela não altera o vetor "para cima", então **a deriva de rumo não chega ao ângulo de direção**:

```
u = ( 2(xz − wy),  2(yz + wx),  1 − 2(x² + y²) )     // quaternion (w,x,y,z), dispositivo -> mundo
θ = atan2(s·u_y, s·u_x);   θ_rel = wrap180(θ − θ₀)    // s = sinal de u_x na calibração
n = clamp((|θ_rel| − dz)/(θmax − dz), 0, 1);   eixo = sinal(θ_rel) · curva(n)
1€: fcmin 1,5 Hz, β 0,1, dcutoff 10 Hz (em graus do celular, antes do ganho)
Inclinação alta: congelar gravidade com norma do plano < 0,35 (≈70°), retomar em 0,40; integrar giro z
Predição: H = idade da amostra + transporte, limitado a 20-30 ms, desaparecendo com |ω| pequeno
```

Em teste numérico [E] com rotação real de 30°, Euler leu 2,88° com o aparelho inclinado 85°, e o `atan2` da gravidade leu 49,11° sob inclinação de 60° em eixo do próprio aparelho (tan θ = tan r / cos p), mas ficou exato em inclinação em eixo do mundo. Qual inclinação os usuários fazem de fato é desconhecido e decide se esse acoplamento importa (S6). Acelerômetro cru sofre com a aceleração tangencial da mão: 10,1° de erro com braço de 0,10 m e 1000 °/s² [E, valores assumidos]. Integração pura de giroscópio também falha, pois o erro de calibração permitido de 0,01 rad/s vira 34°/min [E]. Filtro próprio (Madgwick, Mahony, Kalman) deve ser só alternativa: um complementar simulado teve erro RMS de 3,46° mesmo com viés baixo [E]. O ajuste de dcutoff de 1 para 10 Hz é a mudança isolada mais útil, cortando cerca de 15 ms de atraso de início na simulação [E] (entrada sintética, tremor assumido).

Vale uma ressalva sobre "graus": o dispositivo virtual entrega um eixo normalizado, e é o jogo que decide a rotação. O ganho nominal de 5 (±90° → ±450°) é uma convenção de conta, e em caminhões, com rotação relatada de 1440-1800° [R] ([Steam](https://steamcommunity.com/app/227300/discussions/0/6644556627480705723)), o mesmo arco físico cobre mais graus virtuais, então o ruído aparente sobe [E]. O vJoy usa eixos de 15 bits (0-32767) [R], o que dá cerca de 0,03° em um volante de 900° [E]. Isso é mais que suficiente: o limite é ruído, deriva e ausência de força, não bits de codificador [E].

Para pedais, a nota recomenda sliders por posição do polegar [E]. Inclinação frente/trás, a escolha do JD SimWheel, acopla na direção: 25° de inclinação transforma 30° em 32,5°, ou 12,5° de volante em ganho 5 [E]. No Android, deve-se ler as amostras históricas de `ACTION_MOVE` e rastrear por pointer ID [F] ([MotionEvent](https://developer.android.com/reference/android/view/MotionEvent)). `requestUnbufferedDispatch` não é indicado à maioria dos apps e não deve ser padrão [F\*].

### Transporte: UDP com lock de baixa latência, QR e pacote de 32 bytes

Wi-Fi em 5 ou 6 GHz com PC cabeado é o padrão, e tethering USB é o modo com fio recomendado, porque oferece UDP real sem modo desenvolvedor [E]. **Não foram encontradas medições controladas de latência unidirecional por celular em nenhum transporte.** A melhor evidência de cauda é um estudo de campus com mais de 47 mil dispositivos: p90 perto de 20 ms e p99 perto de 250 ms [F\*], em rede empresarial e não doméstica ([Microsoft Research](https://www.microsoft.com/en-us/research/?p=635400)). Ele serve de limite pessimista e justifica assumir interrupções de centenas de milissegundos no failsafe.

O `WIFI_MODE_FULL_LOW_LATENCY` (API 29+) só atua conectado a um AP, com tela ligada e app em primeiro plano, e pode reduzir varreduras e roaming [F] ([WifiManager](https://developer.android.com/reference/android/net/wifi/WifiManager)); o AOSP acrescenta exigência de internet e desativa o power save 802.11 [F] ([AOSP](https://source.android.com/docs/core/connect/wifi-low-latency)). Em API 34+, `FULL_HIGH_PERF` vira silenciosamente low-latency. Daí decorre que hotspot do celular e roteador sem internet provavelmente não recebem o benefício [E], o que precisa de teste. Também por isso o foreground service ajuda pouco: o app precisa estar visível de qualquer forma, e `connectedDevice` fica só como reserva, com `specialUse` evitado por exigir revisão do Play Console [F] ([FGS](https://developer.android.com/develop/background-work/services/fgs/service-types)).

A proteção de rede local é opt-in no Android 16 e obrigatória no Android 17 para alvo SDK 37+; o `ACCESS_LOCAL_NETWORK` é permissão em runtime, e UDP bloqueado costuma devolver `EPERM` [F] ([Android](https://developer.android.com/privacy-and-security/local-network-permission)). O app deve tratar `EPERM` com mensagem clara. No Windows, se um administrador recusar o aviso do firewall ou um não administrador responder qualquer coisa, regras de bloqueio são criadas e têm precedência sobre as de permissão, então o instalador deve criar a regra por caminho completo e Local Subnet [F] ([Microsoft Learn](https://learn.microsoft.com/windows/security/operating-system-security/network-security/windows-firewall/rules)). O QR é o caminho principal por dispensar multicast e listar todos os IPs do PC, cobrindo VPN, Hyper-V e WSL; mDNS só facilita reconexão [E].

O protocolo proposto é próprio, com 32 bytes binários, 60 bytes no fio e estado absoluto "o mais recente vence", sem retransmissão nem jitter buffer [E]. A largura de banda é irrelevante (58-96 kbit/s). O DSU/Cemuhook não serve como protocolo principal: sticks de 8 bits, CRC32 sem autenticação e sem criptografia [F] ([especificação](https://v1993.github.io/cemuhook-protocol/)), embora sirva depois como ponte local em `127.0.0.1:26760`.

| Offset | Bytes | Campo |
|---|---|---|
| 0 | 2 | magic + versão |
| 2 | 1 + 1 | tipo; flags |
| 4 | 4 | session_id |
| 8 | 4 | seq (descarta se não for mais novo) |
| 12 | 4 | t_send_us (dá a volta a cada 71 min) |
| 16 | 2 + 2 + 2 | direção i16; acelerador u16; freio u16 |
| 22 | 2 | botões |
| 24 | 8 | HMAC-SHA256 truncado |

| Silêncio | Ação no PC (limiares não validados [E]) |
|---|---|
| < 50 ms | Mantém último valor |
| 50-100 ms | Mantém e sinaliza aviso de link |
| > 100 ms | Rampa de pedais a 0 em cerca de 50 ms, mantém direção para evitar guinada |
| > 500 ms | Rampa de direção ao centro em cerca de 200 ms, solta botões |
| > 3 s | Mostra desconectado; dispositivo segue conectado e neutro |

### Receptor Windows: acordar por pacote, não por timer

A recepção deve ser bloqueante e guiada pela chegada do pacote, porque o Windows 11 não garante resolução fina de timer para processos com janela ocultos ou minimizados, justamente o caso de um app de bandeja [F\*] ([Microsoft Learn](https://learn.microsoft.com/en-us/windows/win32/api/timeapi/nf-timeapi-timebeginperiod)). A tarefa MMCSS "Games" é o ponto de partida, e "Pro Audio" só se testes mostrarem starvation [F para a existência das tarefas; E para a escolha] ([MMCSS](https://learn.microsoft.com/en-us/windows/win32/procthread/multimedia-class-scheduler-service)). A parte elevada fica num helper pequeno, com rede e UI sem privilégio [E]. A pilha do PC segue em aberto e é julgamento, sem comparação em fontes primárias [E]: com o vJoy, **Rust + Tauri** evita pausas de GC e permite interface web, o que aproveita a experiência com React; **C#/.NET** também tem bindings para o vJoy e é mais simples para uma interface Windows, desde que o laço quente não aloque memória. No celular, a stack é Kotlin + Jetpack Compose, com o motor em módulo separado e thread própria [decisão].

## O vJoy é o driver escolhido, mas nenhum caminho está limpo

O vJoy combina com o escopo: um Xbox pad virtual sofreria com os filtros de direção que os jogos aplicam a controles, e o BeamNG reduz velocidade e autoridade a 70% no filtro suave ([BeamNG-Advanced-Steering](https://awesome.ecosyste.ms/projects/github.com%2Fadam10603%2FBeamNG-Advanced-Steering)) [R]. O preço é abrir mão, por ora, de F1, FH5 e GTA V, que rejeitam um volante virtual ou o tratam mal. Os demais caminhos da tabela ficam como referência e plano B.

| Caminho | Situação em nov/2026 | Licença | Parece volante? | FFB? | Risco principal |
|---|---|---|---|---|---|
| ViGEmBus 1.22.0 | Aposentado, arquivado, funciona [F] | BSD-3 | Não: X360/DS4 | Só rumble [E: callback não reconfirmado] | Congelado; política futura; aviso de segurança não lido |
| vJoy 2.2.2.0 (Brunner) | Manutenção mínima; assinatura EV + atestado [F] | MIT | "vJoy Device" | Sim (PID, callbacks) | Falhas relatadas no Win 11 até abr/2026 [R] |
| vJoy 2.1.9.1 | Antigo; relato ok no 25H2 em mai/2026 [R] | MIT | Genérico | Fraco (correções só no 2.2.x) | Antigo |
| HIDMaestro 2.0.1 | Ativo, 21/out/2026 [F] | MIT | Sim, VID/PID próprio ou clonado [R] | HID PID 1.0 [R] | Jovem; **comportamento em Secure Boot não documentado**; certificado na raiz da máquina |
| VIIPER + usbip-win2 | Ativo [R] | GPL-3 / MIT | Sem volantes | Não documentado | Sem suporte a volante |
| VirtualPad (Nefarius) | Comercial para parceiros [F] | Proprietária | Desconhecido | Desconhecido | Termos e preço |
| Driver próprio (KMDF/VHF) | Faça você mesmo | Sua | Sim | Sim | EV, entidade, política de assinatura |
| Android BluetoothHidDevice | API 28+ [F] | n/a | Descritor próprio | Não comprovado | Só primeiro plano; varia por OEM; sem iOS; jogos só XInput não veem |

O vJoy resume a fragmentação: o repositório original aponta para outros, e a Brunner, que mantém o build assinado, diz que só publicará novos binários assinados para contribuições importantes [F] ([BrunnerInnovation](https://github.com/BrunnerInnovation/vJoy)). Há relatos conflitantes sobre o 2.2.2.0 ([issue #17](https://github.com/njz3/vJoy/issues/17), [notas de versão](https://github.com/BrunnerInnovation/vJoy/releases/tag/v2.2.2.0)), e o projeto Nimbus preferiu empacotar o 2.1.9.1 por isso [R] ([PR #5](https://github.com/owenpkent/Nimbus-Adaptive-Controller/pull/5)). Um mantenedor aceita ficar no 2.1.9 se as correções de FFB forem dispensáveis [R], o que é o caso de um celular sem motor de torque, então **o 2.1.9.1 é o candidato inicial** se o S1 confirmar [E].

Por padrão, o vJoy aparece como joystick genérico, o que deve bastar em jogos com mapeamento livre de eixos, como ETS2 e AC [E]. Há relato de que habilitar os eixos de acelerador, freio e direção o faz ser detectado como volante pelo DirectInput, mas isso exige um `vJoyConf.exe` modificado e só foi verificado em jogos DirectInput8 ([guia](https://code-monet.github.io/sim-gamer-kit/fffsake/vjoy_configuration/)) [R], então fica fora do início do projeto [E]. A máquina de desenvolvimento roda o Windows build 26300, mais novo que o 25H2 e aparentemente de teste [E]: os relatos acima podem não valer para ela, e um resultado nela também pode não valer para os usuários.

O HIDMaestro é o plano B mais interessante: alega UMDF2 em modo usuário, sem certificado comprado nem modo de teste, com perfis de volante e PID 1.0 ([README](https://github.com/hifihedgehog/HIDMaestro)) [R]. Mas uma busca textual em README e INTERNALS não encontrou menção a Secure Boot, HVCI ou Smart App Control, e o autor testou em edições IoT LTSC. **Instalar um certificado autoassinado na raiz da máquina é um custo real de confiança** [E], e a chave privada precisa ser descartada ou removida. O próprio projeto afirma que dispositivos virtuais são detectáveis por anti-cheat de kernel. Só vale testá-lo se as duas versões do vJoy falharem.

Escrever driver próprio não é primeiro passo. A assinatura recomendada é a HLK; o atestado agora está documentado como cenário de teste, exige EV e dá driver confiável mas não "Windows Certified" [F] ([Microsoft Learn](https://learn.microsoft.com/en-us/windows-hardware/drivers/dashboard/driver-signing-offerings)). Desde abril de 2026 o Windows 11 24H2+ retira confiança do programa de cross-signing, e o post não menciona atestado [F] ([Microsoft](https://techcommunity.microsoft.com/blog/windows-itpro-blog/advancing-windows-driver-security-removing-trust-for-the-cross-signed-driver-pro/4504818)). GameInput e `Windows.Gaming.Input.Custom` servem a jogos ou a fabricantes e não criam dispositivos [F] ([GameInput](https://learn.microsoft.com/en-us/gaming/gdk/docs/features/common/input/overviews/input-overview), [WGI Custom](https://learn.microsoft.com/en-us/uwp/api/windows.gaming.input.custom)). O `BluetoothHidDevice` dispensa driver, mas é desregistrado fora do primeiro plano [F] ([Android](https://developer.android.com/reference/android/bluetooth/BluetoothHidDevice)) e só tem confirmação em uma TV Samsung, com Windows não verificado [R] ([PocketPad](https://github.com/Dathaze20/PocketPad)).

A falsificação de VID/PID de um volante real fica fora do produto: nenhuma fonte confirma lista de permissão por VID/PID, e as desvantagens prováveis são SDK de fabricante, marca e aparência de evasão [E]. A Nefarius usa VID 0x7331 justamente para evitar falsificar hardware real [F] ([Nefarius](https://docs.nefarius.at/research/Virtual-VID-PID-used-by-NSS/)).

## Matriz de compatibilidade: ETS2, ATS, AC e BeamNG encabeçam

**Nenhuma célula foi testada.** Com o vJoy como único driver, a coluna XInput fica só como referência. "Faixa" é a prioridade de lançamento [E]; a Forza Motorsport foi rebaixada aqui da Faixa 1 da nota para a 2, porque não há relato específico com vJoy. A série atual de F1 é o F1 25 com o 2026 Season Pack, e não se achou "F1 26" autônomo [R] ([Box This Lap](https://boxthislap.org/f1-25-update-1-25-f2-2026-sports-update-and-hardware-support/)).

| Jogo | Faixa e saída inicial | DirectInput/vJoy | XInput | Anti-cheat e pontos de atenção | Telemetria | Evidência |
|---|---|---|---|---|---|---|
| **ETS2** | 1, volante | Sim: [apps usam vJoy](https://github.com/totoneei/Engine) | Não verificado | Subtipo volante, sensibilidade 100%, [não linearidade 0-10%](https://steamcommunity.com/app/227300/discussions/0/3033726413401664299); [eixo como velocidade](https://steamcommunity.com/app/227300/discussions/0/3805027459318826439) relatado | [Plugin SCS MIT](https://github.com/RenCloud/scs-sdk-plugin); compatibilidade listada até 1.45-1.46 | R |
| **ATS** | 1, volante | Por inferência do ETS2 | Não verificado | Sem teste separado nas fontes | Mesmo plugin | E |
| **Assetto Corsa** | 1, volante | Sim: [AndroidSteering](https://github.com/teamclouday/AndroidSteering), [MouseToVJoy](https://github.com/R1PeR/MouseToVJoy) | Modo pad existe | Vinculação manual | [Memória compartilhada](https://spacefreak18.github.io/simapi/simd_usage) | R |
| **BeamNG.drive** | 1, volante | Sim, com [guia; versão nova falhou a um autor](https://forum.beammp.com/t/sim-racing-full-mouse-control-and-vehicle-dynamics/1336720) | Sim | [Filtro Direct oficial](https://documentation.beamng.com/support/hardware/steering_wheel_common_problems/) | [UDP OutGauge e MotionSim](https://docs.beamng.com/modding/protocols/) | F/R |
| **ACC** | 2, volante | Provável, [sem prova direta de direção](https://steamcommunity.com/app/805550/discussions/0/3879344844062595156) | Sim, com filtros | Anti-cheat não confirmado | Memória compartilhada | R/E |
| **AC EVO** | 2, volante | [Hub de terceiro](https://github.com/Barden-dev/VirtualWheel-Hub/wiki) | Não verificado | Acesso antecipado; Steam Input suspeito em FFB | Alegada, não confirmada | R |
| **Forza Motorsport** | 2, volante | Em princípio: [API DirectInput, config. manual](https://racinggames.gg/article/forza-motorsport-steering-wheel-support-list-revealed) | Sim | Sem relato de vJoy | [UDP Data Out, 60 pacotes/s](https://support.forza.net/hc/en-us/articles/21742934024211-Forza-Motorsport-Data-Out-Documentation) | F\*/E |
| **Le Mans Ultimate** | 2, volante | Provável: [hub de terceiro; entrada de alta frequência causa problemas](https://github.com/Barden-dev/VirtualWheel-Hub/wiki/Game-Compatibility-and-Plugins) | Sim | [Autor alega leitura sob EAC](https://github.com/Swizzjack/lmu-pitwall); limitar taxa | Memória compartilhada | R |
| **AMS2** | 2, volante | Misto: [volante personalizado](https://steamcommunity.com/app/1066890/discussions/0/3408678077959702310); [fórum chama vJoy de gambiarra](https://forum.reizastudios.com/goto/post?id=267341) | Não verificado | Anti-cheat não confirmado | Memória compartilhada | R |
| **iRacing** | 2, volante | Sim na prática: [irFFB](https://github.com/nlp80/irFFB/wiki/Configuration) | Não verificado | [Termos barram emular protocolos próprios](https://ir-core-sites.iracing.com/members/pdfs/20251106_iRacing_Commercial_Terms_dated_Nov_06_2025.pdf) (trecho; fala de protocolos, não HID); [modo de teste bloqueia corridas](https://www.github.com/shauleiz/vJoy/issues/57) | [Mapa de memória, 60 Hz](https://docs.rs/crate/iracing/latest) | R |
| **rFactor 2** | 2, volante | Não verificado: [hub de terceiro](https://github.com/Barden-dev/VirtualWheel-Hub/wiki/Game-Compatibility-and-Plugins) | Não verificado | Sem evidência direta | Plugin | E |
| **rFactor (1)** | 1, volante | **Validado em 7/nov/2026** com o protótipo Virtual Wheel Racing (vJoy 2.1.9.1): sem inverter eixos, sem atraso perceptível nem tremor parado | Não verificado | Nenhum | Não pesquisada | Teste próprio |
| **EA WRC / Dirt Rally 2** | Fora de foco; DR2 a avaliar | [Mapa de ação em eixo X](https://github.com/Ultrawipf/OpenFFBoard/wiki/Games-setup); vJoy não confirmado | Sim | **Javelin no EA WRC**; [Dirt limita recursos sem volante detectado](https://code-monet.github.io/sim-gamer-kit/game_guides/issues/) | Não confirmada | R/E |
| **F1 23/24/25 + 2026** | Fora de foco | Arriscado: [EA adiciona volantes por patch](https://racinggames.gg/article/f1-25-v113-patch-adds-major-wheel-support-fixes-game-breaking-career-save-bug); [EA diz vJoy sem suporte no F1 23](https://forums.ea.com/discussions/f1-23-en/f1-23-crash-to-desktop-during-specific-race-events/8421646/replies/8421648); [funcionou no F1 2021](https://steamcommunity.com/app/1134570/discussions/0/5190945662882743122) | Caminho oficial de controle | **[Javelin em kernel](https://www.ea.com/news/anticheat-progress-report)**; [aviso da EA sobre controles e volantes](https://help.ea.com/pt-br/articles/f1/f1-25/controllers-wheels-and-vr-support/); [reWASD barrado](https://forum.rewasd.com/forum/rewasd/technical-questions-aa/246126-ea-security-violation?p=246145) | [UDP, porta 20777](https://www.f1laps.com/faqs/f1-25-udp-telemetry-settings/) | R, F\* |
| **FH5** | Fora do MVP | Parcial: [vJoy + EmuWheel](https://forums.forza.net/t/g923-wheel-not-detected-without-vjoy-and-emuwheel-does-yours-work-without-any-extra-software/537410); [crash ao iniciar com driver](https://www.github.com/shauleiz/vJoy/issues/60) | Sim, [zona morta central de 5-10% relatada](https://forums.forza.net/t/why-is-there-a-5-10-centre-deadzone-on-controller/557532) | [EmuWheel exige registro e refazer](https://steamcommunity.com/app/1293830/discussions/0/3049485912455199727) | Não verificada | R |
| **FH6** | Fora do MVP | Não verificado: [modo compatibilidade irregular](https://finalboss.io/forza-horizon-6-wheel-support-is-messy-at) | Presumido | [FFB só no "Device 1"](https://support.forza.net/hc/en-us/articles/51674028831251-FH6-Supported-Wheels-and-Devices) | [UDP, 324 bytes](https://support.forza.net/hc/en-us/articles/51744149102611-Forza-Horizon-6-Data-Out-Documentation) | R/E |
| **GTA V** | Fora do MVP | [Não nativo](https://www.hackster.io/news/the-30-arduino-hack-every-gta-v-fan-should-build-b7a6d9901ade) | Sim, obrigatório | [Sem FFB](https://steamcommunity.com/app/271590/discussions/0/611703999964693971); mods quebram detecção | Não verificada | R |

Fica registrado, caso o F1 volte ao escopo: o custom actionmap resolveu um volante não listado lido como controle [R] ([EA](https://forums.ea.com/discussions/f1-24-technical-issues-en/asetek-wheel-is-not-working--recognized-as-controllerkeyboard/8441232)), mas o formato é da EA e distribuí-lo exige revisão [E]. A busca achou o título de um tópico do F1 25 dizendo que o reWASD não pode rodar junto com o jogo, com página inacessível [F\*] ([EA](https://forums.ea.com/discussions/f1-25-pt/este-software-n%C3%A3o-pode-ser-usado-ao-mesmo-tempo-que-o-jogo-rewasd/12320914)). Um guia de terceiro sobre o EA FC 25, sem valor de declaração da EA, nomeia vJoy Device como item a desativar [R] ([Appuals](https://appuals.com/ea-fc-25-error-this-software-cannot-be-used-at-the-same-time-as-the-game-virtualcontroller/)). **Não se encontrou banimento documentado por vJoy ou ViGEm, nem autorização de nenhum fornecedor.** A petição que alega falsos positivos é de advocacy, não prova de casos. O produto trata uso online e competitivo como "por conta do usuário, verifique as regras".

A telemetria é um diferencial real, com feeds oficiais ou comunitários na maioria dos títulos, e deve ficar no PC, que manda ao celular um fluxo único. A ordem sugerida é BeamNG (UDP), memória compartilhada do AC e o plugin SCS para ETS2 e ATS, que exige consentimento por colocar DLL na pasta do jogo [E]. Se o F1 voltar ao escopo, novas instalações usam o formato 2026 e saves existentes ficam no F1 25, enquanto um terceiro recomenda 2024 ([EA](https://forums.ea.com/blog/f1-games-game-info-hub-en/ea-sports%E2%84%A2-f1%C2%AE25-2026-season-pack-udp-specification/12187347)) [F\*]: o parser precisa ler o formato do cabeçalho. Hápticos são pistas e não substituem FFB, pois não aplicam torque [E]. Os limites do Android (20 ms entre pontos de envelope, cerca de 50 ms entre primitivas) restringem a atualização a 20-50 Hz, e `hasAmplitudeControl()` decide o fallback ([Android](https://developer.android.com/develop/ui/views/haptics/haptics-apis)) [F]. O motor vibrando no aparelho sensoriado pode contaminar o IMU, risco raciocinado e não medido [E].

## O orçamento estimado de latência: cerca de 23 ms no celular, e o jogo pesa mais

As notas divergem aqui, e a diferença muda a conclusão. A nota de transporte estima 13,8 ms específicos do celular assumindo 2 ms de filtro e 3 ms de sensor; a nota de sensores estima 18 ms só no lado do celular, com 10 ms de One Euro em movimento obtidos por simulação. Como a própria nota de transporte remete o filtro à de sensores, este relatório adota a conta de sensores para o trecho do celular e a de transporte para o restante. **Nenhuma linha abaixo é medição.**

| ESTIMATIVA (ms), Wi-Fi 5 GHz, envio a cada amostra | Topo, 200 Hz | Médio, 100 Hz | Básico, 50 Hz |
|---|---|---|---|
| Espera de amostragem T/2 (aritmética) | 2,5 | 5 | 10 |
| Filtro de saída do chip (assumido) | 2 | 3 | 5 |
| HAL e framework (assumido) | 3 | 6 | 15 |
| Matemática do ângulo | < 0,5 | < 0,5 | 1 |
| One Euro em movimento (simulação) | 10 | 15 | 20 |
| Entrega ao socket (assumido) | 0,5 | 0,5 | 1 |
| **Subtotal celular** | **≈ 18** | **≈ 30** | **≈ 52** |
| Rede 3 + recepção 0,3 + driver 1 (típico) | 4,3 | 4,3 | 4,3 |
| **Específico do celular, bruto** | **≈ 23** | **≈ 34** | **≈ 56** |
| Com 10-20 ms de predição (efetivo) | ≈ 9-14 | ≈ 19-24 | ≈ 39-44 |
| Jogo e tela a 60 fps (8,3 + 25 + 8) | 41,3 | 41,3 | 41,3 |
| **Total movimento-a-fóton, bruto** | **≈ 64** | **≈ 75** | **≈ 97** |

Comparado a um volante USB com fio, estimado em cerca de 43 ms no mesmo jogo e tela [E], o celular topo de linha adiciona **cerca de 21 ms brutos, ou 7-12 ms efetivos com predição** [E]. Portanto, o achado da nota de transporte de que o custo extra é "de 8-12 ms, menos de um quadro" só vale com predição ativa ou filtro muito mais leve. E predição não remove atraso: reduz o erro em movimento suave e pode errar em reversões e paradas bruscas, que a simulação não modela. Ela cortou o erro RMS de 7,97° para 1,00° com 2 Hz, ±45° e H = 20 ms [E]. Temporizador livre a 120 Hz soma cerca de 4 ms médios e tethering USB economiza pouco na média (rede de 1-2 ms contra 3 ms [E]), de modo que o ganho do cabo é a cauda. Na faixa de pior caso da nota de transporte, o subtotal específico chega a 59,3 ms e o total a 146 ms [E], ainda sem contar interrupções.

Como referência fraca [R], a gamepadla lista stick a 6,5-8,9 ms em dongle e 13-13,6 ms em Bluetooth, com método não divulgado e um aviso de que o módulo pode conter erros ([gamepadla](https://gamepadla.com/versus/sony-dualsense_vs_xbox-core-controller.html)). A literatura trata 50 ms como aceitável e acima de 100 ms como a evitar em corrida [F\*, resumo] ([Pantel & Wolf](https://cs.ubc.ca/~krasic/cpsc538a/summaries/52)), mas esses números vêm de atraso de rede adicionado, não de movimento-a-fóton, e um levantamento alerta para limiares inconsistentes ([survey](https://ntnu.no/ojs/index.php/nikt/article/view/5252)). Assim, a meta "menos de 50 ms" da nota de sensores deve ser lida como **atraso adicionado**, já que jogo e tela sozinhos consomem cerca de 41 ms na estimativa. Não se achou estudo sobre latência perceptível em sim racing, portanto não é possível afirmar que a diferença é imperceptível.

A prioridade de otimização [E]: primeiro eliminar picos (5 GHz, PC cabeado, lock de baixa latência), depois enviar a cada amostra, depois manter o filtro leve; taxas acima de 200 Hz têm retorno baixo. **A nota de transporte é inconsistente sobre taxa de envio**: recomenda 120 Hz e também enviar a cada amostra, mas 120 Hz presume temporizador e não é divisor inteiro da amostragem de 200 Hz. A resolução proposta é enviar a cada amostra (até 200 Hz), recuando para metade (100 Hz) se a perda crescer, confirmada em S4. PWA fica como demonstração sem instalação: o Chromium limita sensores a 60 Hz [F\*] ([Chromium](https://crrev.com/9d5b8269d088402d1d9c261d1c12585d81a4ac78)), não há lock de Wi-Fi e sensores pedem contexto seguro, com penalidade de cerca de 10-15 ms [E].

## Riscos: o driver supera qualquer risco de sensores

Probabilidade e impacto são julgamento [E].

| ID | Risco | Prob. | Impacto | Mitigação | Spike |
|---|---|---|---|---|---|
| R3 | vJoy falha no Win 11 ou em builds futuras, sem mantenedor ativo; como driver único, para o produto | Média | Muito alto | Fixar versão testada; autoteste; reparo; interface de backend; HIDMaestro como plano B | S1 |
| R1 | Javelin recusa o vJoy, se F1/EA WRC voltarem ao escopo | Alta | Baixo por ora | Sem promessa de F1; dispositivo só em sessão; chave de desligar; sem evasão | Só se o escopo mudar |
| R2 | HIDMaestro, se virar plano B, falha com Secure Boot/HVCI ou gera rejeição pela raiz | Média | Médio | Testar só se o vJoy falhar; remoção completa | S1 |
| R13 | Windows da máquina de desenvolvimento difere do dos usuários | Média | Médio | Testar também em Win 10 e Win 11 estável | S1 |
| R5 | Instalação falha na 1ª sessão (firewall, driver, mapeamento) | Alta | Alto | RF-14, RF-15, diagnóstico por sintoma | S8 |
| R6 | Picos de Wi-Fi em roteadores domésticos e operadoras, com isolamento de AP | Média | Médio | Tethering USB, avisos, failsafe | S4 |
| R7 | Aparelhos básicos com sensores ruins | Média | Médio | RF-06, modo degradado honesto, lista | S5 |
| R8 | Fadiga do braço e sensação sem FFB limitam a sessão | Alta | Médio | Apoio de cotovelo, arco ajustável, hápticos | S6 |
| R9 | Mudanças de plataforma: Android 17 rede local, verificação global em 2027 | Alta | Baixo-médio | RNF-09; registro e guarda de chave | S7 |
| R10 | Mercado sem receita, com rivais grátis e um novo entrante | Alta | Médio | Validar disposição a pagar antes de telemetria | S9 |
| R11 | Marca (o precedente do ViGEm) ou uso de marcas de jogos | Baixa-média | Médio | Nome distinto, busca INPI/WIPO, texto "funciona com" sem logos | S10 |
| R12 | Conta de teste punida por anti-cheat em ACC, LMU ou iRacing | Baixa | Médio | Começar pelos jogos-alvo; testar offline, em conta descartável e sem burlar nada | S2 |

No plano legal, com caráter informativo e sem valor de aconselhamento: a Lei 9.609/98 protege software sem registro ([WIPO](https://www.wipo.int/wipolex/en/legislation/details/513)), e comentadores dizem que ela é silenciosa sobre engenharia reversa ([Sedep](https://www.sedep.com.br/artigos/limitacoes-ao-direito-do-autor-copia-privada-e-engenharia-reversa/)), então **não se deve descompilar o app original nem copiar assets, constantes ou UI**: a base é documentação pública e notas de origem do design [E]. O status da marca "JD SimWheel" é desconhecido, pois INPI e WIPO não foram consultados. O caso ViGEm mostra que conflito de marca derruba projeto neste nicho [F] ([Nefarius](https://docs.nefarius.at/projects/ViGEm/End-of-Life/)). Para LGPD, IP tratado como dado pessoal e registro de operações aparecem em fontes secundárias ([cartilha](https://www.pontoisp.com.br/wp-content/uploads/2020/08/LGPD-Provedores-cartilha.pdf), [Art. 37](https://www.dataprivacybr.org/wp-content/uploads/2021/10/O-legitimo-interesse-na-LGPD.pdf)) [R], e manter telemetria local reduz a superfície [E]. O rótulo de privacidade do [VSteer](https://apps.apple.com/app/id6757863011) declara identificadores e dados de uso para rastreio [F], custo provável do modelo com anúncios [E].

## Custos: entrada barata, e o certificado EV só é necessário para driver próprio

| Item | Valor | Rótulo | Observação |
|---|---|---|---|
| Google Play ou ADC Full Distribution | USD 25 | [F], [FAQ](https://developer.android.com/developer-verification/guides/faq?hl=en) | Se é taxa única não foi confirmado |
| Certificado OV | USD 150-300/ano na Microsoft; Sectigo cerca de 220-322 | [F] e [R], [Microsoft](https://learn.microsoft.com/en-us/windows/apps/package-and-deploy/code-signing-options), [revenda](https://sslinsights.com/digicert-vs-sectigo-code-signing/) | Chave em HSM/token; muitas ACs não emitem a pessoa física sem documentos |
| Azure Artifact Signing | Cerca de USD 9,99/mês | [F] | **Brasil excluído** |
| Store MSIX | Grátis | [F] | Sem driver de kernel [R, terceiro]; instalador separado |
| SignPath Foundation | Grátis | [F] | Só open source qualificado |
| Marca no INPI | Valor atual não extraído | [F\*], [tabela](https://www.gov.br/inpi/pt-br/servicos/custos-e-pagamento/NovaTabeladeRetribuiesINPI_MARCAS_Final_20_dez_25.pdf) | Tabela de 20/dez/2025, com descontos; os R$ 355/142 de 2019 estão obsoletos |
| EV para driver próprio | USD 349-669/ano, SSL.com a USD 359 para autônomos | [R], [My-SSL](https://my-ssl.com/learn/code-signing-certificate-price), [SSL.com](https://www.ssl.com/products/software-integrity/code-signing/ev-sole-proprietor/) | Evitar: SmartScreen trata EV igual a OV desde 2024 [F] |

**Total calculado [E]: USD 245-347 no primeiro ano**, usando OV Sectigo e sem taxas de marca, aparelhos de teste, jogos e tokens, que não foram orçados. Reputação do SmartScreen pode levar semanas e centenas de instalações limpas ([Microsoft](https://learn.microsoft.com/en-us/windows/apps/package-and-deploy/smartscreen-reputation)) [F]. Preços de revenda são indicativos. O modelo sugerido, por julgamento [E], é direção gratuita sem limite de tempo mais desbloqueio único com telemetria, painel e hápticos, perto do teto observado de USD 9,99, com preço local em reais.

## Roteiro: cinco fases e um portão antes de escrever produto

| Fase | Escopo | ESTIMATIVA de esforço (solo, tempo parcial) | Portão de saída |
|---|---|---|---|
| 0. Spikes | S1, S2 e S4-S10 | 2-4 semanas | Versão do vJoy escolhida; jogos-alvo confirmados |
| 1. MVP Android | RF-01 a RF-17, exceto RF-11, e RNF de MVP | 6-10 semanas | Da instalação à primeira curva em ETS2 e AC |
| 2. Beta | Teste fechado no Play, 5-8 jogos, instalador, reputação | 4-8 semanas | Metas de latência e confiabilidade medidas e publicadas |
| 3. Telemetria | Fluxo normalizado, BeamNG, AC e SCS, hápticos, painel | 6-10 semanas | Disposição a pagar validada |
| 4. Extensões | Outros simuladores, SimHub, cobrança | 4-8 semanas | Conforme demanda |

As estimativas são do autor da nota, sem fonte, e nenhum projeto comparável publica dados de esforço. A soma das fases 0-3 é de **18-32 semanas, cerca de 4 a 7,5 meses** [E]; a fase 4 soma mais 4-8 semanas. Essas faixas não foram recalculadas após as decisões de 7 de novembro: retirar o segundo backend reduz o trabalho, aprender Kotlin e Compose aumenta, e o efeito líquido não foi estimado. Na distribuição Android, o Brasil está entre os primeiros países da verificação de desenvolvedor, ativa desde 30 de setembro de 2026 para instalações pelo Play e por lojas participantes ([guia](https://developer.android.com/developer-verification/guides)) [F]. Publicar com Play App Signing dá registro automático, e perder a chave de assinatura impede registrar o pacote ([FAQ](https://developer.android.com/developer-verification/guides/faq?hl=en)) [F], então a chave precisa de cópia segura. Desde 31 de agosto de 2026, apps novos e atualizações no Play precisam mirar a API 36 ([Play](https://developer.android.com/google/play/requirements/target-sdk)) [F]. No PC o caminho é um instalador assinado que encadeia drivers com consentimento, ficando a Microsoft Store para depois.

## Primeiros spikes: experimentos descartáveis, não compromissos de implementação

O objetivo destes spikes é derrubar hipóteses baratas antes de haver produto. Código aqui é descartável, e um resultado negativo é um resultado válido. Durações são ESTIMATIVAS.

| ID | Pergunta | Método | Critério de aprovação proposto | Duração | Decide |
|---|---|---|---|---|---|
| S1 | Qual versão do vJoy funciona em Windows de varejo? | vJoy 2.1.9.1 e 2.2.2.0 em Win 10 e 11 Home/Pro limpos e na máquina de desenvolvimento, com Secure Boot, Integridade de Memória e Smart App Control ligados; HIDMaestro só se ambas falharem | Instala, aparece em `joy.cpl`, remove sem resíduo e sem reboot não anunciado | 2-4 dias | RF-12, versão fixada, plano B |
| S2 | Os jogos-alvo aceitam o vJoy? | Com alimentador sintético, sem burlar nada, em modo offline: ETS2, ATS, AC e BeamNG; depois ACC e LMU, em conta descartável | Jogo abre, vincula e responde sem aviso; título que falhar sai da lista | 1-2 dias | RF-13, lista pública |
| S4 | Qual a cauda real da rede? | UDP de 60 bytes a 100, 120 e 200 Hz, 30 min por configuração, 3+ celulares, lock ligado/desligado; 2,4/5 GHz, USB, hotspot | p50/p95/p99/p99,9, maior lacuna; meta do RNF-01/02 | 2-3 dias | Transporte, failsafe |
| S5 | Quanto dura o trecho do celular? | Log de idade da amostra, 10 s por sensor, rotação em servo ou câmera rápida, com foto-diodo ao menos uma vez; substituir o orçamento | Tabela medida por aparelho | 3-5 dias | RNF-01, parâmetros do filtro |
| S6 | Como as pessoas seguram? | Sessão de 20 min em ETS2 com log de inclinação e erro; háptico ligado/desligado para ruído no IMU | Acoplamento e fadiga em níveis aceitáveis; ruído do IMU com háptico medido | 2-3 dias | RF-03, RF-04, hápticos |
| S7 | Permissões novas quebram algo? | Android 17 com alvo 37; lock em hotspot, tethering e roteador sem internet; loopback e tether sob a proteção de rede local | Mapa do que funciona e mensagem para cada falha | 1-2 dias | RNF-09 |
| S8 | Como os rivais resolvem instalação? | Instalar Sim Wheel Controller, Steering App, Touch Racer; inspecionar drivers instalados, sem descompilar apps | Tabela de atrito e de driver usado | 1-2 dias | RF-14 |
| S9 | Existe demanda paga? | Ler manualmente Reddit (r/simracing, r/trucksim), avaliações do Play, fóruns SCS; página de interesse em PT-BR | Sinal de disposição a pagar | 2-3 dias | Fase 3, modelo |
| S10 | Questões de elegibilidade e marca | Consultar INPI/WIPO; perguntar a SSL.com e Certum sobre CPF/CNPJ/MEI | Respostas escritas | 1-2 dias | Nome, certificado |

## Divergências entre as notas e como foram tratadas

As duas primeiras linhas foram conferidas direto nas fontes primárias ao redigir, em 6 de novembro de 2026.

| Tema | O que as notas dizem | Tratamento | Estado |
|---|---|---|---|
| Datas do ViGEmBus | "Nov 2023" e "Dez 2023" | São eventos diferentes: repositório arquivado e release v1.22.0 em **2/nov/2023** ([GitHub](https://github.com/nefarius/ViGEmBus)); transição jul-dez/2023 e transferência do domínio em **21-26/dez/2023** ([EOL](https://docs.nefarius.at/projects/ViGEm/End-of-Life/)) | Resolvido |
| Verificação Android | Uma nota sugere fluxo avançado para não registrados; outra, sideload não exigido | O prazo de 30/set/2026 vale só para lojas participantes; sideload não é afetado ainda; fluxo avançado previsto para ago/2026 para instalar de não verificados; global em 2027; ADB isento ([FAQ](https://developer.android.com/developer-verification/guides/faq?hl=en)) | Resolvido; caráter único da taxa de USD 25 em aberto |
| Sucessor do ViGEmBus | Nota de drivers cita VirtualPad como "sucessor nomeado", mas o EOL não nomeia nenhum | Sem sucessor oficial; VirtualPad é produto comercial à parte ([Nefarius](https://docs.nefarius.at/projects/VirtualPad/)) | Resolvido; licença não lida |
| Driver recomendado | Drivers: ViGEmBus padrão + HIDMaestro. Jogos: vJoy padrão em sims + XInput | Decidido em 7/nov: só vJoy, sem backend XInput; HIDMaestro fica como plano B | Resolvido por decisão de escopo |
| Versão do vJoy | 2.2.2.0 "funciona" vs falhas até abr/2026; 2.1.9.1 ok em 25H2 | Candidato inicial 2.1.9.1; nenhuma versão validada | **Em aberto** |
| Atestado de drivers | Doc o rotula como teste; tabela ainda o aceita; post de confiança silencia | Tratado como risco de negócio | **Em aberto** |
| Latência do celular | 13,8 ms vs ≈ 18 ms só no celular | Recalculado em ≈ 23 ms bruto [E] | Reconciliado; falta bancada |
| Meta de 50 ms | Fim a fim vs total estimado de 55 ms | Lida como atraso adicionado | Reconciliado |
| Taxa de envio | 120 Hz padrão vs envio a cada amostra | A cada amostra, até 200 Hz, recuo a 100 Hz | Proposta; S4 |
| Papel do Bluetooth | Modo secundário vs nicho | Posterior e experimental | Reconciliado |
| Acima de 200 Hz | Docs: `SecurityException`; AOSP: limita, exceção em debug | Irrelevante ficando em ≤ 200 Hz | Em aberto, sem impacto |
| Nome do app rival | Sim Wheel Controller vs SimWheel Connect | Mesmo pacote `com.ik.simwheel`; sem ligação com o JD SimWheel | Provável; nome oficial a checar |
| Busca por "JD SimWheel" | Nota de jogos: nada exato; nota do app: AppBrain, XDA, APKCombo | Vale a nota do app; marca segue não pesquisada | Resolvido em parte |
| Rotação do VSteer | 4000° vs 2000° | Provavelmente ±2000 = 4000 de batente a batente | Inferência |
| "1-3 ms em USB" | Atribuído ao tmphonepad e a um README não identificado | Alegação, de origem incerta; não usada | Em aberto |
| Crash do vJoy em FH5 | Issues #57 e #60 | Citada a #60; não conferida a página | Em aberto, menor |
| Faixa de vJoy no iRacing | irFFB fala em 0-65535; SDK em 0-32767 | Pode ser escala da API; confirmar | Em aberto, menor |
| Preço de EV | USD 349-669; "400+"; revendas 290-685 | Faixa indicativa; não necessário no plano | Sem impacto |
| Formato F1 UDP | 2026 padrão vs 2024 recomendado | Ler o formato no cabeçalho | Resolvido no design |
| Faixa da Forza Motorsport | Faixa 1 vs célula vJoy sem relato | Rebaixada para Faixa 2 | Resolvido |
| Esforço total | "4,5-7,5 meses" vs soma de 18-32 semanas | Cerca de 4-7,5 meses | Corrigido |
| Foreground service | `connectedDevice` "usar" vs "só reserva" | Atividade em primeiro plano; serviço só como reserva | Resolvido |
| Versão e domínio do original | 4.2 vs 3.0; droidcortex.com vs .eu | 4.2 é a última listada; .eu não resolveu em nov/2026 | Resolvido em parte |

Permanecem sem resposta nesta pesquisa: o motivo da remoção do JD SimWheel; se um CPF brasileiro obtém certificado e registro de hardware; o comportamento do vJoy nos jogos-alvo em builds atuais; a distribuição real de aparelhos da audiência; e qualquer número de retenção ou receita do mercado.

## Conclusão

A pesquisa muda a pergunta. Em 2015 o desafio era fazer um celular funcionar como joystick; em 2026 isso virou commodity, como mostra a leva de repositórios novos de 2025-2026. O que continua difícil está fora do app: uma cadeia de confiança no Windows cada vez mais fechada a drivers de terceiros, anti-cheats de kernel que veem controles virtuais como suspeitos, e a primeira sessão do usuário, em que firewall, driver e mapeamento precisam dar certo juntos. O motivo do fim do JD SimWheel é desconhecido, mas os relatos apontam para a dependência de driver, não para o sensor [E], então o ativo a construir é a camada de compatibilidade e a honestidade sobre ela: lista testada, latência medida com método e instalação que se autodiagnostica.

A consequência prática é sequencial. Dois experimentos de poucos dias, S1 e S2, valem mais que semanas de código, pois confirmam qual versão do vJoy funciona e se os jogos-alvo a aceitam. Com F1 fora de foco, o produto passa a ser "o melhor volante de celular para simuladores de caminhão e de corrida abertos". Esse produto continua viável e talvez seja o mais defensável: é onde o vJoy tem histórico, onde RojX e simSteer só avançam em parte, e onde um desenvolvedor brasileiro pode oferecer PT-BR, que nenhum concorrente verificado anuncia (nem todas as listagens foram checadas, e a demanda ainda precisa ser medida).
