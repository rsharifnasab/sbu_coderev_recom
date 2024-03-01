#!/usr/bin/env python3

import enum
from os.path import join, isfile, basename, splitext
from os import listdir
from json import load as json_load, loads as json_load_str
import logging
from re import split as re_split, sub as re_replace
from json import dumps

DS_DIR = "../datasets/revfinder"

def pprint(data):
    print(dumps(data, indent=4))

def load():
    result = {}
    for filename in listdir(DS_DIR):
        p = join(DS_DIR, filename)
        if not isfile(p) or not filename.endswith("json"):
            continue

        pure_name, _ = splitext(basename(filename))
        logging.debug("loading %s (%s)", filename, pure_name)

        with open(p, "r", encoding="UTF-8") as f:
            content = f.read()
            content = "[" + re_replace(r"\}\s+\{", "},{", content) + "]"
            j = json_load_str(content)
            result[pure_name] = j
        logging.debug("Dataset %s loaded successfully", filename)

    return result


if __name__ == "__main__":
    logging.basicConfig(level=logging.DEBUG)
    pprint(load())
