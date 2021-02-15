package io.github.soir20.moremcmeta.resource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.SimpleResource;
import net.minecraft.util.ResourceLocation;

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

/**
 * A mock resource manager that can provide fake found/missing files.
 * @author soir20
 */
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MockResourceManager implements IResourceManager {
    private static final InputStream EMPTY_STREAM = new ByteArrayInputStream(new byte[] {});

    private final List<String> FILES_TO_ADD;
    private final List<String> MISSING_FILES;
    private final boolean USE_UNIQUE_PACK_NAMES;

    private String lastPackName = "test";

    public MockResourceManager(List<String> filesToAdd, List<String> missingFiles, boolean useUniquePackNames) {
        FILES_TO_ADD = filesToAdd;
        MISSING_FILES = missingFiles;
        USE_UNIQUE_PACK_NAMES = useUniquePackNames;
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

            // Add a dummy character to make pack names different for every resource
            if (USE_UNIQUE_PACK_NAMES) {
                lastPackName += "a";
            }

            return new MockSimpleResource(resourceLocationIn, lastPackName);
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
        List<Pair<String, String>> namespacesAndPaths = new ArrayList<>();

        FILES_TO_ADD.forEach((fileName) -> {
            String[] namespaceAndFile = fileName.split(":", 2);

            if (namespaceAndFile.length == 1) {
                namespacesAndPaths.add(new Pair<>("minecraft", pathIn + "/" + namespaceAndFile[0]));
            } else {
                namespacesAndPaths.add(new Pair<>(namespaceAndFile[0], pathIn + "/" + namespaceAndFile[1]));
            }

        });
        namespacesAndPaths.removeIf(namespaceAndPath -> filter.negate().test(namespaceAndPath.getSecond()));

        List<ResourceLocation> locations = new ArrayList<>();
        namespacesAndPaths.forEach(namespaceAndPath -> locations.add(
                new ResourceLocation(namespaceAndPath.getFirst(), namespaceAndPath.getSecond())
        ));

        return locations;
    }

    @Override
    public Stream<IResourcePack> getResourcePackStream() {
        return Stream.of();
    }

    /**
     * Acts as fake resource. This is a separate class in case we need to override metadata parsing
     * in the future.
     * @author soir20
     */
    private static class MockSimpleResource extends SimpleResource {
        public MockSimpleResource(ResourceLocation locationIn, String packName) {
            super(packName, locationIn, EMPTY_STREAM, null);
        }
    }

}
