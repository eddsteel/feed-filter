# Feed Filter Proxy (2)

Proxy atom/rss feeds, applying filters to their contents. Filters out items, but includes all original data for the channel, and all remaining items.

Supports redirects, no conditional GETs, yet.

The filters are written as [starlark](https://github.com/bazel/starlark) programs, given an `episode` dict with `title`, `description`, and `link` values. You can see a configuration example in `src/dist/etc/feeds.yaml`.
