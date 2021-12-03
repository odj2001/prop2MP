package main;
import java.util.ArrayList;
import java.util.Stack;

/**
 * The Network object contains the structure and all Neurons of the McCulloch-Pitts neural network.
 * It also contains methods to add new Neurons while building the network, to print a visualisation
 * of the network, and to optimise the network to the smallest possible amount of neurons.
 *
 * @author  Kane Lindsay
 * @version 1.0
 * @since   2021-11-18
 */

public class Network {

    Neuron root;
    Neuron pointer; // Points to neuron to which a new neuron could be added as an input.
    Stack<Neuron> incompleteNeurons = new Stack<>();


    public Neuron getRoot() {
        return root;
    }


    public void addNeuron(ArrayList<Object> inputs, String neuronType) {

        // If the network is empty, add the neuron as the root.
        if (root == null) {
            root = new Neuron(inputs, neuronType,null);
            pointer = root;
            incompleteNeurons.push(root);
            return;
        }

        // Find if there are more than 1 un-atomised expression in the neuron's inputs.
        int expressionCounter = 0;
        for (int i = 0; i < pointer.getInputs().size(); i++) {
            if (pointer.getInputs().get(i) instanceof String) {
                // Check if the string is an un-atomised expression
                if (((String) pointer.getInputs().get(i)).contains("!")
                        || ((String) pointer.getInputs().get(i)).contains("||")
                        || ((String) pointer.getInputs().get(i)).contains("&&")) {
                    expressionCounter++;
                }
            }
        }

        // If there are no possible places for a new neuron at the current pointed neuron,
        // Get the last visited incomplete neuron from the stack and point there instead.
        if (expressionCounter > 1) {
            incompleteNeurons.push(pointer);
        } else if (expressionCounter == 0) {
            pointer = incompleteNeurons.pop();
        }

        for (int i = 0; i < pointer.getInputs().size(); i++) {
            if (pointer.getInputs().get(i) instanceof String) {
                // Check if the string is an un-atomised expression
                if (((String) pointer.getInputs().get(i)).contains("!")
                        || ((String) pointer.getInputs().get(i)).contains("||")
                        || ((String) pointer.getInputs().get(i)).contains("&&")) {
                    // Replace the un-atomised expression with a new neuron, make new neuron the pointer
                    Neuron newNeuron = new Neuron(inputs, neuronType, pointer);

                    ArrayList<Object> pointerInputs = pointer.getInputs();
                    pointerInputs.set(i, newNeuron);
                    pointer.setInputs(pointerInputs);
                    pointer = newNeuron;
                    break;
                }
            }
        }
    }


    public void printNetwork(Neuron neuron, String indent) {

        // Check if there are any input neurons to the current neuron.
        boolean anyNeuronInputs = false;
        for (int i = 0; i < neuron.getInputs().size(); i++)	{
            if (neuron.getInputs().get(i) instanceof Neuron) {
                anyNeuronInputs = true;
                break;
            }
        }

        // Print neuron threshold.
        System.out.print("*" + neuron.getThreshold());

        // In the case the neuron does not have any more input neurons to iterate to.
        if (!anyNeuronInputs) {
            for (int i = 0; i < neuron.getInputs().size(); i++) {
                System.out.println();
                System.out.print(indent + "|__" + neuron.getWeight() + "__" + neuron.getInputs().get(i));
            }
        } else {
            // Make recursive calls to print input neurons.
            for (int i = 0; i < neuron.getInputs().size(); i++) {
                System.out.println();
                System.out.print(indent + "|__" + neuron.getWeight() + "__");

                // If an input is not a neuron, just print it.
                if (neuron.getInputs().get(i) instanceof String) {
                    System.out.print(neuron.getInputs().get(i));
                } else if (i == neuron.getInputs().size() - 1 && neuron.getInputs().get(i) instanceof Neuron) {
                    printNetwork((Neuron) neuron.getInputs().get(i), indent + "       ");
                } else {
                    printNetwork((Neuron) neuron.getInputs().get(i), indent + "|      ");
                }
            }
        }
    }


    public void optimiseNetwork(Neuron rootNeuron) {
        // Set up a pre-order traversal of the network tree.
        Stack<Neuron> stack = new Stack<>();
        stack.push(rootNeuron);
        boolean optimisedFlag = true;

        while (!(stack.isEmpty())) {
            Neuron currentNeuron = stack.pop();

            String currNeuronType = currentNeuron.getNeuronType();
            ArrayList<Object> inputs = currentNeuron.getInputs();

            // For all inputs of neuron
            for (int i = 0; i < inputs.size(); i++) {
                // This will get the first neuron input with the same type as the current neuron.
                if (inputs.get(i) instanceof Neuron && ((Neuron) inputs.get(i)).getNeuronType().equals(currNeuronType)){

                    // Merge the inputs of the child neuron & the current neuron, excluding the child neuron itself.
                    ArrayList<Object> childInputs = ((Neuron) inputs.get(i)).getInputs();
                    inputs.remove(i);
                    ArrayList<Object> mergedInputs = new ArrayList<>();
                    mergedInputs.addAll(inputs);
                    mergedInputs.addAll(childInputs);
                    // Set the inputs of the current neuron as the merged inputs.
                    currentNeuron.setInputs(mergedInputs);

                    // If the child node had neuron inputs, set their parent as the current node instead.
                    for (Object childInput : childInputs) {
                        if (childInput instanceof Neuron) {
                            ((Neuron) childInput).setParent(currentNeuron);
                        }
                    }
                    optimisedFlag = false;
                    break;
                }
            }
            // Push input nodes to continue pre-order traversal.
            for (int k = inputs.size()-1; k >= 0; k--) {
                if (inputs.get(k) instanceof Neuron) {
                    stack.push((Neuron) inputs.get(k));
                }
            }
            // Run the algorithm again on the network until no more optimisations are found.
            if (!optimisedFlag) { optimiseNetwork(rootNeuron); }
        }
    }


    static class Neuron {
        private Neuron parent;
        private ArrayList<Object> inputs;
        private final String neuronType;

        public Neuron(ArrayList<Object> inputs, String neuronType, Neuron parent) {
            this.parent = parent;
            this.inputs = inputs;
            this.neuronType = neuronType;
        }

        public void setInputs(ArrayList<Object> inputs) {
            this.inputs = inputs;
        }

        public Neuron getParent() {
            return parent;
        }

        public void setParent(Neuron parent) {
            this.parent = parent;
        }

        public String getNeuronType() {
            return neuronType;
        }

        public ArrayList<Object> getInputs() {
            return inputs;
        }

        // Weights are always the same for every input of each neuron type.
        public int getWeight() {
            switch (getNeuronType()) {
                case "OR":
                case "AND":
                    return (1);
                case "LOGICAL_COMPLEMENT":
                    return (-1);
                default:
                    return 0;
            }
        }

        // Threshold for OR is always 1 as OR only requires one active input.
        // Threshold for LOGICAL_COMPLEMENT is always 0 as it always outputs 1 unless deactivated by an inhibitory input
        // Threshold for AND is the number of its inputs, as they all must be active to fire the neuron.
        public String getThreshold(){
            switch (getNeuronType()) {
                case "OR":
                    return ("1");
                case "LOGICAL_COMPLEMENT":
                    return ("0");
                case "AND":
                    return String.valueOf(getInputs().size());
                default:
                    return "";
            }
        }
    }
}
