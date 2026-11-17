# Manual device test checklist (P1 + P2 + P3)

Build: `app/build/outputs/apk/debug/app-debug.apk` (applicationId `com.cyberhive.wallet.debug`)
Install: `adb -s <device> install -r app\build\outputs\apk\debug\app-debug.apk`
Device: V23 (Android 14). Keep awake: `adb -s <device> shell settings put system screen_off_timeout 2147483647`

> ADB text-entry tip: if a Compose text field doesn't receive `input text`, tap the field
> first, and dismiss the keyboard with the keyboard's own "done" key rather than the
> system BACK key (BACK closes the containing sheet). If typing is unreliable, use the
> on-screen keyboard or NectarLink's real touch input.

## P0 — Build / migration

- [ ] App installs over the previous debug build without "app not installed" (signature stable).
- [ ] Launch from the launcher — no crash, no Room "cannot verify data integrity" error.
- [ ] DB version is 133 (upgrade 131 → 132 → 133 validated at startup).

## P1 — Workspace shell + Subscriptions

### Shell / dock / rail
- [ ] Rail shows 3 cards (Finance / Ledger / Splitly); tapping switches workspace in place.
- [ ] Dock is per-workspace: Finance `Home·Transactions·+·Accounts·Settings`, Ledger `…·Contacts·…`, Splitly `Home·Groups·+·Activity·Settings`.
- [ ] FAB sits in its own slot, never overlaps labels; labels are single-line.
- [ ] Floating pill (bottom-right) opens Quick Links + Utilities panel; tapping outside closes it.
- [ ] Radial + menu blooms with Income / Expense / Transfer + Planned Payment capsule; scrim tap closes.

### Subscriptions board
- [ ] Open a subscription (e.g. via Finance → Subscriptions). Headline shows monthly / annual totals.
- [ ] 30-day timeline renders; a rule with a trial shows the trial badge + expiry alert.
- [ ] Price-hike rule shows the old → new banner; price history is retained after edit.
- [ ] Pause a rule: it dims and drops out of totals/timeline/reminders; unpause restores it.
- [ ] Archive a rule: hidden from board; Restore brings it back.
- [ ] Create a rule: pick icon, method note, lead days, trial + end date, "ends after N cycles" — save and verify it appears.
- [ ] Delete requires a name confirmation.

### Reminders / widget
- [ ] SubscriptionsReminderWorker fires near a rule's lead time (channel `subscriptions`); tap opens the rule editor.
- [ ] Add the Subscriptions widget to the home screen; it shows the 3 nearest renewals; tap opens the editor.

## P2 — Ledger (Udhaar)

- [ ] Ledger workspace shows YOU'LL GET / YOU OWE banner and top outstanding people.
- [ ] Create a customer: system contact picker prefills name + phone (no READ_CONTACTS permission prompt); manual entry also works.
- [ ] Add an entry: amount keypad, contact picker, receipt via camera/gallery (app-private, no gallery pollution), "Remind me on this date" switch gates the due date.
- [ ] Overdue entry shows red "Overdue by N days"; All/Due/Overdue filters work.
- [ ] LedgerReminderWorker fires daily 09:00 (channel `payments`), respects quiet hours, tap opens Ledger.
- [ ] Customer detail: GIVEN/RECEIVED/ENTRIES totals, statement preview, receipt thumbnails.
- [ ] WhatsApp action opens wa.me with prefilled summary; Call opens dialer.
- [ ] Statement screen: This month / Last 3 months / Custom From→To; preview + net line; Share PDF opens the system share sheet.
- [ ] Settle-all writes a real Transaction titled "Settle - <name>" (INCOME if they owe you, EXPENSE if you owe) and clears entries/receipts.
- [ ] Delete customer removes entries + receipt files.

## P3 — Splitly (new)

### Groups / members
- [ ] Splitly workspace empty-state safe (a fresh enable shows the upsell).
- [ ] Create a group (name, type chip TRIP/HOME/COUPLE/OTHER, currency).
- [ ] Open group detail: header (name/currency/member count), balance banner, action chips.
- [ ] Members sheet: add member (name or contact picker), reorder ↑/↓, "You" row marked, member joined-date shows grey (audit row).
- [ ] Leave group archives your member row (history preserved); the group's member count drops.
- [ ] Group edit sheet: rename, change type/currency, Archive group (hidden from list, data preserved).

### Split expense sheet
- [ ] Add expense: enter amount + note; payer chips (multi-payer with amounts).
- [ ] Add 2–3 members; participant list shows all with live owed amounts that always sum to the total.
- [ ] Cycle all 5 modes on a member — EQUAL / EXACT / PERCENT / SHARES / ADJUST — and confirm owed lines recompute live and still sum to total.
- [ ] Toggle participants on/off (subset split) — excluded member gets no owed line.
- [ ] Category chips shortlist + "+ New"; receipt attach (camera/gallery).
- [ ] Save — expense appears in group history with payer count and amount.

### Balances / simplify / settle
- [ ] Per-person balances list shows each member's signed net; group-net header banner matches.
- [ ] Tap an expense → detail: payer(s), date, note, participants/owed lines.
- [ ] Add a comment as PUBLIC, then a PRIVATE one; toggle visibility; delete the expense with confirm.
- [ ] Press Simplify → minimal transfer plan lists the fewest transfers.
- [ ] Apply plan → split_settlements rows + real Transactions are written; refresh shows balances at zero.
- [ ] Activity tab aggregates expenses and settlements across groups.

### Guard / backup
- [ ] Settings → Workspaces: try to hide Splitly while a group exists → blocked with "Keep your data first" + record count.
- [ ] After deleting all groups, hiding Splitly succeeds; the rail drops to 2 cards; re-enable restores the empty-state upsell.
- [ ] Export JSON backup; verify payload includes groups, groupMembers (archived/createdAt), splitExpenses, splitExpensePayers, participants, settlements, comments.
- [ ] Restore the backup onto a clean install — all records + workspace toggles come back; receipt photos remain opt-in.

## Acceptance
- [ ] No crashes across the whole pass (watch `adb logcat` for FATAL / AndroidRuntime).
- [ ] All three workspaces reachable and their data intact after the backup round-trip.
