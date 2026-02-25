# Git & GitHub Usage Standardization Policy  
Smart Home End-User Management Platform – ft_transcendence

**1. Branch naming convention**

- Main branch: `main` (protected)
- Feature branches: `feature/short-description` or `feat/invoice-upload`
- Bugfix: `fix/bug-description` or `bugfix/race-condition-org-invite`
- Hotfix (urgent on main): `hotfix/xxx`
- Release (final): `release/v1.0` (only at end)

**2. Commit message standard – Conventional Commits**

Format:  
`<type>(<scope>): <short description>`

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

Examples:
- `feat(invoices): add PDF upload endpoint`
- `fix(auth): prevent race condition on registration`
- `docs(readme): add modules points justification`
- `chore(docker): add volumes for uploads`
- `refactor(user): extract friends service logic`

**3. Branch protection rules** (must be set on `main`)

- Require PR before merging
- Require at least 1 approval (preferably TL for architecture)
- **Allow squash merging** (recommended default – see Merging Strategy below)
- Do not allow force-push

**4. Pull Request workflow**

1. Create branch from `main`
2. Work + commit often (small commits!)
3. Push branch
4. Open PR with:
   - Title following Conventional Commits
   - Description:
     - What / Why / How
     - Related issue: `Closes #42`
     - Screenshots (frontend)
     - Checklist: Docker works? No console errors? Tests/manual check?
5. Assign reviewers (at least 1, preferably TL + one other)
6. Wait for approval → merge (squash & merge preferred)

**5. Code review expectations**

- Review within 24h (even 5 min comment)
- Be kind & constructive
- Check:
  - Security (input validation, file upload)
  - Readability & Symfony/Angular conventions
  - No magic numbers/strings
  - Documentation if needed

**6. Issue & Project board usage**

- All work via GitHub Issues
- Link PR to issue (`Closes #XX`)
- Use Project board (Team planning template)
  - Columns: Backlog → To Do → In Progress → In Review → Done
  - Assign issues to sprints & people
- PM updates board daily

**7. Communication rules**

- Quick questions → Discord
- Architecture / decisions → GitHub Discussions → Decisions category
- Blockers → @mention in issue + Discord
- Daily stand-up → post in Discord or Discussions (Yesterday / Today / Blockers)

**8. Merging strategy**

**Default & recommended: Squash and merge**

- Keeps `main` clean and linear
- One meaningful commit per feature/fix instead of noisy history
- Easy to revert whole features
- Hides WIP/typo commits → professional look for evaluation

**Alternatives (use rarely)**

- Merge commit → only if preserving detailed branch history is important (rare)
- Rebase and merge → avoid (high conflict risk with part-time team)

Enforce via repo settings:  
- Enable "Allow squash merging"  
- Optionally disable rebase & merge commits

**9. ft_transcendence Specific Guidelines**  
(Extracted & adapted from subject v20.0 – February 2026)

- **Commits & history must show real contribution from all members**  
  → Clear, meaningful commit messages  
  → Commits from every team member visible on `main`  
  → Proper work distribution across the team

- **README.md is mandatory and heavily evaluated**  
  → Must follow exact structure from subject Chapter VI  
  → First line: `*This project has been created as part of the 42 curriculum by <login1>[, <login2>[, ...]].*`  
  → Sections: Description, Instructions, Resources (incl. AI usage), Team Information (roles & responsibilities), Project Management (tools/meetings), Technical Stack + justifications, Database Schema, Features List (with owner), Modules list (with points & justification), Individual Contributions (detailed + challenges)

- **AI usage transparency** (Chapter I – AI Instructions)  
  → Document in README "Resources" section:  
    - Which AI tools were used  
    - For which tasks (code, docs, ideas, explanations, prompting)  
    - How results were checked/reviewed/tested  
  → Never copy-paste AI code without full understanding  
  → Always peer-review AI-generated content  
  → Bad practice example: "Let Copilot write function → can't explain pipes during eval → fail"

- **Peer-evaluation & oral explanation requirements**  
  → Every team member must be able to explain:  
    - How roles were distributed  
    - How work was organized/divided/communicated  
    - Each member's concrete contributions  
    - The whole project (architecture, modules, choices)  
  → Be ready for live code modification request during eval

- **Project management visibility**  
  → Document in README:  
    - Tools used (GitHub Issues/Projects, Discord/Slack, etc.)  
    - Meeting frequency & format  
    - How blockers/risks were managed

This policy ensures the repository looks clean, contributions are traceable, and the project complies with ft_transcendence rules — increasing chances of good evaluation.

Follow strictly → clean repo + strong impression during defense.