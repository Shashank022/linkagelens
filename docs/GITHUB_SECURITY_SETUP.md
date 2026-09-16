# GitHub Security Setup

Repository files configure part of the security posture, but several controls must also be enabled in GitHub repository settings.

Recommended after publication:

1. Enable **Private vulnerability reporting** so `SECURITY.md` can point reporters to a private channel.
2. Enable **Dependabot alerts**.
3. Enable **Dependabot security updates**.
4. Enable **Secret scanning** and **push protection** when available for the repository/account.
5. Protect `main` with required CI checks and pull-request review as the project gains contributors.
6. Keep GitHub Actions permissions restricted. Workflows in this repository declare explicit least-privilege permissions and pin third-party actions to full commit SHAs.
7. Review OpenSSF Scorecard findings rather than treating the badge itself as a security guarantee.
