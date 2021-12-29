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

package io.github.soir20.moremcmeta.fabric.client.adapter;

import io.github.soir20.moremcmeta.client.resource.StagedResourceReloadListener;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.Objects.requireNonNull;

public class SimpleReloadListenerAdapter<T> implements SimpleResourceReloadListener<T>,
        StagedResourceReloadListener<T> {

    private final StagedResourceReloadListener<T> DELEGATE;
    private final ResourceLocation FABRIC_ID;

    public SimpleReloadListenerAdapter(StagedResourceReloadListener<T> original, ResourceLocation fabricId) {
        DELEGATE = requireNonNull(original, "Original listener cannot be null");
        FABRIC_ID = requireNonNull(fabricId, "Fabric ID cannot be null");
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier helper, ResourceManager manager,
                                          ProfilerFiller loadProfiler, ProfilerFiller applyProfiler,
                                          Executor loadExecutor, Executor applyExecutor) {
        return SimpleResourceReloadListener.super.reload(helper, manager, loadProfiler, applyProfiler, loadExecutor,
                applyExecutor);
    }

    @Override
    public CompletableFuture<T> load(ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        return DELEGATE.load(manager, profiler, executor);
    }

    @Override
    public CompletableFuture<Void> apply(T data, ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        return DELEGATE.apply(data, manager, profiler, executor);
    }

    @Override
    public ResourceLocation getFabricId() {
        return FABRIC_ID;
    }
}
