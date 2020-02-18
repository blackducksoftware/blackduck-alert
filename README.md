## Overview ##
<!-- The comments following wrap around the description string for parsing during the build. Do Not Remove -->
<!-- description-text-start -->
This application provides the ability to send notifications from a provider to various distribution channels.
<!-- description-text-end -->
## Build ##

[![Build Status](https://travis-ci.org/blackducksoftware/blackduck-alert.svg?branch=master)](https://travis-ci.org/blackducksoftware/blackduck-alert)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Black Duck Security Risk](https://copilot.blackducksoftware.com/github/repos/blackducksoftware/blackduck-alert/branches/master/badge-risk.svg)](https://copilot.blackducksoftware.com/github/repos/blackducksoftware/blackduck-alert/branches/master)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.blackducksoftware.integration%3Ablackduck-alert&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.synopsys.integration%3Ablackduck-alert)

### Build Steps ###
#### Requirements ####
 - JDK 11

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

### To Run the Server Locally ### 
This section describes running the server locally.

#### Run Commands ####
Execute the one of the following commands which will build the source code and then start the server:

##### Without the Initial Settings Splash Screen #####
```
./gradlew runServer --nosplash
```
Note: The 'nosplash' option sets the encryption and default email address to default values.  With these items set the initial settings screen is not displayed because Alert is configured with its minimum default values.

##### With the All Features ##### 
```
./gradlew runServer
```

#### Accessing the Server #### 
You can access the Alert user interface at the following URL: https://localhost:8443/alert

## Where can I get the latest release? ##
<!-- The comments following wrap around the project url string for parsing during the build. Do Not Remove -->
<!-- project-url-text-start -->
All releases are on the GitHub release page. https://github.com/blackducksoftware/blackduck-alert/releases
<!-- project-url-text-end -->

## Documentation ##
Our public Confluence page: https://synopsys.atlassian.net/wiki/spaces/INTDOCS/pages/187564033/Synopsys+Alert

Installation Documentation available for the following:
 - [Docker Compose](https://github.com/blackducksoftware/blackduck-alert/blob/master/deployment/docker-compose/README.md) 
 - [Docker Swarm](https://github.com/blackducksoftware/blackduck-alert/blob/master/deployment/docker-swarm/README.md)
 - [Kubernetes](https://synopsys.atlassian.net/wiki/spaces/BDLM/pages/153583626/Synopsys+Alert+Installation+Guide+for+Synopsys+Operator)
