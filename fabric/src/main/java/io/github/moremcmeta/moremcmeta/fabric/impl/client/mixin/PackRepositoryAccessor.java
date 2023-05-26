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

package io.github.moremcmeta.moremcmeta.fabric.impl.client.mixin;

import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

/**
 * Allows retrieval of the {@link RepositorySource}s in the {@link PackRepository}.
 * @author soir20
 */
@Mixin(PackRepository.class)
public interface PackRepositoryAccessor {

    /**
     * Gets the pack sources from the repository. Set is immutable in the vanilla code.
     * @return the pack resources in the repository
     */
    @Accessor("sources")
    Set<RepositorySource> moremcmeta_sources();

    /**
     * Replaces the pack sources in the repository.
     * @param sources       the new sources for the pack repository
     */
    @Accessor("sources")
    @Mutable
    void moremcmeta_setSources(Set<RepositorySource> sources);

}
