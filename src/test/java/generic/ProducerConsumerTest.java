package generic;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class ProducerConsumerTest {

    @Test
    void producer() {
        // given
        MyList<Number> numberMyList = new MyList<>();
        Iterable<Integer> integers = List.of(1, 2, 3);

        // when
        numberMyList.pushAllFlexible(integers);

        // then
        assertThat(numberMyList.getList()).isEqualTo(List.of(1, 2, 3));
    }

    @Test
    void consumer() {
        // given
        MyList<Number> numberMyList = new MyList<>();
        List<Integer> integers = List.of(1, 2, 3);
        numberMyList.pushAllFlexible(integers);

        // when
        Collection<Object> objects = new ArrayList<>();
        numberMyList.popAllFlexible(objects);

        // then
        assertThat(objects).isEqualTo(List.of(1, 2, 3));
    }

    static class MyList<E> {
        private final List<E> list = new ArrayList<>();

        // 이런 메소드는 정의되는데는 문제 없지만 producer 테스트에서 컴파일 에러를 발생시킨다.
        // Iterable<E> src 타입으로 Iterable<Integer> 타입이 변환될 수 없기 때문이다.
        // 다른 말로, src에 integers의 주소를 담을 수 없다.
        // 왜냐하면 제네릭은 기본적으로 불공변이기 때문이다. Number를 확장한게 Integer라 할지라도,
        // List<Integer>는 List<Number>를 확장한 것이 아니다.
        // 이펙티브 자바 아이템 31. 한정적 와일드카드... 참조하자.
        public void pushAllAllowIterableOfEOnly(Iterable<E> src) {
            for (E e : src) {
                list.add(e);
            }
        }

        // src 입력 매개변수는 생산자다. 즉 값을 제공하는 역할을 한다.
        // 그리고 그 값을 E 타입의 저장소에 저장할 것이다.
        // 그러므로 E 뿐만 아니라 E를 확장한 타입을 제공할 수 있다.
        // 이 경우 내부 타입 매개변수 E를 extends 하는 타입을 제공해야 한다.
        public void pushAllFlexible(Iterable<? extends E> src) {
            for (E e : src) {
                list.add(e);
            }
        }

        // 이것도 마찬가지로 consumer 테스트에서 컴파일 에러를 일으킨다.
        // 물론 E 타입의 컬렉션을 인수로 받는 것은 문제가 없다.
        // 그러나 E의 super 타입을 다루는 컬렉션을 인수로서 다룰 수 없다.
        // 예를들어 MyList<Number> 인스턴스의 popAllAllowCollectionOfEOnly 메소드에
        // Collection<Object> 인스턴스를 인수로서 전달할 수 없다.
        // 즉, Collection<Number> dst 에 Collection<Object> 를 할당할 수 없다.
        // 왜냐하면 제네릭은 기본적으로 불공변이기 때문이다.
        public void popAllAllowCollectionOfEOnly(Collection<E> dst) {
            dst.addAll(list);
        }

        // dst 입력 매개변수의 타입은 소비자다. 즉 값을 소비하는 역할을 한다.
        // E 타입의 내부 값을 전달받는 역할인 것이다.
        // 그러므로 입력 매개변수는 E 뿐만 아니라 E의 super 타입을 다룰 줄 알면 E 타입의 값을
        // 전달받을 수 있다.
        // 이 경우 타입 매개변수 E를 super 하는 타입을 매개변수로 둘 수 있다.
        public void popAllFlexible(Collection<? super E> dst) {
            dst.addAll(list);
        }

        public List<E> getList() {
            return this.list;
        }
    }
}
