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

package io.github.soir20.moremcmeta.client.texture;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * Finishes items that need to be completed lazily.
 * @param <I> input type
 * @param <O> output type
 */
public interface Finisher<I, O> {

    /**
     * Queues an item that needs to be finished. If an item at the
     * same location is already queued, the original will be discarded
     * and replaced.
     * @param location      location identifying the item
     * @param input         the unfinished item
     */
    void queue(ResourceLocation location, I input);

    /**
     * Finishes all currently-queued items.
     * @return a map with all finished items by their locations
     */
    Map<ResourceLocation, O> finish();

}
