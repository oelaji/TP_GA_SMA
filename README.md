# GA implementation using MAS

## Introduction
Ce projet met en œuvre un algorithme génétique simple visant à trouver la phrase "Bonjour BDCC" en utilisant un modèle basé sur des agents. En adoptant l'architecture Island, chaque agent représente une île avec sa propre population. Les agents interagissent pour échanger les meilleurs individus de leur population respective. Le meilleur individu de chaque population est déterminé par sa valeur de fitness, qui est calculée en comparant le chromosome de cet individu avec celui de la solution recherchée, "Bonjour BDCC". La valeur de fitness correspond au nombre de caractères correctement placés dans le chromosome. Par exemple, si tous les caractères de l'individu "Bonjour BDCC" sont correctement placés, sa valeur de fitness est de 12.
![image](https://github.com/oelaji/TP_GA_SMA/assets/101530033/b164ee3f-254c-42e1-a173-327b46c04b7c)

## Code
> MainContainer
```
public class MainContainer public class extends Agent {
    public static void main(String[] args) throws ControllerException {
        Runtime runtime = Runtime.instance();
        ProfileImpl profile = new ProfileImpl();
        profile.setParameter("gui", "true");
        AgentContainer agentContainer = runtime.createMainContainer(profile);
        agentContainer.start();
    }
}
```

> SimpleContainer
```
public class SimpleContainer {
    public static void main(String[] args) throws ControllerException {
        Runtime runtime = Runtime.instance();
        ProfileImpl profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        AgentContainer agentContainer = runtime.createAgentContainer(profile);
        for (int i = 0; i < GAUtils.ISLAND_NUMBER; i++) {
            AgentController islandAgent = agentContainer.createNewAgent("IslandAgent" + i, IslandAgent.class.getName(), new Object[]{});
            islandAgent.start();
        }
        AgentController masterAgent = agentContainer.createNewAgent("MasterAgent", MasterAgent.class.getName(), new Object[]{});
        masterAgent.start();
    }
}
```

> MasterAgent
```
public class MasterAgent extends Agent {
    @Override
    protected void setup() {
        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setName("master");
        serviceDescription.setType("ga");
        dfAgentDescription.addServices(serviceDescription);
        try {
            DFService.register(this, dfAgentDescription);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage receiver = receive();
                if (receiver != null) {
                    System.out.println("Agent : " + receiver.getSender().getName().split("@")[0] + " => " + receiver.getContent());
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }
    }
}
```

> IslandAgent
```
public class IslandAgent extends Agent {
    private GenticAlgorithm ga = new GenticAlgorithm();

    @Override
    protected void setup() {
        SequentialBehaviour sequentialBehaviour = new SequentialBehaviour();

        sequentialBehaviour.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                ga.initialize();
                ga.sortPopulation();
            }
        });
        sequentialBehaviour.addSubBehaviour(new Behaviour() {
            int iteration = 1;

            @Override
            public void action() {
                ga.crossover();
                ga.mutation();
                ga.sortPopulation();
                iteration++;
            }

            @Override
            public boolean done() {
                return GAUtils.MAX_ITERATIONS == iteration || ga.getBestFintness() == GAUtils.CHROMOSOME_SIZE;
            }
        });
        sequentialBehaviour.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                DFAgentDescription dfAgentDescription = new DFAgentDescription();
                ServiceDescription serviceDescription = new ServiceDescription();
                serviceDescription.setType("ga");
                dfAgentDescription.addServices(serviceDescription);
                DFAgentDescription[] dfAgentDescriptions = null;
                try {
                    dfAgentDescriptions = DFService.search(getAgent(), dfAgentDescription);
                } catch (FIPAException e) {
                    throw new RuntimeException(e);
                }
                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                message.addReceiver(dfAgentDescriptions[0].getName());
                message.setContent(String.valueOf(ga.getPopulation()[0].getFitness()));
                send(message);
            }
        });
        addBehaviour(sequentialBehaviour);
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }
    }
}
```

> GAUtils
```
public class GAUtils {
    public static final int ISLAND_NUMBER = 5;
    public static final int CHROMOSOME_SIZE = 12;
    public static final int POPULATION_SIZE = 50;
    public static final double MUTATION_PROP = 0.2;
    public static final int MAX_ITERATIONS = 2000;
    public static final String message = "Bonjour BDCC";
    public static final String ALPHAS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ ";
}
```

> GenticAlgorithm
```
public class GenticAlgorithm {
    private Individual[] population = new Individual[GAUtils.POPULATION_SIZE];
    private Individual individual1;
    public Individual individual2;

    public void initialize() {
        for (int i = 0; i < GAUtils.POPULATION_SIZE; i++) {
            population[i] = new Individual();
            population[i].calculateFintess();
        }
    }

    public void crossover() {
        individual1 = new Individual(population[0].getChromosome());
        individual2 = new Individual(population[1].getChromosome());
        Random random = new Random();
        int crossPoint = random.nextInt(GAUtils.CHROMOSOME_SIZE - 1);
        crossPoint++;
        for (int i = 0; i < crossPoint; i++) {
            individual1.getChromosome()[i] = population[1].getChromosome()[i];
            individual2.getChromosome()[i] = population[0].getChromosome()[i];
        }
    }

    public void showPopulation() {
        for (Individual individual : population) {
            System.out.println(Arrays.toString(individual.getChromosome()) + " = " + individual.getFitness());
        }
    }

    public void sortPopulation() {
        Arrays.sort(population, Comparator.reverseOrder());
    }

    public void mutation() {
        Random random = new Random();
        if (random.nextDouble() > GAUtils.MUTATION_PROP) {
            int index = random.nextInt(GAUtils.CHROMOSOME_SIZE);
            individual1.getChromosome()[index] = GAUtils.ALPHAS.charAt(random.nextInt(GAUtils.ALPHAS.length()));
        }
        if (random.nextDouble() > GAUtils.MUTATION_PROP) {
            int index = random.nextInt(GAUtils.CHROMOSOME_SIZE);
            individual2.getChromosome()[index] = GAUtils.ALPHAS.charAt(random.nextInt(GAUtils.ALPHAS.length()));
        }
        individual1.calculateFintess();
        individual2.calculateFintess();
        population[GAUtils.POPULATION_SIZE - 2] = individual1;
        population[GAUtils.POPULATION_SIZE - 1] = individual2;
    }

    public int getBestFintness() {
        return population[0].getFitness();
    }

    public Individual[] getPopulation() {
        return population;
    }
}
```

> Individual
```
public class Individual implements Comparable {
    private char[] chromosome = new char[GAUtils.CHROMOSOME_SIZE];
    private int fitness = 0;

    public Individual() {
        Random random = new Random();
        for (int i = 0; i < GAUtils.CHROMOSOME_SIZE; i++) {
            chromosome[i] = GAUtils.ALPHAS.charAt(random.nextInt(GAUtils.ALPHAS.length()));
        }
    }

    public Individual(char[] chromosome) {
        this.chromosome = Arrays.copyOf(chromosome, GAUtils.CHROMOSOME_SIZE);
    }

    public void calculateFintess() {
        for (int i = 0; i < GAUtils.CHROMOSOME_SIZE; i++) {
            if (chromosome[i] == GAUtils.message.charAt(i)) {
                fitness += 1;
            }
        }
    }

    public int getFitness() {
        return fitness;
    }

    public char[] getChromosome() {
        return chromosome;
    }

    public void setChromosome(char[] chromosome) {
        this.chromosome = chromosome;
    }

    @Override
    public int compareTo(Object o) {
        Individual individual = (Individual) o;
        if (this.fitness > individual.fitness) {
            return 1;
        } else if (this.fitness < individual.fitness) {
            return -1;
        } else {
            return 0;
        }
    }
}
```

## Demo
![image](https://github.com/oelaji/TP_GA_SMA/assets/101530033/1e0848b2-c683-43e4-90aa-412e62d0e051)
