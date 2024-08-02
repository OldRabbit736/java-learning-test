package concurrency;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.*;

/**
 * CountDownLatch를 이용해 스레드끼리 서로 협력할 수 있다.
 * 카운트 다운을 하는 스레드가 카운트를 0으로 만들면,
 * 카운트 다운을 기다리던 스레드가 작업을 시작할 수 있다.
 */
public class CountDownLatchTest {

    @Test
    void simple_onoff() throws InterruptedException {
        // given
        CountDownLatch latch = new CountDownLatch(1);

        // when
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
            }
        }).start();

        // then - 다른 스레드가 래치 카운트다운 하기 전이므로 래치 카운트는 1
        assertThat(latch.getCount()).isEqualTo(1);

        // then2 - 래치가 0이 되길 기다린 후 카운트하므로 래치 카운트는 0
        latch.await();
        assertThat(latch.getCount()).isEqualTo(0);
    }

    @Test
    void example_in_api_document() throws InterruptedException {
        int doneCount = 10;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(doneCount);

        for (int i = 0; i < doneCount; i++) {
            new Thread(new Worker(startSignal, doneSignal)).start();
        }

        System.out.println("worker 아직 시작하지마");
        assertThat(startSignal.getCount()).isEqualTo(1);
        assertThat(doneSignal.getCount()).isEqualTo(doneCount);
        Thread.sleep(1000);

        System.out.println("worker 일 시작!");
        startSignal.countDown();
        assertThat(startSignal.getCount()).isEqualTo(0);
        assertThat(doneSignal.getCount()).isEqualTo(doneCount);

        System.out.println("worker들이 일을 끝내길 기다림");
        doneSignal.await();
        System.out.println("오늘 하루 일과 끝. 다 집에 가자.");
        assertThat(startSignal.getCount()).isEqualTo(0);
        assertThat(doneSignal.getCount()).isEqualTo(0);
    }

    static class Worker implements Runnable {
        private final CountDownLatch startSignal;
        private final CountDownLatch doneSignal;

        public Worker(CountDownLatch startSignal, CountDownLatch doneSignal) {
            this.startSignal = startSignal;
            this.doneSignal = doneSignal;
        }

        @Override
        public void run() {
            try {
                // worker 스레드들은 start 신호가 떨어지길 기다림
                startSignal.await();
                doWork();
                // worker는 일을 끝내고 카운트다운을 함으로써 일의 진척도를 알릴 수 있다.
                doneSignal.countDown();
            } catch (InterruptedException exception) {
            }
        }

        void doWork() throws InterruptedException {
            Thread.sleep(1000);
        }
    }

}
