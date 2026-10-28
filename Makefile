# pinned on purpose with := (not ?=): the shell's JAVA_HOME (e.g. sdkman's "current") may point at
# another JDK, and this project's toolchain is JDK 17 (gradle.properties projectSourceCompatibility)
JAVA_HOME := /home/astrapi69/.sdkman/candidates/java/17.0.20-tem

# every target goes through this, so the JDK pin lives in one place
GRADLE := JAVA_HOME=$(JAVA_HOME) ./gradlew

.DEFAULT_GOAL := help

.PHONY: help build build-fast test clean spotless spotless-java spotless-misc spotless-check \
	pitest api-compatibility jacoco-report jar javadoc dependencies dependency-updates \
	version-catalog-format version-catalog-update publish-local publish-snapshot publish-central \
	tag-release install-hooks check-commit-provenance

help:
	@echo "email-tails - make targets"
	@echo ""
	@echo "  build                   clean build: tests + Jacoco + Spotless check + API compatibility (the everyday gate)"
	@echo "  build-fast              assemble without running tests"
	@echo "  test                    tests only"
	@echo "  clean                   remove build output"
	@echo ""
	@echo "  spotless                format, order imports and apply license headers (run before committing)"
	@echo "  spotless-java           Java sources only"
	@echo "  spotless-misc           everything else"
	@echo "  spotless-check          report what spotless would change, without changing it"
	@echo ""
	@echo "  pitest                  PIT mutation testing; the floor stands in build.gradle"
	@echo "  api-compatibility       japicmp against the last release (apiBaselineVersion in gradle.properties)"
	@echo "  jacoco-report           write the coverage report"
	@echo ""
	@echo "  jar                     the library jar"
	@echo "  javadoc                 the javadoc"
	@echo "  dependencies            print the dependency tree"
	@echo "  dependency-updates      report newer versions of dependencies and plugins"
	@echo "  version-catalog-format  sort and format gradle/libs.versions.toml"
	@echo "  version-catalog-update  write the newer versions into the catalog"
	@echo ""
	@echo "  publish-local           install into the local Maven repository"
	@echo "  publish-snapshot        upload a SNAPSHOT to the Central Portal snapshot repository (needs CONFIRM=yes)"
	@echo "  publish-central         upload a release to Maven Central (needs CONFIRM=yes)"
	@echo "  tag-release             PUBLISHES: the RELEASE tag triggers publish.yml (needs CONFIRM=yes)"
	@echo ""
	@echo "  install-hooks           point git at .githooks (once per clone or worktree)"
	@echo "  check-commit-provenance check this branch's commits against develop, as CI does"

build:
	$(GRADLE) clean build

build-fast:
	$(GRADLE) assemble -x test

test:
	$(GRADLE) test

clean:
	$(GRADLE) clean

spotless:
	$(GRADLE) spotlessApply

spotless-java:
	$(GRADLE) spotlessJavaApply

spotless-misc:
	$(GRADLE) spotlessMiscApply

spotless-check:
	$(GRADLE) spotlessCheck

# --rerun-tasks --no-build-cache because a cached PIT run reports success in milliseconds without
# producing numbers
pitest:
	$(GRADLE) pitest --rerun-tasks --no-build-cache

api-compatibility:
	$(GRADLE) apiCompatibility
	@echo "report: build/reports/japicmp/api-compatibility.html"

jacoco-report:
	$(GRADLE) jacocoTestReport

jar:
	$(GRADLE) jar

javadoc:
	$(GRADLE) javadoc

dependencies:
	$(GRADLE) dependencies

dependency-updates:
	$(GRADLE) dependencyUpdates

version-catalog-format:
	$(GRADLE) versionCatalogFormat

version-catalog-update:
	$(GRADLE) versionCatalogUpdate

publish-local:
	$(GRADLE) publishToMavenLocal

# a snapshot is what a push to develop publishes anyway; by hand it exists so that a consumer can
# be built against this branch before it is merged
publish-snapshot:
	@test "$(CONFIRM)" = "yes" || { \
		echo "publish-snapshot uploads to the Central Portal snapshot repository. Re-run with CONFIRM=yes if that is intended."; \
		exit 1; }
	@grep -q -- '-SNAPSHOT' gradle.properties || { echo "projectVersion is not a SNAPSHOT"; exit 1; }
	$(GRADLE) nmcpPublishAllPublicationsToCentralPortalSnapshots

# publishingType is USER_MANAGED, so this uploads a bundle that still waits for a click in the
# Portal. That is a net, not a permission: a published version can never be replaced.
publish-central:
	@test "$(CONFIRM)" = "yes" || { \
		echo "publish-central uploads to Maven Central. Re-run with CONFIRM=yes if that is intended."; \
		exit 1; }
	$(GRADLE) nmcpPublishAllPublicationsToCentralPortal

# Tagging is publishing: .github/workflows/publish.yml triggers on 'RELEASE-*' tag pushes. Decide
# whether to upload BEFORE the tag, not after it.
tag-release:
	@test "$(CONFIRM)" = "yes" || { \
		echo "tag-release creates a RELEASE tag, and pushing it triggers publish.yml, which uploads"; \
		echo "to Maven Central. Re-run with CONFIRM=yes if that is intended."; \
		exit 1; }
	$(GRADLE) tagRelease

# the commit-msg hook refuses a commit that credits a non-human collaborator; CI runs the same
# script over every pull request, so the hook is the early half, not the only one
install-hooks:
	git config core.hooksPath .githooks
	@echo "git hooks: .githooks (commit-msg runs scripts/check-commit-provenance.sh)"

check-commit-provenance:
	./scripts/check-commit-provenance.sh --range develop..HEAD
