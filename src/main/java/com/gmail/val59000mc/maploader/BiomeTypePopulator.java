package com.gmail.val59000mc.maploader;

import com.gmail.val59000mc.UhcCore;
import me.tecc.uhccoreplus.util.UCPLogger;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class BiomeTypePopulator extends BlockPopulator {
    private Chunk current;
    private UCPLogger logger = UCPLogger.global();
    @Override
    public void populate(World world, Random random, Chunk chunk) {
        current = chunk;
        for (int x = 1; x < 15; x++) {
            for (int z = 1; z < 15; z++) {

                placeReplacement(x, z);
            }
        }
        current = null;
    }

    private void placeReplacement(int x, int z) {
        if (current == null) {
            logger.error("Trying to place replacement but current chunk is null. Skipping");
            return;
        }
        Block block = current.getBlock(x, 1, z);
        Biome replacement = getReplacementBiome(block.getBiome());

        if (UhcCore.getVersion() < 16) {
            if (replacement != null) {
                block.setBiome(replacement);
            }
        } else {
            for (int y = 0; y < 200; y++) {
                block = current.getBlock(x, y, z);

                if (replacement != null) {
                    block.setBiome(replacement);
                }
            }
        }
    }

    private Biome getReplacementBiome(Biome biome) {
        switch (biome) {
            case OCEAN:
            case FROZEN_OCEAN:
            case WARM_OCEAN:
            case LUKEWARM_OCEAN:
            case COLD_OCEAN:
                return Biome.PLAINS;
            case DEEP_OCEAN:
            case DEEP_FROZEN_OCEAN:
            case DEEP_WARM_OCEAN:
            case DEEP_LUKEWARM_OCEAN:
            case DEEP_COLD_OCEAN:
                return Biome.FOREST;
        }

        return null;
    }

}