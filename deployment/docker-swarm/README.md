# Black Duck Alert On Docker Swarm.

## Requirements

The docker-compose.yml file contains a volume specification.  
Before installing or upgrading Alert you must create desired persistent storage volumes for Alert.

### Upgrading Alert
The steps in the upgrade procedure are the same as the installation procedure.  
You will remove the stack and then re-deploy your stack.

### Installing Alert

#### Standalone Installation
All below commands assume:
- you are using the namespace 
- you have a cluster with at least 2GB of allocatable memory.
- you have administrative access to your cluster.

