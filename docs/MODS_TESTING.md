# Mods para teste no ambiente de desenvolvimento

## Teste rapido

Coloque novos mods Forge 1.20.1 para teste em:

```text
run/mods/
```

Ao executar `runClient` pelo IntelliJ ou `.\gradlew.bat runClient`, o Forge usa `run/` como a pasta do jogo e tenta carregar automaticamente os JARs presentes em `run/mods/`.

Essa pasta funciona para mods simples, mas alguns mods com mixins falham porque o ambiente de desenvolvimento usa nomes de codigo diferentes do jogo publicado.

## Mods complexos/remapeados

Se um mod em `run/mods/` falhar com erros como `MixinApplyError`, `InvalidMixinException`, `@Shadow` ou campo/metodo nao encontrado, mova o JAR para:

```text
run/dev-mods/
```

O `build.gradle` detecta automaticamente todos os JARs dessa pasta e pede ao ForgeGradle para remapea-los antes do `runClient`.
O nome precisa terminar em `-<versao>.jar`, como `meu-mod-1.2.3.jar`.

Atualmente, estes mods precisam ficar em `run/dev-mods/`:

- Distant Horizons;
- Embeddium;
- Oculus.

## Desativar temporariamente

Para impedir que um mod seja carregado sem apagar o arquivo, mova o JAR para:

```text
run/mods-disabled/
```

## Pastas antigas

- `run/dev-mods/`: mods que precisam de remapeamento no ambiente de desenvolvimento.
- `run/dev-mods-disabled/`: arquivos antigos desativados; nao sao carregados.
- `mods/` na raiz do projeto: arquivo local antigo; nao e carregado pelo `runClient`.

## Regras

- Use somente mods para Minecraft Forge 1.20.1.
- Verifique dependencias obrigatorias do mod.
- Tente primeiro em `run/mods/`; use `run/dev-mods/` se houver erro de mixin/remapeamento.
- Adicione poucos mods por vez para identificar conflitos.
- Depois de adicionar ou remover um JAR, encerre e abra novamente o `runClient`.
- Consulte `run/logs/latest.log` para confirmar carregamento ou diagnosticar erros.
