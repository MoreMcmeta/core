package io.github.soir20.moremcmeta.client.resource;

import net.minecraft.server.packs.metadata.MetadataSectionSerializer;

/**
 * Holds animation metadata that is added by MoreMcmeta and not in the vanilla
 * {@link net.minecraft.client.resources.metadata.animation.AnimationMetadataSection}.
 * @author soir20
 */
public class ModAnimationMetadataSection {
    public static final MetadataSectionSerializer<ModAnimationMetadataSection> SERIALIZER =
            new ModAnimationMetadataSectionSerializer();
    public static final ModAnimationMetadataSection EMPTY = new ModAnimationMetadataSection(false);

    private final boolean DAYTIME_SYNC;

    /**
     * Creates a new metadata holder.
     * @param daytimeSync       whether the animation should sync to the game time
     */
    public ModAnimationMetadataSection(boolean daytimeSync) {
        DAYTIME_SYNC = daytimeSync;
    }

    /**
     * Gets whether the animation should sync to the game time.
     * @return whether the animation should sync to the game time
     */
    public boolean isDaytimeSynced() {
        return DAYTIME_SYNC;
    }

}
