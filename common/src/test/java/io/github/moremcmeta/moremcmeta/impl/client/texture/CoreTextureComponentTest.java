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

package io.github.moremcmeta.moremcmeta.impl.client.texture;

import net.minecraft.resources.ResourceLocation;
import org.junit.Test;

import java.util.List;

/**
 * Tests the default methods of the {@link CoreTextureComponent}.
 * @author soir20
 */
public final class CoreTextureComponentTest {

    @Test
    public void onRegistration_NullState_NoException() {
        CoreTextureComponent component = new CoreTextureComponent() {};
        component.onRegistration(null, new FrameGroupImpl<>(List.of()));
    }

    @Test
    public void onTick_NullState_NoException() {
        CoreTextureComponent component = new CoreTextureComponent() {};
        component.onTick(null, new FrameGroupImpl<>(List.of()));
    }

    @Test
    public void onClose_NullState_NoException() {
        CoreTextureComponent component = new CoreTextureComponent() {};
        component.onClose(null, new FrameGroupImpl<>(List.of()));
    }

    @Test
    public void onRegistration_ValidState_NoException() {
        CoreTextureComponent component = new CoreTextureComponent() {};

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(1)));
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)));
        builder.add(component);
        builder.build().load(null);
    }

    @Test
    public void onUpload_ValidState_NoException() {
        CoreTextureComponent component = new CoreTextureComponent() {};

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(1)));
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)));
        builder.add(component);
        builder.build().upload(new ResourceLocation("dummy.png"));
    }

    @Test
    public void onTick_ValidState_NoException() {
        CoreTextureComponent component = new CoreTextureComponent() {};

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(1)));
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)));
        builder.add(component);
        builder.build().tick();
    }

    @Test
    public void onClose_ValidState_NoException() {
        CoreTextureComponent component = new CoreTextureComponent() {};

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(1)));
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)));
        builder.add(component);
        builder.build().close();
    }

}