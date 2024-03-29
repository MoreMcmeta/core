# MoreMcmeta
![MoreMcmeta logo](./info/img/moremcmeta-logo-gradient.png)

Animate almost any texture with more options. Texture configuration API.

This means:
* more possibilities for resource packs
* [a familiar animation format](https://github.com/MoreMcmeta/core/wiki/User-Docs:-Animation-Format)
* not invasive—better compatibility with other mods

![Mob, GUI, shadow, moon, and enchantment glint examples](./info/img/demo.gif)

## Are You in the Right Place?
If you're here to make a suggestion or bug report, find out how MoreMcmeta works, or contribute to its development, you're in the right place!

If you're looking to download MoreMcmeta or find out more about it, check out the [CurseForge page](https://www.curseforge.com/minecraft/mc-mods/moremcmeta-fabric).

## For All Contributors
The [Contributing Guide](CONTRIBUTING.md) explains how to submit a suggestion, file a bug report, or create a pull request. That page includes all types of contributors, not just developers, and may be a helpful starting point.

The [Code of Conduct](CODE_OF_CONDUCT.md) describes acceptable vs. inappropriate behavior in this repository. In short, use common sense.

## For Developers
### Build
MoreMcmeta uses Gradle and the [Architectury Plugin](https://github.com/architectury/architectury-plugin) for cross-mod loader builds. However, it does not use the Architectury API. There simply isn't enough boilerplate code that MoreMcmeta needs to justify another dependency. JUnit is the unit testing framework.

If you've set up a modded Minecraft environment before, MoreMcmeta is not much different. The main difference is that you'll need to [provide credentials](https://github.com/MoreMcmeta/core/wiki/Plugin-Docs:-Maven) to download MoreMcmeta Maven packages from GitHub Packages. Gradle will do most of the work after you import the project.

There's a lot of build tasks, but the important ones are the `build` and `test` tasks under `common` (cross-loader), `forge`, and `fabric`. These correspond to MoreMcmeta's three source directories/Gradle subprojects.

* The `build` task generates a finished mod in `fabric/build/libs` or `forge/build/libs`.
* The `test` task runs unit tests for the given directory. The best way to view code coverage is to run the task for all subprojects and add the results together.

### Run
After you import the Gradle project, the Architectury plugin should automatically generate run configurations for the client and server on Forge and Fabric. MoreMcmeta is a client-sided mod, but the server tasks are important to verify that it does not crash a dedicated server.

You can also use the `runClient` Gradle tasks if the run configurations do not appear.

### Release
View built releases at the [Releases](https://github.com/MoreMcmeta/core/releases) page. Generally, this page has exactly the same versions as CurseForge. Stable development builds may be there in the future.