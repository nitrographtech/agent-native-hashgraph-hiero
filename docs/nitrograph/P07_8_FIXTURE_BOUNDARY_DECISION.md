# P07-8 fixture boundary decision

The original validation assumed the one-node preactivation fixture would replay to a committed
`STORAGE=424242` state and could serve a four-node reconnect. Exact-head consumption disproved that
assumption: the authenticated state loaded correctly, replayed 29 PCES events, reached `ACTIVE`,
and retained empty `STORAGE`. The frozen executable baseline produced the same result. Its signed
roster contains one member, so it cannot authenticate a four-node reconnect.

This is an expected preactivation boundary, not a runtime failure. The original tag, archive,
manifest, root, and purpose remain unchanged.

P07-8 therefore uses two complementary witnesses:

1. `p06a-fixture-preactivation-v065-ff6490d-round4744` proves one-node historical loading,
   activation, historical provider selection, empty preactivation storage, and deterministic
   rejection without restoring execution.
2. `p07-four-node-postwrite-v065-storage424242-v1` proves a four-member post-write state with
   `STORAGE=424242`, restart, real reconnect, synchronization, and mirror ingestion.

This corrects fixture semantics without reducing any production, historical-integrity, reconnect,
or mirror requirement. Neither fixture replaces or reinterprets the other.
