# Ivy Expand — Project Rules

**Owner-signed rules (RULE-01…05)** and **ratified packs (RULE-10…40, ratified 2026-11-04).**
**Scope:** every agent (human or AI) touching this repo.
**Precedence:** Your word (conversation) > this file > README > everything else.
**Changes:** a rule changes only with your explicit approval; exceptions require a line in `docs/adr/`.
**Violation:** no commit/APK/release proceeds while violated.

---

## RATIFIED decisions (2026-11-04)

**License direction — Owner pick: A3 "Closed-source rewrite later".**

*Status and obligations (this is the only correct reading of A3):*
- Until 100% of Ivy-derived files are replaced by clean-room files, every distributed build is a combined GPL-3.0 work. You **must** distribute it under GPL-3.0 with source available (the public fork satisfies this). Selling under GPL-3.0 is allowed; charging is price-free.
- "Gradually replacing" until the last derived file is gone does not make the APK closed-source before that moment. Exclusive rights arrive at the last clean-room file, not before.
- Practical path: ship GPL-3.0 publicly until the rewrite is complete, then tag the clean-room release; that final tag may be closed-source.
- During the transition, all original Splitly/Ledger code ships under GPL-3.0 — you can sell it, you just can't restrict buyers' GPL rights.
- `LICENSE` stays GPL-3.0 until that point; About/README credits the fork history and Ivy Wallet authors forever.

**Ratified rule packs (binding as of 2026-11-04):** Legal (10-15), UX (16-18), Build (19-24), Quality (25-29), Security (30-34), Ops (35-40) — all become hard blockers for merging, building or releasing on your behalf; no silent bypass.

---

## RULES (ratified 2026-11-04)

**Legal & licensing**
- **RULE-10** — OSS allowlist: MIT / Apache-2.0 / BSD-2 / BSD-3 / ISC / CC0 / Unlicense / Zlib / PostgreSQL. Anything else (incl. MPL-2.0, LGPL) needs your written approval + explicit decision entry.
- **RULE-11** — `THIRD_PARTY_NOTICES.md` maintained on every dep change (name, version, license, canonical URL).
- **RULE-12** — Zero tracking SDKs: no analytics, no ads, no Crashlytics, no GCM/FCM, no session recorders. Firebase only if Phase 5+ (invite sync) is ratified.
- **RULE-13** — GPL compliance kept forever (until the A3 clean-room release): `LICENSE` untouched, copyright notices intact, About screen credits Ivy Wallet authors & fork history.
- **RULE-14** — Attribution-forever: every borrowed glyph/section lists license in `ASSET-SOURCES.md`; OFL/MIT/Apache fine, others need your sign-off.
- **RULE-15** — Prohibited assets: no assets fetched/derived from Play Store / App Store / commercial binaries; no tracing; everything drawn fresh.

**Design, UX, accessibility**
- **RULE-16** — Baseline a11y: TalkBack semantics on interactive elements, >= 48dp targets, contrast >= 4.5:1, dynamic type up to 200% tested, no info by colour alone, reduced-motion honoured.
- **RULE-17** — Every screen: designed empty / loading / error state; retry always visible.
- **RULE-18** — Change freeze: one week before any release candidate, only severity-0/1 fixes land.

**Build, release & git**
- **RULE-19** — Never commit artifacts; releases via GitHub Releases with SHA-256; repo stays text-only; debug keystore only for debug builds; release key in CI secrets or offline. `bundleRelease` is the only signed build path.
- **RULE-20** — `versionCode` always increments; tags signed & annotated; no force-push on `cyberhive54/update`.
- **RULE-21** — CI gates every merge: Detekt clean; unit + Paparazzi green; `--max-workers=2 --stop`; no `./gradlew` autonomy.
- **RULE-22** — Module direction: `:feature` -> `:shared` -> `:app`; module-graph assertion in CI.
- **RULE-23** — No silent refactor: cross-file renames/moves need explicit milestone + separate commit.
- **RULE-24** — Conventional commits: `feat|fix|refactor|docs|test|chore`; imperative mood; <= 72 chars.

**Quality & testing**
- **RULE-25** — Tests first for engines: `SplitEngine`, `SimplifyBalances`, migration, backup round-trip — each ships with >= 1 property test + >= 3 round-trip cases before merge.
- **RULE-26** — Backup round-trip verified before any schema-touching merge.
- **RULE-27** — New screens get Paparazzi snapshot tests (light/dark/RTL/200% font) before merge.
- **RULE-28** — `progress-track.md` updates land in the same commit as the change.
- **RULE-29** — WorkManager tasks are owned, bounded, logged; nothing fires at Application start without a milestone.

**Security & privacy**
- **RULE-30** — Permissions only in the manifest that truly needs them; each new permission = your sign-off + Play declaration.
- **RULE-31** — Never logs PII; financial entries never reach Timber outside Debug.
- **RULE-32** — Backup/export files: user-readable (JSON/CSV); user chooses location; no silent cloud upload.
- **RULE-33** — App-lock pin / biometric respected; sensitive screens avoid plaintext layout dumps.
- **RULE-34** — Screenshots hardened on backgrounding for ledger screens (`FLAG_SECURE` Settings toggle; default off).

**Ops & distribution**
- **RULE-35** — Staged rollouts: internal -> 10% -> 50% -> 100%; crash/ANR > 0.5% blocks rollout.
- **RULE-36** — Post-release: changelog synced with Play listing; `THIRD_PARTY_NOTICES.md` regenerated; `fastlane` metadata in sync.
- **RULE-37** — Dependency cadence per release: quarterly bump, Detekt + lint before merge, no "upgrade for upgrade's sake".

**Governance & social**
- **RULE-38** — External PRs require DCO `Signed-off-by`; legal review before merging new API surface.
- **RULE-39** — If a rule blocks a good change: stop, ask you, never route around it. This file grows only by your hand.
- **RULE-40** — Every architectural decision gets one line in `docs/adr/NNNN-title.md`.

---

## Known tension (owner choice, documented)

A3 (closed-source rewrite later) vs GPL-3.0 fork **as-is**: the fork cannot become closed-source until every Ivy-derived file is replaced by clean-room code. Until then, every release ships open source. This is the cost of A3; A1 (open-source commercial, selling under GPL) is cheaper and adds no legal risk.

---

## Tracked changes summary (authoritative record)

| Date | Event | Decisions |
|---|---|---|
| 2026-11-04 | Ratification of full rulebook | All packs ratified. Owner picked **A3 (closed-source rewrite later)**. Play production trajectory unchanged; fork stays public until A3 finishes. |

*(This table is append-only; never edit history.)*
