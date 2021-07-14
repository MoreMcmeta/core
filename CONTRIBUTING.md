# Contributing Guidelines
Thanks for your interest in improving MoreMcmeta! There are several ways to contribute, even if you are not a developer.

## For All Users

### I'm Not a Developer. How Can I Contribute?
The best way to contribute is to create resource packs using MoreMcmeta. This reveals bugs and areas for improvement, as well as letting more people know about the mod (if you decide to publish your pack).

Reporting bugs and suggesting features are also excellent ways to contribute.

### The Code of Conduct
We ask all contributors to review and follow the [Code of Conduct](CODE_OF_CONDUCT.md).

### I Found a Bug.
Before you report a bug, the [troubleshooting guide](https://github.com/soir20/MoreMcmeta/wiki/User-Docs:-Troubleshooting) may help you resolve the issue. This guide provides immediate help for your and creates less work for the mod's maintainers.

To report a bug, [create an issue](https://github.com/soir20/MoreMcmeta/issues) with the bug report template. The template will guide you as to which information you should provide.

It is helpful if you provide screenshots or a video and your resource packs. A `debug.log` file from the `log` directory inside your Minecraft profile folder is also useful. (The undated, uncompressed log is always the latest. If you need help finding this file, you can ask in the issue you create.)

More detail is always better.

### I Want to Suggest a Feature.
To suggest a feature, [create an issue](https://github.com/soir20/MoreMcmeta/issues) with the feature request template. The template will guide you as to which information you should provide.

Please note that suggestions that break compatibility with the vanilla `.mcmeta` format will not be accepted. However, suggestions that improve the format will be considered.

## For Developers

### Before You Submit a Pull Request
* Please file an issue before implementing a new feature. You don't want to write a new feature and then have it be denied because it doesn't fit with the mod.
* The [wiki](https://github.com/soir20/MoreMcmeta/wiki) has several pages written for developers that may be helpful.
* Do not create or change anything that breaks compatibility with the vanilla `.mcmeta` format.

### Pull Request Requirements and Code Style Guidelines
This section is a little long to be specific about how MoreMcmeta's code is formatted, but the most important goal is to make your code readable to your future self and others.

Currently, a specific linter has not been chosen, though one may be in the future.

#### Formatting
* 100 characters is the soft limit for line length. 120 characters is the hard maximum because some Minecraft and MoreMcmeta names can become longer than 100 characters with generics.
* Four-space indents.
* Spaces between binary operators. No spaces around unary operators or parentheses.
* Spaces after commas.
* All code blocks should have braces, even if the block is only one line.
* Open braces go on the same line as the method or class declaration.
* Use parentheses in boolean expressions to clarify logical operator precedence only. For example:
```
// Good
if (x > 10 && y > 10) {
    // ...
}

// Good
if (x > 10 || (y > 10 && z > 10)) {
    // ...
}

// Bad - it's obvious the relational operators come before the logical ones
if ((x > 10) && (y > 10)) {
    // ...
}

// Bad - at first glance, you might think this is (x > 10 || y > 10) && z > 10
if (x > 10 || y > 10 && z > 10) {
    // ...
}
```

#### Naming
* Use clear and concise names. If you need a comment to explain what a variable is, you need a better name.
* Single-letter variable names are disallowed, except for `x` and `y` for coordinate points. 
* Single-letter class names are for generic parameters.
* Variable and class names should end with a noun. Method names should start with a verb.
* Name tests like `methodOrWork_Condition_Result`.

#### Commenting
* All source code must have complete Javadoc comments. Test code only needs a top-level class comment.
    * Include your name or GitHub username (whichever you prefer) with the `@author` tag.
    * A version number or date is not necessary.

#### Other
* Readability is more important than tiny differences in performance. Obviously, you should not use a quadratic algorithm when a linear one is not difficult to implement.
    * For example, MoreMcmeta uses bitwise operators in the `RGBAInterpolator` because the math is directly related to the binary form of an integer, not for speed.
    * There are usually ways to improve performance that are better than micro-optimizations.
* All code must be unit tested to a reasonable extent, though there is no specific code coverage requirement. See the [unit testing page](https://github.com/soir20/MoreMcmeta/wiki/Dev-Docs:-Unit-Testing) for recommendations.
* See the [package structure page](https://github.com/soir20/MoreMcmeta/wiki/Dev-Docs:-Package-Structure) for guidance on where to put your code.
* Use `requireNonNull` for null checks at the beginning of every method.
* Use `CustomTickable` instead of Minecraft's `Tickable`. This prevents Minecraft's code from updating textures when it is not wanted.

Pull requests may still be rejected even if they meet these requirements. Usually, revisions will be requested first.
