package zadanie2_1;

public class Main {
    /*
     * ZADANIE 2.1 - Wyjaśnienie najmniejszej możliwej wartości licznika
     *
     * Teoretycznie najmniejsza wartość licznika po zakończeniu działania N wątków wynosi 2 (zakładając N >= 2).
     * Wynika to z faktu, że operacja inkrementacji nie jest atomowa i składa się z trzech odrębnych kroków:
     * READ (odczyt z pamięci), INC (zwiększenie o 1 w rejestrze) i WRITE (zapis z powrotem do pamięci).
     *
     * Dokładny przeplot (kolejność instrukcji) prowadzący do wyniku 2:
     *
     * 1. Wątek 2 (W2) rozpoczyna swoją 1. iterację: robi READ i odczytuje początkowe 0.
     *    Natychmiast po tym zostaje wstrzymany (wywłaszczony) przez planistę.
     * 2. Wątek 1 (W1) wykonuje pełne 4 iteracje (odczytuje, zwiększa, zapisuje). Licznik wynosi teraz 4.
     * 3. Wszystkie pozostałe wątki w systemie (W3...WN) wykonują się w całości od początku do końca,
     *    podbijając licznik do bardzo dużej wartości.
     * 4. W2 zostaje wznowiony. Kontynuuje swoją pierwszą iterację opartą na zapamiętanym wcześniej zerze:
     *    robi INC (0+1) i WRITE (zapisuje 1). Praca wszystkich innych wątków zostaje nadpisana jedynką.
     * 5. W1 rozpoczyna swoją ostatnią, 5. iterację. Robi READ i odczytuje aktualną wartość 1.
     *    Natychmiast po tym odczycie znów zostaje wstrzymany.
     * 6. W2 wznawia pracę i bez przerw wykonuje swoje pozostałe 4 iteracje.
     *    Ostatnim jego działaniem jest zapisanie w pamięci wartości 5, po czym W2 kończy życie.
     * 7. Na samym końcu budzi się wstrzymany w kroku 5. Wątek 1.
     *    Jako że pamięta w swoim rejestrze odczytaną wcześniej wartość 1, wykonuje instrukcję INC (1+1)
     *    oraz swój ostateczny WRITE, zapisując na sam koniec programu wartość 2.
     */
}
