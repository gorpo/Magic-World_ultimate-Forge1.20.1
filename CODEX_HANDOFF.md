# Magic World Handoff - 2026-05-25 08:11 America/Sao_Paulo

Este arquivo foi limpo em 2026-05-25 08:11 para remover instrucoes antigas e evitar confusao. O fluxo atual e somente o abaixo.

## Atualizacao Obrigatoria de Continuidade - 2026-05-25 09:15 America/Sao_Paulo

Pedido mais recente do usuario: manter este `CODEX_HANDOFF.md` sempre atualizado, com data/hora, para outra conta/sessao Codex conseguir continuar se o tempo acabar. A partir daqui, antes de continuar tarefas longas, registrar neste arquivo o estado, o que foi feito, o que esta parcial, onde parou e o proximo passo.

## Atualizacao de Continuidade - 2026-05-25 13:20 America/Sao_Paulo

Pedido novo em execucao:

- Fazer a tela de loading inicial aparecer o mais rapido possivel, antes do jogador ver o personagem parado no vazio.
- Armaduras em pedestais/armor stands devem ficar somente dentro do predio do castelo; remover/nao adicionar armor stands na casa porque a casa tem pouco espaco.
- Garantir que baus da casa fiquem dentro da casa e baus do castelo fiquem dentro do castelo.
- Manter muitos baus com itens conforme pedido anterior.
- Criar aves voando ao redor da casa e do castelo, fumacas, brilhos e efeitos para deixar a area com clima magico/encantado.
- Criar sistema de interacao dos personagens com o jogador se nao existir nativamente.
- Usuario autorizou iniciativa criativa dentro da tematica Magic World e quer ser informado dos bonus feitos.
- Instrucoes de port/git recebidas:
  - pasta base: `C:\Users\guilh\Desktop\MinecraftProjects`
  - pasta do port: `Port_1.20.1-Forge_MagicWorld-v1.3`
  - repo: `https://github.com/gorpo/MagicWorld-MagicWand_Mod`
  - antes de subir alteracoes do port, criar branch `V1.0_stable` com o estado funcional anterior as alteracoes de hoje;
  - depois subir port no branch principal e em branch backup `Port`;
  - porem o usuario disse que o port so deve ser feito apos conclusao deste projeto ou quando mandar. Portanto nao executar upload/port agora; apenas documentar.

Proximo passo imediato: editar `StarterPortalEvents.java` para antecipar loading no login, remover armor stands da casa/imported house, consolidar baus internos, adicionar ambientacao magica e melhorar interacao dos aliados.

## Atualizacao de Execucao - 2026-05-25 13:34 America/Sao_Paulo

Pedido acima executado:

- `StarterPortalEvents.java`:
  - tela de loading inicial agora e enviada logo no login quando a propriedade inicial sera criada, antes do delay e antes da construcao pesada. Isso deve reduzir o momento em que o jogador aparece no vazio antes da tela.
  - armor stands/pedestais foram removidos da casa importada e do caminho procedural da casa. Os sets em pedestais ficam concentrados no castelo.
  - baus da casa continuam com colocacao segura por busca de piso livre dentro da area da casa.
  - baus do castelo continuam no centro/interior do castelo e cheios ate o tamanho maximo.
  - aliados da casa deixaram de usar `VILLAGER`; agora sao `ALLAY` nomeados + guardiao.
  - interacao dos personagens foi expandida:
    - clique normal: seguir/parar;
    - Shift+clique: ajuda por funcao/nome, com falas e itens diferentes para ferreiro/oficina, cozinheiro/chef, bibliotecaria, agricultor/tratador e apoio generico.
    - ao ajudar, o aliado tambem passa a seguir o jogador.
  - adicionado ambiente magico:
    - aves/parrots nomeados voando perto da casa e do castelo;
    - allays extras como luzes/guias/mordomos magicos;
    - tigelas/focos de fumaca com campfire, amethyst, vidro azul e end rods ao redor da casa/castelo;
    - focos magicos no portal;
    - particulas periodicas de enchant/end rod/smoke na casa, portal e castelo quando o jogador esta perto.
- Rodado `.\gradlew.bat build magicWorldAllInOneJar` com sucesso.
- Copiado o all-in-one atualizado para `run/mods`; `run/mods` ficou somente com `MagicWorld-Ultimate-NeoForge-26.1.2-1.2.0-all-in-one.jar`.

Observacao: testar em mundo novo. Estruturas ja geradas em mundo antigo nao removem automaticamente armor stands/baus antigos.

## Atualizacao de Execucao - 2026-05-25 13:40 America/Sao_Paulo

Pedido adicional executado:

- `StarterPortalEvents.java`:
  - adicionados 3 soldados montados em cavalos na frente do castelo.
  - soldados/cavalos ficam registrados em patrulha curta e o tick tenta manter o grupo andando em pequeno perimetro na fachada, para ficarem sempre visiveis.
  - adicionados estandartes/postes magicos na frente do castelo com `BLUE_BANNER`, cercas, amethyst, end rods e carpete roxo na area frontal.
- Rodado `.\gradlew.bat build magicWorldAllInOneJar` com sucesso.
- Copiado all-in-one atualizado para `run/mods`; `run/mods` ficou somente com o jar all-in-one.

Observacao: testar em mundo novo para spawnar a nova patrulha/estandartes.

## Atualizacao de Execucao - 2026-05-25 13:50 America/Sao_Paulo

Pedido adicional executado:

- `StarterPortalEvents.java`:
  - adicionadas placas indicando caminho do portal, castelo e retorno para casa.
  - criado sistema subterraneo de trilhos ligando casa e castelo:
    - estacao subterranea da casa;
    - estacao subterranea do castelo;
    - escadas de acesso com corredor alto para evitar bater a cabeca;
    - trilhos energizados por `REDSTONE_BLOCK` abaixo de segmentos periodicos;
    - sem minecart spawnado nos trilhos.
  - em cada ponta do sistema de trilhos foram adicionados 3 baus de suprimentos com minecarts, chest minecarts, hopper minecarts, TNT minecarts, rails, powered/detector/activator rails, redstone blocks, redstone torches e levers.
  - reforcada iluminacao com postes magicos na area da casa, fazendas/estabulos, portal e castelo.
  - adicionadas lanternas/tochas internas na casa/castelo em pontos de apoio.
  - postes magicos usam amethyst, redstone lamp ligada, vidro azul, end rod e particulas de enchant.
  - adicionada camara selada de morcegos no subsolo do castelo com vidro escurecido e morcegos nomeados `Morcego Arcano`.
  - bonus surpresa criado: santuario secreto subterraneo perto do portal, com enchanting table, beacon, bau especial Magic World e placa.
- Rodado `.\gradlew.bat build magicWorldAllInOneJar` com sucesso.
- Copiado all-in-one atualizado para `run/mods`; `run/mods` ficou somente com o jar all-in-one.

Observacao: testar em mundo novo para gerar o metro subterraneo, postes, camara e santuario.

## Atualizacao de Execucao - 2026-05-25 13:55 America/Sao_Paulo

Pedido adicional executado:

- Regra nova do usuario: tambem gravar coisas novas importantes no `README.md`, mas somente quando condizem com o README.
- `README.md` atualizado com a secao `Propriedade Inicial Magic World`, resumindo loading, casa, portal, castelo, dragao, aliados, trilhos subterraneos, iluminacao, patrulha, morcegos e santuario secreto.
- Criada wiki local em `docs/MAGIC_WORLD_WIKI.md`, baseada no README e no codigo atual:
  - criacao de mundo;
  - loading;
  - casa;
  - portal premium;
  - castelo;
  - trilhos subterraneos;
  - aliados/interacao;
  - ambientacao magica;
  - dragao;
  - aura;
  - menu principal;
  - itens;
  - checklist de testes.

Observacao para proximas sessoes: manter a wiki atualizada quando novas mecanicas de jogo forem adicionadas ou alteradas.

