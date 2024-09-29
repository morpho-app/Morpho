package com.morpho.app.model.bluesky

import app.bsky.feed.GetPostThreadResponse
import app.bsky.feed.GetPostThreadResponseThreadUnion
import com.morpho.app.util.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BskyPostTest {
    @Test
    fun testThreadRootPropagation() {
        val threadResponse1: GetPostThreadResponse = json.decodeFromString(threadResponseMidThreadJson)
        threadResponse1.thread.let {entry->
            when(entry) {
                is GetPostThreadResponseThreadUnion.ThreadViewPost -> {
                    val thread = entry.value.toThread()
                    thread.replies.forEach {p ->
                        when(p) {
                            is ThreadPost.BlockedPost -> {}
                            is ThreadPost.NotFoundPost -> {}
                            is ThreadPost.ViewablePost -> {
                                assertEquals(
                                    (thread.parents.first() as ThreadPost.ViewablePost).post.uri,
                                    p.post.reply?.rootPost?.uri,
                                    "Root post uri should match first(highest) parent uri"
                                )

                            }
                        }
                    }
                }
                is GetPostThreadResponseThreadUnion.NotFoundPost -> {
                    fail("Not found post")
                }
                is GetPostThreadResponseThreadUnion.BlockedPost -> {
                    fail("Blocked post")
                }
            }
        }
        val threadResponse2: GetPostThreadResponse = json.decodeFromString(threadTortureTest)
        threadResponse2.thread.let {entry->
            when(entry) {
                is GetPostThreadResponseThreadUnion.ThreadViewPost -> {
                    val thread = entry.value.toThread()
                    thread.replies.forEach {p ->
                        when(p) {
                            is ThreadPost.BlockedPost -> {}
                            is ThreadPost.NotFoundPost -> {}
                            is ThreadPost.ViewablePost -> {
                                assertEquals(
                                    (thread.parents.first() as ThreadPost.ViewablePost).post.uri,
                                    p.post.reply?.rootPost?.uri,
                                    "Root post uri should match first(highest) parent uri"
                                )
                            }
                        }
                    }
                }
                is GetPostThreadResponseThreadUnion.NotFoundPost -> {
                    fail("Not found post")
                }
                is GetPostThreadResponseThreadUnion.BlockedPost -> {
                    fail("Blocked post")
                }
            }
        }
    }

    companion object {
        val threadResponseMidThreadJson = """{"thread":{"${"$"}type":"app.bsky.feed.defs#threadViewPost","post":{"uri":"at://did:plc:uy7ehrx332p6dow5sk32hxzq/app.bsky.feed.post/3k7rmyigapw2y","cid":"bafyreifrkb2ycgkwov2cvgyx245b3dzdkz3fcf6rhkxplmbujbe747jjli","author":{"did":"did:plc:uy7ehrx332p6dow5sk32hxzq","handle":"chadmichael.bsky.social","displayName":"Chad","avatar":"https://av-cdn.bsky.app/img/avatar/plain/did:plc:uy7ehrx332p6dow5sk32hxzq/bafkreihf6a3ei6ni3i4plwyh3c57qzsqllqvkcny2pbfbno26uwyftugte@jpeg","viewer":{"muted":false,"blockedBy":false},"labels":[]},"record":{"text":"If Bluesky is invite only, does this mean that people were sitting on their invites until today or that Bluesky sent more invites to its waiting list or that people are suddenly giving away more invites? Hard to see how there's a correlation when signup isn't open.","${"$"}type":"app.bsky.feed.post","langs":["en"],"reply":{"root":{"cid":"bafyreibyefy7bmcf4inp7gotj5q2x233b73r7tmv4f7r5akbe462tfxiha","uri":"at://did:plc:mndtiksvxikpsy3zl6ebd2kr/app.bsky.feed.post/3k7rlrukr4w2v"},"parent":{"cid":"bafyreibyefy7bmcf4inp7gotj5q2x233b73r7tmv4f7r5akbe462tfxiha","uri":"at://did:plc:mndtiksvxikpsy3zl6ebd2kr/app.bsky.feed.post/3k7rlrukr4w2v"}},"createdAt":"2023-09-19T21:14:01.345Z"},"replyCount":2,"repostCount":0,"likeCount":1,"indexedAt":"2023-09-19T21:14:01.345Z","viewer":{},"labels":[]},"parent":{"${"$"}type":"app.bsky.feed.defs#threadViewPost","post":{"uri":"at://did:plc:mndtiksvxikpsy3zl6ebd2kr/app.bsky.feed.post/3k7rlrukr4w2v","cid":"bafyreibyefy7bmcf4inp7gotj5q2x233b73r7tmv4f7r5akbe462tfxiha","author":{"did":"did:plc:mndtiksvxikpsy3zl6ebd2kr","handle":"mattbinder.bsky.social","displayName":"Matt Binder","avatar":"https://av-cdn.bsky.app/img/avatar/plain/did:plc:mndtiksvxikpsy3zl6ebd2kr/bafkreihslo7xh75d6p4spm4z6zma2aujt52gmlykev3izvoklkgeqaj4vi@jpeg","viewer":{"muted":false,"blockedBy":false},"labels":[]},"record":{"text":"one day after Elon Musk said he would start charging all users for X, Bluesky is seeing its biggest single-day in new user signups mashable.com/article/blue...","${"$"}type":"app.bsky.feed.post","embed":{"${"$"}type":"app.bsky.embed.external","external":{"uri":"https://mashable.com/article/bluesky-record-new-signups-after-musk-says-charge-x-twitter-users-fee","thumb":{"${"$"}type":"blob","ref":{"${"$"}link":"bafkreiejqjdtx4hwolszgugueol4y7ao7ohkjfirnidjmub4fsowqscngm"},"mimeType":"image/jpeg","size":585744},"title":"Bluesky sees record signups day after Musk says X will go paid-only","description":"The day isn't even over yet Bluesky has already seen its biggest influx of new users in a single-day"}},"langs":["en"],"facets":[{"index":{"byteEnd":159,"byteStart":131},"features":[{"uri":"https://mashable.com/article/bluesky-record-new-signups-after-musk-says-charge-x-twitter-users-fee","${"$"}type":"app.bsky.richtext.facet#link"}]}],"createdAt":"2023-09-19T20:52:25.516Z"},"embed":{"${"$"}type":"app.bsky.embed.external#view","external":{"uri":"https://mashable.com/article/bluesky-record-new-signups-after-musk-says-charge-x-twitter-users-fee","title":"Bluesky sees record signups day after Musk says X will go paid-only","description":"The day isn't even over yet Bluesky has already seen its biggest influx of new users in a single-day","thumb":"https://av-cdn.bsky.app/img/feed_thumbnail/plain/did:plc:mndtiksvxikpsy3zl6ebd2kr/bafkreiejqjdtx4hwolszgugueol4y7ao7ohkjfirnidjmub4fsowqscngm@jpeg"}},"replyCount":55,"repostCount":230,"likeCount":916,"indexedAt":"2023-09-19T20:52:25.516Z","viewer":{},"labels":[]},"replies":[]},"replies":[{"${"$"}type":"app.bsky.feed.defs#threadViewPost","post":{"uri":"at://did:plc:3vqyk736eakmlwkmfraauemo/app.bsky.feed.post/3k7rob7ugc72d","cid":"bafyreigmz5b64c4276mhs3houo76gbrjhpophlnrbmwuiqse3gcg4n4a24","author":{"did":"did:plc:3vqyk736eakmlwkmfraauemo","handle":"dnasis.bsky.social","displayName":"Dnasis","avatar":"https://av-cdn.bsky.app/img/avatar/plain/did:plc:3vqyk736eakmlwkmfraauemo/bafkreifvl7hocxbzwlfj43mlleidwxydrf6rzlnc3xhjzo4hpctuavcfr4@jpeg","viewer":{"muted":false,"blockedBy":false},"labels":[]},"record":{"text":"Many people sign up for Bluesky via code or waitlist in the past, and never return, but gain invite codes on their accounts anyway. Elon-related events remind them they have an account, log in, and find invite codes they can share.","${"$"}type":"app.bsky.feed.post","langs":["en"],"reply":{"root":{"cid":"bafyreibyefy7bmcf4inp7gotj5q2x233b73r7tmv4f7r5akbe462tfxiha","uri":"at://did:plc:mndtiksvxikpsy3zl6ebd2kr/app.bsky.feed.post/3k7rlrukr4w2v"},"parent":{"cid":"bafyreifrkb2ycgkwov2cvgyx245b3dzdkz3fcf6rhkxplmbujbe747jjli","uri":"at://did:plc:uy7ehrx332p6dow5sk32hxzq/app.bsky.feed.post/3k7rmyigapw2y"}},"createdAt":"2023-09-19T21:36:48.159Z"},"replyCount":1,"repostCount":1,"likeCount":2,"indexedAt":"2023-09-19T21:36:48.159Z","viewer":{},"labels":[]},"replies":[{"${"$"}type":"app.bsky.feed.defs#threadViewPost","post":{"uri":"at://did:plc:3vqyk736eakmlwkmfraauemo/app.bsky.feed.post/3k7rocw2w6u2t","cid":"bafyreicnfj6alvaosl5qldaoaxm5mslajasyj3u3xfasnxg6fskqklzxkm","author":{"did":"did:plc:3vqyk736eakmlwkmfraauemo","handle":"dnasis.bsky.social","displayName":"Dnasis","avatar":"https://av-cdn.bsky.app/img/avatar/plain/did:plc:3vqyk736eakmlwkmfraauemo/bafkreifvl7hocxbzwlfj43mlleidwxydrf6rzlnc3xhjzo4hpctuavcfr4@jpeg","viewer":{"muted":false,"blockedBy":false},"labels":[]},"record":{"text":"That’s why there’s huge growth here today despite Bluesky staff not distributing a bonus invite code to everyone like they have previously done for Elon-related events","${"$"}type":"app.bsky.feed.post","langs":["en"],"reply":{"root":{"cid":"bafyreibyefy7bmcf4inp7gotj5q2x233b73r7tmv4f7r5akbe462tfxiha","uri":"at://did:plc:mndtiksvxikpsy3zl6ebd2kr/app.bsky.feed.post/3k7rlrukr4w2v"},"parent":{"cid":"bafyreigmz5b64c4276mhs3houo76gbrjhpophlnrbmwuiqse3gcg4n4a24","uri":"at://did:plc:3vqyk736eakmlwkmfraauemo/app.bsky.feed.post/3k7rob7ugc72d"}},"createdAt":"2023-09-19T21:37:44.990Z"},"replyCount":0,"repostCount":1,"likeCount":2,"indexedAt":"2023-09-19T21:37:44.990Z","viewer":{},"labels":[]},"replies":[]}]},{"${"$"}type":"app.bsky.feed.defs#threadViewPost","post":{"uri":"at://did:plc:w2gsbanugiw67kud7sieilh7/app.bsky.feed.post/3k7rn6uemfg2c","cid":"bafyreif52uj4pcjemn5id67emqbavc6cnzn3vfdl23ssngw6fam5xyg4se","author":{"did":"did:plc:w2gsbanugiw67kud7sieilh7","handle":"mattgraham.bsky.social","displayName":"Matt Graham","avatar":"https://av-cdn.bsky.app/img/avatar/plain/did:plc:w2gsbanugiw67kud7sieilh7/bafkreib3xjujq5lgx7esfuczj4p3f6yttgt7gkypftinjdxsrkv3phlf6e@jpeg","viewer":{"muted":false,"blockedBy":false},"labels":[]},"record":{"text":"FWIW I was sitting on some","${"$"}type":"app.bsky.feed.post","langs":["en"],"reply":{"root":{"cid":"bafyreibyefy7bmcf4inp7gotj5q2x233b73r7tmv4f7r5akbe462tfxiha","uri":"at://did:plc:mndtiksvxikpsy3zl6ebd2kr/app.bsky.feed.post/3k7rlrukr4w2v"},"parent":{"cid":"bafyreifrkb2ycgkwov2cvgyx245b3dzdkz3fcf6rhkxplmbujbe747jjli","uri":"at://did:plc:uy7ehrx332p6dow5sk32hxzq/app.bsky.feed.post/3k7rmyigapw2y"}},"createdAt":"2023-09-19T21:17:35.312Z"},"replyCount":2,"repostCount":0,"likeCount":5,"indexedAt":"2023-09-19T21:17:35.312Z","viewer":{},"labels":[]},"replies":[{"${"$"}type":"app.bsky.feed.defs#threadViewPost","post":{"uri":"at://did:plc:afppko2gwdw53mqt7kzdx27g/app.bsky.feed.post/3k7rngaexam2t","cid":"bafyreihw2d37mxuu27oy7dbjdrjkqt47462jjwqntv5b2pqu3u5jhef6cm","author":{"did":"did:plc:afppko2gwdw53mqt7kzdx27g","handle":"jlrube.bsky.social","displayName":"Jake Rubinstein ","avatar":"https://av-cdn.bsky.app/img/avatar/plain/did:plc:afppko2gwdw53mqt7kzdx27g/bafkreiaxvzgqguvnvpc2624now5s6gklicogpz4cnfi7wtjdg6uyvf5qie@jpeg","viewer":{"muted":false,"blockedBy":false},"labels":[]},"record":{"text":"Yeah, I had neglected to offer them to people and nobody had asked!","${"$"}type":"app.bsky.feed.post","langs":["en"],"reply":{"root":{"cid":"bafyreibyefy7bmcf4inp7gotj5q2x233b73r7tmv4f7r5akbe462tfxiha","uri":"at://did:plc:mndtiksvxikpsy3zl6ebd2kr/app.bsky.feed.post/3k7rlrukr4w2v"},"parent":{"cid":"bafyreif52uj4pcjemn5id67emqbavc6cnzn3vfdl23ssngw6fam5xyg4se","uri":"at://did:plc:w2gsbanugiw67kud7sieilh7/app.bsky.feed.post/3k7rn6uemfg2c"}},"createdAt":"2023-09-19T21:21:41.725Z"},"replyCount":0,"repostCount":0,"likeCount":3,"indexedAt":"2023-09-19T21:21:41.725Z","viewer":{},"labels":[]},"replies":[]},{"${"$"}type":"app.bsky.feed.defs#threadViewPost","post":{"uri":"at://did:plc:wbfs32kk4w264yy4smjx5b4t/app.bsky.feed.post/3k7rnexppqa2g","cid":"bafyreiggrlu4v5mqbdpjls46gyyk3gj4b5u7dluuv7q6qougsr74zbtske","author":{"did":"did:plc:wbfs32kk4w264yy4smjx5b4t","handle":"dankguyjeff.bsky.social","avatar":"https://av-cdn.bsky.app/img/avatar/plain/did:plc:wbfs32kk4w264yy4smjx5b4t/bafkreihl3saj5ehr47tf7psjacy5fnq4tigphwqy77qgpm2dzv75c3zd7u@jpeg","viewer":{"muted":false,"blockedBy":false},"labels":[]},"record":{"text":"You might want to air those out before distributing them.","${"$"}type":"app.bsky.feed.post","langs":["en"],"reply":{"root":{"cid":"bafyreibyefy7bmcf4inp7gotj5q2x233b73r7tmv4f7r5akbe462tfxiha","uri":"at://did:plc:mndtiksvxikpsy3zl6ebd2kr/app.bsky.feed.post/3k7rlrukr4w2v"},"parent":{"cid":"bafyreif52uj4pcjemn5id67emqbavc6cnzn3vfdl23ssngw6fam5xyg4se","uri":"at://did:plc:w2gsbanugiw67kud7sieilh7/app.bsky.feed.post/3k7rn6uemfg2c"}},"createdAt":"2023-09-19T21:21:00.080Z"},"replyCount":0,"repostCount":0,"likeCount":0,"indexedAt":"2023-09-19T21:21:00.080Z","viewer":{},"labels":[]},"replies":[]}]}],"viewer":{"canReply":true}}}"""

        val threadTortureTest = """{
    "thread": {
        "${"$"}type": "app.bsky.feed.defs#threadViewPost",
        "post": {
            "uri": "at://did:plc:yfvwmnlztr4dwkb7hwz55r2g/app.bsky.feed.post/3kosdondcpl2i",
            "cid": "bafyreies62mzwzsxvp3z2oejxxhxmvieax26bmwurwlo3lzqboe2unlntu",
            "author": {
                "did": "did:plc:yfvwmnlztr4dwkb7hwz55r2g",
                "handle": "nonbinary.computer",
                "displayName": "Orual",
                "avatar": "https://cdn.bsky.app/img/avatar/plain/did:plc:yfvwmnlztr4dwkb7hwz55r2g/bafkreig4qtqlzxzsnng5hjkbeca7rz4pxownfxs4zy54ixmrg7lsc4nwbu@jpeg",
                "viewer": {
                    "muted": false,
                    "blockedBy": false,
                    "following": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.graph.follow/3kaq3uakjey26",
                    "followedBy": "at://did:plc:yfvwmnlztr4dwkb7hwz55r2g/app.bsky.graph.follow/3kaqglvvmir2u"
                },
                "labels": []
            },
            "record": {
                "${"$"}type": "app.bsky.feed.post",
                "createdAt": "2024-03-29T01:18:01.792Z",
                "langs": [
                    "en"
                ],
                "reply": {
                    "parent": {
                        "cid": "bafyreige5na6u3edbe6jvqkfxo6co7uskxopnhfqnr35ijnoea23elybii",
                        "uri": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.feed.post/3kosdkjzaua2f"
                    },
                    "root": {
                        "cid": "bafyreiavrdj5zop6ievkrkzrvm2mc2q3mkkj5xddn2ngvf2etbco3drgju",
                        "uri": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.feed.post/3koscfhae3h2x"
                    }
                },
                "text": "Yeah, it's a tradeoff. 5GHz is clearly a lot harder to make cheap, tiny, low-power radios for. It's almost impossible to find an embedded WiFi module that supports it. A client was complaining about it. 900MHz is ok for IoT stuff specifically, but you run into bandwidth limitations HARD."
            },
            "replyCount": 1,
            "repostCount": 0,
            "likeCount": 3,
            "indexedAt": "2024-03-29T01:18:01.748Z",
            "viewer": {
                "like": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.feed.like/3kosdtdpey32e"
            },
            "labels": []
        },
        "parent": {
            "${"$"}type": "app.bsky.feed.defs#threadViewPost",
            "post": {
                "uri": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.feed.post/3kosdkjzaua2f",
                "cid": "bafyreige5na6u3edbe6jvqkfxo6co7uskxopnhfqnr35ijnoea23elybii",
                "author": {
                    "did": "did:plc:jmszivbbbadpztauju5dev26",
                    "handle": "ovna.dev",
                    "displayName": "nova 🆒",
                    "avatar": "https://cdn.bsky.app/img/avatar/plain/did:plc:jmszivbbbadpztauju5dev26/bafkreidy7n4if57jupohgjj6zhpd3x34edx3qwkg5qx4ejveoehfdwim3e@jpeg",
                    "viewer": {
                        "muted": false,
                        "blockedBy": false
                    },
                    "labels": [
                        {
                            "src": "did:plc:jmszivbbbadpztauju5dev26",
                            "uri": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.actor.profile/self",
                            "cid": "bafyreia5kk4vghcas5czhse77vnuhfv55zsyaw4ovr3urw73g67vm3ulay",
                            "val": "!no-unauthenticated",
                            "cts": "1970-01-01T00:00:00.000Z"
                        }
                    ]
                },
                "record": {
                    "${"$"}type": "app.bsky.feed.post",
                    "createdAt": "2024-03-29T01:15:43.697Z",
                    "langs": [
                        "en"
                    ],
                    "reply": {
                        "parent": {
                            "cid": "bafyreihdwsiylhll7aeq7o3sgr3rrc6ojb5xes4kmd3xpf3geaeyhs3nxu",
                            "uri": "at://did:plc:yfvwmnlztr4dwkb7hwz55r2g/app.bsky.feed.post/3kosdi4qiyk2f"
                        },
                        "root": {
                            "cid": "bafyreiavrdj5zop6ievkrkzrvm2mc2q3mkkj5xddn2ngvf2etbco3drgju",
                            "uri": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.feed.post/3koscfhae3h2x"
                        }
                    },
                    "text": "in denser environments it can be a problem with lesser hardware. some of my IoT additions are mercurial due to the realities of an apartment building"
                },
                "replyCount": 1,
                "repostCount": 0,
                "likeCount": 2,
                "indexedAt": "2024-03-29T01:15:43.697Z",
                "viewer": {},
                "labels": []
            },
            "parent": {
                "${"$"}type": "app.bsky.feed.defs#threadViewPost",
                "post": {
                    "uri": "at://did:plc:yfvwmnlztr4dwkb7hwz55r2g/app.bsky.feed.post/3kosdi4qiyk2f",
                    "cid": "bafyreihdwsiylhll7aeq7o3sgr3rrc6ojb5xes4kmd3xpf3geaeyhs3nxu",
                    "author": {
                        "did": "did:plc:yfvwmnlztr4dwkb7hwz55r2g",
                        "handle": "nonbinary.computer",
                        "displayName": "Orual",
                        "avatar": "https://cdn.bsky.app/img/avatar/plain/did:plc:yfvwmnlztr4dwkb7hwz55r2g/bafkreig4qtqlzxzsnng5hjkbeca7rz4pxownfxs4zy54ixmrg7lsc4nwbu@jpeg",
                        "viewer": {
                            "muted": false,
                            "blockedBy": false,
                            "following": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.graph.follow/3kaq3uakjey26",
                            "followedBy": "at://did:plc:yfvwmnlztr4dwkb7hwz55r2g/app.bsky.graph.follow/3kaqglvvmir2u"
                        },
                        "labels": []
                    },
                    "record": {
                        "${"$"}type": "app.bsky.feed.post",
                        "createdAt": "2024-03-29T01:14:23.078Z",
                        "langs": [
                            "en"
                        ],
                        "reply": {
                            "parent": {
                                "cid": "bafyreihurksnusgi5ap2s5dsgenosay7aprl4rxtq7r63wyamndiexqg3y",
                                "uri": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.feed.post/3kosd5hum6a2g"
                            },
                            "root": {
                                "cid": "bafyreiavrdj5zop6ievkrkzrvm2mc2q3mkkj5xddn2ngvf2etbco3drgju",
                                "uri": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.feed.post/3koscfhae3h2x"
                            }
                        },
                        "text": "Actually, using the 2.4GHz band was maybe the most sensible choice they made. It's the sweet spot of high enough frequency to have decent bandwidth, but not so high as to be hard to build tiny radios for or to have unreasonably short range at low powers."
                    },
                    "replyCount": 2,
                    "repostCount": 0,
                    "likeCount": 4,
                    "indexedAt": "2024-03-29T01:14:23.078Z",
                    "viewer": {
                        "like": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.feed.like/3kosdjajy5v2z"
                    },
                    "labels": []
                },
                "parent": {
                    "${"$"}type": "app.bsky.feed.defs#threadViewPost",
                    "post": {
                        "uri": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.feed.post/3kosd5hum6a2g",
                        "cid": "bafyreihurksnusgi5ap2s5dsgenosay7aprl4rxtq7r63wyamndiexqg3y",
                        "author": {
                            "did": "did:plc:jmszivbbbadpztauju5dev26",
                            "handle": "ovna.dev",
                            "displayName": "nova 🆒",
                            "avatar": "https://cdn.bsky.app/img/avatar/plain/did:plc:jmszivbbbadpztauju5dev26/bafkreidy7n4if57jupohgjj6zhpd3x34edx3qwkg5qx4ejveoehfdwim3e@jpeg",
                            "viewer": {
                                "muted": false,
                                "blockedBy": false
                            },
                            "labels": [
                                {
                                    "src": "did:plc:jmszivbbbadpztauju5dev26",
                                    "uri": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.actor.profile/self",
                                    "cid": "bafyreia5kk4vghcas5czhse77vnuhfv55zsyaw4ovr3urw73g67vm3ulay",
                                    "val": "!no-unauthenticated",
                                    "cts": "1970-01-01T00:00:00.000Z"
                                }
                            ]
                        },
                        "record": {
                            "${"$"}type": "app.bsky.feed.post",
                            "createdAt": "2024-03-29T01:08:25.236Z",
                            "langs": [
                                "en"
                            ],
                            "reply": {
                                "parent": {
                                    "cid": "bafyreibccge3drllet6dzkoziucz6bbo3verhn4zgfhvjavxvg6xd6xgce",
                                    "uri": "at://did:plc:jfhpnnst6flqway4eaeqzj2a/app.bsky.feed.post/3koscvez7t42z"
                                },
                                "root": {
                                    "cid": "bafyreiavrdj5zop6ievkrkzrvm2mc2q3mkkj5xddn2ngvf2etbco3drgju",
                                    "uri": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.feed.post/3koscfhae3h2x"
                                }
                            },
                            "text": "i have a really good idea, we should put it on a commonly used radio band so we can maximize potential interference"
                        },
                        "replyCount": 2,
                        "repostCount": 0,
                        "likeCount": 4,
                        "indexedAt": "2024-03-29T01:08:25.236Z",
                        "viewer": {},
                        "labels": []
                    },
                    "parent": {
                        "${"$"}type": "app.bsky.feed.defs#threadViewPost",
                        "post": {
                            "uri": "at://did:plc:jfhpnnst6flqway4eaeqzj2a/app.bsky.feed.post/3koscvez7t42z",
                            "cid": "bafyreibccge3drllet6dzkoziucz6bbo3verhn4zgfhvjavxvg6xd6xgce",
                            "author": {
                                "did": "did:plc:jfhpnnst6flqway4eaeqzj2a",
                                "handle": "bossett.social",
                                "displayName": "Bossett",
                                "avatar": "https://cdn.bsky.app/img/avatar/plain/did:plc:jfhpnnst6flqway4eaeqzj2a/bafkreid42e2su4sju7hl2nm4ouacw3icvvytf7r6gab3pvc2qxhqhc5ji4@jpeg",
                                "viewer": {
                                    "muted": false,
                                    "blockedBy": false,
                                    "following": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.graph.follow/3kkertghqtf2w",
                                    "followedBy": "at://did:plc:jfhpnnst6flqway4eaeqzj2a/app.bsky.graph.follow/3kkfv2bpzi22y"
                                },
                                "labels": []
                            },
                            "record": {
                                "${"$"}type": "app.bsky.feed.post",
                                "createdAt": "2024-03-29T01:03:53.824Z",
                                "langs": [
                                    "en"
                                ],
                                "reply": {
                                    "parent": {
                                        "cid": "bafyreie7qolmxu4nujhuqzuxx2xatf5kxybq3kjkwhiy2pauiukfxp25oq",
                                        "uri": "at://did:plc:wzsilnxf24ehtmmc3gssy5bu/app.bsky.feed.post/3kosci2rnph2d"
                                    },
                                    "root": {
                                        "cid": "bafyreiavrdj5zop6ievkrkzrvm2mc2q3mkkj5xddn2ngvf2etbco3drgju",
                                        "uri": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.feed.post/3koscfhae3h2x"
                                    }
                                },
                                "text": "ooh can we make the delay non-deterministic *and* sometimes it would be neat if the devices just developed amnesia from time-to-time"
                            },
                            "replyCount": 1,
                            "repostCount": 0,
                            "likeCount": 5,
                            "indexedAt": "2024-03-29T01:03:53.824Z",
                            "viewer": {
                                "like": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.feed.like/3koscx4yzpu2g"
                            },
                            "labels": []
                        },
                        "parent": {
                            "${"$"}type": "app.bsky.feed.defs#threadViewPost",
                            "post": {
                                "uri": "at://did:plc:wzsilnxf24ehtmmc3gssy5bu/app.bsky.feed.post/3kosci2rnph2d",
                                "cid": "bafyreie7qolmxu4nujhuqzuxx2xatf5kxybq3kjkwhiy2pauiukfxp25oq",
                                "author": {
                                    "did": "did:plc:wzsilnxf24ehtmmc3gssy5bu",
                                    "handle": "flicknow.xyz",
                                    "displayName": "mark 🆒",
                                    "avatar": "https://cdn.bsky.app/img/avatar/plain/did:plc:wzsilnxf24ehtmmc3gssy5bu/bafkreieuclqm4hoq2z75zc7a67l45nanu3vqsq45uks6qqaxaikdqm5r6i@jpeg",
                                    "viewer": {
                                        "muted": false,
                                        "blockedBy": false,
                                        "following": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.graph.follow/3kkhgpzizld2u",
                                        "followedBy": "at://did:plc:wzsilnxf24ehtmmc3gssy5bu/app.bsky.graph.follow/3kkhgp5ueyc2s"
                                    },
                                    "labels": []
                                },
                                "record": {
                                    "${"$"}type": "app.bsky.feed.post",
                                    "createdAt": "2024-03-29T00:56:26.794Z",
                                    "langs": [
                                        "en"
                                    ],
                                    "reply": {
                                        "parent": {
                                            "cid": "bafyreiavrdj5zop6ievkrkzrvm2mc2q3mkkj5xddn2ngvf2etbco3drgju",
                                            "uri": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.feed.post/3koscfhae3h2x"
                                        },
                                        "root": {
                                            "cid": "bafyreiavrdj5zop6ievkrkzrvm2mc2q3mkkj5xddn2ngvf2etbco3drgju",
                                            "uri": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.feed.post/3koscfhae3h2x"
                                        }
                                    },
                                    "text": "wireless manufacturers: wouldn't it be cool if there was a bit of a delay between what you see and hear, and also everything requires batteries now?"
                                },
                                "replyCount": 3,
                                "repostCount": 0,
                                "likeCount": 11,
                                "indexedAt": "2024-03-29T00:56:26.794Z",
                                "viewer": {
                                    "like": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.feed.like/3koscj2yicn2q"
                                },
                                "labels": []
                            },
                            "parent": {
                                "${"$"}type": "app.bsky.feed.defs#threadViewPost",
                                "post": {
                                    "uri": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.feed.post/3koscfhae3h2x",
                                    "cid": "bafyreiavrdj5zop6ievkrkzrvm2mc2q3mkkj5xddn2ngvf2etbco3drgju",
                                    "author": {
                                        "did": "did:plc:jmszivbbbadpztauju5dev26",
                                        "handle": "ovna.dev",
                                        "displayName": "nova 🆒",
                                        "avatar": "https://cdn.bsky.app/img/avatar/plain/did:plc:jmszivbbbadpztauju5dev26/bafkreidy7n4if57jupohgjj6zhpd3x34edx3qwkg5qx4ejveoehfdwim3e@jpeg",
                                        "viewer": {
                                            "muted": false,
                                            "blockedBy": false
                                        },
                                        "labels": [
                                            {
                                                "src": "did:plc:jmszivbbbadpztauju5dev26",
                                                "uri": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.actor.profile/self",
                                                "cid": "bafyreia5kk4vghcas5czhse77vnuhfv55zsyaw4ovr3urw73g67vm3ulay",
                                                "val": "!no-unauthenticated",
                                                "cts": "1970-01-01T00:00:00.000Z"
                                            }
                                        ]
                                    },
                                    "record": {
                                        "${"$"}type": "app.bsky.feed.post",
                                        "createdAt": "2024-03-29T00:55:01.734Z",
                                        "langs": [
                                            "en"
                                        ],
                                        "text": "wires are good, actually"
                                    },
                                    "replyCount": 5,
                                    "repostCount": 0,
                                    "likeCount": 18,
                                    "indexedAt": "2024-03-29T00:54:59.747Z",
                                    "viewer": {},
                                    "labels": []
                                }
                            }
                        }
                    }
                }
            }
        },
        "replies": [
            {
                "${"$"}type": "app.bsky.feed.defs#threadViewPost",
                "post": {
                    "uri": "at://did:plc:yfvwmnlztr4dwkb7hwz55r2g/app.bsky.feed.post/3kosdty4sbj2q",
                    "cid": "bafyreieuinkddwkzq55we2rp5j3k2qrjuzznscmmzsk4g7k6bwsvor4m64",
                    "author": {
                        "did": "did:plc:yfvwmnlztr4dwkb7hwz55r2g",
                        "handle": "nonbinary.computer",
                        "displayName": "Orual",
                        "avatar": "https://cdn.bsky.app/img/avatar/plain/did:plc:yfvwmnlztr4dwkb7hwz55r2g/bafkreig4qtqlzxzsnng5hjkbeca7rz4pxownfxs4zy54ixmrg7lsc4nwbu@jpeg",
                        "viewer": {
                            "muted": false,
                            "blockedBy": false,
                            "following": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.graph.follow/3kaq3uakjey26",
                            "followedBy": "at://did:plc:yfvwmnlztr4dwkb7hwz55r2g/app.bsky.graph.follow/3kaqglvvmir2u"
                        },
                        "labels": []
                    },
                    "record": {
                        "${"$"}type": "app.bsky.feed.post",
                        "createdAt": "2024-03-29T01:21:00.893Z",
                        "langs": [
                            "en"
                        ],
                        "reply": {
                            "parent": {
                                "cid": "bafyreies62mzwzsxvp3z2oejxxhxmvieax26bmwurwlo3lzqboe2unlntu",
                                "uri": "at://did:plc:yfvwmnlztr4dwkb7hwz55r2g/app.bsky.feed.post/3kosdondcpl2i"
                            },
                            "root": {
                                "cid": "bafyreiavrdj5zop6ievkrkzrvm2mc2q3mkkj5xddn2ngvf2etbco3drgju",
                                "uri": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.feed.post/3koscfhae3h2x"
                            }
                        },
                        "text": "One thing that manufacturers don't always do a good job of is choosing case materials with good radio transparency. They also tend to skimp on antennas bc of cost."
                    },
                    "replyCount": 0,
                    "repostCount": 0,
                    "likeCount": 3,
                    "indexedAt": "2024-03-29T01:21:00.746Z",
                    "viewer": {
                        "like": "at://did:plc:jmszivbbbadpztauju5dev26/app.bsky.feed.like/3kose4iycei2g"
                    },
                    "labels": []
                },
                "replies": []
            }
        ]
    }
}"""

    }
}






