package com.lunaflow.app.domain.manager

import com.lunaflow.app.domain.model.CyclePhase

object DailyTipsManager {

    private val menstrualTips = listOf(
        "🌑 Your body is doing hard work right now. Rest is not laziness — it's self-care!",
        "🍫 Craving chocolate? Dark chocolate has magnesium that can ease cramps. Go for it!",
        "🛁 A warm heating pad on your lower belly can help reduce cramp discomfort.",
        "💧 Stay hydrated! Drinking water helps reduce bloating during your period.",
        "🧘 Gentle yoga or stretching can ease tension in your lower back and abdomen.",
        "🥦 Iron-rich foods like spinach, lentils, and beans help replace what your body loses.",
        "😴 Getting enough sleep is extra important right now — your body is recovering.",
        "🩸 Tracking your flow helps you understand your body better over time.",
        "☕ Try limiting caffeine today — it can make cramps and mood swings worse.",
        "🌿 Chamomile tea has anti-inflammatory properties and can help ease period cramps."
    )

    private val follicularTips = listOf(
        "🌒 Your energy is rising! This is a great time to start new projects or workouts.",
        "🥗 Focus on fiber-rich foods — vegetables, fruits, and whole grains support your hormones.",
        "💪 Your strength and endurance are naturally higher now. Push a little harder at the gym!",
        "🧠 Estrogen is rising — your memory and focus are sharper. Take advantage of it!",
        "🌸 This phase is great for trying new things. Sign up for something you've been curious about.",
        "☀️ Spending time outdoors and getting sunlight boosts your serotonin naturally.",
        "🍓 Antioxidant-rich foods like berries and citrus support healthy cell growth.",
        "🎨 Your creative energy peaks in this phase — journaling, art, or music can feel amazing.",
        "👯‍♀️ Social energy tends to be higher now — a great time to connect with friends.",
        "🥚 Your follicles are maturing — your body is preparing for its most fertile time."
    )

    private val ovulationTips = listOf(
        "🌕 You're at your peak energy and confidence — embrace it!",
        "💬 Communication feels easier during ovulation. Have that important conversation now.",
        "❤️ Your fertility is highest right now. Be mindful if you're not trying to conceive.",
        "🏃‍♀️ A great time for intense workouts — your body handles physical demands better now.",
        "🌡️ You might notice a slight rise in body temperature — that's completely normal!",
        "💧 You may have more discharge than usual — this is healthy and normal.",
        "🥑 Healthy fats from avocado, nuts, and olive oil support hormone production.",
        "✨ Your skin may look clearest and most vibrant right now — enjoy it!",
        "🎯 Decision-making feels more natural now — tackle big choices with confidence.",
        "🌺 Some people feel a mild twinge on one side around ovulation — that's called mittelschmerz!"
    )

    private val lutealTips = listOf(
        "🌖 Your body is preparing for the next phase. Listen to what it needs.",
        "😊 If you feel more emotional, that's normal — progesterone is rising.",
        "🍌 Foods rich in B6 (bananas, potatoes, chicken) may help with PMS symptoms.",
        "🧘 Meditation and breathing exercises can help manage the stress response of this phase.",
        "🚫 Reducing salt intake now can help prevent PMS bloating.",
        "😴 Your sleep may be disrupted — try limiting screens before bed.",
        "🏋️ Opt for moderate exercise like walking or yoga instead of intense sessions.",
        "📓 If you feel anxious or down, journaling can be a powerful release.",
        "🫖 Raspberry leaf tea is traditionally used to tone the uterus and ease PMS.",
        "💆 Self-massage on your feet and hands can help relieve PMS tension.",
        "🍰 Cravings are real and have a hormonal cause. Satisfy them mindfully!",
        "🌙 This phase is about turning inward. Rest, reflect, and restore your energy."
    )

    fun getTipForPhase(phase: CyclePhase, index: Int = 0): String {
        val tips = when (phase) {
            CyclePhase.MENSTRUAL  -> menstrualTips
            CyclePhase.FOLLICULAR -> follicularTips
            CyclePhase.OVULATION  -> ovulationTips
            CyclePhase.LUTEAL     -> lutealTips
        }
        return tips[index % tips.size]
    }

    fun getAllTipsForPhase(phase: CyclePhase): List<String> = when (phase) {
        CyclePhase.MENSTRUAL  -> menstrualTips
        CyclePhase.FOLLICULAR -> follicularTips
        CyclePhase.OVULATION  -> ovulationTips
        CyclePhase.LUTEAL     -> lutealTips
    }

    fun getTipCount(phase: CyclePhase): Int = getAllTipsForPhase(phase).size
}
