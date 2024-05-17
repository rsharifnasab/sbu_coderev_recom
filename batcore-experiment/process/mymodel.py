from pprint import pprint

import numpy as np
from batcore.modelbase import RecommenderBase


class SimpleRecommender(RecommenderBase):
    def __init__(self):
        super().__init__()
        self.reviewers = []

    def predict(self, pull, n=10):
        pprint(f"predicting for pull: {pull}")
        return [np.random.choice(self.reviewers)]

    def fit(self, data):
        print("fitting")
        for event in data:
            pprint(f"fitting event: {event}")
            self.reviewers.extend(event['reviewer'])
