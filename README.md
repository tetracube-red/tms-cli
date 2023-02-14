# TetraCube Management System CLI

This project is build to help users to install and configure properly the
TetraCube platform.

The CLI can

* install the platform in a Kubernetes target environment
* configure and initialize house
* invite new guests with the right authorizations

For each kind of operation the CLI ha a proper command:

* [ ] *install*
* [ ] *initialize* the hub with the information
* [ ] *guest create* to create new guests

## How to use the CLI

### Install the platform

The installation requires some information and file that must be provided to the CLI to complete
successfully the application:

* K8S cluster configuration file
* HTTPS certificate files

You need to specify some options in order to install the platform in the target environment, so
beside the "**install**" command you need to specify:

* `--cert-files=<certificates>[,<certificates>...]`: specifies key and certificate files separated by comma
* `--db-password=<dbPassword>`: define the name of database
* `-h, --help`: show this help message and exit.
* `--hostname=<hostname>`: define the platform's hostname
* `--installation-name=<installationName>`: define the platform installation name
