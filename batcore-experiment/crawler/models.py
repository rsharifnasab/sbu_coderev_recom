import os
import threading
from itertools import islice
from logging import getLogger
from os import environ, path
from shutil import rmtree
from subprocess import PIPE, Popen

import pandas as pd
from github import Auth, Commit, Github, PullRequest
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

    def get_commit_author_formatted(self, commit: Commit.Commit):
        try:
            local_commit = self.get_local_commit_by_sha(commit.sha)
        except ValueError:
            local_commit = None

        effective_author = (
            (local_commit and local_commit.author)
            or commit.author
            or (local_commit and local_commit.committer)
            or commit.committer
        )

        return Repo.format_user(effective_author)

    def get_pr_authors(self, pr: PullRequest.PullRequest):
        # TODO: get all commits authors
        use_remote_pr_author = 1
        if use_remote_pr_author:
            return [Repo.format_user(pr.user)]
        else:
            all_authors = set()
            for commit in pr.get_commits():
                all_authors.add(self.get_commit_author_formatted(commit))

            return list(all_authors)

    def get_reviewrs(self, pr: PullRequest.PullRequest):
        all_reviewers = set()
        for comment in pr.get_review_comments():
            all_reviewers.add(Repo.format_user(comment.user))

        return list(all_reviewers)

    def get_local_commit_by_sha(self, sha: str):
        with self.cloned_lock:
            return next(Repository(self.cloned_dir, single=sha).traverse_commits())

    @staticmethod
    def concat(df_base, row_dict):
        assert row_dict != {}, "new row dict is empty"
        new_row_df = pd.DataFrame(row_dict)

        if df_base is not None and len(df_base):
            return pd.concat([df_base, new_row_df], ignore_index=True)
        return pd.DataFrame(row_dict)

    @staticmethod
    def format_user(user):
        assert user is not None, "user is None"
        name = user.name or user.login
        assert user.email or name, f"user {user} does not have name or email"
        return f"{{{name}}}:{{{user.email}}}:{{{name}}}"
        # that should be "{name}:{e-mail}:{login}"

    def pull_iter(self, n=None):
        assert self.gh_repo is not None
        if n:
            return islice(self.gh_repo.get_pulls(state="all"), n)
        return self.gh_repo.get_pulls(state="all")

    def get_pr_modified_files(self, pr: PullRequest.PullRequest) -> list:
        return [mf.filename for mf in pr.get_files()]

    def get_commit_modified_files(self, sha: str) -> list:
        raise "not implemented"
        commit = self.get_local_commit_by_sha(sha)
        return [mf.new_path or mf.old_path for mf in commit.modified_files]
