# Ivy Expand — Development Plan (P/S/M)

**Companion files:** `ivy-expand-plan.md` (the full decision log + specs) · `progress-track.md` (live status board, same IDs)
**Owner:** `cyberhive54/update` fork · **Contact:** this plan governs work on `D:\Ivy-Wallet-f`
**Ground rules:** offline-first · GPLv3 · personal (non-SME) · no paywall · manual confirm always · privacy-first.

---

## 0. How to read the IDs

| Token | Meaning |
|---|---|
| **P<n>** | Phase — one release-sized block |
| **S<n>** | Stage — one coherent slice inside a phase |
| **M<n>** | Milestone — one verifiable gate (build + automated test OR on-device checklist) |
| **T<n>** | Task — child of milestone; tracked in progress-track.md |

**Rules**
- IDs are immutable — never renumber, delete or reuse. Skipped milestones are marked `SKIPPED`, not erased.
- A milestone is **Done** only when its Gate passes (build green or phone checklist confirmed).
- A **Gate** is `CI` (tests + compile), `OD` (on-device checklist), or `OD+CI` (both).
- Dependencies are explicit (`depends: P1.S2.M2`) — nothing is assumed parallel.
- Progress is recorded in `progress-track.md` only.

**Phase gates (the release witness points):**
- **P1 Gate** — install APK on device; toggle Splitly/Ledger on-off under load of real data; restore-from-backup round-trip green.
- **P2 Gate** — ledger flow end-to-end on device (CRUD + Reminders + PDF share).
- **P3 Gate** — full Splitly flow incl. settle; module hidden again at zero records.
- **P5** — optional; Play push happens only after P3 Gate.

---

## P0 — Workspace & build hygiene (no code)

| ID | Milestone | Gate | Depends |
|---|---|---|---|
| P0.S1.M1 | Branch audit: work only on `cyberhive54/update`; PRs none; forks clean (`git status` tracked) | CI | — |
| P0.S1.M2 | `dagger`/debug keystore unchanged; `applicationId = com.cyberhive.wallet[.debug]` verified in Gradle | CI | P0.S1.M1 |
| P0.S1.M3 | Low-spec build policy recorded: `-Xmx1536m`, `--max-workers=2`, `parallel=false`, stop daemons; `assembleDebug` verified on host | OD | P0.S1.M2 |
| P0.S1.M4 | Versioning policy: `version-name/version-code` unchanged until release; bump only after P3 Gate | CI | P0.S1.M1 |
| P0.S1.M5 | Privacy page path reserved: `docs/privacy/index.html` on GitHub Pages; content drafted (Appendix A in ivy-expand-plan.md) | OD | P0.S1.M1 |

---

## P1 — Workspace shell + Subscriptions

> **Theme:** you open the app and see the three rails it was promised to see; subs never surprise you again. Running ref: `ivy-expand-plan.md` §2–§5.

### S1 — Room v131 → v132 (migration slice)

| ID | Milestone | Gate | Depends |
|---|---|---|---|
| P1.S1.M1 | Migration file (`Migration131to132`): nullable columns only (`customerPhone`, `counterpartyName`, `recordDueDate`, `recordPhone`, `recordattachmentUrl`, `remainingOccurrences`, `priceHistory` table, groups/split entities new) | CI | P0.S1.M2 |
| P1.S1.M2 | `schema/com.ivy.data.db.IvyRoomDatabase/132.json` exported + committed; 131→132 upgrade on real device DB (your phone restorable) | OD | P1.S1.M1 |
| P1.S1.M3 | androidTest: backup round-trip — v115→v132 import/export identical collections, photos OFF by default | CI | P1.S1.M1 |
| P1.S1.M4 | androidTest: migration test new columns (nullable, no data loss on existing loans/planned_payment rows) | CI | P1.S1.M1 |

### S2 — Workspace shell, rail, docks

