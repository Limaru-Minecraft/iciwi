package mikeshafter.iciwi.util;

import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class ArgumentNode {

  private final LinkedHashMap<String, ArgumentNode> children = new LinkedHashMap<>();
  private CommandFunction<CommandSender, String[], ArgumentNode> commandFunction;
  private SuggestionFunction<CommandSender, String[], ArgumentNode> suggestionFunction;
  private final String literal;
  private final String name;
  private final Class<?> type;
  private final boolean isLiteral;
  private final ArrayList<String> nameList = new ArrayList<>();
  private ArgumentNode parent = null;

  public ArgumentNode (final String literal) {
    this.literal = literal;
    this.name = literal;
    this.type = null;
    this.isLiteral = true;
  }

  public ArgumentNode (final String name, final Class<?> type) {
    this.name = name;
    this.literal = null;
    this.type = type;
    this.isLiteral = false;
  }

  public static ArgumentNode of(final String literal) {return new ArgumentNode(literal);}

  public static ArgumentNode of(final String name, final Class<?> type) {return new ArgumentNode(name, type);}

  protected ArgumentNode getThis() {return this;}

  private ArrayList<String> getNameList () {
    return this.nameList;
  }

  public ArgumentNode then (final ArgumentNode argumentNode) {
    argumentNode.setParent(this);
    if (this.isRoot()) {
      this.nameList.add(argumentNode.getName());
    } else {
      this.getParent().getNameList().add(argumentNode.getName());
    }
    children.put(argumentNode.getName(), argumentNode);
    return this;
  }

  private String getArg(String[] args, String name) {
    return args[this.nameList.indexOf(name)];
  }

  public String getString (String[] args, String name) {
    return getArg(args, name);
  }

  public int getInteger (String[] args, String name) {
    return isType(getArg(args, name), Integer.class) ? Integer.parseInt(getArg(args, name)) : 0;
  }

  public float getFloat (String[] args, String name) {
    return isType(getArg(args, name), Float.class) ? Float.parseFloat(getArg(args, name)) : 0;
  }

  public long getLong (String[] args, String name) {
    return isType(getArg(args, name), Long.class) ? Long.parseLong(getArg(args, name)) : 0;
  }

  public double getDouble (String[] args, String name) {
    return isType(getArg(args, name), Double.class) ? Double.parseDouble(getArg(args, name)) : 0;
  }

  public boolean getBoolean (String[] args, String name) {
    return isType(getArg(args, name), Boolean.class) && Boolean.parseBoolean(getArg(args, name));
  }

  private void setParent (ArgumentNode argumentNode) {
    this.parent = argumentNode;
  }

  private ArgumentNode getParent () {
    return this.parent;
  }

  private boolean isRoot () {
    return this.parent == null;
  }

  public ArgumentNode executes (CommandFunction<CommandSender, String[], ArgumentNode> commandFunction) {
    this.commandFunction = commandFunction;
    return this;
  }

  public ArgumentNode suggestions (SuggestionFunction<CommandSender, String[], ArgumentNode> suggestionFunction) {
    this.suggestionFunction = suggestionFunction;
    return this;
  }

  public ArgumentNode suggestions (List<String> suggestions) {
    this.suggestionFunction = (CommandSender c, String[] a, ArgumentNode n) -> suggestions;
    return this;
  }

  private ArgumentNode getChild(String name) {return children.get(name);}

  public boolean onCommand(CommandSender sender, String[] args) {return onCommand(sender, parseArgs(args), 0);}

  private boolean isType(String arg, Class<?> clazz) {
    Map<Class<?>, Predicate<String>> canParse = new HashMap<>(){{
      put(Integer.TYPE, s -> {try {Integer.parseInt(s); return true;} catch (Exception e) {return false;}});
      put(Long.TYPE, s -> {try {Long.parseLong(s); return true;} catch (Exception e) {return false;}});
      put(Float.TYPE, s -> {try {Float.parseFloat(s); return true;} catch (Exception e) {return false;}});
      put(Double.TYPE, s -> {try {Double.parseDouble(s); return true;} catch (Exception e) {return false;}});
      put(Boolean.TYPE, s -> s.equals("true") || s.equals("false"));
    }};
    return canParse.get(clazz).test(arg);
  }

  private boolean isType(String arg) {
    return isType(arg, this.type);
  }

  private static String[] parseArgs(String[] args) {
    boolean s = false;
    boolean d = false;
    int[] f = new int[args.length];
    int c = 0;
    for (int i = 0; i < args.length; i++) {
      if (args[i].startsWith("'") && !d) {
        s = true;
      } else if (args[i].startsWith("\"") && !s) {
        d = true;
      } else if (args[i].endsWith("'") && !d) {
        s = false;
      } else if (args[i].endsWith("\"") && !s) {
        d = false;
      }
      f[i] = c;
      if (!s && !d)
        c++;
    }
    String[] r = new String[f[f.length - 1] + 1];
    for (int i = 0; i < args.length; i++) {
      r[f[i]] = r[f[i]] == null ? args[i] : r[f[i]] + " " + args[i];
    }
    return r;
  }

  private boolean onCommand(CommandSender sender, String[] parsedArgs, int argPointer) {
    // last argument
    if (parsedArgs.length == ++argPointer) {
      return this.commandFunction.apply(sender, parsedArgs, this);
    } else {
      if (this.isLiteral)
        return this.getChild(parsedArgs[argPointer]).onCommand(sender, parsedArgs, argPointer);
      else {
        // The next argument is decided as the first instance in which the type check passes.
        String nextArg = null;
        for (var childEntry : this.getChildren().entrySet()) {
          if (childEntry.getValue().isType(parsedArgs[argPointer])) {
            nextArg = childEntry.getKey();
          }
        }
        return nextArg != null && this.getChild(nextArg).onCommand(sender, parsedArgs, argPointer);
      }
    }
  }

  public @Nullable List<String> onTabComplete(CommandSender sender, String[] args) {
    return onTabComplete(sender, parseArgs(args), 0);
  }

  private LinkedHashMap<String, ArgumentNode> getChildren () {return children;}

  private @Nullable List<String> onTabComplete(CommandSender sender, String[] parsedArgs, int argPointer) {
    if (parsedArgs.length == ++argPointer) {
      // final tab completion list
      ArrayList<String> completions = new ArrayList<>();
      // this item's suggestions
      List<String> suggestions = suggestionFunction.apply(sender, parsedArgs, this);
      // get all child literals
      this.children.keySet().forEach(item -> {
        if (this.getChild(item).isLiteral)
        suggestions.add(this.getChild(item).getLiteral());
      });
      // copy matches
      StringUtil.copyPartialMatches(parsedArgs[argPointer-1], suggestions, completions);
      // return
      return completions;
    } else {
      if (this.isLiteral)
        return this.getChild(parsedArgs[argPointer]).onTabComplete(sender, parsedArgs, argPointer);
      else {
        // The next argument is decided as the first instance in which the type check
        // passes.
        String nextArg = null;
        for (var childEntry : this.getChildren().entrySet()) {
          if (childEntry.getValue().isType(parsedArgs[argPointer])) {
            nextArg = childEntry.getKey();
          }
        }
        return nextArg == null ? null : this.getChild(nextArg).onTabComplete(sender, parsedArgs, argPointer);
      }
    }
  }

  private String getLiteral () {return isLiteral ? literal : null;}

  private String getName () {return name;}

  // private Class<?> getType () {return type;}

}
