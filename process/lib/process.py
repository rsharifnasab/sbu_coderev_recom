import logging
import time
from hashlib import md5
from pprint import pprint

import numpy as np
import pandas as pd
import plotly.express as px
import plotly.graph_objects as go
import plotly.io as pio
from batcore.data import MRLoaderData, PullLoader, get_gerrit_dataset
from batcore.tester import RecTester
from lib.utils import graph_demo
from plotly.subplots import make_subplots
from thesis import last_graph

pd.options.mode.chained_assignment = None

logging.basicConfig(level=logging.WARN)

pio.templates.default = "plotly_white"
pio.kaleido.scope.mathjax = None

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


def prepare_result(models, dataset_names, datasets_dir, measures):
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
            ds_model += [result[measure][0] for measure in measures]

            data.append(ds_model)

    df = pd.DataFrame(data, columns=["Model", "Dataset"] + measures)
    return df


def get_color(model_name):
    color_list = px.colors.qualitative.Plotly
    hash_value = md5((model_name + "random str").encode()).hexdigest()
    color_index = int(hash_value, 16) % len(color_list)
    return color_list[color_index]


def plot_measure(df, measure_list, ds_name):
    fig = make_subplots(rows=1, cols=1)

    for model in df["Model"].unique():
        model_data = df[df["Model"] == model]

        # For each measure/model, create a trace
        for i, measure in enumerate(measure_list):
            fig.add_trace(
                go.Bar(
                    x=[
                        f"{dataset or ''}<br>{measure}"
                        for dataset in model_data["Dataset"]
                    ],
                    y=model_data[measure],
                    name=model,
                    marker_color=get_color(model),
                    opacity=0.7,
                    showlegend=i == 0,  # Only show legend for the first measure
                    legendgroup=model,  # Group traces by model
                )
            )

    fig.update_layout(
        title="",
        xaxis_title="مجموعه‌داده و معیار اندازه‌گیری",
        yaxis_title="امتیاز",
        barmode="group",
        height=600,
        width=1200,
        legend_title="مدل",
        font={"size": 12},
        legend={
            "groupclick": "toggleitem"  # This enables filtering when clicking on legend items
        },
        xaxis={"tickangle": -45},  # Rotate x-axis labels for better readability
        yaxis={"range": [0, 1.1]},  # Set y-axis range from 0 to 1
    )

    measure_str = "-".join(measure_list)
    pio.write_image(fig, f"result-{ds_name}-{measure_str}.pdf")
    # fig.show()


def plot_df(df, all_measures, seperate_graphs):
    if seperate_graphs:
        meta_measures = []
        for measure in all_measures:
            meta_measures.append([measure])
    else:
        meta_measures = [all_measures]

    for measure_list in meta_measures:
        plot_measure(df, measure_list, "ds")


def graph_stats():
    for k, v in last_graph.items():
        if not v:
            continue

        graph_demo(v, k)


_ = """
    -----------------------
            Model  Dataset       mrr     acc@3     rec@3    prec@3      f1@3     acc@5     rec@5    prec@5      f1@5    acc@10    rec@10   prec@10     f1@10
3   thesis_extend  gitiles  0.795375  0.906399  0.669303  0.484877  0.533999  0.944604  0.778433  0.354760  0.465605  0.966571  0.846247  0.208897  0.322772
7   thesis_extend    zoekt  0.824217  0.905149  0.844851  0.341012  0.474306  0.945799  0.891373  0.230623  0.359552  0.970190  0.934282  0.127430  0.220365
11  thesis_extend   gwtorm  0.869590  0.938182  0.766788  0.667879  0.691598  0.978182  0.877922  0.600424  0.675197  0.992727  0.945524  0.509548  0.597860
"""

_ = """
             Model       mrr     acc@3     rec@3    prec@3      f1@3     acc@5     rec@5    prec@5      f1@5    acc@10    rec@10   prec@10     f1@10
0            chrev  0.511677  0.571118  0.439716  0.418616  0.403207  0.582792  0.466911  0.414395  0.408644  0.582792  0.468719  0.414078  0.408940
1            acrec  0.647862  0.720571  0.565273  0.406668  0.438842  0.742614  0.610560  0.371075  0.420108  0.745158  0.619918  0.359305  0.409462
2  thesis_noextend  0.789086  0.850898  0.712974  0.495355  0.551288  0.879342  0.786473  0.416242  0.503290  0.892651  0.828787  0.347198  0.431893
3    thesis_extend  0.832115  0.918107  0.759473  0.497662  0.566399  0.955877  0.848685  0.394796  0.499667  0.976496  0.909412  0.282296  0.380790
"""


def combine_df(df):
    models = df["Model"].unique()
    result = pd.DataFrame()

    for model in models:
        model_data = df[df["Model"] == model]
        means = model_data.select_dtypes(include=[np.number]).mean()
        row = pd.Series({"Model": model, "Dataset": None, **means})
        result = result.append(row, ignore_index=True)
    return result


def plot_combined_df(df, measures, seperate_graphs):
    result = combine_df(df)

    if seperate_graphs:
        meta_measures = []
        for measure in measures:
            meta_measures.append([measure])
    else:
        meta_measures = [measures]

    for measure_list in meta_measures:
        plot_measure(result, measure_list, "combined")


################


def create_incremental_plot(df, measure, k_values):
    fig = go.Figure()

    for model in df["Model"]:
        y_values = [
            df[df["Model"] == model][f"{measure}@{k}"].values[0] for k in k_values
        ]
        fig.add_trace(
            go.Scatter(
                x=[f"@{k}" for k in k_values],
                y=y_values,
                mode="lines+markers",
                name=model,
            )
        )

    fig.update_layout(
        # title=f"مقایسه‌ی {measure.capitalize()} در مدل‌های مختلف",
        # xaxis_title=f"{measure.capitalize()}@k",
        yaxis_title=f"{measure.capitalize()}",
        legend=dict(orientation="h", yanchor="bottom", y=1.02, xanchor="right", x=1),
    )

    return fig


# Example usage
k_values = [3, 5, 10]


def plot_incremental_df(raw_df, measures, inc_measures):
    df = combine_df(raw_df)
    for measure in inc_measures:
        fig = create_incremental_plot(df, measure, k_values)
        fig.show()
        pio.write_image(fig, f"result-inc-{measure}.pdf")


def coderev_rec(
    models, dataset_names, dataset_dir, measures, inc_measures, seperate_graphs=False
):
    assert set(measures).issubset(set(MEASURES_ALL))
    df = prepare_result(models, dataset_names, dataset_dir, measures)

    print(df.head())
    print("-" * 20)
    plot_df(df, measures, seperate_graphs)
    print("-" * 20)
    plot_combined_df(df, measures, seperate_graphs)

    print("-" * 20)
    plot_incremental_df(df, measures, inc_measures)

    graph_stats()

    return df
