/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.graphab.metric;

import java.util.Arrays;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.cipr.matrix.DenseLU;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.sparse.*;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.thema.data.feature.Feature;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;

/**
 *
 * @author gvuidel
 */
public class Circuit {
    private GraphGenerator graph;
    private double costR, capaR, capaExp, beta;
    private boolean patch2Ground;
    
    /**
     * for testing native LU decomposition on dense matrix
     * needs native lib of netlib
     */
    private static final boolean dense = false;

    private HashMap<Node, Integer> indNodes;
    private HashMap<Node, Graph> compNodes;
    
    private HashMap<Graph, CompRowMatrix> compMatrix;
    private HashMap<Graph, Preconditioner> compPrecond;
    private HashMap<Graph, DenseLU> compLU;
    
    public Circuit(GraphGenerator graph) {
        if(graph.getType() == GraphGenerator.MST)
            throw new IllegalArgumentException("No circuit in MST graph");
        
        this.graph = graph;
        this.costR = 1;
        this.capaR = 0;
        this.capaExp = 0;
        this.beta = 0;
        this.patch2Ground = false;
        
        init();
    }
    
    public Circuit(GraphGenerator graph, double beta) {
        this.graph = graph;
        this.costR = 1;
        this.capaR = 0;
        this.capaExp = 0;
        this.beta = beta;
        this.patch2Ground = false;
        
        init();
    }
    
    public Circuit(GraphGenerator graph, double costR, double capaR, double capaExp, double beta) {
        this.graph = graph;
        this.costR = costR;
        this.capaR = capaR;
        this.capaExp = capaExp;
        this.beta = beta;
        this.patch2Ground = true;
        
        init();
    }
    
    private void init() {
        indNodes = new HashMap<Node, Integer>();
        compNodes = new HashMap<Node, Graph>();
        compMatrix = new HashMap<Graph, CompRowMatrix>();
        if(!patch2Ground) {
            if(dense)
                compLU = new HashMap<Graph, DenseLU>();
            compPrecond = new HashMap<Graph, Preconditioner>();
        }
        
        for(Graph comp : graph.getComponents()) {
            int nbNodes = comp.getNodes().size();
            if(nbNodes == 1)
                continue;
            int ind = 0;
            for(Node node : (Collection<Node>)comp.getNodes()) {
                indNodes.put(node, ind);
                compNodes.put(node, comp);
                ind++;
            }
            
            List [] indices = new List[nbNodes];
            for(int i = 0; i < nbNodes; i++) {
                indices[i] = new ArrayList(5);
                indices[i].add(i);
            }
            for(Edge edge : (Collection<Edge>)comp.getEdges()) {
                indices[indNodes.get(edge.getNodeA())].add(indNodes.get(edge.getNodeB()));
                indices[indNodes.get(edge.getNodeB())].add(indNodes.get(edge.getNodeA()));
            }
            
                
            CompRowMatrix A;
           
            int [][] tab = new int[nbNodes][];
            for(int i = 0; i < nbNodes; i++) {
                tab[i] = new int[indices[i].size()];
                Collections.sort(indices[i]);
                for(int j = 0; j < tab[i].length; j++)
                    tab[i][j] = (Integer)indices[i].get(j);

            }
            A = new CompRowMatrix(nbNodes, nbNodes, tab);
            
            int i = 0;
            for(Node node : (Collection<Node>)comp.getNodes()) {
                double sum = 0;
                for(Edge edge : (List<Edge>)node.getEdges()) {
                    if(graph.getCost(edge) == 0)
                        throw new RuntimeException("Circuit impossible avec un cout nul !");
                    sum += 1 / (graph.getCost(edge) * costR);
                }
                
                if(patch2Ground && Project.getPatchCapacity(node) == 0)
                    throw new RuntimeException("Circuit impossible avec une capacité nulle !");
                A.set(i, i, sum + (patch2Ground ? 1 / (capaR * Math.pow(Project.getPatchCapacity(node), capaExp)) : 0));
                i++;
            }
            for(Edge edge : (Collection<Edge>)comp.getEdges()) {
                A.set(indNodes.get(edge.getNodeA()), indNodes.get(edge.getNodeB()), -1 / (graph.getCost(edge) * costR));
                A.set(indNodes.get(edge.getNodeB()), indNodes.get(edge.getNodeA()), -1 / (graph.getCost(edge) * costR));
            }          
            
            compMatrix.put(comp, A);
            
            if(!patch2Ground) {
                if(dense) {
                    DenseLU LU = DenseLU.factorize(new DenseMatrix(A));
                    if(!LU.isSingular())
                        compLU.put(comp, LU);
                    else
                        System.out.println("Singular matrix for component size " + nbNodes);
                }
                if(!dense || !compLU.containsKey(comp)){
                    Preconditioner M = new DiagonalPreconditioner(nbNodes);
                    M.setMatrix(A);
                    compPrecond.put(comp, M);
                }
            }
            
        }
    }
    
