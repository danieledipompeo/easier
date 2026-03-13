# Instruction for Docker

 - outputFolder: is specified within the easier config file. Default value is `/mnt/easier-output/`
 - tag: is the tag that specifies the easier version

## Build a Docker image

```bash
cd ..
docker build -t easier:<tag> -f easier-docker/Dockerfile .
```
## Run a Docker image

```bash
cd ..
docker run -it --rm easier:<tag> [config url or local path] [case study github branch] [notify email optional]
```

If you pass a local config path, it must exist inside the container (for example
via a bind mount).

## Run with Docker Compose (surrogate + batch jobs)

Use this mode when `easier-surrogate` must stay up as a web service and multiple
`easier-uml` runs send JSON requests to it.

1. Ensure the endpoint in your `config.ini` points to:

```text
http://easier-surrogate:5000/surrogate
```

2. Start the surrogate service:

```bash
docker compose up -d easier-surrogate
```

3. Submit a batch of runs with the helper script:

```bash
# remote config URL
./run-compose-batch.sh \
	--config-url=https://raw.githubusercontent.com/danieledipompeo/easier-experiment-data/main/ecsa26-nsgaii-ccm-eval-102-surrogate--50/config.ini \
	--case-study-subdir=modelling-energy \
	--output-root=/mnt/data/easier/ecsa26-nsgaii-ccm-eval-102-surrogate--50 \
	--notify-email=foo@example.com \
	--runs=31

# local config file (auto-mounted in each job container)
./run-compose-batch.sh \
	--config-url=./easier-docker/config.ini \
	--case-study-subdir=modelling-energy \
	--output-root=/mnt/data/easier/ecsa26-nsgaii-ccm-eval-102-surrogate--50 \
	--notify-email=foo@example.com \
	--runs=31
```

This creates output folders like `.../run1`, `.../run2`, ..., `.../run31` and
starts detached containers for each run.

Useful commands:

```bash
docker compose ps
docker compose logs -f easier-surrogate
docker ps
```

Stop services when done:

```bash
docker compose down
```
