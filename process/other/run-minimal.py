import pandas as pd
from batcore.baselines import cHRev
from batcore.data import MRLoaderData, PullLoader, get_gerrit_dataset
from batcore.tester import RecTester

# from .mymodel import SimpleRecommender

pd.options.mode.chained_assignment = None

if __name__ == "__main__":
    print("data loading")
    data = MRLoaderData(path="../data/")
    # data = MRLoaderData().from_checkpoint("../crawler/data")

    print("data set loading")
    dataset = get_gerrit_dataset(
        data, max_file=20, model_cls=cHRev, owner_policy="author_no_na"
    )
    # adda pull with max files > max files

    print("iterator over data")
    data_iterator = PullLoader(dataset)

    show_iter = False
    if show_iter:
        for data in data_iterator:
            # print(data)
            from pprint import pprint

            print(pprint(data, indent=4))

        exit()

    # print("get item ids")
    # dataset.get_items2ids()

    # create a CN model. dataset.get_items2ids() provides model
    # with necessary encodings (eg. users2id, files2id) for
    # optimization of evaluation

    print("model loading")
    model = cHRev()  # dataset.get_items2ids())

    print("rec tester instance")
    # create a tester object
    tester = RecTester()

    print("rec tester: test")
    # run the tester and receive dict with all the metrics
    res = tester.test_recommender(model, data_iterator)

    from pprint import pprint

    print("result:")
    pprint(res)
