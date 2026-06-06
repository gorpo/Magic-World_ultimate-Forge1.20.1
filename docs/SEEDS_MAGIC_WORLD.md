# Seeds predefinidas do Magic World

Este documento explica como editar a lista de seeds que aparece na aba `Magic World` da tela de criacao de mundo.

## Como funciona

- O campo `Seed manual` fica vazio e aceita a seed digitada pelo usuario.
- O dropdown com seeds predefinidas sempre comeca com `Selecione a seed`.
- `Selecione a seed` nao aplica nenhuma seed.
- Se uma seed predefinida for selecionada, ela tem prioridade sobre o campo manual.
- Se nenhuma seed predefinida for selecionada, mas o campo manual tiver valor, o mundo usa a seed manual.
- Se os dois campos ficarem vazios/nulos, o Minecraft cria o mundo normalmente com seed aleatoria.

## Onde adicionar novas seeds

Abra:

```text
src/main/java/com/magicworld/client/ClientEvents.java
```

Procure por:

```java
private static final SeedPreset[] MAGIC_SEED_PRESETS
```

A primeira linha deve continuar assim:

```java
new SeedPreset("Selecione a seed", ""),
```

Para adicionar uma nova seed, coloque uma nova linha abaixo dela ou abaixo das seeds existentes:

```java
new SeedPreset("Nome Bonito", "123456789"),
```

Regras:

- Use um nome curto, porque o dropdown precisa caber no botao.
- A seed deve ficar entre aspas, mesmo quando for numerica.
- Seeds repetidas sao permitidas quando o objetivo for mostrar nomes diferentes para o mesmo mundo.
- Depois de editar, rode `./gradlew.bat compileJava --stacktrace`.

## Lista inicial atual

- Magic World: `2048005618087379093`
- Paraiso: `69420070680859076`
- Magnific: `2048005618087379093`
- Biomas Pertos: `8500081009970950196`
- Vale Cerejeira: `6823084440019132920`
- Ilha das Vilas: `2218715947278290213`
- Montanhas Magicas: `460628901`
- Cidade Antiga: `4189766944005904899`
- Bosque e Mansao: `-845619040004837621`
- Cerejeiras Raras: `65434353559200`
