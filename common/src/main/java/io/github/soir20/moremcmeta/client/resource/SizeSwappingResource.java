package io.github.soir20.moremcmeta.client.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSectionSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Replaces {@link AnimationMetadataSection#EMPTY} with a metadata section that calculates
 * frame size as if the texture was animated. This prevents
 * {@link net.minecraft.client.renderer.texture.TextureAtlasSprite}s without a .mcmeta
 * file but with a .moremcmeta file from having their frames condensed together as one frame.
 * @author soir20
 */
public class SizeSwappingResource implements Resource {
    private final Resource ORIGINAL;
    private final InputStream METADATA_STREAM;

    /**
     * Creates a resource that replaces empty animation metadata sections.
     * @param original          the original resource
     * @param metadataStream    input stream for .moremcmeta metadata
     */
    public SizeSwappingResource(Resource original, InputStream metadataStream) {
        ORIGINAL = original;
        METADATA_STREAM = metadataStream;
    }

    /**
     * Gets the location of the original resource.
     * @return location of the original resource
     */
    @Override
    public ResourceLocation getLocation() {
        return ORIGINAL.getLocation();
    }

    /**
     * Gets the input stream of the original resource.
     * @return the input stream of the original resource
     */
    @Override
    public InputStream getInputStream() {
        return ORIGINAL.getInputStream();
    }

    /**
     * Gets metadata from the original resource or a custom empty animation metadata section.
     * @param serializer     metadata serializer
     * @param <T>                           metadata type
     * @return metadata section or null if the resource does not have corresponding metadata
     */
    @Nullable
    @Override
    public <T> T getMetadata(MetadataSectionSerializer<T> serializer) {
        T originalMetadata = ORIGINAL.getMetadata(serializer);

        // .mcmeta files take precedence over .moremcmeta files
        boolean isAnimationSection = serializer instanceof AnimationMetadataSectionSerializer;
        if (!isAnimationSection || originalMetadata != null || METADATA_STREAM == null) {
            return originalMetadata;
        }

        AnimationMetadataSection metadata;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(METADATA_STREAM, StandardCharsets.UTF_8)
        )) {
            JsonObject topLevelJson = GsonHelper.parse(reader);

            String sectionName = serializer.getMetadataSectionName();
            if (!topLevelJson.has(sectionName)) {
                return null;
            }

            JsonObject animJson = topLevelJson.getAsJsonObject(sectionName);
            metadata = (AnimationMetadataSection) serializer.fromJson(animJson);
        } catch (IOException | JsonParseException | IllegalArgumentException err) {

            // The texture reload listener will already log the issue
            return null;

        }

        // Return our own "empty" metadata section; without metadata, the default one squeezes all frames into one
        JsonObject emptyAnimJson = new JsonObject();

        JsonArray framesArray = new JsonArray();
        framesArray.add(0);
        emptyAnimJson.add("frames", framesArray);

        int emptyDimension = -1;
        emptyAnimJson.addProperty("width", metadata.getFrameWidth(emptyDimension));
        emptyAnimJson.addProperty("height", metadata.getFrameHeight(emptyDimension));

        return serializer.fromJson(emptyAnimJson);
    }

    /**
     * Gets the source name of the original resource.
     * @return source name of the original resource
     */
    @Override
    public String getSourceName() {
        return ORIGINAL.getSourceName();
    }

    /**
     * Closes the original resource.
     * @throws IOException  I/O exception
     */
    @Override
    public void close() throws IOException {
        ORIGINAL.close();
    }

}
