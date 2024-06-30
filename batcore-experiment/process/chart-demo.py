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

MEASURES = [
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


INVESTIGATE_RES_2 = False


def result_for_model(model_constructor, model_cls, data_dir):
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


def main(models, dataset_names):
    res_map = {}
    for dataset_name in dataset_names:
        dataset_dir = DATASET_DIRS[dataset_name]
        res_ds_map = {}
        res_map[dataset_name] = res_ds_map
        for model_name, model_cls, model_constructor in models:
            # res_ds_map[model] = {measure: np.random.rand() for measure in MEASURES}
            res_ds_map[model_name] = result_for_model(
                model_constructor, model_cls, dataset_dir
            )
            # each item: (score, error)

    pprint(res_map)

    # Create a DataFrame for the data
    data = []
    for ds in dataset_names:
        for model_name, _, _ in models:
            print(f"aggregating {ds=} in {model_name=}")

            result = res_map[ds][model_name]

            ds_model = [model_name, ds]
            ds_model += [result[measure][0] for measure in MEASURES]

            data.append(ds_model)

    pprint(data)
    print("----------")
    df = pd.DataFrame(data, columns=["Model", "Dataset"] + MEASURES)

    print(df)

    for measure in MEASURES:
        # Create the bar chart using Seaborn
        plt.figure(figsize=(10, 6))
        sns.barplot(x="Dataset", y=measure, hue="Model", data=df)
        plt.title("Recommender Models Metrics - " + measure)
        plt.xlabel("Datasets")
        plt.ylabel(measure)
        plt.legend(title="Models")
        plt.show()


if __name__ == "__main__":
    main(
        [
            ("chrev", cHRev, lambda ds: cHRev()),
            #     ("cn", CN, lambda ds: CN(ds.get_items2ids())),
        ],
        ["aws", "batzel"],
    )
