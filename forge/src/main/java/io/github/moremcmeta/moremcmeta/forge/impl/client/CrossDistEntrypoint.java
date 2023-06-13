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

package io.github.moremcmeta.moremcmeta.forge.impl.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;

import static io.github.moremcmeta.moremcmeta.impl.client.MoreMcmeta.MODID;

/**
 * Entrypoint that works on both the client and server.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mod(MODID)
public class CrossDistEntrypoint {

    /**
     * Mod constructor for both client and server.
     */
    public CrossDistEntrypoint() {

        /* Make sure the mod being absent on the other network side does not
           cause the client to display the server as incompatible. */
        ModLoadingContext.get().registerExtensionPoint(
                ExtensionPoint.DISPLAYTEST,
                () -> Pair.of(
                        () -> FMLNetworkConstants.IGNORESERVERONLY,
                        (a, b) -> true
                )
        );

        // This is safe because client methods are isolated in the MoreMcmetaForge class
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> new MoreMcmetaForge().start());

    }

}
