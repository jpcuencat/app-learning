package com.example.data

import com.example.R
import com.example.model.BlitzRoundItem
import com.example.model.CefrLevel
import com.example.model.ConversationTurn
import com.example.model.ConversationVariant
import com.example.model.EvaluationCriteria
import com.example.model.Lesson
import com.example.model.PocketPhraseCard
import com.example.model.VoiceProfile
import java.util.Locale

object LessonCatalog {

    val voiceProfiles: List<VoiceProfile> = listOf(
        VoiceProfile(
            id = "clara_us",
            realName = "Clara · EE. UU. Natural",
            accentLabelEs = "Inglés americano",
            styleDescriptionEs = "Tono cálido y ritmo claro para conversaciones cotidianas.",
            locale = Locale.US,
            pitch = 1.04f,
            baseRate = 0.98f,
            preferredVoiceNameKeywords = listOf("en-us-x-sfg", "en-us-x-tpf", "sfg", "female")
        ),
        VoiceProfile(
            id = "oliver_uk",
            realName = "Oliver · Reino Unido",
            accentLabelEs = "Inglés británico",
            styleDescriptionEs = "Pronunciación nítida con acento británico moderado.",
            locale = Locale.UK,
            pitch = 0.90f,
            baseRate = 0.95f,
            preferredVoiceNameKeywords = listOf("en-gb-x-rjs", "en-gb-x-gbd", "rjs", "male")
        ),
        VoiceProfile(
            id = "maya_studio",
            realName = "Maya · Pausado Didáctico",
            accentLabelEs = "Inglés internacional",
            styleDescriptionEs = "Articulación suave y ligeramente más pausada para principiantes.",
            locale = Locale.US,
            pitch = 1.12f,
            baseRate = 0.88f,
            preferredVoiceNameKeywords = listOf("en-us-x-iob", "en-us-x-tpc", "iob", "network")
        )
    )

    const val VOICE_COMPARISON_SAMPLE_EN =
        "Hi there! Could I have a medium coffee to go, please?"
    const val VOICE_COMPARISON_SAMPLE_ES =
        "¡Hola! ¿Me podría dar un café mediano para llevar, por favor?"

