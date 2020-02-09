#!/bin/bash

# update deployment on remote server
# Usage: ssh [user]@[server] 'bash -s' < deploy.sh

REMOTE_DIR="~/warta-gki-cawang-search"

cd $REMOTE_DIR
git pull origin master
pm2 restart all