package io.github.soir20.moremcmeta.client.texture;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Tests the {@link CleanupComponent}.
 * @author soir20
 */
public class CleanupComponentTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_RunnableNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new CleanupComponent<Integer>(null);
    }

    @Test
    public void runListeners_RunOnce_ListenerRuns() {
        AtomicInteger timesRan = new AtomicInteger(0);

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> (new CleanupComponent<Integer>(timesRan::incrementAndGet)).getListeners());
        builder.setImage(new MockRGBAImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.close();

        assertEquals(1, timesRan.get());
    }

    @Test
    public void runListeners_RunTwice_ListenerRuns() {
        AtomicInteger timesRan = new AtomicInteger(0);

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> (new CleanupComponent<Integer>(timesRan::incrementAndGet)).getListeners());
        builder.setImage(new MockRGBAImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.close();
        texture.close();

        assertEquals(2, timesRan.get());
    }

}