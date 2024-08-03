package concurrency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

// https://www.baeldung.com/java-cyclic-barrier
public class CyclicBarrierDemo {
    private CyclicBarrier cyclicBarrier;
    private List<List<Integer>> partialResults
            = Collections.synchronizedList(new ArrayList<>());
    private Random random = new Random();
    private int NUM_PARTIAL_RESULTS;
    private int NUM_WORKERS;

    // workter 스레드가 할 일 정의
    // 일을 마치면 CyclicBarrier.await을 호출하여 신호를 준다.
    class NumberCruncherThread implements Runnable {
        @Override
        public void run() {
            String thisThreadName = Thread.currentThread().getName();
            List<Integer> partialResult = new ArrayList<>();

            for (int i = 0; i < NUM_PARTIAL_RESULTS; i++) {
                Integer num = random.nextInt(10);
                System.out.println(thisThreadName + ": Crunching some numbers! Final result - " + num);
                partialResult.add(num);
            }

            partialResults.add(partialResult);
            try {
                System.out.println(thisThreadName + " waiting for others to reach barrier.");
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    // 옵셔널 작업을 정의. (Barrier Action이라고 불린다.)
    // worker 스레드의 Cyclic.await 호출 갯수가 특정 갯수만큼 다다르면, (tripping the barrier)
    // 옵셔널한 작업이 시작된다. 마지막에 배리어를 넘어뜨린 스레드가 이 작업을 수행한다.
    class AggregatorThread implements Runnable {
        @Override
        public void run() {
            String thisThreadName = Thread.currentThread().getName();

            System.out.println(thisThreadName + ": Computing sum of " +
                    NUM_WORKERS + " workers, having " +
                    NUM_PARTIAL_RESULTS + " results each.");
            int sum = 0;

            for (List<Integer> threadResult : partialResults) {
                System.out.print("Adding ");
                for (Integer partialResult : threadResult) {
                    System.out.print(partialResult + " ");
                    sum += partialResult;
                }
                System.out.println();
            }
            System.out.println(thisThreadName + ": Final result = " + sum);
        }
    }

    public void runSimulation(int numWorkers, int numberOfPartialResults) {
        NUM_PARTIAL_RESULTS = numberOfPartialResults;
        NUM_WORKERS = numWorkers;

        cyclicBarrier = new CyclicBarrier(NUM_WORKERS, new AggregatorThread());

        System.out.println("Spawning " + NUM_WORKERS
                + " worker threads to compute "
                + NUM_PARTIAL_RESULTS + " partial results each");

        for (int i = 0; i < NUM_WORKERS; i++) {
            Thread worker = new Thread(new NumberCruncherThread());
            worker.setName("Thread " + i);
            worker.start();
        }
    }

    public static void main(String[] args) {
        CyclicBarrierDemo demo = new CyclicBarrierDemo();
        demo.runSimulation(5, 3);
    }
}
