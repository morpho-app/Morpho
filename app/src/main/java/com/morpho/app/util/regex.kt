package com.morpho.app.util

/// A string containing Unicode representations of characters considered invalid
/// in certain contexts.
///
/// The characters in this group are:
/// - U+FFFE: a non character reserved for internal use
/// - U+FEFF: Zero Width No-Break Space, also used as a byte order mark (BOM)
/// - U+FFFF: another non character reserved for internal use
///
/// These characters are typically used to identify invalid or unexpected
/// character sequences when processing Unicode text.
public const val invalidCharsGroup = """\uFFFE\uFEFF\uFFFF"""
/// A string representing the Unicode range for various space or whitespace characters.
///
/// This range includes the following Unicode characters and blocks:
/// - ASCII Horizontal Tab, Line Feed, Vertical Tab, Form Feed, Carriage Return, and Space: U+0009–U+000D, U+0020
/// - Next Line (NEL): U+0085
/// - No-Break Space: U+00A0
/// - Ogham Space Mark: U+1680
/// - Mongolian Vowel Separator: U+180E
/// - General Punctuation spaces: U+2000–U+200A
/// - Line Separator: U+2028
/// - Paragraph Separator: U+2029
/// - Narrow No-Break Space: U+202F
/// - Medium Mathematical Space: U+205F
/// - Ideographic Space: U+3000
///
/// These characters are generally used to represent spaces or whitespace in different contexts or scripts
/// and are often used in regular expressions to match any whitespace character.
public const val spaces = """\u0009-\u000D\u0020\u0085\u00A0\u1680\u180E\u2000-\u200A\u2028\u2029\u202F\u205F\u3000"""
/// A string representing a regular expression pattern used to match valid
/// port numbers in a URL.
///
/// A valid port number is a sequence of one or more digits, ranging
/// from 0 to 9.
///
/// In networking, valid port numbers can range from 0 to 65535, but
/// this pattern will match any sequence of digits without considering
/// the numeric range.
///
/// Example of valid matches: "80", "8080", "12345".
/// Example of invalid matches: "abc", "12a34", "-123".
public const val validPortNumber = "[0-9]+"
/// Punctuation regex string
public const val punct = """/\!'#%&'\(\)*\+,\\\-\./:;<=>\?@\[\]\^_{|}~\$"""
/// A string containing Unicode directional markers.
///
/// These markers control the directionality of the text, and are particularly
/// important for bidirectional text that includes languages that are written
/// from right to left, such as Arabic and Hebrew.
///
/// The markers include:
/// - U+202A to U+202E: Embedding and override controls
/// - U+061C: Arabic Letter Mark
/// - U+200E: Left-to-Right Mark
/// - U+200F: Right-to-Left Mark
/// - U+2066 to U+2069: Isolate controls
public const val directionalMarkers = """\u202A-\u202E\u061C\u200E\u200F\u2066\u2067\u2068\u2069"""