## Atualizacao de Execucao - 2026-05-25 13:10 America/Sao_Paulo

Pedido novo executado nesta sessao:

- `MagicWorldWorldOptions.java`:
  - `Aura` agora inicia ligada por padrao no menu Magic World.
  - comandos/cheats agora iniciam ligados por padrao.
- `ClientEvents.java`:
  - ao abrir a tela de criacao de mundo, o painel Magic World reseta `Aura=ON` e `Comandos=ON`, entao a criacao pelo Magic World sempre tenta ativar cheats automaticamente, independente das demais opcoes.
- `MagicWorldPortalVisualController.java`:
  - ordem dos resource packs premium corrigida para: `256x`, `models`, `addon`, `bonus`.
- `StarterPortalEvents.java`:
  - gatilho automatico do portal ficou restrito ao centro atravessavel do portal, validando o bloco central `END_PORTAL_FRAME` com laterais de `AMETHYST_BLOCK` e vidros roxo/magenta. Chegar perto, ficar de lado ou encostar nas paredes laterais nao deve mais abrir/trocar o visual.
  - clique direito tambem so alterna se for no centro real do portal, nao nos lados/decoracoes.
  - limpeza de itens derrubados foi adicionada apos as etapas de geracao para remover sementes/blocos/restos soltos no chao.
  - limpeza final remove blocos soltos/flutuando de prismarine e detritos naturais comuns ao redor da propriedade, sem mexer dentro das footprints protegidas da casa importada e do castelo importado.
  - baus e armor stands da casa importada passam por busca de piso livre antes de serem colocados, para evitar baus/stands voando ou atravessando paredes/objetos.
  - todos os baus preenchidos por `putItems` agora ficam cheios ate o tamanho maximo do container, repetindo os itens informados.
  - bau especial `Draconic Aether` agora inclui tambem varinha magica e itens variados.
  - os 6 primeiros baus do castelo importado recebem armadura especial `Draconic Aether` + varinha magica + outros itens, e todos ficam cheios.
  - moradores do castelo nao usam mais `VILLAGER`; foram trocados por aliados `ALLAY`, `SNOW_GOLEM` e `IRON_GOLEM` com nome e persistencia.
- `build.gradle`:
  - os jars de `mods_para_inclusao/` agora tambem entram como `localRuntime` no `runClient`.
  - motivo: em ambiente Gradle/dev, o mod carregado de `build/classes` vence o jar all-in-one de `run/mods`, entao Iris/Sodium embutidos nao eram carregados no teste local. Em launcher normal, o all-in-one continua com JarJar.
- Rodado `.\gradlew.bat build magicWorldAllInOneJar` com sucesso.
- Copiado o all-in-one atualizado para `run/mods`; `run/mods` ficou somente com `MagicWorld-Ultimate-NeoForge-26.1.2-1.2.0-all-in-one.jar`.

Observacoes importantes para testar:

- Testar em mundo novo. Mundo ja gerado nao reposiciona baus/stands nem remove blocos antigos automaticamente.
- Pelo `runClient` do Gradle, os mods graficos agora devem carregar via `localRuntime`; se testar em TLauncher/launcher externo, usar somente o jar all-in-one.
- Ainda nao foi feito um NPC humano customizado igual ao player; os aliados atuais continuam usando entidades vanilla nomeadas/interativas. Para ficar literalmente igual ao jogador, precisa criar entidade/render/modelo humano proprio em tarefa separada.

## Atualizacao de Continuidade - 2026-05-25 09:58 America/Sao_Paulo

Nova sessao retomou pelo handoff. Pedido novo do usuario:

- Na tela `InitialLoadNoticeScreen`, durante o loading, mostrar exatamente o que esta sendo carregado: casa, portal, castelo, dragao etc.
- Depois de tudo construido/carregado, esperar mais alguns segundos com a mensagem `Finalizando estruturas do Magic World` e somente entao mostrar o botao.
- Corrigir o instalador: usar o banner `screenshots/banner_installer.png`; a area de rolagem esta escura/preta e deve ficar legivel; textos dos botoes devem ficar brancos.
- A area de rolagem do instalador deve explicar tudo que sera instalado, onde sera instalado, como proceder e que o TLauncher deve estar instalado.
- Criar o instalador em `.exe`.
- Na instalacao, alem de instalar o NeoForge, tambem instalar o mod, shader e resources nas pastas corretas, baixando pelos Releases do GitHub.

Proximo passo imediato desta sessao: ajustar loading e instalador, compilar/buildar, gerar `.exe` e atualizar este handoff no final.

## Atualizacao de Execucao - 2026-05-25 10:01 America/Sao_Paulo

Pedido novo executado:

- `StarterPortalEvents.java` agora envia mensagens de progresso antes das etapas pesadas:
  - `Carregando casa...`
  - `Carregando fazendas e animais...`
  - `Carregando portal...`
  - `Carregando castelo...`
  - `Carregando dragao...`
  - `Finalizando estruturas do Magic World`
- Foi adicionada espera final de `100` ticks com progresso `98` e `complete=false`; somente depois a tela recebe `100`/`complete=true` e o botao aparece.
- Corrigido caso sem castelos para nao spawnar o dragao duas vezes.
- `scripts/install-magicworld-tlauncher.ps1` agora usa `screenshots/banner_installer.png`.
- Instalador agora mostra texto inicial legivel na area de rolagem explicando o que sera instalado, onde sera instalado, que o TLauncher deve estar instalado, e o que fazer depois.
- Area de rolagem trocada para fundo claro; botoes configurados com texto branco.
- Instalador agora tem URLs padrao para baixar dos Releases do GitHub:
  - mod all-in-one;
  - Resource Packs principal/models/addon/bonus;
  - Shader Pack.
- NeoForge installer baixa por padrao de `maven.neoforged.net` na versao `26.1.2.65-beta`.
- Criado `scripts/MagicWorldInstallerLauncher.cs`.
- Compilado `MagicWorldInstaller.exe` na raiz do projeto.
- Criado pacote local `build/installer/` contendo:
  - `MagicWorldInstaller.exe`;
  - `install-magicworld-tlauncher.ps1`;
  - `screenshots/banner_installer.png`.
- `README.md` atualizado para citar `MagicWorldInstaller.exe` e o banner novo.
- Rodado `.\gradlew.bat build magicWorldAllInOneJar` com sucesso.
- Copiado o all-in-one novo para `run/mods`; `run/mods` ficou apenas com o all-in-one.

Observacao para proxima sessao: o `.exe` atual e um launcher Windows para o script PowerShell. Para publicar no GitHub Releases, anexar o conteudo de `build/installer/` junto ou manter o `.ps1` ao lado do `.exe`, porque o executavel localiza e executa `install-magicworld-tlauncher.ps1`.

## Atualizacao de Execucao - 2026-05-25 10:04 America/Sao_Paulo

Pedido adicional executado:

- Criada pasta `installer/` na raiz do projeto.
- Conteudo de `installer/`:
  - `MagicWorldInstaller.exe`;
  - `install-magicworld-tlauncher.ps1`;
  - `neoforge-26.1.2.65-beta-installer.jar`;
  - `screenshots/banner_installer.png`.
- A pasta `build/installer/` foi sincronizada com o mesmo conteudo.
- `scripts/install-magicworld-tlauncher.ps1` foi ajustado para:
  - funcionar quando estiver dentro da pasta `installer/`;
  - procurar o NeoForge incluso na mesma pasta do script;
  - usar o NeoForge incluso antes de baixar da internet;
  - baixar o NeoForge da Maven oficial apenas se nao houver jar local incluso;
  - manter downloads dos mod/resourcepacks/shader pelos Releases do GitHub com fallback local.
- Recompilado `MagicWorldInstaller.exe`.
- Validada sintaxe do script da pasta `installer/`.

## Atualizacao de Execucao - 2026-05-25 10:06 America/Sao_Paulo

