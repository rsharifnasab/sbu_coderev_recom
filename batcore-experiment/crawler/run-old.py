#!/usr/bin/env python3

import json
import logging
import os
import urllib.request
from itertools import islice
from logging import DEBUG, INFO, WARN, getLogger
from os import environ, path, stat
from pprint import pprint
from shutil import rmtree
from subprocess import PIPE, Popen

import pandas as pd
from dotenv import load_dotenv
from github import Auth, Github, PullRequest
from github import Repository as GHRepository
from pydriller import Repository

_ = DEBUG, INFO, WARN
logging.basicConfig(level=INFO)
log = getLogger("run")

log.debug("importing packages finished without error")

REPO_NAME = "PyGithub/PyGithub"
PR_COUNT = 5



def persist(self, pulls_df):
    file_exists = path.isfile(self.news_path)
    save_params = {
        "index": False,
        "header": not file_exists,
        "encoding": "UTF-8",
        "mode": "a",
    }
    news_df.to_csv(self.news_path, **save_params)
    tags_df.to_csv(self.tags_path, **save_params)
def main():
    repo = Repo(REPO_NAME)
    repo.init()

    pulls = pd.DataFrame()

    for pr in repo.pull_iter(PR_COUNT):
        log.info("processing %d", pr.id)

        modified_files = repo.get_commit_modified_files(pr.merge_commit_sha)
        author_list = repo.get_authors(pr)
        reviewer = repo.get_reviewrs(pr)

        new_row = {
            "key_change": [pr.id],
            "file": [modified_files],
            "reviewer": [reviewer],
            "date": [pr.created_at],
            "owner": [Repo.format_user(pr.user)],
            "title": [pr.title],
            "status": [Repo.convert_state(pr.state)],
            "closed": [pr.closed_at],
            "author": [author_list],
        }
        pprint(new_row)

        pulls = Repo.concat(pulls, new_row)

    print(pulls)


if __name__ == "__main__":
    load_dotenv()
    log.debug("loaded env")
    main()
