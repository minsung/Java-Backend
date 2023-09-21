# Thread #2
### 1. ReadWriteLock
- 임계구간에 접근시 모니터나 뮤텍스를 사용하는 이유는 해당 공유 자원이 변경되었을 수 있기 때문
- 변경이 되지 않는다면 병행성 이슈는 발생하지 않음
- 따라서 어떤 스레드가 읽기만 수행하고 다른 스레드가 변경을 한다면 읽기만 하는 스레드는 복수의 스레드가 공유 자원에 접근해도 이슈는 발생하지 않음
- 단, 쓰기 스레드에 의해 값이 변경될 수 있으므로 별도의 대책은 필요
- 자바에서는 readLock, writeLock을 이용해 이를 구현할 수 있다
- _**읽기만 하는 스레드는 readLock을 사용해서 공유 변수에 접근하고 readLock은 배타적 락이 아니므로 복수의 스레드가 공유 자원에 접근이 가능**_
- _**쓰기 스레드는 writeLock을 사용하여 임계 구간에 진입하는데 이때 스레드는 readLock을 사용하는 스레드들이 임계 구간을 빠져나갈 때까지 대기한 후 배타적 락인 writeLock을 사용함**_
- writeLock이 걸린 시점에는 readLock을 건 스레드들도 writeLock이 종료될 때까지 대기한다
  > - 예를 들어 100개의 스레드 동작이 필요하고 값을 바꾸는 스레드는 2개, 나머지 98개의 스레드는 읽기만 한다고 가정하고 syncronized나 lock을 사용하게 되면 퍼포먼스가 아주 떨어지게 됨
  > - 그 이유는 대다수의 스레드가 대기 상태에 빠지게 되므로
  > - 여기서 readLock, writeLock을 사용한다면 98개의 스레드가 중복해서 동작할 수 있기 때문에 퍼포먼스가 좋아짐
  > - 데이터베이스가 실제로 이 방법을 굉장히 많이 사용

  ```java
  public class ReadWriteLockSample {
      private static final ReadWriteLock lock = new ReentrantReadWriteLock();
      private static int count = 0;

      public static void main(String[] args) throws InterruptedException {
          Thread t1 = new Thread(new WriteThread());
          Thread t2 = new Thread(new ReadThread());
          Thread t3 = new Thread(new ReadThread());
          Thread t4 = new Thread(new ReadThread());
          Thread t5 = new Thread(new ReadThread());
          Thread t6 = new Thread(new ReadThread());

          t1.start();
          t2.start();
          t3.start();
          t4.start();
          t5.start();
          t6.start();

          t1.join();
          t2.join();
          t3.join();
          t3.join();
          t4.join();
          t5.join();
          t6.join();
      }

      static class WriteThread implements Runnable {
          @Override
          public void run() {
              while (count < 10000) {
                  try {
                      System.out.println("write lock");
                      lock.writeLock().lock();
                      count++;
                      Thread.sleep(3000L);
                  } catch (InterruptedException e) {
                      throw new RuntimeException(e);
                  } finally {
                      lock.writeLock().unlock();
                      System.out.println("write unlock");
                  }
              }
          }
      }

      static class ReadThread implements Runnable {
          @Override
          public void run() {
              while (count < 10000) {
                  try {
                      System.out.println("read lock");
                      lock.readLock().lock();
                      System.out.println(count);
                  } finally {
                      lock.readLock().unlock();
                      System.out.println("read unlock");
                  }
              }
          }
      }
  }
  ```
### 2. Concurrent Collection
- java.util.concurrent 패키지 내에 Thread safe를 제공하는 컬렉션들이 준비되어 있음
- 별도의 lock 필요없이 기존 방식대로 사용하면 됨
  > - 실제로 개발을 할 때 공유되는 자원은 int와 같은 기본타입의 값이 아니라 컬렉션과 같은 비교적 큰 규모의 타입들을 사용하게 됨
  > - 예를 들어 택시를 부른다고 하면 택시를 호출하는 오더가 들어가게 되고 그 오더를 여러 명의 스레드에서 공유할 수 있도록 map을 생성하여 안에 저장을 해두어 공유 자원으로 접근
  > - 이 map이 공유 자원이니 상호 배제를 지켜가면서 작업을 해야 하는데 이것을 자동으로 해주는 것이 `ConcurrentHashMap`
