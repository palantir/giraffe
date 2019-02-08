#!/bin/sh
set -ex

SSH_KEY_PATH=/home/circleci/.ssh/integration-test

# generate an ssh key for use in testing
ssh-keygen -t rsa -b 2048 -N '' -f ${SSH_KEY_PATH}

# install sshpass so we can use a password on the the command line
sudo apt-get update
sudo apt-get install -y sshpass

# install an authorized keys file
sshpass -p root -- ssh root@localhost "mkdir -p /root/.ssh && tee -a /root/.ssh/authorized_keys" < ${SSH_KEY_PATH}.pub

# generate a secondary user for testing with the same SSH key
ssh -i ${SSH_KEY_PATH} root@localhost "adduser -D -s /bin/ash giraffe && cp /root/.ssh /home/giraffe && chown -R giraffe:giraffe /home/giraffe"

# copy test file creation scripts
scp -i ${SSH_KEY_PATH} ssh/build/system-test-files/exec-creator.sh ssh.build/system-test-files/file-creator.sh  giraffe@localhost:~

# create test files
ssh -i ${SSH_KEY_PATH} giraffe@localhost "./exec-creator.sh exec && ./file-creator.sh file"
