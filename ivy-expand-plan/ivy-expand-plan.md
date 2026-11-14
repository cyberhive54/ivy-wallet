# Ivy Expand — Plan Index & Spec (Personal · Ledger · Splitly)

> **Three companion files (all live in this folder):**
> - **`development-plan.md`** — the complete phase/stage/milestone breakdown (P0…P5) with gates
> - **`progress-track.md`** — the live checklist; never edit `development-plan.md` to make an ID easier
> - **this file** — vision · decisions · data model · UX · constraints · privacy policy
>
> **Status (2026-11-04, v3 — fully locked & structured):** every decision below was answered by you across Batches 1–8, then re-cut into the P/S/M system in `development-plan.md`.
> **Vision:** one offline-first Android app — personal money management + a KhataBook-style udhaar ledger + optional Splitwise-style groups. Legal: Apache/GPL-3.0, fork remains open source, Play Store later. App name stays **Ivy Wallet** for now.
> **Everything below is the contract.** Phases/stages/milestones are in `development-plan.md`; nothing ships without its Gate passing in `progress-track.md`.

---

## 1. Cross-cutting hard constraints

| # | Decision | Notes |
|---|---|---|
| C0–C5 | applicationId `com.cyberhive.wallet` (debug `.debug`); never `com.ivy.wallet` | fork's critical rule (see `GEMINI.md`) |
| D | **Distribution**: eventual Play Store; no paywall, no ads, no paywall/analytics; keystone for release signing is local-only; branch is `cyberhive54/update` | fork release-notes flow unchanged |
| L | **Play production rules** — no `SEND_SMS` (share-intent prefill only); no `READ_CONTACTS` (system contact picker); no UPI collect/QR (user rejected it) | reminders are user-tapped share sheets; in-app notifications only |
| F | **Offline-first privacy-first** — Room + DataStore is system of record | all money flows fully workable in airplane mode; only Phase 5+ invite sync requires internet |
| M | **GPLv3 preserved** — fork stays public; no SaaS backend in Phases 1–3 | don't land server-side copying of code |
| CSS | **Build constraints** — low-spec host; heap `-Xmx1536m`, `--max-workers=2`, `parallel=false`, `--stop` after every build; user pulls APK itself | APK stays debug-signed locally until release phase |
| G | **Version bump** — `version-name` unchanged until Play release review; `version-code` stays 207 until then | bump in release phase only |
| B | **No paywall / no tracking** — zero analytics, zero crash reporters; local crash files only | one notification worker; no external telemetry |
| B2 | **Backup** — all new tables must round-trip through JSON backup; photos opt-in only | photo restore excluded by default (size) |

**Distribution order:** Phase 1 → Phase 2 → Phase 3 → Phase 5+ (invite sync + language) — each phase is an installable debug APK; you install and test on-device; no Play listing until Phase 3 verified green. **Full milestone breakdown lives in `development-plan.md`.**

---

## 2. Decision log (2026-11-04 — all 100% locked)

### Workspace shell
| # | Answer |
|---|---|
| Rail | Personal Finance → Ledger → Splitly. **Rail shows on workspace home screens only** (inner screens have back/title chrome). Artwork = **bundled flat vector glyphs** (no external images) |
| Toggles | Splitly + Ledger individually hideable/unhideable from Settings (hide requires **zero records**; toggling-off blocked with count message). Personal Finance **always on** — no toggle |
| Group pay flow | You pay → **full amount debited** from your account in one transaction; friends' shares stay **inside Splitly ledger only** (never Loans, never your stats, never your account) |
| Settle-up | **BOTH** a splitly ledger settlement **and** a real `Transaction` on your account |
| 1:1 friends | **1:1 = 2-person groups** (reuse exact same engine, no separate flow, ever) |
| Notifications | **3 channels** — `subscriptions` / `payments` / `groups` (user-mutable in Settings) |
| Category for group expenses | Splitly expense sheet prompts for category: **predefined shortlist chips** + **"+ New" inline add**; global category reused (shared with personal finance; nothing group-scoped in v1) |

