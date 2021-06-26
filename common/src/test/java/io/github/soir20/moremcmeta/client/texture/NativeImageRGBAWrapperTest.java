package io.github.soir20.moremcmeta.client.texture;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the {@link NativeImageRGBAWrapper>} as much as possible without
 * instantiating a {@link com.mojang.blaze3d.platform.NativeImage}, which
 * uses the render system and will throw errors in test code.
 * @author soir20
 */
public class NativeImageRGBAWrapperTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullImage_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new NativeImageRGBAWrapper(null, 0, 0, 100, 100, 2,
                false, false, false, (new IRGBAImage.VisibleArea.Builder()).build());
    }

}