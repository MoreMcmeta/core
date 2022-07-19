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

package io.github.soir20.moremcmeta.impl.client.resource;

import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Objects.requireNonNull;

/**
 * A simple concurrent cache that may be stale. While stale, threads that
 * try to get the values in the cache will wait uninterruptibly until the
 * cache is updated with the desired state. The cache will only be updated
 * when the state changes; all other threads that try to load the cache with
 * the same state will return immediately. The cache can be loaded again with
 * a different state.
 * @author soir20
 * @param <R> type of resource to cache
 * @param <S> type of state
 */
public class TextureCache<R, S> {
    private final Lock LOCK;
    private final Condition IS_CURRENT;
    private final TextureLoader<R> LOADER;
    private final Map<ResourceLocation, R> CACHE;
    private S state;

    /**
     * Creates a new cache.
     * @param loader        loads textures into the cache
     */
    public TextureCache(TextureLoader<R> loader) {
        LOCK = new ReentrantLock();
        IS_CURRENT = LOCK.newCondition();
        CACHE = new HashMap<>();
        LOADER = requireNonNull(loader, "Loader cannot be null");
    }

    /**
     * Loads all texture data at the provided paths into the cache, unless the
     * cache already contains the data for this state.
     * @param repository    repository with all resources
     * @param paths         paths to load texture data from
     * @param newState      state associated with the data to be loaded
     */
    public void load(OrderedResourceRepository repository, Set<String> paths, S newState) {
        requireNonNull(repository, "Repository cannot be null");
        requireNonNull(paths, "Paths cannot be null");
        requireNonNull(newState, "State cannot be null");

        LOCK.lock();
        if (!newState.equals(state)) {
            CACHE.clear();

            /* We would normally want to load data asynchronously during reloading. However, this
               portion of texture loading is efficient, even for large images. We have to do this
               before reloading starts to avoid a race with texture atlases. */
            for (String path : paths) {
                CACHE.putAll(LOADER.load(repository, path));
            }

            state = newState;
            IS_CURRENT.signal();
        }
        LOCK.unlock();
    }

    /**
     * Waits until the cache contains the data for the given state and then returns an immutable
     * copy of the cache contents. While waiting, the thread calling this method cannot be
     * interrupted.
     * @param newState      state associated with the data to be retrieved
     * @return an immutable copy of the cache contents
     */
    public ImmutableMap<ResourceLocation, R> get(S newState) {
        requireNonNull(newState, "State cannot be null");

        LOCK.lock();
        while (!newState.equals(state)) {
            IS_CURRENT.awaitUninterruptibly();
        }

        ImmutableMap<ResourceLocation, R> cacheCopy = ImmutableMap.copyOf(CACHE);
        LOCK.unlock();

        return cacheCopy;
    }

}