- Concurrent List는 존재하지 않음 (아래의 이유 때문에 ConcurrentHashMap 처럼 굳이 자동으로 적용되는 걸 만들지 않았음)
- 리스트들의 동작은 대부분 단순한 편이라 스레드에 대해 안전하게 작동하려면 사실상 모든 동작을 할 때마다 lock이나 syncrinized를 걸어줘야 함
- 직접 syncronized를 사용하는 것보다 ConcurrentHashMap 같은 것을 쓰는게 성능이 훨씬 좋음
- 이유는 map 안의 key마다 lock이 별도로 존재.
- 만약 10개의 요소가 있다고 가정하면 락도 10개가 존재하는 것임. 특정한 하나의 요소에 접근한다고 하면 그 요소에 대해서만 락이 작동
- 나머지 요소들은 락이 걸려있지 않은 상태기 때문에 동시성 문제가 발생하지 않음
- 따라서 성능이 상당히 좋고 실제 데이터베이스도 이런 형태로 구현되어 있음
- 하지만 단점도 존재하는데 map을 순회(Iteration)할때는 동시성을 보장하지 않음
- 위의 예처럼 10개의 요소를 순회한다고 하면 락을 총 10번을 걸어야 하는데 0~9까지 돌면서 0번 락을 잡았다가 풀고 1번으로 락을 걸고 하는 방식에서 0번은 락이 풀린 이후 변경될 가능성이 존재함 
- 이런 케이스가 흔하지 않지만 꼭 순회를 해야 한다면 syncronized 처리를 해줘야 함
- 동시성 이슈가 발생하면 데이터가 정확하지 않기 때문에 정확도가 크게 필요없는 경우 그냥 순회해도 상관없음
  > 예를 들어 택시 오더가 잘 들어가는 것과 잘 빠져나오는지만이 관심사인 경우 오더가 정확하게 몇개 있고 얼마나 됐는지 정확하게 알 필요는 없음

### 3. Atomic
- 병행성 이슈가 발생하는 근본적인 이유는 프로그램이 컴파일되어 실행되면 CPU에서 여러 명령으로 나뉘어 실행되기 때문
- CPU에서는 CAS(Compare And Set, Compare And Swap)이라는 한 줄로 처리되는 명령이 있고 이 명령을 사용하면 lock 없이도 병행성 이슈를 피할 수 있음
  - 자바 코드를 컴파일해서 생성된 .class를 보면 자바코드는 한줄이어도 여러줄로 나뉘어서 실행되는 것을 확인할 수 있는데 CAS는 그 한줄로 실행되기 때문에 속도가 빠름
- 어떤 변수가 파라미터1과 같으면 파라미터2로 변경하라는 명령
- 자바에서는 AtomicInteger, AtomicLong과 같은 클래스를 제공
  ```java
  public class Atomic {
    public static void main(String[] args) throws InterruptedException {
        AtomicLong counter = new AtomicLong(0);
        Runnable runnable = () -> {
            for (int i = 0; i < 10000; i++) {
                counter.incrementAndGet();
            }
        };

        Thread t1 = new Thread(runnable);
        Thread t2 = new Thread(runnable);
        Thread t3 = new Thread(runnable);

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        System.out.println("counter: " + counter.get());
    }
  }
  ```
- syncronized나 lock을 거는 것과 같은 방식으로 안해도 상호 배제를 내부적으로 구현해놨기 때문에 직접 할 필요 없음
- 하지만 Atomic은 내부적으로 lock이 아니고 CAS를 사용함
- 한줄로 된 CPU 명령어기 때문에 동시성 이슈가 발생하지 않음
- 하지만 CPU 명령어기 때문에 복잡한 동작은 할 수 없음
  > - 예를 들어 카운터 변수에 `compareAndSet(0, 1)`을 사용한다면 값이 0이면 1을 증가 시키는데 10개의 스레드가 동시에 실행한다고 가정하면 1개의 스레드만 이 명령을 성공하게 되고 나머지는 값이 1로 바뀌었기 때문에 실패하게 됨
  > - 그리고 Atomic은 명령이 성공할 때까지 실행하게 되어 있음
  > - 처음에 9개가 실패하고 다음번에 한개의 스레드만 성공할테니 8개는 실패하는 순서로 실행됨
  
  > - 페이지 조회 수 같은 것을 분석하기 위해 별도로 DB에 데이터를 저장할 수도 있겠지만 그렇게까지 중요하지 않다면 이것을 AtomicLong으로 선언해두고 카운터를 증가시키는 방식으로도 사용됨
  > - 이것을 통계 데이터를 구하고자 lock을 걸기 시작하면 퍼포먼스가 급격하게 떨어지는 문제가 발생함
  > - NoSQL은 보통 트랜잭션을 잘 지원하지 않는데 CAS는 지원하는 경우가 있음
  > - 10개의 스레드가 DB의 어떤 값을 동시에 변경하려고 할때 DB는 CAS를 이용해서 한명만 성공하는 것을 보장시켜 줌

