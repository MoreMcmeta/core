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

package client.event;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.github.soir20.moremcmeta.forge.client.event.ClientTicker;
import net.minecraftforge.event.TickEvent;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * Tests using and stopping the client-side ticker.
 * @author soir20
 */
public class ClientTickerTest {
    private final MockEventBus EVENT_BUS = new MockEventBus();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @After
    public void tearDown() {
        EVENT_BUS.clear();
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void construct_ItemsNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new ClientTicker(
                null,
                EVENT_BUS,
                TickEvent.Phase.START,
                () -> true
        );
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void construct_BusNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new ClientTicker(
                ImmutableSet.of(() -> {}, () -> {}, () -> {}, () -> {}),
                null,
                TickEvent.Phase.START,
                () -> true
        );
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void construct_PhaseNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new ClientTicker(
                ImmutableSet.of(() -> {}, () -> {}, () -> {}, () -> {}),
                EVENT_BUS,
                null,
                () -> true
        );
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void construct_ConditionNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new ClientTicker(
                ImmutableSet.of(() -> {}, () -> {}, () -> {}, () -> {}),
                EVENT_BUS,
                TickEvent.Phase.START,
                null
        );
    }

    @Test
    public void construct_HasItems_RegisteredInEventBus() {
        new ClientTicker(
                ImmutableSet.of(() -> {}, () -> {}, () -> {}, () -> {}),
                EVENT_BUS,
                TickEvent.Phase.START,
                () -> true
        );

        assertEquals(1, EVENT_BUS.getListeners().size());
    }

