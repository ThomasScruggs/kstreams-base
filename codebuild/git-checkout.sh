#!/usr/bin/env bash

set -ex

echo $*
branch=$1
git=$2
repo_key_base=$3

echo "branch is $branch"
mkdir -p /root/.ssh
aws secretsmanager get-secret-value --secret-id ${repo_key_base}-public-key | jq ."SecretString" | sed 's/"//g' > /root/.ssh/id_rsa.pub
private_key=$(aws secretsmanager get-secret-value --secret-id ${repo_key_base}-private-key | jq ."SecretString" | sed 's/"//g')
echo $private_key > /root/.ssh/id_rsa
chmod 700 /root/.ssh
chmod 600 /root/.ssh/id_rsa
cat /root/.ssh/id_rsa
chmod 600 /root/.ssh/id_rsa.pub
cat /root/.ssh/id_rsa.pub
git clone ${git}
git checkout ${branch}
git branch --set-upstream-to origin/${branch}
#git branch -u origin/${branch}
#git config branch.${branch}.remote origin
#git config branch.${branch}.merge refs/heads/${branch}
git remote set-url origin ${git}:${branch}
git config --global user.email "devops@plumewifi.com"
git config --global user.name "machine-plume"