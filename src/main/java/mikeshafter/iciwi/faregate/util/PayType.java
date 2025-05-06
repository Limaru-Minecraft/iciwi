package mikeshafter.iciwi.faregate.util;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.api.SignInfo;
import mikeshafter.iciwi.util.IciwiUtil;

import org.bukkit.entity.Player;

public abstract class PayType {
final Player player;
final SignInfo signInfo;

public PayType (Player player, SignInfo info) {
	this.player = player;
	this.signInfo = info;
}

/**
 Register entry

 @return Whether the operation was successful.
 */
public abstract boolean onEntry ();

/**
 Register onExit

 @return Whether the operation was successful.
 */
public abstract boolean onExit ();

/**
 Check if a card has a railpass

 @return Whether the operation was successful.
 */
public abstract boolean onMember ();

/**
 Stops and starts a journey without allowing for an OSI

 @return Whether the operation was successful.
 */
public abstract boolean onTransfer ();
}