Pedido adicional executado:

- O instalador agora e um `.exe` unico em `installer/MagicWorldInstaller.exe`.
- O executavel embute:
  - `install-magicworld-tlauncher.ps1`;
  - `screenshots/banner_installer.png`;
  - `neoforge-26.1.2.65-beta-installer.jar`.
- `scripts/MagicWorldInstallerLauncher.cs` foi alterado para extrair esses recursos embutidos para uma pasta temporaria e executar o script de la.
- A pasta `installer/` foi limpa e agora contem somente `MagicWorldInstaller.exe`.
- Removido o `MagicWorldInstaller.exe` solto da raiz do projeto.
- `build/installer/` foi sincronizada e tambem contem somente `MagicWorldInstaller.exe`.
- Validado via reflection que o `.exe` contem os recursos embutidos:
  - `install-magicworld-tlauncher.ps1`;
  - `banner_installer.png`;
  - `neoforge-installer.jar`.
- `README.md` atualizado para indicar `installer/MagicWorldInstaller.exe` como instalador unico.

## Atualizacao de Execucao - 2026-05-25 10:17 America/Sao_Paulo

Pedido adicional executado:

- `InitialLoadNoticeScreen.java`: o texto `O botao aparece quando tudo terminar.` agora some quando `currentComplete=true`, evitando ficar atras/embaixo do botao `CONFIRMAR`.
- `StarterPortalEvents.java`:
  - adicionada margem global `STRUCTURE_BREATHING_MARGIN = 8`;
  - portal movido de `PORTAL_Z_OFFSET = 90` para `PORTAL_Z_OFFSET = 70`, para ficar mais no gramado/espaco verde mostrado no print;
  - castelo foi desacoplado do novo portal e manteve a posicao anterior com `CASTLE_Z_OFFSET_FROM_BASE = 90` e `CASTLE_X_OFFSET_FROM_PORTAL = 40`, preservando a distancia casa/castelo que o usuario disse estar perfeita;
  - limpeza de volume de estruturas importadas agora usa margem de 8 blocos e remove blocos acima ate o topo do mundo antes de colocar casa/castelo;
  - casa importada recebe superficie de respiro com grama ao redor da footprint, sem tocar no volume real da casa;
  - portal agora prepara clareira de `-16..16` e remove blocos acima ate o topo do mundo antes de construir;
  - ring de grama do portal agora limpa ate y=10 em vez de y=4;
  - fallback da casa procedural tambem usa limpeza ate o topo do mundo com margem maior.
- Rodado `.\gradlew.bat build magicWorldAllInOneJar` com sucesso.
- Copiado all-in-one atualizado para `run/mods` e `run/mods` ficou somente com o all-in-one.

Observacao: testar em mundo novo. Mundo ja gerado nao vai reposicionar portal nem limpar montanhas antigas automaticamente.

## Atualizacao de Execucao - 2026-05-25 10:24 America/Sao_Paulo

Pedido adicional executado enquanto usuario testa o jogo:

- `README.md` revisado e atualizado para refletir o estado atual:
  - portal nao usa mais `nether_portal` como marcador;
  - propriedade inicial documentada como casa/castelo importados, nao procedural;
  - etapas de carregamento atualizadas com casa, fazendas, portal, castelo, dragao e finalizacao;
  - documentada margem de respiro de 8 blocos e limpeza de blocos acima das estruturas;
  - instalador unico `installer/MagicWorldInstaller.exe` adicionado aos Releases e recomendacoes;
  - requisitos ajustados para NeoForge instalado manualmente ou via instalador;
  - recomendacao de testar casa/portal/castelo em mundo novo;
  - tabela all-in-one corrigida para incluir `Framework`.
- `Downgrade_1.20.1.txt` revisado:
  - agora deixa explicito que o destino e Forge 1.20.1;
  - remove orientacao antiga de recriar casa/castelo em codigo;
  - manda preservar `imported_house.nbt` e `imported_castle.nbt` ou reconverter dos mapas sem aproximacao procedural;
  - adiciona checklist para margem de respiro, limpeza acima das estruturas, portal no gramado e tela de progresso;
  - adiciona checklist para gerar novo instalador unico com Forge installer 1.20.1 embutido.
- Sem build nesta etapa porque foram alteracoes documentais.

## Atualizacao de Execucao - 2026-05-25 10:27 America/Sao_Paulo

Pedido/bug report do usuario: portal tem blocos solidos que impedem atravessar; usuario quebrou os blocos, mas nada aconteceu.

Correcao aplicada:

- `StarterPortalEvents.java`:
  - centro vertical do portal foi aberto com `AIR` para permitir atravessar;
  - vidro roxo/magenta ficou nas laterais internas, nao bloqueando o meio;
  - o piso central continua com `END_PORTAL_FRAME` como marcador acionavel;
  - gatilho de entrada agora procura marcador do portal num raio de 4 blocos, em vez de exigir pisar exatamente no bloco marcador;
  - clique direito em blocos proximos ao portal procura marcador num raio de 5 blocos;
  - `isStarterPortalMarker` agora tambem reconhece `PURPLE_STAINED_GLASS`, `MAGENTA_STAINED_GLASS` e `AMETHYST_BLOCK`.
- Rodado `.\gradlew.bat build magicWorldAllInOneJar` com sucesso.
- Copiado all-in-one atualizado para `run/mods` e `run/mods` ficou somente com o all-in-one.

Observacao: para ver o portal fisicamente aberto, testar em mundo novo ou regenerar a propriedade. Em mundo ja gerado, blocos antigos do portal nao mudam automaticamente, mas o gatilho por proximidade deve ajudar se ainda houver algum marcador do portal perto.

## Atualizacao de Execucao - 2026-05-25 10:37 America/Sao_Paulo

Pedido adicional executado:

- `InitialLoadNoticeScreen.java`:
  - removido botao `CONFIRMAR`;
  - removido texto `O botao aparece quando tudo terminar.`;
  - quando chega payload `complete=true` ou progresso `100`, a tela fecha sozinha com `minecraft.setScreen(null)`.
- `ClientEvents.java`:
  - corrigido drift do primeiro botao `Magic World` na tela de criacao de mundo;
  - `MagicCreateWorldPanel` agora guarda referencia ao `magicTabButton`;
  - `relayoutMagicPanel` recalcula posicao/tamanho do botao inicial;
  - tick do cliente relayouta o botao enquanto `CreateWorldScreen` estiver aberta, mesmo se o painel Magic World nao estiver visivel.
- `PremiumPortalOptionsScreen.java`:
  - se `Pacote completo` estiver selecionado, `ResourcePack` e `ShaderPack` sao desmarcados e ficam inativos;
  - ao confirmar com pacote completo, envia apenas `complete=true` e `resource/shader=false`; servidor ainda resolve pacote completo como ambos ativos.
- Rodado `.\gradlew.bat build magicWorldAllInOneJar` com sucesso.
- Copiado all-in-one atualizado para `run/mods` e `run/mods` ficou somente com o all-in-one.

## Atualizacao de Execucao - 2026-05-25 09:29 America/Sao_Paulo

Pedido mais recente do usuario: finalizar rapido. O trabalho continua pela extracao real dos mapas, sem aproximar a casa/castelo em codigo.

Estado no momento desta atualizacao:

- O save da casa segue em `tmp/extracted/house/Hacienda House - 01`.
- O save do castelo segue em `tmp/extracted/castle/The Witcher 1.0`.
- A casa foi localizada ao redor do spawn do mapa `Hacienda House - 01`. Caixa de exportacao escolhida para preservar a construcao completa: `x=-248..-130`, `y=5..32`, `z=110..240`.
- O castelo foi localizado pelo mapa interno renderizado em `tmp/castle_maps_vanilla_palette.png`. A melhor area candidata esta no conjunto dos mapas `map_60` e `map_63`, com construcao grande em cinza/laranja. Caixa de exportacao escolhida: `x=-704..-440`, `y=55..112`, `z=-480..-260`.
- Proximo passo imediato: criar `scripts/extract_magicworld_structures.py`, exportar `imported_house.nbt` e `imported_castle.nbt`, integrar no `StarterPortalEvents.java` e rodar `gradlew build magicWorldAllInOneJar`.
- Se esta sessao cair agora: continuar exatamente deste ponto, nao voltar para estrutura procedural.

