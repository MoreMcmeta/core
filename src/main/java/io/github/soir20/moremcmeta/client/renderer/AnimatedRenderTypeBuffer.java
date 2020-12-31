package io.github.soir20.moremcmeta.client.renderer;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Set;

import static io.github.soir20.moremcmeta.MoreMcmeta.ATLAS_LOCATION;

@MethodsReturnNonnullByDefault
public class AnimatedRenderTypeBuffer implements IRenderTypeBuffer {
    private static final Set<ResourceLocation> ANIMATED_TEXTURES = ImmutableSet.of(new ResourceLocation("minecraft:entity/bat"));

    private final IRenderTypeBuffer BUFFER;

    public AnimatedRenderTypeBuffer(IRenderTypeBuffer buffer) {
        BUFFER = buffer;
    }

    @Override
    public IVertexBuilder getBuffer(@Nonnull RenderType renderType) {
        String texturePath = extractTexture(renderType.toString());
        ResourceLocation texture = new ResourceLocation(texturePath);

        /* If we use an animated buffer without swapping the render type,
           the entity won't render correctly, so check both together. */
        if (ANIMATED_TEXTURES.contains(texture)) {
            RenderMaterial material = new RenderMaterial(ATLAS_LOCATION, texture);
            return material.getSprite().wrapBuffer(BUFFER.getBuffer(renderType));
        } else {
            return BUFFER.getBuffer(renderType);
        }

    }

    private static String extractTexture(String renderType) {
        String texturePrefix = "[texture[Optional[";

        int start = renderType.indexOf(texturePrefix) + texturePrefix.length();
        int end = renderType.indexOf(']', start);

        return toAnimatedTexturePath(renderType.substring(start, end));
    }

    private static String toAnimatedTexturePath(String texture) {
        StringBuilder textureBuilder = new StringBuilder(texture);
        int texLength = textureBuilder.length();

        String fileExtension = ".png";
        int extLength = fileExtension.length();

        if (textureBuilder.substring(texLength - extLength).equals(fileExtension)) {
            textureBuilder.delete(texLength - extLength, texLength);
        }

        String namespaceSeparator = ":";
        int pathStart = textureBuilder.indexOf(namespaceSeparator) + 1;

        String pathPrefix = "textures/";
        int prefixEnd = pathStart + pathPrefix.length();

        if (pathStart > 0 && textureBuilder.substring(pathStart, prefixEnd).equals(pathPrefix)) {
            textureBuilder.delete(pathStart, prefixEnd);
        }

        return textureBuilder.toString();
    }

}
