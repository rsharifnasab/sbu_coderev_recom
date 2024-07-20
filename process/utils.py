#!/usr/bin/env python3

from collections import Counter, defaultdict

import numpy as np
from graph_stats import GraphStats, print_summary


def graph_demo(g, name):
    ret = GraphStats(
        name=name,
        G=g,
        slow=False,
    ).summary_generator(
        funcs=[
            "avg degree",
            "density",
            "diameter",
            "effective diameter",
            "avg clustering coeff",
            "transitivity",
            "avg shortest path len",
            "assortativity",
            # "betweenness centrality",
            # "pagerank centrality",
            # "degree centrality",
            # "closeness centrality"
            "plot degree distribution",
            "draw graph",
        ],
        save_path=".",
    )
    print_summary(ret)


def sort_by_frequency(input_list, n):
    count = Counter(input_list)
    sorted_items = sorted(count.items(), key=lambda x: (-x[1], x[0]))
    return [item for item, _ in sorted_items[:n]]


def sort_dict_by_value(dictionary, reverse=False):
    return list(
        dict(
            sorted(
                dictionary.items(),
                key=lambda x: x[1],
                reverse=reverse,
            ),
        ).keys()
    )


class LazyWeightedRandomSelector:
    def __init__(self):
        self.item_counts = defaultdict(int)
        self.total_count = 0
        self.items_array = None
        self.probabilities = None
        self.needs_update = True

    def add_item(self, item):
        self.item_counts[item] += 1
        self.total_count += 1
        self.needs_update = True

    def add_items(self, items):
        for item in items:
            self.add_item(item)

    def _update_arrays(self):
        if self.needs_update:
            self.items_array = np.array(list(self.item_counts.keys()))
            counts_array = np.array(list(self.item_counts.values()))
            self.probabilities = counts_array / self.total_count
            self.counter = Counter(self.item_counts)
            self.needs_update = False

    def select_random(self):
        if self.total_count == 0:
            return None
        self._update_arrays()
        assert self.items_array is not None
        return np.random.choice(self.items_array, p=self.probabilities)

    def select_random_set(self, n):
        if self.total_count == 0:
            return []
        if n <= 0:
            return []
        self._update_arrays()
        assert self.items_array is not None
        return np.random.choice(
            self.items_array,
            size=min(n, len(self.items_array)),
            p=self.probabilities,
            replace=False,  # no duplicate
        )

    def get_most_frequent(self, n):
        if self.total_count == 0:
            return []
        if n <= 0:
            return []
        self._update_arrays()
        assert self.items_array is not None
        most_common = self.counter.most_common(n)
        return [item for item, _ in most_common]


class Timestamp:
    def __init__(self, inp_str):
        self.inp_str = inp_str

    def __str__(self):
        return self.inp_str