### Subscriptions
| # | Answer |
|---|---|
| Lead time | **Default 7 days**, configurable per subscription (0/1/2/3/7) |
| All six included | renewal reminders · bill calendar (30-day timeline + monthly-normalized cost + annual projection) · price-hike detection **with full price history** · free-trial expiry alerts · pause/cancel (archive, restorable) · **finite rules** ("ends after N cycles") |
| Dedicated board | Yes — new Subscriptions screen with monthly total headline & timeline |
| Cost model | Planned Payments engine **extended in place** — zero new money entities |
| Manual confirm | **Yes** — no auto-post, ever (tap Pay to book the transaction) |
| Icons & method | per-rule **icon** + free-text **card/method** note (e.g. `HDFC 4932`) — never PAN |
| Count/projection | monthly-normalized `amount × 12 / monthsPerCycle`; annual = ×12 |
| Trial | one-time rule with `trialEndDate`; alert fires; keep converts to recurring or archives |

### Ledger (KhataBook / udhaar)
| # | Answer |
|---|---|
| Two-way | They owe me **and** I owe (LENT/BORROW symmetric) |
| Book | Customer book screen w/ all entries, month or custom-range filter, **full ledger history PDF** w/ date-range picker |
| Doc | PDF via `PdfDocument` (Helvetica; `INR` shown as `Rs.`); share via FileProvider |
| Phone | **Required** on every customer entry — honoured as answered |
| Contact picker | System contact picker (no READ_CONTACTS) — prefills name+phone; or manual entry |
| Borrow direction | Same book, filtered; symmetric UI |
| Payments | WhatsApp/SMS **share** only; no UPI, no auto-collect, no GST/staff/inventory; amount manually entered; category irrelevant for settle |

### Splitly (Splitwise)
| # | Answer |
|---|---|
| Default OFF | Splitly module **hidden until toggled** in Settings; zero-records rule applies both ways |
| Scope | Groups + offline members (name + optional phone, contact picker prefill) — no sign-in, no multi-user yet |
| Split engine | **Full** — equal (all/subset), exact, percentages, **shares (1×/2×…)**, **adjustment (± deviation)**; single or **multiple payers**; combined with split engine |
| Multi-currency | **Per-expense currency + settle-time FX** at a bundled rate table (offline OK) |
| Receipts | Optional photo attach to group expenses (shared with all three workspaces) |
| Simplify | **Greedy min-cash-flow** behind a **"Simplify balances"** button |
| Settle-up | **Ledger + Transaction** both — friends' shares never hit accounts/loans |
| Comments | Any **group member** comments; visibility **public or private**; notes belong to the **entry author only** |
| Group recurring bills | **Excluded Phase 1–4**; lands in Phase 5+ together with Firebase invite |

### Distribution
| # | Answer |
|---|---|
| Branch | `cyberhive54/update` only; no PRs into trunk until Play push |
| Phase 1 | v132 Room migration + rail/shell + Subscriptions |
| Phase 2 | Ledger workspace on top of Loans |
| Phase 3 | Splitly — empty-state safe; offline whole |
| Phase 5+ | Firebase sync, Hindi/Hinglish, recurring bills, weekly recap (Pro) |
| Play push | only after Phase 3 acceptance on device |

---

## 3. App shell spec

### 3.1 Workspace model

```
enum class Workspace { FINANCE, LEDGER, SPLITLY }   // in shared/base

val LocalWorkspace = compositionLocalOf<Workspace>  // provided by RootActivity + IvyNavGraph

fun isEnabled(ws: Workspace, dataStore: DataStore<Preferences>): Boolean =
    when (ws) {
        FINANCE -> true                                // always on
        LEDGER  -> dataStore.get<Boolean>(KEY_LEDGER) ?: true   // on by default
        SPLITLY -> dataStore.get<Boolean>(KEY_SPLITLY) ?: false // OFF
    }
```

### 3.2 Settings → Workspaces

| Title | Content |
|-------|---------|
| Ledger workspace | Switch (when ON) · totals line "You'll get ₹X · You owe ₹Y" · hidden when 0 records |
| Splitly workspace | Switch (when OFF) · explainer · hidden when records > 0 (Snackbar w/ count) |

**Hide-when-empty rule (both toggles):** toggle OFF only when count == 0; ON always allowed (empty-state upsell). Personal Finance never renders this row.

### 3.3 Rail (landing screens only)

