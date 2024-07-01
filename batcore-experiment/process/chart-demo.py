import logging
from pprint import pprint

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns
from batcore.baselines import CN, WRC, ACRec, RevRec, Tie, cHRev
from batcore.data import MRLoaderData, PullLoader, get_gerrit_dataset
from batcore.tester import RecTester

pd.options.mode.chained_assignment = None


logging.basicConfig(level=logging.INFO)

PRINT_DATA = False

DATASET_DIRS = {
    "aws": "../data",
    "batzel": "../data2",
}

MEASURES_ALL = [
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
    # "top_k",
]

MEASURES = [
    "acc@5",
    "rec@5",
    "prec@5",
    "f1@10",
]


INVESTIGATE_RES_2 = False


def run_model(model_constructor, model_cls, data_dir):
    data = MRLoaderData(
        data_dir,
        verbose=True,
        log_stdout=True,
    )  # .from_checkpoint(data_dir)

    print("data set loading")
    dataset = get_gerrit_dataset(
        data, max_file=20, model_cls=model_cls, owner_policy="author_no_na"
    )

    print("iterator over data")
    data_iterator = PullLoader(dataset)

    print("model loading")
    model = model_constructor(dataset)

    print("creating a rec tester object")
    tester = RecTester()

    print("running the tester over data iterator")
    res = tester.test_recommender(model, data_iterator)

    # print("res 0 : ")
    # pprint(res[0])

    if INVESTIGATE_RES_2:
        print("res 1 : ")
        pprint(res[1].head())
        res[1].to_csv("/tmp/my_csv.csv")

    return res[0]


def prepare_data(models, dataset_names):
    res_map = {}
    for dataset_name in dataset_names:
        dataset_dir = DATASET_DIRS[dataset_name]
        res_ds_map = {}
        res_map[dataset_name] = res_ds_map
        for model_name, model_cls, model_constructor in models:
            res_ds_map[model_name] = run_model(
                model_constructor, model_cls, dataset_dir
            )
            # each item: (score, error)

    # pprint(res_map)

    # Create a DataFrame for the data
    data = []
    for ds in dataset_names:
        for model_name, _, _ in models:
            print(f"aggregating {ds=} in {model_name=}")

            result = res_map[ds][model_name]

            ds_model = [model_name, ds]
            ds_model += [result[measure][0] for measure in MEASURES]

            data.append(ds_model)

    # pprint(data)

    df = pd.DataFrame(data, columns=["Model", "Dataset"] + MEASURES)
    # print(df.head())

    return df


def plot_df_1(df):
    for measure in MEASURES:
        # Create the bar chart using Seaborn
        plt.figure(figsize=(10, 6))
        sns.barplot(x="Dataset", y=measure, hue="Model", data=df)
        plt.title("Recommender Models Metrics - " + measure)
        plt.xlabel("Datasets")
        plt.ylabel(measure)
        plt.legend(title="Models")
        plt.show()


import plotly.express as px
import plotly.io as pio

# Set the default template for better aesthetics
pio.templates.default = "plotly_white"


def plot_df_2(df):
    for measure in MEASURES:
        fig = px.bar(
            df,
            x="Dataset",
            y=measure,
            color="Model",
            barmode="group",
            title=f"Recommender Models Metrics - {measure}",
            labels={"Dataset": "Datasets", measure: measure},
            height=600,
            width=1000,
        )

        # Customize the layout
        fig.update_layout(
            legend_title_text="Models",
            xaxis_title="Datasets",
            yaxis_title=measure,
            font=dict(size=14),
            title_font=dict(size=20),
            legend=dict(font=dict(size=12)),
            hoverlabel=dict(font_size=14),
        )

        # Show the plot
        fig.show()


def plot_df(df):
    import plotly.graph_objects as go
    from plotly.subplots import make_subplots

    # Assuming df is your DataFrame and MEASURES is your list of measures
    # Create a subplot with a single plot
    fig = make_subplots(rows=1, cols=1)

    # Define a color map for the models
    color_map = {
        "chrev": "blue",
        "chrev2": "red",
        "Model3": "green",
        "Model4": "orange",
    }

    # Iterate through each model
    for model in df["Model"].unique():
        model_data = df[df["Model"] == model]

        # For each measure, create a trace
        for i, measure in enumerate(MEASURES):
            fig.add_trace(
                go.Bar(
                    x=[f"{dataset}<br>{measure}" for dataset in model_data["Dataset"]],
                    y=model_data[measure],
                    name=model,  # f"{model} - {measure}",
                    marker_color=color_map[model],
                    opacity=0.6 + 0.1 * i,  # Varying opacity for different measures
                    showlegend=i == 0,  # Only show legend for the first measure
                )
            )

    # Update layout
    fig.update_layout(
        title="Recommender Models Metrics Comparison",
        xaxis_title="Dataset and Measure",
        yaxis_title="Score",
        barmode="group",
        height=600,
        width=1200,
        legend_title="Models",
        font=dict(size=12),
    )

    # Show the plot
    fig.show()


def main(models, dataset_names):
    df = prepare_data(models, dataset_names)

    print(df.head())
    plot_df(df)


if __name__ == "__main__":
    main(
        [
            ("chrev", cHRev, lambda ds: cHRev()),
            ("chrev2", cHRev, lambda ds: cHRev()),
            #     ("cn", CN, lambda ds: CN(ds.get_items2ids())),
        ],
        ["aws", "batzel"],
    )
