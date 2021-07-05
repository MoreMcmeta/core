package io.github.soir20.moremcmeta.client.resource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.util.Unit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class SizeSwappingResourceManagerTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullOriginalManager_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new SizeSwappingResourceManager(null, () -> {});
    }

    @Test
    public void construct_NullCallback_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new SizeSwappingResourceManager(new MockReloadableResourceManager(), null);
    }

    @Test
    public void addPackResources_NullPack_OriginalHasResources() {
        MockReloadableResourceManager original = new MockReloadableResourceManager();
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        expectedException.expect(NullPointerException.class);
        wrapper.add(null);
    }

    @Test
    public void addPackResources_PackAddedToWrapper_OriginalHasResources() {
        PackResources firstPack = new MockPackResources("first");
        PackResources secondPack = new MockPackResources("second");

        MockReloadableResourceManager original = new MockReloadableResourceManager();
        original.add(firstPack);
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});
        wrapper.add(secondPack);
        Iterators.elementsEqual(Stream.of(firstPack, secondPack).iterator(), original.listPacks().iterator());
    }

    @Test
    public void listPacks_PackAddedToWrapper_WrapperListsPacks() {
        PackResources firstPack = new MockPackResources("first");
        PackResources secondPack = new MockPackResources("second");

        MockReloadableResourceManager original = new MockReloadableResourceManager();
        original.add(firstPack);
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});
        wrapper.add(secondPack);
        Iterators.elementsEqual(Stream.of(firstPack, secondPack).iterator(), wrapper.listPacks().iterator());
    }

    @Test
    public void getNamespaces_PacksAdded_SameNamespaces() {
        PackResources firstPack = new MockPackResources("first");
        PackResources secondPack = new MockPackResources("second");

        MockReloadableResourceManager original = new MockReloadableResourceManager();
        original.add(firstPack);
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});
        wrapper.add(secondPack);
        assertEquals(original.getNamespaces(), wrapper.getNamespaces());
    }

    @Test
    public void getResource_NullLocation_NullPointerException() throws IOException {
        MockReloadableResourceManager original = new MockReloadableResourceManager();
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        expectedException.expect(NullPointerException.class);
        wrapper.getResource(null);
    }

    @Test
    public void getResource_MissingResource_IOException() throws IOException {
        ResourceLocation existingLocation = new ResourceLocation("existing-location");
        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public Resource getResource(ResourceLocation location) throws IOException {
                if (location.equals(existingLocation)) {
                    return new MockResourceManager.MockSimpleResource(existingLocation, "dummy");
                }

                throw new IOException("Not found");
            }

            @Override
            public boolean hasResource(ResourceLocation location) {
                return ImmutableSet.of(existingLocation).contains(location);
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        expectedException.expect(IOException.class);
        wrapper.getResource(new ResourceLocation("missing-location"));
    }

    @Test
    public void getResource_FoundResourceNotPNG_RegularResource() throws IOException {
        ResourceLocation existingLocation = new ResourceLocation("existing-location");
        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public Resource getResource(ResourceLocation location) throws IOException {
                if (location.equals(existingLocation)) {
                    return new MockResourceManager.MockSimpleResource(existingLocation, "dummy");
                }

                throw new IOException("Not found");
            }

            @Override
            public boolean hasResource(ResourceLocation location) {
                return ImmutableSet.of(existingLocation).contains(location);
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        Resource resource = wrapper.getResource(existingLocation);
        assertEquals(existingLocation, resource.getLocation());
        assertFalse(resource instanceof SizeSwappingResource);
    }

    @Test
    public void getResource_FoundResourceNotPNGWithModMetadata_RegularResource() throws IOException {
        ResourceLocation existingLocation = new ResourceLocation("existing-location");
        ResourceLocation existingMetadata = new ResourceLocation("existing-location.moremcmeta");
        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public Resource getResource(ResourceLocation location) throws IOException {
                if (location.equals(existingLocation)) {
                    return new MockResourceManager.MockSimpleResource(existingLocation, "dummy");
                } else if (location.equals(existingMetadata)) {
                    return new MockResourceManager.MockSimpleResource(existingMetadata, "dummy");
                }

                throw new IOException("Not found");
            }

            @Override
            public boolean hasResource(ResourceLocation location) {
                return ImmutableSet.of(existingLocation, existingMetadata).contains(location);
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        Resource resource = wrapper.getResource(existingLocation);
        assertEquals(existingLocation, resource.getLocation());
        assertFalse(resource instanceof SizeSwappingResource);
    }

    @Test
    public void getResource_FoundResourceNoModMetadata_RegularResource() throws IOException {
        ResourceLocation existingLocation = new ResourceLocation("existing-location.png");
        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public Resource getResource(ResourceLocation location) throws IOException {
                if (location.equals(existingLocation)) {
                    return new MockResourceManager.MockSimpleResource(existingLocation, "dummy");
                }

                throw new IOException("Not found");
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        Resource resource = wrapper.getResource(existingLocation);
        assertEquals(existingLocation, resource.getLocation());
        assertFalse(resource instanceof SizeSwappingResource);
    }

    @Test
    public void getResource_FoundResourceModMetadataInDifferentPack_RegularResource() throws IOException {
        ResourceLocation existingLocation = new ResourceLocation("existing-location.png");
        ResourceLocation existingMetadata = new ResourceLocation("existing-location.png.moremcmeta");
        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public Resource getResource(ResourceLocation location) throws IOException {
                if (location.equals(existingLocation)) {
                    return new MockResourceManager.MockSimpleResource(existingLocation, "dummy");
                } else if (location.equals(existingMetadata)) {
                    return new MockResourceManager.MockSimpleResource(existingMetadata, "other");
                }

                throw new IOException("Not found");
            }

            @Override
            public boolean hasResource(ResourceLocation location) {
                return ImmutableSet.of(existingLocation, existingMetadata).contains(location);
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        Resource resource = wrapper.getResource(existingLocation);
        assertEquals(existingLocation, resource.getLocation());
        assertFalse(resource instanceof SizeSwappingResource);
    }

    @Test
    public void getResource_FoundResourceModMetadataInSamePack_SizeSwappingResource() throws IOException {
        ResourceLocation existingLocation = new ResourceLocation("existing-location.png");
        ResourceLocation existingMetadata = new ResourceLocation("existing-location.png.moremcmeta");
        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public Resource getResource(ResourceLocation location) throws IOException {
                if (location.equals(existingLocation)) {
                    return new MockResourceManager.MockSimpleResource(existingLocation, "dummy");
                } else if (location.equals(existingMetadata)) {
                    return new MockResourceManager.MockSimpleResource(existingMetadata, "dummy");
                }

                throw new IOException("Not found");
            }

            @Override
            public boolean hasResource(ResourceLocation location) {
                return ImmutableSet.of(existingLocation, existingMetadata).contains(location);
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        Resource resource = wrapper.getResource(existingLocation);
        assertEquals(existingLocation, resource.getLocation());
        assertTrue(resource instanceof SizeSwappingResource);
    }

    @Test
    public void getResource_FoundResourceModAndRegularMetadataInSamePack_SizeSwappingResource() throws IOException {
        ResourceLocation existingLocation = new ResourceLocation("existing-location.png");
        ResourceLocation existingMetadata = new ResourceLocation("existing-location.png.mcmeta");
        ResourceLocation existingModMetadata = new ResourceLocation("existing-location.png.moremcmeta");
        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public Resource getResource(ResourceLocation location) throws IOException {
                if (location.equals(existingLocation)) {
                    return new MockResourceManager.MockSimpleResource(existingLocation, "dummy");
                } else if (location.equals(existingMetadata)) {
                    return new MockResourceManager.MockSimpleResource(existingMetadata, "dummy");
                } else if (location.equals(existingModMetadata)) {
                    return new MockResourceManager.MockSimpleResource(existingModMetadata, "dummy");
                }

                throw new IOException("Not found");
            }

            @Override
            public boolean hasResource(ResourceLocation location) {
                return ImmutableSet.of(existingLocation, existingMetadata, existingModMetadata).contains(location);
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        Resource resource = wrapper.getResource(existingLocation);
        assertEquals(existingLocation, resource.getLocation());
        assertTrue(resource instanceof SizeSwappingResource);
    }

    @Test
    public void hasResource_NullLocation_NullPointerException() {
        MockReloadableResourceManager original = new MockReloadableResourceManager();
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        expectedException.expect(NullPointerException.class);
        wrapper.hasResource(null);
    }

    @Test
    public void hasResource_OriginalDoesNotHaveResource_NotFound() {
        ResourceLocation existingLocation = new ResourceLocation("existing-location.png");
        ResourceLocation existingMetadata = new ResourceLocation("existing-location.png.mcmeta");
        ResourceLocation existingModMetadata = new ResourceLocation("existing-location.png.moremcmeta");
        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public Resource getResource(ResourceLocation location) throws IOException {
                if (location.equals(existingLocation)) {
                    return new MockResourceManager.MockSimpleResource(existingLocation, "dummy");
                } else if (location.equals(existingMetadata)) {
                    return new MockResourceManager.MockSimpleResource(existingMetadata, "dummy");
                } else if (location.equals(existingModMetadata)) {
                    return new MockResourceManager.MockSimpleResource(existingModMetadata, "dummy");
                }

                throw new IOException("Not found");
            }

            @Override
            public boolean hasResource(ResourceLocation location) {
                return ImmutableSet.of(existingMetadata, existingModMetadata).contains(location);
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});
        assertFalse(wrapper.hasResource(existingLocation));
    }

    @Test
    public void hasResource_OriginalHasResource_Found() {
        ResourceLocation existingLocation = new ResourceLocation("existing-location.png");
        ResourceLocation existingMetadata = new ResourceLocation("existing-location.png.mcmeta");
        ResourceLocation existingModMetadata = new ResourceLocation("existing-location.png.moremcmeta");
        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public Resource getResource(ResourceLocation location) throws IOException {
                if (location.equals(existingLocation)) {
                    return new MockResourceManager.MockSimpleResource(existingLocation, "dummy");
                } else if (location.equals(existingMetadata)) {
                    return new MockResourceManager.MockSimpleResource(existingMetadata, "dummy");
                } else if (location.equals(existingModMetadata)) {
                    return new MockResourceManager.MockSimpleResource(existingModMetadata, "dummy");
                }

                throw new IOException("Not found");
            }

            @Override
            public boolean hasResource(ResourceLocation location) {
                return ImmutableSet.of(existingLocation, existingMetadata, existingModMetadata).contains(location);
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});
        assertTrue(wrapper.hasResource(existingLocation));
    }

    @Test
    public void getResources_NullLocation_NullPointerException() throws IOException {
        MockReloadableResourceManager original = new MockReloadableResourceManager();
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        expectedException.expect(NullPointerException.class);
        wrapper.getResources(null);
    }

    @Test
    public void getResources_MissingResource_IOException() throws IOException {
        ResourceLocation existingLocation = new ResourceLocation("existing-location");
        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public List<Resource> getResources(ResourceLocation location) throws IOException {
                if (hasResource(location)) {
                    return ImmutableList.of(
                            new MockResourceManager.MockSimpleResource(location, "dummy"),
                            new MockResourceManager.MockSimpleResource(location, "dummy2"),
                            new MockResourceManager.MockSimpleResource(location, "dummy3")
                    );
                }

                throw new IOException("Not found");
            }

            @Override
            public boolean hasResource(ResourceLocation location) {
                return ImmutableSet.of(existingLocation).contains(location);
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        expectedException.expect(IOException.class);
        wrapper.getResources(new ResourceLocation("missing-location"));
    }

    @Test
    public void getResources_FoundResourceNotPNG_RegularResource() throws IOException {
        ResourceLocation existingLocation = new ResourceLocation("existing-location");
        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public List<Resource> getResources(ResourceLocation location) throws IOException {
                if (hasResource(location)) {
                    return ImmutableList.of(
                            new MockResourceManager.MockSimpleResource(location, "dummy"),
                            new MockResourceManager.MockSimpleResource(location, "dummy2"),
                            new MockResourceManager.MockSimpleResource(location, "dummy3")
                    );
                }

                throw new IOException("Not found");
            }

            @Override
            public boolean hasResource(ResourceLocation location) {
                return ImmutableSet.of(existingLocation).contains(location);
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        List<Resource> resources = wrapper.getResources(existingLocation);
        assertTrue(resources.stream().allMatch((resource) -> existingLocation.equals(resource.getLocation())));
        assertTrue(resources.stream().noneMatch((resource) -> resource instanceof SizeSwappingResource));
    }

    @Test
    public void getResources_FoundResourceNotPNGWithModMetadata_RegularResource() throws IOException {
        ResourceLocation existingLocation = new ResourceLocation("existing-location");
        ResourceLocation existingMetadata = new ResourceLocation("existing-location.moremcmeta");
        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public List<Resource> getResources(ResourceLocation location) throws IOException {
                if (hasResource(location)) {
                    return ImmutableList.of(
                            new MockResourceManager.MockSimpleResource(location, "dummy"),
                            new MockResourceManager.MockSimpleResource(location, "dummy2"),
                            new MockResourceManager.MockSimpleResource(location, "dummy3")
                    );
                }

                throw new IOException("Not found");
            }

            @Override
            public boolean hasResource(ResourceLocation location) {
                return ImmutableSet.of(existingLocation, existingMetadata).contains(location);
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        List<Resource> resources = wrapper.getResources(existingLocation);
        assertTrue(resources.stream().allMatch((resource) -> existingLocation.equals(resource.getLocation())));
        assertTrue(resources.stream().noneMatch((resource) -> resource instanceof SizeSwappingResource));
    }

    @Test
    public void getResources_FoundResourceNoModMetadata_RegularResource() throws IOException {
        ResourceLocation existingLocation = new ResourceLocation("existing-location.png");
        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public List<Resource> getResources(ResourceLocation location) throws IOException {
                if (hasResource(location)) {
                    return ImmutableList.of(
                            new MockResourceManager.MockSimpleResource(location, "dummy"),
                            new MockResourceManager.MockSimpleResource(location, "dummy2"),
                            new MockResourceManager.MockSimpleResource(location, "dummy3")
                    );
                }

                throw new IOException("Not found");
            }

            @Override
            public boolean hasResource(ResourceLocation location) {
                return ImmutableSet.of(existingLocation).contains(location);
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        List<Resource> resources = wrapper.getResources(existingLocation);
        assertTrue(resources.stream().allMatch((resource) -> existingLocation.equals(resource.getLocation())));
        assertTrue(resources.stream().noneMatch((resource) -> resource instanceof SizeSwappingResource));
    }

    @Test
    public void getResources_FoundResourceModMetadataInDifferentPack_RegularResource() throws IOException {
        ResourceLocation existingLocation = new ResourceLocation("existing-location.png");
        ResourceLocation existingMetadata = new ResourceLocation("existing-location.png.moremcmeta");
        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public List<Resource> getResources(ResourceLocation location) throws IOException {
                if (location.equals(existingLocation)) {
                    return ImmutableList.of(
                            new MockResourceManager.MockSimpleResource(location, "dummy"),
                            new MockResourceManager.MockSimpleResource(location, "dummy2"),
                            new MockResourceManager.MockSimpleResource(location, "dummy3")
                    );
                } else if (location.equals(existingMetadata)) {
                    return ImmutableList.of(
                            new MockResourceManager.MockSimpleResource(location, "other"),
                            new MockResourceManager.MockSimpleResource(location, "other2"),
                            new MockResourceManager.MockSimpleResource(location, "other3")
                    );
                }

                throw new IOException("Not found");
            }

            @Override
            public boolean hasResource(ResourceLocation location) {
                return ImmutableSet.of(existingLocation, existingMetadata).contains(location);
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        List<Resource> resources = wrapper.getResources(existingLocation);
        assertTrue(resources.stream().allMatch((resource) -> existingLocation.equals(resource.getLocation())));
        assertTrue(resources.stream().noneMatch((resource) -> resource instanceof SizeSwappingResource));
    }

    @Test
    public void getResources_FoundResourceModMetadataInSamePack_SizeSwappingResource() throws IOException {
        ResourceLocation existingLocation = new ResourceLocation("existing-location.png");
        ResourceLocation existingMetadata = new ResourceLocation("existing-location.png.moremcmeta");
        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public List<Resource> getResources(ResourceLocation location) throws IOException {
                if (hasResource(location)) {
                    return ImmutableList.of(
                            new MockResourceManager.MockSimpleResource(location, "dummy"),
                            new MockResourceManager.MockSimpleResource(location, "dummy2"),
                            new MockResourceManager.MockSimpleResource(location, "dummy3")
                    );
                }

                throw new IOException("Not found");
            }

            @Override
            public boolean hasResource(ResourceLocation location) {
                return ImmutableSet.of(existingLocation, existingMetadata).contains(location);
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        List<Resource> resources = wrapper.getResources(existingLocation);
        assertTrue(resources.stream().allMatch((resource) -> existingLocation.equals(resource.getLocation())));
        assertTrue(resources.stream().allMatch((resource) -> resource instanceof SizeSwappingResource));
    }

    @Test
    public void getResources_FoundResourceModAndRegularMetadataInSamePack_SizeSwappingResource() throws IOException {
        ResourceLocation existingLocation = new ResourceLocation("existing-location.png");
        ResourceLocation existingMetadata = new ResourceLocation("existing-location.png.mcmeta");
        ResourceLocation existingModMetadata = new ResourceLocation("existing-location.png.moremcmeta");
        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public List<Resource> getResources(ResourceLocation location) throws IOException {
                if (hasResource(location)) {
                    return ImmutableList.of(
                            new MockResourceManager.MockSimpleResource(location, "dummy"),
                            new MockResourceManager.MockSimpleResource(location, "dummy2"),
                            new MockResourceManager.MockSimpleResource(location, "dummy3")
                    );
                }

                throw new IOException("Not found");
            }

            @Override
            public boolean hasResource(ResourceLocation location) {
                return ImmutableSet.of(existingLocation, existingMetadata, existingModMetadata).contains(location);
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        List<Resource> resources = wrapper.getResources(existingLocation);
        assertTrue(resources.stream().allMatch((resource) -> existingLocation.equals(resource.getLocation())));
        assertTrue(resources.stream().allMatch((resource) -> resource instanceof SizeSwappingResource));
    }

    @Test
    public void listResources_NullPath_NullPointerException() {
        MockReloadableResourceManager original = new MockReloadableResourceManager();
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        expectedException.expect(NullPointerException.class);
        wrapper.listResources(null, (file) -> true);
    }

    @Test
    public void listResources_NullPredicate_NullPointerException() {
        MockReloadableResourceManager original = new MockReloadableResourceManager();
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        expectedException.expect(NullPointerException.class);
        wrapper.listResources("path", null);
    }

    @Test
    public void listResources_PathAndPredicateMatch_FoundResources() {
        String correctPath = "dummy";
        String correctName = "test string";

        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public Collection<ResourceLocation> listResources(String path, Predicate<String> test) {
                if (correctPath.equals(path) && test.test(correctName)) {
                    return ImmutableSet.of(
                            new ResourceLocation("dummy1"),
                            new ResourceLocation("dummy2"),
                            new ResourceLocation("dummy3")
                    );
                }

                return ImmutableSet.of();
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        assertEquals(
                ImmutableSet.of(
                        new ResourceLocation("dummy1"),
                        new ResourceLocation("dummy2"),
                        new ResourceLocation("dummy3")
                ),
                wrapper.listResources(correctPath, correctName::equals)
        );
    }

    @Test
    public void listResources_OnlyPathMatches_NoResources() {
        String correctPath = "dummy";
        String correctName = "test string";

        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public Collection<ResourceLocation> listResources(String path, Predicate<String> test) {
                if (correctPath.equals(path) && test.test(correctName)) {
                    return ImmutableSet.of(
                            new ResourceLocation("dummy1"),
                            new ResourceLocation("dummy2"),
                            new ResourceLocation("dummy3")
                    );
                }

                return ImmutableSet.of();
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        assertEquals(
                ImmutableSet.of(),
                wrapper.listResources(correctPath, "test"::equals)
        );
    }

    @Test
    public void listResources_OnlyPredicateMatches_FoundResources() {
        String correctPath = "dummy";
        String correctName = "test string";

        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public Collection<ResourceLocation> listResources(String path, Predicate<String> test) {
                if (correctPath.equals(path) && test.test(correctName)) {
                    return ImmutableSet.of(
                            new ResourceLocation("dummy1"),
                            new ResourceLocation("dummy2"),
                            new ResourceLocation("dummy3")
                    );
                }

                return ImmutableSet.of();
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        assertEquals(
                ImmutableSet.of(),
                wrapper.listResources("different_path", correctName::equals)
        );
    }

    @Test
    public void listResources_NeitherMatches_FoundResources() {
        String correctPath = "dummy";
        String correctName = "test string";

        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public Collection<ResourceLocation> listResources(String path, Predicate<String> test) {
                if (correctPath.equals(path) && test.test(correctName)) {
                    return ImmutableSet.of(
                            new ResourceLocation("dummy1"),
                            new ResourceLocation("dummy2"),
                            new ResourceLocation("dummy3")
                    );
                }

                return ImmutableSet.of();
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        assertEquals(
                ImmutableSet.of(),
                wrapper.listResources("different_path", "test"::equals)
        );
    }

    @Test
    public void close_OriginalNotClosed_OriginalClosed() {
        final boolean[] isClosed = {false};

        MockReloadableResourceManager original = new MockReloadableResourceManager() {
            @Override
            public void close() {
                isClosed[0] = true;
            }
        };
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});
        wrapper.close();

        assertTrue(isClosed[0]);
    }

    @Test
    public void registerListener_NullListener_NullPointerException() {
        MockReloadableResourceManager original = new MockReloadableResourceManager();
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        expectedException.expect(NullPointerException.class);
        wrapper.registerReloadListener(null);
    }

    @Test
    public void registerAndReload_RegisteredToWrapper_ListenerReloaded() throws ExecutionException, InterruptedException {
        CompletableFuture<String> listenerCallback = new CompletableFuture<>();
        MockReloadableResourceManager original = new MockReloadableResourceManager();
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        wrapper.registerReloadListener((barrier, manager, firstProfiler, secondProfiler, firstExec, secondExec) -> {
            listenerCallback.complete("done");
            return CompletableFuture.allOf();
        });
        wrapper.createFullReload((cmd) -> {}, (cmd) -> {}, CompletableFuture.completedFuture(Unit.INSTANCE), ImmutableList.of());

        assertEquals("done", listenerCallback.get());
    }

    @Test
    public void registerAndReload_RegisteredToWrapperOriginalReloaded_ListenerReloaded() throws ExecutionException, InterruptedException {
        CompletableFuture<String> listenerCallback = new CompletableFuture<>();
        MockReloadableResourceManager original = new MockReloadableResourceManager();
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        wrapper.registerReloadListener((barrier, manager, firstProfiler, secondProfiler, firstExec, secondExec) -> {
            listenerCallback.complete("done");
            return CompletableFuture.allOf();
        });
        original.createFullReload((cmd) -> {}, (cmd) -> {}, CompletableFuture.completedFuture(Unit.INSTANCE), ImmutableList.of());

        assertEquals("done", listenerCallback.get());
    }

    @Test
    public void registerAndReload_RegisteredBeforeWrapper_ListenerReloaded() throws ExecutionException, InterruptedException {
        CompletableFuture<String> listenerCallback = new CompletableFuture<>();
        MockReloadableResourceManager original = new MockReloadableResourceManager();
        original.registerReloadListener((barrier, manager, firstProfiler, secondProfiler, firstExec, secondExec) -> {
            listenerCallback.complete("done");
            return CompletableFuture.allOf();
        });

        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});

        wrapper.createFullReload((cmd) -> {}, (cmd) -> {}, CompletableFuture.completedFuture(Unit.INSTANCE), ImmutableList.of());

        assertEquals("done", listenerCallback.get());
    }

    @Test
    public void registerAndReload_RegisteredAfterWrapper_ListenerReloaded() throws ExecutionException, InterruptedException {
        CompletableFuture<String> listenerCallback = new CompletableFuture<>();
        MockReloadableResourceManager original = new MockReloadableResourceManager();
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});
        original.registerReloadListener((barrier, manager, firstProfiler, secondProfiler, firstExec, secondExec) -> {
            listenerCallback.complete("done");
            return CompletableFuture.allOf();
        });

        wrapper.createFullReload((cmd) -> {}, (cmd) -> {}, CompletableFuture.completedFuture(Unit.INSTANCE), ImmutableList.of());

        assertEquals("done", listenerCallback.get());
    }

    @Test
    public void reload_NullFirstExecutor_NullPointerException() {
        MockReloadableResourceManager original = new MockReloadableResourceManager();
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});
        expectedException.expect(NullPointerException.class);
        wrapper.createFullReload(null, (cmd) -> {}, CompletableFuture.completedFuture(Unit.INSTANCE), ImmutableList.of());
    }

    @Test
    public void reload_NullSecondExecutor_NullPointerException() {
        MockReloadableResourceManager original = new MockReloadableResourceManager();
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});
        expectedException.expect(NullPointerException.class);
        wrapper.createFullReload((cmd) -> {}, null, CompletableFuture.completedFuture(Unit.INSTANCE), ImmutableList.of());
    }

    @Test
    public void reload_NullFuture_NullPointerException() {
        MockReloadableResourceManager original = new MockReloadableResourceManager();
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});
        expectedException.expect(NullPointerException.class);
        wrapper.createFullReload((cmd) -> {}, (cmd) -> {}, null, ImmutableList.of());
    }

    @Test
    public void reload_NullPacksList_NullPointerException() {
        MockReloadableResourceManager original = new MockReloadableResourceManager();
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});
        expectedException.expect(NullPointerException.class);
        wrapper.createFullReload((cmd) -> {}, (cmd) -> {}, CompletableFuture.completedFuture(Unit.INSTANCE), null);
    }

    @Test
    public void reload_NoListeners_NoException() {
        MockReloadableResourceManager original = new MockReloadableResourceManager();
        SizeSwappingResourceManager wrapper = new SizeSwappingResourceManager(original, () -> {});
        wrapper.createFullReload((cmd) -> {}, (cmd) -> {}, CompletableFuture.completedFuture(Unit.INSTANCE), ImmutableList.of());
    }

    /**
     * Provides access to the results of void methods in {@link SimpleReloadableResourceManager} to
     * verify that the manager wrapper works correctly.
     * @author soir20
     */
    private static class MockReloadableResourceManager extends SimpleReloadableResourceManager {
        private final List<PackResources> PACK_RESOURCES;

        public MockReloadableResourceManager() {
            super(PackType.CLIENT_RESOURCES);
            PACK_RESOURCES = new ArrayList<>();
        }

        @Override
        public void add(PackResources packResources) {
            super.add(packResources);
            PACK_RESOURCES.add(packResources);
        }

        @Override
        public Set<String> getNamespaces() {
            return PACK_RESOURCES.stream().flatMap(
                    (pack) -> pack.getNamespaces(PackType.CLIENT_RESOURCES).stream()
            ).collect(Collectors.toSet());
        }
    }

    /**
     * A dummy {@link PackResources}.
     * @author soir20
     */
    private static class MockPackResources implements PackResources {
        private static final InputStream EMPTY_STREAM = new ByteArrayInputStream("{ }".getBytes());
        private final String NAME;

        public MockPackResources(String name) {
            NAME = name;
        }

        @Override
        public InputStream getRootResource(String string) {
            return EMPTY_STREAM;
        }

        @Override
        public InputStream getResource(PackType packType, ResourceLocation resourceLocation) {
            return EMPTY_STREAM;
        }

        @Override
        public Collection<ResourceLocation> getResources(PackType packType, String string, String string2, int i, Predicate<String> predicate) {
            return ImmutableSet.of();
        }

        @Override
        public boolean hasResource(PackType packType, ResourceLocation resourceLocation) {
            return false;
        }

        @Override
        public Set<String> getNamespaces(PackType packType) {
            return ImmutableSet.of(NAME);
        }

        @Override
        public <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) {
            return null;
        }

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public void close() {

        }
    }

}