- 2–3 pills (Splitly hidden until enabled): **Personal Finance (wallet glyph) · Ledger (book glyph) · Splitly (split glyph)** — Inter, 14sp label, glyph ~56dp; active pill = elevated + accent ring; haptic tick on switch.

### 3.4 Workspace home screens

- **Finance** — today's cashflow + top categories + tap-through to Subscriptions board.
- **Ledger** — "YOU'LL GET ₹X · YOU OWE ₹Y" banner · two columns (Taken / Given) · search · **TOP 5 outstanding** cards.
- **Splitly** — groups list + net totals strip; empty states live here; FAB → new group / new expense.

### 3.5 Docks (liquid pill, five slots + FAB)

| Workspace | Slots (left→right) |
|-----------|--------------------|
| Personal Finance | Home · Transactions · **+** · Accounts · Settings |
| Ledger | Home · Transactions · **+** · Contacts · Settings |
| Splitly | Home · Groups · **+** · Activity · Settings |

- Menu action removed everywhere; the **floating pill bottom-right** handles: Settings, Look, Workspaces toggles, Export/Import JSON, Bug-report, About.
- Docks keep the fork's existing spring-pill GLASS + `IvyWalletLook`/`AccentFamilies` design language untouched.

---

## 4. Room schema (unified migration — `131 → 132` in one task)

`Migration131to132_ExpandSuite` (ALTER-only nullable columns + new tables, zero backfill):

```kotlin
ALTER TABLE `loans`               ADD COLUMN `customerPhone` TEXT DEFAULT NULL;
ALTER TABLE `loans`               ADD COLUMN `counterpartyName` TEXT DEFAULT NULL;
ALTER TABLE `loan_records`        ADD COLUMN `recordDueDate` INTEGER DEFAULT NULL;
ALTER TABLE `loan_records`        ADD COLUMN `recordPhone` TEXT DEFAULT NULL;
ALTER TABLE `loan_records`        ADD COLUMN `attachmentUrl` TEXT DEFAULT NULL;
ALTER TABLE `planned_payment_rules` ADD COLUMN `remainingOccurrences` INTEGER DEFAULT NULL;
ALTER TABLE `planned_payment_rules` ADD COLUMN `icon` TEXT DEFAULT NULL;
ALTER TABLE `planned_payment_rules` ADD COLUMN `paymentMethodNote` TEXT DEFAULT NULL;
ALTER TABLE `planned_payment_rules` ADD COLUMN `cardOrMethod` TEXT DEFAULT NULL;
CREATE TABLE `planned_payment_price_history` (...);
CREATE TABLE `groups`             (...);
CREATE TABLE `group_members`      (...);
CREATE TABLE `split_expenses`     (...);
CREATE TABLE `split_expense_participants` (...);
CREATE TABLE `split_settlements`  (...);
CREATE TABLE `split_comments`     (...);
```

**Schema discipline:** `exportSchema=true` — commit `132.json`; `Migration131to132Test` added to `androidTest`.

### 4.1 New entities (complete)

```kotlin
@Serializable @Entity(tableName = "planned_payment_price_history")
data class PlannedPaymentPriceHistoryEntity(
    @PrimaryKey val id: UUID, val ruleId: UUID, val amount: Double,
    val effectiveFrom: String, val source: String = "created",
)

@Serializable @Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: UUID, val name: String, val type: String = "TRIP",
    val currency: String = "INR", val archived: Boolean = false, val createdAt: Long,
)

@Serializable @Entity(tableName = "group_members")
data class GroupMemberEntity(
    val groupId: UUID, val memberName: String,
    val memberIsYou: Boolean = false, val memberColor: Int = 0, val memberPhone: String? = null,
    @PrimaryKey val id: UUID = UUID.randomUUID(),
)

@Serializable @Entity(tableName = "split_expenses")
data class SplitExpenseEntity(
    val groupId: UUID, val paidByMemberId: UUID, val amount: Double, val currency: String,
    val note: String?, val date: Long, val category: UUID?, val receiptAttachment: String?,
    val settled: Boolean = false, @PrimaryKey val id: UUID = UUID.randomUUID(),
)

@Serializable @Entity(tableName = "split_expense_participants")
data class SplitExpenseParticipantEntity(
    val expenseId: UUID, val memberId: UUID, val shareMode: String,
    val shareValue: Double?, val owedAmount: Double,
    @PrimaryKey val id: UUID = UUID.randomUUID(),
)

@Serializable @Entity(tableName = "split_settlements")
data class SplitSettlementEntity(
    val groupId: UUID, val fromMemberId: UUID, val toMemberId: UUID, val amount: Double, val currency: String,
    val linkedTransactionId: UUID, val createdAt: Long,
    @PrimaryKey val id: UUID = UUID.randomUUID(),
)

@Serializable @Entity(tableName = "split_comments")
data class SplitCommentEntity(
    val expenseId: UUID, val authorMemberId: UUID, val text: String, val createdAt: Long,
    val visibility: String = "PUBLIC", // PUBLIC | PRIVATE (author only)
    @PrimaryKey val id: UUID = UUID.randomUUID(),
)
```

