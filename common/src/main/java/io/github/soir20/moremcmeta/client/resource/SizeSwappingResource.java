/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2021 soir20
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

package io.github.soir20.moremcmeta.client.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSectionSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

/**
 * Replaces {@link AnimationMetadataSection#EMPTY} with a metadata section that calculates
 * frame size as if the texture was animated. This prevents
 * {@link net.minecraft.client.renderer.texture.TextureAtlasSprite}s without a .mcmeta
 * file but with a .moremcmeta file from having their frames condensed together as one frame.
 * Otherwise, this resource should function identically to the
 * @author soir20
 */
public class SizeSwappingResource implements Resource {
    private final Resource ORIGINAL;
    private final int FRAME_WIDTH;
    private final int FRAME_HEIGHT;

    /**
     * Creates a resource that replaces empty animation metadata sections.
     * @param original          the original resource
     * @param frameWidth        width of an animation frame in the texture
     * @param frameHeight       height of an animation frame in the texture
     */
    public SizeSwappingResource(Resource original, int frameWidth, int frameHeight) {
        ORIGINAL = requireNonNull(original, "Original resource cannot be null");
        FRAME_WIDTH = frameWidth;
        FRAME_HEIGHT = frameHeight;
    }

    /**
     * Gets the location of the original resource.
     * @return location of the original resource
     */
    @Override
    public ResourceLocation getLocation() {
        return ORIGINAL.getLocation();
    }

    /**
     * Gets the input stream of the original resource.
     * @return the input stream of the original resource
     */
    @Override
    public InputStream getInputStream() {
        return ORIGINAL.getInputStream();
    }

    /**
     * Checks if this resource has any metadata (vanilla or for this mod).
     * @return whether this resource has metadata
     */
    @Override
    public boolean hasMetadata() {
        return ORIGINAL.hasMetadata();
    }

    /**
     * Gets metadata from the original resource or a custom empty animation metadata section.
     * @param serializer     metadata serializer
     * @param <T>                           metadata type
     * @return metadata section or null if the resource does not have corresponding metadata
     */
    @Nullable
    @Override
    public <T> T getMetadata(MetadataSectionSerializer<T> serializer) {
        requireNonNull(serializer, "Serializer cannot be null");

        T originalMetadata = ORIGINAL.getMetadata(serializer);

        // .moremcmeta files take precedence over .mcmeta files
        boolean isAnimationSection = serializer instanceof AnimationMetadataSectionSerializer;
        if (!isAnimationSection) {
            return originalMetadata;
        }

        // Return our own "empty" metadata section; without metadata, the default one squeezes all frames into one
        JsonObject emptyAnimJson = new JsonObject();

        JsonArray framesArray = new JsonArray();
        framesArray.add(0);
        emptyAnimJson.add("frames", framesArray);

        emptyAnimJson.addProperty("width", FRAME_WIDTH);
        emptyAnimJson.addProperty("height", FRAME_HEIGHT);

        return serializer.fromJson(emptyAnimJson);
    }

    /**
     * Gets the source name of the original resource.
     * @return source name of the original resource
     */
    @Override
    public String getSourceName() {
        return ORIGINAL.getSourceName();
    }

    /**
     * Closes the original resource and the metadata stream.
     * @throws IOException  I/O exception
     */
    @Override
    public void close() throws IOException {
        ORIGINAL.close();
    }

}