    /**
     * Calcule le courant traversé dans chaque élément du graphe 
     * à partir d'une tension V appliquée entre le noeud from et le noeud to
     * 
     * @param from noeud émetteur du courant
     * @param to noeud récepteur du courant
     * @param V potentiel du noeud from, le noeud to est relié à la masse
     * @return le courant traversé dans les liens et les noeuds
     */
    public Map<Object, Double> computePotCourant(Node from, Node to, double V) {
        // on calcule le courant dans le graphe pour un courant émis unitaire
        DenseVector U = solveCircuit(from, to, 1);
        if(U == null)
            return Collections.EMPTY_MAP;
        
        int ind1 = indNodes.get(from);
        int ind2 = indNodes.get(to);
        // La différence de potentiel (U) est égal à R car I = 1
        double R =  Math.abs(U.get(ind1) - U.get(ind2));
        
        Graph comp = compNodes.get(from);
        // on modifie les courants en fonction du rapport entre la tension V et la résistance R du circuit
        HashMap<Object, Double> courant = getCourant(comp, U);
        for(Object id : courant.keySet())
            courant.put(id, courant.get(id) * V / R);
        return courant;
    }
    
    /**
     * Calcule la résistance globale entre 2 noeuds du graphe
     * @param n1
     * @param n2
     * @return 
     */
    public double computeR(Node n1, Node n2) {
        DenseVector U = solveCircuit(n1, n2, 1);
        if(U == null)
            return Double.POSITIVE_INFINITY;
        
        int ind1 = indNodes.get(n1);
        int ind2 = indNodes.get(n2);
        // La différence de potentiel (U) est égal à R car I = 1
        return Math.abs(U.get(ind1) - U.get(ind2));
    }        
    
    /**
     * Calcule le courant traversé dans chaque élément du graphe 
     * 
     * @param n1 noeud émetteur du courant
     * @param n2 noeud récepteur du courant
     * @param courant émis par le noeud n1 et récupéré par le noeud n2
     * @return le courant traversé dans les liens et les noeuds
     */
    public Map<Object, Double> computeCourant(Node n1, Node n2, double courant) {
        DenseVector U = solveCircuit(n1, n2, courant);
        if(U == null)
            return Collections.EMPTY_MAP;
        Graph comp = compNodes.get(n1);
        return getCourant(comp, U);
    }
    
    /**
     * Résoud le circuit quand n1 émet le courant et n2 le reçoit
     * @param n1 noeud d'émission
     * @param n2 noeud de réception
     * @param courant émis par n1
     * @return vecteur contenant le potentiel en chaque noeud de la composante
     * null si n1 et n2 ne sont pas dans la même composante
     */
    private DenseVector solveCircuit(Node n1, Node n2, double courant) {
        if(patch2Ground)
            throw new IllegalStateException("Calcul de R impossible avec patch2Ground");
        
        Graph comp = compNodes.get(n1);
        // si les noeuds ne sont pas dans la même composante on sort
        if(comp == null || compNodes.get(n2) != comp)
            return null;
        
        int nbNodes = comp.getNodes().size();

        int ind1 = indNodes.get(n1);
        int ind2 = indNodes.get(n2);
        
        if(ind1 == ind2)
            throw new IllegalArgumentException("Circuit impossible : même noeud origine et destination");
        
        DenseVector U, Z;
        // Z vector is null only the origin patch has current value
        Z = new DenseVector(nbNodes);
        Z.set(ind1, courant);
        Z.set(ind2, -courant);
        
        if(dense && compLU.containsKey(comp)) {
            U = new DenseVector(solveDense(compLU.get(comp), Z).getData());
        } else {
            // Calcule a starting solution U
            double [] v = new double[nbNodes];
            Arrays.fill(v, 1);
            U = new DenseVector(v);
            solve(compMatrix.get(comp), compPrecond.get(comp), Z, U);
        }
        
        return U;
    }
    
