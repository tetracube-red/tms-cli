# TetraCube Management System CLI

This project is build to help people to install and configure properly the
TetraCube platform.

The CLI can
* install the platform in a Kubernetes target environment
* configure and initialize house
* invite new guests giving the right authorizations

For each kind of operation the CLI ha a proper command:
* install
* create house
* create user

## Installing the TetraCube platform
The command "**install**" accepts a series of arguments that are helpful 
to configure properly the platform and to interact with the target system.

### Installation requirements
You need some stuff to apply an installation successfully.
1. some folder where to store and persist data 
2. how nodes of your kubernetes cluster are named
3. a good control of kubernetes cluster

