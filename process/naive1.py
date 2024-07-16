import numpy as np
from batcore.modelbase import RecommenderBase
from utils import sort_by_frequency


class MostActiveRev(RecommenderBase):
    def __init__(self):
        super().__init__()
        self.reviewers = []

    def predict(self, pull, n=10):  # pyright: ignore [reportIncompatibleMethodOverride]
        return sort_by_frequency(self.reviewers, n)

    def fit(self, data):
        for event in data:
            self.reviewers.extend(event["reviewer"])


class RandomWeightedRec(RecommenderBase):
    def __init__(self):
        super().__init__()
        self.reviewers = []

    def predict(self, pull, n=10):  # pyright: ignore [reportIncompatibleMethodOverride]
        return list(set(np.random.choice(self.reviewers) for _ in range(n)))

    def fit(self, data):
        for event in data:
            self.reviewers.extend(event["reviewer"])


class RandomRec(RecommenderBase):
    def __init__(self):
        super().__init__()
        self.reviewers = set()

    def predict(self, pull, n=10):  # pyright: ignore [reportIncompatibleMethodOverride]
        reviewer_list = list(self.reviewers)
        return list(set(np.random.choice(reviewer_list) for _ in range(n)))

    def fit(self, data):
        for event in data:
            self.reviewers.update(event["reviewer"])
