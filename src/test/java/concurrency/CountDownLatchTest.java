package concurrency;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

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
        Assertions.assertThat(latch.getCount()).isEqualTo(1);

        // then2 - 래치가 0이 되길 기다린 후 카운트하므로 래치 카운트는 0
        latch.await();
        Assertions.assertThat(latch.getCount()).isEqualTo(0);
    }
}
