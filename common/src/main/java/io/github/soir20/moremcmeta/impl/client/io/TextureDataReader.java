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

package io.github.soir20.moremcmeta.impl.client.io;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import io.github.soir20.moremcmeta.api.client.MoreMcmetaPlugin;
import io.github.soir20.moremcmeta.api.client.metadata.MetadataView;
import io.github.soir20.moremcmeta.api.client.metadata.ParsedMetadata;
import io.github.soir20.moremcmeta.api.client.texture.ComponentProvider;
import io.github.soir20.moremcmeta.impl.client.adapter.NativeImageAdapter;
import io.github.soir20.moremcmeta.impl.client.resource.JsonMetadataView;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Reads minimum texture data from byte streams. The {@link TextureDataAssembler} takes
 * this data and puts it together.
 * @author soir20
 */
public class TextureDataReader implements TextureReader<TextureData<NativeImageAdapter>> {
    private final Map<String, MoreMcmetaPlugin> SECTION_TO_PLUGIN;

    public TextureDataReader(Iterable<MoreMcmetaPlugin> plugins) {
        SECTION_TO_PLUGIN = new HashMap<>();
        plugins.forEach((plugin) -> SECTION_TO_PLUGIN.put(plugin.sectionName(), plugin));
    }

    /**
     * Reads texture data from texture and metadata byte streams.
     * @param textureStream     input stream of image data
     * @param metadataStream    input stream of texture metadata (JSON)
     * @return minimum texture data
     * @throws IOException if the image could not be read
     * @throws InvalidMetadataException if the metadata is invalid
     */
    @Override
    public TextureData<NativeImageAdapter> read(InputStream textureStream, InputStream metadataStream)
            throws IOException, InvalidMetadataException {
        requireNonNull(textureStream, "Texture input stream cannot be null");
        requireNonNull(metadataStream, "Metadata input stream cannot be null");

        NativeImage image = NativeImage.read(textureStream);

        MetadataView metadata = readMetadata(metadataStream);
        List<Pair<ParsedMetadata, ComponentProvider>> parsedSections = new ArrayList<>();
        Optional<ParsedMetadata.FrameSize> frameSizeOptional = Optional.empty();
        Optional<Boolean> blurOptional = Optional.empty();
        Optional<Boolean> clampOptional = Optional.empty();

        for (String section : metadata.keys()) {
            MoreMcmetaPlugin plugin = SECTION_TO_PLUGIN.get(section);
            ParsedMetadata sectionData = plugin.parser().parse(metadata);
            parsedSections.add(Pair.of(sectionData, plugin.componentProvider()));

            frameSizeOptional = getIfCompatible(frameSizeOptional, sectionData.frameSize(), "frame size");
            blurOptional = getIfCompatible(blurOptional, sectionData.blur(), "blur");
            clampOptional = getIfCompatible(clampOptional, sectionData.clamp(), "clamp");

            if (sectionData.invalidReason().isPresent()) {
                throw new InvalidMetadataException(String.format("%s marked metadata as invalid: %s",
                        plugin.displayName(), sectionData.invalidReason().get()));
            }
        }

        ParsedMetadata.FrameSize frameSize = frameSizeOptional.orElse(
                new ParsedMetadata.FrameSize(image.getWidth(), image.getHeight())
        );
        boolean blur = blurOptional.orElse(false);
        boolean clamp = clampOptional.orElse(false);

        return new TextureData<>(
                frameSize,
                blur,
                clamp,
                new NativeImageAdapter(image, 0),
                parsedSections
        );
    }

    private MetadataView readMetadata(InputStream metadataStream) throws InvalidMetadataException {
        BufferedReader bufferedReader = null;
        MetadataView metadata;

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(metadataStream, StandardCharsets.UTF_8));
            JsonObject metadataObject = GsonHelper.parse(bufferedReader);

            // Create another root object to reuse its integer parsing code
            MetadataView unsortedRoot = new JsonMetadataView(metadataObject, String::compareTo);

            metadata = new JsonMetadataView(metadataObject,
                    (section1, section2) -> compareSections(unsortedRoot, section1, section2)
            );
        } catch (JsonParseException parseError) {
            throw new InvalidMetadataException("Metadata is not valid JSON");
        } finally {
            IOUtils.closeQuietly(bufferedReader);
        }

        return metadata;
    }

    private int compareSections(MetadataView root, String section1, String section2) {
        MetadataView view1 = root.subView(section1).orElseThrow();
        MetadataView view2 = root.subView(section2).orElseThrow();

        final String PRIORITY_KEY = "priority";
        int priorityDiff = view1.integerValue(PRIORITY_KEY).orElse(0) - view2.integerValue(PRIORITY_KEY).orElse(0);

        if (priorityDiff != 0) {
            return priorityDiff;
        }

        return section1.compareTo(section2);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private <T> Optional<T> getIfCompatible(Optional<T> currentVal, Optional<T> newVal, String propName)
            throws InvalidMetadataException {
        if (currentVal.isPresent() && newVal.isPresent() && currentVal.get() != newVal.get()) {
            throw new InvalidMetadataException(String.format("%s was given conflicting values by two plugins: %s %s",
                    propName, currentVal.get(), newVal.get()));
        }

        return newVal;
    }

}
