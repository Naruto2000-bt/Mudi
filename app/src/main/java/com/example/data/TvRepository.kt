package com.example.data

data class Channel(
    val id: String,
    val name: String,
    val frequency: String,
    val logoUrl: String,
    val streamUrl: String,
    val backupUrl: String,
    val description: String,
    val isWorldCupBroadcaster: Boolean = false,
    val category: String = "Nacional" // Nacional, Deportes, Regional, Cultural
)

data class Program(
    val id: String,
    val channelId: String,
    val title: String,
    val description: String,
    val startTime: String, // HH:mm format
    val endTime: String,   // HH:mm format
    val genre: String,     // Noticias, Deportes, Revista, Comedia, Cultural
    val isLive: Boolean = false
)

data class OnDemandContent(
    val id: String,
    val title: String,
    val category: String, // Comedia, Cultura, Documentales, Cine Tico
    val videoUrl: String,
    val duration: String, // e.g., "45 min"
    val description: String,
    val thumbnail: String,
    val year: String,
    val rating: String // APT, TEENS, +18
)

data class WorldCupMatch(
    val id: String,
    val teamA: String,
    val teamB: String,
    val flagA: String, // Emoji flag
    val flagB: String, // Emoji flag
    val date: String,  // e.g., "Martes 16 de Junio"
    val timeCR: String, // Costa Rica time (UTC-6)
    val stadium: String,
    val group: String,
    val status: String, // PROXIMO, EN_VIVO, FINALIZADO
    val scoreA: Int? = null,
    val scoreB: Int? = null,
    val minute: String? = null, // e.g. "45'"
    val broadcasterChannelIds: List<String> = emptyList()
)

object TvRepository {

    // Stable streaming URLs for flawless ExoPlayer experience.
    // Using widely online, high-performance streaming URLs to ensure 100% playability.
    private const val STREAM_HLS_1 = "https://playouts.redbull.tv/live/redbulltv_international/playlist.m3u8"
    private const val STREAM_HLS_2 = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"
    private const val STREAM_HLS_3 = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
    // Progressive MP4 files for on-demand streaming
    private const val VIDEO_MP4_1 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
    private const val VIDEO_MP4_2 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
    private const val VIDEO_MP4_3 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutback.mp4"
    private const val VIDEO_MP4_4 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

    val channels = listOf(
        Channel(
            id = "teletica_7",
            name = "Teletica Canal 7",
            frequency = "VHF 7 / HD 7.1",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/e/e0/Teletica_Logo.png",
            streamUrl = STREAM_HLS_1,
            backupUrl = STREAM_HLS_3,
            description = "Principal canal generalista de Costa Rica, con noticias, realities, partidos de la Selección Nacional y transmisiones especiales.",
            isWorldCupBroadcaster = true,
            category = "Nacional"
        ),
        Channel(
            id = "repretel_6",
            name = "Repretel Canal 6",
            frequency = "VHF 6 / HD 6.1",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/c/c5/Logotipo_Repretel_Canal_6.png",
            streamUrl = STREAM_HLS_2,
            backupUrl = STREAM_HLS_1,
            description = "Cine, noticias, programas de concursos de la televisión tradicional tica y cobertura de eventos deportivos de primer nivel.",
            isWorldCupBroadcaster = true,
            category = "Nacional"
        ),
        Channel(
            id = "multimedios_8",
            name = "Multimedios CR Canal 8",
            frequency = "VHF 8 / HD 8.1",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/c/cb/Logo_Canal_8_Costa_Rica.png",
            streamUrl = STREAM_HLS_3,
            backupUrl = STREAM_HLS_2,
            description = "Canal de noticias de última hora, deportes en vivo como Fútbol al Día y programas interactivos de debate político y social.",
            category = "Regional"
        ),
        Channel(
            id = "sinart_13",
            name = "Sinart Canal 13",
            frequency = "VHF 13 / HD 13.1",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/f/fb/Canal_13_Costa_Rica_Logo.jpg",
            streamUrl = STREAM_HLS_2,
            backupUrl = STREAM_HLS_1,
            description = "El canal de televisión pública y cultural de Costa Rica. Enfocado en documentales, reportajes nacionales e identidad tica.",
            category = "Cultural"
        ),
        Channel(
            id = "teletica_deportes",
            name = "TD+ HD",
            frequency = "Cable Canal 15 / HD 715",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/d/de/Td_mas_logo.png",
            streamUrl = STREAM_HLS_1,
            backupUrl = STREAM_HLS_3,
            description = "La señal deportiva exclusiva favorita de Costa Rica. Cobertura completa del fútbol nacional, asambleas y el mundial de fútbol.",
            isWorldCupBroadcaster = true,
            category = "Deportes"
        ),
        Channel(
            id = "repretel_11",
            name = "Repretel Canal 11",
            frequency = "VHF 11 / HD 11.1",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/2/29/Logotipo_Repretel_Canal_11.png",
            streamUrl = STREAM_HLS_3,
            backupUrl = STREAM_HLS_2,
            description = "Programación con series juveniles, telenovelas internacionales y la edición popular de Informe 11: Las Historias.",
            category = "Nacional"
        )
    )

