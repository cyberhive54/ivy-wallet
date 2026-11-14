# Ivy Expand — Progress Track

**Companion:** `development-plan.md` (full spec + gates) · `ivy-expand-plan.md` (decisions + rationale)
**File status:** LIVE — this is the only file updated during development. Never edit `development-plan.md` casually to "make an ID easier" — renegotiate with yourself explicitly.
**Owner:** `cyberhive54/update` · **Test APK:** `app/build/outputs/apk/debug/app-debug.apk` (never commit)

## Legend

| Mark | Meaning |
|---|---|
| `☐` | Not started |
| `◐` | In progress |
| `✓` | Done (Gate passed) |
| `⏸` | Blocked (own note: waiting on X) |
| `~` | Partial (something works, something's broken — note what) |
| `✖` | Skipped intentionally (note why) |
| `→` | Moved to another milestone (note new ID) |

**Rules**
- One header row per phase; columns: ID · Milestone (short) · Gate · Status · Done on · Notes.
- Never delete rows; mark `✖` or `→` instead.
- "Done" requires its **Gate** pass exactly as defined in development-plan.md.
- Update immediately on state change — never batch-update at end of week.

---

## P0 — Workspace & build hygiene

| ID | Milestone | Gate | Status | Done on | Notes |
|---|---|---|---|---|---|
| P0.S1.M1 | Branch audit | CI | ✓ | 2026-11-05 | cyberhive54/update, synced; only expected files pending (GEMINI.md, ivy-expand-plan/) |
| P0.S1.M2 | applicationId check | CI | ✓ | 2026-11-05 | com.cyberhive.wallet + .debug suffix; never com.ivy.wallet |
| P0.S1.M3 | Low-spec build policy recorded + assembleDebug verified on host | OD | ✅ | 2026-11-14 | assembleDebug green on host 2026-11-07..14; heap must be 3072m (1536m OOMs KSP/Room) - GEMINI.md updated; APK installed + verified on device V23 |
| P0.S1.M4 | Versioning policy set | CI | ✓ | 2026-11-05 | versionCode 207 / versionName 2026.10.22 unchanged until P3 gate; bump to ≥208 at release |
| P0.S1.M5 | Privacy page drafted | OD | ✓ | 2026-11-05 | docs/privacy/index.html — public-fork signpost; needs GitHub Pages source = /docs (owner action) |

---

## P1 — Workspace shell + Subscriptions

### S1 — Room v131 → v132

| ID | Milestone | Gate | Status | Done on | Notes |
|---|---|---|---|---|---|
| P1.S1.M1 | Migration file (nullable cols + new tables) | CI | ✅ | 2026-11-14 | Migration131to132_ExpandSuite compiles; entity<->legacy round trip lossless for all v132 columns; DDL certification fixed (defaults/indexes/groupRecurringRuleId) |
| P1.S1.M2 | 132.json exported; real-device upgrade | OD | ✅ | 2026-11-14 | 132.json exported + committed (v132, 19 entities); real 131->132 upgrade verified on V23 with existing data; app launches clean |
| P1.S1.M3 | androidTest: backup round-trip | CI | ~ |  | Seeded round-trip androidTest written (backup132_expand_suite_roundtrip: v132 rows + workspace toggles); connected run still pending |
| P1.S1.M4 | androidTest: migration test | CI | ☐ | | |

### S2 — Workspace shell, rail, docks

| ID | Milestone | Gate | Status | Done on | Notes |
|---|---|---|---|---|---|
| P1.S2.M1 | Workspace enum + switching | CI | ~ | 2026-11-14 | Workspace enum + ctx switch/coerce/init/reset; dock content swaps per workspace; verified on device |
| P1.S2.M2 | Settings toggles w/ count guard | OD | ~ | 2026-11-14 | Settings > Workspaces section with live count guard; toggles not yet exercised under real data on device |
| P1.S2.M3 | Rail UI | OD | ~ | 2026-11-14 | Rail on workspace home tabs; device-verified (Finance/Ledger/Splitly cards visible) |
| P1.S2.M4 | Dock split per workspace | OD | ~ | 2026-11-14 | Per-workspace dock (Finance/Ledger/Splitly) + FAB in its own reserved slot + inline pill; labels single-line, no overlap (measured via uiautomator); ledger/splitly dock variants not yet device-verified |
| P1.S2.M5 | Floating menu pill | OD | ~ | 2026-11-14 | Pill lives INSIDE the dock row (Flipkart-style, 46dp gradient circle + seam) - owner screenshot request; panel = Quick Links + Utilities; device-verified (panel opens, 0 crashes) |

### S3 — Subscriptions board

| ID | Milestone | Gate | Status | Done on | Notes |
|---|---|---|---|---|---|
| P1.S3.M1 | Schema extension + backup | CI | ~ | 2026-11-14 | planned_payment_rules += remainingOccurrences/icon/paymentMethodNote/cardOrMethod/isTrial/trialEndsAt/reminderLeadDays/paused/archived; price-history table; backup covers all + workspace toggles |
| P1.S3.M2 | Subs list screen | OD+CI | ~ | 2026-11-14 | Board delivered (headline, chips, rows, badges, 30-day timeline, archived segment, empty states); not yet exercised with real subs on device |
| P1.S3.M3 | Create/edit subscription sheet | OD+CI | ~ | 2026-11-14 | Dedicated editor: icon palette, method note, lead 0/2/3/7/14, trial + end date, ends-after-N, manage row (pause/archive); price-history write-back on final save |
| P1.S3.M4 | Timeline + projection | OD | ~ | 2026-11-14 | 30-day timeline + monthly-normalized/annual projection implemented (legibility re-check pending with real data) |
| P1.S3.M5 | WorkManager reminders | CI+OD | ~ | 2026-11-14 | SubscriptionsReminderWorker (daily 09:30, per-rule lead, trial-aware, skips paused/archived/exhausted) + channel subscriptions + deep link to rule; not yet fired on device |
| P1.S3.M6 | Price-hike + trial alerts | OD+CI | ~ | 2026-11-14 | Hike banner (old->new) + row badges + expired-trial keep/cancel prompt (converts trial -> paid, logs history) |
| P1.S3.M7 | Pause / archive / delete | CI | ~ | 2026-11-14 | Pause (dimmed, zero totals) / Archive+Restore / name-required delete; remainingOccurrences caps the planned-payments generator |

### S4 — Whole-surface wiring

| ID | Milestone | Gate | Status | Done on | Notes |
|---|---|---|---|---|---|
| P1.S4.M1 | Backup v132 round-trip | CI | ~ |  | Export/restore wired for all v132 tables + sharedPrefs toggles; seeded androidTest pending connected run |
| P1.S4.M2 | Subs widget | OD | ~ | 2026-11-14 | Glance widget: 3 nearest renewals, tap -> rule editor, refresh on app entry; not yet added to a home screen on device |
| P1.S4.M3 | Full P1 device pass | OD | ~ | 2026-11-14 | DONE so far on V23: launch, 131->132 upgrade, dock tabs, pill panel, radial add menu, crash fixes (transactions tab, migration DDL). REMAINING: subs CRUD on device, toggles under load, backup export/import round-trip, widget on home, reminder firing |
| P1.S4.M4 | LINT - Detekt clean on P1 modules | CI | ~ |  | New code is Detekt-clean by construction (no suppressions added); full Detekt run pending a build slot |

---

## P2 — Ledger (Udhaar)

### S1 — Data layer

| ID | Milestone | Gate | Status | Done on | Notes |
|---|---|---|---|---|---|
| P2.S1.M1 | Columns + rows preserved | CI | ~ | 2026-11-14 | Loan.customerPhone/LoanRecord.recordPhone carried through legacy datamodels (lossless toEntity); person name kept as alias (counterpartyName) with legacy name preserved; migration pre-fills counterpartyName from name; compiles (legacy-code + feature:loans) |
| P2.S1.M2 | Contact picker integration | OD | ~ | 2026-11-14 | System picker (ACTION_PICK, no READ_CONTACTS) in legacy-code ContactPicker: reusable rememberContactPicker + ContactPickButton; wired into create-customer (name+phone) and add-entry (record phone) modals; manual entry always works. Device verification pending |
| P2.S1.M3 | Per-record due date + overdue filter + reminders | OD+CI | ~ | 2026-11-14 | recordDueDate carried through legacy datamodel + entry modal (Due date row, system date picker, clearable); ledger workspace shows All/Overdue filter + red "Overdue by N days" badge; LedgerReminderWorker (daily 09:00, channel payments, quiet hours 22-07 respected, per-person grouping, tap -> Ledger workspace deep link); compiles |
| P2.S1.M4 | Receipt attach | OD | ~ | 2026-11-14 | ReceiptStorage: app-private filesDir/receipts (relative path in DB, no gallery pollution); entry modal attaches via camera (TakePicture + FileProvider, added fileprovider + file_paths.xml) or gallery (GetContent); delete path removes the file lazily; PDF rendering of receipts comes with P2.S3; compiles |

### S2 — Book screens

| ID | Milestone | Gate | Status | Done on | Notes |
|---|---|---|---|---|---|
| P2.S2.M1 | Customer book split view (Taken / Given) | OD | ~ | 2026-11-14 | Ledger workspace landing rebuilt as the book: search by name, Youll get / You owe split with per-side totals, filters All / Due / Overdue, per-person outstanding + overdue badge; contacts tab keeps the full list. No add-customer sheet yet (comes with M3), entry still via the fork modal |
| P2.S2.M2 | Customer detail + actions row | OD | ~ | 2026-11-14 | New ledger customer detail screen (route + VM): avatar/name, outstanding, GIVEN/RECEIVED/ENTRIES totals, statement preview (last 20 entries), receipt thumbnails (Coil, app-private file), action row WhatsApp (wa.me prefilled) / Call (ACTION_DIAL) / Statement (PDF lands in P2.S3) / Delete all (removes entries + receipts). Compiles; device pass pending |
| P2.S2.M3 | Add/Edit entry sheet | OD | ~ | 2026-11-14 | Entry modal: AmountModal keypad + contact picker + receipt (camera/gallery) + "Remind me on this date" switch gating the due date; category tags on entries still open (fork tags are transaction-scoped) - noted as follow-up |
| P2.S2.M4 | Settle-all action | OD | ~ | 2026-11-14 | Settle chip on customer detail -> settle sheet (amount + direction) -> writes a real Transaction (INCOME when they owe you, EXPENSE when you owe) titled "Settle - <name>" linked to the loan, then clears entries + receipts; settle account = loan account or first account |

### S3 — PDF statement

| ID | Milestone | Gate | Status | Done on | Notes |
|---|---|---|---|---|---|
| P2.S3.M1 | StatementRenderer (shared) | CI | ~ | 2026-11-14 | shared/domain StatementRenderer: PdfDocument A4, Helvetica, header + period + table + bold net line, multi-page, writes to cache; reusable by Splitly later |
| P2.S3.M2 | Date-range picker + share | OD | ~ | 2026-11-14 | Statement screen: This month / Last 3 months / Custom (From-To pickers), in-app preview + net line, Share PDF via FileProvider (fileprovider + file_paths.xml added) |
| P2.S3.M3 | WhatsApp deep-link | OD | ~ | 2026-11-14 | Customer detail WhatsApp action opens wa.me with a prefilled balance summary (no permissions, user taps send); link-less summary text |

### S4 — Hardening

| ID | Milestone | Gate | Status | Done on | Notes |
|---|---|---|---|---|---|
| P2.S4.M1 | `WorkManager` daily sweep | OD | ☐ | | |
| P2.S4.M2 | Backup covers Loans | CI+OD | ~ | 2026-11-14 | Loans/loan records (incl. v132 phone/due/receipt columns) already in the v132 backup payload; receipt photos stay out of the backup by default (app-private, not exported) |
| P2.S4.M3 | Full P2 pass | OD+CI | ☐ | | |

---

## P3 — Splitly

### S1 — Split engine (pure Kotlin)

| ID | Milestone | Gate | Status | Done on | Notes |
|---|---|---|---|---|---|
| P3.S1.M1 | Equal/exact/%/shares/adjustment | CI | ~ | 2026-11-15 | SplitEngine (shared/domain, pure Kotlin): EQUAL / EXACT / PERCENT / SHARES / ADJUSTMENT + subset support; exact-to-the-cent guarantee via leftover-cent distribution; 16 unit tests green (kotest property + round-trip) |
| P3.S1.M2 | Multi-payer + invariants | CI | ~ | 2026-11-15 | multiPayerBalances(): paid-minus-owed per member, group net always zero; property test over random payer sets |
| P3.S1.M3 | FX at settle (static table) | CI | ~ | 2026-11-15 | FxTable: bundled static rates for 25 currencies (USD base), offline-only; unknown currency falls back 1:1; settle-time conversion used by GroupLedger |
| P3.S1.M4 | Simplify greedy + settle dual-write | CI | ~ | 2026-11-15 | SplitEngine.simplify() greedy min-cash-flow; SplitSettlementUseCase writes split_settlements row + a real Transaction per pair (only pairs involving the local user), with rollback if the ledger write fails |

### S2 — Groups & members

| ID | Milestone | Gate | Status | Done on | Notes |
|---|---|---|---|---|---|
| P3.S2.M1 | Create/edit group + members | OD | ☐ | | |
| P3.S2.M2 | Split sheet; validation live | OD | ☐ | | |
| P3.S2.M3 | Expense detail + comments | OD | ☐ | | |
| P3.S2.M4 | History + Settle group button | OD | ☐ | | |
| P3.S2.M5 | Toggle-off guard holds | OD | ☐ | | |

### S3 — Ledger crunchers

| ID | Milestone | Gate | Status | Done on | Notes |
|---|---|---|---|---|---|
| P3.S3.M1 | Per-person group ledger | OD | ☐ | | |
| P3.S3.M2 | Simplify button + apply → zero | OD+CI | ☐ | | |
| P3.S3.M3 | Backup v132 covers splits | CI | ☐ | | |

### S4 — Prove it

| ID | Milestone | Gate | Status | Done on | Notes |
|---|---|---|---|---|---|
| P3.S4.M1 | Goa Trip clean flow | OD | ☐ | | |
| P3.S4.M2 | Hide/unhide module | OD | ☐ | | |
| P3.S4.M3 | LINT + size audit | CI+OD | ☐ | | |

---

## P4 — Splitly refinements (optional)

| ID | Milestone | Gate | Status | Done on | Notes |
|---|---|---|---|---|---|
| P4.S1.M1 | Recurring group rules | CI | ☐ | | |
| P4.S1.M2 | Nudge suggestions | OD | ☐ | | |

---

## P5 — Phase 5+ (post-Play, opt-in)

| ID | Milestone | Gate | Status | Done on | Notes |
|---|---|---|---|---|---|
| P5.S1.M1 | Firebase invites (opt-in) | CI+OD | ☐ | | |
| P5.S2.M1 | Hindi/Hinglish LA | OD | ☐ | | |
| P5.S3.M1 | Weekly nudges | CI | ☐ | | |
| P5.S4.M1 | Email digest (Pro) | CI | ☐ | | |

---

## Scoreboard

| Section | Done | / Total | % |
|---|---|---|---|
| P0 | 4 | 5 | 80% |
| P1 | 0 | 20 | 0% |
| P2 | 0 | 13 | 0% |
| P3 | 0 | 13 | 0% |
| P4 | 0 | 2 | |
| P5 | 0 | 4 | |
| **TOTAL** | **4** | **57** | **7%** |

*(Update counts at each milestone boundary. If anything moves, the totals must move too — never hand-edit freely.)*

---

## How to update this file (30 seconds)

1. Before starting work on an ID, mark it `◐`.
2. Gate pass? → `✓`, set `Done on` + one-liner ("v132 verified on device with 12 records").
3. Blocked? → `⏸` + reason + what you need.
4. Got part-way and your brain switched tasks → `~` + what works / what's broken.
5. Skipped for good → `✖` + why (don't lie — it bites later).
6. Decided a different design → `→` old-ID LOST; new ID reflects the new decision; never overwrite entries.
7. Commit alongside the code: `docs(expand): progress-track @ P1.S2.M3 ✓`