    val lessons: List<Lesson> = listOf(
        // 1. A1 · Pedir en una cafetería (SoHo, NYC)
        Lesson(
            id = "cafe_order",
            level = CefrLevel.A1,
            titleEs = "Pedir en una cafetería",
            mapStopLabelEs = "SoHo Café",
            practiceSummaryEs = "Pides un café con cortesía, indicas que lo quieres para llevar y preguntas cuánto cuesta.",
            reviewSummaryEs = "En Una cafetería diferente, pides té, indicas que lo vas a tomar en el local y preguntas el total.",
            imageRes = R.drawable.img_lesson_cafe_1795700719389,
            rewardPhraseEn = "Could I have a coffee to go, please?",
            rewardPhraseEs = "¿Me podría dar un café para llevar, por favor?",
            practice = ConversationVariant(
                titleEs = "Pedir en una cafetería",
                sceneGoalEs = "Pedir un café con cortesía en SoHo, indicar que es para llevar y preguntar el precio.",
                skillSummaryEs = "Pedir bebidas con cortesía, indicar que algo es para llevar y preguntar cuánto cuesta.",
                nycLocationLabel = "SoHo Coffee Bar · NYC",
                nycTimeLabel = "8:40 AM",
                turns = listOf(
                    ConversationTurn(
                        id = "cafe_p1",
                        turnNumber = 1,
                        partnerRoleEs = "Sam · Barista en SoHo",
                        partnerLineEn = "Hi there! What can I get you?",
                        partnerLineEsHelper = "¡Hola! ¿Qué te puedo preparar?",
                        taskInstructionEs = "Pide un café de forma amable.",
                        hintEs = "Usa \"Could I have...\" o \"I'd like...\" + \"a coffee, please\".",
                        modelExampleEn = "Could I have a coffee, please?",
                        defaultSuccessExplanationEs = "¡Excelente! Usar una fórmula amable como \"Could I have a coffee, please?\" o \"I'd like a coffee, please\" suena educado y natural en cualquier cafetería de Nueva York.",
                        reusableTipEs = "Reutiliza \"Could I have a [bebida], please?\" con tea, latte, cappuccino o orange juice.",
                        successReactionEn = "Awesome! Fresh roast coming right up.",
                        starterSparksEn = listOf("Could I have", "a coffee", "please?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("coffee", "espresso", "latte", "cappuccino", "americano"),
                            requirePolitenessForFullPass = true,
                            missingPolitenessHintEs = "Mencionaste el café, pero en inglés conviene suavizar el pedido con \"Could I have...\", \"I'd like...\" o \"please\"."
                        )
                    ),
                    ConversationTurn(
                        id = "cafe_p2",
                        turnNumber = 2,
                        partnerRoleEs = "Sam · Barista en SoHo",
                        partnerLineEn = "Sure! For here or to go?",
                        partnerLineEsHelper = "¡Claro! ¿Para tomar aquí o para llevar?",
                        taskInstructionEs = "Indica que lo quieres para llevar.",
                        hintEs = "En Nueva York se dice \"To go, please\" (y en Reino Unido \"Take away, please\").",
                        modelExampleEn = "To go, please.",
                        defaultSuccessExplanationEs = "¡Muy bien! \"To go\" (o \"take away\") es la expresión exacta que esperan en una cafetería cuando vas caminando por la ciudad.",
                        reusableTipEs = "También puedes decir \"I'd like it to go, please\" cuando pidas comida o bebidas.",
                        successReactionEn = "Got it! I'll grab a sleeve and lid for you.",
                        starterSparksEn = listOf("To go,", "please.", "I'd like it"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("to go", "take away", "takeaway", "take out", "takeout"),
                            requirePolitenessForFullPass = false
                        )
                    ),
                    ConversationTurn(
                        id = "cafe_p3",
                        turnNumber = 3,
                        partnerRoleEs = "Sam · Barista en SoHo",
                        partnerLineEn = "Got it, one coffee to go. Anything else?",
                        partnerLineEsHelper = "Entendido, un café para llevar. ¿Algo más?",
                        taskInstructionEs = "Pregunta cuánto cuesta tu café.",
                        hintEs = "Usa \"How much is it?\" o \"How much does it cost?\".",
                        modelExampleEn = "That's all, thanks. How much is it?",
                        defaultSuccessExplanationEs = "¡Objetivo cumplido! \"How much is it?\" es la forma más directa y natural de preguntar el precio de un artículo.",
                        reusableTipEs = "Si compras varias cosas, cambia \"is it\" por \"are they\" o pregunta \"How much is the total?\".",
                        successReactionEn = "That'll be $4.50. Enjoy your morning in SoHo!",
                        starterSparksEn = listOf("That's all,", "How much", "is it?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("how much", "cost", "price", "total", "pay"),
                            requirePolitenessForFullPass = false
                        )
                    )
                )
            ),
            review = ConversationVariant(
                titleEs = "Una cafetería diferente",
                sceneGoalEs = "Pedir un té con cortesía en West Village, quedarte en el local y preguntar el total.",
                skillSummaryEs = "Variar tu pedido en una cafetería: pedir té, quedarte en el local y confirmar el total.",
                nycLocationLabel = "West Village Tea · NYC",
                nycTimeLabel = "10:15 AM",
                turns = listOf(
                    ConversationTurn(
                        id = "cafe_r1",
                        turnNumber = 1,
                        partnerRoleEs = "Elena · Barista West Village",
                        partnerLineEn = "Good morning! What would you like to order?",
                        partnerLineEsHelper = "¡Buenos días! ¿Qué te gustaría pedir?",
                        taskInstructionEs = "Pide un té con cortesía.",
                        hintEs = "Prueba con \"Could I have a tea, please?\" o \"I would like a green tea, please.\"",
                        modelExampleEn = "Could I have a tea, please?",
                        defaultSuccessExplanationEs = "¡Genial! Pediste el té con cortesía usando una estructura natural que sirve en cualquier café o restaurante.",
                        reusableTipEs = "Puedes especificar el tipo antes de tea: \"a green tea\", \"a black tea\" o \"a mint tea\".",
                        successReactionEn = "Lovely choice! Steeped hot and fresh.",
                        starterSparksEn = listOf("Could I have", "a tea,", "please?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("tea", "chai", "matcha", "chamomile"),
                            requirePolitenessForFullPass = true,
                            missingPolitenessHintEs = "Pediste té correctamente, pero agrega \"please\", \"Could I have\" o \"I'd like\" para sonar más cortés."
                        )
                    ),
                    ConversationTurn(
                        id = "cafe_r2",
                        turnNumber = 2,
                        partnerRoleEs = "Elena · Barista West Village",
                        partnerLineEn = "Of course. Will you have it here or to take away?",
                        partnerLineEsHelper = "Por supuesto. ¿Lo vas a tomar aquí o para llevar?",
                        taskInstructionEs = "Indica que lo vas a tomar en el local.",
                        hintEs = "Puedes decir \"For here, please\" o \"I'll have it here\".",
                        modelExampleEn = "For here, please.",
                        defaultSuccessExplanationEs = "¡Perfecto! \"For here\" (o \"to stay\") indica claramente que te sentarás en el local y suele venir en taza de cerámica.",
                        reusableTipEs = "Combínalo con cortesía: \"For here, please\" o \"To stay, thank you\".",
                        successReactionEn = "Perfect! Grab that sunny table by the window.",
                        starterSparksEn = listOf("For here,", "please.", "I'll stay"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("for here", "here", "stay", "sit", "inside", "drink here", "eat in"),
                            requirePolitenessForFullPass = false
                        )
                    ),
                    ConversationTurn(
                        id = "cafe_r3",
                        turnNumber = 3,
                        partnerRoleEs = "Elena · Barista West Village",
                        partnerLineEn = "Great, I'll bring your tea right over.",
                        partnerLineEsHelper = "Genial, enseguida te llevo tu té.",
                        taskInstructionEs = "Pregunta cuál es el total a pagar.",
                        hintEs = "Usa \"What is the total?\" o \"How much is the total?\".",
                        modelExampleEn = "Thank you! How much is the total?",
                        defaultSuccessExplanationEs = "¡Excelente cierre! Preguntar por el total con \"What's the total?\" o \"How much is the total?\" funciona para cualquier compra.",
                        reusableTipEs = "Si quieres saber si aceptan tarjeta, añade: \"Can I pay by card?\".",
                        successReactionEn = "It's $4 even. Have a wonderful day!",
                        starterSparksEn = listOf("Thank you!", "What is", "the total?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("total", "how much", "cost", "owe", "pay", "price"),
                            requirePolitenessForFullPass = false
                        )
                    )
                )
            )
        ),

        // 2. A1 · Pedir indicaciones (5th Ave & Subway NYC)
        Lesson(
            id = "ask_directions",
            level = CefrLevel.A1,
            titleEs = "Pedir indicaciones",
            mapStopLabelEs = "5th Ave",
            practiceSummaryEs = "Preguntas dónde está la farmacia, pides que repitan la indicación y confirmas dónde girar.",
            reviewSummaryEs = "En Llegar a la estación de metro, preguntas por otro destino y practicas con una ruta distinta.",
            imageRes = R.drawable.img_nyc_avenue_1795706681354,
            rewardPhraseEn = "Sorry, could you repeat that, please?",
            rewardPhraseEs = "Disculpa, ¿podrías repetir eso, por favor?",
            practice = ConversationVariant(
                titleEs = "Pedir indicaciones",
                sceneGoalEs = "Ubicar una farmacia en Manhattan, pedir que repitan la ruta y confirmar el giro.",
                skillSummaryEs = "Pedir direcciones en la calle, solicitar que repitan y confirmar un giro.",
                nycLocationLabel = "5th Ave & 42nd St · NYC",
                nycTimeLabel = "11:20 AM",
                turns = listOf(
                    ConversationTurn(
                        id = "dir_p1",
                        turnNumber = 1,
                        partnerRoleEs = "Maya · Vecina en Manhattan",
                        partnerLineEn = "Hey! You look a bit lost in Midtown. Can I help you?",
                        partnerLineEsHelper = "¡Hola! Pareces un poco perdido en Midtown. ¿Puedo ayudarte?",
                        taskInstructionEs = "Pregunta dónde está la farmacia.",
                        hintEs = "Usa \"Excuse me, where is the pharmacy?\" (o \"drugstore\" / \"chemist\").",
                        modelExampleEn = "Excuse me, where is the pharmacy?",
                        defaultSuccessExplanationEs = "¡Muy bien! \"Where is the pharmacy?\" va directo al grano y es muy fácil de entender en cualquier avenida neoyorquina.",
                        reusableTipEs = "Cambia \"pharmacy\" por \"bank\", \"hospital\", \"bus stop\" o \"supermarket\" para cualquier destino.",
                        successReactionEn = "Oh yeah, there's a big one right around the corner!",
                        starterSparksEn = listOf("Excuse me,", "where is", "the pharmacy?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("pharmacy", "drugstore", "chemist"),
                            secondaryAnyKeywords = listOf("where", "how do i get", "directions", "find", "near", "is there"),
                            missingSecondaryHintEs = "Mencionaste la farmacia, pero falta formular la pregunta de ubicación (por ejemplo, \"Where is the pharmacy?\")."
                        )
                    ),
                    ConversationTurn(
                        id = "dir_p2",
                        turnNumber = 2,
                        partnerRoleEs = "Maya · Vecina en Manhattan",
                        partnerLineEn = "Go straight two blocks, then turn left at the corner.",
                        partnerLineEsHelper = "Sigue recto dos cuadras y luego gira a la izquierda en la esquina.",
                        taskInstructionEs = "Pide amablemente que repitan la indicación.",
                        hintEs = "Di \"Sorry, could you repeat that, please?\" o \"Could you say that again?\".",
                        modelExampleEn = "Sorry, could you repeat that, please?",
                        defaultSuccessExplanationEs = "¡Perfecto! Pedir que repitan con \"Could you repeat that, please?\" te salva cuando los locales hablan a ritmo neoyorquino.",
                        reusableTipEs = "Si necesitas aún más claridad, añade \"more slowly\": \"Could you repeat that more slowly, please?\".",
                        successReactionEn = "Of course! Let me say it a little slower.",
                        starterSparksEn = listOf("Sorry,", "could you repeat", "that, please?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("repeat", "again", "slower", "slowly", "pardon", "didn't catch", "did not catch"),
                            requirePolitenessForFullPass = true,
                            missingPolitenessHintEs = "Se entiende que quieres que repitan, pero agrega \"Sorry\", \"Could you...\" o \"please\" para sonar amable."
                        )
                    ),
                    ConversationTurn(
                        id = "dir_p3",
                        turnNumber = 3,
                        partnerRoleEs = "Maya · Vecina en Manhattan",
                        partnerLineEn = "Sure: walk straight two blocks and turn left at the corner.",
                        partnerLineEsHelper = "Claro: camina dos cuadras recto y gira a la izquierda en la esquina.",
                        taskInstructionEs = "Confirma si debes girar a la izquierda en la esquina.",
                        hintEs = "Ejemplo: \"So I turn left at the corner, right?\" o \"Turn left at the corner, thanks!\".",
                        modelExampleEn = "So I turn left at the corner, right?",
                        defaultSuccessExplanationEs = "¡Excelente confirmación! Repetir el giro clave (\"turn left at the corner\") evita perderte en la ciudad.",
                        reusableTipEs = "La estructura \"So I [acción], right?\" sirve para confirmar horarios, direcciones o instrucciones.",
                        successReactionEn = "Exactly! You can't miss the green sign. Good luck!",
                        starterSparksEn = listOf("So I", "turn left", "at the corner?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("left"),
                            secondaryAnyKeywords = listOf("turn", "corner", "right", "blocks", "confirm", "so", "correct"),
                            missingSecondaryHintEs = "Mencionaste \"left\", intenta incluir \"turn left\" o \"at the corner\" para confirmar la ruta."
                        )
                    )
                )
            ),
            review = ConversationVariant(
                titleEs = "Llegar a la estación de metro",
                sceneGoalEs = "Ubicar la entrada del Subway (4-5-6), pedir que repitan y confirmar el giro a la derecha.",
                skillSummaryEs = "Preguntar por transporte público, pedir aclaraciones y verificar una ruta distinta.",
                nycLocationLabel = "Lexington & 51st · NYC",
                nycTimeLabel = "1:05 PM",
                turns = listOf(
                    ConversationTurn(
                        id = "dir_r1",
                        turnNumber = 1,
                        partnerRoleEs = "Leo · Local en NYC",
                        partnerLineEn = "Hi there! Are you looking for somewhere nearby?",
                        partnerLineEsHelper = "¡Hola! ¿Buscas algún lugar por aquí cerca?",
                        taskInstructionEs = "Pregunta dónde está la estación de metro.",
                        hintEs = "Usa \"Where is the subway station?\" o \"How do I get to the metro station?\".",
                        modelExampleEn = "Excuse me, where is the subway station?",
                        defaultSuccessExplanationEs = "¡Muy bien! En Nueva York se usa muchísimo \"subway station\", y \"metro station\" también se entiende perfecto.",
                        reusableTipEs = "También puedes preguntar \"Is the subway station far from here?\" para saber la distancia.",
                        successReactionEn = "The 4-5-6 line is super close!",
                        starterSparksEn = listOf("Excuse me,", "where is the", "subway station?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("metro", "subway", "underground", "train station", "tube"),
                            secondaryAnyKeywords = listOf("where", "how", "get to", "find", "near", "station"),
                            missingSecondaryHintEs = "Mencionaste el metro, pero recuerda preguntar dónde está usando \"Where is...\" o \"How do I get to...\"."
                        )
                    ),
                    ConversationTurn(
                        id = "dir_r2",
                        turnNumber = 2,
                        partnerRoleEs = "Leo · Local en NYC",
                        partnerLineEn = "Walk past the park and take the second street on your right.",
                        partnerLineEsHelper = "Pasa el parque y toma la segunda calle a tu derecha.",
                        taskInstructionEs = "Pide que repitan la indicación más despacio.",
                        hintEs = "Di \"Could you say that again slowly, please?\" o \"Could you repeat that?\".",
                        modelExampleEn = "Could you say that again more slowly, please?",
                        defaultSuccessExplanationEs = "¡Estupendo! Pedir que hablen más despacio demuestra seguridad al comunicarte en inglés.",
                        reusableTipEs = "Otras opciones naturales: \"Could you speak a bit slower, please?\" o \"One more time, please?\".",
                        successReactionEn = "Sure thing, let me slow down!",
                        starterSparksEn = listOf("Could you", "say that again", "slowly, please?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("repeat", "again", "slowly", "slower", "one more time"),
                            requirePolitenessForFullPass = true,
                            missingPolitenessHintEs = "Añade \"Could you...\" o \"please\" para que la petición suene cordial."
                        )
                    ),
                    ConversationTurn(
                        id = "dir_r3",
                        turnNumber = 3,
                        partnerRoleEs = "Leo · Local en NYC",
                        partnerLineEn = "No problem: pass the park, then turn right on the second street.",
                        partnerLineEsHelper = "Sin problema: pasa el parque y luego gira a la derecha en la segunda calle.",
                        taskInstructionEs = "Confirma que giras a la derecha después del parque.",
                        hintEs = "Ejemplo: \"So I turn right after the park, right?\".",
                        modelExampleEn = "So I turn right after the park, right?",
                        defaultSuccessExplanationEs = "¡Objetivo cumplido! Confirmaste el punto de referencia (\"park\") y la dirección exacta (\"right\").",
                        reusableTipEs = "Usar \"after the [lugar]\" o \"before the [lugar]\" hace que confirmar rutas sea facilísimo.",
                        successReactionEn = "Spot on! You'll see the green globes right there.",
                        starterSparksEn = listOf("So I", "turn right", "after the park?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("right"),
                            secondaryAnyKeywords = listOf("park", "turn", "second", "street", "after", "past"),
                            missingSecondaryHintEs = "Mencionaste \"right\", intenta añadir \"turn right\" o \"after the park\" para confirmar el punto exacto."
                        )
                    )
                )
            )
        ),

        // 3. A1 · Registro en el hotel (Midtown NYC)
        Lesson(
            id = "hotel_checkin",
            level = CefrLevel.A1,
            titleEs = "Registro en el hotel",
            mapStopLabelEs = "Midtown Hotel",
            practiceSummaryEs = "Avisas que tienes una reserva, preguntas la hora del desayuno y pides la clave del Wi-Fi.",
            reviewSummaryEs = "En Salida del hotel, entregas la llave, preguntas si pueden guardar tu maleta y pides un taxi.",
            imageRes = R.drawable.img_lesson_hotel_1795700721728,
            rewardPhraseEn = "I have a reservation. What time is breakfast?",
            rewardPhraseEs = "Tengo una reserva. ¿A qué hora es el desayuno?",
            practice = ConversationVariant(
                titleEs = "Registro en el hotel",
                sceneGoalEs = "Hacer check-in en Midtown, confirmar la hora del desayuno y pedir el Wi-Fi.",
                skillSummaryEs = "Presentar tu reserva de hotel y hacer preguntas prácticas en recepción.",
                nycLocationLabel = "Midtown Boutique Hotel · NYC",
                nycTimeLabel = "3:30 PM",
                turns = listOf(
                    ConversationTurn(
                        id = "hotel_p1",
                        turnNumber = 1,
                        partnerRoleEs = "Jordan · Recepción Midtown",
                        partnerLineEn = "Welcome to Horizon NYC! How can I help you today?",
                        partnerLineEsHelper = "¡Bienvenido a Horizon NYC! ¿En qué puedo ayudarte hoy?",
                        taskInstructionEs = "Di que tienes una reserva.",
                        hintEs = "Usa \"Hi, I have a reservation\" o \"I'd like to check in, please\".",
                        modelExampleEn = "Hello! I have a reservation.",
                        defaultSuccessExplanationEs = "¡Perfecto! \"I have a reservation\" es la frase universal para iniciar el registro en cualquier hotel.",
                        reusableTipEs = "Puedes añadir tu apellido así: \"I have a reservation under Garcia\".",
                        successReactionEn = "Welcome to New York! Let me pull up your booking.",
                        starterSparksEn = listOf("Hello!", "I have a", "reservation."),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("reservation", "booking", "check in", "check-in", "booked", "room")
                        )
                    ),
                    ConversationTurn(
                        id = "hotel_p2",
                        turnNumber = 2,
                        partnerRoleEs = "Jordan · Recepción Midtown",
                        partnerLineEn = "Found it! Here is your keycard for room 1204 with a skyline view.",
                        partnerLineEsHelper = "¡La encontré! Aquí tienes la tarjeta de la habitación 1204 con vista a la ciudad.",
                        taskInstructionEs = "Pregunta a qué hora es el desayuno.",
                        hintEs = "Di \"What time is breakfast?\" o \"When is breakfast served?\".",
                        modelExampleEn = "Thank you! What time is breakfast?",
                        defaultSuccessExplanationEs = "¡Muy bien! \"What time is breakfast?\" es clara, natural y funciona en todos los alojamientos.",
                        reusableTipEs = "Reutiliza \"What time is...?\" con \"check-out\", \"dinner\" o \"the airport shuttle\".",
                        successReactionEn = "We serve fresh bagels and coffee from 7 to 10 AM!",
                        starterSparksEn = listOf("Thank you!", "What time is", "breakfast?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("breakfast"),
                            secondaryAnyKeywords = listOf("what time", "when", "start", "hour", "served"),
                            missingSecondaryHintEs = "Mencionaste el desayuno; agrega \"What time is...\" o \"When is...\" para preguntar la hora."
                        )
                    ),
                    ConversationTurn(
                        id = "hotel_p3",
                        turnNumber = 3,
                        partnerRoleEs = "Jordan · Recepción Midtown",
                        partnerLineEn = "Breakfast is from seven to ten in the lobby. Anything else?",
                        partnerLineEsHelper = "El desayuno es de siete a diez en el vestíbulo. ¿Algo más?",
                        taskInstructionEs = "Pide la contraseña del Wi-Fi.",
                        hintEs = "Prueba con \"What is the Wi-Fi password, please?\".",
                        modelExampleEn = "Could I have the Wi-Fi password, please?",
                        defaultSuccessExplanationEs = "¡Genial! Pedir la contraseña con \"What's the Wi-Fi password?\" o \"Could I have the Wi-Fi password?\" es súper útil al viajar.",
                        reusableTipEs = "También puedes preguntar \"Is the Wi-Fi free?\" si no estás seguro del costo.",
                        successReactionEn = "It's printed right inside your key sleeve. Enjoy your stay!",
                        starterSparksEn = listOf("Could I have", "the Wi-Fi", "password, please?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("wifi", "wi-fi", "password", "internet")
                        )
                    )
                )
            ),
            review = ConversationVariant(
                titleEs = "Salida del hotel",
                sceneGoalEs = "Hacer check-out, dejar tu maleta un rato y pedir un yellow cab al aeropuerto.",
                skillSummaryEs = "Gestionar tu salida del hotel, equipaje y transporte en inglés.",
                nycLocationLabel = "Midtown Lobby · NYC",
                nycTimeLabel = "10:45 AM",
                turns = listOf(
                    ConversationTurn(
                        id = "hotel_r1",
                        turnNumber = 1,
                        partnerRoleEs = "Jordan · Recepción Midtown",
                        partnerLineEn = "Good morning! How was your stay in New York?",
                        partnerLineEsHelper = "¡Buenos días! ¿Qué tal estuvo tu estancia en Nueva York?",
                        taskInstructionEs = "Di que quieres hacer el check-out.",
                        hintEs = "Usa \"I'd like to check out, please.\".",
                        modelExampleEn = "It was great! I'd like to check out, please.",
                        defaultSuccessExplanationEs = "¡Muy bien! \"Check out\" es el verbo exacto para entregar la habitación al salir.",
                        reusableTipEs = "Puedes añadir \"Here is my key\" al entregar la tarjeta o llave.",
                        successReactionEn = "So glad you loved it! You're all checked out.",
                        starterSparksEn = listOf("I'd like to", "check out,", "please."),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("check out", "check-out", "checkout", "leaving", "key")
                        )
                    ),
                    ConversationTurn(
                        id = "hotel_r2",
                        turnNumber = 2,
                        partnerRoleEs = "Jordan · Recepción Midtown",
                        partnerLineEn = "Everything is settled. Can I help you with anything else?",
                        partnerLineEsHelper = "Todo está listo. ¿Puedo ayudarte con algo más?",
                        taskInstructionEs = "Pregunta si puedes dejar tu maleta un rato.",
                        hintEs = "Di \"Can I leave my luggage here?\" o \"Could you hold my bag?\".",
                        modelExampleEn = "Could I leave my luggage here for a few hours?",
                        defaultSuccessExplanationEs = "¡Excelente! \"Leave my luggage\" o \"store my bag\" es una frase clave cuando tu vuelo sale tarde.",
                        reusableTipEs = "Usa \"bag\", \"suitcase\" o \"luggage\" (recuerda que luggage no lleva plural en inglés).",
                        successReactionEn = "Of course! Here's your luggage claim ticket.",
                        starterSparksEn = listOf("Could I leave", "my luggage", "here?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("luggage", "bag", "suitcase", "bags", "baggage"),
                            secondaryAnyKeywords = listOf("leave", "store", "keep", "hold", "can i", "could i"),
                            missingSecondaryHintEs = "Mencionaste el equipaje; agrega \"Can I leave...\" o \"Could you keep...\" para hacer la solicitud."
                        )
                    ),
                    ConversationTurn(
                        id = "hotel_r3",
                        turnNumber = 3,
                        partnerRoleEs = "Jordan · Recepción Midtown",
                        partnerLineEn = "Of course, we'll keep it safe for you.",
                        partnerLineEsHelper = "Por supuesto, la guardaremos segura para ti.",
                        taskInstructionEs = "Pide amablemente que llamen a un taxi.",
                        hintEs = "Prueba con \"Could you call a taxi for me, please?\".",
                        modelExampleEn = "Could you call a taxi for me, please?",
                        defaultSuccessExplanationEs = "¡Objetivo cumplido! \"Could you call a taxi, please?\" es educada, clara y muy usada en viajes.",
                        reusableTipEs = "Para indicar destino añade \"to JFK airport\" o \"to Penn Station\".",
                        successReactionEn = "A yellow cab will be right outside in two minutes!",
                        starterSparksEn = listOf("Could you call", "a taxi,", "please?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("taxi", "cab", "uber"),
                            requirePolitenessForFullPass = true,
                            missingPolitenessHintEs = "Añade \"Could you...\" o \"please\" para pedir el taxi con cortesía."
                        )
                    )
                )
            )
        ),

        // 4. A1 JEFE · Imprevisto en Times Square
        Lesson(
            id = "boss_a1_twist",
            level = CefrLevel.A1,
            titleEs = "Jefe A1: Imprevisto en Times Sq",
            mapStopLabelEs = "Jefe A1",
            isBossMission = true,
            practiceSummaryEs = "Reto sorpresa en hora pico: te dan la bebida equivocada, falla tu tarjeta y buscas un cajero.",
            reviewSummaryEs = "En Segundo imprevisto, resuelves un cambio rápido y confirmas cómo llegar al banco.",
            imageRes = R.drawable.img_nyc_avenue_1795706681354,
            rewardPhraseEn = "Excuse me, I ordered coffee, not tea.",
            rewardPhraseEs = "Disculpa, pedí café, no té.",
            practice = ConversationVariant(
                titleEs = "Imprevisto en Times Sq (Jefe A1)",
                sceneGoalEs = "Resolver dos imprevistos en hora pico de NYC: corregir tu bebida y ubicar un ATM.",
                skillSummaryEs = "Improvisar ante pequeños imprevistos reales en inglés (pedido equivocado y pago).",
                nycLocationLabel = "Times Square Espresso · NYC",
                nycTimeLabel = "9:05 AM",
                turns = listOf(
                    ConversationTurn(
                        id = "boss_a1_p1",
                        turnNumber = 1,
                        partnerRoleEs = "Chris · Barista en hora pico",
                        partnerLineEn = "Here you go, one iced tea! Next in line, please!",
                        partnerLineEsHelper = "¡Aquí tienes, un té helado! ¡Siguiente en la fila, por favor!",
                        taskInstructionEs = "¡Sorpresa! Te dieron té en lugar de café. Corrige el error con cortesía.",
                        hintEs = "Usa \"Excuse me, I ordered a coffee, not tea\".",
                        modelExampleEn = "Excuse me, I ordered a coffee, please.",
                        defaultSuccessExplanationEs = "¡Qué temple! Empezar con \"Excuse me\" y aclarar \"I ordered a coffee\" resuelve el error sin sonar brusco.",
                        reusableTipEs = "Sirve para cualquier confusión: \"Excuse me, I ordered [X]\".",
                        successReactionEn = "Whoa, my bad! Grabbing your hot coffee right now.",
                        starterSparksEn = listOf("Excuse me,", "I ordered", "a coffee."),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("coffee", "ordered", "wrong", "mistake"),
                            requirePolitenessForFullPass = true
                        )
                    ),
                    ConversationTurn(
                        id = "boss_a1_p2",
                        turnNumber = 2,
                        partnerRoleEs = "Chris · Barista en hora pico",
                        partnerLineEn = "Oh, sorry! Here is your coffee. Wait—our card machine is down, cash only today!",
                        partnerLineEsHelper = "¡Uy, perdón! Aquí está tu café. Espera: nuestra terminal falló, hoy solo efectivo.",
                        taskInstructionEs = "Pregunta dónde hay un cajero automático (ATM) cerca.",
                        hintEs = "Di \"Where is the nearest ATM?\" o \"Is there an ATM near here?\".",
                        modelExampleEn = "No problem! Is there an ATM near here?",
                        defaultSuccessExplanationEs = "¡Superaste al Jefe A1! \"Is there an ATM near here?\" te salva de cualquier apuro viajando.",
                        reusableTipEs = "Reutiliza \"Is there a [lugar] near here?\" con pharmacy, bank o supermarket.",
                        successReactionEn = "Right next door inside the bodega! I'll hold your cup.",
                        starterSparksEn = listOf("Is there", "an ATM", "near here?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("atm", "cash", "bank", "machine"),
                            secondaryAnyKeywords = listOf("where", "near", "is there", "find", "closest", "nearest")
                        )
                    )
                )
            ),
            review = ConversationVariant(
                titleEs = "Revancha: Imprevisto rápido",
                sceneGoalEs = "Practicar de nuevo cómo aclarar un pago y ubicar un banco cercano.",
                skillSummaryEs = "Mantener la calma en inglés cuando algo cambia de repente.",
                nycLocationLabel = "Midtown Bodega · NYC",
                nycTimeLabel = "4:10 PM",
                turns = listOf(
                    ConversationTurn(
                        id = "boss_a1_r1",
                        turnNumber = 1,
                        partnerRoleEs = "Cajero en Midtown",
                        partnerLineEn = "Sorry, we only take cash right now.",
                        partnerLineEsHelper = "Lo siento, ahora mismo solo aceptamos efectivo.",
                        taskInstructionEs = "Pregunta si puedes pagar con tarjeta o dónde hay un banco.",
                        hintEs = "Di \"Where is the nearest bank?\" o \"Can I pay by card?\".",
                        modelExampleEn = "Okay, where is the nearest bank, please?",
                        defaultSuccessExplanationEs = "¡Excelente reacción rápida!",
                        reusableTipEs = "Añadir \"nearest\" significa \"el más cercano\".",
                        successReactionEn = "Just across the avenue on the corner!",
                        starterSparksEn = listOf("Where is the", "nearest bank,", "please?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("bank", "atm", "card", "cash")
                        )
                    )
                )
            )
        ),

        // 5. A2 · En el restaurante (Brooklyn NYC)
        Lesson(
            id = "restaurant_a2",
            level = CefrLevel.A2,
            titleEs = "En el restaurante",
            mapStopLabelEs = "Brooklyn",
            practiceSummaryEs = "Pides una mesa para dos, preguntas por opciones vegetarianas y pides la cuenta.",
            reviewSummaryEs = "En Cambio en el pedido, aclaras un detalle del plato, pides agua y confirmas pago con tarjeta.",
            imageRes = R.drawable.img_lesson_cafe_1795700719389,
            rewardPhraseEn = "Do you have any vegetarian options?",
            rewardPhraseEs = "¿Tienen opciones vegetarianas?",
            practice = ConversationVariant(
                titleEs = "En el restaurante",
                sceneGoalEs = "Conseguir mesa en Brooklyn, preguntar por platos vegetarianos y pedir la cuenta.",
                skillSummaryEs = "Desenvolverte en un restaurante: mesa, preguntas de menú y pago.",
                nycLocationLabel = "Williamsburg Bistro · NYC",
                nycTimeLabel = "7:45 PM",
                turns = listOf(
                    ConversationTurn(
                        id = "rest_p1",
                        turnNumber = 1,
                        partnerRoleEs = "Nora · Anfitriona Brooklyn",
                        partnerLineEn = "Good evening! Welcome to Maple Bistro in Brooklyn.",
                        partnerLineEsHelper = "¡Buenas noches! Bienvenidos a Maple Bistro en Brooklyn.",
                        taskInstructionEs = "Pide una mesa para dos personas.",
                        hintEs = "Usa \"Could we have a table for two, please?\".",
                        modelExampleEn = "Hi! A table for two, please.",
                        defaultSuccessExplanationEs = "¡Genial! \"A table for two, please\" funciona de inmediato al llegar sin reserva.",
                        reusableTipEs = "Cambia el número según tu grupo: \"a table for four, please\".",
                        successReactionEn = "Right this way, we have a cozy patio table!",
                        starterSparksEn = listOf("A table", "for two,", "please."),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("table"),
                            secondaryAnyKeywords = listOf("two", "2", "couple"),
                            missingSecondaryHintEs = "Mencionaste la mesa; indica también que es para dos (\"for two\")."
                        )
                    ),
                    ConversationTurn(
                        id = "rest_p2",
                        turnNumber = 2,
                        partnerRoleEs = "Nora · Mesera Brooklyn",
                        partnerLineEn = "Here are your menus. Do you have any questions about the dishes?",
                        partnerLineEsHelper = "Aquí tienen los menús. ¿Tienen alguna pregunta sobre los platos?",
                        taskInstructionEs = "Pregunta si tienen opciones vegetarianas.",
                        hintEs = "Di \"Do you have any vegetarian options?\".",
                        modelExampleEn = "Do you have any vegetarian options?",
                        defaultSuccessExplanationEs = "¡Excelente! \"Do you have any... options?\" es una estructura A2 súper versátil.",
                        reusableTipEs = "Reemplaza \"vegetarian\" por \"vegan\" o \"gluten-free\" cuando lo necesites.",
                        successReactionEn = "Yes! Our roasted mushroom pasta is a Brooklyn favorite.",
                        starterSparksEn = listOf("Do you have", "any vegetarian", "options?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("vegetarian", "vegan", "veggie", "meatless")
                        )
                    ),
                    ConversationTurn(
                        id = "rest_p3",
                        turnNumber = 3,
                        partnerRoleEs = "Nora · Mesera Brooklyn",
                        partnerLineEn = "I hope you enjoyed your meal! Can I get you dessert?",
                        partnerLineEsHelper = "¡Espero que hayan disfrutado la comida! ¿Les traigo postre?",
                        taskInstructionEs = "Pide la cuenta amablemente.",
                        hintEs = "Usa \"Could we have the check, please?\" o \"The bill, please\".",
                        modelExampleEn = "No thanks. Could we have the check, please?",
                        defaultSuccessExplanationEs = "¡Objetivo cumplido! En Nueva York \"check\" es la palabra más habitual (y \"bill\" también funciona perfecto).",
                        reusableTipEs = "Si pagan por separado, añade: \"Could we split the check?\".",
                        successReactionEn = "Coming right up! Thank you for dining with us.",
                        starterSparksEn = listOf("Could we have", "the check,", "please?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("check", "bill", "pay"),
                            requirePolitenessForFullPass = true
                        )
                    )
                )
            ),
            review = ConversationVariant(
                titleEs = "Cambio en el pedido",
                sceneGoalEs = "Aclarar amablemente un error en tu plato, pedir agua y preguntar si aceptan tarjeta.",
                skillSummaryEs = "Resolver pequeños imprevistos en un restaurante con naturalidad y cortesía.",
                nycLocationLabel = "DUMBO Kitchen · NYC",
                nycTimeLabel = "8:20 PM",
                turns = listOf(
                    ConversationTurn(
                        id = "rest_r1",
                        turnNumber = 1,
                        partnerRoleEs = "Mesero en DUMBO",
                        partnerLineEn = "Here is your grilled chicken salad. Enjoy!",
                        partnerLineEsHelper = "Aquí está tu ensalada de pollo a la parrilla. ¡Buen provecho!",
                        taskInstructionEs = "Di amablemente que pediste la ensalada sin pollo (vegetariana).",
                        hintEs = "Usa \"Excuse me, I ordered the salad without chicken\" o \"the vegetarian salad\".",
                        modelExampleEn = "Excuse me, I ordered the salad without chicken.",
                        defaultSuccessExplanationEs = "¡Muy bien resuelto! Empezar con \"Excuse me\" suaviza cualquier corrección en un restaurante.",
                        reusableTipEs = "Usa \"I ordered [X] instead of [Y]\" para aclarar pedidos con tacto.",
                        successReactionEn = "So sorry! I'll bring your veggie salad right out.",
                        starterSparksEn = listOf("Excuse me,", "I ordered", "without chicken."),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("chicken", "vegetarian", "without", "ordered", "mistake", "wrong"),
                            requirePolitenessForFullPass = true
                        )
                    ),
                    ConversationTurn(
                        id = "rest_r2",
                        turnNumber = 2,
                        partnerRoleEs = "Mesero en DUMBO",
                        partnerLineEn = "So sorry about that! I'll replace it right away. Would you like anything to drink?",
                        partnerLineEsHelper = "¡Lo siento mucho! La cambio enseguida. ¿Quieres algo de tomar?",
                        taskInstructionEs = "Pide un vaso de agua, por favor.",
                        hintEs = "Di \"Could I have a glass of water, please?\".",
                        modelExampleEn = "Could I have a glass of water, please?",
                        defaultSuccessExplanationEs = "¡Perfecto! Pediste agua con cortesía y claridad.",
                        reusableTipEs = "Puedes especificar \"still water\" (sin gas) o \"sparkling water\" (con gas).",
                        successReactionEn = "Sure thing, ice cold water coming right up!",
                        starterSparksEn = listOf("Could I have", "a glass of water,", "please?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("water"),
                            requirePolitenessForFullPass = true
                        )
                    ),
                    ConversationTurn(
                        id = "rest_r3",
                        turnNumber = 3,
                        partnerRoleEs = "Mesero en DUMBO",
                        partnerLineEn = "Here is your check whenever you're ready.",
                        partnerLineEsHelper = "Aquí tienes la cuenta cuando estés listo.",
                        taskInstructionEs = "Pregunta si puedes pagar con tarjeta.",
                        hintEs = "Di \"Can I pay by card?\" o \"Do you take credit cards?\".",
                        modelExampleEn = "Can I pay by card?",
                        defaultSuccessExplanationEs = "¡Listo! \"Can I pay by card?\" es breve, clara y se entiende en todo el mundo.",
                        reusableTipEs = "Si prefieres efectivo, pregunta \"Can I pay in cash?\".",
                        successReactionEn = "Absolutely, tap or chip right here!",
                        starterSparksEn = listOf("Can I", "pay by", "card?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("card", "credit", "debit", "contactless")
                        )
                    )
                )
            )
        ),

        // 6. A2 JEFE · Hora pico antes de Broadway
        Lesson(
            id = "boss_a2_twist",
            level = CefrLevel.A2,
            titleEs = "Jefe A2: Antes de Broadway",
            mapStopLabelEs = "Jefe A2",
            isBossMission = true,
            practiceSummaryEs = "Reto sorpresa: el restaurante está lleno y tu obra en Broadway empieza en 40 minutos.",
            reviewSummaryEs = "En Revancha en el bistró, negocias sentarte en la barra y pides algo rápido.",
            imageRes = R.drawable.img_nyc_avenue_1795706681354,
            rewardPhraseEn = "How long is the wait? We're in a bit of a hurry.",
            rewardPhraseEs = "¿Cuánto es la espera? Llevamos un poco de prisa.",
            practice = ConversationVariant(
                titleEs = "Antes de Broadway (Jefe A2)",
                sceneGoalEs = "Preguntar el tiempo de espera y aceptar una alternativa rápida antes del show.",
                skillSummaryEs = "Negociar tiempos de espera y explicar que tienes prisa con cortesía.",
                nycLocationLabel = "Theater District · NYC",
                nycTimeLabel = "6:50 PM",
                turns = listOf(
                    ConversationTurn(
                        id = "boss_a2_p1",
                        turnNumber = 1,
                        partnerRoleEs = "Anfitrión Theater District",
                        partnerLineEn = "We're packed before the Broadway shows tonight, even with reservations.",
                        partnerLineEsHelper = "Estamos llenísimos antes de las obras de Broadway, incluso con reserva.",
                        taskInstructionEs = "Pregunta cuánto tiempo es la espera.",
                        hintEs = "Di \"How long is the wait?\" o \"How long will it take?\".",
                        modelExampleEn = "How long is the wait?",
                        defaultSuccessExplanationEs = "¡Excelente pregunta A2! \"How long is the wait?\" es exactamente lo que dice un neoyorquino.",
                        reusableTipEs = "También funciona en filas de museos, taxis o aeropuertos.",
                        successReactionEn = "Let me check our waitlist for you!",
                        starterSparksEn = listOf("How long", "is the", "wait?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("how long", "wait", "minutes", "time")
                        )
                    ),
                    ConversationTurn(
                        id = "boss_a2_p2",
                        turnNumber = 2,
                        partnerRoleEs = "Anfitrión Theater District",
                        partnerLineEn = "About twenty-five minutes for a table, or we have two seats at the bar right now.",
                        partnerLineEsHelper = "Unos 25 minutos para mesa, o tenemos dos lugares en la barra ahora mismo.",
                        taskInstructionEs = "Acepta la barra y explica que llevan prisa.",
                        hintEs = "Di \"The bar is great, we're in a hurry\".",
                        modelExampleEn = "The bar works great, we're in a hurry!",
                        defaultSuccessExplanationEs = "¡Jefe A2 derrotado! \"We're in a hurry\" es natural y directo.",
                        reusableTipEs = "Para suavizarlo añade \"a bit\": \"We're in a bit of a hurry\".",
                        successReactionEn = "Smart move! Follow me, you'll make the curtain easily!",
                        starterSparksEn = listOf("The bar is great,", "we're in a", "hurry!"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("bar", "seats", "hurry", "rush", "great", "fine", "okay", "take")
                        )
                    )
                )
            ),
            review = ConversationVariant(
                titleEs = "Revancha en el bistró",
                sceneGoalEs = "Confirmar disponibilidad en la barra y aceptar con cortesía.",
                skillSummaryEs = "Adaptarte a cambios de último minuto en un restaurante.",
                nycLocationLabel = "Hell's Kitchen · NYC",
                nycTimeLabel = "7:15 PM",
                turns = listOf(
                    ConversationTurn(
                        id = "boss_a2_r1",
                        turnNumber = 1,
                        partnerRoleEs = "Anfitrión Hell's Kitchen",
                        partnerLineEn = "Would you like to sit at the bar instead?",
                        partnerLineEsHelper = "¿Te gustaría sentarte en la barra en su lugar?",
                        taskInstructionEs = "Di que sí con cortesía porque tienes prisa.",
                        hintEs = "Di \"Yes please, we're in a hurry\".",
                        modelExampleEn = "Yes please, that would be great!",
                        defaultSuccessExplanationEs = "¡Muy ágil y cordial!",
                        reusableTipEs = "\"That would be great\" es un comodín elegante para aceptar ofertas.",
                        successReactionEn = "Awesome, right this way!",
                        starterSparksEn = listOf("Yes please,", "that would be", "great!"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("yes", "sure", "great", "bar", "hurry", "please")
                        )
                    )
                )
            )
        ),

        // 7. B1 · Reprogramar una cita (Bryant Park NYC)
        Lesson(
            id = "reschedule_b1",
            level = CefrLevel.B1,
            titleEs = "Reprogramar una cita",
            mapStopLabelEs = "Bryant Park",
            practiceSummaryEs = "Explicas que no podrás asistir, propones un nuevo horario y pides confirmación.",
            reviewSummaryEs = "En Ajustar una reunión, solicitas mover una videollamada y acuerdas una nueva fecha.",
            imageRes = R.drawable.img_lesson_hotel_1795700721728,
            rewardPhraseEn = "Would tomorrow afternoon work for you?",
            rewardPhraseEs = "¿Te vendría bien mañana por la tarde?",
            practice = ConversationVariant(
                titleEs = "Reprogramar una cita",
                sceneGoalEs = "Avisar de un cambio de planes en Manhattan, proponer nuevo horario y pedir confirmación.",
                skillSummaryEs = "Reprogramar citas profesionales o personales con tono educado en inglés.",
                nycLocationLabel = "Bryant Park Office · NYC",
                nycTimeLabel = "12:30 PM",
                turns = listOf(
                    ConversationTurn(
                        id = "resch_p1",
                        turnNumber = 1,
                        partnerRoleEs = "Asistente en Midtown",
                        partnerLineEn = "Manhattan Dental Clinic, how may I help you?",
                        partnerLineEsHelper = "Clínica Dental Manhattan, ¿en qué puedo ayudarte?",
                        taskInstructionEs = "Di que necesitas reprogramar tu cita de hoy.",
                        hintEs = "Usa \"I need to reschedule my appointment for today\".",
                        modelExampleEn = "Hi, I need to reschedule my appointment today.",
                        defaultSuccessExplanationEs = "¡Muy bien! El verbo \"reschedule\" es justo el término natural para cambiar una cita de fecha u hora.",
                        reusableTipEs = "También puedes decir \"Could I move my appointment to another day?\".",
                        successReactionEn = "No problem at all, let's find you a better slot.",
                        starterSparksEn = listOf("I need to", "reschedule my", "appointment."),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("reschedule", "change", "move", "postpone"),
                            secondaryAnyKeywords = listOf("appointment", "meeting", "visit", "today"),
                            missingSecondaryHintEs = "Menciona que se trata de tu cita (\"my appointment\") para que quede más claro."
                        )
                    ),
                    ConversationTurn(
                        id = "resch_p2",
                        turnNumber = 2,
                        partnerRoleEs = "Asistente en Midtown",
                        partnerLineEn = "Of course. What day and time would work better for you?",
                        partnerLineEsHelper = "Por supuesto. ¿Qué día y hora te vendrían mejor?",
                        taskInstructionEs = "Propón moverla para mañana por la tarde.",
                        hintEs = "Di \"Would tomorrow afternoon work?\" o \"How about tomorrow afternoon?\".",
                        modelExampleEn = "Would tomorrow afternoon work?",
                        defaultSuccessExplanationEs = "¡Excelente nivel B1! \"Would [horario] work?\" suena profesional y muy natural.",
                        reusableTipEs = "Usa \"How about Thursday at three?\" para proponer horarios concretos.",
                        successReactionEn = "Let me check... yes, 3:00 PM is wide open!",
                        starterSparksEn = listOf("Would", "tomorrow afternoon", "work?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("tomorrow"),
                            secondaryAnyKeywords = listOf("afternoon", "pm", "work", "available", "how about", "at")
                        )
                    ),
                    ConversationTurn(
                        id = "resch_p3",
                        turnNumber = 3,
                        partnerRoleEs = "Asistente en Midtown",
                        partnerLineEn = "Yes, tomorrow at 3:00 PM is open. I've updated your booking.",
                        partnerLineEsHelper = "Sí, mañana a las 3:00 PM está libre. Ya actualicé tu reserva.",
                        taskInstructionEs = "Agradece y pregunta si te enviarán un correo de confirmación.",
                        hintEs = "Di \"Thanks! Will you send a confirmation email?\".",
                        modelExampleEn = "Thank you! Could you send me a confirmation email?",
                        defaultSuccessExplanationEs = "¡Objetivo cumplido! Confirmar por correo (\"confirmation email\") cierra la llamada con seguridad.",
                        reusableTipEs = "También funciona con \"text message\" o \"calendar invite\".",
                        successReactionEn = "Just sent it to your inbox. See you tomorrow!",
                        starterSparksEn = listOf("Thank you!", "Could you send a", "confirmation email?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("email", "confirmation", "confirm", "message")
                        )
                    )
                )
            ),
            review = ConversationVariant(
                titleEs = "Ajustar una reunión",
                sceneGoalEs = "Pedir mover una reunión laboral, proponer el viernes y confirmar la invitación.",
                skillSummaryEs = "Negociar cambios de agenda laboral con claridad y cortesía.",
                nycLocationLabel = "Hudson Yards · NYC",
                nycTimeLabel = "1:50 PM",
                turns = listOf(
                    ConversationTurn(
                        id = "resch_r1",
                        turnNumber = 1,
                        partnerRoleEs = "Colega en NYC",
                        partnerLineEn = "Hey! Are we still on for our call at 2:00?",
                        partnerLineEsHelper = "¡Hola! ¿Seguimos en pie para nuestra llamada a las 2:00?",
                        taskInstructionEs = "Explica que estás retrasado y pregunta si pueden mover la reunión.",
                        hintEs = "Di \"Sorry, I'm running late. Could we move our meeting?\".",
                        modelExampleEn = "Sorry, I'm running late. Could we reschedule the meeting?",
                        defaultSuccessExplanationEs = "¡Natural y claro! \"Running late\" es la expresión idiomática perfecta para decir que vas con retraso.",
                        reusableTipEs = "Úsala en cualquier contexto: \"Sorry I'm running five minutes late\".",
                        successReactionEn = "Totally fine, NYC traffic is wild today!",
                        starterSparksEn = listOf("Sorry, I'm", "running late.", "Could we reschedule?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("late", "reschedule", "move", "delay", "postpone", "busy")
                        )
                    ),
                    ConversationTurn(
                        id = "resch_r2",
                        turnNumber = 2,
                        partnerRoleEs = "Colega en NYC",
                        partnerLineEn = "No worries! When are you free?",
                        partnerLineEsHelper = "¡Sin problema! ¿Cuándo estás libre?",
                        taskInstructionEs = "Propón el viernes por la mañana.",
                        hintEs = "Di \"How about Friday morning?\" o \"Are you free on Friday morning?\".",
                        modelExampleEn = "How about Friday morning?",
                        defaultSuccessExplanationEs = "¡Genial! \"How about Friday morning?\" es ágil y muy común entre compañeros de trabajo.",
                        reusableTipEs = "Puedes añadir hora: \"How about Friday morning at ten?\".",
                        successReactionEn = "Friday morning looks great on my calendar!",
                        starterSparksEn = listOf("How about", "Friday", "morning?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("friday"),
                            secondaryAnyKeywords = listOf("morning", "am", "free", "work", "about")
                        )
                    ),
                    ConversationTurn(
                        id = "resch_r3",
                        turnNumber = 3,
                        partnerRoleEs = "Colega en NYC",
                        partnerLineEn = "Friday morning works for me. Let's do 10:00 AM.",
                        partnerLineEsHelper = "El viernes por la mañana me funciona. Hagámoslo a las 10:00 AM.",
                        taskInstructionEs = "Confirma el horario y di que enviarás o esperarás la invitación.",
                        hintEs = "Di \"Sounds great, I'll send the calendar invite.\".",
                        modelExampleEn = "Sounds great! I'll send the updated invite.",
                        defaultSuccessExplanationEs = "¡Cierre impecable! \"Sounds great\" es una forma muy natural de mostrar acuerdo.",
                        reusableTipEs = "Otras alternativas: \"Perfect, see you then!\" o \"Deal, talk to you on Friday!\".",
                        successReactionEn = "Awesome, talk to you Friday morning!",
                        starterSparksEn = listOf("Sounds great!", "I'll send the", "calendar invite."),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("invite", "calendar", "great", "perfect", "thanks", "thank", "friday", "10", "ten")
                        )
                    )
                )
            )
        ),

        // 8. B1 JEFE · Vuelo retrasado en JFK
        Lesson(
            id = "boss_b1_twist",
            level = CefrLevel.B1,
            titleEs = "Jefe B1: Vuelo retrasado JFK",
            mapStopLabelEs = "Jefe B1",
            isBossMission = true,
            practiceSummaryEs = "Reto sorpresa: tu vuelo hacia JFK se retrasó y debes proponer una videollamada al cliente.",
            reviewSummaryEs = "En Plan B en el aeropuerto, confirmas el enlace de la llamada y agradeces la flexibilidad.",
            imageRes = R.drawable.img_nyc_avenue_1795706681354,
            rewardPhraseEn = "My flight was delayed. Could we switch to a video call?",
            rewardPhraseEs = "Mi vuelo se retrasó. ¿Podemos cambiar a una videollamada?",
            practice = ConversationVariant(
                titleEs = "Vuelo retrasado JFK (Jefe B1)",
                sceneGoalEs = "Comunicar un retraso aéreo hacia Nueva York y proponer una reunión virtual.",
                skillSummaryEs = "Gestionar imprevistos laborales en inglés con seguridad y soluciones concretas.",
                nycLocationLabel = "JFK Terminal 4 · NYC",
                nycTimeLabel = "2:15 PM",
                turns = listOf(
                    ConversationTurn(
                        id = "boss_b1_p1",
                        turnNumber = 1,
                        partnerRoleEs = "Cliente en Wall St",
                        partnerLineEn = "Hi! I'm already at the office waiting for our 4 PM session.",
                        partnerLineEsHelper = "¡Hola! Ya estoy en la oficina esperando nuestra sesión de las 4 PM.",
                        taskInstructionEs = "Discúlpate, explica que tu vuelo se retrasó y propón hacerlo por videollamada.",
                        hintEs = "Di \"Sorry, my flight was delayed. Could we do a video call instead?\".",
                        modelExampleEn = "So sorry, my flight was delayed. Could we switch to a video call?",
                        defaultSuccessExplanationEs = "¡Brillante manejo profesional! Explicar el motivo y dar la solución en una sola frase transmite liderazgo.",
                        reusableTipEs = "\"Could we switch to...\" sirve para cambiar formato, hora o lugar.",
                        successReactionEn = "Appreciate the heads-up! A video call works great.",
                        starterSparksEn = listOf("So sorry,", "my flight was delayed.", "Could we do a video call?"),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("flight", "delayed", "late", "video", "call", "online", "zoom"),
                            requirePolitenessForFullPass = true
                        )
                    )
                )
            ),
            review = ConversationVariant(
                titleEs = "Plan B en el aeropuerto",
                sceneGoalEs = "Agradecer la flexibilidad y confirmar el envío del enlace.",
                skillSummaryEs = "Cerrar acuerdos rápidos por teléfono o mensaje en inglés.",
                nycLocationLabel = "JFK Lounge · NYC",
                nycTimeLabel = "2:25 PM",
                turns = listOf(
                    ConversationTurn(
                        id = "boss_b1_r1",
                        turnNumber = 1,
                        partnerRoleEs = "Cliente en Wall St",
                        partnerLineEn = "No problem at all! A video call works for me.",
                        partnerLineEsHelper = "¡Sin problema! Una videollamada me funciona perfecto.",
                        taskInstructionEs = "Agradece su comprensión y di que le envías el enlace ahora mismo.",
                        hintEs = "Di \"Thanks for understanding, I'll send the link right now\".",
                        modelExampleEn = "Thank you for understanding! I'll send the link right now.",
                        defaultSuccessExplanationEs = "¡Cierre profesional de nivel B1!",
                        reusableTipEs = "\"Thank you for understanding\" es oro puro en situaciones laborales.",
                        successReactionEn = "Got it! See you online in a few.",
                        starterSparksEn = listOf("Thank you for", "understanding!", "I'll send the link."),
                        criteria = EvaluationCriteria(
                            requiredAnyKeywords = listOf("thank", "thanks", "link", "send", "appreciate")
                        )
                    )
                )
            )
        )
    )

    val pocketPhraseCards: List<PocketPhraseCard> = lessons.map { lesson ->
        PocketPhraseCard(
            id = "card_${lesson.id}",
            lessonId = lesson.id,
            level = lesson.level,
            categoryEs = lesson.mapStopLabelEs,
            phraseEn = lesson.rewardPhraseEn,
            meaningEs = lesson.rewardPhraseEs
        )
    }

    fun buildBlitzDeck(): List<BlitzRoundItem> {
        val raw = listOf(
            Triple(
                findTurn("cafe_p1"),
                "Could I have a coffee, please?",
                listOf("Give coffee now.", "Where is the coffee table?")
            ),
            Triple(
                findTurn("cafe_p2"),
                "To go, please.",
                listOf("Yes, it is five dollars.", "Turn left at the corner.")
            ),
            Triple(
                findTurn("cafe_p3"),
                "That's all, thanks. How much is it?",
                listOf("I have a reservation.", "For here, please.")
            ),
            Triple(
                findTurn("cafe_r1"),
                "Could I have a tea, please?",
                listOf("My flight was delayed.", "Where is the subway?")
            ),
            Triple(
                findTurn("cafe_r2"),
                "For here, please.",
                listOf("Could you repeat that?", "I need to check out.")
            ),
            Triple(
                findTurn("dir_p1"),
                "Excuse me, where is the pharmacy?",
                listOf("Could we have the check?", "A table for two, please.")
            ),
            Triple(
                findTurn("dir_p2"),
                "Sorry, could you repeat that, please?",
                listOf("To go, please.", "What time is breakfast?")
            ),
            Triple(
                findTurn("dir_p3"),
                "So I turn left at the corner, right?",
                listOf("Can I pay by card?", "I'll have a green tea.")
            ),
            Triple(
                findTurn("hotel_p1"),
                "Hello! I have a reservation.",
                listOf("How much is the total?", "Turn right after the park.")
            ),
            Triple(
                findTurn("hotel_p2"),
                "Thank you! What time is breakfast?",
                listOf("Excuse me, I ordered coffee.", "Could we switch to a video call?")
            ),
            Triple(
                findTurn("rest_p1"),
                "Hi! A table for two, please.",
                listOf("Where is the nearest ATM?", "Could I leave my luggage here?")
            ),
            Triple(
                findTurn("resch_p1"),
                "Hi, I need to reschedule my appointment today.",
                listOf("Do you have vegetarian options?", "What is the Wi-Fi password?")
            )
        )

        return raw.mapNotNull { (turn, correct, distractors) ->
            turn?.let {
                BlitzRoundItem(
                    id = "blitz_${it.id}",
                    partnerRoleEs = it.partnerRoleEs,
                    partnerLineEn = it.partnerLineEn,
                    taskInstructionEs = it.taskInstructionEs,
                    correctOptionEn = correct,
                    optionsEn = (listOf(correct) + distractors).shuffled(),
                    turnForFreeEvaluation = it,
                    explanationEs = it.defaultSuccessExplanationEs
                )
            }
        }.shuffled()
    }

    private fun findTurn(turnId: String): ConversationTurn? {
        return lessons
            .flatMap { it.practice.turns + it.review.turns }
            .firstOrNull { it.id == turnId }
    }

    fun findLesson(lessonId: String): Lesson? = lessons.firstOrNull { it.id == lessonId }

    fun findVoiceProfile(voiceId: String): VoiceProfile =
        voiceProfiles.firstOrNull { it.id == voiceId } ?: voiceProfiles.first()
}
