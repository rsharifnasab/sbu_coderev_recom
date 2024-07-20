#!/usr/bin/env python3

import logging

from batcore.data import CN, WRC, ACRec, RevFinder, RevRec, Tie, cHRev, xFinder
from lib.data_stats import plot_df_stats
from lib.process import coderev_rec
from lib.stat_test import stat_test
from naive1 import MostActiveRev, RandomRec, RandomWeightedRec
from thesis import Thesis1, Thesis2

_ = MostActiveRev, RandomRec, RandomWeightedRec
_ = RevRec, ACRec, cHRev, CN, xFinder, RevFinder, Tie, WRC
_ = Thesis1, Thesis2

logging.basicConfig(level=logging.WARN)


MEASURES = [
    "mrr",
    #
    "acc@3",
    "rec@3",
    "prec@3",
    "f1@3",
    #
    "acc@5",
    "rec@5",
    "prec@5",
    "f1@5",
    #
    "acc@10",
    "rec@10",
    "prec@10",
    "f1@10",
    # "time",
]

INVESTIGATE_DS = not True


def main():
    ds_names = [
        # "gerrit",
        #   "gerrit-ci-scripts",
        #     "git-repo",
        #      "k8s-gerrit",
        #     "gitiles",
        #    "zoekt",
        "gwtorm",
    ]

    if INVESTIGATE_DS:
        plot_df_stats(
            "../data-all/",
            dataset_names=ds_names,
        )

    df = coderev_rec(
        models=[
            ("chrev", cHRev, lambda _: cHRev()),
            ("acrec", ACRec, lambda _: ACRec()),
            # ("tie", Tie, lambda ds: Tie(ds.get_items2ids())),  # should be item list?
            ####
            # very slow
            # ("revfinder", RevFinder, lambda ds: RevFinder(ds.get_items2ids())),
            # inaccurate
            # ("xfinder", xFinder, lambda _: xFinder()),
            ####
            # ("revrec", RevRec, lambda ds: RevRec(ds.get_items2ids())),
            # ("cn", CN, lambda ds: CN(ds.get_items2ids())),
            # ("wrc", WRC, lambda ds: WRC(ds.get_items2ids())),
            ####
            # ("naive_rand", RandomRec, lambda _: RandomRec()),
            # ("naive_wrand", RandomWeightedRec, lambda _: RandomWeightedRec()),
            # ("naive_freq", MostActiveRev, lambda _: MostActiveRev()),
            ####
            ("thesis_extend", Thesis1, lambda _: Thesis1(True)),
            ("thesis_noextend", Thesis1, lambda _: Thesis1(False)),
            # ("thesis_2_extend", Thesis2, lambda _: Thesis2(True)),
            # ("thesis_2_noextend", Thesis2, lambda _: Thesis2(False)),
        ],
        dataset_names=ds_names,
        dataset_dir="../data-all/",
        measures=MEASURES,
        seperate_graphs=not False,
    )
    stat_test(df, measures=MEASURES)


if __name__ == "__main__":
    main()
