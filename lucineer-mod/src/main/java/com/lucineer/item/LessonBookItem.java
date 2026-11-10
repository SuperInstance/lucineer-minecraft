package com.lucineer.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.ArrayList;
import java.util.List;

/**
 * Lesson Book Item - AI-generated educational lessons.
 */
public class LessonBookItem extends Item {
    
    public LessonBookItem(Properties properties) {
        super(properties);
    }
    
    public List<String> getLessons(ItemStack stack) {
        List<String> lessons = new ArrayList<>();
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("lessons")) {
                ListTag list = tag.getList("lessons", 8);
                for (int i = 0; i < list.size(); i++) {
                    lessons.add(list.getString(i));
                }
            }
        }
        return lessons;
    }
    
    public void addLesson(ItemStack stack, String lessonId) {
        List<String> lessons = new ArrayList<>(getLessons(stack));
        if (!lessons.contains(lessonId)) {
            lessons.add(lessonId);
            CompoundTag tag = stack.getOrCreateTag();
            ListTag list = new ListTag();
            for (String l : lessons) {
                list.add(StringTag.valueOf(l));
            }
            tag.put("lessons", list);
        }
    }
    
    public String getCurrentLesson(ItemStack stack) {
        List<String> lessons = getLessons(stack);
        if (lessons.isEmpty()) {
            return "No lessons loaded. Use MCP server to generate new lessons.";
        }
        return lessons.get(lessons.size() - 1);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            String currentLesson = getCurrentLesson(stack);
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("📖 " + currentLesson),
                false
            );
        }
        
        return InteractionResultHolder.success(stack);
    }
}
