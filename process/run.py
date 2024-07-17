import logging

from batcore.data import CN, WRC, ACRec, RevFinder, RevRec, Tie, cHRev, xFinder
from data_stats import plot_df_stats
from naive1 import MostActiveRev, RandomRec, RandomWeightedRec
from thesis import Thesis1

from process import coderev_rec

_ = MostActiveRev, RandomRec, RandomWeightedRec
_ = RevRec, ACRec, cHRev, CN, xFinder, RevFinder, Tie, WRC

logging.basicConfig(level=logging.WARN)


MEASURES = [
    "mrr",
    #
    "acc@3",
    "rec@3",
    "prec@3",
    "f1@3",
    #
    "acc@10",
    "rec@10",
    "prec@10",
    "f1@10",
    # "time",
]


def main():
    plot_df_stats(
        "../data-combined/",
        dataset_names=None,
    )
    return
    coderev_rec(
        models=[
            ("chrev", cHRev, lambda _: cHRev()),
            ("acrec", ACRec, lambda _: ACRec()),
            ("tie", Tie, lambda ds: Tie(ds.get_items2ids())),  # should be item list?
            ("revfinder", RevFinder, lambda ds: RevFinder(ds.get_items2ids())),
            ####
            # ("xfinder", xFinder, lambda _: xFinder()),
            ####
            # ("revrec", RevRec, lambda ds: RevRec(ds.get_items2ids())),
            # ("cn", CN, lambda ds: CN(ds.get_items2ids())),
            # ("wrc", WRC, lambda ds: WRC(ds.get_items2ids())),
            ####
            # ("naive_rand", RandomRec, lambda _: RandomRec()),
            ("naive_wrand", RandomWeightedRec, lambda _: RandomWeightedRec()),
            ("naive_freq", MostActiveRev, lambda _: MostActiveRev()),
            ####
            ("thesis_extend", Thesis1, lambda _: Thesis1(True)),
            ("thesis_noextend", Thesis1, lambda _: Thesis1(False)),
        ],
        dataset_names=[
            "aws",
            #  "bazlets",
            #  "k8s",
        ],
        dataset_dir="../data-combined/",
        measures=MEASURES,
    )


if __name__ == "__main__":
    main()