## Atualizacao de Execucao - 2026-05-25 09:42 America/Sao_Paulo

Extracao real dos mapas concluida:

- Criado `scripts/extract_magicworld_structures.py`.
- Gerado `src/main/resources/data/magicworld/structure/imported_house.nbt`.
  - Origem: `tmp/extracted/house/Hacienda House - 01`.
  - Caixa usada: `x=-248..-130`, `y=5..32`, `z=110..240`.
  - Tamanho do template: `119 x 28 x 131`.
  - Blocos exportados: `97642`.
  - Palette: `434` estados.
  - `DataVersion`: `2586`, para o DataFixer atualizar nomes antigos da casa.
- Gerado `src/main/resources/data/magicworld/structure/imported_castle.nbt`.
  - Origem: `tmp/extracted/castle/The Witcher 1.0`.
  - Caixa usada: `x=-704..-440`, `y=64..112`, `z=-480..-260`.
  - Tamanho do template: `265 x 49 x 221`.
  - Blocos exportados: `700751`.
  - Palette: `69` estados.
  - `DataVersion`: `3465`.
- Os dois `.nbt` foram abertos com `nbtlib.load` para validar que estao legiveis.
- Proximo passo imediato: integrar `imported_house` e `imported_castle` em `StarterPortalEvents.java` usando `StructureTemplateManager`, remover o castelo procedural do caminho normal e manter apenas fallback se o template nao carregar.

### Regra Atual Mais Importante

- Casa e castelo devem ser extraidos exatamente dos mapas fornecidos pelo usuario, sem recriacao aproximada em codigo.
- Casa origem: `tmp/casa_principal_do_personagem.zip`.
- Castelo origem: `tmp/mapa_aproveitar_castelo.zip`.
- Se outra sessao assumir: nao substituir por uma casa/castelo procedural "parecido"; o usuario pediu "exatamente e sem nenhuma diferenca".

### Estado Atual do Trabalho Nesta Sessao

Arquivos ja extraidos:

- `tmp/casa_principal_do_personagem.zip` foi expandido para `tmp/extracted/house/Hacienda House - 01`.
- `tmp/mapa_aproveitar_castelo.zip` foi expandido para `tmp/extracted/castle/The Witcher 1.0`.

Dados confirmados com leitura de `level.dat` via Python/nbtlib:

- Casa `Hacienda House - 01`:
  - `LevelName`: `Hacienda House - 01`
  - Spawn: `X=-165`, `Y=10`, `Z=171`
  - Player pos aproximado: `X=-163.67`, `Y=10`, `Z=171.39`
  - Mundo em formato moderno com `Sections/Palette/BlockStates`.
- Castelo/mapa `The Witcher 1.0`:
  - `LevelName`: `The Witcher`
  - Spawn: `X=-240`, `Y=64`, `Z=248`
  - Player pos aproximado: `X=-249.62`, `Y=73.06`, `Z=233.50`
  - Mundo em formato antigo com `Sections/Blocks/Data` usando IDs numericos de blocos. Precisa conversao legado -> nomes modernos.

Arquivos/imagens temporarias geradas para localizar o castelo:

- `tmp/castle_top_spawn_-240_248.png`
- `tmp/castle_top_candidate_z512.png`
- `tmp/castle_top_candidate_negz.png`
- `tmp/castle_top_wide.png`
- `tmp/castle_slice_spawn_y60.png`
- `tmp/castle_slice_spawn_y64.png`
- `tmp/castle_slice_spawn_y70.png`
- `tmp/castle_slice_spawn_y73.png`
- `tmp/castle_slice_spawn_y80.png`
- `tmp/castle_slice_spawn_y90.png`
- `tmp/castle_slice_z512_y64.png`
- `tmp/castle_slice_z512_y73.png`
- `tmp/castle_slice_z512_y80.png`
- `tmp/castle_slice_z512_y90.png`
- `tmp/castle_slice_z512_y100.png`
- `tmp/castle_slice_negz_y64.png`
- `tmp/castle_slice_negz_y73.png`
- `tmp/castle_slice_negz_y80.png`
- `tmp/castle_slice_negz_y90.png`
- `tmp/castle_slice_negz_y100.png`
- `tmp/castle_maps_contact.png`

Observacao: ainda nao foi confirmado visualmente qual estrutura do mapa `The Witcher 1.0` e o castelo que o usuario quer. `tmp/castle_maps_contact.png` mostra os mapas internos do save e pode ajudar a encontrar coordenadas. O spawn do castelo nao parece ser a construcao grande final; ha estruturas espalhadas.

### Alteracoes de Codigo Ja Aplicadas Mas Ainda Nao Buildadas

Estas alteracoes foram feitas antes do usuario interromper para priorizar a extracao exata. Elas estao no working tree e precisam ser compiladas/validadas depois:

- `src/main/java/com/magicworld/network/MagicWorldNetwork.java`
  - Adicionado `InitialLoadProgressPayload(int progress, String message, boolean complete)`.
  - Registrado payload client-side para progresso do loading inicial.

- `src/main/java/com/magicworld/client/InitialLoadNoticeScreen.java`
  - Tela mudou de aviso estatico para barra de progresso.
  - Botao `CONFIRMAR` fica oculto/inativo ate `complete=true` ou progresso `100`.
  - Adicionado estado estatico `currentProgress/currentMessage/currentComplete`.
  - Ainda precisa build para confirmar imports/API.

- `src/main/java/com/magicworld/client/ClientEvents.java`
  - Handler de `OpenInitialLoadNoticePayload` agora reseta progresso e abre tela.
  - Handler de `InitialLoadProgressPayload` atualiza a barra.
  - Painel Magic World da tela de criacao de mundo foi parcialmente ajustado: altura aumentada e bloco de botoes baixado. Ainda falta revisar o texto amarelo para garantir que fique abaixo do branco e acima dos botoes sem sobreposicao.

- `src/main/java/com/magicworld/event/StarterPortalEvents.java`
  - `buildEstateExpansionStep` agora recebe `ServerPlayer` para enviar progresso da tela de loading.
  - Envia progresso em etapas: casa, fazendas, portal, castelo, dragao e final.
  - Portal nao usa mais `Blocks.NETHER_PORTAL`; o roxo foi trocado para `PURPLE_STAINED_GLASS`/`MAGENTA_STAINED_GLASS`, evitando ir ao Nether.
  - `isStarterPortalMarker` nao reconhece mais `Blocks.NETHER_PORTAL`.
  - Gramado limpo do portal passou de 5 para 8 blocos.
  - Baus/placas/postes do portal foram movidos para fora da area limpa para nao serem apagados e nao derrubarem itens no chao.
  - `buildAnimalPen` foi alterado para grama no chao e cercas em `y+1`; animais devem ter espaco livre, sem feno/plank no piso. Ainda precisa teste em novo mundo.

### Importante Sobre Build/Teste

- Depois de retomar, rodar `.\gradlew.bat build magicWorldAllInOneJar`.
- Ainda nao foi rodado build depois dessas alteracoes de loading/portal/fazendas/layout.
- Depois de buildar, copiar o all-in-one para `run/mods` e manter `run/mods` somente com o all-in-one, salvo se for tomada decisao explicita sobre Iris/Sodium/Framework nao carregarem via JarJar.
- Usuario testa o jogo manualmente. Sempre avisar para testar em mundo novo, porque estruturas ja geradas em mundo antigo nao sao substituidas automaticamente.