    // EPG schedules in Costa Rican local time (CST)
    val programsByChannel = mapOf(
        "teletica_7" to listOf(
            Program("p1_1", "teletica_7", "Telenoticias Edición Matutina", "Noticias nacionales e internacionales, clima del Valle Central y el tránsito de San José en vivo.", "06:00", "08:30", "Noticias"),
            Program("p1_2", "teletica_7", "Buen Día", "Revista matutina interactiva con expertos de salud, recetas, manualidades y consejos del hogar tico.", "08:30", "11:00", "Revista"),
            Program("p1_3", "teletica_7", "Qué Buena Tarde", "El show de variedades vespertino de Costa Rica, repleto de risas, juegos de mesa y carisma.", "11:00", "12:00", "Revista"),
            Program("p1_4", "teletica_7", "Telenoticias Edición Mediodía", "Análisis periodístico profundo, entrevistas exclusivas de la coyuntura costarricense.", "12:00", "13:30", "Noticias"),
            Program("p1_5", "teletica_7", "Copa Mundial FIFA: Costa Rica vs Alemania", "Señal en vivo desde el estadio mundialista. Con la narración del Team de Teletica Deportes.", "13:30", "16:30", "Deportes", isLive = true),
            Program("p1_6", "teletica_7", "De Boca en Boca", "Show de farándula nacional, entrevistas con creadores de contenido, artistas y famosos costarricenses.", "16:30", "18:00", "Revista"),
            Program("p1_7", "teletica_7", "7 Días", "Programa televisivo de investigación profunda sobre seguridad, política y economía de Costa Rica.", "18:00", "19:00", "Noticias"),
            Program("p1_8", "teletica_7", "Telenoticias Edición Estelar", "El noticiero estelar líder de la televisión nacional costarricense.", "19:00", "20:30", "Noticias"),
            Program("p1_9", "teletica_7", "El Chinamo de fin de año", "Humor picante, toros a la tica, concursos y toda la locura navideña costarricense.", "20:30", "23:00", "Comedia")
        ),
        "repretel_6" to listOf(
            Program("p2_1", "repretel_6", "Noticias Repretel Primer Turno", "Reporte temprano de los sucesos, accidentes de tránsito y opinión pública tica.", "06:00", "08:00", "Noticias"),
            Program("p2_2", "repretel_6", "La Película Matutina", "Cine de drama y aventura para toda la familia tica.", "08:00", "10:30", "Cine"),
            Program("p2_3", "repretel_6", "Giros", "El programa de revista matutino con recetas de cocina, ejercicios y medicina preventiva.", "10:30", "12:30", "Revista"),
            Program("p2_4", "repretel_6", "Copa Mundial FIFA: Argentina vs España", "Transmisión oficial de Repretel Deportes Canal 6 de la Copa del Mundo.", "12:30", "15:30", "Deportes", isLive = true),
            Program("p2_5", "repretel_6", "Combate Costa Rica", "Equipos azul y naranja se enfrentan en intensas pruebas físicas y de destreza en un show repleto de adrenalina.", "15:30", "18:00", "Entretenimiento"),
            Program("p2_6", "repretel_6", "Noticias Repretel Estelar", "Balance de noticias y noticias de última hora en el territorio nacional.", "18:00", "19:30", "Noticias"),
            Program("p2_7", "repretel_6", "La Selección de Humor", "Comediantes ticos de la radio nacional traen sketches y chistes tradicionales del folklore tico.", "19:30", "21:30", "Comedia"),
            Program("p2_8", "repretel_6", "CSI: Costa de Marfil", "Serie policíaca internacional de misterio.", "21:30", "23:00", "Drama")
        ),
        "multimedios_8" to listOf(
            Program("p3_1", "multimedios_8", "Telediario Al Minuto Matutino", "Noticias de última hora sobre San José, Alajuela, Heredia y Cartago al instante.", "06:00", "09:00", "Noticias"),
            Program("p3_2", "multimedios_8", "Fútbol al Día", "El show de debate futbolero más polémico de Costa Rica liderado por Diego Obando.", "09:00", "11:00", "Deportes"),
            Program("p3_3", "multimedios_8", "Telediario Al Minuto Mediodía", "La información con un enfoque muy cercano a la comunidad local.", "11:00", "13:00", "Noticias"),
            Program("p3_4", "multimedios_8", "La Gozadera", "Hablemos de música, cumbia, y entrevistas con grupos de música tropical nacionales.", "13:00", "15:00", "Revista"),
            Program("p3_5", "multimedios_8", "A Cachete con Luis Carlos Monge", "El late night show más popular de entrevistas profundas y cargadas de humor costarricense.", "15:00", "17:00", "Revista", isLive = true),
            Program("p3_6", "multimedios_8", "Telediario Estelar", "Noticias, deportes y opinión del pueblo costarricense.", "17:00", "19:00", "Noticias"),
            Program("p3_7", "multimedios_8", "Voz Pública", "Programa de investigación acerca del gasto público costarricense y municipalidades.", "19:00", "21:00", "Cultural"),
            Program("p3_8", "multimedios_8", "Cine de Estreno", "Películas clásicas de comedia.", "21:00", "23:00", "Cine")
        ),
        "sinart_13" to listOf(
            Program("p4_1", "sinart_13", "Costa Rica Noticias", "Servicio informativo estatal transparente centrado en salud pública, educación y regiones alejadas.", "07:00", "09:00", "Noticias"),
            Program("p4_2", "sinart_13", "Herederos del Folklor", "Serie de documentales que celebran las tradiciones musicales, cocina en leña de Nicoya e historia de Guanacaste.", "09:00", "11:00", "Cultural"),
            Program("p4_3", "sinart_13", "Café Nacional", "Hablemos de emprendimientos ticos, agricultura familiar de la zona de Los Santos e innovación local.", "11:00", "13:00", "Revista"),
            Program("p4_4", "sinart_13", "Documental: Parques Nacionales CR", "Imágenes asombrosas en ultra HD de la biodiversidad de Corcovado, Tortuguero y el Volcán Poás.", "13:00", "15:00", "Cultural", isLive = true),
            Program("p4_5", "sinart_13", "Música de la Orquesta Sinfónica Nacional", "Grabaciones especiales de gala del ensamble orquestal costarricense interpretando obras clásicas.", "15:00", "17:00", "Cultural"),
            Program("p4_6", "sinart_13", "Punto y Contrapunto", "Debate respetuoso y académico acerca del futuro energético e infraestructura del país.", "17:00", "19:00", "Debate"),
            Program("p4_7", "sinart_13", "Costa Rica Desde el Aire", "Visuales deslumbrantes con dron de las costas, cordilleras y atardeceres de Costa Rica.", "19:00", "21:00", "Cultural"),
            Program("p4_8", "sinart_13", "Teatro en la Casa", "Obras teatrales independientes de artistas locales escenificadas en vivo para televisión.", "21:00", "23:00", "Cultural")
        ),
        "teletica_deportes" to listOf(
            Program("p5_1", "teletica_deportes", "TD+ Noticias", "Fútbol nacional, ciclismo de ruta en San Carlos y boxeo costarricense.", "08:00", "10:00", "Deportes"),
            Program("p5_2", "teletica_deportes", "La Platea", "Polémica extrema de la de primera división con exjugadores icónicos del Saprissa y Alajuelense.", "10:00", "12:00", "Deportes"),
            Program("p5_3", "teletica_deportes", "Mundial al Día: Los Rivales", "Un repaso táctico de las selecciones del mundial, enfocándose en la escuadra nacional.", "12:00", "13:30", "Deportes"),
            Program("p5_4", "teletica_deportes", "PREVIA: Costa Rica vs Alemania", "Análisis con exseleccionados del histórico cotejo mundialista.", "13:30", "14:00", "Deportes", isLive = true),
            Program("p5_5", "teletica_deportes", "Mundial FIFA LIVE: Partidos y Especiales", "Streaming de alta definición del duelo Costa Rica vs Alemania.", "14:00", "16:30", "Deportes", isLive = true),
            Program("p5_6", "teletica_deportes", "La Libreta de TD+", "Análisis estadístico interactivo con tecnologías de punta sobre el rendimiento tico del mundial.", "16:30", "18:00", "Deportes"),
            Program("p5_7", "teletica_deportes", "Barlompa", "Risas, memes deportivos, la contracultura de las canchas de barrio y asambleas ticas.", "18:00", "20:00", "Deportes"),
            Program("p5_8", "teletica_deportes", "Resumen de la Jornada Mundialista", "Lo mejor, lo peor, goles y polémicas arbitrales de la fecha de hoy.", "20:00", "22:00", "Deportes")
        ),
        "repretel_11" to listOf(
            Program("p6_1", "repretel_11", "Noticias Repretel 11", "Noticiario ágil y cercano.", "09:00", "10:30", "Noticias"),
            Program("p6_2", "repretel_11", "Telenovela: Pasión de Gavilanes", "La aclamada telenovela romántica internacional.", "10:30", "12:00", "Drama"),
            Program("p6_3", "repretel_11", "Informe 11: Las Historias Match Especial", "Los misterios de tumba-muertos, duendes en Escazú y cazadores de leyendas en Puntarenas.", "12:00", "14:00", "Cultural"),
            Program("p6_4", "repretel_11", "Conexión Fútbol Costa Rica", "El show de juegos y chistes donde los panelistas discuten la actualidad deportiva nacional de forma amena.", "14:00", "16:00", "Deportes", isLive = true),
            Program("p6_5", "repretel_11", "La Viuda Negra", "Serie dramática internacional.", "16:00", "18:00", "Drama"),
            Program("p6_6", "repretel_11", "Informe 11: Las Historias (Edición Estelar)", "El show favorito de las familias ticas. Recorrido por bellas fincas, artesanías y folklore nacional.", "18:00", "19:30", "Cultural"),
            Program("p6_7", "repretel_11", "Al Cierre", "El resumen de noticias para finalizar el día tico de manera ecuánime.", "19:30", "21:00", "Noticias"),
            Program("p6_8", "repretel_11", "Comedia: Caras Vecinas", "Serie cómica nacional grabada en el cantón central de San José.", "21:00", "23:00", "Comedia")
        )
    )

