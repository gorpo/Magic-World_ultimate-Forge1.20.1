# Codex Handoff - MagicWorld Final

Data: 2026-06-04
Branch de trabalho: V3.0
Estado: projeto finalizado para entrega.

## Decisoes Fixas

- README deve ficar ludico, simples e voltado ao jogador; nao colocar build, arquitetura ou texto tecnico nele.
- Wiki tecnica fica em `docs/MAGIC_WORLD_WIKI.md` e deve concentrar arquivos, funcoes e regras de manutencao.
- Nao reintroduzir o sistema antigo de habitantes customizados.
- Usar villagers vanilla para moradores/trabalhadores.
- Evitar scans grandes em tick e rotinas pesadas para saves antigos.
- Fluxo principal assume mapa novo.
- Loading MagicWorld usa fluxo vanilla para evitar tela preta com Iris/Sodium.
- Painel MagicWorld na criacao de mundo fecha quando aba vanilla retoma os widgets.

## Mundo E Estruturas

- Praca compacta dos portais deve continuar relativa a base em `base.offset(-24, 0, 48)`.
- Portais funcionais devem manter visual real: Nether com obsidian/nether portal, End com frames/end portal, Gateway com bedrock/end gateway.
- Em outra dimensao, o jogador deve nascer 4 blocos ao sul do portal de retorno salvo.
- Criar apenas um portal de retorno por tipo/dimensao por jogador, salvo em persistent data com prefixo `MagicWorldFunctionalReturnPortal`.
- Castelo nao deve ter retangulo de agua/lava em cima. Canais/corredores baixos ficam secos.
- Casas verdes devem manter janelas coloridas, floreiras, varanda e decoracao magica.
- `Ferreiro de Ferramentas` e `Cozinheiro do Salao` devem ficar no chao, nao em pavilhoes flutuantes.

## Entrega

- Compilar antes de commit/release.
- Gerar jar normal e all-in-one em `build/libs`.
- Atualizar `pacote_distribuivel/.minecraft/mods` com o jar all-in-one final para teste local.
- `installer/MagicWorldInstaller.exe` e `pacote_distribuivel` sao ignorados pelo Git; publicar como assets do release quando autenticado.
