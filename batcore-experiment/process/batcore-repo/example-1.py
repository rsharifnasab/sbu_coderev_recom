import pandas as pd
from tqdm.utils import RE_ANSI

from batcore.baselines import CN, RevRec
from batcore.data import MRLoaderData, PullLoader, get_gerrit_dataset
from batcore.tester import RecTester

pd.options.mode.chained_assignment = None

if __name__ == "__main__":

    print("data loading")
    data = MRLoaderData().from_checkpoint("../../crawler/data")

    print("data set loading")
    dataset = get_gerrit_dataset(data, max_file=5600, model_cls=RevRec)

    print("iterator over data")
    data_iterator = PullLoader(dataset, 10)

    print("get item ids")
    dataset.get_items2ids()

    # create a CN model. dataset.get_items2ids() provides model
    # with necessary encodings (eg. users2id, files2id) for
    # optimization of evaluation

    print("model loading")
    model = RevRec(dataset.get_items2ids())

    # create a tester object
    tester = RecTester()

    # run the tester and receive dict with all the metrics
    res = tester.test_recommender(model, data_iterator)

    print(res[0])
