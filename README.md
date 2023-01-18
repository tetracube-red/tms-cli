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

* `-g, --gatekeeper-admin-password`: required - the password of the *admin* user
of Keycloak service
* `-a, --gatekeeper-hostname`: required - the hostname where the Keycloak service
will be exposed in the network and **MUST** be equal to the CN field of the certificate

* `OPERATION_TYPE`: specifies which operation you want perform with the CLI, available 
options are: install
* `KUBERNETES_CONFIG_FILE`: **always required** - specify the full path where is possible 
to read a valid kubernetes value (same file to allow kubectl to connect to the same cluster);
* `INSTALLATION_NAME`: **required only for installation** - how the installation should 
be named, this is useful in case of multiple installations in the same cluster;
* `DB_PASSWORD`: **required only for installation** - the database password;
* `DB_DATA_PATH`: **required only for installation** - the path where the database 
files will be persisted;
* `INSTALLATION_EXPOSE_SERVICES`: **required only for installation** - all internal 
services will be exposed through the load balancer of K8s;
* `AFFINITY_NODE_NAME`: 
* `GATEKEEPER_ADMIN_PASSWORD`: 
* `SOLUTION_HOSTNAME`: 
* `CERTIFICATE_KEY_FILE`: 
* `CERTIFICATE_PEM_FILE`: 