# Codex Handoff - Inicio Port Neoforge

Atualizado em 2026-06-05.

## Pedido atual

Refazer o port para Forge 1.20.1 usando este repositorio como base local e o projeto NeoForge 26 ao lado como referencia.

- Base Forge: `C:\Users\guilh\Desktop\MinecraftProjects\Magic-World_ultimate-Forge1.20.1`
- Origem NeoForge: `C:\Users\guilh\Desktop\MinecraftProjects\neoforge-26.1.2-mdk`
- Remoto: `https://github.com/gorpo/Magic-World_ultimate-Forge1.20.1.git`
- Branch de trabalho: `Inicio-Port-Neoforge`

Observacao: `Inicio Port Neoforge` com espacos nao e nome valido de branch Git. Usar sempre `Inicio-Port-Neoforge`.

## Regra de retomada obrigatoria

Se a sessao cair ou outro agente continuar:

1. Ler este arquivo primeiro.
2. Rodar `git status --short --branch`.
3. Confirmar branch `Inicio-Port-Neoforge`.
4. Ler `docs/WIKI.md`.
5. Usar `docs/neoforge-reference/` apenas como referencia do NeoForge.
6. Nao portar nada fora do escopo atual sem pedido novo do usuario.
7. Atualizar este handoff ao final de cada bloco de trabalho.

## Escopo autorizado nesta etapa

Portar somente:

- inicio do jogo;
- comportamento inicial dos menus ao abrir;
- menus iniciais da criacao de mundo;
- planos de fundo estaticos das telas;
- tela de loading inicial.

Nao portar nesta etapa:

- gameplay completo do NeoForge;
- entidades custom novas;
- resource/shader installers;
- estruturas/mundo inicial alem do minimo ja existente;
- sistemas extras que aparecerem na wiki NeoForge.

## Feito nesta sessao

- Handoff antigo apagado e substituido por este arquivo novo.
- Wiki local recriada em `docs/WIKI.md`.
- Confirmado que nao havia pasta wiki local anterior.
- Confirmado projeto NeoForge de origem em `neoforge-26.1.2-mdk`.
- Git local inicializado neste projeto.
- Remoto `origin` configurado para `https://github.com/gorpo/Magic-World_ultimate-Forge1.20.1.git`.
- Branch local criado: `Inicio-Port-Neoforge`.
- Documentacao NeoForge copiada como referencia para `docs/neoforge-reference/`:
  - `MAGIC_WORLD_WIKI.md`
  - `README_NEOFORGE.md`
  - `CODEX_HANDOFF_NEOFORGE.md`
  - `building_mod_candidates.md`
  - `screenshots_to_update.md`
  - `porting-forge-1.20.1/README.md`
- `.gitignore` ajustado para nao versionar pastas locais/pesadas como `tmp`, `pacote_distribuivel`, `installer`, `screenshots` e `run`.

## Observacoes importantes

- `tmp` tem conteudo local grande e pode ajudar no port, mas nao deve ir para GitHub.
- `pacote_distribuivel` tem cerca de 1.6 GB e nao deve ir para GitHub.
- A wiki NeoForge e referencia de destino, nao estado atual do Forge.
- A base Forge atual ainda usa `mod_id=examplemod` e pacote `com.example.examplemod`.

## Proximo passo imediato

Portar somente as telas/menus iniciais e backgrounds:

1. Copiar assets estaticos de GUI do NeoForge para `src/main/resources/assets/examplemod/textures/gui/`.
2. Criar/adaptar classes Forge 1.20.1 para:
   - tela de titulo customizada;
   - botoes/tema visual;
   - painel Magic World na criacao de mundo;
   - loading inicial com fechamento automatico apos concluir.
3. Rodar `./gradlew.bat build`.
4. Atualizar este handoff com resultado.
5. Commitar e enviar branch `Inicio-Port-Neoforge` ao GitHub.
