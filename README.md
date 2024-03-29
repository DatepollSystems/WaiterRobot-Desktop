# WaiterRobot-Mediator

## Install App

Download the latest Version [here](https://datepollsystems.github.io/WaiterRobot-Desktop/download.html).

## Run

To run with a specific version you can supply the app version as system properties / vm-options
`-Dapp.version="99.99.99"`

> Use `99.99.99` as version so that the backend knows that all features are available or supply a specific
> version if you have checked out an old release or want to test with a "real" version string.

## Create Release

Releases are created by CI and are published to GitHub releases. There are executables for Windows, Mac and Linux
created. To create a new release just push a tag in the form of `vmajor.minor.patch` (e.g. `v1.0.0`) to GitHub. This
will start the CI. After a few minutes the artifacts can be downloaded
from [here](https://datepollsystems.github.io/WaiterRobot-Desktop/download.html) (
or[GitHub Releases](https://github.com/DatepollSystems/WaiterRobot-Desktop/releases)).

For convenience there is also a gradle task to start a release.

```sh
./gradlew release
```

You will be asked if you want to increase `major`, `minor` or `patch`.  
You can also supply a specific version by using the `v` parameter `./gradlew release -Pv=1.2.3`.

## Recommendations

- Install the `Compose Multiplatform IDE Support`
  plugin ([details](https://plugins.jetbrains.com/plugin/16541-compose-multiplatform-ide-support)) for IntelliJ IDEA

## Manual Testing Tool

This tool is for simulating an event and testing the whole system.
It is only possible to test on lava or local instances.

To use it simply use the `Manual Testing Tool` IntelliJ run configuration or start it by manually clicking on the "run"
icon next to the main function in IntelliJ.

The tool will ask for some information like credentials, organization, event and waiters to use. Just follow the given
instructions. Do not forget to also start a Mediator for the selected event with all printer connected to the Virtual
Printer. Otherwise, all the orders will queue up and be printed at the next Mediator start.
The Tool will also log everything that happens to files.
The virtual printers save all pdfs to the file system in the `virtualPrinters` folder.