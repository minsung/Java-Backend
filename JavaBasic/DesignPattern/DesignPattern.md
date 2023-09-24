# Design Pattern

### 1. 디자인 패턴?
- 객체지향 프로그래밍 (OOP)에서 설계의 원칙이 여러가지가 있음 (ex. SOLID...)
- OOP의 관점에서 잘 만들어진 일종의 코딩 템플릿이 디자인 패턴

### 2. Singleton
- 오직 하나의 인스턴스만 생성하도록 강제하는 방법
  ```java
  class Singleton {
      private static final Singleton instance = new Singleton();

      private Singleton() {

      }

      public Singleton getInstance() {
          return instance;
      }
  }
  ```
  - `private`으로 생성자를 선언해주어야 함
  - `private static`으로 인스턴스를 하나 만들어주어야 함


- 일반적으로는 위와같이 사용해도 크게 문제 없지만 리플렉션을 사용하여 실행 시점에 코드를 조작하여 private 접근 제어를 public으로 변경하는 등의 조작을 통해 싱글턴으로 동작을 안하게 만들수가 있음
  - Reflection?
    - 프로그램의 메타 데이터를 조작해서 코드를 런타임에서 변경시킬 수 있는 방법
    - 스프링도 실제 리플렉션을 이용해서 만든 것
- 실제로 해당 싱글턴을 사용하지 않을수도 있는데 위의 코드는 클래스가 로딩되는 상황에서 객체가 생성이 되므로 메모리가 낭비될 수 있어 실제로는 아래와 같은 방법이 조금 더 좋은 방법
  ```java
  class Singleton {
    private static Singleton instance;

    private Singleton() {

    }

    public Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }

        return instance;
    }
  }
  ```
- 하지만 이렇게 구현하게 되면 멀티 스레드 환경에서 동시에 접근한다고 가정했을 때 인스턴스가 여러개 생성될 수 있으므로 syncronized를 사용해야 함
  ```java
  class Singleton {
    private static Singleton instance;

    private Singleton() {

    }

    public Singleton getInstance() {
        synchronized (Singleton.class) {
            if (instance == null) {
                instance = new Singleton();
            }
        }
        
        return instance;
    }
  }
  ```
- 하지만 syncronized를 이렇게 사용하게 되면 인스턴스를 호출할 때마다 락이 걸리게 되므로 성능이 좋지 않게 됨
  ```java
  class Singleton {
    private static Singleton instance;

    private Singleton() {

    }

    public Singleton getInstance() {
        if (instance == null) {
            synchronized (SingletonExample.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }

        return instance;
    }
  }
  ```
- 이와 같이 구현하면 성능 최적화와 스레드 안정성을 보장할 수 있는데 그 이유는
  - 첫번째 null 체크에서는 null이 아닌 경우 동기화 블록에 진입하지 않고 빠르게 리턴할 수 있으므로 성능 최적화
  - 두번째 null 체크는 동기화 된 블록 내에서 여러 스레드가 이 조건을 확인 하더라도 하나의 스레드만이 인스턴스를 생성하고 나머지는 대기하게 되어 중복 생성을 방지
- 아래와 같은 시나리오를 생각해볼 수 있음
    > 1. 스레드 A와 스레드 B가 동시에 getInstance 메서드에 접근
    > 2. 스레드 A가 먼저 if (instance == null)을 확인하고 인스턴스가 null임을 인식
    > 3. 스레드 A가 synchronized 블록으로 진입하고 인스턴스를 생성
    > 4. 그런 동안 스레드 B는 if (instance == null)에서 대기
    > 5. 스레드 A가 인스턴스를 생성한 후 instance에 할당
    > 6. 스레드 A가 동기화 블록을 빠져나가고, 스레드 B가 동기화 블록으로 진입
    > 7. 스레드 B는 다시 if (instance == null)을 확인하게 되지만, 이제 instance는 null이 아니므로 인스턴스를 중복 생성하지 않고 기존 인스턴스를 반환

