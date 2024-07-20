from pprint import pprint

import numpy as np
from scipy.stats import alpha, wilcoxon

"""
                 Model            Dataset      f1@3     f1@10
0            chrev  gerrit-ci-scripts  0.244649  0.252656
1            acrec  gerrit-ci-scripts  0.491070  0.404089
2    thesis_extend  gerrit-ci-scripts  0.565718  0.401080
3  thesis_noextend  gerrit-ci-scripts  0.551245  0.416899
4            chrev           git-repo  0.385873  0.384777
"""


def wilcoxon_impl(thesis_datas, base_datas, measures, alpha=0.05):
    # print("thesis data")
    # pprint(thesis_datas)
    # print("base data")
    # pprint(base_datas)

    thesis_list = []
    for measure in measures:
        thesis_list.extend(thesis_datas[measure])
    base_list = []
    for measure in measures:
        base_list.extend(base_datas[measure])

    # pprint(thesis_list)
    # pprint(base_list)

    thesis_arr = np.array(thesis_list)
    base_arr = np.array(base_list)

    diff = thesis_arr - base_arr
    _ = diff

    # Perform the Wilcoxon signed-rank test
    statistic, p_value = wilcoxon(thesis_arr, base_arr)

    significant_difference = False
    if p_value < alpha:
        significant_difference = True

    return (
        p_value,
        significant_difference,
        statistic,
        np.mean(thesis_arr),
        np.mean(base_arr),
    )


def stat_test(df, measures):
    models = df["Model"].unique()

    thesis_models = [model for model in models if "thesis" in model.lower()]
    base_models = [model for model in models if "thesis" not in model.lower()]

    for thesis_model in thesis_models:
        for base_model in base_models:
            thesis_model_data = df[df["Model"] == thesis_model]
            base_model_data = df[df["Model"] == base_model]

            p_value, significant_difference, _, avg_thesis, avg_base = wilcoxon_impl(
                thesis_model_data,
                base_model_data,
                measures=measures,
                alpha=0.1,
            )
            print(
                f"thesis={thesis_model:<14} ({avg_thesis:.4f}), base={base_model:<8} ({avg_base:.4f}), p={p_value:.6f}, is_significant={significant_difference}"
            )
