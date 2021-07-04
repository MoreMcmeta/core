package io.github.soir20.moremcmeta.client.adapter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the {@link ChangingPointsAdapter}.
 * @author soir20
 */
public class ChangingPointsAdapterTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void read_NullImage_NullPointerException() {
        ChangingPointsAdapter adapter = new ChangingPointsAdapter();
        expectedException.expect(NullPointerException.class);
        adapter.read(null, 100, 100, 0);
    }

}