#!/bin/bash
artifactory_url=${1}
artifactory_user=${2}
artifactory_pw=${3}

set -e

mkdir -p ~/.sbt
chmod 755 ~/.sbt
echo "realm=Artifactory Realm" > ~/.sbt/.credentials
echo "host=${artifactory_url}" >> ~/.sbt/.credentials
echo "user=${artifactory_user}" >> ~/.sbt/.credentials
echo "password=${artifactory_pw}" >> ~/.sbt/.credentials
