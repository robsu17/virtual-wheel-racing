# Como gerar uma versão (guia do mantenedor)

## A chave de assinatura do Android

O APK de release é assinado com uma chave que **fica fora do repositório**, na pasta do mantenedor:

```
%USERPROFILE%\VirtualWheelRacing-assinatura\
├─ virtual-wheel-racing-release.jks   a chave (PKCS12, RSA 4096, válida até 2054)
└─ keystore.properties                caminho, alias e senha da chave
```

Impressão digital do certificado (SHA-256), para conferir APKs publicados:

```
18:B4:9A:79:05:75:7C:35:78:79:E9:7F:48:62:B8:92:CE:2A:38:EB:F1:49:85:F1:E8:9D:D5:67:E1:CD:6A:67
```

**Guarde uma cópia dessa pasta em dois lugares seguros**, por exemplo um pendrive e um cofre de senhas ou nuvem criptografada. Sem a chave e a senha, nenhuma atualização do app instala por cima das versões já distribuídas: as pessoas teriam de desinstalar e parear de novo. Nunca envie esses arquivos para o GitHub; o `.gitignore` já bloqueia `*.jks` e `keystore.properties`.

O Gradle procura o `keystore.properties` nesta ordem: a propriedade `-Pvwr.signing=<caminho>`, a variável de ambiente `VWR_SIGNING_PROPERTIES` e a pasta acima. Sem o arquivo, o build de release sai **sem assinatura**, o que basta para quem contribui e para a CI.

## Passo a passo de uma versão

1. Suba a versão em dois lugares:
   - `android/app/build.gradle.kts`: `versionCode` (sempre +1) e `versionName`;
   - `desktop/app/build.gradle.kts`: `packageVersion`.
2. Gere os arquivos:
   ```bash
   gradlew.bat test :android:app:assembleRelease :desktop:app:packageMsi
   ```
3. Confira a assinatura do APK (a impressão digital deve ser a de cima):
   ```bash
   apksigner verify --print-certs android\app\build\outputs\apk\release\app-release.apk
   ```
4. Copie para `build\release` com os nomes `virtual-wheel-racing-<versão>-android.apk` e `virtual-wheel-racing-<versão>-windows.msi`, e gere o `SHA256SUMS.txt`.
5. Crie a tag `v<versão>` no commit da versão, envie, e publique o Release no GitHub com os três arquivos.

## Play Store, quando chegar a hora

Na primeira publicação, o Play Console oferece o **Play App Signing**. Escolha usar **a chave existente** (exportada com a ferramenta PEPK que o próprio Console indica). Assim o APK do GitHub e o da Play Store têm a mesma assinatura e um atualiza o outro. Se o Google gerar uma chave nova, as duas versões passam a ser incompatíveis entre si.
