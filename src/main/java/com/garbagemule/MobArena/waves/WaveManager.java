package com.garbagemule.MobArena.waves;

import com.garbagemule.MobArena.framework.Arena;
import com.garbagemule.MobArena.waves.enums.WaveBranch;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

public class WaveManager
{
    private Arena arena;
    private ConfigurationSection section;

    private Wave defaultWave, currentWave, nextWave;
    private TreeSet<Wave> recurrentWaves, singleWaves;

    private int wave, finalWave;
    private Random random = new Random();

    public WaveManager(Arena arena, ConfigurationSection section) {
        this.arena     = arena;
        this.section   = section;
        this.wave      = 0;
        this.finalWave = 0;

        reloadWaves();
    }

    public void reset() {
        reloadWaves();
        wave = 0;
        determineNextWave();
    }

    public void reloadWaves() {
        ConfigurationSection rConfig = section.getConfigurationSection("recurrent");
        ConfigurationSection sConfig = section.getConfigurationSection("single");

        recurrentWaves = WaveParser.parseWaves(arena, rConfig, WaveBranch.RECURRENT);
        singleWaves    = WaveParser.parseWaves(arena, sConfig, WaveBranch.SINGLE);

        // getParent() => go back to the arena-node to access settings
        finalWave = section.getParent().getInt("settings.final-wave", 0);

        if (recurrentWaves.isEmpty()) {
            if (singleWaves.isEmpty()) {
                arena.getPlugin().getLogger().warning("Found no waves for arena " + arena.configName() + ", using default wave.");
            } else {
                arena.getPlugin().getLogger().info("Found no 'recurrent' waves for arena " + arena.configName() + ", using default wave.");
            }
            defaultWave = WaveParser.createDefaultWave();
        } else {
            if (singleWaves.isEmpty()) {
                arena.getPlugin().getLogger().info("Found no 'single' waves for arena " + arena.configName());
            }
            defaultWave = recurrentWaves.first();
        }
    }

    private void determineNextWave() {
        // Single waves take precedence over recurrent waves
        List<Wave> singles = findSingleWaveCandidates(wave + 1);
        if (!singles.isEmpty()) {
            nextWave = pickRandomWave(singles);
            return;
        }

        List<Wave> recurrents = findRecurrentWaveCandidates(wave + 1);
        if (!recurrents.isEmpty()) {
            nextWave = pickRandomWave(recurrents);
        } else {
            nextWave = defaultWave.copy();
        }
    }

    private List<Wave> findSingleWaveCandidates(int wave) {
        List<Wave> candidates = new ArrayList<>(singleWaves.size());
        for (Wave w : singleWaves) {
            if (w.matches(wave)) {
                candidates.add(w);
            }
        }

        return candidates;
    }

    private List<Wave> findRecurrentWaveCandidates(int wave) {
        List<Wave> matches = new ArrayList<>(recurrentWaves.size());
        for (Wave w : recurrentWaves) {
            if (w.matches(wave)) {
                matches.add(w);
            }
        }

        int priority = 0;
        for (Wave w : matches) {
            if (w.getPriority() > priority) {
                priority = w.getPriority();
            }
        }

        List<Wave> candidates = new ArrayList<>(matches.size());
        for (Wave w : matches) {
            if (w.getPriority() == priority) {
                candidates.add(w);
            }
        }

        return candidates;
    }

    private Wave pickRandomWave(List<Wave> candidates) {
        if (candidates.size() == 1) {
            return candidates.get(0).copy();
        }

        int index = random.nextInt(candidates.size());
        return candidates.get(index).copy();
    }

    /**
     * Increment the wave number and get the next Wave to be spawned.
     * Note that this method is a mutator.
     * @return the next Wave
     */
    public Wave next() {
        currentWave = nextWave;

        wave++;
        determineNextWave();

        return currentWave;
    }

    /**
     * Get the next Wave to be spawned. This is an accessor and does not
     * advance the "counter". Note that the Wave objects, however, are
     * mutable.
     * @return the next Wave
     */
    public Wave getNext() {
        return nextWave;
    }

    /**
     * Get the current wave that's being used.
     * Note that the current wave might not have spawned yet.
     * @return the current wave
     */
    public Wave getCurrent() {
        return currentWave;
    }

    /**
     * Get the current wave number.
     * @return the current wave number
     */
    public int getWaveNumber() {
        return wave;
    }

    /**
     * Get the final wave number.
     * @return the final wave number
     */
    public int getFinalWave() {
        return finalWave;
    }
}
