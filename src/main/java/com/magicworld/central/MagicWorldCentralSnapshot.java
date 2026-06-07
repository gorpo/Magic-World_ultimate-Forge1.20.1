package com.magicworld.central;

import java.util.List;

public record MagicWorldCentralSnapshot(
        String name,
        String mode,
        int plannedResidents,
        int detectedMagicPeople,
        int detectedVillagers,
        int detectedGolems,
        int detectedOutOfArea,
        List<MagicWorldCentralResidentPlan> residents,
        List<SectorStatus> sectors,
        List<String> detectedNames
) {
    public List<Section> sections() {
        return List.of(
                section("Varinha", "Ativacao e foco magico",
                        "Abrir central da varinha",
                        "Recarregar brilho arcano",
                        "Preparar efeitos premium",
                        "Atalho seguro para poderes leves"),
                section("Tempo", "Controle de dia e noite",
                        "Virar dia no Overworld",
                        "Virar noite para aventuras",
                        "Limpar chuva quando precisar",
                        "Chamar chuva sem trovoes"),
                section("Portais", "Rotas magicas da propriedade",
                        "Portal premium preservado",
                        "Nether, Fim e Gateway em revisao",
                        "Retorno para o Overworld",
                        "Spawn seguro reutilizado"),
                section("Fazendas", "Cuidado com comida e animais",
                        "Villagers vanilla cuidam da lavoura",
                        "Currais com portoes funcionais",
                        "Comida animal em producao",
                        "Iluminacao e decoracao ao redor"),
                section("Castelo", "Centro de protecao do MagicWorld",
                        "Torres observam a propriedade",
                        "Baus e salas premium",
                        "Caminho entre casa e castelo",
                        "Area pronta para novas etapas"),
                section("Biomas", "Viagens e descobertas",
                        "Teleporte por bioma premium",
                        "Estruturas especiais",
                        "Exploracao guiada",
                        "Retorno seguro planejado"),
                section("Encantos", "Efeitos magicos do jogador",
                        "Brilho, particulas e protecao",
                        "Transformacoes em preview",
                        "Armaduras premium",
                        "Poderes ajustados por etapa"),
                section("Mundo", "Estado atual da magia",
                        "Entidades vanilla preservadas",
                        "Sistemas pesados antigos fora",
                        "Menus prontos para nova base",
                        "Area detectada: " + detectedTotal()),
                section("Premium", "Recursos ligados ao portal",
                        "Varinha magica",
                        "Armadura draconica",
                        "Ferramentas e atalhos",
                        "Pacotes visuais do portal"),
                section("Proximas etapas", "Base limpa para evoluir",
                        "Sem peso extra na base",
                        "Acoes leves primeiro",
                        "Funcoes novas por partes",
                        "Sem mensagens tecnicas na tela")
        );
    }

    public int detectedTotal() {
        return detectedMagicPeople + detectedVillagers + detectedGolems;
    }

    private Section section(String title, String subtitle, String... lines) {
        return new Section(title, subtitle, List.of(lines));
    }

    public record SectorStatus(MagicWorldCentralSector sector, String status, String note) {
    }

    public record Section(String title, String subtitle, List<String> lines) {
    }
}
