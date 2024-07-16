from datetime import datetime
from os import listdir, path
from pprint import pprint

from batcore.data import (
    CN,
    WRC,
    ACRec,
    MRLoaderData,
    PullLoader,
    RevFinder,
    RevRec,
    Tie,
    cHRev,
    get_gerrit_dataset,
    xFinder,
)
from naive1 import MostActiveRev, RandomRec, RandomWeightedRec
from thesis import Thesis1

_ = MostActiveRev, RandomRec, RandomWeightedRec
_ = RevRec, ACRec, cHRev, CN, xFinder, RevFinder, Tie, WRC


BATC_LOG_FILE = "/tmp/batcore_logs_ds"


def single_ds_stat(model_cls, data_dir):
    data = MRLoaderData(
        data_dir,
        verbose=False,
        log_file_path=BATC_LOG_FILE,
    )

    dataset = get_gerrit_dataset(
        data, max_file=20, model_cls=model_cls, owner_policy="author_no_na"
    )

    data_iterator = PullLoader(dataset)

    authors = set()
    files = set()
    reviewers = set()
    dates = set()
    event_count = 0

    for datas in data_iterator:
        for datas2 in datas:
            if not isinstance(datas2, list):
                datas2 = [datas2]
                for data in datas2:
                    authors.update(data["author"])

                    files.update(data["file"])

                    reviewers.update(data["reviewer"])

                    dates.add(data["date"])

                    event_count += 1
                    # pprint(data)

    print(
        f"--- {data_dir=}\n{len(authors)=}\n{len(files)=}\n{len(reviewers)=}\n{event_count=}"
    )
    parsed_dates = [timestamp for timestamp in dates]

    min_date = min(parsed_dates)
    max_date = max(parsed_dates)

    print(f"{min_date=}, {max_date=}")


def df_stats(dataset_dir, dataset_names):
    res = {}
    if not dataset_names:
        dataset_names = [
            d for d in listdir(dataset_dir) if path.isdir(path.join(dataset_dir, d))
        ]

    for ds in dataset_names:
        res[ds] = single_ds_stat(Thesis1, path.join(dataset_dir, ds))

    return res