### Proximo Passo Exato

1. Criar script de extracao que leia ambos formatos:
   - moderno `Palette/BlockStates` para a casa;
   - antigo `Blocks/Data` para o mapa do castelo.
2. Localizar bounding box exata da casa no mundo `Hacienda House - 01`, provavelmente ao redor de `X=-165`, `Y=10`, `Z=171`.
3. Localizar bounding box exata do castelo no mundo `The Witcher 1.0`; ainda nao confirmado. Usar `tmp/castle_maps_contact.png` e varredura de blocos construidos/alturas para achar a construcao correta.
4. Exportar para `.nbt` de estrutura Minecraft em `src/main/resources/data/magicworld/structure/` ou `src/main/resources/data/magicworld/structures/` conforme API de 26.1.2 aceitar.
5. Implementar colocacao das estruturas com `StructureTemplateManager`/`StructureTemplate.placeInWorld`.
6. Trocar `buildPremiumHouse`/`buildPersonalCastle` para usarem a estrutura importada, preservando os sistemas adicionais pedidos pelo usuario: baus lotados, armor stands, luz extra, spawn perto da cama, fazendas/plantacoes/cercas/estabulo proximos.
7. Buildar e deixar jar pronto para teste.

## Pedido Atual

1. Usar `tmp/casa_principal_do_personagem.zip` como base da casa principal do personagem.
2. Usar `tmp/mapa_aproveitar_castelo.zip` como base do castelo.
3. Remover por completo o castelo procedural antigo do mod.
4. Colocar o castelo novo a 40 blocos a direita do portal.
5. O castelo so deve gerar depois do portal estar completo.
6. O dragao deve ser uma das ultimas coisas a spawnar.
7. O dragao deve voar baixo, com altura variavel, somente pelo perimetro da casa, portal e castelo.
8. O dragao deve alternar voltas na casa, no portal e no castelo, pousando nos tres locais.
9. Mostrar uma mensagem/pop-up uma unica vez explicando que a primeira criacao de mapa pode demorar enquanto a casa, portal, fazendas, castelo e dragao carregam.
10. Garantir que o jogo rode somente com o jar all-in-one em `run/mods`.
11. Portal deve seguir o visual do print: estrutura compacta de madeira, luz, folhas/vinhas e portal roxo; em volta do portal, 5 blocos para cada lado devem ser gramado reto sem decoracao.
12. Casa pessoal:
    - spawn do jogador ao lado da cama;
    - bau(s) ao lado da cama com muitos itens, todas armaduras, armas e ferramentas de todos os sets;
    - casa muito iluminada;
    - armor stands com sets espalhados pelos comodos;
    - apenas personagens parecidos com o jogador dentro da casa, interativos, conversam, seguem e ajudam;
    - terreno cercado maior, com plantacoes proximas dentro da cerca;
    - fazenda cercada e pequena area coberta com cada animal de fazenda do jogo;
    - pequeno estabulo com cavalos.

## Estado Ja Implementado Antes Deste Handoff

Arquivos principais:

- `src/main/java/com/magicworld/event/StarterPortalEvents.java`
  - Gera propriedade inicial em etapas para reduzir peso no primeiro carregamento.
  - Criacao atual inclui casa, fazendas, portal, castelo procedural, dragao e aliados.
  - Ja existe logica de aliados seguindo o jogador e ajudando via interacao.
  - Ja existe portal premium com menu de ResourcePack/ShaderPack/Pacote completo.
  - Ja existe suporte para modo criativo escolhido na tela Magic World.
  - O castelo procedural antigo ainda precisa ser removido/substituido.

- `src/main/java/com/magicworld/entity/PeacefulDragon.java`
  - Dragao pacifico, invulneravel, sem ataque e com rota controlada por codigo.
  - Atualmente ainda usa rota eliptica antiga e precisa ser alterado para casa/portal/castelo.

- `src/main/java/com/magicworld/network/MagicWorldNetwork.java`
  - Payloads atuais: abrir menu premium, confirmar opcoes premium, aplicar visual premium.
  - Deve receber novo payload para pop-up de demora inicial.

- `src/main/java/com/magicworld/client/ClientEvents.java`
  - Registra handlers client-side.
  - Deve abrir o novo pop-up de demora inicial.

- `src/main/java/com/magicworld/client/PremiumPortalOptionsScreen.java`
  - Tela premium atual.
  - Pode servir de modelo para a nova tela simples com botao CONFIRMAR.

- `build.gradle`
  - Task `magicWorldAllInOneJar` embute jars via JarJar.
  - Dependencias all-in-one devem ficar em `mods_para_inclusao`.
  - `run/mods` deve ficar somente com `MagicWorld-Ultimate-NeoForge-26.1.2-1.2.0-all-in-one.jar` para teste.

## Recursos Externos Temporarios

- `tmp/casa_principal_do_personagem.zip`
  - Mapa/zip fornecido pelo usuario para aproveitar a casa principal.
  - Precisa ser inspecionado e convertido para estrutura/gerador utilizavel no mod.

- `tmp/mapa_aproveitar_castelo.zip`
  - Mapa/zip fornecido pelo usuario para aproveitar o castelo.
  - Precisa ser inspecionado e convertido para estrutura/gerador utilizavel no mod.

## Observacao Sobre os Zips

Os zips sao mundos completos com arquivos `.mca`. A casa esta em formato moderno com `Palette/BlockStates`; o mapa do castelo esta em formato antigo com IDs numericos em `Blocks/Data`. A prioridade atual e converter/extrair as estruturas exatamente dos mundos fornecidos. Nao recriar visualmente em codigo e nao trocar por estrutura procedural aproximada.

## All-In-One

O all-in-one deve conter:

- MagicWorld;
- Iris;
- Sodium;
- Distant Horizons;
- FerriteCore;
- ModernFix;
- ImmediatelyFast;
- Lithostitched;
- Entity Texture Features;
- Entity Model Features;
- JourneyMap;
- Effortless Building;
- Framework;
- Controllable.

Local de origem dos jars:

- `mods_para_inclusao/`

Local de teste:

- `run/mods/`

Regra de teste:

- `run/mods/` deve conter apenas o jar all-in-one final.

## Downgrade Para 1.20.1

Este projeto inteiro deve receber downgrade para Minecraft 1.20.1 usando **Forge**, nao NeoForge. Nao copiar cegamente: 26.1.2 usa APIs, nomes de classes, imports, networking, GUI e alguns blocos/itens diferentes. Use este checklist como mapa de migracao.

### Pastas Temporarias e Referencias

- [ ] `tmp/casa_principal_do_personagem.zip`
  - mundo completo de referencia para a casa principal.
  - nao e estrutura `.nbt` pronta; se for usar no downgrade, converter ou recriar em codigo.
- [ ] `tmp/mapa_aproveitar_castelo.zip`
  - mundo completo de referencia para o castelo.
  - nao e estrutura `.nbt` pronta; se for usar no downgrade, converter ou recriar em codigo.
- [ ] `local-backups/`
  - backups locais de mods/run anteriores.
- [ ] `mods_para_inclusao/`
  - jars usados no all-in-one atual. Para 1.20.1 trocar por versoes 1.20.1 equivalentes.
- [ ] `run/mods/`
  - pasta de teste. No final do downgrade deve conter somente o jar all-in-one 1.20.1.
- [ ] `scripts/install-magicworld-tlauncher.ps1`
  - instalador local atual para TLauncher + NeoForge 26.1.2.
  - no downgrade, criar versao Forge 1.20.1 equivalente.
- [ ] `screenshots/banner_instalador_magicworld.png`
  - banner do instalador atual.
- [ ] `screenshots/banner_tlauncher_magicworld.png`
  - banner da secao TLauncher + Magic World.

### Build, Loader e Jars

