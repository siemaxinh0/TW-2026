package zadanie1_2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        int runs = 100;
        List<Integer> results = new ArrayList<>();

        for (int r = 0; r < runs; r++) {
            Counter counter = new Counter();

            Thread t1 = new Thread(() -> {
                for (int i = 0; i < 10000; i++) counter.increment();
            });

            Thread t2 = new Thread(() -> {
                for (int i = 0; i < 10000; i++) counter.decrement();
            });

            t1.start();
            t2.start();

            t1.join();
            t2.join();

            results.add(counter.getValue());
        }

        printConsoleHistogram(results, 10);
    }

    private static void printConsoleHistogram(List<Integer> data, int numBins) {
        int min = Collections.min(data);
        int max = Collections.max(data);

        if (min == max) {
            System.out.println("Wszystkie wykonania zwróciły wartość: " + min);
            return;
        }

        double binSize = (double) (max - min) / numBins;
        int[] bins = new int[numBins];

        for (int value : data) {
            int binIndex = (int) ((value - min) / binSize);
            if (binIndex >= numBins) {
                binIndex = numBins - 1;
            }
            bins[binIndex]++;
        }

        System.out.println("Histogram końcowych wartości zmiennej Counter (100 wykonań):\n");
        for (int i = 0; i < numBins; i++) {
            int rangeStart = (int) Math.round(min + i * binSize);
            int rangeEnd = (int) Math.round(min + (i + 1) * binSize);

            System.out.printf("[%6d, %6d) | ", rangeStart, rangeEnd);
            for (int j = 0; j < bins[i]; j++) {
                System.out.print("*");
            }
            System.out.println(" (" + bins[i] + ")");
        }
    }
}
