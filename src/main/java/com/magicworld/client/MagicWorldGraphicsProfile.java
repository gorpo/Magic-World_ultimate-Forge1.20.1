package com.magicworld.client;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.network.chat.Component;

public enum MagicWorldGraphicsProfile {
    ULTRA_FRACO(
            "Ultra fraco",
            4,
            4,
            ParticleStatus.MINIMAL,
            GraphicsStatus.FAST,
            CloudStatus.OFF,
            0.5D,
            false,
            60
    ),
    FRACO(
            "Fraco",
            6,
            5,
            ParticleStatus.MINIMAL,
            GraphicsStatus.FAST,
            CloudStatus.OFF,
            0.6D,
            false,
            75
    ),
    INTERMEDIARIO(
            "Intermediario",
            8,
            6,
            ParticleStatus.DECREASED,
            GraphicsStatus.FAST,
            CloudStatus.FAST,
            0.75D,
            true,
            90
    ),
    MEDIO(
            "Medio",
            10,
            8,
            ParticleStatus.DECREASED,
            GraphicsStatus.FANCY,
            CloudStatus.FAST,
            0.85D,
            true,
            120
    ),
    FORTE(
            "Forte",
            14,
            10,
            ParticleStatus.ALL,
            GraphicsStatus.FANCY,
            CloudStatus.FANCY,
            1.0D,
            true,
            144
    ),
    ULTRA_FORTE(
            "Ultra forte",
            20,
            12,
            ParticleStatus.ALL,
            GraphicsStatus.FABULOUS,
            CloudStatus.FANCY,
            1.0D,
            true,
            240
    );

    private final String label;
    private final int renderDistance;
    private final int simulationDistance;
    private final ParticleStatus particles;
    private final GraphicsStatus graphicsStatus;
    private final CloudStatus clouds;
    private final double entityDistance;
    private final boolean ambientOcclusion;
    private final int framerateLimit;

    MagicWorldGraphicsProfile(
            String label,
            int renderDistance,
            int simulationDistance,
            ParticleStatus particles,
            GraphicsStatus graphicsStatus,
            CloudStatus clouds,
            double entityDistance,
            boolean ambientOcclusion,
            int framerateLimit
    ) {
        this.label = label;
        this.renderDistance = renderDistance;
        this.simulationDistance = simulationDistance;
        this.particles = particles;
        this.graphicsStatus = graphicsStatus;
        this.clouds = clouds;
        this.entityDistance = entityDistance;
        this.ambientOcclusion = ambientOcclusion;
        this.framerateLimit = framerateLimit;
    }

    public String label() {
        return label;
    }

    public String description() {
        return "Render "
                + renderDistance
                + " chunks, simulacao "
                + simulationDistance
                + ", particulas "
                + particles.name().toLowerCase()
                + ", grafico "
                + graphicsStatus.name().toLowerCase()
                + ".";
    }

    public void apply(
            Minecraft minecraft
    ) {
        minecraft.options.renderDistance()
                .set(renderDistance);
        minecraft.options.simulationDistance()
                .set(simulationDistance);
        minecraft.options.particles()
                .set(particles);
        minecraft.options.graphicsMode()
                .set(graphicsStatus);
        minecraft.options.cloudStatus()
                .set(clouds);
        minecraft.options.entityDistanceScaling()
                .set(entityDistance);
        minecraft.options.ambientOcclusion()
                .set(ambientOcclusion);
        minecraft.options.framerateLimit()
                .set(framerateLimit);
        minecraft.options.save();

        if (minecraft.player != null) {
            minecraft.player.sendSystemMessage(
                    Component.literal(
                            "Perfil grafico Magic World aplicado: "
                                    + label
                    )
            );
        }
    }
}
