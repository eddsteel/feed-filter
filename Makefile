NAME:=$(shell grep -E '^name +:=' build.sbt | cut -f 2 -d '"')
VERSION:=$(shell grep -E '^version +:=' build.sbt | cut -f 2 -d'"')
IMAGE:=$(shell grep -E '^packageName +in +Docker +:=' deploy.sbt | cut -f 2 -d'"')

version:
	@echo "${NAME}-${VERSION} (${IMAGE}:${VERSION})"

service:
	sbt package

target/scala-2.12/${NAME}_2.12-${VERSION}.jar: service

container:
	sbt docker:publishLocal

push-container: # what, like you're so special?
	@docker save ${IMAGE}:${VERSION} | gzip > /tmp/feedfilter.tar.gz
	@rsync -zP /tmp/feedfilter.tar.gz eddsteel.com:/tmp
	@rsync -zP feed-filter.service eddsteel.com:/tmp

push:
	docker push ${IMAGE}
