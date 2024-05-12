import pandas as pd
from batcore.baselines import CN
from batcore.data import MRLoaderData, PullLoader, get_gerrit_dataset
from batcore.tester import RecTester

pd.options.mode.chained_assignment = None

if __name__ == "__main__":

    print("data loading")
    data = MRLoaderData().from_checkpoint("../crawler/data")
    # data = MRLoaderData("../../crawler/data", from_checkpoint=True)

    print("data set loading")
    dataset = get_gerrit_dataset(data, max_file=2, model_cls=CN)
    # adda pull with max files > max files

    print("iterator over data")
    data_iterator = PullLoader(dataset, 1)

    for it in data_iterator:
       print("---->")
       print(it)

    exit()

    print("get item ids")
    dataset.get_items2ids()

    # create a CN model. dataset.get_items2ids() provides model
    # with necessary encodings (eg. users2id, files2id) for
    # optimization of evaluation

    print("model loading")
    model = CN(dataset.get_items2ids())

    # create a tester object
    tester = RecTester()

    # run the tester and receive dict with all the metrics
    res = tester.test_recommender(model, data_iterator)

    print(res[0])
