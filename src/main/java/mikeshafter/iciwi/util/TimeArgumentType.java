package mikeshafter.iciwi.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;

public class TimeArgumentType implements CustomArgumentType<Long, String> {
    @Override public @NotNull Long parse (@NotNull StringReader reader) {
        try {
            String timeName = reader.readString();
    		String[] bits = timeName.split(":");
    		if (bits.length == 2) {
    			long hours = 1000 * (Long.parseLong(bits[0]) - 8);
    			long minutes = 1000 * Long.parseLong(bits[1]) / 60;
    			return hours + minutes;
    		}
            else {
    			return (long) ((Double.parseDouble(timeName) - 8) * 1000);
    		}
    	} catch (Exception ex) {
    		// No one uses shortcuts for durations!
    		return -1L;
    	}
    }
    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
    public static TimeArgumentType time () {
        return new TimeArgumentType();
    }
}
