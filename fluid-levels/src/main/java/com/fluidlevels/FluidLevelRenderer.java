package com.fluidlevels;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class FluidLevelRenderer {
	private static final int WATER_TEXT_COLOR = -7807489;
	private static final int LAVA_TEXT_COLOR = -188;

	private static final int RENDER_DISTANCE = 32;
	private static final int SCAN_RADIUS_XZ = 24;
	private static final int SCAN_RADIUS_Y = 12;
	private static final int RESCAN_INTERVAL_TICKS = 8;

	private static final BlockState LAVA_MARKER_BLOCK = Blocks.RED_WOOL.defaultBlockState();
	private static final BlockState WATER_MARKER_BLOCK = Blocks.BLUE_WOOL.defaultBlockState();

	private static final List<MarkerEntry> SOURCE_MARKERS = new ArrayList<>();
	private static long lastScanTick = Long.MIN_VALUE;
	private static BlockPos lastScanCenter = BlockPos.ZERO;

	private FluidLevelRenderer() {
	}

	public static void render(WorldRenderContext context) {
		if (!FluidLevelsClient.isEnabled()) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) {
			return;
		}

		PoseStack poseStack = context.matrixStack();
		if (poseStack == null) {
			return;
		}

		ClientLevel level = mc.level;
		BlockPos playerPos = mc.player.blockPosition();
		Camera camera = mc.gameRenderer.getMainCamera();
		Vec3 camPos = camera.getPosition();

		if (FluidLevelsClient.isWoolSourceMode()) {
			if (shouldRescan(level, playerPos)) {
				rescanSourceMarkers(level, playerPos);
			}
			renderSourceWoolMarkers(mc, poseStack, camPos);
			return;
		}

		renderTextLabels(poseStack, camera.getYRot(), level, playerPos, camPos);
	}

	private static void renderTextLabels(
			PoseStack poseStack, float yRot, ClientLevel level, BlockPos origin, Vec3 camPos) {
		for (int dx = -32; dx <= 32; dx++) {
			for (int dy = -16; dy <= 16; dy++) {
				for (int dz = -32; dz <= 32; dz++) {
					BlockPos pos = origin.offset(dx, dy, dz);
					BlockState state = level.getBlockState(pos);

					if (!(state.getBlock() instanceof LiquidBlock)) {
						continue;
					}
					if (!state.is(Blocks.WATER) && !state.is(Blocks.LAVA)) {
						continue;
					}

					int fluidLevel = state.getValue(LiquidBlock.LEVEL);
					if (FluidLevelsClient.isSourceOnly() && fluidLevel != 0) {
						continue;
					}

					String label;
					if (fluidLevel == 0) {
						label = "S";
					} else if (fluidLevel >= 8) {
						label = String.valueOf(fluidLevel);
					} else {
						label = String.valueOf(8 - fluidLevel);
					}

					int color = state.is(Blocks.WATER) ? WATER_TEXT_COLOR : LAVA_TEXT_COLOR;
					boolean fullHeightColumn = fluidLevel >= 8;

					BlockState above = level.getBlockState(pos.above());
					boolean aboveIsFluid = above.getBlock() instanceof LiquidBlock;

					if (fullHeightColumn && aboveIsFluid) {
						renderSideLabel(poseStack, pos, camPos, label, color);
					} else if (!aboveIsFluid) {
						renderTopLabel(poseStack, yRot, pos, camPos, label, color);
					}
				}
			}
		}
	}

	private static void renderSourceWoolMarkers(Minecraft mc, PoseStack poseStack, Vec3 camPos) {
		BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();
		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
		double maxDistSq = RENDER_DISTANCE * RENDER_DISTANCE;

		for (MarkerEntry marker : SOURCE_MARKERS) {
			BlockPos pos = marker.pos();
			double centerX = pos.getX() + 0.5;
			double centerY = pos.getY() + 0.5;
			double centerZ = pos.getZ() + 0.5;
			if (camPos.distanceToSqr(centerX, centerY, centerZ) > maxDistSq) {
				continue;
			}

			poseStack.pushPose();
			poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
			blockRenderer.renderSingleBlock(
					marker.blockState(),
					poseStack,
					bufferSource,
					LightTexture.FULL_BRIGHT,
					OverlayTexture.NO_OVERLAY);
			poseStack.popPose();
		}
		bufferSource.endBatch();
	}

	private static boolean shouldRescan(ClientLevel level, BlockPos playerPos) {
		long gameTime = level.getGameTime();
		if (gameTime - lastScanTick >= RESCAN_INTERVAL_TICKS) {
			return true;
		}

		int movedManhattan =
				Math.abs(playerPos.getX() - lastScanCenter.getX())
						+ Math.abs(playerPos.getY() - lastScanCenter.getY())
						+ Math.abs(playerPos.getZ() - lastScanCenter.getZ());
		return movedManhattan >= 4;
	}

	private static void rescanSourceMarkers(ClientLevel level, BlockPos center) {
		SOURCE_MARKERS.clear();
		lastScanTick = level.getGameTime();
		lastScanCenter = center;

		for (int dx = -SCAN_RADIUS_XZ; dx <= SCAN_RADIUS_XZ; dx++) {
			for (int dy = -SCAN_RADIUS_Y; dy <= SCAN_RADIUS_Y; dy++) {
				for (int dz = -SCAN_RADIUS_XZ; dz <= SCAN_RADIUS_XZ; dz++) {
					BlockPos pos = center.offset(dx, dy, dz);
					BlockState state = level.getBlockState(pos);
					if (!state.is(Blocks.LAVA) && !state.is(Blocks.WATER)) {
						continue;
					}
					if (state.hasProperty(LiquidBlock.LEVEL) && state.getValue(LiquidBlock.LEVEL) == 0) {
						BlockState marker = state.is(Blocks.LAVA) ? LAVA_MARKER_BLOCK : WATER_MARKER_BLOCK;
						SOURCE_MARKERS.add(new MarkerEntry(pos.immutable(), marker));
					}
				}
			}
		}
	}

	private static void renderTopLabel(
			PoseStack poseStack,
			float yRot,
			BlockPos pos,
			Vec3 camPos,
			String text,
			int color) {
		double dx = pos.getX() + 0.5 - camPos.x;
		double dy = pos.getY() + 0.95 - camPos.y;
		double dz = pos.getZ() + 0.5 - camPos.z;
		double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (dist > 32.0) {
			return;
		}

		poseStack.pushPose();
		poseStack.translate(dx, dy, dz);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - yRot));
		poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0f));

		float scale = 0.035f;
		if (dist > 10.0) {
			scale *= (float) (10.0 / dist) * 0.7f;
		}
		poseStack.scale(scale, -scale, scale);
		drawText(poseStack, text, color);
		poseStack.popPose();
	}

	private static void renderSideLabel(
			PoseStack poseStack, BlockPos pos, Vec3 camPos, String text, int color) {
		double ox = camPos.x - (pos.getX() + 0.5);
		double oz = camPos.z - (pos.getZ() + 0.5);
		double labelX;
		double labelZ;
		float yRotBillboard;

		if (Math.abs(ox) > Math.abs(oz)) {
			labelX = pos.getX() + (ox > 0 ? 1.01 : -0.01) - camPos.x;
			labelZ = pos.getZ() + 0.5 - camPos.z;
			yRotBillboard = ox > 0 ? -90.0f : 90.0f;
		} else {
			labelX = pos.getX() + 0.5 - camPos.x;
			labelZ = pos.getZ() + (oz > 0 ? 1.01 : -0.01) - camPos.z;
			yRotBillboard = oz > 0 ? 0.0f : 180.0f;
		}

		double dy = pos.getY() + 0.5 - camPos.y;
		double dist = Math.sqrt(labelX * labelX + dy * dy + labelZ * labelZ);
		if (dist > 32.0) {
			return;
		}

		poseStack.pushPose();
		poseStack.translate(labelX, dy, labelZ);
		poseStack.mulPose(Axis.YP.rotationDegrees(yRotBillboard));

		float scale = 0.035f;
		if (dist > 10.0) {
			scale *= (float) (10.0 / dist) * 0.7f;
		}
		poseStack.scale(scale, -scale, scale);
		drawText(poseStack, text, color);
		poseStack.popPose();
	}

	private static void drawText(PoseStack poseStack, String text, int color) {
		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;
		Matrix4f matrix = poseStack.last().pose();
		float x = -font.width(text) / 2.0f;
		float y = -(font.lineHeight / 2.0f);

		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
		font.drawInBatch(
				text,
				x,
				y,
				color,
				false,
				matrix,
				bufferSource,
				Font.DisplayMode.NORMAL,
				0,
				LightTexture.FULL_BRIGHT);
		bufferSource.endBatch();
	}

	private record MarkerEntry(BlockPos pos, BlockState blockState) {}
}
