# Troubleshooting Guide
This guide aims to help you resolve common issues without needing to file a bug report.

## Nothing Happens When I Install the Mod
1. Make sure you've [animated a texture](MCMETA-FORMAT.md). MoreMcmeta doesn't change the game until you animate a texture. See the default pack in the next bullet.

2. Try using the trial resource pack. If the trial textures work, there may be an issue with your textures.
   * Use a [JSON validator](https://jsonlint.com/) to detect syntax issues in your `.mcmeta` files.
   * Make sure your animation data is under a `"moremcmeta"` heading, **not** an `"animation"` heading.
   * Check the [`.mcmeta` format guide](MCMETA-FORMAT.md). In particular, make sure you defined a width and height for non-square frames.
   * Please [report](https://github.com/soir20/MoreMcmeta/issues) the issue if you can't determine the cause. Include the textures/`.mcmeta` files you are using.
    
3. Make sure you downloaded the mod from an [official site](../../README.md#download).

4. Check that you are using a Forge profile in the Minecraft launcher.

5. Ensure you installed the mod in the `mods` folder associated with your Forge launcher profile.

6. Try removing all other mods. If MoreMcmeta works alone, there may be a compatibility issue. Add mods back to determine which one is causing the problem.
   * Please [report](https://github.com/soir20/MoreMcmeta/issues) the issue. However, compatibility issues with coremods or mods that alter Minecraft's texturing process will likely not be resolved.
    
7. If all else fails, [report](https://github.com/soir20/MoreMcmeta/issues) the problem. Include any textures/`.mcmeta` files that are not working.

## Low FPS/Client Lag
Although you can install MoreMcmeta on a server without errors, it only does client-side work. Therefore, MoreMcmeta probably isn't the culprit if you're experiencing tick lag (block breaking delays, etc.).

However, if you are experiencing client-side lag, try these steps:
1. Remove MoreMcmeta from your `mods` folder to check if the lag issue persists. If you still have lag, MoreMcmeta probably isn't the cause.
2. Disable interpolation on each of your textures. Interpolation isn't enabled by default, so if you or your resource pack didn't enable it for a texture, you don't have to do anything.
3. Remove a few of the largest animated textures in your resource pack (the largest by individual frame size, not number of frames).
4. [Report](https://github.com/soir20/MoreMcmeta/issues) the problem. Please include your hardware information (graphics card manufacturer/series) and your resource pack.

### How Does MoreMcmeta Minimize Lag?
Animating a texture requires updating many pixels—the game has to do more work to render a frame. Because of this, animating many textures with MoreMcmeta can cause lag.

The game has to do even more work when frames are interpolated. It has to calculate the color of each pixel in the interpolated frame from the current frame and the next frame. Storing all of these interpolated frames for numerous textures does not work well because it would require a significant amount of memory.

To reduce the amount of work required for rendering, MoreMcmeta analyzes your textures when they are first loaded (usually during world startup). It determines which pixels change throughout the animation. Then it selectively interpolates the pixels that change. For example, about 50% or more of a GUI texture's pixels can be skipped.

MoreMcmeta also only ticks (updates) the textures it controls when they are rendered, instead of updating them on every tick like block textures. This means GUIs that aren't open and mobs that aren't visible won't contribute to the frame rendering time.

## Other Issues
Please ensure that you downloaded MoreMcmeta from an [official site](../../README.md#download) and are using the latest `.jar` file for your Minecraft version.

If that does not resolve the problem, [report a bug](https://github.com/soir20/MoreMcmeta/issues). Include screenshots of the issue and example texture/`.mcmeta` files.