#!/usr/bin/env python3

from collections import Counter


def sort_by_frequency(input_list, n):
    count = Counter(input_list)
    sorted_items = sorted(count.items(), key=lambda x: (-x[1], x[0]))
    return [item for item, _ in sorted_items[:n]]


class Timestamp:
    def __init__(self, inp_str):
        self.inp_str = inp_str

    def __str__(self):
        return self.inp_str
