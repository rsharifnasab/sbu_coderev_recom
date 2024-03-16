import logging
from logging import getLogger
from os import makedirs, path
from pprint import pformat, pprint

import pandas as pd
from models import Repo

log = getLogger("crawl")

SAVE_EVERY_ITER = 5


class Crawler:
    def __init__(self, save_path: str, repo: Repo, pull_count=10, save_every_iter=5):
        self.save_path = save_path
        self.pulls_path = path.join(self.save_path, "pull.csv")
        self.commits_path = path.join(self.save_path, "commits.csv")
        self.comments_path = path.join(self.save_path, "comments.csv")
        print(self.pulls_path)

        self.pull_count = pull_count
        self.save_every_iter = save_every_iter

        if not path.exists(self.save_path):
            makedirs(self.save_path)

        self.repo: Repo = repo

    def persist(self, pulls_df, commits_df, comments_df):
        log.debug("persisting dfs")
        file_exists = path.isfile(self.pulls_path)
        save_params = {
            "index": False,
            "header": not file_exists,
            "encoding": "UTF-8",
            "mode": "a",
        }

        pulls_df["date"] = pulls_df["date"].dt.strftime("%Y-%m-%d %H:%M:%S")
        if len(pulls_df["closed"].dropna()):
            #print(pulls_df["closed"])
            #print(pulls_df[~pulls_df["closed"].isna()])
            pulls_df["closed"] = pulls_df[~pulls_df["closed"].isna()].dt.strftime("%Y-%m-%d %H:%M:%S")

        if len(comments_df):
            comments_df["key_date"] = comments_df["key_date"].dt.strftime("%Y-%m-%d %H:%M:%S")
        pulls_df.to_csv(self.pulls_path, **save_params)
        commits_df.to_csv(self.commits_path, **save_params)
        comments_df.to_csv(self.comments_path, **save_params)

    def crawl(self):
        assert self.repo.gh_repo is not None
        assert self.repo.gh is not None
        assert self.repo.cache_dir is not None
        assert self.repo.cloned_dir is not None

        pulls = pd.DataFrame()
        commits = pd.DataFrame()
        comments = pd.DataFrame()

        #for pr_comment in self.repo.gh_repo.get_pulls_review_comments():
        #    print(pr_comment.url)
        #    break

        for i, pr in enumerate(self.repo.pull_iter(self.pull_count)):
            try:
                log.info("processing %d", pr.id)

                modified_files = self.repo.get_commit_modified_files(
                    pr.merge_commit_sha
                )
                author_list = self.repo.get_authors(pr)
                reviewer = self.repo.get_reviewrs(pr)

                new_pulls_row = {
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

                new_commits_row = {
                        "key_commit" : [],
                        "key_change" : [],
                        "key_file": [],
                        "key_user" : [],
                        "date" : [],
                }
                for commit in pr.get_commits():
                    for changed_file in commit.files:
                        new_commits_row["key_commit"].append(commit.sha)
                        new_commits_row["key_change"].append(pr.id)
                        new_commits_row["key_file"].append(changed_file.filename)
                        new_commits_row["key_user"].append(Repo.format_user(commit.committer))
                        new_commits_row["date"].append(commit.commit.last_modified_datetime)
                log.info("fetched commits %s", pformat(new_commits_row))


                new_comments_row = {
                        "key_change": [],
                        "key_file": [],
                        "key_user" : [],
                        "key_date": [],
                }

                for comment in pr.get_review_comments():
                    new_comments_row["key_change"].append(pr.id)
                    new_comments_row["key_file"].append(comment.path)
                    new_comments_row["key_user"].append(Repo.format_user(comment.user))
                    new_comments_row["key_date"].append(comment.created_at)
                log.debug("fetched comments %s", pformat(new_comments_row))




                log.info(pformat(new_pulls_row))
                log.info(pformat(new_commits_row))
                log.info(pformat(new_comments_row))

                pulls = Repo.concat(pulls, new_pulls_row)
                commits = Repo.concat(commits, new_commits_row)
                comments = Repo.concat(comments, new_comments_row)

                if i % self.save_every_iter == 0:
                    self.persist(pulls, commits, comments)
                    pulls = pulls.iloc[0:0]
                    commits = commits.iloc[0:0]
                    comments = comments.iloc[0:0]
                    logging.debug("saving successfull")
            except ValueError as e:
                log.warning(e)
