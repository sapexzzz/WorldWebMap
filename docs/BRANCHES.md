# Branch topology

`main` is the default, documentation-only landing branch. It must not contain loader source, Gradle build files, CI workflows, or generated audit reports.

| Branch | Purpose | Status |
| --- | --- | --- |
| `2.2fabric` | Active Fabric source and CI | `0.2.2` release candidate, not published |
| `2.2forge` | Active Forge source and CI | `0.2.2` release candidate, not published |
| `2.1fabric` | Final Fabric 2.1 source | Frozen archive |
| `2.1forge` | Final Forge 2.1 source | Frozen archive |

Loader changes belong only on the matching active 2.2 branch. Reports and local runtime data stay untracked; release creation requires a separately approved process.
