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

## Run with Docker Compose (isolated run pairs)

Use this mode when each run must keep its own `easier-surrogate` paired with its
own `easier-uml` container. The batch helper starts one Docker Compose project
per run, so every run gets an isolated network and a dedicated surrogate model
service.

1. Ensure the endpoint in your `config.ini` points to:

```text
http://easier-surrogate:5000/surrogate
```

2. Submit a batch of runs with the helper script:

```bash
# remote config URL
./run-compose-batch.sh \
	--config-url=https://raw.githubusercontent.com/danieledipompeo/easier-experiment-data/main/ecsa26-nsgaii-ccm-eval-102-surrogate--50/config.ini \
	--case-study-subdir=modelling-energy \
	--output-root=/mnt/data/easier/ecsa26-nsgaii-ccm-eval-102-surrogate--50 \
	--notify-email=foo@example.com \
	--project-prefix=ecsa26 \
	--runs=31

# local config file (auto-mounted in each job container)
./run-compose-batch.sh \
	--config-url=./easier-docker/config.ini \
	--case-study-subdir=modelling-energy \
	--output-root=/mnt/data/easier/ecsa26-nsgaii-ccm-eval-102-surrogate--50 \
	--notify-email=foo@example.com \
	--project-prefix=ecsa26 \
	--runs=31
```

This creates output folders like `.../run1`, `.../run2`, ..., `.../run31`, plus
compose projects like `ecsa26-run1`, `ecsa26-run2`, ..., `ecsa26-run31`. Each
project contains one `easier-surrogate` and one `easier-uml-job` container.

Useful commands:

```bash
docker compose -p ecsa26-run1 ps
docker compose -p ecsa26-run1 logs -f easier-surrogate
cat /mnt/data/easier/ecsa26-nsgaii-ccm-eval-102-surrogate--50/batch-projects.txt
```

Stop services when done:

```bash
while read -r project _; do
	docker compose -p "$project" down
done < /mnt/data/easier/ecsa26-nsgaii-ccm-eval-102-surrogate--50/batch-projects.txt
```
