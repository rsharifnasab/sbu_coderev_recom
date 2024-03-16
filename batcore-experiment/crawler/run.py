#!/usr/bin/env python3

import logging
from logging import DEBUG, INFO, WARN, getLogger

import urllib3
from crawler import Crawler
from dotenv import load_dotenv
from models import Repo

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

_ = DEBUG, INFO, WARN
logging.basicConfig(level=DEBUG)
log = getLogger("run")

log.debug("importing packages finished without error")

REPO_NAME = "PyGithub/PyGithub"
PR_COUNT = 50


def main():
    load_dotenv()
    log.debug("loaded env")

    repo = Repo(REPO_NAME)
    repo.init()

    crawler = Crawler("./data/", repo, pull_count=PR_COUNT)
    crawler.fast_crawl()


if __name__ == "__main__":
    main()