| ID | Milestone | Gate | Depends |
|---|---|---|---|
| P1.S2.M1 | `Workspace` enum + `LocalWorkspace` in `shared/base`; `IvyNavGraph` switches content per workspace; rail active state tracked | CI | P1.S1.M2 |
| P1.S2.M2 | Settings → Workspaces: 2 toggles (Ledger / Splitly) w/ hide-when-empty guard + count message; Personal Finance has no toggle | OD | P1.S2.M1 |
| P1.S2.M3 | Rail UI: 2–3 cards (vector glyphs only) on landing screens; tap switches w/ haptic; show/hide by toggle; Splitly hidden until toggled ON | OD | P1.S2.M2 |
| P1.S2.M4 | Dock split per workspace (Finance `Home·Transactions·+·Accounts·Settings` / Ledger `Home·Transactions·+·Contacts·Settings` / Splitly `Home·Groups·+·Activity·Settings`) + menu pill bottom-right | OD | P1.S2.M3 |
| P1.S2.M5 | Floating menu pill (owner-merged 2026-11-05): QUICK LINKS (Search, Categories, Tags, Budgets, Reports, Planned Payments, Loans) + UTILITIES (Look & feel, Workspaces, Export / Import, Report bug -> repo issues, About with version/license); opens as a Flipkart-style pill card; dock Settings slot covers the old pill "Settings" item | OD | P1.S2.M4 |

### S3 — Subscriptions board (migrate + rebuild the UI)

| ID | Milestone | Gate | Depends |
|---|---|---|---|
| P1.S3.M1 | `PlannedPaymentRule` extended: `remainingOccurrences`, `icon`, `paymentMethodNote`, `cardOrMethod`; `PlannedPaymentPriceHistory` table; backup covers it | CI | P1.S1.M2 |
| P1.S3.M2 | Subscriptions list screen: month headline ("₹x /mo · ₹y /yr · N subscriptions"), filter chips, list rows w/ icon/method note/renew date/hike badge | OD+CI | P1.S3.M1 |
| P1.S3.M3 | Create/edit subscription sheet: name/amount/currency/cycle/start date/category/**7-day default lead (configurable 0/2/3/7/14)**/account/icon/card note/trial/end-after-N; **always manual Pay** | OD+CI | P1.S3.M1 |
| P1.S3.M4 | 30-day timeline + monthly-normalized cost + annual projection; legible for ₹18,000 rent and ₹649 OTT simultaneously; empty states | OD | P1.S3.M2 |
| P1.S3.M5 | WorkManager daily check: notifications for subscriptions (channel `subscriptions`, lead configurable; overdues flagged); single worker skips off-rules; user-tap opens rule screen | CI + OD | P1.S3.M4 |
| P1.S3.M6 | Price-hike detection: log every edit/mark-paid amount change; banner "+23% · old vs new"; trial-end alert w/ keep/cancel prompt (converts trial → recurring) | OD+CI | P1.S3.M3 |
| P1.S3.M7 | Pause / archive / restore / delete (name required) + **ending after N cycles** wiring into rule engine; existing Planned Payments untouched | CI | P1.S3.M3 |

### S4 — Wiring the whole P1 surface

| ID | Milestone | Gate | Depends |
|---|---|---|---|
| P1.S4.M1 | Backup v132 payload registered end-to-end; restore test includes Subscriptions + toggle state persists | CI | P1.S3.M7 |
| P1.S4.M2 | Subs widget on homescreen (glance/remote views): 3 nearest renewals + tap-to-app | OD | P1.S4.M1 |
| P1.S4.M3 | Full P1 device pass: onboarding unaffected; add 3 subs incl. trial; restore round-trip; toggles behave with counts | OD | P1.S4.M2 |
| P1.S4.M4 | LINT — Detekt clean on P1 modules; no new suppressions | CI | P1.S4.M3 |

---

## P2 — Ledger (KhataBook-style Udhaar)

> **Theme:** family-shop credit book with contacts, PDFs and WhatsApp pushes. Spec ref: `ivy-expand-plan.md` §6.

### S1 — Data layer

| ID | Milestone | Gate | Depends |
|---|---|---|---|
| P2.S1.M1 | `Loan.customerPhone`, `LoanRecord.recordPhone`; `loans` "LENT/BORROW" rows pre-filled via migrate; `personEditName` alias accepted; free-text legacy rows remain valid | CI | P1.S1.M2 |
| P2.S1.M2 | Contact picker (system picker, **no READ_CONTACTS**) reused for both new flows: create customer + add entry; manual entry always works | OD | P2.S1.M1 |
| P2.S1.M3 | Per-record due date (`recordDueDate`) + overdue filter + highlight in detail; reminders reuse `WorkManager` (channel `payments`) | OD+CI | P2.S1.M2 |
| P2.S1.M4 | Receipt attach per record (photo camera + gallery) → app-private `receipts/` → lazy cleanup on delete; visible in PDF | OD | P2.S1.M3 |

