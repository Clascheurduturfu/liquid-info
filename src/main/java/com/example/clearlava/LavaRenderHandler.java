package com.example.clearlava;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.minecraft.class_1058; // TextureAtlasSprite
import net.minecraft.class_3610; // FluidState
import net.minecraft.class_2338; // BlockPos
import net.minecraft.class_1920; // BlockRenderView

public class LavaRenderHandler implements FluidRenderHandler {
    private final FluidRenderHandler defaultLava;
    private final FluidRenderHandler defaultWater;
    public static boolean useWaterTexture = false;
    public static boolean useOpaqueLava = false;

    public LavaRenderHandler(FluidRenderHandler defaultLava, FluidRenderHandler defaultWater) {
        this.defaultLava = defaultLava;
        this.defaultWater = defaultWater;
    }

    @Override
    public class_1058[] getFluidSprites(net.minecraft.class_1920 view, net.minecraft.class_2338 pos, net.minecraft.class_3610 state) {
        if (!useOpaqueLava && useWaterTexture) {
            return defaultWater.getFluidSprites(view, pos, state);
        } else {
            return defaultLava.getFluidSprites(view, pos, state);
        }
    }

    @Override
    public int getFluidColor(net.minecraft.class_1920 view, net.minecraft.class_2338 pos, net.minecraft.class_3610 state) {
        if (!useOpaqueLava && useWaterTexture) {
            return defaultWater.getFluidColor(view, pos, state);
        } else {
            return defaultLava.getFluidColor(view, pos, state);
        }
    }
}