    // Local content on demand
    val onDemandItems = listOf(
        OnDemandContent(
            id = "od_pension",
            title = "La Pensión (Especial Tico)",
            category = "Comedia",
            videoUrl = VIDEO_MP4_1,
            duration = "45 min",
            description = "Disfruta del capítulo clásico de la comedia más querida de Costa Rica. Vive las aventuras de Doña Tere, Camacho, Tony y Azucena en la pensión más famosa de San José.",
            thumbnail = "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&q=80&w=600",
            year = "2021",
            rating = "APT"
        ),
        OnDemandContent(
            id = "od_maikol",
            title = "Maikol Yordan: El Cortometraje Especial",
            category = "Cine Tico",
            videoUrl = VIDEO_MP4_2,
            duration = "22 min",
            description = "Maikol Yordan Soto Sibaja, el campesino tico más gracioso del país, viaja de su humilde finca a la capital tica en busca de un misterioso tesoro para salvar a su amada familia.",
            thumbnail = "https://images.unsplash.com/photo-1598899134739-24c46f58b8c0?auto=format&fit=crop&q=80&w=600",
            year = "2018",
            rating = "APT"
        ),
        OnDemandContent(
            id = "od_informe_historias",
            title = "Informe 11: Leyendas de Cartago",
            category = "Documentales",
            videoUrl = VIDEO_MP4_3,
            duration = "35 min",
            description = "Especial documental sobre los misterios de la provincia de Cartago: Leyendas de la vieja metrópoli, el fantasma del sanatorio Durán y testimonios paranormales espeluznantes.",
            thumbnail = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&q=80&w=600",
            year = "2023",
            rating = "TEENS"
        ),
        OnDemandContent(
            id = "od_destinos_tv",
            title = "Destinos TV: Playas de Guanacaste",
            category = "Cultura",
            videoUrl = VIDEO_MP4_4,
            duration = "28 min",
            description = "Hermoso recorrido audiovisual por las playas más paradisíacas de la hermosa provincia de Guanacaste: Playa Tamarindo, Conchal, Flamingo y los bosques secos de Santa Rosa.",
            thumbnail = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&q=80&w=600",
            year = "2024",
            rating = "APT"
        ),
        OnDemandContent(
            id = "od_media_docena",
            title = "La Media Docena: Lo Mejor de los Sketches",
            category = "Comedia",
            videoUrl = VIDEO_MP4_1,
            duration = "50 min",
            description = "Una compilación imperdible con los mejores gags, personajes absurdos y sketches de comedia tica fina del talentoso elenco costarricense del show La Media Docena.",
            thumbnail = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&q=80&w=600",
            year = "2020",
            rating = "APT"
        ),
        OnDemandContent(
            id = "od_parques_cr",
            title = "Volcanes Activos de Costa Rica",
            category = "Documentales",
            videoUrl = VIDEO_MP4_2,
            duration = "40 min",
            description = "Sube a la cima de los colosos más impresionantes del país: El Volcán Arenal, el Volcán Poás con su laguna ácida de color turquesa, el gélido Volcán Irazú y las fumarolas de Rincón de la Vieja.",
            thumbnail = "https://images.unsplash.com/photo-1467269204594-9661b134dd2b?auto=format&fit=crop&q=80&w=600",
            year = "2022",
            rating = "APT"
        )
    )

