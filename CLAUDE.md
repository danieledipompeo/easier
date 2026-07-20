# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

EASIER is a multi-objective optimization framework (research replication package) that refactors software
architecture models (UML + MARTE/DAM profiles) to improve non-functional attributes — performance, reliability,
energy, power, economic cost, and number of performance antipatterns — while minimizing refactoring cost. It's a
multi-module Maven/Java project built on jMetal (evolutionary algorithms), Eclipse Epsilon (EOL/EVL/EWL model
transformation), EMF/UML2, and an optional Python surrogate model service.

## Build & Test Commands

```bash
# One-time: install vendored UML2/profile jars not on Maven Central
./mvn-install.sh

# Build everything (skip tests, since a full run needs the LQNS solver + submodules)
./mvnw clean package -DskipTests

# Run the full test suite for a module
./mvnw -pl easier-uml test

# Run a single test class
./mvnw -pl easier-uml test -Dtest=ObjectiveEstimatorMockTest

# Run a single test method
./mvnw -pl easier-uml test -Dtest=ObjectiveEstimatorMockTest#setObjective

# Run one optimization experiment locally (needs lqns at /usr/local/bin/lqns or --solver override)
java -Xmx12g -jar easier-uml/target/easier-uml-0.6.3-jar-with-dependencies.jar @easier-core/config.ini
```

JDK 17 is required (`jdk.version` in the root `pom.xml`). Config files use JCommander argument-file syntax
(`@config.ini`, one flag/value per line) — see `easier-core/config.ini` for the canonical example and inline
comments documenting supported algorithms/objectives/quality-indicators.

**Known build gap**: a full `easier-uml`/`easier-reliability` reactor build can fail with
`Could not resolve dependencies for ... it.univaq.sealab.umlreliability` even after running
`mvn-install.sh`, because `easier-reliability`'s pinned submodule commit depends on
`it.univaq.disim.sealab.uml.profiles:org.eclipse.papyrus.marte.static.profile:1.2.0.201703081153`, a
version whose install command is commented out in `mvn-install.sh` (a jar for it exists under
`easier-maven/`, but installing it manually still fails one level deeper on a missing
`org.eclipse.papyrus:org.eclipse.papyrus.extra.releng:1.2.0-SNAPSHOT` parent POM). This reproduces on a
clean checkout — it isn't caused by local changes. `easier-core` alone builds fine and is a useful
isolation check when this blocks a PR's `easier-uml`-side changes.

## Module Map & Git Submodules

The root `pom.xml` aggregates: `easier-core`, `easier-uml`, `easier-epsilon`, `easier-uml2lqn`,
`easier-refactoringLibrary`, `easier-uml2lqnCaseStudy`, `easier-reliability`.

Several of these are **git submodules** (see `.gitmodules`), each independently versioned and hosted under
`SEALABQualityGroup` (or `mtucci` for `easier-user-profile`):

- `easier-refactoringLibrary` → `padre-perf_detection` (branch `devFuzziness`)
- `easier-uml2lqn` → `uml2lqn` (branch `easier`)
- `easier-uml2lqnCaseStudy` → `uml2lqn-casestudies` (branch `support-workload`)
- `easier-reliability` → `uml-reliability` (branch `easier`)
- `easier-user-profile` → `easier-user-profile`

When these show as modified (`m`) in `git status`, that's a submodule pointer change, not local edits — check
`git -C <submodule> status` / `git -C <submodule> log` before assuming something is dirty. Also check for
**untracked content inside a submodule** before relying on it (e.g. a new case-study model under
`easier-uml2lqnCaseStudy/`) — it may only exist in that one local checkout and never have been pushed to
the submodule's own remote, so a test or config referencing it will fail for anyone else (CI included)
unless it's pushed there separately.

`easier-surrogate` is a standalone Python project (its own `.git`), not a Maven module or submodule of this repo.

## Core Architecture: Optimization Pipeline

Entry point: `easier-uml/.../Launcher.java` (`main`). Flow:

1. **Config parsing** — JCommander binds CLI/`@file` args onto the `Configurator` singleton
   (`easier-core/.../utils/Configurator.java`, accessed everywhere as `Configurator.eINSTANCE`). It holds every
   tunable: algorithm choice, objectives list, node characteristics, refactoring cost factors (BRF), surrogate
   settings, search-budget stopping criteria, etc. New CLI flags are added here as `@Parameter` fields.