- 위의 방법보다 더 좋은 방법은 `enum`을 이용하는 방법이 있음
  ```java
  public enum Singleton {
      INSTANCE;
      int value;

      public int getValue() {
          return value;
      }

      public void setValue(int value) {
          this.value = value;
      }
  }
  ```
### 2. Factory method
- 인스턴스를 만든다는 행위는 매우 중요하므로 이것을 별도의 클래스로 분리하여 생성만 담당하는 역할을 부여한 형태
- 많은 곳에서 new 키워드를 이용해서 객체를 생성하는 것보다 한 곳에서 생성한다면 관리가 용이해질 수 있음 
    ```java
    class Pizza {
    
    }
    
    class Factory {
        public Pizza createPizza() {
            return new Pizza();
        }
    }
    ```
### 3. Strategy
- 어떠한 행위, 전략 등을 별도의 클래스나 인터페이스로 추출한 형태
- 행위, 전략의 변화에 다른 클래스들은 영향을 받지 않도록 할 수 있음
- 행동(behavior)을 캡슐화하고 해당 행동을 교환하여 동적으로 객체의 행동을 변경할 수 있는 디자인 패턴
1. 전략 인터페이스 (Strategy Interface)
    ```java
    public interface PaymentStrategy {
        void pay(int amount);
    }
    ```
2. 구체적인 전략 클래스들 (Concrete Strategy Classes)
    ```java
    public class CreditCardPayment implements PaymentStrategy {
        private String cardNumber;
        private String name;
    
        public CreditCardPayment(String cardNumber, String name) {
            this.cardNumber = cardNumber;
            this.name = name;
        }
    
        @Override
        public void pay(int amount) {
            System.out.println(amount + "원을 신용카드로 결제했습니다. 카드 번호: " + cardNumber);
        }
    }
    
    public class PayPalPayment implements PaymentStrategy {
        private String email;
    
        public PayPalPayment(String email) {
            this.email = email;
        }
    
        @Override
        public void pay(int amount) {
            System.out.println(amount + "원을 PayPal로 결제했습니다. 이메일: " + email);
        }
    } 
    ```
3. 컨텍스트 클래스 (Context Class)
    ```java
    public class ShoppingCart {
        private PaymentStrategy paymentStrategy;
    
        public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
            this.paymentStrategy = paymentStrategy;
        }
    
        public void checkout(int amount) {
            paymentStrategy.pay(amount);
        }
    }
    ```
4. 메인 클래스 (Main Class)
    ```java
    public class Main {
        public static void main(String[] args) {
            ShoppingCart cart = new ShoppingCart();
    
            // 신용카드로 결제
            PaymentStrategy creditCardPayment = new CreditCardPayment("1234-5678-9012-3456", "John Doe");
            cart.setPaymentStrategy(creditCardPayment);
            cart.checkout(100);
    
            // PayPal로 결제
            PaymentStrategy payPalPayment = new PayPalPayment("john.doe@example.com");
            cart.setPaymentStrategy(payPalPayment);
            cart.checkout(50);
        }
    }
    ```
> - 위의 예제에서는 스트레티지 패턴을 사용하여 ShoppingCart 클래스의 결제 방법을 동적으로 변경할 수 있음
> - PaymentStrategy 인터페이스를 구현하는 여러 구체적인 전략 클래스를 만들고, 이를 ShoppingCart에서 설정하여 사용
> - 이렇게 하면 결제 방법을 손쉽게 변경할 수 있으며, 코드의 유연성을 높일 수 있음

### 4. Visitor
- 어떠한 일련의 과정을 컬렉션으로 만든 후 순차적으로 하나씩 접근하는 패턴
- 프로세스나 절차를 표현하기 적합

1. Visitor 인터페이스 (Cook)
    ```java
    interface Cook {
        void visit(Dough dough);
        void visit(Sauce sauce);
        void visit(Topping topping);
    }
    ```
