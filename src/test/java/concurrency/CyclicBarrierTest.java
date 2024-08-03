package concurrency;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;


public class CyclicBarrierTest {

    @Test
    void test() throws InterruptedException {
        // given
        List<Integer> workDoneList = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger sum = new AtomicInteger(0);
        int parties = 3;
        CyclicBarrier barrier = new CyclicBarrier(parties, () -> {
            int total = 0;
            for (int i : workDoneList) {
                total += i;
            }
            sum.compareAndSet(0, total);
        });
        // 현재 barrier 닿은 스레드 없음.
        assertThat(workDoneList.size()).isEqualTo(0);
        assertThat(sum.get()).isEqualTo(0);
        assertThat(barrier.getNumberWaiting()).isEqualTo(0);

        // when
        for (int i = 0; i < parties; i++) {
            new Thread(() -> {
                try {
                    workDoneList.add(new Random().nextInt(100));
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
        Thread.sleep(500);

        // then - 모든 스레드가 barrier에 닿음. barrier action도 끝남.
        assertThat(workDoneList.size()).isEqualTo(parties);
        int expectedSum = workDoneList.stream().reduce(0, Integer::sum);
        assertThat(expectedSum).isEqualTo(sum.get());
    }


}
