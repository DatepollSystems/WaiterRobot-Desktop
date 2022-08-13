# WaiterRobot-Mediator

## Build

At the moment compose for desktop does not support cross-compilation. Therefor we currently distribute "bundled jars".
It's also required to have one jar per platform and architecture.

1. Change `implementation(compose.desktop.currentOs)` to use the dependency for the platform you want to create a jar
   for.
2. Do a gradle sync and run `./gradlew build`
3. You will find the jar at `build/libs/Mediator-*.jar`

### Native executables

[see](https://github.com/JetBrains/compose-jb/blob/master/tutorials/Native_distributions_and_local_execution/README.md)
(Currently no cross-compilation support.)
TODO create CI which builds for all platforms when cross-compilation is supported.

## Recommendations

- Install the `Compose Multiplatform IDE Support`
  plugin ([details](https://plugins.jetbrains.com/plugin/16541-compose-multiplatform-ide-support)) for IntelliJ IDEA

