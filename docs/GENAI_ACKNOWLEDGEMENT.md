# GenAI Acknowledgement

**Group 6 — Student Academic Records & Attendance Management System**

In line with the course's GenAI policy, we disclose that generative AI tools were
used during this project, and we describe how.

## How GenAI was used

- **Learning and understanding** — clarifying Spring Security's filter chain, JWT
  validation, JPA relationship mapping and React Context/Router concepts.
- **Scaffolding and boilerplate** — generating repetitive structures (DTOs,
  mappers, CRUD controllers) that we then reviewed and adapted.
- **Debugging** — diagnosing errors such as the `lower(bytea)` null-parameter issue,
  the `pg_hba.conf` BOM problem, and wrong-verb responses returning 500 instead of
  405.
- **Documentation review** — drafting and tidying the README, ERD notes and this
  report.
- **Test design** — suggesting boundary cases (grade cut-offs, attendance with no
  records, the authorization matrix).

## How the work was validated

Nothing was accepted blindly. Every piece of generated code was:

1. **Reviewed** by the responsible member for correctness and fit.
2. **Compiled and run** against the real application.
3. **Tested** — 23 unit tests, 52 integration checks and a security/QA sweep, with
   key figures cross-checked against independent SQL queries.
4. **Understood** — each member can explain, defend and modify the code in their
   area, including the business rules and security decisions.

## Statement

The design decisions, the architecture, the business rules and the final
implementation are ours. GenAI was a tool for learning, drafting and debugging, in
the same way documentation, tutorials and Stack Overflow are — it did not replace
our understanding of the system, and every group member is prepared to demonstrate
and defend their own contribution in the viva.
