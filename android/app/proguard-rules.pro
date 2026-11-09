# Regras do R8 para o build de release.

# Leitor de QR do Google (ML Kit code scanner): ele encontra partes internas por reflexão.
# Sem estas regras, o R8 remove essas partes e o app fecha ao tocar em "Ler código QR"
# (NullPointerException em com.google.android.gms.internal.mlkit_code_scanner).
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_code_scanner.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_barcode.** { *; }
-keep class com.google.android.gms.internal.mlkit_common.** { *; }
-keep class * implements com.google.firebase.components.ComponentRegistrar { <init>(); }
