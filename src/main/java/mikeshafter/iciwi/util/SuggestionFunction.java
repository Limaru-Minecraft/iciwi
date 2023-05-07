package mikeshafter.iciwi.util;

import org.bukkit.command.CommandSender;

import java.util.List;

@Deprecated
@FunctionalInterface
public interface SuggestionFunction<C extends CommandSender, A , N extends ArgumentNode> {
  List<String> apply(C sender, A args, N node);
}