    @Test
    public void construct_NoItems_RegisteredInEventBus() {
        new ClientTicker(
                ImmutableSet.of(),
                EVENT_BUS,
                TickEvent.Phase.START,
                () -> true
        );

        assertEquals(1, EVENT_BUS.getListeners().size());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void tick_EventNull_NullPointerException() {
        MockTickable firstTickable = new MockTickable();
        MockTickable secondTickable = new MockTickable();
        MockTickable thirdTickable = new MockTickable();

        ClientTicker ticker = new ClientTicker(
                ImmutableList.of(firstTickable, firstTickable, secondTickable, thirdTickable),
                EVENT_BUS,
                TickEvent.Phase.START,
                () -> true
        );

        expectedException.expect(NullPointerException.class);
        ticker.tick(null);
    }

    @Test
    public void tick_Duplicates_DuplicatesTickedTwice() {
        MockTickable firstTickable = new MockTickable();
        MockTickable secondTickable = new MockTickable();
        MockTickable thirdTickable = new MockTickable();

        ClientTicker ticker = new ClientTicker(
                ImmutableList.of(firstTickable, firstTickable, secondTickable, thirdTickable),
                EVENT_BUS,
                TickEvent.Phase.START,
                () -> true
        );

        ticker.tick(new TickEvent.ClientTickEvent(TickEvent.Phase.START));

        assertEquals(2, firstTickable.getTicks());
        assertEquals(1, secondTickable.getTicks());
        assertEquals(1, thirdTickable.getTicks());
    }

    @Test
    public void tick_SameStartPhaseTrueCondition_AllItemsTicked() {
        MockTickable firstTickable = new MockTickable();
        MockTickable secondTickable = new MockTickable();
        MockTickable thirdTickable = new MockTickable();

        ClientTicker ticker = new ClientTicker(
                ImmutableSet.of(firstTickable, secondTickable, thirdTickable),
                EVENT_BUS,
                TickEvent.Phase.START,
                () -> true
        );

        ticker.tick(new TickEvent.ClientTickEvent(TickEvent.Phase.START));

        assertEquals(1, firstTickable.getTicks());
        assertEquals(1, secondTickable.getTicks());
        assertEquals(1, thirdTickable.getTicks());
    }

    @Test
    public void tick_SameStartPhaseFalseCondition_NoItemsTicked() {
        MockTickable firstTickable = new MockTickable();
        MockTickable secondTickable = new MockTickable();
        MockTickable thirdTickable = new MockTickable();

        ClientTicker ticker = new ClientTicker(
                ImmutableSet.of(firstTickable, secondTickable, thirdTickable),
                EVENT_BUS,
                TickEvent.Phase.START,
                () -> false
        );

        ticker.tick(new TickEvent.ClientTickEvent(TickEvent.Phase.START));

        assertEquals(0, firstTickable.getTicks());
        assertEquals(0, secondTickable.getTicks());
        assertEquals(0, thirdTickable.getTicks());
    }

    @Test
    public void tick_DifferentStartPhaseTrueCondition_NoItemsTicked() {
        MockTickable firstTickable = new MockTickable();
        MockTickable secondTickable = new MockTickable();
        MockTickable thirdTickable = new MockTickable();

        ClientTicker ticker = new ClientTicker(
                ImmutableSet.of(firstTickable, secondTickable, thirdTickable),
                EVENT_BUS,
                TickEvent.Phase.START,
                () -> true
        );

        ticker.tick(new TickEvent.ClientTickEvent(TickEvent.Phase.END));

        assertEquals(0, firstTickable.getTicks());
        assertEquals(0, secondTickable.getTicks());
        assertEquals(0, thirdTickable.getTicks());
    }

    @Test
    public void tick_DifferentStartPhaseFalseCondition_NoItemsTicked() {
        MockTickable firstTickable = new MockTickable();
        MockTickable secondTickable = new MockTickable();
        MockTickable thirdTickable = new MockTickable();

        ClientTicker ticker = new ClientTicker(
                ImmutableSet.of(firstTickable, secondTickable, thirdTickable),
                EVENT_BUS,
                TickEvent.Phase.START,
                () -> false
        );

        ticker.tick(new TickEvent.ClientTickEvent(TickEvent.Phase.END));

        assertEquals(0, firstTickable.getTicks());
        assertEquals(0, secondTickable.getTicks());
        assertEquals(0, thirdTickable.getTicks());
    }

    @Test
    public void tick_SameEndPhaseTrueCondition_AllItemsTicked() {
        MockTickable firstTickable = new MockTickable();
        MockTickable secondTickable = new MockTickable();
        MockTickable thirdTickable = new MockTickable();

        ClientTicker ticker = new ClientTicker(
                ImmutableSet.of(firstTickable, secondTickable, thirdTickable),
                EVENT_BUS,
                TickEvent.Phase.END,
                () -> true
        );

        ticker.tick(new TickEvent.ClientTickEvent(TickEvent.Phase.END));

        assertEquals(1, firstTickable.getTicks());
        assertEquals(1, secondTickable.getTicks());
        assertEquals(1, thirdTickable.getTicks());
    }

    @Test
    public void tick_SameEndPhaseFalseCondition_NoItemsTicked() {
        MockTickable firstTickable = new MockTickable();
        MockTickable secondTickable = new MockTickable();
        MockTickable thirdTickable = new MockTickable();

        ClientTicker ticker = new ClientTicker(
                ImmutableSet.of(firstTickable, secondTickable, thirdTickable),
                EVENT_BUS,
                TickEvent.Phase.END,
                () -> false
        );

        ticker.tick(new TickEvent.ClientTickEvent(TickEvent.Phase.END));

        assertEquals(0, firstTickable.getTicks());
        assertEquals(0, secondTickable.getTicks());
        assertEquals(0, thirdTickable.getTicks());
    }

    @Test
    public void tick_DifferentEndPhaseTrueCondition_NoItemsTicked() {
        MockTickable firstTickable = new MockTickable();
        MockTickable secondTickable = new MockTickable();
        MockTickable thirdTickable = new MockTickable();

        ClientTicker ticker = new ClientTicker(
                ImmutableSet.of(firstTickable, secondTickable, thirdTickable),
                EVENT_BUS,
                TickEvent.Phase.END,
                () -> true
        );

        ticker.tick(new TickEvent.ClientTickEvent(TickEvent.Phase.START));

        assertEquals(0, firstTickable.getTicks());
        assertEquals(0, secondTickable.getTicks());
        assertEquals(0, thirdTickable.getTicks());
    }

    @Test
    public void tick_DifferentEndPhaseFalseCondition_NoItemsTicked() {
        MockTickable firstTickable = new MockTickable();
        MockTickable secondTickable = new MockTickable();
        MockTickable thirdTickable = new MockTickable();

        ClientTicker ticker = new ClientTicker(
                ImmutableSet.of(firstTickable, secondTickable, thirdTickable),
                EVENT_BUS,
                TickEvent.Phase.END,
                () -> false
        );

        ticker.tick(new TickEvent.ClientTickEvent(TickEvent.Phase.START));

        assertEquals(0, firstTickable.getTicks());
        assertEquals(0, secondTickable.getTicks());
        assertEquals(0, thirdTickable.getTicks());
    }

    @Test
    public void stopTicking_NeverTicked_UnregisteredInEventBus() {
        MockTickable firstTickable = new MockTickable();
        MockTickable secondTickable = new MockTickable();
        MockTickable thirdTickable = new MockTickable();

        ClientTicker ticker = new ClientTicker(
                ImmutableSet.of(firstTickable, secondTickable, thirdTickable),
                EVENT_BUS,
                TickEvent.Phase.START,
                () -> true
        );

        ticker.stopTicking();

        assertEquals(0, EVENT_BUS.getListeners().size());
    }

    @Test
    public void stopTicking_HasTicked_UnregisteredInEventBus() {
        MockTickable firstTickable = new MockTickable();
        MockTickable secondTickable = new MockTickable();
        MockTickable thirdTickable = new MockTickable();

        ClientTicker ticker = new ClientTicker(
                ImmutableSet.of(firstTickable, secondTickable, thirdTickable),
                EVENT_BUS,
                TickEvent.Phase.START,
                () -> true
        );

        ticker.tick(new TickEvent.ClientTickEvent(TickEvent.Phase.START));

        ticker.stopTicking();

        assertEquals(0, EVENT_BUS.getListeners().size());
    }

    @Test
    public void stopTicking_StoppedTicking_Nothing() {
        MockTickable firstTickable = new MockTickable();
        MockTickable secondTickable = new MockTickable();
        MockTickable thirdTickable = new MockTickable();

        ClientTicker ticker = new ClientTicker(
                ImmutableSet.of(firstTickable, secondTickable, thirdTickable),
                EVENT_BUS,
                TickEvent.Phase.START,
                () -> true
        );

        ticker.stopTicking();
        ticker.stopTicking();

        assertEquals(0, EVENT_BUS.getListeners().size());
    }
}