- [ ] `build.gradle`
  - portar plugin `net.neoforged.moddev` para **ForgeGradle** compativel com Minecraft Forge 1.20.1.
  - revisar `minecraft_version`, `forge_version`, Java toolchain e task `magicWorldAllInOneJar`.
  - `jvmArguments.addAll '-Xms2G', '-Xmx8G'` foi adicionado no run client atual; manter equivalente no downgrade.
  - adaptar JarJar/metadados para Forge 1.20.1.
  - trocar todos os jars embutidos por versoes Forge 1.20.1: Oculus/Embeddium se Iris/Sodium nao forem Forge, Distant Horizons Forge, FerriteCore Forge, ModernFix Forge, ImmediatelyFast Forge, ETF/EMF Forge, JourneyMap Forge, Effortless Building Forge, Framework Forge e Controllable Forge.
- [ ] `gradle.properties`
  - revisar `mod_id`, `mod_version`, `minecraft_version`, ranges e memoria do Gradle.
- [ ] `settings.gradle`
  - manter nome do projeto ou ajustar para o workspace 1.20.1.

### Registro Principal do Mod

- [ ] `src/main/java/com/magicworld/MagicWorld.java`
  - registra mod, itens, armaduras, entidade `peaceful_dragon`, abas criativas e listeners.
  - adaptar `DeferredRegister`, `RegistryObject`/`DeferredHolder`, `CreativeModeTabs`, `BuildCreativeModeTabContentsEvent` e `EntityType.Builder`.
  - checar diferencas de imports NeoForge 26.1.2 vs 1.20.1.
- [ ] `src/main/java/com/magicworld/MagicWorldClient.java`
  - entrada client-side, se usada pela versao escolhida.
- [ ] `src/main/java/com/magicworld/Config.java`
  - configs do portal/visual. Adaptar `ModConfigSpec` se API mudar.
- [ ] `src/main/java/com/magicworld/MagicWorldWorldOptions.java`
  - estado da tela Magic World de criacao de mundo: portal, castelo, aura, comandos, perfil PC, modo e dificuldade.

### Varinha Magica

- [ ] `src/main/java/com/magicworld/item/VarinhaMagicaItem.java`
  - item original do projeto.
  - adaptar metodos de uso/interacao para API 1.20.1 (`use`, `useOn`, `interactLivingEntity`, contexts e results).
- [ ] `src/main/java/com/magicworld/event/CraftEvents.java`
  - receita/entrega/comportamentos ligados a crafting se aplicavel.
- [ ] assets da varinha:
  - `src/main/resources/assets/magicworld/models/item/varinha_magica.json`
  - `src/main/resources/assets/magicworld/textures/item/varinha_magica.png`
  - `src/main/resources/assets/magicworld/items/varinha_magica.json`
  - conferir formato de item model entre 26.1.2 e 1.20.1.

### Armaduras, Armas e Itens Premium

- [ ] registros em `src/main/java/com/magicworld/MagicWorld.java`
  - set `draconic_aether`: helmet, chestplate, leggings, boots.
  - adaptar material de armadura, `ArmorItem`, layers/equipment assets para 1.20.1.
- [ ] assets:
  - `src/main/resources/assets/magicworld/models/item/draconic_aether_*.json`
  - `src/main/resources/assets/magicworld/textures/item/draconic_aether_*.png`
  - `src/main/resources/assets/magicworld/textures/entity/equipment/humanoid/draconic_aether.png`
  - `src/main/resources/assets/magicworld/textures/entity/equipment/humanoid_leggings/draconic_aether.png`
  - se 1.20.1 usar caminho antigo de armor texture, mover/adaptar para o formato esperado.
- [ ] menus de ferramentas/armaduras:
  - `src/main/java/com/magicworld/client/menus/PremiumArmorMenu.java`
  - `src/main/java/com/magicworld/client/menus/PremiumToolsMenu.java`
  - `src/main/java/com/magicworld/client/PremiumDetailsScreen.java`
  - `src/main/java/com/magicworld/client/PremiumEntry.java`

### Menu Principal e Submenus

- [ ] `src/main/java/com/magicworld/client/KeyBindings.java`
  - tecla `H` para abrir menu.
  - adaptar `KeyMapping`, eventos de registro e consumo da tecla.
- [ ] `src/main/java/com/magicworld/client/PremiumMenuScreen.java`
  - menu principal `MagicWorld - Magic Wand`.
  - adaptar renderizacao da tela para 1.20.1 (`GuiGraphicsExtractor` pode nao existir; usar `GuiGraphics`/metodos equivalentes).
- [ ] componentes visuais:
  - `MagicWorldMenuTheme.java`
  - `MagicWorldMenuButton.java`
  - `MagicWorldIconButton.java`
  - `PremiumDetailsScreen.java`
- [ ] submenus em `src/main/java/com/magicworld/client/menus/`
  - BiomeTeleportMenu, BossControlMenu, DimensionMenu, DungeonSpawnerMenu, GraphicsProfilesMenu, LuckyBlockMenu, MobSpawnerMenu, NPCMenu, ParticleEffectsMenu, PortalMenu, PremiumArmorMenu, PremiumCompanionsMenu, PremiumMountsMenu, PremiumPowersMenu, PremiumToolsMenu, StructureRainMenu, TimeControlMenu, TransformationMenu, TrollMenu, VarinhaMagicaMenu, WaveSurvivalMenu, WeatherControlMenu, WorldEventsMenu.
  - revisar comandos: em 1.20.1 alguns ids de estruturas/biomas/comandos podem mudar.

### Botao "Magic World" na Criacao de Mundo

- [ ] `src/main/java/com/magicworld/client/ClientEvents.java`
  - metodo: `ClientEvents.ClientForgeEvents.onScreenInit(ScreenEvent.Init.Post event)`.
  - procurar `Button magicTabButton = Button.builder(`.
  - o botao e criado quando a tela e `CreateWorldScreen`.
  - ele chama `showMagicPanel(screen, true)`.
  - a "segunda tela" e um painel overlay na propria `CreateWorldScreen`, nao uma classe separada.
- [ ] widgets internos no mesmo arquivo:
  - `MagicCreateWorldLineCover`
  - `MagicCreateWorldBackdrop`
  - `MagicCreateWorldTitle`
  - `MagicCreateWorldInfo`
- [ ] funcoes a portar:
  - `showMagicPanel`
  - `magicPanelLayout`
  - `relayoutMagicPanel`
  - `createWorldFromMagicTab`
  - `syncWorldCreationOptions`
  - `syncAutomaticCommands`
- [ ] riscos de 1.20.1:
  - `CreateWorldScreen.onCreate` pode ter nome/assinatura diferente; ajustar reflexao.
  - eventos `ScreenEvent.Opening` e `ScreenEvent.Init.Post` podem ter pacote/nome diferente.
  - widgets/render podem usar `GuiGraphics` em vez de `GuiGraphicsExtractor`.

### Portal, Casa, Fazenda, Castelo e Spawn Inicial

- [ ] `src/main/java/com/magicworld/event/StarterPortalEvents.java`
  - geracao em etapas da propriedade inicial.
  - inclui casa do personagem, spawn ao lado da cama, baus, armor stands, currais, plantacoes, portal, castelo e dragao.
  - adaptar eventos `PlayerLoggedInEvent`, `RightClickBlock`, `EntityInteract`, `PlayerTickEvent`.
  - adaptar `ServerPlayer.RespawnConfig` e `LevelData.RespawnData`: esses nomes/metodos provavelmente diferem em 1.20.1.
  - adaptar `EntitySpawnReason.STRUCTURE`: em 1.20.1 pode ser `MobSpawnType.STRUCTURE` ou equivalente.
  - adaptar `ValueInput/ValueOutput`: em 1.20.1 persistencia NBT usa `CompoundTag`.
