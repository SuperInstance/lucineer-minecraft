package com.lucineer.item;

import net.minecraft.world.item.Item;

/**
 * Neuron Cell Item - Building block for neural networks.
 */
public class NeuronCellItem extends Item {
    
    public enum ActivationFunction {
        SIGMOID, RELU, TANH, LEAKY_RELU
    }
    
    private ActivationFunction activation = ActivationFunction.SIGMOID;
    private float bias = 0.0f;
    
    public NeuronCellItem(Properties properties) {
        super(properties);
    }
    
    public ActivationFunction getActivation() { return activation; }
    public void setActivation(ActivationFunction activation) { this.activation = activation; }
    public float getBias() { return bias; }
    public void setBias(float bias) { this.bias = bias; }
    
    /**
     * Compute activation function output.
     */
    public float activate(float x) {
        return switch (activation) {
            case SIGMOID -> (float) (1.0 / (1.0 + Math.exp(-x)));
            case RELU -> Math.max(0, x);
            case TANH -> (float) Math.tanh(x);
            case LEAKY_RELU -> x > 0 ? x : 0.01f * x;
        };
    }
    
    /**
     * Compute neuron output from weighted inputs.
     */
    public float compute(float[] inputs, float[] weights) {
        float sum = bias;
        for (int i = 0; i < Math.min(inputs.length, weights.length); i++) {
            sum += inputs[i] * weights[i];
        }
        return activate(sum);
    }
    
    public String getActivationName() {
        return switch (activation) {
            case SIGMOID -> "Sigmoid (0-1)";
            case RELU -> "ReLU (0+)";
            case TANH -> "Tanh (-1 to 1)";
            case LEAKY_RELU -> "Leaky ReLU";
        };
    }
}
