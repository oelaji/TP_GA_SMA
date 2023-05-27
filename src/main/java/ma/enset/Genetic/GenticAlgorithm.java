package ma.enset.Genetic;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

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
