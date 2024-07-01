from pprint import pprint

import numpy as np
from batcore.modelbase import RecommenderBase


class Naive(RecommenderBase):
    def __init__(self):
        super().__init__()
        self.reviewers = []

    def predict(self, pull, n=10):
        # pprint(f"predicting for pull: {pull}")
        # print(f"len(reviewres): {len(self.reviewers)}")
        return [self.reviewers[0] for _ in range(n)]
        return [np.random.choice(self.reviewers) for _ in range(n)]

    def fit(self, data):
        # print("fitting")
        for event in data:
            # pprint(f"fitting event: {event}")
            self.reviewers.extend(event["reviewer"])
