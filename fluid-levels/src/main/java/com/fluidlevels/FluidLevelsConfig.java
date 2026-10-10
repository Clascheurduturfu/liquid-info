package com.fluidlevels;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class FluidLevelsConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance()
			.getConfigDir().resolve("fluidlevels.json");

	// Persisted state
	public boolean enabled = true;
	public boolean sourceOnly = true;
	public boolean woolSourceMode = false;

	// Singleton instance
	private static FluidLevelsConfig INSTANCE;

	public static FluidLevelsConfig getInstance() {
		if (INSTANCE == null) {
			load();
		}
		return INSTANCE;
	}

	public static void load() {
		if (Files.exists(CONFIG_PATH)) {
			try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
				INSTANCE = GSON.fromJson(reader, FluidLevelsConfig.class);
				if (INSTANCE == null) {
					INSTANCE = new FluidLevelsConfig();
				}
				FluidLevelsClient.LOGGER.info("Fluid Levels config loaded from {}", CONFIG_PATH);
			} catch (Exception e) {
				FluidLevelsClient.LOGGER.error("Failed to load Fluid Levels config, using defaults", e);
				INSTANCE = new FluidLevelsConfig();
			}
		} else {
			INSTANCE = new FluidLevelsConfig();
			save(); // Create the file with defaults on first run
		}
	}

	public static void save() {
		if (INSTANCE == null) {
			INSTANCE = new FluidLevelsConfig();
		}
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(INSTANCE, writer);
			}
		} catch (Exception e) {
			FluidLevelsClient.LOGGER.error("Failed to save Fluid Levels config", e);
		}
	}
}
