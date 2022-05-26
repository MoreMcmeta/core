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

package io.github.soir20.moremcmeta.api.client.texture;

import io.github.soir20.moremcmeta.api.client.metadata.ParsedMetadata;

/**
 * Creates {@link TextureComponent}s from initial texture and frame information. Applies initial
 * transformations to the predefined frames.
 * @author soir20
 * @since 4.0.0
 */
@FunctionalInterface
public interface ComponentProvider {

    /**
     * Assembles initial data about a texture into {@link TextureComponent}s.
     * @param metadata      metadata parsed earlier by this plugin's
     *                      {@link io.github.soir20.moremcmeta.api.client.metadata.MetadataParser}.
     * @param frames        all the predefined frames, which are mutable to allow for initial
     *                      {@link ColorTransform}s to be applied
     * @return texture components for this texture. Neither the {@link Iterable} nor the individual
     *         components may be null.
     */
    Iterable<TextureComponent<CurrentFrameView>> assemble(ParsedMetadata metadata, FrameGroup<MutableFrameView> frames);

}
