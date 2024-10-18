import java.util.Random;
import java.util.concurrent.Semaphore;

class Buffer {
    private final int[] buffer;
    private int in = 0; // Índice de inserción
    private int out = 0; // Índice de extracción
    private final Semaphore mutex = new Semaphore(1);
    private final Semaphore lleno = new Semaphore(0);
    private final Semaphore vacio;

    public Buffer(int size) {
        buffer = new int[size];
        vacio = new Semaphore(size); // Inicializa los espacios vacíos
    }

    public void poner(int item) throws InterruptedException {
        vacio.acquire(); // Espera si el buffer está lleno
        mutex.acquire(); // Sección crítica
        buffer[in] = item;
        mostrarEstado("Producto añadido: " + item);
        in = (in + 1) % buffer.length; // Actualiza el índice de inserción
        mutex.release();
        lleno.release(); // Señala que hay un nuevo elemento
    }

    public int sacar() throws InterruptedException {
        lleno.acquire(); // Espera si el buffer está vacío
        mutex.acquire(); // Sección crítica
        int item = buffer[out];
        mostrarEstado("Producto consumido: " + item);
        out = (out + 1) % buffer.length; // Actualiza el índice de extracción
        mutex.release();
        vacio.release(); // Señala que hay un espacio libre
        return item;
    }

    private void mostrarEstado(String mensaje) {
        System.out.print(mensaje + " | Estado del buffer: [");
        for (int i = 0; i < buffer.length; i++) {
            if (i == in) {
                System.out.print("[*] "); // Posición de inserción
            } else if (i == out) {
                System.out.print("[x] "); // Posición de extracción
            } else if (buffer[i] != 0) {
                System.out.print(buffer[i] + " "); // Elemento en el buffer
            } else {
                System.out.print("~ "); // Espacio vacío
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
        
        // Inicia productores
        for (int i = 0; i < 2; i++) {
            new Productor(buffer).start();
        }
        
        // Inicia consumidores
        for (int i = 0; i < 3; i++) {
            new Consumidor(buffer).start();
        }
    }
}
