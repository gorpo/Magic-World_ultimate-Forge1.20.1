# Magic Wolrd Light

Mod Forge 1.20.1 separado do Magic World principal.

## Uso

- Item: `magic wolrd light`
- ID: `magicworldlight:magic_world_light`
- Quando o item esta no inventario, o mod cria luz vanilla temporaria no bloco dos pes e da cabeca do jogador.
- Quando o item sai do inventario, o jogador troca de dimensao, morre, sai do mundo ou o servidor fecha, as luzes temporarias rastreadas sao removidas.

## Receita

```text
 G
GTG
 G
```

- `G`: glowstone dust
- `T`: torch

## Build

Na raiz do repositorio:

```powershell
.\gradlew.bat -p magic-world-light build
```

O build gera o JAR em `magic-world-light/build/libs/` e copia uma copia para `mods/`.
