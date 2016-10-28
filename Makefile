commit  := $(shell git rev-parse --short HEAD)
tag     := $(shell git tag -l 'v*-rc*' --points-at HEAD)
VERSION := $(shell if [[ -n "$(tag)" ]]; then echo $(tag) | sed 's/^v//'; else echo $(commit); fi)

ARTIFACTORY_PUBLISH := $(shell if [[ -n "$(tag)" ]]; then echo release; else echo dev; fi)

all: build

clean:
	mvn clean

build:
	mvn compile

test: test-unit

test-unit: clean
	mvn test

package:
	mvn versions:set -DnewVersion=$(VERSION) -DgenerateBackupPoms=false
	mvn package -DskipTests=true

dist: clean package

publish:
	mvn jar:jar deploy:deploy -DpublishRepo=$(ARTIFACTORY_PUBLISH)

.PHONY: all clean build test test-unit package dist publish