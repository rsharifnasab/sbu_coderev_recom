
## Install batcore:

+ Install pyenv and pyenv-virtualenv

+ Install batc with correct python versoin

```bash
pyenv venv 3.8 batc
pyenv activate batc
pyenv shell batc
pip install git+https://github.com/JetBrains-Research/batcore
pip install -r requirements.txt
```

## Run

+ Fill the desired values in `run.py`
    + Gather dataset and set it's path in `run.py`
    + Select models and datasets and measures you want
    + Select that you want to investigate datasets or not

+ Run
    + Activate pyenv shell: `pyenc activate batc`
    + Run: `python3 run.py`
