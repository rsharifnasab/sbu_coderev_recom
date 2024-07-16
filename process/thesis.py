from pprint import pprint

import networkx as nx
from batcore.modelbase import RecommenderBase
from utils import Timestamp, graph_demo, sort_by_frequency, sort_dict_by_value

PRINT_GRAPH = False


class Thesis1(RecommenderBase):
    def __init__(self):
        super().__init__()
        self.reviewers = []
        self.G = nx.Graph()

    def predict(self, pull, n=10):  # pyright: ignore [reportIncompatibleMethodOverride]
        owner = list(pull["owner"])[0]
        connected_edges = list(self.G.edges(owner, data=True))

        connected_people = {}
        for edge in connected_edges:
            src, dest, data = edge
            print("data : ")
            pprint(data)
            connected_people[src] = data["value"]
            connected_people[dest] = data["value"]

        if connected_edges:
            del connected_people[owner]

        ans = sort_dict_by_value(connected_people, reverse=True)[:n]

        if len(connected_edges) < n:
            ans.extend(
                sort_by_frequency(
                    self.reviewers,
                    n - len(ans),
                )
            )

        if PRINT_GRAPH:
            graph_demo(self.G)

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

            self.reviewers.extend(reviewers)
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
