package io.github.soir20.moremcmeta.client.resource;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the {@link ModAnimationMetadataSection}.
 * @author soir20
 */
public class ModAnimationMetadataSectionTest {

    @Test
    public void getSynced_IsSynced_True() {
        ModAnimationMetadataSection metadata = new ModAnimationMetadataSection(true);
        assertTrue(metadata.isDaytimeSynced());
    }
    @Test
    public void getSynced_NotSynced_False() {
        ModAnimationMetadataSection metadata = new ModAnimationMetadataSection(false);
        assertFalse(metadata.isDaytimeSynced());
    }

}