    // World Cup Matches details with true Central Standard Time (Costa Rica UTC-6)
    val worldCupMatches = listOf(
        WorldCupMatch(
            id = "wc_match_1",
            teamA = "Costa Rica",
            teamB = "Alemania",
            flagA = "🇨🇷",
            flagB = "🇩🇪",
            date = "Martes 16 de Junio, 2026",
            timeCR = "14:00",
            stadium = "Estadio Nacional de Costa Rica",
            group = "Grupo E - Fecha 1",
            status = "EN_VIVO",
            scoreA = 2,
            scoreB = 1,
            minute = "78'",
            broadcasterChannelIds = listOf("teletica_7", "teletica_deportes", "repretel_6")
        ),
        WorldCupMatch(
            id = "wc_match_2",
            teamA = "España",
            teamB = "Costa Rica",
            flagA = "🇪🇸",
            flagB = "🇨🇷",
            date = "Sábado 20 de Junio, 2026",
            timeCR = "11:00",
            stadium = "Estadio Centenario, Doha",
            group = "Grupo E - Fecha 2",
            status = "PROXIMO",
            broadcasterChannelIds = listOf("teletica_7", "repretel_6")
        ),
        WorldCupMatch(
            id = "wc_match_3",
            teamA = "Costa Rica",
            teamB = "Japón",
            flagA = "🇨🇷",
            flagB = "🇯🇵",
            date = "Miércoles 24 de Junio, 2026",
            timeCR = "17:00",
            stadium = "Estadio Lusail, Doha",
            group = "Grupo E - Fecha 3",
            status = "PROXIMO",
            broadcasterChannelIds = listOf("teletica_7", "teletica_deportes")
        ),
        WorldCupMatch(
            id = "wc_match_4",
            teamA = "Costa Rica",
            teamB = "Argentina",
            flagA = "🇨🇷",
            flagB = "🇦🇷",
            date = "Fase Anterior Amistosa",
            timeCR = "16:00",
            stadium = "Estadio Nacional, San José",
            group = "Amistoso del Centenario",
            status = "FINALIZADO",
            scoreA = 1,
            scoreB = 1,
            broadcasterChannelIds = listOf("teletica_7", "repretel_6")
        ),
        WorldCupMatch(
            id = "wc_match_5",
            teamA = "Brasil",
            teamB = "Francia",
            flagA = "🇧🇷",
            flagB = "🇫🇷",
            date = "Jueves 18 de Junio, 2026",
            timeCR = "14:00",
            stadium = "Estadio Al Bayt, Al Khor",
            group = "Grupo A - Fecha 2",
            status = "PROXIMO",
            broadcasterChannelIds = listOf("repretel_6", "teletica_deportes")
        )
    )

    // Standing Groups
    val groupStandings = listOf(
        StandingRow("🇨🇷 Costa Rica", played = 1, wins = 1, draws = 0, losses = 0, points = 3, goalDiff = "+1"),
        StandingRow("🇪🇸 España", played = 0, wins = 0, draws = 0, losses = 0, points = 0, goalDiff = "0"),
        StandingRow("🇯🇵 Japón", played = 0, wins = 0, draws = 0, losses = 0, points = 0, goalDiff = "0"),
        StandingRow("🇩🇪 Alemania", played = 1, wins = 0, draws = 0, losses = 1, points = 0, goalDiff = "-1")
    )
}

data class StandingRow(
    val team: String,
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val points: Int,
    val goalDiff: String
)
