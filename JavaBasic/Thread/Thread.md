# Thread
### 1. 스레드

- 컴퓨터는 다수의 프로그램들이 동작하고 있음, 우리가 작성해서 실행시킨 프로그램은 그 중 하나일 뿐
- 복수의 프로그램이 동작하므로 CPU는 각각의 프로그램을 번갈아가며 실행
- 이때 OS에서 각 프로그램들의 실행 문맥을 기록해두고 각각을 번갈아가며 CPU에 할당하는데 이것을 스레드라고 함
- 즉 OS에서 스케줄링의 단위가 스레드임

### 2. 병행성과 병렬성
- 일반적으로 병행성이 있는 시스템은 병렬성도 가지고 있어서 두 개념이 혼용되기도 함
- 컴퓨터에서는 병행성에 의해 발생하는 이슈들이 있고 이것이 스레드를 이해하는데 매우 중요

  #### 병행성(Concurrency)
  - 한명이 여러가지 일을 번갈아가며 처리하는 것

  #### 병렬성(Parallelism)
  - 동시에 복수의 일을 처리하는 것

<p align="center">
    <a href="https://techdifferences.com/difference-between-concurrency-and-parallelism.html">
        <img src="difference-between-concurrency-and-parallelism.jpg" width="707" height="384">
    </a>
</p> 

### 3. 공유자원과 임계구역
- 컴퓨터에는 복수의 스레드에 의해 공유되는 자원들이 있음
- 한 스레드가 공유 자원에 접근할 때 이것을 임계구역(Critical Section)이라고 함
- 임계구역에 진입할 때는 각별한 주의가 필요
- 우리가 작성한 자바 파일을 컴파일하면 결과로 생성된 .class 파일에는 훨씬 긴 명령들이 적혀있음
- 이것은 우리가 작성한 라인들이 실제 컴퓨터에서는 훨씬 많은 단계로 실행된다는 의미
- 여러 스레드가 동작하는 환경(스케줄링에 의해 여러 스레드가 병행 실행되는 환경)에서는 개발자의 의도와 다르게 동작할 가능성이 큼

### 4. 상호배제 (Mutual Exclusion)
- 임계구역에는 하나의 스레드만 진입 가능해야 하며 이것을 상호배제라고 함
  ##### Runnable로 상호배제를 하지 않고 구현한 예제
    ```java
    public class ThreadExample {
      private static int count = 0;   // 공유 변수

      public static void main(String[] args) throws InterruptedException {
        List<Thread> pool = new ArrayList<>();

        for (int i = 0; i < 10; i++) {  // 10개의 스레드 생성
          Thread t = new Thread(() -> {
            for (int j = 0; j < 10000; j++) {   // 각 스레드마다 1만번 수행
              count++;
            }
          });

          t.start();
          pool.add(t);
        }

        for (Thread t : pool) {
          t.join();
        }

        System.out.println(count);
      }
    }
    ```
  - count가 100,000이 나와야 하지만 결과는 그렇게 되지 않음
  ##### syncronized 구문을 사용하여 해결 (Monitor)
  - syncronized는 자바에서 native로 지원하는 구문이며 메소드에 적용 가능하고 별도의 블록으로도 실행 가능
  - 해당 구문이 적용되면 한개의 스레드만 진입이 가능
  ```java
  public class SyncronizedExample {
    private static int count = 0;

    public static void main(String[] args) throws InterruptedException {
      List<Thread> pool = new ArrayList<>();

      for (int i = 0; i < 10; i++) {
        Thread t = new Thread(() -> {
          for (int j = 0; j < 10000; j++) {
            synchronized (SyncronizedExample.class) {
              count++;
            }
          }
        });

        t.start();
        pool.add(t);
      }

      for (Thread t : pool) {
        t.join();
      }

      System.out.println(count);
    }
  }
  ```
  ##### lock을 사용하여 해결 (Mutex)
  - Mutex(Lock)을 이용해도 같은 결과를 얻을 수 있음
  ```java
  public class LockExample {
    private static int count = 0;

    public static void main(String[] args) {
      List<Thread> pool = new ArrayList<>();
      Lock lock = new ReentrantLock();

      for (int i = 0; i < 10; i++) {
        Thread t = new Thread(() -> {
          for (int j = 0; j < 10000; j++) {
            lock.lock();
            try {
              count++;
            } finally {
              lock.unlock();
            }
          }
        });
            
        t.start();
        pool.add(t);
      }

      for (Thread t : pool) {
        t.join();
      }

      System.out.println(count);
    }
  }
  ```
  ##### Syncronized와 Lock의 차이
  - Syncronized는 Native로 지원되는 기능이고 Lock은 라이브러리로 구현되어 있어 Syncronized가 속도 측면에서 유리하나 그 차이는 크지 않음
  - Syncronized는 무한 대기 같은 상황이 발생 할 수 있으나 Lock은 tryLcok() 메소드를 통해 일정 시간 동안만 대기하거나 하는 등의 유연한 사용이 가능
  - 상황에 맞게 두가지 방식 중 알맞은 방식으로 선택하는 것이 좋음