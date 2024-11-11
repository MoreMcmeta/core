/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2024 soir20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moremcmeta.moremcmeta.impl.client.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import io.github.moremcmeta.moremcmeta.impl.client.mixinaccess.LocatableSpriteAtlas;
import io.github.moremcmeta.moremcmeta.impl.client.mixinaccess.NamedTexture;
import io.github.moremcmeta.moremcmeta.impl.client.texture.BoundTextureState;
import it.unimi.dsi.fastutil.longs.LongRBTreeSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL32C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Tracks and updates the textures and sprites being used.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(VertexBuffer.class)
public class VertexBufferMixin {
    private static final int VERTS_PER_QUAD = 4;

    @Unique
    private final Map<NamedTexture, Set<ResourceLocation>> BOUND_TEXTURE_TO_BASES = new HashMap<>();
    @Unique
    private final LongSet UV_COORDS = new LongRBTreeSet();
    @Unique
    private boolean dirty;

    @Shadow
    @Nullable
    private VertexFormat format;

    /**
     * Recomputes the used UV coordinates when mesh data is updated. This avoids scanning all vertices
     * every time the buffer data is drawn for blocks.
     * @param meshData      mesh data uploaded to the buffer
     * @param callbackInfo  callback info from Mixin
     */
    @Inject(method = "upload(Lcom/mojang/blaze3d/vertex/MeshData;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/MeshData;drawState()Lcom/mojang/blaze3d/vertex/MeshData$DrawState;"))
    private void moremcmeta_onBufferUpload(MeshData meshData, CallbackInfo callbackInfo) {
        MeshData.DrawState drawState = meshData.drawState();
        VertexFormat newFormat = drawState.format();

        int uOffset = newFormat.getOffset(VertexFormatElement.UV);

        // Skip vertex formats without textures
        if (uOffset == -1) {
            BOUND_TEXTURE_TO_BASES.clear();
            UV_COORDS.clear();
            dirty = false;
            return;
        }

        if (!newFormat.equals(format)) {
            BOUND_TEXTURE_TO_BASES.clear();
            UV_COORDS.clear();
        }

        dirty = true;

        int vOffset = uOffset + 4;
        int vertexSize = newFormat.getVertexSize();
        ByteBuffer vertexBuffer = meshData.vertexBuffer();
        int vertices = drawState.vertexCount();

        int baseIndex = 0;
        for (int quad = 0; quad < vertices / VERTS_PER_QUAD; quad++) {
            float uSum = 0.0f;
            float vSum = 0.0f;
            for (int vertex = 0; vertex < VERTS_PER_QUAD; vertex++) {
                uSum += vertexBuffer.getFloat(baseIndex + uOffset);
                vSum += vertexBuffer.getFloat(baseIndex + vOffset);
                baseIndex += vertexSize;
            }

            float centerU = uSum / VERTS_PER_QUAD;
            float centerV = vSum / VERTS_PER_QUAD;

            UV_COORDS.add(packUv(centerU, centerV));
        }
    }

    /**
     * Updates any mod-controlled textures when they are used (the buffer is drawn).
     * @param callbackInfo  callback info from Mixin
     */
    @Inject(method = "draw()V", at = @At("HEAD"))
    private void moremcmeta_onBufferDraw(CallbackInfo callbackInfo) {
        int oldActiveTexture = GlStateManager._getActiveTexture();
        GlStateManager._activeTexture(GL32C.GL_TEXTURE0);
        BoundTextureState.currentTexture().ifPresent((boundTexture) -> {
            Set<ResourceLocation> textures = BOUND_TEXTURE_TO_BASES.computeIfAbsent(boundTexture, (key) -> new HashSet<>());

            // We need to wait until draw time to update the textures list to ensure the texture is bound
            if (dirty) {
                if (boundTexture instanceof LocatableSpriteAtlas boundAtlas) {
                    for (long packedCoords : UV_COORDS) {
                        float centerU = unpackU(packedCoords);
                        float centerV = unpackV(packedCoords);
                        Optional<TextureAtlasSprite> possibleSprite = boundAtlas.moremcmeta_findSprite(centerU, centerV);
                        possibleSprite.ifPresent((sprite) -> textures.add(sprite.contents().name()));
                    }
                } else {
                    textures.addAll(boundTexture.moremcmeta_names());
                }

                dirty = false;
                UV_COORDS.clear();
            }

            NamedTexture.uploadDependencies(textures);
        });
        GlStateManager._activeTexture(oldActiveTexture);
    }

    /**
     * Pack float coordinates into a single long.
     * @param u     u coordinate
     * @param v     v coordinate
     * @return packed coordinates
     */
    private static long packUv(float u, float v) {
        return ((long) Float.floatToIntBits(u)) << 32 | (long) Float.floatToIntBits(v);
    }

    /**
     * Gets the u coordinate from packed coordinates.
     * @param packedCoords  packed coordinates
     * @return u coordinate
     */
    private static float unpackU(long packedCoords) {
        return Float.intBitsToFloat((int) (packedCoords >>> 32));
    }

    /**
     * Gets the v coordinate from packed coordinates.
     * @param packedCoords  packed coordinates
     * @return v coordinate
     */
    private static float unpackV(long packedCoords) {
        return Float.intBitsToFloat((int) packedCoords);
    }

}
