# Magic World Wiki

Wiki local baseada no README e no codigo atual do projeto NeoForge 26.1.2.

## Visao Geral

Magic World Ultimate transforma o inicio do mundo em uma propriedade magica com casa, portal, castelo, aliados, dragao pacifico, recursos premium e ambientacao de fantasia.

O pacote principal e o jar all-in-one:

```text
MagicWorld-Ultimate-NeoForge-26.1.2-1.2.0-all-in-one.jar
```

Ele deve ficar sozinho em `run/mods` durante os testes locais.

## Criacao de Mundo

Na tela de criacao de mundo existe o painel **Magic World**.

Padroes atuais:

- Portal/propriedade inicial: ON.
- Castelo: ON.
- Aura: ON.
- Cheats/comandos: ON automatico.
- Modo inicial: Survival, salvo escolha diferente no painel.
- Dificuldade inicial: Normal, salvo escolha diferente no painel.

Use mundo novo para testar geracao de estruturas. Mundos antigos preservam estruturas ja geradas.

## Loading Inicial

O loading inicial abre logo no login quando a propriedade Magic World sera criada.

Mensagens esperadas incluem:

- Carregando casa.
- Carregando fazendas e animais.
- Carregando portal.
- Carregando castelo.
- Carregando dragao.
- Finalizando estruturas do Magic World.

A tela fecha automaticamente ao concluir.

## Propriedade Inicial

A propriedade inicial e criada em etapas para reduzir travamentos:

- casa principal importada;
- baus internos cheios;
- aliados da casa;
- fazendas, animais e estabulos;
- portal premium;
- castelo importado;
- dragao pacifico;
- vida extra, patrulhas e ambientacao magica.

## Casa

A casa tem:

- spawn do jogador ao lado da cama;
- baus internos com equipamentos, comida, recursos, armadura Draconic Aether e varinha magica;
- baus e cama so devem ser posicionados em locais internos com teto;
- aliados magicos nomeados;
- iluminacao interna extra;
- fazendas e estabulos iluminados no entorno.

Armor stands nao devem ficar na casa. Os pedestais de armaduras ficam no castelo.

## Portal Premium

O portal fica no gramado e ativa o visual premium somente quando o jogador passa pelo centro real do portal.

Nao deve ativar por:

- chegar perto;
- encostar nas laterais;
- ficar na frente sem atravessar;
- clicar em decoracoes laterais.

Ordem dos resource packs premium:

```text
1. MagicWorldResource_1.20.1-256x.zip
2. MagicWorldResource_1.20.1-models.zip
3. MagicWorldResource_1.20.1-addon.zip
4. MagicWorldResource_1.20.1-bonus.zip
```

## Castelo

O castelo importado contem:

- baus internos cheios;
- pelo menos 6 baus com armadura Draconic Aether e varinha magica;
- armor stands internos com sets;
- aliados magicos nomeados;
- guardiao;
- estandartes na frente;
- 3 soldados montados em cavalos patrulhando um pequeno perimetro na fachada;
- camara selada de morcegos.

## Trilhos Subterraneos

Existe um sistema subterraneo de trilhos entre casa e castelo.

Caracteristicas:

- estacao no subsolo da casa;
- estacao no subsolo do castelo;
- escadas de acesso com vao alto;
- galeria selada com piso, paredes e teto;
- trilhos energizados conectados por curva real;
- iluminacao forte com sea lantern, glowstone e end rods;
- protecao que remove monstros hostis dentro da area do tunel;
- redstone blocks alimentando segmentos;
- nenhum minecart nasce no trilho;
- cada estacao tem 3 baus com pecas para expandir o sistema.

Itens dos baus de trilho incluem:

- minecart;
- chest minecart;
- hopper minecart;
- furnace minecart;
- TNT minecart;
- rail;
- powered rail;
- detector rail;
- activator rail;
- redstone block;
- redstone torch;
- lever.

## Aliados e Interacao

Personagens aliados sao entidades nomeadas e persistentes.

Interacoes:

- clique normal: seguir ou parar de seguir;
- Shift + clique: pedir ajuda.

A ajuda depende do nome/funcao:

- ferreiro/oficina: recursos, diamantes e experiencia;
- cozinheiro/chef: comida;
- bibliotecaria: experiencia, livros e lapis;
- agricultor/tratador: sementes, comida de fazenda e leads;
- outros aliados: suprimentos basicos e protecao.

