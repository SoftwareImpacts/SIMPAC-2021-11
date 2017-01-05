/*
 * Copyright (C) 2014 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
 * http://thema.univ-fcomte.fr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.thema.graphab.metric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.uib.cipr.matrix.DenseLU;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.sparse.CG;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import no.uib.cipr.matrix.sparse.DiagonalPreconditioner;
import no.uib.cipr.matrix.sparse.IterativeSolver;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import no.uib.cipr.matrix.sparse.Preconditioner;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.thema.data.feature.Feature;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;

/**
 * Calculates electric circuit on graph.
 * 
 * @author Gilles Vuidel
 */
public class Circuit {
    private GraphGenerator graph;
    
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
    
    /**
     * Creates a new Circuit with graph
     * @param graph the graph representing the electric circuit
     */
    public Circuit(GraphGenerator graph) {
        if(graph.getType() == GraphGenerator.MST) {
            throw new IllegalArgumentException("No circuit in MST graph");
        }
        this.graph = graph;
        
        init();
    }
    
    /**
     * Initialize the matrices representing the circuits and the preconditionners
     */
    private void init() {
        indNodes = new HashMap<>();
        compNodes = new HashMap<>();
        compMatrix = new HashMap<>();

        if(dense) {
            compLU = new HashMap<>();
        }
        compPrecond = new HashMap<>();
        
        
        for(Graph comp : graph.getComponents()) {
            int nbNodes = comp.getNodes().size();
            if(nbNodes == 1) {
                continue;
            }
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
            
            int [][] tab = new int[nbNodes][];
            for(int i = 0; i < nbNodes; i++) {
                tab[i] = new int[indices[i].size()];
                Collections.sort(indices[i]);
                for(int j = 0; j < tab[i].length; j++) {
                    tab[i][j] = (Integer)indices[i].get(j);
                }

            }
            
            CompRowMatrix A = new CompRowMatrix(nbNodes, nbNodes, tab);
            
            int i = 0;
            for(Node node : (Collection<Node>)comp.getNodes()) {
                double sum = 0;
                for(Edge edge : (List<Edge>)node.getEdges()) {
                    if(graph.getCost(edge) == 0) {
                        throw new RuntimeException("Circuit impossible avec un cout nul !");
                    }
                    sum += 1 / graph.getCost(edge);
                }
                
                A.set(i, i, sum);
                i++;
            }
            for(Edge edge : (Collection<Edge>)comp.getEdges()) {
                A.set(indNodes.get(edge.getNodeA()), indNodes.get(edge.getNodeB()), -1 / graph.getCost(edge));
                A.set(indNodes.get(edge.getNodeB()), indNodes.get(edge.getNodeA()), -1 / graph.getCost(edge));
            }          
            
            compMatrix.put(comp, A);
            
            if(dense) {
                DenseLU LU = DenseLU.factorize(new DenseMatrix(A));
                if(!LU.isSingular()) {
                    compLU.put(comp, LU);
                } else {
                    System.out.println("Singular matrix for component size " + nbNodes);
                }
            }
            if(!dense || !compLU.containsKey(comp)){
                Preconditioner M = new DiagonalPreconditioner(nbNodes);
                M.setMatrix(A);
                compPrecond.put(comp, M);
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
        if(U == null) {
            return Collections.EMPTY_MAP;
        }
        
        int ind1 = indNodes.get(from);
        int ind2 = indNodes.get(to);
        // La différence de potentiel (U) est égal à R car I = 1
        double R =  Math.abs(U.get(ind1) - U.get(ind2));
        
        Graph comp = compNodes.get(from);
        // on modifie les courants en fonction du rapport entre la tension V et la résistance R du circuit
        HashMap<Object, Double> courant = getCourant(comp, U);
        for(Object id : courant.keySet()) {
            courant.put(id, courant.get(id) * V / R);
        }
        return courant;
    }
    
    /**
     * Calcule la résistance globale entre 2 noeuds du graphe
     * @param n1 noeud 1
     * @param n2 noeud 2
     * @return la résistance entre n1 et n2
     */
    public double computeR(Node n1, Node n2) {
        if(n1 == n2) {
            return 0;
        }
        DenseVector U = solveCircuit(n1, n2, 1);
        if(U == null) {
            return Double.POSITIVE_INFINITY;
        }
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
        if(U == null) {
            return Collections.EMPTY_MAP;
        }
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
       
        Graph comp = compNodes.get(n1);
        // si les noeuds ne sont pas dans la même composante on sort
        if(comp == null || compNodes.get(n2) != comp) {
            return null;
        }
        
        int nbNodes = comp.getNodes().size();

        int ind1 = indNodes.get(n1);
        int ind2 = indNodes.get(n2);
        
        if(ind1 == ind2) {
            throw new IllegalArgumentException("Circuit impossible : même noeud origine et destination");
        }
        
        // Z vector is null only the origin patch has current value
        DenseVector Z = new DenseVector(nbNodes);
        Z.set(ind1, courant);
        Z.set(ind2, -courant);
        
        DenseVector U;
        if(dense && compLU.containsKey(comp)) {
            U = new DenseVector(solveDense(compLU.get(comp), Z).getData());
        } else {
            U = solve(compMatrix.get(comp), compPrecond.get(comp), Z);
        }
        
        return U;
    }
    
    /**
     * NE PAS UTILISER RESULTAT TROP APPROXIMATIF
     * Calcule les résistances entre 1 noeud du graphe et tous les autres.
     * Calcul beaucoup plus rapide que {@link #computeR } mais très approximatif...
     * @param n1 noeud recevant tout le courant
     * @return la résistance entre chaque noeud et n1
     */
    public Map<Node, Double> computeRs(Node n1) {
        DenseVector U = solveCircuit(n1, 0);
        if(U == null) {
            return Collections.EMPTY_MAP;
        }

        HashMap<Node, Double> mapR = new HashMap<>();
        mapR.put(n1, 0.0);
        
        for(Node n2 : (Collection<Node>)compNodes.get(n1).getNodes()) {
            if(mapR.containsKey(n2)) {
                continue;
            }
            calcR(n2, U, mapR);
        }

        return mapR;
    }     
    
    /**
     * Recursive method called used by computeRs
     * @param n1
     * @param U
     * @param mapR
     * @return 
     */
    private double calcR(Node n1, DenseVector U, HashMap<Node, Double> mapR) {
        if(mapR.containsKey(n1)) {
            return mapR.get(n1);
        }
        int ind1 = indNodes.get(n1);
        double r = 0;
        List<Edge> edges = n1.getEdges();
        for(Edge edge : edges) {
            Node n2 = edge.getOtherNode(n1);
            int ind2 = indNodes.get(n2);
            if(U.get(ind1) - U.get(ind2) > 0)  {// si le courant part vers cet edge
                r += 1 / (calcR(n2, U, mapR) + graph.getCost(edge));
            }
            
        }
        mapR.put(n1, 1 / r);
        return 1 / r;
    }
    
    /**
     * Calcule le courant traversé dans chaque élément du graphe.
     * Chaque noeud émet du courant (=capacity^beta) sauf n1 qui récupère tout le courant
     * Cette version est une approximation de computeCourant(n1, n2) qui évite 
     * de calculer toutes les paires.
     * @param n1 noeud récepteur du courant
     * @param beta capacity exponent for current emission
     * @return le courant traversé dans les liens et les noeuds
     */
    public Map<Object, Double> computeCourantTo(Node n1, double beta) {
        DenseVector U = solveCircuit(n1, beta);
        if(U == null) {
            return Collections.EMPTY_MAP;
        }
        Graph comp = compNodes.get(n1);
        return getCourant(comp, U);
    }        
    
    /**
     * Résoud le circuit quand n1 reçoit le courant émis par tous les autres noeuds.
     * Chaque noeud émet un courant égal à capacity^beta sauf n1 qui récupère tout le courant
     * Cette version est une approximation de solveCircuit(n1, n2) qui évite 
     * de calculer toutes les paires.
     * @param n1 noeud récepteur du courant
     * @param beta capacity exponent for current emission
     * @return vecteur contenant le potentiel en chaque noeud de la composante
     * null si n1 est un noeud isolé
     */
    private DenseVector solveCircuit(Node n1, double beta) {
        
        Graph comp = compNodes.get(n1);   
        // si c'est un noeud isolé
        if(comp == null) {
            return null;
        }
        int nbNodes = comp.getNodes().size();
        int ind1 = indNodes.get(n1);
        
        DenseVector Z = new DenseVector(nbNodes);
        double sumI = 0;
        for(Node node : (Collection<Node>)comp.getNodes()) {
            int ind = indNodes.get(node);
            if(ind == ind1) {
                continue;
            }
            double I = Math.pow(Project.getPatchCapacity(node), beta);
            Z.set(ind, I);
            sumI += I;
        }
        Z.set(ind1, -sumI);
        
        DenseVector U;
        if(dense && compLU.containsKey(comp)) {
            U = new DenseVector(solveDense(compLU.get(comp), Z).getData());
        } else {
            U = solve(compMatrix.get(comp), compPrecond.get(comp), Z);
        }

        return U;
    }        
    
    /**
     * Solve A.U = Z for sparse matrix A with preconditioner P
     * @param A the sparse matrix
     * @param P the preconditioner
     * @param Z the vector
     * @return the vector U
     */
    private DenseVector solve(CompRowMatrix A, Preconditioner P, DenseVector Z) {
        // Calcule a starting solution U
        double [] v = new double[Z.size()];
        Arrays.fill(v, 1);
        DenseVector U = new DenseVector(v);        
        
        // Allocate storage for Conjugate Gradients
        IterativeSolver solver = new CG(U);
        solver.setPreconditioner(P);
        try {
            // Start the solver, and check for problems
            solver.solve(A, Z, U);
            return U;
        } catch (IterativeSolverNotConvergedException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Solve A.U = Z for dense matrix A by LU decomposition
     * @param A the dense matrix
     * @param Z the vector
     * @return the vector U
     */
    private DenseMatrix solveDense(DenseLU A, DenseVector Z) {
        return A.solve(new DenseMatrix(Z, true));
    }
    
    /**
     * Retrieve the current through each node and each edge
     * @param comp graph component
     * @param U voltage vector
     * @return le courant traversé dans les liens et les noeuds de la composante comp
     */
    private HashMap<Object, Double> getCourant(Graph comp, DenseVector U) {
        HashMap<Object, Double> courant = new HashMap<>();
        for(Edge edge : (Collection<Edge>)comp.getEdges()) {
            int iA = indNodes.get(edge.getNodeA());
            int iB = indNodes.get(edge.getNodeB());
            courant.put(((Feature)edge.getObject()).getId(), Math.abs(U.get(iA) - U.get(iB)) / graph.getCost(edge));
        }
        for(Node node : (Collection<Node>)comp.getNodes()) {
            double in = 0, out = 0;
            List<Edge> edges = node.getEdges();
            int iA = indNodes.get(node);
            for(Edge edge : edges) {
                int iB = indNodes.get(edge.getOtherNode(node));
                double c = (U.get(iA) - U.get(iB)) / graph.getCost(edge);
                if(c < 0) {
                    in += -c;
                } else {
                    out += c;
                }
            }
            courant.put(((Feature)node.getObject()).getId(), Math.min(in, out));
        }
        
        return courant;
    }
   
}
