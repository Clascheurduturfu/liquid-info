package com.example.clearlava;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_304; // KeyBinding
import net.minecraft.class_3612; // Fluids
import net.minecraft.class_4696; // RenderLayers
import net.minecraft.class_1921; // RenderLayer
import net.minecraft.class_2561; // Text
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ClearLavaMod implements ClientModInitializer {
    private static class_304 toggleTextureKey;
    private static class_304 toggleOpaqueKey;
    private static Path configPath;

    @Override
    public void onInitializeClient() {
        System.out.println("[ClearLava] Mod initializing...");
        try {
            // Setup config path: config/clearlava.txt
            configPath = FabricLoader.getInstance().getConfigDir().resolve("clearlava.txt");

            // Read config if it exists
            if (Files.exists(configPath)) {
                try {
                    java.util.List<String> lines = Files.readAllLines(configPath);
                    for (String line : lines) {
                        String[] parts = line.split(":", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            boolean value = Boolean.parseBoolean(parts[1].trim());
                            if (key.equals("waterTexture")) {
                                LavaRenderHandler.useWaterTexture = value;
                            } else if (key.equals("opaqueLava")) {
                                LavaRenderHandler.useOpaqueLava = value;
                            }
                        }
                    }
                    System.out.println("[ClearLava] Loaded config: waterTexture=" + LavaRenderHandler.useWaterTexture + ", opaqueLava=" + LavaRenderHandler.useOpaqueLava);
                } catch (Exception e) {
                    System.err.println("[ClearLava] Failed to read config file:");
                    e.printStackTrace();
                }
            }

            // Apply render layer based on config
            updateRenderLayer();

            // Setup custom fluid render handlers
            FluidRenderHandler waterHandler = FluidRenderHandlerRegistry.INSTANCE.get(class_3612.field_15910);
            FluidRenderHandler lavaHandler = FluidRenderHandlerRegistry.INSTANCE.get(class_3612.field_15908);
            if (waterHandler != null && lavaHandler != null) {
                LavaRenderHandler customHandler = new LavaRenderHandler(lavaHandler, waterHandler);
                FluidRenderHandlerRegistry.INSTANCE.register(class_3612.field_15908, customHandler);
                FluidRenderHandlerRegistry.INSTANCE.register(class_3612.field_15907, customHandler);
                System.out.println("[ClearLava] Registered custom LavaRenderHandler.");
            } else {
                System.err.println("[ClearLava] Failed to locate water or lava default handlers!");
            }

            // Register hotkeys: F9 (toggle texture) and F4 (reset opacity to normal)
            toggleTextureKey = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.clearlava.toggle_texture",
                298, // GLFW_KEY_F9
                "category.clearlava"
            ));

            toggleOpaqueKey = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.clearlava.toggle_opaque",
                293, // GLFW_KEY_F4
                "category.clearlava"
            ));

            // Tick listener to handle hotkeys
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                // Handle F9 (texture swap)
                while (toggleTextureKey.method_1436()) { // wasPressed()
                    LavaRenderHandler.useWaterTexture = !LavaRenderHandler.useWaterTexture;
                    saveConfig();

                    if (client.field_1769 != null) {
                        client.field_1769.method_3279(); // reload()
                    }

                    if (client.field_1724 != null) {
                        String msg = LavaRenderHandler.useWaterTexture ?
                            "§b[ClearLava] Lava texture set to Water (Blue)" :
                            "§6[ClearLava] Lava texture set to Lava (Translucent Orange)";
                        client.field_1724.method_7353(class_2561.method_43470(msg), true);
                    }
                }

                // Handle F4 (toggle opaque / vanilla)
                while (toggleOpaqueKey.method_1436()) { // wasPressed()
                    LavaRenderHandler.useOpaqueLava = !LavaRenderHandler.useOpaqueLava;
                    saveConfig();
                    updateRenderLayer();

                    if (client.field_1769 != null) {
                        client.field_1769.method_3279(); // reload()
                    }

                    if (client.field_1724 != null) {
                        String msg = LavaRenderHandler.useOpaqueLava ?
                            "§c[ClearLava] Lava opacity set to Opaque (Vanilla)" :
                            "§a[ClearLava] Lava opacity set to Translucent (Clear)";
                        client.field_1724.method_7353(class_2561.method_43470(msg), true);
                    }
                }
            });

            System.out.println("[ClearLava] Initialization completed!");
        } catch (Exception e) {
            System.err.println("[ClearLava] Critical error during init:");
            e.printStackTrace();
        }
    }

    public static void updateRenderLayer() {
        try {
            Field mapField = class_4696.class.getDeclaredField("field_21471");
            mapField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>) mapField.get(null);

            class_1921 layer = LavaRenderHandler.useOpaqueLava ?
                class_1921.method_23577() : // getSolid()
                class_1921.method_23583(); // getTranslucent()

            map.put(class_3612.field_15908, layer);
            map.put(class_3612.field_15907, layer);
            System.out.println("[ClearLava] Set render layer to: " + (LavaRenderHandler.useOpaqueLava ? "SOLID" : "TRANSLUCENT"));
        } catch (Exception e) {
            System.err.println("[ClearLava] Failed to update render layer:");
            e.printStackTrace();
        }
    }

    private static void saveConfig() {
        try {
            String content = "waterTexture:" + LavaRenderHandler.useWaterTexture + "\n" +
                             "opaqueLava:" + LavaRenderHandler.useOpaqueLava;
            Files.write(configPath, content.getBytes());
        } catch (Exception e) {
            System.err.println("[ClearLava] Failed to save config file:");
            e.printStackTrace();
        }
    }
}
