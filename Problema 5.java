import java.util.concurrent.Semaphore; // Importamos o semáforo como mecanismo de sincronização
import java.util.concurrent.locks.ReentrantLock; // Importamos as ferramentas de lock como mecanismo de exclusão mútua
import java.util.Random; // Importamos o módulo random para gerar números randômicos em um determinado intervalo

public class Sistema_Transporte {

    private int passageiros_aguardando = 0;
    private int passageiros_no_onibus = 0;

    private final Semaphore controle_parada = new Semaphore(1);
    private final Semaphore controle_assentos = new Semaphore(0);
    private final ReentrantLock lock = new ReentrantLock();

    private Random gerador_random = new Random();

    public void cria_pessoa(int identificador) throws InterruptedException {
        new Thread(() -> {
            System.out.println("Passageiro de ID " + identificador + " tenta acessar a parada de ônibus.");

            try {
                controle_parada.acquire();

                System.out.println("Passageiro de ID " + identificador + " conseguiu entrar na parada de ônibus!");

                controle_parada.release();

                lock.lock();

                try {
                    passageiros_aguardando++;
                } finally {
                    lock.unlock();
                }

                controle_assentos.acquire();
                System.out.println("Passageiro de ID " + identificador + " tenta conseguir um assento no ônibus.");

                lock.lock();

                try {
                    System.out.println("Passageiro de ID " + identificador + " conseguiu um assento no ônibus!");

                    passageiros_no_onibus++;
                    passageiros_aguardando--;
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                System.out.println("Houve um erro na execução.");
            }
        }).start();
    }

    public void gerencia_onibus() throws InterruptedException {
        int total_onibus = 40;

        for (int j = 0; j < total_onibus; j++) {

            int tempo_espera = 1 + (gerador_random.nextInt(3));

            Thread.sleep(tempo_espera * 1000);

            System.out.println("Um novo ônibus acaba de chegar!");

            controle_parada.acquire();

            controle_assentos.release(50);

            Thread.sleep(750);

            controle_assentos.acquire(50 - passageiros_no_onibus);
            passageiros_no_onibus = 0;

            System.out.println("O ônibus está partindo!");

            controle_parada.release();
        }
    }

    public static class Pessoa implements Runnable {

        private final Sistema_Transporte transporte_publico;
        private final String tipo;
        private final int id;

        public Pessoa(Sistema_Transporte transporte_publico, String tipo, int id) {
            this.transporte_publico = transporte_publico;
            this.tipo = tipo;
            this.id = id;
        }

        public void run() {
            if (tipo == "Motorista") {
                try {
                    transporte_publico.gerencia_onibus();
                } catch (InterruptedException e) {
                    System.out.println("Houve um erro na execução.");
                }
            } else if (tipo == "Passageiro") {
                try {
                    transporte_publico.cria_pessoa(id);
                } catch (InterruptedException e) {
                    System.out.println("Houve um erro na execução.");
                }
            }

        }
    }

    public static void main(String[] args) throws InterruptedException {
        Sistema_Transporte transporte_publico = new Sistema_Transporte();

        Pessoa motorista = new Pessoa(transporte_publico, "Motorista", -1);
        Thread motorista_thread = new Thread(motorista);
        motorista_thread.start();

        int total_pessoas = 200;
        for (int i = 0; i < total_pessoas; i++) {
           Pessoa passageiro = new Pessoa(transporte_publico, "Passageiro", i);
           Thread passageiro_thread = new Thread(passageiro);
           passageiro_thread.start();
        }
    }
}
