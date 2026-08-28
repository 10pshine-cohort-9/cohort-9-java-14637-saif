## TEST FILE ORGANIZATION

All test scripts must be stored inside the standard Maven test source tree.

Backend tests MUST be placed under:

backend/contact-management-backend/src/test/java/

Tests should follow the same package structure as the production code.

Example:

src/main/java/com/saif/contactmanagement/service/impl/UserServiceImpl.java
        ↓
src/test/java/com/saif/contactmanagement/service/impl/UserServiceImplTest.java

src/main/java/com/saif/contactmanagement/util/JwtService.java
        ↓
src/test/java/com/saif/contactmanagement/util/JwtServiceTest.java

src/main/java/com/saif/contactmanagement/controller/AuthController.java
        ↓
src/test/java/com/saif/contactmanagement/controller/AuthControllerTest.java

Do NOT place test files:
- inside src/main/java/
- in random test folders
- in the project root
- inside target/
- inside generated/build directories

The target/ directory must never contain manually maintained source or test files.


## MANDATORY GIT & FEATURE-BASED BRANCHING WORKFLOW

Git is part of the project requirements.

The project already uses feature-based branching.
Continue using the existing branching strategy.

### Core Rule

EVERY implementation change MUST be committed.

Do not leave completed implementation work uncommitted.

This includes:
- production code
- tests
- configuration changes
- security changes
- database changes
- SonarQube/SonarLint configuration
- documentation when it is part of the implementation

### Feature-Based Branching

Do NOT develop unrelated features directly on the main branch.

Create/use a dedicated feature branch for each logical feature.

Examples:

feature/user-registration
feature/user-login
feature/jwt-authentication
feature/contact-crud
feature/contact-search
feature/change-password
feature/user-profile
feature/frontend-auth
feature/frontend-contacts

Follow the existing repository's actual naming convention if it differs from these examples.

### Implementation Workflow

For EVERY feature:

1. Inspect the current repository state.
2. Confirm the current branch.
3. Create or switch to the appropriate feature branch.
4. Implement the feature.
5. Write the corresponding tests.
6. Run the tests.
7. Check code coverage.
8. Fix failures and meaningful coverage gaps.
9. Run code-quality analysis.
10. Review the Git diff.
11. Commit the completed work.
12. Only then move to the next logical feature.

### Commit Rule

Commit after a coherent piece of work is completed and validated.

Do NOT:
- make one huge commit containing unrelated features
- commit broken code intentionally
- commit failing tests
- commit secrets
- commit .env files containing real credentials
- commit target/
- commit IDE-generated files unless already tracked intentionally

### Commit Messages

Use clear conventional-style messages.

Examples:

feat(auth): implement user login
feat(auth): add JWT authentication filter
test(auth): add login service tests
test(auth): add JWT service tests
fix(security): configure stateless JWT security
test(security): add authentication security tests
feat(contacts): implement contact creation

The commit message must describe what was actually changed.

### Before Every Commit

Run:

- application compilation
- relevant unit tests
- full test suite when appropriate
- coverage verification
- code-quality review
- git diff review
- git status

Never commit code that has not been validated.

### Important

The AI must NEVER silently make changes and leave them uncommitted when the feature is complete.

If it implements a feature, it must:
IMPLEMENT → TEST → VALIDATE → REVIEW → COMMIT

Then report:

- branch used
- files changed
- tests added
- test result
- coverage result
- commit hash/message
- next feature

### Existing Work

Before creating a new branch, inspect the repository's existing branches and Git history.

Do NOT recreate branches or commits that already exist.

Do NOT rewrite existing history unless explicitly instructed.

Preserve the feature-based branching strategy already established in this repository.


## DO NOT SKIP VERIFICATION

Speed is important, but correctness has priority.

Do not say a feature is "done" merely because:
- the application starts
- the code compiles
- the endpoint responds once
- the IDE shows no errors

A feature is DONE only after:

Implementation
+ Tests
+ Test execution
+ At least 90% required coverage maintained
+ Security/edge-case verification
+ Code-quality review
+ Git diff review
+ Commit