2. ConcreteVisitor 클래스 (PizzaChef)
    ```java
    class PizzaChef implements Cook {
        @Override
        public void visit(Dough dough) {
            System.out.println("피자 도우를 만듭니다.");
        }
    
        @Override
        public void visit(Sauce sauce) {
            System.out.println("피자 소스를 추가합니다.");
        }
    
        @Override
        public void visit(Topping topping) {
            System.out.println(topping.getName() + "를 추가합니다.");
        }
    }
    ```
3. Element 인터페이스 (PizzaComponent)
    ```java
    interface PizzaComponent {
        void accept(Cook cook);
    }
    ```
4. ConcreteElement 클래스 (Dough, Sauce, Topping)
    ```java
    class Dough implements PizzaComponent {
        @Override
        public void accept(Cook cook) {
            cook.visit(this);
        }
    }
    
    class Sauce implements PizzaComponent {
        @Override
        public void accept(Cook cook) {
            cook.visit(this);
        }
    }
    
    class Topping implements PizzaComponent {
        private String name;
    
        public Topping(String name) {
            this.name = name;
        }
    
        public String getName() {
            return name;
        }
    
        @Override
        public void accept(Cook cook) {
            cook.visit(this);
        }
    }
    ```
5. ObjectStructure 클래스 (Pizza)
    ```java
    class Pizza {
        private List<PizzaComponent> components = new ArrayList<>();
    
        public void addComponent(PizzaComponent component) {
            components.add(component);
        }
    
        public void cookPizza(Cook cook) {
            for (PizzaComponent component : components) {
                component.accept(cook);
            }
        }
    }
    ```
6. 클라이언트 코드 (Main)
    ```java
    public class Main {
        public static void main(String[] args) {
            Pizza pizza = new Pizza();
            pizza.addComponent(new Dough());
            pizza.addComponent(new Sauce());
            pizza.addComponent(new Topping("치즈"));
            pizza.addComponent(new Topping("페퍼로니"));
    
            Cook chef = new PizzaChef();
            pizza.cookPizza(chef);
        }
    }
    ```
> - 위의 예제에서는 Pizza 객체의 다양한 구성 요소인 Dough, Sauce, Topping 등이 accept 메서드를 통해 Cook (여기서는 PizzaChef)를 방문하며, 각각의 과정을 처리
> - PizzaChef 클래스에서는 방문자가 어떤 작업을 수행할지 정의되어 있음
> - Visitor 패턴을 사용하면 피자 만드는 과정을 나타내는 객체 구조와 해당 과정을 처리하는 방문자를 분리하여 새로운 피자 종류나 추가적인 과정을 쉽게 확장하거나 수정할 수 있음

### 5. Builder
- 디자인 패턴으로 보기엔 애매하지만 자주 사용되는 코딩 방식
- 객체를 생성하는데 필요한 많은 매개변수가 있거나 초기화 순서가 복잡한 경우에 유용
- 여러 옵션들이 있는 생성자를 대체하고 코드를 깔끔하게 작성 할 수 있음
```java
class Car {
    private String brand;
    private String model;
    private int year;
    private String color;

    public Car(String brand, String model, int year, String color) {
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.color = color;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getYear() {
        return year;
    }

    public String getColor() {
        return color;
    }

    public static class Builder {
        private String brand;
        private String model;
        private int year;
        private String color;

        Builder setBrand(String brand) {
            this.brand = brand;
            return this;
        }

        Builder setModel(String model) {
            this.model = model;
            return this;
        }

        Builder setYear(int year) {
            this.year = year;
            return this;
        }

        Builder setColor(String color) {
            this.color = color;
            return this;
        }

        Car build() {
            return new Car(brand, model, year, color);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Car car = new Car.Builder()
                .setBrand("Toyota")
                .setModel("Camry")
                .setYear(2022)
                .setColor("Blue")
                .build();

        System.out.println("Car: " + car.getBrand() + " " + car.getModel() + " " + car.getYear() + " " + car.getColor());
    }
}
```