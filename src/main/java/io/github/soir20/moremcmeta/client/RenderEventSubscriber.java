package io.github.soir20.moremcmeta.client;

import io.github.soir20.moremcmeta.client.renderer.AnimatedRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RenderEventSubscriber {

    /* Any mods' pre-render events that fired before this event will fire again,
       so we should try to fire first */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static <T extends LivingEntity, M extends EntityModel<T>> void
    onRenderLivingPre(final RenderLivingEvent.Pre<T, M> event) {
        T entity = (T) event.getEntity();

        if (event.isCancelable() && !(event.getBuffers() instanceof AnimatedRenderTypeBuffer)) {
            event.setCanceled(true);

            float partialTicks = event.getPartialRenderTick();
            float yaw = MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw);

            // Inject our own buffer that can create both animated and non-animated vertex builders
            AnimatedRenderTypeBuffer buffer = new AnimatedRenderTypeBuffer(event.getBuffers());

            event.getRenderer().render(entity, yaw, partialTicks, event.getMatrixStack(), buffer, event.getLight());
        }
    }

}
