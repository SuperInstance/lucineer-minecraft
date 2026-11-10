package com.lucineer.block.entity;

import com.lucineer.data.NeuralNetworkData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * Block Entity for Neural Network Bench.
 * 
 * Build and train simple neural networks.
 */
public class NeuralNetworkBlockEntity extends BlockEntity {
    
    private NeuralNetworkData networkData = NeuralNetworkData.DEFAULT;
    private float[] inputValues = new float[]{0.5f, 0.5f};
    private boolean isTraining = false;
    
    public NeuralNetworkBlockEntity(BlockPos pos, BlockState state) {
        super(com.lucineer.block.entity.LucineerBlockEntities.NEURAL_NETWORK_ENTITY.get(), pos, state);
    }
    
    /**
     * Perform forward pass through the network.
     */
    public float[] forwardPass() {
        List<Integer> layers = networkData.layerSizes();
        if (layers.isEmpty()) return new float[0];
        
        float[] activations = inputValues.clone();
        
        for (int l = 0; l < layers.size() - 1; l++) {
            int inputSize = layers.get(l);
            int outputSize = layers.get(l + 1);
            float[] newActivations = new float[outputSize];
            
            for (int j = 0; j < outputSize; j++) {
                float sum = 0;
                for (int i = 0; i < Math.min(inputSize, activations.length); i++) {
                    float weight = getWeight(l, j, i);
                    sum += activations[i] * weight;
                }
                newActivations[j] = sigmoid(sum);
            }
            
            activations = newActivations;
        }
        
        return activations;
    }
    
    private float getWeight(int layer, int neuron, int input) {
        List<Float> weights = networkData.weights();
        int index = 0;
        List<Integer> layers = networkData.layerSizes();
        
        for (int l = 0; l < layer; l++) {
            index += layers.get(l) * layers.get(l + 1);
        }
        index += neuron * layers.get(layer) + input;
        
        if (index < weights.size()) {
            return weights.get(index);
        }
        return 0.5f; // Default weight
    }
    
    private float sigmoid(float x) {
        return (float) (1.0 / (1.0 + Math.exp(-x)));
    }
    
    public void setInput(int index, float value) {
        if (index >= 0 && index < inputValues.length) {
            inputValues[index] = Math.max(0, Math.min(1, value));
            setChanged();
        }
    }
    
    public float getInput(int index) {
        if (index >= 0 && index < inputValues.length) {
            return inputValues[index];
        }
        return 0;
    }
    
    public void startTraining() {
        isTraining = true;
        setChanged();
    }
    
    public void stopTraining() {
        isTraining = false;
        setChanged();
    }
    
    public boolean isTraining() { return isTraining; }
    public NeuralNetworkData getNetworkData() { return networkData; }
    
    public void addLayer() {
        List<Integer> currentLayers = networkData.layerSizes();
        if (currentLayers.size() < 5) {
            // Add a hidden layer before the output
            List<Integer> newLayers = new java.util.ArrayList<>(currentLayers.subList(0, currentLayers.size() - 1));
            newLayers.add(3);
            newLayers.add(currentLayers.get(currentLayers.size() - 1));
            
            networkData = new NeuralNetworkData(
                newLayers,
                networkData.weights(),
                networkData.epoch(),
                networkData.loss(),
                networkData.isTraining()
            );
            setChanged();
        }
    }
    
    public int getEpoch() { return networkData.epoch(); }
    public float getLoss() { return networkData.loss(); }
    
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        for (int i = 0; i < inputValues.length; i++) {
            tag.putFloat("input_" + i, inputValues[i]);
        }
        tag.putBoolean("training", isTraining);
    }
    
    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        for (int i = 0; i < inputValues.length; i++) {
            inputValues[i] = tag.getFloat("input_" + i);
        }
        isTraining = tag.getBoolean("training");
    }
}
