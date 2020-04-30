package mod.linguardium.pocketplants.utils;

import net.minecraft.nbt.*;

import java.util.HashMap;
import java.util.Map;

public class Nbt {
    public static Map<Class,Integer> TagTypes= new HashMap<>();

    public static void init() {
        TagTypes.put(EndTag.class, 0);
        TagTypes.put(ByteTag.class, 1);
        TagTypes.put(ShortTag.class, 2);
        TagTypes.put(IntTag.class, 3);
        TagTypes.put(LongTag.class, 4);
        TagTypes.put(FloatTag.class, 5);
        TagTypes.put(DoubleTag.class, 6);
        TagTypes.put(ByteArrayTag.class, 7);
        TagTypes.put(StringTag.class, 8);
        TagTypes.put(ListTag.class, 9);
        TagTypes.put(CompoundTag.class, 10);
        TagTypes.put(IntArrayTag.class, 11);
        TagTypes.put(LongArrayTag.class, 12);
    }
}