### 4.2 Backup — full round-trip promise

`BackupDataUseCase` serialises every new entity; photos remain OFF the wire by default; round-trip test re-runs identical DAC collections.

---

## 5. Subscriptions (Phase 1) — spec

### 5.1 Subscriptions board screen

```
₹1,316.00 / mo · ₹15,792.00 / yr                              + Add
──────────────────────────────────────────────────────────────
{All · Rents · Utilities · Plans · OTT}  (filter row)
──────────────────────────────────────────────────────────────
This month
  Netflix   Annual            ₹999.00     next 12 Jan
  Spotify   Premium · Auto    ₹149.00     next 05 Dec   ⚑ hike  (+17%)
  Rent      ×12               ₹18,000.00  next 01 Dec
  Gym       · card note        ₹1,200.00   next 25 Nov
──────────────────────────────────────────────────────────────
30-day timeline
  25 Nov  ₹1,200  Gym
  01 Dec  ₹18,000 Rent
  05 Dec  ₹149    Spotify
```

### 5.2 Sub creation sheet

Fields: **Name · Amount · Currency · Cycle · Start date · Next charge · Reminder lead (7 default) · Account · Category · Icon · Card note (optional) · Trial toggle · End after N cycles (optional) · Notes** — auto-post never exists; always manual Pay.

### 5.3 Notifications

`WorkManager` daily job; notifications fire per `Reminders` spec (channel `subscriptions`); deep-link to rule; actions: **Mark paid / Skip this cycle / Remind tomorrow / Cancel**.

### 5.4 Data retention

Archived rows live in "Past" indefinitely; hard delete requires account-name confirmation.

---

## 6. Ledger (Phase 2) — spec

### 6.1 Customer book

```
You owe           You’ll get
Sir          ₹4,200      Ramesh      ₹8,400
Motorola     ₹3,000      Mom.         ₹1,000
Search: ____________  [Add customer]
```

- States: **Overdue X days / Due in Y days / No date / All** · swipe: call / WhatsApp · tap → inline edit + settle

### 6.2 Customer detail

Avatar/name · totals (lent · received · owed now · last entry) · receipt attachment · WhatsApp / Dial / Statement PDF / Delete all.

### 6.3 PDF statement

Header **"Statement of account"** · period (last 3 months / this month / custom range) · table with running balance · net line in bold · `PdfDocument` (Helvetica; `Rs.` for INR) · FileProvider share.

### 6.4 Reminders

Daily 09:00 (configurable 06:00–22:00 in Settings); tap → ledger book at the overdue person's row; inline action: WhatsApp text / Settle / Tomorrow.

### 6.5 Receipts

Camera picker (`TakePicture`, FileProvider) → stored app-private; rendered in detail + in PDF; shared uploader with Personal Finance and Splitly boards.

---

## 7. Splitly (Phase 3) — spec

### 7.1 Landing

```
You’ll get ₹4,000 · You owe ₹1,200
```

Groups: card w/ balance ("Goa Trip · you're owed ₹6,400"), last expense line, FAB → new expense.

### 7.2 Split form

```
Goa Trip dinner · 12 Oct · ₹4,800
Paid by [You ▼] [+Add payer]   Currency [INR ▼]
Split between: [☑ You ☑ Rahul ☑ Priya ☑ Dev]
Amount per share  ₹1,200  (equal) | [Share ₹] / [%] / [$$] / [×]
[Add receipt photo]  [Save]
```

