from batcore.baselines import CN
from batcore.data import MRLoaderData, PullLoader, get_gerrit_dataset
from batcore.tester import RecTester

print("mr loader data initiating")
# reloads saved data from the checkpoint
data = MRLoaderData(
    path="/home/roozbeh/Desktop/batcore-data-email/dataset/gerrit-review.googlesource.com",
)

print("get gerrit dataset")

# gets dataset for the CN model. Pull request with more than 56 files are removed
dataset = get_gerrit_dataset(
    data, max_file=56, model_cls=CN, owner_policy="author_no_na"
)

print("create pull loader instance")
# creates an iterator over dataset that iterates over pull request one-by-one
data_iterator = PullLoader(dataset, 10)

print("create CN model")
# creates a CN model. dataset.get_items2ids() provides model with necessary encodings
# (eg. users2id, files2id) for optimization of evaluation
model = CN(dataset.get_items2ids())

print("create RecTester")
# create a tester object
tester = RecTester()

print("test recommender")
# run the tester and receive dict with all the metrics
res = tester.test_recommender(model, data_iterator)

print(res)
