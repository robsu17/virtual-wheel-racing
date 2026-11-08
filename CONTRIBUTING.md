# Como contribuir

Obrigado por querer ajudar o Virtual Wheel Racing! Toda contribuição conta, e algumas não exigem escrever código.

## Formas de ajudar

- **Testar em jogos:** é a contribuição mais valiosa agora. Teste em um jogo e abra uma issue com o modelo **Compatibilidade de jogo**, tenha funcionado ou não.
- **Testar em celulares:** rode a **Calibração** do app e informe o modelo do celular, a versão do Android e o resultado (leituras por segundo e modo).
- **Relatar bugs:** use o modelo **Relatar um problema**, com os passos para reproduzir.
- **Sugerir melhorias:** use o modelo **Sugestão**.
- **Código e documentação:** veja abaixo.

## Preparando o ambiente

Os pré-requisitos e os comandos de compilação estão no [README em português](README.pt-BR.md#pré-requisitos-para-desenvolver). Antes de começar uma mudança grande, abra uma issue para conversar sobre a ideia: evita trabalho perdido.

## Como o código é organizado

Leia o [docs/arquitetura.md](docs/arquitetura.md). Em resumo:

- **Regras puras ficam em `shared/domain`** e não conhecem Android, rede nem vJoy.
- **O formato do pacote fica em `shared/protocol`** e é o mesmo nos dois lados. Mudou o formato? Atualize os testes de "pacote de ouro".
- **Só `desktop/vjoy` fala com o vJoy.** Outro driver entra como outra implementação de `VirtualWheel`, passando pelo `VirtualWheelContract`.
- **O caminho de tempo real não cria objetos por pacote** e não usa coroutines. Veja a seção 7 da arquitetura antes de mexer em `DrivingSession` ou em `ReceiverService`.

## Antes de abrir um pull request

1. Rode os testes:
   ```bash
   gradlew.bat test :android:engine:testDebugUnitTest
   ```
2. Se mexeu no vJoy, rode também o contrato contra o driver real:
   ```bash
   gradlew.bat :desktop:vjoy:test -Pvwr.vjoy=true
   ```
3. Confira se o app Android e o programa do Windows compilam:
   ```bash
   gradlew.bat :android:app:assembleDebug :desktop:app:compileKotlin
   ```
4. Descreva no pull request o que mudou, por quê e como você testou (jogo, celular, versão do Windows).

## Mensagens de commit

O projeto segue o padrão [Conventional Commits](https://www.conventionalcommits.org/pt-br/v1.0.0/), com a **descrição em inglês**:

```
tipo(escopo opcional): descrição curta no imperativo, em inglês

Corpo opcional explicando o porquê.
```

| Tipo | Quando usar | Exemplo |
|---|---|---|
| `feat` | Função nova para quem usa | `feat(android): add vibration on kerbs` |
| `fix` | Correção de bug | `fix(desktop): quote program path in firewall rule` |
| `refactor` | Mudança de código sem mudar o comportamento | `refactor(protocol): extract packet header writer` |
| `perf` | Melhoria de desempenho | `perf(engine): avoid allocation when sending state` |
| `test` | Testes novos ou corrigidos | `test(receiver): cover heartbeat timeout` |
| `docs` | Só documentação | `docs: add ETS2 setup guide` |
| `build` | Gradle, dependências, instalador | `build: bump Compose Multiplatform to 1.9.1` |
| `ci` | GitHub Actions | `ci: cache Gradle between runs` |
| `chore` | Manutenção que não entra nas outras | `chore: update third-party license list` |

**Escopos usados:** `android`, `engine`, `desktop`, `receiver`, `vjoy`, `protocol`, `domain`, `pairing`, `ui`, `installer`.

**Mudança que quebra compatibilidade** (por exemplo, um formato de pacote ou de QR que versões antigas não entendem): coloque `!` depois do tipo, como em `feat(protocol)!: add packet version 2`, e explique no corpo com `BREAKING CHANGE:`.

Os commits anteriores a 8/nov/2026 não seguem esse padrão; ele vale daqui para frente.

## Estilo

- Kotlin no estilo oficial (`kotlin.code.style=official`).
- Textos de interface em português do Brasil.
- Comentários explicam o porquê, não o que o código já diz.
- Uma mudança por pull request, com testes quando houver regra nova.

## Licença das contribuições

O projeto é distribuído sob a [GPL-3.0](LICENSE). Ao enviar uma contribuição, você concorda que ela seja distribuída sob a mesma licença.

## Conduta

Este projeto segue o [código de conduta](CODE_OF_CONDUCT.md). Seja gentil e respeitoso.
