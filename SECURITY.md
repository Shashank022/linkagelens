# Security Policy

## Reporting a vulnerability

Please do **not** open a public GitHub issue for an exploitable security problem.

Preferred reporting channel:

1. Open the repository's **Security** tab.
2. Choose **Report a vulnerability** / GitHub Private Vulnerability Reporting when available.
3. Include the affected version/commit, impact, reproduction steps, and a minimal proof of concept if safe to share.

If private vulnerability reporting is temporarily unavailable, contact the repository owner privately through the contact method listed on the GitHub profile and avoid publishing exploit details until a coordinated fix is available.

## What to include

- affected LinkageLens version or commit;
- operating system and Java version;
- attack prerequisites;
- exact impact;
- minimal reproduction;
- suggested mitigation if known.

## Scope

Security-sensitive areas include malicious class/JAR parsing, archive handling, CI workflow security, report injection, and supply-chain compromise.

## Safe research

Please avoid testing against systems you do not own or have permission to test, destructive payloads, credential theft, or disclosure of third-party secrets.

## Supported versions

Until the project reaches 1.0, security fixes are applied to the latest release line. Users should upgrade to the newest available version.
