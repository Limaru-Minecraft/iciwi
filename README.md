# iciwi
Universal transportation ticket plugin.

# Development
1. Use JDK 17 & Gradle 8
2. Fork or clone this project locally
3. Wait for the project to boot up; Gradle may take some time
4. Start developing!

# Terminology used in this README
**Station**: A valid station defined in Iciwi's config.
**TOC**: Train Operating Company. Iciwi is built to handle multiple competing companies, not just a single nationalised railway network (although it could do that as well). TOCs are the entities defined in Iciwi that collect the final fares from railways.

# Usage
## Fare Gates

Fare gates are the contraptions to let players in and out of a transit network. They form the bulk of the signs on a server. To use them as a player, simply right-click on their sign or block, depending on the type of gate. However, for server admins, setting up may be a bit harder.

### Flags
Flags mostly determine which block the fare gate looks at for opening or closing. Here are the flags included with Iciwi:

- V: Validator. Makes the sign into a validator. (This mode makes the sign not cause blocks to update)
- S: Sideways sign. Place this sign on the right-hand side block before the fare gate.
- L: Lefty sign. Place this sign on the LEFT of the fare gate.
- D: Double fare gate. Use 2 blocks for the fare gate.
- R: Redstone activator. Activates the lever placed below the block that the sign is attached to.
- E: Eye-level sign. Place this sign 1 block above where you would normally place it.
- F: Trapdoor fare gate. Place a trapdoor 2 blocks above this sign to use, and right-click the trapdoor to use.

**By default (i.e. no flags), Iciwi's fare gate signs are placed on the block directly to the right of a fare gate.**

Flags may be combined in many combinations to give different results. For example, the flag combination `SLDE` creates a two-block-wide fare gate where its sign is placed on the left to the side, one block above where the fare gate is.

### Sign Types
Sign types tell the sign and the player using them (broadly) what to do. An entry sign allows a player to enter the network, an exit sign allows a player to exit the network, you get the idea. Like other plugins, the name of the sign type is denoted in [square brackets] as they are programmed to perform an action.

- [Entry]: Lets the player enter the network through the defined station.
- [Exit]: Lets the player exit the network from the defined station.
- [Member]: Only lets a player pass if they have a rail pass for the TOC that operates the sign's defined station.
- [Validator]: Both an [Entry] and [Exit] sign at once. This sign DOES NOT default to not opening anything despite its name, the sign that does that requires the V flag.
- [Faregate]: Fare gate sign. A variant of the [Validator] sign that does nothing when clicked by default. Similar to the F flag.
- [Transfer]: Cuts a journey and starts a new one. Despite its name, in the code it does the opposite of what it says - it stops a transfer (explained below) from happening.

### Syntax
Each sign should follow the following syntax:
```
<Sign type><Flags>
<Station Name>
[anything]
[anything]
```
Mandatory fields are in <> while optional fields are in []. [anything] refers to literally anything.

### Using fare gates

For 'normal' signs (Entry, Exit, Member, and Validator signs without the F flag), right-clicking the sign will open the desired block.

For signs activated by other methods, players should right-click on an openable block (i.e. fence gates and trapdoors) located two blocks above the sign. The plugin will check if the sign is valid and if the player fulfils all conditions to open the gate (things like whether the player has a valid ticket or card) before opening the gate. If there is no sign or if the sign is invalid, the second check will not happen and the handling will be passed back to the server; otherwise, the gate will continue to be closed. Below is an illustration of how the setup should look like:
```
(Openable block)
(Any block)
(Sign)
```

## Ticket Machine
Ticket machines form up the other half of Iciwi. 3 types of ticket machines are installed by default, namely, the normal ticket machine, the custom ticket machine, and the rail pass machine. All ticket machines come in the form of action signs.

Syntax of all ticket machines:
```
[Tickets|CustomTickets|Passes]
<Station Name>
[anything]
[anything]
```

### Normal ticket machine
The normal ticket machine (first line [Tickets]) allows you to do everything you need with tickets. You can buy tickets for any journey from the station the ticket machine is located, or get an Iciwi card and perform related functions. Most stations should have at least one of these.

### Custom ticket machine
The custom ticket machine (first line [CustomTickets]) only allows you to buy single paper tickets. This machine predates the current ticket machine and should be found in larger stations with more rail lines to allow more players to buy paper tickets at the same time.

### Rail pass machine
The rail pass machine (first line [Passes]) allows you to only check and buy rail passes. Currently, all rail passes must be linked to a card.

## Ticket Machine Usage (Normal Ticket Machine)
Ticket machines have been made more and more self-explanatory over the years of coding. You probably already know how to use one and just testing whether I'll actually teach you how to use one using the informative README that Iciwi has. Well, bad news, I won't because I am a very busy man.

## Transferring
Exiting one station and entering another within a certain time (changeable in config) counts as a transfer. Transferring will usually lead to lower fares as the two separate journeys made with a transfer in between are counted as a single journey.

However, this may lead to errors in pricing if a journey from a single system to another is made, especially if the second journey is lower-priced than the first. In that case, place a [Transfer] sign in the middle; it cuts the first journey and starts a new fare for the second. [Transfer] signs can also be placed in the paid area, in which they act like an [Exit] followed directly by a [Transfer] outside the gate line and then an [Entry].

# Admin usage

## fares.yml
This file lists all the fares.

Format:
```yml
FromStation:
  ToStation:
    Class: price
    Class: price
    Class: price
    Class: price
  ToStation:
    Class: price
    Class: price
    Class: price
    Class: price

(And so on)
```
You need to set a class as a default class to use when someone uses an Iciwi card to tap in/out. By default, the default class is `Second` (because metros aren't first class for obvious reasons)

## owners.yml
This file determines where the money goes when someone taps in/out.

`Aliases` Determines who to pay when someone uses the /coffers commands. This list is in the form `Operator: Username`. One operator can only be assigned one username, but one username can have many operators listed under them.

`Operators` Determines which **operator** owns which station. This list is in the form `Station: Operator`. One station can only be assigned one operator.

`Coffers` **You shouldn't touch this,** but this lists how much each operator has earned since the last `/coffers empty`. If a station does not have an operator, the money earned from that station goes to the operator `'null'`.

`RailPassPrices` Prices for rail passes. This list is in the form `Operator: Days: Price`, where Days refer to the number of days of free travel until expiry.

Sample `owners.yml`:
```yml
Aliases:
  ExampleOperator: ExampleUsername
Operators:
  ExampleStation: ExampleOperator
Coffers:
  ExampleOperator: 0.0
  'null': 4.22
RailPassPrices:
  ExampleOperator:
    '7': 25.0
    '30': 100.0
```

## Commands
Iciwi uses the Cloud Command Framework for its commands. Type /iciwi to see every command added.

## Dependencies
- BKCommonLib (you should already have this installed if you have TrainCarts installed.)
