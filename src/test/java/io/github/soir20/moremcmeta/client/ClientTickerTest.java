package io.github.soir20.moremcmeta.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.github.soir20.moremcmeta.client.renderer.texture.MockTickable;
import io.github.soir20.moremcmeta.eventbus.MockEventBus;
import net.minecraftforge.event.TickEvent;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests using and stopping the client-side ticker.
 * @author soir20
 */
public class ClientTickerTest {
    private final MockEventBus EVENT_BUS = new MockEventBus();

    @After
    public void tearDown() {
        EVENT_BUS.clear();
    }

    @Test
    public void tick_HasItems_RegisteredInEventBus() {
        new ClientTicker(
                ImmutableSet.of(() -> {}, () -> {}, () -> {}, () -> {}),
                EVENT_BUS,
                TickEvent.Phase.START,
                () -> true
        );

        assertEquals(1, EVENT_BUS.getListeners().size());
    }

    @Test
    public void tick_NoItems_RegisteredInEventBus() {
        new ClientTicker(
                ImmutableSet.of(),
                EVENT_BUS,
                TickEvent.Phase.START,
                () -> true
        );

        assertEquals(1, EVENT_BUS.getListeners().size());
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