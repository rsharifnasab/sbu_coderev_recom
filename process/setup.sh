#!/usr/bin/env bash

set -euo pipefail
REPO_URL="https://github.com/JetBrains-Research/batcore.git"
DIR_NAME="batcore-repo"

if [ ! -d "$DIR_NAME" ]; then
    echo "Directory $DIR_NAME does not exist. Cloning repository..."
    git clone --branch main --depth 1 $REPO_URL $DIR_NAME
else
    echo "Directory $DIR_NAME already exists."
fi

pacman -S tk
# install pyenv and pyenv-virtualenv
pyenv install 3.8.19
pyenv virtualenv 3.8 batc
pyenv shell batc
#pyenv activate batc
uv pip install -r ./requirements.txt

uv pip install git+https://github.com/JetBrains-Research/batcore
