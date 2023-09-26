# File I/O

### 1. File I/O?
- 유닉스/리눅스 계열의 OS는 입출력을 모두 file로 처리
  - 키보드, 마우스, 모니터, 프린터 심지어 메모, 네트워크 소켓도 포함
- 따라서 유닉스 계열 OS의 I/O란 파일을 읽고 쓰는 것이고 대부분의 I/O 인터페이스도 파일과 동일함 (완전히 같지는 않음)
- 자바도 유닉스 계열과 비슷하게 I/O 장치들에 대해서 비슷한 인터페이스를 제공

### 2. Linux file system
- 파일의 소유권과 권한
  - 파일에는 읽기/쓰기/실행의 3가지 권한이 부여됨
  - 리눅스의 경우 ls -al 명령으로 확인이 가능하며 파일 소유 여부에 따라 아래와 같이 2가지 방법으로 표기
    - rwxrwxrwx (r: 읽기, w: 쓰기, x: 실행), 앞 3글자는 소유자, 중간 3글자는 그룹, 마지막 3글자는 이외의 유저
    - 숫자로도 표현 가능 (ex. 777 -> 4: 읽기, 2: 쓰기, 1: 실행)

### 3. java.io.file
- 자바에서 File I/O를 위해 필요한 기본적인 패키지는 `java.io.file`이며 1.7부터 제공되는 (nio2) `java.nio.file.Files`도 있음
- 기존 `java.io.file`은 심볼릭 링크와 같은 유닉스의 파일 특성을 제대로 지원하지 못하는 경우가 있어 nio가 추가됨

### 4. Binary I/O - Stream
- 스트림이란 `연속된 데이터의 흐름`이라는 의미로 어떤 데이터가 연속적으로 끊김없이 입력되거나 출력되는 처리 방식을 말함
- 스트림의 특성은 데이터들을 한번씩만 처리 가능하며 뒤로 가거나 앞으로 이동은 불가능함
- 자바에서는 `InputStream`, `OutputStream` 두가의 입출력을 담당하는 스트림이 있음
- 둘은 `Closable`을 구현한 추상클래스이고 반드시 상속을 통해 구현해야 함
- InputStream과 OutputStream은 모두 `Byte`를 처리하기 위한 방법임
- `FileInputStream`, `ObjectInputStream`, `BufferedInputStream`을 가장 많이 사용

### 5. Random Access
- 파일은 원래 Random Access가 가능한 데이터이고 앞서 살펴본 스트림 방식 (Binary, Text)들은 이러한 Random Access의 특성을 활용하기 어려움
- 텍스트 에디터와 같이 파일을 순차적으로 접근하는 것이 아닌 임의로 접근해야 할 필가 있을 때 Random Access를 활용할 수 있음
  ```java
  public class RandomAccess {
    private static final String FILE_NAME = "./text.txt";
    private static byte[] buffer = new byte[8192];

    public static void main(String[] args) throws IOException {
        // 배열에 utf-8 인코딩하여 문자열 저장
        buffer = "abcdefghijklmnopqrstuvwxyz".getBytes(StandardCharsets.UTF_8);

        // 파일을 읽기/쓰기 모드로 염
        try (RandomAccessFile raf = new RandomAccessFile(FILE_NAME, "rw");) {
            // 배열의 내용을 파일에 쓰기 (문자열 저장됨)
            raf.write(buffer);

            long pointer = raf.getFilePointer();    // 파일의 포인터 위치 확인
            long length = raf.length();             // 파일의 길이 확인

            System.out.println("pointer: " + pointer + ", length: " + length);

            // 포인터 초기화
            raf.seek(0);

            int read = raf.read(buffer);    // 파일에서 데이터 읽어 버퍼에 저장
            buffer = Arrays.copyOfRange(buffer, 0, read);   // 실제로 읽은 데이터만 버퍼에 복사

            System.out.println(new String(buffer));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
  }
  ```

### 6. NIO
- Java 1.4에 추가된 기존 I/O의 단점인 속도를 개선하고자 도입된 새로운 I/O 패키지
- 기존 I/O가 jvm 내에 메모리를 이용하여 버퍼링 하던 것에 비해 NIO는 OS의 메모리를 이용하게 되므로 속도에 이점이 있음
  ```java
  public class NIO {
    private static final String FILE_NAME = "./text.txt";

    public static void main(String[] args) {
        byte[] data = "abcdefghijklmnopqrstuvwxyz".getBytes(StandardCharsets.UTF_8);

        try (FileChannel fc = new FileOutputStream(FILE_NAME).getChannel();) {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            fc.write(buffer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileChannel fc = new FileInputStream(FILE_NAME).getChannel();) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            fc.read(buffer);
            buffer.flip();

            while (buffer.hasRemaining()) {
                System.out.println(buffer.get(data));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
  }
  ```