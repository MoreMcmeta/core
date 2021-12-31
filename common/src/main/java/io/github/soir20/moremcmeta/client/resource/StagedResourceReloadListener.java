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

/**
 * Resource reload listener with two stages: load and apply. The load stage is for gathering 
 * resources and data. The apply stage accepts the data from the load stage and does any 
 * necessary work with it. This is based off of Fabric's listener, but it can be used on Forge 
 * as well.
 * @param <T>   type of data that is loaded
 */
public interface StagedResourceReloadListener<T> extends PreparableReloadListener {

    /**
     * Executes the load and apply tasks in order when the resource manager reloads.
     * @param barrier           barrier between data gathering and using that data
     * @param manager           Minecraft's resource manager
     * @param loadProfiler      profiler for load stage
     * @param applyProfiler     profiler for apply stage
     * @param loadExecutor      asynchronously executes load stage tasks
     * @param applyExecutor     asynchronously executes apply stage tasks
     * @return a task for both the load and apply stages
     */
    default CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier, ResourceManager manager,
                                           ProfilerFiller loadProfiler, ProfilerFiller applyProfiler,
                                           Executor loadExecutor, Executor applyExecutor) {
        return load(manager, loadProfiler, loadExecutor).thenCompose(barrier::wait).thenCompose(
                (data) -> this.apply(data, manager, applyProfiler, applyExecutor)
        );
    }

    /**
     * Load stage that gathers resources.
     * @param manager           Minecraft's resource manager
     * @param loadProfiler      profiler for load stage
     * @param loadExecutor      asynchronously executes load stage tasks
     * @return the task for the load stage returning the retrieved data
     */
    CompletableFuture<T> load(ResourceManager manager, ProfilerFiller loadProfiler, Executor loadExecutor);

    /**
     * Apply stage that uses resources from the load stage.
     * @param data              the retrieved data from the load stage
     * @param manager           Minecraft's resource manager
     * @param applyProfiler     profiler for apply stage
     * @param applyExecutor     asynchronously executes apply stage tasks
     * @return the task for the apply stage that does not return anything
     */
    CompletableFuture<Void> apply(T data, ResourceManager manager,
                                  ProfilerFiller applyProfiler, Executor applyExecutor);

}
