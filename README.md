# EASIER Replication Package

EASIER is a multi-objective optimization framework for software architecture model refactoring.

This README is designed for a conference replication package: it provides environment requirements, build steps, run modes, and output expectations for reproducibility.

## What Is In This Repository

Main modules:

- `easier-core`: core optimization abstractions and configuration parsing.
- `easier-uml`: executable optimization engine (main entrypoint).
- `easier-uml2lqn`: model transformation utilities.
- `easier-uml2lqnCaseStudy`: bundled case studies and model assets.
- `easier-refactoringLibrary`: refactoring action definitions.
- `easier-reliability`: reliability-related objective support.
- `easier-surrogate`: optional Python surrogate model service (REST API).
- `easier-docker`: Docker entrypoint script and default config example.

Top-level orchestration scripts:

- `mvn-install.sh`: installs required UML/profile jars into your local Maven repository.
- `run-compose-batch.sh`: launches N isolated Docker Compose run pairs (`easier-uml-job` + `easier-surrogate`).
- `run-compose-batch-shared-surrogate-fastapi.sh`: launches N jobs against one shared FastAPI surrogate instance.

## Prerequisites

For local execution:

- Linux/macOS shell environment.
- JDK 17.
- Maven (or the provided Maven Wrapper `./mvnw`).
- LQNS solver available at `/usr/local/bin/lqns` (or provide `--solver` in config).

For containerized execution:

- Docker Engine.
- Docker Compose v2 (`docker compose`).

Optional:

- `mail`/`mailx` command for end-of-run email notification.

## Quick Start (Replication Baseline)

1. Clone and enter the repository.

```bash
git clone https://github.com/danieledipompeo/easier.git
cd easier
```

2. Install required local UML/profile artifacts into Maven local repository.

```bash
./mvn-install.sh
```

3. Build the project.

```bash
./mvnw clean package -DskipTests
```

4. Run one experiment locally using a config file.

```bash
java -Xmx12g \
	-jar easier-uml/target/easier-uml-0.6.3-jar-with-dependencies.jar \
	@easier-core/config.ini
```

Notes:

- The config file format is JCommander argument-file style: each option and value is provided on separate lines.
- Model paths (`-m`, `-initialModelPath`) are resolved relative to `easier-uml2lqnCaseStudy`.

## Configuration File Essentials

The default example is `easier-core/config.ini`.

Important parameters commonly needed for replication:

- `-maxEval`: total number of evaluations.
- `-popSize`: population size.
- `-algo`: optimization algorithm (`nsgaii`, `spea2`, `rnsga`, `pesa2`, `rs`).
- `-qI`: quality indicator.
- `-outF`: output directory.
- `-tmpF`: temporary directory.
- `-m`: target UML model path.
- `-initialModelPath`: baseline model path for quality computation.
- `--solver`: LQNS solver path.
- `-s` / `--surrogate`: enable surrogate evaluation.
- `-se` / `--surrogate-endpoint`: surrogate API endpoint.

## Docker Usage

Build the Docker image:

```bash
docker build -t easier:<tag> -f easier-docker/Dockerfile .
```

Run a single job container:

```bash
docker run -it --rm easier:<tag> \
	<config-url-or-local-path> \
	<case-study-subdir> \
	[notify-email]
```

Behavior of container entrypoint (`easier-docker/easier.sh`):

- Downloads or copies config file.
- Downloads case-study models from `SEALABQualityGroup/uml2lqn-casestudies` using the provided branch/subdir argument.
- Executes `easier.jar` and writes logs into the configured output folder.

## Batch Replication with Docker Compose

### Mode A: Isolated Pair Per Run

Each run gets a dedicated `easier-surrogate` service and one `easier-uml-job` service.

```bash
./run-compose-batch.sh \
	--config-url=https://raw.githubusercontent.com/danieledipompeo/easier-experiment-data/main/ecsa26/config.ini \
	--case-study-subdir=modelling-energy \
	--output-root=/mnt/data/easier/ecsa26-batch \
	--notify-email=you@example.org \
	--project-prefix=ecsa26 \
	--runs=31
```

Artifacts:

- Per-run output directories: `/mnt/data/easier/ecsa26-batch/run1`, `run2`, ...
- Batch project index: `/mnt/data/easier/ecsa26-batch/batch-projects.txt`

### Mode B: Shared Surrogate for All Runs

Starts one shared surrogate container, then launches multiple `easier-uml-job` containers.

```bash
./run-compose-batch-shared-surrogate-fastapi.sh \
	--config-url=https://raw.githubusercontent.com/danieledipompeo/easier-experiment-data/main/ecsa26/config.ini \
	--case-study-subdir=modelling-energy \
	--output-root=/mnt/data/easier/fastapi-batch \
	--runs=31 \
	--project-name=easier-fastapi-batch \
	--surrogate-tag=fast-api \
	--surrogate-port=5000
```

## Expected Outputs

A typical run writes:

- run log file: `easier__<case-study>__<algorithm>.log`
- copied effective config: `config.ini`
- generated optimization outputs (including experiment/indicator data and produced fronts)
- optional Java Flight Recorder trace: `easier.jfr` (if JVM params are unchanged)

For controlled replication, archive:

- input config file(s)
- exact commit hash of this repository
- case-study branch/subdir used
- complete output folder for each run

## Reproducibility Checklist

- Use the same JDK major version (17).
- Keep the same `config.ini` (algorithm, budget, objectives, indicators).
- Keep fixed case-study source branch and model paths.
- Use the same run count (`--runs`) and orchestration mode (isolated vs shared surrogate).
- Archive container image tags and Maven artifact versions used for the run.

## Publications

- [Cortellessa, Vittorio, and Daniele Di Pompeo. "Analyzing the sensitivity of multi-objective software architecture refactoring to configuration characteristics." Information and Software Technology 135 (2021): 106568.](https://doi.org/10.1016/j.infsof.2021.106568)
- [Cortellessa, Vittorio, et al. "On the impact of Performance Antipatterns in multi-objective software model refactoring optimization." 2021 47th Euromicro Conference on Software Engineering and Advanced Applications (SEAA). IEEE, 2021.](https://doi.org/10.1109/SEAA53835.2021.00036)
- [Arcelli, Davide, Vittorio Cortellessa, Mattia D'Emidio, and Daniele Di Pompeo. "EASIER: an Evolutionary Approach for multi-objective Software archItecturE Refactoring." In 2018 IEEE International Conference on Software Architecture (ICSA), pp. 105-10509. IEEE, 2018.](https://doi.org/10.1109/ICSA.2018.00020)
- [Arcelli, Davide, Vittorio Cortellessa, and Daniele Di Pompeo. "A metamodel for the specification and verification of model refactoring actions." In Proceedings of the 2nd International Workshop on Refactoring, pp. 14-21. ACM, 2018.](http://doi.acm.org/10.1145/3242163.3242167)

## License

This repository is licensed under the **MIT License**.

- Main license file: `LICENSE` (repository root).
- Documentation/theme assets under `docs` retain their existing CC0 notice in `docs/LICENSE`.

When preparing replication artifacts, include both license files if you redistribute source and documentation assets together.
