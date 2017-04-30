VERSION:=slice1

container:
	sbt docker:publishLocal

push-container: # what, like you're so special?
	@docker save feed-filter:${VERSION} | gzip > /tmp/feedfilter.tar.gz
	@rsync -zP /tmp/feedfilter.img.gz eddsteel.com:/tmp
