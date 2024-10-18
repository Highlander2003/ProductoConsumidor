import java.util.Random;
import java.util.concurrent.Semaphore;

class Buffer {
    private final int[] buffer;
    private int in = 0;
    private int out = 0;
    private final Semaphore mutex = new Semaphore(1);
    private final Semaphore lleno = new Semaphore(0);
    private final Semaphore vacio;

    public Buffer(int size) {
        buffer = new int[size];
        vacio = new Semaphore(size);
    }

    public void poner(int item) throws InterruptedException {
        vacio.acquire(); // Decrementa vacío; espera si el buffer está lleno
        mutex.acquire(); // Entrada en sección crítica
        buffer[in] = item;
        in = (in + 1) % buffer.length;
        mostrarEstado(); // Mostrar estado del buffer
        mutex.release(); // Salida de sección crítica
        lleno.release(); // Incrementa lleno para indicar un nuevo elemento
    }

    public int sacar() throws InterruptedException {
        lleno.acquire(); // Decrementa lleno; espera si el buffer está vacío
        mutex.acquire(); // Entrada en sección crítica
        int item = buffer[out];
        out = (out + 1) % buffer.length;
        mostrarEstado(); // Mostrar estado del buffer
        mutex.release(); // Salida de sección crítica
        vacio.release(); // Incrementa vacío para indicar un nuevo espacio libre
        return item;
    }

    private void mostrarEstado() {
        System.out.print("Estado del buffer: [");
        for (int i = 0; i < buffer.length; i++) {
            if (i == in) {
                System.out.print("[*]"); // Indica la posición de inserción
            } else if (i == out) {
                System.out.print("[x]"); // Indica la posición de extracción
            } else {
                System.out.print(" " + buffer[i] + " ");
            }
        }
        System.out.println("]");
    }
}

class Productor extends Thread {
    private final Buffer buffer;
    private final Random random = new Random();

    public Productor(Buffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                int item = random.nextInt(100); // Genera un nuevo elemento
                buffer.poner(item);
                System.out.printf("Productor produjo: %d%n", item);
                Thread.sleep(random.nextInt(1000)); // Simula tiempo de producción
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Consumidor extends Thread {
    private final Buffer buffer;
    private final Random random = new Random();

    public Consumidor(Buffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                int item = buffer.sacar();
                System.out.printf("Consumidor consumió: %d%n", item);
                Thread.sleep(random.nextInt(1000)); // Simula tiempo de procesamiento
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class ProductorConsumidor {
    public static void main(String[] args) {
        final int BUFFER_SIZE = 10;
        Buffer buffer = new Buffer(BUFFER_SIZE);
        
        // Crear y empezar productores
        for (int i = 0; i < 2; i++) {
            new Productor(buffer).start();
        }
        
        // Crear y empezar consumidores
        for (int i = 0; i < 3; i++) {
            new Consumidor(buffer).start();
        }
    }
}
