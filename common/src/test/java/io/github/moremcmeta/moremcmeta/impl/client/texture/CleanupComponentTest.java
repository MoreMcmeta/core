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

import com.google.common.collect.ImmutableList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Tests the {@link CleanupComponent}.
 * @author soir20
 */
public final class CleanupComponentTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_RunnableNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new CleanupComponent(null);
    }

    @Test
    public void runListeners_RunOnce_ListenerRuns() {
        AtomicInteger timesRan = new AtomicInteger(0);

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CleanupComponent(timesRan::incrementAndGet));
        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        texture.close();

        assertEquals(1, timesRan.get());
    }

    @Test
    public void runListeners_RunTwice_ListenerRuns() {
        AtomicInteger timesRan = new AtomicInteger(0);

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CleanupComponent(timesRan::incrementAndGet));
        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        texture.close();
        texture.close();

        assertEquals(2, timesRan.get());
    }

}