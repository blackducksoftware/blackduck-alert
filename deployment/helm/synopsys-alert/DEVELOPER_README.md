# Alert Helm Chart Versioning
This document covers the versioning of the Helm charts for synopsys-alert.
The version of the helm chart can be released independently of a product release if only modifications to the helm chart have been made.

## Table of Contents
- [Updating chart version](#updating-chart-version)
    - [Understanding Alert chart versioning](#understanding-alert-chart-versioning)
    - [Updating the chart number](#updating-the-chart-number)
    - [Updating the chart for a new release version](#updating-the-chart-for-a-new-release-version)
- [Creating the package with Gradle](#creating-the-package-with-gradle)
- [Creating the package manually](#creating-the-package-manually)
    - [Using Chart.yaml version](#using-chartyaml-version)
    - [Specifying versions](#specifying-versions)

## Updating chart version
This section will walk through updating the helm chart versioning.

### Understanding Alert chart versioning
In Chart.yaml the following version attributes exist:
```yaml
# This is the chart version. This version number should be incremented each time you make changes
# to the chart and its templates, including the app version.
version: ALERT_VERSION_TOKEN

# This is the version number of the application being deployed. This version number should match
# the version of Alert
appVersion: ALERT_VERSION_TOKEN
```
When the build runs the ALERT_VERSION_TOKEN text will be replaced with the Gradle project version.

Example:
```yaml
# This is the chart version. This version number should be incremented each time you make changes
# to the chart and its templates, including the app version.
version: 6.0.0

# This is the version number of the application being deployed. This version number should match
# the version of Alert
appVersion: 6.0.0
```

### Updating the chart number
If there is an issue with the Helm chart that requires a new Helm chart to be published modify the `version:` attribute value.
Here a chart number will be added to the end of the `ALERT_VERSION_TOKEN` i.e. `ALERT_VERSION_TOKEN-1`

The format will be:
```yaml
# This is the chart version. This version number should be incremented each time you make changes
# to the chart and its templates, including the app version.
version: ALERT_VERSION_TOKEN-<CHART_NUMBER>

# This is the version number of the application being deployed. This version number should match
# the version of Alert
appVersion: ALERT_VERSION_TOKEN
```
- The addition of the `-<CHART_NUMBER>` is left to the developer to add 
- Replace <CHART_NUMBER> with the number of the chart starting from 1 and increment with each release of the chart for that application version of Alert.
    - Examples:
        - 6.0.0-1
        - 6.0.0-2
        - ...
        - 6.0.0-n
       
### Updating the chart for a new release version
When working on a new release of Alert reset the chart versioning back to the original.  Remove the chart number `-<CHART_NUMBER>` if it exists.

Reset to:
```yaml
# This is the chart version. This version number should be incremented each time you make changes
# to the chart and its templates, including the app version.
version: ALERT_VERSION_TOKEN

# This is the version number of the application being deployed. This version number should match
# the version of Alert
appVersion: ALERT_VERSION_TOKEN
```

## Creating the package with Gradle
If you want to build the package file from the source run the following command
```console
$ ./gradlew helmValidation
```
A helm package tar ball will be created in the following directory:

`build/deployment/helm/synopsys-alert`

## Creating the package manually
If you want to build the package manually for testing you can follow these procedures.

### Using Chart.yaml version
To use the appVersion and version in the Chart.yaml file execute this command.
```console
$ helm package . 
```

### Specifying versions
To specify a different version than the values in the Chart.yaml file execute this command.
```console
$ helm package . --app-version <APP_VERSION> --version <CHART_VERSION>
```
- Note: 
    - Replace `<APP_VERSION>` with the application version to use.
    - Replace `<CHART_VERSION>` with the helm chart version to use.





