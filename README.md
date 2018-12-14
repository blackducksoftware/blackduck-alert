## Overview ##
<!-- The comments following wrap around the description string for parsing during the build. Do Not Remove -->
<!-- description-text-start -->
This application provides the ability to send notifications from a provider to various distribution channels.
<!-- description-text-end -->
## Build ##

[![Build Status](https://travis-ci.org/blackducksoftware/blackduck-alert.svg?branch=master)](https://travis-ci.org/blackducksoftware/blackduck-alert)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Black Duck Security Risk](https://copilot.blackducksoftware.com/github/repos/blackducksoftware/blackduck-alert/branches/master/badge-risk.svg)](https://copilot.blackducksoftware.com/github/repos/blackducksoftware/blackduck-alert/branches/master)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.blackducksoftware.integration%3Ablackduck-alert&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.blackducksoftware.integration%3Ablackduck-alert)


### Build Steps ###
To compile the UI and create the jar file run:

```
./gradlew clean build
```
To build the docker image:

```
./gradlew buildDockerImage
```
To build the zip file containing the orchestration files for deployment:

```
./gradlew createDeploymentZip
```


## Where can I get the latest release? ##
<!-- The comments following wrap around the project url string for parsing during the build. Do Not Remove -->
<!-- project-url-text-start -->
All releases are on the GitHub release page. https://github.com/blackducksoftware/blackduck-alert/releases
<!-- project-url-text-end -->

## Documentation ##
Our public Confluence page: https://synopsys.atlassian.net/wiki/spaces/INTDOCS/pages/622882/Black+Duck+Alert
