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

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface StagedResourceReloadListener<T> extends PreparableReloadListener {

    default CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier, ResourceManager manager,
                                           ProfilerFiller loadProfiler, ProfilerFiller applyProfiler,
                                           Executor loadExecutor, Executor applyExecutor) {
        return load(manager, loadProfiler, loadExecutor).thenCompose(barrier::wait).thenCompose(
                (data) -> this.apply(data, manager, applyProfiler, applyExecutor)
        );
    }

    CompletableFuture<T> load(ResourceManager manager, ProfilerFiller loadProfiler, Executor loadExecutor);

    CompletableFuture<Void> apply(T data, ResourceManager manager,
                                  ProfilerFiller applyProfiler, Executor applyExecutor);

}
