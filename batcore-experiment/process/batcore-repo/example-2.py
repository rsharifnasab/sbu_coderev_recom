import logging

import pandas as pd
from numpy import fromstring

from batcore.baselines import CN, RevRec
from batcore.data import (
    MRLoaderData,
    PullLoader,
    RevRec,
    RevRecDataset,
    SpecialDatasets,
    StandardDataset,
    get_gerrit_dataset,
)
from batcore.tester import RecTester

pd.options.mode.chained_assignment = None


logging.basicConfig(level=logging.DEBUG)

if __name__ == "__main__":

    data = MRLoaderData(
        verbose=True,
        log_stdout=True,
    ).from_checkpoint("../../crawler/data")

    print("data set loading")
    dataset = get_gerrit_dataset(data, max_file=5600, model_cls=CN)
    print(f"{dataset.data=}")

    print("iterator over data")
    data_iterator = PullLoader(dataset, 2)
    # print(f"{data_iterator.data}")

    if False:
        print("data iterator content:")
        for k, v in data_iterator:
            print(k)
            print("---")
            print(v)
            print("-----------------------")
        exit(0)

    print("get item ids")
    print(dataset.get_items2ids())

    # create a CN model. dataset.get_items2ids() provides model
    # with necessary encodings (eg. users2id, files2id) for
    # optimization of evaluation

    print("model loading")
    model = CN(dataset.get_items2ids())

    print("creating a rec tester object")
    tester = RecTester()

    print("running the tester over data iterator")
    res = tester.test_recommender(model, data_iterator)

    print(res[0])
