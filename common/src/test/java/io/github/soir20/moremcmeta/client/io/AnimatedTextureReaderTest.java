package io.github.soir20.moremcmeta.client.io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.apache.logging.log4j.LogManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Tests the {@link AnimatedTextureReader}.
 * @author soir20
 */
public class AnimatedTextureReaderTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullLogger_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new AnimatedTextureReader(null);
    }

    @Test
    public void read_NullTextureStream_NullPointerException() throws IOException {
        AnimatedTextureReader reader = new AnimatedTextureReader(LogManager.getLogger());
        expectedException.expect(NullPointerException.class);
        reader.read(null, new ByteArrayInputStream("".getBytes()));
    }

    @Test
    public void read_NullMetadataStream_NullPointerException() throws IOException {
        AnimatedTextureReader reader = new AnimatedTextureReader(LogManager.getLogger());
        expectedException.expect(NullPointerException.class);
        reader.read(new ByteArrayInputStream("".getBytes()), null);
    }

}