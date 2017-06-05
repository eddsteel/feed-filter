VERSION:=slice5

container:
	sbt docker:publishLocal

push-container: # what, like you're so special?
	@docker save feed-filter:${VERSION} | gzip > /tmp/feedfilter.tar.gz
	@rsync -zP /tmp/feedfilter.tar.gz eddsteel.com:/tmp
	@rsync -zP feed-filter.service eddsteel.com:/tmp
