package mikeshafter.iciwi.util;

import org.bukkit.command.CommandSender;

@FunctionalInterface
public interface CommandFunction<C extends CommandSender, A , N extends ArgumentNode> {
  boolean apply(C sender, A args, N node);
}