### S2 — Book screens

| ID | Milestone | Gate | Depends |
|---|---|---|---|
| P2.S2.M1 | Customer book split view (Taken / Given): per-person running total; filters All / Due / Overdue / Borrowed; search by name | OD | P2.S1.M2 |
| P2.S2.M2 | Customer detail: statement preview (last 20) + actions row (**WhatsApp · Call · Delete · Settle**) + receipt image | OD | P2.S1.M4 |
| P2.S2.M3 | Add/Edit entry sheet w/ amount keypad (reuse `AmountModal`) + category tags + optional "remind on" date switch | OD | P2.S1.M3 |
| P2.S2.M4 | Settle-all action from detail: deletes remaining records → opens shared "settle" sheet → writes EXPENSE/INCOME transaction tag `Settle` | OD | P2.S2.M3 |

### S3 — PDF statement

| ID | Milestone | Gate | Depends |
|---|---|---|---|
| P2.S3.M1 | `StatementRenderer` (shared with Splitly later): header (app + name + period) + table + net line; Helvetica / `INR` rendered as `Rs.` | CI | P2.S2.M4 |
| P2.S3.M2 | Date-range picker: last 3 months / this month / custom (From→To); preview in-app; share via FileProvider | OD | P2.S3.M1 |
| P2.S3.M3 | WhatsApp deep-link opens prefilled text with name+balance link-less summary; no permissions, user taps send | OD | P2.S2.M2 |

### S4 — Hardening

| ID | Milestone | Gate | Depends |
|---|---|---|---|
| P2.S4.M1 | `WorkManager` ledger sweep daily — notifies if any dues (channel `payments`, respects quiet hours) | OD | P2.S1.M3 |
| P2.S4.M2 | Backup covers Loans/attachments OFF by default; restore test on your device data copy | CI+OD | P2.S3.M2 |
| P2.S4.M3 | Full P2 device pass; lint clean | OD+CI | P2.S4.M2 |

---

## P3 — Splitly (Splitwise-style)

> **Theme:** group expense splits; full engine; settle-up also writes a real transaction. Spec ref: `ivy-expand-plan.md` §7.

### S1 — Split engine (pure Kotlin)

| ID | Milestone | Gate | Depends |
|---|---|---|---|
| P3.S1.M1 | `SplitEngine` (equal / equal-subset / exact / % / shares / adjustment); any combination round-trip equals total (property tests) | CI | P2.S4.M2 |
| P3.S1.M2 | Multi-payer shares combined with split engine; totals + hidden invariants (`Σ owed == total - 0`) | CI | P3.S1.M1 |
| P3.S1.M3 | FX at settle: bundled static rate table for 25 currencies; offline OK; online refresh only if user opts in (never required) | CI | P3.S1.M2 |
| P3.S1.M4 | `SimplifyBalances` greedy min-cash-flow → list of transfers; apply = `SplitSettlementEntity` rows + one `Transaction` per pair; `SettlementTest` covers both halves atomically | CI | P3.S1.M2 |

### S2 — Groups & members

| ID | Milestone | Gate | Depends |
|---|---|---|---|
| P3.S2.M1 | `Group` create/edit/archive; type chips; currency picker; members add/reorder; self-row marked; leave = archive row | OD | P1.S1.M2 |
| P3.S2.M2 | Split expense sheet: payer(s) picker, 5 mode chips, amounts derived live, sum validated at save; category shortlist + **+ New**; receipt attach; equal engine from subsets | OD | P3.S1.M2 |
| P3.S2.M3 | Expense detail: payer/date/note/participants/owed-lines; comment per member (public/private; author-only note); delete with confirm | OD | P3.S2.M2 |
| P3.S2.M4 | Group history list w/ month filter; "**Settle group**" button; membership audit rows (createdAt shows grey) | OD | P3.S2.M3 |
| P3.S2.M5 | Toggle-off guard holds: zero records → train shows switch; 1 record → hide blocked with message; toggle on → empty-state upsell shows | OD | P1.S2.M2 |

