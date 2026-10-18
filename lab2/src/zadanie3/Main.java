package zadanie3;

import java.util.concurrent.atomic.AtomicInteger;

/*
 * ZADANIE 3 - Semafor licznikowy (ogólny) zaimplementowany za pomocą semaforów binarnych
 *
 * Wartość semafora ogólnego (value) jest chroniona semaforem binarnym "mutex".
 * Wątki, które nie mogą przejść (value == 0), czekają na drugim semaforze binarnym "queue".
 * Liczba czekających wątków jest zapamiętywana w zmiennej "waiting".
 *
 * Uwaga: naiwne rozwiązanie (P: value--; if (value < 0) queue.P(); V: value++; queue.V())
 * jest błędne, bo semafor binarny nie zlicza podniesień. Dwa kolejne V() na queue,
 * zanim obudzony wątek zdąży zareagować, dają tylko jedno przepuszczenie (drugie V
 * "ginie"), więc część wątków zostaje uśpiona na zawsze.
 *
 * Dlatego stosujemy technikę "przekazywania pałeczki" (passing the baton):
 * wątek wykonujący V() nie zwalnia mutexa, jeśli ktoś czeka - budzi go, a ten
 * dziedziczy mutex i dopiero on go zwalnia (albo przekazuje dalej).
 * Dzięki temu queue.V() jest wywoływane tylko wtedy, gdy queue ma stan false,
 * a stan wartości i liczby czekających nigdy nie jest widziany w stanie pośrednim.
 *
 * CZY SEMAFOR BINARNY JEST SZCZEGÓLNYM PRZYPADKIEM OGÓLNEGO?
 * Tak - w sensie użycia: semafor ogólny z wartością początkową 1, używany poprawnie
 * (każde V() poprzedzone swoim P()), zachowuje się jak semafor binarny
 * (wartość nigdy nie przekracza 1) - ta własność jest wykorzystana np. w mutexie.
 * Różnica jest w definicji operacji V(): semafor binarny jest "nasycany"
 * (V przy stanie 1 nic nie zmienia), a ogólny zwiększa wartość (1 -> 2 -> 3...).
 * Ściśle więc semafor binarny to semafor ogólny z ograniczoną do {0,1} wartością;
 * nie można go zastąpić ogólnym bez dodatkowego założenia o poprawnym użyciu,
 * a jak widać w zadaniu - ogólny można zbudować z dwóch binarnych (są równoważne
 * pod względem siły wyrazu).
 */
class BinarySemaphore {
    private boolean state;

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

class CountingSemaphore {
    private int value;
    private int waiting = 0;
    private final BinarySemaphore mutex = new BinarySemaphore(true);
    private final BinarySemaphore queue = new BinarySemaphore(false);

    public CountingSemaphore(int initialValue) {
        if (initialValue < 0) throw new IllegalArgumentException("wartość początkowa < 0");
        this.value = initialValue;
    }

    public void P() throws InterruptedException {
        mutex.P();
        if (value == 0) {
            waiting++;
            mutex.V();
            queue.P();          // po obudzeniu wątek dziedziczy mutex (pałeczka)
            waiting--;
        }
        value--;
        release();
    }

    public void V() throws InterruptedException {
        mutex.P();
        value++;
        release();
    }

    // Wywoływane przez wątek posiadający mutex.
    private void release() {
        if (waiting > 0 && value > 0) {
            queue.V();          // przekazanie pałeczki czekającemu wątkowi
        } else {
            mutex.V();
        }
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int permits = 3;
        int threads = 10;
        int iterations = 2000;

        CountingSemaphore semaphore = new CountingSemaphore(permits);
        AtomicInteger inside = new AtomicInteger();
        AtomicInteger maxInside = new AtomicInteger();
        AtomicInteger violations = new AtomicInteger();

        Thread[] workers = new Thread[threads];
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                try {
                    for (int i = 0; i < iterations; i++) {
                        semaphore.P();
                        int now = inside.incrementAndGet();
                        if (now > permits) violations.incrementAndGet();
                        maxInside.accumulateAndGet(now, Math::max);
                        Thread.yield();
                        inside.decrementAndGet();
                        semaphore.V();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        for (Thread w : workers) w.start();
        for (Thread w : workers) w.join();

        System.out.println("Semafor licznikowy, wartość początkowa = " + permits);
        System.out.println("Wątków: " + threads + ", iteracji na wątek: " + iterations);
        System.out.println("Maksymalnie jednocześnie w sekcji krytycznej: " + maxInside.get());
        System.out.println("Naruszeń limitu: " + violations.get());

        // Semafor ogólny z wartością 1 używany jako mutex (zachowuje się jak binarny)
        CountingSemaphore mutexLike = new CountingSemaphore(1);
        int[] counter = {0};
        Thread[] incs = new Thread[4];
        for (int t = 0; t < incs.length; t++) {
            incs[t] = new Thread(() -> {
                try {
                    for (int i = 0; i < 50000; i++) {
                        mutexLike.P();
                        counter[0]++;
                        mutexLike.V();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        for (Thread w : incs) w.start();
        for (Thread w : incs) w.join();
        System.out.println("Semafor ogólny (1) jako mutex: licznik = " + counter[0] + " (oczekiwano 200000)");
    }
}
