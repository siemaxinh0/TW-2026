package zadanie2;

import java.util.concurrent.atomic.AtomicInteger;

/*
 * ZADANIE 2 - Dlaczego w semaforze (wait / notify) potrzebny jest while, a nie if
 *
 * TEORIA
 * Z monitorem obiektu związane są dwie różne kolejki:
 *   1. kolejka wejściowa (entry set) - wątki, które chcą wejść do bloku synchronized
 *      i czekają na zajęty monitor,
 *   2. kolejka oczekujących (wait set) - wątki, które wywołały wait() (zwolniły monitor
 *      i śpią, dopóki ktoś nie wywoła notify()).
 *
 * Wątek obudzony przez notify() NIE dostaje monitora od razu - przechodzi z wait set
 * do kolejki wejściowej i musi konkurować o monitor z wszystkimi innymi wątkami,
 * które już w niej czekają lub dopiero wywołają P().
 *
 * Wersja z if - scenariusz błędu (wątki A, B, C):
 *   1. A trzyma semafor (stan = false). B wywołuje P(), widzi false i usypia w wait().
 *   2. A wywołuje V(): stan = true, notify() budzi B -> B trafia do kolejki wejściowej.
 *   3. Zanim B dostanie monitor, A (albo C) wywołuje P(): monitor jest wolny, stan == true,
 *      więc przechodzi dalej i ustawia stan = false (wchodzi do sekcji krytycznej).
 *   4. B dostaje monitor i wraca z wait(). Instrukcja if nie sprawdza warunku ponownie,
 *      więc B ustawia stan = false i też wchodzi do sekcji krytycznej.
 *   => dwa wątki są jednocześnie w sekcji krytycznej, wzajemne wykluczanie jest złamane.
 *
 * Wersja z while: po obudzeniu B sprawdza warunek jeszcze raz, widzi stan == false
 * i wraca do wait(). Warunek jest więc weryfikowany w momencie, gdy wątek faktycznie
 * posiada monitor, a nie w momencie, gdy został powiadomiony. Pętla chroni też przed
 * tzw. spurious wakeups (wybudzenie bez notify), które dopuszcza specyfikacja Javy.
 *
 * EKSPERYMENT
 * Kilka wątków wielokrotnie wykonuje: P(); sekcja krytyczna; V().
 * W sekcji krytycznej zliczamy, ilu wątków jest w niej jednocześnie
 * oraz zwiększamy wspólny licznik (wynik powinien być równy liczbie wykonań).
 */
class SemaphoreIf {
    private boolean state = true;

    public synchronized void P() throws InterruptedException {
        if (!state) {          // BŁĄD: warunek nie jest sprawdzany ponownie po obudzeniu
            wait();
        }
        state = false;
    }

    public synchronized void V() {
        state = true;
        notify();
    }
}

class SemaphoreWhile {
    private boolean state = true;

    public synchronized void P() throws InterruptedException {
        while (!state) {       // POPRAWNIE: warunek sprawdzany po każdym obudzeniu
            wait();
        }
        state = false;
    }

    public synchronized void V() {
        state = true;
        notify();
    }
}

interface Sem {
    void P() throws InterruptedException;

    void V();
}

public class Main {
    static int sharedCounter;

    static void experiment(String name, Sem sem, int threads, int iterations) throws InterruptedException {
        sharedCounter = 0;
        AtomicInteger inside = new AtomicInteger();
        AtomicInteger violations = new AtomicInteger();
        AtomicInteger maxInside = new AtomicInteger();

        Thread[] workers = new Thread[threads];
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                try {
                    for (int i = 0; i < iterations; i++) {
                        sem.P();
                        int now = inside.incrementAndGet();
                        if (now > 1) violations.incrementAndGet();
                        maxInside.accumulateAndGet(now, Math::max);
                        sharedCounter++;
                        inside.decrementAndGet();
                        sem.V();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        for (Thread w : workers) w.start();
        for (Thread w : workers) w.join();

        System.out.println(name + ":");
        System.out.println("  max wątków jednocześnie w sekcji krytycznej = " + maxInside.get());
        System.out.println("  naruszeń wzajemnego wykluczania             = " + violations.get());
        System.out.println("  licznik = " + sharedCounter + " (oczekiwano " + (threads * iterations) + ")");
    }

    public static void main(String[] args) throws InterruptedException {
        int threads = 8;
        int iterations = 100000;

        SemaphoreIf semIf = new SemaphoreIf();
        experiment("Semafor z instrukcją if   ", new Sem() {
            public void P() throws InterruptedException { semIf.P(); }
            public void V() { semIf.V(); }
        }, threads, iterations);

        SemaphoreWhile semWhile = new SemaphoreWhile();
        experiment("Semafor z instrukcją while", new Sem() {
            public void P() throws InterruptedException { semWhile.P(); }
            public void V() { semWhile.V(); }
        }, threads, iterations);
    }
}
