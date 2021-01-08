package io.github.soir20.moremcmeta.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.soir20.moremcmeta.MoreMcmeta;
import io.github.soir20.moremcmeta.client.renderer.AnimatedRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RenderEventSubscriber {
    private static final MethodHandle renderMap;
    static {
        MethodHandle renderItemHandle = null;

        try {
            Method renderItem = FirstPersonRenderer.class.getDeclaredMethod("renderItemInFirstPerson",
                    AbstractClientPlayerEntity.class, float.class, float.class, Hand.class, float.class,
                    ItemStack.class, float.class, MatrixStack.class, IRenderTypeBuffer.class, int.class);
            renderItem.setAccessible(true);
            renderItemHandle = MethodHandles.lookup().unreflect(renderItem);
        } catch (NoSuchMethodException err) {
            MoreMcmeta.LOGGER.error("Map rendering method not found");
        } catch (IllegalAccessException err) {
            MoreMcmeta.LOGGER.error("Map rendering method not accessible");
        } finally {
            renderMap = renderItemHandle;
        }
    }

    /* Any mods' pre-render events that fired before this event will fire again,
       so we should try to fire first */
    //@SubscribeEvent(priority = EventPriority.HIGHEST)
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

    @SuppressWarnings("unused")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderHand(final RenderHandEvent event) throws Throwable {
        if (event.isCancelable() && !(event.getBuffers() instanceof AnimatedRenderTypeBuffer)) {
            event.setCanceled(true);

            Minecraft minecraft = Minecraft.getInstance();
            FirstPersonRenderer renderer = minecraft.getFirstPersonRenderer();
            AbstractClientPlayerEntity player = minecraft.player;

            if (renderMap != null) {
                renderMap.invokeExact(renderer, player, event.getPartialTicks(), event.getInterpolatedPitch(),
                        event.getHand(), event.getSwingProgress(), event.getItemStack(), event.getEquipProgress(),
                        event.getMatrixStack(), (IRenderTypeBuffer) new AnimatedRenderTypeBuffer(event.getBuffers()),
                        event.getLight());
            }
        }
    }

}