- [ ] blocos e estados usados na geracao:
  - revisar nomes/propriedades de `Blocks.*`, `DoorBlock.HALF`, `BedBlock.PART`, `StairBlock.FACING`, `FenceGateBlock.FACING`, `HorizontalDirectionalBlock.FACING`, `StandingSignBlock.ROTATION`.
  - conferir blocos mais novos ou com comportamento diferente: `CHERRY`, `SNIFFER`/itens se forem adicionados depois, `WATER_CAULDRON`, `SCULK`, `AMETHYST_BLOCK`, `RESPAWN_ANCHOR`, `END_PORTAL_FRAME`, `NETHER_PORTAL`, banners, lanternas, slabs e stairs.
  - se algum bloco nao existir ou tiver nome diferente em 1.20.1, substituir por equivalente vanilla 1.20.1.
- [ ] baus e inventarios:
  - `placeChest`
  - `putItems`
  - em 1.20.1 revisar `Container`, block entities e chamada `setChanged`.
- [ ] personagens aliados:
  - `spawnNamedCharacter`
  - `isMagicWorldAlly`
  - `handleFollowingAllies`
  - `giveAllyHelp`
  - adaptar IA/navegacao se `Mob.getNavigation().moveTo` mudar.

### Portal Premium e Visual

- [ ] `src/main/java/com/magicworld/client/PremiumPortalOptionsScreen.java`
  - tela de ResourcePack, ShaderPack e Pacote completo.
  - adaptar `Checkbox`, `Button`, `Screen`, render.
- [ ] `src/main/java/com/magicworld/client/InitialLoadNoticeScreen.java`
  - pop-up unico avisando demora inicial.
  - adaptar renderizacao e botao confirmar.
- [ ] `src/main/java/com/magicworld/client/MagicWorldPortalVisualController.java`
  - aplica resource pack/shader selecionado.
  - revisar API de resource packs e shader loader em 1.20.1.
- [ ] `src/main/java/com/magicworld/network/MagicWorldNetwork.java`
  - payloads: abrir menu premium, confirmar opcoes, aplicar visual, abrir aviso inicial.
  - 1.20.1 pode exigir `SimpleChannel`/`NetworkRegistry` ou outro sistema, dependendo do loader.

### Dragao Pacifico

- [ ] `src/main/java/com/magicworld/entity/PeacefulDragon.java`
  - entidade pacifica baseada em `EnderDragon`.
  - rota baixa casa/portal/castelo.
  - adaptar:
    - `hurt`, `hurtServer`, `onCrystalDestroyed`, `canUsePortal`;
    - `flightHistory`;
    - `setFightOrigin`;
    - persistencia `ValueInput/ValueOutput` para `CompoundTag`;
    - imports de `DamageSource`, `EndCrystal`, `EnderDragonPart`.
- [ ] registro da entidade:
  - `src/main/java/com/magicworld/MagicWorld.java`
  - renderer em `src/main/java/com/magicworld/client/ClientEvents.java`.

### Criaturas Premium

- [ ] classes em `src/main/java/com/magicworld/entity/`
  - PremiumAxolotl, PremiumBat, PremiumBee, PremiumCamel, PremiumCat, PremiumChicken, PremiumCow, PremiumCreeper, PremiumFox, PremiumFrog, PremiumGoat, PremiumHorse, PremiumIronGolem, PremiumPanda, PremiumPig, PremiumRabbit, PremiumSheep, PremiumSnowGolem, PremiumSquid, PremiumTurtle, PremiumVillager, PremiumWolf.
  - conferir quais entidades existem em 1.20.1 e quais metodos/constructors mudaram.
- [ ] `PremiumEntityTags.java`
  - tags/identificacao de entidades premium.
- [ ] `src/main/resources/assets/magicworld/textures/gui/mobs/`
  - icones/previews dos mobs usados nos menus.

### Eventos de Jogo e Poderes

- [ ] `src/main/java/com/magicworld/event/AuraEvents.java`
  - poderes do jogador, protecao, quebra forte, retorno ao local da morte.
- [ ] `src/main/java/com/magicworld/event/MobEvents.java`
  - transformacoes/interacoes com mobs.
- [ ] `src/main/java/com/magicworld/event/PlayerJoinEvents.java`
  - logica no login.
- [ ] `src/main/java/com/magicworld/event/PremiumBlocks.java`
  - blocos/recompensas premium.
- [ ] `src/main/java/com/magicworld/event/PremiumEnemies.java`
  - inimigos premium.
- [ ] `src/main/java/com/magicworld/util/ChatMessages.java`
  - mensagens auxiliares.

### Title Screen, Backgrounds e GUI

- [ ] `src/main/java/com/magicworld/client/MagicWorldTitleScreen.java`
  - tela inicial customizada.
- [ ] `src/main/java/com/magicworld/client/MagicWorldGraphicsProfile.java`
  - perfis Fraco/Medio/Forte e aplicacao de opcoes graficas.
- [ ] `src/main/java/com/magicworld/client/MagicWorldDistantHorizonsButton.java`
  - integracao visual com Distant Horizons.
- [ ] assets GUI:
  - `src/main/resources/assets/magicworld/textures/gui/menu_background.png`
  - `src/main/resources/assets/magicworld/textures/gui/title_*.png`
  - `src/main/resources/assets/magicworld/textures/gui/backgrounds/`
  - `src/main/resources/assets/magicworld/textures/gui/previews/`
  - revisar formato e nomes aceitos em 1.20.1.

### Resource Pack, Models e Lang

- [ ] `src/main/resources/assets/magicworld/lang/en_us.json`
- [ ] `src/main/resources/assets/magicworld/lang/pt_br.json`
- [ ] `src/main/resources/assets/magicworld/models/item/`
- [ ] `src/main/resources/assets/magicworld/textures/item/`
- [ ] `src/main/resources/assets/magicworld/textures/entity/`
- [ ] `src/main/resources/assets/magicworld/equipment/`
  - conferir diferenca de formato de equipment assets entre 26.1.2 e 1.20.1.
- [ ] `src/main/resources/pack.mcmeta`
  - trocar `pack_format` para o valor correto de Minecraft 1.20.1.

### Mixins e Compatibilidade Grafica

- [ ] `src/main/resources/magicworld.mixins.json`
- [ ] `src/main/java/com/magicworld/mixin/`
  - CubeMapMagicWorldDepthMixin
  - IrisConfigMagicWorldMixin
  - MagicWorldScreenBackgroundMixin
  - RenderSetupSamplerCompatMixin
  - SodiumConfigBuilderMagicWorldMixin
  - SodiumDonationButtonCompatMixin
  - SodiumExternalPageEntryMagicWorldMixin
  - SodiumVideoSettingsScreenMagicWorldLayoutMixin
- [ ] em 1.20.1 revisar todos os targets/classes/metodos porque Sodium/Iris/Oculus/Embeddium usam nomes diferentes.
- [ ] se usar Forge 1.20.1, considerar Embeddium/Oculus em vez de Sodium/Iris, dependendo da compatibilidade real.

### Diferencas de Imports/API Que Devem Ser Revisadas

- [ ] Networking:
  - 26.1.2 usa `CustomPacketPayload`, `StreamCodec`, `PayloadRegistrar`.
  - 1.20.1 pode usar `SimpleChannel`, `FriendlyByteBuf` e handlers diferentes.
- [ ] Render GUI:
  - 26.1.2 usa `GuiGraphicsExtractor` em varias telas.
  - 1.20.1 tende a usar `GuiGraphics`.
- [ ] NBT:
  - 26.1.2 usa `ValueInput`/`ValueOutput` em algumas entidades.
  - 1.20.1 usa `CompoundTag`.
- [ ] Spawn de entidades:
  - 26.1.2 usa `EntitySpawnReason`.
  - 1.20.1 pode usar `MobSpawnType`.
- [ ] Respawn:
  - revisar `ServerPlayer.RespawnConfig` e `LevelData.RespawnData`.
- [ ] Identifiers:
  - 26.1.2 usa `net.minecraft.resources.Identifier`.
  - 1.20.1 pode usar `ResourceLocation`.
