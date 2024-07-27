import java.util.concurrent.Semaphore; // Importamos o semáforo como mecanismo de sincronização
import java.util.concurrent.locks.ReentrantLock; // Importamos as ferramentas de lock como mecanismo de exclusão mútua
import java.util.Random; // Importamos o módulo random para gerar números randômicos em um determinado intervalo

public class Sistema_Banheiro {
    private int total_no_banheiro = 0; // Indica o número total (homens + mulheres) de pessoas ocupando o banheiro em um determinado momento.
    private boolean tem_homens = false; // Indica se existem homens no banheiro em um determinado momento.
    private boolean tem_mulheres = false; // Indica se existem mulheres no banheiro em um determinado momento.

    private final Semaphore capacidade = new Semaphore(3); // Semáforo que funciona como uma catraca de acesso ao banheiro (ocupação máxima = 3)
    private final ReentrantLock lock = new ReentrantLock();

    public static Random gerador_random = new Random(); // Objeto gerador de números inteiros aleatórios

    public void entra_homem(int id) throws InterruptedException { // Função responsável por coordenar a (tentativa de) entrada de um homem no banheiro.
        new Thread(() -> {
            // Início do processo de um homem (tentar) entrar no banheiro, ou seja, passar na catraca (semáforo) que controla o acesso ao banheiro.
            System.out.println("Homem " + id + " entrou na fila para tenta acessar o banheiro.");
            try {
                lock.lock();
                while (tem_mulheres) { // Enquanto houver mulheres no banheiro, a entrada do homem fica bloqueada. Entramos em um loop que simula um estado de "busy waiting" até que não existam mais mulheres no banheiro.
                    lock.unlock();
                    lock.lock();
                }
                
                tem_homens = true; // Todas as mulheres saíram do banheiro, então o homem conseguirá entrar, e devemos alterar o valor da variável booleana que indica se há homens no banheiro.
                lock.unlock();

                capacidade.acquire(); // Bloqueia 1 acesso ao banheiro.
                lock.lock();
                try { // O homem conseguiu entrar no banheiro, portanto aumentamos o número total de pessoas no banheiro e indicamos que ele entrou.
                    total_no_banheiro++;
                    System.out.println("Homem " + id + " conseguiu acessar o banheiro.");
                } finally {
                    lock.unlock();
                }

                int tempo_simulado = 300 + (gerador_random.nextInt(300)); // Como cada pessoa pode demorar mais ou menos tempo dentro do banheiro, geramos um tempo simulado aleatório.
                Thread.sleep(tempo_simulado); // Interrompemos a execução pelo tempo simulado (até que a pessoa termine suas necessidades e saia do banheiro)

                lock.lock();
                try { // O homem saiu do banheiro, portanto reduzimos o número total de pessoas no banheiro e indicamos que ele saiu.
                    total_no_banheiro--;
                    System.out.println("Homem " + id + " saiu do banheiro.");

                    if (total_no_banheiro == 0) { // Caso o total de pessoas no banheiro, nesse momento, seja zero, redefinimos o valor da variável que indica se há homens no banheiro.
                        tem_homens = false;
                    }
                } finally {
                    lock.unlock();
                }

                capacidade.release(); // LIbera 1 acesso ao banheiro.
            } catch (InterruptedException e) {
                System.out.println("Houve um erro na execução.");
            }
        }).start();
    }

    public void entra_mulher(int id) throws InterruptedException { // Função responsável por coordenar a (tentativa de) entrada de uma mulher no banheiro.
        new Thread(() -> {
            // Início do processo de uma mulher (tentar) entrar no banheiro, ou seja, passar na catraca (semáforo) que controla o acesso ao banheiro.
            System.out.println("Mulher " + id + " entrou na fila para tenta acessar o banheiro.");
            try {
                lock.lock();
                while (tem_homens) { // Enquanto houver homens no banheiro, a entrada da mulher fica bloqueada. Entramos em um loop que simula um estado de "busy waiting" até que não existam mais homens no banheiro.
                    lock.unlock();
                    //Thread.sleep(0);
                    lock.lock();
                }
                tem_mulheres = true; // Todas os homens saíram do banheiro, então a mulher conseguirá entrar, e devemos alterar o valor da variável booleana que indica se há mulheres no banheiro.
                lock.unlock();

                capacidade.acquire(); // Bloqueia 1 acesso ao banheiro.
                lock.lock();
                try { // A mulher conseguiu entrar no banheiro, portanto aumentamos o número total de pessoas no banheiro e indicamos que ela entrou.
                    total_no_banheiro++;
                    System.out.println("Mulher " + id + " conseguiu acessar o banheiro.");
                } finally {
                    lock.unlock();
                }

                int tempo_simulado = 300 + (gerador_random.nextInt(300)); // Como cada pessoa pode demorar mais ou menos tempo dentro do banheiro, geramos um tempo simulado aleatório.
                Thread.sleep(tempo_simulado); // Interrompemos a execução pelo tempo simulado (até que a pessoa termine suas necessidades e saia do banheiro)

                lock.lock();
                try { // A mulher saiu do banheiro, portanto reduzimos o número total de pessoas no banheiro e indicamos que ele saiu.
                    total_no_banheiro--;
                    System.out.println("Mulher " + id + " saiu do banheiro.");

                    if (total_no_banheiro == 0) { // Caso o total de pessoas no banheiro, nesse momento, seja zero, redefinimos o valor da variável que indica se há mulheres no banheiro.
                        tem_mulheres = false;
                    }
                } finally {
                    lock.unlock();
                }

                capacidade.release(); // Libera 1 acesso ao banheiro.
            } catch (InterruptedException e) {
                System.out.println("Houve um erro na execução.");
            }
        }).start();
    }

    public static class Pessoa implements Runnable {
        private final Sistema_Banheiro banheiro_unisex;
        private final String genero;
        private final int id;

        public Pessoa(Sistema_Banheiro banheiro, String genero, int id) {
            this.banheiro_unisex = banheiro;
            this.genero = genero;
            this.id = id;
        }

        public void run() {
            if (genero.equals("Homem")) {
                try {
                    banheiro_unisex.entra_homem(id);
                } catch (InterruptedException e) {
                    System.out.println("Houve um erro na execução.");
                }
            } else {
                try {
                    banheiro_unisex.entra_mulher(id);
                } catch (InterruptedException e) {
                    System.out.println("Houve um erro na execução.");
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Sistema_Banheiro banheiro_unisex = new Sistema_Banheiro(); // Cria a instância da classe do sistema do banheiro unisex.

        int total_pessoas = 20; // Define um número (arbitrário) de pessoas que utilizarão o banheiro.
        for (int i = 0; i < total_pessoas; i++) {
            int indice_genero = gerador_random.nextInt(2); // Gera um número aleatório entre 0 e 1, de forma que:
            // Se for 0, geramos a entrada de um homem no sistema;
            // Se for 1, geramos a entrada de uma mulher no sistema.

            if (indice_genero == 0) {
                Pessoa pessoa = new Pessoa(banheiro_unisex, "Homem", i);
                Thread pessoa_thread = new Thread(pessoa);
                pessoa_thread.start();
            } else {
                Pessoa pessoa = new Pessoa(banheiro_unisex, "Mulher", i);
                Thread pessoa_thread = new Thread(pessoa);
                pessoa_thread.start();
            }
            
            Thread.sleep(250); // Simulação de um tempo (arbitrário) entre a chegada de cada pessoa na fila do banheiro.
        }
    }
}
