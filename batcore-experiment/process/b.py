from batcore.baselines import CN
from batcore.data import MRLoaderData, PullLoader, get_gerrit_dataset
from batcore.tester import RecTester

# reloads saved data from the checkpoint
data = MRLoaderData(
   path= "/home/roozbeh/Desktop/batcore-data-email/dataset/gerrit-review.googlesource.com",
)

data.to_checkpoint("~/Desktop/batcore-data-email/my-attemp/my-checkpoint/")

exit()

# gets dataset for the CN model. Pull request with more than 56 files are removed
dataset = get_gerrit_dataset(data, max_file=56, model_cls=CN)

# creates an iterator over dataset that iterates over pull request one-by-one
data_iterator = PullLoader(dataset, 10)

# creates a CN model. dataset.get_items2ids() provides model with necessary encodings
# (eg. users2id, files2id) for optimization of evaluation
model = CN(dataset.get_items2ids())

# create a tester object
tester = RecTester()

# run the tester and receive dict with all the metrics
res = tester.test_recommender(model, data_iterator)

print(res)
