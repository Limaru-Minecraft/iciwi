package mikeshafter.iciwi.util;

import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ArgumentNode {
  private final LinkedHashMap<String, ArgumentNode> children = new LinkedHashMap<>();
  private CommandFunction<CommandSender, String[], ArgumentNode> commandFunction = (c, a, n) -> onHelp(c, a);
  private SuggestionFunction<CommandSender, String[], ArgumentNode> suggestionFunction = (c, a, n) -> new ArrayList<>();
  private String description;
  private final String literal;
  private final String name;
  private final Class<?> type;
  private final boolean isLiteral;
  private final ArrayList<String> childrenNames = new ArrayList<>();
  private ArgumentNode parent = null;

  // CONSTRUCTION METHODS

  /**
   * Creates a literal ArgumentNode
   *
   * @param literal String to use as the identifier and value of the ArgumentNode
   */
  public ArgumentNode (final String literal) {
    this.literal = literal;
    this.name = literal;
    this.type = null;
    this.isLiteral = true;
  }

  /**
   * Creates a value ArgumentNode
   *
   * @param name Identifier of the ArgumentNode
   * @param type Type of the value
   */
  public ArgumentNode (final String name, final Class<?> type) {
    this.name = name;
    this.literal = null;
    this.type = type;
    this.isLiteral = false;
  }

  /**
   * Static constructor for literal ArgumentNodes
   *
   * @param literal String to use as the identifier and value of the ArgumentNode
   * @return the generated ArgumentNode
   */
  public static ArgumentNode of (final String literal) { return new ArgumentNode(literal); }

  /**
   * Static constructor for value ArgumentNode
   *
   * @param name Identifier of the ArgumentNode
   * @param type Type of the value
   * @return the generated ArgumentNode
   */
  public static ArgumentNode of (final String name, final Class<?> type) { return new ArgumentNode(name, type); }

  /**
   * Creates a child ArgumentNode
   *
   * @param argumentNode the child ArgumentNode
   * @return this node
   */
  public ArgumentNode then (final ArgumentNode argumentNode) {
    argumentNode.setParent(this);
    if (this.isRoot()) {
      this.childrenNames.add(argumentNode.getName());
    }
    else {
      this.getParent().getChildrenNames().add(argumentNode.getName());
    }
    children.put(argumentNode.getName(), argumentNode);
    return this;
  }

  // FUNCTIONAL METHODS

  /**
   * Sets the function to run when the command leading up to this node is executed.
   *
   * @param commandFunction Function to run when the command is executed.
   * @return this node
   */
  public ArgumentNode executes (CommandFunction<CommandSender, String[], ArgumentNode> commandFunction) {
    this.commandFunction = commandFunction;
    return this;
  }

  /**
   * Sets what will be suggested in the tab complete list when tab is pressed using a function.
   *
   * @param suggestionFunction Function (that returns List<String>) to generate the list of suggestions.
   * @return this node
   */
  public ArgumentNode suggestions (SuggestionFunction<CommandSender, String[], ArgumentNode> suggestionFunction) {
    this.suggestionFunction = suggestionFunction;
    return this;
  }

  /**
   * Sets what will be suggested in the tab complete list when tab is pressed using a function.
   *
   * @param suggestions The list of suggestions
   * @return this node
   */
  public ArgumentNode suggestions (List<String> suggestions) {
    this.suggestionFunction = (CommandSender c, String[] a, ArgumentNode n) -> suggestions;
    return this;
  }

  /**
   * Sets the description shown when the help command is used. Usage should be generated automatically.
   * 
   * @param description Description of what the full command does
   * @return this node
   */
  public ArgumentNode description (String description) {
    this.description = description;
    return this;
  }

  // EXECUTION METHODS

  /**
   * Executes when onCommand returns false, or when the help command is executed
   * 
   * @param sender Source of the command
   * @param args   Passed command arguments
   * @return this method (or its helper method) should always return true
   */

  public boolean onHelp (CommandSender sender, String[] args) {
    sender.sendMessage(helpMessage(sender, parseArgs(args), 0))
  }

  /**
   * Executes when onCommand returns false, or when the help command is executed
   * 
   * @param sender Source of the command
   * @param args   Parsed command arguments
   * @return this method (or its helper method) should always return true
   */

  public String helpMessage (CommandSender sender, String[] parsedArgs, int argPointer) {
    if (parserArgs.length == ++argPointer) {
      // while the pointer is incremented, the node has not been incremented!
      // apply the CommandFunction at the next node
      var next = this.getChild(parsedArgs[argPointer-1]);
      return description;
    }
    else {
      if (this.isLiteral) {
        for (var childKey : this.getChildren().keySet()) {
          if (childKey.startsWith(parsedArgs[argPointer - 1])) {
            return this.getChild(childKey).helpMessage(sender, parsedArgs, argPointer);
          }
        }
        return "No description/usage was provided for this command.";
      }
      else {
        // The next argument is decided as the first instance in which the type check passes.
        String nextArg = null;
        for (var childEntry : this.getChildren().entrySet()) {
          if (childEntry.getValue().isType(parsedArgs[argPointer])) {
            nextArg = childEntry.getKey();
          }
        }
        if (nextArg == null) 
          return "No description/usage was provided for this command.";
        
        return this.getChild(nextArg).helpMessage(sender, parsedArgs, argPointer);
      }
    }
    
  }

  /**
   * Executes the given command, returning its success. If false is returned, then the "usage" plugin.yml entry for this command (if defined) will be sent to the player.
   *
   * @param sender Source of the command
   * @param args   Passed command arguments
   * @return true if a valid command, otherwise false
   */
  public boolean onCommand (CommandSender sender, String[] args) {
    return onCommand(sender, parseArgs(args), 0);
  }

  /**
   * Executes the given command, returning its success. If false is returned, then the "usage" plugin.yml entry for this command (if defined) will be sent to the player.
   *
   * @param sender     Source of the command
   * @param parsedArgs Parsed command arguments
   * @param argPointer Pointer at which to parse the command
   * @return true if a valid command, otherwise false
   */
  private boolean onCommand (CommandSender sender, String[] parsedArgs, int argPointer) {
    // last argument
    if (parsedArgs.length == ++argPointer) {
      // while the pointer is incremented, the node has not been incremented!
      // apply the CommandFunction at the next node
      var next = this.getChild(parsedArgs[argPointer-1]);
      return next.commandFunction.apply(sender, parsedArgs, next);
    }
    else {
      if (this.isLiteral) {
        for (var childKey : this.getChildren().keySet()) {
          if (childKey.startsWith(parsedArgs[argPointer - 1])) {
            return this.getChild(childKey).onCommand(sender, parsedArgs, argPointer);
          }
        }
        return false;
      }
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

  /**
   * Requests a list of possible completions for a command argument.
   *
   * @param sender Source of the command. For players tab-completing a command inside a command block, this will be the player, not the command block.
   * @param args   The arguments passed to the command, including final partial argument to be completed and command label.
   * @return A List of possible completions for the final argument, or null to default to the command executor
   */
  public @Nullable List<String> onTabComplete (CommandSender sender, String[] args) {
    return onTabComplete(sender, parseArgs(args), 0);
  }

  /**
   * Requests a list of possible completions for a command argument.
   *
   * @param sender     Source of the command. For players tab-completing a command inside a command block, this will be the player, not the command block.
   * @param parsedArgs The arguments passed to the command, including final partial argument to be completed and command label, parsed using ArgumentNode#parseArgs
   * @param argPointer Pointer at which to parse the command
   * @return A List of possible completions for the final argument, or null to default to the command executor
   */
  private @Nullable List<String> onTabComplete (CommandSender sender, String[] parsedArgs, int argPointer) {
    if (parsedArgs.length == ++argPointer) {
      // final tab completion list
      ArrayList<String> completions = new ArrayList<>();
      // this item's suggestions
      List<String> suggestions = suggestionFunction.apply(sender, parsedArgs, this);
      // get all child literals
      this.children.keySet().forEach(item -> {
        if (this.getChild(item).isLiteral) suggestions.add(this.getChild(item).getLiteral());
      });
      // copy matches
      StringUtil.copyPartialMatches(parsedArgs[argPointer-1], suggestions, completions);
      // return
      return completions;
    } else {
      if (this.isLiteral) {
        for (var childKey : this.getChildren().keySet()) {
          if (childKey.startsWith(parsedArgs[argPointer-1])) {
            return this.getChild(childKey).onTabComplete(sender, parsedArgs, argPointer);
          }
        }
        return null;
      }
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

  // ARGUMENT METHODS

  /**
   * Internal generic method to get the argument from an array using its identifier
   *
   * @param args the argument array
   * @param name identifier
   * @return the argument's value
   */
  private String getArg (String[] args, String name) {
    return args[this.childrenNames.indexOf(name)];
  }

  /**
   * Gets an argument of type Integer from an array using its identifier
   *
   * @param args the argument array
   * @param name identifier
   * @return the argument's value
   */
  public int getInteger (String[] args, String name) {
    return isType(getArg(args, name), Integer.class) ? Integer.parseInt(getArg(args, name)) : 0;
  }

  /**
   * Gets an argument of type Long from an array using its identifier
   *
   * @param name identifier
   * @param args the argument array
   * @return the argument's value
   */
  public long getLong (String[] args, String name) {
    return isType(getArg(args, name), Long.class) ? Long.parseLong(getArg(args, name)) : 0;
  }

  /**
   * Gets an argument of type Float from an array using its identifier
   *
   * @param args the argument array
   * @param name identifier
   * @return the argument's value
   */
  public float getFloat (String[] args, String name) {
    return isType(getArg(args, name), Float.class) ? Float.parseFloat(getArg(args, name)) : 0;
  }

  /**
   * Gets an argument of type Double from an array using its identifier
   *
   * @param args the argument array
   * @param name identifier
   * @return the argument's value
   */
  public double getDouble (String[] args, String name) {
    return isType(getArg(args, name), Double.class) ? Double.parseDouble(getArg(args, name)) : 0;
  }

  /**
   * Gets an argument of type Boolean from an array using its identifier
   *
   * @param args the argument array
   * @param name identifier
   * @return the argument's value
   */
  public boolean getBoolean (String[] args, String name) {
    return isType(getArg(args, name), Boolean.class) && Boolean.parseBoolean(getArg(args, name));
  }

  /**
   * Gets an argument of type String from an array using its identifier
   * @param args the argument array
   * @param name identifier
   * @return the argument's value
   */
  public String getString (String[] args, String name) {
    return getArg(args, name);
  }

  // PRIVATE METHODS

  /**
   * Parses arguments sent by the player
   *
   * @param args Arguments
   * @return Parsed arguments
   */
  private String[] parseArgs (String[] args) {
    boolean s = false;
    boolean d = false;
    int[] f = new int[args.length];
    int c = 0;
    for (int i = 0; i < args.length; i++) {
      if (args[i].startsWith("'") && !d) {
        s = true;
      }
      else if (args[i].startsWith("\"") && !s) {
        d = true;
      }
      else if (args[i].endsWith("'") && !d) {
        s = false;
      }
      else if (args[i].endsWith("\"") && !s) {
        d = false;
      }
      f[i] = c;
      if (!s && !d) {
        c++;
      }
    }
    String[] r = new String[f[f.length - 1] + 1];
    for (int i = 0; i < args.length; i++) {
      r[f[i]] = r[f[i]] == null ? args[i] : r[f[i]] + " " + args[i];
    }
    return r;
  }

  /**
   * Gets the identifier of this node
   *
   * @return The identifier
   */
  private String getName () { return name; }

  /**
   * Gets the literal of this node, if it exists
   * @return The literal if it exists, otherwise null.
   */
  private String getLiteral () { return isLiteral ? literal : null; }

  /**
   * Gets the names of the Node's children
   *
   * @return names of the Node's children
   */
  private ArrayList<String> getChildrenNames () { return this.childrenNames; }

  /**
   * Gets whether a node is the top (root) node.
   * More formally, returns true when this node's parent is null.
   *
   * @return whether this node is a root node
   */
  private boolean isRoot () { return this.parent == null; }

  /**
   * Gets a node's parent node
   *
   * @return Parent node
   */
  private ArgumentNode getParent () { return this.parent; }

  /**
   * Sets a node's parent node.
   *
   * @param argumentNode Parent node
   */
  private void setParent (ArgumentNode argumentNode) { this.parent = argumentNode; }

  /**
   * Checks if an argument is of this node's type.
   *
   * @param arg the argument
   * @return True if the argument is of this node's type, false otherwise.
   */
  private boolean isType (String arg) { return isType(arg, this.type); }

  /**
   * Checks if an argument is of type clazz. Can only check primitives.
   *
   * @param arg   the argument
   * @param clazz the type
   * @return True if the argument is of type clazz, false otherwise.
   */
  private boolean isType (String arg, Class<?> clazz) {
    if (clazz == Integer.TYPE) {
      try {
        Integer.parseInt(arg);
        return true;
      } catch (Exception ignored) {
        return false;
      }
    }
    else if (clazz == Long.TYPE) {
      try {
        Long.parseLong(arg);
        return true;
      } catch (Exception ignored) {
        return false;
      }
    }
    else if (clazz == Float.TYPE) {
      try {
        Float.parseFloat(arg);
        return true;
      } catch (Exception ignored) {
        return false;
      }
    }
    else if (clazz == Double.TYPE) {
      try {
        Double.parseDouble(arg);
        return true;
      } catch (Exception ignored) {
        return false;
      }
    }
    else if (clazz == Boolean.TYPE) {
      return arg.equals("true") || arg.equals("false");
    }
    else return this.type == String.class;
  }

  /**
   * Gets a child using its identifier
   *
   * @param name the child's identifier
   * @return the child node
   */
  private ArgumentNode getChild (String name) { return children.get(name); }

  /**
   * Gets the children's identifiers and their values
   *
   * @return A LinkedHashMap of format (identifier: node)
   */
  private LinkedHashMap<String, ArgumentNode> getChildren () { return children; }

}
