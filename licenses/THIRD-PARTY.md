# Componentes de terceiros

O Virtual Wheel Racing é distribuído sob a GPL-3.0 (arquivo `LICENSE` na raiz). Estes são os componentes de terceiros que vão junto, cada um com a sua licença. Antes de cada versão, confira cada licença na fonte original.

| Componente | Onde entra | Licença | Fonte |
|---|---|---|---|
| vJoy 2.1.9.1 (instalador `vJoySetup.exe`) | Instalador do Windows | MIT (`vJoy-MIT.txt`, que também vai ao lado do instalador) | https://github.com/jshafer817/vJoy — assinado por "On-site Dental Systems (Justin Shafer)"; SHA-256 `F103CED4E7FF7CCB49C8415A542C56768ED4DA4FEA252B8F4FFDAC343074654A` |
| Barlow Semi Condensed | Android e Windows | SIL Open Font License 1.1 | `BarlowSemiCondensed-OFL.txt` |
| Inter | Android | SIL Open Font License 1.1 | `Inter-OFL.txt` |
| ZXing core | Windows (gera o QR) | Apache 2.0 | https://github.com/zxing/zxing |
| JNA | Windows (chama a vJoyInterface.dll) | Apache 2.0 ou LGPL 2.1 (à escolha) | https://github.com/java-native-access/jna |
| Google code scanner (Play Services) | Android (lê o QR) | Termos do Google Play Services | https://developers.google.com/ml-kit/terms |
| Compose Multiplatform, Jetpack Compose, Kotlin | Android e Windows | Apache 2.0 | https://github.com/JetBrains/compose-multiplatform |

## Atenção: leitor de QR do Google

O leitor de QR do app Android vem do Google Play Services, que não é software livre. Muitos apps GPL usam o Play Services, mas lojas como o F-Droid recusam apps que dependem dele, e há discussão sobre como ele se encaixa na GPL. Trocar por um leitor livre (por exemplo, ZXing com CameraX) resolve as duas coisas e está na lista de melhorias.
