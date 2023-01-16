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
1. a valid (also self-signed) SSL certificate
2. some folder where to store and persist data
3. how nodes of your kubernetes cluster are named
4. a good control of kubernetes cluster

#### How to create a self-signed certificate
In some cases is not important to have a valid certificate, but you can
trust in a certificate that is made by you.
1. run OpenSSL command to generate the key and certificate itself:
```shell
openssl req -newkey rsa:2048 -nodes \
  -keyout keycloak-server.key.pem \
  -x509 -days 3650 \
  -out keycloak-server.crt.pem
```

It will prompt for details like
```text
Country Name (2 letter code) []:
State or Province Name (full name) []:
Locality Name (eg, city) []:
Organization Name (eg, company) []:
Organizational Unit Name (eg, section) []:
Common Name (eg, fully qualified host name) []:
Email Address []
```

### Installation options explained
The `install` command accepts these options
* `-c, --k8s-config`: required - specify the full path where is possible to read a valid
kubernetes value (same file to allow kubectl to connect to the same cluster)
* `-n, --installation-name`: not required - how the installation should be named,
this is useful in case of multiple installations in the same cluster. If not supplied 
a default name will be used
* `-d, --db-data-path`: required - the path where the database file are persisted
and is used in case of database container rollout
* `-p, --db-password`: required - the database password
* `-e, --expose-services`: not required - if specified all internal services will
be exposed through the load balancer of the cluster
* `-g, --gatekeeper-admin-password`: required - the password of the *admin* user
of Keycloak service
* `-a, --gatekeeper-hostname`: required - the hostname where the Keycloak service
will be exposed in the network and **MUST** be equal to the CN field of the certificate
  -Dquarkus.args=" -e -g change_me_admin --gatekeeper-hostname=gatekeeper.tetracube.red --gatekeeper-certificate=/home/dave_cube/keycloak-server.crt.pem,/home/dave_cube/keycloak-server.key.pem"