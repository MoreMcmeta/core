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

package io.github.moremcmeta.moremcmeta.api.client.texture;

import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataAnalyzer;
import io.github.moremcmeta.moremcmeta.api.client.metadata.AnalyzedMetadata;

/**
 * Creates {@link TextureComponent}s from initial texture and frame information. Applies initial
 * transformations to the predefined frames.
 * @author soir20
 * @since 4.0.0
 */
@FunctionalInterface
public interface ComponentBuilder {

    /**
     * Assembles initial data about a texture into a {@link TextureComponent}. <b>This method may be
     * called from multiple threads concurrently. If there is any state shared between calls, it must
     * be synchronized properly for concurrent usage.</b>
     * @param metadata      metadata analyzed earlier by this plugin's
     *                      {@link MetadataAnalyzer}.
     * @param frames        all the predefined frames, which are mutable to allow for initial
     *                      {@link ColorTransform}s to be applied
     * @return texture component for this texture
     */
    TextureComponent<CurrentFrameView> build(AnalyzedMetadata metadata, FrameGroup<? extends MutableFrameView> frames);

}
