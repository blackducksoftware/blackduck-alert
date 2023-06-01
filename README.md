## Overview ##
<!-- The comments following wrap around the description string for parsing during the build. Do Not Remove -->
<!-- description-text-start -->
This application provides the ability to send notifications from a provider to various distribution channels.
<!-- description-text-end -->
## Build ##

[![Build Status](https://travis-ci.org/blackducksoftware/blackduck-alert.svg?branch=master)](https://travis-ci.org/blackducksoftware/blackduck-alert)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Black Duck Security Risk](https://copilot.blackducksoftware.com/github/repos/blackducksoftware/blackduck-alert/branches/master/badge-risk.svg)](https://copilot.blackducksoftware.com/github/repos/blackducksoftware/blackduck-alert/branches/master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.synopsys.integration%3Ablackduck-alert&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.synopsys.integration%3Ablackduck-alert)
### Build Steps ###
#### Requirements ####
 - JDK 17
 - Node 16.15.1 (Recommended)
 - NPM 8.11.0 (Recommended)

To compile the UI and create the jar file run:


```bash
./gradlew clean build
```
To build the docker images:

```bash
./gradlew dockerBuildAllImages
```
To build the zip file containing the orchestration files for deployment:

```bash
./gradlew createDeploymentZip
```

### To Run the Server Locally ### 
This section describes running the server locally.

#### Run Commands ####
Execute the one of the following commands which will build the source code and then start the server:

##### Run the Server Locally #####
```bash
./gradlew runServer
```
Note: The server will be running locally over: https://localhost:8443/alert

#### UI Development with Hot Module Replacement
These commands will start a webpack dev server that can access the backend for rapid UI development.  
The UI files will automatically be reloaded if there are changes through hot module replacement.

##### 1. Start the Backend Server #####
```bash
./gradlew runServer
```
Note: The server will be running locally over: https://localhost:8443/alert

##### 2. Start the webpack dev server #####
```bash
npm run start
```
Note: The webpack dev server will be running locally over: https://localhost:9000/alert 

## Where can I get the latest release? ##
<!-- The comments following wrap around the project url string for parsing during the build. Do Not Remove -->
<!-- project-url-text-start -->
All releases are on the GitHub release page. https://github.com/blackducksoftware/blackduck-alert/releases
<!-- project-url-text-end -->

## Documentation ##
Our public documentation, including installation instructions, is located here: https://sig-product-docs.synopsys.com/bundle/synopsys-alert/page/topics/general/synopsys-alert-overview.html
