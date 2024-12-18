package GTDT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm {
    // Lớp đại diện cho cạnh của đồ thị
    static class Edge {
        int u, v, w;

        public Edge(int u, int v, int w) {
            this.u = u;
            this.v = v;
            this.w = w;
        }
    }

    // Lớp đại diện cho cá thể (một cây khung)
    static class Individual {
        List<Edge> edges; // Danh sách các cạnh trong cây khung
        int fitness;      // Tổng trọng số các cạnh

        public Individual(List<Edge> edges, int fitness) {
            this.edges = edges;
            this.fitness = fitness;
        }
    }

    // Hàm khởi tạo quần thể ban đầu
    static List<Individual> initializePopulation(List<Edge> edges, int numVertices, int populationSize) {
        List<Individual> population = new ArrayList<>();
        Random random = new Random();

        while (population.size() < populationSize) {
            // Tạo ngẫu nhiên một cá thể
            Collections.shuffle(edges);
            List<Edge> candidate = new ArrayList<>();
            UnionFind uf = new UnionFind(numVertices);

            for (Edge edge : edges) {
                if (uf.union(edge.u, edge.v) && candidate.size() < numVertices - 1) {
                    candidate.add(edge);
                }
                if (candidate.size() == numVertices - 1) break;
            }

            // Kiểm tra xem đồ thị có liên thông hay không
            if (candidate.size() == numVertices - 1) {
                int fitness = candidate.stream().mapToInt(e -> e.w).sum();
                population.add(new Individual(candidate, fitness));
            }
        }

        return population;
    }

    // Lựa chọn cá thể (Roulette Wheel Selection)
    static Individual selectIndividual(List<Individual> population) {
        if (population.isEmpty()) {
            throw new IllegalArgumentException("Quần thể rỗng, không thể chọn cá thể!");
        }
        return population.get(new Random().nextInt(population.size()));
    }

    // Lai ghép hai cá thể
    static Individual crossover(Individual parent1, Individual parent2, int numVertices) {
        List<Edge> childEdges = new ArrayList<>(parent1.edges);

        Random random = new Random();
        for (Edge edge : parent2.edges) {
            if (random.nextBoolean() && !childEdges.contains(edge)) {
                childEdges.add(edge);
            }
        }

        // Sửa chữa để đảm bảo tính hợp lệ của cây khung
        return repair(childEdges, numVertices);
    }

    // Đột biến cá thể
    static void mutate(Individual individual, List<Edge> allEdges, int numVertices) {
        Random random = new Random();
        int index = random.nextInt(individual.edges.size());
        individual.edges.remove(index);

        // Thêm cạnh mới ngẫu nhiên từ danh sách toàn bộ cạnh
        for (int i = 0; i < allEdges.size(); i++) {
            Edge newEdge = allEdges.get(random.nextInt(allEdges.size()));
            if (!individual.edges.contains(newEdge)) {
                individual.edges.add(newEdge);
                break;
            }
        }

        // Sửa chữa để đảm bảo tính hợp lệ của cá thể
        repair(individual.edges, numVertices);
    }

    // Sửa chữa cá thể (đảm bảo cây khung hợp lệ)
    static Individual repair(List<Edge> edges, int numVertices) {
        Collections.shuffle(edges);
        List<Edge> repairedEdges = new ArrayList<>();
        UnionFind uf = new UnionFind(numVertices);

        for (Edge edge : edges) {
            if (uf.union(edge.u, edge.v) && repairedEdges.size() < numVertices - 1) {
                repairedEdges.add(edge);
            }
        }

        int fitness = repairedEdges.stream().mapToInt(e -> e.w).sum();
        return new Individual(repairedEdges, fitness);
    }

    // Thuật toán di truyền chính
    static Individual geneticAlgorithm(List<Edge> edges, int numVertices, int populationSize, 
            int maxGenerations, double crossoverRate, double mutationRate) {
        List<Individual> population = initializePopulation(edges, numVertices, populationSize);

        // Kiểm tra nếu không thể tạo quần thể hợp lệ (đồ thị không liên thông)
        if (population.isEmpty()) {
            return null;  // Nếu không có cá thể hợp lệ nào được tạo ra
        }

        Random random = new Random();

        for (int generation = 0; generation < maxGenerations; generation++) {
            List<Individual> newPopulation = new ArrayList<>();

            for (int i = 0; i < populationSize; i++) {
                Individual parent1 = selectIndividual(population);
                Individual parent2 = selectIndividual(population);

                Individual child;
                if (random.nextDouble() < crossoverRate) {
                    child = crossover(parent1, parent2, numVertices);
                } else {
                    child = new Individual(new ArrayList<>(parent1.edges), parent1.fitness);
                }

                if (random.nextDouble() < mutationRate) {
                    mutate(child, edges, numVertices);
                }

                newPopulation.add(child);
            }

            population = newPopulation;
        }

        // Trả về cá thể tốt nhất
        return population.stream().min(Comparator.comparingInt(ind -> ind.fitness)).orElse(null);
    }

    // Hàm chính
    public static void main(String[] args) {
    	List<Edge> edges = new ArrayList<>();
    	int numVertices = 6;
    	Random random = new Random();
    	for (int i = 0; i < numVertices; i++) {
    	    for (int j = i + 1; j < numVertices; j++) {
    	        edges.add(new Edge(i, j, random.nextInt(10) + 1)); // Trọng số ngẫu nhiên từ 1 đến 10
    	    }
    	}

    	int populationSize = 30;
    	int maxGenerations = 200;
    	double crossoverRate = 0.8;
    	double mutationRate = 0.3;

    	Individual result = geneticAlgorithm(edges, numVertices, populationSize, maxGenerations, crossoverRate, mutationRate);

    	System.out.println("Cây khung nhỏ nhất:");
    	for (Edge edge : result.edges) {
    	    System.out.println(edge.u + " - " + edge.v + " : " + edge.w);
    	}
    	System.out.println("Tổng trọng số: " + result.fitness);
    }

    // Lớp Union-Find để kiểm tra chu trình
    static class UnionFind {
        int[] parent;

        public UnionFind(int size) {
            parent = new int[size];
            for (int i = 0; i < size; i++) parent[i] = i;
        }

        public int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);
            }
            return parent[x];
        }

        public boolean union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);
            if (rootX != rootY) {
                parent[rootX] = rootY;
                return true;
            }
            return false;
        }
    }
}
