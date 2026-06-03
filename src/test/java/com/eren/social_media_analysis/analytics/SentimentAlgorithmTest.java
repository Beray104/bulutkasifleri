package com.eren.social_media_analysis.analytics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SentimentAlgorithmTest {

    /** Eski algoritma: basit sözlük, ağırlıksız */
    static String oldAlgorithm(String content) {
        String lower = content.toLowerCase(new Locale("tr", "TR"));
        String[] tokens = lower.replaceAll("[^\\p{L}\\p{N} ]", " ").split("\\s+");
        Set<String> posWords = new HashSet<>(Arrays.asList(
                "iyi","harika","mukemmel","pozitif","basarili","sevildi","great","good","happy"));
        Set<String> negWords = new HashSet<>(Arrays.asList(
                "berbat","kotu","rezalet","uzucu","terrible","bad","sad","fail"));

        long pos = Arrays.stream(tokens).filter(posWords::contains).count();
        long neg = Arrays.stream(tokens).filter(negWords::contains).count();
        long score = pos - neg;
        if (score > 0) return "POSITIVE";
        if (score < 0) return "NEGATIVE";
        return "NEUTRAL";
    }

    /** Yeni algoritma: ağırlıklı sözlük + kural tabanlı */
    static String newAlgorithm(String content) {
        String lower = content.toLowerCase(new Locale("tr", "TR"));
        String[] tokens = lower.replaceAll("[^\\p{L}\\p{N} ]", " ").split("\\s+");
        Set<String> tokenSet = new HashSet<>(Arrays.asList(tokens));

        // Ağırlıklı sözlükler
        Map<String, Integer> posWeights = new HashMap<>();
        // Güçlü +3
        for (String w : new String[]{"mükemmel","mukemmel","harika","süper","super","excellent","amazing","outstanding","perfect","wonderful","love"})
            posWeights.put(w, 3);
        // Orta +2
        for (String w : new String[]{"iyi","güzel","guzel","başarılı","basarili","good","great","happy","nice","cool"})
            posWeights.put(w, 2);
        // Zayıf +1
        for (String w : new String[]{"tamam","fena","ok","fine","okay","decent","alright"})
            posWeights.put(w, 1);

        Map<String, Integer> negWeights = new HashMap<>();
        // Güçlü -3
        for (String w : new String[]{"berbat","rezalet","korkunç","korkunc","terrible","horrible","awful","hate","worst"})
            negWeights.put(w, 3);
        // Orta -2
        for (String w : new String[]{"kötü","kotu","üzücü","uzucu","başarısız","basarisiz","bad","sad","angry","upset","poor","fail"})
            negWeights.put(w, 2);
        // Zayıf -1
        for (String w : new String[]{"vasat","sıradan","siradan","boring","dull","mediocre"})
            negWeights.put(w, 1);

        // Ham skor
        int rawScore = 0;
        int posCount = 0;
        int negCount = 0;
        for (String t : tokens) {
            if (posWeights.containsKey(t)) { rawScore += posWeights.get(t); posCount++; }
            if (negWeights.containsKey(t)) { rawScore -= negWeights.get(t); negCount++; }
        }

        // Olumsuzlama
        Set<String> negationSet = new HashSet<>(Arrays.asList(
                "değil","degil","yok","olmaz","hayır","hayir","hiç","hic","not","no","never"));
        boolean hasNegation = tokenSet.stream().anyMatch(negationSet::contains);
        if (hasNegation) {
            if (posCount > 0 && negCount == 0) {
                 rawScore = -rawScore;
            } else {
                 rawScore = -Math.abs(rawScore) - 2;
            }
        }

        // Büyük harf yoğunluğu
        long upperCount = content.chars().filter(Character::isUpperCase).count();
        double upperRatio = (double) upperCount / (content.length() + 1);
        if (upperRatio > 0.3) rawScore = (int)(rawScore * 1.2);

        // Soru cümlesi
        boolean isQuestion = content.contains("?");
        if (isQuestion) rawScore = 0;

        // Normalize
        double normalized = (double) rawScore / (tokens.length + 1);
        if (normalized > 0.05) return "POSITIVE";
        if (normalized < -0.05) return "NEGATIVE";
        return "NEUTRAL";
    }

    // ----------------------------------------------------------------
    // Test verisi: [metin, beklenen_etiket]
    // ----------------------------------------------------------------

    @ParameterizedTest(name = "[{index}] \"{0}\" -> {1}")
    @CsvSource({
        // Açık pozitif
        "Bu ürün harika çok memnun kaldım,                    POSITIVE",
        "Excellent service loved every moment,                 POSITIVE",
        "Bugün iyi bir gün güzel haberler aldım,              POSITIVE",
        // Açık negatif
        "Bu uygulama berbat hiç beğenmedim,                   NEGATIVE",
        "Terrible experience would not recommend,              NEGATIVE",
        "Kötü hizmet çok üzücü bir deneyimdi,                 NEGATIVE",
        // Olumsuzlama (zor durum)
        "Bu uygulama harika değil,                             NEGATIVE",
        "Not good at all very bad service,                     NEGATIVE",
        "İyi değil berbat,                                     NEGATIVE",
        // Soru (nötr'e çekilmeli)
        "Bu ürün iyi mi?,                                      NEUTRAL",
        "Is this service good or bad?,                         NEUTRAL",
        // Nötr
        "Bugün hava bulutlu,                                   NEUTRAL",
        "Toplantı saat 3 te,                                   NEUTRAL",
        // Karışık (negatif ağır basmalı)
        "Bir kısım iyi ama genel olarak berbat rezalet,        NEGATIVE",
        // Büyük harf yoğunluğu
        "BU UYGULAMA HARIKA MÜKEMMEL,                         POSITIVE",
        "BU BERBAT REZALET,                                     NEGATIVE",
    })
    @DisplayName("Yeni algoritma doğruluk testi")
    void newAlgorithmAccuracyTest(String text, String expected) {
        String result = newAlgorithm(text.trim());
        assertEquals(expected.trim(), result,
                "Metin: \"" + text.trim() + "\" -> Beklenen: " + expected.trim() + ", Gerçek: " + result);
    }

    @Test
    @DisplayName("Eski vs Yeni algoritma karşılaştırma raporu")
    void compareAlgorithms() {
        List<String[]> testData = Arrays.asList(
            new String[]{"Bu ürün harika çok memnun kaldım",       "POSITIVE"},
            new String[]{"Excellent service loved every moment",    "POSITIVE"},
            new String[]{"Bu uygulama berbat hiç beğenmedim",       "NEGATIVE"},
            new String[]{"Terrible experience would not recommend", "NEGATIVE"},
            new String[]{"Bu uygulama harika değil",                "NEGATIVE"},  // olumsuzlama
            new String[]{"Not good at all very bad service",        "NEGATIVE"},  // olumsuzlama
            new String[]{"Bu ürün iyi mi?",                         "NEUTRAL"},   // soru
            new String[]{"Bugün hava bulutlu",                      "NEUTRAL"},
            new String[]{"Bir kısım iyi ama genel olarak berbat",   "NEGATIVE"},
            new String[]{"İyi değil berbat",                        "NEGATIVE"}   // olumsuzlama
        );

        int oldCorrect = 0, newCorrect = 0;

        for (String[] td : testData) {
            String text     = td[0];
            String expected = td[1];
            String oldRes   = oldAlgorithm(text);
            String newRes   = newAlgorithm(text);
            if (oldRes.equals(expected)) oldCorrect++;
            if (newRes.equals(expected)) newCorrect++;
        }

        double oldAcc = (double) oldCorrect / testData.size() * 100;
        double newAcc = (double) newCorrect / testData.size() * 100;

        assertTrue(newAcc > oldAcc, "Yeni algoritma eski algoritmadan daha doğru olmalı!");
        assertTrue(newAcc >= 70.0, "Yeni algoritma en az %70 doğruluk hedefini geçmeli!");
    }
}