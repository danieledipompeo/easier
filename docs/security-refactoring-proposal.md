# Proposal: a security dimension for EASIER

## 1. Gap analysis

EASIER currently optimizes UML+MARTE/DAM architecture models along six axes: performance
antipatterns (`pas`), reliability, energy, power, economic cost, and refactoring cost (`changes`),
plus response-time variants. There is no security dimension, and no groundwork for one:

- **No security stereotypes** exist in any MARTE/DAM profile fragment vendored or referenced by
  this project (`easier-reliability/src/main/resources/umlprofiles/{MARTE,DAM}.profile.uml`, the
  MagicDraw MARTE fragments under `easier-refactoringLibrary/model/uml/BGCS/`). MARTE/DAM usage
  here is exclusively performance/reliability/energy-oriented (`GaExecHost`, `GaCommChannel`,
  `RtUnit`, `GaScenario`, `GaStep`, `DaComponent`, `DaConnector`, `DaFailure`).
- **No security-named refactoring actions, EOL/EVL scripts, or objectives** exist anywhere in
  `easier-uml`/`easier-refactoringLibrary`/`Configurator`.
- Elements literally named "security" in the case-study models are coincidental: `eshopper.uml`
  has a `security` node/artifact that is just one of several ordinary e-commerce microservice
  containers (alongside `wishlist`, etc.); `acmeair.uml` has `Auth`/`ValidateId` components that
  are ordinary login functionality, not hardened access control.
- **`GaCommChannel` — the one MARTE stereotype that could plausibly represent a network link's
  security-relevant properties — is dead code.** It's referenced only in an unused helper
  (`easier-refactoringLibrary/evl/library/node.eol`'s `isNetworkLink()`/network-usage calculation)
  and is never actually applied to any `CommunicationPath` in any real case-study model. Worse,
  `CommunicationPath` elements are unnamed (`name=""`) in every case study checked, and every
  existing action that touches links (`UMLCloneNode`'s `cloneLink()`) handles them *in bulk* via
  `Node.getDirectlyLinkedNode()` — there is no precedent anywhere in this codebase for selecting
  one specific link by identity. This directly shaped the prototype's design (Section 3): a new
  action targeting "encrypt this one specific channel" has no reusable selection mechanism to
  build on, so the prototype instead targets a `Node`, exactly like `UMLResourceScaling` already
  does.

## 2. Proposed lightweight `Security` profile (design only, not implemented this pass)

A proper implementation would eventually want dedicated stereotypes, so security properties are
queryable structurally (by an EVL antipattern rule, say) rather than inferred from a `Comment`'s
text, as the Section 3 prototype does as an interim measure:

| Stereotype | Extends | Tagged values | Purpose |
|---|---|---|---|
| `SecureChannel` | `Association` | `encryptionOverheadFactor: Real` | Marks a communication path as encrypted, with a performance cost. |
| `AccessControlled` | `Component` / `Node` | `authRequired: Boolean`, `minPrivilegeLevel: Integer` | Marks an element as access-gated. |
| `AuditLogged` | `Component` | `logOverheadFactor: Real` | Marks an element as producing an audit trail, with a performance cost. |

This requires proper EMF/Ecore profile registration (a `.profile.uml`/`.ecore`/`.genmodel` triple,
a pathmap URI, and re-generating/registering the profile the way the vendored MARTE/DAM profiles
are) — a materially bigger lift than a single prototype action warrants, and is deferred.

## 3. Candidate refactoring actions

Each candidate is mapped to a security-tactic category (Bass, Clements & Kazman's taxonomy:
Detect / Resist / React / Recover attacks), a structural transformation, its target element type,
and the existing EASIER action it's most directly analogous to (i.e., what to copy as a starting
point).

| Action | Tactic | Transformation | Target | Analogous to | Status |
|---|---|---|---|---|---|
| `EncryptComm` | Resist | Reduce a node's `GaExecHost::speedFactor` (crypto CPU overhead) + attach a marker `Comment` | `Node` | `UMLResourceScaling` | **Prototyped this pass** |
| `InsertAuthGateway` | Resist / Detect | Insert a new gateway `Component`, reroute messages through it | `Component`/`Message` | `UMLMvOperationToNCToNN` | Proposed |
| `IsolateOnDedicatedNode` | Resist | Move a sensitive `Component` onto a new, dedicated `Node` | `Component` | `UMLMvComponentToNN` | Proposed |
| `AddAuditProbe` | Detect | Attach a logging marker/tag to a component's message flow | `Component` | `UMLChangePassiveResource` | Proposed |
| `RestrictInterfaceExposure` | Resist (least privilege) | Remove unused/over-broad operations from a public `Interface`; cost = # operations removed | `Interface` | `UMLRemoveComponent`'s removal pattern | Proposed |

`InsertAuthGateway`, `IsolateOnDedicatedNode`, `AddAuditProbe`, and `RestrictInterfaceExposure` are
proposed at the design level only — not implemented in this pass.

## 4. Objective-pipeline integration (design sketch, not wired this pass)

Objectives are config-driven (`Configurator.getObjectivesList()`, a `List<String>`), and jMetal's
objective-vector size derives from that list's length at construction (`UMLRProblem`/`RSolution`),
so adding a new objective is primarily a wiring change, following the exact template `pas` already
uses:

1. A new `Secur-UML-MARTE.evl`, analogous to `easier-refactoringLibrary/evl/AP-UML-MARTE.evl`,
   with critiques like `UnhardenedNode` (a `Node` with no `EASIER-SECURITY:`-marked `Comment` and
   at least one outbound `CommunicationPath`) or `OverprivilegedInterface`.
2. A `easier-security` submodule, mirroring `easier-reliability`'s clean
   `Model → extractor → compute()` shape (`UMLModelPapyrus` → a domain extractor → a `compute()`
   returning a `double`) — `easier-reliability`'s entire API surface is 3 constructor/method calls,
   a good template.
3. Registration in `ObjectiveEstimator`: a `Configurator.SECURITY_LABEL` constant, a
   `security(...)` compute method, entries in `initObjectives()`/`computeObjectives()`, and adding
   the label to the `knownExactLabels` set in `setConsideredObjectives()`.

## 5. Stated limitation

LQN (the queueing-network formalism EASIER solves models against via `lqns`) can express a
security control's **performance overhead** — an extra task/entry/activity with added demand — but
cannot express an abstract security *guarantee* (confidentiality, correctness of an access-control
policy). Any resulting metric from Section 4 would measure overhead and/or the presence/absence of
mitigations in the model, not verified security correctness. This should be stated plainly
wherever such a metric is reported, to avoid overclaiming what it means.
