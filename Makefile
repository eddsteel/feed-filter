VERSION:=$(shell git rev-parse HEAD | cut -c1-7)

login:
	$$(aws ecr get-login --no-include-email)

image:
	docker build --tag $(DOCKER_IMG) .
	docker tag $(DOCKER_IMG):latest $(DOCKER_IMG):$(VERSION)

tag:
	docker tag $(DOCKER_IMG):latest $(DOCKER_REPO)/$(DOCKER_IMG):latest

push:
	docker push $(DOCKER_IMG):latest
	docker push $(DOCKER_REPO)/$(DOCKER_IMG):latest

run:
	docker run $(DOCKER_IMG):latest

deploy: login image tag push
