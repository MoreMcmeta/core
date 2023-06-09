/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
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

package io.github.moremcmeta.moremcmeta.fabric.impl.client.mixin;

import io.github.moremcmeta.moremcmeta.fabric.impl.client.event.ResourceManagerInitializedCallback;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires an event after the resource manager is initialized on Fabric.
 * @author soir20
 */
@Mixin(Minecraft.class)
@SuppressWarnings("unused")
public final class ManagerInitializationMixin {

    /**
     * Injects a callback after the resource manager is initialized on Fabric.
     * @param info      information about the callback
     */
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;reload()V"),
            method = "<init>*")
    @SuppressWarnings("ConstantConditions")
    public void moremcmeta_onReloaded(CallbackInfo info) {
        ResourceManagerInitializedCallback.EVENT.invoker().onManagerInitialized((Minecraft) (Object) this);
    }

}