    /**
     * Calcule le courant traversé dans chaque élément du graphe 
     * Chaque noeud émet du courant sauf n1 qui récupère tout le courant
     * Cette version est une approximation de computeCourant(n1, n2) qui évite 
     * de calculer toutes les paires.
     * @param n1 noeud récepteur du courant
     * @return le courant traversé dans les liens et les noeuds
     */
    public Map<Object, Double> computeCourantTo(Node n1) {
        if(patch2Ground)
            throw new IllegalStateException("Calcul du courant impossible avec patch2Ground");
        
        Graph comp = compNodes.get(n1);   
        // si c'est un noeud isolé
        if(comp == null)
            return Collections.EMPTY_MAP;
        
        int nbNodes = comp.getNodes().size();
        int ind1 = indNodes.get(n1);
        
        DenseVector U, Z;
        Z = new DenseVector(nbNodes);
        double sumI = 0;
        for(Node node : (Collection<Node>)comp.getNodes()) {
            int ind = indNodes.get(node);
            if(ind == ind1)
                continue;
            double I = Math.pow(Project.getPatchCapacity(node), beta);
            Z.set(ind, I);
            sumI += I;
        }
        Z.set(ind1, -sumI);
        
        if(dense && compLU.containsKey(comp)) {
            U = new DenseVector(solveDense(compLU.get(comp), Z).getData());
        } else {
            // Calcule a starting solution U
            double [] v = new double[nbNodes];
            Arrays.fill(v, 1);
            U = new DenseVector(v);
            solve(compMatrix.get(comp), compPrecond.get(comp), Z, U);
        }

        return getCourant(comp, U);
    }        
    
    /**
     * Calcule le courant traversé dans chaque élément du graphe 
     * Chaque noeud est relié à la masse à travers une résistance sauf n1 qui émet le courant
     * TODO pourquoi enlever la résistance à la masse de n1 ?
     * @param n1 noeud émetteur du courant
     * @return le courant traversé dans les liens et les noeuds
     */
    public Map<Object, Double> computeCourantFrom(Node n1) {
        
        if(!patch2Ground)
            throw new IllegalStateException("Calcul du courant impossible sans patch2Ground");
        
        Graph comp = compNodes.get(n1);
        // si la composante n'existe pas c'est un noeud isolé, on sort
        if(comp == null)
            return Collections.EMPTY_MAP;
        
        int nbNodes = comp.getNodes().size();
        double capa = Project.getPatchCapacity(n1);
        int ind1 = indNodes.get(n1);
        
        DenseVector U, Z;
        // Z vector is null only the origin patch has current value
        Z = new DenseVector(nbNodes);
        Z.set(ind1, Math.pow(capa, beta));
        // Calcule a starting solution U
        double [] v = new double[nbNodes];
        Arrays.fill(v, 1);
        v[ind1] = Math.pow(capa, beta);
        U = new DenseVector(v);

        CompRowMatrix A = compMatrix.get(comp).copy();
        A.add(ind1, ind1, -1 / (capaR * Math.pow(capa, capaExp)));
        
        solve(A, calcPrecond(A), Z, U);

       return getCourant(comp, U);
        
    }
    
    private DenseVector solve(CompRowMatrix A, Preconditioner P, DenseVector Z, DenseVector U) {
        // Allocate storage for Conjugate Gradients
        IterativeSolver solver = new CG(U);

        solver.setPreconditioner(P);
        try {
            // Start the solver, and check for problems
            solver.solve(A, Z, U);
            return U;
        } catch (IterativeSolverNotConvergedException ex) {
            Logger.getLogger(Circuit.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
    
    private DenseMatrix solveDense(DenseLU LU, DenseVector Z) {
        return LU.solve(new DenseMatrix(Z, true));
    }
    
    /**
     * 
     * @param comp composante du graphe
     * @param U vecteur des potentiels
     * @return le courant traversé dans les liens et les noeuds de la composante comp
     */
    private HashMap<Object, Double> getCourant(Graph comp, DenseVector U) {
        HashMap<Object, Double> courant = new HashMap<Object, Double>();
        for(Edge edge : (Collection<Edge>)comp.getEdges()) {
            int iA = indNodes.get(edge.getNodeA());
            int iB = indNodes.get(edge.getNodeB());
            courant.put(((Feature)edge.getObject()).getId(), Math.abs(U.get(iA) - U.get(iB)) / (graph.getCost(edge) * costR));
        }
        for(Node node : (Collection<Node>)comp.getNodes()) {
            double in = 0, out = 0;
            List<Edge> edges = node.getEdges();
            int iA = indNodes.get(node);
            for(Edge edge : edges) {
                int iB = indNodes.get(edge.getOtherNode(node));
                double c = (U.get(iA) - U.get(iB)) / (graph.getCost(edge) * costR);
                if(c < 0)
                    in += -c;
                else
                    out += c;
            }
            courant.put(((Feature)node.getObject()).getId(), Math.min(in, out));
        }
        
        return courant;
    }
    
    /**
     * Calcule le preconditioner dans le cas path2Ground = true
     * @param A
     * @return 
     */
    private Preconditioner calcPrecond(CompRowMatrix A) {
        // Create  preconditioner
        Preconditioner M = new ICC(A.copy());
        // Set up the preconditioner, and attach it
        M.setMatrix(A);
        return M;
    }
}
