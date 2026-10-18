package zadanie1;

/*
 * ZADANIE 1 - Semafor binarny (wait / notify) i synchronizacja programu "Wyścig"
 *
 * "Wyścig" to program z laboratorium 1 (zadanie1_1): dwa wątki działają na wspólnym
 * liczniku - jeden 10000 razy go zwiększa, drugi 10000 razy zmniejsza.
 * Bez synchronizacji wynik końcowy jest losowy (zamiast 0), bo value++ / value--
 * nie są operacjami atomowymi (read - modify - write).
 *
 * Semafor binarny ma dwa stany (true = otwarty, false = zamknięty):
 *   P() (opuszczenie): jeśli stan == true, ustaw false; w przeciwnym razie wstrzymaj wątek.
 *   V() (podniesienie): jeśli są wątki wstrzymane na P() - obudź jeden z nich,
 *                       w przeciwnym razie ustaw stan na true.
 *
 * Implementacja używa monitora obiektu (synchronized) oraz metod wait() i notify().
 * Czekanie na stan jest w pętli while (powód - patrz zadanie2).
 */
class BinarySemaphore {
    private boolean state;

    public BinarySemaphore() {
        this(true);
    }

    public BinarySemaphore(boolean initialState) {
        this.state = initialState;
    }

    public synchronized void P() throws InterruptedException {
        while (!state) {
            wait();
        }
        state = false;
    }

    public synchronized void V() {
        state = true;
        notify();
    }
}

class Counter {
    private int value = 0;
    private final BinarySemaphore semaphore;

    public Counter(BinarySemaphore semaphore) {
        this.semaphore = semaphore;
    }

    public void increment() throws InterruptedException {
        semaphore.P();
        try {
            value++;
        } finally {
            semaphore.V();
        }
    }

    public void decrement() throws InterruptedException {
        semaphore.P();
        try {
            value--;
        } finally {
            semaphore.V();
        }
    }

    public int getValue() {
        return value;
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int runs = 20;
        int iterations = 100000;
        int incorrect = 0;

        for (int r = 0; r < runs; r++) {
            Counter counter = new Counter(new BinarySemaphore());

            Thread t1 = new Thread(() -> {
                try {
                    for (int i = 0; i < iterations; i++) counter.increment();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            Thread t2 = new Thread(() -> {
                try {
                    for (int i = 0; i < iterations; i++) counter.decrement();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            t1.start();
            t2.start();

            t1.join();
            t2.join();

            System.out.println("Uruchomienie " + (r + 1) + ": końcowa wartość licznika = " + counter.getValue());
            if (counter.getValue() != 0) incorrect++;
        }

        System.out.println("Niepoprawnych wyników: " + incorrect + " / " + runs);
    }
}
