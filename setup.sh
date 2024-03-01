#!/usr/bin/env bash

set -euo pipefail

function init_datasets() {
    (
        mkdir -p ./datasets
        cd ./datasets
        git clone --branch master --depth 1 https://github.com/XLipcak/rev-rec.git || true
        git clone --branch master --depth 1 https://github.com/patanamon/revfinder.git || true
    )
}

init_datasets