/// `invalidDomainChars` is a constant [RegExp] pattern string, designed to
/// match any characters that are considered invalid within a domain name
/// context.
///
/// It is constructed from the following components:
/// - `[punct]` is intended to represent any punctuation characters.
/// - `[spacesGroup]` matches any characters considered as whitespace or spaces.
/// - `[invalidCharsGroup]` is meant to match any other characters that are
///    invalid in domain names.
/// - `[directionalMarkersGroup]` is designed to match any Unicode directional
///    markers.
public const val invalidDomainCharacters = punct + spaces + invalidCharsGroup + directionalMarkers
/// `validDomainChars` is a constant [RegExp] pattern string intended to match
/// any character that is not included in the [invalidDomainChars] string,
/// representing valid characters allowed in a domain name.
///
/// This constant can be used to create a [RegExp] object to check whether
/// a given string or part of a string conforms to the character
/// restrictions of domain names.
public const val validDomainChars = "[^$invalidDomainCharacters]"
/// Regex to match a complete valid domain name
public const val validDomainName = "(?:(?:$validDomainChars(?:-|$validDomainChars)*)?$validDomainChars\\.)"
/// `validCctld` is a constant [RegExp] pattern string constructed to match
/// a wide range of valid country code top-level domains (ccTLDs). The string
/// encompasses an extensive list of ccTLDs representing various countries
/// and territories, utilizing different scripts and alphabets.
///
/// The pattern is designed to match against:
/// - Latin script ccTLDs: such as `.uk`, `.eu`, `.us`.
/// - Non-Latin script ccTLDs: including Internationalized Domain Names (IDNs)
///   like `.中国`, `.ไทย`.
/// - Special characters: ccTLDs containing special characters like hyphens,
///   e.g., `.co.uk`.
/// Very cursed long line to get this done
public const val validCctld = "(?:(?:" +
        "한국|香港|澳門|新加坡|台灣|台湾|中國|中国|გე|ລາວ|ไทย|ලංකා|ഭാരതം|ಭಾರತ|భారత్|சிங்கப்பூர்|இலங்கை|இந்தியா|ଭାରତ|" +
        "ભારત|ਭਾਰਤ|ভাৰত|ভারত|বাংলা|भारोत|भारतम्|भारत|ڀارت|پاکستان|موريتانيا|مليسيا|مصر|قطر|فلسطين|عمان|" +
        "عراق|سورية|سودان|تونس|بھارت|بارت|ایران|امارات|المغرب|السعودية|الجزائر|البحرين|الاردن|հայ|қаз|" +
        "укр|срб|рф|мон|мкд|ею|бел|бг|ευ|ελ|zw|zm|za|yt|ye|ws|wf|vu|vn|vi|vg|ve|vc|va|uz|uy|us|um|uk|" +
        "ug|ua|tz|tw|tv|tt|tr|tp|to|tn|tm|tl|tk|tj|th|tg|tf|td|tc|sz|sy|sx|sv|su|st|ss|sr|so|sn|sm|sl|" +
        "sk|sj|si|sh|sg|se|sd|sc|sb|sa|rw|ru|rs|ro|re|qa|py|pw|pt|ps|pr|pn|pm|pl|pk|ph|pg|pf|pe|pa|om|" +
        "nz|nu|nr|np|no|nl|ni|ng|nf|ne|nc|na|mz|my|mx|mw|mv|mu|mt|ms|mr|mq|mp|mo|mn|mm|ml|mk|mh|mg|mf|" +
        "me|md|mc|ma|ly|lv|lu|lt|ls|lr|lk|li|lc|lb|la|kz|ky|kw|kr|kp|kn|km|ki|kh|kg|ke|jp|jo|jm|je|it|" +
        "is|ir|iq|io|in|im|il|ie|id|hu|ht|hr|hn|hm|hk|gy|gw|gu|gt|gs|gr|gq|gp|gn|gm|gl|gi|gh|gg|gf|ge|" +
        "gd|gb|ga|fr|fo|fm|fk|fj|fi|eu|et|es|er|eh|eg|ee|ec|dz|do|dm|dk|dj|de|cz|cy|cx|cw|cv|cu|cr|co|" +
        "cn|cm|cl|ck|ci|ch|cg|cf|cd|cc|ca|bz|by|bw|bv|bt|bs|br|bq|bo|bn|bm|bl|bj|bi|bh|bg|bf|be|bd|bb|" +
        "ba|az|ax|aw|au|at|as|ar|aq|ao|an|am|al|ai|ag|af|ae|ad|ac" +
        """)(?=[^0-9a-zA-Z@+-]|$))"""