- Modes cycle (all sum to total): equal (all/subset) → exact → % → shares → adjustment.
- Live-computed; mismatch impossible via UI; validation at Save.

### 7.3 Simplify engine

`SimplifyBalances` — greedy min-flow; "Apply this plan — replaces ledger with one settle per pair"; cancel-safe.

### 7.4 Comments

- Any member of the group can comment; per-comment **Public / Private**.
- Notes belong to the **author** (who entered the record).
- True cross-device sync = **Phase 5+ (Firebase)**.

### 7.5 Settlement

- Pick/split-settle; writes **both** `split_settlements` + `Transaction`; balances update atomically.

### 7.6 Group creation — no invites in v1

Groups offline; joinable via manual add/contact picker; leave = archived row preserved.

### 7.7 Deferred — Phase 5+

Recurring group bills and invite links explicitly out of v1 (Ice pattern: revisit on Phase review).

### 7.8 Category chips

Predefined shortlist + **+ New**; categories remain **global** (shared with Personal Finance); CSV-clean.

---

## 8. Backup wiring — explicit

- JSON payload serialises every new entity
- `exportSchema=true` — commit `132.json`
- Legacy backup → import works fine (absent rows tolerated)
- CSV export adds `Group`/`Split` columns; import unaware of splits (documented)

---

## 9. Notifications — one worker, three channels

- `WorkManager` daily job (battery-flexible).
- **Channels:** `subscriptions` / `payments` / `groups` — mutable in Settings → Notifications; global quiet hours 22:00→07:00 by default (user-editable).
- No network calls; Room-only reads; DND respected.

---

## 10. Locked-decisions summary

| Area | Decision |
|---|---|
| Distribution | Fork → eventually Play; local signing; branch `cyberhive54/update` |
| Toggles | Ledger on by default · Splitly off; both hideable only at zero records; Finance always on |
| Group pay model | Full amount debited once; friends tracked in Splitly ledger only |
| Settlement | SettlementEntity + Transaction both |
| Subs | Extended Planned Payments; manual always; 7-day default; hike detection + history |
| PDF | Helvetica (`Rs.`); FileProvider; full history or custom range |
| Book | Lent/borrowed symmetric; contact picker; required phone; WhatsApp reminder |
| Engine | Equal subset/exact/%/shares/adjustment; multi-payer; settle-time FX |

---

## 11. Privacy policy (Appendix A · to host at GitHub Pages)

Hosted at `https://cyberhive54.github.io/ivy-wallet/` (or org/pages path you prefer).

**About this app**

An offline-first personal finance workspace (budgets, a lending ledger, group expense splits). Everything you enter is stored only on your device. Open-source under GNU GPLv3 (`github.com/cyberhive54/ivy-wallet`).

**What data we collect**

None. No analytics SDKs, no ad networks, no crash-reporting service of our own, and no account registration. Nothing leaves your device unless you explicitly share it (system share sheet).

**How data is stored**

- Transactions, accounts, categories, loans, groups — local SQLite (`Room`)
- Backup files — written to whatever folder you pick (cloud drive / photos / local storage)
- Receipts — stored in app-private storage; never uploaded

**Sharing**

Only actions you tap: system contact picker, `ACTION_SEND` (WhatsApp/SMS/call), `tel:` intent.

**Third parties**

None. No Firebase, no ads, no pixels, no cookies.

**Your rights**

Your data sits on your device. Uninstalling deletes what you haven't backed up. Source on GitHub.

**Changes**

Changes ship with the source; commits visible in git log.

**Contact** — issue tracker on the public fork (GitHub).

---

## 12. Explicit rejections (this plan is the complete contract — anything not here is out)

- Cloud sync · QR invite in v1 · UPI/QR collect · auto bank import/SMS import · ads · paywall
- Multi-user/sign-in · staff roles · GST/inventory/invoice split · goal-based savings/investing tracking
- Email digest (deferred) · Hindi (deferred) · Multi-currency conversion beyond settle-time rates
- EXACT/Decimal precision beyond the 5 split modes listed · Sponsored category injection

---

*This file is the index + spec. **Execution plan lives in `development-plan.md`; live status in `progress-track.md`.***
