import random

import numpy as np
from batcore.modelbase import RecommenderBase
from utils import LazyWeightedRandomSelector, sort_by_frequency


class MostActiveRev(RecommenderBase):
    def __init__(self):
        super().__init__()
        self.reviewers = LazyWeightedRandomSelector()

    def predict(self, pull, n=10):  # pyright: ignore [reportIncompatibleMethodOverride]
        return self.reviewers.get_most_frequent(n)

    def fit(self, data):
        for event in data:
            self.reviewers.add_items(event["reviewer"])


class RandomWeightedRec(RecommenderBase):
    def __init__(self):
        super().__init__()
        self.reviewers = LazyWeightedRandomSelector()

    def predict(self, pull, n=10):  # pyright: ignore [reportIncompatibleMethodOverride]
        _ = pull
        return self.reviewers.select_random_set(n)

    def fit(self, data):
        for event in data:
            self.reviewers.add_items(event["reviewer"])


class RandomRec(RecommenderBase):
    def __init__(self):
        super().__init__()
        self.reviewers: set[str] = set()

    def predict(self, pull, n=10):  # pyright: ignore [reportIncompatibleMethodOverride]
        return random.sample(self.reviewers, n)

    def fit(self, data):
        for event in data:
            self.reviewers.update(event["reviewer"])
