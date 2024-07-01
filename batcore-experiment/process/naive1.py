from collections import Counter
from pprint import pprint

import numpy as np
from batcore.modelbase import RecommenderBase


def sort_by_frequency(input_list, n):
    count = Counter(input_list)
    sorted_items = sorted(count.items(), key=lambda x: (-x[1], x[0]))
    return [item for item, _ in sorted_items[:n]]


class MostActiveRev(RecommenderBase):
    def __init__(self):
        super().__init__()
        self.reviewers = []

    def predict(self, pull, n=10):
        return sort_by_frequency(self.reviewers, n)

    def fit(self, data):
        for event in data:
            self.reviewers.extend(event["reviewer"])


class RandomWeightedRec(RecommenderBase):
    def __init__(self):
        super().__init__()
        self.reviewers = []

    def predict(self, pull, n=10):
        return [np.random.choice(self.reviewers) for _ in range(n)]

    def fit(self, data):
        for event in data:
            self.reviewers.extend(event["reviewer"])


class RandomRec(RecommenderBase):
    def __init__(self):
        super().__init__()
        self.reviewers = set()

    def predict(self, pull, n=10):
        reviewer_list = list(self.reviewers)
        return [np.random.choice(reviewer_list) for _ in range(n)]

    def fit(self, data):
        for event in data:
            self.reviewers.update(event["reviewer"])
