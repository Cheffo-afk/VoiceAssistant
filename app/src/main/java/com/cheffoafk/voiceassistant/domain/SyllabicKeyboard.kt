package com.cheffoafk.voiceassistant.domain

/**
 * Gruppo di sillabe con la stessa consonante iniziale.
 * @param label  Etichetta mostrata nel selettore (es. "B", "ST").
 * @param syllables  Lista di sillabe del gruppo (es. ["BA","BE","BI","BO","BU"]).
 */
data class SyllabicGroup(val label: String, val syllables: List<String>)

/** Vocali sempre disponibili in cima alla tastiera. */
val SYLLABIC_VOWELS = listOf("A", "E", "I", "O", "U", "Y")

/**
 * Tutti i gruppi consonantici dell'italiano, ordinati alfabeticamente per label.
 * Aggiungere o rimuovere gruppi da qui per modificare la tastiera senza toccare l'UI.
 */
val SYLLABIC_GROUPS: List<SyllabicGroup> = listOf(
    SyllabicGroup("B",  listOf("BA", "BE", "BI", "BO", "BU")),
    SyllabicGroup("BB", listOf("BBA", "BBE", "BBI", "BBO", "BBU")),
    SyllabicGroup("BBR",listOf("BBRA","BBRE","BBRI","BBRO","BBRU")),
    SyllabicGroup("BR", listOf("BRA", "BRE", "BRI", "BRO", "BRU")),
    SyllabicGroup("C",  listOf("CA", "CE", "CI", "CO", "CU")),
    SyllabicGroup("CC", listOf("CCA", "CCE", "CCI", "CCO", "CCU")),
    SyllabicGroup("CCR",listOf("CCRA","CCRE","CCRI","CCRO","CCRU")),
    SyllabicGroup("CH", listOf("CHE", "CHI")),
    SyllabicGroup("CL", listOf("CLA", "CLE", "CLI", "CLO", "CLU")),
    SyllabicGroup("CR", listOf("CRA", "CRE", "CRI", "CRO", "CRU")),
    SyllabicGroup("D",  listOf("DA", "DE", "DI", "DO", "DU")),
    SyllabicGroup("DD", listOf("DDA", "DDE", "DDI", "DDO", "DDU")),
    SyllabicGroup("DDR",listOf("DDRA","DDRE","DDRI","DDRO","DDRU")),
    SyllabicGroup("DR", listOf("DRA", "DRE", "DRI", "DRO", "DRU")),
    SyllabicGroup("F",  listOf("FA", "FE", "FI", "FO", "FU")),
    SyllabicGroup("FF", listOf("FFA", "FFE", "FFI", "FFO", "FFU")),
    SyllabicGroup("FFR",listOf("FFRA","FFRE","FFRI","FFRO","FFRU")),
    SyllabicGroup("FR", listOf("FRA", "FRE", "FRI", "FRO", "FRU")),
    SyllabicGroup("G",  listOf("GA", "GE", "GI", "GO", "GU")),
    SyllabicGroup("GG", listOf("GGA", "GGE", "GGI", "GGO", "GGU")),
    SyllabicGroup("GGR",listOf("GGRA","GGRE","GGRI","GGRO","GGRU")),
    SyllabicGroup("GH", listOf("GHE", "GHI")),
    SyllabicGroup("GL", listOf("GLI", "GLA", "GLE", "GLO", "GLU")),
    SyllabicGroup("GN", listOf("GNA", "GNE", "GNI", "GNO", "GNU")),
    SyllabicGroup("GR", listOf("GRA", "GRE", "GRI", "GRO", "GRU")),
    SyllabicGroup("J",  listOf("JA", "JE", "JI", "JO", "JU")),
    SyllabicGroup("K",  listOf("KA", "KE", "KI", "KO", "KU")),
    SyllabicGroup("L",  listOf("LA", "LE", "LI", "LO", "LU")),
    SyllabicGroup("LL", listOf("LLA", "LLE", "LLI", "LLO", "LLU")),
    SyllabicGroup("LLR",listOf("LLRA","LLRE","LLRI","LLRO","LLRU")),
    SyllabicGroup("M",  listOf("MA", "ME", "MI", "MO", "MU")),
    SyllabicGroup("MM", listOf("MMA", "MME", "MMI", "MMO", "MMU")),
    SyllabicGroup("MMR",listOf("MMRA","MMRE","MMRI","MMRO","MMRU")),
    SyllabicGroup("N",  listOf("NA", "NE", "NI", "NO", "NU")),
    SyllabicGroup("NN", listOf("NNA", "NNE", "NNI", "NNO", "NNU")),
    SyllabicGroup("NNR",listOf("NNRA","NNRE","NNRI","NNRO","NNRU")),
    SyllabicGroup("P",  listOf("PA", "PE", "PI", "PO", "PU")),
    SyllabicGroup("PP", listOf("PPA", "PPE", "PPI", "PPO", "PPU")),
    SyllabicGroup("PPR",listOf("PPRA","PPRE","PPRI","PPRO","PPRU")),
    SyllabicGroup("PR", listOf("PRA", "PRE", "PRI", "PRO", "PRU")),
    SyllabicGroup("Q",  listOf("QA", "QE", "QI", "QO", "QU")),
    SyllabicGroup("QU", listOf("QUA", "QUE", "QUI", "QUO")),
    SyllabicGroup("R",  listOf("RA", "RE", "RI", "RO", "RU")),
    SyllabicGroup("RR", listOf("RRA", "RRE", "RRI", "RRO", "RRU")),
    SyllabicGroup("RRS",listOf("RRSA","RRSE","RRSI","RRSO","RRSU")),
    SyllabicGroup("S",  listOf("SA", "SE", "SI", "SO", "SU")),
    SyllabicGroup("SC", listOf("SCA", "SCE", "SCI", "SCO", "SCU")),
    SyllabicGroup("SP", listOf("SPA", "SPE", "SPI", "SPO", "SPU")),
    SyllabicGroup("SS", listOf("SSA", "SSE", "SSI", "SSO", "SSU")),
    SyllabicGroup("SST",listOf("SSTA","SSTE","SSTI","SSTO","SSTU")),
    SyllabicGroup("ST", listOf("STA", "STE", "STI", "STO", "STU")),
    SyllabicGroup("STR",listOf("STRA","STRE","STRI","STRO","STRU")),
    SyllabicGroup("T",  listOf("TA", "TE", "TI", "TO", "TU")),
    SyllabicGroup("TT", listOf("TTA", "TTE", "TTI", "TTO", "TTU")),
    SyllabicGroup("TTR",listOf("TTRA","TTRE","TTRI","TTRO","TTRU")),
    SyllabicGroup("TR", listOf("TRA", "TRE", "TRI", "TRO", "TRU")),
    SyllabicGroup("V",  listOf("VA", "VE", "VI", "VO", "VU")),
    SyllabicGroup("VV", listOf("VVA", "VVE", "VVI", "VVO", "VVU")),
    SyllabicGroup("W",  listOf("WA", "WE", "WI", "WO", "WU")),
    SyllabicGroup("X",  listOf("XA", "XE", "XI", "XO", "XU")),
    SyllabicGroup("Z",  listOf("ZA", "ZE", "ZI", "ZO", "ZU")),
    SyllabicGroup("ZZ", listOf("ZZA", "ZZE", "ZZI", "ZZO", "ZZU")),
)

