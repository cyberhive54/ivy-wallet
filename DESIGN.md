# Ivy Expand — DESIGN.md (Finance, Ledger, Splitly)

> Direction contract (development-only). Mode: **Operate** — these are task screens
> people use daily to record money. Scanability, calm confidence, and native
> affordances outrank expression.

## THESIS
Money screens should read like a well-kept ledger, not a dashboard: a single calm
surface, one authoritative balance, then a chronological record. Refuses the card
grid of metrics, the floating-metric hero, and the purple-on-near-black template.

## QUALITY PASS (2026-11-18)
The first redesign applied a token layer onto existing layouts. It read as generic
because it changed surface, not hierarchy. Rejected tells, not to be reintroduced:
- press-scale on every card and pill (motion must communicate state, not fidget)
- tracked all-caps section labels; use sentence case
- single-letter avatars in tinted circles; identity lives in the name
- two competing ExtraBold hero numerals; hierarchy comes from scale and spacing
- colored shadows and gradient button fills; flat hairline surfaces only
- one accent smeared across section links, icons and empty states; **use it once**
  for the single primary action on a screen

## OWN-WORLD (revised)
Palette: near-black canvas (`UI.colors.pure`) with one indigo accent (`#5E5CE6`)
reserved for the primary action, green (`#30D158`) for money received, amber
(`#FF9F0A`) for money paid, and red (`#FF6B5B`) for destructive actions only.
Surfaces: neutral translucent fill with a 1px hairline border on 16–20dp radii,
no shadows, no gradients. Type: `ivyInter`, title 20sp SemiBold, body 15sp Medium,
caption 12sp Regular, secondary text at 0.72 alpha (never decorative gray),
numerals SemiBold and tabular. Motion: critically damped spring (damping 1.0,
stiffness 320) for sheet rise and state changes only; no bounce, no press scaling.
Reduced motion → cross-fade.

## STORY
You can tell, in one glance, who owes whom and by how much; every action (add,
settle, comment) is a single tap from where you are; nothing hides the record.

## FIRST VIEWPORT
Workspace home: a single balance band (they owe | you owe), then the record.
Group detail: title + subtitle, balance band, one primary action, then balances
and expenses in one scroll. Sheets rise from the bottom on a dim scrim with a
sticky primary action.

## FORM
Code-led native Compose. `seed: none (pinned)` — owner direction: "quality pass,
keep structure" across all three workspaces. Structure is preserved; typography,
color discipline, spacing and motion are corrected in place.

## FINISH
unreviewed and undocumented is unfinished; each quality pass ends with a rendered
device review at shipping size, not just a green compile.
