package zadanie2_2;

public class Main {
     /*
     * ZADANIE 2.2 - Dowód na to, dlaczego 2 to absolutne minimum
     *
     * Aby końcowa wartość licznika po zakończeniu całego programu wynosiła 1,
     * absolutnie ostatnia instrukcja WRITE w systemie musiałaby zapisać wartość 1.
     * To oznacza, że wątek wykonujący ten zapis musiałby w swojej ostatniej (piątej)
     * iteracji pętli odczytać z pamięci wartość 0.
     *
     * Jest to jednak logicznie niemożliwe z następujących powodów:
     *
     * 1. Żaden wątek w systemie nigdy nie zmniejsza licznika ani nie zapisuje zera.
     *    Zawsze zapisywana jest wartość o 1 większa od odczytanej (czyli co najmniej 1).
     * 2. Zanim jakikolwiek wątek dotrze do swojej ostatniej (piątej) iteracji,
     *    musiał już wykonać cztery wcześniejsze zapisy do pamięci. To gwarantuje, że
     *    początkowe zero dawno przestało istnieć.
     * 3. Najmniejszą wartością, jaka może "magicznie" pojawić się w pamięci tuż przed
     *    ostatnim odczytem, jest 1. Może ona pochodzić wyłącznie od innego wątku,
     *    który w swojej pierwszej iteracji zapamiętał początkowe 0, usnął na długi czas
     *    i dopiero teraz obudził się, by zapisać 1 (tak jak to opisano w przeplocie z 2.1).
     *
     * Wniosek:
     * Skoro najmniejszą wartością, jaką jakikolwiek wątek może odczytać (READ) na starcie
     * swojej ostatniej iteracji jest 1, to po dodaniu jedynki (INC) najmniejszą możliwą
     * do zapisania wartością (WRITE) kończącą program jest 1 + 1 = 2.
     */
}
