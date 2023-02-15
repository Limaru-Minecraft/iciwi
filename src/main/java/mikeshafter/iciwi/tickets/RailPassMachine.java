package mikeshafter.iciwi.tickets;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import static mikeshafter.iciwi.util.MachineUtil.*;

import java.util.ArrayList;
import java.util.List;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.util.Clickable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class RailPassMachine implements Machine {

  // Attributes
  private Inventory inv;
  private Clickable[] clickables;
  private ItemStack selectedItem;
  private List<String> operators;
  private final Player player;
  private boolean bottomInv;

  // Constant helper classes
  private final CardSql cardSql = new CardSql();
  private final Owners owners = new Owners();
  private final Lang lang = new Lang();
  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);

  public RailPassMachine (Player player) { 
    bottomInv = true; 
    this.player = player; 
  }

  // card selection menu. player clicks in their own inventory to select a card
  public void selectCard() 
  {
    // Setup listener for bottom inventory selection
    // Create inventory
    inv = this.plugin.getServer().createInventory(null, 9, this.lang.getComponent("select-card"));
    // Start listening and open inventory
    player.openInventory(inv);
  }

  // main menu after inserting iciwi card
  @Override public void onCardSelection() { railPass(selectedItem); }

  // rail pass menu
  public void railPass(ItemStack item) {
    // get available railpasses
    ArrayList<String> railPassNames = new ArrayList<>();
    this.operators.forEach((o) -> railPassNames.addAll(owners.getRailPassNames(o)));

    int invSize = (railPassNames.size() / 9 + 1) * 9;
    inv = plugin.getServer().createInventory(null, invSize, lang.getComponent("ticket-machine"));
    clickables = new Clickable[invSize];

    // get serial number
    String serial = parseComponent(Objects.requireNonNull(item.getItemMeta().lore()).get(1));

    // rail pass viewer
    clickables[0] = Clickable.of(makeItem(Material.WHITE_STAINED_GLASS_PANE,0, Component.text("View Rail Passes")),
        (event) -> {
          // print current rail passes
          // get current passes
          List<TextComponent> discountList = cardSql.getAllDiscounts(serial).entrySet().stream()
              .sorted(Map.Entry.comparingByValue())
              .map(railPass -> Component.text().content(
                  // Show expiry date
                  "\u00A76- \u00A7a" + railPass.getKey() + "\u00a76 | Exp. "
                      + String.format("\u00a7b%s\n", new Date(railPass.getValue() * 1000)))
                  // Option to extend (currently disabled)
                  // .append(Component.text().content("\u00a76 | Extend
                  // \u00a7a")).clickEvent(ClickEvent.runCommand("/iciwi railpass "+serial+"
                  // "+railPass.getKey()))
                  .build())
              .toList();
          // menu title
          TextComponent menu = Component.text().content("==== Rail Passes You Own ====\n")
              .color(NamedTextColor.GOLD).build();
          // build content
          for (TextComponent displayEntry : discountList)
            menu = menu.append(displayEntry);
          menu = menu.append(Component.text("\n"));
          // send to player
          player.sendMessage(menu);
        });

    // create all rail pass buttons
    for (int i = 1; i <= railPassNames.size(); i++) {
      clickables[i] = Clickable.of(makeItem(Material.LIME_STAINED_GLASS_PANE,0, Component.text(railPassNames.get(i))), (event) -> {
        String name = parseComponent(
            Objects.requireNonNull(event.getCurrentItem()).getItemMeta().displayName());
        double price = this.owners.getRailPassPrice(name);

        if (Iciwi.economy.getBalance(player) >= price) {
          // take money from player
          Iciwi.economy.withdrawPlayer(player, price);

          // check if the card already has the rail pass
          if (this.cardSql.getAllDiscounts(serial).containsKey(name))
            // Extend by the duration of the rail pass (change start time to the current
            // expiry time)
            {
            this.cardSql.setDiscount(serial, name, this.cardSql.getExpiry(serial, name));
            player.sendMessage(this.lang.getString("added-rail-pass"));
            }

          else
            // New rail pass
            {
            this.cardSql.setDiscount(serial, name, Instant.now().getEpochSecond());
            player.sendMessage(this.lang.getString("extended-rail-pass"));
            }


          // pay the TOC
          this.owners.deposit(this.owners.getRailPassOperator(name), price);
        } 
        else
          player.sendMessage(this.lang.getString("not-enough-money"));

        // close inventory
        player.closeInventory();
      });
    }

    // set items and open inventory
    inv = setItems(clickables, inv);
    player.openInventory(inv);

  }

  @Override
  public Clickable[] getClickables() { return this.clickables; }

  @Override
  public boolean useBottomInv() { return bottomInv; }

  @Override
  public void setSelectedItem(ItemStack selectedItem) { this.selectedItem = selectedItem; }

  @Override
  public ItemStack getSelectedItem() { return selectedItem; }

  // puts the items of a clickable[] into an inventory
  public Inventory setItems(Clickable[] clickables, Inventory inventory) {
    Function<Clickable[], ItemStack[]> getItems = (c) -> {
      ItemStack[] items = new ItemStack[c.length];
      for (int i = 0; i < c.length; i++)
        if (c[i] != null)
          items[i] = c[i].getItem();
      return items;
    };
    inventory.setStorageContents(getItems.apply(clickables));
    return inventory;
  }

  @Override
  public void setBottomInv(boolean b) {
    this.bottomInv = b;
  }
}
