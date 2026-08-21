/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2024 Trashbox Bobylev
 *
 * Eternity Pixel Dungeon
 * Copyright (C) 2026 Eternity Pixel Dungeon Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.messages;

import java.util.Locale;

public enum Languages {
	ENGLISH("english",      "",   Status.O_COMPLETE,   null, null),
	SPANISH("español",      "es", Status.O_COMPLETE,   new String[]{"Aurelio", "Patryck", "Zanarok"}, new String[]{"Aarón", "Aether", "Akron", "Aleksandr", "Alguien", "Anthony", "Arturo", "Aurelio", "Carlos", "Dalt", "Diego", "Dio", "Eduardo", "Ezequiel", "Gabriel", "Gaston", "Ignacio", "Iván", "Jesús", "José", "Juan", "Lauty", "Luis", "Manuel", "Mario", "Matías", "Maximiliano", "Miguel", "Nicolás", "Pablo", "Patryck", "Pedro", "Rafael", "Rodrigo", "Santiago", "Sebastián", "Sergio", "Tomás", "Valentín", "Vicente", "Víctor", "Zanarok"}),
	RUSSIAN("русский",      "ru", Status.O_COMPLETE, new String[]{"Anisim", "Beded", "Dark_Despair", "Dishonored99", "Dmitry", "Double_Key", "Dragon200", "Fatality", "Goo", "Kikuru", "Kinash", "M0use", "Mister_V", "Neko", "Nexus", "Nik", "Nikita", "Omikron", "Pavel", "Sergey", "Vadim", "Vesel", "Vlad", "Vladimir", "Yury", "Zombie"}, new String[]{"Anisim", "Beded", "Dark_Despair", "Dishonored99", "Dmitry", "Double_Key", "Dragon200", "Fatality", "Goo", "Kikuru", "Kinash", "M0use", "Mister_V", "Neko", "Nexus", "Nik", "Nikita", "Omikron", "Pavel", "Sergey", "Vadim", "Vesel", "Vlad", "Vladimir", "Yury", "Zombie"}),
	PORTUGUESE("português", "pt", Status.O_COMPLETE, new String[]{"Alexandre", "Arthur", "Bruno", "Carlos", "Diego", "Eduardo", "Felipe", "Gabriel", "Guilherme", "Gustavo", "Henrique", "Igor", "João", "Lucas", "Matheus", "Pedro", "Rafael", "Rodrigo", "Thiago", "Victor", "Vinicius"}, new String[]{"Alexandre", "Arthur", "Bruno", "Carlos", "Diego", "Eduardo", "Felipe", "Gabriel", "Guilherme", "Gustavo", "Henrique", "Igor", "João", "Lucas", "Matheus", "Pedro", "Rafael", "Rodrigo", "Thiago", "Victor", "Vinicius"}),
	GERMAN("deutsch",       "de", Status.__UNREVIEW, new String[]{"Dallukas", "KrystalCroft", "Wuzzy", "Zap0", "apxwn", "bernhardreiter", "davedude"}, new String[]{"2711chrissi", "Abracadabra", "Anaklysmos", "Ceeee", "DarkPixel", "David.transifex", "EmilKevinManuel", "ErichME", "Faquarl", "JorahEtLabora", "LenzB", "MacMoff", "Micksha", "Niseko", "Ordoviz", "Sarius", "Shtynow", "SirEddi", "Sorpl3x", "SurmanPP", "SwissQ", "ThunfischGott", "Timo_S", "Topicranger", "azrdev", "carrageen", "dome.scheidler", "galactictrans", "gekko303", "jeinzi", "johannes.schobel", "karoshi42", "koryphea", "luciocarreras", "lukasghesse", "mklr", "niemand", "oragothen", "razzifazzi0", "spixi", "tanjay", "unbekannterTyp", "wunst"}),
	CHINESE("中文",         "zh", Status.O_COMPLETE, new String[]{"Chronie_Lynn_Iwa", "Jinkeloid(zdx00793)", "endlesssolitude", "catand"}, new String[]{"931451545", "Budding", "Fatir", "Fishbone", "Hcat", "HoofBumpBlurryface", "Horr_lski", "Lery", "Lyn_0401", "Lyx0527", "Ooooscar", "RainSlide", "ShatteredFlameBlast", "SpaceAnchor", "Teller", "hmdzl001", "leo", "tempest102", "catand"}),
	CHI_TRAD("繁體中文",     "zh-hant", Status.__UNREVIEW, new String[]{"JZR", "Yichm", "p2635"}, new String[]{"DT227", "Fishbone", "Ken4Ro", "Lstron", "Relrin167", "Sotis425", "Zoe096423", "arnolam", "jackymaxj", "redbrow", "shiba"}),
	KOREAN("한국어",        "ko", Status.O_COMPLETE, new String[]{"Bae", "Choi", "Jung", "Kang", "Kim", "Lee", "Park", "Yoon"}, new String[]{"Bae", "Choi", "Jung", "Kang", "Kim", "Lee", "Park", "Yoon"}),
	FRENCH("français",      "fr", Status.O_COMPLETE, new String[]{"Alexandre", "Antoine", "Clément", "Julien", "Lucas", "Maxime", "Nicolas", "Pierre", "Romain", "Thomas", "Valentin"}, new String[]{"Alexandre", "Antoine", "Clément", "Julien", "Lucas", "Maxime", "Nicolas", "Pierre", "Romain", "Thomas", "Valentin"}),
	JAPANESE("日本語",      "ja", Status.O_COMPLETE, new String[]{"daingewuvzeevisiddfddd", "oz51199"}, new String[]{"Gosamaru", "NickZhrbin", "Otogiri", "Siraore_Rou", "amama", "grassedge", "kiyofumimanabe", "librada", "mocklike", "tomofumikitano"}),
	POLISH("polski",        "pl", Status.__UNREVIEW, new String[]{"Daniel Witanski", "Deksippos", "MrKukurykpl", "chronon", "kuadziw", "szymex73"}, new String[]{"Akmetari", "AntiTime", "Boguc", "Chasseur", "Ciechu", "Darden", "DarkKnightComes", "DogeseleQ", "GRan0000", "Hammil", "I256I", "KarixDaii", "KrnabrnyOlaf", "Lufix", "MJedi", "MrCommander", "Odiihinia", "Ostsee0912", "Peperos", "RolsoN", "Scharnvirk", "Serpens13", "Tangens", "VasteelXolotl", "Voyteq", "Wiiiiiii", "bobas10", "bogumilg", "bvader95", "dusakus", "elchudy", "jajkoswinka", "michaub", "mikolka9144", "ozziezombie", "szczoteczka22", "taki1", "transportowiec96"}),
	ITALIAN("italiano",     "it", Status.__UNREVIEW, new String[]{"MottledElm", "NeoAugustus", "bizzolino", "funnydwarf", "inkubo87"}, new String[]{"4est", "Danelix", "DaniMare", "Danzl", "Dj1234", "Eriliken", "Esse78", "Guiller124", "Hydr46605", "IoannesMaria", "LN_90", "Mat323", "Mister64", "Noostale", "PicchiSeba", "Tugamer89", "Tysal", "andrea049ita", "andreafaffo", "andrearubbino00", "angelica.caruso", "cantarini", "carinellialessandro31", "dmytro.tokayev", "lorenzofrosi05", "mamon68596", "mattiuw", "max1234ita", "maxifire32", "nessunluogo", "righi.a", "umby000", "unknown888", "valerio.bozzolan"}),
	TURKISH("türkçe",       "tr", Status.__UNREVIEW, new String[]{"LokiofMillenium", "Mustafa.10", "T3kin5iZ", "emrebnk", "gorkem_yılmaz"}, new String[]{"AGORAAA", "AchernarPrime", "AcuriousPotato", "BurningDaylight", "ErenayDev", "Helgon", "Koga", "Mehmet_Emin_21", "MuratEfeYilmaz", "OzanAlkan", "TR_Muhittin", "Talha_0_0", "TheMBDsvs", "Yllcare", "YORGANSIZMTAV", "ahmetbakicakir", "akkaya.mustafa", "alikeremozfidan", "alpekin98", "barankrky", "denizakalin", "eraysall402", "erdemozdemir98", "hasantahsin160", "immortalsamuraicn", "kayikyaki", "kempilbey", "melezorus34", "mitux", "mustafadoslu", "ryuga", "superDpermn", "utkanozer13", "yasirckr85", "yukete"}),
	VIETNAMESE("tiếng việt","vi", Status.O_COMPLETE, new String[]{"Chuseko", "The_Hood", "nguyenanhkhoapythus"}, new String[]{"BlueSheepAlgodoo", "Phuc2401", "SpaceMetropolis", "Teh_boi", "Threyja", "Toluu", "bruhwut", "buicongminh_t63", "deadlevel13", "duongfg250", "h4ndy_c4ndy", "hniV", "khangxyz3g", "ngolamaz3", "nkhhu", "vdgiapp", "vtvinh24"}),
	UKRANIAN("українська",  "uk", Status.O_COMPLETE, new String[]{"Oster", "Snikewin", "zhushman00"}, new String[]{"AlexFenixUA", "Buster54", "Doodlinka", "Dotsent", "Lyttym", "MaxQuiet", "Mops", "Sadsaltan1", "TarasUA", "TheGuyBill", "Tomfire", "Volkov", "ZverWolf", "_bor_", "alexfenixva", "ddmaster3463", "filalex77", "holuydadko", "ingvarfed", "iu0v1", "jesternotricks", "lezzen", "myshokoleksander05", "oliolioxinfree", "qweez", "romanokurg", "so1der", "sterenkevicsasa", "vlisivka", "xojltoh", "yukete", "zhawty"}),
	INDONESIAN("indonesia", "in", Status.__UNREVIEW, new String[]{"RF_4R4F1_03", "rakapratama"}, new String[]{"An_Ironstone", "Flasherx", "INDRA_SYAHPUTRA", "Izulhaaq", "Karanh", "M.Bintang.K", "PineFirebloom", "QiuQiuQi", "Ruzz_Axleod", "Taka31", "ZakyM313", "ZangieF347", "aachunemiku", "anagakenny24", "aryasatya_arifien", "atmorojo", "di9526985", "esprogarap", "hatsunnimiku", "icebearwand", "kirimaja", "lupar21", "luthfidzaky_ldzy", "mkakhsan301", "nicoalvito", "noeldycreator", "oolek", "wisnugafur"}),
	CZECH("čeština",        "cs", Status.__UNREVIEW, new String[]{"16cnovotny", "ObisMike", "novotnyvaclav"}, new String[]{"AshenShugar", "Autony", "Block_Vader", "Buba237", "JStrange", "Nerdiniel", "Patrik123", "RealBrofessor", "Thorn_123", "chuckjirka", "emteckos2", "kristanka", "luhan.lukas"}),
	DUTCH("nederlands",     "nl", Status.O_COMPLETE, new String[]{"AlbertBrand", "Mvharen"}, new String[]{"AvanLieshout", "Blokheck011", "Frankwert", "Gehenna", "Valco", "ZephyrZodiac", "link200023", "ojppe", "rmw", "th3f4llenh0rr0r"}),
	SWEDISH("svenska",      "sv", Status.__UNREVIEW, new String[]{"leowitchhh", "yeager"}, new String[]{"KeyB", "Moistmemesneverlie", "antonaut", "dotMavriQ"}),
	HUNGARIAN("magyar",     "hu", Status.O_COMPLETE, new String[]{"dorheim", "summoner001", "szalaik"}, new String[]{"Csanevox", "Navetelen", "acszoltan111", "balazsszalab", "clarovani", "dhialub", "nanometer", "nardomaa", "savarall", "szemetvodor"}),
	GREEK("ελληνικά",       "el", Status.X_UNFINISH, new String[]{"Aeonius", "Saxy"}, new String[]{"DU_Clouds", "VasKyr", "YiorgosH", "fr3sh", "nikolaoskelirakis", "stefboi", "toumbo", "val.exe"}),
	BELARUSIAN("беларуская","be", Status.X_UNFINISH, new String[]{"AprilRain(Vadzim Navumau)"}, new String[]{"4ebotar", "Loentrin"}),
	ESPERANTO("esperanto",  "eo", Status.O_COMPLETE, new String[]{"Verdulo"}, new String[]{"Raizin", "Rwelean", "kameluloj"});

	public enum Status{
		X_UNFINISH, //unfinished, ~80-99% translated
		__UNREVIEW, //unreviewed, but 100% translated
		O_COMPLETE, //complete, 100% reviewed
	}

	private String name;
	private String code;
	private Status status;
	private String[] reviewers;
	private String[] translators;

	Languages(String name, String code, Status status, String[] reviewers, String[] translators){
		this.name = name;
		this.code = code;
		this.status = status;
		this.reviewers = reviewers;
		this.translators = translators;
	}

	public String nativeName(){
		return name;
	}

	public String code(){
		return code;
	}

	public Status status(){
		return status;
	}

	public String[] reviewers() {
		if (reviewers == null) return new String[]{};
		else return reviewers.clone();
	}

	public String[] translators() {
		if (translators == null) return new String[]{};
		else return translators.clone();
	}

	public static Languages matchLocale(Locale locale){
		if (locale.getLanguage().equals("zh") && locale.toString().contains("Hant")){
			return Languages.CHI_TRAD;
		}
		return matchCode(locale.getLanguage());
	}

	public static Languages matchCode(String code){
		for (Languages lang : Languages.values()){
			if (lang.code().equals(code))
				return lang;
		}
		return ENGLISH;
	}
}
