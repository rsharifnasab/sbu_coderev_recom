#!/usr/bin/env python3

from collections import Counter

from graph_stats import GraphStats, print_summary


def graph_demo(g):
    ret = GraphStats(
        name="author-reviewer",
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


class Timestamp:
    def __init__(self, inp_str):
        self.inp_str = inp_str

    def __str__(self):
        return self.inp_str
