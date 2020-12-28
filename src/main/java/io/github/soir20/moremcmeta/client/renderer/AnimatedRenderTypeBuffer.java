package io.github.soir20.moremcmeta.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

import static io.github.soir20.moremcmeta.MoreMcmeta.ATLAS_LOCATION;

@MethodsReturnNonnullByDefault
public class AnimatedRenderTypeBuffer implements IRenderTypeBuffer {
    private static final Set<ResourceLocation> ANIMATED_TEXTURES = ImmutableSet.of(new ResourceLocation("minecraft:entity/bat"));

    /**
     * List of entity render types we will swap in when we want an animated, stitched texture.
     * Order types by their declaration in {@link net.minecraft.client.renderer.RenderType}
     * and ensure they do not duplicate another type in the list.
     */
    private static final Map<String, RenderType> RENDER_TYPE_SWAPS;
    static {
        RenderType[] renderTypes = {
                // Omit getArmorCutoutNoCull (duplicate of getEntityCutoutNoCullZOffset w/ outline)
                RenderType.getEntitySolid(ATLAS_LOCATION),
                RenderType.getEntityCutout(ATLAS_LOCATION),
                RenderType.getEntityCutoutNoCull(ATLAS_LOCATION, true),
                RenderType.getEntityCutoutNoCull(ATLAS_LOCATION, false),
                RenderType.getEntityCutoutNoCullZOffset(ATLAS_LOCATION, true),
                RenderType.getEntityCutoutNoCullZOffset(ATLAS_LOCATION, false),
                RenderType.getItemEntityTranslucentCull(ATLAS_LOCATION),
                RenderType.getEntityTranslucentCull(ATLAS_LOCATION),
                RenderType.getEntityTranslucent(ATLAS_LOCATION, true),
                RenderType.getEntityTranslucent(ATLAS_LOCATION, false),
                RenderType.getEntitySmoothCutout(ATLAS_LOCATION),
                RenderType.getEntityDecal(ATLAS_LOCATION),
                RenderType.getEntityNoOutline(ATLAS_LOCATION),
                RenderType.getEntityShadow(ATLAS_LOCATION),
                // Omit getEntityAlpha (extra parameters)
                RenderType.getEyes(ATLAS_LOCATION)
                // Omit getEnergySwirl (extra parameters)
        };

        ImmutableMap.Builder<String, RenderType> builder = ImmutableMap.builder();

        for (RenderType renderType : renderTypes) {
            builder.put(extractTexture(renderType.toString())[1], renderType);
        }

        RENDER_TYPE_SWAPS = builder.build();
    }

    private final IRenderTypeBuffer BUFFER;

    public AnimatedRenderTypeBuffer(IRenderTypeBuffer buffer) {
        BUFFER = buffer;
    }

    @Override
    public IVertexBuilder getBuffer(@Nonnull RenderType renderType) {
        String[] textureAndType = extractTexture(renderType.toString());
        ResourceLocation texture = new ResourceLocation(textureAndType[0]);
        String typeWithoutTexture = textureAndType[1];

        /* If we use an animated buffer without swapping the render type,
           the entity won't render correctly, so check both together. */
        if (ANIMATED_TEXTURES.contains(texture) && canSwap(typeWithoutTexture)) {
            RenderMaterial material = new RenderMaterial(ATLAS_LOCATION, texture);
            return material.getSprite().wrapBuffer(BUFFER.getBuffer(swap(typeWithoutTexture)));
        } else {
            return BUFFER.getBuffer(renderType);
        }

    }

    private static String[] extractTexture(String renderType) {
        String texturePrefix = "[texture[Optional[";

        int start = renderType.indexOf(texturePrefix) + texturePrefix.length();
        int end = renderType.indexOf(']', start);

        String remainingType = new StringBuilder(renderType).delete(start, end).toString();
        String texture = toAnimatedTexturePath(renderType.substring(start, end));

        return new String[]{texture, remainingType};
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

    private static boolean canSwap(String renderType) {
        return RENDER_TYPE_SWAPS.containsKey(renderType);
    }

    private static RenderType swap(String renderType) {
        return RENDER_TYPE_SWAPS.get(renderType);
    }

}
