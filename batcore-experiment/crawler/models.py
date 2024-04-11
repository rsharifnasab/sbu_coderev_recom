import os
import threading
from itertools import islice
from logging import getLogger
from os import environ, path
from shutil import rmtree
from subprocess import PIPE, Popen

import pandas as pd
from github import Auth, Github, PullRequest
from github import Repository as GHRepository
from pydriller import Repository

log = getLogger("run")


class RepositoryCloneError(Exception):
    pass


class Repo:
    def __init__(self, repo_name: str, repo_url=None, cache_dir=None):
        self.repo_name = repo_name
        self.repo_url = repo_url or f"https://github.com/{repo_name}.git"
        self.cache_dir = cache_dir or "/tmp/cloned_repos"
        self.cloned_dir = path.join(
            self.cache_dir, self.repo_name.replace(r"/", "-"))
        self.gh: Github | None = None
        self.gh_repo: GHRepository.Repository | None = None
        self.local_repo: Repository | None = None
        self.cloned_lock = threading.Lock()

    def git_clone(self):
        os.makedirs(self.cache_dir, exist_ok=True)
        if path.exists(self.cloned_dir):
            log.info("repo %s already exists", self.repo_name)
            return

        log.info("cloning to cache")
        with Popen(
            ["git", "clone", self.repo_url, self.cloned_dir],
            stdout=PIPE,
            stderr=PIPE,
        ) as clone_process:
            _, stderr = clone_process.communicate()

            if clone_process.returncode == 0:
                log.info("cloning to cache successfull")
                return

            log.warning("cloning failed with exit code %d",
                        clone_process.returncode)

            if os.path.exists(self.cloned_dir):
                rmtree(self.cloned_dir)

            raise RepositoryCloneError(stderr.decode().strip())

    def init(self):
        self.git_clone()
        log.info("connecting to github")
        gh_token = environ["gh_auth_token"]
        auth = Auth.Token(gh_token)
        self.gh = Github(auth=auth)
        self.gh_repo = self.gh.get_repo(self.repo_name)
        self.local_repo = Repository(self.cloned_dir)
        log.info("authentication successfull to github")

    @staticmethod
    def convert_state(gh_state):
        if gh_state == "closed":
            return "ABANDONED"
        if gh_state == "merged":
            return "MERGED"
        if gh_state == "open":
            return "OPEN"
        log.warning("invalid PR status (%s)", gh_state)
        return "OPEN"

    def get_commit_author_formatted(self, commit_sha: str):
        local_commit = self.get_local_commit_by_sha(commit_sha)
        assert local_commit is not None

        author = local_commit.author

        # handle web UI problem
        if not author or not author.email:
            author = local_commit.committer

        if not author:
            return ""

        return Repo.format_user(author)

    def get_pr_authors(self, pr: PullRequest.PullRequest):
        all_authors = set()
        for commit in pr.get_commits():
            all_authors.add(self.get_commit_author_formatted(commit.sha))

        return list(all_authors)

    def get_reviewrs(self, pr: PullRequest.PullRequest):
        all_reviewrs = set()
        for comment in pr.get_review_comments():
            all_reviewrs.add(Repo.format_user(comment.user))

        return list(all_reviewrs)

    def get_local_commit_by_sha(self, sha):
        with self.cloned_lock:
            try:
                return next(Repository(self.cloned_dir, single=sha).traverse_commits())
            except ValueError:
                return None

    @staticmethod
    def concat(df_base, row_dict):
        new_row_df = pd.DataFrame(row_dict)
        if df_base is not None and len(df_base):
            return pd.concat([df_base, new_row_df], ignore_index=True)
        return pd.DataFrame(row_dict)

    @staticmethod
    def format_user(user):
        assert user is not None, "user is None"
        return f"{{{user.name}}}:{{{user.email}}}:{{{user.name}}}"
        # that should be "{name}:{e-mail}:{login}"

    def pull_iter(self, n=None):
        assert self.gh_repo is not None
        if n:
            return islice(self.gh_repo.get_pulls(state="all"), n)
        return self.gh_repo.get_pulls(state="all")

    def get_commit_modified_files(self, sha) -> list:
        commit = self.get_local_commit_by_sha(sha)
        if commit:
            return [mf.new_path or mf.old_path for mf in commit.modified_files]
        return []
