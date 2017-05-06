# Feed Filter

Proxy atom feeds, applying filters to their contents. Filter out
items, but include all original data for the channel, and all
remaining items.

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

## Slice 2: Proper error handling with `EitherT`s
## Slice 3: Wittertainment but only if it's Simon and Mark
## Slice 4: Configuration file for above
## Slice 5: Support partial get (pass through cache headers)


## Notes

Main thing is not to lose information, so can't use a library that
normalises feeds in some way (e.g. ROME).
