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

logging.getLogger("requests").setLevel(logging.DEBUG)
logging.getLogger("urllib3").setLevel(logging.DEBUG)
logging.getLogger("pydriller").setLevel(logging.WARNING)
logging.getLogger("git").setLevel(logging.WARNING)

logging.getLogger("requests").setLevel(logging.WARN)
logging.getLogger("urllib3").setLevel(logging.WARN)


log = getLogger("run")

log.debug("importing packages finished without error")

REPO_NAME = "1995parham/github-do-not-ban-us"
REPO_NAME = "aradzie/keybr.com"
PR_COUNT = 5000


def main():
    load_dotenv()
    log.debug("loaded env")

    repo = Repo(REPO_NAME)
    repo.init()

    crawler = Crawler("./data/", repo, pull_count=PR_COUNT)
    crawler.crawl()


if __name__ == "__main__":
    main()
