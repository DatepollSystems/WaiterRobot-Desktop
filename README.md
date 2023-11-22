# WaiterRobot-Mediator

## Install App
Download the latest Version [here](https://datepollsystems.github.io/waiterrobot-ios/download.html). 

## Run

To run with a specific version you can supply the app version as system properties / vm-options
`-Dapp.version="99.99.99"`

> Use `99.99.99` as version so that the backend knows that all features are available or supply a specific 
> version if you have checked out an old release or want to test with a "real" version string.

## Create Release

Releases are created by CI and are published to GitHub releases. There are executables for Windows, Mac and Linux
created. To create a new release just push a tag in the form of `vmajor.minor.patch` (e.g. `v1.0.0`) to GitHub. This
will start the CI. After a few minutes the artifacts can be downloaded
from [Releases](https://github.com/DatepollSystems/waiterrobot-desktop/releases) or 
[Download](https://datepollsystems.github.io/waiterrobot-ios/download.html).

## Recommendations

- Install the `Compose Multiplatform IDE Support`
  plugin ([details](https://plugins.jetbrains.com/plugin/16541-compose-multiplatform-ide-support)) for IntelliJ IDEA

