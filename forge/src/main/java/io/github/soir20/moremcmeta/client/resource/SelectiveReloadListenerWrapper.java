package io.github.soir20.moremcmeta.client.resource;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Wraps a general resource reload listener with Forge's {@link ISelectiveResourceReloadListener}.
 * Helps separate cross-loader resource code from Forge's interface.
 * @author soir20
 */
@ParametersAreNonnullByDefault
public class SelectiveReloadListenerWrapper implements ISelectiveResourceReloadListener {
    private final IResourceType RESOURCE_TYPE;

    /* Forge deprecates the general listener in favor of the Forge-provided selective one.
       The point of this class is to decouple the resource reload code from Forge;
       we're still using the selective one. */
    @SuppressWarnings("deprecation")
    private final ResourceManagerReloadListener LISTENER;

    /**
     * Creates a selective wrapper for a given listener and type.
     * @param resourceType      the type of resource reloading that should trigger the listener
     * @param listener          the listener triggered when the given type reloads
     */
    @SuppressWarnings("deprecation")
    public SelectiveReloadListenerWrapper(IResourceType resourceType,
                                          ResourceManagerReloadListener listener) {
        RESOURCE_TYPE = resourceType;
        LISTENER = listener;
    }

    /**
     * Triggers the wrapped listener when the correct resource type is loaded.
     * @param resourceManager       resource manager that is reloading
     * @param resourcePredicate     predicate for the type that is being reloaded
     */
    @Override
    public void onResourceManagerReload(ResourceManager resourceManager,
                                        Predicate<IResourceType> resourcePredicate) {
        requireNonNull(resourceManager, "Resource manager cannot be null");
        requireNonNull(resourcePredicate, "Resource predicate cannot be null");

        if (!resourcePredicate.test(getResourceType())) {
            return;
        }

        LISTENER.onResourceManagerReload(resourceManager);
    }

    /**
     * Gets the resource type whose reloading will trigger this listener.
     * @return the resource type for this listener
     */
    @Override
    public IResourceType getResourceType()
    {
        return RESOURCE_TYPE;
    }

}