public const val validGtld = "(?:(?:" +
        "삼성|닷컴|닷넷|香格里拉|餐厅|食品|飞利浦|電訊盈科|集团|通販|购物|谷歌|诺基亚|联通|网络|网站|网店|网址|组织机构|移动|珠宝|点看|游戏|淡马锡|机构|書籍|时尚|新闻|" +
        "政府|政务|招聘|手表|手机|我爱你|慈善|微博|广东|工行|家電|娱乐|天主教|大拿|大众汽车|在线|嘉里大酒店|嘉里|商标|商店|商城|公益|公司|八卦|健康|信息|佛山|企业|" +
        "中文网|中信|世界|ポイント|ファッション|セール|ストア|コム|グーグル|クラウド|みんな|คอม|संगठन|नेट|कॉम|همراه|موقع|موبايلي|كوم|" +
        "كاثوليك|عرب|شبكة|بيتك|بازار|العليان|ارامكو|اتصالات|ابوظبي|קום|сайт|рус|орг|онлайн|москва|ком|" +
        "католик|дети|zuerich|zone|zippo|zip|zero|zara|zappos|yun|youtube|you|yokohama|yoga|yodobashi|" +
        "yandex|yamaxun|yahoo|yachts|xyz|xxx|xperia|xin|xihuan|xfinity|xerox|xbox|wtf|wtc|wow|world|" +
        "works|work|woodside|wolterskluwer|wme|winners|wine|windows|win|williamhill|wiki|wien|whoswho|" +
        "weir|weibo|wedding|wed|website|weber|webcam|weatherchannel|weather|watches|watch|warman|" +
        "wanggou|wang|walter|walmart|wales|vuelos|voyage|voto|voting|vote|volvo|volkswagen|vodka|" +
        "vlaanderen|vivo|viva|vistaprint|vista|vision|visa|virgin|vip|vin|villas|viking|vig|video|" +
        "viajes|vet|versicherung|vermögensberatung|vermögensberater|verisign|ventures|vegas|vanguard|" +
        "vana|vacations|ups|uol|uno|university|unicom|uconnect|ubs|ubank|tvs|tushu|tunes|tui|tube|trv|" +
        "trust|travelersinsurance|travelers|travelchannel|travel|training|trading|trade|toys|toyota|" +
        "town|tours|total|toshiba|toray|top|tools|tokyo|today|tmall|tkmaxx|tjx|tjmaxx|tirol|tires|tips|" +
        "tiffany|tienda|tickets|tiaa|theatre|theater|thd|teva|tennis|temasek|telefonica|telecity|tel|" +
        "technology|tech|team|tdk|tci|taxi|tax|tattoo|tatar|tatamotors|target|taobao|talk|taipei|tab|" +
        "systems|symantec|sydney|swiss|swiftcover|swatch|suzuki|surgery|surf|support|supply|supplies|" +
        "sucks|style|study|studio|stream|store|storage|stockholm|stcgroup|stc|statoil|statefarm|" +
        "statebank|starhub|star|staples|stada|srt|srl|spreadbetting|spot|sport|spiegel|space|soy|sony|" +
        "song|solutions|solar|sohu|software|softbank|social|soccer|sncf|smile|smart|sling|skype|sky|" +
        "skin|ski|site|singles|sina|silk|shriram|showtime|show|shouji|shopping|shop|shoes|shiksha|shia|" +
        "shell|shaw|sharp|shangrila|sfr|sexy|sex|sew|seven|ses|services|sener|select|seek|security|" +
        "secure|seat|search|scot|scor|scjohnson|science|schwarz|schule|school|scholarships|schmidt|" +
        "schaeffler|scb|sca|sbs|sbi|saxo|save|sas|sarl|sapo|sap|sanofi|sandvikcoromant|sandvik|samsung|" +
        "samsclub|salon|sale|sakura|safety|safe|saarland|ryukyu|rwe|run|ruhr|rugby|rsvp|room|rogers|" +
        "rodeo|rocks|rocher|rmit|rip|rio|ril|rightathome|ricoh|richardli|rich|rexroth|reviews|review|" +
        "restaurant|rest|republican|report|repair|rentals|rent|ren|reliance|reit|reisen|reise|rehab|" +
        "redumbrella|redstone|red|recipes|realty|realtor|realestate|read|raid|radio|racing|qvc|quest|" +
        "quebec|qpon|pwc|pub|prudential|pru|protection|property|properties|promo|progressive|prof|" +
        "productions|prod|pro|prime|press|praxi|pramerica|post|porn|politie|poker|pohl|pnc|plus|" +
        "plumbing|playstation|play|place|pizza|pioneer|pink|ping|pin|pid|pictures|pictet|pics|piaget|" +
        "physio|photos|photography|photo|phone|philips|phd|pharmacy|pfizer|pet|pccw|pay|passagens|" +
        "party|parts|partners|pars|paris|panerai|panasonic|pamperedchef|page|ovh|ott|otsuka|osaka|" +
        "origins|orientexpress|organic|org|orange|oracle|open|ooo|onyourside|online|onl|ong|one|omega|" +
        "ollo|oldnavy|olayangroup|olayan|okinawa|office|off|observer|obi|nyc|ntt|nrw|nra|nowtv|nowruz|" +
        "now|norton|northwesternmutual|nokia|nissay|nissan|ninja|nikon|nike|nico|nhk|ngo|nfl|nexus|" +
        "nextdirect|next|news|newholland|new|neustar|network|netflix|netbank|net|nec|nba|navy|natura|" +
        "nationwide|name|nagoya|nadex|nab|mutuelle|mutual|museum|mtr|mtpc|mtn|msd|movistar|movie|mov|" +
        "motorcycles|moto|moscow|mortgage|mormon|mopar|montblanc|monster|money|monash|mom|moi|moe|moda|" +
        "mobily|mobile|mobi|mma|mls|mlb|mitsubishi|mit|mint|mini|mil|microsoft|miami|metlife|merckmsd|" +
        "meo|menu|men|memorial|meme|melbourne|meet|media|med|mckinsey|mcdonalds|mcd|mba|mattel|" +
        "maserati|marshalls|marriott|markets|marketing|market|map|mango|management|man|makeup|maison|" +
        "maif|madrid|macys|luxury|luxe|lupin|lundbeck|ltda|ltd|lplfinancial|lpl|love|lotto|lotte|" +
        "london|lol|loft|locus|locker|loans|loan|llp|llc|lixil|living|live|lipsy|link|linde|lincoln|" +
        "limo|limited|lilly|like|lighting|lifestyle|lifeinsurance|life|lidl|liaison|lgbt|lexus|lego|" +
        "legal|lefrak|leclerc|lease|lds|lawyer|law|latrobe|latino|lat|lasalle|lanxess|landrover|land|" +
        "lancome|lancia|lancaster|lamer|lamborghini|ladbrokes|lacaixa|kyoto|kuokgroup|kred|krd|kpn|" +
        "kpmg|kosher|komatsu|koeln|kiwi|kitchen|kindle|kinder|kim|kia|kfh|kerryproperties|" +
        "kerrylogistics|kerryhotels|kddi|kaufen|juniper|juegos|jprs|jpmorgan|joy|jot|joburg|jobs|jnj|" +
        "jmp|jll|jlc|jio|jewelry|jetzt|jeep|jcp|jcb|java|jaguar|iwc|iveco|itv|itau|istanbul|ist|" +
        "ismaili|iselect|irish|ipiranga|investments|intuit|international|intel|int|insure|insurance|" +
        "institute|ink|ing|info|infiniti|industries|inc|immobilien|immo|imdb|imamat|ikano|iinet|ifm|" +
        "ieee|icu|ice|icbc|ibm|hyundai|hyatt|hughes|htc|hsbc|how|house|hotmail|hotels|hoteles|hot|" +
        "hosting|host|hospital|horse|honeywell|honda|homesense|homes|homegoods|homedepot|holiday|" +
        "holdings|hockey|hkt|hiv|hitachi|hisamitsu|hiphop|hgtv|hermes|here|helsinki|help|healthcare|" +
        "health|hdfcbank|hdfc|hbo|haus|hangout|hamburg|hair|guru|guitars|guide|guge|gucci|guardian|" +
        "group|grocery|gripe|green|gratis|graphics|grainger|gov|got|gop|google|goog|goodyear|goodhands|" +
        "goo|golf|goldpoint|gold|godaddy|gmx|gmo|gmbh|gmail|globo|global|gle|glass|glade|giving|gives|" +
        "gifts|gift|ggee|george|genting|gent|gea|gdn|gbiz|gay|garden|gap|games|game|gallup|gallo|" +
        "gallery|gal|fyi|futbol|furniture|fund|fun|fujixerox|fujitsu|ftr|frontier|frontdoor|frogans|" +
        "frl|fresenius|free|fox|foundation|forum|forsale|forex|ford|football|foodnetwork|food|foo|fly|" +
        "flsmidth|flowers|florist|flir|flights|flickr|fitness|fit|fishing|fish|firmdale|firestone|fire|" +
        "financial|finance|final|film|fido|fidelity|fiat|ferrero|ferrari|feedback|fedex|fast|fashion|" +
        "farmers|farm|fans|fan|family|faith|fairwinds|fail|fage|extraspace|express|exposed|expert|" +
        "exchange|everbank|events|eus|eurovision|etisalat|esurance|estate|esq|erni|ericsson|equipment|" +
        "epson|epost|enterprises|engineering|engineer|energy|emerck|email|education|edu|edeka|eco|eat|" +
        "earth|dvr|dvag|durban|dupont|duns|dunlop|duck|dubai|dtv|drive|download|dot|doosan|domains|" +
        "doha|dog|dodge|doctor|docs|dnp|diy|dish|discover|discount|directory|direct|digital|diet|" +
        "diamonds|dhl|dev|design|desi|dentist|dental|democrat|delta|deloitte|dell|delivery|degree|"      +
        "deals|dealer|deal|dds|dclk|day|datsun|dating|date|data|dance|dad|dabur|cyou|cymru|cuisinella|"  +
        "csc|cruises|cruise|crs|crown|cricket|creditunion|creditcard|credit|cpa|courses|coupons|coupon|" +
        "country|corsica|coop|cool|cookingchannel|cooking|contractors|contact|consulting|construction|"  +
        "condos|comsec|computer|compare|company|community|commbank|comcast|com|cologne|college|coffee|"  +
        "codes|coach|clubmed|club|cloud|clothing|clinique|clinic|click|cleaning|claims|cityeats|city|"   +
        "citic|citi|citadel|cisco|circle|cipriani|church|chrysler|chrome|christmas|chloe|chintai|cheap|" +
        "chat|chase|charity|channel|chanel|cfd|cfa|cern|ceo|center|ceb|cbs|cbre|cbn|cba|catholic|"      +
        "catering|cat|casino|cash|caseih|case|casa|cartier|cars|careers|career|care|cards|caravan|car|" +
        "capitalone|capital|capetown|canon|cancerresearch|camp|camera|cam|calvinklein|call|cal|cafe|"  +
        "cab|bzh|buzz|buy|business|builders|build|bugatti|budapest|brussels|brother|broker|broadway|" +
        "bridgestone|bradesco|box|boutique|bot|boston|bostik|bosch|boots|booking|book|boo|bond|bom|" +
        "bofa|boehringer|boats|bnpparibas|bnl|bmw|bms|blue|bloomberg|blog|blockbuster|blanco|"         +
        "blackfriday|black|biz|bio|bingo|bing|bike|bid|bible|bharti|bet|bestbuy|best|berlin|bentley|" +
        "beer|beauty|beats|bcn|bcg|bbva|bbt|bbc|bayern|bauhaus|basketball|baseball|bargains|barefoot|" +
        "barclays|barclaycard|barcelona|bar|bank|band|bananarepublic|banamex|baidu|baby|azure|axa|aws|" +
        "avianca|autos|auto|author|auspost|audio|audible|audi|auction|attorney|athleta|associates|asia|" +
        "asda|arte|art|arpa|army|archi|aramco|arab|aquarelle|apple|app|apartments|aol|anz|anquan|" +
        "android|analytics|amsterdam|amica|amfam|amex|americanfamily|americanexpress|alstom|alsace|" +
        "ally|allstate|allfinanz|alipay|alibaba|alfaromeo|akdn|airtel|airforce|airbus|aigo|aig|agency|" +
        "agakhan|africa|afl|afamilycompany|aetna|aero|aeg|adult|ads|adac|actor|active|aco|accountants|" +
        "accountant|accenture|academy|abudhabi|abogado|able|abc|abbvie|abbott|abb|abarth|aarp|aaa|" +
        """onion)(?=[^0-9a-zA-Z@+-]|${"$"}))"""
