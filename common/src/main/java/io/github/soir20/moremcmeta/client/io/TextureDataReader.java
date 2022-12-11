/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2022 soir20
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

package io.github.soir20.moremcmeta.client.io;

import com.google.gson.JsonParseException;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.soir20.moremcmeta.client.adapter.NativeImageAdapter;
import io.github.soir20.moremcmeta.client.resource.ModAnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.server.packs.resources.ResourceMetadata;

import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

/**
 * Reads minimum texture data from byte streams. The {@link TextureDataAssembler} takes
 * this data and puts it together.
 * @author soir20
 */
public class TextureDataReader implements TextureReader<TextureData<NativeImageAdapter>> {

    /**
     * Reads texture data from texture and metadata byte streams.
     * @param textureStream     input stream of image data
     * @param metadataStream    input stream of texture metadata (JSON)
     * @return minimum texture data
     * @throws IOException if the image could not be read
     * @throws JsonParseException if the metadata JSON is syntactically invalid
     * @throws IllegalArgumentException if the metadata JSON is valid, but the provided values do not fit constraints.
     *                                  See constraints in the associated
     *                                  {@link net.minecraft.server.packs.metadata.MetadataSectionSerializer}.
     */
    @Override
    public TextureData<NativeImageAdapter> read(InputStream textureStream, InputStream metadataStream)
            throws IOException, JsonParseException, IllegalArgumentException {
        requireNonNull(textureStream, "Texture input stream cannot be null");
        requireNonNull(metadataStream, "Metadata input stream cannot be null");

        NativeImage image = NativeImage.read(textureStream);

        ResourceMetadata metadata = ResourceMetadata.fromJsonStream(metadataStream);

        // TODO: add registration for metadata instead of hardcoding them

        AnimationMetadataSection animationMetadata =
                metadata.getSection(AnimationMetadataSection.SERIALIZER).orElse(AnimationMetadataSection.EMPTY);
        ModAnimationMetadataSection modAnimationMetadata =
                metadata.getSection(ModAnimationMetadataSection.SERIALIZER).orElse(null);
        TextureMetadataSection textureMetadata =
                metadata.getSection(TextureMetadataSection.SERIALIZER).orElse(null);

        FrameSize frameSize = animationMetadata.calculateFrameSize(image.getWidth(), image.getHeight());

        TextureData<NativeImageAdapter> data = new TextureData<>(
                frameSize.width(),
                frameSize.height(),
                new NativeImageAdapter(image, 0)
        );

        data.addMetadataSection(AnimationMetadataSection.class, animationMetadata);
        data.addMetadataSection(ModAnimationMetadataSection.class, modAnimationMetadata);
        data.addMetadataSection(TextureMetadataSection.class, textureMetadata);

        return data;
    }

}