2. **UML→LQN transformation** — if `output.xml`/`output.lqxo` don't already exist next to the model,
   `WorkflowUtils.applyTransformation` + `invokeSolver` run the `easier-uml2lqn` transformation and invoke the
   external LQNS solver to produce a Layered Queueing Network analysis of the UML model.
3. **Problem/algorithm setup** — `FactoryBuilder` wires up the jMetal `ExperimentAlgorithm` (nsgaii, spea2,
   rnsga, pesa2, rs, ibea, nsgaiii — see `Configurator`'s `-algo` doc), a `UMLRProblem`/`UMLRSolution` pair, and
   `RSolutionListEvaluator` as the jMetal `SolutionListEvaluator`.
4. **Evaluation loop** — `RSolutionListEvaluator.evaluate()` (in
   `easier-uml/.../evolutionary/operator/`) drives each candidate solution through
   `WorkflowUtils.executeFlow` (applies the solution's refactoring actions, re-runs the LQN transform/solver),
   then either:
   - computes objectives directly via `ObjectiveEstimator.computeObjectives`/`setConsideredObjectives`, or
   - if `--surrogate` is enabled and the solution is marked for surrogate evaluation, batches solutions and
     POSTs them to the Python surrogate service (`--surrogate-endpoint`) via `ObjectiveEstimator.surrogateEvaluation`.
   Failed solutions are logged to CSV and replaced with a freshly generated candidate rather than aborting the run.
5. **Objectives** (`ObjectiveEstimator`, `easier-uml/.../evolutionary/operator/`): performance antipatterns (`pas`,
   via the Epsilon-based antipattern detector in `easier-refactoringLibrary`), reliability (delegates to the
   `easier-reliability` submodule), refactoring cost/`changes` (sum of BRF-weighted action costs from
   `Configurator`), `perfq`, system response time, energy, power, economic cost, and their `*PerScenario`
   variants (dynamically expanded per use-case scenario found in the LQN output).
6. Results are written as jMetal experiment output (FUN/VAR files, reference fronts) plus a JSON dump via
   `EasierExperimentDAO`/`FileUtils`.

Refactoring actions themselves (clone, move-operation-to-component, move-component-to-new-node, remove-node,
resource-scaling, etc.) live under `easier-uml/.../actions` and `easier-refactoringLibrary`; the antipattern
detection and model-diagram generation is implemented as Eclipse Epsilon scripts (`.evl`/`.eol`/`.ewl`/`.egl`)
under `easier-refactoringLibrary/{evl,ewl,egl}`.

**Adding a new refactoring action requires two registrations, not one**: implementing the
`UMLRefactoringAction` subclass and adding its `case` to `RefactoringActionFactory.getRandomAction`'s switch
is not sufficient on its own — `Configurator.listOfActions()` (what the mutation operator actually samples
from) is derived strictly from the keys of `Configurator.brfs_list`. An action missing from `brfs_list` is
reachable in the switch but will never be randomly selected during a search, and `getBRF()`'s fallback
(`1.23`) silently masks the omission instead of erroring. Register the action's name in the default
`brfs_list` (with an explicit BRF weight) alongside the factory `case`.

## Surrogate Model Service (`easier-surrogate/`)

Optional Python service that approximates objective evaluation to avoid running the full UML→LQN→solve pipeline
for every candidate. Two server variants exist: `surrogate_server.py` (Flask) and `surrogate_server_fastapi.py`
(FastAPI, with a Memcached-backed model cache in `memcached_cache.py`). Core logic in `surrogate.py`
(`EasierSurrogate`: one XGBoost model per objective, supports incremental/active-learning retraining) and
`encoder.py` (`EasierEncoder`: turns a solution's refactoring-action sequence into a feature vector). Serialized
models live in `models/*.json`, keyed by case study and objective (`<case-study>__y__<objective>.json`).

The Java side POSTs `{caseStudy, solutions, iteration, k}` to the `/surrogate` endpoint and expects a flat JSON
response with per-solution predicted objectives (see `ObjectiveEstimator.surrogateEvaluation`); `k` controls the
retrain interval (`--surrogate-retrain-interval`). Relevant config flags: `-s`/`--surrogate`,
`-se`/`--surrogate-endpoint`, `--surrogate-probability`, `--surrogate-retrain-interval`.

## Docker / Batch Replication

- `easier-docker/Dockerfile` + `easier-docker/easier.sh`: builds a runnable image bundling the
  `easier-uml` jar, `easier-refactoringLibrary`, and `easier-uml2lqn`; the entrypoint downloads a config file and
  the target/initial case-study models from `SEALABQualityGroup/uml2lqn-casestudies` at a given branch, then runs
  the jar and tees output to a log file (optionally emailing a completion notice via `mail`).
- `docker-compose.yml` defines the `easier-surrogate` service (built from `easier-surrogate/Dockerfile`) and an
  `easier-uml-job` service (profile `jobs`) that depends on the surrogate being healthy.
- `run-compose-batch.sh` / `run-compose-batch-shared-surrogate-fastapi.sh`: orchestrate N replicated runs, either
  each with its own isolated surrogate container or all sharing one FastAPI surrogate instance. See `README.md`
  for full usage examples and the reproducibility checklist (pin JDK, config, case-study branch, run count).

## Notes

- No `.cursorrules`/Copilot instructions exist in this repo. `easier-surrogate/.gemini/GEMINI.md` has
  Gemini-CLI-specific notes on that subproject only.
- `easier-user-profile` and `easier-dataAnalyst` are auxiliary modules not wired into the root `pom.xml`'s
  `<modules>` list — treat them as standalone unless a task explicitly touches them.
- **`main` is the active branch and GitHub's configured default** on `origin` (danieledipompeo/easier) as of
  2026-07-20 — target PRs at `main`, not `devUML` (deleted; it was a stale 2023 branch that had drifted onto
  an unrelated GitHub-Pages-style `docs/` layout). A handful of old, never-merged 2020–2022 branches
  (`DEV`, `dev-logicalSpecification`, `devWorsenModels`, `ft-compute-qi-each-step`, `refactor-evo-operator`,
  `reference-pareto-test`, `vnzstc-dev`) are still on `origin`, kept pending manual review — don't assume
  they're current or build on them without checking first.
- `gh pr create` against this fork can fail with a misleading `Head sha can't be blank ... No commits
  between <base> and <head>` error if `--repo danieledipompeo/easier` is omitted — `gh` defaults to
  resolving against the upstream parent (`SEALABQualityGroup/EASIER`), which doesn't have the branch. Always
  pass `--repo` explicitly here.

# Project architecture

- Preserve algorithmic behavior exactly.
- Never change the optimization logic unless explicitly requested.
- Keep compatibility with the current JMetal version.
- Minimize public API changes.
- Favor small reusable components over deep inheritance hierarchies.
- Keep SonarQube issues in mind, but readability is more important than maximizing the quality score.
- Do not introduce new dependencies without approval.
- Refactor incrementally with small commits.

# Collaboration Workflow

Before making significant changes:
- Produce a short implementation plan.
- Explain the rationale behind the proposed architecture.
- Wait for approval before large refactorings.

When refactoring:
- Preserve behavior exactly.
- Prefer extracting reusable components over copying code.
- Avoid changing public APIs unless explicitly requested.
- Verify that the project builds after each logical step.

When modifying multiple files:
- Keep commits small and logically grouped.
- Explain each commit message before committing.

Never:
- Rewrite Git history unless explicitly requested.
- Force-push branches.
- Remove code that may still be in use without asking.

# Git Workflow

Before creating a branch:
- Check whether a similar feature branch already exists.

When working with branches:
- Keep one logical feature per branch.
- Avoid mixing refactoring with new features.
- Keep main releasable.
- Before merging, summarize the changes introduced by the branch.

When asked to reorganize branches:
- Analyze the complete branch graph first.
- Identify implemented features independently from the branch names.
- Detect duplicated work and obsolete branches.
- Produce a migration plan before executing any Git command.

Never:
- Rebase or rewrite published branches without approval.
- Delete branches without confirmation.

# Continuous Project Memory

When an important architectural decision is made:
- Propose an update to this CLAUDE.md.
- Never modify this file automatically.
- Wait for approval before updating project memory.