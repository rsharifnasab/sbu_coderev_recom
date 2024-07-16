import logging
import time
from hashlib import md5
from pprint import pprint

import pandas as pd
import plotly.graph_objects as go
import plotly.io as pio
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
from batcore.tester import RecTester
from data_stats import df_stats
from naive1 import MostActiveRev, RandomRec, RandomWeightedRec
from plotly.subplots import make_subplots
from thesis import Thesis1

_ = MostActiveRev, RandomRec, RandomWeightedRec
_ = RevRec, ACRec, cHRev, CN, xFinder, RevFinder, Tie, WRC

pd.options.mode.chained_assignment = None

logging.basicConfig(level=logging.WARN)

pio.templates.default = "plotly_white"

MEASURES_ALL = [
    "time",
    "mrr",
    "acc@1",
    "acc@3",
    "acc@5",
    "acc@10",
    "rec@1",
    "rec@3",
    "rec@5",
    "rec@10",
    "prec@1",
    "prec@3",
    "prec@5",
    "prec@10",
    "f1@1",
    "f1@3",
    "f1@5",
    "f1@10",
]

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

assert set(MEASURES).issubset(set(MEASURES_ALL))


INVESTIGATE_RES_2 = False
BATC_LOG_FILE = "/tmp/batcore_logs"


def run_model(model_constructor, model_cls, data_dir):
    start_time = time.time()

    data = MRLoaderData(
        data_dir,
        verbose=False,
        log_file_path=BATC_LOG_FILE,
    )

    dataset = get_gerrit_dataset(
        data, max_file=20, model_cls=model_cls, owner_policy="author_no_na"
    )

    data_iterator = PullLoader(dataset)

    model = model_constructor(dataset)

    tester = RecTester()

    print("running the tester over data iterator")
    res = tester.test_recommender(model, data_iterator)

    execution_time = time.time() - start_time
    res[0]["time"] = (execution_time, 0)

    if INVESTIGATE_RES_2:
        print("res 1 : ")
        pprint(res[1].head())
        res[1].to_csv("/tmp/my_csv.csv")

    return res[0]


def prepare_result(models, dataset_names, datasets_dir):
    res_map = {}
    for dataset_name in dataset_names:
        res_ds_map = {}
        res_map[dataset_name] = res_ds_map
        for model_name, model_cls, model_constructor in models:
            print(f"--------- {dataset_name} -> {model_name} ---------")
            res_ds_map[model_name] = run_model(
                model_constructor,
                model_cls,
                datasets_dir + dataset_name,
            )
            # each item: (score, error)

    print("------- aggregating data ---------")
    data = []
    for ds in dataset_names:
        for model_name, _, _ in models:
            print(f"aggregating {ds=} in {model_name=}")

            result = res_map[ds][model_name]

            ds_model = [model_name, ds]
            ds_model += [result[measure][0] for measure in MEASURES]

            data.append(ds_model)

    df = pd.DataFrame(data, columns=["Model", "Dataset"] + MEASURES)
    return df


def get_color(model_name):
    color_list = [
        "red",
        "blue",
        "green",
        "yellow",
        "purple",
        "orange",
        "pink",
        "cyan",
        "magenta",
        "brown",
    ]
    hash_value = md5(model_name.encode()).hexdigest()
    color_index = int(hash_value, 16) % len(color_list)
    return color_list[color_index]


def plot_df(df):
    fig = make_subplots(rows=1, cols=1)

    for model in df["Model"].unique():
        model_data = df[df["Model"] == model]

        # For each measure/model, create a trace
        for i, measure in enumerate(MEASURES):
            fig.add_trace(
                go.Bar(
                    x=[f"{dataset}<br>{measure}" for dataset in model_data["Dataset"]],
                    y=model_data[measure],
                    name=model,
                    marker_color=get_color(model),
                    opacity=0.7,
                    showlegend=i == 0,  # Only show legend for the first measure
                    legendgroup=model,  # Group traces by model
                )
            )

    fig.update_layout(
        title="Recommender Models Metrics Comparison",
        xaxis_title="Dataset and Measure",
        yaxis_title="Score",
        barmode="group",
        height=600,
        width=1200,
        legend_title="Models",
        font={"size": 12},
        legend={
            "groupclick": "toggleitem"  # This enables filtering when clicking on legend items
        },
        xaxis={"tickangle": -45},  # Rotate x-axis labels for better readability
        yaxis={"range": [0, 1.1]},  # Set y-axis range from 0 to 1
    )

    fig.show()


def coderev_rec(models, dataset_names, dataset_dir):
    df = prepare_result(models, dataset_names, dataset_dir)

    print(df.head())
    plot_df(df)


if __name__ == "__main__":
    df_stats(
        "../data-combined/",
        dataset_names=None,
    )
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
    )
