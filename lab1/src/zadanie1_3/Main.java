package zadanie1_3;

// Nasz autorski zamek oparty na Algorytmie Petersona
class PetersonLock {
    // Słowo kluczowe 'volatile' jest tu niezbędne, aby wątki widziały nawzajem
    // zmiany tych zmiennych w pamięci RAM (zapobiega to cachowaniu ich w rejestrach procesora).
    // Nie jest to mechanizm synchronizujący w sensie 'locka', lecz dyrektywa pamięciowa.
    private volatile boolean flag0 = false;
    private volatile boolean flag1 = false;
    private volatile int turn = 0;

    // Metody dla Wątku 0 (inkrementującego)
    public void lock0() {
        flag0 = true;            // Zgłaszam chęć wejścia
        turn = 1;                // Ale daję pierwszeństwo drugiemu wątkowi
        while (flag1 && turn == 1) {
            // Aktywne czekanie (busy waiting) - pętla kręci się w miejscu,
            // dopóki drugi wątek jest w sekcji krytycznej
        }
    }

    public void unlock0() {
        flag0 = false;           // Zwalniam zamek
    }

    // Metody dla Wątku 1 (dekrementującego)
    public void lock1() {
        flag1 = true;            // Zgłaszam chęć wejścia
        turn = 0;                // Ale daję pierwszeństwo pierwszemu
        while (flag0 && turn == 0) {
            // Aktywne czekanie
        }
    }

    public void unlock1() {
        flag1 = false;           // Zwalniam zamek
    }
}

class Counter {
    private int value = 0;

    public void increment() {
        value++;
    }

    public void decrement() {
        value--;
    }

    public int getValue() {
        return value;
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();
        PetersonLock lock = new PetersonLock();

        // Wątek 0
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                lock.lock0();           // Zamykamy nasz autorski zamek
                counter.increment();    // Sekcja krytyczna
                lock.unlock0();         // Otwieramy zamek
            }
        });

        // Wątek 1
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                lock.lock1();           // Zamykamy zamek dla drugiego wątku
                counter.decrement();    // Sekcja krytyczna
                lock.unlock1();         // Otwieramy zamek
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("Końcowa wartość zmiennej: " + counter.getValue());
    }
}
