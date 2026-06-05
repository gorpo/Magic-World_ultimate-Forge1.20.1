# Sistema De Port - Forge 1.20.1

Este diretorio contem o sistema para portar Magic World Ultimate de NeoForge 26.1.2 para Minecraft 1.20.1 Forge.

O port deve acontecer em workspace separado. A versao final NeoForge continua preservada neste repositorio.

## Comandos

Gerar workspace Forge 1.20.1:

```powershell
.\scripts\new-forge-1201-port.ps1
```

Gerar em caminho especifico:

```powershell
.\scripts\new-forge-1201-port.ps1 -DestinationRoot "C:\Users\guilh\Desktop\MinecraftProjects\Port_1.20.1-Forge_MagicWorld-v1.3"
```

Analisar pontos bloqueantes do port:

```powershell
.\scripts\analyze-forge-1201-port.ps1
```

Analisar o workspace gerado:

```powershell
.\scripts\analyze-forge-1201-port.ps1 -SourceRoot "C:\Users\guilh\Desktop\MinecraftProjects\Port_1.20.1-Forge_MagicWorld-v1.3"
```

Testar compilacao do workspace gerado:

```powershell
.\scripts\test-forge-1201-port.ps1
```

## O Que O Gerador Faz

- Cria um workspace Forge 1.20.1 separado.
- Copia `src`, `gradle`, `gradlew`, `gradlew.bat`, `settings.gradle` e docs essenciais.
- Troca Gradle NeoForge por ForgeGradle.
- Troca metadados NeoForge por `mods.toml` Forge.
- Ajusta `gradle.properties` para Minecraft 1.20.1, Forge 47.x e Java 17.
- Aplica substituicoes mecanicas seguras em Java.
- Duplica texturas de armadura para o caminho antigo esperado no 1.20.1.
- Gera `PORTING_STATUS.md` dentro do workspace.

## O Que Continua Manual

- Registro de itens/entidades: `DeferredItem`, `DeferredHolder`, `registerSimpleItem` e APIs 26.1.2 precisam virar Forge 1.20.1.
- Armadura Draconic Aether: API de `ArmorMaterial`/`ArmorType` mudou.
- Networking: payloads NeoForge 26.1.2 precisam virar `SimpleChannel` Forge 1.20.1.
- Telas/render: `GuiGraphicsExtractor` nao existe em 1.20.1.
- Spawn: `EntitySpawnReason` nao existe em 1.20.1; usar `MobSpawnType` ou criacao manual.
- Mixins de telas modernas precisam ser reescritos para nomes/metodos 1.20.1.
- Mods all-in-one precisam ser trocados por equivalentes 1.20.1 Forge.

## Ordem Recomendada

1. Gerar workspace com `new-forge-1201-port.ps1`.
2. Rodar `test-forge-1201-port.ps1` para obter a primeira lista real de erros.
3. Corrigir primeiro `MagicWorld.java`, `Config.java` e `MagicWorldNetwork.java`.
4. Corrigir menus/telas cliente.
5. Corrigir `StarterPortalEvents.java` e estruturas importadas.
6. Corrigir entidades premium e dragao.
7. Validar em jogo com jar normal.
8. Somente depois montar pacote/all-in-one Forge 1.20.1.
