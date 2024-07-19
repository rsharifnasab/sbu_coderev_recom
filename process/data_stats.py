from os import listdir, path

import plotly.express as px
import plotly.graph_objects as go
import plotly.io as pio
from batcore.data import (
    CN,
    WRC,
    ACRec,
    MRLoaderData,
    PullLoader,
    RevFinder,
    RevRec,
    Tie,
    cHRev,
    get_gerrit_dataset,
    xFinder,
)
from naive1 import MostActiveRev, RandomRec, RandomWeightedRec
from plotly.subplots import make_subplots
from thesis import Thesis1
from tqdm import tqdm

_ = MostActiveRev, RandomRec, RandomWeightedRec
_ = RevRec, ACRec, cHRev, CN, xFinder, RevFinder, Tie, WRC


BATC_LOG_FILE = "/tmp/batcore_logs_ds"


def single_ds_stat(model_cls, data_dir):
    data = MRLoaderData(
        data_dir,
        verbose=False,
        log_file_path=BATC_LOG_FILE,
    )

    dataset = get_gerrit_dataset(
        data, max_file=20, model_cls=model_cls, owner_policy="author_no_na"
    )

    data_iterator = PullLoader(dataset)

    authors = set()
    files = set()
    reviewers = set()
    dates = set()
    event_count = 0

    for datas in data_iterator:
        for datas2 in datas:
            if not isinstance(datas2, list):
                datas2 = [datas2]
                for data in datas2:
                    authors.update(data["author"])

                    files.update(data["file"])

                    reviewers.update(data["reviewer"])

                    dates.add(data["date"])

                    event_count += 1

    # TODO
    parsed_dates = [timestamp for timestamp in dates]

    res = {
        "data_dir": data_dir,
        "authors": len(authors),
        "files": len(files),
        "reviewers": len(reviewers),
        "event_count": event_count,
        "min_date": min(parsed_dates),
        "max_date": max(parsed_dates),
        # "dates": parsed_dates,
    }

    return res


def df_stats(dataset_dir, dataset_names=None):
    res = {}
    if not dataset_names:
        dataset_names = [
            d for d in listdir(dataset_dir) if path.isdir(path.join(dataset_dir, d))
        ]

    for ds in tqdm(dataset_names):
        try:
            res[ds] = single_ds_stat(Thesis1, path.join(dataset_dir, ds))
        except ValueError as e:
            print(f"cannot process {ds=}, because {e}")

    return res


def plot_df_stats(dataset_dir, dataset_names):
    data = df_stats(dataset_dir, dataset_names)

    # Sort projects by event_count in descending order
    projects = sorted(
        data.keys(),
        key=lambda x: data[x]["event_count"],
        reverse=False,
    )

    # Now 'sorted_projects' contains the project names sorted by their event_count# Create subplots
    fig = make_subplots(
        rows=3,
        cols=2,
        subplot_titles=(
            "Authors",
            "Reviewers",
            "Event Count",
            "File Count",
            "Project Timeline",
            None,
        ),
    )

    colors = px.colors.qualitative.Plotly
    color_map = {project: colors[i % len(colors)] for i, project in enumerate(projects)}

    ######### AUTHORS #########
    for project in projects:
        fig.add_trace(
            go.Bar(
                x=[project],
                y=[data[project]["authors"]],
                text=[project],
                name=project,
                legendgroup=project,
                showlegend=False,
                marker_color=color_map[project],
                textposition="auto",
                hovertemplate="%{text}: %{y:,}<extra></extra>",
            ),
            row=1,
            col=1,
        )
    fig.update_yaxes(
        title_text="Authors",
        row=1,
        col=1,
        type="log",
    )

    ########## REVIEWERS ###########
    reviewers = [data[p]["reviewers"] for p in projects]
    for project, reviewer_count in zip(projects, reviewers):
        fig.add_trace(
            go.Bar(
                x=[project],
                y=[reviewer_count],
                text=[project],
                name=project,
                legendgroup=project,
                showlegend=False,
                marker_color=color_map[project],
                textposition="auto",
                hovertemplate="%{text}: %{y:,}<extra></extra>",
            ),
            row=1,
            col=2,
        )
    fig.update_yaxes(
        title_text="Reviewers",
        row=1,
        col=2,
        type="log",
        matches="y1",  # Sync y-axis with the first subplot
    )

    ############## EVENT #############
    event_counts = [data[p]["event_count"] for p in projects]
    for project, count in zip(projects, event_counts):
        fig.add_trace(
            go.Bar(
                x=[project],
                y=[count],
                text=[project],
                name=project,
                legendgroup=project,
                showlegend=False,
                marker_color=color_map[project],
                #
                textposition="auto",
                hovertemplate="%{text}: %{y:,}<extra></extra>",
            ),
            row=2,
            col=1,
        )
    fig.update_yaxes(
        title_text="Event Count",
        row=2,
        col=1,
        type="log",
    )

    ########### FILES COUNT ##########
    file_counts = [data[p]["files"] for p in projects]
    for project, count in zip(projects, file_counts):
        fig.add_trace(
            go.Bar(
                x=[project],
                y=[count],
                text=[project],
                name=project,
                legendgroup=project,
                showlegend=False,
                marker_color=color_map[project],
                #
                textposition="auto",
                hovertemplate="%{text}: %{y:,}<extra></extra>",
            ),
            row=2,
            col=2,
        )
    fig.update_yaxes(
        title_text="File Count",
        row=2,
        col=2,
        type="log",
    )

    ############# TIMELINE #########
    for project in projects:
        fig.add_trace(
            go.Scatter(
                x=[data[project]["min_date"], data[project]["max_date"]],
                y=[project, project],
                name=project,
                legendgroup=project,
                showlegend=True,
                marker_color=color_map[project],
                #
                mode="lines+markers",
                #
                hovertemplate="%{y}: %{x}<extra></extra>",
            ),
            row=3,
            col=1,
        )
    fig.update_yaxes(
        title_text="Date",
        row=3,
        col=1,
        categoryorder="array",
        categoryarray=projects[::-1],
    )

    fig.update_layout(
        height=1200,
        width=1000,
        title_text="Dataset Statistics",
        # correct order
        xaxis={
            "categoryorder": "array",
            "categoryarray": projects,
        },
    )

    pio.show(fig)
