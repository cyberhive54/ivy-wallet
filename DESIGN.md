# Ivy Expand — DESIGN.md (Splitly + Ledger workspaces)

> Direction contract (development-only). Mode: **Operate** — these are task screens
> people use daily to record money. Scanability, calm confidence, and native
> affordances outrank expression.

## THESIS
Money screens should read like a well-kept ledger, not a dashboard: a single calm
surface, one authoritative balance at the top, then a chronological record. Refuses
the "card grid of metrics + neon accent" look and the floating-metric hero.

## OWN-WORLD
Palette: near-black canvas (`UI.colors.pure`) with one indigo action accent
(`#5E5CE6`), a green receivable (`#30D158`), an amber payable (`#FF9F0A`), and a
red destructive (`#FF6B5B`). Surfaces: 1px hairline borders on 16–20dp radii,
never stacked shadows. Type: `ivyInter`, display 28sp ExtraBold with -0.4sp
tracking, body 15sp Medium, caption 12sp, numerals tabular. Motion: critically
damped springs (damping 1.0, response ~0.3s) for sheets/expansion; the only bounce
is on user-driven gestures (dismissible sheets). Reduced motion → cross-fade only.

## STORY
You can tell, in one glance, who owes whom and by how much; every action (add,
settle, comment) is a single tap from where you are; nothing hides the record.

## FIRST VIEWPORT
Workspace home: a single net-balance band (They owe | You owe), then a Filter row,
then the list. Group detail: title + subtitle, net band, three action pills, then
balances and expenses in one scroll. Sheets rise from the bottom on a dim scrim,
with a grabber and a sticky primary action.

## FORM
Code-led native Compose, positioned 1st of the structure set (native Operate screens
are built, not pictured). `seed: none (pinned)` — "use the apple design skills and
impeccable to redesign Splitly and Ledger completely".

## FINISH
unreviewed and undocumented is unfinished; this build ends with the finish review,
the verdict, DESIGN.md, and every shipping raster carrying its provenance.
