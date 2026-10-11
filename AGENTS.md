# AGENTS.md

StudioBridge is a Java 11 Swing application (Maven, FlatLaf) that makes Bambu Lab
printers visible in Bambu Studio and Orca Slicer by sending the UDP package a
printer would send. `mvn package` builds the fat JAR and runs the tests.

## Pull requests and releases

- Start from an up to date `main`: `git fetch` and pull before creating a branch.
- Every pull request carries a label before it is merged, because the release
  notes are grouped by it (`.github/release.yml`): `enhancement`, `bug`,
  `dependencies`, `build`, `documentation` or `maintenance` (refactoring,
  cleanup, version bumps). A PR that does two things takes both labels and is
  listed once, under whichever category comes first. An unlabelled PR is not an
  error, it just lands under "Other Changes", where nobody looks.
- `breaking change` goes on top of one of those whenever an existing
  installation can stop working the way it did: a setting that changes meaning,
  a profile or settings file that has to be changed by hand, a CLI argument that
  behaves differently. It is the first category, so such a PR is listed under
  Breaking Changes rather than among the fixes.
- Every PR that changes what a user sees or does adds its entry to the topmost
  section of `CHANGELOG.md`, in English. A new version gets a new section at the
  top, headed `Version <x>` exactly as in `pom.xml`, below a line of dashes. The
  headings inside a section are `Breaking:`, `New Features:`, `Fixes:` and
  `Maintenance:`, and the entries say what changed for the user and why, ending
  with the PR number.
- The release job of `build-and-release.yml` runs on a `v.<version>` tag. It puts
  the pull requests GitHub groups by label first, then the download table, then
  the `CHANGELOG.md` section of the version. A version without a section gets a
  note saying so instead.
- The version lives only in `pom.xml`; the application reads it from the
  filtered `version.properties`.

## Code

- Every text the GUI shows goes through `I18n.t("key")` and lives in
  `src/main/resources/i18n/<code>.properties`, one file per language, UTF-8.
  English (`en.properties`) is the fallback for a missing key. A new text gets
  its key in every language file; `I18nTest` holds every language to the
  English keys and placeholders (`{name}`). The command line output, the UDP
  package and identifiers stay English.
- The profile list only holds saved profiles. No selection means a new
  profile, whose name is `MainMenu.NEW_PROFILE` (translated, reserved in every
  language); read the selection with `selectedProfile()`, never compare
  against a literal. New, import, rename and delete live in the menu button next to the list.
- Identifiers, comments, commit messages and PR descriptions are written in
  English.

- Most files use CRLF line endings, a few use LF. Keep the line endings of the
  file you edit.
