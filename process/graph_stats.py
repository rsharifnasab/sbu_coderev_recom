#!/usr/bin/env python3

# from concurrent.futures import ProcessPoolExecutor
# from multiprocessing.pool import Pool
import time
from math import log2
from random import choices as random_choices
from statistics import mean
from typing import Callable

import networkx as nx
from matplotlib import pyplot as plt
from pyvis.network import Network

measure_on = False


def measure_time(f):
    def timed(*args, **kw):
        ts = time.time()
        result = f(*args, **kw)
        te = time.time()
        diff = te - ts
        if measure_on and diff > 10:
            print(f"{f.__name__} {te-ts:.4f}s")
        return result

    return timed


class GraphStats:
    def __init__(self, name: str, G, slow=False, force_undirect=True):
        self.name = name.capitalize()
        if force_undirect:
            G = nx.Graph(G)
        self.G = nx.freeze(G)
        self.is_directed = G.is_directed()

        self.undirected = nx.Graph(self.G)
        self.undirected_giant = self._largest_cc(self.undirected)

        self.slow = slow

    @staticmethod
    def _largest_cc(G):
        largest_component = max(nx.connected_components(G), key=len)
        return G.subgraph(largest_component).copy()

    def node_count(self):
        return len(self.G.nodes)

    def edges_count(self):
        return len(self.G.edges)

    def average_degree(self):
        result = None
        if self.is_directed:
            result = self.edges_count() / self.node_count()
        else:
            result = 2.0 * self.edges_count() / self.node_count()
        return f"{result:.2f}"

    def __density(self):
        n = self.node_count()
        possible_edges = n * (n - 1)
        if self.is_directed:
            return self.edges_count() / possible_edges
        return (2 * self.edges_count()) / possible_edges

    def density(self):
        return f"{self.__density()*100:.4f}%"

    @measure_time
    def diameter(self):
        effective_g = self.undirected_giant
        result = None
        # use exact method
        if self.slow:
            result = nx.algorithms.distance_measures.diameter(effective_g)
        else:
            # use approximation method
            result = nx.algorithms.approximation.distance_measures.diameter(effective_g)
        return f"{result}"

    @measure_time
    def avg_clustering_coeff(self):
        memory_save = 0
        if memory_save:
            result = nx.average_clustering(self.G)
        else:
            clustering = nx.clustering(self.G)
            assert isinstance(clustering, dict)
            total = sum(clustering.values())
            count = len(clustering)
            result = total / count
            if self.is_directed:
                result = total / count * 2
        return f"{result:.4f}"

    def transitivity(self):
        return f"{nx.transitivity(self.G):.4f}"

    @measure_time
    def avg_shortest_path_len(self):
        if self.slow:
            return nx.average_shortest_path_length(self.undirected_giant)

        n_samples = max(self.edges_count() // 20, 10_000)

        nodes = list(self.undirected_giant.nodes())
        lens = []
        for _ in range(n_samples):
            n1, n2 = random_choices(nodes, k=2)
            l = nx.shortest_path_length(self.undirected_giant, source=n1, target=n2)
            lens.append(l)

        result = mean(lens)
        return f"{result:.4f}"

    @measure_time
    def effective_diameter(self):
        n_samples = max(self.edges_count() // 20, 10_000)
        percent = 90

        nodes = list(self.undirected_giant.nodes())
        lens = []
        for _ in range(n_samples):
            n1, n2 = random_choices(nodes, k=2)
            l = nx.shortest_path_length(self.undirected_giant, source=n1, target=n2)
            lens.append(l)

        lens = sorted(lens)
        percentile_idx = (n_samples * percent) // 100
        result = lens[percentile_idx]
        return f"{result:.1f}"

    @measure_time
    def assortativity(self):
        result = nx.degree_pearson_correlation_coefficient(self.G)
        return f"{result:.4f}"

    @staticmethod
    def select_top5(d):
        result = []
        for k, v in sorted(d.items(), key=lambda item: item[1], reverse=True):
            result.append((k, v))
        return result[:5]

    @staticmethod
    def str_top5(d):
        top5 = GraphStats.select_top5(d)
        result = ""
        for k, v in top5:
            result += f"{k}:{v:.5f} "
        return result

    @measure_time
    def closeness_centrality(self):
        result = nx.closeness_centrality(self.undirected)
        return self.str_top5(result)

    @measure_time
    def degree_centrality(self):
        result = nx.degree_centrality(self.undirected)
        return self.str_top5(result)

    @measure_time
    def pagerank_centerality(self):
        result = nx.pagerank(self.undirected, alpha=0.9)
        return self.str_top5(result)

    @measure_time
    def betweenness_centrality(self):
        k_samples = max(self.node_count() // 1000, 50)

        result = nx.betweenness_centrality(self.undirected, k=k_samples)
        return self.str_top5(result)

    def plot_degree_distribution(self, save_dir, loglog_threshold=100):
        degree_freq = nx.degree_histogram(self.undirected)
        degrees = range(len(degree_freq))

        # Filter out zero values
        non_zero_degrees = [d for d in degrees[1:] if degree_freq[d] > 0]
        non_zero_freq = [f for f in degree_freq[1:] if f > 0]

        plt.figure(figsize=(12, 8))

        # Determine if we should use loglog scale
        use_loglog = max(non_zero_degrees) >= loglog_threshold

        if use_loglog:
            plt.loglog(
                non_zero_degrees, non_zero_freq, "bo-", markersize=4, linewidth=1
            )
            plt.xscale("log")
            plt.yscale("log")
            plt.xlabel("دزجه (لگاریتمی)")
            plt.ylabel("فرکانس (لگاریتمی)")
            plt.xticks([1, 10, 100, 1000], ["1", "10", "100", "1000"])
            plt.yticks([1, 10, 100, "1000"])
        else:
            plt.plot(non_zero_degrees, non_zero_freq, "bo-", markersize=4, linewidth=1)
            plt.xlabel("درجه")
            plt.ylabel("فرکانس")

        plt.title(f"توزیع درجه‌ی {self.name}")
        plt.grid(True, which="both", ls="-", alpha=0.2)

        addr = f"{save_dir}/{self.name}.png"
        plt.savefig(addr, dpi=300, bbox_inches="tight")
        plt.close()

        return addr

    def draw_graph(self, save_dir):
        addr = f"{save_dir}/{self.name}"

        pos = nx.spring_layout(self.G, seed=42)
        pos = nx.kamada_kawai_layout(self.G)
        edge_weights = nx.get_edge_attributes(self.G, "value")
        edges, weights = zip(*edge_weights.items())

        # Draw the graph
        plt.figure(figsize=(10, 8))
        nx.draw_networkx_nodes(self.G, pos, node_color="skyblue", node_size=150)

        # Draw the edges with varying widths
        nx.draw_networkx_edges(
            self.G,
            pos,
            edgelist=edges,
            width=[log2(w) for w in weights],
            edge_color="gray",
        )

        plt.axis("off")  # Hide the axis

        # Save the figure as a PDF
        plt.savefig(addr + ".pdf", format="pdf")

        for node in self.G.nodes:
            print(self.G.nodes[node])
            self.G.nodes[node]["label"] = ""

        net = Network(notebook=True, cdn_resources="in_line")
        net.from_nx(self.G)
        for node in net.nodes:
            node["label"] = ""
        net.save_graph(addr + ".html")
        return addr

    def __str__(self):
        return f"{self.name} Graph (nodes: #{self.node_count()})"

    @staticmethod
    def apply(tup):
        result = ""
        for e in tup:
            result += " "
            if isinstance(e, Callable):
                result += str(e())
            else:
                result += str(e)
        return result

    def summary_generator(self, funcs=None, save_path="./"):
        tasks = [
            (f"\n{self.name} Graph N:{self.node_count()} E:{self.edges_count()}",),
            ("avg degree:", self.average_degree),
            ("density:", self.density),
            ("diameter:", self.diameter),
            ("effective diameter:", self.effective_diameter),
            ("avg clustering coeff:", self.avg_clustering_coeff),
            ("transitivity", self.transitivity),
            ("avg shortest path len", self.avg_shortest_path_len),
            ("assortativity", self.assortativity),
            ("betweenness centrality", self.betweenness_centrality),
            ("pagerank centrality", self.pagerank_centerality),
            ("degree centrality", self.degree_centrality),
            ("closeness centrality", self.closeness_centrality),
            (
                "plot degree distribution",
                lambda: self.plot_degree_distribution(save_path),
            ),
            (
                "draw graph",
                lambda: self.draw_graph(save_path),
            ),
        ]

        effective_tasks = [tasks[0]]

        if funcs is None:
            effective_tasks = tasks
        else:
            for task in tasks:
                if task[0] in funcs:
                    effective_tasks.append(task)

        return map(GraphStats.apply, effective_tasks)


def print_summary(summary_generator):
    for i, line in enumerate(summary_generator):
        if i != 0:
            print("| ", end="")
        print(line)
    print()