### 4. Future
- 비동기 작업을 제공하는 인터페이스로 몇가지 구현체들이 있음
- Java 5부터 제공되었으나 몇가지 한계점으로 인해 Java 8에서 CompletableFuture라는 개선된 구현체가 등장
  ```java
  public class Future {
    public static void main(String[] args) throws InterruptedException {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("Future start");

            int sum = 0;
            for (int i = 0; i < 10000; i++) {
                sum += i;
            }

            System.out.println("Future end");

            return sum;
        });

        System.out.println("Main start");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        int result = future.join();

        System.out.println("Main end: " + result);
    }
  }
  ```
  > - 메인 스레드가 퓨처를 생성하는 시점 10000까지 덧셈을 실행하고 메인 스레드는 다음 코드를 계속 실행하게 되어 병렬 처리가 가능해지며 퍼포먼스가 향상됨
  > - 동기화가 필요한 시점에 join()을 사용하여 처리
  > - 외부에 있는 서비스를 호출한다던지 DB에서 어떤 값을 불러온다던지 하는 I/O가 발생하는 작업들은 스레드를 오랫동안 블록시키기 때문에 그것을 별도의 스레드로 퓨쳐로 작성해서 호출하도록 처리하고 그 동안 메인 스레드는 다른 작업을 수행하면서 결과가 필요한 시점에 join
  > - 이것을 활용할 수 있는 경우 적극적으로 써주는 것이 좋음

### 5. ExecutorService
- Thread 실행시 start, join 등의 절차를 직접 수행할 필요 없이 대신 스레드 라이프사이클을 관리해 줌
- 총 4가지 종류가 있음
  - newFixedThreadPool(int) : 주어진 값 만큼의 고정된 크기의 스레드 풀
    - 작업이 없어도 스레드가 종료되지 않음
  - newCachedThreadPool() : 작업이 주어질 때 필요한 수 만큼의 스레드를 생성하는 스레드 풀
    - 작업이 없으면 스레드가 종료 됨
    - 작업이 많아지면 스레드가 더 생성 됨
    - 스레드가 생성될 때 많은 자원을 소모하기 때문에 자주 만들어야 하는 상황에서는 권장하지 않음
    - 하지만 자원이 너무 한정적이어서 newFixedThreadPool을 사용해서 자원을 낭비하는게 싫다면 이것을 선택할 수도 있음
  - newScheduledThreadPool(int) : 주어진 값 만큼 주기적으로 실행되는 스레드 풀
  - newSingleThreadExecutor() : 1개의 스레드로 만들어진 스레드 풀
  ```java
  public class ExecutorServiceSample {
    private static int count = 0;

    public static void main(String[] args) throws InterruptedException {
        java.util.concurrent.ExecutorService es = Executors.newFixedThreadPool(3);

        for (int i = 0; i < 10; i++) {
            es.submit(() -> {
                Thread t = Thread.currentThread();

                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                System.out.println(t.getId() + ":" + t.getName());
            });

            // 더 이상 ExecutorService에 Task를 추가할 수 없음
            // 모든 스레드를 완료할 때까지 대기 후 종료
            es.shutdown();

            // Timeout을 설정하고 완료되기를 기다림
            // 주어진 시간 내에 완료시 true를, 완료되지 않으면 false
            if (es.awaitTermination(10, TimeUnit.SECONDS)) {

            } else {
                es.shutdown();
            }
        }
    }
  }
  ```