package io.github.soir20.moremcmeta.client.texture;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * Finishes items that need to be completed lazily.
 * @param <I> input type
 * @param <O> output type
 */
public interface IFinisher<I, O> {

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
