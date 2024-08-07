from typing import Dict, Optional

import networkx as nx
from batcore.modelbase import RecommenderBase
from lib.utils import (
    LazyWeightedRandomSelector,
    Timestamp,
    graph_demo,
    sort_dict_by_value,
)
from networkx import Graph

PRINT_GRAPH = False

last_graph: Dict[str, Optional[Graph]] = {
    "thesis_extend": None,
    "thesis_noextend": None,
    "thesis_2_extend": None,
    "thesis_2_noextend": None,
}


class Thesis1(RecommenderBase):
    """
    Method 1:
    + create undirected weighted reviewer-author graph
    + create reviewer-list based on neighbors with highest weight
    + if the list is not long enough, extend with with most_frequent
    """

    def __init__(self, should_extend):
        super().__init__()
        self.reviewers = LazyWeightedRandomSelector()
        self.G = nx.Graph()
        self.should_extend = should_extend

    def predict(self, pull, n=10):  # pyright: ignore [reportIncompatibleMethodOverride]
        owner = list(pull["owner"])[0]
        connected_edges = list(self.G.edges(owner, data=True))

        connected_people = {}
        for edge in connected_edges:
            src, dest, data = edge
            connected_people[src] = data["value"]
            connected_people[dest] = data["value"]

        if connected_edges:
            del connected_people[owner]

        ans = sort_dict_by_value(connected_people, reverse=True)[:n]

        if self.should_extend:
            if len(connected_edges) < n:
                ans.extend(self.reviewers.get_most_frequent(n - len(ans)))

        if self.should_extend:
            last_graph["thesis_extend"] = self.G
        else:
            last_graph["thesis_noextend"] = self.G

        if PRINT_GRAPH:
            graph_demo(self.G, "author-reviewer")

        return ans

    def author_reviewer_connect(self, author, reviewer):
        if self.G.has_edge(author, reviewer):
            current_value = self.G.edges[author, reviewer]["value"]
            self.G.edges[author, reviewer]["value"] = current_value + 1
        else:
            self.G.add_edge(author, reviewer, value=1)

    def fit(self, data):
        for event in data:
            authors = event["author"]
            owners = event["owner"]
            _ = owners

            reviewers = event["reviewer"]

            self.reviewers.add_items(reviewers)
            for rev in reviewers:
                for aut in authors:
                    self.author_reviewer_connect(aut, rev)


###########################


class Thesis2(RecommenderBase):
    """
    Method 2:
    + Directed graph
    """

    def __init__(self, should_extend):
        super().__init__()
        self.reviewers = LazyWeightedRandomSelector()
        self.G = nx.DiGraph()
        self.should_extend = should_extend

    def predict(self, pull, n=10):  # pyright: ignore [reportIncompatibleMethodOverride]
        owner = list(pull["owner"])[0]
        connected_edges = list(self.G.edges(owner, data=True))

        connected_people = {}
        for edge in connected_edges:
            src, dest, data = edge
            connected_people[src] = data["value"]
            connected_people[dest] = data["value"]

        if connected_edges:
            del connected_people[owner]

        ans = sort_dict_by_value(connected_people, reverse=True)[:n]

        if self.should_extend:
            if len(connected_edges) < n:
                ans.extend(self.reviewers.get_most_frequent(n - len(ans)))

        if self.should_extend:
            last_graph["thesis_2_extend"] = self.G
        else:
            last_graph["thesis_2_noextend"] = self.G

        if PRINT_GRAPH:
            graph_demo(self.G, "author-reviewer")

        return ans

    def author_reviewer_connect(self, author, reviewer):
        if self.G.has_edge(author, reviewer):
            current_value = self.G.edges[author, reviewer]["value"]
            self.G.edges[author, reviewer]["value"] = current_value + 1
        else:
            self.G.add_edge(author, reviewer, value=1)

    def fit(self, data):
        for event in data:
            authors = event["author"]
            owners = event["owner"]
            _ = owners

            reviewers = event["reviewer"]

            self.reviewers.add_items(reviewers)
            for rev in reviewers:
                for aut in authors:
                    self.author_reviewer_connect(aut, rev)


_ = {
    "author": {"Thomas Draebing:thomas.draebing@sap.com:"},
    "closed": Timestamp("2023-08-16 13:25:15"),
    "date": Timestamp("2023-08-16 13:09:33"),
    "file": [
        "k8s-gerrit/container-images/gerrit-base/Dockerfile",
        "k8s-gerrit/helm-charts/gerrit/values.yaml",
        "k8s-gerrit/helm-charts/gerrit-replica/README.md",
        "k8s-gerrit/tests/helm-charts/gerrit/test_chart_gerrit_plugins.py",
        "k8s-gerrit/helm-charts/gerrit-operator-crds/templates/gerritclusters.gerritoperator.google.com-v1.yml",
        "k8s-gerrit/container-images/gerrit-init/README.md",
        "k8s-gerrit/operator/src/main/java/com/google/gerrit/k8s/operator/cluster/model/GerritCluster.java",
        "k8s-gerrit/helm-charts/gerrit/README.md",
        "k8s-gerrit/helm-charts/gerrit-replica/values.yaml",
        "k8s-gerrit/operator/src/main/java/com/google/gerrit/k8s/operator/gerrit/dependent/GerritInitConfigMap.java",
        "k8s-gerrit/operator/src/main/java/com/google/gerrit/k8s/operator/gerrit/model/GerritInitConfig.java",
        "k8s-gerrit/container-images/gerrit-init/tools/gerrit-initializer/initializer/tasks/download_plugins.py",
        "k8s-gerrit/container-images/gerrit-init/tools/gerrit-initializer/initializer/config/init_config.py",
        "k8s-gerrit/operator/src/main/java/com/google/gerrit/k8s/operator/gerrit/model/Gerrit.java",
        "k8s-gerrit/operator/src/main/java/com/google/gerrit/k8s/operator/gerrit/model/GerritPlugin.java",
        "k8s-gerrit/helm-charts/gerrit-operator-crds/templates/gerrits.gerritoperator.google.com-v1.yml",
        "k8s-gerrit/helm-charts/gerrit-replica/templates/gerrit-replica.configmap.yaml",
        "k8s-gerrit/helm-charts/gerrit/templates/gerrit.configmap.yaml",
        "k8s-gerrit/operator/src/main/java/com/google/gerrit/k8s/operator/gerrit/model/GerritTemplateSpec.java",
        "k8s-gerrit/operator/src/main/java/com/google/gerrit/k8s/operator/gerrit/model/GerritModule.java",
    ],
    "key_change": "k8s-gerrit-382997",
    "owner": {"Thomas Draebing:thomas.draebing@sap.com:"},
    "reviewer": [
        "Matthias Sohn:matthias.sohn@gmail.com:",
        "Saša Živkov:sasa.zivkov@sap.com:",
        "Wendy Wen Wang:wendy.wang10@sap.com:",
    ],
    "status": "NEW",
    "title": "Allow to install lib modules\\n\\nChange-Id: "
    "I1a44607dbd5f442afd4da61e2720a1eda20ca6fb\\n",
    "type": "pull",
}
