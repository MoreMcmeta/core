package io.github.soir20.moremcmeta.resource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.SimpleResource;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MockResourceManager implements IResourceManager {
    private static final InputStream EMPTY_STREAM = new ByteArrayInputStream(new byte[] {});

    private final Set<String> FILES_TO_ADD;
    private final Set<String> MISSING_FILES;
    private final Set<String> INVALID_METADATA_FILES;

    public MockResourceManager(Set<String> filesToAdd, Set<String> missingFiles,
                               Set<String> filesWithoutValidMetadata) {
        FILES_TO_ADD = filesToAdd;
        MISSING_FILES = missingFiles;
        INVALID_METADATA_FILES = filesWithoutValidMetadata;
    }

    @Override
    public Set<String> getResourceNamespaces() {
        return ImmutableSet.of();
    }

    @Override
    public IResource getResource(ResourceLocation resourceLocationIn) throws IOException {
        int fileNameStart = resourceLocationIn.getPath().lastIndexOf('/') + 1;
        boolean isMissing = MISSING_FILES.contains(resourceLocationIn.getPath().substring(fileNameStart));

        if (isMissing) {
            throw new IOException();
        } else {
            return new MockSimpleResource(resourceLocationIn, INVALID_METADATA_FILES);
        }
    }

    @Override
    public boolean hasResource(ResourceLocation path) {
        return false;
    }

    @Override
    public List<IResource> getAllResources(ResourceLocation resourceLocationIn) {
        return ImmutableList.of();
    }

    @Override
    public Collection<ResourceLocation> getAllResourceLocations(String pathIn, Predicate<String> filter) {
        List<String> paths = new ArrayList<>();

        FILES_TO_ADD.forEach((fileName) -> paths.add(pathIn + "/" + fileName));
        paths.removeIf(filter.negate());

        List<ResourceLocation> locations = new ArrayList<>();
        paths.forEach((path) -> locations.add(new ResourceLocation(path)));

        return locations;
    }

    @Override
    public Stream<IResourcePack> getResourcePackStream() {
        return Stream.of();
    }

    private static class MockSimpleResource extends SimpleResource {
        private final boolean HAS_METADATA;

        public MockSimpleResource(ResourceLocation locationIn, Set<String> filesWithoutMetadata) {
            super("test", locationIn, EMPTY_STREAM, null);
            int fileNameStart = locationIn.getPath().lastIndexOf('/') + 1;
            HAS_METADATA = !filesWithoutMetadata.contains(locationIn.getPath().substring(fileNameStart));
        }

        @Override
        public boolean hasMetadata() {
            return HAS_METADATA;
        }

        @Nullable
        @Override
        public <T> T getMetadata(IMetadataSectionSerializer<T> serializer) {
            if (HAS_METADATA) {
                return serializer.deserialize(null);
            } else {
                throw new RuntimeException();
            }
        }
    }
}
