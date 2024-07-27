import java.util.concurrent.Semaphore; // Importamos o semáforo como mecanismo de sincronização
import java.util.concurrent.locks.ReentrantLock; // Importamos as ferramentas de lock como mecanismo de exclusão mútua
import java.util.Random; // Importamos o módulo random para gerar números randômicos em um determinado intervalo

public class Sistema_Transporte {

    private int passageiros_aguardando = 0; // Indica o número de pessoas aguardando na parada
    private int passageiros_no_onibus = 0;

    private final Semaphore controle_parada = new Semaphore(1); // Semáforo que funciona como uma catraca de acesso à parada de ônibus
    private final Semaphore controle_assentos = new Semaphore(0); // Semáforo que funciona como uma catraca de acesso aos assentos de um determinado ônibus
    private final ReentrantLock lock = new ReentrantLock();

    private Random gerador_random = new Random(); // Objeto gerador de números inteiros aleatórios

    public void cria_pessoa(int identificador) throws InterruptedException { // Função responsável por "criar" (gerar) uma pessoa (mais especificamente, um passageiro) nesse sistema.
        new Thread(() -> {
            // Início do processo de (tentar) entrar na parada de ônibus, ou seja, passar na catraca (semáforo) que controla o acesso à parada.
            System.out.println("Passageiro de ID " + identificador + " tenta acessar a parada de ônibus.");

            try {
                controle_parada.acquire();

                System.out.println("Passageiro de ID " + identificador + " conseguiu entrar na parada de ônibus!");

                controle_parada.release();

                lock.lock();

                try { // Uma pessoa conseguiu entrar na parada, portanto aumentamos o número de pessoas aguardando por um ônibus.
                    passageiros_aguardando++;
                } finally {
                    lock.unlock();
                }

                // Início do processo de (tentar) entrar no ônibus e conseguiur um assento, ou seja, passar na catraca (semáforo) que controla o acesso aos assentos de um ônibus.
                controle_assentos.acquire();
                System.out.println("Passageiro de ID " + identificador + " tenta conseguir um assento no ônibus.");

                lock.lock();

                try { // Uma pessoa conseguiu entrar no ônibus e sentar em um lugar, portanto reduzimos o número de pessoas aguardando por um ônibus e aumentamos o de pessoas já dentro do ônibus.
                    System.out.println("Passageiro de ID " + identificador + " conseguiu um assento no ônibus!");

                    passageiros_no_onibus++;
                    passageiros_aguardando--;
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                System.out.println("Houve um erro na execução.");
            }
        }).start(); // Inicia a execução da Thread
    }

    public void gerencia_onibus() throws InterruptedException { // Função responsável por coordenar o sistema de ônibus, o que é feito por uma pessoa do tipo motorista, ao invés de passageiro.
        int total_onibus = 40; // Número arbitrário que representa o total de ônibus que passarão pela parada.

        for (int j = 0; j < total_onibus; j++) {

            int tempo_espera = 1 + (gerador_random.nextInt(3)); // Gera um inteiro entre 1 e 3, que representa o tempo de espera (que será convertido para segundos) para um ônibus passar na parada.

            Thread.sleep(tempo_espera * 1000); // Coloca a Thread para aguardar pelo tempo de espera (agora convertido em segundos)

            System.out.println("Um novo ônibus acaba de chegar!");

            controle_parada.acquire(); // Bloqueia o acesso à parada enquanto o ônibus atual estiver na parada.

            controle_assentos.release(50); // Libera o total de assentos (50) do ônibus.

            Thread.sleep(750); // Simulação de um tempo (arbitrário) para que as pessoas possam subir no ônibus e sentar nos seus devidos lugares.

            controle_assentos.acquire(50 - passageiros_no_onibus); // Atualiza o número de assentos bloqueados a partir do número de passageiros sentados no ônibus.
            passageiros_no_onibus = 0; // Redefine o número de passageiros para o ônibus seguinte.

            System.out.println("O ônibus está partindo!");

            controle_parada.release(); // Libera novamente o acesso à parada de ônibus.
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

        public void run() { // Função de execução para uma instância da classe Pessoa (sendo um Motorista, ele executará o gerenciamento do sistema de ônibus; sendo um Passageiro, ele tentará acessar a parada e um assento de um determinado ônibus).
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
        Sistema_Transporte transporte_publico = new Sistema_Transporte(); // Cria uma instância do sistema de transporte público

        Pessoa motorista = new Pessoa(transporte_publico, "Motorista", -1); // Cria uma única pessoa "Motorista" (que pode ser entendido como um coordenador do sistema de transporte)
        Thread motorista_thread = new Thread(motorista);
        motorista_thread.start();

        int total_pessoas = 200; // Define um número (arbitrário) de Passageiros que utilizarão esse sistema de transporte.
        for (int i = 0; i < total_pessoas; i++) {
           Pessoa passageiro = new Pessoa(transporte_publico, "Passageiro", i); // Cria uma pessoa "Passageiro", com um ID i.
           Thread passageiro_thread = new Thread(passageiro);
           passageiro_thread.start();

           Thread.sleep(250); // Simulação de um tempo (arbitrário) de chegada entre cada Passageiro, para evitar que todos cheguem ao mesmo tempo, criando uma situação mais real.
        }
    }
}
