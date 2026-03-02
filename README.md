# RFID Example

Aplicativo Android em Jetpack Compose para leitura e codificação de tags RFID
usando o SDK da Zebra. O projeto conecta a um reader disponível, permite
inventário em tempo real, leitura de bancos de memória e escrita de EPC.

## Funcionalidades
- Conexão/desconexão com reader RFID Zebra.
- Inventário de tags com lista de EPCs em tempo real.
- Tela de detalhes de tag com leitura de MB01–MB04.
- Tela de codificação para escrever novo EPC.
- Logs detalhados no Logcat para acompanhar o fluxo RFID.

## Tecnologias e arquitetura
- Kotlin + Jetpack Compose.
- Camada de controle em `RfidController` para orquestrar o fluxo RFID.
- Integração com hardware via `RfidHardware` e implementação Zebra.
- Estado de UI concentrado em `RfidUiState`.

## Requisitos
- Android Studio atualizado.
- Dispositivo Android físico (Bluetooth/USB OTG conforme reader).
- Reader RFID Zebra compatível.
- SDKs Zebra em `app/libs` (já incluídos no repositório).

## Como executar
1. Abra o projeto no Android Studio.
2. Sincronize o Gradle.
3. Execute em um dispositivo físico.
4. Aceite as permissões de Bluetooth (e localização em versões antigas do Android).

## Como usar
1. Abra o app e toque em **Conectar**.
2. Para inventário, toque em **Inventário** e use **Start Inventory**.
3. Para ler bancos, toque em **Tag detalhes** e aproxime uma tag.
4. Para codificar EPC, toque em **Codificar**, informe o EPC (hex) e aproxime a tag.

## Logs (Logcat)
O app registra mensagens com as tags:
- `RfidController`
- `ZebraRfidHardware`

Exemplo de filtro:

```
adb logcat | grep -E "RfidController|ZebraRfidHardware"
```

## Estrutura de diretórios
- `app/src/main/java/br/com/example/rfid/controller`: regras de negócio e fluxo RFID.
- `app/src/main/java/br/com/example/rfid/model`: modelos de dados (ex.: detalhes da tag).
- `app/src/main/java/br/com/example/rfid/ui`: telas e componentes Compose.
- `app/src/main/java/br/com/example/rfid/zebra`: integração com o SDK Zebra.

## Testes
Execute os testes unitários com:

```
./gradlew test
```

## Notas sobre os AARs da Zebra
Para evitar warnings do Gradle/manifest sobre namespace duplicado e permissões repetidas,
os manifestos internos dos AARs foram ajustados:

- `API3_READER-release-2.0.5.238.aar`: remoção de `<uses-permission android.permission.BLUETOOTH_CONNECT>` duplicado.
- Namespaces únicos para as libs que antes usavam `com.zebra.rfid.api3`:
  - `API3_READER-release-2.0.5.238.aar` → `com.zebra.rfid.api3.reader`
  - `API3_INTERFACE-release-2.0.5.238.aar` → `com.zebra.rfid.api3.intf`
  - `API3_TRANSPORT-release-2.0.5.238.aar` → `com.zebra.rfid.api3.transport`
  - `API3_CMN-release-2.0.5.238.aar` → `com.zebra.rfid.api3.cmn`
  - `API3_LLRP-release-2.0.5.238.aar` → `com.zebra.rfid.api3.llrp`
- `API3_ASCII-release-2.0.5.238.aar` → `com.zebra.rfid.api3.ascii`

Se você substituir as libs Zebra por versões novas, será necessário reaplicar esses ajustes.
Para evitar erros do Android Studio ao baixar sources, os AARs possuem arquivos
`-sources.jar` vazios em `app/libs`. Ao atualizar as libs, recrie esses arquivos.
