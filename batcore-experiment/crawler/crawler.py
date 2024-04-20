from concurrent.futures import ThreadPoolExecutor, as_completed
from logging import getLogger
from os import makedirs, path
from pprint import pformat

import pandas as pd
from models import Repo

log = getLogger("crawl")

SAVE_EVERY_ITER = 1

THREAD_COUNT = 1


class Crawler:
    def __init__(self, save_path: str, repo: Repo, pull_count=10, save_every_iter=5):
        self.save_path = save_path
        self.pulls_path = path.join(self.save_path, "pulls.csv")
        self.commits_path = path.join(self.save_path, "commits.csv")
        self.comments_path = path.join(self.save_path, "comments.csv")

        self.pull_count = pull_count
        self.save_every_iter = save_every_iter

        if not path.exists(self.save_path):
            makedirs(self.save_path)

        self.repo: Repo = repo

    @staticmethod
    def format_date(date):
        desired_format = "%Y-%m-%d %H:%M:%S"
        if pd.notnull(date):
            return pd.to_datetime(date).strftime(desired_format)
        else:
            return date

    def persist(self, pulls_df, commits_df, comments_df):
        log.debug("persisting dfs")
        file_exists = path.isfile(self.pulls_path)
        save_params = {
            "index": False,
            "header": not file_exists,
            "encoding": "UTF-8",
            "mode": "a",
        }

        pulls_df["date"] = pulls_df["date"].apply(Crawler.format_date)
        pulls_df["closed"] = pulls_df["closed"].apply(Crawler.format_date)
        comments_df["date"] = comments_df["date"].apply(Crawler.format_date)

        pulls_df["key_change"] = pulls_df["key_change"].astype(int)
        commits_df["key_change"] = commits_df["key_change"].astype(int)
        comments_df["key_change"] = comments_df["key_change"].astype(int)

        pulls_df.to_csv(self.pulls_path, **save_params)
        commits_df.to_csv(self.commits_path, **save_params)
        comments_df.to_csv(self.comments_path, **save_params)

    def fetch_pr_func(self, pr):
        try:
            log.info("processing %d", pr.html_url)

            modified_files = self.repo.get_commit_modified_files(
                pr.merge_commit_sha)
            author_list = self.repo.get_pr_authors(pr)
            reviewer = self.repo.get_reviewrs(pr)

            new_pulls_row = {
                "key_change": [pr.number],
                "file": [modified_files],
                "reviewer": [reviewer],
                "date": [pr.created_at],
                "owner": [Repo.format_user(pr.user)],
                "title": [pr.title],
                "status": [Repo.convert_state(pr.state)],
                "closed": [pr.closed_at],
                "author": [author_list],
            }

            new_commits_row = {
                "key_commit": [],
                "key_change": [],
                "key_file": [],
                "key_user": [],
                "date": [],
            }
            for commit in pr.get_commits():
                for changed_file in commit.files:
                    new_commits_row["key_commit"].append(commit.sha)
                    new_commits_row["key_change"].append(pr.number)
                    new_commits_row["key_file"].append(changed_file.filename)
                    new_commits_row["key_user"].append(
                        Repo.format_user(commit.committer)
                    )
                    new_commits_row["date"].append(
                        commit.commit.last_modified_datetime)

            new_comments_row = {
                "key_change": [],
                "key_file": [],
                "key_user": [],
                "date": [],
            }

            for comment in pr.get_review_comments():
                new_comments_row["key_change"].append(int(pr.number))
                new_comments_row["key_file"].append(comment.path)
                new_comments_row["key_user"].append(
                    Repo.format_user(comment.user))
                new_comments_row["date"].append(comment.created_at)

            # log.debug(pformat(new_pulls_row))
            # log.debug(pformat(new_commits_row))
            # log.debug(pformat(new_comments_row))

            return new_pulls_row, new_commits_row, new_comments_row

        except ValueError as e:
            log.warning(e)
            return {}, {}, {}

    def fast_crawl(self):
        assert self.repo.gh_repo is not None
        assert self.repo.gh is not None
        assert self.repo.cache_dir is not None
        assert self.repo.cloned_dir is not None

        pulls = pd.DataFrame()
        commits = pd.DataFrame()
        comments = pd.DataFrame()

        pull_iterator = self.repo.pull_iter(self.pull_count)

        with ThreadPoolExecutor(max_workers=THREAD_COUNT) as executor:
            futures = [
                executor.submit(self.fetch_pr_func, url) for url in pull_iterator
            ]
            # TODO
            iterator = as_completed(futures)
            for i, feature in enumerate(iterator):
                pull_row, commits_row, comments_row = feature.result()

                pulls = Repo.concat(pulls, pull_row)
                commits = Repo.concat(commits, commits_row)
                comments = Repo.concat(comments, comments_row)

                if i % SAVE_EVERY_ITER == 0:
                    rows = len(pulls["date"])
                    log.debug("%d persisting %d rows", i, rows)
                    self.persist(pulls, commits, comments)

                    pulls = pulls.iloc[0:0]
                    commits = commits.iloc[0:0]
                    comments = comments.iloc[0:0]

                    log.info("saving %d rows successfull", rows)

    def crawl(self):
        assert self.repo.gh_repo is not None
        assert self.repo.gh is not None
        assert self.repo.cache_dir is not None
        assert self.repo.cloned_dir is not None

        pulls = pd.DataFrame()
        commits = pd.DataFrame()
        comments = pd.DataFrame()

        for i, pr in enumerate(self.repo.pull_iter(self.pull_count)):
            try:
                log.info("processing %s", pr.html_url)
                log.debug("new pull row")
                new_pulls_row = {
                    "key_change": [pr.number],
                    "file": [self.repo.get_pr_modified_files(pr)],
                    "reviewer": [self.repo.get_reviewrs(pr)],
                    "date": [pr.created_at],
                    "owner": [[Repo.format_user(pr.user)]],
                    "title": [pr.title],
                    "status": [Repo.convert_state(pr.state)],
                    "closed": [pr.closed_at],
                    "author": [self.repo.get_pr_authors(pr)],
                }

                log.debug("new commit row")
                new_commits_row = {
                    "key_commit": [],
                    "key_change": [],
                    "key_file": [],
                    "key_user": [],
                    "date": [],
                }
                for commit in pr.get_commits():
                    for changed_file in commit.files:
                        new_commits_row["key_commit"].append(commit.sha)
                        new_commits_row["key_change"].append(pr.number)
                        new_commits_row["key_file"].append(
                            changed_file.filename)
                        new_commits_row["key_user"].append(
                            self.repo.get_commit_author_formatted(commit),
                        )
                        new_commits_row["date"].append(
                            commit.commit.last_modified_datetime
                        )

                log.debug("new comment row")
                new_comments_row = {
                    "key_change": [],
                    "key_file": [],
                    "key_user": [],
                    "date": [],
                }

                for comment in pr.get_review_comments():
                    new_comments_row["key_change"].append(pr.number)
                    new_comments_row["key_file"].append(comment.path)
                    new_comments_row["key_user"].append(
                        Repo.format_user(comment.user))
                    new_comments_row["date"].append(comment.created_at)

                # log.debug(pformat(new_pulls_row))
                # log.debug(pformat(new_commits_row))
                # log.debug(pformat(new_comments_row))

                pulls = Repo.concat(pulls, new_pulls_row)
                commits = Repo.concat(commits, new_commits_row)
                comments = Repo.concat(comments, new_comments_row)

                if i % self.save_every_iter == 0:
                    log.debug("persisting...")
                    self.persist(pulls, commits, comments)
                    pulls = pulls.iloc[0:0]
                    commits = commits.iloc[0:0]
                    comments = comments.iloc[0:0]
                    log.debug("saving successfull")
            except ValueError as e:
                log.warning(e)
                raise e  # TODO
