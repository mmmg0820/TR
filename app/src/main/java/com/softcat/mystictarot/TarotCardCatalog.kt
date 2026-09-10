package com.softcat.mystictarot

private val majorMeaningOverrides = mapOf(
    "The Fool" to "새로운 시작과 가능성이 열리는 시기입니다. 두려움보다 호기심을 믿고 움직이면 예상 밖의 기회가 찾아올 수 있습니다.",
    "The Magician" to "당신에게 필요한 재능과 도구는 이미 갖춰져 있습니다. 생각만 하던 일을 실제 행동으로 옮길 때 좋은 흐름이 생깁니다.",
    "The High Priestess" to "겉으로 보이는 정보보다 마음속 직감이 더 중요한 시기입니다. 서두르기보다 조용히 관찰하면 숨은 답을 발견할 수 있습니다.",
    "The Empress" to "관계와 일상에 따뜻한 성장의 기운이 흐릅니다. 자신을 돌보고 주변과 나누는 태도가 더 큰 풍요를 부릅니다.",
    "The Emperor" to "흔들리는 상황을 정리하고 기준을 세워야 할 때입니다. 감정보다는 구조와 책임감이 문제를 해결하는 열쇠가 됩니다.",
    "The Hierophant" to "검증된 방식이나 신뢰할 수 있는 조언이 도움이 됩니다. 혼자 판단하기보다 경험 있는 사람의 지혜를 참고해 보세요.",
    "The Lovers" to "중요한 관계나 선택의 기로에 서 있습니다. 마음이 끌리는 방향과 책임져야 할 현실 사이의 균형이 필요합니다.",
    "The Chariot" to "목표를 향해 강하게 밀고 나갈 수 있는 흐름입니다. 다만 속도보다 방향을 분명히 해야 원하는 결과에 가까워집니다.",
    "Strength" to "강하게 밀어붙이기보다 부드럽게 버티는 힘이 필요한 때입니다. 감정을 다스리면 상황은 생각보다 안정적으로 풀립니다.",
    "The Hermit" to "잠시 거리를 두고 자신만의 답을 찾아야 합니다. 외부의 소음보다 내면의 기준을 확인하는 시간이 중요합니다.",
    "Wheel of Fortune" to "상황이 새로운 국면으로 바뀌고 있습니다. 변화를 억지로 막기보다 흐름을 읽고 유연하게 대응하는 것이 좋습니다.",
    "Justice" to "공정한 판단과 책임 있는 선택이 필요한 시기입니다. 감정에 치우치지 않고 사실을 기준으로 보면 답이 분명해집니다.",
    "The Hanged Man" to "지금은 억지로 움직이기보다 관점을 바꿔야 할 때입니다. 잠시 멈춤이 오히려 더 나은 선택을 준비하게 해줍니다.",
    "Death" to "오래된 흐름이 끝나고 새로운 단계가 시작됩니다. 붙잡고 있던 것을 정리하면 더 건강한 변화가 찾아옵니다.",
    "Temperance" to "서로 다른 요소를 차분히 맞춰야 하는 시기입니다. 급한 결론보다 적절한 조율이 좋은 결과를 만듭니다.",
    "The Devil" to "무언가에 지나치게 묶여 있지는 않은지 돌아봐야 합니다. 욕망을 인정하되 그것이 당신을 지배하지 않게 하는 것이 중요합니다.",
    "The Tower" to "예상치 못한 변화가 기존의 틀을 흔들 수 있습니다. 불편하더라도 진실을 마주하면 더 단단한 기반을 다시 세울 수 있습니다.",
    "The Star" to "어두운 시간을 지나 회복과 희망의 기운이 들어옵니다. 작지만 진심 어린 믿음이 앞으로 나아갈 힘이 됩니다.",
    "The Moon" to "상황이 명확하지 않아 불안이 커질 수 있습니다. 확실하지 않은 정보에 휘둘리기보다 시간을 두고 진실을 확인하세요.",
    "The Sun" to "밝고 긍정적인 결과가 기대되는 카드입니다. 자신감을 가지고 표현하면 주변의 인정과 좋은 성과가 따를 수 있습니다.",
    "Judgement" to "과거를 돌아보고 중요한 결정을 내려야 할 때입니다. 스스로를 탓하기보다 배운 것을 바탕으로 새롭게 출발하세요.",
    "The World" to "하나의 여정이 완성에 가까워지고 있습니다. 지금까지의 노력이 결실을 맺으며 다음 단계로 넘어갈 준비가 됩니다."
)