Ao ajudar, o aliado tambem passa a seguir o jogador.

## Ambientacao Magica

Elementos atuais:

- aves/parrots ao redor da casa e castelo;
- allays como luzes/guias;
- fumaca de campfire;
- amethyst blocks;
- end rods;
- lanternas;
- redstone lamps;
- particulas de enchant, end rod, portal e smoke;
- postes magicos externos;
- santuario secreto subterraneo perto do portal;
- galeria arcana secreta no tunel do metro, com mesa de encantamento, livros e guardiao allay.

## Dragao Pacifico

O `magicworld:peaceful_dragon` e um guardiao visual:

- nao ataca;
- nao quebra blocos;
- fica na area da propriedade;
- voa baixo e patrulha casa, portal e castelo.

## Aura

Aura inicia ON no menu Magic World.

Ela aplica protecoes e poderes ao jogador quando ativa, incluindo:

- resistencia;
- regeneracao;
- velocidade;
- protecao contra queda;
- ajuda em combate/interacao conforme eventos do mod.

## Menu Principal

Tecla:

```text
H
```

Abre o menu **MagicWorld - Magic Wand**, com sistemas de:

- varinha;
- blocos;
- criaturas;
- comandos;
- graficos;
- portais;
- ferramentas;
- armaduras;
- NPCs;
- particulas;
- clima;
- tempo;
- bosses;
- dungeons;
- montarias.

## Mods Built In E Atalhos

O all-in-one tambem inclui mods de terceiros reais via Jar-in-Jar. Eles nao precisam ser instalados separados, mas seus menus e atalhos continuam existindo.

| Mod | Atalho/menu principal | Uso pratico |
| --- | --- | --- |
| **JourneyMap** | `J` abre o mapa em tela cheia. O minimapa aparece no HUD. | Criar waypoints, localizar casa/castelo/portal, navegar por coordenadas e marcar pontos importantes. |
| **Effortless Building** | `ALT` abre/usa o menu de construcao rapida. | Construir linhas, paredes, espelhos, repeticoes e preenchimentos sem colocar bloco por bloco. |
| **Controllable** | Conectar controle/gamepad e revisar `Opcoes > Controles`. | Jogar Java com controle, mapear botoes e ajustar sensibilidade. |
| **Iris Shaders** | `Opcoes > Video > Shader Packs` ou ativacao pelo portal premium. | Usar o Shader Pack Magic World. |
| **Sodium** | `Opcoes > Video`. | Ajustar desempenho e renderizacao. |
| **Distant Horizons** | Botao `Horizontes Distantes` no menu grafico/Video Settings. | Configurar LODs e mundo visivel a longa distancia. |
| **Entity Texture Features / Entity Model Features** | Sem tecla principal obrigatoria. | Ativar recursos avancados de resource packs/modelos de entidades. |

Imagens de referencia salvas em `screenshots/`:

- `external_journeymap_minimap.png`
- `external_journeymap_fullscreen.png`
- `external_effortless_mirror_settings.png`
- `external_controllable_controller.webp`

Fontes oficiais/referencias: JourneyMap Docs, Effortless Building no CurseForge e Controllable/MrCrayfish.

## Itens Principais

Itens do mod:

- `magicworld:varinha_magica`
- `magicworld:draconic_aether_helmet`
- `magicworld:draconic_aether_chestplate`
- `magicworld:draconic_aether_leggings`
- `magicworld:draconic_aether_boots`

## Testes Recomendados

Para testar uma build nova:

1. Confirme que `run/mods` tem somente o jar all-in-one.
2. Rode o cliente.
3. Crie mundo novo pelo painel Magic World.
4. Confira se o loading aparece antes da geracao pesada.
5. Verifique casa, portal, castelo e dragao.
6. Atravesse o centro do portal para ativar visual premium.
7. Confira os baus internos da casa e do castelo.
8. Confira armor stands somente dentro do castelo.
9. Teste a patrulha montada na frente do castelo.
10. Teste o metro subterraneo casa/castelo.
11. Teste clique e Shift+clique nos aliados.

## Observacoes de Desenvolvimento

- O handoff vivo fica em `CODEX_HANDOFF.md`.
- O checklist do port Forge 1.20.1 fica em `Downgrade_1.20.1.txt`.
- O port so deve ser executado quando o projeto NeoForge atual estiver concluido ou quando o usuario mandar.
