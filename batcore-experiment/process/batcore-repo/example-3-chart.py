import logging

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
    "kebyr": "../../crawler/data",
    "another_ds": "../../crawler/data_51",
}

MEASURES = ["accuracy", "precision", "recall", "top_k"]


def result_for_model(model_cls, data_dir):
    data = MRLoaderData(
        verbose=True,
        log_stdout=True,
    ).from_checkpoint(data_dir)

    print("data set loading")
    dataset = get_gerrit_dataset(data, max_file=5600, model_cls=model_cls)

    print("iterator over data")
    data_iterator = PullLoader(dataset, 2)

    print("get item ids")
    print(dataset.get_items2ids())

    print("model loading")
    model = model_cls(dataset.get_items2ids())

    print("creating a rec tester object")
    tester = RecTester()

    print("running the tester over data iterator")
    res = tester.test_recommender(model, data_iterator)
    return res


def main(clazzez, dataset_names):

    models = [cls.__name__ for cls in clazzez]

    res_map = {}
    for dataset_name in dataset_names:
        dataset_dir = DATASET_DIRS[dataset_name]
        res_ds_map = {}
        res_map[dataset_name] = res_ds_map
        for model in models:
            res_ds_map[model] = {measure: np.random.rand()
                                 for measure in MEASURES}
            # result_for_model(cls, DATASET_DIRS[dataset_name])

    print(res_map)

    # Create a DataFrame for the data
    data = []
    for ds in dataset_names:
        for model in models:
            data.append(
                [model, ds] + [res_map[ds][model][measure]
                               for measure in MEASURES]
            )

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
    main([CN, RevRec, ACRec, cHRev, Tie, WRC], ["kebyr", "another_ds"])
