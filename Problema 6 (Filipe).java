import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

public class Sistema_Banheiro {
    private int total_no_banheiro = 0;
    private boolean tem_homens = false;
    private boolean tem_mulheres = false;

    private final Semaphore capacidade = new Semaphore(3);
    private final ReentrantLock lock = new ReentrantLock();

    public static Random gerador_random = new Random();

    public void entra_homem(int id) throws InterruptedException {
        new Thread(() -> {
            //System.out.println("Homem " + id + " tenta acessar o banheiro.");
            try {
                lock.lock();
                while (tem_mulheres) {
                    lock.unlock();
                    //Thread.sleep(0);
                    lock.lock();
                }
                tem_homens = true;
                lock.unlock();

                capacidade.acquire();
                lock.lock();
                try {
                    total_no_banheiro++;
                    System.out.println("Homem " + id + " conseguiu acessar o banheiro.");
                } finally {
                    lock.unlock();
                }

                int tempo_simulado = 300 + (gerador_random.nextInt(300));
                Thread.sleep(tempo_simulado);

                lock.lock();
                try {
                    total_no_banheiro--;
                    System.out.println("Homem " + id + " saiu do banheiro.");

                    if (total_no_banheiro == 0) {
                        tem_homens = false;
                    }
                } finally {
                    lock.unlock();
                }

                capacidade.release();
            } catch (InterruptedException e) {
                System.out.println("Houve um erro na execução.");
            }
        }).start();
    }

    public void entra_mulher(int id) throws InterruptedException {
        new Thread(() -> {
            //System.out.println("Mulher " + id + " tenta acessar o banheiro.");
            try {
                lock.lock();
                while (tem_homens) {
                    lock.unlock();
                    //Thread.sleep(0);
                    lock.lock();
                }
                tem_mulheres = true;
                lock.unlock();

                capacidade.acquire();
                lock.lock();
                try {
                    total_no_banheiro++;
                    System.out.println("Mulher " + id + " conseguiu acessar o banheiro.");
                } finally {
                    lock.unlock();
                }

                int tempo_simulado = 300 + (gerador_random.nextInt(300));
                Thread.sleep(tempo_simulado);

                lock.lock();
                try {
                    total_no_banheiro--;
                    System.out.println("Mulher " + id + " saiu do banheiro.");

                    if (total_no_banheiro == 0) {
                        tem_mulheres = false;
                    }
                } finally {
                    lock.unlock();
                }

                capacidade.release();
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
        Sistema_Banheiro banheiro_unisex = new Sistema_Banheiro();

        int total_pessoas = 20;
        for (int i = 0; i < total_pessoas; i++) {
            int indice_genero = gerador_random.nextInt(2); // 0 --> Homem, 1 --> Mulher

            if (indice_genero == 0) {
                Pessoa pessoa = new Pessoa(banheiro_unisex, "Homem", i);
                Thread pessoa_thread = new Thread(pessoa);
                pessoa_thread.start();
            } else {
                Pessoa pessoa = new Pessoa(banheiro_unisex, "Mulher", i);
                Thread pessoa_thread = new Thread(pessoa);
                pessoa_thread.start();
            }
            
            Thread.sleep(400);
        }
    }
}