private fun minorMeaning(suitKr: String, rankKr: String): String {
    val suitTheme = when (suitKr) {
        "완드" -> "의지, 시작, 추진력"
        "컵" -> "감정, 관계, 마음의 흐름"
        "소드" -> "생각, 판단, 갈등의 정리"
        "펜타클" -> "현실, 일, 돈과 생활 기반"
        else -> "현실적인 흐름"
    }
    val rankTheme = when (rankKr) {
        "에이스" -> "새로운 가능성이 막 열리는 단계"
        "2" -> "두 방향 사이의 균형과 선택"
        "3" -> "협력, 확장, 다음 단계의 준비"
        "4" -> "안정, 휴식, 구조를 세우는 과정"
        "5" -> "마찰, 결핍, 흔들림을 마주하는 시기"
        "6" -> "회복, 교류, 이전보다 나아지는 흐름"
        "7" -> "방어, 평가, 스스로 기준을 지키는 태도"
        "8" -> "속도, 반복, 집중해서 밀어붙이는 힘"
        "9" -> "완성 직전의 긴장과 마지막 점검"
        "10" -> "하나의 주기가 끝나며 부담이나 결실이 드러나는 단계"
        "페이지" -> "서툴지만 신선한 배움과 호기심"
        "기사" -> "움직임, 돌파, 방향을 향한 강한 에너지"
        "여왕" -> "돌봄, 수용, 성숙하게 다루는 힘"
        "왕" -> "책임, 통제, 주도권을 잡는 단계"
        else -> "지금의 선택을 점검하는 흐름"
    }
    return "$suitKr $rankKr 카드는 $suitTheme 영역에서 ${rankTheme}이라는 흐름을 보여줍니다. 지금은 카드가 놓인 위치의 의미와 함께, 무엇을 키우고 무엇을 조절해야 하는지 차분히 살펴보세요."
}

val standardTarot78: List<TarotCard> = buildList {
    val major = listOf(
        "The Fool" to "바보", "The Magician" to "마법사", "The High Priestess" to "여사제", "The Empress" to "여황제",
        "The Emperor" to "황제", "The Hierophant" to "교황", "The Lovers" to "연인", "The Chariot" to "전차",
        "Strength" to "힘", "The Hermit" to "은둔자", "Wheel of Fortune" to "운명의 수레바퀴", "Justice" to "정의",
        "The Hanged Man" to "매달린 사람", "Death" to "죽음", "Temperance" to "절제", "The Devil" to "악마",
        "The Tower" to "탑", "The Star" to "별", "The Moon" to "달", "The Sun" to "태양",
        "Judgement" to "심판", "The World" to "세계"
    )
    major.forEachIndexed { index, (en, kr) ->
        add(TarotCard(index, en, kr, "Major Arcana", majorMeaningOverrides[en] ?: "$kr 카드는 현재 흐름에서 중요한 전환점과 상징적인 메시지를 보여줍니다."))
    }

    val suits = listOf("Wands" to "완드", "Cups" to "컵", "Swords" to "소드", "Pentacles" to "펜타클")
    val ranks = listOf(
        "Ace" to "에이스", "Two" to "2", "Three" to "3", "Four" to "4", "Five" to "5", "Six" to "6", "Seven" to "7",
        "Eight" to "8", "Nine" to "9", "Ten" to "10", "Page" to "페이지", "Knight" to "기사", "Queen" to "여왕", "King" to "왕"
    )
    suits.forEach { (suitEn, suitKr) ->
        ranks.forEach { (rankEn, rankKr) ->
            val id = size
            add(
                TarotCard(
                    id = id,
                    nameEn = "$rankEn of $suitEn",
                    nameKr = "$suitKr $rankKr",
                    arcana = "Minor Arcana",
                    basicMeaning = minorMeaning(suitKr, rankKr)
                )
            )
        }
    }
}
