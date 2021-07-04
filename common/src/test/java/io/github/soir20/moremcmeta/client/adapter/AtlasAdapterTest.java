package io.github.soir20.moremcmeta.client.adapter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the {@link AtlasAdapter}.
 * @author soir20
 */
public class AtlasAdapterTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullLocation_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new AtlasAdapter(null);
    }

}