- [ ] Eventos:
  - revisar pacotes `net.neoforged.neoforge.event.*` vs Forge/NeoForge 1.20.1.
- [ ] Registros:
  - revisar `DeferredRegister`, holders e nomes de registries.

### README e Documentacao

- [ ] `README.md`
  - portar descricao do projeto, sistemas, instalacao e releases para 1.20.1.
  - atualizar trecho all-in-one para os mods 1.20.1 realmente embutidos.
  - atualizar compatibilidade, requisitos e nomes de arquivos.
  - manter secao do instalador proprio para TLauncher, mas alterar NeoForge 26.1.2 para Forge 1.20.1.
  - atualizar banners se o visual do instalador mudar.
- [ ] Instalador TLauncher para o downgrade Forge 1.20.1
  - origem atual: `scripts/install-magicworld-tlauncher.ps1`
  - criar versao apontando para Forge 1.20.1 e jars Forge 1.20.1.
  - detectar `.minecraft`, copiar all-in-one em `mods`, resource pack em `resourcepacks`, shader pack em `shaderpacks`.
  - nao editar executavel do TLauncher, nao burlar login/autenticacao e nao redistribuir Minecraft pronto.
  - manter memoria 8 GB quando houver arquivo/config editavel com seguranca.
- [ ] Banners do instalador
  - origem atual: `screenshots/banner_instalador_magicworld.png`
  - origem atual: `screenshots/banner_tlauncher_magicworld.png`
  - usar como base visual no README 1.20.1 ou recriar em `screenshots/`.
- [ ] `credits.txt`
  - revisar creditos/licencas de mods embutidos 1.20.1.
- [ ] `CODEX_HANDOFF.md`
  - manter este checklist atualizado durante o downgrade.

## Proximo Passo Imediato

1. Inspecionar `tmp/casa_principal_do_personagem.zip`.
2. Inspecionar `tmp/mapa_aproveitar_castelo.zip`.
3. Implementar nova casa/terreno/fazendas em etapas.
4. Substituir castelo procedural por castelo novo a 40 blocos a direita do portal.
5. Ajustar portal para visual do print e gramado livre 5 blocos ao redor.
6. Ajustar dragao para rota casa/portal/castelo e spawn tardio.
7. Criar pop-up unico de aviso de demora inicial.
8. Rodar `.\gradlew.bat build magicWorldAllInOneJar`.
9. Deixar `run/mods` somente com o all-in-one e avisar o usuario para testar o jogo.

## Atualizacao - 2026-05-25 13:44

- Repositorio antigo Forge 1.20.1 `C:\Users\guilh\Desktop\MinecraftProjects\MagicWorld-MagicWand_Mod` foi restaurado para `origin/main`.
- HEAD confirmado em `015ba5b docs: atualiza handoff do downgrade`.
- `git status --short` ficou limpo apos `git reset --hard origin/main` e `git clean -fd`.
- Artefatos locais do port que estavam nesse repositorio foram removidos, incluindo `Mods_para_allinone_minecraft1.20.1/` e `src/main/java/com/example/examplemod/entity/PeacefulDragon.java`.
- A partir de agora o port nao deve ser feito nesse repositorio antigo finalizado. O port deve ir para um novo repositorio GitHub dedicado.
- `gh` nao esta instalado nesta maquina, entao a criacao automatica do novo repositorio GitHub ainda nao foi executada por CLI.

## Atualizacao - 2026-05-25 13:47

- Usuario definiu o repositorio oficial do port Forge 1.20.1 como `https://github.com/gorpo/Magic-World_ultimate-Forge1.20.1`.
- Repositorio remoto confirmado existente com branch `main`.
- Pasta local clonada: `C:\Users\guilh\Desktop\MinecraftProjects\Magic-World_ultimate-Forge1.20.1`.
- O repositorio antigo `MagicWorld-MagicWand_Mod` deve continuar preservado/restaurado; nao usar para o port.
- Proximo passo: copiar a base atual do projeto para o novo repo do port, excluindo caches/build/run/tmp/jars locais, ajustar README/docs para deixar claro que e port Forge 1.20.1 em andamento, commitar e enviar para `main`.

## Atualizacao - 2026-05-25 13:48

- Base atual do projeto foi copiada para `C:\Users\guilh\Desktop\MinecraftProjects\Magic-World_ultimate-Forge1.20.1`.
- Copia excluiu `.git`, `.gradle`, `build`, `run`, `tmp`, caches locais e jars `neoforge-*.jar`.
- README do repo Forge recebeu aviso no topo informando que este e o repositorio oficial do port Forge 1.20.1 e que a base ainda vem do NeoForge 26.1.2.
- Proximo passo imediato: commitar e enviar para `origin/main` do repo `Magic-World_ultimate-Forge1.20.1`.

## Atualizacao - 2026-05-25 13:49

- Importacao inicial do port enviada para `https://github.com/gorpo/Magic-World_ultimate-Forge1.20.1`.
- Branch enviado: `main`.
- Commit enviado: `c8bfa41 chore: importa base do port Forge 1.20.1`.
- Repositorio antigo `MagicWorld-MagicWand_Mod` continua limpo e preservado.
- Proximo passo real do port: adaptar Gradle/mod metadata/APIs de NeoForge 26.1.2 para Forge 1.20.1 seguindo `Downgrade_1.20.1.txt`.

## Atualizacao de Execucao - 2026-05-25 14:03 America/Sao_Paulo

Pedido adicional executado no projeto NeoForge 26.1.2:

- Corrigido metro subterraneo:
  - tunel agora e uma galeria selada com piso, paredes e teto de deepslate;
  - trilhos energizados ficam conectados por uma curva real usando `RAIL` curvo no canto;
  - escadas de acesso da casa/castelo ganharam paredes, teto e luzes;
  - estacoes receberam luz extra;
  - area do tunel fica protegida por limpeza periodica de monstros hostis.
- Corrigido menu de criacao de mundo:
  - quando qualquer opcao Magic World exige comandos, o botao de comandos/cheats do Minecraft e sincronizado para ON;
  - toggles de Casa/Castelo/Aura forcam nova sincronizacao de comandos.
- Corrigidos baus/cama da casa importada:
  - baus/cama agora usam busca por posicao interna com teto acima;
  - baus/camas sem teto na area da casa sao removidos durante a decoracao.
- Bonus novo criado:
  - Galeria Arcana secreta no caminho do metro, com mesa de encantamento, luzes, livros e guardiao allay.
- `README.md` atualizado com essas mudancas.
- Rodado `.\gradlew.bat build magicWorldAllInOneJar` com sucesso.
- `run/mods` ficou somente com `MagicWorld-Ultimate-NeoForge-26.1.2-1.2.0-all-in-one.jar` atualizado.

Observacao: testar em mundo novo. Mundo ja gerado antes nao corrige estruturas antigas automaticamente.

## Atualizacao de Documentacao - 2026-05-25 14:15 America/Sao_Paulo

- README e wiki receberam secao pratica dos mods built in do all-in-one:
  - JourneyMap: `J` para mapa em tela cheia, minimapa e waypoints;
  - Effortless Building: `ALT` para menu/uso de construcao rapida;
  - Controllable: suporte a controle/gamepad e configuracao em controles;
  - Iris Shaders: Shader Packs;
  - Sodium: menu de video/performance;
  - Distant Horizons: botao Horizontes Distantes;
  - Entity Texture Features / Entity Model Features: suporte visual para packs.
- Imagens externas baixadas para `screenshots/`:
  - `external_journeymap_minimap.png`
  - `external_journeymap_fullscreen.png`
  - `external_effortless_mirror_settings.png`
  - `external_controllable_controller.webp`
- Fontes usadas para referencia/imagens: JourneyMap Docs, CurseForge/Effortless Building e MrCrayfish/Controllable.
- Proximo passo: sincronizar com repo do port e subir README/wiki/imagens online.
