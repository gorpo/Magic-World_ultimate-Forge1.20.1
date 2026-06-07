# Magic World - candidatos de mods de construcao

Pesquisa feita em 2026-05-24. Nenhuma dependencia foi adicionada ao projeto.

## Candidatos principais

| Mod | Loader/plataforma observado | Papel | Status para Magic World |
| --- | --- | --- | --- |
| Effortless Building | Modrinth lista Fabric, Forge e NeoForge; compatibilidade inclui 26.1.2. | Mod de construcao com espelhos, arrays, modos de construcao e randomizer. | Bom candidato opcional. Precisa confirmar se o servidor/amigo tambem instalara, pois nao e apenas client-side. |
| WorldEdit | Build 7.3.16 publicado para NeoForge/Fabric em MC 1.21.6-1.21.8. | Ferramenta pesada de edicao, selecao e schematics. | Bom candidato opcional para criativo/dev, nao obrigatorio para jogadores comuns. |
| Starter Structure | Modrinth lista Fabric, Forge, NeoForge e Quilt; compatibilidade inclui 26.1.x. | Gera estrutura inicial por schematics/nbt, util para spawn e portal inicial. | Candidato forte para futuro portal/spawn pronto, opcional. |

## Candidatos com cuidado

| Mod | Motivo do cuidado | Decisao recomendada |
| --- | --- | --- |
| Axiom | Modrinth mostra plataforma Fabric. Pode exigir Sinytra Connector em NeoForge, aumentando risco de crash. | Nao adicionar agora. Avaliar so em perfil separado de construcao. |
| Effortless Structure | Projeto similar/relacionado a construcao por estruturas, mas precisa confirmar versao exata para 26.1.2 antes de usar. | Manter como pesquisa secundaria. |

## Separacao recomendada

| Tipo | Mods |
| --- | --- |
| Obrigatorio | Nenhum. O Magic World deve continuar rodando sem mods de construcao externos. |
| Opcional para jogadores | Effortless Building, se confirmado no mesmo loader/versao do pacote final. |
| Opcional para criadores/dev | WorldEdit, Starter Structure. |
| Evitar no pacote principal por enquanto | Axiom via Connector, por risco adicional e por nao ser NeoForge nativo. |

## Fontes consultadas

- Effortless Building: https://modrinth.com/mod/effortless-building
- WorldEdit 7.3.16 NeoForge/Fabric: https://modrinth.com/plugin/worldedit/version/R846T4GX
- Axiom: https://modrinth.com/mod/axiom/versions
- Starter Structure: https://modrinth.com/mod/starter-structure
