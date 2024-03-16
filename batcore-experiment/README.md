# Experiment with Bat Core


## Gather Dataset

### Official way: MR-Loader
+ Downloads all gerrit data at once with weak error handling mechanims.

```bash
docker run \
    --volume ./eclipse-gerrit-data:/root \
    -it \
    ghcr.io/jetbrains-research/mr-loader/mr-loader:latest \
    GerritLoad \
    --url https://eclipse.gerrithub.io/
```
+ Repo link is available [here](https://github.com/JetBrains-Research/MR-loader)
+ It is nearly impossible to run locally without docker. Because output path is hardcoded to `/root`!
+ It does not save the output as it goes, but rather save in an intermediate format and saves to otuput at the end (which would never come)
+ Dataset format is documented [here](https://github.com/JetBrains-Research/MR-loader?tab=readme-ov-file#dataset-format)

### Customized way: from GitHub
