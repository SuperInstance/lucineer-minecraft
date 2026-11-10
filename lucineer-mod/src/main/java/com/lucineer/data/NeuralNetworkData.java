package com.lucineer.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import java.util.List;

/**
 * Stores neural network structure and training state.
 */
public record NeuralNetworkData(
    List<Integer> layerSizes,    // e.g., [2, 3, 1]
    List<Float> weights,          // Flattened weight matrix
    int currentEpoch,
    float currentLoss,
    boolean isTraining
) {
    public static final Codec<NeuralNetworkData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.listOf().fieldOf("layer_sizes").forGetter(NeuralNetworkData::layerSizes),
            Codec.FLOAT.listOf().fieldOf("weights").forGetter(NeuralNetworkData::weights),
            Codec.INT.fieldOf("epoch").forGetter(NeuralNetworkData::currentEpoch),
            Codec.FLOAT.fieldOf("loss").forGetter(NeuralNetworkData::currentLoss),
            Codec.BOOL.fieldOf("training").forGetter(NeuralNetworkData::isTraining)
        ).apply(instance, NeuralNetworkData::new)
    );
    
    public static final StreamCodec<ByteBuf, NeuralNetworkData> STREAM_CODEC = StreamCodec.of(
        (buf, data) -> {
            buf.writeInt(data.layerSizes.size());
            for (int i : data.layerSizes) buf.writeInt(i);
            buf.writeInt(data.weights.size());
            for (float f : data.weights) buf.writeFloat(f);
            buf.writeInt(data.currentEpoch);
            buf.writeFloat(data.currentLoss);
            buf.writeBoolean(data.isTraining);
        },
        buf -> {
            int sizeCount = buf.readInt();
            List<Integer> sizes = new java.util.ArrayList<>();
            for (int i = 0; i < sizeCount; i++) sizes.add(buf.readInt());
            int weightCount = buf.readInt();
            List<Float> weights = new java.util.ArrayList<>();
            for (int i = 0; i < weightCount; i++) weights.add(buf.readFloat());
            int epoch = buf.readInt();
            float loss = buf.readFloat();
            boolean training = buf.readBoolean();
            return new NeuralNetworkData(sizes, weights, epoch, loss, training);
        }
    );
    
    public static final NeuralNetworkData DEFAULT = new NeuralNetworkData(
        List.of(2, 3, 1),
        List.of(),
        0,
        1.0f,
        false
    );
}
