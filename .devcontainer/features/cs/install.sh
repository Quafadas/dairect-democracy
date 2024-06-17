#!/bin/sh

set -e

# Install Coursier - can call cs from command line
curl -fL "https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz" | gzip -d > /usr/local/bin/cs && chmod +x /usr/local/bin/cs

# Install metals - should prevent dependancy downloads on container start
cs install metals