public const val validPunycode = """(?:xn--[\-0-9a-z]+)"""
public const val validSubdomain = """(?:(?:$validDomainChars(?:[_-]|$validDomainChars)*)?$validDomainChars\.)"""
public const val validDomain = "(?:$validSubdomain*$validDomainName(?:$validGtld|$validCctld|$validPunycode))"
public const val validUrlPrecedingChars = "(?:[^A-Za-z0-9@＠\$#＃$invalidCharsGroup]|[$directionalMarkers]|^)"
public const val latinAccentChars = """\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u00FF\u0100-\u024F\u0253\u0254\u0256\u0257\u0259\u025B\u0263\u0268\u026F\u0272\u0289\u028B\u02BB\u0300-\u036F\u1E00-\u1EFF"""
public const val cyrillicLettersAndMarks = """\u0400-\u04FF"""
public const val validUrlQueryChars = """[a-z0-9!?\*'@\(\);:&=\+\${'$'}/%#\[\]\-_\.,~|]"""
public const val validUrlQueryEndingChars = """[a-z0-9\-_&=#/]"""
public const val validGeneralUrlPathChars = """[a-z${cyrillicLettersAndMarks}0-9!\*';:=\+,\.\${'$'}/%#\[\]\-\u2013_~@\|&$latinAccentChars]"""
public const val validUrlBalancedParens = """\((?:$validGeneralUrlPathChars+|(?:$validGeneralUrlPathChars*\($validGeneralUrlPathChars+\)$validGeneralUrlPathChars*))\)"""
public const val validUrlPathEndingChars = """[\+\-a-z${cyrillicLettersAndMarks}0-9=_#/$latinAccentChars]|(?:$validUrlBalancedParens)"""
public const val validUrlPath = "(?:(?:$validGeneralUrlPathChars(?:$validUrlBalancedParens$validGeneralUrlPathChars*)*$validUrlPathEndingChars)|(?:@$validGeneralUrlPathChars+/))"
public const val validUrl = "(" +
        "($validUrlPrecedingChars)" +
        "(" +
        "(https?:\\/\\/)?" +
        "($validDomain)" +
        "(?::($validPortNumber))?" +
        "(\\/$validUrlPath*)?" +
        "(\\?$validUrlQueryChars*$validUrlQueryEndingChars)?" +
        ")" + ")"
public val urlRegex = Regex(validUrl)
public const val validMentionPrecedingChars = """(?:^|[^a-zA-Z0-9_!#${'$'}%&*@＠]|(?:^|[^a-zA-Z0-9_+~.-]))"""
public const val validMention = "($validMentionPrecedingChars)([@])($validDomain)"
public val mentionRegex = Regex(validMention)
public val whitespageRegex = Regex("""/ +(?=\n)""")
public val whitespageEofRegex = Regex("""/\s+${'$'}| +(?=\n)""")