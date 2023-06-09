/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2023 soir20
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

package io.github.moremcmeta.moremcmeta.api.client.metadata;

import java.util.Optional;

/**
 * Contains data analyzed by the {@link MetadataAnalyzer}. <b>There may be multiple instances being used
 * by different threads concurrently. If there is any state shared between instances, it must be
 * synchronized properly for concurrent usage.</b>
 * @author soir20
 * @since 4.0.0
 */
public interface AnalyzedMetadata {

    /**
     * Gets the frame width read from the metadata. {@link Optional#empty()} indicates "no opinion."
     * All plugins that do not return {@link Optional#empty()} from this method must return equal
     * frame widths; otherwise, the metadata is invalid.
     * @return the frame width this plugin determined or {@link Optional#empty()} if this plugin
     *         has no opinion.
     */
    default Optional<Integer> frameWidth() {
        return Optional.empty();
    }

    /**
     * Gets the frame height read from the metadata. {@link Optional#empty()} indicates "no opinion."
     * All plugins that do not return {@link Optional#empty()} from this method must return equal
     * frame heights; otherwise, the metadata is invalid.
     * @return the frame height this plugin determined or {@link Optional#empty()} if this plugin
     *         has no opinion.
     */
    default Optional<Integer> frameHeight() {
        return Optional.empty();
    }

    /**
     * Gets the blur boolean read from the metadata. {@link Optional#empty()} indicates "no opinion."
     * All plugins that do not return {@link Optional#empty()} from this method must return equal
     * booleans; otherwise, the metadata is invalid.
     * @return the blur setting this plugin determined or {@link Optional#empty()} if this plugin
     *         has no opinion.
     */
    default Optional<Boolean> blur() {
        return Optional.empty();
    }

    /**
     * Gets the clamp boolean read from the metadata. {@link Optional#empty()} indicates "no opinion."
     * All plugins that do not return {@link Optional#empty()} from this method must return equal
     * booleans; otherwise, the metadata is invalid.
     * @return the clamp setting this plugin determined or {@link Optional#empty()} if this plugin
     *         has no opinion.
     */
    default Optional<Boolean> clamp() {
        return Optional.empty();
    }

}