### S3 — Offline ledger crunchers

| ID | Milestone | Gate | Depends |
|---|---|---|---|
| P3.S3.M1 | Per-person group ledger screen: who owes whom each line; group-net header banner; "settled" items filtered out by default | OD | P3.S1.M4 |
| P3.S3.M2 | Simplify button: shows minimal-transfer plan (e.g., S→Q 2100, S→P 200, R→P 1500…); apply clears group to zero nets atomically; undo-dismissible | OD+CI | P3.S1.M4 |
| P3.S3.M3 | Backup v132 covers groups & splits (JSON) ✓; photos still opt-in | CI | P3.S2.M4 |

### S4 — Prove it

| ID | Milestone | Gate | Depends |
|---|---|---|---|
| P3.S4.M1 | Full flow on device with real-looking data: create Goa Trip → 3 members → Hotel 12,000 paid by you split equal → simplify → settle → balances zero | OD | P3.S3.M2 |
| P3.S4.M2 | Hide module → rail shows 2 cards; re-enable → 3 cards; toggling only at zero records | OD | P3.S4.M1 |
| P3.S4.M3 | LINT clean; backup restores onto clean install; APK size delta documented | CI+OD | P3.S4.M1 |

---

## P4 — Splitly refinements (post-P3, optional)

| ID | Milestone | Gate | Depends |
|---|---|---|---|
| P4.S1.M1 | Recurring group rules (rent/utilities) → auto-create SharedExpense; editions warn on stale amounts | CI | P3.S1.M1 |
| P4.S1.M2 | Persistent settle suggestions in group list ("X owes you ₹3,000 · nudge") | OD | P3.S3.M1 |

---

## P6 — A3 clean-room rewrite (post-Play, optional, MULTI-YEAR)

> Recorded 2026-11-04 after owner's decision: **A3 — closed-source rewrite later**. Not scheduled for build until Play is live and P1..P5 verified.

| ID | Milestone | Gate | Depends |
|---|---|---|---|
| P6.S1.M1 | A3 project-spec: inventory of every Ivy-derived file; clean-room protocol (public docs / behavioural tests only); provenance log | CI | P3.S4.M1 |
| P6.S1.M2 | Build your own remake track for one engine (SplitEngine) — parity tests, zero Ivy source reference | CI | P6.S1.M1 |
| P6.S1.M3 | Replace module-by-module: Base → Data → Domain → UI/Compose screens → build verification per module; CI runs both suites | CI | P6.S1.M2 |
| P6.S1.M4 | Clean-room release candidate: provenance log empty of Ivy-derived files; release signed with new key; `versionCode` bumps; Play listing updated | OD+CI | P6.S1.M3 |

---
---
## P5 — Phase 5+ (post-Play, opt-in)

| ID | Milestone | Gate | Depends |
|---|---|---|---|
| P5.S1.M1 | **Firebase invite links** — module-aware opt-in; token→group-snapshot read-only; backend degradation doesn't affect offline UX (airplane QA); invite mode user-controllable OFF by default | CI+OD | P3.S4.M1 |
| P5.S2.M1 | Hindi/Hinglish for Subscriptions + Ledger screens; English stays v1; all strings extracted; scripts review | OD | P2.S4.M3 |
| P5.S3.M1 | Scheduled local nudges weekly for uneven settlements; digest opt-in (notifications channel `groups`) | CI | P3.S1.M4 |
| P5.S4.M1 | Weekly email digest (**Pro**) — needs backend opt-in; infrequent (1/wk); no ads | CI | P5.S1.M1 |

---

## Appendix — Play gate (cut P4/P5 before Play, recommend join after P3 verified)

- PlayConsole: app name "Ivy Wallet" (rename only when Play verified); applicationId `com.cyberhive.wallet`; $25 dev fee already handled or not; AAB + release key locally; version-code 208+; staged 20% rollout.
- Reference: `ivy-expand-plan.md` §8–§9.

**Plan discipline:** any change to this file goes through you (APROVAL gate) — never silent, never stale.

*(File location: `C:\Users\ar54c\.opencode\plan\ivy-expand-plan\development-plan.md`)*
