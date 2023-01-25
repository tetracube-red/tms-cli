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
* 🟢 create user - build in progress

## How to use the CLI

1. create local copy of the file `deployment.example.yaml`;
2. fill the file with your data according with the description supplied in the
file itself;
3. run the cli indicating the path of the configuration file;

The CLI accepts these options to run:
* `--config-file`, `-c`: 

## ToDo

Here a random list of things to do as next steps
 - [x] remove keycloak as gatekeeper
 - [ ] configure private and public keys for jwt publication
 - [ ] create config maps and secrets for creating jwt and assign them to the proper services
 - [ ] query the house api to create the house
 - [ ] query the guest api to create guest and associate with the house
 - [ ] in some way the responses of the apis should be stored for further more uses (save them into the file?)
 - 