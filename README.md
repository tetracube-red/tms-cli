# TetraCube Management System CLI

This project is build to help people to install and configure properly the
TetraCube platform.

The CLI can
* install the platform in a Kubernetes target environment
* configure and initialize house
* invite new guests giving the right authorizations

For each kind of operation the CLI ha a proper command:
* 🟢 install - build in progress
* 🟦 create house - to do
* 🟦 create user - to do

## How to use the CLI

In the main folder of the project there is a file called **.env.example** file. This file 
contains all possibile values used by the CLI to start any kind of operation. 

Here the explained list of available operations. Each operation is required only in certain
cases or only in certain kind of operation. 

* `OPERATION_TYPE`: specifies which operation you want perform with the CLI, available 
options are: install
* `KUBERNETES_CONFIG_FILE`: **always required** - specify the full path where is possible 
to read a valid kubernetes value (same file to allow kubectl to connect to the same cluster);
* `INSTALLATION_NAME`: **always required** - how the installation should 
be named, this is useful in case of multiple installations in the same cluster;
* `DB_PASSWORD`: **required only for installation** - the database password;
* `DB_DATA_PATH`: **required only for installation** - the path where the database 
files will be persisted;
* `INSTALLATION_EXPOSE_SERVICES`: **required only for installation** - all internal 
services will be exposed through the load balancer of K8s;
* `AFFINITY_NODE_NAME`: **required only for installation** - the name of the node where
the db data path is created;
* `GATEKEEPER_ADMIN_PASSWORD`: **always required** - the admin password
to use for administrator of keycloak system
* `SOLUTION_HOSTNAME`: **always required** - the hostname exported for all
services in K8s platform
* `CERTIFICATE_KEY_FILE`: **required only for installation** - the key of SSL certificate file  
* `CERTIFICATE_PEM_FILE`: **required only for installation** - the key of SSL certificate 
itself 