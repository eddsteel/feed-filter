# Feed Filter Proxy

Proxy atom/rss feeds, applying filters to their contents. Filter out
items, but include all original data for the channel, and all
remaining items.

Supports redirects and conditional gets and not much else.

[![Build Status](https://travis-ci.org/eddsteel/feed-filter.svg?branch=master)](https://travis-ci.org/eddsteel/feed-filter)

## ~~Slice 1: Chapo Trap House without teasers~~

To test the idea. Provide the Chapo feed but no teasers because I'm a paid up grey wolf.

```
receive request -> request original (no cache) -> filter out those with Teaser in title -> serve feed
```

Filter is:

Extract: `FeedItem => String // in this case title`
Filter: `String => Boolean // in this case if not regex match`

What configuration might look like (bear in mind):

```yaml
    - name: chapo
      src: http://etc
      extract: title
      rule:
          type: filter-not
          contains: \bTeaser\b
```

## ~~Slice 2: Proper error handling with `EitherT`s~~

No need to throw exceptions like an animal. Haven't bothered with any
aliases yet. Added wart remover and scalafmt too.

## ~~Slice 3: Wittertainment but only if it's Simon and Mark~~

Are the abstractions right? To test filter out Five Live movie reviews
when Simon or Mark aren't there. That's not really wittertainment.

This just filters for `/sit(s|ting)? in for (Simon|Mark)/` in feed
description, which seems to catch the right ones.

## ~~Slice 4: Support redirects~~

Support redirects, and it's about time we use proper logging.

## ~~Slice 5: Configuration file for above~~
## ~~Slice 6: Build improvements~~
## ~~Slice 7/8: Support conditional get (pass through cache headers)~~
## ~~Slice 9: Docker streamline~~
Use alpine-scala, remove Makefile (SBT gets it all), update travis to
deploy through to docker hub.

## Notes

Main thing is not to lose information, so can't use a library that
normalises feeds in some way (e.g. ROME). Instead we parse enough XML
to get items out, and apply filters to them.
