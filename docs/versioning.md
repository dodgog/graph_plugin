How do we publish a schema?
    what will change and what wont
    tables, columns,


similar to derived property, can have an additional lens with retrieval rules:
val author: String? by entity.optional(
        "author",
        defaultValue = null,
        decode = { it },
        lens = {
            if (VERSION > 0){
                it.attributes["author"]?.value
            } else if (VERSION >1){
                it.attributes["writer"]?.value.also { it+"abc" }
            }
        }
    )

author : attrs[author_v1] mapping to value
or author_v1 : attrs[author(_v1)] mapping to value

DocumentNode_v1
DocumentNode_v2
or
DocumentNode.VERSION

Entity.VERSION


https://github.com/wzhudev/reverse-linear-sync-engine
https://docs.bsky.app/docs/advanced-guides/custom-schemas
https://docs.bsky.app/blog/pinned-posts
https://github.com/bluesky-social/atproto/blob/main/packages/lexicon/README.md
https://nostr.com/
https://github.com/nostr-protocol/nostr
https://www.inkandswitch.com/cambria/
https://schema.org/docs/releases.html#v10.0
https://json-ld.org/


