package com.fluidlevels;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FluidLevelsClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("FluidLevels");

	private static KeyMapping toggleKey;
	private static KeyMapping toggleSourceOnlyKey;
	private static KeyMapping toggleWoolModeKey;

	public static boolean isEnabled() {
		return FluidLevelsConfig.getInstance().enabled;
	}

	public static boolean isSourceOnly() {
		return FluidLevelsConfig.getInstance().sourceOnly;
	}

	public static boolean isWoolSourceMode() {
		return FluidLevelsConfig.getInstance().woolSourceMode;
	}

	@Override
	public void onInitializeClient() {
		LOGGER.info("Fluid Levels mod initialized");

		// Load persisted config
		FluidLevelsConfig.load();

		// F7 = enable/disable the mod
		toggleKey = KeyBindingHelper.registerKeyBinding(
				new KeyMapping(
						"key.fluidlevels.toggle",
						InputConstants.Type.KEYSYM,
						GLFW.GLFW_KEY_F7,
						"category.fluidlevels.main"));

		// F5 = toggle source only
		toggleSourceOnlyKey = KeyBindingHelper.registerKeyBinding(
				new KeyMapping(
						"key.fluidlevels.toggle_source_only",
						InputConstants.Type.KEYSYM,
						GLFW.GLFW_KEY_F5,
						"category.fluidlevels.main"));

		// F6 = toggle wool mode
		toggleWoolModeKey = KeyBindingHelper.registerKeyBinding(
				new KeyMapping(
						"key.fluidlevels.toggle_wool_mode",
						InputConstants.Type.KEYSYM,
						GLFW.GLFW_KEY_F6,
						"category.fluidlevels.main"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			FluidLevelsConfig config = FluidLevelsConfig.getInstance();

			while (toggleKey.consumeClick()) {
				config.enabled = !config.enabled;
				FluidLevelsConfig.save();
				if (client.player != null) {
					client.player.displayClientMessage(
							Component.literal(
									config.enabled ? "Fluid levels: ON" : "Fluid levels: OFF"),
							true);
				}
			}
			while (toggleSourceOnlyKey.consumeClick()) {
				config.sourceOnly = !config.sourceOnly;
				FluidLevelsConfig.save();
				if (client.player != null) {
					client.player.displayClientMessage(
							Component.literal(
									config.sourceOnly
											? "Fluid levels: source only (less lag)"
											: "Fluid levels: all levels"),
							true);
				}
			}
			while (toggleWoolModeKey.consumeClick()) {
				config.woolSourceMode = !config.woolSourceMode;
				FluidLevelsConfig.save();
				if (client.player != null) {
					client.player.displayClientMessage(
							Component.literal(
									config.woolSourceMode
											? "Fluid levels: source wool mode (lava=red, water=blue)"
											: "Fluid levels: original text mode"),
							true);
				}
			}
		});

		WorldRenderEvents.AFTER_ENTITIES.register(FluidLevelRenderer::render);
	}
}

